package agents;

/**
 *
 * @author Patrick Monroe
 *
 */
public class DepartureMediator extends Mediator {

	public enum DepartureStatus {
		RequestingClearance,
		RequestingPushback,
		PushbackRequested,
		RequestingDirections,
		RequestingTakeOff,
		AwaitingTakeOff,
		TakenOff,
		Pushback,
		AwaitingResponse,
		Taxiing,
		TakingOff,
		};

	public String getStatusString() {

		if (status == DepartureStatus.RequestingClearance) {
			return "Requesting Clearance";
		}
		if (status == DepartureStatus.Pushback) {
			return "Performing Pushback";
		}
		if (status == DepartureStatus.RequestingPushback) {
			return "Requesting Pushback";
		}
		if (status == DepartureStatus.RequestingDirections) {
			return "Requesting Directions";
		}
		if (status == DepartureStatus.RequestingTakeOff) {
			return "Requesting Takeoff";
		}
		if (status == DepartureStatus.AwaitingTakeOff) {
			return "Awaiting Takeoff";
		}
		if (status == DepartureStatus.TakenOff) {
			return "Taken Off";
		}
		if (status == DepartureStatus.AwaitingResponse) {
			return "Awaiting Response";
		}
		if (status == DepartureStatus.Taxiing) {
			return "Taxiing";
		}

		if (status == DepartureStatus.TakingOff) {
			return "Taking Off";
		}



		return null;
	}

	private DepartureStatus status;
	public DepartureMediator(Flight flight){
		f = flight;
		status = DepartureStatus.RequestingClearance;
	}

	public DepartureStatus getStatus(){
		return status;
	}
	public String getStringStatus(){
		return status.toString();
	}
	public void setStatus(DepartureStatus status){
		this.status = status;
	}

}
