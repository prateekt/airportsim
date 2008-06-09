package agents;

/*
 * @author Patrick  Monroe
 */
public abstract class Mediator {
	protected Flight f;

	/**
	 *
	 * @return f
	 */
	public Flight getFlight(){
		return f;
	}

	public String getStatusString() {
		return "";
	}


}
