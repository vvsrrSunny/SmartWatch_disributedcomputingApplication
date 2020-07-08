package components;



import com.zeroc.demos.IceStorm.sensors.Demo.*;

import files.WeatherThread;

public class WeatherAlarm {

	public static void main(String[] args) {
		int status = 0;
		java.util.List<String> extraArgs = new java.util.ArrayList<String>();

		//
		// Try with resources block - communicator is automatically destroyed
		// at the end of this try block
		//
		try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "configfiles\\config.pub",
				extraArgs)) {
			communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.sensors");
			//
			// Install shutdown hook to (also) destroy communicator during JVM shutdown.
			// This ensures the communicator gets destroyed when the user interrupts the
			// application with Ctrl-C.
			//
			Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));

			status = runWeatherPublisher(communicator, extraArgs.toArray(new String[extraArgs.size()]), "James");
		}
		System.exit(status);
	}

	// copied
	public static void usage() {
		System.out.println("Usage: [--datagram|--twoway|--oneway] [topic]");
	}

	// run copy
	private static int runWeatherPublisher(com.zeroc.Ice.Communicator communicator, String[] args, String user) {
		String option = "None";
		String topicName = "Weather";
		int i;

		for (i = 0; i < args.length; ++i) {
			String oldoption = option;
			if (args[i].equals("--datagram")) {
				option = "Datagram";
			} else if (args[i].equals("--twoway")) {
				option = "Twoway";
			} else if (args[i].equals("--oneway")) {
				option = "Oneway";
			} else if (args[i].startsWith("--")) {
				usage();
				return 1;
			} else {
				topicName = args[i++];
				break;
			}

			if (!oldoption.equals(option) && !oldoption.equals("None")) {
				usage();
				return 1;
			}
		}

		if (i != args.length) {
			usage();
			return 1;
		}

		com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx
				.checkedCast(communicator.propertyToProxy("TopicManager.Proxy"));
		if (manager == null) {
			System.err.println("invalid proxy");
			return 1;
		}

		//
		// Retrieve the topic.
		//
		com.zeroc.IceStorm.TopicPrx topic;
		try {
			topic = manager.retrieve(topicName);
		} catch (com.zeroc.IceStorm.NoSuchTopic e) {
			try {
				topic = manager.create(topicName);
			} catch (com.zeroc.IceStorm.TopicExists ex) {
				System.err.println("temporary failure, try again.");
				return 1;
			}
		}

		//
		// Get the topic's publisher object, and create a Sensordata proxy with
		// the mode specified as an argument of this application.
		//
		com.zeroc.Ice.ObjectPrx publisher = topic.getPublisher();
		if (option.equals("Datagram")) {
			publisher = publisher.ice_datagram();
		} else if (option.equals("Twoway")) {
			// Do nothing.
		} else // if(oneway)
		{
			publisher = publisher.ice_oneway();
		}
		SenorsdataPrx sensorsdata = SenorsdataPrx.uncheckedCast(publisher);

		System.out.println("publishing tick events. Press ^C to terminate the application.");
		try {
			WeatherThread sensorWeather = new WeatherThread(
					"C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\weather_alarm.txt");
			Thread threadWeather = new Thread(sensorWeather);
			threadWeather.start();
			while (true) {
			String weatherReadings = sensorWeather.getOutput();	
				//sensorsdata.valuesOfData( weatherReadings+ " this is weather");
			sensorsdata.valuesOfData( weatherReadings);
				try {
					Thread.currentThread();
					Thread.sleep(60000);
				} catch (java.lang.InterruptedException e) {
				}
			}
		} catch (com.zeroc.Ice.CommunicatorDestroyedException ex) {
			// Ctrl-C triggered shutdown hook, which destroyed communicator - we're
			// terminating
		}

		return 0;
	}

}
