package agents;
import java.util.*;
import java.util.concurrent.*;

import agent.*;
import agents.Flight.FlightType;
import interfaces.*;
import guiIntegration.*;
import airport.objects.*;
import airport.TheSimulator;
/*
 * @author Prateek Tandon
 */
public class PilotAgent extends Agent implements Pilot {

	public final static int THRESHOLD_RADIUS = 10;

	/*
	 * Flight variable
	 */
	private Flight flight;

	/*
	 * Pilot possible states
	 */
	public enum PilotState
	{AWAITING_RESPONSE,
		FLYING,
		SAW_RUNWAY,
		CLEARED_TO_LAND,
		LANDING,
		DOCKED,
		DOCKED_SUCCESSFULLY,
		MUST_CONTACT_GROUND,
		CONTACTING_GROUND,
		NAVIGATING_DEPARTURE_ROUTE,
		NAVIGATING_DOCK_ROUTE,
		DONE,
		REQUEST_CLEARANCE,
		CLEARANCE_GRANTED,
		REQUEST_PUSHBACK,
		CLEARED_TO_PUSHBACK, MUST_CONTACT_LC, HOLDING, REQUEST_TAKEOFF, TAKEOFF, TAKEN_OFF, INITIAL}

	/*
	 * The name of the agent
	 */
	String name;

	/*
	 * Current state this pilot is in.
	 */
	PilotState state;

	/*
	 * the pilot's trusty lil airplane
	 */
	Airplane airplane;

	/*
	 * Knowledge of the airport
	 */
	Airport airport;

	/*
	 * Mapping of the radio frequencies to the Agents sitting on
	 * those radio frequencies. The Pilot knows about these from the
	 * onset of the program.
	 */
	public Map<Double, ATC> radio;

	/*
	 * Route directions given by ATCs that the pilot must follow
	 * Once the pilot finishes following these directions, he is DONE.
	 */
	Queue<String> directions;

	AgentPair myPair;

	/*
	 * Represents a command the pilot must echo to a given ATC
	 */
	class EchoedCommand {

		public EchoedCommand(String command, ATC replyTo, EchoType echoType) {
			this.command = command;
			this.replyTo = replyTo;
			this.echoType = echoType;
		}

		private String command; //command to echo
		private ATC replyTo; //agent who echo must be sent to; ATC is a base interface that all ATCs should implement.
		private EchoType echoType; //type of echo
	}

	/*
	 * FIFO queue for echoing commands.
	 */
	Queue<EchoedCommand> commandsToEcho;

	/*
	 * Refers to the ground controller assigned to the Pilot
	 */
	public GroundControl myGC = null;

	/*
	 * Hacked Connected to Local Controller
	 */
	public LocalControl myLC = null;

	/*
	 * Agent Pair for this agent.
	 */
	public PilotAgent(String name,Airport airport) {
		this.name = name;
		myPair = new AgentPair(name, AgentPair.AgentType.PILOT, voiceSemaphore);

		airplane = new Airplane();
		airplane.setName(name);

		airplane.setActive(false);

		//basic flight data  (hardcoded for test)
		flight = null;

		state = PilotState.INITIAL;

		radio = Collections.synchronizedMap(new HashMap<Double, ATC>());

		//init data structures
		directions = new LinkedBlockingQueue<String>();
		commandsToEcho = new LinkedBlockingQueue<EchoedCommand>();

		this.airport = airport;

	}

	public void placePlane() {
		airplane.setActive(true);
		airplane.setAlive(true);
		TheSimulator.getInstance().addAirplane(airplane);
	}
	public void setFlight(Flight flight){
		this.flight = flight;
	}
	public void setStatus(PilotState status){
		this.state = status;
	}


	// ----------------MESSAGES

	/**
	 * @return the myGC
	 */
	public GroundControl getMyGC() {
		return myGC;
	}

	/**
	 * @param myGC the myGC to set
	 */
	public void setMyGC(GroundControl myGC) {
		this.myGC = myGC;
		radio.put(1.11, myGC);
	}

	/**
	 * @return the myLC
	 */
	public LocalControl getMyLC() {
		return myLC;
	}

	/**
	 * @param myLC the myLC to set
	 */
	public void setMyLC(LocalControl myLC) {
		this.myLC = myLC;
	}

	/**
	 * @return the myCD
	 */
	public ClearanceDelivery getMyCD() {
		return myCD;
	}

	/**
	 * @param myCD the myCD to set
	 */
	public void setMyCD(ClearanceDelivery myCD) {
		this.myCD = myCD;
	}

	/*
	 * Message recieved when pilot sees a runway. This initiates the dialog.
	 *
	 * @param The runway the pilot sees.
	 */
	public void msgISeeARunway(String runway) {

		//add the airplane to the Simulator
		TheSimulator.getInstance().addAirplane(airplane);

		String msg = "I see a runway - " + runway;
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, myPair,msg);
		state = PilotState.SAW_RUNWAY;
//		directions.add(runway);
		flight.setWay(runway);
		stateChanged();
	}

	/*
	 * Message from local control to pilot saying that its okay to initiate the landing sequence.
	 *
	 * @param lc The local controller that authorizes the landing
	 * @param the Runway identifier code in the form of "<RunwayName> <alpha | beta>"
	 * @param windCondition The current wind condition the landing will take place in
	 */
	public void msgClearedToLand(LocalControl lc, String runway,String windCondition) {
		String msg = "Cleared to land on runway " + runway + ". The wind condition is " + windCondition;
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, lc.getPair(), msg);
		state = PilotState.CLEARED_TO_LAND;
		commandsToEcho.add(new EchoedCommand("Cleared To Land on runway " + runway + ", Wind Condition: " + windCondition, lc, EchoType.CLEARED_TO_LAND));

		//create a "land" airplane action
		AirplaneAction action = new AirplaneAction();
		long time = 1000;

		String temp[] = runway.split(" ");
		String runwayname = temp[0];
		String runwaypoint = temp[1];

		//we have to prase the runway
		action.setName("land");
		action.setTime(time);
		action.addProperty("wayname",runwayname);
		action.addProperty("waypoint",runwaypoint);
		final Pilot me = this;

		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				myLC.msgIHaveLanded(me);
			}
		});

		//velocity and acceleration are taken care of by TheSimulator
		airplane.addAction(action);

		//the airplane is now active
		airplane.setActive(true);
		airplane.setAlive(true);

		stateChanged();
	}

	/*
	 * Message from local controller relaying the ground control's radio frequency to the pilot.
	 *
	 * @param lc The local controller that sends the message
	 * @param frequency The frequency of ground control on the radio
	 * @param additionalDirections Some other stuff that the pilot has to do
	 */
	public void msgPleaseContactGround(LocalControl lc, double  frequency,
			ArrayList<String> additionalDirections,
			ArrayList<AirplaneAction> actions) {
		String msg = "Please Contact Ground Control at frequency " + frequency
					+ ". Stick to route: " + stringify(additionalDirections);
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, lc.getPair(),msg);
		state = PilotState.MUST_CONTACT_GROUND;
		//myGC = (GroundControlAgent) radio.get(frequency);

		//parse the directions
		for (AirplaneAction action : actions) {
			airplane.addAction(action.clone());
		}

		//for the last action, make it signal (contact ground)
		List<AirplaneAction> actions2 =
			airplane.getActions();

		AirplaneAction action = actions2.get(actions2.size()-1);
		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				state = PilotState.CONTACTING_GROUND;
				stateChanged();
			}
		});

		commandsToEcho.add(
				new EchoedCommand(
						"Must Contact Ground, Frequency: " + frequency
						+ " and also do all this: "
						+ stringify(additionalDirections), lc, EchoType.CONTACT_GROUND_CONTROL));

		stateChanged();
	}

	/*
	 * Message from ground controller telling pilot what route to take through the airport
	 * to get to his gate.
	 *
	 * @param gc The ground control who sends the message
	 * @param routePoints The points to use on the route
	 */
	public void msgHereAreArrivalInstructions(GroundControl gc,
			ArrayList<String> additionalDirections,
			ArrayList<AirplaneAction> actions) {
		String msg = "Arrival stick to route: " + stringify(additionalDirections);
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, gc.getPair(),msg);

		state = PilotState.NAVIGATING_DOCK_ROUTE;
		commandsToEcho.add(new EchoedCommand("Instructions: " + stringify(additionalDirections), gc, EchoType.USE_THIS_ROUTE));

		//parse the directions
		for (AirplaneAction action : actions) {
			airplane.addAction(action.clone());
		}

		//for the last action, make it signal (contact ground)
		List<AirplaneAction> actions2 =
			airplane.getActions();

		AirplaneAction action = actions2.get(actions2.size()-1);
		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				state = PilotState.DOCKED;
				stateChanged();
			}
		});


		//when the pilot is done with this, he should be docked
		stateChanged();
	}

	/*
	 * Helper method to turn a collection of strings into a single strings delimited by commas.
	 *
	 * @return a string with commands consisting of elems of the collection
	 */
	private String stringify(Collection<String> c) {
		return StringUtil.separatedString(c,", ");
	}

	//---- Actions: Arrival  Scenario

	/*
	 * Action to request landing clearance from local controller.
	 */
	private void requestToLand() {
		Do("I am requesting to land");

		//hardcoded approach for now
		String approachType = "Visual Approach";

		myLC.msgReadyToLand(this, flight.getPlaneName(), approachType, flight.getWay());
		state = PilotState.AWAITING_RESPONSE;
		stateChanged();
	}

	/**
	 * Land the plane
	 */
	private void land() {
		//set its state to landing
		state = PilotState.LANDING;

	}

	/*
	 * Action to contact ground.
	 */
	private void contactGround() {
		Do("I am contacting ground.");
		myGC.msgHelloGround(this, flight.getPlaneName(), flight.getGate());
		state = PilotState.AWAITING_RESPONSE;
		stateChanged();
	}

	/**
	 * Notify GroundControl that we have docked successfully
	 *
	 */
	private void notifyGroundOfDock() {
		Do("I am notifying ground that I have docked.");
		myGC.msgIHaveDocked(this);
		state = PilotState.DOCKED_SUCCESSFULLY;
		stateChanged();
	}

	/*
	 * Action to echo the command
	 *
	 * @param ec The command to echo
	 */
	public void echoCommand(EchoedCommand ec) {
		//Do("ECHO: " + ec.command);

		ec.replyTo.msgEchoCommand(this, ec.command, ec.echoType);


		if(state!=PilotState.MUST_CONTACT_GROUND
				&& state!=PilotState.REQUEST_PUSHBACK
				&& state!=PilotState.MUST_CONTACT_LC
				&& state!=PilotState.NAVIGATING_DOCK_ROUTE &&
				state!=PilotState.NAVIGATING_DEPARTURE_ROUTE &&
				state != PilotState.CLEARED_TO_PUSHBACK) {
			state= PilotState.AWAITING_RESPONSE;
		}
		stateChanged();

	}
	//---Scheduler

	/*
	 * Scheduler function
	 */
	public boolean pickAndExecuteAnAction() {

		if(state==PilotState.DONE) {
			return true; //do nothing
		}
		else if(commandsToEcho.size() > 0) {
			echoCommand(commandsToEcho.remove()); //echo first command on queue
			return true;
		} else {

			//states involving landing
			if(state==PilotState.SAW_RUNWAY) {
				requestToLand();
				return true;
			} else if (state==PilotState.CLEARED_TO_LAND) {
				//land the plane
				land();
			}
			else if(state==PilotState.CONTACTING_GROUND) {
				contactGround();
				return  true;
			}
			else if(state==PilotState.DOCKED) {
				//first, check that we have actually docked
				if (isDocked()) {
					notifyGroundOfDock();
				}

			}
			//states involving take off
			if(state==PilotState.REQUEST_CLEARANCE)
				requestClearance();
			else if(state==PilotState.REQUEST_PUSHBACK) {
				requestPushBack();
			}
			else if(state==PilotState.CLEARED_TO_PUSHBACK) {
				pushBack();
			}
			else if(state==PilotState.MUST_CONTACT_LC)
				contactLC();

		}
		return  false;
	}

	//---Data [additional]: Departure Scenario

	/*
	 * Hacked connection to clearance delivery agent.
	 */
	public ClearanceDelivery myCD = null;

	//---Messages: Departure Scenario

	public void msgIWantToTakeOff(String gate, String destination) {
		//add the airplane to the Simulator

		state = PilotState.REQUEST_CLEARANCE;
		stateChanged();
	}

	/*
	 * Message from ground control telling pilot that clearance has been granted.
	 *
	 * @param ca The clearance delivery staff that authorizes the request
	 * @param runway The runway to take off from
	 * @param radioFrequency The frequency to stay on
	 * @param transponderCode The code to follow
	 */
	public void msgClearanceGranted(ClearanceDelivery ca, String runway, double radioFrequency, int transponderCode) {
		String msg =
			"Clearance Granted. Please use " + runway + " and contact ground at radio frequency at " + radioFrequency + " and code " + transponderCode;
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, ca.getPair(), msg);
		state = PilotState.CLEARANCE_GRANTED;
//		myGC = (GroundControlAgent) radio.get(radioFrequency);
		commandsToEcho.add(new EchoedCommand("Clearance Granted. Runway: " + runway + ", frequency: " + radioFrequency + ", Code: " + transponderCode, ca,  EchoType.CD_CLEARANCE_GRANTED));
		stateChanged();
	}

	/*
	 * Message from clearance delivery saying what mode to use.
	 *
	 * @param ca The clearance delivery agent
	 * @param guidanceMode the mode to use
	 * @param letter The letter code being used
	 */
	public void msgHereIsMode(ClearanceDelivery ca, String guidanceMode, char letter) {
		String msg ="Guidance Mode " + guidanceMode + " "+ letter;
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, ca.getPair(),msg);
		state = PilotState.REQUEST_PUSHBACK;
		commandsToEcho.add(new EchoedCommand("Mode: " + guidanceMode + ", letter: " + letter, ca, EchoType.HERE_IS_MODE));
		stateChanged();
	}

	/*
	 * Message from  ground control saying that pushback has been granted.
	 *
	 * @param gc The ground controller that authorizes the request
	 * @param gate The gate to taxi  to
	 */
	public void msgPushBackGranted(GroundControl gc, String gate)  {
		String msg = "Push back granted. Taxi from gate " + gate;
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, gc.getPair(),msg);
		state = PilotState.CLEARED_TO_PUSHBACK;

		commandsToEcho.add(new EchoedCommand("Push back granted: " + gate, gc, EchoType.PUSHBACK_GRANTED));

/*		//parse the directions
		for (AirplaneAction action : instructions.values()) {
				airplane.addAction(action.clone());
		}*/

		stateChanged();
	}

	/*
	 * Message from ground control to pilot with take off instructions.
	 *
	 * @param gc The ground controller
	 * @param instructions The instructions to follow
	 */
	public void msgHereAreDepartureInstructions(final GroundControl gc,
			ArrayList<String> additionalDirections,
			ArrayList<AirplaneAction> actions) {
		String msg = "Stick to route: " + stringify(additionalDirections);
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, gc.getPair(), msg);
		state = PilotState.NAVIGATING_DEPARTURE_ROUTE;	//this does nothing, basically
		commandsToEcho.add(new EchoedCommand("Instructions: " + stringify(additionalDirections),
				gc, EchoType.HERE_ARE_INSTRUCTIONS));

		//parse the directions
		//parse the directions
		for (AirplaneAction action : actions) {
			airplane.addAction(action.clone());
		}


		//for the last action, make it signal (contact local control) - but only
		//if we're on the special runway
		List<AirplaneAction> actions2 =
			airplane.getActions();

		AirplaneAction action = actions2.get(actions2.size()-1);

		final Pilot me = this;
		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				//check that we're on the special way, or at least, the current way
				//intersects it
				Way planeWay = airplane.getWay();
				Way specialWay = airport.getSpecialWay();

				System.out.println("Test3");

				if (planeWay == specialWay) {
					state = PilotState.MUST_CONTACT_LC;
					//let the gc know we're good
					gc.msgTaxiComplete(me);
					stateChanged();
				} else if (planeWay != null) {

					//see if the plane is sitting on the intersection
					AirportNode intersection = airport.getIntersectionNode(planeWay.getName(),specialWay.getName());

					if (intersection != null) {
						Compass planeCompass = airplane.getCompass();
						Compass specialCompass = intersection.getCompass();

						if (planeCompass.getDistanceTo(specialCompass) < THRESHOLD_RADIUS) {
							state = PilotState.MUST_CONTACT_LC;
							stateChanged();
							gc.msgTaxiComplete(me);
						}
					}

				} else {
				   	//find the nearest intersection to where the plane is now
			    	AirportNode nearest = ATCAgent.getClosestIntersection(airport,airplane.getCompass());

					if (nearest != null) {
						//first check that this intersection is on
						ArrayList<AirportNode> nodes = airport.getNearestIntersections(specialWay.getName());

						if (nodes.contains(nearest)) {
							airplane.setWay(specialWay);

							Compass planeCompass = airplane.getCompass();
							Compass specialCompass = nearest.getCompass();

							if (planeCompass.getDistanceTo(specialCompass) < THRESHOLD_RADIUS) {
								state = PilotState.MUST_CONTACT_LC;
								stateChanged();
								gc.msgTaxiComplete(me);
							}
						}
					}
				}
			}
		});


		stateChanged();
	}

	/*
	 * Message from local control saying to position and hold.
	 *
	 * @param la The local controller
	 */
	public void msgPositionAndHold(final LocalControl la) {
		String msg = "Position and Hold.";
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, la.getPair(),msg);
		state = PilotState.HOLDING;

		//now that we're on the special runway,
		//let's turn towards the beta point
		Compass endpoint = airport.getSpecialWay().getEndpointCompass("beta");
		AirplaneAction action = new AirplaneAction();
		action.setName("turntowards");
		action.addProperty("endpoint",endpoint);

		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				//only when we've finished turning do we echo back
				commandsToEcho.add(new EchoedCommand("Position and Hold", la, EchoType.POSITION_AND_HOLD));
				stateChanged();
			}
		});
		airplane.addAction(action);
	}

	/*
	 * Message from local control saying pilot is cleared for take off.
	 */
	public void msgClearedForTakeOff() {
		String msg = "Cleared for Take off";
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, myLC.getPair(), msg);
		state = PilotState.TAKEOFF;
		commandsToEcho.add(new EchoedCommand("Cleared for Takeoff", myLC, EchoType.CLEARED_FOR_TAKEOFF));

		//add a takeoff instruction
		AirplaneAction action = new AirplaneAction();
		action.setName("takeoff");

		final Pilot me = this;
		airplane.addAction(action);
		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				state = PilotState.TAKEN_OFF;
				myLC.msgIHaveTakenOff(me);
				stateChanged();
			}
		});

		stateChanged();

	}

	/*
	 * Message from local control saying pilot is finished take off sequence.
	 */
	public void msgGoodBye() {
		String msg = "Good Bye";
		Do(msg,false);
		TraceDB.getInstance().updateMessageTrace(myPair, myLC.getPair(), msg);
		state = PilotState.FLYING;
		stateChanged();
	}

	//---Departure Actions

	/*
	 * Action pilot uses to request clearance from c.d.
	 */
	public void requestClearance() {
		Do("I am requesting clearance.");
		myCD.msgRequestingClearance(this, flight.getPlaneName(), flight.getGate(), flight.getDestination());
		state = PilotState.AWAITING_RESPONSE;
	}

	/*
	 * Action that pilot uses to request pushback.
	 */
	public void requestPushBack()  {
		Do("I am requesting pushback.");
		//hardcoded param
		myGC.msgRequestPushBack(this, flight.getPlaneName(),  flight.getWay(), flight.getGate());
		state = PilotState.AWAITING_RESPONSE;
	}

	/*
	 * Action that pilot uses to taxi.
	 */
	public void pushBack() {
		Do("I am having my plane taxied.");
		myGC.msgPushbackComplete(this, flight.getLocation());
		state = PilotState.AWAITING_RESPONSE;
	}

	/*
	 * Action that pilot uses to contact local control.
	 */
	public void contactLC() {
		Do("I am contacting local control.");

		myLC.msgIAmOnRunway(this,airplane.getWay().getName());
		state = PilotState.AWAITING_RESPONSE;
	}

	public Airplane getAirplane() {
		return airplane;
	}

	public AgentPair getPair() {
		return myPair;
	}

	public void setAirport(Airport airport) {
		this.airport = airport;
	}
	public String toString() {
		return "Pilot " + name;
	}

	/**
	 * @return the state
	 */
	public PilotState getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(PilotState state) {
		this.state = state;
	}

	/**
	 * @return the radio
	 */
	public Map<Double, ATC> getRadio() {
		return radio;
	}

	/**
	 * @param radio the radio to set
	 */
	public void setRadio(Map<Double, ATC> radio) {
		this.radio = radio;
	}

	/**
	 * @return the directions
	 */
	public Queue<String> getDirections() {
		return directions;
	}

	/**
	 * @param directions the directions to set
	 */
	public void setDirections(Queue<String> directions) {
		this.directions = directions;
	}

	/**
	 * @return the commandsToEcho
	 */
	public Queue<EchoedCommand> getCommandsToEcho() {
		return commandsToEcho;
	}

	/**
	 * @param commandsToEcho the commandsToEcho to set
	 */
	public void setCommandsToEcho(Queue<EchoedCommand> commandsToEcho) {
		this.commandsToEcho = commandsToEcho;
	}

	/**
	 * @return the flight
	 */
	public Flight getFlight() {
		return flight;
	}

	public String getName() {
		return name;
	}

	public boolean isDocked() {

		//check if we're near our gate node
		AirportNode node = airport.getGateNode(flight.getGate());
		if (node == null) {
			System.out.println("PilotAgent.isDocked(): Error, " + flight.getGate() + " does not exist as a node!");
			return false;
		}

		Compass compass = airplane.getCompass();

		double distance = compass.getDistanceTo(node.getCompass());

		if (distance > THRESHOLD_RADIUS) return false;

		return true;
	}
}