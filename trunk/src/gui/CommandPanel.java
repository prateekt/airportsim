package gui;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import agents.*;

/**
 *
 * @author Julian
 *
 */
public abstract class CommandPanel extends JPanel implements ActionListener, ChangeListener {

	GUI gui;
	AirportViewPanel airportViewPanel;	//the map
	JScrollPane airportScrollPane;

	private JToolBar sceneToolBar; //JToolBar object for controlling scenarios - Komdetch
	private JButton playScene; //button to begin playing scenario
	private JButton nextTimeStep; //button to advance to next time step in scenario
	private JButton pauseScene;
	private JButton resetButton;	//this restarts the simulation

	private JSlider zoomSlider;	//this controls the zoom factor of the program

	public CommandPanel(GUI gui) {
		this.gui = gui;
		initUI();
	}

	/**
	 * Initializes the UI components of TowerControlFrame
	 */
	protected void initUI() {

		//initialize the fonts
		PrintManager.getInstance().addFont("small", new Font("Georgia",Font.BOLD,12));
		PrintManager.getInstance().addFont("medium", new Font("Georgia",Font.BOLD,16));
		PrintManager.getInstance().addFont("large", new Font("Georgia",Font.BOLD,24));
		PrintManager.getInstance().addFont("huge", new Font("Georgia",Font.BOLD,48));

		setLayout(new BorderLayout());

		airportViewPanel = new AirportViewPanel(gui);

		JPanel commandPanel = initCommandPanel();

		//add(commandPanel,cons);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				airportViewPanel,commandPanel);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(550);

		Dimension minimumSize = new Dimension(-1,150);

		airportViewPanel.setMinimumSize(minimumSize);
		commandPanel.setMinimumSize(minimumSize);

		splitPane.setMinimumSize(new Dimension(800,-1));

		JPanel utilityPanel = initUtilityPanel();

		JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				splitPane,utilityPanel);
		splitPane2.setDividerLocation(800);
		splitPane2.setOneTouchExpandable(true);


		add(splitPane2,BorderLayout.CENTER);
	}

	protected JPanel initCommandPanel() {
		return null;
	}

	protected JPanel initUtilityPanel() {
		return null;
	}


	/**
	 * Handles all the menu actions n TowerControlFrame
	 * @param e Represents what action happened
	 */
	public void actionPerformed(ActionEvent e) {

	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == zoomSlider) {
			//if (!zoomSlider.getValueIsAdjusting()) {
				int zoom = zoomSlider.getValue();
				double zoomFactor = (double)zoom/100.0;

				if (airportViewPanel != null)
					airportViewPanel.setZoomFactor(zoomFactor);
			//}
		}
	}

	public void update() {

	}

	public void updateWorld() {
		airportViewPanel.repaint();
		repaint();
	}

	public AirportViewPanel getAirportViewPanel() {
		return airportViewPanel;
	}



}
