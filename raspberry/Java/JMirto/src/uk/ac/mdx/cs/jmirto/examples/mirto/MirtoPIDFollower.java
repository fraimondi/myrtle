/*
 * A simple program just to print the values of the IR sensors every 1 second
 */

package uk.ac.mdx.cs.jmirto.examples.mirto;

import uk.ac.mdx.cs.jmirto.JMirtoRobot;

public class MirtoPIDFollower {
	
	private int cutOffIR = 40;
	
	private int PWR = 90;
	
	private int freq = 50; // frequency of updates;
	private int maxDelta = PWR; // max correction

	private double Kp = 0.03;
	private double Kd = 1.4;
	private double Ki = 0.0001;
	
	private double curError = 2000;
	private double prevError = 2000;

	
	private int cutIR(int in) {
		if ( in < cutOffIR ) {
			return 0;
		} else {
			return in;
		}
	}
	
	private double computeError(int left, int middle, int right, double previous) {
		if ( (left+right+middle) == 0 ) {
			return previous;
		} else {
			return ( (middle*2000 + right*4000) / (left + middle + right) );
		}
	}
	
	
	public void navigate() {
		
		JMirtoRobot robot = new JMirtoRobot("/dev/tty.usbmodem1411");

		try {
			System.out.println("Starting in 2 seconds...");
			Thread.sleep(2000);
			System.out.println("Starting now");
			
			robot.enableIR();
			
			Thread.sleep(500);
			
			long timeNow = System.currentTimeMillis();
			long oldTime = 0;
			
			double proportional = 0;
			double integral = 0;
			double derivative = 0;
			
			int correction = 0;
			
			// print IR values every interval milliseconds
			while (true) {
				timeNow = System.currentTimeMillis();
				
				if (( timeNow - oldTime) > freq) {

					int leftIR = cutIR(robot.getIR(3));
					int middleIR = cutIR(robot.getIR(2));
					int rightIR = cutIR(robot.getIR(1));
				
					curError = computeError(leftIR,middleIR,rightIR,prevError);
				
					proportional = curError - 2000;
				
					if (proportional == 0) {
						integral = 0;
					} else {
						integral += proportional;
					}
				
					derivative = proportional - ( prevError - 2000);
				
					prevError = curError;
				
					correction = (int) Math.floor(Kp*proportional + Ki*integral + Kd*derivative);
				
					int delta = correction;
				
					if (delta>maxDelta) {
						delta=maxDelta;
					} else if (delta < (-maxDelta)) {
						delta = (-maxDelta);
					}
				
					if (delta < 0) {
						robot.setMotors( (PWR+delta), -PWR);
					} else {
						robot.setMotors( PWR, -(PWR-delta) );
					}
					oldTime = timeNow;
				}
				// We do not want to flood the serial port if setting motors is
				// enabled
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MirtoPIDFollower mytest = new MirtoPIDFollower();
		mytest.navigate();
	}
}
