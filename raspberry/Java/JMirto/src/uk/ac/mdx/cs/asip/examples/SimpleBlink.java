package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;

/* 
 * @author Franco Raimondi
 * 
 * A simple board with just the I/O services.
 * The main method does a standard blink test.
 */
public class SimpleBlink extends SimpleSerialBoard {
	
	public SimpleBlink(String port) {
		super(port);
	}

	public static void main(String[] args) {
		// We could pass the port as an argument, for the moment
		// I hard-code it because I'm lazy.
		
		SimpleBlink testBoard = new SimpleBlink("/dev/tty.usbmodem1411");
		
		try {
			Thread.sleep(1000);
			testBoard.requestPortMapping();
			Thread.sleep(500);
			testBoard.setPinMode(13, AsipClient.OUTPUT);
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				testBoard.digitalWrite(13, AsipClient.HIGH);
				Thread.sleep(2000);
				testBoard.digitalWrite(13, AsipClient.LOW);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
