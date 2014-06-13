package uk.ac.mdx.cs.asip;

import java.util.HashMap;

/*
 * @author Franco Raimondi
 * A Java client for the Arduino service interface protocol
 */
public class AsipClient {
	
	
	/************   BEGIN CONSTANTS DEFINITION ****************/
	private boolean DEBUG = true; // Do you want me to print verbose debug information?
	
	private final int MAX_NUM_DIGITAL_PINS = 72; // 9 ports of 8 pins at most?
	private final int MAX_NUM_ANALOG_PINS = 16; // Just a random number...
	
	// Low-level tags for I/O service:
	private final char IO_SERVICE 		= 'I'; // tag indicating message is for I/O service
	private final char PIN_MODE     	= 'P'; // i/o request  to Arduino to set pin mode
	private final char DIGITAL_WRITE	= 'D'; // i/o request  to Arduino is digitalWrite
	private final char ANALOG_WRITE  	= 'A'; // i/o request to Arduino is analogWrite)
	private final char PORT_DATA     	= 'p'; // i/o event from Arduino is digital port data
	private final char ANALOG_VALUE  	= 'a'; // i/o event from Arduino is value of analog pin
	private final char PORT_MAPPING		= 'M'; // i/o event from Arduino is port mapping to pins

	// Pin modes (these are public)
    public static final int INPUT  			=	0x00; 	// defined in Arduino.h
    public static final int OUTPUT 			= 	0x01; 	// defined in Arduino.h
    public static final int INPUT_PULLUP 	=  	0x02; 	// defined in Arduino.h
    public static final int ANALOG 			=	0x03; 	// analog pin in analogInput mode
    public static final int PWM				=	0x04; 	// digital pin in PWM output mode
    public static final int SERVO 			=	0x05;   // digital pin in servo mode
    public final int OTHER					=	0xFF;   // High level device not handled at pin level,
    													// this is the default mode for any pin not 
    													// explicitly set to one of the other modes
    
    private final char EVENT_HANDLER 			= '@'; // Standard incoming message
    private final char ERROR_MESSAGE_HEADER 	= '~'; // Incoming message: error report
    
    private final byte HIGH 	= 	1;
    private final byte LOW 		= 	0;
	/************   END CONSTANTS DEFINITION ****************/

    
	/************   BEGIN PRIVATE FIELDS DEFINITION ****************/
    private int[] digital_input_pins;
    private int[] analog_input_pins;
    private int[] pin_mode; // FIXME: do we need this at all?
    
    // We need to store that port x at position y corresponds to PIN z.
    // We store this as a map from port numbers (x) to another map 
    // position(y)->pin(z).
    // (see below the description of processPinMapping())
    private HashMap<Integer,HashMap<Integer,Integer>> portMapping;
    
    // A map from service IDs to actual implementations
    private HashMap<Character,AsipService> services;
    
    // The output channel (where we write messages). This should typically
    // be a serial port, but could be anything else.
    private AsipWriter out;
	/************   END PRIVATE FIELDS DEFINITION ****************/
 
    // A simple constructor to initialize things
    public AsipClient() {    		
    	
    	portMapping = new HashMap<Integer,HashMap<Integer,Integer>>();    	
    	digital_input_pins = new int[MAX_NUM_DIGITAL_PINS];
    	analog_input_pins = new int[MAX_NUM_ANALOG_PINS];
    	pin_mode = new int[MAX_NUM_DIGITAL_PINS+MAX_NUM_ANALOG_PINS];
    	
    	if (DEBUG) {
    		System.out.println("End of constructor: arrays and maps created");
    	}	
    } // end of default constructor
    
    // A constructor taking the writer as parameter.
    public AsipClient(AsipWriter w) {
    	this(); // calling default constructor
    	out = w;
    }
    
    /************ BEGIN PUBLIC METHODS *************/
    // This method processes an input received on the serial port.
    // See protocol description for more detailed information.
	public void processInput(String input) {
		switch (input.charAt(0)) {

		case EVENT_HANDLER:
			handleInputEvent(input);
			break;

		case ERROR_MESSAGE_HEADER:
			handleInputError(input);
			break;

		default:
			// FIXME: better error handling required!
			System.out.println("Strange character received at position 0: "
					+ input);

		}

	}
    
	/* TODO: add digitalRead() */
	
	/* TODO: add analogRead() */
	
	/* TODO: add setPinMode() */
	
	// It is possible to add services at run-time:
	public void addService(char serviceID, AsipService s) {
		services.put(serviceID,s);
	}

	// Just return the list of services
	public HashMap<Character,AsipService> getServices() {
		return services;
	}
	
	// I'm not sure we want this public... FIXME?
    public int[] getDigitalPins() { return digital_input_pins; }
    
    // Getter and Setter for output channel
    public AsipWriter getAsipWriter() { return out; };
    public void setAsipWriter(AsipWriter w) { out = w; }
    /************ END PUBLIC METHODS *************/
	
    /************ BEGIN PRIVATE METHODS *************/
    
    // A method to do what is says on the tin... 
    private void handleInputEvent(String input) {
   		if (input.charAt(1) == IO_SERVICE) {		
   			// Digital pins (in port)
		 
			/* the port data event is something like:
			@I,P,4,F	
			this message says the data on port 4 has a value of F */
			if ( input.charAt(3) == PORT_DATA) {
				// We need to process port number and bit mask for it. 
				int port = Integer.parseInt(input.substring(5,6));
				int bitmask = Integer.parseInt(input.substring(7),16); // convert to base 16
				processPortData(port,bitmask);				
			} else if ( input.charAt(3) == PORT_MAPPING ) {
				processPinMapping(input);
			}
			else {
				System.out.println("Service not recognised in position 3 for I/O service: " + input);
			}			
   		} // end of IO_SERVICE
   
   		
   		else if ( services.keySet().contains(input.charAt(1)) ) {
   			// Is this one of the services we know? If this is the case,
   			// we call it and we process the input
   			services.get(input.charAt(1)).processResponse(input);
   		}
   		
   		else {
   			// We don't know what to do with it.
			System.out.println("Event not recognised at position 1: " + input);
		}
    }
    
    // To handle a message starting with an error header (this is a
    // form of error reporting from Arduino)
    private void handleInputError(String input) {
    	// FIXME: improve error handling
    	System.out.println("Error message received: "+input);
    	
    }
    
    
    // A method to process input messages for digital pins. 
    // We get a port and a sequence of bits. The mapping between ports and pins
    // is stored in portMapping. See comments for processPinMapping for additional details.
    // FIXME: add the usual error checking etc. (Franco, you are too lazy!)
    private void processPortData(int port, int bitmask) {
    	
    	//FIXME: we should check that no data arrives before portMapping has been created initialized!
    	if (DEBUG) {
    		System.out.println("DEBUG: processPortData for port "+port+" and bitmask "+bitmask);
    	}
    	HashMap<Integer,Integer> singlePortMap = portMapping.get(port);
    	
    	for (HashMap.Entry<Integer, Integer> pinMap : singlePortMap.entrySet() ) {
    		if ( (pinMap.getKey() & bitmask) != 0x0 ) {
    			digital_input_pins[pinMap.getValue()] = HIGH;    		
    	    	if (DEBUG) {
    	    		System.out.println("DEBUG: processPortData setting pin " + pinMap.getValue() + " to HIGH");
    	    	}
    		} 
    		else {
    			digital_input_pins[pinMap.getValue()] = LOW;
    	    	if (DEBUG) {
    	    		System.out.println("DEBUG: processPortData setting pin " + pinMap.getValue() + " to LOW");
    	    	}
    		}
    	}
    	
    }
    
    // At the beginning we receive a mapping of the form:
    // @I,M,20{4:1,4:2,4:4,4:8,4:10,4:20,4:40,4:80,2:1,2:2,2:4,2:8,2:10,2:20,3:1,3:2,3:4,3:8,3:10,3:20}
    // This means that there are 20 PINs; PIN 0 is mapped in position 0 of port 4 (4:1)
    // PIN 1 is mapped in position 1 (2^1) of port 4;
    // PIN 2 is mapped in position 2 (2^2) of port 4, i.e. 4:4;
    // ...
    // PIN 4 is mapped in position 4 (2^4=16=0x10) of port 4, i.e. 4:10;
    // PIN 9 is mapped in position 2 of port 2, i.e. 2:2, etc.
    private void processPinMapping(String mapping) {
    	
    	// FIXME: maybe add a bit of error checking: check that the
    	// length corresponds to the number of PINs, etc.
    	// For the moment I take the substring comprised between "{" and "}"
    	// and I create an array of strings for each element.
    	String[] ports = mapping.substring(mapping.indexOf("{")+1,mapping.indexOf("}")).split(",");
    	    
    	// I just iterate over the array getting each port:position 
    	int curPin = 0;
    	for (String singleMapping: ports) {
    		int port = Integer.parseInt(singleMapping.split(":")[0]);
    		int position = Integer.parseInt(singleMapping.split(":")[1],16);

    		// If portMapping already contains something for this port,
    		// we add the additional position mapping
    		if (portMapping.containsKey(port)) {
    			portMapping.get(port).put(position, curPin);
    		} else {
    			// Otherwise, we create a new key in portMapping
    			HashMap<Integer,Integer> newMap = new HashMap<Integer,Integer>();
    			newMap.put(position, curPin);
    			portMapping.put(port,newMap);
    			
    		}    		
    		curPin++;
    	}
    	
    	if (DEBUG) {
    		System.out.print("DEBUG: Port bits to PIN numbers mapping: ");
    		System.out.println("DEBUG: "+portMapping);
    	}
    }
    /************ END PRIVATE METHODS *************/
    

    /************ TESTING *************/
    // A simple main method to test off-line
    public static void main(String[] args) {
    	AsipClient testClient = new AsipClient();
    	testClient.processInput("@I,M,20{4:1,4:2,4:4,4:8,4:10,4:20,4:40,4:80,2:1,2:2,2:4,2:8,2:10,2:20,3:1,3:2,3:4,3:8,3:10,3:20}"); 
    	testClient.processInput("@I,p,4,F");
       	testClient.processInput("@I,p,4,10");
       	testClient.processInput("@I,p,4,FF");
    }
    
}
