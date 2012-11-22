package transactions;
import java.sql.Connection;
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
	
	
	
}
