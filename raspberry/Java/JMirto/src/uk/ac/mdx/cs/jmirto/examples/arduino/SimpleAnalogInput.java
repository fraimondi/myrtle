/**
 * Franco 140404: a simple example to read the
 * value of a potentiomenter on pin analogInputPin
 * and then change the sleep time for blinking
 * an LED on ledPin
 * 
 **/

package uk.ac.mdx.cs.jmirto.examples.arduino;

import org.firmata.Firmata;
import uk.ac.mdx.cs.jmirto.arduino.ArduinoBoard;

public class SimpleAnalogInput {
	
	final int ledPin = 13;
	final int analogInputpin = 2;
	
	ArduinoBoard board;
	Firmata firmata;
	
	public SimpleAnalogInput() {
        board = new ArduinoBoard("/dev/tty.usbmodem1411");
        firmata = board.getFirmata();
	}
	
	/* Just an infinite loop */
	public void loop() throws Exception {
		firmata.pinMode(ledPin, Firmata.OUTPUT);
		firmata.pinMode(analogInputpin, Firmata.ANALOG);
		while (true) {
			int curVal = firmata.analogRead(analogInputpin);
			System.out.println("curVal is "+curVal);
			System.out.println("Turning ON");
			firmata.digitalWrite(ledPin, Firmata.HIGH);
			Thread.sleep(curVal+100);
			System.out.println("Turning OFF");
			firmata.digitalWrite(ledPin, Firmata.LOW);
			Thread.sleep(curVal+100);
		}
	}
	
	public static void main(String[] args) {
		SimpleAnalogInput test = new SimpleAnalogInput();
		try {
			test.loop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
