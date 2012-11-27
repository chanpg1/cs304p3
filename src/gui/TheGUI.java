package gui;

//We need to import the java.sql package to use JDBC
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
//for reading from the command line
import java.io.*;

import javax.imageio.ImageIO;
//for the login window
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

//import the exceptions
import exceptions.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import tableOperations.*;
import transactions.*;

public class TheGUI implements ActionListener {
	private Connection con;

    // user is allowed 3 login attempts
    private int loginAttempts = 0;

    // components of the login window
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JFrame mainFrame;
    
    // components of Database GUI
    private MainFrame mainWindow;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;
    private JPanel librarianPanel;
    private JPanel clerkPanel;
    private JPanel borrowerPanel;
    private JPanel adminPanel;
    private JTable table;
	private JTable table2;
	private JTable table3;
    private JPanel tablePanel;
	private JPanel tablePanel2;
	private JPanel tablePanel3;
    private JScrollPane tableScrollPane;
	private JScrollPane tableScrollPane2;
	private JScrollPane tableScrollPane3;
    //The transaction classes
    private LibrarianTransactions libTrans;
    private BorrowerTransactions borTrans;
    private ClerkTransactions clkTrans;
    private AdminTransactions admTrans;
    
    //Constants
    private final static int DAY_IN_MILLISECS = 1000 * 60 * 60 * 24; //number of milliseconds in a day, used for computing due date with Java Date object
    private final static int YEAR_IN_MILLISECS = 1000 * 60 * 60 * 24*365;
    /*
     * constructs login window and loads JDBC driver
     */ 
    public TheGUI()
    {
      mainFrame = new JFrame("User Login");
      
      JLabel usernameLabel = new JLabel("Enter username: ");
      JLabel passwordLabel = new JLabel("Enter password: ");

      usernameField = new JTextField(10);
      passwordField = new JPasswordField(10);
      passwordField.setEchoChar('*');

      JButton loginButton = new JButton("Log In");

      JPanel contentPane = new JPanel();
      mainFrame.setContentPane(contentPane);


      // layout components using the GridBag layout manager

      GridBagLayout gb = new GridBagLayout();
      GridBagConstraints c = new GridBagConstraints();

      contentPane.setLayout(gb);
      contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      // place the username label 
      c.gridwidth = GridBagConstraints.RELATIVE;
      c.insets = new Insets(10, 10, 5, 0);
      gb.setConstraints(usernameLabel, c);
      contentPane.add(usernameLabel);

      // place the text field for the username 
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = new Insets(10, 0, 5, 10);
      gb.setConstraints(usernameField, c);
      contentPane.add(usernameField);

      // place password label
      c.gridwidth = GridBagConstraints.RELATIVE;
      c.insets = new Insets(0, 10, 10, 0);
      gb.setConstraints(passwordLabel, c);
      contentPane.add(passwordLabel);

      // place the password field 
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = new Insets(0, 0, 10, 10);
      gb.setConstraints(passwordField, c);
      contentPane.add(passwordField);

      // place the login button
      c.gridwidth = GridBagConstraints.REMAINDER;
      c.insets = new Insets(5, 10, 10, 10);
      c.anchor = GridBagConstraints.CENTER;
      gb.setConstraints(loginButton, c);
      contentPane.add(loginButton);

      // register password field and OK button with action event handler
      passwordField.addActionListener(this);
      loginButton.addActionListener(this);

      // anonymous inner class for closing the window
      mainFrame.addWindowListener(new WindowAdapter() 
      {
	public void windowClosing(WindowEvent e) 
	{ 
	  System.exit(0); 
	}
      });

      // size the window to obtain a best fit for the components
      mainFrame.pack();

      // center the frame
      Dimension d = mainFrame.getToolkit().getScreenSize();
      Rectangle r = mainFrame.getBounds();
      mainFrame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );

      // make the window visible
      mainFrame.setVisible(true);

      // place the cursor in the text field for the username
      usernameField.requestFocus();

      try 
      {
		// Load the Oracle JDBC driver
		DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
      }
      catch (SQLException ex)
      {
    	  JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", 1);
    	  System.out.println("Message: " + ex.getMessage());
    	  System.exit(-1);
      }
    }


    /*
     * connects to Oracle database named ug using user supplied username and password
     */ 
    private boolean connect(String username, String password)
    {
      String connectURL = "jdbc:oracle:thin:@dbhost.ugrad.cs.ubc.ca:1522:ug"; 

      try 
      {
		con = DriverManager.getConnection(connectURL,username,password);
	
		System.out.println("\nConnected to Oracle!");
		return true;
      }
      catch (SQLException ex)
      {
    	  JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", 1);	
    	  System.out.println("Message: " + ex.getMessage());
    	  return false;
      }
    }


    /*
     * event handler for login window
     */ 
    public void actionPerformed(ActionEvent e) 
    {
	if ( connect(usernameField.getText(), String.valueOf(passwordField.getPassword())) )
	{
	  // if the username and password are valid, 
	  // remove the login window and display a text menu 
	  mainFrame.dispose();
      showWindow();
      libTrans = new LibrarianTransactions(con);
      borTrans = new BorrowerTransactions(con);
      clkTrans = new ClerkTransactions(con);
      admTrans = new AdminTransactions(con);
	}
	else
	{
	  loginAttempts++;
	  
	  if (loginAttempts >= 3)
	  {
	      mainFrame.dispose();
	      System.exit(-1);
	  }
	  else
	  {
	      // clear the password
	      passwordField.setText("");
	  }
	}             
                    
    }


    /*
     * displays simple text interface
     */ 
    private void showWindow()
    {
		
		//Create the main containers
		mainWindow = new MainFrame("CS304 AMAZING DB APP");
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        tabbedPane = new JTabbedPane();        
        librarianPanel = new JPanel();
        librarianPanel.setLayout(new GridLayout(0,2));
        //librarianPanel.setLayout(new BoxLayout(librarianPanel, BoxLayout.Y_AXIS));
        clerkPanel = new JPanel();
        clerkPanel.setLayout(new GridLayout(0,2));
        borrowerPanel = new JPanel();
        borrowerPanel.setLayout(new GridLayout(0,2));
        adminPanel = new JPanel();
        adminPanel.setLayout(new GridLayout(0,2));
        tableScrollPane = new JScrollPane();
		tableScrollPane2 = new JScrollPane();
		tableScrollPane3 = new JScrollPane();
        tablePanel = new JPanel();
		tablePanel2 = new JPanel();
		tablePanel3 = new JPanel();
		tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
		tablePanel2.setLayout(new BoxLayout(tablePanel2, BoxLayout.Y_AXIS));
		tablePanel3.setLayout(new BoxLayout(tablePanel3, BoxLayout.Y_AXIS));
        tablePanel.add(tableScrollPane);
		tablePanel2.add(tableScrollPane2);
		tablePanel3.add(tableScrollPane3);
        
        //import icon for tabs
        ImageIcon icon = new ImageIcon("images/icon.png", "Pretty Icon");        
        
        //Set up Librarian, Clerk and Borrower Panels        
        createLibrarianPanel();
        createClerkPanel();
        createBorrowerPanel();
        createAdminPanel();
        
        //Create the tabs        
        tabbedPane.addTab("Librarian", icon, librarianPanel, "Librarian Transactions");
        tabbedPane.addTab("Clerk", icon, clerkPanel, "Clerk Transactions");
        tabbedPane.addTab("Borrower", icon, borrowerPanel, "Borrower Transactions");
        tabbedPane.addTab("Admin", icon, adminPanel, "Admin Operations");
        contentPanel.add(tabbedPane);
        contentPanel.add(tablePanel);         
		contentPanel.add(tablePanel2);
		contentPanel.add(tablePanel3);
		
        // this line adds the panel to the
        // Frame's content pane
        mainWindow.addWindowListener(new WindowAdapter() 
        {
        	public void windowClosing(WindowEvent e) 
        	{ 
        	  System.exit(0); 
        	}
              });
        mainWindow.getContentPane().add(contentPanel);
        //mainWindow.getContentPane().add(tablePanel);
		mainWindow.setMinimumSize(new Dimension(450, 300));
        mainWindow.pack();
		mainWindow.setVisible(true);

    }
    
    //Creates the Clerk tab
    public void createClerkPanel(){
    	JButton addBorrowerButton = new JButton("Add Borrower");
    	JButton checkOutBooksButton = new JButton("Check out Books");
    	JButton processReturnButton = new JButton("Process a Return");
		JButton checkOverdueButton = new JButton("Check Overdue Items");
		
		//add listener for Add Book button
    	addBorrowerButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField name = new JTextField(5);
				JTextField addr = new JTextField(5);
				JTextField pass = new JTextField(5);
				JTextField phone = new JTextField(5);
				JTextField email = new JTextField(5);
				JTextField sinorst = new JTextField(5);
				JTextField type = new JTextField(5);

		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("Name: "));
			     myPanel.add(name);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Address: "));
			     myPanel.add(addr);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Password: "));
			     myPanel.add(pass);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Phone: "));
			     myPanel.add(phone);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Email: "));
			     myPanel.add(email);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Sin or Student Number: "));
			     myPanel.add(sinorst);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("type: "));
			     myPanel.add(type);
			     
			     int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
			     try{
				     if (result == JOptionPane.OK_OPTION) {
	
				    	 clkTrans.addBorrower(name.getText(),
				    			 				addr.getText(),				    			 				 
				    			 				pass.getText(), 
				    			 				Long.parseLong(phone.getText()), 
				    			 				email.getText(), 
				    			 				Integer.parseInt(sinorst.getText()), 
				    			 				type.getText());
				     }
				     Statement stmt = con.createStatement();
				     ResultSet rs = stmt.executeQuery("SELECT * FROM borrower");
				     buildTable("List of Borrowers in Database", rs);
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Book cannot be added", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());

			            try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
				    	System.out.println("Message: " + ex2.getMessage());
				    	System.exit(-1);		
				    }
				}
				 
			}
          });
		
		//add listener for Check out Book button
    	checkOutBooksButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField callNum = new JTextField(5);
				JTextField bid = new JTextField(5);

		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("bid: "));
			     myPanel.add(bid);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Call Number(s): (separate by semi-colon) "));
			     myPanel.add(callNum);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     try{

				     int result = JOptionPane.showConfirmDialog(null, myPanel, 
				               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
				     if(!callNum.getText().isEmpty() && result == JOptionPane.OK_OPTION)
				     {
				    	 if(callNum.getText().contains(";")){
								String[] callNums = callNum.getText().split("\\s*;\\s*"); //delimit call numbers by semicolon
								for(int i = 0; i < callNums.length; i++){
									clkTrans.borrowBook(callNums[i], Integer.parseInt(bid.getText()));
								}
							}
				    	 else{
				    		 clkTrans.borrowBook(callNum.getText(), Integer.parseInt(bid.getText()));
				    	 }
					     Statement stmt = con.createStatement();
					     ResultSet rs = stmt.executeQuery("SELECT * FROM borrowing WHERE bid = '"+bid.getText()+"'");
					     buildTable("List of of borrowings for bid "+bid.getText(), rs);
				     }
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Book cannot be added", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());

			            try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
				    	System.out.println("Message: " + ex2.getMessage());
				    	System.exit(-1);		
				    }
				}
				 
			}
          });
    	
    	/*
    	 * Processes a return. 
    	 * When  an item is returned, the clerk records the return by providing the item's catalogue number. 
    	 * The system determines the borrower who had borrowed the item and records that the the item is "in".  
    	 * If the item is overdue, a fine is assessed for the borrower.  
    	 * If there is a hold request for this item by another borrower, the item is registered as "on hold" and a message is send to the borrower who made the hold request.
    	 */    	
    	processReturnButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField callNum = new JTextField(5);
				JTextField copyNo = new JTextField(5);

		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("Call Number: "));
			     myPanel.add(callNum);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Copy Number: "));
			     myPanel.add(copyNo);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     
			     try{

				     int result = JOptionPane.showConfirmDialog(null, myPanel, 
				               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
				     if(!callNum.getText().isEmpty() && result == JOptionPane.OK_OPTION)
				     {
				    	 Calendar cal = Calendar.getInstance();
				    	 java.sql.Date inDate = new java.sql.Date(cal.getTimeInMillis());
			    		 String result2 = clkTrans.returnBook(callNum.getText(), Integer.parseInt(copyNo.getText()), inDate);
			    		 Statement stmt1 = con.createStatement();
			    		 Statement stmt2 = con.createStatement();
			    		 Statement stmt3 = con.createStatement();
			    		 ResultSet rs1 = null;
			    		 ResultSet rs2 = null;
			    		 ResultSet rs3 = null;
			    		 //If return was processed successfully
			    		 if(result2.equals("T")){
			    			 JOptionPane.showMessageDialog(null, "Book was returned successfully", "Process Return Completed", JOptionPane.INFORMATION_MESSAGE);
			    			 rs1 = stmt1.executeQuery("SELECT * FROM bookcopy WHERE callnumber = '"+callNum.getText()+"'");						     
						     rs2 = stmt2.executeQuery("SELECT * FROM borrowing WHERE callnumber = '"+callNum.getText()+"' AND copyno ="+copyNo.getText()+"ORDER BY indate DESC");
						     rs3 = stmt3.executeQuery("SELECT null FROM bookcopy WHERE callnumber IS NULL");  //Placeholder table
						     buildThreeTables("List of Copies for Call Nummber "+callNum.getText()+":", "Borrowings Related to This Item", "", rs1, rs2, rs3);
						     //Remove the placeholder table
						     tableScrollPane3.removeAll();
						     tablePanel3.removeAll();
						     mainWindow.pack();
			    		 }
			    		 //if failed to process return
			    		 else if(result2.equals("F")){
			    			 JOptionPane.showMessageDialog(null, "The book was not returned. \nLikely Cause: book was already returned", "Process Return Failed", JOptionPane.WARNING_MESSAGE);
			    		 }
			    		 //if item was returned successfully AND placed on hold
			    		 else{
			    			 JOptionPane.showMessageDialog(null, "Book was returned successfuly. \nHold request completed for borrower with bid "+result2+".", "Book Returned and Held", JOptionPane.INFORMATION_MESSAGE);
			    			 JOptionPane.showMessageDialog(null, "Email message sent to account with bid "+result2+", reminding the borrower that his or her requested item is now available.", "Message Sent", JOptionPane.INFORMATION_MESSAGE);
						     rs1 = stmt1.executeQuery("SELECT * FROM bookcopy WHERE callnumber = '"+callNum.getText()+"'");
						     rs2 = stmt2.executeQuery("SELECT * FROM holdrequest WHERE callnumber = '"+callNum.getText()+"' AND bid = "+result2);
						     rs3 = stmt3.executeQuery("SELECT * FROM borrowing WHERE callnumber = '"+callNum.getText()+"' AND copyno ="+copyNo.getText()+"ORDER BY indate DESC");
						     buildThreeTables("List of Copies for Call Nummber "+callNum.getText()+":", "Updated Hold Requests on This Item: ", "Borrowings Related to This Item", rs1, rs2, rs3);
			    		 }
					     
			    		 stmt1.close();
			    		 stmt2.close();
			    		 stmt3.close();
				     }
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Book cannot be returned", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());
			    	 ex.printStackTrace();
			            try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
				    	ex2.printStackTrace();
				    	System.out.println("Message: " + ex2.getMessage());
				    	System.exit(-1);		
				    }
				}
				 
			}
          });
    	
    	/*
    	 * Checks overdue items. 
    	 * The system displays a list of the items that are overdue and the borrowers who have checked them out.  
    	 * The clerk may decide to send an email messages to any of them (or to all of them). 
    	 */
    	checkOverdueButton.addActionListener(new ActionListener(){            
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
						ResultSet booksOut = clkTrans.booksOut();				
						DefaultTableModel tModel = booksOutRSToTableModel(new DefaultTableModel(), booksOut);
						int numRows = tModel.getRowCount();
						String message = "Message sent to: \n"+String.format("%1$-" + 15 + "s", "BID")+" || "+String.format("%1$" + 15 + "s", "EMAIL") +"\n";
						int result = JOptionPane.showConfirmDialog(null, "Email ALL borrowers with overdue books?");
						buildOverdueTable("List of outstanding overdue books (CLICK ROWS TO SEND EMAIL):",tModel);
					
						if(result == JOptionPane.YES_OPTION){
							for(int i = 0; i < numRows; i++){						
								String bid = tModel.getValueAt(i, 0).toString();
								bid.trim();
								String email = (String)tModel.getValueAt(i, 1);
								message = message + String.format("%1$-" + 15 + "s", bid)+ " || "+ String.format("%1$" + 15 + "s", email) +"\n"; 
							}
							JOptionPane.showMessageDialog(null, message, "Emails Sent", JOptionPane.INFORMATION_MESSAGE);
						}					
						else if(result == JOptionPane.NO_OPTION){
							JOptionPane.showMessageDialog(null, "Click table rows to send individual emails", "Info", JOptionPane.INFORMATION_MESSAGE);
						}
		    
			     }			    
				catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());
			    	 ex.printStackTrace();
			            try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
				    	ex2.printStackTrace();
				    	System.out.println("Message: " + ex2.getMessage());
				    	System.exit(-1);		
				    }
				}
				 
			}
          });
    	
    	clerkPanel.add(addBorrowerButton);
    	clerkPanel.add(checkOutBooksButton);
    	clerkPanel.add(processReturnButton);
    	clerkPanel.add(checkOverdueButton); 
    }
    
    //Creates the Borrower tab
    public void createBorrowerPanel(){
    	JButton placeHoldButton = new JButton("Place hold");
    	JButton searchBookButton = new JButton("Search book");
    	JButton showFineButton = new JButton("Show Your Fines");
		JButton payFineButton = new JButton("Pay a Fine");
		JButton myAccountButton = new JButton("My Account");
		
    	//Show fines for a given borrower
    	showFineButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTextField bidField = new JTextField(10);
				JPanel myPanel = new JPanel();
			    myPanel.add(new JLabel("bid"));
				myPanel.add(bidField);
				ResultSet rs=null;
				int result = JOptionPane.showConfirmDialog(null, myPanel, 
						"ShowFine:", JOptionPane.OK_CANCEL_OPTION);
			     try{
				     if (result == JOptionPane.OK_OPTION) {
				    	 rs=borTrans.showFine(Integer.parseInt(bidField.getText()));
				     

					     if(rs.next()==false){
					    	 if(borTrans.isBoridExist(Integer.parseInt(bidField.getText()))){
					    	 JOptionPane.showMessageDialog(null, "You currently have no fines","Notice",JOptionPane.INFORMATION_MESSAGE);
					    	 }
					    	 else
					    	 {
					    	JOptionPane.showMessageDialog(null, "Borid does not exist in database","Warning",JOptionPane.WARNING_MESSAGE);
					    	 }
					    }
					  
					     rs.beforeFirst();
					     buildTable("Your Fines", rs);
					 }     
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Hold cannot be placed", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } 
			     catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());
	     
			     }
			     }
			});

    	//add listener to Search Book button
    	searchBookButton.addActionListener(new ActionListener()
		{            
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTextField searchField = new JTextField(10);
				JComboBox searchCategory = new JComboBox();

				searchCategory.addItem("Title");
				searchCategory.addItem("Author");
				searchCategory.addItem("Subject");
				
				JPanel myPanel = new JPanel();
				
				myPanel.add(searchField);
				myPanel.add(searchCategory);

				int result = JOptionPane.showConfirmDialog(null, myPanel, 
						"Search for a book:", JOptionPane.OK_CANCEL_OPTION);
							     try{
								     if (result == JOptionPane.OK_OPTION) {
								    	 String category = (String) searchCategory.getSelectedItem();
								    	 String query = searchField.getText();
								    	 ResultSet matches = borTrans.findMatch(category, query);
								    	 ResultSet inCopies;
								    	 ResultSet outCopies;
								    	 String callnum;
								    	 String title;
								    	 int inCount;
								    	 int outCount;
								    	 ArrayList<String> titles = new ArrayList<String>();
								    	 ArrayList<String> callNums = new ArrayList<String>();
								    	 ArrayList<Integer> inCounts = new ArrayList<Integer>();
								    	 ArrayList<Integer> outCounts = new ArrayList<Integer>();

								    	 //for each match, find numbers of copies in and out
								    	 while(matches.next()){
								    		 callnum = matches.getString("callnumber");							    		 
								    		 callNums.add(callnum);
								    		 title = matches.getString("title");
								    		 titles.add(title);
								    		 
								    		 Statement stmt = con.createStatement();
								    		 
								    		 //Get Number of Copies in
								    		 inCopies = stmt.executeQuery("SELECT callnumber, COUNT(callNumber) AS count " +
														"FROM bookCopy " +
														"WHERE callNumber = '"+callnum+ "' AND status = 'in' " +
														"GROUP BY callNumber");
								    		 if(inCopies.next())
								    			 inCount = inCopies.getInt("count");
								    		 else
								    			 inCount = 0;
								    		 
								    		 inCounts.add(inCount);
								    		 
								    		//Get Number of Copies out
								    		 outCopies = stmt.executeQuery("SELECT callnumber, COUNT(callNumber) AS count " +
														"FROM bookCopy " +
														"WHERE callNumber = '"+callnum+ "' AND status = 'out' " +
														"GROUP BY callNumber");								    		 
								    		 if(outCopies.next())
								    			 outCount = outCopies.getInt("count");
								    		 else
								    			 outCount = 0;
								    		 
								    		 outCounts.add(outCount);	

								    	 }
								    	 
								    	 //Build table based on the 4 ArrayLists of Data
								    	 DefaultTableModel model= new DefaultTableModel();
							    		 String cols[]=new String[4];
							    		 cols[0] = "Call Number";
							    		 cols[1] = "Title";
							    		 cols[2] = "Copies in";
							    		 cols[3] = "Copies out";
							    		 model.setColumnIdentifiers(cols);		 

							    		 for(int i = 0; i<callNums.size();i++){
							    			 Object data[]= new Object[4];
							    			 data[0] = callNums.get(i);
							    			 data[1] = titles.get(i);
							    			 data[2] = inCounts.get(i);
							    			 data[3] = outCounts.get(i);
		
							    			 model.addRow(data);
							    		 }
							    		 
							    		 buildTable("Search Results for " + category + ": " + "'"+ query + "'", model);
										
								     }
					
								    
							     }
							     catch (NumberFormatException ex){
							    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Hold cannot be placed", JOptionPane.WARNING_MESSAGE);
							    	 System.out.println("Invalid input: " + ex.getMessage());
							    	 System.out.println("Please try again.");
							     }
							     catch (SQLException e) {
										JOptionPane.showMessageDialog(null, "SQL Error:\n " + e.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
										e.printStackTrace();
								 }
			}
		});

    	//add listener for Place Hold button
    	placeHoldButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField bid = new JTextField(5);
				JTextField callNumber = new JTextField(5);
				
		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("Borrower ID (bid):"));
			     myPanel.add(bid);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Call Number:"));
			     myPanel.add(callNumber);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer			    
			     
			     int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
			     try{
				     if (result == JOptionPane.OK_OPTION) {
				    	 borTrans.holdOutBook(Integer.parseInt(bid.getText()), callNumber.getText());
				     }
				     PreparedStatement ps = con.prepareStatement("SELECT hid, callnumber AS Call_Number, issueddate AS Issued_Date FROM holdRequest WHERE bid = ?");
				     ps.setInt(1, Integer.parseInt(bid.getText()));
				     ResultSet rs = ps.executeQuery();
				     buildTable("Your Hold Requests", rs);
			     }
			     catch (bookAvailableException ex){
			    	JOptionPane.showMessageDialog(null, ex.getMessage(), "Hold cannot be placed", JOptionPane.INFORMATION_MESSAGE);
			     }
			     catch (bookNotExistsException ex){
			    	JOptionPane.showMessageDialog(null, ex.getMessage(), "Hold cannot be placed", JOptionPane.INFORMATION_MESSAGE);
			     }
			     catch (bidNotExistsException ex){
			    	JOptionPane.showMessageDialog(null, ex.getMessage(), "Hold cannot be placed", JOptionPane.INFORMATION_MESSAGE);
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Hold cannot be placed", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage() + "\nPlease try again\n");
			     } 
			     catch (SQLException ex) {
			    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	System.out.println("Message: " + ex.getMessage());

			        try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);	
				    	System.out.println("Message: " + ex2.getMessage());
				    	System.exit(-1);		
				    }
				}
				 
			}
          });

    	//add Listener for Pay Fine button
		payFineButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JTextField boridField = new JTextField(10);
				JTextField fidField = new JTextField(10);
				JTextField bidField = new JTextField(10);
				JTextField payField = new JTextField(10);
				
				JPanel myPanel = new JPanel();				
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			    myPanel.add(new JLabel("borid"));
				myPanel.add(boridField);
			    myPanel.add(new JLabel("fid"));
				myPanel.add(fidField);
			    myPanel.add(new JLabel("bid"));
				myPanel.add(bidField);				

				JPanel payPanel = new JPanel();				
				payPanel.setLayout(new BoxLayout(payPanel, BoxLayout.Y_AXIS));
			    payPanel.add(new JLabel("Credit Card Number"));
				payPanel.add(payField);
				
				ResultSet rs=null;
				int result = JOptionPane.CANCEL_OPTION;
				int payMe = JOptionPane.showConfirmDialog(null, payPanel, "Enter Credit Card Number:", JOptionPane.OK_CANCEL_OPTION);
				if(payMe == JOptionPane.OK_OPTION && !payField.getText().isEmpty()){
					result = JOptionPane.showConfirmDialog(null, myPanel, "Pay Fine", JOptionPane.OK_CANCEL_OPTION);
				}
				else{
					JOptionPane.showMessageDialog(null, "Please pay up before we air-drop Andy on your house.");
				}

			     try{
			    	 int success=0;
				     if (result == JOptionPane.OK_OPTION) {
				    	 success =borTrans.payFine(Integer.parseInt(fidField.getText()));
				    	 rs=borTrans.showFine(Integer.parseInt(bidField.getText()));
					 	 
				    	 //returns 0 if failed
					     if(success==0){
					    	 JOptionPane.showMessageDialog(null, "Warning: Payment could not be processed\nIf cancel was not selected, please check the fine id","Warning",JOptionPane.WARNING_MESSAGE);
					     }
						 
					     //returns 2 if already paid
					     else if(success==2){
					    	 JOptionPane.showMessageDialog(null, "Payment has already been paid.\n Transaction not processed ","Warning",JOptionPane.WARNING_MESSAGE);
					     }
					     
					     //returns 1 if successful
					     else if(success==1){
					    	 JOptionPane.showMessageDialog(null, "Thank You for paying your fine.\n Please remember to return your books on time from now on","Notice",JOptionPane.INFORMATION_MESSAGE);
					     }
					     else{
						     if(rs.next()==false){
						    	 JOptionPane.showMessageDialog(null, "Congrats, You currently have no fines","Notice",JOptionPane.INFORMATION_MESSAGE);
						     }
					     }
					     rs.beforeFirst();
					     buildTable("Your fines", rs);
				     }

			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Hold cannot be placed", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } 
			     catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());
			}}});
		
		//add listener for My Account button
		myAccountButton.addActionListener(new ActionListener()
		{            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField bidField = new JTextField(5);
				
				JPanel myPanel = new JPanel();
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
				myPanel.add(new JLabel("Enter your Borrower ID:"));
				myPanel.add(bidField);
				myPanel.add(Box.createHorizontalStrut(15)); // a spacer
				
				int result = JOptionPane.showConfirmDialog(null, myPanel, 
						"Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
				
				if (result == JOptionPane.OK_OPTION) {
					 PreparedStatement ps;
					 PreparedStatement ps2;
					 PreparedStatement ps3;
						try {
							ps = con.prepareStatement(
								"SELECT borrowing.callNumber, book.title, book.mainAuthor, borrowing.copyNo, borrowing.outDate, borrowing.inDate FROM book, bookcopy, borrower, borrowing WHERE borrowing.callNumber = book.callNumber AND borrowing.callNumber = bookcopy.callNumber AND borrowing.copyNo = bookcopy.copyNo AND borrower.bid = borrowing.bid AND borrower.bid = ?");
							ps.setString(1, bidField.getText()); 
							
							
							ps2 = con.prepareStatement(
								"SELECT issuedDate, holdrequest.callNumber, title, mainAuthor FROM holdrequest JOIN book on holdrequest.callNumber = book.callNumber WHERE holdrequest.bid = ?");
							ps2.setString(1, bidField.getText()); 
						
							
							ps3 = con.prepareStatement(
								"SELECT fine.fid, borrowing.borid, title, mainAuthor, borrowing.callNumber, amount as fineAmount FROM borrowing, book, fine WHERE borrowing.borid = fine.borid AND book.callNumber = borrowing.callNumber AND fine.paiddate IS NULL AND borrowing.bid = ?");
							ps3.setString(1, bidField.getText()); 
					
							ResultSet rs = ps.executeQuery(); 
							ResultSet rs2 = ps2.executeQuery(); 
							ResultSet rs3 = ps3.executeQuery(); 
					    	buildThreeTables("My Borrowed Books", "My Hold Requests", "My Outstanding Fines", rs, rs2, rs3);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	 
				}

			}
		});

		borrowerPanel.add(myAccountButton);
		borrowerPanel.add(payFineButton);
		borrowerPanel.add(showFineButton);
		borrowerPanel.add(searchBookButton);
    	borrowerPanel.add(placeHoldButton);
    }
    
    //Creates the Librarian tab
    //to refactor later
    public void createLibrarianPanel(){
    	JButton addBookButton = new JButton("Add a book");
    	JButton printBorrowingsButton = new JButton("Print all borrowings");
    	JButton getPopularBooksButton = new JButton("Show popular books");
    	JButton showAllFineButton = new JButton("Show All Fines");
    	JButton showAllBooksButton = new JButton("Show all books");
    	JButton showAllBorrowersButton = new JButton("Show all Borrowers");
    	
    	//add listener for Add Book button
    	addBookButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField callNumber = new JTextField(5);
				JTextField isbn = new JTextField(5);
				JTextField title = new JTextField(5);
				JTextField mainAuthor = new JTextField(5);
				JTextField publisher = new JTextField(5);
				JTextField year = new JTextField(5);
				JTextField subjects = new JTextField(5);
				JTextField otherAuthors = new JTextField(5);
		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("Call Number:"));
			     myPanel.add(callNumber);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("ISBN:"));
			     myPanel.add(isbn);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Title:"));
			     myPanel.add(title);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Main Author"));
			     myPanel.add(mainAuthor);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Publisher:"));
			     myPanel.add(publisher);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Year:"));
			     myPanel.add(year);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Subjects (optional, separate by semi-colon):"));
			     myPanel.add(subjects);
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			     myPanel.add(new JLabel("Other Authors (optional, separate by semi-colon):"));
			     myPanel.add(otherAuthors);

			     
			     int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
			     try{
				     if (result == JOptionPane.OK_OPTION) {
				    	 libTrans.addNewBook(callNumber.getText(),
				    			 isbn.getText(), 
				    			 title.getText(),
				    			 mainAuthor.getText(), 
				    			 publisher.getText(), 
				    			 Integer.parseInt(year.getText()),
				    			 subjects.getText(),
				    			 otherAuthors.getText());
				     }
				     Statement stmt = con.createStatement();
				     ResultSet rs = stmt.executeQuery("SELECT * FROM book");
				     buildTable("List of Books in Database", rs);
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Book cannot be added", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());

			            try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
				    	System.out.println("Message: " + ex2.getMessage());
				    	System.exit(-1);		
				    }
				}
				 
			}
          });
    	
    	//add listener for Print Borrowings button
    	/*
    	 * Generate a report with all the books that have been checked out. 
    	 * For each book the report shows the date it was checked out and the due date. 
    	 * The system flags the items that are overdue. The items are ordered by the book call number.  
    	 * If a subject is provided the report lists only books related to that subject, otherwise all the books that are out are listed by the report.	
    	*/
    	printBorrowingsButton.addActionListener(new ActionListener()
        {
    		ResultSet rs = null;
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JPanel myPanel = new JPanel();
				myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
				JTextField subject = new JTextField(5);
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			    myPanel.add(new JLabel("Subject (optional, leave blank to include all subjects):"));
			    myPanel.add(subject);

			    int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
			    try{
				     if (result == JOptionPane.OK_OPTION) {
				    	 rs = libTrans.generateBorrowingsReport(subject.getText());
				     }
				     DefaultTableModel tModel = borrowingsRSToTableModel(new DefaultTableModel(), rs);
				     buildTable("Books currently checked out", tModel);
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Operation cannot proceed", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());

			        try 
				    {
					con.rollback();	
				    }
				    catch (SQLException ex2)
				    {
				    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
						System.out.println("Message: " + ex2.getMessage());
						System.exit(-1);		
				    }
				}
			} 
    		
        });
    	
    	//add listener for Get Popular Books button
    	getPopularBooksButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				JTextField topN = new JTextField(5);
				ResultSet rs = null;
		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("Enter the number of top hits you wish to see: (leave blank for 10)"));
			     myPanel.add(topN);			     
			     
			     int result = JOptionPane.showConfirmDialog(null, myPanel, 
			               "Please Enter the Following Info:", JOptionPane.OK_CANCEL_OPTION);
			     try{
				     if (result == JOptionPane.OK_OPTION) {
				    	 if(topN.getText().length() > 0)
				    		 rs = libTrans.booksByPopularity(Integer.parseInt(topN.getText()));
				    	 else	//if nothing was entered
				    		 rs = libTrans.booksByPopularity(10);
					     DefaultTableModel tModel = topBooksRSToTableModel(new DefaultTableModel(), rs);				    	 					     
					     buildTable("Most Popular Books", tModel);
				     }
			     }
			     catch (NumberFormatException ex){
			    	 JOptionPane.showMessageDialog(null, "Invalid input detected\n " + ex.getMessage() + "\nPlease try again", "Operation cannot proceed", JOptionPane.WARNING_MESSAGE);
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     }
			     catch (SQLException ex) {
			    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
				    System.out.println("Message: " + ex.getMessage());
			     }
				 
			}
          });
    	
    	//add listener for Show All Fines button
    	showAllFineButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {

				ResultSet rs=null;

			     try{

			    	 rs=libTrans.showAllFine();

				     if(rs.next()==false){
				    	 JOptionPane.showMessageDialog(null, "There are no fines","Notice",JOptionPane.INFORMATION_MESSAGE);
				     }
				     rs.beforeFirst();
				     buildTable("List of All Fines", rs);

			     }
			     catch (NumberFormatException ex){
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
				    	System.out.println("Message: " + ex.getMessage());
		}}});
    	
    	//add listener for Show All Books button
    	showAllBooksButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {

				ResultSet rs=null;
				rs=libTrans.showAllBooks();
				buildTable("List of All Titles in Database", rs);
			}});
    	
    	//add listener for Show All Borrowers button
    	showAllBorrowersButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {

				ResultSet rs=null;
				rs=libTrans.showAllBorrowers();
				buildTable("List of All Borrowers in Database", rs);
			}});	    	
		    	
    	librarianPanel.add(addBookButton);
    	librarianPanel.add(printBorrowingsButton);
    	librarianPanel.add(getPopularBooksButton);
    	librarianPanel.add(showAllFineButton);
    	librarianPanel.add(showAllBooksButton);
    	librarianPanel.add(showAllBorrowersButton);
    }
    
    public void createAdminPanel(){
    	JButton createAllTblAndSeqButton = new JButton("CREATE all tables and sequences");
    	JButton dropAllTblAndSeqButton = new JButton("DROP all tables and sequences");
    	JButton clearAllTblButton = new JButton("CLEAR all tables");
    	JButton resetAllSeqButton = new JButton("RESET all sequences");
    	JButton seedAllTblButton = new JButton("SEED all tables");
    	
    	//add listener for create tables and sequences button
    	createAllTblAndSeqButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				boolean created = false; 

		
			     JPanel myPanel = new JPanel();
			     myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			     myPanel.add(new JLabel("Create a new set of empty tables and sequences?"));
			     myPanel.add(new JLabel("(will return error and roll back if tables/sequences already exist)"));
			     myPanel.add(Box.createHorizontalStrut(15)); // a spacer

			     int result = JOptionPane.showConfirmDialog(null, myPanel, "Confirmation", JOptionPane.YES_NO_OPTION);
			     try{
				     if (result == JOptionPane.YES_OPTION) {
				    	 created = admTrans.createAllTblAndSeq();
				     }
				     else{return;}
				     if(created){
					     Statement stmt1 = con.createStatement();
					     ResultSet rs1 = stmt1.executeQuery("SELECT * FROM book");
					     Statement stmt2 = con.createStatement();
					     ResultSet rs2 = stmt2.executeQuery("SELECT * FROM borrower");
					     Statement stmt3 = con.createStatement();
					     ResultSet rs3 = stmt3.executeQuery("SELECT * FROM borrowing");
					     buildThreeTables("Books Table", "Borrower Table", "Borrowing Table", rs1, rs2, rs3);
					     stmt1.close();
					     stmt2.close();
					     stmt3.close();
					     JOptionPane.showMessageDialog(null, "New tables and sequences created successfully", "Operation successful", JOptionPane.INFORMATION_MESSAGE);
				     }
				     else{
				    	 JOptionPane.showMessageDialog(null, "Failed to create new tables and sequences", "Operation failed", JOptionPane.WARNING_MESSAGE);
				     }

			     }
			      	catch (SQLException ex) {
			    	 JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			    	 System.out.println("Message: " + ex.getMessage());

				        try 
					    {
				        	con.rollback();	
					    }
					    catch (SQLException ex2)
					    {
					    	JOptionPane.showMessageDialog(null, "SQL Error:\n " + ex2.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
					    	System.out.println("Message: " + ex2.getMessage());
					    	System.exit(-1);		
					    }
				}
				 
			}
          });
    	
    	//add listener for drop tables and sequences button
    	dropAllTblAndSeqButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				boolean dropped = false; 
		
			    JPanel myPanel = new JPanel();
			    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			    myPanel.add(new JLabel("Clear and drop ALL tables and sequences?"));
			    myPanel.add(new JLabel("(will roll back on error)"));
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
	
			    int result = JOptionPane.showConfirmDialog(null, myPanel, "Confirmation", JOptionPane.YES_NO_OPTION);
	
			    if (result == JOptionPane.YES_OPTION) {
			    	dropped = admTrans.dropAllTblAndSeq();
			    }
			    else{return;}
			    if(dropped){
			    	JOptionPane.showMessageDialog(null, "All tables and sequences dropped successfully", "Operation successful", JOptionPane.INFORMATION_MESSAGE);
			    }
			    else{
			    	JOptionPane.showMessageDialog(null, "Failed to drop tables and sequences", "Operation failed", JOptionPane.WARNING_MESSAGE);
			    }     
				 
			}
        });
    	
    	//add listener for clear tables and sequences button
    	clearAllTblButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				boolean cleared = false; 
		
			    JPanel myPanel = new JPanel();
			    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			    myPanel.add(new JLabel("Clear ALL tables?"));
			    myPanel.add(new JLabel("(will roll back on error)"));
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
	
			    int result = JOptionPane.showConfirmDialog(null, myPanel, "Confirmation", JOptionPane.YES_NO_OPTION);
	
			    if (result == JOptionPane.YES_OPTION) {
			    	cleared = admTrans.clearAllTables();
			    }
			    else{return;}
			    if(cleared){
			    	JOptionPane.showMessageDialog(null, "All tables cleared successfully", "Operation successful", JOptionPane.INFORMATION_MESSAGE);
			    }
			    else{
			    	JOptionPane.showMessageDialog(null, "Failed to clear tables", "Operation failed", JOptionPane.WARNING_MESSAGE);
			    }     
				 
			}
        });
    	
    	//add listener for reset sequences button
    	resetAllSeqButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				boolean reset = false; 
		
			    JPanel myPanel = new JPanel();
			    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
			    myPanel.add(new JLabel("Reset ALL sequences?"));
			    myPanel.add(new JLabel("(will roll back on error)"));
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
	
			    int result = JOptionPane.showConfirmDialog(null, myPanel, "Confirmation", JOptionPane.YES_NO_OPTION);
	
			    if (result == JOptionPane.YES_OPTION) {
			    	reset = admTrans.resetAllSequences();
			    }
			    else{return;}
			    if(reset){
			    	JOptionPane.showMessageDialog(null, "Successfully reset all sequences", "Operation successful", JOptionPane.INFORMATION_MESSAGE);
			    }
			    else{
			    	JOptionPane.showMessageDialog(null, "Failed to reset sequences", "Operation failed", JOptionPane.WARNING_MESSAGE);
			    }     
				 
			}
        });
    	
    	//add listener for seed tables button
    	seedAllTblButton.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {

				boolean seeded = false; 
		
			    JTextField numBorrowers = new JTextField(5);
			    JTextField numBooks = new JTextField(5);			    
				JPanel myPanel = new JPanel();
			    
			    myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));			    
			    myPanel.add(new JLabel("Number of borrowers to generate: "));
			    myPanel.add(numBorrowers);
			    myPanel.add(new JLabel("Number of books to generate: "));
			    myPanel.add(numBooks);
			    myPanel.add(Box.createHorizontalStrut(15)); // a spacer
			    myPanel.add(new JLabel("NOTE: SEEDING TABLE WILL RESET ALL SEQUENCES AND REMOVE ALL PRESENT DATA"));
	
			    int result = JOptionPane.showConfirmDialog(null, myPanel, "Enter parameters for seeding", JOptionPane.YES_NO_OPTION);
	
			    if (result == JOptionPane.YES_OPTION && Integer.parseInt(numBorrowers.getText()) > 0) {
			    	//minus 1 from numBorrowers because one special borrower will be added by hardcode
			    	seeded = admTrans.seedAllTables(Integer.parseInt(numBorrowers.getText())-1, Integer.parseInt(numBooks.getText()));
			    }
			    else if(result == JOptionPane.NO_OPTION){
			    	return;
			    }
			    else if(Integer.parseInt(numBorrowers.getText()) <= 0){
			    	JOptionPane.showMessageDialog(null, "Number of Borrowers must be greater than 0", "Invalid input", JOptionPane.WARNING_MESSAGE);
			    }
			    else{return;}
			    if(seeded){
			    	JOptionPane.showMessageDialog(null, "Successfully seeded all tables", "Operation successful", JOptionPane.INFORMATION_MESSAGE);
			    }
			    else{
			    	JOptionPane.showMessageDialog(null, "Failed to seed tables", "Operation failed", JOptionPane.WARNING_MESSAGE);
			    }     
				 
			}
        });
    	
		adminPanel.add(createAllTblAndSeqButton);
    	adminPanel.add(dropAllTblAndSeqButton);
    	adminPanel.add(clearAllTblButton);
    	adminPanel.add(resetAllSeqButton);
    	adminPanel.add(seedAllTblButton);
    }
    /*Helper for printBorrowingsButton: Converts the resultSet from libTrans.generateBorrowingsReport() into a table model*/
    DefaultTableModel borrowingsRSToTableModel(DefaultTableModel model,ResultSet row) throws SQLException
    {    	   	
    	ResultSetMetaData meta= row.getMetaData();
	    if(model==null) model= new DefaultTableModel();
	    String cols[]=new String[meta.getColumnCount()+2]; //+2 because we're not using the last RS column, but will be adding two extra columns of our own at the end
	    for(int i=0;i< cols.length-2;++i)  //length-2 because discarding last column of RS (booktimelimit)
	    	{
	    		cols[i]= meta.getColumnLabel(i+1);
	    	}
	    	cols[cols.length-2] = "Due on";  //Set second last column's label to "Due on"
	    	cols[cols.length-1] = "Overdue?";  //Set second last column's label to "Overdue?"
	    	model.setColumnIdentifiers(cols);
	
		while(row.next()){
				Object data[]= new Object[cols.length];
				
				//For the given row, fetch data from every RS column except the last.
				//This leaves the last 2 table columns empty
				for(int i=0;i< data.length-2;++i){
			    		data[i]=row.getObject(i+1);
			   	}
				
				//For the second last table column, calculate and output dueDate based on booktimelimit
				SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );		
				Date currentDate = new Date();
				Date outDate = row.getDate("Out");
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(outDate.getTime());
				cal.add(Calendar.DAY_OF_MONTH, row.getInt("booktimelimit"));
				Date dueDate = new Date(cal.getTimeInMillis());
				data[cols.length-2]	=  dateFormat.format(dueDate); //formats dueDate to specified format
				
				//For the very last table column, determine if borrowing is overdue
				if(dueDate.before(currentDate)){ 
					data[cols.length-1] = true;
				}
				else{
					data[cols.length-1] = false;
				}
					
				model.addRow(data);
		}
		return model;
	}
    
    /*Helper for buildTable: Converts a Top Books resultSet into a table model*/
    DefaultTableModel topBooksRSToTableModel(DefaultTableModel model,ResultSet row) throws SQLException
    {
	    ResultSetMetaData meta= row.getMetaData();
	    if(model==null) model= new DefaultTableModel();
	    String cols[]=new String[meta.getColumnCount()];
	    cols[0] = "Call Number";
	    cols[1] = "Title";
	    cols[2] = "No. Borrowings";
	
	    model.setColumnIdentifiers(cols);
	
		while(row.next()){
				Object data[]= new Object[cols.length];
				for(int i=0;i< data.length;++i){
			    		data[i]=row.getObject(i+1);
			   	}
					model.addRow(data);
		}
		return model;
	}
    
    /*Helper for buildTable: Converts a Books Out resultSet into a table model*/
    DefaultTableModel booksOutRSToTableModel(DefaultTableModel model,ResultSet row) throws SQLException
    {
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		Date outDate;
		Calendar cal;
		java.sql.Date dueDate;
		
    	ResultSetMetaData meta= row.getMetaData();
	    if(model==null) model= new DefaultTableModel();
	    String cols[]=new String[meta.getColumnCount()+1];
	    for(int i=0;i< cols.length-1;++i)
	    	{
	    	cols[i]= meta.getColumnLabel(i+1);
	    	}
	    cols[meta.getColumnCount()] = "Due Date"; 
	    model.setColumnIdentifiers(cols);
	
		while(row.next()){
				Object data[]= new Object[cols.length];
				//determine due date for item
				outDate = row.getDate("outdate");
				cal = Calendar.getInstance();
				cal.setTimeInMillis(outDate.getTime()); //set cal's time to outDate's time
				cal.add(Calendar.DATE, row.getInt("bookTimeLimit")); //add days to cal to get due date
				dueDate = new java.sql.Date(cal.getTimeInMillis()); //convert due date to sql Date object
				//if item is overdue, add data to row, and add row to table
				if(dueDate.before(new java.sql.Date(System.currentTimeMillis()))){ 
					data[data.length-1] = dateFormat.format(dueDate);
					for(int i=0;i< data.length-1;i++){
						data[i]=row.getObject(i+1);				
					}
					model.addRow(data);
				}			

		}
		return model;
	}    
    
    /*Helper for buildTable: Converts a resultSet into a table model*/
    DefaultTableModel resultSetToTableModel(DefaultTableModel model,ResultSet row) throws SQLException
    {
	    ResultSetMetaData meta= row.getMetaData();
	    if(model==null) model= new DefaultTableModel();
	    String cols[]=new String[meta.getColumnCount()];
	    for(int i=0;i< cols.length;++i)
	    	{
	    	cols[i]= meta.getColumnLabel(i+1);
	    	}
	
	    	model.setColumnIdentifiers(cols);
	
		while(row.next()){
				Object data[]= new Object[cols.length];
				for(int i=0;i< data.length;i++){
						data[i]=row.getObject(i+1);				
			   	}
					model.addRow(data);
		}
		return model;
	}    
    
    
    //build and display a table based on a given ResultSet
    public void buildTable(String title, final ResultSet rs){   
    	DefaultTableModel tModel = null;
		try {
			tModel = resultSetToTableModel(new DefaultTableModel(),rs);
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(null, "SQL Error:\n " + e1.getMessage() + "\nPlease try again", "Error Detected", JOptionPane.ERROR_MESSAGE);
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}  	  	
		//Clear Scroll Panel and update with new table
		tableScrollPane.removeAll();
		tablePanel.removeAll();
		tableScrollPane2.removeAll();
		tablePanel2.removeAll();
		tableScrollPane3.removeAll();
		tablePanel3.removeAll();
        table = new JTable(tModel);    
        tableScrollPane = new JScrollPane(table);
		tablePanel.add(new Label(title));
        tablePanel.add(tableScrollPane);        
        mainWindow.pack();
    }
    
    //build and display a table based on a given Table Model
    public void buildTable(String title, DefaultTableModel tModel){   	  	
		//Clear Scroll Panel and update with new table
		tableScrollPane.removeAll();
		tablePanel.removeAll();
		tableScrollPane2.removeAll();
		tablePanel2.removeAll();
		tableScrollPane3.removeAll();
		tablePanel3.removeAll();
        table = new JTable(tModel);    
        tableScrollPane = new JScrollPane(table);
		tablePanel.add(new Label(title));
        tablePanel.add(tableScrollPane);        
        mainWindow.pack();
    }
    
    //build and display a table for overdue items based on a given Table Model
    public void buildOverdueTable(String title, DefaultTableModel tModel){   	  	
		//Clear Scroll Panel and update with new table
		tableScrollPane.removeAll();
		tablePanel.removeAll();
		tableScrollPane2.removeAll();
		tablePanel2.removeAll();
		tableScrollPane3.removeAll();
		tablePanel3.removeAll();
        table = new JTable(tModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new MouseListener(){            

			@Override
			//On click, ask to send email to corresponding account
			public void mouseClicked(MouseEvent e) {
				JPanel myPanel = new JPanel();				
				int row = table.getSelectedRow();				
				String message = "Email sent to: "; 
				String email = (String)table.getValueAt(row, 1);
				myPanel.add(new Label("Send Email to "+email+"?"));
				 int result = JOptionPane.showConfirmDialog(null, myPanel, "Confirm send email", JOptionPane.OK_CANCEL_OPTION);
				if(result == JOptionPane.OK_OPTION)
					JOptionPane.showMessageDialog(null, message+email, "Emails Sent", JOptionPane.INFORMATION_MESSAGE);
			}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

          });
        tableScrollPane = new JScrollPane(table);
		tablePanel.add(new Label(title));
        tablePanel.add(tableScrollPane);        
        mainWindow.pack();
    }
    
    //Construct all 3 Tables
	public void buildThreeTables(String title1, String title2, String title3, final ResultSet rs1, final ResultSet rs2, final ResultSet rs3){   
		DefaultTableModel tModel1 = null;
		DefaultTableModel tModel2 = null;
		DefaultTableModel tModel3 = null;
		try {
			tModel1 = resultSetToTableModel(new DefaultTableModel(),rs1);
			tModel2 = resultSetToTableModel(new DefaultTableModel(),rs2);
			tModel3 = resultSetToTableModel(new DefaultTableModel(),rs3);
		} catch (SQLException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}  	  	
		
		//Clear Scroll Panel and update with new table
		tableScrollPane.removeAll();
		tablePanel.removeAll();
		table = new JTable(tModel1);    
		tableScrollPane = new JScrollPane(table);
		tablePanel.add(new Label(title1));
		tablePanel.add(tableScrollPane);
		
		//Clear Scroll Panel and update with new table
		tableScrollPane2.removeAll();
		tablePanel2.removeAll();
		table2 = new JTable(tModel2);    
		tableScrollPane2 = new JScrollPane(table2);
		tablePanel2.add(new Label(title2));
		tablePanel2.add(tableScrollPane2); 
		
		//Clear Scroll Panel and update with new table
		tableScrollPane3.removeAll();
		tablePanel3.removeAll();
		table3 = new JTable(tModel3);    
		tableScrollPane3 = new JScrollPane(table3);
		tablePanel3.add(new Label(title3));
		tablePanel3.add(tableScrollPane3); 
				
		mainWindow.pack();
	}
	
    
    class MainFrame extends JFrame
    {
        public MainFrame(String title)
        {
           // call the superclass constructor
           super(title);

           // this line terminates the program when the X button
           // located at the top right hand corner of the
           // window is clicked
           addWindowListener(new WindowAdapter()
             {
               public void windowClosing(WindowEvent e)
               {
                 System.exit(0);
               }
             });
        }
    }      
    
    public static void main(String args[])
    {
      TheGUI b = new TheGUI();
    }
   
}
