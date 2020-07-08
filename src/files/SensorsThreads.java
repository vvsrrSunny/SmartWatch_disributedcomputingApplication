package files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SensorsThreads implements Runnable {
	String path;
	String output = null;

	public SensorsThreads(String path) {
		this.path = path;

	}

	public String getOutput() {
		return output;
	}

	public void run() {
		List<String> linesAQI = Collections.emptyList();

		try {
//  			linesAQI = Files.readAllLines(Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\" + username + "AQI.txt"),
//  					StandardCharsets.UTF_8);
			linesAQI = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(linesAQI);
		int currentcounterAQI = 0;
		String currentcounterAQIString = null;
		String currentAQI = null;
		// sleep for second
		int j = -1;
		while (true) {

			if (currentcounterAQI <= 0) {
				j++;
				if (j >= 3)
					j = 0;
				String s = linesAQI.get(j);
				for (String s1 : s.split(",")) {
					currentAQI = s1;
					break;
				}
				for (String s1 : s.split(",")) {
					currentcounterAQIString = s1;
				}
				currentcounterAQI = Integer.parseInt(currentcounterAQIString);

			}
			// System.out.println(currentAQI);
			output = currentAQI;
			// System.out.println(currentcounterAQI);
			currentcounterAQI--;
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
