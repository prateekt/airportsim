//
//  UserSchema.java
//  FAA_Control
//
//  Created by Riley Marsh on 10/30/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//
package airport;
import airport.objects.*;
import java.util.*;
import java.io.*;

public class UserSchema implements Serializable{
	int userID;
	String username;
	String password;
	String firstName;
	String lastName;
	ArrayList<String> positions;

	public UserSchema(){}
	public UserSchema(int userID, String username, String password, String firstName, String lastName){
		this.userID = userID;
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public UserSchema(int userID, String username, String password, String firstName, String lastName, ArrayList<String> positions){
		this.userID = userID;
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.positions = positions;
	}
	public void setUsername(String username){
		this.username = username;
	}
	public void setFirstName(String firstName){
		this.firstName = firstName;
	}
	public void setLastName(String lastName){
		this.lastName = lastName;
	}
	public void setPassword(String password){
		this.password = password;
	}
	public void setUserID(int userID){
		this.userID = userID;
	}
	public void setUserPositions(ArrayList<String> positions){
		this.positions = positions;
	}

	public int getUserID(){ return userID; }
	public String getUsername(){ return username; }
	public String getPassword(){ return password; }
	public String getFirstName(){ return firstName; }
	public String getLastName(){ return lastName; }
	public ArrayList<String> getUserPositions(){ return positions;}

	public boolean equals(UserSchema user){
		if(user.getUsername() == this.username)
			return true;
		return false;
	}


}
