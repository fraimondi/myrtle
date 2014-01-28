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

#ifndef HUBeeWheel_H
#define HUBeeWheel_H
#include "Arduino.h"

class HUBeeBMDWheel
{
    public:
        HUBeeBMDWheel();
        HUBeeBMDWheel(int In1Pin, int In2Pin, int PWMPin);
        HUBeeBMDWheel(int In1Pin, int In2Pin, int PWMPin, int STBYPin);
        void setupPins(int In1Pin, int In2Pin, int PWMPin);
        void setupPins(int In1Pin, int In2Pin, int PWMPin, int STBYPin);
        void setBrakeMode(boolean brakeMode);
        void stopMotor();
        void setStandbyMode(boolean standbyMode);
        void setDirectionMode(boolean direction);
        boolean getDirectionMode();
        void setMotorPower(int MPower);
        int getMotorPower();
    
    private:
        void initialise();
        void setMotor();
        int motorPower;
        int rawPower;
        boolean motorDirection;
        boolean motorBrakeMode;
        boolean motorStandbyMode;
        boolean motorDirectionMode;
        //pin assignments
        int IN1;
        int IN2;
        int _PWM;
        int STBY;
};
#endif


