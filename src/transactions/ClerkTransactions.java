package transactions;
import java.sql.Connection;
import tableOperations.*;

//We need to import the java.sql package to use JDBC
import java.sql.*;

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
	
	//TODO Add transactions
	
	
	
}
