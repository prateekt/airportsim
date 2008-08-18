package tests;

import interfaces.GroundControl;
import interfaces.Pilot;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import junit.framework.TestCase;
import agents.ArrivalMediator;
import agents.Command;
import agents.DepartureMediator;
import agents.EchoType;
import agents.Flight;
import agents.GroundControlAgent;
import agents.ScenarioParser;
import airport.TheSimulator;
import airport.mock.MockClearanceDelivery;
import airport.mock.MockLocalControl;
import airport.mock.MockPilot;
import airport.objects.Airplane;
import airport.objects.AirplaneAction;
import airport.objects.AirplaneActionCallback;
import airport.objects.Airport;
import airport.objects.AirportNode;
import airport.objects.Compass;
import airport.objects.Way;

/**
 * Suite of tests testing the GroundControlAgent.
 * @author Henry Yuen
 *
 */
public class GroundControlAgentTest extends TestCase {

	private MockClearanceDelivery clearanceDelivery;
	private MockLocalControl localControl;
	private Airport airport;
	private ScenarioParser scenarioParser;
	private Semaphore testSemaphore;

	public GroundControlAgentTest(String s) {

		super(s);
		System.out.println("Hello, this test takes a while (especially on aludra). Please be patient.");
		if (!TheSimulator.getInstance().isAlive())
			TheSimulator.getInstance().start();

		testSemaphore = new Semaphore(1);	//used to control one test at a time
	}

	private void initializeFixtures() throws Exception {
		testSemaphore.acquire();

		clearanceDelivery = new MockClearanceDelivery();
		localControl = new MockLocalControl();

		//first, load the scenario
		try {
			ScenarioParser p = new ScenarioParser();
			p.load("resource/scenarios/lax.xml");

			airport = p.getAirport();

		} catch (Exception ex) {
			ex.printStackTrace();
		}


		TheSimulator.getInstance().reset();
		TheSimulator.getInstance().setRunning(true,false);

	}

	private Flight createSimpleFlight() {
		Flight flight = new Flight();
		flight.setDestination("LAX");
		flight.setGate("G3");
		flight.setGuidanceMode("Visual");
		flight.setLocation("Nowhere");
		flight.setOrigin("SFX");
		flight.setPlaneName("Test Plane");
		flight.setWay("24R");
		return flight;
	}

	private MockPilot createMockPilot(String name,Flight flight,GroundControl gc) {
		MockPilot pilot = new MockPilot(name);
		pilot.setFlight(flight);
		Airplane airplane = new Airplane();
		airplane.setName(flight.getPlaneName());
		TheSimulator.getInstance().addAirplane(airplane);

		pilot.setAirplane(airplane);
		pilot.setMyCD(clearanceDelivery);
		pilot.setMyGC(gc);
		pilot.setMyLC(localControl);
		return pilot;
	}
	/**
	 * Scenario 1: 1st Normative arrival scenario;
	 * the Ground Control agent receives an arriving plane
	 */
	public void testNormativeArrivalScenario() throws Exception {
		//clearancedelivery and local control are already set up
		initializeFixtures();
		//create the GroundControlAgent which we want to test
		GroundControlAgent groundControl = new GroundControlAgent("Ground Control",airport);
		//create a pseudo-flight object
		Flight flight = createSimpleFlight();
		flight.setType(Flight.FlightType.Arrival);

		MockPilot pilot = createMockPilot("Test Pilot",flight,groundControl);

		//let's land this pilot first
		AirplaneAction action = new AirplaneAction();
		action.setName("land");
		action.setTime(1000);
		action.addProperty("wayname","24R");
		action.addProperty("waypoint","beta");

		Airplane airplane = pilot.getAirplane();

		final Semaphore sem = new Semaphore(1);

		sem.acquire();

		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				//once it has landed, release the semaphore
				sem.release();
			}
		});

		airplane.setAlive(true);
		airplane.setActive(true);

		//velocity and acceleration are taken care of by TheSimulator
		airplane.addAction(action);

		assertNotNull(airplane.getFrontAction());

		sem.acquire();

		groundControl.msgHelloGround(pilot,flight.getPlaneName(),flight.getGate());

		//do our tests
		Map<Pilot,ArrivalMediator> arrivals = groundControl.getArrivals();
		Map<Pilot,DepartureMediator> departures = groundControl.getDepartures();

		//make sure this is valid
		assertNotNull(arrivals);
		assertNotNull(departures);
		assertEquals(arrivals.size(),1);	//there should only be one arrival
		assertEquals(departures.size(),0);	//there should be no departures

		for (Pilot p : arrivals.keySet()) {
			assertEquals(p,pilot);
		}

		//grab the mediator from the
		ArrivalMediator arrival = arrivals.get(pilot);

		assertNotNull(arrival);

		//check the attributes of the arrival flight
		Flight arrivalFlight = arrival.getFlight();

		assertEquals(arrivalFlight.getGate(),flight.getGate());
		//that's all we can really assert thus far, because the GroundControlAgent
		//only uses the nugget of information that is the gate

		Map<Flight,Pilot> pilots = groundControl.getPilots();

		assertNotNull(pilots);
		assertEquals(pilots.size(),1);	//there should be only one pilot doing something

		assertTrue(pilots.containsKey(arrivalFlight));

		Pilot arrivalPilot = pilots.get(arrivalFlight);

		assertEquals(arrivalPilot,pilot);	//these should be one and the same

		//We now have tested all the data structure validity,
		//Now let's test that the agent logic is correct.

		//1. Test the arrival status
		assertEquals(arrival.getStatus(),ArrivalMediator.ArrivalStatus.Landed);

		//Next step - run the ground control for one time step
		boolean result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);	//the groundControl should have successfully picked out an action

		//the action should've been that the ground control
		//gave a route to the landed plane to his/her gate
		assertEquals(arrival.getStatus(),ArrivalMediator.ArrivalStatus.Taxiing);

		//check that the commands issued is what it is
		Map<Pilot,Queue<Command>> issued = groundControl.getCommandsIssued();

		assertEquals(issued.size(),1);	//there should be only one queue
		assertTrue(issued.containsKey(pilot));

		Queue<Command> queue = issued.get(pilot);
		assertNotNull(queue);

		assertEquals(queue.size(),1);	//we have only issued one command

		Command command = queue.peek();
		//let's ensure it's the correct path
		assertEquals(command.getCommand(),
				"Must use this route: Turn into Y beta, Turn into 6R beta, Turn into W beta, Turn into E beta, Dock at gate G3");

		assertEquals(command.getEchoType(),EchoType.USE_THIS_ROUTE);

		//echo this back to the ground control agent
		groundControl.msgEchoCommand(pilot,command.getCommand(),command.getEchoType());

		//make the ground control pick another action - which should be the verification
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		//hould still be taxiing (to the knowledge of the ground control)
		assertEquals(arrival.getStatus(),ArrivalMediator.ArrivalStatus.Taxiing);

		//finally, let's notify the ground control that the plane has docked
		groundControl.msgIHaveDocked(pilot);

		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		//the arrivals should be gone by now
		assertEquals(arrivals.size(),0);

		//the plane has successfully docked
		//normative test case concluded

		testSemaphore.release();
	}

	/**
	 * Scenario 1: 2nd Normative departure scenario;
	 * the Ground Control agent receives a departing plane
	 */
	public void testNormativeDepartureScenario() throws Exception {
		//clearancedelivery and local control are already set up
		initializeFixtures();
		//create the GroundControlAgent which we want to test
		GroundControlAgent groundControl = new GroundControlAgent("Ground Control",airport);
		//create a pseudo-flight object
		Flight flight = createSimpleFlight();
		flight.setType(Flight.FlightType.Departure);
		flight.setWay("E");

		MockPilot pilot = createMockPilot("Test Pilot",flight,groundControl);

		Airplane airplane = pilot.getAirplane();
		AirportNode gateNode = airport.getGateNode(flight.getGate());

		//place the airplane at the gatenode
		airplane.setCompass(gateNode.getCompass().clone());

		//get the way associated with runway
		Way way = airport.getWay(flight.getWay());
		Compass endpoint = way.getEndpointCompass("beta");
		airplane.getCompass().setAngle(endpoint.getAngle());

		//the airplane is also active
		airplane.setWay(way);
		airplane.setAlive(true);
		airplane.setActive(true);

		groundControl.msgRequestPushBack(pilot,flight.getPlaneName(),flight.getWay(),flight.getGate());

		//do our tests
		Map<Pilot,ArrivalMediator> arrivals = groundControl.getArrivals();
		Map<Pilot,DepartureMediator> departures = groundControl.getDepartures();

		//make sure this is valid
		assertNotNull(arrivals);
		assertNotNull(departures);
		assertEquals(arrivals.size(),0);
		assertEquals(departures.size(),1);

		for (Pilot p : departures.keySet()) {
			assertEquals(p,pilot);
		}

		//grab the mediator from the
		DepartureMediator departure = departures.get(pilot);

		assertNotNull(departure);

		//check the attributes of the arrival flight
		Flight departureFlight = departure.getFlight();

		assertEquals(departureFlight.getGate(),flight.getGate());
		assertEquals(departureFlight.getWay(),flight.getWay());
		//that's all we can really assert thus far, because the GroundControlAgent
		//only uses the nugget of information that is the gate

		Map<Flight,Pilot> pilots = groundControl.getPilots();

		assertNotNull(pilots);
		assertEquals(pilots.size(),1);	//there should be only one pilot doing something

		assertTrue(pilots.containsKey(departureFlight));

		Pilot departurePilot = pilots.get(departureFlight);

		assertEquals(departurePilot,pilot);	//these should be one and the same

		//We now have tested all the data structure validity,
		//Now let's test that the agent logic is correct.

		//1. Test the departure status
		assertEquals(departure.getStatus(),DepartureMediator.DepartureStatus.RequestingPushback);

		//Next step - run the ground control for one time step
		boolean result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);	//the groundControl should have successfully picked out an action

		//the action should've been that the ground control
		//has given the pilot pushback
		assertEquals(departure.getStatus(),DepartureMediator.DepartureStatus.AwaitingResponse);

		//check that the commands issued is what it is
		Map<Pilot,Queue<Command>> issued = groundControl.getCommandsIssued();

		assertEquals(issued.size(),1);	//there should be only one queue
		assertTrue(issued.containsKey(pilot));

		Queue<Command> queue = issued.get(pilot);
		assertNotNull(queue);

		assertEquals(queue.size(),1);	//we have only issued one command

		Command command = queue.peek();


		//let's ensure it's the correct path
		assertEquals(command.getCommand(),
				"Push back granted: G3");

		assertEquals(command.getEchoType(),EchoType.PUSHBACK_GRANTED);

		//echo this back to the ground control agent
		groundControl.msgEchoCommand(pilot,command.getCommand(),command.getEchoType());

		//make the ground control pick another action - which should be the verification
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		//hould still be taxiing (to the knowledge of the ground control)
		assertEquals(departure.getStatus(),DepartureMediator.DepartureStatus.RequestingDirections);

		//let the ground control give directions
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		assertEquals(issued.size(),1);	//there should be only one queue
		assertTrue(issued.containsKey(pilot));

		assertNotNull(queue);

		assertEquals(queue.size(),1);	//we have only issued one command

		command = queue.peek();

		//let's ensure it's the correct path
		assertEquals(command.getCommand(),
				"Instructions: Cross W, Turn into Y alpha, Turn into 6R alpha, Cross Z, Cross AA, Turn into BB alpha, Taxi to 24R");

		assertEquals(command.getEchoType(),EchoType.HERE_ARE_INSTRUCTIONS);

		//echo this back to the ground control agent
		groundControl.msgEchoCommand(pilot,command.getCommand(),command.getEchoType());

		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		assertEquals(departures.size(),0);

		/*

		//the arrivals should be gone by now
		assertEquals(arrivals.size(),1);
		//the plane has successfully docked
		//normative test case concluded
*/

		testSemaphore.release();
	}

	/**
	 * Scenario 3: 3rd Normative scenario
	 * the Ground Control agent receives a departing plane, AND an arriving plane at the same time
	 */
	public void testNormativeBothScenario() throws Exception {
		//clearancedelivery and local control are already set up
		initializeFixtures();

		//create the GroundControlAgent which we want to test
		GroundControlAgent groundControl = new GroundControlAgent("Ground Control",airport);

		//START UP ARRIVAL SCENARIO

		//create a pseudo-flight1 object
		Flight flight1 = createSimpleFlight();
		flight1.setType(Flight.FlightType.Arrival);

		MockPilot pilot1 = createMockPilot("Test Pilot Arriving",flight1,groundControl);

		//let's land this pilot1 first
		AirplaneAction action = new AirplaneAction();
		action.setName("land");
		action.setTime(1000);
		action.addProperty("wayname","24R");
		action.addProperty("waypoint","beta");

		Airplane airplane = pilot1.getAirplane();

		final Semaphore sem = new Semaphore(1);

		sem.acquire();

		action.setCallback(new AirplaneActionCallback() {
			public void run() {
				//once it has landed, release the semaphore
				sem.release();
			}
		});

		airplane.setAlive(true);
		airplane.setActive(true);

		//velocity and acceleration are taken care of by TheSimulator
		airplane.addAction(action);

		assertNotNull(airplane.getFrontAction());

		sem.acquire();

		groundControl.msgHelloGround(pilot1,flight1.getPlaneName(),flight1.getGate());

		//do our tests
		Map<Pilot,ArrivalMediator> arrivals = groundControl.getArrivals();
		Map<Pilot,DepartureMediator> departures = groundControl.getDepartures();

		//make sure this is valid
		assertNotNull(arrivals);
		assertNotNull(departures);
		assertEquals(arrivals.size(),1);	//there should only be one arrival
		assertEquals(departures.size(),0);	//there should be no departures

		for (Pilot p : arrivals.keySet()) {
			assertEquals(p,pilot1);
		}

		//grab the mediator from the
		ArrivalMediator arrival = arrivals.get(pilot1);

		assertNotNull(arrival);

		//check the attributes of the arrival flight1
		Flight arrivalFlight = arrival.getFlight();

		assertEquals(arrivalFlight.getGate(),flight1.getGate());
		//that's all we can really assert thus far, because the GroundControlAgent
		//only uses the nugget of information that is the gate



		/*
		 * 	START THE DEPARTURE SCENARIO
		 *
		 */
		//create a pseudo-flight object
		Flight flight2 = createSimpleFlight();
		flight2.setType(Flight.FlightType.Departure);
		flight2.setWay("E");

		MockPilot pilot2 = createMockPilot("Test Pilot",flight2,groundControl);

		Airplane airplane2 = pilot2.getAirplane();
		AirportNode gateNode = airport.getGateNode(flight2.getGate());

		//place the airplane2 at the gatenode
		airplane2.setCompass(gateNode.getCompass().clone());

		//get the way associated with runway
		Way way = airport.getWay(flight2.getWay());
		Compass endpoint = way.getEndpointCompass("beta");
		airplane2.getCompass().setAngle(endpoint.getAngle());

		//the airplane2 is also active
		airplane2.setWay(way);
		airplane2.setAlive(true);
		airplane2.setActive(true);

		groundControl.msgRequestPushBack(pilot2,flight2.getPlaneName(),flight2.getWay(),flight2.getGate());


		//END DEPARTURE START
		//BEGIN ARRIVAL

		Map<Flight,Pilot> pilot1s = groundControl.getPilots();

		assertNotNull(pilot1s);
		assertEquals(pilot1s.size(),2);

		assertTrue(pilot1s.containsKey(arrivalFlight));

		Pilot arrivalPilot = pilot1s.get(arrivalFlight);

		assertEquals(arrivalPilot,pilot1);	//these should be one and the same

		//We now have tested all the data structure validity,
		//Now let's test that the agent logic is correct.

		//1. Test the arrival status
		assertEquals(arrival.getStatus(),ArrivalMediator.ArrivalStatus.Landed);

		//Next step - run the ground control for one time step
		boolean result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);	//the groundControl should have successfully picked out an action

		//the action should've been that the ground control
		//gave a route to the landed plane to his/her gate
		assertEquals(arrival.getStatus(),ArrivalMediator.ArrivalStatus.Taxiing);

		//check that the commands issued is what it is
		Map<Pilot,Queue<Command>> issued = groundControl.getCommandsIssued();

		assertEquals(issued.size(),1);	//there should be only one queue
		assertTrue(issued.containsKey(pilot1));

		Queue<Command> queue = issued.get(pilot1);
		assertNotNull(queue);

		assertEquals(queue.size(),1);	//we have only issued one command

		Command command = queue.peek();
		//let's ensure it's the correct path
		assertEquals(command.getCommand(),
		"Must use this route: Turn into Y beta, Turn into 6R beta, Turn into W beta, Turn into E beta, Dock at gate G3");

		assertEquals(command.getEchoType(),EchoType.USE_THIS_ROUTE);

		//echo this back to the ground control agent
		groundControl.msgEchoCommand(pilot1,command.getCommand(),command.getEchoType());

		//END ARRIVAL CODE
		//BEGIN DEPARTURE CODE
		//do our tests

		//make sure this is valid
		assertNotNull(arrivals);
		assertNotNull(departures);
		assertEquals(arrivals.size(),1);
		assertEquals(departures.size(),1);

		for (Pilot p : departures.keySet()) {
			assertEquals(p,pilot2);
		}

		//grab the mediator from the
		DepartureMediator departure = departures.get(pilot2);

		assertNotNull(departure);

		//check the attributes of the arrival flight2
		Flight departureFlight = departure.getFlight();

		assertEquals(departureFlight.getGate(),flight2.getGate());
		assertEquals(departureFlight.getWay(),flight2.getWay());
		//that's all we can really assert thus far, because the GroundControlAgent
		//only uses the nugget of information that is the gate

		Map<Flight,Pilot> pilots = groundControl.getPilots();

		assertNotNull(pilots);
		assertEquals(pilots.size(),2);	//there should be only one pilot2 doing something

		assertTrue(pilots.containsKey(departureFlight));

		Pilot departurePilot = pilots.get(departureFlight);

		assertEquals(departurePilot,pilot2);	//these should be one and the same

		//We now have tested all the data structure validity,
		//Now let's test that the agent logic is correct.

		//1. Test the departure status
		assertEquals(departure.getStatus(),DepartureMediator.DepartureStatus.RequestingPushback);


		//BEGIN ARRIVAL CODE





		//make the ground control pick another action - which should be the verification
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		//hould still be taxiing (to the knowledge of the ground control)
		assertEquals(arrival.getStatus(),ArrivalMediator.ArrivalStatus.Taxiing);

		//finally, let's notify the ground control that the plane has docked
		groundControl.msgIHaveDocked(pilot1);

		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		//the arrivals should be gone by now
		assertEquals(arrivals.size(),0);

		//the plane has successfully docked

		//END ARRIVAL CODE
		//BEGIN DEPARTURE CODE
		//Next step - run the ground control for one time step
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);	//the groundControl should have successfully picked out an action

		//the action should've been that the ground control
		//has given the pilot2 pushback
		assertEquals(departure.getStatus(),DepartureMediator.DepartureStatus.AwaitingResponse);

		assertEquals(issued.size(),2);	//there should be only one queue
		assertTrue(issued.containsKey(pilot2));

		queue = issued.get(pilot2);
		assertNotNull(queue);

		assertEquals(queue.size(),1);	//we have only issued one command

		command = queue.peek();


		//let's ensure it's the correct path
		assertEquals(command.getCommand(),
				"Push back granted: G3");

		assertEquals(command.getEchoType(),EchoType.PUSHBACK_GRANTED);

		//echo this back to the ground control agent
		groundControl.msgEchoCommand(pilot2,command.getCommand(),command.getEchoType());

		//make the ground control pick another action - which should be the verification
		groundControl.pickAndExecuteAnAction();
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		//hould still be taxiing (to the knowledge of the ground control)
		assertEquals(departure.getStatus(),DepartureMediator.DepartureStatus.RequestingDirections);

		//let the ground control give directions
		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		assertEquals(issued.size(),2);	//there should be only one queue
		assertTrue(issued.containsKey(pilot2));

		assertNotNull(queue);

		assertEquals(queue.size(),1);	//we have only issued one command

		command = queue.peek();

		//let's ensure it's the correct path
		assertEquals(command.getCommand(),
				"Instructions: Cross W, Turn into Y alpha, Turn into 6R alpha, Cross Z, Cross AA, Turn into BB alpha, Taxi to 24R");

		assertEquals(command.getEchoType(),EchoType.HERE_ARE_INSTRUCTIONS);

		//echo this back to the ground control agent
		groundControl.msgEchoCommand(pilot2,command.getCommand(),command.getEchoType());

		result = groundControl.pickAndExecuteAnAction();

		assertTrue(result);

		assertEquals(departures.size(),0);

		//END DEPARTURE CODE



		//normative test case concluded



		testSemaphore.release();
	}


}
