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
package com.longevitysoft.java.bigslice.model;

/**
 * @author fbeachler
 *
 * TODO extract base class
 */
public class Slic3rConfig_099 implements Slic3rConfig {

	private String configPath;

	public String getCmdLineArgs() {
		return "";
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

}
