package transactions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import tableOperations.*;

//We need to import the java.sql package to use JDBC
import java.sql.*;

public class LibrarianTransactions {	
	
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
	
	public LibrarianTransactions(Connection con){
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
	
	//Adds a new book or a copy for an existing book to the library. 
	//The librarian provides the information for the new book, and the system adds it to the library.
	public boolean addNewBook(String callNumber, String isbn,	String title, String mainAuthor, String publisher, int year, String subjects, String otherAuthors){
		try{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");	
			ps.setString(1, callNumber);
			ResultSet rs = ps.executeQuery();
			//if book with same callNumber present
			if(rs.next()){
				//if exact match with existing titles, authors, etc., add a new book copy
				if(rs.getString("isbn").equals(isbn) && rs.getString("title").equals(title) && 
						rs.getString("mainAuthor").equals(mainAuthor) && rs.getString("publisher").equals(publisher) && rs.getInt("year")==year){
					//browse book copy table to find max copy number for this book
					ps = con.prepareStatement("SELECT * FROM bookCopy WHERE callNumber = ?");	
					ps.setString(1, callNumber);
					ResultSet rs2 = ps.executeQuery();					
					int maxCopyNo = 0;
					int currNum;
					while(rs2.next()){
						currNum = rs2.getInt("copyNo");
						if(currNum > maxCopyNo){
							maxCopyNo = currNum + 1;
						}
					}
					this.bookCopyTable.insertBookCopy(callNumber, maxCopyNo + 1 , "in");
					return true;
				}
				//if not matching with existing titles, author, etc., return false
				else{
					System.out.println("Book matches existing call number, but mismatches with other details.");
					System.out.println("Please try again");
					return false;
				}
			}
			//if callNumber is new, insert new book in Book, BookCopy, hasAuthor and hasSubject
			else
			{
				this.bookTable.insertBook(callNumber, isbn, title, mainAuthor, publisher, year);
				//insert into bookCopy
				this.bookCopyTable.insertBookCopy(callNumber, 0, "in");
				//insert main author into hasAuthor
				this.hasAuthorTable.insertHasAuthor(callNumber, mainAuthor);
				//if other authors also present, insert those too into hasAuthor
				if(otherAuthors.length() > 0){
					String[] authors = otherAuthors.split("\\s*;\\s*"); //delimit otherAuthors by semicolon
					for(int i = 0; i < authors.length; i++){
						this.hasAuthorTable.insertHasAuthor(callNumber, authors[i]);
					}
					
				}			
				//insert to hasSubjects, if user input any
				if(subjects.length() > 0){
					String[] subjs = subjects.split("\\s*;\\s*"); //delimit subjects by semicolon
					for(int i = 0; i < subjs.length; i++){
						this.hasSubjectTable.insertHasSubject(callNumber, subjs[i]);
					}
					
				}
				return true;
			}
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}
		return false;		
	}
	
	/*
	 * Generate a report of all books that are on loan. Filters by subject if it is provided
	 * For each book the report returns the callNum, the check-out date, the borid, the title, and the time limit for that loan in days   
	 */
	public ResultSet generateBorrowingsReport(String subject){
		ResultSet rs = null;
		PreparedStatement ps;
		try{
			if(subject.length()==0){
				ps = con.prepareStatement("SELECT Bk.callnumber AS callNum, title, Bcp.copyno, borid, outdate AS Out, Btype.booktimelimit " +
											"FROM borrowing Bwg, book Bk, borrower Bwr, borrowertype Btype, bookcopy Bcp " +
											"WHERE Bcp.status = 'out' AND Bcp.copyno = Bwg.copyno AND Bcp.callnumber = Bwg.callnumber AND Bwg.bid = Bwr.bid AND Bk.callNumber = Bwg.callNumber AND Bwg.indate IS NULL AND Btype.type = Bwr.type " +
											"ORDER BY Bk.callnumber");
			}
			else{
				ps = con.prepareStatement("SELECT Bk.callnumber AS callNum, title, Bcp.copyno, borid, subject, outdate AS Out, Btype.booktimelimit " +
						"FROM borrowing Bwg, book Bk, borrower Bwr, borrowertype Btype, hassubject Subj, bookcopy Bcp " +
						"WHERE Bcp.status = 'out' AND Bcp.copyno = Bwg.copyno AND Bcp.callnumber = Bwg.callnumber AND Bwg.bid = Bwr.bid AND Bk.callNumber = Bwg.callNumber AND Bwg.indate IS NULL AND Btype.type = Bwr.type AND " +
						"UPPER(Subj.subject) LIKE '%" + subject.toUpperCase() + "%' AND Subj.callNumber = Bwg.callNumber " +
								"ORDER BY Bk.callnumber");
			}
			rs = ps.executeQuery();			
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}			
		return rs;
	}
	
	/*
	 * Generate a report with the most popular items in a given year.  
	 * The librarian provides a year and a number n. 
	 * The system lists out the top n books that where borrowed the most times during that year. 
	 * The books are ordered by the number of times they were borrowed.
	 */
	public ResultSet booksByPopularity(int topN, int year){
		ResultSet rs = null;
		PreparedStatement ps;
		try{
			//if year == -1, return top N books from all time
			if(year == -1){
				ps = con.prepareStatement("SELECT TopBooks.callnumber, bk2.title, numborrowings " +
						"FROM (SELECT Bk.callnumber, count(Bwg.borid) AS NUMBORROWINGS " +
								"FROM borrowing Bwg, book Bk " +
								"WHERE Bk.callNumber = Bwg.callNumber " +
								"GROUP BY Bk.callnumber " +
								"ORDER BY count(BWG.BORID) DESC) TopBooks, " +
								"book Bk2 " +
						"WHERE Bk2.callNumber = TopBooks.callNumber AND ROWNUM <= " + topN);	//take the top N rows
			}
			//otherwise, return top N books from year specified
			else{
				ps = con.prepareStatement("SELECT TopBooks.callnumber, bk2.title, numborrowings " +
						"FROM (SELECT Bk.callnumber, count(Bwg.borid) AS NUMBORROWINGS " +
								"FROM borrowing Bwg, book Bk " +
								"WHERE Bk.callNumber = Bwg.callNumber AND to_char(Bwg.outdate, 'yyyy') = '"+year+"' " +
								"GROUP BY Bk.callnumber " +
								"ORDER BY count(BWG.BORID) DESC) TopBooks, " +
								"book Bk2 " +
						"WHERE Bk2.callNumber = TopBooks.callNumber AND ROWNUM <= " + topN);	//take the top N rows	
			}
	
			rs = ps.executeQuery();			
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}			
		return rs;
	}
	
	//Gets list of all fines in the system
	public ResultSet showAllFine(){
		ResultSet rs=null;
		try {
			Statement stmt = con.createStatement();
			stmt.execute("alter session set nls_date_format='DD/MM/YYYY'");
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT F.fid, amount, TO_CHAR(issueDate, 'YYYY-MM-DD') AS Date_Issued, TO_CHAR(paidDate, 'YYYY-MM-DD') AS Date_Paid, Brr.bid, Bk.title, Bg.borid " +
										"FROM fine F, borrowing Bg, borrower Brr, book Bk " +
										"WHERE F.borid = Bg.borid AND Bg.bid = Brr.bid AND Bg.callNumber = Bk.callNumber " +
										"ORDER BY fid DESC", 
										ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = ps.executeQuery();	
			 
		} catch (SQLException e) {
			e.printStackTrace();
		}	

		return rs;
	}
	
	//Gets list of all books in the system
	public ResultSet showAllBooks(){
		ResultSet rs=null;
		try {				
			Statement stmt = con.createStatement();
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT * " +
										"FROM book ",
										ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = ps.executeQuery();	
			 
		} catch (SQLException e) {
			e.printStackTrace();
		}	

		return rs;
	}
	
	//Gets list of all borrowers in the system
	public ResultSet showAllBorrowers(){
		ResultSet rs=null;
		try {				
			Statement stmt = con.createStatement();
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT * " +
										"FROM borrower ",
										ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs = ps.executeQuery();	
			 
		} catch (SQLException e) {
			e.printStackTrace();
		}	

		return rs;
	}
}
