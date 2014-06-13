package uk.ac.mdx.cs.asip;

/*
 * @author Franco Raimondi
 * 
 * A generic interface for ASIP services.
 * 
 */
public interface AsipService {

	// A service must have an ID.
	char serviceID = 'X';
	
	// A service should implement setter and getter for ID.
	char getID();
	void setID(char id);
	
	// A service must specify how to process responses 
	public abstract void processResponse(String message);
	

}
