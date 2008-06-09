/**
 *
 */
package interfaces;

import agents.Flight;
import java.util.*;
import airport.objects.*;
import guiIntegration.AgentPair;

/**
 * @author Henry Yuen
 *
 */
public interface Pilot {

	//ARRIVAL SCENARIO///////////////////////////////////////////////

	/**
	 * Called by an external iniator, telling the Pilot that he has seen
	 * the runway and is ready to land
	 * @param runway The runway that the pilot is going to land on
	 */
	public void msgISeeARunway(String runway);

	/**
	 * Sent by the LocalControl, permitting the Pilot to land
	 * @param localControl Reference to the callee
	 * @param windCondition Description of the wind conditions
	 */
	public void msgClearedToLand(LocalControl localControl,String runway,String windCondition);

	/**
	 *
	 * @param localControl
	 * @param frequency
	 * @param additionalDirections
	 */
	public void msgPleaseContactGround(LocalControl localControl,
			double frequency,
			ArrayList<String> additionalDirections,
			ArrayList<AirplaneAction> actions);

	/**
	 *
	 * @param groundControl
	 * @param routePoints
	 */
	public void msgHereAreArrivalInstructions(GroundControl groundControl,
			ArrayList<String> additionalDirections,
			ArrayList<AirplaneAction> actions);


	//DEPARTURE SCENARIO/////////////////////////////////////////////////////


	/**
	 * Departure clearance is granted by the ClearanceDelivery agent.
	 *
	 * @param
	 */
	public void msgClearanceGranted(ClearanceDelivery cd,String runway, double frequency,int transponderCode);

	public void msgHereIsMode(ClearanceDelivery cd,String guidanceMode,char mode);

	public void msgPushBackGranted(GroundControl gc,String gate);

	public void msgHereAreDepartureInstructions(GroundControl gc,ArrayList<String> additionalCommands,
			ArrayList<AirplaneAction> direcionMap);

	public void msgPositionAndHold(LocalControl lc);

	public void msgClearedForTakeOff();

	public void msgGoodBye();

	//some accessor features
	public Airplane getAirplane();

	public AgentPair getPair();

	public Flight getFlight();

	public String getName();
	public String toString();

	/**
	 * @return the myGC
	 */
	public GroundControl getMyGC();

	/**
	 * @param myGC the myGC to set
	 */
	public void setMyGC(GroundControl myGC);

	/**
	 * @return the myLC
	 */
	public LocalControl getMyLC();

	/**
	 * @param myLC the myLC to set
	 */
	public void setMyLC(LocalControl myLC);

	/**
	 * @return the myCD
	 */
	public ClearanceDelivery getMyCD();

	/**
	 * @param myCD the myCD to set
	 */
	public void setMyCD(ClearanceDelivery myCD);


}
