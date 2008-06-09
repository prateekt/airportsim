package gui;

/**
 * This panel is designed to display trace messages for Local Control users.
 * The user has the option of filtering content to show only Local Control Messages
 * or showing all  messages in the system.
 * The structure is nearly identical to GCTracePanel.java
 * author: John Baldo
 */

import guiIntegration.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class TracePanel extends JPanel {

	private JEditorPane traceBox;
	private JScrollPane sPane;
	private JPanel scrollPanel;
	private String myContent = "";
	private StringBuffer myMessages;

	public TracePanel(){

		scrollPanel = new JPanel();		//create new panel to hold scrollpane
		scrollPanel.setLayout(new GridLayout(1,1)); //set layout to grid 1,1

		//SpringLayout layout = new SpringLayout(); //Create Spring Layout
		//setLayout(layout);			//Apply Spring Layout to overall frame

		//setLayout(new GridLayout(2,1));
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		traceBox = new JEditorPane();  			//Create Text Pane

		traceBox.setContentType("text/html");	//Allow html in strings
		traceBox.setEditable(false);	    	//do not allow user to edit
		traceBox.setPreferredSize(new Dimension(1000,200));

		String initialStream = MessageData.getInstance().getLCString(); //load starting trace stream
		traceBox.setText(initialStream);
		sPane = new JScrollPane(traceBox,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
				);      //Put TextField into a scrolling pane
		traceBox.setCaretPosition(traceBox.getDocument().getLength());


		scrollPanel.add(sPane);				    //wrap text area in scrollpane

		add(scrollPanel);
		setMaximumSize(new Dimension(1000,200));
		setPreferredSize(new Dimension(1000,200));
		setMinimumSize(new Dimension(1000,200));
		myMessages = new StringBuffer();

		TraceDB.getInstance().setTracePanel(this);
	}

	/*
	 * Method by which one adds a trace message to the gui.
	 *
	 * @param t The
	 */
	public void addMessage(TraceMessage t) {

		AgentPair to = t.getTo();
		AgentPair from = t.getFrom();
		String content = t.getMessage();

		String color =  "#000000";
		String type = "";

		if(to.getType()==AgentPair.AgentType.PILOT) {
			color = "#0000FF";
			type = "(Pilot)";
		}
		else if(to.getType()==AgentPair.AgentType.LOCAL_CONTROL) {
			color = "#006600";
			type = "(Local Control)";

		}
		else if(to.getType()==AgentPair.AgentType.GROUND_CONTROL)  {
			color = "#660099";
			type = "(Ground Control)";
		}
		else if(to.getType()==AgentPair.AgentType.CLEARANCE_DELIVERY) {
			color = "#669966";
			type = "(Clearance Delivery)";
		}


		String message = "<font size=\"3\" color=\"" + color + "\">" + to.getName() + type + ": " + content + "</font>";
		addMessageToComponent(message);
	}

	/*
	 * Helper method to generate html to add to text area.
	 */
	private void addMessageToComponent(String messageText) {
		String head = "<html><body>";
		String end = "</html></body>";
		myMessages.append(messageText + "<br><br>");
		myContent = head + myMessages.toString() + end;
	}

	public void update() {
		java.util.List<TraceMessage> messages = TraceDB.getInstance().getTraceMessages();
		myContent = "";
		myMessages =  new StringBuffer();
		synchronized (messages) {
			for(TraceMessage m: messages) {
				addMessage(m);
			}
		}
		traceBox.setText(myContent);
	}

	/*
	 * Unit Test for TracePanel
	 */
	public static void main(String[] args) {
		TracePanel t = new TracePanel();
		JFrame f = new JFrame("HI");
		f.setSize(500,500);
		f.getContentPane().add(t);
		f.setVisible(true);
	}
}
