package gui;
/*
	Writen By: Eric Quillen
*/

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import agents.LoginAgent;

/** Login JPanel.
*	@author Eric Quillen
* modified by John Baldo
*/
public class LoginPanel extends JPanel{

	// Begin Variable Declarations

	private LoginPanel self = this;
	private GUI gui;

	private JPanel loginPanel;					// Login Panel - used to login to the system.
	private SpringLayout layout;				// Layout for loginPanel.

	private JLabel heading;						// Heading for Panel.
	private JLabel userNameLabel;				// Prompt for user id.
	private JLabel passwordLabel;				// Prompt for user password.
	private JLabel positionLabel;				// Prompt for position.
	private JLabel scenarioLabel;
	private JLabel errorUserName;				// Error Message - User not Found.
	private JLabel errorPassword;				// Error Message - Password does not match.
	private JLabel errorPosition;				// Error Message - Not hired for position.

	private JTextField userNameTextField;		// Text field for user id.
	private JPasswordField passwordField;		// Text field for password.
	private JComboBox positionComboBox;			// Combo box to choose current position.
	private JComboBox scenarioComboBox;

	private JButton loginButton;				// Button to login to the System.

	private loginButtonAL loginAL;				// Action Listener for loginButton.

	private String[] towerCabPositions;			// Array of all Tower Cab positions.

	private String[] scenarios;

	// End Variable Declarations

	// Constructor
	/** Creates a JPanel for the Login Panel.
	*/
	public LoginPanel(GUI x)
	{
		gui = x;

		towerCabPositions = new String[] {"Ground Control", "Local Control", "Clearance Delivery",
			"Supervisor",};

		scenarios = new String[] {"LAX"};

		loginPanel = new JPanel();
		setPreferredSize(new Dimension(1024, 768));
		layout = new SpringLayout();
		setLayout(layout);

		heading = new JLabel("<html><font size = 20>Control Tower Login</font></html>");
		userNameLabel = new JLabel("User Name: ");
		passwordLabel = new JLabel("Password: ");
		positionLabel = new JLabel("Choose Position: ");
		scenarioLabel = new JLabel("Choose Scenario: ");
		errorUserName = new JLabel("<html><font color = red>Invalid User Name of Password</font></html>");
		errorUserName.setVisible(false);
		errorPassword = new JLabel("<html><font color = red>Invalid Password</font></html>");
		errorPassword.setVisible(false);
		errorPosition = new JLabel("<html><font color = red>Invalid Position</font></html>");
		errorPosition.setVisible(false);

		userNameTextField = new JTextField(20);
		userNameTextField.setText("hyuen");
		passwordField = new JPasswordField(20);
		passwordField.setText("password");
		positionComboBox = new JComboBox(towerCabPositions);
		positionComboBox.setSelectedIndex(1);
		scenarioComboBox = new JComboBox(scenarios);
		scenarioComboBox.setSelectedIndex(0);

		loginButton = new JButton("Login");
		loginAL = new loginButtonAL();
		loginButton.addActionListener(loginAL);

		add(heading);
		add(userNameLabel);
		add(userNameTextField);
		add(passwordLabel);
		add(passwordField);
		add(positionLabel);
		add(positionComboBox);
		add(scenarioLabel);
		add(scenarioComboBox);
		add(loginButton);
		add(errorUserName);
		add(errorPassword);
		add(errorPosition);

		layoutComponents();

		setVisible(true);
	}

	// Sends a message to the LoginAgent to login in.
	class loginButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			LoginAgent.getInstance().msgWantToLogin(self, userNameTextField.getText().trim(), passwordField.getPassword(),
				positionComboBox.getSelectedItem().toString().trim().toLowerCase().replace(" ", ""),
				scenarioComboBox.getSelectedItem().toString());
		}
	}

	/** Display an error if login fails or logs the user in.
	*	@param x code of the login result.
	*/
	public void msgLoginResult(int x)
	{
		resetError();
			switch (x)
			{
				case 1:
						gui.goTo(positionComboBox.getSelectedItem().toString().trim());
						resetFields();
						break;

				case 2: errorUserName.setVisible(true);
						break;

				case 3: errorPassword.setVisible(true);
						break;

				case 4: errorPosition.setVisible(true);
						break;
			}
	}

	/** Resets all Error messages to invisible.
	*/
	public void resetError()
	{
		errorUserName.setVisible(false);
		errorPosition.setVisible(false);
		errorPassword.setVisible(false);
	}

	/** Resets all fields back to their original state.
	*/
	public void resetFields()
	{
		resetError();
		userNameTextField.setText("");
		passwordField.setText("");
		positionComboBox.setSelectedItem("");
	}

	/** Places Components using {@link SpringLayout}.
	*/
	public void layoutComponents()
	{

		// Place heading.
		layout.putConstraint(SpringLayout.EAST, heading,
                             675,
                             SpringLayout.WEST, loginPanel);
        layout.putConstraint(SpringLayout.NORTH, heading,
                             200,
                             SpringLayout.NORTH, loginPanel);

		// Place userNameLabel.
		layout.putConstraint(SpringLayout.EAST, userNameLabel,
                             400,
                             SpringLayout.WEST, loginPanel);
        layout.putConstraint(SpringLayout.NORTH, userNameLabel,
                             50,
                             SpringLayout.SOUTH, heading);

		// Place userNameTextField
		layout.putConstraint(SpringLayout.WEST, userNameTextField,
                             20,
                             SpringLayout.EAST, userNameLabel);
        layout.putConstraint(SpringLayout.NORTH, userNameTextField,
                             0,
                             SpringLayout.NORTH, userNameLabel);

		// Place passwordLabel.
		layout.putConstraint(SpringLayout.EAST, passwordLabel,
                             0,
                             SpringLayout.EAST, userNameLabel);
        layout.putConstraint(SpringLayout.NORTH, passwordLabel,
                             15,
                             SpringLayout.SOUTH, userNameLabel);

		// Place passwordField
		layout.putConstraint(SpringLayout.WEST, passwordField,
                             20,
                             SpringLayout.EAST, passwordLabel);
        layout.putConstraint(SpringLayout.NORTH, passwordField,
                             0,
                             SpringLayout.NORTH, passwordLabel);

		// Place positionLabel.
		layout.putConstraint(SpringLayout.EAST, positionLabel,
                             0,
                             SpringLayout.EAST, userNameLabel);
        layout.putConstraint(SpringLayout.NORTH, positionLabel,
                             15,
                             SpringLayout.SOUTH, passwordLabel);

		// Place positionComboBox.
		layout.putConstraint(SpringLayout.WEST, positionComboBox,
                             20,
                             SpringLayout.EAST, positionLabel);
        layout.putConstraint(SpringLayout.NORTH, positionComboBox,
                             0,
                             SpringLayout.NORTH, positionLabel);


		// Place positionLabel.
		layout.putConstraint(SpringLayout.EAST, scenarioLabel,
                             0,
                             SpringLayout.EAST, positionLabel);
        layout.putConstraint(SpringLayout.NORTH, scenarioLabel,
                             15,
                             SpringLayout.SOUTH, positionLabel);

        layout.putConstraint(SpringLayout.WEST, scenarioComboBox,
                20,
                SpringLayout.EAST, scenarioLabel);
        layout.putConstraint(SpringLayout.NORTH, scenarioComboBox,
                0,
                SpringLayout.NORTH, scenarioLabel);




		// Place loginButton.
		layout.putConstraint(SpringLayout.WEST, loginButton,
                             20,
                             SpringLayout.EAST, scenarioComboBox);
        layout.putConstraint(SpringLayout.NORTH, loginButton,
                             0,
                             SpringLayout.NORTH, scenarioComboBox);

		// Place errorUserName.
		layout.putConstraint(SpringLayout.WEST, errorUserName,
                             30,
                             SpringLayout.WEST, userNameTextField);
        layout.putConstraint(SpringLayout.SOUTH, errorUserName,
                             -15,
                             SpringLayout.NORTH, userNameTextField);

		// Place errorPassword.
		layout.putConstraint(SpringLayout.WEST, errorPassword,
                             30,
                             SpringLayout.WEST, userNameTextField);
        layout.putConstraint(SpringLayout.SOUTH, errorPassword,
                             -15,
                             SpringLayout.NORTH, userNameTextField);

		// Place errorPosition.
		layout.putConstraint(SpringLayout.WEST, errorPosition,
                             30,
                             SpringLayout.WEST, userNameTextField);
        layout.putConstraint(SpringLayout.SOUTH, errorPosition,
                             -15,
                             SpringLayout.NORTH, userNameTextField);
	}
}
