/**
 *
 */
package agents;

import gui.GroundControlPanel;
import guiIntegration.AgentPair;
import interfaces.GroundControl;
import interfaces.Pilot;

import java.util.ArrayList;

import agent.StringUtil;
import airport.objects.AirplaneAction;
import airport.objects.Airport;

/**
 * @author Henry Yuen
 *
 */
public class UserGroundControlAgent extends ATCAgent implements GroundControl {

	GroundControlPanel groundControlPanel;

	/*
	 * Constructor for Ground Control Agent.
	 */
	public UserGroundControlAgent(String name,Airport airport,GroundControlPanel panel) {
		super(name,airport);
		myPair = new AgentPair(name, AgentPair.AgentType.GROUND_CONTROL, voiceSemaphore);
		groundControlPanel = panel;
		groundControlPanel.setGroundControlAgent(this);
	}

	protected boolean pickAndExecuteAnAction() {
		//check if there are arrivals
		synchronized (arrivals) {
			if (arrivals.size() > 0) {

				for (ArrivalMediator arrival : arrivals.values()) {
					if (arrival.getStatus() == ArrivalMediator.ArrivalStatus.Docked) {
						removeArrival(arrival);
						return true;
					}
				}
			}
		}


		synchronized (departures) {
			if (departures.size() > 0) {
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
			if (doCommandVerification())
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

		groundControlPanel.newPilotMessage(pilot,"Hello Ground Control. Awaiting arrival instructions.",arrival);

		stateChanged();
	}

	public void msgIHaveDocked(Pilot pilot) {

		//said pilot has docked
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

		groundControlPanel.newPilotMessage((Pilot)pilot,"Hello Ground Control. Requesting Pushback.",departure);

		stateChanged();

	}

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
			DepartureMediator.DepartureStatus.RequestingPushback &&
			departure.getStatus() !=
			DepartureMediator.DepartureStatus.Pushback) {
			//egregious error
			//raise risk
			return;
		}

		//set its new status
		departure.setStatus(DepartureMediator.DepartureStatus.RequestingDirections);
		groundControlPanel.update();
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
		groundControlPanel.update();
		stateChanged();

	}

	private boolean doCommandVerification() {
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


				//this is only if it took time for the
				//pushback to happen
				if (departure != null && departure.getStatus() == DepartureMediator.DepartureStatus.RequestingPushback) {
					departure.setStatus(DepartureMediator.DepartureStatus.Pushback);
					groundControlPanel.update();
				}
			}


			return true;
		}

		return false;
	}

	/*
	 * 	GUI Interface Functions. The glue code between
	 *  the GUI and the UserGroundControl. For example, when the user
	 *  has finished selecting his waypoints to the plane, the GUI
	 *  will forward those waypoints to the giveArrivalRouteToPilot() function, which
	 *  forwards it to the pilot. It is essentially an action.
	 */
	//this is called by the groundcontrolpanel
	public void giveArrivalRouteToPilot(Pilot pilot,ArrayList<String> commands,ArrayList<AirplaneAction> actions) {


		ArrivalMediator arrival = arrivals.get(pilot);
		if (arrival == null) {
			//this pilot doesn't exist!
			//raise an error in the gui
			groundControlPanel.raiseErrorMessage("Not a valid pilot to give an arrival route to");
			return;
		}

		arrival.setStatus(ArrivalMediator.ArrivalStatus.Taxiing);

		pilot.msgHereAreArrivalInstructions(this,commands,actions);

		setPilotVerified(pilot,false);

		//now fill out the commands issued
		issueCommand(pilot,new Command(pilot,
				"Must use this route: " + StringUtil.separatedString(commands,", "),
				EchoType.USE_THIS_ROUTE));

		//update the UI accordingly
		groundControlPanel.update();

	}


	public void giveDepartureRouteToPilot(Pilot pilot,ArrayList<String> commands,ArrayList<AirplaneAction> actions) {
		DepartureMediator departure = departures.get(pilot);
		if (departure == null) {
			//this pilot doesn't exist!
			//raise an error in the gui
			groundControlPanel.raiseErrorMessage("Not a valid pilot to give departure route instructions to");
			return;
		}

		//check the status first, that the pilot
		//was actually asking for directions

		if (departure.getStatus() != DepartureMediator.DepartureStatus.RequestingDirections) {
			//raise an error in the gui
			groundControlPanel.raiseErrorMessage(
					"Pilot " + pilot.getName() + ": Sorry, but I am not requesting directions right now.");
			return;
		}
		//
		//departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);

		pilot.msgHereAreDepartureInstructions(this,commands,actions);

		setPilotVerified(pilot,false);

		//now fill out the commands issued
		issueCommand(pilot,new Command(pilot,
				"Instructions: " + StringUtil.separatedString(commands,", "),
				EchoType.HERE_ARE_INSTRUCTIONS));

		//update the UI accordingly
		groundControlPanel.update();

	}

	public void grantPushbackToPilot(Pilot pilot) {
		DepartureMediator departure = departures.get(pilot);
		if (departure == null) {
			//no such pilot
			//raise an error in the gui
			groundControlPanel.raiseErrorMessage("Not a valid pilot to grant pushabck to");
			return;
		}

		//check that the pilot is really requesting pushback
		if (departure.getStatus() !=
				DepartureMediator.DepartureStatus.RequestingPushback) {

			//raise an error in the gui
			groundControlPanel.raiseErrorMessage("Pilot " + pilot.getName() + ": I was not requesting pushback.");
			return;
		}

		Do("I'm handling pushback.");
		Flight flight = departure.getFlight();
		pilot.msgPushBackGranted(this,flight.getGate());

		issueCommand(pilot,new Command(pilot,
				"Push back granted: " + flight.getGate(),
				EchoType.PUSHBACK_GRANTED));

		stateChanged();

	}

	///ACTIONS/////////////////////////////////////////////////////////////////////////////
	public void removeArrival(ArrivalMediator arrival) {
		arrivals.remove(arrival);

		//let the gui know we have docked
		Pilot pilot = pilots.get(arrival.getFlight());
		groundControlPanel.removeFlight(pilot);
	}

	private void removeDeparture(DepartureMediator departure) {

		//let the gui know we have docked
		Pilot pilot = pilots.get(departure.getFlight());
		departures.remove(pilot);
		groundControlPanel.removeFlight(pilot);
	}

}
