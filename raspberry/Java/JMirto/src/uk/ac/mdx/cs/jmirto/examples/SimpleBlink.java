/**
 * Franco 140404: the old classic blink on pin 13
 */

package uk.ac.mdx.cs.jmirto.examples;

import org.firmata.Firmata;
import uk.ac.mdx.cs.jmirto.arduino.ArduinoBoard;

public class SimpleBlink {
	
	final int pin = 13;
	
	ArduinoBoard board;
	Firmata firmata;
	
	public SimpleBlink() {
        board = new ArduinoBoard("/dev/tty.usbmodem1411");
        firmata = board.getFirmata();
	}
	
	/* Just an infinite loop */
	public void blink() throws Exception {
		firmata.pinMode(pin, Firmata.OUTPUT);
		while (true) {
			System.out.println("Turning ON");
			firmata.digitalWrite(pin, Firmata.HIGH);
			Thread.sleep(1000);
			System.out.println("Turning OFF");
			firmata.digitalWrite(pin, Firmata.LOW);
			Thread.sleep(1000);
		}
	}
	
	public static void main(String[] args) {
		SimpleBlink test = new SimpleBlink();
		try {
			test.blink();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
