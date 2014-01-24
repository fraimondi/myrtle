/*
 * QuadEncoder.h
 * this module supports quadrature encoders, such as on HUBee wheels
 * 
 * Michael Margolis Dec 2013
 */

#include "Arduino.h"

// defines for left and right encoders
const int ENCODER_LEFT  = 0;
const int ENCODER_RIGHT = 1;

volatile unsigned long prevMicros[2];  // stores the time of the previous reading
long prevCount[2];   // stores the most recent sent count

typedef struct  // holds encoder data captured in interrupt
{
  volatile long count[2];               // encoder pulse count 
  volatile unsigned long pulseWidth[2]; // most recent pulse width
} 
encoderData_t;

encoderData_t encoderData;   // the raw encoder data
encoderData_t encoderCache;  // cached copy to be used outside of interrupt routine

// encoder pins
int wheel_1QeiAPin = 3; //external interrupt 0 used for wheel 1 encoder chanel A
int wheel_1QeiBPin = 7; //wheel 1 encoder chanel B input
int wheel_2QeiAPin = 2; //external interrupt 1 used for wheel 2 encoder chanel A
int wheel_2QeiBPin = 4; //wheel 2 encoder chanel B input


void encodersGetData(unsigned long &pulse1,long &count1, unsigned long &pulse2,  long &count2)
{
  noInterrupts();
  memcpy(&encoderCache, &encoderData, sizeof(encoderCache));  // get a copy of the encoder data   
  interrupts();
  pulse1 = encoderCache.pulseWidth[ENCODER_LEFT] ;
  count1 = encoderCache.count[ENCODER_LEFT] - prevCount[ENCODER_LEFT]; 
  pulse2 = encoderCache.pulseWidth[ENCODER_RIGHT];
  count2 = encoderCache.count[ENCODER_RIGHT] - prevCount[ENCODER_RIGHT];  
  
    // store the current encoder counts
  prevCount[ENCODER_LEFT] = encoderCache.count[ENCODER_LEFT];
  prevCount[ENCODER_RIGHT] = encoderCache.count[ENCODER_RIGHT];
}

//wheel 1 quadrature encoder interrupt function
void QEI_wheel_1()
{  
  encoderData.pulseWidth[ENCODER_LEFT] = micros() - prevMicros[ENCODER_LEFT];
  prevMicros[ENCODER_LEFT] = micros(); // store the current time

  //ChA has changed state
  //Check the state of ChA
  if(digitalRead(wheel_1QeiAPin))
  {
    //pin has gone high
    //check chanel B
    if(digitalRead(wheel_1QeiBPin))
    {
      //both are high
      encoderData.count[ENCODER_LEFT]--;
      return;
    }
    //ChB is low
    encoderData.count[ENCODER_LEFT]++;
    return;
  }
  //if you get here then ChA has gone low, check ChB
  if(digitalRead(wheel_1QeiBPin))
  {
    //ChB is high
    encoderData.count[ENCODER_LEFT]++;
    return;
  }
  //if you get here then A has gone low and B is low
  encoderData.count[ENCODER_LEFT]--;
}

//wheel 2 quadrature encoder interrupt function
void QEI_wheel_2()
{
  encoderData.pulseWidth[ENCODER_RIGHT] = micros() - prevMicros[ENCODER_RIGHT];
  prevMicros[ENCODER_RIGHT] = micros(); // store the current time

  //ChA has changed state
  //Check the state of ChA
  if(digitalRead(wheel_2QeiAPin))
  {
    //pin has gone high
    //check chanel B
    if(digitalRead(wheel_2QeiBPin))
    {
      //both are high
      encoderData.count[ENCODER_RIGHT]++;
      return;
    }
    //ChB is low
    encoderData.count[ENCODER_RIGHT]--; 
    return;
  }
  //if you get here then ChA has gone low, check ChB
  if(digitalRead(wheel_2QeiBPin))
  {
    //ChB is high
    encoderData.count[ENCODER_RIGHT]--;
    return;
  }
  //if you get here then A has gone low and B is low
  encoderData.count[ENCODER_RIGHT]++;
}


void encodersBegin() 
{
  pinMode(wheel_1QeiAPin, INPUT_PULLUP);
  pinMode(wheel_2QeiAPin, INPUT_PULLUP);
  pinMode(wheel_1QeiBPin, INPUT_PULLUP);
  pinMode(wheel_2QeiBPin, INPUT_PULLUP);

  attachInterrupt(1, QEI_wheel_1, CHANGE);
  attachInterrupt(0, QEI_wheel_2, CHANGE);
}
