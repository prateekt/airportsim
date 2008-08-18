package gui;

import interfaces.Pilot;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import agents.ATCAgent;
import airport.objects.Airplane;
import airport.objects.AirplaneAction;
import airport.objects.Airport;
import airport.objects.AirportNode;

/**
 *
 * @author Julian
 *
 */
public class WaypointMakerPanel extends JPanel implements ActionListener {

	Airport airport;
	Pilot pilot;

	JPanel instructionsPanel;

	JList waypointList;

	JButton deleteWaypointButton;
	JButton submitButton;
	JButton cancelButton;

	ATCPanel atcPanel;

	ArrayList<AirportNode> waypoints;
	ArrayList<String> commands;
	ArrayList<AirplaneAction> actions;
	AirportNode selectedWaypoint = null;

	public WaypointMakerPanel(ATCPanel panel,Airport airport) {

		atcPanel = panel;
		this.airport = airport;

		BorderLayout bl=new BorderLayout();
		bl.setVgap(10);
		setLayout(bl);

		instructionsPanel = new JPanel();
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel,BoxLayout.Y_AXIS));
		instructionsPanel.add(new JLabel("<html><b>Set Waypoints for Plane</b></html>"));
		instructionsPanel.add(new JLabel("Click on the waypoints in order to add them to the list"));

		add(instructionsPanel,BorderLayout.NORTH);

		waypointList = new JList();
		waypointList.setModel(new DefaultListModel());

		ListSelectionModel lsm = waypointList.getSelectionModel();
		lsm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lsm.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
		        boolean isAdjusting = evt.getValueIsAdjusting();

		        if (!lsm.isSelectionEmpty() && !isAdjusting) {
		            // Find out which indexes are selected.
		        	int index = lsm.getMinSelectionIndex();
		        	if (index >= 0 && index < waypoints.size()) {
		        		selectedWaypoint = waypoints.get(index);
		        	} else {
		        		selectedWaypoint = null;
		        	}
		        }
			}
		});



		JScrollPane scrollPane = new JScrollPane(waypointList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		add(scrollPane,BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		deleteWaypointButton = new JButton("Delete Waypoint");
		deleteWaypointButton.addActionListener(this);
		submitButton = new JButton("Submit");
		submitButton.addActionListener(this);

		buttonPanel.add(cancelButton);
		buttonPanel.add(deleteWaypointButton);
		buttonPanel.add(submitButton);

		add(buttonPanel,BorderLayout.SOUTH);

		deactivate();

	}

	public void addWaypoint(AirportNode node) {
		waypoints.add(node);

		updateList();
	}

	public void updateList() {
		//clear the jlist
		//and recompute all the directions
		Airplane airplane = pilot.getAirplane();

		String wayName = "";

		if (airplane.getWay() == null) {
			wayName = "";
		} else {
			wayName = airplane.getWay().getName();
		}

		commands = new ArrayList<String>();
		actions = ATCAgent.convertPathToAirplaneActions(airport,wayName,waypoints,commands);

		DefaultListModel listModel = new DefaultListModel();

		listModel.clear();
		for (String cmd : commands) {
			listModel.addElement(cmd);
		}

		waypointList.setModel(listModel);
		repaint();
	}

	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();

		if (source == cancelButton) {
			//nothing happens
			atcPanel.waypointMakerFinished();
			return;
		} else if (source == deleteWaypointButton) {
			//is there anything selected?
			if (selectedWaypoint == null) {
				JOptionPane.showMessageDialog(this,"Must selected a waypoint to delete!");
				return;
			} else {
				waypoints.remove(selectedWaypoint);

				//we must notify the airportviewpanel
				atcPanel.getAirportViewPanel().removeWaypoint(selectedWaypoint);

				selectedWaypoint = null;
				updateList();
			}

		} else if (source == submitButton) {

			//oh boy, we submit our instructions back to our atcpanel,
			//and then we quit
			atcPanel.submitWaypoints(pilot,actions,commands);
			atcPanel.waypointMakerFinished();
			return;
		}
	}

	public void activate(Pilot pilot) {
		this.pilot = pilot;
		selectedWaypoint = null;

		actions = null;
		commands = new ArrayList<String>();
		waypoints = new ArrayList<AirportNode>();

		instructionsPanel.setVisible(true);
		waypointList.setEnabled(true);
		deleteWaypointButton.setEnabled(true);
		submitButton.setEnabled(true);
		cancelButton.setEnabled(true);
	}

	public void deactivate() {
		instructionsPanel.setVisible(false);
		waypointList.setEnabled(false);
		deleteWaypointButton.setEnabled(false);
		submitButton.setEnabled(false);
		cancelButton.setEnabled(false);

		//clear the jlist
		DefaultListModel model = (DefaultListModel)waypointList.getModel();
		model.clear();

		waypointList.repaint();
	}

}
