package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.SimpleSerialBoard;

/* 
 * @author Franco Raimondi
 * 
 * A simple board with just the I/O services.
 * The main method does a standard blink test.
 */
public class SimpleBoard extends SimpleSerialBoard {
	
	public SimpleBoard(String port) {
		super(port);
	}

	public static void main(String[] args) {
		// We could pass the port as an argument, for the moment
		// I hard-code it because I'm lazy.
		
		SimpleBoard testBoard = new SimpleBoard("/dev/tty.usbmodem1411");
		
		testBoard.setPinMode(52, AsipClient.OUTPUT);

		while(true) {
			try {
				testBoard.digitalWrite(52, AsipClient.HIGH);
				Thread.sleep(1000);
				testBoard.digitalWrite(52, AsipClient.LOW);
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
