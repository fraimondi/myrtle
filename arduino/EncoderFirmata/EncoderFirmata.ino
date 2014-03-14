/*
 * EncoderFirmata sketch with Firmata Sysex additions to support wheel encoders
 * Michael Margolis 2013
 * Updated 28 Jan to add sysex msgs for ir sensors and switches and firmata pin requests
 * Updated 10 March  to support servos
 *
 * Firmata definitions and support code for new sysex msgs are in the Firmata tab
 * Interrupt handlers and support code for encoders is in QuadEncoder tab
 * the HUBeeWheel tabs contain a copy of the standard HUBee arduino library with a fix to a macro name collision
 */

#include "HUBeeWheel.h"
#include <Servo.h>
#include <Firmata.h>


#define MAX_QUERIES 8
#define MINIMUM_SAMPLING_INTERVAL 10

#define REGISTER_NOT_SPECIFIED -1

/* analog inputs */
int analogInputsToReport = 0; // bitwise array to store pin reporting

/* digital input ports */
byte reportPINs[TOTAL_PORTS];       // 1 = report this port, 0 = silence
byte previousPINs[TOTAL_PORTS];     // previous 8 bits sent

/* pins configuration */
byte pinConfig[TOTAL_PINS];         // configuration of every pin
byte portConfigInputs[TOTAL_PORTS]; // each bit: 1 = pin in INPUT, 0 = anything else
int pinState[TOTAL_PINS];           // any value that has been written

/* timer variables */
unsigned long currentMillis;        // store the current value from millis()
unsigned long previousMillis;       // for comparison with currentMillis
int samplingInterval = 19;          // how often to run the main loop (in ms)

const byte WHEEL1 = 0; // indices for the encoder arrrays
const byte WHEEL2 = 1;

// unsolicited encoder messages not implimented in this version
boolean spontaneousMsgs = false; // flag to send unsolicited encoder messages if true
// spontanious encoder messages now sent using the same loop timeing as firmata analog messages

//declare two wheel objects - each encapsulates all the control functions for a wheel
HUBeeBMDWheel wheel_1;
HUBeeBMDWheel wheel_2;

const int nbrIrSensors                 = 3;
const int irSensorPins[nbrIrSensors]   = {A1, A2, A3};
const int irControlPin                 =  14; // A0
const int nbrSwitches                  = 2;
const int bumpSwitchPins[nbrSwitches]  = {A4, A5};

Servo servos[MAX_SERVOS];

void outputPort(byte portNumber, byte portValue, byte forceSend)
{
  // pins not configured as INPUT are cleared to zeros
  portValue = portValue & portConfigInputs[portNumber];
  // only send if the value is different than previously sent
  if (forceSend || previousPINs[portNumber] != portValue) {
    Firmata.sendDigitalPort(portNumber, portValue);
    previousPINs[portNumber] = portValue;
  }
}

/* -----------------------------------------------------------------------------
 * check all the active digital inputs for change of state, then add any events
 * to the Serial output queue using Serial.print() */
void checkDigitalInputs(void)
{
  /* Using non-looping code allows constants to be given to readPort().
   * The compiler will apply substantial optimizations if the inputs
   * to readPort() are compile-time constants. */
  if (TOTAL_PORTS > 0 && reportPINs[0]) outputPort(0, readPort(0, portConfigInputs[0]), false);
  if (TOTAL_PORTS > 1 && reportPINs[1]) outputPort(1, readPort(1, portConfigInputs[1]), false);
  if (TOTAL_PORTS > 2 && reportPINs[2]) outputPort(2, readPort(2, portConfigInputs[2]), false);
  if (TOTAL_PORTS > 3 && reportPINs[3]) outputPort(3, readPort(3, portConfigInputs[3]), false);
  if (TOTAL_PORTS > 4 && reportPINs[4]) outputPort(4, readPort(4, portConfigInputs[4]), false);
  if (TOTAL_PORTS > 5 && reportPINs[5]) outputPort(5, readPort(5, portConfigInputs[5]), false);
  if (TOTAL_PORTS > 6 && reportPINs[6]) outputPort(6, readPort(6, portConfigInputs[6]), false);
  if (TOTAL_PORTS > 7 && reportPINs[7]) outputPort(7, readPort(7, portConfigInputs[7]), false);
  if (TOTAL_PORTS > 8 && reportPINs[8]) outputPort(8, readPort(8, portConfigInputs[8]), false);
  if (TOTAL_PORTS > 9 && reportPINs[9]) outputPort(9, readPort(9, portConfigInputs[9]), false);
  if (TOTAL_PORTS > 10 && reportPINs[10]) outputPort(10, readPort(10, portConfigInputs[10]), false);
  if (TOTAL_PORTS > 11 && reportPINs[11]) outputPort(11, readPort(11, portConfigInputs[11]), false);
  if (TOTAL_PORTS > 12 && reportPINs[12]) outputPort(12, readPort(12, portConfigInputs[12]), false);
  if (TOTAL_PORTS > 13 && reportPINs[13]) outputPort(13, readPort(13, portConfigInputs[13]), false);
  if (TOTAL_PORTS > 14 && reportPINs[14]) outputPort(14, readPort(14, portConfigInputs[14]), false);
  if (TOTAL_PORTS > 15 && reportPINs[15]) outputPort(15, readPort(15, portConfigInputs[15]), false);
}


// -----------------------------------------------------------------------------
/* sets the pin mode to the correct state and sets the relevant bits in the
 * two bit-arrays that track Digital I/O and PWM status
 */
void setPinModeCallback(byte pin, int mode)
{

  if (IS_PIN_SERVO(pin) && mode != SERVO && servos[PIN_TO_SERVO(pin)].attached()) {
    servos[PIN_TO_SERVO(pin)].detach();
  }
  if (IS_PIN_ANALOG(pin)) {
    reportAnalogCallback(PIN_TO_ANALOG(pin), mode == ANALOG ? 1 : 0); // turn on/off reporting
  }
  if (IS_PIN_DIGITAL(pin)) {
    if (mode == INPUT) {
      portConfigInputs[pin / 8] |= (1 << (pin & 7));
    } else {
      portConfigInputs[pin / 8] &= ~(1 << (pin & 7));
    }
  }
  pinState[pin] = 0;
  switch (mode) {
    case ANALOG:
      if (IS_PIN_ANALOG(pin)) {
        if (IS_PIN_DIGITAL(pin)) {
          pinMode(PIN_TO_DIGITAL(pin), INPUT); // disable output driver
          digitalWrite(PIN_TO_DIGITAL(pin), LOW); // disable internal pull-ups
        }
        pinConfig[pin] = ANALOG;
      }
      break;
    case INPUT:
      if (IS_PIN_DIGITAL(pin)) {
        pinMode(PIN_TO_DIGITAL(pin), INPUT); // disable output driver
        digitalWrite(PIN_TO_DIGITAL(pin), HIGH); // disable internal pull-ups
        pinConfig[pin] = INPUT;
      }
      break;
    case OUTPUT:
      if (IS_PIN_DIGITAL(pin)) {
        digitalWrite(PIN_TO_DIGITAL(pin), LOW); // disable PWM
        pinMode(PIN_TO_DIGITAL(pin), OUTPUT);
        pinConfig[pin] = OUTPUT;
      }
      break;
    case PWM:
      if (IS_PIN_PWM(pin)) {
        pinMode(PIN_TO_PWM(pin), OUTPUT);
        analogWrite(PIN_TO_PWM(pin), 0);
        pinConfig[pin] = PWM;
      }
      break;
    case SERVO:
      if (IS_PIN_SERVO(pin)) {
        pinConfig[pin] = SERVO;
        if (!servos[PIN_TO_SERVO(pin)].attached()) {
          servos[PIN_TO_SERVO(pin)].attach(PIN_TO_DIGITAL(pin));
        }
      }
      break;

    default:
      Firmata.sendString("Unknown pin mode"); // TODO: put error msgs in EEPROM
  }
  // TODO: save status to EEPROM here, if changed
}

void analogWriteCallback(byte pin, int value)
{
  if (pin < TOTAL_PINS) {
    switch (pinConfig[pin]) {
      case SERVO:
        if (IS_PIN_SERVO(pin))
          servos[PIN_TO_SERVO(pin)].write(value);
        pinState[pin] = value;
        break;
      case PWM:
        if (IS_PIN_PWM(pin))
          analogWrite(PIN_TO_PWM(pin), value);
        pinState[pin] = value;
        break;
    }
  }
}

void digitalWriteCallback(byte port, int value)
{
  byte pin, lastPin, mask = 1, pinWriteMask = 0;

  if (port < TOTAL_PORTS) {
    // create a mask of the pins on this port that are writable.
    lastPin = port * 8 + 8;
    if (lastPin > TOTAL_PINS) lastPin = TOTAL_PINS;
    for (pin = port * 8; pin < lastPin; pin++) {
      // do not disturb non-digital pins (eg, Rx & Tx)
      if (IS_PIN_DIGITAL(pin)) {
        // only write to OUTPUT and INPUT (enables pullup)
        // do not touch pins in PWM, ANALOG, SERVO or other modes
        if (pinConfig[pin] == OUTPUT || pinConfig[pin] == INPUT) {
          pinWriteMask |= mask;
          pinState[pin] = ((byte)value & mask) ? 1 : 0;
        }
      }
      mask = mask << 1;
    }
    writePort(port, (byte)value, pinWriteMask);
  }
}


// -----------------------------------------------------------------------------
/* sets bits in a bit array (int) to toggle the reporting of the analogIns
 */
//void FirmataClass::setAnalogPinReporting(byte pin, byte state) {
//}
void reportAnalogCallback(byte analogPin, int value)
{
  if (analogPin < TOTAL_ANALOG_PINS) {
    if (value == 0) {
      analogInputsToReport = analogInputsToReport &~ (1 << analogPin);
    } else {
      analogInputsToReport = analogInputsToReport | (1 << analogPin);
    }
  }
  // TODO: save status to EEPROM here, if changed
}

void reportDigitalCallback(byte port, int value)
{
  if (port < TOTAL_PORTS) {
    reportPINs[port] = (byte)value;
  }
  // do not disable analog reporting on these 8 pins, to allow some
  // pins used for digital, others analog.  Instead, allow both types
  // of reporting to be enabled, but check if the pin is configured
  // as analog when sampling the analog inputs.  Likewise, while
  // scanning digital pins, portConfigInputs will mask off values from any
  // pins configured as analog
}

void initWheels()
{
  wheel_1.setupPins(8, 11, 9); //setup using pins 12 and 2 for direction control, and 3 for PWM speed control
  wheel_2.setupPins(12, 13, 10); //setup using pins 13 and 4 for direction control, and 11 for PWM speed control
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
  firmataSysexBegin();
  encodersBegin();
  initWheels();
  for (int sw = 0; sw < nbrSwitches; sw++) {
    pinMode( bumpSwitchPins[sw], INPUT_PULLUP);
  }
  pinMode(irControlPin, OUTPUT);
  digitalWrite(irControlPin, LOW); // turn off (until needed)
}


#define MYRTLE_FIRMATA // define this to select only A1 through A3 for analog output
void loop()
{
  byte pin, analogPin;

  /* DIGITALREAD - as fast as possible, check for changes and output them to the
   * FTDI buffer using Serial.print()  */
  checkDigitalInputs();
  firmataProcessInput();

  currentMillis = millis();
  if (currentMillis - previousMillis > samplingInterval) {
    previousMillis += samplingInterval;
    /* ANALOGREAD - do all analogReads() at the configured sampling interval */
    for (pin = 0; pin < TOTAL_PINS; pin++) {
#ifdef MYRTLE_FIRMATA
      if ( pin >= 15 && pin <= 17) {  // we only care about analog values of pins A1-A3 (digital 15-17)
        analogPin = PIN_TO_ANALOG(pin);
        Firmata.sendAnalog(analogPin, analogRead(analogPin));
      }
#else
      if (IS_PIN_ANALOG(pin) && pinConfig[pin] == ANALOG) {
        analogPin = PIN_TO_ANALOG(pin);
        if (analogInputsToReport & (1 << analogPin)) {
          Firmata.sendAnalog(analogPin, analogRead(analogPin));
        }
      }
#endif
    }
    if (spontaneousMsgs) {
      encoderDataRequest();
    }
  }
}

void irSensorsGetData(byte argc, int *argv)
{
  digitalWrite(irControlPin, HIGH);
  delay(5);
  int samplesToAverage = 1;  // incease this to average multiple samples
  for (int sensor = 0; sensor < argc; sensor++) {
    if ( sensor < nbrIrSensors) {
      argv[sensor] = 0;
      for ( int i = 0; i < samplesToAverage; i++) {
        argv[sensor] += analogRead(irSensorPins[sensor]);
      }
      argv[sensor] /= samplesToAverage;
    }
    else
      argv[sensor] = 0;
  }
  digitalWrite(irControlPin, LOW);
}

void switchGetData(byte argc, int *argv)
{
  for (int sw = 0; sw < argc; sw++) {
    if ( sw < nbrSwitches )
      argv[sw] =  digitalRead( bumpSwitchPins[sw]) ? 0 : 1; // 0 when not pressed
    else
      argv[sw] = 0;
  }
}
