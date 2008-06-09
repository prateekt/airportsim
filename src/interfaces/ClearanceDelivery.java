package interfaces;

/*
 * @author Henry Yuen, Prateek Tandon
 */
public interface ClearanceDelivery extends ATC {
	void msgRequestingClearance(Pilot pilot, String planeName, String gate, String destination);
}
