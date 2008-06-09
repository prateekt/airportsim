package gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.table.*;
import java.util.*;

import agents.*;
import airport.objects.*;

import interfaces.*;

/**
 *
 * @author Julian
 *
 */
public class FlightStatusPanel extends JPanel {
	FlightTableModel flightTableModel;
	JTable flightTable;
	Pilot selectedPilot = null;

	public FlightStatusPanel() {
		selectedPilot = null;


		setLayout(new BorderLayout());

		flightTableModel = new FlightTableModel();
		flightTable = new JTable(flightTableModel);
		flightTable.setPreferredScrollableViewportSize(new Dimension(500,300));
		flightTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);


		ListSelectionModel selectionModel = flightTable.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				ListSelectionModel lsm = (ListSelectionModel)evt.getSource();
		        boolean isAdjusting = evt.getValueIsAdjusting();

		        if (!lsm.isSelectionEmpty() && !isAdjusting) {
		            // Find out which indexes are selected.
		        	int index = lsm.getMinSelectionIndex();
		        	Pilot pilot = flightTableModel.getPilot(index);
		        	if (pilot != null) {
		        		selectedPilot = pilot;
		        	}
		        }
			}
		});

		JScrollPane scrollPane = new JScrollPane(flightTable,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		add(scrollPane,BorderLayout.CENTER);
		setMaximumSize(new Dimension(700,500));

	}


    public void addFlight(Pilot pilot,Mediator mediator) {
    	flightTableModel.addFlight(pilot,mediator);
    	selectedPilot = flightTableModel.getPilot(flightTable.getSelectedRow());
    	flightTable.revalidate();
    	flightTable.repaint();
    	updateUI();
    }

    public void removeFlight(Pilot pilot) {
    	if (selectedPilot == pilot)
    		selectedPilot = null;

    	flightTableModel.removeFlight(pilot);
    	selectedPilot = flightTableModel.getPilot(flightTable.getSelectedRow());
    	flightTable.revalidate();
    	flightTable.repaint();
    	updateUI();
    }

	// All table information goes in here.
    public class FlightTableModel extends AbstractTableModel {
    	// Three different columns:  User's name, position, and online status
    	String[] columnNames = {"Pilot", "Flight", "Status", "Location"};

    	// Sample data.  Data entered is a two dimensional array of objects.
    	ArrayList<Pilot> pilots = null;
    	HashMap<Pilot,Mediator> statuses = null;

    	// Essential functions
    	public FlightTableModel() {
    		pilots = new ArrayList<Pilot>();
    		statuses = new HashMap<Pilot,Mediator>();
    	}
        public int getColumnCount() {
        	return columnNames.length;
        }

        public int getRowCount() {
        	return pilots.size();
        }

        public String getColumnName(int col) {
        	return columnNames[col];
        }

        // Returns the object at the indicated position
        public Object getValueAt(int row, int col) {
        	if (pilots.size() == 0) return "";
        	if (row >= pilots.size()) return "";

        	Pilot pilot = pilots.get(row);
        	if (pilot == null) return "";

        	switch (col) {
        	case 0:
        		//plane ID is the plane name
        		return pilot.getName();
        	case 1:
        		//get the flight
        		Flight flight = pilot.getFlight();
        		return flight.getPlaneName();
        	case 2:

        		Mediator mediator = statuses.get(pilot);
        		return mediator.getStatusString();

        	case 3:
        		Airplane airplane = pilot.getAirplane();

        		//get it's location based on its runway
        		Way way = airplane.getWay();
        		if (way == null) {
        			return "Off-runway";
        		} else {
        			return way.getName();
        		}

        	}

        	return "";
        }

        // Turns booleans into checkboxes.  Don't ask.  I don't know how it works myself.
        public Class getColumnClass(int c) {
            Object obj = getValueAt(0,c);
            if (obj == null) {
            	return "".getClass();
            }
            return obj.getClass();
        }


        // The table cannot be edited by the user, no matter what.
        public boolean isCellEditable(int row, int col) {
           	return false;
        }

        public void addFlight(Pilot pilot,Mediator mediator) {
        	pilots.add(pilot);
        	statuses.put(pilot,mediator);
        }

        public void clearData() {
        	pilots.clear();
        	statuses.clear();
        }

        public void removeFlight(Pilot pilot) {
        	pilots.remove(pilot);
        	statuses.remove(pilot);
        }

        public Pilot getPilot(int row) {
        	if (pilots.size() == 0) return null;
        	if (row < 0) return null;
        	if (row >= pilots.size()) return null;
        	return pilots.get(row);
        }
    }

    public Pilot getSelectedPilot() {
    	return selectedPilot;
    }


	/**
	 * @return the flightTableModel
	 */
	public FlightTableModel getFlightTableModel() {
		return flightTableModel;
	}


	/**
	 * @return the flightTable
	 */
	public JTable getFlightTable() {
		return flightTable;
	}
}
