package gui;
/*
	Writen By: Eric Quillen
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import airport.*;

/** System Menu Bar.
*	@author Eric Quillen
*/
public class FAAMenuBar extends JFrame{

	GUI gui;

	JMenuBar menuBar;
		JMenu system;
			JMenuItem changePassword;
			ChangePasswordAL changePasswordAL;
			JMenuItem logout;
			LogoutAL logoutAL;

	public FAAMenuBar(GUI x)
	{
		gui = x;

		menuBar = new JMenuBar();
			system = new JMenu("System");
				changePassword = new JMenuItem("Change Password");
				changePasswordAL = new ChangePasswordAL();
				changePassword.addActionListener(changePasswordAL);
				logout = new JMenuItem("Log Out");
				logoutAL = new LogoutAL();
				logout.addActionListener(logoutAL);


//		system.add(changePassword);		//Will add back in next version
		system.add(logout);

		menuBar.add(system);
	}

	// Action listener for changePassword menu item and flips the card panel in the main gui
	// to the changePassword panel.
	class ChangePasswordAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			gui.goTo("Change Password");
		}
	}

	// Action listener for logout menu item and flips the card panel in the main gui
	// to the login panel.
	class LogoutAL implements ActionListener{
		public void actionPerformed(ActionEvent e)
		{
			//clean up the simulator
			TheSimulator.getInstance().reset();
			gui.goTo("Login");

		}
	}

	/** Removes the System menu to the menubar.
	*/
	public void removeMenu()
	{
		menuBar.remove(system);
	}

	/** Adds the System menu to the menubar.
	*/
	public void addMenu()
	{
		menuBar.add(system);
	}


	/** Returns a JMenuBar object.
	*	@return Returns the menu bar created in this class.
	*/
	public JMenuBar returnMenuBar()
	{
		return menuBar;
	}
}
