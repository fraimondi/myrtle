/***************************************************
  MdxBotTest
  Test program for MDX platform using Hub-ee wheels

  // connect the left/center/right IR sensors to pins A0/A1/A2
  // connect the IR LED control to A3
  // connect the left/right bump switches to A4/A5 (wired to connect to gnd when pressed)
  
  Michael Margolis Jan 16 2014
****************************************************/ 

#include <MdxRobotMotor.h>

// Robot properties:
const float WHEEL_DIAMETER_MM      = 60;
const float WHEEL_CIRCUMFERENCE_MM = WHEEL_DIAMETER_MM * 3.1416;
const float WHEEL_TRACK            = 115.0;              //tyre seperation in mm  
const float FRICTION_FACTOR        = 1.2;                //increase movement to counteract rotational friction
const float WHEEL_TRAVEL_PER_360   = WHEEL_TRACK * 3.1416 * FRICTION_FACTOR;  // mm per robot revolution 

const int   COUNTS_PER_REV       = 64;
const float COUNTS_PER_MM        = COUNTS_PER_REV / WHEEL_CIRCUMFERENCE_MM;
const float COUNTS_PER_CM        = (COUNTS_PER_REV / WHEEL_CIRCUMFERENCE_MM) * 10;
const float MM_PER_DEGREE        = WHEEL_TRAVEL_PER_360 / 360.0; 


const int leftBumpSwPin = A4;
const int rightBumpSwPin = A5;

const int nbrIrSensors = 3;
const int IrPins[nbrIrSensors] = {A0,A1,A2};
const int IrControlPin = A3;

int irMinReading[nbrIrSensors];
int irMaxReading[nbrIrSensors];

void setup()
{
  Serial.begin(9600);
  pinMode(IrControlPin, OUTPUT);
  pinMode(leftBumpSwPin, INPUT_PULLUP);
  pinMode(rightBumpSwPin, INPUT_PULLUP);
  
  encodersBegin();
  motorBegin(MOTOR_LEFT); 
  motorBegin(MOTOR_RIGHT); 
  
  testBumpSwitches(); // comment this out if switches not fitted)
  testIrSensors();
  testMovement();
}


void loop()
{   
}


char * switchPrompts[] = {"Press both switches", "Release left", "Release right", "Test Succeeded"}; 
byte switchBits; // bitmask equals 0 if pressed, 1 if released (using pull-ups)

void testBumpSwitches()
{
  int state    =  0;
  int oldState = -1;

  while(true)
  {
     if(state != oldState){
        Serial.println(switchPrompts[state]);
        oldState = state;    
     }
     if( state == 3)
        return;  // test passed         

     bitWrite(switchBits, 0, digitalRead(rightBumpSwPin));        
     bitWrite(switchBits, 1, digitalRead(leftBumpSwPin));

     
     if(switchBits == B00) { //both switches are pressed (using pull-ups)
       state = 1;           
     }
     else if( switchBits == B10){  //left released 
       if(state == 1)
         state = 2;     
     }
     else if( switchBits == B11){   // both released
       if(state == 2)
           state = 3;  
        else   
          state = 0; // start over
     }     
         
     delay(100); // debounce time     
  }   
}

void testMovement()
{
  // forwards and back (MIN_SPEED is defined in MdxRobotMotor.h)
   for(int speed = MIN_SPEED; speed <=100; speed += 14) {
      move(500, speed);  // move forward 250 mm
      move(-500, speed); // and back   
      delay(1000);
   }
   delay(2000);
   // rotate right and left
   for(int speed = MIN_SPEED; speed <=100; speed += 14) {
      rotate(180, speed);  // rotate clockwise 90 degrees
      rotate(-180, speed); // and back  
      delay(1000); 
   }  
}

void testIrSensors()
{
  for(int i=0; i < nbrIrSensors; i++) 
  {
    irMinReading[i] = 1023;
    irMaxReading[i] = 0;
  }  
  
  digitalWrite(IrControlPin, HIGH);
  // rotate cw for half a second  
  motorForward(MOTOR_LEFT,  50);  // run test at 50% speed
  motorReverse(MOTOR_RIGHT, 50);
  unsigned long startTime = millis();
  while(millis() - startTime < 500)
     calibrateIrSensors();
  
  // rotate back   
  motorForward(MOTOR_RIGHT,  50);  // run test at 50% speed
  motorReverse(MOTOR_LEFT, 50);
  startTime = millis();
  while(millis() - startTime < 500)
     calibrateIrSensors();     
  digitalWrite(IrControlPin, LOW);  
  
  motorBrake(MOTOR_LEFT);
  motorBrake(MOTOR_RIGHT);
  
  for(int i=0; i < nbrIrSensors; i++) 
  {
    Serial.print("Sensor "); Serial.print(i); Serial.print(" min = ");
    Serial.print(irMinReading[i]); Serial.print(", max="); Serial.print(irMaxReading[i]);
    if(irMinReading[i] < 100 && irMaxReading[i] > 500)
       Serial.println(" : passed");
    else   
       Serial.println(" ! FAILED");   
  }  
}

void calibrateIrSensors()
{
  for(int i=0; i < nbrIrSensors; i++) 
  {
     int val = analogRead(IrPins[i]);
     if( val <  irMinReading[i])
        irMinReading[i] = val;
     if( val >  irMaxReading[i])
        irMaxReading[i] = val;
   }
}

// distance is in millimeters, speed in percent
void move( int distanceMM, int speed )
{
   Serial.print("Moving "); Serial.print(distanceMM); Serial.print("mm at speed % "); Serial.println(speed);
   if(distanceMM > 0)
   {
      motorForward(MOTOR_LEFT,  speed);
      motorForward(MOTOR_RIGHT, speed);
      moveUntil(distanceMM, 5000 ); // abort if not at target within 5 seconds
   } 
   else if(distanceMM < 0)
   {
      motorReverse(MOTOR_LEFT,  speed);
      motorReverse(MOTOR_RIGHT, speed);
      moveUntil(-distanceMM, 5000 ); // abort if not at target within 5 seconds
   }   
}

void rotate( int angle, int speed)
{
   unsigned int distanceMM = 0;
   
   Serial.print("Rotating "); Serial.print(angle); Serial.print(" degrees at speed % "); Serial.println(speed);
   if(angle > 0)
   {
      motorForward(MOTOR_LEFT,  speed);
      motorReverse(MOTOR_RIGHT, speed);
      distanceMM = map(angle, 0,360, 0, WHEEL_TRAVEL_PER_360);
   } 
   else if(angle < 0)
   {
      motorReverse(MOTOR_LEFT,  speed);
      motorForward(MOTOR_RIGHT, speed);
      distanceMM = map(-angle, 0,360, 0, WHEEL_TRAVEL_PER_360);
   } 
   moveUntil(distanceMM, 5000 ); // abort after 5 seconds
}


// move the given absolute distance or until the given duration has expired
// returns the difference between expected and actual counts
long moveUntil( int distanceMM, unsigned int maxDuration)
{
  // encoder values are stored in the following two arrays
  unsigned long pulse[2];  // length of encoder pulse in microseconds
  long count[2];           // number of pulses since the last encoder request 
  
  long totalLeftCount  = 0;
  long totalRightCount = 0;  
  unsigned long totalEncoderTarget = distanceMM * COUNTS_PER_MM; 
  long result;

  unsigned long startTime = millis();
  
  while(true) // do until completed movement or aborted by timeout 
  {     
    encodersGetData(pulse[MOTOR_LEFT], count[MOTOR_LEFT], pulse[MOTOR_RIGHT], count[MOTOR_RIGHT]);
    totalLeftCount += abs(count[MOTOR_LEFT]);
    totalRightCount += abs(count[MOTOR_RIGHT]);       
   
    if( totalLeftCount >= totalEncoderTarget || totalRightCount >= totalEncoderTarget)
    {
       long MMPerSec = (distanceMM * 1000L) / (millis() - startTime);
       Serial.print( "stopped by distance, speed = "); Serial.print( MMPerSec);      
       break;
    }
    if( millis() - startTime > maxDuration)
    {
       Serial.print( "stopped by timeout!,");  
       break;
    }
  }
  motorBrake(MOTOR_LEFT);
  motorBrake(MOTOR_RIGHT);
  
  result=  totalRightCount - totalLeftCount;
  
  if( totalRightCount == 0) {
    Serial.println(" Error - no count on right encoder"); 
  }
  else if( totalLeftCount == 0) {
    Serial.println(" Error - no count on left encoder"); 
  } 
  else {
    Serial.print(" diff between wheel counts = "); Serial.println(result);
  }  
  return result;
}

