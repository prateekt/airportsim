package tests;

import interfaces.*;
import agent.*;
import agents.*;
import airport.objects.*;

import airport.mock.MockPilot;
import junit.framework.TestCase;
import agents.*;
import airport.objects.*;


/*
 * ===Patrick did an extremely good job on his unit tests. However, the API for the clearance
 * agent was changed so the unit tests (now commented) had to be changed as well.===
 * 
 * @author Patrick Monroe
 * 
 */
public class UserClearanceTest extends TestCase {

	//Test to check if the User Clearance Control works properly
/*	public void testUserClearance(){
		//Create Test Data
		MockPilot mp = new MockPilot("Mock Pilot");
		Flight f = new Flight("SW111" , "A4", "RW8", "VISUAL", "Departure", "LAX", "JFK");
		mp.setFlight(f);

		UserClearanceAgent uca = new UserClearanceAgent("Test UCA", new Airport());

		//Check that the UCA has not received any clearance requests
		assertTrue(uca.getCRSize() == 0);

		//Tell the mock pilot to request clearance
		//mp.msgPleaseContactClearance(uca, f);
		uca.msgRequestingClearance(mp,mp.getName(),f.getGate(),f.getDestination());

		//Check that the UCA has received one clearance request
		assertTrue(uca.getCRSize() == 1);

		//Tell the UCA to process the request.. Check the trace for confirmation
		uca.processTestRequest();

		//Check that the UCA has not received any clearance requests
		assertTrue(uca.getCRSize() == 0);

		//Tell the UCA to give the pilot a mode.. Check the trace for confirmation
		uca.testGiveMode(mp);

	}*/

}
