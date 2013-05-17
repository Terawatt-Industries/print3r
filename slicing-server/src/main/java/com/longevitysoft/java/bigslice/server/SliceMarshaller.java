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
import java.util.ArrayList;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Header;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.longevitysoft.java.bigslice.Constants;
import com.longevitysoft.java.bigslice.model.Slic3rConfig;
import com.longevitysoft.java.bigslice.model.Slic3rConfig_099;

/**
 * @author fbeachler
 * 
 */
@Service(value = "stlMarshaller")
public class SliceMarshaller implements CamelContextAware, InitializingBean {

	private static final transient Logger LOG = LoggerFactory
			.getLogger(SliceMarshaller.class);

	/**
	 * Camel context set by Spring
	 */
	private CamelContext camel;

	/**
	 * scanned files from config dir
	 */
	private ArrayList<Slic3rConfig> cachedConfigs;

	/**
	 * path to slic3r executable
	 */
	@Value("#{runtimeProps['app.data.path']}")
	private String baseDataPath;

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.longevitysoft.java.bigslice.server.Slicer#slice(java.lang.String)
	 */
	public void marshall(
			@Body byte[] body,
			@Header(value = Constants.MSG_HEADER_CAMEL_FILE_NAME) String fileName,
			@Header(value = Constants.MSG_HEADER_CAMEL_FILE_ABSOLUTE_PATH) String filePath) {
		Endpoint epMarshall = camel
				.getEndpoint(Constants.EP_NAME_JMS_QUEUE_MARSHALLSTL);
		Endpoint epSlice = camel
				.getEndpoint(Constants.EP_NAME_JMS_QUEUE_PENDINGSTL);
		Exchange inEx = epMarshall.createExchange(ExchangePattern.InOnly);
		ArrayList<Exchange> outEx = new ArrayList<Exchange>();
		String filePathOut = null;
		// check if config header present
		if (null == inEx.getIn().getHeader(
				Constants.MSG_HEADER_SLICER_CONFIG_PATH)) {
			// parse config filepath to set filename
			// loop through config files and setup exchanges
			for (Slic3rConfig config : cachedConfigs) {
				filePathOut = extractOutputFilenameFromPath(config
						.getConfigPath().replace(
								buildScanPath() + Constants.SLASH, ""));
				Exchange ex = epSlice.createExchange(ExchangePattern.InOut);
				Message msg = new DefaultMessage();
				msg.setBody(inEx.getOut().getBody());
				// preserve any existing (non file) headers, before we
				// re-populate headers
				if (inEx.getOut().hasHeaders()) {
					msg.setHeaders(inEx.getOut().getHeaders());
					// remove any file related headers, as we will re-populate
					// file headers
					msg.removeHeaders("CamelFile*");
				}
				String configFPath = config.getConfigPath();
				msg.setHeader(Constants.MSG_HEADER_CAMEL_FILE_ABSOLUTE_PATH,
						filePath.replace(Constants.FILEPATH_STLDROP,
								Constants.FILEPATH_DONE));
				msg.setHeader(Constants.MSG_HEADER_SLICE_CONFIG_ARRAY_LIST,
						configFPath);
				msg.setHeader(Constants.MSG_HEADER_OUTPUT_PATH, filePathOut);
				ex.setIn(msg);
				outEx.add(ex);
			}
		} else {
			Exchange ex = epSlice.createExchange(ExchangePattern.InOut);
			Message msg = new DefaultMessage();
			msg.setBody(inEx.getOut().getBody());
			String configFilePath = (String) inEx.getIn().getHeader(
					Constants.MSG_HEADER_SLICER_CONFIG_PATH);
			// FIXME non-secure if msgs come from untrusted sources
			// parse config filepath to set filename
			filePathOut = extractOutputFilenameFromPath(configFilePath)
					.replace(buildScanPath() + Constants.SLASH, "");
			// preserve any existing (non file) headers, before we
			// re-populate headers
			if (inEx.getOut().hasHeaders()) {
				msg.setHeaders(inEx.getOut().getHeaders());
				// remove any file related headers, as we will re-populate
				// file headers
				msg.removeHeaders("CamelFile*");
			}
			msg.setHeader(Constants.MSG_HEADER_CAMEL_FILE_ABSOLUTE_PATH,
					filePath.replace(Constants.FILEPATH_STLDROP,
							Constants.FILEPATH_DONE));
			msg.setHeader(Constants.MSG_HEADER_SLICE_CONFIG_ARRAY_LIST,
					configFilePath);
			msg.setHeader(Constants.MSG_HEADER_OUTPUT_PATH, filePathOut);
			ex.setIn(msg);
			outEx.add(ex);
		}
		if (null != outEx && outEx.size() > 0) {
			// send exchange(s) to slicer endpoint
			Producer producer;
			try {
				producer = epSlice.createProducer();
				producer.start();
				for (int i = 0; i < outEx.size(); i++) {
					producer.process(outEx.get(i));
				}
				producer.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// store pathnames of slicer config files
		cachedConfigs = new ArrayList<Slic3rConfig>();
		ArrayList<String> configPaths = scanDir(buildScanPath(), new File(
				buildScanPath()));
		if (null != configPaths && configPaths.size() > 0) {
			for (int i = 0; i < configPaths.size(); i++) {
				cachedConfigs.add(new Slic3rConfig_099());
				cachedConfigs.get(i).setConfigPath(configPaths.get(i));
				LOG.info(new StringBuilder().append("config file ")
						.append(cachedConfigs.get(i).getConfigPath())
						.append(" stored.").toString());
			}
		} else {
			// TODO: it would be nice to make this a warning and build-in a
			// default slicing profile
			LOG.error(new StringBuilder()
					.append("0 config files found in ")
					.append(buildScanPath())
					.append(", nothing will get sliced unless you pass a config filepath (header) to the marshaller endpoint!")
					.toString());
		}
	}

	public String buildScanPath() {
		return Constants.CURRENT_DIR + baseDataPath + Constants.SLASH
				+ Constants.FILEPATH_CONFIG_DIRNAME;
	}

	/**
	 * @param spcs
	 * @param aFile
	 * @return
	 */
	public ArrayList<String> scanDir(String dir, File aFile) {
		ArrayList<String> ret = new ArrayList<String>();
		if (aFile.isFile()) {
			String name = aFile.getPath();
			if (null == name) {
				name = aFile.getName();
			}
			if (name.contains(".ini")) {
				ret.add(name);
			}
		} else if (aFile.isDirectory()) {
			File[] listOfFiles = aFile.listFiles();
			if (listOfFiles != null) {
				for (int i = 0; i < listOfFiles.length; i++)
					ret.addAll(scanDir(aFile.getPath(), listOfFiles[i]));
			}
		}
		return ret;
	}

	/**
	 * Extracts a gcode output filename from a qualified path.
	 * 
	 * @param path
	 * @return
	 */
	public String extractOutputFilenameFromPath(final String path) {
		StringBuilder ret = new StringBuilder();
		String p2 = path.replace(".ini", Constants.BLANK);
		// first parse out / identifier
		String[] pathParts = StringUtils.splitByWholeSeparator(p2,
				Constants.SLASH);
		if (pathParts != null && pathParts.length > 1) {
			p2 = pathParts[pathParts.length - 1];
			pathParts[pathParts.length - 1] = "";
			ret.append(StringUtils.join(pathParts, Constants.SLASH)).append(
					Constants.SLASH);
		}
		// next strip out the __ identifier in config filename
		String[] nameParts = StringUtils.splitByWholeSeparator(p2,
				Constants.DOUBLESCORE);
		if (nameParts.length > 0) {
			String lastPart = nameParts[nameParts.length - 1];
			nameParts[nameParts.length - 1] = "";
			ret.append(StringUtils.join(nameParts, Constants.SLASH))
					.append(Constants.TILDE_BANG_TILDE).append(lastPart);
		}
		return ret.toString();
	}
}