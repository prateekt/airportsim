package gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;

/**
 *
 * @author John Baldo
 *
 */
public class ConfirmPanel extends JPanel {

	private JLabel fromLabel;
	private JScrollPane scrollPane;
	private JEditorPane messagePane;
	private JButton confirmButton;

	private String text;
	private String from;

	private TabbedConfirmPanel ownerDialog;

	public ConfirmPanel() {

		setLayout(new BorderLayout());

		fromLabel = new JLabel();

		messagePane = new JEditorPane();
		messagePane.setContentType("text/html");	//Allow html in strings
		messagePane.setEditable(false);	    	//do not allow user to edit

		scrollPane = new JScrollPane(messagePane,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		confirmButton = new JButton("Confirm");

		add(fromLabel,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		add(confirmButton,BorderLayout.SOUTH);

		text = null;
		from = null;
		ownerDialog = null;
	}

	public void setOwnerDialog(final TabbedConfirmPanel dialog) {
		ownerDialog = dialog;

		final ConfirmPanel me = this;

		//add the new actionlistener
		confirmButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvt) {
				Object source = actEvt.getSource();
				if (source == confirmButton) {
					dialog.removeTab(me);
				}
			}
		});

	}
	public void setFrom(String from) {
		this.from =  from;
		fromLabel.setText("<html>Message from: <b>" + from + "</b></html>");
		updateUI();
	}

	public String getFrom() {
		return from;
	}

	public void setText(String text) {
		this.text = text;
		messagePane.setText(text);
		updateUI();
	}

	public String getText() {
		return text;
	}

	public void addActionListener(ActionListener al) {
		confirmButton.addActionListener(al);
	}

}
