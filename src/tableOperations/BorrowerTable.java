package tableOperations;

// We need to import the java.sql package to use JDBC
import java.sql.*;

// for reading from the command line
import java.io.*;

// for the login window
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/*
 * This class implements a graphical login window and a simple text
 * interface for interacting with the branch table 
 */ 
public class BorrowerTable
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    /*
     * constructs login window and loads JDBC driver
     */ 
    public BorrowerTable(Connection con)
    {   	
      try 
      {
		// Load the Oracle JDBC driver
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		this.con = con;
		this.con.setAutoCommit(false);
      }
      catch (SQLException ex)
      {
		System.out.println("Message: " + ex.getMessage());
		System.exit(-1);
      }
      
    }

    /*
     * inserts a borrower
     */ 
 	public void insertBorrower(int bid, String password, String name, String address, int phone, String emailAddress, int sinOrStNo, Date expiryDate, String type)
 	{ 	
 		PreparedStatement ps;

 		try
 		{
 			ps = con.prepareStatement("INSERT INTO Borrower VALUES (?,?,?,?,?,?,?,?,?)");

 			ps.setInt(1, bid);
 			ps.setString(2, password);
 			ps.setString(3, name);
 			ps.setString(4, address);
 			ps.setInt(5, phone);
 			ps.setString(6, emailAddress); 		  
 			ps.setInt(7, sinOrStNo);
 			ps.setDate(8, expiryDate);

 			ps.executeUpdate();

 			// commit work 
 			con.commit();

 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			try 
 			{
 				// undo the insert
 				con.rollback();	
 			}
 			catch (SQLException ex2)
 			{
 				System.out.println("Message: " + ex2.getMessage());
 				System.exit(-1);
 			}
 		}
 	}
    
    /*
     * deletes a borrower by bid
     */ 
    public void deleteBook(int bid)
    {
		  
		try
		{
			Statement stmt = con.createStatement();
			int rowCount = stmt.executeUpdate("DELETE FROM borrower WHERE bid = " + bid);

		
			if (rowCount == 0)
			{
				System.out.println("\nBorrower ID " + bid + " does not exist!");
			}

			con.commit();
			stmt.close();

		}
		catch (SQLException ex)
		{
		    System.out.println("Message: " + ex.getMessage());
	
	            try 
		    {
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
			System.out.println("Message: " + ex2.getMessage());
			System.exit(-1);
		    }
		}
    }
    
    /*
     * display information about branches
     */ 
    public void showBook()
    {
    	int bid; 
		String password; 
		String name;
		String address;
		int phone;
		String emailAddress;
		int sinOrStNo;
		Date expiryDate;

		Statement  stmt;
		ResultSet  rs;
	   
		try
		{
		  stmt = con.createStatement();
	
		  rs = stmt.executeQuery("select * from book");
	
		  // get info on ResultSet
		  ResultSetMetaData rsmd = rs.getMetaData();
	
		  // get number of columns
		  int numCols = rsmd.getColumnCount();
	
		  System.out.println(" ");
		  
		  // display column names;
		  for (int i = 0; i < numCols; i++)
		  {
		      //special case for title column
			  if(i==2){
		    	  System.out.printf("%-40s", rsmd.getColumnName(i+1));    continue;
		      }
			  // get column name and print it
			  
		      System.out.printf("%-20s", rsmd.getColumnName(i+1));    
		  }
	
		  System.out.println(" ");
	
		  while(rs.next())
		  {
			  // for display purposes get everything from Oracle 
		      // as a string		
				PreparedStatement ps;
		      // simplified output formatting; truncation may occur
	
		      bid = rs.getInt("bid");
		      System.out.printf("%-20.20d", bid);
	
		      password = rs.getString("password");
		      System.out.printf("%-20.20s", password);
	
		      name = rs.getString("name");
		      System.out.printf("%-40.40s", name);
	
		      address = rs.getString("address");
		      System.out.printf("%-20.20s", address);
		      
		      phone = rs.getInt("phone");
		      System.out.printf("%-20.20d", phone);	
		      
		      emailAddress = rs.getString("emailAddress");
		      System.out.printf("%-20.20s", emailAddress);
		      
		      sinOrStNo = rs.getInt("sinOrStNo");
		      System.out.printf("%-20.20d", sinOrStNo);
		      
		      expiryDate = rs.getDate("expiryDate");
		      System.out.printf("%-20.20dMy", expiryDate);
		      System.out.println();
		  }
	 
		  // close the statement; 
		  // the ResultSet will also be closed
		  stmt.close();
		}
		catch (SQLException ex)
		{
		    System.out.println("Message: " + ex.getMessage());
		}	
    }
    
 
    public static void main(String args[])
    {
      
    }
}

