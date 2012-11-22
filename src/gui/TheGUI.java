package gui;

//We need to import the java.sql package to use JDBC
import java.sql.*;

//for reading from the command line
import java.io.*;

import javax.imageio.ImageIO;
//for the login window
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
    private JTable table;
    private JPanel tablePanel;
    private JScrollPane tableScrollPane;
    //The transaction classes
    private LibrarianTransactions libTrans;
    

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
		mainWindow = new MainFrame("The Window");
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        tabbedPane = new JTabbedPane();        
        librarianPanel = new JPanel();
        librarianPanel.setLayout(new BoxLayout(librarianPanel, BoxLayout.Y_AXIS));
        clerkPanel = new JPanel();
        clerkPanel.setLayout(new BorderLayout());
        borrowerPanel = new JPanel();
        borrowerPanel.setLayout(new BorderLayout());
        tableScrollPane = new JScrollPane();
        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(tableScrollPane);
        
        //import icon for tabs
        ImageIcon icon = new ImageIcon("images/icon.png", "Pretty Icon");        
        
        //Set up Librarian, Clerk and Borrower Panels        
        createLibrarianPanel();
        createClerkPanel();
        createBorrowerPanel();
        
        //Create the tabs        
        tabbedPane.addTab("Librarian", icon, librarianPanel, "Librarian Transactions");
        tabbedPane.addTab("Clerk", icon, clerkPanel, "Clerk Transactions");
        tabbedPane.addTab("Borrower", icon, borrowerPanel, "Borrower Transactions");
        contentPanel.add(tabbedPane);
        contentPanel.add(tablePanel);         
        
        
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
    	
    }
    
    //Creates the Borrower tab
    public void createBorrowerPanel(){
    	
    }
    
    //Creates the Librarian tab
    //to refactor later
    public void createLibrarianPanel(){
    	JButton addBookButton = new JButton("Add a book");
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
				     buildTable(rs);
			     }
			     catch (NumberFormatException ex){
			    	 System.out.println("Invalid input: " + ex.getMessage());
			    	 System.out.println("Please try again.");
			     } catch (SQLException ex) {
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
          });
    	librarianPanel.add(addBookButton, BorderLayout.LINE_START);

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
				for(int i=0;i< data.length;++i){
			    		data[i]=row.getObject(i+1);
			   	}
					model.addRow(data);
		}
		return model;
	}
    
    //build and display a table based on a given ResultSet
    public void buildTable(final ResultSet rs){   
    	DefaultTableModel tModel = null;
		try {
			tModel = resultSetToTableModel(new DefaultTableModel(),rs);
		} catch (SQLException e1) {
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}  	  	
		//Clear Scroll Panel and update with new table
        tableScrollPane.removeAll();
        tablePanel.removeAll();
        table = new JTable(tModel);    
        tableScrollPane = new JScrollPane(table);
        tablePanel.add(tableScrollPane);        
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
    /**Not necessary for now, but keeping here just in case we need a menu later
    private void createMenuBar(){
    	JMenu menu, submenu;
    	JMenuItem menuItem;
    	
    	//Create the menu bar.
    	menuBar = new JMenuBar();
    	
    	menuBar.add(createLibrarianMenu());
    	
    	

    	mainWindow.setJMenuBar(menuBar);
    }
    
    private JMenu createLibrarianMenu(){
    	JMenu menu, submenu;
    	JMenuItem menuItem;
    	//Build the Librarian menu.
    	menu = new JMenu("Librarian");
    	menu.setMnemonic(KeyEvent.VK_L);
    	menu.getAccessibleContext().setAccessibleDescription("Menu for all librarian transactions");
    	
    	//Transactions
    	//Add new book
    	menuItem = new JMenuItem("Add new book",
    	                         KeyEvent.VK_N);
    	menuItem.setAccelerator(KeyStroke.getKeyStroke(
    	        KeyEvent.VK_N, ActionEvent.ALT_MASK));
    	menuItem.getAccessibleContext().setAccessibleDescription("Adds a new book");
    	menuItem.setMnemonic(KeyEvent.VK_B);
    	menuItem.addActionListener(new ActionListener()
        {            
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				
			}
          });
    	menu.add(menuItem);
    }
    **/
}
