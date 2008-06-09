package interfaces;

/*
 * @author Prateek Tandon
 */
public interface LocalControl extends ATC {
	public void msgReadyToLand(Pilot pilot,String planeName,String approachType, String runway);
	public void msgIAmOnRunway(Pilot pilot,String runway);
	public void msgIHaveLanded(Pilot pilot);
	public void msgIHaveTakenOff(Pilot pilot);
}
