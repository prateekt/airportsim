package faa;

import gui.ClearanceDeliveryPanel;
import gui.GUI;
import gui.GroundControlPanel;
import gui.LocalControlPanel;
import interfaces.ClearanceDelivery;
import interfaces.GroundControl;
import interfaces.LocalControl;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import speech.FreeTTSSpeakerEngine;
import speech.SpeakerEngine;
import speech.SpeechRecognitionEngine;
import speech.SphinxSpeechRecognitionEngine;
import agent.Agent;
import agents.Flight;
import agents.LoggingAgent;
import agents.LoginAgent;
import agents.PilotAgent;
import agents.ScenarioParser;
import agents.UserClearanceAgent;
import agents.UserGroundControlAgent;
import agents.UserLocalControlAgent;
import airport.TheSimulator;
import airport.objects.Airport;

public class FAAControl {
	static GUI gui = null;

	/*
	 * Instance of SpeakerEngine
	 */
	private static SpeakerEngine seInstance;

	/*
	 * Instance of Speech recognition engine
	 */
	private static SpeechRecognitionEngine srInstance;

	/*
	 * Define SpeakerEngine as singleton for the FAA simulation.
	 */
	public static SpeakerEngine getSpeakerEngine() {
		if(seInstance==null) {
			 seInstance = new FreeTTSSpeakerEngine();
		}
		return seInstance;
	}

	/*
	 * Define SpeechRecognitionEngine as singleton for the FAA simulation.
	 */
	public static SpeechRecognitionEngine getSpeechRecognitionEngine() {
		if(srInstance==null) {
			 srInstance = new SphinxSpeechRecognitionEngine("blah",gui);
		}
		return srInstance;
	}


	public FAAControl() {
		//start the auxilary agents
		LoggingAgent.getInstance().startThread();
		LoginAgent.getInstance().setFAAControl(this);
		LoginAgent.getInstance().startThread();
		getSpeakerEngine().setVoiceOn(true);
		getSpeakerEngine().startThread();

		gui = new GUI();

		TheSimulator.getInstance().setGUI(gui);
		TheSimulator.getInstance().start();

		boolean speechRecognitionEnabled = true;

		if (speechRecognitionEnabled) {
			new Thread(getSpeechRecognitionEngine()).start();
		}

	}


	public void run() {


	}

	public static void main(String[] args) {

		FAAControl control = new FAAControl();
		control.run();
	}

	public void startControl(String username,String position,String scenario) {

		List<PilotAgent> pilots = null;
		LocalControl la = null;
		GroundControl gc = null;
		ClearanceDelivery ca = null;
		Airport airport = null;

		TheSimulator.getInstance().setRunning(true,false);

		//first, load the scenario
		try {
			ScenarioParser p = new ScenarioParser();
			p.load("resource/scenarios/" + scenario.toLowerCase() + ".xml");

			pilots = p.getPilots();
			la = p.getLocalControlAgent();
			gc = p.getGroundControlAgent();
			ca = p.getClearanceAgent();
			airport = p.getAirport();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		gui.setAirport(airport);
		gui.goTo(position);

		//depending on the position, we will forgo some agents
		if ("groundcontrol".equals(position)) {
			gc = new UserGroundControlAgent(username,airport,(GroundControlPanel)gui.getCurrentPanel());
			gui.setTitle("FAA Control Software - Welcome, Ground Controller " + username);
		} else if ("clearancedelivery".equals(position)) {
			ca = new UserClearanceAgent(username,airport,(ClearanceDeliveryPanel)gui.getCurrentPanel());
			gui.setTitle("FAA Control Software - Welcome, Clearance Delivery Controller " + username);
		} else if ("localcontrol".equals(position)) {
			la = new UserLocalControlAgent(username,airport,(LocalControlPanel)gui.getCurrentPanel());
			gui.setTitle("FAA Control Software - Welcome, Local Controller " + username);
		}

		Timer timer = new Timer();

		for(final PilotAgent pilot: pilots) {
			pilot.setMyCD(ca);
			pilot.setMyLC(la);
			pilot.setMyGC(gc);
			pilot.startThread();

			final Flight flight = pilot.getFlight();
			if (flight.getType() == Flight.FlightType.Departure) {
				pilot.placePlane();
			}

			timer.schedule(new TimerTask() {
				public void run() {
					if (flight.getType() == Flight.FlightType.Departure) {
						pilot.msgIWantToTakeOff(flight.getGate(),flight.getOrigin());
					} else {
						pilot.msgISeeARunway(flight.getWay());
					}
				}
			},flight.getTime());
		}

		((Agent)ca).startThread();
		((Agent)la).startThread();
		((Agent)gc).startThread();
	}
}
