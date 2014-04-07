package uk.ac.mdx.cs.jmirto;

import org.firmata.Firmata;
import uk.ac.mdx.cs.jmirto.arduino.ArduinoBoard;

public class JMirtoRobot {

	ArduinoBoard board;
	Firmata firmata;
	
	public JMirtoRobot(String port) {
        board = new ArduinoBoard(port);
        firmata = board.getFirmata();

        // Enable bump switches
        // FIXME: this will probably not work
        firmata.pinMode(18, Firmata.INPUT);
        firmata.pinMode(19, Firmata.INPUT);
        
        // Enable continuous reporting
        firmata.sendSysexMessage((byte) 0x7d, (byte) 2);

	}
	
	public void shutdown() {
		board.shutdown();
	}
	// Takes a value between -100 and + 100 and
	// converts it between -255 and + 255
	public void setMotor(int wheel, int speed) {
		if ( speed > 100 ) speed = 100;
		if ( speed < -100 ) speed = -100;
		
		speed = (int) Math.floor(speed*2.55);
		firmata.sendSysexMessage( (byte) 0x7D, (byte) (wheel+5), speed);
	}
	
	public void setMotors(int w1, int w2) {
		setMotor(0,w1);
		setMotor(1,w2);
	}
	
	public void stopMotors() {
		setMotors(0,0);
	}
	
	public void enableIR() {
		firmata.pinMode(14, Firmata.INPUT);
		firmata.digitalWrite(14, Firmata.HIGH);	
	}
	
	public void disableIR() {
		firmata.pinMode(14, Firmata.OUTPUT);
		firmata.digitalWrite(14, Firmata.LOW);
	}
	
	// FIXME: this is probably not going to work
	public boolean isLeftBumpPressed() {
		if ( firmata.digitalRead(18) == Firmata.LOW ) {
			return true;
		} else {
			return false;
		}
	}
	// FIXME: this is probably not going to work
	public boolean isRightBumpPressed() {
		if ( firmata.digitalRead(18) == Firmata.LOW ) {
			return true;
		} else {
			return false;
		}
	}
	
	
	// FIXME: add check for range of num
	public int getIR(int num) {
		return firmata.analogRead(num);
	}
	
	// FIXME: add check for range of num
	public int getCount(int num) {
		return firmata.getCount(num);
	}
	
	public void requestDistance() {
		firmata.sendSysexMessage( (byte) 0x7d, (byte) 9); 
	}

	public int getDistance() {
		return firmata.getSonarDistance();
	}
	
}
