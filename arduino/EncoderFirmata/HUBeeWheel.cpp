/*HUB-ee BMD Arduino Lib
 Provides an object for a single motor using the BMD motor driver, includes optional standby control.
 Designed for the BMD or BMD-S motor driver which uses the Toshiba TB6593FNG motor driver IC
 Created by Creative Robotics Ltd in 2012.
 Released into the public domain.
 */

/*
 * this version renames PWM variable to avoid Arduino macro name clash 
 * Michael Margolis Dec 2013
 */


#include "HUBeeWheel.h" // this is the modified version of the hubee library

HUBeeBMDWheel::HUBeeBMDWheel()
{
}

HUBeeBMDWheel::HUBeeBMDWheel(int In1Pin, int In2Pin, int PWMPin)
{
  IN1 = In1Pin;
  IN2 = In2Pin;
  _PWM= PWMPin;
  STBY = -1;
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  initialise();
}

HUBeeBMDWheel::HUBeeBMDWheel(int In1Pin, int In2Pin, int PWMPin, int STBYPin)
{
  IN1 = In1Pin;
  IN2 = In2Pin;
  _PWM= PWMPin;
  STBY = STBYPin;
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  pinMode(STBY, OUTPUT);
  initialise();
}

void HUBeeBMDWheel::setupPins(int In1Pin, int In2Pin, int PWMPin)
{
  IN1 = In1Pin;
  IN2 = In2Pin;
  _PWM= PWMPin;
  STBY = -1;
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  initialise();
}

void HUBeeBMDWheel::setupPins(int In1Pin, int In2Pin, int PWMPin, int STBYPin)
{
  IN1 = In1Pin;
  IN2 = In2Pin;
  _PWM= PWMPin;
  STBY = STBYPin;
  pinMode(IN1, OUTPUT);
  pinMode(IN2, OUTPUT);
  initialise();
}

void HUBeeBMDWheel::initialise()
{
  motorPower = 0;
  rawPower = 0;
  motorDirection = 1;
  motorBrakeMode = 0;
  motorStandbyMode = 0;
  motorDirectionMode = 0;
  setMotor();
}

void HUBeeBMDWheel::setBrakeMode(boolean brakeMode)
{
  //sets the braking mode
  //0 = freewheeling - motor will freewheel when power is set to zero
  //1 = braking - motor terminals are shorted when power is set to zero, motor will stop quickly
  motorBrakeMode = brakeMode;
}

void HUBeeBMDWheel::setDirectionMode(boolean directionMode)
{
  //set the direction mode
  //setting this to 1 will invert the normal motor direction
  motorDirectionMode = directionMode;
}

boolean HUBeeBMDWheel::getDirectionMode()
{
  //returns the motor direction mode
  return motorDirectionMode;
}

void HUBeeBMDWheel::stopMotor()
{
  //halt the motor using the current braking mode
  analogWrite(_PWM, 0);
  digitalWrite(IN1, motorBrakeMode);
  digitalWrite(IN2, motorBrakeMode);
}


void HUBeeBMDWheel::setStandbyMode(boolean standbyMode)
{
  //set the standby mode if a standby pin has been assigned.
  //1 = standby mode active
  //0 = standby mode inactive
  //invert the value because holding the STBY pin LOW activates standby on the IC
  motorStandbyMode = 1-standbyMode;
  if(STBY>=0)
  {
    digitalWrite(STBY, motorStandbyMode);
  }
}

void HUBeeBMDWheel::setMotorPower(int MPower)
{
  //set the motor - public function
  //costrain the value to what is allowed
  motorPower = constrain(MPower, -255, 255);
  setMotor(); //call private func to actually set the motor
}

void HUBeeBMDWheel::setMotor()
{
  //set the motor - private function
  rawPower = abs(motorPower);
  if(motorPower < 0) motorDirection = 0;
  else motorDirection = 1;

  if(motorPower == 0)
  {
    stopMotor();
    return;
  }
  //write the speed value to PWM
  analogWrite(_PWM, rawPower);
  //XOR the motor Direction and motorDirectionMode boolean values
  /*
    if the motorDirectionMode value is 1 then the output will be inverted
   motorDirection  ^       motorDirectionMode
   0   ^   0 == 0
   1   ^   0 == 1
   
   0   ^   1 == 1
   1   ^   1 == 0
   */
  digitalWrite(IN1,   (motorDirection ^ motorDirectionMode) );
  //invert the direction for the second control line
  digitalWrite(IN2, 1-(motorDirection ^ motorDirectionMode) );
}

int HUBeeBMDWheel::getMotorPower()
{
  //return the motor power value
  return motorPower;
}

