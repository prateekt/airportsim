package guiIntegration;

import java.util.*;
import faa.FAAControl;
import gui.*;
import java.util.concurrent.*;

/*
 * Object used to interface with trace gui components with CSCI 105.
 * The TraceDB is a singleton.
 *
 * @author Prateek Tandon
 */
public class TraceDB {

	/*
	 * The instance of the TraceDB
	 */
	private static TraceDB instance;

	/*
	 * Queue of messages.
	 */
	private List<TraceMessage> traceMessages;

	private TracePanel tracePanel;

	private TraceDB() {
		traceMessages = Collections.synchronizedList(new ArrayList<TraceMessage>());
		tracePanel = null;
	}

	/*
	 * Returns instance of TraceDB.
	 */
	public static TraceDB getInstance() {
		if(instance==null){
			instance = new TraceDB();
		}
		return instance;
	}

	public void setTracePanel(TracePanel panel) {
		tracePanel = panel;
	}
	/*
	 * Method used to update the message trace on the GUI.
	 */
	public void updateMessageTrace(AgentPair to, AgentPair from, String content) {
		TraceMessage t = new TraceMessage(to, from, content);
		synchronized (traceMessages) {
			traceMessages.add(t);
		}

		if (tracePanel != null) tracePanel.update();

		Semaphore voiceSemaphore = to.getVoiceSemaphore();
		try {
			if(voiceSemaphore.availablePermits()==0) {
				voiceSemaphore.release();
			}

			voiceSemaphore.acquire();
			
			if(from!=null && from.getType()==AgentPair.AgentType.PILOT) {
				FAAControl.getSpeakerEngine().msgPleaseSpeak(content, voiceSemaphore, 0);
			}
			else if(from!=null && from.getType()==AgentPair.AgentType.LOCAL_CONTROL) {
				FAAControl.getSpeakerEngine().msgPleaseSpeak(content, voiceSemaphore, 1);
			}
			else if(from!=null && from.getType()==AgentPair.AgentType.GROUND_CONTROL) {
				FAAControl.getSpeakerEngine().msgPleaseSpeak(content, voiceSemaphore, 2);				
			}
			else if(from!=null && from.getType()==AgentPair.AgentType.CLEARANCE_DELIVERY) {
				FAAControl.getSpeakerEngine().msgPleaseSpeak(content, voiceSemaphore, 3);				
			}
			else {
				voiceSemaphore.release();
			}
			
			voiceSemaphore.acquire();

			if(voiceSemaphore.availablePermits()==0) {
				voiceSemaphore.release();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Method used to update the message trace on the GUI.
	 */
	public void updateMessageTrace_ECHO(AgentPair to, AgentPair from, String content) {
		TraceMessage t = new TraceMessage(to, from, content);
		traceMessages.add(t);
	}




	/**
	 * @return the traceMessages
	 */
	public synchronized List<TraceMessage> getTraceMessages() {
		return traceMessages;
	}

	/**
	 * @param traceMessages the traceMessages to set
	 */
	public synchronized void setTraceMessages(List<TraceMessage> traceMessages) {
		this.traceMessages = traceMessages;
	}
}
