package airport;
/*
	Writen By: Eric Quillen
*/

import java.util.*;
import java.lang.*;

// This class stores Personnel data.
/** Stores all info pertaining to an employee.
*	@author Eric Quillen
*/
public class Personnel implements java.io.Serializable{

	// Begin Variable Declarations

	private String firstName;				// Employee's first name.
	private String lastName;				// Employee's last name.
	private String email;					// Employee's email.
	private ArrayList<String> positions;	// Employee's Tower Cab positions.
	private String userID;					// Employee's User Id for system.
	private char[] password;				// Employee's password for system.

	// End Variable Declarations

	// Empty Constuctor
	/** Empty Constructor
	*/
	public Personnel()
	{
	}

	// Constuctor
	/** Full Constructor
	*	@param first Employee's first name.
	*	@param last Employee's last name.
	*	@param mail Employee's email.
	*	@param pos Employee's Tower Cab positions.
	*	@param ID Employee's User Id for system.
	*	@param pass Employee's password for system.
	*/
	public Personnel(String first, String last, String mail, ArrayList<String> pos, String ID, char[] pass)
	{
		setFirstName(first);
		setLastName(last);
		setEmail(mail);
		setPositions(pos);
		setUserID(ID);
		setPassword(pass);
	}

	// Acessors / Mutators
	/** First Name Mutator
	*	@param x employees First Name.
	*/
	public void setFirstName(String x)
	{
		firstName = x;
	}

	/** First Name Acessor
	*	@return firstName Employee's Frist Name.
	*/
	public String getFirstName()
	{
		return firstName;
	}


	/**	Last Name Mutator
	*	@param x employees Last Name.
	*/
	public void setLastName(String x)
	{
		lastName = x;
	}

	/** Last Name Acessor
	*	@return lastName Employee's Last Name.
	*/
	public String getLastName()
	{
		return lastName;
	}


	/**	Email Mutator
	*	@param x employees email.
	*/
	public void setEmail(String x)
	{
		email = x;
	}

	/** Email Acessor
	*	@return email Employee's Email adress.
	*/
	public String getEmail()
	{
		return email;
	}


	/**	Positions Mutator
	*	@param x List of employee Positions.
	*/
	public void setPositions(ArrayList<String> x)
	{
		positions = (ArrayList<String>)x.clone();
	}

	/** Position Acessor
	*	@return positiions Employee's hired positions.
	*/
	public ArrayList<String> getPositions()
	{
		return positions;
	}


	/**	UserID Mutator
	*	@param x employees UserID.
	*/
	public void setUserID(String x)
	{
		userID = x;
	}

	/** UserID Acessor
	*	@return userID Employee's UserID.
	*/
	public String getUserID()
	{
		return userID;
	}


	/**	Password Mutator
	*	@param x employees Password.
	*/
	public void setPassword(char[] x)
	{
		password = x.clone();
	}

	/** Password Acessor
	*	@return password Employee's password.
	*/
	public char[] getPassword()
	{
		return password;
	}


	/**	Gets the employees full name by last then first.
	*	@return x employees Full Name.
	*/
	public String getFullName()
	{
		String x = (getLastName() + " " + getFirstName());
		return x;
	}

	/** prints all employee info to the terminal.
	*/
	public void printEmployees()
	{
		System.out.print(((char) 27) + "[2J");
		System.out.print(((char) 27) + "[0;0H");
		System.out.println("Name: " + getFirstName() + " " + getLastName());
		System.out.println("User ID: " + getUserID());
		System.out.println("Password: " + Arrays.toString(getPassword()));
		System.out.println("Email: " + getEmail());
		System.out.println("Positions: " + getPositions() + "\n\n\n");
	}
}
