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
public class FineTable
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    /*
     * constructs login window and loads JDBC driver
     */ 
    public FineTable(Connection con)
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
     * inserts a fine
     */ 
 	public void insertFine(float amount, Date issueDate, int borid)
 	{ 	

 		PreparedStatement ps;

 		try
 		{
 			ps = con.prepareStatement("INSERT INTO fine VALUES (fid_counter.nextval,?,?,NULL,?)");

 			ps.setFloat(1, amount);
 			ps.setDate(2, issueDate);
 			ps.setInt(3, borid);
 			
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
     * deletes a fine by fid
     */ 
    public void deleteBorrowing(int fid)
    {
		  
		try
		{
			Statement stmt = con.createStatement();
			int rowCount = stmt.executeUpdate("DELETE FROM fine WHERE fid = " + fid);
		
			if (rowCount == 0)
			{
				System.out.println("\nfid " + fid + " does not exist!");
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
     * display information about fine table
     */ 
    public void showFine()
    {    	    	
 		int fid;
		float amount;
		Date issueDate;
		Date paidDate;
		int borid;

		Statement  stmt;
		ResultSet  rs;
	   
		try
		{
		  stmt = con.createStatement();
	
		  rs = stmt.executeQuery("select * from fine");
	
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
		      // simplified output formatting; truncation may occur

		      fid = rs.getInt("fid");
		      System.out.printf("%-20.20d", fid);
		      
		      amount = rs.getFloat("amount");
		      System.out.printf("%-20.20f", amount);
		      
		      issueDate = rs.getDate("issueDate");
		      System.out.printf("%-20.20dMy", issueDate);
		      
		      paidDate = rs.getDate("paidDate");
		      System.out.printf("%-20.20dMy", paidDate);		      		      
		      
		      borid = rs.getInt("borid");
		      System.out.printf("%-20.20d", borid);	
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

