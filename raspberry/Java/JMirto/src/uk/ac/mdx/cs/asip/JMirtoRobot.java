package uk.ac.mdx.cs.asip;

import uk.ac.mdx.cs.asip.services.BumpService;
import uk.ac.mdx.cs.asip.services.EncoderService;
import uk.ac.mdx.cs.asip.services.IRService;
import uk.ac.mdx.cs.asip.services.MotorService;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class JMirtoRobot {
	
	// For debugging
	boolean DEBUG = true;

	// The ASIP board is normally attached to a serial port (provided by jssc)
	SerialPort serialPort;
	
	// The client for the aisp protocol
	AsipClient asip;

	// The robot has 2 motors (wheels), 2 encoders, 3 IR sensors, 2 bump sensors
	
	private MotorService m0, m1;
	private EncoderService e0, e1;
	private IRService ir0,ir1,ir2;
	private BumpService b0,b1;
	
	
	
	// This constructor takes the name of the serial port and it
	// creates the serialPort object and the asip client.
	// We then attach a listener to the serial port with SerialPortReader; this
	// listener calls the aisp method to process input.
	public JMirtoRobot(String port) {
	
		serialPort = new SerialPort(port);

		asip = new AsipClient(new SimpleWriter());
		
		try {
			serialPort.openPort();// Open port
			serialPort.setParams(57600, 8, 1, 0);// Set params
			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS
					+ SerialPort.MASK_DSR;// Prepare mask
			serialPort.setEventsMask(mask);// Set mask
			serialPort.addEventListener(new SerialPortReader());// Add
																// SerialPortEventListener
		} catch (SerialPortException ex) {
			System.out.println(ex);
		}

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (DEBUG) {
			System.out.println("DEBUG: Serial port configured and ASIP client ready");
		}

	}
	
	// We set up things here, attaching services etc.
	public void setup() {

		// Adding two motors.
		m0 = new MotorService(0, this.asip);
		m1 = new MotorService(1, this.asip);
		this.asip.addService('M', m0);
		this.asip.addService('M', m1);		
		if (DEBUG) {
			System.out.println("DEBUG: 2 motor services added");
		}
		
		// Adding two encoders
		e0 = new EncoderService(0, this.asip);
		e1 = new EncoderService(1, this.asip);
		e0.setReportingInterval(25);
		e1.setReportingInterval(25);
		this.asip.addService('E', e0);
		this.asip.addService('E', e1);
		if (DEBUG) {
			System.out.println("DEBUG: 2 encoder services added");
		}
		
		// Adding 3 IR sensors
		ir0 = new IRService(0, this.asip);
		ir1 = new IRService(1, this.asip);
		ir2 = new IRService(2, this.asip);
		ir0.setReportingInterval(25);
		ir1.setReportingInterval(25);
		ir2.setReportingInterval(25);
		this.asip.addService('R', ir0);
		this.asip.addService('R', ir1);
		this.asip.addService('R', ir2);
		if (DEBUG) {
			System.out.println("DEBUG: 3 IR services added");
		}
		
		// Adding two bumpers
		b0 = new BumpService(0, this.asip);
		b1 = new BumpService(1, this.asip);
		b0.setReportingInterval(25);
		b1.setReportingInterval(25);
		this.asip.addService('B', b0);
		this.asip.addService('B', b1);
		if (DEBUG) {
			System.out.println("DEBUG: 2 bumper services added");
		}
		
	}
	
	// Setting the two motors speed
	public void setMotors(int s0, int s1) {
		m0.setMotor(s0);
		m1.setMotor(s1);
		if (DEBUG) {
			System.out.println("DEBUG: setting motors to ("+s0+","+s1+")");
		}
	}
	
	public void stopMotors() {
		m0.stopMotor();
		m1.stopMotor();
	}
	
	public int getIR(int i) {
		// Franco, this is horrible code, IR should be a list!
		// FIXME
		switch (i) {

		case 0: 
			return ir0.getIR();
			
		case 1:
			return ir1.getIR();
		
		case 2:
			return ir2.getIR();
		
		default: 
			return -1;	
		}
	}
	
	
	public int getCount(int i) {
		// As above, this is horrible code.
		// FIXME
		switch (i) {

		case 0: 
			return e0.getCount();
			
		case 1:
			return e1.getCount();

		default: 
			return -1;	
		}
	}
	
	public boolean isPressed(int i) {
		// As above, this is horrible code.
		// FIXME
		switch (i) {
		case 0: 
			return b0.isPressed();
			
		case 1:
			return b1.isPressed();

		default: 
			return false;	
		}			
	}

	// As described above, SimpleSerialBoard writes messages to
	// the serial port.
    private class SimpleWriter implements AsipWriter {
        public void write(String val) {
          try {	
			serialPort.writeString(val);
			serialPort.writeString("\n");
          } catch (SerialPortException e) {
        	  // TODO Auto-generated catch block
        	  e.printStackTrace();
          }	
        }
    }
	
	// A class for a listener that calls the processInput method of
	// the AispClient.
	private class SerialPortReader implements SerialPortEventListener {
		
		private String buffer = ""; // we store partial messages here.
		
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
            	try {
            		String val = serialPort.readString();
            		//System.out.println("DEBUG: received on serial: "+val);
            		if ( val.contains("\n")) {
            			// If there is at least one newline, we need to process
            			// the message (the buffer may contain previous characters).
            			while (val.contains("\n") && (val.length()>0)) {
            				// But remember that there could be more than one newline 
            				// in the buffer
            				buffer += val.substring(0,val.indexOf("\n"));
            				//System.out.println("DEBUG: processing "+buffer);
            				asip.processInput(buffer);
            				buffer = "";
            				val = val.substring(val.indexOf("\n")+1);
            			}
            			// If there is some leftover to process we add tu buffer
            			if (val.length() > 0 ) {
            				buffer = val;
            			}
            		} else {
            			buffer += val;
            		}
				} catch (SerialPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
	
	// A main method for testing
	public static void main(String[] args) {
		
		JMirtoRobot robot = new JMirtoRobot("/dev/tty.usbmodem1411");
//		JMirtoRobot robot = new JMirtoRobot("/dev/ttyAMA0");

		
		try {
			Thread.sleep(500);
			robot.setup();
			Thread.sleep(500);	
			while (true) {
				System.out.println("IR: "+robot.getIR(0) + ","+robot.getIR(1)+","+robot.getIR(2));
				System.out.println("Encoders: "+robot.getCount(0) + ","+robot.getCount(1));
				System.out.println("Bumpers: "+robot.isPressed(0) + ","+robot.isPressed(1));
				System.out.println("Setting motors to 50,50");
				robot.setMotors(250, 250);
				Thread.sleep(1500);
				System.out.println("Stopping motors");
				robot.stopMotors();
				Thread.sleep(1500);
			}
/*			System.out.println("Setting motors to 50,50");
			robot.setMotors(50, 50);
			Thread.sleep(3000);
			System.out.println("Stopping motors");
			robot.stopMotors();
			Thread.sleep(500);
			System.out.println("Setting motors to 80,-80");
			robot.setMotors(80, -80);
			Thread.sleep(3000);
			System.out.println("Stopping motors");
			robot.stopMotors();
			Thread.sleep(3000);
			System.out.println("Setting motors to -100,100");
			robot.setMotors(-100, 100);
			Thread.sleep(3000);
			System.out.println("Stopping motors");
			robot.stopMotors();
			System.out.println("All done, see you soon!");
*/
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
