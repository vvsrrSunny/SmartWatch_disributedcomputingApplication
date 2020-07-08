package components;
// delete if you used the default package in Lab 1

import com.zeroc.Ice.Communicator;

import rmis.CustomerPreferenceI;

public class PreferenceManager
{
	private Communicator communicator;
	
    public static void main(String[] args)
    {
    	
    	PreferenceManager preferenceManager = new PreferenceManager();
    	preferenceManager.communicator = com.zeroc.Ice.Util.initialize(args);
    	
    	System.out.println("Server starting.");
        
        com.zeroc.Ice.ObjectAdapter adapter = preferenceManager.communicator.createObjectAdapterWithEndpoints("SimplePrinterAdapter", "default -p 10002");
        com.zeroc.Ice.Object object = new CustomerPreferenceI();
        
        adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimplePrinter"));
        adapter.activate();
        
        System.out.println("Adapter activated. Waiting for data.");
        preferenceManager.communicator.waitForShutdown();
        
        System.out.println("Server ending");
    }
}