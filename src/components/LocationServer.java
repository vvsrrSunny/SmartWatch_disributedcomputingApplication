package components;
//

// Copyright (c) ZeroC, Inc. All rights reserved.
//

import java.util.HashMap;
import java.util.Map;

import com.zeroc.Ice.Communicator;
import com.zeroc.demos.IceStorm.sensors.Demo.*;

import Demo.LocationQuery;
import files.LocationDataPointing;
import rmis.CustomerPreferenceI;
import rmis.LocationQueryI;

public class LocationServer {
	// static LocationdataPrx locationPublishdata;
	public static Map<String, String> mymap = new HashMap<String, String>();
	static {
//		mymap.put("A", "indoor");
//		mymap.put("B", "indoor");
//		mymap.put("C", "outdoor");
//		mymap.put("D", "outdoor");
		try {
			mymap = LocationDataPointing.location();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String LocationToContectmanager = null;

	public static class LocationdataI implements Locationdata {
		@Override
		public void valuesOfLocationData(String location, com.zeroc.Ice.Current current) {
			 //System.out.println(location + " printing in Locationserver ");
			int fromIndex = 0;
			fromIndex = location.lastIndexOf(",");
			String str = Character.toString(location.charAt(fromIndex + 2));
			String indoorOrOutdoor = null;
			if (mymap.containsKey(str)) {
				indoorOrOutdoor = mymap.get(str);
			}
			location = location + ", " + indoorOrOutdoor;
			LocationToContectmanager = location;
		}

	}

	public static void main(String[] args) {
		int status = 0;
		java.util.List<String> extraArgs = new java.util.ArrayList<String>();

		com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "configfiles\\config.sub",
				extraArgs);
		communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.sensors");
		//
		// Destroy communicator during JVM shutdown
		//
		Thread destroyHook = new Thread(() -> communicator.destroy());
		Runtime.getRuntime().addShutdownHook(destroyHook);

		try {
			status = runLocationSubscriber(communicator, destroyHook, extraArgs.toArray(new String[extraArgs.size()]));
		} catch (Exception ex) {
			ex.printStackTrace();
			status = 1;
		}
		try {
			// com.zeroc.Ice.Communicator communicators =
			// com.zeroc.Ice.Util.initialize(args);
			// status = runLocationQueryServer(communicator, destroyHook,
			// extraArgs.toArray(new String[extraArgs.size()]));
		} catch (Exception ex) {
			ex.printStackTrace();
			status = 1;
		}

		// trying
		Thread secondThread = new Thread(() -> {
			System.out.println("Second thread.");
			try {

				com.zeroc.Ice.Communicator communicator2 = com.zeroc.Ice.Util.initialize(args);
				System.out.println("Server starting fro Location Query here");

				com.zeroc.Ice.ObjectAdapter adapter2 = communicator2
						.createObjectAdapterWithEndpoints("SimplePrinterAdapter", "default -p 9999");
				com.zeroc.Ice.Object object = new LocationQueryI();

				adapter2.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
				adapter2.activate();

				System.out.println("Adapter activated. Waiting for data.");
				communicator2.waitForShutdown();

				System.out.println("Server ending");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		secondThread.start();

		// trying ends

		if (status != 0) {
			System.exit(status);
		}
		//
		// Else the application waits for Ctrl-C to destroy the communicator
		//
		com.zeroc.Ice.Communicator communicatorLocationtoContextManager = com.zeroc.Ice.Util.initialize(args,
				"configfiles\\config.pub", extraArgs);
		communicatorLocationtoContextManager.getProperties().setProperty("Ice.Default.Package",
				"com.zeroc.demos.IceStorm.sensors");
		//
		// Destroy communicator during JVM shutdown
		//
		Thread destroyHookLocationtoContextManager = new Thread(() -> communicatorLocationtoContextManager.destroy());
		Runtime.getRuntime().addShutdownHook(destroyHookLocationtoContextManager);

		try {
			status = runLocationtoContextManagerPublisher(communicatorLocationtoContextManager,
					destroyHookLocationtoContextManager, extraArgs.toArray(new String[extraArgs.size()]));
		} catch (Exception ex) {
			ex.printStackTrace();
			status = 1;
		}

		if (status != 0) {
			System.exit(status);
		}

	}

	private static int runLocationQueryServer(Communicator communicatorer, Thread destroyHook, String[] array) {
		// TODO Auto-generated method stub
		String[] args = null;
		com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args);
		System.out.println("Server starting fro Location Query here");

		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("SimplePrinterAdapter",
				"default -p 9999");
		com.zeroc.Ice.Object object = new CustomerPreferenceI();

		adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
		adapter.activate();

		System.out.println("Adapter activated. Waiting for data.");
		communicator.waitForShutdown();

		System.out.println("Server ending");
		return 0;
	}

	public static void usage() {
		System.out.println("Usage: [--batch] [--datagram|--twoway|--ordered|--oneway] "
				+ "[--retryCount count] [--id id] [topic]");
	}

	private static int runLocationSubscriber(com.zeroc.Ice.Communicator communicator, Thread destroyHook,
			String[] args) {
		args = communicator.getProperties().parseCommandLineOptions("Sensordata", args);

		String topicName = "Location";
		String option = "None";
		boolean batch = false;
		String id = null;
		String retryCount = null;
		int i;
		for (i = 0; i < args.length; ++i) {
			String oldoption = option;
			if (args[i].equals("--datagram")) {
				option = "Datagram";
			} else if (args[i].equals("--twoway")) {
				option = "Twoway";
			} else if (args[i].equals("--ordered")) {
				option = "Ordered";
			} else if (args[i].equals("--oneway")) {
				option = "Oneway";
			} else if (args[i].equals("--batch")) {
				batch = true;
			} else if (args[i].equals("--id")) {
				++i;
				if (i >= args.length) {
					usage();
					return 1;
				}
				id = args[i];
			} else if (args[i].equals("--retryCount")) {
				++i;
				if (i >= args.length) {
					usage();
					return 1;
				}
				retryCount = args[i];
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

		if (batch && (option.equals("Twoway") || option.equals("Ordered"))) {
			System.err.println("batch can only be set with oneway or datagram");
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

		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Sensordata.Subscriber");

		//
		// Add a servant for the Ice object. If --id is used the
		// identity comes from the command line, otherwise a UUID is
		// used.
		//
		// id is not directly altered since it is used below to detect
		// whether subscribeAndGetPublisher can raise
		// AlreadySubscribed.
		//
		com.zeroc.Ice.Identity subId = new com.zeroc.Ice.Identity(id, "");
		if (subId.name == null) {
			subId.name = java.util.UUID.randomUUID().toString();
		}
		com.zeroc.Ice.ObjectPrx subscriber = adapter.add(new LocationdataI(), subId);

		//
		// Activate the object adapter before subscribing.
		//
		adapter.activate();

		java.util.Map<String, String> qos = new java.util.HashMap<>();
		if (retryCount != null) {
			qos.put("retryCount", retryCount);
		}
		//
		// Set up the proxy.
		//
		if (option.equals("Datagram")) {
			if (batch) {
				subscriber = subscriber.ice_batchDatagram();
			} else {
				subscriber = subscriber.ice_datagram();
			}
		} else if (option.equals("Twoway")) {
			// Do nothing to the subscriber proxy. Its already twoway.
		} else if (option.equals("Ordered")) {
			// Do nothing to the subscriber proxy. Its already twoway.
			qos.put("reliability", "ordered");
		} else if (option.equals("Oneway") || option.equals("None")) {
			if (batch) {
				subscriber = subscriber.ice_batchOneway();
			} else {
				subscriber = subscriber.ice_oneway();
			}
		}

		try {
			topic.subscribeAndGetPublisher(qos, subscriber);
		} catch (com.zeroc.IceStorm.AlreadySubscribed e) {
			// This should never occur when subscribing with an UUID
			if (id == null) {
				e.printStackTrace();
				return 1;
			}
			System.out.println("reactivating persistent subscriber");
		} catch (com.zeroc.IceStorm.InvalidSubscriber e) {
			e.printStackTrace();
			return 1;
		} catch (com.zeroc.IceStorm.BadQoS e) {
			e.printStackTrace();
			return 1;
		}

		//
		// Replace the shutdown hook to unsubscribe during JVM shutdown
		//
		final com.zeroc.IceStorm.TopicPrx topicF = topic;
		final com.zeroc.Ice.ObjectPrx subscriberF = subscriber;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				topicF.unsubscribe(subscriberF);
			} finally {
				communicator.destroy();
			}
		}));
		Runtime.getRuntime().removeShutdownHook(destroyHook); // remove old destroy-only shutdown hook

		return 0;
	}

	private static int runLocationtoContextManagerPublisher(com.zeroc.Ice.Communicator communicator, Thread destroyHook,
			String[] args) {

		String option = "None";
		String topicName = "LocationToCtxManager";
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
		LocationdataPrx locationPublishdata = LocationdataPrx.uncheckedCast(publisher);

		System.out.println("publishing tick events. Press ^C to terminate the application.");
		try {

			// custom code begins
			// String username = "James";

			// custom code ends
			while (true) {

				locationPublishdata.valuesOfLocationData(LocationToContectmanager);
				try {
					Thread.currentThread();
					Thread.sleep(1000);
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