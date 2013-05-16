/*
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
package com.longevitysoft.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RuntimeExec {
	public StreamWrapper getStreamWrapper(InputStream is, String type) {
		return new StreamWrapper(is, type);
	}

	public class StreamWrapper extends Thread {
		InputStream is = null;
		String type = null;
		String message = null;

		public String getMessage() {
			return message;
		}

		StreamWrapper(InputStream is, String type) {
			this.is = is;
			this.type = type;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuffer buffer = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					buffer.append(line);// .append("\n");
				}
				message = buffer.toString();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}