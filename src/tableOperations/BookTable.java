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
public class BookTable
{
    // command line reader 
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    private Connection con;

    /*
     * constructs login window and loads JDBC driver
     */ 
    public BookTable(Connection con)
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
     * inserts a book
     */ 
    public void insertBook(String callNumber, String isbn,	String title, String mainAuthor, String publisher, int year)
    {
		PreparedStatement  ps;
		  
		try
		{
		  ps = con.prepareStatement("INSERT INTO book VALUES (?,?,?,?,?,?)");

		  ps.setString(1, callNumber);
		  ps.setString(2, isbn);	  
		  ps.setString(3, title);		  
		  ps.setString(4, mainAuthor);
		  ps.setString(5, publisher); 
		  ps.setInt(6, year);
	
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
     * deletes a book by Call Number
     */ 
    public void deleteBook(String callNumber)
    {
		  
		try
		{
			Statement stmt = con.createStatement();
			int rowCount = stmt.executeUpdate("DELETE FROM book WHERE callNumber = " + callNumber);

		
			if (rowCount == 0)
			{
				System.out.println("\nCall Number " + callNumber + " does not exist!");
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
     * updates the title of a book
     */ 
    public void updateBookTitle(String callNumber, String title)
    {

	PreparedStatement  	ps;
	  
	try
	{
	  ps = con.prepareStatement("UPDATE book SET title = ? WHERE callNumber = ?");

	  ps.setString(2, callNumber);
	  ps.setString(1, title);

	  int rowCount = ps.executeUpdate();
	  if (rowCount == 0)
	  {
	      System.out.println("\nCall Number " + callNumber + " does not exist!");
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
    public void showBook()
    {
	String     callNumber;
	String     isbn;
	String     title;
	String     mainAuthor;
	String     publisher;
	int		   year;
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

	      // simplified output formatting; truncation may occur

	      callNumber = rs.getString("callNumber");
	      System.out.printf("%-20.20s", callNumber);

	      isbn = rs.getString("isbn");
	      System.out.printf("%-20.20s", isbn);

	      title = rs.getString("title");
	      System.out.printf("%-40.40s", title);

	      mainAuthor = rs.getString("mainAuthor");
	      System.out.printf("%-20.20s", mainAuthor);

	      publisher = rs.getString("publisher");
	      System.out.printf("%-20.20s", publisher);
	      
	      year = rs.getInt("year");
	      System.out.printf("%-20.20d", year);
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

