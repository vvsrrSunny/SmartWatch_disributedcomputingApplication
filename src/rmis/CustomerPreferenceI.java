package rmis;

import java.util.Collections;
import java.util.List;

import com.zeroc.Ice.Current;

import files.PreferencesFromFileReader;

public class CustomerPreferenceI implements Demo.CustomerPreference
{
	List<String> linesAQI = Collections.emptyList();
	
	@Override
	public String customerPreferenceMethod(String CumstomerName,String preferecnceType , Current current) {
		// TODO Auto-generated method stub
		 //System.out.println(CumstomerName+"I am modifing");
		PreferencesFromFileReader preferencesFromFileReader = new PreferencesFromFileReader();
		String output= null;
		try {
			output = preferencesFromFileReader.getPteference(CumstomerName, preferecnceType);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output;
	      
		
	}
}