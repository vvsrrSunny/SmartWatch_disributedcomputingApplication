package files;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationDataPointing {
	
	public static Map<String, String> location() throws Exception {
		List<String> locationposition = Collections.emptyList();
		Map<String, String> mymap = new HashMap<String, String>();
		 String path = "C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\location_file.txt";
		 try {
//			linesAQI = Files.readAllLines(Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\" + username + "AQI.txt"),
//					StandardCharsets.UTF_8);
			 locationposition = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			 for(int i=0;i<locationposition.size();i++) {
				 System.out.println(locationposition.get(i));
				 String str = locationposition.get(i); 
				 if(str.indexOf("Indoor")>-1) {
					 String strFind = ",";
					 int fromIndex = 0;
					 while ((fromIndex = str.indexOf(strFind, fromIndex)) != -1 ){
						 
						 mymap.put(Character.toString(str.charAt(fromIndex-1)), "Indoor");
				            
				            fromIndex++;
				            
				        }
				 }
				 if(str.indexOf("Outdoor")>-1) {
					 String strFind = ",";
					 int fromIndex = 0;
					 while ((fromIndex = str.indexOf(strFind, fromIndex)) != -1 ){
						 
						 mymap.put(Character.toString(str.charAt(fromIndex-1)), "Outdoor");
				            
				            fromIndex++;
				            
				        }
				 }
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mymap;

	}

}