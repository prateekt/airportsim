package agents;

import java.util.*;
import java.util.concurrent.*;
import agent.Agent;
import interfaces.*;
import guiIntegration.*;

import airport.*;
import airport.objects.*;

import net.sourceforge.jsl.*;

/**
 * This is the generic ATC base-class.
 *
 * V1.1 (HY) - now implements visual cognition, and A*.
 *
 * @author Henry Yuen, Prateek Tandon
 *
 */
public abstract class ATCAgent extends Agent implements ATC {

	protected static double THRESHOLD_RADIUS = 25;	//in pixels
	/**
	 * Name of the ATC agent
	 */
	String name;

	//Command Verification///////////////////////////////////
	/**
	 * This is the queue that stores all the echo'd commands
	 * from the pilot to verify
	 */
	Queue<Command> commandsToVerify;

	/**
	 * Each pilot is associated with a queue of commands that were
	 * issued to the pilot from this ATC. These are compared with the
	 * commandsToVerify
	 */
	Map<Pilot, Queue<Command>> commandsIssued;

	/**
	 * Each pilot associated a boolean that indicates
	 * whether his last echo'd command was verified or not
	 */
	Map<Pilot,Boolean> verifiedPilots;

	AgentPair myPair;

	/**
	 * All ATC's have a model of the airport
	 */
	Airport airport;

	protected Map<Pilot,ArrivalMediator> arrivals;
	protected Map<Pilot,DepartureMediator> departures;
	//associate flights with pilots
	protected Map<Flight,Pilot> pilots;
	protected TraceDB traceDB;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * Constructor for ATC Agent
	 */
	public ATCAgent(String name,Airport airport) {
		this.airport = airport;
		this.name = name;
		commandsToVerify = new ConcurrentLinkedQueue<Command>();
		commandsIssued = Collections.synchronizedMap(new HashMap<Pilot,Queue<Command>>());
		verifiedPilots = Collections.synchronizedMap(new HashMap<Pilot,Boolean>());
    	arrivals = Collections.synchronizedMap(new HashMap<Pilot,ArrivalMediator>());
		departures = Collections.synchronizedMap(new HashMap<Pilot,DepartureMediator>());
		pilots = Collections.synchronizedMap(new HashMap<Flight,Pilot>());
		traceDB = TraceDB.getInstance();
	}

	/**
	 * Called by the Pilot to echo the command that was just given.
	 * @param pilot
	 * @param echoedCommand
	 */
	public void msgEchoCommand(Pilot pilot,String echoedCommand, EchoType echoType) {

		String msg = "Echo from " + pilot + ": " + echoedCommand;
		Do(msg);
		TraceDB.getInstance().updateMessageTrace_ECHO(myPair, pilot.getPair(), msg);

		if (pilot == null) {
			return;
		}

		Command command = new Command(pilot, echoedCommand, echoType);
		commandsToVerify.offer(command);
		stateChanged();
	}


	/**
	 * Verifies that echo'd command from the pilots match the issued
	 * command from the ATC.
	 * @return boolean Returns whether the verification was successful or not
	 */
	protected boolean verifyCommand(Command  echo) {
		Pilot pilot = echo.getPilot();
		if (commandsIssued.containsKey(pilot) == false) {
			//severe error
			print("ATC.verifyCommand(): Severe error; pilot echoed a command when there were none issued to him");
			//raise a risk
			return false;
		}
		Queue<Command> issued = commandsIssued.get(pilot);
		//get the original command issued
		Command original = issued.poll();
		if (echo.equals(original)) {
			//all is good
			return true;
		} else {
			//Severe error! Raise a risk
			print("ATC.verifyCommand(): Pilot echo\'d command does not match command issued!");

			//resend the command
		}

		return false;
	}

	public AgentPair getPair() {
		return myPair;
	}

	protected void setPilotVerified(Pilot pilot,boolean verified) {
		verifiedPilots.put(pilot,verified);
	}

	protected boolean getPilotVerified(Pilot pilot) {
		if (verifiedPilots.containsKey(pilot) == false) return false;
		return verifiedPilots.get(pilot);
	}

	protected void issueCommand(Pilot pilot,Command command) {
		Queue<Command> issued;
		if (commandsIssued.containsKey(pilot) == false) {
			issued = new ConcurrentLinkedQueue<Command>();
			commandsIssued.put(pilot,issued);
		} else {
			issued = commandsIssued.get(pilot);
		}

		issued.offer(command);
	}

	//OCCULUS OPERATUM!
	protected boolean isPlaneAtWayPoint(String waystr,Airplane airplane) {
		String temp[] = waystr.split(" ");
		String wayname = temp[0];
		String waypoint = temp[1];

		//get the runway
		Way way = airport.getWay(wayname);

		Compass endpoint = way.getEndpointCompass(waypoint);

		if (endpoint == null) {
			//this is so egregious I don't even know what to do

		}

		//let's see that the pilot is essentially on top of this endpoint
		double distance = endpoint.getDistanceTo(airplane.getCompass());
		if (distance < THRESHOLD_RADIUS) {
			return true;
		}
		return false;

	}

	public static String getIntersectingWay(String intersection,String currentWay) {
		String temp = intersection.replace(currentWay,"");
		temp = temp.replace("-","");
		temp = temp.trim();
		return temp;
	}

	public static AirportNode getClosestIntersection(Airport airport,Compass plane) {

		double distance = Double.POSITIVE_INFINITY;
		double distance2 = Double.POSITIVE_INFINITY;
		AirportNode closest = null;
		AirportNode closest2 = null;

		Compass airplaneDir = new Compass();
		airplaneDir.setX(Math.cos(plane.getAngle()));
		airplaneDir.setY(Math.sin(plane.getAngle()));

		for (AirportNode anode : airport.getConnectivityGraph().values()) {
			Compass nodeCompass = anode.getCompass();
			double d = nodeCompass.getDistanceTo(plane);
			if (d < distance) {
				distance = d;
				closest = anode;
			}

			//next, compute the displacement vector
			Compass displacement = nodeCompass.subtract(plane);
			//next, compute the airplane dir vector
			double dot = displacement.dotProduct(airplaneDir);

			if (dot > 0) {
				if (d < distance2) {
					distance2 = d;
					closest2 = anode;
				}
			}
		}

		if (closest2 != null) return closest2;

		return closest;
	}


	public static ArrayList<AirportNode> findPath(AirportNode start,AirportNode end) {

		//uses A* to find the optimal path to the gate
		AirportSearchNode asnode = new AirportSearchNode(start,end,0,0);

		AbstractOpenClosedListSearch search = new AStarSearch();	//A* search wrapped in a box
		AirportSearchNode result = null;

		//set the seed
		search.setSeed(asnode);
		//search.setExpandGoal(true);

		try {
			result = (AirportSearchNode) search.search();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (result == null) {
			//couldn't find a solution
			return null;
		}

		List path = result.getPath();

		//redo the path
		ArrayList<AirportNode> path2 = new ArrayList<AirportNode>();

		//the path is a bunch of edges from which
		//we have to extract the airportNodes from :)
		for (Object o : path) {
			//SearchEdge edge = (SearchEdge)o[e];
			//AirportNode anode = (AirportNode)edge.getDomainObject();
			AirportNode anode = (AirportNode)o;
			path2.add(anode);
		}


		return path2;

	}

	public static ArrayList<AirplaneAction> convertPathToAirplaneActions(Airport airport,String initialWay,ArrayList<AirportNode> path,ArrayList<String> commands) {
		String currentWay = initialWay;	//start it off

		ArrayList<AirplaneAction> actions = new ArrayList<AirplaneAction>();

		for (int n=0;n<path.size();n++) {
			AirportNode anode = path.get(n);
			AirportNode.NodeType type = anode.getType();

			String name = anode.getName();

			AirplaneAction action = null;
			String command = "";

			//now figure out what the name really means
			//it's either a gate, or it's an intersection
			if (type == AirportNode.NodeType.INTERSECTION) {

				//extract the two runway names
				String[] waynames = name.split("-");

				AirportNode anode2 = null;
				if (n < path.size()-1) anode2 = path.get(n+1);

				if (anode2 == null) {
					//that means we'll just taxi to this place
					String newwayName = getIntersectingWay(name,currentWay);

					command = "Taxi to " + newwayName;
					commands.add(command);
				} else {
					//this is an intersection
					//ok, look ahead
					String name2 = anode2.getName();
					name2 = name2.toLowerCase();
					//System.out.println("***DEBUG** Current Way: " + currentWay);

					if (name2.indexOf(currentWay.toLowerCase()) > -1){
						//it's going to stay on the same runway
						//it's merely a "cross" command
						//maintain the semantics of the command by
						//splitting up the contents of the String
						String newwayName = getIntersectingWay(name,currentWay);
						command = "Cross " + newwayName;
						commands.add(command);
					} else {
						//we're turning at this intersection
						//we have to find out whether we're turning right or left
						//well, we compute whether the next node is closer to
						//runway alpha or beta

						//find out what's the new runway
						String newwayName = waynames[0].equals(currentWay) ?
											waynames[1] : waynames[0];


						//get the newway from the airport
						Way newway = airport.getWay(newwayName);
						if (newway == null) {
							System.out.println("ATCAgent findpath(): newway not found");
						}

						Compass alpha = newway.getEndpointCompass("alpha");

						double d1 = alpha.getDistanceTo(anode2.getCompass());
						double d2 = alpha.getDistanceTo(anode.getCompass());

						String point = "";
/*
						System.out.println("Ding:"+newwayName);
						System.out.println("Chipotle:"+anode2.getName());
						System.out.println("Chipotle:"+anode.getName());
*/
						if (d1 > d2) {
							point = "beta";
						}
						else {
							point = "alpha";
						}

						command = "Turn into " + newwayName + " " + point;
						commands.add(command);
						currentWay = newwayName;

					} //end else

				} //end if anode2 != null

				//explicitly make two actions
				action = new AirplaneAction();
				action.setName("turntowards");
				action.addProperty("endpoint",anode.getCompass());
				actions.add(action);

				action = new AirplaneAction();
				action.setName("taxi");
				action.addProperty("endpoint",anode.getCompass());
				actions.add(action);

			} //end if this is an intersection
			else if (type == AirportNode.NodeType.GATE) {
				String gatename = "";

				String[] tokens = name.split(" ");

				for (int i=1;i<tokens.length;i++)
					gatename += tokens[i] + " ";

				gatename = gatename.trim();

				//this is a dock command, if it is the last one in the sequence
				if (n == path.size()-1) {
					command = "Dock at gate " + gatename;
					commands.add(command);

					//explicitly make two actions
					action = new AirplaneAction();
					action.setName("turntowards");
					action.addProperty("endpoint",anode.getCompass());
					actions.add(action);

					action = new AirplaneAction();
					action.setName("taxi");
					action.addProperty("endpoint",anode.getCompass());
					actions.add(action);
				} else {
					//look one ahead -
					AirportNode anode2 = path.get(n+1);

					//taxi to the next endpoint

					command = "Taxi from gate " + gatename;
					commands.add(command);

					//explicitly make two actions
					action = new AirplaneAction();
					action.setName("turntowards");
					action.addProperty("endpoint",anode2.getCompass());
					actions.add(action);

					action = new AirplaneAction();
					action.setName("taxi");
					action.addProperty("endpoint",anode2.getCompass());
					actions.add(action);
				}

			}

		} //end for all SearchEdges

		return actions;
	}

	public Map<Pilot, ArrivalMediator> getArrivals() {
		return arrivals;
	}

	public void setArrivals(Map<Pilot, ArrivalMediator> arrivals) {
		this.arrivals = arrivals;
	}

	public Map<Pilot, DepartureMediator> getDepartures() {
		return departures;
	}

	public void setDepartures(Map<Pilot, DepartureMediator> departures) {
		this.departures = departures;
	}

	public AgentPair getMyPair() {
		return myPair;
	}

	public void setMyPair(AgentPair myPair) {
		this.myPair = myPair;
	}

	public Map<Flight, Pilot> getPilots() {
		return pilots;
	}

	public void setPilots(Map<Flight, Pilot> pilots) {
		this.pilots = pilots;
	}

	public Map<Pilot, Queue<Command>> getCommandsIssued() {
		return commandsIssued;
	}

	public void setCommandsIssued(Map<Pilot, Queue<Command>> commandsIssued) {
		this.commandsIssued = commandsIssued;
	}

}

/**
 *	this is the node type for the ATC doing the mental searching of
 *  an optimal path to the gate
 */

class AirportSearchNode extends AbstractSearchNode {
	enum NodeType { WAY, GATE }

	private int depth = 0;
	private double cost = 0;

	AirportNode node;
	AirportNode goal;

	NodeType type;

	public AirportSearchNode(AirportNode node,AirportNode goal,int depth,double cost) {
		this.node = node;
		this.goal = goal;
		this.depth = depth;
		this.cost = cost;
		setDomainObject(node);
	}

	public List getPath() {
		if (isGoal()) {
			List path = new ArrayList();
			getFirstPathIn(path,this);
			Collections.reverse(path);
			return path;
		} else {
			return null;
		}
	}

	public Collection expand() {

		//find all the things it is connected to an expand
		Collection followers = new ArrayList<AirportSearchNode>();

		HashMap<String,AirportNode> neighbors = node.getNeighbors();

		//System.out.println("===");
		for (AirportNode anode : neighbors.values()) {
			double distance = anode.getCompass().getDistanceTo(node.getCompass()) + 1;	//ensures that
			//there are no zero distances
			AirportSearchNode asnode = new AirportSearchNode(anode,goal,depth+1,cost + distance);

			followers.add(asnode);
			//System.out.println("Search : " + node.getName() + " " + anode.getName());
			linkForward(anode,asnode);
		}

		return followers;
	}

	public int getBreadth() {
		return 0;	//not needed
	}

	public int getDepth() {
		return depth;
	}

	public double getCost() {
		return cost;
	}

	public double getEstimatedRestCost() {
		//calculates the euclidean distance between this and the goal
		Compass c1 = node.getCompass();
		Compass c2 = goal.getCompass();

		return c1.getDistanceTo(c2);
	}

	public boolean isGoal() {
		return node == goal;
	}

	//overload the path traversal method
	private void getFirstPathIn(List path,SearchNode searchNode) {
		Iterator it = searchNode.getIncomingEdges().iterator();
		AirportNode node = (AirportNode)searchNode.getDomainObject();
		path.add(node);
		if (it.hasNext()) {
			SearchEdge edge = (SearchEdge) it.next();
			//path.add(edge);
			getFirstPathIn(path, edge.getFromNode());
		}
	}
}