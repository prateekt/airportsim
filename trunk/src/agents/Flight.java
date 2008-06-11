package agents;

/*
 * Temporary mediator Flight object.
 *
 * @author Prateek Tandon
 */
public class Flight {
	public enum FlightType { Arrival, Departure }

	private FlightType type;
	private String planeName;
	private String gate;
	private String way;
	private String guidanceMode;
	private String location;
	private String origin;
	private String destination;
	private long time;


	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	/*
	 * Minimalist constructor
	 */
	public Flight() {

	}
	public Flight(String name) {
		planeName = name;
	}

	public Flight(String gate,String runway,String guidanceMode,Flight.FlightType type) {
		this.gate = gate;
		this.way = runway;
		this.guidanceMode = guidanceMode;
		this.type = type;
		time = 0;
	}

	public Flight(String planeName,String gate, String runway, String guidanceMode, String type, String origin, String destination) {
		this.planeName = planeName;
		this.gate = gate;
		this.way = runway;
		this.guidanceMode = guidanceMode;
		if (type.equals("Arrival")){
			this.type = FlightType.Arrival;
		}
		else{
			this.type = FlightType.Departure;
		}

		time = 0;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the destination
	 */
	public String getDestination() {
		return destination;
	}
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}
	/**
	 * @return the gate
	 */
	public String getGate() {
		return gate;
	}
	/**
	 * @param gate the gate to set
	 */
	public void setGate(String gate) {
		this.gate = gate;
	}
	/**
	 * @return the way
	 */
	public String getWay() {
		return way;
	}
	/**
	 * @param way the way to set
	 */
	public void setWay(String runway) {
		this.way = runway;
	}
	/**
	 * @return the guidanceMode
	 */
	public String getGuidanceMode() {
		return guidanceMode;
	}
	/**
	 * @param guidanceMode the guidanceMode to set
	 */
	public void setGuidanceMode(String guidanceMode) {
		this.guidanceMode = guidanceMode;
	}
	/**
	 * @return the planeName
	 */
	public String getPlaneName() {
		return planeName;
	}
	/**
	 * @param planeName the planeName to set
	 */
	public void setPlaneName(String planeName) {
		this.planeName = planeName;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String source) {
		this.origin = source;
	}
	public FlightType getType() {
		return type;
	}
	public void setType(FlightType type) {
		this.type = type;
	}

	public String toString() {
		return planeName;
	}
}
