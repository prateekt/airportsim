package voicerecognition1;

import interfaces.SpeechRecognitionEngine;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import gui.*;
import interfaces.*;
import javax.swing.*;
import java.awt.event.*;

public class SphinxSpeechRecognitionEngine extends SpeechRecognitionEngine {

	private Recognizer recognizer;
	private Microphone microphone;
	private GUI gui;
	
	public SphinxSpeechRecognitionEngine(String configFile, GUI gui) {
		this.gui = gui;
		try {
			File f = new File("src/voicerecognition1/helloworld.config.xml");
	    	URL url = f.toURI().toURL();
	        ConfigurationManager cm = new ConfigurationManager(url);
	        recognizer = (Recognizer) cm.lookup("recognizer");
	        microphone = (Microphone) cm.lookup("microphone");
        } catch (IOException e) {
            System.err.println("Problem when loading HelloWorld: " + e);
            e.printStackTrace();
        } catch (PropertyException e) {
            System.err.println("Problem configuring HelloWorld: " + e);
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.err.println("Problem creating HelloWorld: " + e);
            e.printStackTrace();
        }
	}
	
	public void run() {
		try {
	        recognizer.allocate();
	        if (microphone.startRecording()) {
	        	while(true) {
		        	Result result = recognizer.recognize();
		            if (result != null) {
		            	System.out.println("Say something.");
		            	String resultText = result.getBestFinalResultNoFiller();
		            	System.out.println("You said: " + resultText + "\n");
		            	forwardToUserControlledAgent(resultText);
		            }
	        	}
	        }
		}
		catch(IOException e) {
            System.err.println("Problem when loading HelloWorld: " + e);
            e.printStackTrace();			
		}
		
	}
	
	public void forwardToUserControlledAgent(String msg) {
		if(gui!=null && msg!=null && !msg.trim().equals("")) {
			//user logged in as local control
			if(gui.getCurrentPanel() instanceof LocalControlPanel)
				forwardToLocalControl(msg);
			//user logged in as ground control
			else if(gui.getCurrentPanel() instanceof  GroundControlPanel)
				forwardToGroundControl(msg);
			else if(gui.getCurrentPanel() instanceof ClearanceDeliveryPanel)
				forwardToClearanceDelivery(msg);
		}
	}
	
	public void forwardToGroundControl(String msg) {
		GroundControlPanel gp = (GroundControlPanel) gui.getCurrentPanel();
		FlightStatusPanel fp = gp.getFlightStatusPanel();
		FlightStatusPanel.FlightTableModel fm = fp.getFlightTableModel();
		JTable ft = fp.getFlightTable();

		for(int x=0; x < fm.getRowCount(); x++) {
			Pilot p = fm.getPilot(x);
			if(msg.toLowerCase().indexOf(p.getName().toLowerCase()) > -1) {				
				//select the row in the table
				ft.setRowSelectionInterval(x, x);
				ft.repaint();
				
				if(msg.indexOf("push back granted") > -1) {
					gp.actionPerformed(new ActionEvent(gp.getGrantPushbackButton(), 0, "blah"));
				}
			}
		}		
	}
	
	public void forwardToClearanceDelivery(String msg) {
			ClearanceDeliveryPanel cp = (ClearanceDeliveryPanel) gui.getCurrentPanel();
			FlightStatusPanel fp = cp.getFlightStatusPanel();
			FlightStatusPanel.FlightTableModel fm = fp.getFlightTableModel();
			JTable ft = fp.getFlightTable();

			for(int x=0; x < fm.getRowCount(); x++) {
				Pilot p = fm.getPilot(x);
				if(msg.toLowerCase().indexOf(p.getName().toLowerCase()) > -1) {				
					//select the row in the table
					ft.setRowSelectionInterval(x, x);
					ft.repaint();
					
					if(msg.indexOf("clearance granted") > -1) {
						cp.actionPerformed(new ActionEvent(cp.getGrantClearanceButton(), 0, "blah"));
					}
				}
			}		
	}
	
	public void forwardToLocalControl(String msg) {
		LocalControlPanel lp = (LocalControlPanel) gui.getCurrentPanel();
		FlightStatusPanel fp = lp.getFlightStatusPanel();
		FlightStatusPanel.FlightTableModel fm = fp.getFlightTableModel();
		JTable ft = fp.getFlightTable();
	
		for(int x=0; x < fm.getRowCount(); x++) {
			Pilot p = fm.getPilot(x);
			if(msg.toLowerCase().indexOf(p.getName().toLowerCase()) > -1) {				
				//select the row in the table
				ft.setRowSelectionInterval(x, x);
				ft.repaint();
				
				if(msg.indexOf("take off") > -1) {
					lp.actionPerformed(new ActionEvent(lp.getClearForTakeoffButton(), 0, "blah"));
				}
				else if(msg.indexOf("land") > -1) {
					lp.actionPerformed(new ActionEvent(lp.getClearForLandingButton(), 0, "blah"));
				}
				else if(msg.indexOf("position and hold") > -1) {
					lp.actionPerformed(new ActionEvent(lp.getPositionAndHoldButton(), 0, "blah"));
				}
			}
		}
	}
}
