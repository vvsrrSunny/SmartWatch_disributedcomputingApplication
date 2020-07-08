# SmartWatch_disributedcomputingApplication

Abstract
Here we are going to provide detail description on the distributed computing project and the design paradigm of communication of RMI and publish and subscribe methods. Out of these two, we will describe the which commination paradigm is been used in the design.

Explanation 
Here we are presenting the diagram that shows pictographically represented diagram that explains the relation between the components.
   
****** Please refer tot eh design details file for better understing ******  
Paradigm	Brief description of how commination takes place
1.	Location sensor	Location Server	Publish/subscribe	Sends the location data of the user to the location server every second. 
2.	Air pollution sensor	Context Manager	Publish/subscribe	Sends AQI data of the user to the context manager component every second.
3.	Temperature sensor	Context Manager	Publish/subscribe	Sends temperature data of the user to the context manager every second. 
4.	Weather Alarm	Context Manager	Publish/subscribe	Sends weather information to the context manager every minute
5.	Location Server	Context Manager	Publish/subscribe	Location server will access location files and sends whether user is indoor or outdoor and his location every second
6.	Context manager	Location Server	RMI	Context manager queries location server for the list of all Indoor or outdoor location and Location server access location file and send the output.
7.	Context manager	Preference Manager	RMI	Context manager queries the preference manager for the user preferences when any of the threshold in sensor reading is reached and preference manager will investigate the files of user’s preferences, process the output and sends.
8.	User Interface	Context manager	RMI	User through user interface logs into the system by sending username to context manager.
9.	Context manager	User Interface	Publish/subscribe	Context manager sends any weather or APO or Temperature alert to the user.
10.	User Interface	Context Manager	RMI	User query for the information about a location by sending location name to context manager. Context manager sends process location name and sends information. 
11.	User Interface	Context Manager	RMI	User query for the list of places in his location. Context manager sends process his current location and sends places name list.

Table1. explains the relation between the components in the system presented in figure 1
Flow of control from entry 
Allsernsors.java file will impersonate in the way that the air pollution sensor and temperature publish their data to context manager every second while location sensor publish data to location server every second. Location as subscribed to location sensor receivers the location, it then processes whether the current location is indoor or outdoor and then publish these details to the context manager. Weather server also publish its report to context manager every 60 seconds.
Context manager will have username when user logs in using RMI. So, it queries the preference manager for the threshold temperature and medical type. It also contains file that contains all the details about the location names and information. Context manager as subscribed to location server, temperature sensor, air pollution sensor and weather sensor will process the data in the following way.
•	Checks whether the weather is extreme and if yes, it queries for the user preference and send a query to location server for all indoor locations and then use those preference and locations to get suggestions to publish the warning and suggestions to the user interface.
•	Checks the Temperature and if it exceeds the threshold, it queries for the user preference and use that preference and locations to get suggestions to publish the warning and suggestions to the user interface.
•	Checks whether the APO over threshold and if yes, it queries for the user preference and send a query to location server for all indoor locations and then use those preference and locations to get suggestions to publish the warning and suggestions to the user interface.
•	It also responds a server to the user interface when is receives queries from users through RMI. It responds information of a location or responds list of places in the location based on the query from the user. 

Exiting the system
When the user askes for exit then the Exit message is queried to Context manager. Then it checks whether it is the last user. If yes, context manager should send the status signal to the context manager. Context manager will make an RMI request to the preference manager to shutdown.  Its up to the design decision to shut down all sensors. In reality these sensors are not in the context manager to shut down. So, the context manager will set the flag to shut down as true which makes status to one. By this context manager will terminate execution. 
