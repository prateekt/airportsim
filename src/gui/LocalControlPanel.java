/*package gui;


import javax.swing.*;
import java.awt.*;

/**
 *
 * @author Henry Yuen
 *
 */

/*
public class LocalControlPanel extends JPanel{

	private AirportViewPanel airportMap;
	private LCTracePanel tracePanel;

	public LocalControlPanel(GUI gui) {

		setLayout(new BorderLayout());
		tracePanel = new LCTracePanel();
		airportMap = new AirportViewPanel(gui);

		JScrollPane airportScrollPane = new JScrollPane(airportMap,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		airportMap.setScrollPane(airportScrollPane);

		//add(airportMap);
		add(airportScrollPane,BorderLayout.CENTER);
		add(tracePanel,BorderLayout.SOUTH);
	}

	public void updateMap() {
		airportMap.repaint();
		airportMap.updateUI();
	}

	public void updateTrace() {
		tracePanel.update();
	}
}
*/

package gui;

/*******************
 * This panel contains the map and trace for the ground controller
 *
 * @author Julian Vergel de Dios
 *******************/

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;

import agents.*;
import airport.objects.*;
import interfaces.*;

public class LocalControlPanel extends ATCPanel implements ActionListener {

	UserLocalControlAgent localControlAgent;

	JButton positionAndHoldButton;
	JButton clearForTakeoffButton;
	JButton clearForLandingButton;
	JButton useRouteButton;

	private enum DirectionType {
		Null,Arrival,Departure,
	}

	private DirectionType directionType;

	public LocalControlPanel(GUI gui) {
		super(gui);
		localControlAgent = null;
		directionType = DirectionType.Null;
	}

	public void setLocalControlAgent(UserLocalControlAgent agent) {
		localControlAgent = agent;
	}

	protected JPanel initButtonPanel() {
		//now we add all the command buttons
		clearForLandingButton = new JButton("Clear for Landing");
		useRouteButton = new JButton("Give Arrival Route to Taxiway");
		positionAndHoldButton = new JButton("Position and Hold");
		clearForTakeoffButton = new JButton("Clear for Takeoff");

		clearForLandingButton.addActionListener(this);
		useRouteButton.addActionListener(this);
		positionAndHoldButton.addActionListener(this);
		clearForTakeoffButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(4,1));
		buttonPanel.add(clearForLandingButton);
		buttonPanel.add(useRouteButton);
		buttonPanel.add(positionAndHoldButton);
		buttonPanel.add(clearForTakeoffButton);
		return buttonPanel;
	}

	protected void disableButtonPanel() {
		clearForLandingButton.setEnabled(false);
		useRouteButton.setEnabled(false);
		positionAndHoldButton.setEnabled(false);
		clearForTakeoffButton.setEnabled(false);
	}

	protected void enableButtonPanel() {
		clearForLandingButton.setEnabled(true);
		useRouteButton.setEnabled(true);
		positionAndHoldButton.setEnabled(true);
		clearForTakeoffButton.setEnabled(true);
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();

		//check that there is actually a flight selected
		Pilot pilot = flightStatusPanel.getSelectedPilot();
		if (pilot == null) {
			JOptionPane.showMessageDialog(null,"Must select a pilot to issue command to!");
			return;
		}

		if (source == clearForLandingButton) {
			clearForLanding(pilot);
			//launchWaypointMaker(pilot);
		} else if (source == useRouteButton) {
			launchWaypointMaker(pilot);
			//grantPushback(pilot);
		} else if (source == positionAndHoldButton) {
			positionAndHold(pilot);
		} else if (source == clearForTakeoffButton) {
			clearForTakeoff(pilot);
		}
	}

	/*
	private void grantPushback(Pilot pilot) {
		groundControlAgent.grantPushbackToPilot(pilot);

	}*/

	private void clearForLanding(Pilot pilot) {
		localControlAgent.clearPilotForLanding(pilot);
	}

	private void positionAndHold(Pilot pilot) {
		localControlAgent.positionAndHold(pilot);
	}

	private void clearForTakeoff(Pilot pilot) {
		localControlAgent.clearPilotForTakeoff(pilot);
	}

	//waypoint maker
	public void submitWaypoints(Pilot pilot,ArrayList<AirplaneAction> actions,ArrayList<String> commands) {

		//this should only be called by a WaypointMakerPanel
		localControlAgent.giveRouteToTaxiway(pilot,commands,actions);
	}

	/**
	 * @return the positionAndHoldButton
	 */
	public JButton getPositionAndHoldButton() {
		return positionAndHoldButton;
	}

	/**
	 * @return the clearForTakeoffButton
	 */
	public JButton getClearForTakeoffButton() {
		return clearForTakeoffButton;
	}

	/**
	 * @return the clearForLandingButton
	 */
	public JButton getClearForLandingButton() {
		return clearForLandingButton;
	}

	/**
	 * @return the useRouteButton
	 */
	public JButton getUseRouteButton() {
		return useRouteButton;
	}


}
