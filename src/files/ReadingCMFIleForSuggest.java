package files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadingCMFIleForSuggest {
 // this calss takes servesis "pool", and locations in string "A B" and provides all the recommended cities
	public List<String> readingSuggestionsMethod(String recomended, String input) throws IOException {
		//String input = "A B C";
		//String recomended = "pool";
		
		String[] convertedRankArray = input.split(" ");
		List<String> convertedRankList = new ArrayList<String>();
		for (String str : convertedRankArray) {
			convertedRankList.add(str.trim());
		}
		// this is to print all the indoor locations.
//		for (String str : convertedRankList) {
//			System.out.println(str);
//		}
		List<String> CMFile = Collections.emptyList();
		List<String> output = new ArrayList<String>();
		CMFile = Files.readAllLines(Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\cm_file.txt"),
				StandardCharsets.UTF_8);
		String name = null;
		boolean locationFlag = false;
		boolean servicesFlag = false;
		for (int i = 0; i < CMFile.size(); i++) {
			// this to print all the lines in the file.
			//System.out.println(CMFile.get(i));
			String currentLine = CMFile.get(i);
			if (currentLine.indexOf("name: ") > -1) {
				name = currentLine.substring(currentLine.indexOf(":") + 2, currentLine.length());
				locationFlag = false;
				servicesFlag = false;
			}

			for (String location : convertedRankList) {
				if (currentLine.indexOf("location: " + location) > -1) {
					System.out.println(currentLine + "found here");
					locationFlag = true;

				}
			}
			if (currentLine.indexOf("services:") > -1) {
				if (currentLine.indexOf(recomended) > -1) {
					servicesFlag = true;
				}
				if (locationFlag && servicesFlag) {
					output.add(name);
				}
			}

		}
		for (String string : output) {
			System.out.println(string);
		}
		return output;
	}
	
	
	public List<String> readingItemsFromCurrentLocationMethod( String input) throws IOException {
		//String input = "A B C";
		//String recomended = "pool";
		
		String[] convertedRankArray = input.split(" ");
		List<String> convertedRankList = new ArrayList<String>();
		for (String str : convertedRankArray) {
			convertedRankList.add(str.trim());
		}
		// this is to print all the indoor locations.
//		for (String str : convertedRankList) {
//			System.out.println(str);
//		}
		List<String> CMFile = Collections.emptyList();
		List<String> output = new ArrayList<String>();
		CMFile = Files.readAllLines(Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\cm_file.txt"),
				StandardCharsets.UTF_8);
		String name = null;
		boolean locationFlag = false;
		boolean servicesFlag = false;
		for (int i = 0; i < CMFile.size(); i++) {
			// this to print all the lines in the file.
			//System.out.println(CMFile.get(i));
			String currentLine = CMFile.get(i);
			if (currentLine.indexOf("name: ") > -1) {
				name = currentLine.substring(currentLine.indexOf(":") + 2, currentLine.length());
				locationFlag = false;
				servicesFlag = false;
			}

			for (String location : convertedRankList) {
				if (currentLine.indexOf("location: " + location) > -1) {
					System.out.println(currentLine + "found here");
					locationFlag = true;

				}
			}
			if (currentLine.indexOf("services:") > -1) {
					servicesFlag = true;
				if (locationFlag && servicesFlag) {
					output.add(name);
				}
			}

		}
		for (String string : output) {
			System.out.println(string);
		}
		return output;
	}
	public String readingForInfoWithItem(String inputHere) throws IOException {
		List<String> CMFile = Collections.emptyList();
		String output = "";
		String input = inputHere;
		input = input.strip();
		CMFile = Files.readAllLines(Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\cm_file.txt"),
				StandardCharsets.UTF_8);
		String name = null;
		Boolean flag = false;
		Boolean infoflag = false;
		for (int i = 0; i < CMFile.size(); i++) {
			// this to print all the lines in the file.
			// System.out.println(CMFile.get(i));
			String currentLine = CMFile.get(i);
			if (currentLine.indexOf("name: ") > -1) {
				name = currentLine.substring(currentLine.indexOf(":") + 2, currentLine.length());
				name = name.strip();
				if (name.indexOf(input)>-1) {
					flag = true;
				
				}
			}
			if (currentLine.indexOf("information: ") > -1 && flag == true) {
				infoflag = true;
			}
			if(currentLine.indexOf("services: ") > -1) {
				infoflag = false;
				flag = false;
			}
			if(infoflag == true && flag == true) {
				
				output = output.concat(currentLine+"\n");
				System.out.print(currentLine);
			}
			
		}
		return output;
		
	}

}
