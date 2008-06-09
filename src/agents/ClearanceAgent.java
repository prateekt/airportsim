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
	 * The clearance request models a request by a pilot for clearance.
	 */
	class ClearanceRequest{
		Pilot pilot;
		String planeName;
		String gate;
		String destination;

		public ClearanceRequest(Pilot pilot, String planeName, String gate, String destination){
			this.pilot = pilot;
			this.planeName = planeName;
			this.gate = gate;
			this.destination = destination;
		}

		public Pilot getPilot(){
			return pilot;
		}

		public String getPlaneName(){
			return planeName;
		}
		public String getGate(){
			return gate;
		}
		public String getDestination(){
			return destination;
		}
	}

	/*
	 * The queue of clearance requests the clearance agent has.
	 */
	private Queue<ClearanceRequest> clearanceRequests;

	public Queue<ClearanceRequest> getClearanceRequests() {
		return clearanceRequests;
	}
	
	/*
	 * Constructor for Clearance Agent
	 */
	public ClearanceAgent(String name,Airport airport){
		super(name,airport);
		myPair = new AgentPair(name, AgentPair.AgentType.CLEARANCE_DELIVERY, voiceSemaphore);
		clearanceRequests = new LinkedBlockingQueue<ClearanceRequest>();
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
	public void msgRequestingClearance(Pilot pilot, String planeName, String gate, String destination){
		Do("I'm " + pilot + ". Requesting clearance.",false);
		TraceDB.getInstance().updateMessageTrace(myPair, pilot.getPair(), "I'm " + pilot + ". Requesting clearance.");
		ClearanceRequest cr = new ClearanceRequest(pilot, planeName, gate, destination);
		clearanceRequests.add(cr);
		stateChanged();
	}

	//---Scheduler

	/*
	 * Scheduler for Clearance Agent
	 */
	public boolean pickAndExecuteAnAction() {
		
		synchronized (commandsToVerify) {
			if(commandsToVerify.size() > 0) {
				Command c = commandsToVerify.remove();
				if(verifyCommand(c)) {
					if(c.getEchoType()==EchoType.CD_CLEARANCE_GRANTED) {
						givePilotMode(c.getPilot());
					}
				}
				return true;
			}
		}

		synchronized (clearanceRequests) {
			if(clearanceRequests.size() > 0){
				processRequest(clearanceRequests.remove());
				return true;
			}
		}

		return  false;
	}

	//--ACTIONS

	/*
	 * Action to process a single clearance request.
	 *
	 * @param cr The clearance request to process
	 */
	private void processRequest(ClearanceRequest cr) {
		//Hardcoded params for now
		double  frequency = 1000;
		int code = 89;

		//get the flight associated with the pilot
		Flight flight = cr.getPilot().getFlight();

		Do("Processing clearance request from " + cr.getPilot());
		cr.getPilot().msgClearanceGranted(this, flight.getWay(), frequency, code);

		//Queue command to commands issued
		String command = "Clearance Granted. Runway: " + flight.getWay() + ", frequency: " + frequency + ", Code: " + code;
		issueCommand(cr.getPilot(),new Command(cr.getPilot(),command,EchoType.CD_CLEARANCE_GRANTED));
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



