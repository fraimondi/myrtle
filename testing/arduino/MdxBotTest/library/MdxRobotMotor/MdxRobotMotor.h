/*******************************************************
    MdxRobotMotor.h
    low level motor driver interface for Mdx Robot
	
    Michael Margolis Jan 17 2014
********************************************************/

#ifndef MDX_ROBOT_MOTOR_H
#define MDX_ROBOT_MOTOR_H 

#include "QuadEncoder.h"

// defines for left and right motors
const int MOTOR_LEFT  = 0;
const int MOTOR_RIGHT = 1;

extern const int MIN_SPEED;
extern int motorState[2];
extern const int NBR_SPEEDS;

void motorBegin(int motor);

// return the amount of time needed to move the given distance at the current speed
// only used if robot does not have encoders
unsigned long motorDistanceMMToTime(unsigned long distanceMM);

// speed range is 0 to 100 percent
int motorGetTargetPWM(int speed);
int  motorGetSpeed(int motor);
void motorSetSpeed(int motor, int speed);
void motorWritePWM(int motor, int value);
int  motorReadPWM(int motor);

void motorForward(int motor, int speed);

void motorReverse(int motor, int speed);

void motorStop(int motor);

void motorBrake(int motor);

#endif