package guiIntegration;
import java.util.concurrent.*;

/*
 * A pairing of an Agent's name and the type of agent that they are.
 * 
 * @author Prateek Tandon
 */
public class AgentPair {
	
	public enum AgentType {
		PILOT, GROUND_CONTROL, LOCAL_CONTROL, CLEARANCE_DELIVERY
	}
	
	private String name;
	private AgentType type;
	private Semaphore voiceSemaphore;

	public AgentPair(String name, AgentType type, Semaphore voiceSemaphore) {
		this.name = name;
		this.type = type;
		this.voiceSemaphore = voiceSemaphore;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the type
	 */
	public AgentType getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(AgentType type) {
		this.type = type;
	}

	/**
	 * @return the voiceSemaphore
	 */
	public Semaphore getVoiceSemaphore() {
		return voiceSemaphore;
	}

	/**
	 * @param voiceSemaphore the voiceSemaphore to set
	 */
	public void setVoiceSemaphore(Semaphore voiceSemaphore) {
		this.voiceSemaphore = voiceSemaphore;
	}
}
