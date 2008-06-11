package agents;
import java.util.*;
import java.util.concurrent.*;
import agent.*;
import interfaces.*;
import guiIntegration.*;
import airport.*;
import airport.objects.*;

/*
 * @author Patrick Monroe, Prateek Tandon
 */
public class ClearanceAgent extends ATCAgent implements ClearanceDelivery {

	/*
	 * Constructor for Clearance Agent
	 */
	public ClearanceAgent(String name,Airport airport){
		super(name,airport);
		myPair = new AgentPair(name, AgentPair.AgentType.CLEARANCE_DELIVERY, voiceSemaphore);
	}

	// ----------------MESSAGES

	/*
	 * Message from pilot asking for clearance
	 *
	 * @param pilot The pilot who sent the clearance request
	 * @param planeName The name of the plane the pilot controls
	 * @param gate The gate where the pilot is at
	 * @param destination Where the pilot must go
	 */
	public void msgRequestingClearance(Pilot pilot, String planeName, String gate, String way){
		Do("Pilot " + pilot + ". Requesting clearance.",false);
		TraceDB.getInstance().updateMessageTrace(myPair, pilot.getPair(), "Pilot " + pilot + ". Requesting clearance.");

		//create a new departuremediator
		Flight flight = new Flight(gate,way,null,Flight.FlightType.Departure);
		DepartureMediator departure = new DepartureMediator(flight);
		departure.setStatus(DepartureMediator.DepartureStatus.RequestingClearance);

		departures.put(pilot,departure);

		pilots.put(flight,pilot);
		stateChanged();
	}

	//---Scheduler

	/*
	 * Scheduler for Clearance Agent
	 */
	public boolean pickAndExecuteAnAction() {

		if (doVerification()) return true;

		synchronized (departures) {
			if(departures.size() > 0){
				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() == DepartureMediator.DepartureStatus.RequestingClearance) {
						processRequest(departure);
					}
				}
				return true;
			}
		}

		return  false;
	}


	private boolean doVerification() {
		synchronized (commandsToVerify) {
			if(commandsToVerify.size() > 0) {
				Command c = commandsToVerify.remove();
				if(verifyCommand(c)) {
					if(c.getEchoType()==EchoType.CD_CLEARANCE_GRANTED) {
						givePilotMode(c.getPilot());
						departures.remove(c.getPilot());
					}
				}
				return true;
			}
		}

		return false;

	}

	//--ACTIONS

	/*
	 * Action to process a single clearance request.
	 *
	 * @param cr The clearance request to process
	 */
	private void processRequest(DepartureMediator departure) {
		//Hardcoded params for now
		double  frequency = 1000;
		int code = 89;

		//get the flight associated with the pilot
		Flight flight = departure.getFlight();

		Pilot pilot = pilots.get(flight);

		Do("Processing clearance request from " + pilot);
		pilot.msgClearanceGranted(this, flight.getWay(), frequency, code);

		//Queue command to commands issued
		String command = "Clearance Granted. Runway: " + flight.getWay() + ", frequency: " + frequency + ", Code: " + code;
		issueCommand(pilot,new Command(pilot,command,EchoType.CD_CLEARANCE_GRANTED));
	}

	/*
	 * Action giving pilot the mode  of guidance.
	 *
	 * @param p The pilot to give the mode to
	 */
	private void givePilotMode(Pilot p) {
		//Hardcoded params for now
		String guidanceMode = "VISUAL";
		char mode = '1';

		Do("Giving pilot mode.");
		p.msgHereIsMode(this, guidanceMode, mode);

		//Queue command to commands issued
		String command = "Mode: " + guidanceMode + ", letter: " + mode;
		issueCommand(p,new Command(p,command,EchoType.HERE_IS_MODE));
	}
}



