package gui;
/*
	Writen By: Eric Quillen
*/

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import agents.LoginAgent;
import airport.Personnel;

/** Employee Registration JPanel.
*	@author Eric Quillen
*/
public class RegistrationPanel extends JPanel implements ListSelectionListener{

	// Begin Variable Declarations

	private GUI gui;						// Link to main gui class.

	private Personnel employee;			// Link to Personnel Class.

	private JPanel registrationPanel;		// Registration Panel - Supervisor can add or edit Personnel info.
	private SpringLayout layout;			// Layout for registrationPanel.

	private JScrollPane scrollPane;			// Holds list of employees.

	private JList employeeList;				// List of employees.

	private JLabel firstNameLabel;			// Prompt for employee's first name.
	private JLabel lastNameLabel;			// Prompt for employee's last name.
	private JLabel userIDLabel;				// Prompt for employee's userID.
	private JLabel positionsLabel;			// Prompt for employee's hired positions.
	private JLabel heading;					// Heading for Panel.
	private JLabel listHeading;				// Heading for JList.
	private JLabel error;					// Error -- all fields are not filled.

	private JTextField firstNameTextField;	// Text Field for employee's first name.
	private JTextField lastNameTextField;	// Text Field for employee's last name.
	private JTextField userIDTextField;		// Text Field for employee's userID.

	private JCheckBox fdcd;					// Check Box for position Flight Data / Clearance Delivery.
	private JCheckBox groundControl;		// Check Box for position Ground Control.
	private JCheckBox localControl;			// Check Box for position Local Control.
	private JCheckBox helicopterControl;	// Check Box for position Helicopter Control.
	private JCheckBox trafficManager;		// Check Box for position Traffic Manager.
	private JCheckBox supervisor;			// Check Box for position Supervisor.

	private JButton submitButton;			// Button to submit info.
	private JButton clearFormButton;		// Button to clear all fields.
	private JButton clearFormButton2;
	private JButton editButton;				// Button to edit employee.
	private JButton deleteButton;			// Button to delete employees.

	private SubmitButtonAL submitAL;		// Action Listener for submitButton.
	private ClearFormButtonAL clearFormAL;	// Action Listener for clearFormButton.
	private EditButtonAL editAL;			// Action Listener for editButton.
	private DeleteButtonAL deleteAL;		// Action Listener for deleteButton.

	// End Variable Declarations
	/** Creates the Reguistration Panel
	*	@param x link to the HW3GUI.
	*/
	public RegistrationPanel(GUI x)
	{
		gui = x;

		registrationPanel = new JPanel();
		setPreferredSize(new Dimension(1000, 650));
		layout = new SpringLayout();
		setLayout(layout);

		employeeList = new JList();
		employeeList.addListSelectionListener(this);

		scrollPane = new JScrollPane(employeeList);
		scrollPane.setPreferredSize(new Dimension(150, 640));

		firstNameLabel = new JLabel("First Name: ");
		lastNameLabel = new JLabel("Last Name: ");
		userIDLabel = new JLabel("User Name: ");
		positionsLabel = new JLabel("Hired Positions: ");
		heading	= new JLabel("<html><font size = 20>Employee Management</font></html>");
		listHeading = new JLabel("<html><bold>Employee List</bold></html>");
		error = new JLabel("<html><font color = red>Fill all fields and choose at least one position</font><html>");

		firstNameTextField = new JTextField(20);
		userIDTextField = new JTextField(20);
		lastNameTextField = new JTextField(20);

		fdcd = new JCheckBox("Flight Data / Clearance Delivery");
		groundControl = new JCheckBox("Ground Control");
		localControl = new JCheckBox("Local Control");
		helicopterControl = new JCheckBox("Helicopter Control");
		trafficManager = new JCheckBox("Traffic Manager");
		supervisor = new JCheckBox("Supervisor");

		submitButton = new JButton("Submit");
		clearFormButton = new JButton("Clear Form");
		clearFormButton2 = new JButton("Clear Form");
		editButton = new JButton("Submit Edit");
		deleteButton = new JButton("Delete Employee");

		submitAL = new SubmitButtonAL();
		clearFormAL = new ClearFormButtonAL();
		editAL = new EditButtonAL();
		deleteAL = new DeleteButtonAL();

		submitButton.addActionListener(submitAL);
		clearFormButton.addActionListener(clearFormAL);
		clearFormButton2.addActionListener(clearFormAL);
		editButton.addActionListener(editAL);
		deleteButton.addActionListener(deleteAL);

		add(listHeading);
		add(heading);
		add(firstNameLabel);
		add(firstNameTextField);
		add(userIDLabel);
		add(userIDTextField);
		add(lastNameLabel);
		add(lastNameTextField);
		add(positionsLabel);
		add(fdcd);
		add(groundControl);
		add(localControl);
		add(helicopterControl);
		add(trafficManager);
		add(supervisor);
		add(submitButton);
		add(clearFormButton);
		add(clearFormButton2);
		add(deleteButton);
		add(editButton);
		add(scrollPane);
		add(error);

		placeComponents();

		editButton.setVisible(false);
		deleteButton.setVisible(false);
		error.setVisible(false);
		clearFormButton2.setVisible(false);
		validate();
	}

	// Submits new employee info to the database.
	class SubmitButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			ArrayList<String> pos = getPositions();
			if(firstNameTextField.getText().equals("") || lastNameTextField.getText().equals("") ||
				userIDTextField.getText().equals("") || (pos.size() == 0))
			{
				error.setVisible(true);
			}
			else
			{
				submitEmployee();
			}

		}
	}

	/** Creates a HW3Personnel object and passes it to the database. Then refreshes the employee list.
	*/
	public void submitEmployee()
	{
		ArrayList<String> pos = getPositions();
		char[] defaultPassword = "password".toCharArray();

		String email = (userIDTextField.getText() + "@USCcontrol.com");


		employee = new Personnel(firstNameTextField.getText(),
									lastNameTextField.getText(),
									email,
									pos,
									userIDTextField.getText(),
									defaultPassword);
		LoginAgent.getInstance().msgWantToRegister(employee);
		refreshList();
		reset();
	}

	// Resets the registration form.
	class ClearFormButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			reset();
		}
	}

	// Submits and edit to a current employee.
	class EditButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			ArrayList<String> pos = getPositions();
			if(firstNameTextField.getText().equals("") || lastNameTextField.getText().equals("") ||
				userIDTextField.getText().equals("") || (pos.size() == 0))
			{
				error.setVisible(true);
			}
			else
			{
				employee.setFirstName(firstNameTextField.getText());
				employee.setLastName(lastNameTextField.getText());
				employee.setUserID(userIDTextField.getText());
				employee.setPositions(getPositions());
				LoginAgent.getInstance().msgWantToDelete(employee.getUserID());
				LoginAgent.getInstance().msgWantToRegister(employee);
				refreshList();
				reset();
			}
		}
	}

	// Deletes currently selected employee.
	class DeleteButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("Delete user button pressed.");
			LoginAgent.getInstance().msgWantToDelete(gui.getUsername(getValue()));
			System.out.println("User deleted.");
			refreshList();
			System.out.println("List refreshed.");
			reset();
		}
	}

	/** Refreshes the list of employees in the registration panel.
	*/
	public void refreshList(){
		System.out.println("Requesting list refresh.");
		LoginAgent.getInstance().msgWantUserList(this);
	}

	/** Called by the LoginAgent to refresh the employee list.
	*	@param map A map of of HW3Personnel.
	*/
	public void printList(Map<String, Personnel> map)
	{
		System.out.println("printList command recieved.");
		gui.setMap(map);
		employeeList.setListData(map.keySet().toArray());
		scrollPane.revalidate();
	}

	/** Gets the value of the currently selected employee.
	*	@return The name of the employee.
	*/
	public String getValue()
	{
		if (employeeList.getSelectedValue() == null)
		{
			return ("");
		}
		else
		{
			return employeeList.getSelectedValue().toString().trim();
		}
	}

	// List listener
	/** List listener and when the value changes all fields are filled with the selected employee's info.
	*/
	public void valueChanged(ListSelectionEvent e)
	{
			if (e.getValueIsAdjusting() == true)
			{
				reset();
				employee = gui.getEmployee(getValue());
				firstNameTextField.setText(employee.getFirstName());
				lastNameTextField.setText(employee.getLastName());
				userIDTextField.setText(employee.getUserID());
				userIDTextField.setEditable(false);
				ArrayList<String> positions = (ArrayList<String>)employee.getPositions().clone();
				if(positions.contains("flightdata/clearancedelivery"))
				{
					fdcd.setSelected(true);
				}
				if(positions.contains("groundcontrol"))
				{
					groundControl.setSelected(true);
				}
				if(positions.contains("localcontrol"))
				{
					localControl.setSelected(true);
				}
				if(positions.contains("helicoptercontrol"))
				{
					helicopterControl.setSelected(true);
				}
				if(positions.contains("trafficmanager"))
				{
					trafficManager.setSelected(true);
				}
				if(positions.contains("supervisor"))
				{
					supervisor.setSelected(true);
				}
				submitButton.setVisible(false);
				editButton.setVisible(true);
				deleteButton.setVisible(true);
				clearFormButton.setVisible(false);
				clearFormButton2.setVisible(true);
			}
	}

	/** Resets all fields back to their empty state.
	*/
	public void reset()
	{
		firstNameTextField.setText("");
		lastNameTextField.setText("");
		userIDTextField.setEditable(true);
		userIDTextField.setText("");
		fdcd.setSelected(false);
		groundControl.setSelected(false);
		localControl.setSelected(false);
		helicopterControl.setSelected(false);
		trafficManager.setSelected(false);
		supervisor.setSelected(false);
		submitButton.setVisible(true);
		editButton.setVisible(false);
		deleteButton.setVisible(false);
		error.setVisible(false);
		clearFormButton.setVisible(true);
		clearFormButton2.setVisible(false);
	}

	/** Gets a list of positions selected by the user.
	*	@return List of postions for the employee.
	*/
	public ArrayList<String> getPositions()
	{
		ArrayList<String> positions = new ArrayList<String>(7);

		if (fdcd.isSelected())
		{
			positions.add("flightdata/clearancedelivery");
		}

		if (groundControl.isSelected())
		{
			positions.add("groundcontrol");
		}

		if (localControl.isSelected())
		{
			positions.add("localcontrol");
		}

		if (helicopterControl.isSelected())
		{
			positions.add("helicoptercontrol");
		}

		if (trafficManager.isSelected())
		{
			positions.add("trafficmanager");
		}

		if (supervisor.isSelected())
		{
			positions.add("supervisor");
		}

		return positions;
	}

	/** Places Components using {@link SpringLayout}.
	*/
	public void placeComponents()
	{
		// Place scrollPane.
		layout.putConstraint(SpringLayout.WEST, scrollPane,
                             5,
                             SpringLayout.WEST, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, scrollPane,
                             20,
                             SpringLayout.NORTH, registrationPanel);

		// Place listHeading.
		layout.putConstraint(SpringLayout.WEST, listHeading,
                             30,
                             SpringLayout.WEST, scrollPane);
        layout.putConstraint(SpringLayout.SOUTH, listHeading,
                             0,
                             SpringLayout.NORTH, scrollPane);

		// Place heading.
		layout.putConstraint(SpringLayout.EAST, heading,
                             750,
                             SpringLayout.WEST, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, heading,
                             100,
                             SpringLayout.NORTH, registrationPanel);

		// Place error.
		layout.putConstraint(SpringLayout.EAST, error,
                             700,
                             SpringLayout.WEST, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, error,
                             30,
                             SpringLayout.SOUTH, heading);

		// Place firstNameLabel.
		layout.putConstraint(SpringLayout.EAST, firstNameLabel,
                             450,
                             SpringLayout.WEST, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, firstNameLabel,
                             50,
                             SpringLayout.SOUTH, heading);

		// Place firstNameTextField.
		layout.putConstraint(SpringLayout.WEST, firstNameTextField,
                             20,
                             SpringLayout.EAST, firstNameLabel);
        layout.putConstraint(SpringLayout.NORTH, firstNameTextField,
                             0,
                             SpringLayout.NORTH, firstNameLabel);

		// Place lastNameLabel.
		layout.putConstraint(SpringLayout.EAST, lastNameLabel,
                             0,
                             SpringLayout.EAST, firstNameLabel);
        layout.putConstraint(SpringLayout.NORTH, lastNameLabel,
                             15,
                             SpringLayout.SOUTH, firstNameLabel);

		// Place lastNameTextField.
		layout.putConstraint(SpringLayout.WEST, lastNameTextField,
                             20,
                             SpringLayout.EAST, lastNameLabel);
        layout.putConstraint(SpringLayout.NORTH, lastNameTextField,
                             0,
                             SpringLayout.NORTH, lastNameLabel);

		//Place userIDLabel.
		layout.putConstraint(SpringLayout.EAST, userIDLabel,
                             0,
                             SpringLayout.EAST, lastNameLabel);
        layout.putConstraint(SpringLayout.NORTH, userIDLabel,
                             15,
                             SpringLayout.SOUTH, lastNameLabel);

		// Place UserIDTextField.
		layout.putConstraint(SpringLayout.WEST, userIDTextField,
                             20,
                             SpringLayout.EAST, userIDLabel);
        layout.putConstraint(SpringLayout.NORTH, userIDTextField,
                             0,
                             SpringLayout.NORTH, userIDLabel);


		// Place positionsLabel.
		layout.putConstraint(SpringLayout.EAST, positionsLabel,
                             0,
                             SpringLayout.EAST, userIDLabel);
        layout.putConstraint(SpringLayout.NORTH, positionsLabel,
                             15,
                             SpringLayout.SOUTH, userIDLabel);

		// Place groundControl.
		layout.putConstraint(SpringLayout.WEST, groundControl,
                             20,
                             SpringLayout.WEST, positionsLabel);
        layout.putConstraint(SpringLayout.NORTH, groundControl,
                             5,
                             SpringLayout.SOUTH, positionsLabel);

		// Place localControl.
		layout.putConstraint(SpringLayout.WEST, localControl,
                             0,
                             SpringLayout.WEST, groundControl);
        layout.putConstraint(SpringLayout.NORTH, localControl,
                             5,
                             SpringLayout.SOUTH, groundControl);

		// Place helicopterControl.
		layout.putConstraint(SpringLayout.WEST, helicopterControl,
                             0,
                             SpringLayout.WEST, groundControl);
        layout.putConstraint(SpringLayout.NORTH, helicopterControl,
                             5,
                             SpringLayout.SOUTH, localControl);

		// Place trafficManager.
		layout.putConstraint(SpringLayout.WEST, trafficManager,
                             50,
                             SpringLayout.EAST, groundControl);
        layout.putConstraint(SpringLayout.NORTH, trafficManager,
                             0,
                             SpringLayout.NORTH, groundControl);

		// Place supervisor.
		layout.putConstraint(SpringLayout.WEST, supervisor,
                             50,
                             SpringLayout.EAST, groundControl);
        layout.putConstraint(SpringLayout.NORTH, supervisor,
                             0,
                             SpringLayout.NORTH, localControl);

		// Place fdcd.
		layout.putConstraint(SpringLayout.WEST, fdcd,
                             50,
                             SpringLayout.EAST, groundControl);
        layout.putConstraint(SpringLayout.NORTH, fdcd,
                             0,
                             SpringLayout.NORTH, helicopterControl);



		// Place submitButton.
		layout.putConstraint(SpringLayout.EAST, submitButton,
                             525,
                             SpringLayout.WEST, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, submitButton,
                             20,
                             SpringLayout.SOUTH, fdcd);

		// Place editButton.
		layout.putConstraint(SpringLayout.EAST, editButton,
                             625,
                             SpringLayout.WEST, registrationPanel);
        layout.putConstraint(SpringLayout.NORTH, editButton,
                             20,
                             SpringLayout.SOUTH, fdcd);

		// Place clearFormButton.
		layout.putConstraint(SpringLayout.WEST, clearFormButton,
                             20,
                             SpringLayout.EAST, submitButton);
        layout.putConstraint(SpringLayout.NORTH, clearFormButton,
                             20,
                             SpringLayout.SOUTH, fdcd);

		// Place clearFormButton2.
		layout.putConstraint(SpringLayout.WEST, clearFormButton2,
                             10,
                             SpringLayout.EAST, editButton);
        layout.putConstraint(SpringLayout.NORTH, clearFormButton2,
                             20,
                             SpringLayout.SOUTH, fdcd);

		// Place deleteButton.
		layout.putConstraint(SpringLayout.EAST, deleteButton,
                             -5,
                             SpringLayout.WEST, editButton);
        layout.putConstraint(SpringLayout.NORTH, deleteButton,
                             20,
                             SpringLayout.SOUTH, fdcd);

	}

}
