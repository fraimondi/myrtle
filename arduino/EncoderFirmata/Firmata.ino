/*
 * Sysex additions to support Myrtle Robot:
 *   Quadrature encoders, motor control, IR Sensors and Bump Switches
 *
 * Michael Margolis Dec 2013 - Jan 2014
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

/*
  * MEM - Jan 27 2014 added support for IR sensors and bump switches
 */

/* Myrtle requests 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  MYRTLE_DATA Command (0x7D) // 0x7D looks to be a free sysex tag
 * 2  encoder request tag indicates the nature of the request
 * 3  END_SYSEX (0xF7)  
 */

/* Encoder data reply 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  MYRTLE_DATA Command (0x7D) 
 * 2  encoder reply tag (1)
 * 3  Body length   number of bytes following the tag excluding END_SYSEX    
 * 4  time_m1 bit  0-6  // duration of most recent encoder pulse for motor 1
 * 5  time_m1 bit  7-13 // each unit is 10 microseconds (value = 0 if motor stopped)
 * 6  count_m1 bit 0-6  // pulse count for motor 1 since last request (0 if first request)
 * 7  count_m1 bit 7-13
 * 8  time_m2 bit  0-6  // duration for motor 2 encoder
 * 9  time_m2 bit  7-13
 * 10 count_m2 bit 0-6  // pulse count for motor 2
 * 11 count_m1 bit 7-13
 * 12 END_SYSEX (0xF7) 
 */


/* IRSENSOR_REQUEST data reply 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  MYRTLE_DATA Command (0x7D) 
 * 2  IRSENSOR_REQUEST tag (7)
 * 3  Body length   number of bytes following the tag excluding END_SYSEX   
 * 4  sensor1 bit  0-6  // analog read data is in bits 0-9 
 * 5  sensor1 bit  7-13 
 * 6  sensor2  bit 0-6 
 * 7  sensor2  bit 7-13
 * 8  sensor3  bit  0-6  
 * 9  sensor3  bit  7-13
 * 10 sensor4  bit 0-6    // reserved for future use
 * 11 sensor4  bit 7-13
 * 12 END_SYSEX (0xF7) 
 */


/* BUMPSWITCH_REQUEST data reply 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  MYRTLE_DATA Command (0x7D) 
 * 2  BUMPSWITCH_REQUEST tag (8)
 * 3  Body length   number of bytes following the tag excluding END_SYSEX 
 * 4  left switch  big 0-6  // bit 0 is 1 when switch pressed, else 0
 * 5  right switch bit 0-6 
 * 6  reserved    bit 0-6   // reserved for future use
 * 7  reserved    bit 0-6 
 * 8 END_SYSEX (0xF7) 
 */


/* DISTANCESENSOR_REQUEST data reply 
 * ------------------------------
 * 0  START_SYSEX (0xF0)
 * 1  MYRTLE_DATA Command (0x7D) 
 * 2  DISTANCESENSOR_REQUEST tag (9)
 * 3  Body length   number of bytes following the tag excluding END_SYSEX   
 * 4  distance  bit  0-6  // distance in mm 
 * 5  distance  bit  7-13 
 * 6  time      bit 0-6 // timestamp (TBD)
 * 7  time      bit 7-13
 * 8  reserved  bit 0-6 // reserved for future use
 * 9  reserved  bit 7-13
 * 8 END_SYSEX (0xF7) 
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

const int MYRTLE_DATA    =  0x7D; // Myrtle Robot command tag
const int ENCODER_REQUEST = 1;    // version 1 request format
const int ENCODER_REPLY   = 1;    // version 1 reply format
const int ENCODER_BODY_LEN = 8;   // bytes following the tag excluding END_SYSEX   

const int ENABLE_SPONTANEOUS_MSGS      = 2;   
const int DISABLE_SPONTANEOUS_MSGS     = 3;
const int SET_SPONTANEOUS_MSG_INTERVAL = 4;

const int SET_WHEEL1_SPEED             = 5;
const int SET_WHEEL2_SPEED             = 6;

const int IRSENSOR_REQUEST   = 7;    // analog IR reflectance sensor data
const int IRSENSOR_BODY_LEN  = 8;    // bytes following the tag excluding END_SYSEX

const int BUMPSWITCH_REQUEST  = 8;   // bump switch states
const int BUMPSWITCH_BODY_LEN = 4;   // bytes following the tag excluding END_SYSEX

const int DISTANCESENSOR_REQUEST = 9; // request for distance
const int DISTANCESENSOR_BODY_LEN = 6; // bytes following the tag excluding END_SYSEX

const int PULSE_WIDTH_SCALE = 10; // number of microseconds per unit sent in firmata msg

const int nbrIrSensorFields  = 4; // 3 ir sensors and one reserved field
const int nbrSwitchFields    = 4; // 2 bump switched and two reserved fields
const int nbrDistanceSensorFields = 3; // distance, time and a reserved field 

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
  Serial.write(START_SYSEX);
  Serial.write(MYRTLE_DATA);
  Serial.write(ENCODER_REPLY);
  Serial.write(ENCODER_BODY_LEN); 
  // pulse width is 10 microsecond units
  sendWheelData(pulse1 / PULSE_WIDTH_SCALE, count1 ); 
  sendWheelData(pulse2 / PULSE_WIDTH_SCALE, count2 ); 
  Serial.write(END_SYSEX); 
}

void irSensorsDataRequest()
{

  int values[nbrIrSensorFields];
  irSensorsGetData(nbrIrSensorFields, values );  
  Serial.write(START_SYSEX);
  Serial.write(MYRTLE_DATA);
  Serial.write(IRSENSOR_REQUEST);
  Serial.write(IRSENSOR_BODY_LEN); 
  for(int i=0; i < nbrIrSensorFields; i++) {
    sendValueAsTwo7bitBytes(values[i]);
  }
  Serial.write(END_SYSEX); 
}

void switchDataRequest()
{

  int values[nbrSwitchFields];
  switchGetData(nbrSwitchFields, values);  
  Serial.write(START_SYSEX);
  Serial.write(MYRTLE_DATA);
  Serial.write(BUMPSWITCH_REQUEST);
  Serial.write(BUMPSWITCH_BODY_LEN); 
  for(int i=0; i < nbrSwitchFields; i++) {
    if(i < nbrSwitches)
      Serial.write((byte)(values[i] & 0x7F) );
    else   
      Serial.write((byte)0 );
  }
  Serial.write(END_SYSEX); 
}


void distanceSensorDataRequest()
{
  sonar.ping_timer(sendDistanceResponse);
}

void sendDistanceResponse() {
  if (sonar.check_timer()) {
    int values[nbrDistanceSensorFields];
    Serial.write(START_SYSEX);
    Serial.write(MYRTLE_DATA);
    Serial.write(DISTANCESENSOR_REQUEST);
    Serial.write(DISTANCESENSOR_BODY_LEN); 
    sendValueAsTwo7bitBytes((int)(sonar.ping_result / US_ROUNDTRIP_CM)*10);
    Serial.write((byte)0); // FIXME: add time
    Serial.write((byte)0); // Reserved field
    Serial.write(END_SYSEX); 
  }
  // FIXME!!! there is a problem with distance > bound. need to debug a bit
}

void  sendValueAsTwo7bitBytes(int value)
{
  Serial.write((byte)(value & 0x7F) );
  Serial.write((byte)(value >> 7) & 0x7F);  
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
  case MYRTLE_DATA:
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
      //spontaneousMsgInterval = argv[1];
      samplingInterval = argv[1];
      DEBUG_PRINT("interval set to "); 
      DEBUG_PRINTln(samplingInterval);
    }    
    else if(argv[0] == SET_WHEEL1_SPEED) {
      int speed = two7bitBytesToSignedInt(argv[1], argv[2]);
      DEBUG_PRINT("WHEEL1 speed set to "); 
      DEBUG_PRINTln(speed);
      setSpeed(WHEEL1, speed);
    }
    else if(argv[0] == SET_WHEEL2_SPEED) {
      int speed = two7bitBytesToSignedInt(argv[1], argv[2]);
      DEBUG_PRINT("WHEEL2 speed set to "); 
      DEBUG_PRINTln(speed);
      setSpeed(WHEEL2, speed);
    }  
    else if(argv[0] == IRSENSOR_REQUEST){
      irSensorsDataRequest();
    }
    else if(argv[0] == BUMPSWITCH_REQUEST){
      switchDataRequest();
    }
    else if(argv[0] == DISTANCESENSOR_REQUEST){
      distanceSensorDataRequest();
    }
    break;
  case SERVO_CONFIG:
    if(argc > 4) {
      // these vars are here for clarity, they'll optimized away by the compiler
      byte pin = argv[0];
      int minPulse = argv[1] + (argv[2] << 7);
      int maxPulse = argv[3] + (argv[4] << 7);

      if (IS_PIN_SERVO(pin)) {
        if (servos[PIN_TO_SERVO(pin)].attached())
          servos[PIN_TO_SERVO(pin)].detach();
        servos[PIN_TO_SERVO(pin)].attach(PIN_TO_DIGITAL(pin), minPulse, maxPulse);
        setPinModeCallback(pin, SERVO);
      }
    }
    break;
  case SAMPLING_INTERVAL:
    if (argc > 1) {
      samplingInterval = argv[0] + (argv[1] << 7);
      if (samplingInterval < MINIMUM_SAMPLING_INTERVAL) {
        samplingInterval = MINIMUM_SAMPLING_INTERVAL;
      }      
    } 
    else {
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
    Serial.write(START_SYSEX);
    Serial.write(CAPABILITY_RESPONSE);
    for (byte pin=0; pin < TOTAL_PINS; pin++) {
      if (IS_PIN_DIGITAL(pin)) {
        Serial.write((byte)INPUT);
        Serial.write(1);
        Serial.write((byte)OUTPUT);
        Serial.write(1);
      }
      if (IS_PIN_ANALOG(pin)) {
        Serial.write(ANALOG);
        Serial.write(10);
      }
      if (IS_PIN_PWM(pin)) {
        Serial.write(PWM);
        Serial.write(8);
      }
      Serial.write(127);
    }
    Serial.write(END_SYSEX);
    break;
  case PIN_STATE_QUERY:
    if (argc > 0) {
      byte pin=argv[0];
      Serial.write(START_SYSEX);
      Serial.write(PIN_STATE_RESPONSE);
      Serial.write(pin);
      if (pin < TOTAL_PINS) {
        Serial.write((byte)pinConfig[pin]);
        Serial.write((byte)pinState[pin] & 0x7F);
        if (pinState[pin] & 0xFF80) Serial.write((byte)(pinState[pin] >> 7) & 0x7F);
        if (pinState[pin] & 0xC000) Serial.write((byte)(pinState[pin] >> 14) & 0x7F);
      }
      Serial.write(END_SYSEX);
    }
    break;
  case ANALOG_MAPPING_QUERY:
    Serial.write(START_SYSEX);
    Serial.write(ANALOG_MAPPING_RESPONSE);
    for (byte pin=0; pin < TOTAL_PINS; pin++) {
      Serial.write(IS_PIN_ANALOG(pin) ? PIN_TO_ANALOG(pin) : 127);
    }
    Serial.write(END_SYSEX);
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
    } 
    else {
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


