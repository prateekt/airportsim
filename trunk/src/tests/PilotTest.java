package tests;

import faa.FAAControl;
import junit.framework.TestCase;
import airport.objects.*;
import airport.TheSimulator;
import agents.*;
import agents.PilotAgent.PilotState;
import airport.mock.*;
import java.util.*;


public class PilotTest extends TestCase{

	//Real agent
	private PilotAgent pilot;
	
	//mock objects
	private MockClearanceDelivery cd;
	private MockGroundControl gc;
	private MockLocalControl lc;
	
	public PilotTest(String s) {
		super(s);
	}

	/**
	 * Method called to set up the test case. Basically initializes all objects
	 * used to tests and ensures all agents know about their connections.
	 */
	protected void setUp() {
		//initialize pilot agent
		pilot = new PilotAgent("PILOT", new Airport());
		
		//initialize mock objects
		cd = new MockClearanceDelivery("CD");
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
	 * Unit Test 3: Test whether pilot is initially who I think is.
	 *
	 * Tests: Constructor of PilotAgent
	 */
	public void testInitialPilotSetup() {
		assertTrue(pilot.getName().equals("PILOT"));
		assertTrue(pilot.getState()==PilotState.INITIAL);
		assertTrue(pilot.getRadio()!=null);
		assertTrue(pilot.getDirections()!=null);
		assertTrue(pilot.getCommandsToEcho()!=null);
		assertTrue(TheSimulator.activeCount()!=0);
	}
	/**
	 * Unit Test 4: Arrival Scenario
	 *
	 * Tests:
	 * 		All messages and actions related to basic arrival scenario of 1 plane.
	 */
	public void testArrival() {
		
		//pilot sees runway
		assertTrue(pilot.getState()==PilotState.INITIAL);
		pilot.msgISeeARunway("RUNWAY");
		assertTrue(pilot.getState()==PilotState.SAW_RUNWAY);
		assertTrue(pilot.getFlight().getWay().equals("RUNWAY"));
		
		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getState()==PilotState.AWAITING_RESPONSE);
		assertTrue(pilot.pickAndExecuteAnAction()==false);
		
		//receive landing clearance
		pilot.msgClearedToLand(lc, "RUNWAY blah blah", "WIND_CONDITION");
		assertTrue(pilot.getState()==PilotState.CLEARED_TO_LAND);
		assertTrue(pilot.getAirplane()!=null);
		
		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.pickAndExecuteAnAction()==false);
		
		//contact gc request
		pilot.msgPleaseContactGround(lc, 1.11, new ArrayList<String>(), new ArrayList<AirplaneAction>());
		assertTrue(pilot.getCommandsToEcho().size()==1);
		
		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getCommandsToEcho().size()==0);
		assertTrue(pilot.pickAndExecuteAnAction()==false);
		
		//get arrival instructions
		pilot.msgHereAreArrivalInstructions(gc, new ArrayList<String>(), new ArrayList<AirplaneAction>());
		assertTrue(pilot.getCommandsToEcho().size()==1);
		assertTrue(pilot.getState()==PilotState.NAVIGATING_DOCK_ROUTE);
		
		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getCommandsToEcho().size()==0);
		assertTrue(pilot.pickAndExecuteAnAction()==false);
	}

	/**
	 * Unit Test 4: Departure Scenario
	 *
	 * Tests:
	 * 		All messages and actions related to basic departure scenario of 1 plane.
	 */
	public void testDeparture() {
		
		//wants to take off
		assertTrue(pilot.getState()==PilotState.INITIAL);
		pilot.msgIWantToTakeOff("gate", "destination");
		assertTrue(pilot.getState()==PilotState.REQUEST_CLEARANCE);

		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getState()==PilotState.AWAITING_RESPONSE);
		assertTrue(pilot.pickAndExecuteAnAction()==false);
		
		
		//receive mode
		pilot.msgHereIsMode(cd, "guidanceMode", 'A');
		assertTrue(pilot.getCommandsToEcho().size()==1);
		assertTrue(pilot.getState()==PilotState.REQUEST_PUSHBACK);
		
		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getCommandsToEcho().size()==0);
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getState()==PilotState.AWAITING_RESPONSE);
		assertTrue(pilot.pickAndExecuteAnAction()==false);

		//pushback granted
		pilot.msgPushBackGranted(gc, "gate");
		assertTrue(pilot.getState()==PilotState.CLEARED_TO_PUSHBACK);
		assertTrue(pilot.getCommandsToEcho().size()==1);

		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getCommandsToEcho().size()==0);
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getState()==PilotState.AWAITING_RESPONSE);
		assertTrue(pilot.pickAndExecuteAnAction()==false);

		//departure instructions
		pilot.msgHereAreDepartureInstructions(gc,new ArrayList<String>(), new ArrayList<AirplaneAction>());
		assertTrue(pilot.getState()==PilotState.NAVIGATING_DEPARTURE_ROUTE);
		assertTrue(pilot.getCommandsToEcho().size()==1);

		//execute action
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getCommandsToEcho().size()==0);
		assertTrue(pilot.pickAndExecuteAnAction()==false);

		//contact lc
		pilot.setState(PilotState.MUST_CONTACT_LC);
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getState()==PilotState.AWAITING_RESPONSE);
		
		//position and hold
		pilot.msgPositionAndHold(lc);
		assertTrue(pilot.getState()==PilotState.HOLDING);
		
		//cleared for takeoff
		pilot.msgClearedForTakeOff();
		assertTrue(pilot.getState()==PilotState.TAKEOFF);
		
		//good bye
		pilot.msgGoodBye();
/*		assertTrue(pilot.getCommandsToEcho().size()==1);
		assertTrue(pilot.pickAndExecuteAnAction());
		assertTrue(pilot.getCommandsToEcho().size()==0);
		assertTrue(pilot.pickAndExecuteAnAction()==false);*/		
	}	

}
