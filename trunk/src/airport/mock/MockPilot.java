package airport.mock;

import guiIntegration.AgentPair;
import interfaces.ClearanceDelivery;
import interfaces.GroundControl;
import interfaces.LocalControl;
import interfaces.Pilot;

import java.util.ArrayList;

import agent.StringUtil;
import agents.EchoType;
import agents.Flight;
import airport.objects.Airplane;
import airport.objects.AirplaneAction;

/*
 * Mock object for unit testing.
 *
 * @author Prateek Tandon
 */

public class MockPilot implements Pilot{

	private String name;
		
	public MockPilot() {
		this.name = "name";
	}

	
	GroundControl myGC;
	LocalControl myLC;
	ClearanceDelivery myCD;
	Flight flight;
	Airplane airplane;

	ArrayList<AirplaneAction> actions;

	public void setAirplane(Airplane airplane) {
		this.airplane = airplane;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MockPilot(String name) {
		this.name = name;
		actions = new ArrayList<AirplaneAction>();
	}

	//ARRIVAL SCENARIO///////////////////////////////////////////////

	/**
	 * Called by an external iniator, telling the Pilot that he has seen
	 * the runway and is ready to land
	 * @param runway The runway that the pilot is going to land on
	 */
	public void msgISeeARunway(String runway) {
		System.out.println("Msg Recieved: I see a runway - " + runway);
	}

	/**
	 * Sent by the LocalControl, permitting the Pilot to land
	 * @param localControl Reference to the callee
	 * @param windCondition Description of the wind conditions
	 */
	public void msgClearedToLand(LocalControl localControl,String runway,String windCondition) {
		System.out.println("Msg Recieved: LocalControl " + localControl + " says cleared  to land at runway " + runway);

	}

	/**
	 *
	 * @param localControl
	 * @param frequency
	 * @param additionalDirections
	 */
	public void msgPleaseContactGround(LocalControl localControl,
			double frequency,ArrayList<String> additionalDirections,ArrayList<AirplaneAction> directionsMap) {
		System.out.println("Msg Received: LocalControl " + localControl + " says to contact ground at frequency " + frequency + ".");
	}

	/**
	 *
	 * @param groundControl
	 * @param routePoints
	 */
	public void msgHereAreArrivalInstructions(GroundControl groundControl,ArrayList<String> routePoints,ArrayList<AirplaneAction> actions) {
		String route = StringUtil.separatedString(routePoints,", ");
		System.out.println("Msg Received: GroundControl " + groundControl + " has given a route: " + route);

		this.actions.addAll(actions);
	}


	//DEPARTURE SCENARIO/////////////////////////////////////////////////////


	/**
	 * Departure clearance is granted by the ClearanceDelivery agent.
	 *
	 * @param
	 */
	public void msgClearanceGranted(ClearanceDelivery cd,String runway, double frequency,int transponderCode) {
		System.out.println("Msg Received: Clearance Delivery " + cd + " has granted clearance for departure.");
	}

	public void msgHereIsMode(ClearanceDelivery cd,String guidanceMode,char mode) {
		System.out.println("Msg Received: Clearance Delivery " + cd + " has given a mode: " + mode);
	}

	public void msgPushBackGranted(GroundControl gc,String gate) {
		System.out.println("Msg Received: Ground Control " + gc + " has granted pushback.");
	}

	public void msgHereAreDepartureInstructions(GroundControl gc, ArrayList<String> directions,ArrayList<AirplaneAction> directionsMap) {
		System.out.println("Msg Received: Ground Control " + gc + " has given instructions.");
	}

	public void msgPositionAndHold(LocalControl lc) {
		System.out.println("Msg Received: Local Control " + lc + " has said to position and hold.");
	}

	public void msgClearedForTakeOff() {
		System.out.println("Msg Received: Cleared to take off.");
	}

	public AgentPair getPair() {
		return null;
	}

	public void msgEchoCommand(Pilot p, String c, EchoType t) {
		//unimplemented so never used
	}

	public void msgGoodBye() {

	}

	public Airplane getAirplane() {
		return airplane;
	}

	public Flight getFlight() {
		return flight;
	}

	public String getName() {
		return name;
	}

	public ClearanceDelivery getMyCD() {
		return myCD;
	}

	public GroundControl getMyGC() {
		return myGC;
	}

	public LocalControl getMyLC() {
		return myLC;
	}

	public void setMyCD(ClearanceDelivery myCD) {
		this.myCD = myCD;

	}

	public void setMyGC(GroundControl myGC) {
		this.myGC = myGC;

	}

	public void setMyLC(LocalControl myLC) {
		this.myLC = myLC;

	}

	public ArrayList<AirplaneAction> getActions() {
		return actions;
	}
}
