package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

public class IRService implements AsipService {
	
	private char serviceID = 'R';
	
	// An ir sensor has a unique ID (there may be more than one ir sensor
	// attached, each one has a different irID)
	private int irID;

	// The service should be attached to a client
	private AsipClient asip; 
	
	private final char TAG_IR_RESPONSE = 'e';

	private int value; // value for the sensor
	
	public IRService(int id, AsipClient c) {
		this.irID =id;
		this.asip = c;
	}
	
	// Standard getters and setters;
	public char getServiceID() {
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;
	}
	public int geIRID() {
		return this.irID;
	}
	public void setIRID(int id) {
		this.irID = id;
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
		// A response for a message is something like "@R,e,3,100,200,300"
		if (message.charAt(3) != TAG_IR_RESPONSE) {
			// FIXME: improve error checking
			// We have received a message but it is not an encoder reporting event
			System.out.println("IR message received but I don't know how to process it: "+message);
		} else {
			this.value = Integer.parseInt(message.split(",")[this.irID+3]);
		}
	}
	
	public int getIR() {
		return this.value;
	}
	
}

