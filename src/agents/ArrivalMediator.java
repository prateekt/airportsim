package agents;

public class ArrivalMediator extends Mediator {
	public enum ArrivalStatus {Flying, ReadyToLand, ClearedToLand,
		Landed,
		Landing,
		Taxiing, Arrived,AwaitingResponse,Docked};

	public String getStatusString() {
		if (status == ArrivalStatus.Flying) {
			return "In Air";
		}
		if (status == ArrivalStatus.ReadyToLand) {
			return "Ready to Land";
		}
		if (status == ArrivalStatus.ClearedToLand) {
			return "Cleared to Land";
		}
		if (status == ArrivalStatus.Landed) {
			return "Landed. Awaiting arrival instructions";
		}
		if (status == ArrivalStatus.Landing) {
			return "Landing";
		}
		if (status == ArrivalStatus.Taxiing) {
			return "Taxiing";
		}
		if (status == ArrivalStatus.Arrived) {
			return "Arrived";
		}

		return null;
	}

	private ArrivalStatus status;

	public ArrivalMediator(Flight flight){
		f = flight;
		status = ArrivalStatus.Flying;
	}

	public ArrivalStatus getStatus(){
		return status;
	}
	public String getStringStatus(){
		return status.toString();
	}
	public void setStatus(ArrivalStatus status){
		this.status = status;
	}

}
