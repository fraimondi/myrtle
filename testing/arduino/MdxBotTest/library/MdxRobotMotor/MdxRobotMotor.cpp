/*******************************************************
    MdxRobotMotor.cpp // Hub-ee version
    low level driver providing an abstact motor interface

    Michael Margolis Jan 16 2014
********************************************************/

#include <Arduino.h>

#include "MdxRobotMotor.h"
#include "HUBeeWheel.h"

const int differential = 0; // % faster left motor turns compared to right 

// HUB-ee wheel pins      (left,right):             
const byte M_PWM_PIN[2]   = {8 ,12}; 
const byte M_DIRA_PIN[2]  = {11,13};     
const byte M_DIRB_PIN[2]  = {9 ,10};
/* end of motor pin defines */

HUBeeBMDWheel Wheel[2];  //declare two wheel objects - each encapsulates all the control functions for a wheel

int  motorSpeed[2]  = {0,0}; // motor speed stored here (0-100%)
int  motorDir[2]    = {1,1}; // 1 is forward, -1 is reverse 

// tables hold time in ms to rotate robot 360 degrees at various speeds 
// this enables conversion of rotation angle into timed motor movement 
// The speeds are percent of max speed
// Note: low cost motors do not have enough torque at low speeds so
// the robot will not move below this value 
// Interpolation is used to get a time for any speed from MIN_SPEED to 100%

const int MIN_SPEED = 30; // first table entry is this speed as percent
const int SPEED_TABLE_INTERVAL = 10; // each table entry is 10% faster speed
const int NBR_SPEEDS =  1 + (100 - MIN_SPEED)/ SPEED_TABLE_INTERVAL;
 
int speedTable[NBR_SPEEDS]  = { 30,  40,   50,   60,   70,  80,  90,  100}; // speeds  
int motorPWM[NBR_SPEEDS]    = { 100, 125,  150,  170,  190, 210, 230, 250}; // pwm value 
int currentPWM[2]; // the current pwm for each motor

long msPerCM =  1000 / 40; // 40cm per sec is the fastes speed


void motorBegin(int side)
{
  Wheel[side].setupPins(M_PWM_PIN[side], M_DIRA_PIN[side],M_DIRB_PIN[side]); 
  if( side == MOTOR_RIGHT)
     Wheel[side].setDirectionMode(1); //reverse right side to move forward 
  Wheel[side].setBrakeMode(1);  // terminals shorted when braked to stop motor quickly (0 is freewheeling)
  motorStop(side);
}

// return the number of milliseconds needed to move the given distance at the current speed
// only used if robot does not have encoders
unsigned long motorDistanceMMToTime(unsigned long distance)
{
   //Serial.print("d to t "); Serial.println((distance * msPerCM * 10L ) /  (motorSpeed[0] + motorSpeed[1] /2));
   return (distance * msPerCM * 10L ) /  (motorSpeed[0] + motorSpeed[1] /2);
}

int motorGetTargetPWM(int speed)
{
  int PWM = 0; 
   if(speed >= MIN_SPEED)
   {
     int index = (speed - MIN_SPEED) / SPEED_TABLE_INTERVAL ; // index into speed and time tables
     int p0 = motorPWM[index];
     int p1 = motorPWM[index+1];    // pwm for the next higher speed 
     PWM =   map(speed,  speedTable[index],  speedTable[index+1], p0, p1);
   }
   return PWM;
}


// speed range is 0 to 100
void motorSetSpeed(int side, int speed)
{
   if(speed <= 0)
      speed = 0;
   else if(speed > 100)
      speed = 100;	  
	  
   motorSpeed[side] = speed;           // save the value
   currentPWM[side] =  motorGetTargetPWM(speed); // store the pwm value for this speed
   Wheel[side].setMotorPower( currentPWM[side] * motorDir[side]);
}

int motorGetSpeed(int side)
{
   return motorSpeed[side];
}


int motorReadPWM(int side)
{
  return currentPWM[side];
}

void motorForward(int side, int speed)
{
  motorDir[side] = 1;
  motorSetSpeed(side, speed); 
}

void motorReverse(int side, int speed)
{
  motorDir[side] = -1;
  motorSetSpeed(side, speed);
}

void motorStop(int side)
{
  motorSetSpeed(side, 0);   
}

void motorBrake(int side)
{
  Wheel[side].stopMotor(); // shorts the motor pins to stop quickly 
}
