package airport.mock;
import guiIntegration.AgentPair;
import interfaces.LocalControl;
import interfaces.Pilot;
import agents.EchoType;

/*
 * Mock object for unit testing.
 *
 * @author Prateek Tandon
 */
public class MockLocalControl implements LocalControl{
	private String name;
	
	public MockLocalControl(String name) {
		this.name = name;
	}
	
	public MockLocalControl() {
		this.name = "name";
	}

	public void msgReadyToLand(Pilot pilot, String planeName, String approachType, String runway) {
		System.out.println("Received Msg: Pilot " + pilot + " is ready to land.");
	}
	public void msgIAmOnRunway(Pilot pilot, String runway) {
		System.out.println("Received Msg: Pilot " + " is on runway " + runway);
	}

	public void msgEchoCommand(Pilot pilot, String echoedCommand, EchoType et) {
		System.out.println("Recieved Msg from pilot " + pilot + " echo: " + echoedCommand);
	}
	
	public void msgIHaveLanded(Pilot pilot) {
		
	}
	
	public  void msgIHaveTakenOff(Pilot pilot) {
		
	}

	public AgentPair getPair() {
		return null;
	}

}
