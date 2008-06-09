package agents;

import airport.objects.Airport;
import interfaces.ClearanceDelivery;
import interfaces.Pilot;
import gui.*;
import interfaces.*;

import java.util.*;

/**
 *
 * @author Patrick Monroe
 *
 */
public class UserClearanceAgent extends ATCAgent implements ClearanceDelivery {

	ClearanceDeliveryPanel clearancePanel;

	public UserClearanceAgent(String name,Airport airport,ClearanceDeliveryPanel panel){
		super(name,airport);
		this.clearancePanel = panel;
		panel.setClearanceAgent(this);
	}

	protected boolean pickAndExecuteAnAction() {
		//now check if there are any commands to verify
		if (commandsToVerify.size() > 0) {
			if (doCommandVerification())
				return true;
		}



		return false;
	}


	private boolean doCommandVerification() {
		Command c = commandsToVerify.remove();
		if(verifyCommand(c)) {
			if(c.getEchoType()==EchoType.CD_CLEARANCE_GRANTED) {
				giveMode(c.getPilot());
				return true;
			}
		}
		return false;
	}

	public void msgRequestingClearance(Pilot pilot, String planeName,
			String gate, String destination) {
		// TODO Auto-generated method stub
		String message = planeName + "requesting clearance.";
		//ClearanceRequest cr = new ClearanceRequest(pilot, planeName, gate, destination);
		//clearanceRequests.add(cr);
		DepartureMediator departure = new DepartureMediator(pilot.getFlight());
		departures.put(pilot,departure);

		clearancePanel.newPilotMessage((PilotAgent)pilot, message, departure);
	}

	public void grantClearance(Pilot pilot){

		DepartureMediator departure = departures.get(pilot);

		if (departure == null) {

			//error
			return;
		}

		double  frequency = 1000;
		int code = 89;
		Do(pilot + " : Clearance Granted. Please use this runway: " + pilot.getFlight().getWay() + " and contact ground at radio frequency at " + frequency + " and code " + code);

		departure.setStatus(DepartureMediator.DepartureStatus.Taxiing);
		clearancePanel.update();

		pilot.msgClearanceGranted(this, pilot.getFlight().getWay(), frequency, code);

		//Queue command to commands issued
		String command = "Clearance Granted. Runway: " + pilot.getFlight().getWay() + ", frequency: " + frequency + ", Code: " + code;
		issueCommand(pilot ,new Command(pilot ,command,EchoType.CD_CLEARANCE_GRANTED));
	}

	public void giveMode(Pilot pilot){
		DepartureMediator departure = departures.get(pilot);
		if (departure == null) {
			return;
		}

		clearancePanel.removeFlight(pilot);

		String guidanceMode = "VISUAL";
		char mode = '1';

		Do("Giving pilot mode.");
		pilot.msgHereIsMode(this, guidanceMode, mode);

		//Queue command to commands issued
		String command = "Mode: " + guidanceMode + ", letter: " + mode;
		issueCommand(pilot,new Command(pilot,command,EchoType.HERE_IS_MODE));
	}


}
