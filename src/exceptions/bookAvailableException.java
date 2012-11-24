package exceptions;

public class bookAvailableException extends Exception {
	private String callNum = null;
	 
    public bookAvailableException(){
        
    }
    
    public bookAvailableException(String cn){
    	callNum = cn;
    }
    
    public String toString(){
        if(callNum.isEmpty())
        	return "Book is still in" ;
        else
        	return "Book with callNum '" + callNum + "' is still in";
    }
    
    public String getMessage(){
        if(callNum.isEmpty())
        	return "Book is still in" ;
        else
        	return "Book with callNum '" + callNum + "' is still in";
    }
}
