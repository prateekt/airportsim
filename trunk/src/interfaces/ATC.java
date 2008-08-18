package interfaces;
import guiIntegration.AgentPair;
import agents.EchoType;

/*
 * @author  Prateek Tandon
 */
public interface ATC {
	public void msgEchoCommand(Pilot pilot,String echoedCommand, EchoType echoType);	
	public AgentPair getPair();
}
