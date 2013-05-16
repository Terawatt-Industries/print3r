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

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;

import com.longevitysoft.java.bigslice.Constants;

/**
 * This class defines the routes on the Server. The class extends a base class
 * in Camel {@link RouteBuilder} that can be used to easily setup the routes in
 * the configure() method.
 */
public class ServerRoutes extends RouteBuilder {

	/**
	 * path to slic3r executable
	 */
	@Value("#{runtimeProps['app.data.path']}")
	private String baseDataPath;

	@Override
	public void configure() throws Exception {
		from(
				Constants.SCHEMA_FILE + baseDataPath + Constants.SLASH
						+ Constants.FILEPATH_STLDROP
						+ "?initialDelay=1000&delay=5000&move="
						+ Constants.PARENT_DIR + Constants.FILEPATH_DONE + Constants.SLASH
						+ "${file:name}").to(
				Constants.EP_NAME_JMS_QUEUE_MARSHALLSTL);
		from(Constants.EP_NAME_JMS_QUEUE_MARSHALLSTL).to(
				"bean:stlMarshaller?method=marshall");
		from(Constants.EP_NAME_JMS_QUEUE_PENDINGSTL).to(
				"bean:slic3r?method=slice");
	}

}
