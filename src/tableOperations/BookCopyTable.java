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
public class BookCopyTable 
{

    private Connection con;
    /*
     * constructs login window and loads JDBC driver
     */ 
    public BookCopyTable(Connection con)
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
     * inserts a BookCopy
     */ 
    public void insertBookCopy(String callNumber, int copyNo, String status)
    {
		PreparedStatement  ps;
		  
		try
		{
		  ps = con.prepareStatement("INSERT INTO bookcopy VALUES (?,?,?)");
	
		  ps.setString(1, callNumber);
		  ps.setInt(2, copyNo);
		  ps.setString(3, status);
	
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
     * deletes a branch
     */ 
    public void deleteBookCopy(String callNumber, int copyNo)
    {
    	PreparedStatement  	ps;
	  
	try
	{
	  ps = con.prepareStatement("DELETE FROM bookcopy WHERE callNumber = ? AND copyNo = ?");

	  ps.setString(1, callNumber);
	  ps.setInt(2, copyNo);

	  int rowCount = ps.executeUpdate();

	  if (rowCount == 0)
	  {
		  System.out.println("\nBook with callNumber " + callNumber + "and copy number" + copyNo + " does not exist!");
	  }

	  con.commit();

	  ps.close();
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
     * updates the status of a Book copy
     */ 
    public void updateBookCopyStatus(String callNumber, int copyNo, String status)
    {
	
		PreparedStatement  ps;
		  
		try
		{
		  ps = con.prepareStatement("UPDATE bookcopy SET status = ? WHERE callNumber = ? AND copyNo = ?");
	
		  ps.setString(2, callNumber);
		  ps.setInt(3, copyNo);
		  ps.setString(1, status);
		  
		  int rowCount = ps.executeUpdate();
		  if (rowCount == 0)
		  {
		      System.out.println("\nBook with callNumber " + callNumber + "and copy number" + copyNo + " does not exist!");
		  }
	
		  con.commit();
	
		  ps.close();
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
    public void showBookCopy()
    {
	String				callNumber;			
	int					copyNo;
	String				status;
	Statement  stmt;
	ResultSet  rs;
	   
	try
	{
	  stmt = con.createStatement();

	  rs = stmt.executeQuery("SELECT * FROM bookcopy");

	  // get info on ResultSet
	  ResultSetMetaData rsmd = rs.getMetaData();

	  // get number of columns
	  int numCols = rsmd.getColumnCount();

	  System.out.println(" ");
	  
	  // display column names;
	  for (int i = 0; i < numCols; i++)
	  {
	      // get column name and print it

	      System.out.printf("%-15s", rsmd.getColumnName(i+1));    
	  }

	  System.out.println(" ");

	  while(rs.next())
	  {
	      // for display purposes get everything from Oracle 
	      // as a string

	      // simplified output formatting; truncation may occur

	      callNumber = rs.getString("callNumber");
	      System.out.printf("%-15.15s", callNumber);

	      copyNo = rs.getInt("copyNo");
	      System.out.printf("%-15.20d", copyNo);

	      status = rs.getString("status");
	      System.out.printf("%-15.15s", status);
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

