package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

public class DistanceService implements AsipService {

	private char serviceID = 'D';
	
	// A distance sensor has a unique ID (there may be more than one distance
	// sensor attached, each one has a different distanceID)
	private int distanceID;
	
	// The service should be attached to a client
	private AsipClient asip; 
	
	// This is the last measured distance (-1 if not initialised)
	private int lastDistance;
	
	// Some constants (see docs)
	private final char REQUEST_SINGLE_DISTANCE = 'M';
	private final char DISTANCE_EVENT = 'e';
	
	// The constructor takes the id of the distance sensor.
	public DistanceService(int id, AsipClient c) {
		this.distanceID = id;
		this.asip = c;
		this.lastDistance = -1;
	}
	
	public char getServiceID() {
		return this.serviceID;
	}

	public void setServiceID(char id) {
		this.serviceID = id;
	}

	public void requestDistance() {
		this.asip.getAsipWriter().write(this.serviceID+","+REQUEST_SINGLE_DISTANCE);
	}
	
	public void enableContinuousReporting(int interval) {
		this.asip.getAsipWriter().write(this.serviceID+","+AsipService.AUTOEVENT_REQUEST+","+interval);
	}
	
	public int getDistance() {
		return this.lastDistance;
	}
	
	public void processResponse(String message) {
		// A response for a message is something like "@D,e,1,25,35,..."
		if (message.charAt(3) != DISTANCE_EVENT) {
			// FIXME: improve error checking
			// We have received a message but it is not a distance reporting event
			System.out.println("Distance message received but I don't know how to process it: "+message);
		} else {
			this.lastDistance = Integer.parseInt(message.split(",")[3+this.distanceID]);
		}
	}
	
}
