/**
 * Franco 140404: an ArduinoBoard puts together a jssc serial port
 * and a Firmata object. The serialPort has a listener that calls
 * firmata.processInput; Firmata uses a FirmataWriter that writes 
 * on the serial port using jssc.
 * 
 * To send a Firmata message / query Firmata values, a user should get
 * the Firmata instance using getFirmata
 */

package uk.ac.mdx.cs.jmirto.arduino;


import org.firmata.Firmata;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


public class ArduinoBoard {

	SerialPort serialPort;
	Firmata firmata;
		
	/* The constructor needs the port name */
	/* TODO: add automatic port recognition, similar to Racket? */
	public ArduinoBoard(String port) {
		firmata = new Firmata(new FirmataWriter());
		serialPort = new SerialPort(port);

        try {
            serialPort.openPort();//Open port
            serialPort.setParams(57600, 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        }	
        
        try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        firmata.init();
	}
	
	public Firmata getFirmata() {
		return firmata;
	}
	
    private class FirmataWriter implements Firmata.Writer {
        public void write(int val) {
          try {	
			serialPort.writeInt(val);
          } catch (SerialPortException e) {
        	  // TODO Auto-generated catch block
        	  e.printStackTrace();
          }	
        }
    }
    
    
	private class SerialPortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
            	try {
					byte buffer[] = serialPort.readBytes(event.getEventValue());
//					System.out.println("DEBUG: buffer length is "+buffer.length);
					for (byte b: buffer) {
//						System.out.println("DEGUB: got byte "+(b & 0xFF));
						firmata.processInput(b);
					}
				} catch (SerialPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
	
	public void shutdown() {
		try {
			serialPort.closePort();
			Thread.sleep(500);
			serialPort = null;
		} catch (SerialPortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
