package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

// A service for servos. 

public class ServoService implements AsipService {

	private char serviceID = 'S';
	
	// A servo has a unique ID (there may be more than one servo
	// attached, each one has a different servoID)
	private int servoID;
	
	// The service should be attached to a client
	private AsipClient asip; 
	
	// The constructor takes the id of the servo.
	public ServoService(int id, AsipClient c) {
		this.servoID = id;
		this.asip = c;
	}
	
	public char getServiceID() {
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;		
	}
	public int getServoID() {
		return this.servoID;
	}
	public void setServoID(int id) {
		this.servoID = id;
	}
	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	
	public void processResponse(String message) {
		// Nothing to do here... (no response from the servo)		
	}
	
	// This method sends the message to set the servo angle
	public void setServo(int angle) {
		asip.getAsipWriter().write(serviceID+","+"W"+","+this.servoID+","+angle);
	}

}
