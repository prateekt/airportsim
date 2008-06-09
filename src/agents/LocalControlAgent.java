package agents;

import java.util.*;

import agent.StringUtil;
import interfaces.*;
import guiIntegration.*;
import airport.objects.*;



/**
 * @author Ashwin Appiah
 *
 */

public class LocalControlAgent extends ATCAgent implements LocalControl {

	/*
	 * Frequency ground controller sits on. Local controller knows this.
	 */
	private static double GC_FREQUENCY  = 1.11;

    /*
     * Constructor for Local Control Agent
     */
    public LocalControlAgent(String name,Airport airport) {
    	super(name,airport);
    	myPair = new AgentPair(name, AgentPair.AgentType.LOCAL_CONTROL, voiceSemaphore);
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
		stateChanged();
    }

    public void msgIHaveTakenOff(Pilot pilot) {

    	DepartureMediator departure = departures.get(pilot);
    	if (departure != null) {
    		departure.setStatus(DepartureMediator.DepartureStatus.TakenOff);
    		stateChanged();
    	}
    }
    //Scheduler

    protected boolean pickAndExecuteAnAction() {

		//ARRIVAL SCHEDULING CODE////////////////////////////////

		//check if there are arrivals
    	synchronized (arrivals) {
			if (arrivals.size() > 0) {
				//let's service those that are landing first
				for (ArrivalMediator arrival : arrivals.values()) {
					if (arrival.getStatus() ==
						ArrivalMediator.ArrivalStatus.ReadyToLand) {
						handleLandingRequest(arrival);
						return true;
					}
				}

				//now those that have just landed, let's take care of them
				for (ArrivalMediator arrival : arrivals.values()) {
					if (arrival.getStatus() ==
						ArrivalMediator.ArrivalStatus.Landed) {
						Flight flight = arrival.getFlight();
						Pilot pilot = pilots.get(flight);

						//now, let's check that the airplane is actually in its correct position
						//get the latest command issued for this dude

						//extract info from way
						String tokens[] = flight.getWay().split(" ");
						//join
						String wayname = "";
						for (int t=0;t<tokens.length-1;t++) {
							wayname += tokens[t] + " ";
						}
						wayname = wayname.trim();

						//check to see if the plane has reached a stop
						Airplane airplane = pilot.getAirplane();
						Way way = airplane.getWay();
						boolean arrived = airplane.getActive() &&
											airplane.getVelocity() == 0.0 &&
											(way != null && way.getName().equals(wayname));

						if (getPilotVerified(pilot) && arrived) {

							if (arrived) {
								handleLandedPlane(flight,pilot,arrival);
								return true;
							}
						}
					}

				}
			}
    	}

		//check if pushback is requested
    	synchronized (departures) {
			if (departures.size() > 0) {

				//the first priority is to say goodbye to departing planes
				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() ==
						DepartureMediator.DepartureStatus.TakenOff) {
						Flight flight = departure.getFlight();
						Pilot pilot = pilots.get(flight);

						//has the pilot taken off yet?

						if (getPilotVerified(pilot)) {
							//we've got a good candidate
							handleTakenOffPlane(flight,pilot,departure);
							return true;
						}
					}
				}

				//send any awaiting planes on their merry way
				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() ==
						DepartureMediator.DepartureStatus.AwaitingTakeOff) {
						Flight flight = departure.getFlight();
						Pilot pilot = pilots.get(flight);
						if (getPilotVerified(pilot)) {
							//we've got a good candidate
							handleAwaitingPlane(flight,pilot,departure);
							return true;
						}
					}
				}

				//see if there exists one such that they're requesting  takeoff
				for (DepartureMediator departure : departures.values()) {
					if (departure.getStatus() ==
						DepartureMediator.DepartureStatus.RequestingTakeOff) {
						//we've got a good candidate
						handleTakeOffRequest(departure);
						return true;
					}
				}


			}
    	}

		//now check if there are any commands to verify
		if (commandsToVerify.size() > 0) {
			if (doVerification()) return true;
		}

    	return false;

    }


    private boolean doVerification() {
		Command command = commandsToVerify.poll();
		boolean r = verifyCommand(command);

		if (r) {
			//it was successful
			setPilotVerified(command.getPilot(),true);
			//	check what kind of command it was
			EchoType type = command.getEchoType();
			if (type == EchoType.CLEARED_TO_LAND) {	//the pilot echo'd his confirmation that as
													//cleared to land.
		    	ArrivalMediator arrival = arrivals.get(command.getPilot());
		    	arrival.setStatus(ArrivalMediator.ArrivalStatus.Landing);	//the pilot is landing now
			} else if (type == EchoType.CONTACT_GROUND_CONTROL) {	//the pilot is going to contact
																	//ground control

				//let's remove them from the arrivals list
				//we no longer have to take care of them
				arrivals.remove(command.getPilot());
			} else if (type == EchoType.POSITION_AND_HOLD) {
				DepartureMediator departure = departures.get(command.getPilot());
				departure.setStatus(DepartureMediator.DepartureStatus.AwaitingTakeOff);
			} else if (type == EchoType.CLEARED_FOR_TAKEOFF) {
				//DepartureMediator departure = departures.get(command.getPilot());
				//departure.setStatus(DepartureMediator.DepartureStatus.TakenOff);
			} else if (type == EchoType.GOOD_BYE) {
				departures.remove(command.getPilot());
			}

			return true;

		}

		return false;
    }


    //Actions

    private void handleLandingRequest(ArrivalMediator arrival){

    	Flight flight = arrival.getFlight();
    	Pilot pilot = pilots.get(flight);

    	arrival.setStatus(ArrivalMediator.ArrivalStatus.Landing);

    	Do("Received landing request from pilot " + pilot);

    	String windCondition = "WINDY";

    	pilot.msgClearedToLand(this,flight.getWay(), windCondition);
    	setPilotVerified(pilot,false);

    	issueCommand(pilot,new Command(pilot,
    			"Cleared to land on runway " + flight.getWay() + ". Wind condition:" + windCondition,EchoType.CLEARED_TO_LAND));

    	arrival.setStatus(ArrivalMediator.ArrivalStatus.ClearedToLand);

    }

    private void handleLandedPlane(Flight flight,Pilot pilot,ArrivalMediator arrival) {
    	//tell the pilot to contact the ground
    	Do("Received word that pilot has landed. Redirecting him to Ground Control");

    	arrival.setStatus(ArrivalMediator.ArrivalStatus.AwaitingResponse);

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
    		System.out.println("handleLandedPlane(): error, there does not exist a path to the gate specified!");
    		return;
    	}

    	//try to loop through the path till we hit a taxiway
    	Object[] anodes = path.toArray();

    	boolean foundTaxiway = false;
    	int a = 0;
    	for (a=0;a<anodes.length-1;a++) {
    		AirportNode anode1 = (AirportNode)anodes[a];
    		AirportNode anode2 = (AirportNode)anodes[a+1];
/*
    		HashMap<AirportNode,Way> edges2 = anode1.getEdges();
    		for(AirportNode t : edges2.keySet()) {
    			System.out.println(t.getName() + "/" + edges2.get(t).getName());
    		}*/
    		Way way = anode1.getWayTo(anode2);
    		if (way != null) {
    			//check if this way is a taxiway
    			if ("taxiway".equals(way.getType().toLowerCase())) {
    				//yes!
    				foundTaxiway = true;
    				break;
    			}
    		}
    	}

    	if (!foundTaxiway) {
    		//another egregious error
    		System.out.println("handleLandedPlane(): error, there does not exist a path that includes a taxiway to the gate specified!");
    		return;
    	}

    	//get a small subset of the anodes
    	ArrayList<AirportNode> subset = new ArrayList<AirportNode>();

    	for (int b=0;b<a+2;b++) {
    		AirportNode anode = (AirportNode)anodes[b];
    		subset.add(anode);
    	}

    	ArrayList<String> commands = new ArrayList<String>();

    	//generate commands
    	ArrayList<AirplaneAction> actions =
    		convertPathToAirplaneActions(airport,airplane.getWay().getName(),subset,commands);

    	pilot.msgPleaseContactGround(this, GC_FREQUENCY,commands, actions);
    	setPilotVerified(pilot,false);

    	issueCommand(pilot,new Command(pilot,
    			"Must Contact Ground, Frequency: " + GC_FREQUENCY + " and also do all this: "
    				+ StringUtil.separatedString(commands,", "),
    			EchoType.CONTACT_GROUND_CONTROL));
    }

    private void handleTakeOffRequest(DepartureMediator departure){
		Flight flight = departure.getFlight();

		//compute additional instructions for the pilot
		Pilot pilot = pilots.get(flight);
		if (pilot == null) {
			//oh no! egregious error!
			Do("No pilot associated for flight " + flight);
			return;
		}

		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);

		Do("Received word from pilot " + pilot + " that he is requesting take-off");

		pilot.msgPositionAndHold(this);
		setPilotVerified(pilot,false);

		issueCommand(pilot,new Command(pilot,"Position and Hold",EchoType.POSITION_AND_HOLD));
    }

    private void handleAwaitingPlane(Flight flight,Pilot pilot,DepartureMediator departure) {

    	Do("Clearing pilot " + pilot + " for take-off");
    	pilot.msgClearedForTakeOff();
    	setPilotVerified(pilot,false);

		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);

    	issueCommand(pilot,new Command(pilot,"Cleared for Takeoff",EchoType.CLEARED_FOR_TAKEOFF));
    }

    private void handleTakenOffPlane(Flight flight,Pilot pilot,DepartureMediator departure) {

    	pilot.msgGoodBye();
    	setPilotVerified(pilot,false);
		departure.setStatus(DepartureMediator.DepartureStatus.AwaitingResponse);
    	issueCommand(pilot,new Command(pilot,"Good Bye",EchoType.GOOD_BYE));
    }

}
