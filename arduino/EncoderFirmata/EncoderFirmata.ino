/*
 * EncoderFirmata sketch with Firmata Sysex additions to support wheel encoders
 * Michael Margolis 2013
 *
 * Firmata definitions and support code for new sysex msgs are in the Firmata tab
 * Interrupt handlers and support code for encoders is in QuadEncoder tab
 * the HUBeeWheel tabs contain a copy of the standard HUBee arduino library with a fix to a macro name collision
 */

#include "HUBeeWheel.h"


const byte WHEEL1 = 0; // indices for the encoder arrrays
const byte WHEEL2 = 1;

// unsolicited encoder messages not implimented in this version
boolean spontaneousMsgs = false; // flag to send unsolicited encoder messages if true
long spontaneousMsgInterval = 50; // milliseconds between unsolicited messages

//declare two wheel objects - each encapsulates all the control functions for a wheel
HUBeeBMDWheel wheel_1;
HUBeeBMDWheel wheel_2;

void initWheels() 
{
  wheel_1.setupPins(8,11,9); //setup using pins 12 and 2 for direction control, and 3 for PWM speed control
  wheel_2.setupPins(12,13,10);//setup using pins 13 and 4 for direction control, and 11 for PWM speed control
  wheel_1.setDirectionMode(0); //Direction Mode determines how the wheel responds to positive and negative motor power values 
  wheel_2.setDirectionMode(0);
  wheel_1.setBrakeMode(0); //Sets the brake mode to zero - freewheeling mode - so wheels are easy to turn by hand
  wheel_2.setBrakeMode(0); //Sets the brake mode to zero - freewheeling mode - so wheels are easy to turn by hand
}

void setSpeed( int motor, int speed)
{
 if ( motor == WHEEL1 ) {
   wheel_1.setMotorPower(speed);
 }
 if ( motor == WHEEL2 ) {   
   wheel_2.setMotorPower(speed);  
 } 
}

void setup()
{
  encodersBegin();  
  initWheels();
  firmataSysexBegin();
}

unsigned long previousMillis;    // for comparison with currentMillis

void loop()
{
  firmataProcessInput();

  if(spontaneousMsgs) {
    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis > spontaneousMsgInterval) {
      previousMillis += spontaneousMsgInterval;
      encoderDataRequest();
    }
  }
}

