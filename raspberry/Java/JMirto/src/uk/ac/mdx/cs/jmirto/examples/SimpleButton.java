/**
 * Franco 140404: a simple example to read a digital
 * input. See http://arduino.cc/en/tutorial/button for
 * the circuit. The button changes the state of e led 
 **/

package uk.ac.mdx.cs.jmirto.examples;

import org.firmata.Firmata;
import uk.ac.mdx.cs.jmirto.arduino.ArduinoBoard;

public class SimpleButton {
	
	final int ledPin = 13;
	final int digitalInputpin = 2;
	
	ArduinoBoard board;
	Firmata firmata;
	
	public SimpleButton() {
        board = new ArduinoBoard("/dev/tty.usbmodem1411");
        firmata = board.getFirmata();
	}
	
	/* Just an infinite loop */
	public void loop() throws Exception {
		
		firmata.pinMode(ledPin, Firmata.OUTPUT);
		firmata.pinMode(digitalInputpin, Firmata.INPUT);
		
		// We turn off the ledPin
		firmata.digitalWrite(ledPin, Firmata.LOW);
		int ledPinStatus = Firmata.LOW;
		
		int oldVal = 0;
		while (true) {
			int curVal = firmata.digitalRead(digitalInputpin);
			if ( ( curVal != oldVal ) & (curVal == Firmata.LOW ) ) {
				// The button has been released. If it was LOW, we set it
				// to HIGH (and vice-versa)
				
				if ( ledPinStatus == Firmata.LOW ) {
					firmata.digitalWrite(ledPin, Firmata.HIGH);
					ledPinStatus = Firmata.HIGH;
				} else {
					firmata.digitalWrite(ledPin, Firmata.LOW);
					ledPinStatus = Firmata.LOW;
				}
			}
			oldVal = curVal;
		}
	}
	
	public static void main(String[] args) {
		SimpleButton test = new SimpleButton();
		try {
			test.loop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
