package components;
//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

import java.util.Scanner;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;
import com.zeroc.demos.IceStorm.sensors.Demo.*;

import Demo.LocationQueryPrx;
import Demo.UserToContextmanagerPrx;

public class EnviroAppUI
{
	static String user = "James";
    public static class LocationdataI implements Locationdata// warnings here
    {
    	
    	
     		@Override
		public void valuesOfLocationData(String Warning, Current current) {
			// TODO Auto-generated method stub
			 System.out.println(Warning);
		}
    }

    public static void main(String[] args)
    {
        System.out.println("Enter user name");
        Scanner sc2 = new Scanner(System.in);
	    String option2 = sc2.next();
	    user = option2.strip();
	    
	    
        
    	int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "configfiles\\config.sub", extraArgs);
        communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.sensors");
        //
        // Destroy communicator during JVM shutdown
        //
        Thread destroyHook = new Thread(() -> communicator.destroy());
        Runtime.getRuntime().addShutdownHook(destroyHook);
        runAsClientForcxtManagerForLogin(communicator, destroyHook, user);
    	Thread userToContextmanagerServerThread = new Thread(() -> {        
    		try
            {
                int statu = runForWarnings(communicator, destroyHook, extraArgs.toArray(new String[extraArgs.size()]));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                
            }

            if(status != 0)
            {
                System.exit(status);
            }
    		});
    	userToContextmanagerServerThread.start();
		while (true) {
			
//			--Please select an option--:
//				1. Search for information on a specific item of interest
//				2. Search for items of interest in current location
//				E. Exit
			System.out.println("--Please select an option--:");
		    System.out.println("1. Search for information on a specific item of interest");
		    System.out.println("2. Search for items of interest in current location");
		    System.out.println("E. Exit");
		    Scanner sc = new Scanner(System.in);
		    String option = sc.next();
		    option = option.strip();
		    if(option.equals("1")) {
		    	System.out.println("enter the item name");
		    	Scanner sc3 = new Scanner(System.in);
			    String option3 = sc.next();
			    
		    	String iteminfo = runAsClientForcxtManagerForInfo(communicator, destroyHook, user, option3);
		    	
		    	System.out.println(iteminfo);
		    }else if(option.equals("2")) {
		    	String iteminfo = runAsClientForcxtManagerForLocNames(communicator, destroyHook, user, "Temp");
		    	System.out.println(iteminfo);
		    }else if(option.equals("E")) {
		    	System.exit(0);
		    }else 
		    {
		    	System.out.println("Enter valid number");
		    }
		    
			
		}

	
		
    }

    private static void runAsClientForcxtManagerForLogin(Communicator communicator, Thread destroyHook,
			String user) {
		// TODO Auto-generated method stub
    	UserToContextmanagerPrx printerWorkerUserToContextmanager;
		com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 9995");
		printerWorkerUserToContextmanager = UserToContextmanagerPrx.checkedCast(base);

		if (printerWorkerUserToContextmanager == null) {
			throw new Error("Invalid proxy");
		}
		 printerWorkerUserToContextmanager.userToLogin(user);

		
		
	}
    private static String runAsClientForcxtManagerForLocNames(Communicator communicator, Thread destroyHook,
			String user, String string) {
		// TODO Auto-generated method stub
    	UserToContextmanagerPrx printerWorkerUserToContextmanager;
		com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 9997");
		printerWorkerUserToContextmanager = UserToContextmanagerPrx.checkedCast(base);

		if (printerWorkerUserToContextmanager == null) {
			throw new Error("Invalid proxy");
		}
		String result = printerWorkerUserToContextmanager.querytoCxtForItemsList("", user);

		return result;
		
	}

	private static String runAsClientForcxtManagerForInfo(Communicator communicator, Thread destroyHook, String user2,
			String locationName) {
    	// TODO Auto-generated method stub
		UserToContextmanagerPrx printerWorkerUserToContextmanager;
		com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -p 9998");
		printerWorkerUserToContextmanager = UserToContextmanagerPrx.checkedCast(base);

		if (printerWorkerUserToContextmanager == null) {
			throw new Error("Invalid proxy");
		}
		String result = printerWorkerUserToContextmanager.querytoCxtForItemDetails(locationName, user);

		return result;
	
	
	}

	private static int runForWarnings(com.zeroc.Ice.Communicator communicator, Thread destroyHook, String[] args)
    {
        args = communicator.getProperties().parseCommandLineOptions("Sensordata", args);

        String topicName = "Warning";
        String option = "None";
        boolean batch = false;
        String id = null;
        String retryCount = null;
        if(batch && (option.equals("Twoway") || option.equals("Ordered")))
        {
            System.err.println("batch can only be set with oneway or datagram");
            return 1;
        }

        com.zeroc.IceStorm.TopicManagerPrx manager = com.zeroc.IceStorm.TopicManagerPrx.checkedCast(
            communicator.propertyToProxy("TopicManager.Proxy"));
        if(manager == null)
        {
            System.err.println("invalid proxy");
            return 1;
        }

        //
        // Retrieve the topic.
        //
        com.zeroc.IceStorm.TopicPrx topic;
        try
        {
            topic = manager.retrieve(topicName);
        }
        catch(com.zeroc.IceStorm.NoSuchTopic e)
        {
            try
            {
                topic = manager.create(topicName);
            }
            catch(com.zeroc.IceStorm.TopicExists ex)
            {
                System.err.println("temporary failure, try again.");
                return 1;
            }
        }

        com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("Warnings.Subscriber");
        com.zeroc.Ice.Identity subId = new com.zeroc.Ice.Identity(id, "");
        if(subId.name == null)
        {
            subId.name = java.util.UUID.randomUUID().toString();
        }
        com.zeroc.Ice.ObjectPrx subscriber = adapter.add(new LocationdataI(), subId);

        //
        // Activate the object adapter before subscribing.
        //
        adapter.activate();

        java.util.Map<String, String> qos = new java.util.HashMap<>();
        if(retryCount != null)
        {
            qos.put("retryCount", retryCount);
        }
        //
        // Set up the proxy.
        //
        if(option.equals("Datagram"))
        {
            if(batch)
            {
                subscriber = subscriber.ice_batchDatagram();
            }
            else
            {
                subscriber = subscriber.ice_datagram();
            }
        }
        else if(option.equals("Twoway"))
        {
            // Do nothing to the subscriber proxy. Its already twoway.
        }
        else if(option.equals("Ordered"))
        {
            // Do nothing to the subscriber proxy. Its already twoway.
            qos.put("reliability", "ordered");
        }
        else if(option.equals("Oneway") || option.equals("None"))
        {
            if(batch)
            {
                subscriber = subscriber.ice_batchOneway();
            }
            else
            {
                subscriber = subscriber.ice_oneway();
            }
        }

        try
        {
            topic.subscribeAndGetPublisher(qos, subscriber);
        }
        catch(com.zeroc.IceStorm.AlreadySubscribed e)
        {
            // This should never occur when subscribing with an UUID
            if(id == null)
            {
                e.printStackTrace();
                return 1;
            }
            System.out.println("reactivating persistent subscriber");
        }
        catch(com.zeroc.IceStorm.InvalidSubscriber e)
        {
            e.printStackTrace();
            return 1;
        }
        catch(com.zeroc.IceStorm.BadQoS e)
        {
            e.printStackTrace();
            return 1;
        }

        //
        // Replace the shutdown hook to unsubscribe during JVM shutdown
        //
        final com.zeroc.IceStorm.TopicPrx topicF = topic;
        final com.zeroc.Ice.ObjectPrx subscriberF = subscriber;
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            try
            {
                topicF.unsubscribe(subscriberF);
            }
            finally
            {
                communicator.destroy();
            }
        }));
        Runtime.getRuntime().removeShutdownHook(destroyHook); // remove old destroy-only shutdown hook

        return 0;
    }
}