//
//LoginAgent.java
//FAA_control
//
//created by Andrew Tio
//portions of register/login/remove by James Marsh
//

package agents;
import faa.FAAControl;
import airport.*;
import airport.objects.*;
import agent.Agent;
import gui.*;
import java.util.*;

public class LoginAgent extends Agent{

	FAAControl faaControl;

	LoginPanel loginGui;
	RegistrationPanel registrationGui;

	static private LoginAgent admin = new LoginAgent("Login Agent");
	List<NewUser> loggingInUsers = new LinkedList<NewUser>();
	List<UserSchema> addingUsers = new LinkedList<UserSchema>();
	List<String> deletingUsers = new LinkedList<String>();
	List<ChangingPassUser> passChangingUsers = new LinkedList<ChangingPassUser>();
	LoggingAgent logger;
	String name;
    boolean guiWantsList = false;

	private LoginAgent(String n) {
		super();
		name = n;
		System.out.println("LoginAgent created");
		logger = LoggingAgent.getInstance();
	}

	public String getName(){
		return name;
	}

	public static LoginAgent getInstance(){
		return admin;
	}
	//Pressing "Login" button will trigger this msg
	synchronized public void msgWantToLogin(LoginPanel loginGui, String username, char[] password, String position,String scenario){
		print("msgWantToLogin received.");
		this.loginGui = loginGui;
		NewUser u = new NewUser();
		u.setName(username);
		u.setWord(translateToString(password));
		u.setPosition(position);
		u.setScenario(scenario);
		loggingInUsers.add(u);
		print(""+loggingInUsers.size()+" user requesting login.");
		stateChanged();
	}
	//Pressing "Register" button will trigger this msg
	synchronized public void msgWantToRegister(Personnel person){
		print("msgWantToRegister");
		UserSchema u = new UserSchema();
		u.setFirstName(person.getFirstName());
		u.setLastName(person.getLastName());
		u.setUsername(person.getUserID());
		u.setPassword(translateToString(person.getPassword()));
		u.setUserPositions(person.getPositions());
		addingUsers.add(u);
		stateChanged();
	}

	synchronized public void msgWantToDelete(String username){
		print("msgWantToDelete");
		deletingUsers.add(username);
		stateChanged();
	}

	synchronized public void msgWantUserList(RegistrationPanel gui){
		registrationGui = gui;
		guiWantsList = true;
		stateChanged();
	}

	synchronized public void msgWantToChangePassword(String uname, char[] oin, char[] nin, char[] confirmnin){
		String o = translateToString(oin);
		String n = translateToString(nin);
		String confirmn = translateToString(confirmnin);
		print("msgWantToChangePassword received.  Now checking "+uname+
				" "+o+" "+n+" "+confirmn+"and confirmation of new password.");
		if(n==confirmn){
			print("Passwords match.");
			ChangingPassUser u = new ChangingPassUser(uname,o,n);
			passChangingUsers.add(u);
			print("added to passChangingUsers list");
		}
		else {
			print("Passwords do not match");
			//loginGui.passwordsDontMatch(); //Implement this later
		}
		stateChanged();
	}
	    /** Scheduler.  Determine what action is called for, and do it. */
	synchronized protected boolean pickAndExecuteAnAction() {

	    if(!loggingInUsers.isEmpty()){
	    	print("login:: username: " +loggingInUsers.get(0).getUsername()+ " pword:" +loggingInUsers.get(0).getPassword() + " scenario:"+loggingInUsers.get(0).getScenario());
	    	lookup(loggingInUsers.get(0));
	    	return true;
	    }
		if(!deletingUsers.isEmpty()){
	    	print("del:: username: " +deletingUsers.get(0));
	    	deleteUser(deletingUsers.get(0));
	    	return true;
	    }
	    if(!addingUsers.isEmpty()){
	    	print("add:: username: " +addingUsers.get(0).getUsername()+ " pword:" +addingUsers.get(0).getPassword());
	    	addUser(addingUsers.get(0));
	    	return true;
	    }
	    if(!passChangingUsers.isEmpty()){
	    	print(""+passChangingUsers.size()+" trying to change password");
	    	print("username: "+passChangingUsers.get(0).getUName()+
	    			" oldpword: " +passChangingUsers.get(0).getOld()+
	    			" newpword: "+passChangingUsers.get(0).getNew());
	    	changePass(passChangingUsers.get(0).getUName(),passChangingUsers.get(0).getOld(),
	    					passChangingUsers.get(0).getNew());
	    	return true;
	    }
		if(guiWantsList == true){
			giveGuiList();
			return true;
		}
	    return false;
	}

	// Actions
	private void lookup(NewUser lookupUser){
		Do("Look up the user "+lookupUser+" in database");
	    UserSchema user = Database.findUser(lookupUser.getUsername(),lookupUser.getPassword());
	    if(user == null){
			Do("User does not exist.");
			loginGui.msgLoginResult(2);
			loggingInUsers.remove(0);
		}
	    else if(user.getUserPositions().contains(lookupUser.getPosition())){
			Do("User "+user.getUserID()+" logged in as "+lookupUser.getPosition()+".");
	    	logger.msgLoggedInUser(user.getUserID());
	    	//atc.msgUserLoggedIn(user.getUserID(), lookupUser.getPosition());
	    	//loginGui.msgLoginResult(1);

	    	String position = lookupUser.getPosition();

	    	if ("supervisor".equals(position)) {
	    		loginGui.msgLoginResult(1);
	    	} else
	    		faaControl.startControl(lookupUser.getUsername(),lookupUser.getPosition(),lookupUser.getScenario());

			loggingInUsers.remove(0);
	    }
		else {
			Do("User tried to login with invalid position.");
	    	loginGui.msgLoginResult(4);
			loggingInUsers.remove(0);
	    }
	}

	private void addUser(UserSchema newUser){
	    Do("Add user to database");
	    Database.addUser(newUser);
		addingUsers.remove(0);
	}

	private void deleteUser(String username){
	    Do("Delete user from database");
	    Database.removeUser(username);
		deletingUsers.remove(0);
	}

	private void changePass(String u, String o, String p){
	    Do("Asking Database to change password");
	    boolean result= Database.changePassword(u,o,p);
	    passChangingUsers.remove(0);
	    //loginGui.msgStatOfPasswordChange(result);
	}

	private void giveGuiList(){
		List<UserSchema> userList = Database.getUserList();
		Map<String, Personnel> map = new TreeMap<String, Personnel>();

		for(UserSchema user:userList){
			Personnel temp = new Personnel();
			temp.setFirstName(user.getFirstName());
			temp.setLastName(user.getLastName());
			temp.setPositions(user.getUserPositions());
			temp.setUserID(user.getUsername());
			temp.setPassword(user.getPassword().toCharArray());
			map.put(user.getLastName()+" "+user.getFirstName(),temp);
		}

		registrationGui.printList(map);
		guiWantsList = false;

	}

	private class ChangingPassUser{
	    String username;
	    String oldPword;
	    String newPword;

	    public ChangingPassUser(String u, String o, String n){
	    	this.username=u;
	    	this.oldPword=o;
	    	this.newPword=n;
	    }
	    public String getUName(){
	    	return username;
	    }
	    public String getOld(){
	    	return oldPword;
	    }
	    public String getNew(){
	    	return newPword;
	    }
	}

	private class NewUser{
	    String uname;
	    String pword;
		String position;
		String scenario;

	    public NewUser(){
	    	super();
	    }
	    public void setName(String u){
	    	uname=u;
	    }
	    public void setWord(String p){
	    	pword=p;
	    }
		public void setPosition(String position){
			this.position = position;
		}
	    public String getUsername(){
	    	return uname;
	    }
	    public String getPassword(){
	    	return pword;
	    }
		public String getPosition(){
			return position;
		}
		public String getScenario() {
			return scenario;
		}
		public void setScenario(String scenario) {
			this.scenario = scenario;
		}
	}


	private String translateToString(char[] input){
		String output = "";
		for(int i = 0; i < input.length; i++){
			output += input[i];
		}
		return output;
	}


	public void setFAAControl(FAAControl ctrl) {
		faaControl = ctrl;
	}



}
