/*
 * Sysex additions to support quadrature encoders and motor control
 *
 * Michael Margolis Dec 2013
 */

/*
 * Firmata is a generic protocol for communicating with microcontrollers
 * from software on a host computer. It is intended to work with
 * any host computer software package.
 *
 * To download a host software package, please clink on the following link
 * to open the download page in your default browser.
 *
 * http://firmata.org/wiki/Download
 */

/*
 Copyright (C) 2009 Jeff Hoefs.  All rights reserved.
 Copyright (C) 2009 Shigeru Kobayashi.  All rights reserved.
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 See file LICENSE.txt for further information on licensing terms.
 */

/* Encoder data request 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  ENCODER_DATA Command (0x7D) // 0x7D looks to be a free sysex tag
 * 2  encoder request tag (1)
 * 3  END_SYSEX (0xF7)  
 */

/* Encoder data reply 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  ENCODER_DATA Command (0x7D) 
 * 2  encoder reply tag (1)
 * 3  time_m1 bit  0-6  // duration of most recent encoder pulse for motor 1
 * 4  time_m1 bit  7-13 // each unit is 10 microseconds (value = 0 if motor stopped)
 * 5  count_m1 bit 0-6  // pulse count for motor 1 since last request (0 if first request)
 * 6  count_m1 bit 7-13
 * 7  time_m2 bit  0-6  // duration for motor 2 encoder
 * 8  time_m2 bit  7-13
 * 9  count_m2 bit 0-6  // pulse count for motor 2
 * 10 count_m1 bit 7-13
 * 11 END_SYSEX (0xF7) 
 */


#include <Firmata.h>

//#define SOFT_SERIAL_DEBUG // uncomment for debug printing 
#ifdef SOFT_SERIAL_DEBUG
#define DEBUG_PRINT(x)  mySerial.print(x)
#define DEBUG_PRINTln(x)  mySerial.println(x)

#include <SoftwareSerial.h>
SoftwareSerial mySerial(0xff, A0); // RX disabled, TX on A0
#else
#define DEBUG_PRINT(x)  
#define DEBUG_PRINTln(x)  
#endif

const int ENCODER_DATA    =  0x7D;  // encoder command tag
const int ENCODER_REQUEST = 1;    // version 1 request format
const int ENCODER_REPLY   = 1;    // version 1 reply format

const int ENABLE_SPONTANEOUS_MSGS      = 2;   
const int DISABLE_SPONTANEOUS_MSGS     = 3;
const int SET_SPONTANEOUS_MSG_INTERVAL = 4;

const int SET_WHEEL1_SPEED             = 5;
const int SET_WHEEL2_SPEED             = 6;


const int PULSE_WIDTH_SCALE = 10; // number of microseconds per unit sent in firmata msg


void firmataSysexBegin()
{
#ifdef SOFT_SERIAL_DEBUG  
    mySerial.begin(57600);
    mySerial.println("started sysex");
#endif    
    Firmata.setFirmwareVersion(FIRMATA_MAJOR_VERSION, FIRMATA_MINOR_VERSION);
    Firmata.begin(57600);
    Firmata.attach(ANALOG_MESSAGE, analogWriteCallback);
  Firmata.attach(DIGITAL_MESSAGE, digitalWriteCallback);
  Firmata.attach(REPORT_ANALOG, reportAnalogCallback);
  Firmata.attach(REPORT_DIGITAL, reportDigitalCallback);
  Firmata.attach(SET_PIN_MODE, setPinModeCallback);
    Firmata.attach(START_SYSEX, sysexCallback);
  Firmata.attach(SYSTEM_RESET, systemResetCallback); //not used in this version 
  systemResetCallback();
}

void firmataProcessInput()
{
  while (Firmata.available()) {
    Firmata.processInput();
  }  
}

void encoderDataRequest()
{
  unsigned long pulse1;
  unsigned long pulse2;
  long count1;
  long count2;
  encodersGetData(pulse1, count1, pulse2, count2);  
  Firmata.write(START_SYSEX);
  Firmata.write(ENCODER_DATA);
  Firmata.write(ENCODER_REPLY);
  // pulse width is 10 microsecond units
  sendWheelData(pulse1 / PULSE_WIDTH_SCALE, count1 ); 
  sendWheelData(pulse2 / PULSE_WIDTH_SCALE, count2 ); 
  Firmata.write(END_SYSEX); 
}

void  sendValueAsTwo7bitBytes(int value)
{
  Firmata.write((byte)(value & 0x7F) );
  Firmata.write((byte)(value >> 7) & 0x7F);  
}

void  sendSignedValueAsTwo7bitBytes(int value)
{
  int signBit = 0x0; 
  
  if( value < 0) {
     value = - value; 
     signBit = 0x2000; // msb high if negative number
  }
  if(value > 0x1fff)
        value = 0x1fff; // truncate to 13 bits
  value |= signBit;
  sendValueAsTwo7bitBytes(value);
}

int  two7bitBytesToInt(byte b1, byte b2)
{
  int value = b1 + (((int)b2)<< 7);
  return value;
}

int  two7bitBytesToSignedInt(byte b1, byte b2)
{
  int value = b1 + (((int)b2)<< 7);
  if (value >= 0x2000){
     value = -(value & 0x1fff); // remove sign bit and negate val
  }
  return value;
}

void sendWheelData( unsigned int pulseWidth, int count)
{ 
  sendValueAsTwo7bitBytes(pulseWidth);
  sendSignedValueAsTwo7bitBytes(count);
}

void sysexCallback(byte command, byte argc, byte *argv)
{
  switch(command) {
  case ENCODER_DATA:
    if( argv[0] == ENCODER_REQUEST) {
      encoderDataRequest();
    }
    else if(argv[0] == ENABLE_SPONTANEOUS_MSGS){
      spontaneousMsgs = true;    
    }
    else if(argv[0] == DISABLE_SPONTANEOUS_MSGS){
      spontaneousMsgs = false;
    }
    else if(argv[0] == SET_SPONTANEOUS_MSG_INTERVAL) {
       spontaneousMsgInterval = argv[1];
       DEBUG_PRINT("interval set to "); DEBUG_PRINTln(spontaneousMsgInterval);
    }    
    else if(argv[0] == SET_WHEEL1_SPEED) {
       int speed = two7bitBytesToSignedInt(argv[1], argv[2]);
       DEBUG_PRINT("WHEEL1 speed set to "); DEBUG_PRINTln(speed);
       setSpeed(WHEEL1, speed);
    }
    else if(argv[0] == SET_WHEEL2_SPEED) {
       int speed = two7bitBytesToSignedInt(argv[1], argv[2]);
       DEBUG_PRINT("WHEEL2 speed set to "); DEBUG_PRINTln(speed);
       setSpeed(WHEEL2, speed);
    }  
  break;
  case SAMPLING_INTERVAL:
    if (argc > 1) {
      samplingInterval = argv[0] + (argv[1] << 7);
      if (samplingInterval < MINIMUM_SAMPLING_INTERVAL) {
        samplingInterval = MINIMUM_SAMPLING_INTERVAL;
      }      
    } else {
      //Firmata.sendString("Not enough data");
    }
    break;
  case EXTENDED_ANALOG:
    if (argc > 1) {
      int val = argv[1];
      if (argc > 2) val |= (argv[2] << 7);
      if (argc > 3) val |= (argv[3] << 14);
      analogWriteCallback(argv[0], val);
    }
    break;
  case CAPABILITY_QUERY:
    Firmata.write(START_SYSEX);
    Firmata.write(CAPABILITY_RESPONSE);
    for (byte pin=0; pin < TOTAL_PINS; pin++) {
      if (IS_PIN_DIGITAL(pin)) {
        Firmata.write((byte)INPUT);
        Firmata.write(1);
        Firmata.write((byte)OUTPUT);
        Firmata.write(1);
      }
      if (IS_PIN_ANALOG(pin)) {
        Firmata.write(ANALOG);
        Firmata.write(10);
      }
      if (IS_PIN_PWM(pin)) {
        Firmata.write(PWM);
        Firmata.write(8);
      }
      Firmata.write(127);
    }
    Firmata.write(END_SYSEX);
    break;
  case PIN_STATE_QUERY:
    if (argc > 0) {
      byte pin=argv[0];
      Firmata.write(START_SYSEX);
      Firmata.write(PIN_STATE_RESPONSE);
      Firmata.write(pin);
      if (pin < TOTAL_PINS) {
        Firmata.write((byte)pinConfig[pin]);
	Firmata.write((byte)pinState[pin] & 0x7F);
	if (pinState[pin] & 0xFF80) Firmata.write((byte)(pinState[pin] >> 7) & 0x7F);
	if (pinState[pin] & 0xC000) Firmata.write((byte)(pinState[pin] >> 14) & 0x7F);
      }
      Firmata.write(END_SYSEX);
    }
    break;
  case ANALOG_MAPPING_QUERY:
    Firmata.write(START_SYSEX);
    Firmata.write(ANALOG_MAPPING_RESPONSE);
    for (byte pin=0; pin < TOTAL_PINS; pin++) {
      Firmata.write(IS_PIN_ANALOG(pin) ? PIN_TO_ANALOG(pin) : 127);
    }
    Firmata.write(END_SYSEX);
    break;
  }
}


void systemResetCallback()
{
  
  
  for (byte i=0; i < TOTAL_PORTS; i++) {
    reportPINs[i] = false;      // by default, reporting off
    portConfigInputs[i] = 0;	// until activated
    previousPINs[i] = 0;
  }
  // pins with analog capability default to analog input
  // otherwise, pins default to digital output
  for (byte i=0; i < TOTAL_PINS; i++) {
    if (IS_PIN_ANALOG(i)) {
      // turns off pullup, configures everything
      setPinModeCallback(i, ANALOG);
    } else {
      // sets the output to 0, configures portConfigInputs
      setPinModeCallback(i, OUTPUT);
    }
  }
  // by default, do not report any analog inputs
  analogInputsToReport = 0;
  
#ifdef CONTINUOUS_MSGS  
  continuousMsgs = false; // not used in this version
#endif  
}

