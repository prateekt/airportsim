package gui;

/*******************
 * This panel contains the map and trace for the ground controller
 *
 * @author John Baldo
 *******************/

import interfaces.Pilot;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import agents.UserClearanceAgent;

public class ClearanceDeliveryPanel extends ATCPanel implements ActionListener {

	UserClearanceAgent clearanceAgent;

	JButton grantClearanceButton;
	JButton giveModeButton;


	public ClearanceDeliveryPanel(GUI gui) {
		super(gui);
		clearanceAgent = null;
	}

	public void setClearanceAgent(UserClearanceAgent agent) {
		clearanceAgent = agent;
	}

	protected JPanel initButtonPanel() {
		//now we add all the command buttons
		grantClearanceButton = new JButton("Grant Clearance");
		//giveModeButton = new JButton("Give Mode");

		grantClearanceButton.addActionListener(this);
		//giveModeButton.addActionListener(this);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2,1));
		buttonPanel.add(grantClearanceButton);
		//buttonPanel.add(giveModeButton);

		return buttonPanel;
	}

	protected void disableButtonPanel() {
		grantClearanceButton.setEnabled(false);
		//giveModeButton.setEnabled(false);
	}

	protected void enableButtonPanel() {
		grantClearanceButton.setEnabled(true);
		//giveModeButton.setEnabled(true);
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();

		//check that there is actually a flight selected
		Pilot pilot = flightStatusPanel.getSelectedPilot();
		if (pilot == null) {
			JOptionPane.showMessageDialog(null,"Must select a pilot to issue command to!");
			return;
		}

		if (source == grantClearanceButton) {
			clearanceAgent.grantClearance(pilot);
		}
	}

	/**
	 * @return the grantClearanceButton
	 */
	public JButton getGrantClearanceButton() {
		return grantClearanceButton;
	}


}
