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
package com.longevitysoft.java.util;

/**
 * @author fbeachler
 *
 */
public class ArrayUtil {

	protected static final String COMMA = ",";

	/**
	 * Add an element to an array.
	 * 
	 * @param array
	 * @param s
	 * @return
	 */
	public static String[] addToArray(String[] array, String s) {
		String[] ans = null;
		if (0 == array.length) {
			ans = new String[1];
		} else {
			ans = new String[array.length + 1];
		}
		System.arraycopy(array, 0, ans, 0, array.length);
		ans[ans.length - 1] = s;
		return ans;
	}

	/**
	 * Add an element to an array.
	 * 
	 * @param array
	 * @param val
	 * @return
	 */
	public static int[] addToArray(int[] array, int val) {
		int[] ans = null;
		if (0 == array.length) {
			ans = new int[1];
		} else {
			ans = new int[array.length + 1];
		}
		System.arraycopy(array, 0, ans, 0, array.length);
		ans[ans.length - 1] = val;
		return ans;
	}

	/**
	 * Pop the last element off the array.
	 * 
	 * @param array
	 * @param val
	 * @return an empty array if the array was blank, otherwise the array with
	 *         the last element removed.
	 */
	public static String[] popFromArray(String[] array) {
		String[] ans = null;
		if (0 == array.length) {
			ans = new String[0];
		} else {
			ans = new String[array.length - 1];
			System.arraycopy(array, 0, ans, 0, array.length - 1);
		}
		return ans;
	}

	/**
	 * Convenience method for {@link this#implodeArray(Object[], String)} which
	 * uses a {@link Constants#COMMA} delimiter.
	 * 
	 */
	public static StringBuilder implodeArray(Object[] array) {
		if (null == array) {
			return null;
		}
		return implodeArray(array, COMMA);
	}

	/**
	 * Implode an object array.
	 * 
	 * @param array
	 * @param delim
	 * @return
	 */
	public static StringBuilder implodeArray(Object[] array, String delim) {
		if (null == array) {
			return null;
		}
		StringBuilder exploded = new StringBuilder();
		if (0 != array.length) {
			exploded.append(array[0].toString());
			for (int i = 1; i < array.length; i++) {
				exploded.append(delim).append(array[i]);
			}
		}
		return exploded;
	}

	/**
	 * Convenience method for {@link this#implodeArray(int[], String)} which
	 * uses a {@link Constants#COMMA} delimiter.
	 * 
	 */
	public static StringBuilder implodeArray(int[] array) {
		if (null == array) {
			return null;
		}
		return implodeArray(array, COMMA);
	}

	/**
	 * Implode an int array.
	 * 
	 * @param array
	 * @param delim
	 * @return
	 */
	public static StringBuilder implodeArray(int[] array, String delim) {
		if (null == array) {
			return null;
		}
		StringBuilder exploded = new StringBuilder();
		if (0 != array.length) {
			exploded.append(Integer.toString(array[0]));
			for (int i = 1; i < array.length; i++) {
				exploded.append(delim).append(array[i]);
			}
		}
		return exploded;
	}
}
