package airport.mock;

import agents.EchoType;
import interfaces.*;
import guiIntegration.*;

/*
 * Mock Ground Control object for unit testing.
 *
 * @author Henry Yuen
 */
public class MockGroundControl implements GroundControl{

	private String name;
	
	public MockGroundControl(String name) {
		this.name = name;
	}
	
	public MockGroundControl() {
		this.name = "name";
	}
	
	
	//ARRIVAL MESSAGES/////////////////////////////////////

	/**
	 * Called by Pilot after he is told by the LocalControl to talk to Ground
	 * @param pilot
	 * @param planeName
	 * @param currentAction
	 */
	public void msgHelloGround(Pilot pilot, String planeName, String gate) {
		System.out.println("Received Msg: pilot " + pilot + " said hello.");
	}

	/**
	 * called by the Pilot when he wants to be pushed back from the gate.
	 * @param pilot
	 * @param gate
	 * @param location
	 */
	public void msgRequestPushBack(Pilot pilot,String gate,String location,String thing) {
		System.out.println("Received Msg: Pilot " + pilot + "requested pushback.");
	}


	/**
	 * called by the Pilot when he is ready to be transferred.
	 * @param pilot
	 * @param gate
	 * @param location
	 */
	public void msgTaxiComplete(Pilot pilot,String gate,String location) {
		System.out.println("Received Msg: Pilot " + pilot + " said taxi complete.");
	}

	/**
	 * called by the Pilot when he wants to be pushed back from the gate.
	 * @param pilot
	 * @param gate
	 * @param location
	 */
	public void msgRequestPushBack(Pilot pilot,String planeName,int flightNumber,
			String runway,
			String gate,
			String currentAction) {
		System.out.println("Received Msg: Pilot " + pilot + " has requested pushback.");
	}

	/**
	 * called by the Pilot when he is ready to be transferred.
	 * @param pilot
	 * @param currentLoc
	 */
	public void msgPushbackComplete(Pilot pilot, String currentLoc) {
		System.out.println("Received Msg: Pilot " + pilot + " has said taxi complete.");
	}

	public void msgEchoCommand(Pilot pilot, String echoedCommand, EchoType et) {
		System.out.println("Recieved Msg from pilot " + pilot + " echo: " + echoedCommand);
	}

	public void msgIHaveDocked(Pilot pilot) {

	}

	public AgentPair getPair() {
		return null;
	}

	public void msgTaxiComplete(Pilot pilot) {
		// TODO Auto-generated method stub

	}
}
