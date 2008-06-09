//
//  Database.java
//  FAA_Control
//
//  Created by Riley Marsh on 10/30/07.
//  Copyright 2007 Riley Marsh. All rights reserved.
//
package airport;
import airport.objects.*;

import java.util.*;
import java.io.*;
import java.sql.*;

public class Database {

	// A list of users in the system. This will be reset upon opening.
	// This is a temporary fix until we have a database.
	private static List<UserSchema> userList = null;

	// Location of the users file
	private static String filename = "resource/usersList.dat";

	// Database driver settings
	private static String userName = "jamesmar";
	private static String userPassword = "";
	private static String dbDriverName = "com.mysql.jdbc.Driver";
	private static String dbURL = "10.8.11.70";//jdbc:mysql://



	/** Validates a new user and sends to database add.
	*	@param newUser The UserSchema with the new user's information.
	*	@return True if user add was successful.
	*/
	public static boolean addUser(UserSchema newUser){
		// could do validation on the user here
		// then pass to database
		if(addUserToTable(newUser))
			System.out.println("Database: Add sucessful for "+newUser.getUsername() + "[" + newUser.getUserPositions() + "]");
		else
			System.out.println("Database: " + newUser.getUsername() + " could not be added");
		return true;
	}

	/** Validates a remove user and sends to database remove.
	*	@param username The UserSchema with the remove user's information.
	*	@return True if user remove was successful.
	*/
	public static boolean removeUser(String username){
		// get the full user from the username
		UserSchema removeUser = retrieveUserLocal(username);
		if(removeUserFromTable(removeUser))
			System.out.println("Database: Remove sucessful for "+removeUser.getUsername());
		else
			System.out.println("Database: " + removeUser.getUsername()+" could not be found or removed");
		return true;
	}

	/** Adds a User to the database.
	*	@param newUser The UserSchema with the new user's information.
	*	@return True if user add was successful.
	*/
	private static boolean addUserToTable(UserSchema newUser){
		// Insert new user in database
		userList.add(newUser);
		return true;
	}

	/** Removes a User from the database.
	*	@param removeUser The UserSchema with the user to remove.
	*	@return True if user remove was successful.
	*/
	private static boolean removeUserFromTable(UserSchema removeUser){
		// Remove the User in the database
		return userList.remove(removeUser);
	}

	/** Finds a user in the database and checks if the supplied password is correct.
	*	@param username The username of the user we would like to log in.
	*	@param password The password of the user to check before login.
	*	@return UserSchema of the user if exists and password is correct.<br />null if does not exist
	*/
	public static UserSchema findUser(String username, String password){
		System.out.println("Database: Received request to find |"+username+"| in with password |"+password+"|.");

		// two lines for retrieving users form either mock or real database
		// from real database
		//UserSchema user = retrieveUserDatabase(username);

		// from mock database
		UserSchema user = retrieveUserLocal(username);

		if(user == null){
			System.out.println("Database: User not found, returned null");
			return null;
		}
		else if(username.equals( user.getUsername() ) && password.equals( user.getPassword() )){
			System.out.println("Database: User found and returned");
			return user;
		}
		else
			System.out.println("Database: Invalid password for user.");
			return null;
	}

	/** Looks up a user and changes the password if the correct old password is provided.
	*	@param username Username for lookup in database
	*	@param oldPassword Old password for comparison to allow updating
	*	@param newPassword Password to change user to if allowed
	*	@return Boolean true - if password changed, false - if password not changed
	*/
	public static boolean changePassword(String username, String oldPassword, String newPassword){
		UserSchema user = findUser(username, oldPassword);
		if(user == null)
			return false;
		user.setPassword(newPassword);
		//updateUserTable(user);
		return true;

	}

	/** Get a list of all registered users for display in admin panel.
	*	@return List<UserSchema> A list of users
	*/
	public static List<UserSchema> getUserList(){
		return userList;
	}

	// hack function to fill the user list
	public static List<UserSchema> createUserList(){
		List<UserSchema> list = new LinkedList<UserSchema>();

		ArrayList<String> positions1 = new ArrayList<String>();
		positions1.add("groundcontrol");
		positions1.add("supervisor");
		positions1.add("localcontrol");
		positions1.add("clearancedelivery");

		ArrayList<String> positions2 = new ArrayList<String>();
		positions2.add("trafficmanager");
		positions2.add("helicoptercontrol");

		ArrayList<String> positions3 = new ArrayList<String>();
		positions3.add("trafficmanager");
		positions3.add("helicoptercontrol");
		positions3.add("groundcontrol");
		positions3.add("supervisor");
		positions3.add("localcontrol");
		positions3.add("clearancedelivery");

		UserSchema user1 = new UserSchema(0, "hyuen","password","Henry","Yuen");
		user1.setUserPositions(positions3);
		UserSchema user2 = new UserSchema(1, "ptandon","password","Prateek","Tandon");
		user2.setUserPositions(positions3);
		UserSchema user3 = new UserSchema(2, "ajay","usc","Ajay","Prasad");
		user3.setUserPositions(positions1);
		UserSchema user4 = new UserSchema(3, "andrew","trojans","Andrew","Tio");
		user4.setUserPositions(positions2);
		UserSchema user5 = new UserSchema(4, "eric","e","Eric","Quillen");
		user5.setUserPositions(positions3);


		list.add(user1);
		list.add(user2);
		list.add(user3);
		list.add(user4);
		list.add(user5);

		return list;

	}


	/** Internal function to get the user information from the userList if it exists
	*	@param username Username to look up in the mock database.
	*	@return UserSchema if user exists, null if it does not.
	*/
	private static UserSchema retrieveUserLocal(String username){

		// Iterate userList and look for username
		for(UserSchema user:userList){
			if(username.equals(user.getUsername()))
				return user;
		}
		return null;

	}


	/** Internal function to get the user information from the real database if it exists
	*	@param username Username to look up in the real database.
	*	@return UserSchema if user exists, null if it does not.
	*/
	private static UserSchema retrieveUserDatabase(String username){
		// make a connection
		Connection dbConnection = connectToDatabase();
		// search the database and return
		UserSchema searchUser = new UserSchema();
		try {
			synchronized (dbConnection) {
				Statement stmt = dbConnection.createStatement();
				String request = "select * from users where Username='"+username+"'";

				ResultSet result = stmt.executeQuery(request);

				if(result.next()){
					searchUser.setUserID(result.getInt("UserID"));
					searchUser.setUsername(result.getString("Username"));
					searchUser.setFirstName(result.getString("FirstName"));
					searchUser.setLastName(result.getString("LastName"));
					searchUser.setPassword(result.getString("Password"));
					//build positions list
					ArrayList<String> positions = new ArrayList<String>();
					String positionsString = result.getString("Positions");
					String[] positionsArray = positionsString.split(",");
					for(int i = 0; i < positionsArray.length; i++){
						//add each element
						positions.add(positionsArray[i]);
					}
					searchUser.setUserPositions(positions);
					stmt.close();
					dbConnection.close();
				}
			}
		} catch(Exception e){
		}

		return searchUser;

	}

	/** Write a log to the database by sorting the Log into fields.
	*	@param newLog Log to be added
	*	@return True - if successful, False - if failed.
	*/
	public static boolean writeLog(Log newLog){
		// split the log and write it to the database
		System.out.println("Database: Time["+newLog.getLogTime()+"] Type["
							+newLog.getLogType()+"] Message["
							+newLog.getLogMessage()+"]");
		return true;
	}

	/** Fetch a log file from the database based on the logTime.
	*	@param logTime Time of the log to be fetched.
	*	@return Log if found, null if not.
	*/
	public static Log retrieveLog(java.util.Date logTime){
		// get log at time from database and return it

		//static for testing retrieve log which does not work with gui as of yet.
		Log log1 = new Log(new java.util.Date(), "actionExecuted", "action: clear runway\nuser: 2");
		Log log2 = new Log(new java.util.Date(), "userLogin", "userID: 1");


		Log log = null;

		if(logTime == log1.getLogTime())
			log = log1;
		else if(logTime == log2.getLogTime())
			log = log2;

		return log;
	}

	// external hack for testing connectToDatabase()
	public static void connect(){
		connectToDatabase();
	}

	/** Create and return a connection to the database.
	*	@return A Connection to the database if succeeded or null if failed
	*/
    private static Connection connectToDatabase() {
		// username, password, url, and driver are declared at head of file
        Connection dbConnection;
        try {
            Class.forName(dbDriverName);
            dbConnection = DriverManager.getConnection(
                    dbURL,
                    userName, userPassword);
            dbConnection.setAutoCommit(false);
			System.out.println("Database: Connection successfully established");
            return dbConnection;
        } catch (Exception e) {
            System.out.println("Database: Connection Failed");
            System.out.println(e.toString());
            return null;
        }
    }

	/** Method to save list of users to a file for next session. Uses serialized files.
	 */
	public static void saveFile(){
		System.out.println("Database: Saving File");
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try{
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(userList);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}

	}

	/** Method to open and load the saved list of users.
	 */
	public static void openFile(){
		System.out.println("Database: Opening File");
		FileInputStream fis = null;
		ObjectInputStream in = null;

		try{
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			userList = (List<UserSchema>)in.readObject();
			in.close();
		} catch (ClassNotFoundException e){
			e.printStackTrace();
		} catch (FileNotFoundException e){
			System.out.println("Database: No file found. User list recreated");
			userList = createUserList();
		} catch (IOException e){
			e.printStackTrace();
		}

		// Test if the data was read, if not initialize it.
		try{
			userList.size();
		}catch(NullPointerException e) {
			userList = createUserList();
		}

	}

}
