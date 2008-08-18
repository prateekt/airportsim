//
//  DBTest.java
//  FAA_Control
//
//  Created by Riley Marsh on 10/30/07.
//  Copyright 2007 Riley Marsh. All rights reserved.
//
package tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBTest {
	
	public static void main(String[] args){
		String driver = "com.mysql.jdbc.Driver";
        String host = "10.0.1.201";
        String dbName = "faacontrol";
		
        String url = "jdbc:mysql://" + host + "/" + dbName;
		
        String username = "csci201";
        String password = "csci201";
		
		try {
			System.out.println("driver: "+driver);
			System.out.println("url: "+url);
			System.out.println("username: "+username);
			System.out.println("password: "+password);
			Class.forName(driver);
			Connection connection =
				DriverManager.getConnection(url, username, password);
			System.out.println("Connected Successfully");
		} catch(ClassNotFoundException cnfe) {
			System.out.println("Error loading driver: " + cnfe);
		} catch(SQLException sqle) {
			System.out.println("Error connecting: " + sqle);
		} 
	}

		
		
}
