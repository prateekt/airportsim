package interfaces;
import guiIntegration.*;
import agents.*;

/*
 * @author  Prateek Tandon
 */
public interface ATC {
	public void msgEchoCommand(Pilot pilot,String echoedCommand, EchoType echoType);	
	public AgentPair getPair();
}
