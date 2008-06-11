package tests;

import junit.framework.TestCase;
import agents.ClearanceAgent;
import agents.Flight;
import faa.FAAControl;
import airport.mock.MockGroundControl;
import airport.mock.MockLocalControl;
import airport.mock.MockPilot;
import airport.objects.Airport;

/*
 * ===Patrick did an extremely good job on his unit tests. However, the API for the clearance
 * agent was changed so the unit tests (now commented) had to be changed as well.===
 *
 * @author Patrick Monroe
 *
 */
public class ClearanceTest extends TestCase {

	//Real agent
	private ClearanceAgent cd;

	//mock objects
	private MockPilot pilot;
	private MockGroundControl gc;
	private MockLocalControl lc;

	public ClearanceTest(String s) {
		super(s);
	}

	/**
	 * Method called to set up the test case. Basically initializes all objects
	 * used to tests and ensures all agents know about their connections.
	 */
	protected void setUp() {
		//initialize pilot agent
		cd = new ClearanceAgent("CD", new Airport());

		//initialize mock objects
		pilot = new MockPilot("Pilot");
		gc = new MockGroundControl("GC");
		lc =  new MockLocalControl("LC");

		//initialize  agent connections
		pilot.setMyCD(cd);
		pilot.setMyGC(gc);
		pilot.setMyLC(lc);
		pilot.setFlight(new Flight());

		//turn voice off
		FAAControl.getSpeakerEngine().setVoiceOn(false);
	}

	/**
	 * Unit Test 1: Does  JUnit even work?
	 */
	public void testJUnit() {
		assertTrue(true);
	}
	/**
	 * Unit Test 2: Does my setup actually work?
	 */
	 public void testSetUp() {
		assertTrue(true);
		assertTrue(pilot!=null);
		assertTrue(cd!=null);
		assertTrue(gc!=null);
		assertTrue(lc!=null);
	}

	/**
	 * Unit Test 3: Test whether cd is initially who I think is.
	 *
	 * Tests: Constructor of cd
	 */
	public void testInitialCDSetup() {
		//assertTrue(cd.getClearanceRequests()!=null);
	}

	public void testClearanceScenario() {
		//assertTrue(cd.getClearanceRequests()!=null);
		cd.msgRequestingClearance(pilot, "planeName", "gate", "destination");
		//assertTrue(cd.getClearanceRequests().size()==1);
		//assertTrue(cd.pickAndExecuteAnAction());
		//assertTrue(cd.getClearanceRequests().size()==0);
	}

	//Basic Case: 1 pilot requesting Clearance
	/*public void testClearance1(){

		//Setup the Test Data
		MockPilot mp = new MockPilot("Mock Pilot");
		Flight f = new Flight("SW111" , "A4", "RW8", "VISUAL", "Departure", "LAX", "JFK");
		mp.setFlight(f);
		ClearanceAgent ca = new ClearanceAgent("CA", new Airport());

		//Check that the CA has 0 requests pending
		assertTrue(ca.getClearanceRequestSize() == 0);

		//Tell the MockPilot to message the ClearanceAgent requesting Clearance
		mp.msgPleaseContactClearance(ca, mp.getFlight());

		//Check that the CA received the message and placed it in its ClearanceRequest Queue
		assertTrue(ca.getClearanceRequestSize() == 1);

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check thats the correct rule fired
		assertTrue(ca.getTopSchedule() == "Process Requests");

		//Check that the correct action fired
		assertTrue(ca.getTopAction() == "Clearance Granted");

		//Check that the Clearance Request is empty again
		assertTrue(ca.getClearanceRequestSize() == 0);

		//Tell the MockPilot to Echo back the command just given
		mp.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//Tell the MockPilot to Echo back the command just given
		mp.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");
		System.out.println("******END OF TEST 1*******");
	}*/

	//Basic Case: 2 pilots requesting Clearance
	/*public void testClearance2(){

		//Setup the Test Data
		MockPilot mp = new MockPilot("Mock P1");
		Flight f = new Flight("SW111" , "A4", "RW8", "VISUAL", "Departure", "LAX", "JFK");
		mp.setFlight(f);

		MockPilot mp2 = new MockPilot("Mock P2");
		Flight f2 = new Flight("SW112" , "A9", "RW2", "VISUAL", "Departure", "LAX", "JFK");
		mp2.setFlight(f2);

		ClearanceAgent ca = new ClearanceAgent("CA", new Airport());

		//Check that the CA has 0 requests pending
		assertTrue(ca.getClearanceRequestSize() == 0);

		//Tell the MockPilot's to message the ClearanceAgent requesting Clearance
		mp.msgPleaseContactClearance(ca, mp.getFlight());
		mp2.msgPleaseContactClearance(ca, mp2.getFlight());

		//Check that the CA received the message and placed it in its ClearanceRequest Queue
		assertTrue(ca.getClearanceRequestSize() == 2);

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check thats the correct rule fired
		assertTrue(ca.getTopSchedule() == "Process Requests");

		//Check that the correct action fired
		assertTrue(ca.getTopAction() == "Clearance Granted");

		//Check that the Clearance Request is down to 1
		assertTrue(ca.getClearanceRequestSize() == 1);

		//Tell the MockPilot to Echo back the command just given
		mp.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//Tell the MockPilot to Echo back the command just given
		mp.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//******************  Pilot 2 ********************

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check thats the correct rule fired
		assertTrue(ca.getTopSchedule() == "Process Requests");

		//Check that the correct action fired
		assertTrue(ca.getTopAction() == "Clearance Granted");

		//Check that the Clearance Request is down to 1
		assertTrue(ca.getClearanceRequestSize() == 0);

		//Tell the MockPilot to Echo back the command just given
		mp2.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//Tell the MockPilot to Echo back the command just given
		mp2.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		System.out.println("******END OF TEST 2*******");
	}*/

	//WACKY CASE! Pilot 1 is first to have request processed, but waits to start echoing commands
	//until after P2 has completed his full run.
	/*public void testClearance3(){

		//Setup the Test Data
		MockPilot mp = new MockPilot("Mock P1");
		Flight f = new Flight("SW111" , "A4", "RW8", "VISUAL", "Departure", "LAX", "JFK");
		mp.setFlight(f);

		MockPilot mp2 = new MockPilot("Mock P2");
		Flight f2 = new Flight("SW112" , "A9", "RW2", "VISUAL", "Departure", "LAX", "JFK");
		mp2.setFlight(f2);

		ClearanceAgent ca = new ClearanceAgent("CA", new Airport());

		//Check that the CA has 0 requests pending
		assertTrue(ca.getClearanceRequestSize() == 0);

		//Tell the MockPilot's to message the ClearanceAgent requesting Clearance
		mp.msgPleaseContactClearance(ca, mp.getFlight());
		mp2.msgPleaseContactClearance(ca, mp2.getFlight());

		//Check that the CA received the message and placed it in its ClearanceRequest Queue
		assertTrue(ca.getClearanceRequestSize() == 2);

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check thats the correct rule fired
		assertTrue(ca.getTopSchedule() == "Process Requests");

		//Check that the correct action fired
		assertTrue(ca.getTopAction() == "Clearance Granted");

		//Check that the Clearance Request is down to 1
		assertTrue(ca.getClearanceRequestSize() == 1);

		//******************  Pilot 2 ********************

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check thats the correct rule fired
		assertTrue(ca.getTopSchedule() == "Process Requests");

		//Check that the correct action fired
		assertTrue(ca.getTopAction() == "Clearance Granted");

		//Check that the Clearance Request is down to 1
		assertTrue(ca.getClearanceRequestSize() == 0);

		//Tell the MockPilot to Echo back the command just given
		mp2.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//Tell the MockPilot to Echo back the command just given
		mp2.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//******************PILOT 1
		//Tell the MockPilot to Echo back the command just given
		mp.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		//Tell the MockPilot to Echo back the command just given
		mp.msgEchoCommand();

		//Call the Scheduler and check that it performs something
		assertTrue(ca.callScheduler());

		//Check that the correct rule fired
		assertTrue(ca.getTopSchedule() == "Command Verify");

		System.out.println("******END OF TEST 3*******");
	}*/
}
