package gui;
/*
	Writen By: Eric Quillen
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import agents.LoginAgent;

/** Supervisor JPanel.
*	@author Eric Quillen
*/
public class SupervisorPanel extends JPanel{

	GUI gui;

	private RegistrationPanel registrationPanel;				// Admin Panel

	JPanel supervisorPanel;

	JTabbedPane tabbedPane;

	/** Creates a JPanel for the Supervisor Position.
	*/
	public SupervisorPanel(GUI x)
	{
		gui = x;

		registrationPanel = new RegistrationPanel(gui);


		supervisorPanel = new JPanel(new GridLayout(1,1));

		tabbedPane = new JTabbedPane();
		tabbedPane.add("Employee Registration", registrationPanel);

		add(tabbedPane);

		setVisible(true);
	}

	/** Updates the employee list in the Registration Panel.
	*/
	public void updateList()
	{
		LoginAgent.getInstance().msgWantUserList(registrationPanel);
	}

	/** Clears and Updates Risk Arrays in the the Ground Control and Local Control Panels.
	*/
	public void clearUpdateArrays()
	{
	}

	/** Resets Ground Control and Local Control Screens
	*/
	public void resetScreen()
	{
	}
}
