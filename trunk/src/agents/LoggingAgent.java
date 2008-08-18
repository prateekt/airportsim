//
//  LoggingAgent.java
//  FAA_Control
//
//
//  Created by Riley Marsh on 10/30/07.
//  Copyright 2007 Riley Marsh. All rights reserved.
//
package agents;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import agent.Agent;
import airport.Database;
import airport.Log;

public class LoggingAgent extends Agent{

	private static LoggingAgent log = new LoggingAgent("Logster");
	private String name;

	/** Constructor
	 * @param name The name of the LoggingAgent
	*/
	private LoggingAgent(String name){
		super();
		System.out.println("LoggingAgent Created");
		this.name = name;

	}

	/** Method to return the single instance of LoggingAgent so there is only
	 * one copy of the agent.
	 * @return LoggingAgent The single LoggingAgent created for the program
	 */
	public static LoggingAgent getInstance(){
		return log;
	}

	// DATA //////////////////////////////////////////////////////

	/** A data class to store the date of the requested log as well as the agent
	 * to which the log should be returned.
	 */
	private class LogRequest {
		Date requestTime;
		Agent requestAgent;

		/** Constructor
		 * @param requestTime The date and time of the requested log
		 * @param requestAgent The agent requesting the log
		 */
		public LogRequest(Date requestTime, Agent requestAgent){
			this.requestTime = requestTime;
			this.requestAgent = requestAgent;
		}

		/** Accessor method for request time.
		 * @return Date The date of the log that is requested
		 */
		public Date getRequestTime(){ return requestTime; }

		/** Accessor method for request agent.
		 * @return Agent The agent to which we should return the log
		 */
		public Agent getRequestAgent(){ return requestAgent; }
	}


	private List<Log> loggableEvents
			= Collections.synchronizedList( new LinkedList<Log>() );

	private List<LogRequest> logRequests
			= Collections.synchronizedList( new LinkedList<LogRequest>() );




	// MESSAGES //////////////////////////////////////////////////

	/** Message recieved by LoggingAgent when a user is added to the system.
	 *	@param userID The int value of the added user's ID for logging.
	 */
	public void msgAddedUser(int userID){
		// could only take int userID? or should I use full User to have name, etc?
		Do("User["+userID+"] created");
		loggableEvents.add(new Log(new Date(), "userCreated", "created userID: "+userID));
		stateChanged();
	}

	/** Message recieved by LoggingAgent when a user logs in to the system.
	 *	@param userID The int value of the added user's ID for logging.
	 */
	public void msgLoggedInUser(int userID){
		Do("User["+userID+"] logged in");
		loggableEvents.add(new Log(new Date(), "userLogin", "userID: "+userID+" logged in"));
		stateChanged();
	}

	/** Message recieved by LoggingAgent when the RiskManager finds a risk.
	 *	@param risk The Risk that was found.
	 */
	/*
	public void msgRiskMitigationFound(RiskMitigation risk){
		// is userID necessary?
		Do("Risk["+risk+"] found in knowledge base");
		loggableEvents.add(new Log(new Date(), "riskFound", "risk: "+risk+"\nmitigation(s): "
							+risk.getMitigations()));
		stateChanged();
	}
	*/

	/** Message recieved from ATCAssistant with it reports a RiskMitigation to the user.
	*   @param risk The Risk that was found.
	*/
	/*
	public void msgRiskMitigationOutput(RiskMitigation risk){
		// gets riskMitigation with priority level associated
		Do("Risk["+risk+"] reported to user");
		loggableEvents.add(new Log(new Date(), "riskReported", "risk: "+risk+"\npriority: "
							+risk.getSeverity()+"\nmitigation(s): "+risk.getMitigations()));
		stateChanged();
	}
	*/

	/** Message recieved from ATCAssistant when a user chooses to execute an action
	 *  @param executedAction The action the user chose
	 *  @param userID The user who made the choice
	*/
	/*
	public void msgActionExecuted(Action executedAction, int userID){
		// gets action from ATC that the user executed.
		Do("User["+userID+"] executed action["+executedAction+"]");
		loggableEvents.add(new Log(new Date(), "actionExecuted", "action: "+executedAction+"\nuser: "
							+userID));
		stateChanged();
	}
	*/



	// SCHEDULER /////////////////////////////////////////////////
	protected boolean pickAndExecuteAnAction(){
		if ( !logRequests.isEmpty() ){
			retrieveLog(logRequests.remove(0));
			return true;
		}
		else if( !loggableEvents.isEmpty() ){
			writeLog(loggableEvents.remove(0));
			return true;
		}
		return false;

	}



	// ACTIONS ///////////////////////////////////////////////////

	/** Action to compile a chunk of log and send it to the database
	 *  @param newLog A log to be added to the database
	 */
	private void writeLog(Log newLog){
		// Sort log entries by time
		Do("Writing log to database");
		// Send log to database
		Database.writeLog(newLog);
	}

	/* Action to retrieve a log for a specific agent and from a specific time.
	 *	@param request The internal LogRequest class that contains the Log and the requestingAgent.
	 */
	private void retrieveLog(LogRequest request){
		// retrieve log from database
		Do("Retrieving log from database");
		Log retrievedLog = Database.retrieveLog(request.getRequestTime());
		// send log to requesting agent
		Do( "Sending log from "+retrievedLog.getLogTime()+" to "+request.getRequestAgent() );
		//request.getRequestAgent().msgHereIsLog(retrievedLog);

	}

	// HELPER METHODS //////////////////////////////////////////////////

	/** Method to return the agent's name.
	 * @return String The LoggingAgent's name
	 */
	public String getName(){
		return this.name;
	}
}
