package interfaces;

/*
 * @author Henry Yuen
 */
public interface GroundControl extends ATC{

	//ARRIVAL MESSAGES/////////////////////////////////////

	/**
	 * Called by Pilot after he is told by the LocalControl to talk to Ground
	 * @param pilot
	 * @param planeName
	 * @param currentAction
	 */
	public void msgHelloGround(Pilot pilot, String planeName,String gate);

	/**
	 * called by the Pilot when he wants to be pushed back from the gate.
	 * @param pilot
	 * @param gate
	 * @param location
	 */
	public void msgRequestPushBack(Pilot pilot,String planeName,
			String runway,
			String gate);


	/**
	 * called by the Pilot when he is ready to be transferred.
	 * @param pilot
	 * @param currentLoc
	 */
	public void msgPushbackComplete(Pilot pilot, String currentLoc);

	public void msgIHaveDocked(Pilot pilot);
	public void msgTaxiComplete(Pilot pilot);

}
