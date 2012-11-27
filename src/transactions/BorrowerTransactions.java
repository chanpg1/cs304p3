package transactions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import tableOperations.*;

//We need to import the java.sql package to use JDBC
import java.sql.*;

//import the exceptions
import exceptions.*;

public class BorrowerTransactions {	
	
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
	
	public BorrowerTransactions(Connection con){
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
	
	/*
	 * Search for books using keyword search on titles, authors and subjects. 
	 * The result is a list of books that match the search (but not together with the number of copies that are in and out).
	 */
	public ResultSet findMatch(String category, String query) throws SQLException{
		PreparedStatement ps;
		String sqlInput;
		ResultSet rs;

		if(category.equals("Title")){
			sqlInput = "SELECT callNumber AS callnumber, title " +
						"FROM book " +
						"WHERE UPPER(title) LIKE '%"+query.toUpperCase()+"%'";
			/*
			inCopies = "SELECT BK1.callNumber AS callNumber, COUNT(BK1.callNumber) AS count, BC1.status AS status " +
						"FROM book BK1, bookCopy BC1 " +
						"WHERE BK1.callNumber = BC1.callNumber AND UPPER(BK1.title) LIKE '%"+query+"%' AND BC1.status = 'in' " +
						"GROUP BY BK1.callNumber, BC1.status";
			outCopies ="SELECT BK2.callNumber AS callNumber, COUNT(BK2.callNumber) AS count, BC2.status AS status " +
						"FROM book BK2, bookCopy BC2 " +
						"WHERE BK2.callNumber = BC2.callNumber AND UPPER(BK2.title) LIKE '%"+query+"%' AND BC2.status = 'out' " +
						"GROUP BY BK2.callNumber, BC2.status"; 
			ps = con.prepareStatement(inCopies, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rsIn = ps.executeQuery();
			ps = con.prepareStatement(outCopies, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rsOut = ps.executeQuery();
			*/
		}
		else if(category.equals("Author")){
			sqlInput = "SELECT DISTINCT BK1.callnumber AS callNumber, BK1.title AS title " +
						"FROM book BK1, hasAuthor HA1 " +
						"WHERE BK1.callNumber = HA1.callNumber AND UPPER(HA1.name) LIKE '%"+query.toUpperCase()+"%'";
		}
		else if(category.equals("Subject")){
			sqlInput = "SELECT DISTINCT BK1.callnumber AS callNumber, BK1.title AS title " +
						"FROM book BK1, hasSubject HS1 " +
						"WHERE BK1.callNumber = HS1.callNumber AND UPPER(HS1.subject) LIKE '%"+query.toUpperCase()+"%'";
		}
		else{
			//Search by title by default
			sqlInput = "SELECT callNumber AS callnumber, title " +
					"FROM book " +
					"WHERE UPPER(title) LIKE '%"+query.toUpperCase()+"%'";
		}
	
		ps = con.prepareStatement(sqlInput, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		//ps.setString(1, query.toUpperCase());
		rs = ps.executeQuery();
		return rs;

	}
	
	
	/*
	 * Place a hold request for a book that is out. 
	 * When the item is returned, the system sends an email to the borrower and 
	 * informs the library clerk to keep the book out of the shelves.
	 */
	//ASSUMPTION: NOT REQUESTING FOR A PARTICULAR COPYNO, GET FIRST AVAILABLE COPY OF BOOK
	public void holdOutBook(int bid, String callNumber) throws bookAvailableException, bookNotExistsException, bidNotExistsException{
		PreparedStatement ps;
		try {
			//Check if book is actually in library database
			ps = con.prepareStatement("SELECT * FROM book WHERE callNumber = ?");			
			ps.setString(1, callNumber);
			ResultSet rs0 = ps.executeQuery();
			if(rs0.next()==false)
			{
				throw new bookNotExistsException(callNumber);
			}
			//Check if bid is actually in library database
			ps = con.prepareStatement("SELECT * FROM borrower WHERE bid = ?");			
			ps.setInt(1, bid);
			ResultSet rs1 = ps.executeQuery();
			if(rs1.next()==false)
			{
				throw new bidNotExistsException(bid);
			}			
			//Check if there are copies in; if so, deny hold request with an exception.
			ps = con.prepareStatement("SELECT * FROM bookcopy WHERE callNumber = ? AND status = 'in'");			
			ps.setString(1, callNumber);
			ResultSet rs2 = ps.executeQuery();
			if(rs2.next())
			{
				throw new bookAvailableException(callNumber); 
			}
			else
			{
				//If book is in database, and no copy is in, then all copies are either on-hold or out
				//can now make a hold for the next available returned copy
				ps = con.prepareStatement("(SELECT * FROM bookcopy WHERE callNumber = ? AND status = 'out')");			
				ps.setString(1, callNumber);
				ResultSet rs3 = ps.executeQuery();
				if(rs3.next())
				{
					Date inputDate = new Date(System.currentTimeMillis()); 
					holdRequestTable.insertHoldRequest(bid, callNumber, inputDate);
				}
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

	}
	
	//Checks if Borid exists
	public boolean isBoridExist(int borid){
		ResultSet rs=null;
		try {
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT * FROM borrowing WHERE borid = ?");
			ps.setInt(1, borid);
			rs = ps.executeQuery();
			if(rs.next()==false){
				return false;
			}
			else{
				return true;
			}
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return false;

	}
	
	//Gets ResultSet of fines for a given bid
	public ResultSet showFine(int bid){
		ResultSet rs=null;
		try {
			PreparedStatement ps;
			ps = con.prepareStatement("SELECT fid, F.borid AS borid, title AS Book_Title, amount, TO_CHAR(issueDate, 'YYYY-MM-DD') AS Date_Issued, TO_CHAR(paidDate, 'YYYY-MM-DD') AS Date_Paid " +
										"FROM fine F, borrowing B, book Bk " +
										"WHERE F.borid = B.borid AND B.callNumber = Bk.callNumber AND B.bid = ?", 
										ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ps.setInt(1, bid);
			rs = ps.executeQuery();	

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		return rs;
	}
	
	//Pays a fine
	//ASSUMPTION: Fine is paid in whole
	//returns 1 if successful
	//returns 2 if already paid
	//returns 0 if failed
	public int payFine(int fid){
		int success=0;
		java.sql.Date inputDate =new java.sql.Date(System.currentTimeMillis());
		try {
			PreparedStatement ps;
			ps = con.prepareStatement("alter session set nls_date_format='DD/MM/YYYY'");
			ps.execute();
			ps = con.prepareStatement("SELECT paiddate FROM fine WHERE fid = ?");
			ps.setInt(1, fid);
			ResultSet rs= ps.executeQuery();
			Date result;
			if(rs.next())
				result = rs.getDate(1);
			else
				return 0;
			//System.out.print(result);
			if(result==null){
				ps = con.prepareStatement("UPDATE fine SET paiddate=? WHERE fid = ?");
				ps.setDate(1, inputDate);
				ps.setInt(2, fid);
				success=ps.executeUpdate();	
				con.commit();
			}
			else{
				return 2;
			}
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		if(success>0){
			return 1;
		}
		else{
			return 0;
		}
	}
	
}
