package rmis;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.zeroc.Ice.Current;

import components.LocationServer;



public class LocationQueryI implements Demo.LocationQuery
{
	List<String> linesAQI = Collections.emptyList();
	

	@Override
	public String locationQueryMethod(String LocationType, Current current) {
		// TODO Auto-generated method stub
		Map<String, String> map = LocationServer.mymap;
		String result = "";
		for ( Map.Entry<String, String> entry : map.entrySet()) {
		    String key = entry.getKey();
		    key = key.strip();
		    String value = entry.getValue();
		    if (containsIgnoreCase(value, LocationType)) {
		    	result = result+" "+key;
		    }
		    result = result.strip();
		    // do something with key and/or tab
		}		
		return result;
	}
	
	public static boolean containsIgnoreCase(String str, String subString) {
		return str.toLowerCase().contains(subString.toLowerCase());
	}
}