package guiIntegration;

/*
 * A Trace Message is a data container for a single message or action executed or sent by an agent. 
 * The class is used for interfacing the GUI with the backend.
 * 
 * @author Prateek Tandon
 */
public class TraceMessage {

	public enum TraceType {
		MESSAGE, ACTION
	}

	/*
	 * Who the message is to.
	 */
	private AgentPair to;

	/*
	 * Who the message is from.
	 */
	private AgentPair from;

	/*
	 * The text of the message.
	 */
	private String message;

	/*
	 * The type of trace
	 */
	private TraceType type;

	/*
	 * The constructor for creating a Trace Message
	 */
	public TraceMessage(AgentPair to, AgentPair from, String message) {
		this.to = to;
		this.from = from;
		this.message = message;
		this.type = TraceType.MESSAGE;
	}

	/*
	 * The constructor for creating a Action Message
	 */
	public TraceMessage(String message) {
		this.to = null;
		this.from = null;
		this.message = message;
		this.type = TraceType.ACTION;
	}

	/**
	 * @return the to
	 */
	public AgentPair getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(AgentPair to) {
		this.to = to;
	}

	/**
	 * @return the from
	 */
	public AgentPair getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(AgentPair from) {
		this.from = from;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the type
	 */
	public TraceType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(TraceType type) {
		this.type = type;
	}	
}
