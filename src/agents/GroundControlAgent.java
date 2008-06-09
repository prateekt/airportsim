/**
 *
 */
package agents;

import java.util.*;

import agent.StringUtil;
import interfaces.*;
import guiIntegration.*;
import airport.*;
import airport.objects.*;

/**
 * @author Henry Yuen
 *
 */
public class GroundControlAgent extends ATCAgent implements GroundControl {

	/*
	 * Constructor for Ground Control Agent.
	 */
	public GroundControlAgent(String name,Airport airport) {
		super(name,airport);
		myPair = new AgentPair(name, AgentPair.AgentType.GROUND_CONTROL, voiceSemaphore);

	}

	/* (non-Javadoc)
	 * @see agent.Agent#pickAndExecuteAnAction()
	 */
	public boolean pickAndExecuteAnAction() {


		//ARRIVAL SCHEDULING CODE////////////////////////////////

		//check if there are arrivals
		synchronized (arrivals) {
			if (arrivals.size() > 0) {
				//there are arrivals to service
				for (ArrivalMediator arrival : arrivals.values()) {
					if (arrival.getStatus() == ArrivalMediator.ArrivalStatus.Landed) {
						giveArrivalDirections(arrival);
						return true;
					}
				}

				for (ArrivalMediator arrival : arrivals.values()) {
					if (arrival.getStatus() == ArrivalMediator.ArrivalStatus.Docked) {
						removeArrival(arrival);
						return true;
					}
				}

				return true;
			}
		}

		//check if pushback is requested
		synchronized (departures) {
			if (departures.size() > 0) {
				//see if there exists one such that they're requesting pushback, service those first
				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() ==
						DepartureMediator.DepartureStatus.RequestingPushback) {
						//we've got a good candidate
						Flight flight = departure.getFlight();
						Pilot pilot = pilots.get(flight);
						handlePushBack(flight,pilot,departure);
						return true;
					}
				}

				//finally, check if there exists one such that its status is they're
				//requesting directions from the Ground Control
				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() ==
						DepartureMediator.DepartureStatus.RequestingDirections) {
						//we've got a good candidate
						Flight flight = departure.getFlight();
						Pilot pilot = pilots.get(flight);
						//check if the pilot is verified yet
						if (getPilotVerified(pilot)) {
							giveDepartureInstructions(flight,pilot,departure);
							return true;
						}
					}
				}

				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() == DepartureMediator.DepartureStatus.AwaitingTakeOff) {
						removeDeparture(departure);
						return true;
					}
				}

			}
		}

		//now check if there are any commands to verify
		if (commandsToVerify.size() > 0) {
			if (doVerification())
				return true;
		}

		return false;
	}

	private boolean doVerification() {
		Command command = commandsToVerify.poll();
		boolean r = verifyCommand(command);

		if (r) {
			//it was successful
			setPilotVerified(command.getPilot(),true);

			//get the echotype
			EchoType type = command.getEchoType();

			if (type == EchoType.USE_THIS_ROUTE) {

			} else if (type == EchoType.PUSHBACK_GRANTED) {
				DepartureMediator departure =
					departures.get(command.getPilot());

				departure.setStatus(DepartureMediator.DepartureStatus.RequestingDirections);
			}


			return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see interfaces.GroundControl#msgHelloGround(interfaces.Pilot, java.lang.String, java.lang.String)
	 */
	public void msgHelloGround(Pilot pilot, String planeName,String gate) {

		String msg = "Ground Control, I'm" + pilot + " at designation frequency. Directions pronto.";
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		//The pilot just contacted the Ground, presumably he was transferred
		//over from the Local Control

		//create a new ArrivalMediator object

		Flight flight = new Flight(gate,null,null,Flight.FlightType.Arrival);
		ArrivalMediator arrival = new ArrivalMediator(flight);
		arrival.setStatus(ArrivalMediator.ArrivalStatus.Landed);

		//add this new arrival into the arrivals list
		arrivals.put(pilot,arrival);

		pilots.put(flight,pilot);
		stateChanged();
	}

	public void msgIHaveDocked(Pilot pilot) {
		String msg = "Received docked message from " + pilot;
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		if (!arrivals.containsKey(pilot)) {
			Do("Received taxi complete message from " + pilot + " but there was no prior interactions!");
			//raise risk
			return;
		}

		ArrivalMediator arrival = arrivals.get(pilot);
		//make sure the last thing was that it was requesting pushback
		if (arrival.getStatus() !=
			ArrivalMediator.ArrivalStatus.Taxiing) {
			//egregious error
			//raise risk
			return;
		}

		//set its new status
		arrival.setStatus(ArrivalMediator.ArrivalStatus.Docked);
		stateChanged();

	}




	/**
	 * Contacted by the pilot to do pushback. This is
	 * first occurence of communication from the pilot to the ground control
	 * in the departure case.
	 */
	public void msgRequestPushBack(Pilot pilot,String planeName,
			String runway,
			String gate) {

		String msg =  pilot + " here. Requesting pushback.";
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		Flight flight = new Flight(gate,runway,null,Flight.FlightType.Departure);
		DepartureMediator departure = new DepartureMediator(flight);
		departure.setStatus(DepartureMediator.DepartureStatus.RequestingPushback);

		departures.put(pilot,departure);

		pilots.put(flight,pilot);
		stateChanged();

	}

	/* (non-Javadoc)
	 * @see interfaces.GroundControl#msgTaxiComplete(interfaces.Pilot, java.lang.String, java.lang.String)
	 */
	public void msgPushbackComplete(Pilot pilot, String currentLoc) {
		String msg = "Pushback complete.";
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		//search for existing DepartureMediators
		if (!departures.containsKey(pilot)) {
			Do("Received taxi complete message from " + pilot + " but there was no prior interactions!");
			//raise risk
			return;
		}

		DepartureMediator departure = departures.get(pilot);
		//make sure the last thing was that it was requesting pushback
		if (departure.getStatus() !=
			DepartureMediator.DepartureStatus.RequestingPushback) {
			//egregious error
			//raise risk
			return;
		}

		//set its new status
		departure.setStatus(DepartureMediator.DepartureStatus.RequestingDirections);
		stateChanged();

	}

	public void msgTaxiComplete(Pilot pilot) {
		String msg = "Taxi complete.";
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		//search for existing DepartureMediators
		if (!departures.containsKey(pilot)) {
			Do("Received taxi complete message from " + pilot + " but there was no prior interactions!");
			//raise risk
			return;
		}

		DepartureMediator departure = departures.get(pilot);
		//make sure the last thing was that it was requesting pushback
		if (departure.getStatus() !=
			DepartureMediator.DepartureStatus.RequestingDirections) {
			//egregious error
			//raise risk
			return;
		}

		//set its new status
		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingTakeOff);
		stateChanged();


	}

	//--Arrivals

	/**
	 * This gives directions to the taxiing arrival flight
	 * @param arrival The arrival mediator object
	 */
	private void giveArrivalDirections(ArrivalMediator arrival) {
		Do("I'm giving the pilot directions to his gate");
		if (arrival == null) return;	//invalid arrival mediator object

		Flight flight = arrival.getFlight();

		//compute additional instructions for the pilot
		Pilot pilot = pilots.get(flight);
		if (pilot == null) {
			//oh no! egregious error!
			Do("No pilot associated for flight " + flight);
			return;
		}

		arrival.setStatus(ArrivalMediator.ArrivalStatus.Taxiing);

		//plan a path to the gate
	   	Airplane airplane = pilot.getAirplane();

    	//the plane has landed, we're going to assume he's still on a runway
    	//let's find a preliminary path to his gate
    	String gate = pilot.getFlight().getGate();
    	AirportNode end = airport.getGateNode(gate);

    	//find the nearest intersection to where the plane is now
    	AirportNode nearest = this.getClosestIntersection(airport,airplane.getCompass());

    	//see if we can plan a route?
    	ArrayList<AirportNode> path = findPath(nearest,end);

    	//there is no path to the gate specified!
    	if (path == null) {
    		System.out.println("giveArrivalDirections(): error, there does not exist a path to the gate specified!");
    		return;
    	}

    	ArrayList<String> commands = new ArrayList<String>();
    	//convert this path to commands
    	ArrayList<AirplaneAction> actions =
    		this.convertPathToAirplaneActions(airport,airplane.getWay().getName(),path,commands);

		pilot.msgHereAreArrivalInstructions(this, commands,actions);
		setPilotVerified(pilot,false);

		//now fill out the commands issued
		issueCommand(pilot,new Command(pilot,
				"Must use this route: " + StringUtil.separatedString(commands,", "),
				EchoType.USE_THIS_ROUTE));

	}

	private synchronized void removeArrival(ArrivalMediator arrival) {
		Flight flight = arrival.getFlight();
		Pilot pilot = pilots.get(flight);
		arrivals.remove(pilot);
	}

	private synchronized void removeDeparture(DepartureMediator departure) {
		Flight flight = departure.getFlight();
		Pilot pilot = pilots.get(flight);
		departures.remove(pilot);
	}

	//-- Departures

	private void handlePushBack(Flight flight,Pilot pilot,DepartureMediator departure) {
		Do("I'm handling pushback.");
		if (departure == null) return;

		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);

		//tell the pilot that this pushback was granted
		pilot.msgPushBackGranted(this,flight.getGate());
		setPilotVerified(pilot,false);

		issueCommand(pilot,new Command(pilot,
				"Push back granted: " + flight.getGate(),
				EchoType.PUSHBACK_GRANTED));

	}

	private void giveDepartureInstructions(Flight flight,Pilot pilot,DepartureMediator departure) {
		Do("I am giving the pilot departure instructions.");
		if (departure == null) return;

		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);

		//remove it from the departures map
		departures.remove(pilot);

    	Airplane airplane = pilot.getAirplane();

    	//find the nearest intersection to where the plane is now
    	AirportNode nearest = this.getClosestIntersection(airport,airplane.getCompass());

    	//randomly pick a "special" route
    	AirportNode end = airport.getSpecialWayLaunchPoint();	//this is alpha, by default

    	if (end == null) {
    		System.out.println("giveDepartureInstructions(): egregious error, airport does not have any special intersections");
    		return;
    	}


    	//see if we can plan a route?
    	ArrayList<AirportNode> path = findPath(nearest,end);

    	//there is no path to the special point specified!
    	if (path == null) {
    		System.out.println("giveDepartureInstructions(): error, there does not exist a path to the special point specified!");
    		return;
    	}

    	ArrayList<String> commands = new ArrayList<String>();

    	//generate commands
    	ArrayList<AirplaneAction> actions =
    		this.convertPathToAirplaneActions(airport,airplane.getWay().getName(),path,commands);

		pilot.msgHereAreDepartureInstructions(this, commands,actions);
		setPilotVerified(pilot,false);

		issueCommand(pilot,new Command(pilot,
				"Instructions: " + StringUtil.separatedString(commands,", "),
				EchoType.HERE_ARE_INSTRUCTIONS));

	}

	//GETTERS AND SETTERS FOR THE UNIT TESTS////////////////////////////////////////////////////////////

}
