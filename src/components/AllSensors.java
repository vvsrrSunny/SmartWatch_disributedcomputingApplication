package components;
//
// Copyright (c) ZeroC, Inc. All rights reserved.
//



import com.zeroc.demos.IceStorm.sensors.Demo.*;

import files.SensorsThreads;

public class AllSensors
{
    public static void main(String[] args)
    {
        int status = 0;
        java.util.List<String> extraArgs = new java.util.ArrayList<String>();

        //
        // Try with resources block - communicator is automatically destroyed
        // at the end of this try block
        //
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "configfiles\\config.pub", extraArgs))
        {   
            communicator.getProperties().setProperty("Ice.Default.Package", "com.zeroc.demos.IceStorm.sensors");
            
            //
            // Install shutdown hook to (also) destroy communicator during JVM shutdown.
            // This ensures the communicator gets destroyed when the user interrupts the application with Ctrl-C.
            //
            Runtime.getRuntime().addShutdownHook(new Thread(() -> communicator.destroy()));
           String users = "James";// edit usernames here like Bryan etc to run different users
            status = runAQIandTempThenLocPublisher(communicator, extraArgs.toArray(new String[extraArgs.size()]),users);
        }
        System.exit(status);
    }

    public static void usage()
    {
        System.out.println("Usage: [--datagram|--twoway|--oneway] [topic]");
    }

    private static int runAQIandTempThenLocPublisher(com.zeroc.Ice.Communicator communicator, String[] args, String user)
    {
        String username = user;
    	String option = "None";
        String topicName = "AQIandTemp";
        String topicName2 = "Location";
        int i;

        for(i = 0; i < args.length; ++i)
        {
            String oldoption = option;
            if(args[i].equals("--datagram"))
            {
                option = "Datagram";
            }
            else if(args[i].equals("--twoway"))
            {
                option = "Twoway";
            }
            else if(args[i].equals("--oneway"))
            {
                option = "Oneway";
            }
            else if(args[i].startsWith("--"))
            {
                usage();
                return 1;
            }
            else
            {
                topicName = args[i++];
                break;
            }

            if(!oldoption.equals(option) && !oldoption.equals("None"))
            {
                usage();
                return 1;
            }
        }

        if(i != args.length)
        {
            usage();
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
        com.zeroc.IceStorm.TopicPrx topicLocation;
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
        // opy of the above try code
        try
        {
            
            topicLocation= manager.retrieve(topicName2);
        }
        catch(com.zeroc.IceStorm.NoSuchTopic e)
        {
            try
            {
                
                topicLocation = manager.create(topicName2);
            }
            catch(com.zeroc.IceStorm.TopicExists ex)
            {
                System.err.println("temporary failure, try again. 2");
                return 1;
            }
        }

        //
        // Get the topic's publisher object, and create a Sensordata proxy with
        // the mode specified as an argument of this application.
        //
        com.zeroc.Ice.ObjectPrx publisher = topic.getPublisher();
        com.zeroc.Ice.ObjectPrx publisherForLocation = topicLocation.getPublisher();
        if(option.equals("Datagram"))
        {
            publisher = publisher.ice_datagram();
        }
        else if(option.equals("Twoway"))
        {
            // Do nothing.
        }
        else // if(oneway)
        {
            publisher = publisher.ice_oneway();
          //  publisher2 = publisher2.ice_oneway();
        }
        SenorsdataPrx sensorsdata = SenorsdataPrx.uncheckedCast(publisher);
        LocationdataPrx sensorsdataforTemp = LocationdataPrx.uncheckedCast(publisherForLocation);
        System.out.println("publishing tick events. Press ^C to terminate the application.");
        try
        {
            // custom code begins
            //String username = "James";
    		SensorsThreads sensorAQI= new SensorsThreads("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\" + username + "AQI.txt");  
    		Thread threadAQI = new Thread(sensorAQI);
    		SensorsThreads sensorTemp = new SensorsThreads("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\" + username + "Temperature.txt");
    		Thread threadTemp = new Thread(sensorTemp);
    		SensorsThreads sensorLoc = new SensorsThreads("C:\\Users\\lalitha\\eclipse-workspace\\NewProject2\\src\\" + username + "Location.txt");
    		Thread threadLoc = new Thread(sensorLoc);
    		threadAQI.start();
    		threadTemp.start();
    		threadLoc.start();
    		//custom code ends
            while(true)
            {
            	// custom code begins
            	String AQI = sensorAQI.getOutput();
            	String userAQI = username+", AQI, "+AQI;
            	
    			//System.out.println(AQI);
    			String Temp = sensorTemp.getOutput();
    			String userTemp = username+", Temperature, "+Temp;
    			//System.out.println(Temp);
    			String loc = sensorLoc.getOutput();
    			String userLoc = username+", Location, "+loc;
    			
    			
                // custom code end
            	//sensordata.valuesOfData("hello");
    			sensorsdata.valuesOfData(userAQI);
    			sensorsdata.valuesOfData(userTemp);
    			sensorsdataforTemp.valuesOfLocationData(userLoc);
                try
                {
                    Thread.currentThread();
                    Thread.sleep(1000);
                }
                catch(java.lang.InterruptedException e)
                {
                }
            }
        }
        catch(com.zeroc.Ice.CommunicatorDestroyedException ex)
        {
            // Ctrl-C triggered shutdown hook, which destroyed communicator - we're terminating
        }

        return 0;
    }
}