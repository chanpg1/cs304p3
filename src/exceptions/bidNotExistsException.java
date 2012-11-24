package exceptions;

public class bidNotExistsException extends Exception {
	private int bid = -1;
	
    public bidNotExistsException(){
    	
    }
    
    public bidNotExistsException(int bid){
    	this.bid = bid;
    }
    
    public String toString(){
        if(bid < 0)
        	return "Invalid Borrower ID: not found in library database" ;
        else
        	return "Invalid Borrower ID: '" + bid + "' not in library database";
    }
    
    public String getMessage(){
    	 if(bid < 0)
         	return "Invalid Borrower ID: not found in library database" ;
         else
         	return "Invalid Borrower ID: '" + bid + "' not in library database";
    }
}
