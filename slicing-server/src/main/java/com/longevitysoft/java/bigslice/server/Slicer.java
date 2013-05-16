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

import org.apache.camel.Header;

import com.longevitysoft.java.bigslice.Constants;

public interface Slicer {

	/**
	 * Multiplies the given number by a pre-defined constant.
	 * 
	 * @param originalNumber
	 *            The number to be multiplied
	 * @return The result of the multiplication
	 */
	void slice(@Header(value = Constants.MSG_HEADER_SLICE_CONFIG_ARRAY_LIST) String configList,
			@Header(value = Constants.MSG_HEADER_CAMEL_FILE_ABSOLUTE_PATH) String filePath,
			@Header(value = Constants.MSG_HEADER_OUTPUT_PATH) String headerOutputFilename);

}
