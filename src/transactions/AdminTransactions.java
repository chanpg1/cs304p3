package transactions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import tableOperations.*;

//We need to import the java.sql package to use JDBC
import java.sql.*;
import java.util.Calendar;

import javax.swing.JOptionPane;

public class AdminTransactions {	
	
	private Connection con;
	private BookTable bookTable;
	private BookCopyTable bookCopyTable; 
	private BorrowerTable borrowerTable;
	private BorrowerTypeTable borrowerTypeTable;
	private BorrowingTable borrowingTable;
	private FineTable fineTable;
	private HasAuthorTable hasAuthorTable;
	private HasSubjectTable hasSubjectTable;
	private HoldRequestTable holdRequestTable;
	
	public AdminTransactions(Connection con){
		this.con = con;
		bookTable = new BookTable(con);
		bookCopyTable = new BookCopyTable(con); 
		borrowerTable = new BorrowerTable(con);
		borrowerTypeTable = new BorrowerTypeTable(con);
		borrowingTable = new BorrowingTable(con);
		fineTable = new FineTable(con);
		hasAuthorTable = new HasAuthorTable(con);
		hasSubjectTable = new HasSubjectTable(con);
		holdRequestTable = new HoldRequestTable(con);		
	}
	
	//Create ALL tables and sequences in database
	public boolean createAllTblAndSeq(){
		try{
			Statement stmt = con.createStatement();
			//Create Tables
			stmt.execute("create table BorrowerType	(type varchar(8) not null PRIMARY KEY,	bookTimeLimit int not null ) ");
			stmt.execute("grant select on BorrowerType to public"); 							
			
			stmt.execute("create table Borrower	(bid number(10) not null PRIMARY KEY, " +
								"password varchar(20) not null, name varchar(40) not null, " +
								"address varchar(50), phone number(10),	emailAddress varchar(30), " +
								"sinOrStNo number(9) not null UNIQUE, expiryDate date not null, " +	
								"type varchar(8) not null, foreign key (type) references BorrowerType)");
			stmt.execute("grant select on Borrower to public");
			
			stmt.execute("create table Book (callNumber varchar(20) not null PRIMARY KEY, " +
								"isbn varchar(13) not null UNIQUE, title varchar(40) not null, mainAuthor varchar(40) not null, " +
								"publisher varchar(40) not null, year number(4) not null)");
			stmt.execute("grant select on Book to public");
			
			stmt.execute("create table HasAuthor (callNumber varchar(20) not null, name varchar(40) not null, " +
							"PRIMARY KEY (callNumber, name), foreign key (callNumber) references Book)");
			stmt.execute("grant select on HasAuthor to public");
  
			stmt.execute("create table HasSubject (callNumber varchar(20) not null,	subject varchar(20) not null, " +
					"PRIMARY KEY (callNumber, subject),	foreign key (callNumber) references Book)");
			stmt.execute("grant select on HasSubject to public");
			
			stmt.execute("create table BookCopy	(callNumber varchar(20) not null, copyNo number(3) not null, " +
					"status varchar(7) not null, PRIMARY KEY (callNumber, copyNo), foreign key (callNumber) references Book)");
			stmt.execute("grant select on BookCopy to public");
  
			stmt.execute("create table HoldRequest (hid number(10) not null PRIMARY KEY, bid number(10) not null, " +
						"callNumber varchar(20) not null, issuedDate date not null,	" +
						"foreign key (bid) references Borrower,	foreign key (callNumber) references Book)");
			stmt.execute("grant select on HoldRequest to public");
 
			stmt.execute("create table Borrowing (borid number(15) not null PRIMARY KEY, bid number(10) not null, " +
						"callNumber varchar(20) not null, copyNo number(3) not null, outDate date not null, " +
						"inDate date, foreign key (bid) references Borrower, " +
						"foreign key (callNumber, copyNo) references BookCopy(callNumber, copyNo))");
			stmt.execute("grant select on Borrowing to public");

			stmt.execute("create table Fine (fid number(10) not null PRIMARY KEY, amount float not null, " +
						"issueDate date not null, paidDate date , borid number(15) not null, " +
						"foreign key (borid) references Borrowing)");
			stmt.execute("grant select on Fine to public");
			
			//Create Sequences
			stmt.execute("CREATE SEQUENCE bid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			stmt.execute("CREATE SEQUENCE hid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			stmt.execute("CREATE SEQUENCE borid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			stmt.execute("CREATE SEQUENCE fid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			
			con.commit();
			System.out.println("Tables and Sequences created");
			stmt.close();
			return true;			
		}
		catch (SQLException ex) {
			System.out.println("Message: " + ex.getMessage());		
			JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Creating Tables and Sequences", JOptionPane.ERROR_MESSAGE);
		    try 
		    {
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
		    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Rolling Back Changes", JOptionPane.ERROR_MESSAGE);
		    	System.out.println("Message: " + ex2.getMessage());
		    	ex2.printStackTrace();
		    	System.exit(-1);		
		    }
		}
		return false;		
	}
	
	//Clear all tables
	public boolean clearAllTables(){

		try{
			Statement stmt = con.createStatement();
			//Clear Tables
			stmt.execute("delete Fine");
			stmt.execute("delete Borrowing");
			stmt.execute("delete HoldRequest");
			stmt.execute("delete BookCopy");
			stmt.execute("delete HasSubject");
			stmt.execute("delete HasAuthor");
			stmt.execute("delete Book");
			stmt.execute("delete Borrower");
			stmt.execute("delete BorrowerType");
			
			con.commit();
			stmt.close();
			return true;
		}
		catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Clearing Tables", JOptionPane.ERROR_MESSAGE);
			System.out.println("Message: " + ex.getMessage());
			ex.printStackTrace();
			try 
		    {
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
		    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Rolling Back Changes", JOptionPane.ERROR_MESSAGE);
		    	System.out.println("Message: " + ex2.getMessage());
		    	ex2.printStackTrace();
		    	System.exit(-1);		
		    }
		}			
		return false;
	}
	
	//Reset all sequences
	public boolean resetAllSequences(){

		try{
			Statement stmt = con.createStatement();
			//reset the sequences
			stmt.execute("DROP SEQUENCE bid_counter");
			stmt.execute("DROP SEQUENCE hid_counter");
			stmt.execute("DROP SEQUENCE borid_counter");
			stmt.execute("DROP SEQUENCE fid_counter");
			stmt.execute("CREATE SEQUENCE bid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			stmt.execute("CREATE SEQUENCE hid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			stmt.execute("CREATE SEQUENCE borid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			stmt.execute("CREATE SEQUENCE fid_counter START WITH 0 INCREMENT BY 1 MINVALUE 0 NOCYCLE");
			
			con.commit();
			stmt.close();
			return true;
		}
		catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Reseting Sequences", JOptionPane.ERROR_MESSAGE);
			System.out.println("Message: " + ex.getMessage());
			ex.printStackTrace();
			try 
		    {
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
		    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Rolling Back Changes", JOptionPane.ERROR_MESSAGE);
		    	System.out.println("Message: " + ex2.getMessage());
		    	ex2.printStackTrace();
		    	System.exit(-1);		
		    }
		}			
		return false;
	}

	//Remove all tables and sequences in database
	public boolean dropAllTblAndSeq(){

		try{
			
			Statement stmt = con.createStatement();
			//clear the tables
			this.clearAllTables();
			//drop the tables
			stmt.execute("drop table Fine");
			stmt.execute("drop table Borrowing");
			stmt.execute("drop table HoldRequest");
			stmt.execute("drop table BookCopy");
			stmt.execute("drop table HasSubject");
			stmt.execute("drop table HasAuthor");
			stmt.execute("drop table Book");
			stmt.execute("drop table Borrower");
			stmt.execute("drop table BorrowerType");
			//drop the sequences
			stmt.execute("DROP SEQUENCE bid_counter");
			stmt.execute("DROP SEQUENCE hid_counter");
			stmt.execute("DROP SEQUENCE borid_counter");
			stmt.execute("DROP SEQUENCE fid_counter");
			
			con.commit();
			stmt.close();
			return true;
		}
		catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Reseting Sequences", JOptionPane.ERROR_MESSAGE);
			System.out.println("Message: " + ex.getMessage());
			ex.printStackTrace();
			try 
		    {
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
		    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Rolling Back Changes", JOptionPane.ERROR_MESSAGE);
		    	System.out.println("Message: " + ex2.getMessage());
		    	ex2.printStackTrace();
		    	System.exit(-1);		
		    }
		}			
		return false;
	}
	
	//Seed all tables in database
	public boolean seedAllTables(int numBorrowers, int numBooks){
		//Clear tables and reset sequences to avoid conflicts
		resetAllSequences();
		clearAllTables();
		try{
			
			Statement stmt = con.createStatement();
			String[] firstnames = {"Greg","Devin","David","Shao","Kristal","Kirk","Mandy","George","Wei","Frank","Niko","Andrei","Christine"};			
			String[] lastnames = {"Mendeleev","Wong","Chan","White","Smith","Mundy","Lenin","Trotsky","Liddle","Erhoff","Zeng","Honda","Kim"};
			String[] subjects = {"Geography","Science","Health","Leisure","Fiction","Non-fiction","Literature","CS304","Databases","SQL"};
			String[] borrowerTypes = {"student","staff","faculty","student","student","student"}; //skew array so that student is selected more often 
			int numFirstNames = firstnames.length;
			int numLastNames = lastnames.length;
			int numSubjects = subjects.length;
			int numBorrowerTypes = borrowerTypes.length;
			//Min + (int)(Math.random() * ((Max - Min) + 1))  --> +1 means will include max
			
			//insert the 3 borrower types						
			stmt.execute("INSERT INTO BorrowerType VALUES ('student', 14)");
			stmt.execute("INSERT INTO BorrowerType VALUES ('faculty', 84)");
			stmt.execute("INSERT INTO BorrowerType VALUES ('staff', 42)");
			con.commit();
			
			//Add special borrower
			Calendar tempCal = Calendar.getInstance();
			tempCal.add(Calendar.YEAR, 2);
			java.sql.Date tempExpDate = new java.sql.Date(tempCal.getTimeInMillis());
			long tempPhone = new Long("6042573830");
			this.borrowerTable.insertBorrower("99414", "Jackie Chan", "210 Mock Street", tempPhone, "jackie@chan.com", 123321123, tempExpDate, "student");
			
			//insert borrowers			
			for(int i = 0; i<numBorrowers; i++){
				//Randomly generate password, name, address, phone, emailAddress, sinOrStNo, expiryDate, and type
				int fnIndex = 0 + (int)(Math.random() * ((numFirstNames - 0)));
				int lnIndex = 0 + (int)(Math.random() * ((numLastNames - 0)));
				int typeIndex = 0 + (int)(Math.random() * ((numBorrowerTypes - 0)));
				String password = ""+(25000 + (int)(Math.random() * ((1000000 - 25000))));
				String name = firstnames[fnIndex] + " " + lastnames[lnIndex];
				String address = (0 + (int)(Math.random() * ((120 - 0))))+" "+lastnames[fnIndex]+" Street";
				long phone = 1000000000 + (int)(Math.random() * ((1999999999 -1000000000)));
				String emailAddress = firstnames[fnIndex]+"@"+lastnames[lnIndex]+".com";
				int sinOrStNo = 100000000 + (int)(Math.random() * ((999999999 -100000000)));
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, 2);
				java.sql.Date expiryDate = new java.sql.Date(cal.getTimeInMillis());
				String type = borrowerTypes[typeIndex];
				this.borrowerTable.insertBorrower(password, name, address, phone, emailAddress, sinOrStNo, expiryDate, type);
			}
			
			//insert books
			String callNumber = null; //to re-use later
			for(int i = 0; i<numBooks; i++){
				//Randomly generate author, subject, other author, isbn, title, year, callNumber
				int fnIndex = 0 + (int)(Math.random() * ((numFirstNames - 0)));
				int lnIndex = 0 + (int)(Math.random() * ((numLastNames - 0)));
				int subjIndex = 0 + (int)(Math.random() * ((numSubjects - 0)));
				String mainAuthor = firstnames[fnIndex] + " " + lastnames[lnIndex];
				String subject = subjects[subjIndex];
				String otherAuthor = firstnames[lnIndex] + " " + lastnames[fnIndex];
				String isbn = ""+(0 + (int)(Math.random() * ((10000000 - 0))));
				String title = "CS304 Textbook "+i;
				String publisher = mainAuthor + " Press";
				int year = 1900 + (int)(Math.random() * ((2013 - 1900)));
				callNumber = ""+fnIndex+lnIndex+isbn;
				
				//Insert book, book copy, authors, and subject
				this.bookTable.insertBook(callNumber, isbn, title, mainAuthor, publisher, year);
				this.bookCopyTable.insertBookCopy(callNumber, 0, "in");
				this.hasAuthorTable.insertHasAuthor(callNumber, mainAuthor);
				this.hasAuthorTable.insertHasAuthor(callNumber, otherAuthor);
				this.hasSubjectTable.insertHasSubject(callNumber, subject);
				//determine number of additional copies for the book to add
				int numExtraCopies = 0 + (int)(Math.random() * ((5 - 0)));
				for(int j = 0; j < numExtraCopies; j++){
					this.bookCopyTable.insertBookCopy(callNumber, j+1, "in");
				}
				//determine if a borrower should borrow book
				int borrow= 0 + (int)(Math.random() * ((2 - 0)));				
				if(borrow == 1){
					int bid = 0 + (int)(Math.random() * ((numBorrowers - 0)+1));
					Calendar cal = Calendar.getInstance();
					int days_borrowed = 0 + (int)(Math.random() * ((30 - 0)+1));
					cal.add(Calendar.DATE, (-1*days_borrowed));
					java.sql.Date outDate = new java.sql.Date(cal.getTimeInMillis());
					this.borrowingTable.insertBorrowing(bid, callNumber, 0, outDate);
					this.bookCopyTable.updateBookCopyStatus(callNumber, 0, "out");
				}
				//determine if someone should place a hold on the book
				int hold= 0 + (int)(Math.random() * ((2 - 0)));
				if(borrow == 1 && hold == 1 && numExtraCopies == 0){
					int bid = 0 + (int)(Math.random() * ((numBorrowers - 0)+1));
					Calendar cal = Calendar.getInstance();
					java.sql.Date issuedDate = new java.sql.Date(cal.getTimeInMillis());
					this.holdRequestTable.insertHoldRequest(bid, callNumber, issuedDate);
				}
			}
			
			//Insert fines
			
			Calendar fineCal = Calendar.getInstance();
			fineCal.add(Calendar.YEAR, -1);
			java.sql.Date fineIssueDate = new java.sql.Date(fineCal.getTimeInMillis());			
			//this.borrowingTable.insertBorrowing(0, callNumber, 0, fineIssueDate);				
			this.fineTable.insertFine(59, fineIssueDate, 0);
			fineCal = Calendar.getInstance();
			fineCal.add(Calendar.YEAR, -2);
			fineIssueDate = new java.sql.Date(fineCal.getTimeInMillis());
			//this.borrowingTable.insertBorrowing(0, callNumber, 0, fineIssueDate);	
			this.fineTable.insertFine(24, fineIssueDate, 0);			
			
			con.commit();
			stmt.close();
			return true;
		}
		catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Reseting Sequences", JOptionPane.ERROR_MESSAGE);
			System.out.println("Message: " + ex.getMessage());
			ex.printStackTrace();
			try 
		    {
			con.rollback();	
		    }
		    catch (SQLException ex2)
		    {
		    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Rolling Back Changes", JOptionPane.ERROR_MESSAGE);
		    	System.out.println("Message: " + ex2.getMessage());
		    	ex2.printStackTrace();
		    	System.exit(-1);		
		    }
		}			
		return false;
	}
}
