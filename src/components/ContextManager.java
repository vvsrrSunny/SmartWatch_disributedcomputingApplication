package components;
//

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;

// Copyright (c) ZeroC, Inc. All rights reserved.
//

import com.zeroc.demos.IceStorm.sensors.Demo.*;

import Demo.CustomerPreferencePrx;
import Demo.LocationQueryPrx;
import files.ReadingCMFIleForSuggest;
import rmis.LocationQueryI;
import rmis.UserToContextmanagerI;

public class ContextManager {
	static int medicalType = -1;
	public static String user = null;
	static String locationServerData = null;
	static String APOAndTempOfUser = null;
	public static String APOOfUser = null;
	public static String TempOfUser = null;
	public static String LocOfUser = null;

	static int currentPriority = 0;// 3 for weather, 2 for APO , 1 for temperature
	public static int currrentWeatherCondition = 0;
	static List<String> CMFile = Collections.emptyList();
	static {
		try {
			CMFile = Files.readAllLines(
					Paths.get("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\cm_file.txt"),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class SensordataI implements Senorsdata {
		@Override
		public void valuesOfData(String data, com.zeroc.Ice.Current current) {
			System.out.println(data + "  priniting in contemt manager from allsensors ");
			APOAndTempOfUser = data;
			int indexG;
			
			if (APOAndTempOfUser.indexOf(user) > -1) {
				if (APOAndTempOfUser.indexOf("AQI") > -1) {

					indexG = APOAndTempOfUser.indexOf("AQI");
					indexG = indexG + 4;
					APOOfUser = APOAndTempOfUser.substring(indexG);
					APOOfUser = APOOfUser.strip();
				} else {
					indexG = APOAndTempOfUser.indexOf("Temperature");
					indexG = indexG + 12;
					TempOfUser = APOAndTempOfUser.substring(indexG);
					TempOfUser = TempOfUser.strip();
				}
			}

		}
	}

	public static class LocationdataI implements Locationdata {

		@Override
		public void valuesOfLocationData(String locationFromServer, Current current) {
			// TODO Auto-generated method stub
			// System.out.println(locationFromServer + " printing in contextmanager from
			// location server");
			locationServerData = locationFromServer;
		}
	}

	public static class WeatherdataI implements Senorsdata {

		@Override
		public void valuesOfData(String weatherFromServer, Current current) {
			// TODO Auto-generated method stub
			weatherFromServer = weatherFromServer.strip();
			currrentWeatherCondition = Integer.parseInt(weatherFromServer);
			// System.out.println(currrentWeatherCondition + " printing in contextmanager
			// from weather server");
		}
	}

	public static void main(String[] args) throws InterruptedException {
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

		Thread userLoginThread = new Thread(() -> {
			System.out.println("Second thread.");
			try {

				com.zeroc.Ice.Communicator communicator2 = com.zeroc.Ice.Util.initialize(args);
				// System.out.println("Server starting fro Location Query here");

				com.zeroc.Ice.ObjectAdapter adapter2 = communicator2
						.createObjectAdapterWithEndpoints("SimplePrinterAdapter", "default -p 9995");
				com.zeroc.Ice.Object object = new UserToContextmanagerI();

				adapter2.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
				adapter2.activate();

				// System.out.println("Adapter activated. Waiting for data.");
				communicator2.waitForShutdown();

				System.out.println("Server ending");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		userLoginThread.start();

		while (true) {
			java.util.concurrent.TimeUnit.SECONDS.sleep(2);
			if (user != null) {
				// Thread userCheckAndThenRunCxtManager = new Thread(() -> {
				// int status;

				try {
					status = runAQIandTempSubscriber(communicator, destroyHook,
							extraArgs.toArray(new String[extraArgs.size()]));
				} catch (Exception ex) {
					ex.printStackTrace();
					status = 1;
				}
				try {
					status = runLocationSubscriber(communicator, destroyHook,
							extraArgs.toArray(new String[extraArgs.size()]));
				} catch (Exception ex) {
					ex.printStackTrace();
					status = 1;
				}
				try {

					status = runWeatherSubscriber(communicator, destroyHook,
							extraArgs.toArray(new String[extraArgs.size()]));
				} catch (Exception ex) {
					ex.printStackTrace();
					status = 1;
				}
				try {

					status = runUserInterfaceWarningPublisher(communicator, destroyHook, args, "basic test here");
				} catch (Exception ex) {
					ex.printStackTrace();
					status = 1;
				}

				Thread weatherThreadinCxt = new Thread(() -> {
					System.out.println("weatherThreadinCxt");
					try {

						int statu = runWeatherAlertsToUsersPublisher(communicator, destroyHook);
					} catch (Exception ex) {
						ex.printStackTrace();

					}
				});
				weatherThreadinCxt.start();
				Thread APOThreadInCxt = new Thread(() -> {
					System.out.println("APOThreadInCxt");
					try {
						Thread.sleep(2000); // wait 5 seconds
						int statu = runAPOAlertsToUsersPublisher(communicator, destroyHook);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				APOThreadInCxt.start();

				Thread TempThreadInCxt = new Thread(() -> {
					System.out.println("APOThreadInCxt");
					try {
						Thread.sleep(2000); // wait 5 seconds
						int statu = runTempAlertsToUsersPublisher(communicator, destroyHook);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				TempThreadInCxt.start();

				Thread userToContextmanagerThread = new Thread(() -> {
					System.out.println("Second thread.");
					try {

						com.zeroc.Ice.Communicator communicator2 = com.zeroc.Ice.Util.initialize(args);
						// System.out.println("Server starting fro Location Query here");

						com.zeroc.Ice.ObjectAdapter adapter2 = communicator2
								.createObjectAdapterWithEndpoints("SimplePrinterAdapter", "default -p 9998");
						com.zeroc.Ice.Object object = new UserToContextmanagerI();

						adapter2.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
						adapter2.activate();

						// System.out.println("Adapter activated. Waiting for data.");
						communicator2.waitForShutdown();

						System.out.println("Server ending");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				userToContextmanagerThread.start();
				Thread userToContextmanagerServerThread = new Thread(() -> {
					System.out.println("Second thread.");
					try {

						com.zeroc.Ice.Communicator communicator2 = com.zeroc.Ice.Util.initialize(args);
						// System.out.println("Server starting fro Location Query here");

						com.zeroc.Ice.ObjectAdapter adapter2 = communicator2
								.createObjectAdapterWithEndpoints("SimplePrinterAdapter", "default -p 9997");
						com.zeroc.Ice.Object object = new UserToContextmanagerI();

						adapter2.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
						adapter2.activate();

						// System.out.println("Adapter activated. Waiting for data.");
						communicator2.waitForShutdown();

						System.out.println("Server ending");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				userToContextmanagerServerThread.start();

				if (status != 0) {
					System.exit(status);
				}
				//
				// Else the application waits for Ctrl-C to destroy the communicator
				//
				// });
				// userCheckAndThenRunCxtManager.start();

				break;
			}
		}

	}

	static public void test(String str) {
		System.out.println(str);
	}

	private static int runUserInterfaceWarningPublisher(com.zeroc.Ice.Communicator communicator, Thread destroyHook,
			String[] args, String content) {

		String option = "None";
		String topicName = "Warning";
		int i;

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

		try {

			// custom code begins
			// String username = "James";

			// custom code ends

			locationPublishdata.valuesOfLocationData(content);
			try {
				Thread.currentThread();
				Thread.sleep(1000);
			} catch (java.lang.InterruptedException e) {
			}

		} catch (com.zeroc.Ice.CommunicatorDestroyedException ex) {
			// Ctrl-C triggered shutdown hook, which destroyed communicator - we're
			// terminating
		}

		return 0;
	}

	private static int runTempAlertsToUsersPublisher(Communicator communicator, Thread destroyHook) {
		// TODO Auto-generated method stub

		String username = "";
		String currentLocation;
		String currentAPO;
		String currentTemperature;
		String indoorOrOutdoor = "Indoor";
		while (true) {
			// code just for parsing the location string
			int firstorderindex = locationServerData.indexOf(",");
			username = locationServerData.substring(0, firstorderindex);

			firstorderindex++;
			int secondorderindex = locationServerData.indexOf(",", firstorderindex);
			firstorderindex = secondorderindex;
			firstorderindex++;
			secondorderindex = locationServerData.indexOf(",", firstorderindex);
			currentLocation = locationServerData.substring(firstorderindex, secondorderindex);
			currentLocation = currentLocation.strip();
			LocOfUser = currentLocation;
			firstorderindex = secondorderindex;
			firstorderindex++;
			secondorderindex = locationServerData.length();
			indoorOrOutdoor = locationServerData.substring(firstorderindex, secondorderindex);
			indoorOrOutdoor = indoorOrOutdoor.strip();

			currentAPO = APOOfUser;
			currentTemperature = TempOfUser;
			// System.out.println(indoorOrOutdoor + "hehehehheehheh");
			try {
				String tempForMedicalType = runAsClientForPreferenceManager(communicator, destroyHook, user, "Medical");
				tempForMedicalType = tempForMedicalType.strip();
				medicalType = Integer.parseInt(tempForMedicalType);
				// System.out.println(medicalType + " medicaltype no errores proceed ");
				String TempPreferencePlace = runAsClientForPreferenceManager(communicator, destroyHook, user, "Temp");
				String TresholedTemp = TempPreferencePlace.substring(5, 8);
				TresholedTemp = TresholedTemp.strip();
				if (Integer.parseInt(currentTemperature) > Integer.parseInt(TresholedTemp)
						&& currrentWeatherCondition == 0) {
					TempPreferencePlace = preferenceExtraction(TempPreferencePlace);
					String outcomeIndoor = runAsClientForLocationQuery(communicator, destroyHook, "Indoor");
					String outcomeOutdoor = runAsClientForLocationQuery(communicator, destroyHook, "Indoor");
					// System.out.println(outcome + " Query to locations server has run successfully
					// here ");
					ReadingCMFIleForSuggest readingCMFIleForSuggest = new ReadingCMFIleForSuggest();
					List<String> suggestionsIndoor = readingCMFIleForSuggest
							.readingSuggestionsMethod(TempPreferencePlace, outcomeIndoor);
					List<String> suggestionsOutdoor = readingCMFIleForSuggest
							.readingSuggestionsMethod(TempPreferencePlace, outcomeOutdoor);
					List<String> suggestions = new ArrayList(suggestionsIndoor);
					suggestions.addAll(suggestionsOutdoor);

					// suggest this and user and send temperature warning.
//			        Warning, TEMPERATURE is now 36 (james's limit is 35)
//			        		Current Location: C
//			        		Current weather alarm status: no alarmSuggestion - please go to an indoor or outdoor pool
//			        		at one of these locations: Indooroopilly Shopping Centre, South Bank Parklands, Garden City.

					try {

						String[] args = null;
						String content = "Warning, TEMPERATURE is now " + currentTemperature + " (" + user
								+ "'s limit is " + TresholedTemp + ")\n";
						content = content.concat("Current Location: " + currentLocation + "\n");
						content = content.concat("Current weather alarm status: no alarm" + "\n");
						String citiesCommaSeparated = String.join(",", suggestions);
						content = content.concat("Suggestion - please go to an indoor or outdoor " + TempPreferencePlace
								+ " at one of these locations: " + citiesCommaSeparated + "\n");
						int statu = runUserInterfaceWarningPublisher(communicator, destroyHook, args, content);
					} catch (Exception ex) {
						ex.printStackTrace();

					}
				}

				try {
					Thread.currentThread();
					Thread.sleep(1000);
				} catch (java.lang.InterruptedException e) {
				}

			} catch (Exception ex) {
				ex.printStackTrace();

			}
		}
	}

	private static int runAPOAlertsToUsersPublisher(Communicator communicator, Thread destroyHook) {
		// TODO Auto-generated method stub
		int counter = 0;
		String username = "";
		String currentLocation;
		String currentAPO;
		String currentTemperature;
		String indoorOrOutdoor = "Indoor";
		while (true) {
			// code just for parsing the location string
			int firstorderindex = locationServerData.indexOf(",");
			username = locationServerData.substring(0, firstorderindex);

			firstorderindex++;
			int secondorderindex = locationServerData.indexOf(",", firstorderindex);
			firstorderindex = secondorderindex;
			firstorderindex++;
			secondorderindex = locationServerData.indexOf(",", firstorderindex);
			currentLocation = locationServerData.substring(firstorderindex, secondorderindex);
			currentLocation = currentLocation.strip();

			firstorderindex = secondorderindex;
			firstorderindex++;
			secondorderindex = locationServerData.length();
			indoorOrOutdoor = locationServerData.substring(firstorderindex, secondorderindex);
			indoorOrOutdoor = indoorOrOutdoor.strip();

			currentAPO = APOOfUser;
			currentTemperature = TempOfUser;
			System.out.println(indoorOrOutdoor + "hehehehheehheh");
			try {
				String tempForMedicalType = runAsClientForPreferenceManager(communicator, destroyHook, user, "Medical");
				tempForMedicalType = tempForMedicalType.strip();
				medicalType = Integer.parseInt(tempForMedicalType);
				System.out.println(medicalType + " medicaltype  no errores proceed ");
				int AQIInteger = Integer.parseInt(currentAPO);
				int baseTime = 10;
				if (AQIInteger > -1 & AQIInteger < 51) {
					baseTime = 30;
				} else if (AQIInteger > 50 & AQIInteger < 101) {
					baseTime = 15;
				} else if (AQIInteger > 100 & AQIInteger < 151) {
					baseTime = 10;
				} else if (AQIInteger > 150 & AQIInteger < 201) {
					baseTime = 5;
				}
				int thresholedAPO = medicalType * baseTime;
				if (counter > thresholedAPO && currrentWeatherCondition == 0) {
					String APOPreferencePlace = runAsClientForPreferenceManager(communicator, destroyHook, user, "APO");
					// System.out.println(APOPreferencePlace);
					APOPreferencePlace = preferenceExtraction(APOPreferencePlace);
					String outcome = runAsClientForLocationQuery(communicator, destroyHook, "Indoor");
					// System.out.println(outcome + " Query to locations server has run successfully
					// here ");
					ReadingCMFIleForSuggest readingCMFIleForSuggest = new ReadingCMFIleForSuggest();
					List<String> suggestions = readingCMFIleForSuggest.readingSuggestionsMethod(APOPreferencePlace,
							outcome);

					// make suggstion here with warning.
//					Warning, SIGNIFICANT AIR POLLUTION LEVEL detected, the current AQI is 188 (james's limit is
//							15)
//							Current Location: B
//							Suggestion - please go to an indoor cinema at one of these locations: Indooroopilly
//							Shopping Centre, Garden City.

					try {

						String[] args = null;
						String content = "Warning, SIGNIFICANT AIR POLLUTION LEVEL detected, the current AQI is "
								+ currentAPO + " (" + user + "'s limit is" + thresholedAPO + ")\n";
						content = content.concat("Current Location: " + currentLocation + "\n");
						String citiesCommaSeparated = String.join(",", suggestions);
						content = content.concat("Suggestion - please go to an indoor " + APOPreferencePlace
								+ " at one of these locations: " + citiesCommaSeparated + "\n");
						int statu = runUserInterfaceWarningPublisher(communicator, destroyHook, args, content);
					} catch (Exception ex) {
						ex.printStackTrace();

					}
				}

				counter++;
				if (indoorOrOutdoor.equalsIgnoreCase("Indoor")) {
					counter = 0;
				}
				try {
					Thread.currentThread();
					Thread.sleep(1000);
				} catch (java.lang.InterruptedException e) {
				}

			} catch (Exception ex) {
				ex.printStackTrace();

			}
		}
	}

	private static int runWeatherAlertsToUsersPublisher(Communicator communicator, Thread destroyHook) {
		// TODO Auto-generated method stub

		int previousWeatherCondition = currrentWeatherCondition;
		try {
			// same code
			String username = "";
			String currentLocation;
			String currentAPO;
			String currentTemperature;
			String indoorOrOutdoor = "Indoor";
			while (true) {
//				1 - heavy rain
//				 2 - hail storm
//				 3 - strong wind 
				int firstorderindex = locationServerData.indexOf(",");
				username = locationServerData.substring(0, firstorderindex);

				firstorderindex++;
				int secondorderindex = locationServerData.indexOf(",", firstorderindex);
				firstorderindex = secondorderindex;
				firstorderindex++;
				secondorderindex = locationServerData.indexOf(",", firstorderindex);
				currentLocation = locationServerData.substring(firstorderindex, secondorderindex);
				currentLocation = currentLocation.strip();

				firstorderindex = secondorderindex;
				firstorderindex++;
				secondorderindex = locationServerData.length();
				indoorOrOutdoor = locationServerData.substring(firstorderindex, secondorderindex);
				indoorOrOutdoor = indoorOrOutdoor.strip();

				currentAPO = APOOfUser;
				currentTemperature = TempOfUser;

				// if (currrentWeatherCondition !=0 ) {
				if (currrentWeatherCondition != 0) {
					// some how we need to get all the user name here. and run this as thread to all
					// the users
					String WeatherPreferencePlace = runAsClientForPreferenceManager(communicator, destroyHook, user,
							"Weather");
					System.out.println(WeatherPreferencePlace);
					WeatherPreferencePlace = preferenceExtraction(WeatherPreferencePlace);
					// System.out.println(WeatherPreferencePlace + "atleat here ");
					String outcome = runAsClientForLocationQuery(communicator, destroyHook, "Indoor");
					// System.out.println(outcome + " Query to locations server has run successfully
					// here ");
					ReadingCMFIleForSuggest readingCMFIleForSuggest = new ReadingCMFIleForSuggest();
					List<String> suggestions = readingCMFIleForSuggest.readingSuggestionsMethod(WeatherPreferencePlace,
							outcome);
					currentPriority = 3;
					System.out.println("send suggestions to user as weather alarm");
					// send suggestions to user as weather alarm;

					try {
						String weathercondition = "HEAVY RAIN";
						if (currrentWeatherCondition == 1)
							weathercondition = "HEAVY RAIN";
						else if (currrentWeatherCondition == 2)
							weathercondition = "HAIL STROM";
						else if (currrentWeatherCondition == 1)
							weathercondition = "STRONG WIND";

//						Warning, EXTREME WEATHER is detected, the current weather event is HEAVY RAIN
//						Current Location: C
//						Suggestion - please go to an indoor pool at one of these locations: Indooroopilly Shopping
//						Centre, South Bank Parklands, Garden City.

						String[] args = null;
						String content = "Warning, EXTREME WEATHER is detected, the current weather event is "
								+ weathercondition + "\n";
						content = content.concat("Current Location: " + currentLocation + "\n");
						String citiesCommaSeparated = String.join(",", suggestions);
						content = content.concat("Suggestion - please go to an indoor " + WeatherPreferencePlace
								+ " at one of these locations: " + citiesCommaSeparated + "\n");
						int statu = runUserInterfaceWarningPublisher(communicator, destroyHook, args, content);
					} catch (Exception ex) {
						ex.printStackTrace();

					}

				}
				try {
					Thread.currentThread();
					Thread.sleep(1000);
				} catch (java.lang.InterruptedException e) {
				}
			}
		} catch (Exception ex) {

		}

		return 0;

	}

	private static String preferenceExtraction(String output) {
		int fromIndex = 0;
		int endIndex = 0;
		fromIndex = output.lastIndexOf("suggest");
		fromIndex = fromIndex + 8;
		endIndex = output.length() - 1;
		output = output.substring(fromIndex, endIndex);

		return output;

	}

	private static String runAsClientForLocationQuery(Communicator communicator, Thread destroyHook,
			String locationType) {
		// TODO Auto-generated method stub
		LocationQueryPrx printerWorkerLocationQuery;
		com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 9999");
		printerWorkerLocationQuery = LocationQueryPrx.checkedCast(base);

		if (printerWorkerLocationQuery == null) {
			throw new Error("Invalid proxy");
		}
		String result = printerWorkerLocationQuery.locationQueryMethod(locationType);

		return result;
	}

	private static String runAsClientForPreferenceManager(Communicator communicator, Thread destroyHook, String user,
			String preferenceType) {
		// TODO Auto-generated method stub
		CustomerPreferencePrx printerWorkerCustomerPreference;
		com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 10002");
		printerWorkerCustomerPreference = CustomerPreferencePrx.checkedCast(base);

		if (printerWorkerCustomerPreference == null) {
			throw new Error("Invalid proxy");
		}
		String result = printerWorkerCustomerPreference.customerPreferenceMethod(user, preferenceType);

		return result;
	}

	private static int runWeatherSubscriber(Communicator communicator, Thread destroyHook, String[] args) {
		// TODO Auto-generated method stub
		args = communicator.getProperties().parseCommandLineOptions("Sensordata", args);

		String topicName = "Weather";
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

		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("WeatherFromWeatherServer.Subscriber");

		com.zeroc.Ice.Identity subId = new com.zeroc.Ice.Identity(id, "");
		if (subId.name == null) {
			subId.name = java.util.UUID.randomUUID().toString();
		}
		com.zeroc.Ice.ObjectPrx subscriber = adapter.add(new WeatherdataI(), subId);
		// com.zeroc.Ice.ObjectPrx subscriber2 = adapter2.add(new LocationdataI(),
		// subId2);
		//
		// Activate the object adapter before subscribing.
		//
		adapter.activate();
		// adapter2.activate();

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
				// subscriber2 = subscriber2.ice_batchDatagram();
			} else {
				subscriber = subscriber.ice_datagram();
				// subscriber2 = subscriber2.ice_datagram();
			}
		} else if (option.equals("Twoway")) {
			// Do nothing to the subscriber proxy. Its already twoway.
		} else if (option.equals("Ordered")) {
			// Do nothing to the subscriber proxy. Its already twoway.
			qos.put("reliability", "ordered");
		} else if (option.equals("Oneway") || option.equals("None")) {
			if (batch) {
				subscriber = subscriber.ice_batchOneway();
				// subscriber2 = subscriber2.ice_batchOneway();
			} else {
				subscriber = subscriber.ice_oneway();
				// subscriber2 = subscriber2.ice_oneway();
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

	public static void usage() {
		System.out.println("Usage: [--batch] [--datagram|--twoway|--ordered|--oneway] "
				+ "[--retryCount count] [--id id] [topic]");
	}

	private static int runAQIandTempSubscriber(com.zeroc.Ice.Communicator communicator, Thread destroyHook,
			String[] args) {
		args = communicator.getProperties().parseCommandLineOptions("Sensordata", args);

		String topicName = "AQIandTemp";
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

		com.zeroc.Ice.Identity subId = new com.zeroc.Ice.Identity(id, "");
		if (subId.name == null) {
			subId.name = java.util.UUID.randomUUID().toString();
		}
		com.zeroc.Ice.ObjectPrx subscriber = adapter.add(new SensordataI(), subId);

		com.zeroc.Ice.Identity subId2 = new com.zeroc.Ice.Identity(id, "");
		if (subId2.name == null) {
			subId2.name = java.util.UUID.randomUUID().toString();
		}
		// com.zeroc.Ice.ObjectPrx subscriber2 = adapter2.add(new LocationdataI(),
		// subId2);
		//
		// Activate the object adapter before subscribing.
		//
		adapter.activate();
		// adapter2.activate();

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
				// subscriber2 = subscriber2.ice_batchDatagram();
			} else {
				subscriber = subscriber.ice_datagram();
				// subscriber2 = subscriber2.ice_datagram();
			}
		} else if (option.equals("Twoway")) {
			// Do nothing to the subscriber proxy. Its already twoway.
		} else if (option.equals("Ordered")) {
			// Do nothing to the subscriber proxy. Its already twoway.
			qos.put("reliability", "ordered");
		} else if (option.equals("Oneway") || option.equals("None")) {
			if (batch) {
				subscriber = subscriber.ice_batchOneway();
				// subscriber2 = subscriber2.ice_batchOneway();
			} else {
				subscriber = subscriber.ice_oneway();
				// subscriber2 = subscriber2.ice_oneway();
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

	private static int runLocationSubscriber(com.zeroc.Ice.Communicator communicator, Thread destroyHook,
			String[] args) {
		args = communicator.getProperties().parseCommandLineOptions("Sensordata", args);

		String topicName = "LocationToCtxManager";
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

		com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("DataFromLocationServer.Subscriber");

		// com.zeroc.Ice.ObjectAdapter adapter2 =
		// communicator.createObjectAdapter("Sensordata.Subscriber");

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

		com.zeroc.Ice.Identity subId2 = new com.zeroc.Ice.Identity(id, "");
		if (subId2.name == null) {
			subId2.name = java.util.UUID.randomUUID().toString();
		}
		// com.zeroc.Ice.ObjectPrx subscriber2 = adapter2.add(new LocationdataI(),
		// subId2);
		//
		// Activate the object adapter before subscribing.
		//
		adapter.activate();
		// adapter2.activate();

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
				// subscriber2 = subscriber2.ice_batchDatagram();
			} else {
				subscriber = subscriber.ice_datagram();
				// subscriber2 = subscriber2.ice_datagram();
			}
		} else if (option.equals("Twoway")) {
			// Do nothing to the subscriber proxy. Its already twoway.
		} else if (option.equals("Ordered")) {
			// Do nothing to the subscriber proxy. Its already twoway.
			qos.put("reliability", "ordered");
		} else if (option.equals("Oneway") || option.equals("None")) {
			if (batch) {
				subscriber = subscriber.ice_batchOneway();
				// subscriber2 = subscriber2.ice_batchOneway();
			} else {
				subscriber = subscriber.ice_oneway();
				// subscriber2 = subscriber2.ice_oneway();
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

}