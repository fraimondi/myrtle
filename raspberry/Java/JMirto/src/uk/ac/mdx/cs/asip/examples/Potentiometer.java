/**
 * 
 */
package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;

/**
 * @author Mike Bottone
 * this class provides a simple example of how it is possible to read analog values into the Arduino
 * in this case, the readings we get control the rate at which the LED blinks.
 */

public class Potentiometer extends SimpleSerialBoard {

	
	public Potentiometer(String port) {
		super(port);
	}
	
	public static void main(String[] args) {
		
		int potPin = 2 ; // the number for the potentiometer pin on the Arduino
		int ledPin = 13;  // the number for the LED pin on the Arduino
		
		int potValue = 0; // initialise the variable storing the potentiometer readings
		
	
		// We could pass the port as an argument, for the moment
		// I hard-code it because I'm lazy.
	
		Potentiometer testBoard = new Potentiometer("/dev/tty.usbmodem1411");
		
		testBoard.requestPortMapping();
		testBoard.setAutoReportInterval(50);
		testBoard.setPinMode(ledPin, AsipClient.OUTPUT);  // declare the LED pin as an output
		testBoard.setPinMode(potPin+14, AsipClient.ANALOG);
		
		// Set the LED to blink with a delay  based on the current value of the potentiometer
		while (true) {
						
			potValue = testBoard.analogRead(potPin);     			// read the potentiometer
			
			testBoard.digitalWrite(ledPin, AsipClient.HIGH);	 	 // turn the LED pin on
				try {
					Thread.sleep(200+potValue); 							// then stop the program for some time
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}           				     	
			testBoard.digitalWrite(ledPin, AsipClient.LOW);   	// turn the LED pin off
				try {
					Thread.sleep(200+potValue);						// then stop the program for some time
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}             				    	 
			}			
		}
		
	}
		
	
	