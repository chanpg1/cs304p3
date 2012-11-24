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
public class HoldRequestTable
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    /*
     * constructs login window and loads JDBC driver
     */ 
    public HoldRequestTable(Connection con)
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
     * inserts a hold request
     */ 
 	public void insertHoldRequest(int bid, String callNumber, Date issuedDate)
 	{ 	 		 
 		 		
 		PreparedStatement ps;

 		try
 		{
 			ps = con.prepareStatement("INSERT INTO HoldRequest VALUES (hid_counter.nextval,?,?,?)");

 			ps.setInt(1, bid);
 			ps.setString(2, callNumber);
 			ps.setDate(3, issuedDate);

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
     * deletes an holdRequest by hid number
     */ 
    public void deleteHoldRequest(int hid)
    {
		  
		try
		{
			Statement stmt = con.createStatement();
			int rowCount = stmt.executeUpdate("DELETE FROM holdRequest WHERE hid = " + hid);
		
			if (rowCount == 0)
			{
				System.out.println("\nhid " + hid + " does not exist!");
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
     * display information about HoldRequest
     */ 
    public void showHoldREquest()
    {    	

		int hid;
		int bid;
		String callNumber;
		Date issuedDate;
		Statement  stmt;
		ResultSet  rs;
	   
		try
		{
		  stmt = con.createStatement();
	
		  rs = stmt.executeQuery("select * from holdRequest");
	
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

		      hid = rs.getInt("hid");
		      System.out.printf("%-20.20d", hid);
		      
		      bid = rs.getInt("bid");
		      System.out.printf("%-20.20d", bid);
		      
		      callNumber = rs.getString("callNumber");
		      System.out.printf("%-20.20s", callNumber);
		      
		      issuedDate = rs.getDate("issuedDate");
		      System.out.printf("%-20.20dMy", issuedDate);	
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

