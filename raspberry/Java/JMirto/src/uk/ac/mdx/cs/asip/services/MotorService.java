package uk.ac.mdx.cs.asip.services;

import uk.ac.mdx.cs.asip.AsipClient;

public class MotorService implements AsipService {

	private char serviceID = 'M';
	
	// A motor has a unique ID (there may be more than one motor
	// attached, each one has a different motorID)
	private int motorID;
	
	// The service should be attached to a client
	private AsipClient asip; 
	
	private final char TAG_SET_MOTOR_SPEED = 'm';
	
	public MotorService(int id, AsipClient c) {
		this.motorID = id;
		this.asip = c;
	}
	
	// Standar getters and setters;
	public char getServiceID() {
		// TODO Auto-generated method stub
		return this.serviceID;
	}
	public void setServiceID(char id) {
		this.serviceID = id;
	}
	public int getMotorID() {
		return this.motorID;
	}
	public void setMotorID(int id) {
		this.motorID = id;
	}
	public void setClient(AsipClient c) {
		this.asip = c;
	}
	public AsipClient getClient() {
		return this.asip;
	}
	
	public void processResponse(String message) {
		// Do nothing for motors.
	}
	
	public void setMotor(int speed) {
		// Speed should be between -100 and +100
		if (speed > 255 ) {
			speed = 255;
		}
		if (speed < -255 ) {
			speed = -255;
		}
		System.out.println("Setting motor "+this.motorID+" to "+speed+"...");
		asip.getAsipWriter().write(serviceID+"," 
									+ TAG_SET_MOTOR_SPEED+
									"," + this.motorID + "," + speed);
		System.out.println("Done");
	}
	
	// Stop the motor (just set speed to 0)
	public void stopMotor() {
		this.setMotor(0);
	}

}
