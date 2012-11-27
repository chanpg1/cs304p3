package transactions;
import java.sql.Connection;
import tableOperations.*;

//We need to import the java.sql package to use JDBC
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ClerkTransactions {	
	
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
	
	//Constants
    private final static int DAY_IN_MILLISECS = 1000 * 60 * 60 * 24; //number of milliseconds in a day, used for computing due date with Java Date object
    private final static int YEAR_IN_MILLISECS = 1000 * 60 * 60 * 24 *365;
    
	public ClerkTransactions(Connection con){
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
	
	//Add a new borrower to the library. The user should  provide all the required information
	public boolean addBorrower(String name, String addr, String pass, Long phone, String email, int sinorst, String type){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 2);
		java.sql.Date expiryDate = new java.sql.Date(cal.getTimeInMillis());
		try{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM Borrower WHERE sinOrStNo = '" + sinorst + "'");
			ResultSet rs = ps.executeQuery();
			PreparedStatement ps2 = con.prepareStatement("SELECT * FROM BorrowerType WHERE type = '" + type + "'");
			ResultSet rs2 = ps2.executeQuery();
			//check if sinOrStNo is unique
			if(rs.next()){
				System.out.println("Another borrower with the same sin or student number exist.");
				System.out.println("Please try again");
			}
			else if(!rs2.next()){
				System.out.println("Borrower Type does not exist");
				System.out.println("Please try again");
			}
			else
			{
				this.borrowerTable.insertBorrower(pass, name, addr, phone, email, sinorst, expiryDate, type);
				return true;
			}
		}
		catch (SQLException e) {
			System.out.println("THIS Message: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Check-out items borrowed by a borrower. 
	 * To borrow items, borrowers provide their card number and a list with the call numbers of the items they want to check out. 
	 * The system determines if the borrower's account is valid and if the library items are available for borrowing. 
	 * Then it creates one or more borrowing records and prints a note with the items and their due day (which is giver to the borrower). 
	 */
	public boolean borrowBook(String callNum, int bid){
		try{
			PreparedStatement ps = con.prepareStatement("SELECT copyNo FROM BookCopy WHERE callNumber = '" + 
				callNum + "' AND status = 'in'");
			ResultSet rs = ps.executeQuery();
			PreparedStatement ps2 = con.prepareStatement("SELECT * FROM Borrower WHERE bid = '" + bid + "'");
			ResultSet rs2 = ps2.executeQuery();
			//check if bid exists
			if(!rs2.next()){
				System.out.println("Invalid bid: not found in database.");
				System.out.println("Please try again");
				return false;
			}
			if(rs.next()){
				Calendar cal = Calendar.getInstance();
				java.sql.Date outDate = new java.sql.Date(cal.getTimeInMillis());
				this.bookCopyTable.updateBookCopyStatus(callNum, rs.getInt("copyNo"), "out");
				this.borrowingTable.insertBorrowing(bid, callNum, rs.getInt("copyNo"), outDate);
				return true;
			}
			else{
				System.out.println("No copy of this book is available.");
				System.out.println("Please try again");
			}
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	/*
	 * Processes a return. 
	 * When  an item is returned, the clerk records the return by providing the item's catalogue number. 
	 * The system determines the borrower who had borrowed the item and records that the the item is "in".  
	 * If the item is overdue, a fine is assessed for the borrower.  
	 * If there is a hold request for this item by another borrower, the item is registered as "on-hold".
	 */
	//date is entered by clerk
	//fee calculated by fee = days_overdue * $1
	//return 	F = fail to return book
	//			T = book returned
	//			bid = book returned and on hold
	public String returnBook(String callNum, int copyNo, Date inDate){		
		try{
			SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
			String inputInDate = dateFormat.format(inDate);

			PreparedStatement ps = con.prepareStatement("UPDATE Borrowing SET inDate = TO_DATE('" + inputInDate
					+ "', 'yyyy-MM-dd') WHERE inDate IS NULL AND callNumber = '"+ callNum +"' AND copyNo = " + copyNo);
			ps.executeUpdate();
			con.commit();
			ps.close();
			PreparedStatement psx = con.prepareStatement("SELECT * FROM Borrowing WHERE callNumber = '" + 
					callNum + "' AND copyNo = " + copyNo + " AND inDate = TO_DATE('" + inputInDate
					+ "', 'yyyy-MM-dd')");
			ResultSet rsx = psx.executeQuery();
			if(rsx.next()){
				PreparedStatement psy = con.prepareStatement("SELECT type FROM Borrower WHERE bid = "
						+ rsx.getInt("bid"));
				ResultSet rsy = psy.executeQuery();
				rsy.next();
				PreparedStatement psz = con.prepareStatement("SELECT bookTimeLimit FROM BorrowerType " +
						"WHERE type = '" + rsy.getString("type") + "'");
				ResultSet rsz = psz.executeQuery();
				rsz.next();
				Date outDate = rsx.getDate("outdate");
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(outDate.getTime()); //set cal's time to outDate's time
				cal.add(Calendar.DATE, rsz.getInt("bookTimeLimit")); //add days to cal to get due date
				java.sql.Date dueDate = new java.sql.Date(cal.getTimeInMillis()); //convert due date to sql Date object
				if(dueDate.before(inDate)){
					//Calculate the number of days overdue
					//Borrower charged $1 for each day overdue					
					double days_overdue = Math.floor(((inDate.getTime())-(dueDate.getTime()))/DAY_IN_MILLISECS);					
					int int_days_overdue = (int)days_overdue;
					this.fineTable.insertFine(int_days_overdue, inDate, rsx.getInt("borid"));
				}
			}
			else{
				System.out.println("This book is not being borrowed by anyone");
				System.out.println("Please try again");
				return "F";
			}
			PreparedStatement ps2 = con.prepareStatement("SELECT bid, hid FROM HoldRequest WHERE callNumber = '" + 
					callNum + "' ORDER BY issueddate ASC");
			ResultSet rs2 = ps2.executeQuery();
			if(rs2.next()){
				this.bookCopyTable.updateBookCopyStatus(callNum, copyNo, "on-hold");
				this.holdRequestTable.deleteHoldRequest(Integer.parseInt(rs2.getString("hid")));
				return rs2.getString("bid");
			}
			else{
				this.bookCopyTable.updateBookCopyStatus(callNum, copyNo, "in");
				return "T";
			}
			
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}
		return "F";
	}
	
	//Returns all books that are currently out
	public ResultSet booksOut(){
		try{
			PreparedStatement ps = con.prepareStatement("SELECT Bwg.bid, Bwr.emailaddress, Bwg.callNumber, Bwg.borid, Bwg.outDate, Bt.booktimelimit " +
														"FROM Borrowing Bwg, Borrower Bwr, BorrowerType Bt " +
														"WHERE Bwg.bid=Bwr.bid AND Bwr.type=Bt.type AND Bwg.inDate is NULL ");
					
			ResultSet rs = ps.executeQuery();
			return rs;
		}
		catch (SQLException e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	
	
}
