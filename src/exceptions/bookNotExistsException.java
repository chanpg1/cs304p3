package exceptions;

public class bookNotExistsException extends Exception {
	private String callNum = null;
	
    public bookNotExistsException(){
    	
    }
    
    public bookNotExistsException(String cn){
    	callNum = cn;
    }
    
    public String toString(){
        if(callNum.isEmpty())
        	return "Book does not exist" ;
        else
        	return "Book with callNum '" + callNum + "' not in library database";
    }
    
    public String getMessage(){
    	if(callNum.isEmpty())
        	return "Book does not exist" ;
        else
        	return "Book with callNum '" + callNum + "' not in library database";
    }
}
