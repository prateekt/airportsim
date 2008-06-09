package gui;

/*******************
 * This panel contains the map and trace for the ground controller
 *
 * @author John Baldo
 *******************/

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;

import agents.*;
import airport.objects.*;
import interfaces.*;

public class GroundControlPanel extends ATCPanel implements ActionListener {

	UserGroundControlAgent groundControlAgent;

	JButton arrivalUseRouteButton;
	JButton departureUseRouteButton;
	JButton grantPushbackButton;

	private enum DirectionType {
		Null,Arrival,Departure,
	}

	private DirectionType directionType;

	public GroundControlPanel(GUI gui) {
		super(gui);
		groundControlAgent = null;
		directionType = DirectionType.Null;
	}

	public void setGroundControlAgent(UserGroundControlAgent agent) {
		groundControlAgent = agent;
	}

	protected JPanel initButtonPanel() {
		//now we add all the command buttons
		arrivalUseRouteButton = new JButton("Give Arrival Route");
		departureUseRouteButton = new JButton("Give Departure Route");
		grantPushbackButton = new JButton("Grant Pushback");

		arrivalUseRouteButton.addActionListener(this);
		departureUseRouteButton.addActionListener(this);
		grantPushbackButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3,1));
		buttonPanel.add(arrivalUseRouteButton);
		buttonPanel.add(departureUseRouteButton);
		buttonPanel.add(grantPushbackButton);
		return buttonPanel;
	}

	protected void disableButtonPanel() {
		arrivalUseRouteButton.setEnabled(false);
		departureUseRouteButton.setEnabled(false);
		grantPushbackButton.setEnabled(false);
	}

	protected void enableButtonPanel() {
		arrivalUseRouteButton.setEnabled(true);
		departureUseRouteButton.setEnabled(true);
		grantPushbackButton.setEnabled(true);
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();

		//check that there is actually a flight selected
		Pilot pilot = flightStatusPanel.getSelectedPilot();
		if (pilot == null) {
			JOptionPane.showMessageDialog(null,"Must select a pilot to issue command to!");
			return;
		}

		if (source == arrivalUseRouteButton) {
			directionType = DirectionType.Arrival;
			launchWaypointMaker(pilot);
		} else if (source == grantPushbackButton) {
			grantPushback(pilot);
		} else if (source == departureUseRouteButton) {
			directionType = DirectionType.Departure;
			launchWaypointMaker(pilot);
		}
	}

	private void grantPushback(Pilot pilot) {
		groundControlAgent.grantPushbackToPilot(pilot);

	}

	//waypoint maker
	public void submitWaypoints(Pilot pilot,ArrayList<AirplaneAction> actions,ArrayList<String> commands) {

		//this should only be called by a WaypointMakerPanel

		//once we have our waypoints, let's submit it back to the
		//UserGroundControlAgent
		if (directionType == DirectionType.Arrival)
			groundControlAgent.giveArrivalRouteToPilot(pilot,commands,actions);
		else if (directionType == DirectionType.Departure)
			groundControlAgent.giveDepartureRouteToPilot(pilot,commands,actions);
	}

	/**
	 * @return the arrivalUseRouteButton
	 */
	public JButton getArrivalUseRouteButton() {
		return arrivalUseRouteButton;
	}

	/**
	 * @return the departureUseRouteButton
	 */
	public JButton getDepartureUseRouteButton() {
		return departureUseRouteButton;
	}

	/**
	 * @return the grantPushbackButton
	 */
	public JButton getGrantPushbackButton() {
		return grantPushbackButton;
	}


}
