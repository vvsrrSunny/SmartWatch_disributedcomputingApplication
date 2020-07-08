package files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class WeatherThread implements Runnable {

	String path;
	String output = null;
	int count = 0;

	public WeatherThread(String path) {
		this.path = path;

	}

	public String getOutput() {
		return output;
	}

	public void run() {
		List<String> linesWeather = Collections.emptyList();

		try {
//  			
			linesWeather = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {

			if (count < linesWeather.size()) {

				output = linesWeather.get(count);
			} else {
				count = 0;

				output = linesWeather.get(count);

			}
			count++;

			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (java.lang.InterruptedException e) {
			}
		}

	}
}
