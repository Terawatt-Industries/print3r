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
package com.longevitysoft.java.bigslice;

/**
 * @author fbeachler
 *
 */
public final class Constants {
	
	public static final String BLANK = "";
	public static final String SPACE = " ";
	public static final String UNDERSCORE = "_";
	public static final String DOUBLESCORE = "__";
	public static final String DOT = ".";
	public static final String SLASH = "/";
	public static final String CURRENT_DIR = DOT + SLASH;
	public static final String PARENT_DIR = ".." + SLASH;
	public static final String NEWLINE = "\n";

	public static final String SCHEMA_FILE = "file://";
	public static final String SCHEMA_VM = "vm://";
	public static final String SCHEMA_URN = "urn://";
	public static final String SCHEMA_HTTP = "http://";
	public static final String SCHEMA_HTTPS = "https://";

	public static final String EP_NAME_JMS_QUEUE_PENDINGSTL = "jms:queue:pendingstl";
	public static final String EP_NAME_JMS_QUEUE_MARSHALLSTL = "jms:queue:marshallstl";

	public static final String MSG_HEADER_SLICER_CONFIG_PATH = "CamelSlicerConfigPath";
	public static final String MSG_HEADER_OUTPUT_PATH = "CamelOutputPath";
	public static final String MSG_HEADER_SLICE_CONFIG_ARRAY_LIST = "CamelSliceConfigList";
	public static final String MSG_HEADER_CAMEL_FILE_ABSOLUTE_PATH = "CamelFileAbsolutePath";
	public static final String MSG_HEADER_CAMEL_FILE_NAME = "CamelFileName";

	public static final String SLIC3R_PARAM_VAL_INPUT_FILENAME_BASE = "[input_filename_base]";
	public static final String SLIC3R_PARAM_NAME_LOAD = "--load";
	public static final String SLIC3R_CLI_PARAM_OUTPUT_FILENAME_FORMAT = "--output-filename-format=";

	public static final String FILEPATH_CONFIG_DIRNAME = "configs";
	public static final String FILEPATH_DONE = "done";
	public static final String FILEPATH_STLDROP = "stldrop";
	public static final String FILEPATH_GCODEOUT = "out";
	public static final String FILEPATH_OUT_DEFAULT = "default";

	public static final String STREAM_NAME_OUTPUT = "OUTPUT";
	public static final String STREAM_NAME_ERROR = "ERROR";

	public static final String EXT_GCODE = ".gcode";
	public static final String TILDE_BANG_TILDE = "~!~";
}
