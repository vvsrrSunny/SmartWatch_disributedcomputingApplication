package rmis;

import java.util.List;

import com.zeroc.Ice.Current;

import Demo.UserToContextmanager;
import components.ContextManager;
import files.PreferencesFromFileReader;
import files.ReadingCMFIleForSuggest;

public class UserToContextmanagerI implements UserToContextmanager{

	@Override
	public void userToLogin(String CumstomerName, Current current) {
		// TODO Auto-generated method stub
	ContextManager.user = CumstomerName;
	ContextManager.test(ContextManager.user+" heheheehehheh");
	}

	@Override
	public String querytoCxtForItemDetails(String locationName, String user, Current current) {
		// TODO Auto-generated method stub
		ReadingCMFIleForSuggest readingCMFIleForSuggest = new ReadingCMFIleForSuggest();
		String output = null;
		try {
			output = readingCMFIleForSuggest.readingForInfoWithItem(locationName);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (output!="")
		return output;
		else
			return "could not find the search";
		
	}

	@Override
	public String querytoCxtForItemsList(String locationName, String user, Current current) {
		// TODO Auto-generated method stub
		ReadingCMFIleForSuggest readingCMFIleForSuggest = new ReadingCMFIleForSuggest();
		List<String> output= null;
		try {
			String loc = ContextManager.LocOfUser;
			output = readingCMFIleForSuggest.readingItemsFromCurrentLocationMethod(loc);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String output2 =  String.join(",", output);
		return output2;
		
	}

}
