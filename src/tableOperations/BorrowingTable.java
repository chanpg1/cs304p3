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
public class BorrowingTable
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    /*
     * constructs login window and loads JDBC driver
     */ 
    public BorrowingTable(Connection con)
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
     * inserts a borrowing
     */ 
 	public void insertBorrowing(int bid, String callNumber, int copyNo, Date outDate)
 	{ 	

 		PreparedStatement ps;

 		try
 		{
 			ps = con.prepareStatement("INSERT INTO Borrowing VALUES (borid_counter.nextval,?,?,?,?,NULL)");

 			ps.setInt(1, bid);
 			ps.setString(2, callNumber);
 			ps.setInt(3, copyNo);
 			ps.setDate(4, outDate);

 			ps.executeUpdate();

 			// commit work 
 			con.commit();

 			ps.close();
 		}
 		catch (SQLException ex)
 		{
 			System.out.println("Message: " + ex.getMessage());
 			ex.printStackTrace();
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
     * deletes a borrower type by type
     */ 
    public void deleteBorrowing(int borid)
    {
		  
		try
		{
			Statement stmt = con.createStatement();
			int rowCount = stmt.executeUpdate("DELETE FROM borrowing WHERE borid = " + borid);
		
			if (rowCount == 0)
			{
				System.out.println("\nborid " + borid + " does not exist!");
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
     * display information about borrower type
     */ 
    public void showBorrowing()
    {    	    	
	 	int borid;
		int bid;
		String callNumber;
		int copyNo;
		Date outDate;
		Date inDate;

		Statement  stmt;
		ResultSet  rs;
	   
		try
		{
		  stmt = con.createStatement();
	
		  rs = stmt.executeQuery("select * from borrowing");
	
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
		      borid = rs.getInt("borid");
		      System.out.printf("%-20.20d", borid);
		      
		      bid = rs.getInt("bid");
		      System.out.printf("%-20.20d", bid);
		      
		      callNumber = rs.getString("callNumber");
		      System.out.printf("%-20.20s", callNumber);
		      
		      copyNo = rs.getInt("copyNo");
		      System.out.printf("%-20.20d", copyNo);
		      
		      outDate = rs.getDate("outDate");
		      System.out.printf("%-20.20dMy", outDate);
		      
		      inDate = rs.getDate("inDate");
		      System.out.printf("%-20.20dMy", inDate);	
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

