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
public class HasAuthorTable
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    /*
     * constructs login window and loads JDBC driver
     */ 
    public HasAuthorTable(Connection con)
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
     * inserts a has author entry
     */ 
 	public void insertHasAuthor(String callNumber, String name)
 	{ 	
 		
 		PreparedStatement ps;

 		try
 		{
 			ps = con.prepareStatement("INSERT INTO HasAuthor VALUES (?,?)");

 			ps.setString(1, callNumber);
 			ps.setString(2, name);

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
     * deletes an hasAuthor entry by callNumber and author
     */ 
    public void deleteHasAuthor(String callNumber, String name)
    {
		  
		try
		{
			Statement stmt = con.createStatement();
			int rowCount = stmt.executeUpdate("DELETE FROM HasAuthor WHERE callNumber = " + callNumber + " AND name = " + name);
		
			if (rowCount == 0)
			{
				System.out.println("\nAuthor " + name + " for call number " + callNumber + " does not exist!");
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
    public void showHasAuthor()
    {    	
		String callNumber;
		String name; 

		Statement  stmt;
		ResultSet  rs;
	   
		try
		{
		  stmt = con.createStatement();
	
		  rs = stmt.executeQuery("select * from hasAuthor");
	
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
	
		      callNumber = rs.getString("callNumber");
		      System.out.printf("%-20.20s", callNumber);
		      
		      name = rs.getString("name");
		      System.out.printf("%-20.20s", name);	
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

