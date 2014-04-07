/*
 * A simple program just to print the values of the IR sensors every 1 second
 */

package uk.ac.mdx.cs.jmirto.examples.mirto;

import uk.ac.mdx.cs.jmirto.JMirtoRobot;

public class MirtoIRPrinter {
	
	public static void main(String[] args) {
		
		JMirtoRobot robot = new JMirtoRobot("/dev/tty.usbmodem1411");

		try {
			System.out.println("Starting in 2 seconds...");
			Thread.sleep(2000);
			System.out.println("Starting now");
			
			robot.enableIR();
			
			Thread.sleep(500);
			
			long timeNow = System.currentTimeMillis();
			long interval = 1000;
			long oldTime = 0;
			
			// print IR values every interval milliseconds
			while (true) {
				timeNow = System.currentTimeMillis();
				if ( (timeNow - oldTime) > interval ) {
					System.out.println("Left IR: " + robot.getIR(3) +
							"; Middle IR: " + robot.getIR(2) +
							"; Right IR: " + robot.getIR(1));
					oldTime = timeNow;
				}
				
				// Uncomment the following block if you want to test the wheels as well.
					
				if ( robot.getIR(3) < 500 ) {
					robot.setMotor(0, 80);
				} else {
					robot.setMotor(0, 0);
				}
				if ( robot.getIR(1) < 500 ) {
					robot.setMotor(1, 80);
				} else {
					robot.setMotor(1, 0);
				}
				
				
				// We do not want to flood the serial port if setting motors is
				// enabled
				Thread.sleep(20);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
