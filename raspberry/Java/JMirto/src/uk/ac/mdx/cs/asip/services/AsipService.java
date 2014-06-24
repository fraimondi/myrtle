package uk.ac.mdx.cs.asip.services;

/*
 * @author Franco Raimondi
 * 
 * A generic interface for ASIP services.
 * 
 */
public interface AsipService {

	// Public constants for autoevent requests
	public static final char AUTOEVENT_REQUEST = 'A';
	
	// A service must have an ID.
	// A service should implement setter and getter for ID.	
	char getServiceID();
	void setServiceID(char id);
	
	// A service must specify how to process responses 
	public abstract void processResponse(String message);
	

}
