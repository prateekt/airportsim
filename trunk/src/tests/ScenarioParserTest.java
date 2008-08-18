package tests;

import guiIntegration.AgentPair;
import interfaces.ClearanceDelivery;
import interfaces.GroundControl;
import interfaces.LocalControl;

import java.util.List;

import junit.framework.TestCase;
import agents.PilotAgent;
import agents.ScenarioParser;
import airport.objects.Airport;

/**
 * Tests the loading of XML scenario files, and airport files
 * @author Henry Yuen
 *
 */
public class ScenarioParserTest extends TestCase {


	/**
	 * The only scenario file we have now
	 * is the LAX scenario file, so we'll just test based on that
	 */
	public void testLAXScenario() throws Exception {
		List<PilotAgent> pilots = null;
		LocalControl la = null;
		GroundControl gc = null;
		ClearanceDelivery ca = null;
		Airport airport = null;

		//first, load the scenario
		ScenarioParser p = new ScenarioParser();
		p.load("resource/scenarios/lax.xml");

		la = p.getLocalControlAgent();
		assertNotNull(la);
		//let's check the attributes of the la
		assertEquals(la.getPair().getType(),AgentPair.AgentType.LOCAL_CONTROL);

		gc = p.getGroundControlAgent();
		assertNotNull(gc);
		assertEquals(gc.getPair().getType(),AgentPair.AgentType.GROUND_CONTROL);

		ca = p.getClearanceAgent();
		assertNotNull(ca);
		assertEquals(ca.getPair().getType(),AgentPair.AgentType.CLEARANCE_DELIVERY);

		airport = p.getAirport();
		assertNotNull(airport);

		//let's ensure several things about the LAX airport
		assertEquals(airport.getName(),"LAX");
		assertEquals(airport.getGates().size(),2);
		assertEquals(airport.getHangars().size(),1);
		assertEquals(airport.getWays().size(),9);
		assertEquals(airport.getSpecialWay().getName(),"24R");


		pilots = p.getPilots();
		assertNotNull(pilots);
		assertEquals(pilots.size(),2);

		for(final PilotAgent pilot: pilots) {
			assertNotNull(pilot);
			assertNotNull(pilot.getFlight());
			assertNotNull(pilot.getAirplane());

		}

	}
}

