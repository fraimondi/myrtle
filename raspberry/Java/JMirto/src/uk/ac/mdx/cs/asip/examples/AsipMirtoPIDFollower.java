package uk.ac.mdx.cs.asip.examples;

import uk.ac.mdx.cs.asip.JMirtoRobot;


public class AsipMirtoPIDFollower {
	private int cutOffIR = 40;
	
	private int PWR = 95;
	
	private int freq = 35; // frequency of updates;
	private int maxDelta = PWR; // max correction

	private double Kp = 0.050;
	private double Kd = 1.6;
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
		
		JMirtoRobot robot = new JMirtoRobot("/dev/ttyAMA0");
//		JMirtoRobot robot = new JMirtoRobot("/dev/tty.usbmodem1411");
		
		try {
			System.out.println("Setting up in 2 seconds...");
			Thread.sleep(2000);
			System.out.println("Starting now");

			robot.setup();
			
			System.out.println("Robot set up completed");
			
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

					int leftIR = cutIR(robot.getIR(2));
					int middleIR = cutIR(robot.getIR(1));
					int rightIR = cutIR(robot.getIR(0));
				
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
						robot.setMotors( (int) (2.55*(PWR+delta)), (int) (2.55*(-PWR)));
					} else {
						robot.setMotors( (int) (2.55*PWR), (int) (-(PWR-delta)*2.55) );
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
		AsipMirtoPIDFollower mytest = new AsipMirtoPIDFollower();
		mytest.navigate();
	}
}
