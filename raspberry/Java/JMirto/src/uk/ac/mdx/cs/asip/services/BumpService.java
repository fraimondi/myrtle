package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

public class BumpService implements AsipService {
	
	private char serviceID = 'B';
	
	// An ir sensor has a unique ID (there may be more than one ir sensor
	// attached, each one has a different irID)
	private int bumpID;

	// The service should be attached to a client
	private AsipClient asip; 
	
	private final char TAG_BUMP_RESPONSE = 'e';

	private boolean pressed; // value for the sensor
	
	public BumpService(int id, AsipClient c) {
		this.bumpID =id;
		this.asip = c;
	}
	
	// Standard getters and setters;
	public char getServiceID() {
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;
	}
	public int geBumpID() {
		return this.bumpID;
	}
	public void setBumpID(int id) {
		this.bumpID = id;
	}
	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	// Set the reporting time to t milliseconds
	// (use t=0 to disable reporting)
	// Notice that this will affect all IR sensors
	public void setReportingInterval(int t) {
		this.asip.getAsipWriter().write(this.serviceID+","+AsipService.AUTOEVENT_REQUEST+","+t);
	}
	
	public void processResponse(String message) {
		// FIXME
		// A response for a message is something like "@B,e,2,0,1"
		if (message.charAt(3) != TAG_BUMP_RESPONSE) {
			// FIXME: improve error checking
			// We have received a message but it is not an encoder reporting event
			System.out.println("Bump message received but I don't know how to process it: "+message);
		} else {
			this.pressed = ( (Integer.parseInt(message.split(",")[this.bumpID+3]) == 1) ? false : true);
		}
	}
	
	public boolean isPressed() {
		return this.pressed;
	}
}