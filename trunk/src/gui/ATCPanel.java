package gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

import agents.*;
import java.util.*;

import airport.objects.*;
import interfaces.*;

/**
 *
 * @author Henry Yuen
 *
 */
public abstract class ATCPanel extends CommandPanel {

	FlightStatusPanel flightStatusPanel;
	JPanel buttonPanel;
	WaypointMakerPanel waypointMaker;
	TabbedConfirmPanel tabbedConfirmPanel;

	public ATCPanel(GUI gui) {
		super(gui);
	}

	protected JPanel initCommandPanel() {

		JPanel commandPanel = new JPanel();
		commandPanel.setLayout(new BoxLayout(commandPanel,BoxLayout.X_AXIS));

		//init status pane
		//insert the flight status panel
		flightStatusPanel = new FlightStatusPanel();
		commandPanel.add(flightStatusPanel);

		//now we add all the command buttons
		buttonPanel = initButtonPanel();

		buttonPanel.setMinimumSize(new Dimension(150,-1));
		//buttonPanel.setMaximumSize(new Dimension(200,500));

		commandPanel.add(buttonPanel);

		return commandPanel;
	}

	public void newPilotMessage(final Pilot pilot,String message,final Mediator mediator) {
		ConfirmPanel panel = new ConfirmPanel();
		panel.setText(message);
		panel.setFrom("Pilot " + pilot.getName());
		panel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//once the ATC user confirms it, it should go into the flight
				//status panel
				flightStatusPanel.addFlight(pilot,mediator);
			}
		});

		tabbedConfirmPanel.addTab(panel);
	}

	public void removeFlight(Pilot pilot) {
		flightStatusPanel.removeFlight(pilot);
	}

	protected JPanel initButtonPanel() {
		return new JPanel();
	}

	protected JPanel initUtilityPanel() {
		JPanel utilityPanel = new JPanel();
		utilityPanel.setLayout(new GridLayout(2,1));

		tabbedConfirmPanel = new TabbedConfirmPanel();

		utilityPanel.add(tabbedConfirmPanel);

		waypointMaker = new WaypointMakerPanel(this,gui.getAirport());
		utilityPanel.add(waypointMaker);

		airportViewPanel.setWaypointMaker(waypointMaker);

		utilityPanel.setMinimumSize(new Dimension(300,-1));

		return utilityPanel;
	}

	protected void disableButtonPanel() {

	}

	protected void enableButtonPanel() {

	}

	public void waypointMakerFinished() {
		//destroy the waypoint maker
		waypointMaker.deactivate();

		airportViewPanel.setNormalMode();
		enableButtonPanel();

		repaint();
	}

	public void submitWaypoints(Pilot pilot,ArrayList<AirplaneAction> actions,ArrayList<String> commands) {

	}

	protected void launchWaypointMaker(Pilot pilot) {

		//set our mode into waypoint maker
		disableButtonPanel();

		waypointMaker.activate(pilot);
		waypointMaker.repaint();

		airportViewPanel.setWaypointMode();
		//let it go!

		repaint();

	}

	public void update() {
		repaint();
	}

	public void raiseErrorMessage(String message) {
		JOptionPane.showMessageDialog(this,message);
	}

	/**
	 * @return the flightStatusPanel
	 */
	public FlightStatusPanel getFlightStatusPanel() {
		return flightStatusPanel;
	}
}
