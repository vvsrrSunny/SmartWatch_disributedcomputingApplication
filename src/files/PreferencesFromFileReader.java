package files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;


public class PreferencesFromFileReader {

	static List<String> locationposition = Collections.emptyList();
     
	
	public String getPteference(String username, String preferenceType) throws Exception {

		String path = "C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\preference_file";
		String output = "null";
		try {
//			linesAQI = Files.readAllLines(Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\" + username + "AQI.txt"),
//					StandardCharsets.UTF_8);
			locationposition = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			for (int i = 0; i < locationposition.size(); i++) {
				// System.out.println(locationposition.get(i));
				if (containsIgnoreCase(locationposition.get(i), username)) {
					// System.out.println(locationposition.get(i));
					if (containsIgnoreCase(preferenceType, "medical")) {
						int beginIndex = locationposition.get(i + 1).indexOf(":");
						beginIndex = beginIndex + 2;
						int endIndex = locationposition.get(i + 1).length();
						output = locationposition.get(i + 1).substring(beginIndex, endIndex);
						output.strip();
						return output;
						
					}
					if (containsIgnoreCase(preferenceType, "Temp")) {
//						
						//System.out.println(outputProvider(i, 2));
						return outputProvider(i, 2);
					}
					if (containsIgnoreCase(preferenceType, "APO")) {
						//System.out.println(outputProvider(i, 3));
						return outputProvider(i, 3);
					}
					if (containsIgnoreCase(preferenceType, "weather")) {
						//System.out.println(outputProvider(i, 4));
						return outputProvider(i, 4);
					}
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;

	}

	static String outputProvider(int indexOfUserPlace, int indexOfPreference) {
		String result = null;
		int beginIndex = locationposition.get(indexOfUserPlace + indexOfPreference).indexOf(":");
		beginIndex = beginIndex + 2;
		int endIndex = locationposition.get(indexOfUserPlace + indexOfPreference).indexOf("//");
		result = locationposition.get(indexOfUserPlace + indexOfPreference).substring(beginIndex, endIndex);
		result.strip();

		return result;

	}

	public static boolean containsIgnoreCase(String str, String subString) {
		return str.toLowerCase().contains(subString.toLowerCase());
	}

}