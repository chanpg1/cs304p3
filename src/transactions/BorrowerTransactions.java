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
	
	//TODO Add transactions
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
	
}
