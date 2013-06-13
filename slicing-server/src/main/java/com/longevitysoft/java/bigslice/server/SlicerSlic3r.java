/**
    BigSlice Slicing Framework by Longevity Software LLC d.b.a. Terawatt Industries
    is licensed under a Creative Commons Attribution-NonCommercial 3.0 Unported License.
    Based on a work at https://github.com/Terawatt-Industries/bigslice.
    Permissions beyond the scope of this license may be available at http://terawattindustries.com.

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.longevitysoft.java.bigslice.server;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.longevitysoft.java.RuntimeExec;
import com.longevitysoft.java.RuntimeExec.StreamWrapper;
import com.longevitysoft.java.bigslice.Constants;

/**
 * @author fbeachler
 * 
 */
@Service(value = "slic3r")
public class SlicerSlic3r implements CamelContextAware, InitializingBean,
		Slicer {

	private static final transient Logger LOG = LoggerFactory
			.getLogger(SlicerSlic3r.class);

	/**
	 * Camel context set by Spring
	 */
	private CamelContext camel;

	/**
	 * path to slic3r executable
	 */
	@Value("#{T(java.util.Arrays).asList(runtimeProps['slicer.exec.path'].split(';'))}")
	private List<String> pathToExecutable;

	/**
	 * Alternate names for slic3r execs. If none provided, the full slc3r exec
	 * path is used (FIXME).
	 */
	@Value("#{T(java.util.Arrays).asList(runtimeProps['slicer.exec.outname.filter'].split(';'))}")
	private List<String> execOutputFilenameFilter;

	/**
	 * path to slic3r executable
	 */
	@Value("#{runtimeProps['app.data.path']}")
	private String baseDataPath;

	/**
	 * Thread manager.
	 */
	private ExecutorService executorService;

	@Value("#{runtimeProps['app.bgthread.size']}")
	private int executorThreadSize;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.longevitysoft.java.bigslice.server.Slicer#slice(java.lang.String)
	 */
	@Override
	public void slice(
			@Header(value = Constants.MSG_HEADER_SLICE_CONFIG_ARRAY_LIST) String configList,
			@Header(value = Constants.MSG_HEADER_CAMEL_FILE_ABSOLUTE_PATH) String filePath,
			@Header(value = Constants.MSG_HEADER_OUTPUT_PATH) String headerOutputFilename) {
		Endpoint epSlice = camel
				.getEndpoint(Constants.EP_NAME_JMS_QUEUE_PENDINGSTL);
		Exchange inEx = epSlice.createExchange(ExchangePattern.InOnly);
		// check if output filename header present
		StringBuilder configFileParam = new StringBuilder();
		if (null != configList) {
			configList = configList.trim();
			String[] configs = configList.split(",");
			LOG.debug("configs received in msg header");
			configFileParam.append(Constants.SPACE);
			for (String configName : configs) {
				configFileParam.append(Constants.SLIC3R_PARAM_NAME_LOAD)
						.append(Constants.SPACE).append(configName);
			}
		}
		// check if output filename header present
		try {
			StringBuilder gcodeOutputFilename = new StringBuilder().append(
					Constants.PARENT_DIR).append(Constants.FILEPATH_GCODEOUT); // slic3r
																				// uses
																				// STL
																				// folder
																				// by
																				// default
			if (null != headerOutputFilename) {
				gcodeOutputFilename.append(Constants.SLASH).append(
						headerOutputFilename.replace(
								Constants.TILDE_BANG_TILDE,
								Constants.SLIC3R_PARAM_VAL_INPUT_FILENAME_BASE
										+ Constants.UNDERSCORE));
				// init directory-tree in output filename
				int lastSlash = headerOutputFilename.lastIndexOf("/");
				if (0 < lastSlash) {
					String outputTree = headerOutputFilename.substring(0,
							lastSlash);
					outputTree = outputTree.replace("/./", "/");
					File outDir = new File(buildOutputPath() + Constants.SLASH
							+ outputTree);
					outDir.mkdirs();
				}
			} else {
				gcodeOutputFilename.append(Constants.SLASH).append(
						Constants.SLIC3R_PARAM_VAL_INPUT_FILENAME_BASE);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-dd-MM__HH-mm-ss");
				gcodeOutputFilename.append("_TS").append(
						sdf.format(Calendar.getInstance().getTime()));
			}
			if (null == pathToExecutable) {
				throw new InterruptedException("no exec path found");
			}
			for (int i = 0; i < pathToExecutable.size(); i++) {
				String execPath = pathToExecutable.get(i);
				// inject executable name into gcode output filename
				String insToken = null;
				int insertPos = gcodeOutputFilename
						.indexOf(Constants.SLIC3R_PARAM_VAL_INPUT_FILENAME_BASE);
				if (0 < insertPos) {
					insertPos += Constants.SLIC3R_PARAM_VAL_INPUT_FILENAME_BASE
							.length();
					if (null != execOutputFilenameFilter) {
						insToken = execOutputFilenameFilter.get(i);
					}
					if (null == insToken) {
						insToken = sanitizeFilename(execPath);
					}
					insToken = Constants.UNDERSCORE + insToken;
				}
				// build exec string
				final StringBuilder execStr = new StringBuilder(execPath)
						.append(configFileParam)
						.append(Constants.SPACE)
						.append(Constants.SLIC3R_CLI_PARAM_OUTPUT_FILENAME_FORMAT)
						.append(gcodeOutputFilename.substring(0, insertPos))
						.append(insToken)
						.append(gcodeOutputFilename.substring(insertPos,
								gcodeOutputFilename.length()))
						.append(Constants.EXT_GCODE).append(Constants.SPACE)
						.append(filePath);
				LOG.debug("executing-slic3r: " + execStr + Constants.NEWLINE);
				final String fPath = filePath;
				final StringBuilder gcodeOutFName = gcodeOutputFilename;
				executorService.execute(new Runnable() {
					@Override
					public void run() {
						try {
							Runtime rt = Runtime.getRuntime();
							RuntimeExec rte = new RuntimeExec();
							StreamWrapper error, output;
							Process proc = rt.exec(execStr.toString());
							error = rte.getStreamWrapper(proc.getErrorStream(),
									Constants.STREAM_NAME_ERROR);
							output = rte.getStreamWrapper(
									proc.getInputStream(),
									Constants.STREAM_NAME_OUTPUT);
							int exitVal = 0;
							error.start();
							output.start();
							error.join(3000);
							output.join(3000);
							exitVal = proc.waitFor();
							// TODO process exitVal for caller - decide what to
							// do in
							// http://camel.apache.org/exception-clause.html
							LOG.info(new StringBuilder()
									.append("stl-file-path: ").append(fPath)
									.append(", output-file-path:")
									.append(gcodeOutFName)
									.append(Constants.NEWLINE)
									.append(", proc-output: ")
									.append(output.getMessage())
									.append(Constants.NEWLINE)
									.append(", proc-error: ")
									.append(error.getMessage()).toString());
						} catch (Exception e) {
							LOG.trace(e.toString());
						}
					}
				});
			}
		} catch (InterruptedException e) {
			LOG.trace(e.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// exec process with buffered error and std out
		if (255 < executorThreadSize || 1 > executorThreadSize) {
			executorThreadSize = 1;
		}
		executorService = Executors.newFixedThreadPool(executorThreadSize); /*
															 * new
															 * ThreadPoolExecutor
															 * ( 1, // core //
															 * thread // pool //
															 * size 2, //
															 * maximum thread
															 * pool size 1, //
															 * time to wait
															 * before resizing
															 * pool
															 * TimeUnit.MINUTES,
															 * new
															 * ArrayBlockingQueue
															 * <Runnable>(3,
															 * true), new
															 * ThreadPoolExecutor
															 * .
															 * CallerRunsPolicy(
															 * ));
															 */
	}

	/**
	 * @return
	 */
	public String buildOutputPath() {
		return Constants.CURRENT_DIR + baseDataPath + Constants.SLASH
				+ Constants.FILEPATH_GCODEOUT;
	}

	/**
	 * Replaces undesirable characters in a filename string with _ (underscore).
	 * 
	 * @param in
	 * @return
	 */
	protected String sanitizeFilename(String in) {
		String ret = in;
		ret = ret.replaceAll("[\\\\.;,/]", "_");
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.camel.CamelContextAware#setCamelContext(org.apache.camel.
	 * CamelContext)
	 */
	@Override
	public void setCamelContext(CamelContext camelContext) {
		this.camel = camelContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.camel.CamelContextAware#getCamelContext()
	 */
	@Override
	public CamelContext getCamelContext() {
		return this.camel;
	}

}