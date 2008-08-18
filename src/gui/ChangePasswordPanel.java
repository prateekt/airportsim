package gui;
/*
	Writen By: Eric Quillen
*/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SpringLayout;

/** Creates a {@link JPanel} for the user to changer his/her password.
*	@author Eric Quillen
*/
public class ChangePasswordPanel extends JPanel{

	private GUI gui;							// Link to main gui class

	private JPanel changePasswordPanel;			// Change Password Panel.
	private SpringLayout layout;				// Layout for Panel.

	private JLabel oldPasswordError;			// Error -- current password does not match.
	private JLabel newPasswordError;			// Error -- new password fields do not match.
	private JLabel oldPasswordLabel;			// Prompt for current password.
	private JLabel newPasswordLabel;			// Prompt for employee's new password.
	private JLabel newPasswordLabel2;			// Prompt for employee's new password for error check.
	private JLabel heading;						// Heading for the panel.

	private JPasswordField oldPasswordField;	// Employee's current Password.
	private JPasswordField newPasswordField;	// Employee's new Password.
	private JPasswordField newPasswordField2;	// Employee's new Password error check.

	private JButton submitButton;				// Button to submit info.
	private JButton clearFormButton;			// Button to clear all fields.

	private SubmitButtonAL submitAL;			// Action Listener for submitButton.
	private ClearFormButtonAL clearFormAL;		// Action Listener for clearFormButton.

	/** Creates a GUI panel for the user to change his/her password
	*/
	public ChangePasswordPanel(GUI x)
	{
		gui = x;

		changePasswordPanel = new JPanel();
		layout = new SpringLayout();
		setLayout(layout);

		oldPasswordError = new JLabel("<html><font color = red>Invalid Current Password</font></html>");
		newPasswordError = new JLabel("<html><font color = red>New Entered Passwords Do Not Match</font></html>");
		oldPasswordLabel = new JLabel("Enter your current password: ");
		newPasswordLabel = new JLabel("Enter your new password: ");
		newPasswordLabel2 = new JLabel("Reenter your new password: ");
		heading	= new JLabel("<html><font size = 20>Password Management</font></html>");

		oldPasswordField = new JPasswordField(20);
		newPasswordField = new JPasswordField(20);
		newPasswordField2= new JPasswordField(20);

		submitButton = new JButton("Submit");
		clearFormButton = new JButton("Clear Form");

		submitAL = new SubmitButtonAL();
		clearFormAL = new ClearFormButtonAL();

		submitButton.addActionListener(submitAL);
		clearFormButton.addActionListener(clearFormAL);

		add(heading);
		add(oldPasswordError);
		add(newPasswordError);
		add(oldPasswordLabel);
		add(oldPasswordField);
		add(newPasswordLabel);
		add(newPasswordField);
		add(newPasswordLabel2);
		add(newPasswordField2);
		add(submitButton);
		add(clearFormButton);

		placeComponents();

		oldPasswordError.setVisible(false);
		newPasswordError.setVisible(false);
	}

	class SubmitButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			resetError();
			// If old password matches
			/*if(Arrays.equals(oldPasswordField.getPassword(), gui.currentPassword()))
			{
				// If new password matches 2nd entered new password
				if(Arrays.equals(newPasswordField.getPassword(), newPasswordField2.getPassword()))
				{
					gui.changePassword(newPasswordField.getPassword());
					reset();
					gui.goTo("Login");
				}
				else
				{
					newPasswordError.setVisible(true);
				}
			}
			else
			{
				oldPasswordError.setVisible(true);
			}*/
		}
	}

	class ClearFormButtonAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			reset();
		}
	}

	/** Resets all components.
	*/
	public void reset()
	{
		resetError();
		oldPasswordField.setText("");
		newPasswordField.setText("");
		newPasswordField2.setText("");
	}

	/** Resets error messages to not visible.
	*/
	public void resetError()
	{
		oldPasswordError.setVisible(false);
		newPasswordError.setVisible(false);
	}

	/** Places all GUI components using {@link SpringLayout}.
	*/
	public void placeComponents()
	{
		// Place heading.
		layout.putConstraint(SpringLayout.EAST, heading,
                             600,
                             SpringLayout.WEST, changePasswordPanel);
        layout.putConstraint(SpringLayout.NORTH, heading,
                             50,
                             SpringLayout.NORTH, changePasswordPanel);

		// Place oldPasswordError.
		layout.putConstraint(SpringLayout.EAST, oldPasswordError,
                             500,
                             SpringLayout.WEST, changePasswordPanel);
        layout.putConstraint(SpringLayout.NORTH, oldPasswordError,
                             30,
                             SpringLayout.SOUTH, heading);

		// Place newPasswordError.
		layout.putConstraint(SpringLayout.EAST, newPasswordError,
                             500,
                             SpringLayout.WEST, changePasswordPanel);
        layout.putConstraint(SpringLayout.NORTH, newPasswordError,
                             30,
                             SpringLayout.SOUTH, heading);

		// Place oldPasswordLabel.
		layout.putConstraint(SpringLayout.EAST, oldPasswordLabel,
                             375,
                             SpringLayout.WEST, changePasswordPanel);
        layout.putConstraint(SpringLayout.NORTH, oldPasswordLabel,
                             50,
                             SpringLayout.SOUTH, heading);

		// Place oldPasswordField.
		layout.putConstraint(SpringLayout.WEST, oldPasswordField,
                             20,
                             SpringLayout.EAST, oldPasswordLabel);
        layout.putConstraint(SpringLayout.NORTH, oldPasswordField,
                             0,
                             SpringLayout.NORTH, oldPasswordLabel);

		// Place newPasswordLabel.
		layout.putConstraint(SpringLayout.EAST, newPasswordLabel,
                             375,
                             SpringLayout.WEST, changePasswordPanel);
        layout.putConstraint(SpringLayout.NORTH, newPasswordLabel,
                             15,
                             SpringLayout.SOUTH, oldPasswordLabel);

		// Place newPasswordField.
		layout.putConstraint(SpringLayout.WEST, newPasswordField,
                             20,
                             SpringLayout.EAST, newPasswordLabel);
        layout.putConstraint(SpringLayout.NORTH, newPasswordField,
                             0,
                             SpringLayout.NORTH, newPasswordLabel);

		// Place newPasswordLabel2.
		layout.putConstraint(SpringLayout.EAST, newPasswordLabel2,
                             375,
                             SpringLayout.WEST, changePasswordPanel);
        layout.putConstraint(SpringLayout.NORTH, newPasswordLabel2,
                             15,
                             SpringLayout.SOUTH, newPasswordLabel);

		// Place newPasswordField2.
		layout.putConstraint(SpringLayout.WEST, newPasswordField2,
                             20,
                             SpringLayout.EAST, newPasswordLabel2);
        layout.putConstraint(SpringLayout.NORTH, newPasswordField2,
                             0,
                             SpringLayout.NORTH, newPasswordLabel2);

		// Place submitButton.
		layout.putConstraint(SpringLayout.EAST, submitButton,
                             0,
                             SpringLayout.EAST, newPasswordLabel2);
        layout.putConstraint(SpringLayout.NORTH, submitButton,
                             15,
                             SpringLayout.SOUTH, newPasswordLabel2);

		// Place submitButton.
		layout.putConstraint(SpringLayout.WEST, clearFormButton,
                             0,
                             SpringLayout.WEST, newPasswordField2);
        layout.putConstraint(SpringLayout.NORTH, clearFormButton,
                             15,
                             SpringLayout.SOUTH, newPasswordLabel2);

	}

}
