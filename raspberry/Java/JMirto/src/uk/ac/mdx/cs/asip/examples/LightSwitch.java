package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;

/* 
 * @author Mike Bottone
 * 
 * A simple board with just the I/O services on a fixed port.
 * The main method simulates a light switch.
 */

public class LightSwitch extends SimpleSerialBoard {

	
	public LightSwitch(String port) {
		super(port);
	}
	
	public static void main(String[] args) {
		
		int buttonPin = 2; // the number for the pushbutton pin on the Arduino
		int ledPin = 13;  // the number for the LED pin on the Arduino
		
		int buttonState = 0; // initialise the variable for when we press the button
		
	
		// We could pass the port as an argument, for the moment
		// I hard-code it because I'm lazy.
	
		LightSwitch testBoard = new LightSwitch("/dev/tty.usbmodemfd511");
		
		testBoard.requestPortMapping();
		testBoard.setPinMode(ledPin, AsipClient.OUTPUT);
		testBoard.setPinMode(buttonPin, AsipClient.INPUT);

		// read the current state of the button
		while (true) {
			buttonState = testBoard.digitalRead(buttonPin);

			// check if the button is pressed and the corresponding state is
			// HIGH (1)
			// FIXME: we should check for state changed, otherwise we flood the
			// channel! For the moment I add a sleep(20) below.

			if (buttonState == AsipClient.HIGH) {

				testBoard.digitalWrite(ledPin, AsipClient.HIGH);
				// Thread.sleep(500); }
			} else {
				testBoard.digitalWrite(ledPin, AsipClient.LOW); // we turn it
																// off otherwise
			}
			;

			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		
	}
		
	
	}


