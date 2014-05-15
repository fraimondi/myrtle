package uk.ac.mdx.cs.jmirto.cabot;

import java.io.*;
import java.util.*;

public class CANTExperiment {
  public int trainingLength = -1;
  boolean inTest = false;
  
  public CANTExperiment () {}
  
  public boolean getInTest(){return inTest;}
  
  public static CANTNet getNet(String otherNetName) {
    Enumeration eNum = CANT23.nets.elements();
    CANTNet net = (CANTNet)eNum.nextElement();
    String netName = net.getName();
    while (netName.compareTo(otherNetName) != 0) 
      {
      net = (CANTNet)eNum.nextElement();
      netName = net.getName();
      }
    return (net);
  }
  
  public boolean experimentDone (int Step) {
    return (false);  
  }

  public void switchToTest () {}
  
  public void measure(int currentStep) {}
  
  public int selectPattern (int curPattern, int numPatterns, CANTNet net) {
    curPattern++;
    curPattern %= numPatterns;
    return (curPattern);
  }
  
  public boolean isEndEpoch(int Cycle) {
    return (false);
  }

  public void endEpoch() {
    Enumeration eNum = CANT23.nets.elements();
    CANTNet net;
    do	{
      net = (CANTNet)eNum.nextElement();
      net.clear();
    }
    while (eNum.hasMoreElements());
  }
  
 
  public void printExpName () {
     System.out.println("base experiment");
  }
}
	
