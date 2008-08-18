package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author John Baldo
 *
 */
public class TabbedConfirmPanel extends JPanel {
	private JTabbedPane tabbedPane;
	private int tabCount = 0;

	TracePanel tracePanel = null;

	public TabbedConfirmPanel() {
		setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();
		add(tabbedPane,BorderLayout.CENTER);

		setMinimumSize(new Dimension(250,-1));
		setSize(new Dimension(250,100));

		JPanel intro = new JPanel(new BorderLayout());

		String instructions = "<html><center>Welcome to the FAA Tower Control System</center><br>" +
					"<br>New messages from pilots will appear here. Click \"Confirm\" to accept them.<br><br>" +
					"1. Ground Control<br>" +
					"When giving arrival instructions, take it to its proper gate (red circle).<br>" +
					"When giving departure instructions, take it to Runway 24R. Local Control will handle it from there.<br>" +
					"<br>" +
					"2. Local Control<br>" +
					"When giving arrival instructions, simply get it out of Runway 24R and onto some taxiway. Ground Control will handle it from there.<br>" +
		"</html>";

		intro.add(new JLabel(instructions));

		tracePanel = new TracePanel();

		tabbedPane.addTab("System",intro);
		tabbedPane.add("Trace Log", tracePanel);

		tabCount = 0;
	}


	public void addTab(ConfirmPanel panel) {
		tabCount++;

		panel.setOwnerDialog(this);
		tabbedPane.addTab(panel.getFrom(),panel);

		if (tabCount == 1) {
			setVisible(true);
		}

		//make this new tab visible
		tabbedPane.setSelectedComponent(panel);

		repaint();
	}

	public void removeTab(ConfirmPanel panel) {
		tabCount = tabCount - 1;
		if (tabCount < 0) {
			System.out.println("TabbedConfirmPanel error: Tried to remove tab from empty tabs!");
			tabCount = 0;
			return;
		}

		tabbedPane.remove(panel);
		repaint();

		if (tabCount == 0) {
			//hide ourselves
			//setVisible(false);
		}
	}

	public void update() {
		tracePanel.update();
	}

}
