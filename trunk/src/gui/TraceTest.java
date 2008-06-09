package gui;

/**
 * This is a test of the LCTracePanel and GCTracePanel classes
 * To test
 * written by: John Baldo
 */

import guiIntegration.*;
import java.util.concurrent.*;
import guiIntegration.AgentPair.*;

import java.awt.*;
import javax.swing.*;

public class TraceTest extends JFrame{

	/**
	 * CHANGE NAME HERE TO TEST OTHER MESSAGE PANEL
	 */
	private static TracePanel test;


	public TraceTest(){
		/**
		 * CHANGE NAME HERE TO TEST OTHER MESSAGE PANEL
		 */
		test = new TracePanel();

		setSize(500,250);

		//Fire Some Test Messages

		AgentPair m1To = new AgentPair("DELTA123", AgentType.PILOT, new Semaphore(1,true));
		AgentPair m1From = new AgentPair("Jill", AgentType.LOCAL_CONTROL, new Semaphore(1,true));
		String m1m = "Flight DELTA123 Cleared to land on Runway 2";
		TraceMessage m1 = new TraceMessage(m1To, m1From, m1m);

		MessageData.getInstance().fireTraceMessage(m1);

		AgentPair m2To = new AgentPair("TWA123", AgentType.PILOT, new Semaphore(1,true));
		AgentPair m2From = new AgentPair("Bob", AgentType.GROUND_CONTROL, new Semaphore(1,true));
		String m2m = "Flight TWA123 Please turn right onto Taxiway 4A";
		TraceMessage m2 = new TraceMessage(m2To, m2From, m2m);

		MessageData.getInstance().fireTraceMessage(m2);

		AgentPair m3To = new AgentPair("Bob", AgentType.GROUND_CONTROL, new Semaphore(1,true));
		AgentPair m3From = new AgentPair("Jill", AgentType.LOCAL_CONTROL, new Semaphore(1,true));
		String m3m = "Flight DELTA123 Cleared for Ground Guidance";
		TraceMessage m3 = new TraceMessage(m3To, m3From, m3m);

		MessageData.getInstance().fireTraceMessage(m3);

		AgentPair m4To = new AgentPair("Jill", AgentType.LOCAL_CONTROL, new Semaphore(1,true));
		AgentPair m4From = new AgentPair("Bob", AgentType.GROUND_CONTROL, new Semaphore(1,true));
		String m4m = "Thanks Sweetheart";
		TraceMessage m4 = new TraceMessage(m4To, m4From, m4m);

		MessageData.getInstance().fireTraceMessage(m4);

		AgentPair m5To = new AgentPair("Bob", AgentType.GROUND_CONTROL, new Semaphore(1,true));
		AgentPair m5From = new AgentPair("Jill", AgentType.LOCAL_CONTROL, new Semaphore(1,true));
		String m5m = "F#ck Off";
		TraceMessage m5 = new TraceMessage(m5To, m5From, m5m);

		MessageData.getInstance().fireTraceMessage(m5);

		AgentPair m7To = new AgentPair("Jill", AgentType.LOCAL_CONTROL, new Semaphore(1,true));
		AgentPair m7From = new AgentPair("Bob", AgentType.GROUND_CONTROL, new Semaphore(1,true));
		String m7m = "Boy am I drunk";
		TraceMessage m7 = new TraceMessage(m7To, m7From, m7m);

		MessageData.getInstance().fireTraceMessage(m7);

		AgentPair m6To = null;
		AgentPair m6From = new AgentPair("SWA321", AgentType.PILOT, new Semaphore(1,true));
		String m6m = "This is SWA321 on visual approach (Broadcast example)";
		TraceMessage m6 = new TraceMessage(m6To, m6From, m6m);

		MessageData.getInstance().fireTraceMessage(m6);

		test.update();

		//setLayout(new GridLayout(1,1));
		add(test);
		setVisible(true);
	}
	public static void main(String[] args) {

		TraceTest testing = new TraceTest();

	}

}
