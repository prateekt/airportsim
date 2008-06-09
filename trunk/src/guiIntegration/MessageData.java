package guiIntegration;

/*
 * A MessageData is a data container for all TraceMessage data to be handled by the GUI.
 * The class is accessed by each display panel and handles new TraceMessage organization.
 * NOTE: This class is essentially a static container.  (All data is static)
 * This means each instance will be identical.  Adding a message to one instance will
 * make the message visible to everyone in the system.
 *
 * author: John Baldo
 */

//SEE ALSO: TraceMessage.java and AgentPair.java in src/guiIntegration/


import java.util.*;

public class MessageData{

    private static Integer index;
	//Index position used when adding to All Messages vector, messList

//ALL MESSAGES

    private static Vector<TraceMessage> messList;
	//All Messages as TraceMessages

    private static Vector<String> messListStrings;
	//All Messages formatted as Strings

    private static String messAll;
	//One string containing all messages

//LOCAL CONTROL MESSAGES

    private static Vector<Integer> messLC;
	//Index positions of TraceMessages to or from Local Control in messList

    private static String messLCAll;
	//One string containing all LC messages

//GROUND CONTROL MESSAGES

    private static Vector<Integer> messGC;
	//Index positions of TraceMessages to or from Ground Control in messList

    private static String messGCAll;
	//One string containing all GC messages

  //CD MESSAGES

    private static Vector<Integer> messCD;
	//Index positions of TraceMessages to or from Clearance Delivery in messList

    private static String messCDAll;
	//One string containing all Clearance Delivery messages

    
//PILOT MESSAGES

    private static HashMap<String, Vector<Integer>> messPilot;
	//string - flight name or identifier
	//Vector - indexes of messages to or from that flight

    private static HashMap<String,String> messPilotStrings;
	//All messages to or from a particular pilot as one string, mapped by pilot

    private static String messPilotAll;
	//One string with all Pilot messages

//INDICATORS
//These variables serve as flags, triggered when a message is added to a list
//They are designed to avoid adding a message twice during a broadcast (message to all users)

	private static Integer addedToLC;
	private static Integer addedToGC;
	private static Integer addedToPilot;
	private static Integer addedToCD;

	private static MessageData instance;

	public static MessageData getInstance() {
		if(instance==null)
			instance = new MessageData();
		return instance;
	}

private MessageData(){

//Initiate data collections and variables

	messList =  new Vector<TraceMessage>();
	messListStrings = new Vector<String>();
	messAll = "\n <html>";

	messLC = new Vector<Integer>();
	messLCAll = "\n <html>";

	messGC = new Vector<Integer>();
	messGCAll = "\n <html>";

	messPilot = new HashMap<String, Vector<Integer>>();
	messPilotStrings = new HashMap<String,String>();
	messPilotAll = "\n <html>";
	
	messCD = new Vector<Integer>();
	messCDAll = "\n <html>";

	addedToLC = 0;
	addedToGC = 0;
	addedToPilot = 0;
	addedToCD = 0;

	index = 0;
}


public void fireTraceMessage(TraceMessage tm){

//Called by back end to add a new message to the GUI


	if(tm.getType() == TraceMessage.TraceType.MESSAGE){ //determine whether an action or message

		messList.add(index, tm); //add TraceMessage to end of master message list

		String toPrefix = "";
		//This string will hold the prefix which identifies the type of agent
		//and the specific name of the agent in parens '()'
		//the prefix will be shown before the actual message

		String messageText = tm.getMessage(); //get actual text of message

		String completeMessage = "";
			//complete message = fromPrefix + messageText


////////////BUILD STRINGS - this section creates and formats a message string depending on its TO data

		if(tm.getTo() == null){ // a Null TO field indicates that the message is a broadcast to all users
					//NOTE: this is not a true broadcast, because only users which have
					//previously interacted with the system will receive the message

			toPrefix = "ALL USERS:";

			completeMessage = "<b>" + toPrefix + "</b> " + messageText + "<br><br>";
			//Complete message formatted with html bold prefix


			if(addedToGC == 0)//If message has not been added to GC
			{

				messGCAll = messGCAll + completeMessage;
				//add to end of GC message string

				messGC.add(index);
				//add reference to index of TraceMessage object in master message list messList

				addedToGC = 1; //alerts system that the message has been added to Ground Control Lists

			}

			if(addedToLC == 0)//If message has not be added to LC
			{
				messLCAll = messLCAll + completeMessage;
				//add to end of LC message string

				messLC.add(index);
				//add reference to index of TraceMessage object in master message list messList

				addedToLC = 1; //alerts system that the message has been added to Ground Control Lists
			}

			if(addedToCD == 0)//If message has not be added to CD
			{
				messCDAll = messCDAll + completeMessage;
				//add to end of LC message string

				messCD.add(index);
				//add reference to index of TraceMessage object in master message list messList

				addedToCD = 1; //alerts system that the message has been added to Ground Control Lists
			}

			
			
			if(addedToPilot == 0)//If the message has not been added to Pilot message string
			{

				messPilotAll = messPilotAll + completeMessage; //add to end of pilot message string
				addedToPilot = 1; //alerts system that the message has been added to Pilot String

			}


			//Now add to all Pilot User Message Index Lists (messPilot)

			Set<String> pilotUsers = messPilot.keySet();  //All KNOWN pilot Users as Set
				//NOTE: this is not a true broadcast, because only users which have
				//previously interacted with the system will receive the message

			for(Iterator it = pilotUsers.iterator(); it.hasNext();){

				String pilotName = (String)it.next(); //get next name

					//add the message to user's list of message indices
					Vector<Integer> pilotIndex = messPilot.get(pilotName);//get list
					pilotIndex.add(index);	//add to list
					messPilot.put(pilotName, pilotIndex); //replace list

			}//end for loop

			//Now, using the same user list, add to String of Pilot Messages (messPilotStrings)

			Set<String> pilotUsers2 = messPilot.keySet();  //All KNOWN pilot Users as Set
			//NOTE: this is not a true broadcast, because only users which have
			//previously interacted with the system will receive the message

			for(Iterator it = pilotUsers2.iterator(); it.hasNext();){

				String pilotName = (String)it.next(); //get next name

					//add the message to user's message string
					String pilotString = messPilotStrings.get(pilotName);//get string
					pilotString = pilotString + completeMessage;	//add to end of string
					messPilotStrings.put(pilotName, pilotString); //replace string

			}//end for loop

		}//end of broadcast case

		else{//Message is destined for one specific user or group of users

		AgentPair to = tm.getTo();

		String name = to.getName();

			if((to.getType() == AgentPair.AgentType.GROUND_CONTROL)  && (addedToGC == 0)){
				//If the message is for Ground Control users
				//add the message to Ground Control lists

				toPrefix = "GC(" + name + "):"; //Prefix before message text

				completeMessage = "<b>" + toPrefix + "</b> " + messageText + "<br><br>";
					//Complete message formatted with html bold prefix

				messGCAll = messGCAll + completeMessage;
				//add to end of GC message string

				messGC.add(index);
				//add reference to index of TraceMessage object in master message list messList

				addedToGC = 1; //alerts system that the message has been added to Ground Control Lists
			} else

			if((to.getType() == AgentPair.AgentType.CLEARANCE_DELIVERY)  && (addedToCD == 0)){
				//If the message is for CD users
				//add the message to CD lists

				toPrefix = "CD(" + name + "):"; //Prefix before message text

				completeMessage = "<b>" + toPrefix + "</b> " + messageText + "<br><br>";
					//Complete message formatted with html bold prefix

				messCDAll = messCDAll + completeMessage;
				//add to end of CD message string

				messCD.add(index);
				//add reference to index of TraceMessage object in master message list messList

				addedToCD = 1; //alerts system that the message has been added to Ground Control Lists
			} else

				
			if((to.getType() == AgentPair.AgentType.LOCAL_CONTROL)  && (addedToLC == 0)){
				//If the message is for Local Control users
				//add the message to Local Control lists

				toPrefix = "LC(" + name + "):"; //Prefix before message text

				completeMessage = "<b>" + toPrefix + "</b> " + messageText + "<br><br>";
					//Complete message formatted with html bold prefix

				messLCAll = messLCAll + completeMessage;
				//add to end of GC message string

				messLC.add(index);
				//add reference to index of TraceMessage object in master message list messList

				addedToLC = 1; //alerts system that the message has been added to Local Control Lists
			}

			if((to.getType() == AgentPair.AgentType.PILOT)  && (addedToPilot == 0)){
				//If the message is for Pilot users
				//add the message to Pilot lists


				if(to.getName() != null){ //the message is meant for only one pilot user

					String pName = to.getName(); //get user's name

					toPrefix = "Pilot(" + pName + "):"; //Prefix before message text

					completeMessage = "<b>" + toPrefix + "</b> " + messageText + "<br><br>";
						//Complete message formatted with html bold prefix

					if(messPilot.containsKey(pName)){  //if the pilot exists in system

						Vector<Integer> pIndex = messPilot.get(pName); //get index list
						pIndex.add(index);  //add to list
						messPilot.put(pName, pIndex);	//replace list

						String pString = messPilotStrings.get(pName); //get pilot's string
						pString = pString + completeMessage; //add message to string
					}
					else{
					//The pilot is new to system and we must enter pilot's name

						Vector<Integer> pIndex = new Vector<Integer>(); //create new index list
						pIndex.add(index); //add to index list
						messPilot.put(pName, pIndex); //add list to map

						messPilotStrings.put(pName,completeMessage); // start string of messages for pilot

					}//end specific pilot user case
				}else

				{//The message is a broadcast to all KNOWN pilot users
				//NOTE: this is not a true broadcast, because only users which have
				//previously interacted with the system will receive the message


					toPrefix = "ALL PILOTS:"; //Prefix before message text

					completeMessage = "<b>" + toPrefix + "</b> " + messageText + "<br><br>";
						//Complete message formatted with html bold prefix


					Set<String> pilotUsers = messPilot.keySet();  //All KNOWN pilot Users as Set

					for(Iterator it = pilotUsers.iterator(); it.hasNext();){

						String pilotName = (String)it.next(); //get next name

	  					 if(name!=pilotName){//if this user did not send the message

							//add the message to user's list of message indices
							Vector<Integer> pilotIndex = messPilot.get(name);//get list
							pilotIndex.add(index);	//add to list
							messPilot.put(name, pilotIndex); //replace list
							}
					}//end for loop

					//Now, using the same user list, add to String of Pilot Messages (messPilotStrings)

					for(Iterator it = pilotUsers.iterator(); it.hasNext();){

						String pilotName = (String)it.next(); //get next name

	  					 if(name!=pilotName){//if this user did not send the message

							//add the message to user's message string
							String pilotString = messPilotStrings.get(name);//get string
							pilotString = pilotString + completeMessage;	//add to end of string
							messPilotStrings.put(name, pilotString); //replace string
							}
					}//end for loop

				}//end Pilot Broadcast case

				messPilotAll = messPilotAll + completeMessage;
				//add to end of Pilot message string


				addedToPilot = 1; //alerts system that the message has been added to Pilot string

			}//end PILOT case

		}//end message to specific user case



//////////////////////////////This Section will handle FROM Data

		AgentPair from = tm.getFrom();

		String name = from.getName();

		if((from.getType() == AgentPair.AgentType.GROUND_CONTROL) && (addedToGC == 0)){
			//If from Ground Control Agent

			messGCAll = messGCAll + completeMessage;
				//add to end of GC message string

			messGC.add(index);
				//add reference to index of TraceMessage object in master message list messList

			addedToGC = 1; //alerts system that the message has been added to Ground Control Lists


		}else

		if((from.getType() == AgentPair.AgentType.CLEARANCE_DELIVERY) && (addedToCD == 0)){
			//If from Ground Control Agent

			messCDAll = messCDAll + completeMessage;
				//add to end of GC message string

			messCD.add(index);
				//add reference to index of TraceMessage object in master message list messList

			addedToCD = 1; //alerts system that the message has been added to Ground Control Lists


		}else

			
		if((from.getType() == AgentPair.AgentType.LOCAL_CONTROL) && (addedToLC == 0)){
			//If from Local Control Agent

			messLCAll = messLCAll + completeMessage;
				//add to end of LC message string

			messLC.add(index);
				//add reference to index of TraceMessage object in master message list messList

			addedToLC = 1; //alerts system that the message has been added to Local Control Lists

		} else

		if(from.getType() == AgentPair.AgentType.PILOT){

			//If from Pilot Agent

			if(messPilotStrings.containsKey(name)){ //check if this is the first message related to this pilot

				Vector<Integer> currentIndex = messPilot.get(name); //get current index list
				currentIndex.add(index); //add index reference
				messPilot.put(name, currentIndex); //replace list

				String currentString = messPilotStrings.get(name); //get string
				currentString = currentString + completeMessage;   //add new message to end
				messPilotStrings.put(name, currentString);	   //replace in map

			}else{

				Vector<Integer> newIndex = new Vector<Integer>(); //make a new list
				newIndex.add(index);  //add index reference
				messPilot.put(name, newIndex); //add index list to pilot message map

				messPilotStrings.put(name,completeMessage);
					//add this message to the string of messages for this pilot user

			}

			if(addedToPilot == 0){

			messPilotAll = messPilotAll + completeMessage; //add to end of pilot message string

			addedToPilot = 1; //alerts system that the message has been added to Pilot String

			}



		}

		messListStrings.add(completeMessage); //Add the string to list of all message strings

		messAll = messAll + completeMessage;




/////////////////////////UPDATE CONTROL VARIABLES
//Prepare for next message

index++;

addedToLC = 0;
addedToGC = 0;
addedToPilot = 0;
addedToCD = 0;

	} //end IF message


}//end new message function

//Access Methods

public String getLCString(){
	/**
	 * returns one string containing all Local Control messages
	 */
	return messLCAll;
}

public String getAllString(){
	/**
	 * returns one string containing messages of all types
	 */
	return messAll;
}

public String getGCString(){
	/**
	 * returns one string containing all Ground Control messages
	 */
	return messGCAll;
}

public String getCDString() {
	return messCDAll;
}

}//end class

















