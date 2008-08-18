package agents;

import gui.LocalControlPanel;
import guiIntegration.AgentPair;
import interfaces.LocalControl;
import interfaces.Pilot;

import java.util.ArrayList;

import agent.StringUtil;
import airport.objects.AirplaneAction;
import airport.objects.Airport;


/**
 *
 * @author Ashwin Appiah
 *
 */
public class UserLocalControlAgent extends ATCAgent implements LocalControl {

	LocalControlPanel localControlPanel;
	/*
	 * Frequency ground controller sits on. Local controller knows this.
	 */
	private static double GC_FREQUENCY  = 1.11;

    /*
     * Constructor for Local Control Agent
     */
    public UserLocalControlAgent(String name,Airport airport,LocalControlPanel panel) {
    	super(name,airport);
    	myPair = new AgentPair(name, AgentPair.AgentType.LOCAL_CONTROL, voiceSemaphore);
    	this.localControlPanel = panel;
    	localControlPanel.setLocalControlAgent(this);
    }

    //Messages

   public void  msgReadyToLand(Pilot pilot,String planeName,String approachType,String runway){
		String msg = "Ready to land.";
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		//create a new ArrivalMediator object
		Flight flight = new Flight(planeName,runway,null,Flight.FlightType.Arrival);
		ArrivalMediator arrival = new ArrivalMediator(flight);
		arrival.setStatus(ArrivalMediator.ArrivalStatus.ReadyToLand);

		//add this new arrival into the arrivals list
		arrivals.put(pilot,arrival);

		pilots.put(flight,pilot);

		localControlPanel.newPilotMessage(pilot,"Requesting clearance to land on runway " + runway,arrival);

		stateChanged();
    }

   public void msgIHaveLanded(Pilot pilot) {
	   //set the status to landing
	   ArrivalMediator arrival = arrivals.get(pilot);
	   if (arrival == null) {
		   //no such pilot
		   return;
	   }

	   arrival.setStatus(ArrivalMediator.ArrivalStatus.Landed);
	   localControlPanel.update();
	   stateChanged();
   }

    //----Departures

    public void msgIAmOnRunway(Pilot pilot,String runway) {
		String msg = "Ready to take off.";
		Do(msg,false);
		traceDB.updateMessageTrace(myPair, pilot.getPair(), msg);

		//create a new DepartureMediator object
		Flight flight = new Flight(null,runway,null,Flight.FlightType.Departure);
		DepartureMediator departure = new DepartureMediator(flight);
		departure.setStatus(DepartureMediator.DepartureStatus.RequestingTakeOff);

		//add this new arrival into the departures list
		departures.put(pilot,departure);

		pilots.put(flight,pilot);

		localControlPanel.newPilotMessage(pilot,"I am on the runway, waiting for departure.",departure);

		stateChanged();
    }

    public void msgIHaveTakenOff(Pilot pilot) {

    	DepartureMediator departure = departures.get(pilot);
    	if (departure != null) {
    		departure.setStatus(DepartureMediator.DepartureStatus.TakenOff);
    		localControlPanel.update();
    		stateChanged();
    	}

    }
    //Scheduler

    protected boolean pickAndExecuteAnAction() {

    	synchronized (departures) {
    		for (DepartureMediator departure : departures.values()) {
    			if (departure.getStatus() ==
    					DepartureMediator.DepartureStatus.TakenOff) {
    				sayGoodBye(departure);
    			}
    		}
    	}

		//now check if there are any commands to verify
		if (commandsToVerify.size() > 0) {
			Command command = commandsToVerify.poll();
			boolean r = verifyCommand(command);

			if (r) {
				//it was successful
				setPilotVerified(command.getPilot(),true);
				//	check what kind of command it was
				EchoType type = command.getEchoType();
				if (type == EchoType.GOOD_BYE) {
					departures.remove(command.getPilot());
				} else if (type == EchoType.CONTACT_GROUND_CONTROL) {
					localControlPanel.removeFlight(command.getPilot());
					arrivals.remove(command.getPilot());
				}

				return true;

			}
		}

    	return false;

    }



    //Actions
    private void sayGoodBye(DepartureMediator departure) {
    	Pilot pilot = pilots.get(departure.getFlight());
    	pilot.msgGoodBye();
    	setPilotVerified(pilot,false);
		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);
    	localControlPanel.removeFlight(pilot);
    	stateChanged();
    }

    public void clearPilotForLanding(Pilot pilot){

    	ArrivalMediator arrival = arrivals.get(pilot);
    	if (arrival == null) {
    		//error message
    		localControlPanel.raiseErrorMessage("This pilot did not request clearance to land.");
    		return;
    	}
    	Flight flight = arrival.getFlight();

    	arrival.setStatus(ArrivalMediator.ArrivalStatus.Landing);

    	String windCondition = "WINDY";

    	pilot.msgClearedToLand(this,flight.getWay(), windCondition);
    	setPilotVerified(pilot,false);

    	localControlPanel.update();

    }

    public void giveRouteToTaxiway(Pilot pilot,ArrayList<String> commands,ArrayList<AirplaneAction> actions) {
    	ArrivalMediator arrival = arrivals.get(pilot);
    	if (arrival == null) {
    		//error message
    		localControlPanel.raiseErrorMessage("This pilot is not in the landing phase.");
    		return;
    	}

    	if (arrival.getStatus() != ArrivalMediator.ArrivalStatus.Taxiing) {
    		if (arrival.getStatus() != ArrivalMediator.ArrivalStatus.Landed) {
    			localControlPanel.raiseErrorMessage("This pilot has not landed yet!");
    			return;
    		}
    	}

    	arrival.setStatus(ArrivalMediator.ArrivalStatus.Taxiing);
    	//tell the pilot to contact the ground
    	Do("Received word that pilot has landed. Redirecting him to Ground Control");

    	pilot.msgPleaseContactGround(this, GC_FREQUENCY,commands, actions);
    	setPilotVerified(pilot,false);

    	issueCommand(pilot,new Command(pilot,
    			"Must Contact Ground, Frequency: " + GC_FREQUENCY + " and also do all this: "
    				+ StringUtil.separatedString(commands,", "),
    			EchoType.CONTACT_GROUND_CONTROL));

    	localControlPanel.update();
    }

    public void positionAndHold(Pilot pilot){

    	DepartureMediator departure = departures.get(pilot);
    	if (departure == null) {
    		localControlPanel.raiseErrorMessage("This pilot is not in the take-off phase");
    		return;
    	}

    	if (departure.getStatus() != DepartureMediator.DepartureStatus.RequestingTakeOff) {
    		localControlPanel.raiseErrorMessage("This pilot did not request take-off.");
    		return;
    	}

		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingTakeOff);

		Do("Received word from pilot " + pilot + " that he is requesting take-off");

		pilot.msgPositionAndHold(this);
		setPilotVerified(pilot,false);

		localControlPanel.update();
    }

    public void clearPilotForTakeoff(Pilot pilot) {
    	DepartureMediator departure = departures.get(pilot);
    	if (departure == null) {
    		localControlPanel.raiseErrorMessage("This pilot is not in the take-off phase");
    		return;
    	}

    	if (departure.getStatus() != DepartureMediator.DepartureStatus.AwaitingTakeOff) {
    		localControlPanel.raiseErrorMessage("You did not tell the pilot to position and hold.");
    		return;
    	}

    	Do("Clearing pilot " + pilot + " for take-off");
    	pilot.msgClearedForTakeOff();
    	setPilotVerified(pilot,false);

		departure.setStatus(DepartureMediator.DepartureStatus.TakingOff);

    	issueCommand(pilot,new Command(pilot,"Cleared for Takeoff",EchoType.CLEARED_FOR_TAKEOFF));

    	localControlPanel.update();
    }


}
