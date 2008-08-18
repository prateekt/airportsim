package airport.mock;
import guiIntegration.AgentPair;
import interfaces.ClearanceDelivery;
import interfaces.Pilot;
import agents.EchoType;

/*
 * Mock Clearance Delivery object for unit testing.
 * 
 * @author Prateek Tandon
 */
public class MockClearanceDelivery implements ClearanceDelivery{
	
	private String name;
	
	public MockClearanceDelivery(String name) {
		this.name = name;
	}
	
	public MockClearanceDelivery(){
		name = "Bert";
	}
	
	public void msgRequestingClearance(Pilot pilot, String planeName, String gate, String destination) {
		System.out.println("Pilot " + pilot + "just requested clearance.");
	}
	
	public void msgEchoCommand(Pilot pilot, String echoedCommand, EchoType echoType) {
		System.out.println("Pilot " + pilot + " just echoed command. Here is his echo: " + echoedCommand);
	}
	
	public void msgRequestPushBack(Pilot pilot, String currentGate) {
		System.out.println("Pilot " + pilot + "requested pushback.");
	}
	
	public void msgTaxiComplete(Pilot pilot, String currentLoc) {
		System.out.println("Pilot " + pilot + " finished getting taxied.");		
	}
	
	public AgentPair getPair() {
		return null;
	}
		
}
