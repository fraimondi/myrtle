package uk.ac.mdx.cs.asip.examples;

import org.firmata.Firmata;

import uk.ac.mdx.cs.asip.AsipClient;
import uk.ac.mdx.cs.asip.AsipWriter;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/* 
 * @author Franco Raimondi
 * 
 * A simple board with just the I/O services
 */
public class SimpleBoard {

	// We need this one to communicate
	SerialPort serialPort;
	
	// The client for the aisp protocol
	AsipClient asip;
	
	public SimpleBoard(String port) {
		
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
		
		asip = new AsipClient(new SimpleWriter());
	}
	
	
    private class SimpleWriter implements AsipWriter {
        public void write(String val) {
          try {	
			serialPort.writeString(val);
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
            		String val = serialPort.readString();
            		asip.processInput(val);
				} catch (SerialPortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
    }
	
}
