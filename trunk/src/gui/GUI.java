package gui;

/*********************
 * This creates the main GUI for the application.
 *
 * @author Julian Vergel de Dios, John Baldo
 *
 *********************/

/*
import java.awt.GridLayout;
import java.util.*;
import javax.swing.*;

import airport.*;
import airport.objects.*;
import guiIntegration.*;

public class GUI extends JFrame {

	private Airport airport; //initialized by backend via initAirport(Airport) method
	private HashMap<String,Airplane> airplanes; //updated by backend via updateWorld() method
	private LocalControlPanel lcp;
	private GroundControlPanel gcp;
	private ClearanceDeliveryPanel cdp;
	private static GUI instance = null;

	private GUI() {
		this.airport = null;		//initializes airport and plane locations to null
		this.airplanes =new HashMap<String,Airplane>();

		lcp = new LocalControlPanel(this);
		gcp = new GroundControlPanel(this);
		cdp = new ClearanceDeliveryPanel(this);

		JTabbedPane frameTabs = new JTabbedPane(); //creates the tabbed layout for the frame
		frameTabs.addTab("Local Control", lcp);
		frameTabs.addTab("Ground Control", gcp);
		frameTabs.addTab("Clearance Delivery", cdp);
		setSize(1024,768);
        setTitle("Control Tower System");
		setLayout(new GridLayout(1,1));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		add(frameTabs);

		//setResizable(false);
		setVisible(true);
	}

	//get method for Airport object
	public Airport getAirport() {
		return airport;
	}

	//get method for Airplane HashMap object
	public HashMap<String,Airplane> getAirplanes() {
		return airplanes;
	}

	//update method for the WorldState object
	public void updateWorld() {
		//this.worldState = RealWorld.getCurrentState();
		lcp.updateMap();
		gcp.updateMap();
		repaint();
	}

	public void addPlane(Airplane ap) {
		airplanes.put(ap.getName(),ap);
	}

	//initialize method for Airport, passed from backend at beginning of execution
	public void setAirport(Airport ap) {
		this.airport = ap;

		lcp.updateMap();
		gcp.updateMap();
	}

	public void updateMessages(TraceMessage m){
		MessageData.getInstance().fireTraceMessage(m);
		lcp.updateTrace();
		cdp.updateTrace();
		gcp.updateTrace();
	}

	public static GUI getInstance() {

		if (instance == null) {
			instance = new GUI();
		}

		return instance;
	}

}
*/


/*
	Writen By: Eric Quillen
*/
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import airport.*;
import airport.objects.*;

// main gui. Sets up main window.
/**
 * @author Henry Yuen
 */
public class GUI extends JFrame {

	// Begin Variable Declarations
	private Map<String, Personnel> sortedNamesMap;			// TreeMap to hold all Personnel with key being Full Name.

	private JFrame mainFrame;									// Main Frame

	private FAAMenuBar mainMenuBar;								// Menu Bar.

	private ChangePasswordPanel changePasswordPanel;			// Change Password Panel

	private JPanel cardPanel;									// A Panel with Cardlayout to switch between Panels.
	private LoginPanel loginPanel;							// Login Panel
	private SupervisorPanel supervisorPanel;				//supervisorPanel

	private JPanel currentPanel;							//the current card that is being displayed

	private GroundControlPanel groundControlPanel;

	private Airport airport; //initialized by backend via initAirport(Airport) method
	private HashMap<String,Airplane> airplanes; //updated by backend via updateWorld() method

	// End Variable Declarations
	/** Creates a JFrame and initalizes all GUI classes.
	* @param initTemp initial conditions file name
	* @param xmlTemp xml scenario file name.
	*/
	public GUI()
	{

		this.airport = null;		//initializes airport and plane locations to null
		this.airplanes =new HashMap<String,Airplane>();

		Database.openFile();
		sortedNamesMap = new TreeMap<String, Personnel>();

		mainFrame = new JFrame("FAA Control");
		setSize(1150,768);
        setTitle("Control Tower System");
		setLayout(new GridLayout(1,1));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Window Listener added to the mainFrame so when the window is closed
		// it will close the program.
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
						  Database.saveFile();
						  System.exit(0);
            }
        });

		mainMenuBar = new FAAMenuBar(this);

		cardPanel = new JPanel(new CardLayout());

		loginPanel = new LoginPanel(this);
		//changePasswordPanel = new ChangePasswordPanel(this);
		//supervisorPanel = new SupervisorPanel(this);

		cardPanel.add(loginPanel, "Login");
		//cardPanel.add(changePasswordPanel, "Change Password");
		//cardPanel.add(supervisorPanel, "Supervisor");

		currentPanel = loginPanel;

		setJMenuBar(mainMenuBar.returnMenuBar());

		add(cardPanel);

		mainMenuBar.removeMenu();

		//initialize the fonts
		PrintManager.getInstance().addFont("small", new Font("Georgia",Font.BOLD,12));
		PrintManager.getInstance().addFont("medium", new Font("Georgia",Font.BOLD,16));
		PrintManager.getInstance().addFont("large", new Font("Georgia",Font.BOLD,24));
		PrintManager.getInstance().addFont("huge", new Font("Georgia",Font.BOLD,48));

		validate();
		//setResizable(false);
		setVisible(true);
	}


	// Switch Panels.
	/** Swiches Panels using CardLayout.
	*	@param x Name of Panel to switch to.
	*/
	public void goTo(String x)
	{
		x = x.toLowerCase();

		JPanel newPanel = null;

		if ("login".equals(x)) {
			newPanel = new LoginPanel(this);
		} else if ("supervisor".equals(x)) {
			newPanel = new SupervisorPanel(this);
		} else if ("groundcontrol".equals(x)) {
			newPanel = new GroundControlPanel(this);
		} else if ("localcontrol".equals(x)) {
			newPanel = new LocalControlPanel(this);
		} else if ("clearancedelivery".equals(x)) {
			newPanel = new ClearanceDeliveryPanel(this);
		}

		if (currentPanel != null) {
			//cardPanel.remove(currentPanel);
			cardPanel.removeAll();
		}

		if (newPanel != null) {
			cardPanel.add(newPanel,x);
			CardLayout cl = (CardLayout)(cardPanel.getLayout());
			cl.show(cardPanel, x);
			cardPanel.updateUI();
		}

		currentPanel = newPanel;

		if(x.equals("login"))
		{
			mainMenuBar.removeMenu();
		}
		else
		{
			mainMenuBar.addMenu();
		}

		if(x.equals("supervisor"))
		{
			//supervisorPanel.updateList();
			((SupervisorPanel)newPanel).updateList();
		}

	}

	/** Gets the Username of an employee.
	*	@param lastFirst Name of employee.
	*	@return Username of the employee.
	*/
	public String getUsername(String lastFirst)
	{
		return sortedNamesMap.get(lastFirst).getUserID();
	}

	/** Sets the a TreeMap of HW3Personnel with the employees name as the key.
	*	@param map
	*/
	public void setMap(Map<String, Personnel> map)
	{
		sortedNamesMap.clear();
		sortedNamesMap = map;
	}

	/** Gets a HW3Personnel.
	*	@param x Key to the sortedNamesMap - employee's name.
	*	@return A {@link HW3Personnel}.
	*/
	public Personnel getEmployee(String x)
	{
		return sortedNamesMap.get(x);
	}

	//get method for Airport object
	public Airport getAirport() {
		return airport;
	}

	//get method for Airplane HashMap object
	public HashMap<String,Airplane> getAirplanes() {
		return airplanes;
	}

	public void addPlane(Airplane ap) {
		airplanes.put(ap.getName(),ap);
	}

	//initialize method for Airport, passed from backend at beginning of execution
	public void setAirport(Airport ap) {
		this.airport = ap;
	}

	public void updateWorld() {
		if (currentPanel instanceof CommandPanel) {
			CommandPanel cp = (CommandPanel)currentPanel;
			cp.updateWorld();
		}
	}

	public JPanel getCurrentPanel() {
		return currentPanel;
	}
}

