/*
 * QuadEncoder.h
 * this module supports quadrature encoders, such as on HUBee wheels
 * 
 * Michael Margolis Dec 2013
 */


void encodersBegin(); 

void encodersGetData(unsigned long &pulse1,long &count1, unsigned long &pulse2,  long &count2);
