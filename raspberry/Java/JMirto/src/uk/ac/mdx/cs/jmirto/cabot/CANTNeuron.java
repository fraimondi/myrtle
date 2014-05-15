package uk.ac.mdx.cs.jmirto.cabot;

import java.io.*;
import java.util.*;

public class CANTNeuron {

  public static boolean flag = true;
  private static final int SYNAPSES_TO_ALLOC = 800;

  protected double currentActivation;
  protected boolean isInhibitory;
  private int timeActive;
  protected float fatigue;
  public int id;
//   change it to private later
  public Synapse synapses[];
  private int currentSynapses;
  private boolean fired = false;
   //look into this later
  private double totalIncomingSynapticStrength;
   
  public boolean getInhibitory() {return (isInhibitory);}
  public void setInhibitory(boolean inhib) {isInhibitory = inhib;}
  public int getCurrentSynapses() {return (currentSynapses);}
  public int getId() { return id;}
  public void setActivation(double Activation) {
     currentActivation = Activation;}
  public double getActivation() {return currentActivation;}   
  public boolean isInhibitory() {return isInhibitory;  }
  public float getFatigue() { return fatigue;  }
  public void setFatigue(float Fatigue) {fatigue = Fatigue;  }
  public void setCompensatoryBase(double newBase) {compensatoryBase = newBase;}

   //undone do I actually need this
  public CANTNet parentNet;

  public CANTNeuron(int neuronId ,CANTNet net) {
     parentNet = net;
     currentActivation = 0;
     timeActive = 0;
     fatigue = 0;
     id = neuronId;
     currentSynapses = 0;
     synapses = new Synapse[SYNAPSES_TO_ALLOC];
     double temp = CANT23.random.nextFloat()*100;
     isInhibitory = temp < parentNet.getLikelihoodOfInhibitoryNeuron()? true:false;
    }

  public CANTNeuron(int neuronId ,CANTNet net, int synapsesPerNeuron) {
     parentNet = net;
     currentActivation = 0;
     timeActive = 0;
     fatigue = 0;
     id = neuronId;
     currentSynapses = 0;
     synapses = new Synapse[synapsesPerNeuron];
     double temp = CANT23.random.nextFloat()*100;
     isInhibitory = temp < parentNet.getLikelihoodOfInhibitoryNeuron()? true:false;
    }

//undone put this generic stuff somewhere else.
  public void Assert(boolean test){
    int x = -1;
    if (! test) x = (1 / (1 +x));
  }


  public float getTotalConnectionStrength (){
    float connectionStrength = (float)0.0;
    for (int synap=0; synap < currentSynapses; synap++)
      connectionStrength +=  synapses[synap].getWeight();
    return (connectionStrength);
  }
  
  //Read Connections To a particular net
  public void readNeuronConnectTo (LineNumberReader inputFile, CANTNet toNet) {
    String inputLine,paramString;
    String toNetName="";
    StringTokenizer tokenizedLine;
    int testID=-1;
    double Weight = 0.0;
    int toNeuronID = 0;
    int axonsToRead=0;
    Double tempDouble;

    //read the neuron ID
    try {
      inputLine = inputFile.readLine();
      tokenizedLine = new StringTokenizer(inputLine);
      paramString=tokenizedLine.nextToken();
      testID = Integer.parseInt(paramString);
    }
    catch (IOException e) {
      System.err.println("Bad Neuron Connect Read" + e.toString());
      System.exit(1);
    }
  	
    //read the number of axons
    try {
      inputLine = inputFile.readLine();
      tokenizedLine = new StringTokenizer(inputLine);
      paramString=tokenizedLine.nextToken();
      axonsToRead = Integer.parseInt(paramString);
    }
    catch (IOException e) {
      System.err.println("Bad Neuron Connect Read Axon" + e.toString());
      System.exit(1);
    }
     
    //read in the axons.
    for (int cAxons = 0; cAxons < axonsToRead; cAxons++) {
      try {
        inputLine = inputFile.readLine();
        tokenizedLine = new StringTokenizer(inputLine);
        toNetName=tokenizedLine.nextToken();
        paramString=tokenizedLine.nextToken();
        toNeuronID = Integer.parseInt(paramString);
        paramString=tokenizedLine.nextToken();
        tempDouble = new Double(paramString);
  	    Weight =  tempDouble.doubleValue();
      }
    
      catch (IOException e) {
        System.err.println("Bad Axon Read" + e.toString());
        System.exit(1);
      }      	 
    
      //set the inhibitory neural value based on the first value
      if (cAxons == 0)
        if (Weight < 0) isInhibitory = true;
        else isInhibitory = false;

      if (toNet.getName().compareTo(toNetName) == 0)  
        {
        addConnection(toNet.neurons[toNeuronID],Weight);
        }
    }
  }

  //read a neuron from an open file
  public void readNeuron (LineNumberReader inputFile, boolean readInterConnections) {
    String inputLine,paramString,toNetName;
    StringTokenizer tokenizedLine;
    int testID=-1;
    double Weight = 0.0;
    int toNeuronID = 0;
    int axonsToRead=0;
    Double tempDouble;

    //read the neuron ID
    try {
      inputLine = inputFile.readLine();
      tokenizedLine = new StringTokenizer(inputLine);
      //  System.out.println(inputLine);
      paramString=tokenizedLine.nextToken();
      testID = Integer.parseInt(paramString);
    }
    catch (IOException e) {
      System.err.println("Bad Neuron Read" + e.toString());
      System.exit(1);
    }
  	
    //assert(testID == ID);   // 8/12/02

    //read the number of axons
    try {
      inputLine = inputFile.readLine();
      tokenizedLine = new StringTokenizer(inputLine);
      // System.out.println(inputLine);
      paramString=tokenizedLine.nextToken();
      axonsToRead = Integer.parseInt(paramString);
    }
    catch (IOException e) {
      System.err.println("Bad Neuron Read Axon" + e.toString());
      System.exit(1);
    }
     
    //read in the axons.
    for (int cAxons = 0; cAxons < axonsToRead; cAxons++) {
      try {
        inputLine = inputFile.readLine();
        tokenizedLine = new StringTokenizer(inputLine);
	toNetName=tokenizedLine.nextToken();
	if (parentNet.getName().compareTo(toNetName) != 0)  
	  {
	      //System.out.println(testID + " " + toNetName + " " + parentNet.getName());
	  if (readInterConnections)
            System.out.println("reading multiple nets not yet supported");  
	  }	
        else {
          paramString=tokenizedLine.nextToken();
          toNeuronID = Integer.parseInt(paramString);
          paramString=tokenizedLine.nextToken();
          tempDouble = new Double(paramString);
  	  Weight =  tempDouble.doubleValue();
	  }
      }
    
      catch (IOException e) {
        System.err.println("Bad Axon Read" + e.toString());
        System.exit(1);
      }      	 
    
      //set the inhibitory neural value based on the first value
      if (cAxons == 0)
        if (Weight < 0) isInhibitory = true;
        else isInhibitory = false;

      addConnection(parentNet.neurons[toNeuronID],Weight);
    }
  }
  
  public void modifyFatigue(){
        if (fired)
          fatigue += parentNet.getFatigueRate();
        else {
//          fatigue -= 2*(1/fatigue);
          fatigue -= parentNet.getFatigueRecoveryRate();
          if (fatigue < 0) fatigue = 0;
        }
    }

//------Stuff for working between runs of the net-----------
    public void addConnection(CANTNeuron to, double weight) {
      //search for an existing connection.
      for (int cSynapse = 0 ; cSynapse < currentSynapses; cSynapse ++){
        if (synapses[cSynapse].getTo() == to){
          flag= true;
	  //          synapses[cSynapse].setWeight(weight);
          flag=false;
          return;
        }
      }
      synapses[currentSynapses] = new Synapse(parentNet,this,to,weight);
      currentSynapses++;
    }

    public void clear(){
      currentActivation = 0;
      timeActive = 0;
      fatigue = 0;
    }
//--------------------stuff for running the net-------------
  public boolean getFired(){
    return fired;
  }
    
  public void setFired(){
    if ((currentActivation - fatigue) >= parentNet.getActivationThreshold())
      fired = true;
    else
      fired = false;
  }
  
  //Reset Activation and apply decay
  public void resetActivation(){
    if (fired) {
      currentActivation = 0;
      timeActive++;
      parentNet.recordNeuronActiveTime(timeActive);
      Assert (timeActive > 0); 
    }
    else
      currentActivation = currentActivation / parentNet.getDecay();
  }

    public void spreadActivation(){
      //For each Synapse add the weight to its from node.
      for (int synapseIndex = 0; synapseIndex < currentSynapses; synapseIndex++){
        CANTNeuron toNeuron = synapses[synapseIndex].getTo();
        if(!isInhibitory)
          toNeuron.setActivation(toNeuron.currentActivation + synapses[synapseIndex].getWeight());
        else
          toNeuron.setActivation(toNeuron.currentActivation + (synapses[synapseIndex].getWeight()));
      }
    }
    public boolean spontaneouslyActivate(){
      if (!parentNet.isSpontaneousActivationOn()) return false;
      if (CANT23.random.nextFloat() <= 0.03) return true;
      return false;
    }

////-----------------Stuff for the learning rules---------------------

    //If your strengthening (absolute value) increase a lot if the total weights
    //are small, a little if they are large.
    protected double compensatoryBase = 1.3;
    protected double getStrengthCompensatoryModifier(double Strength){

      if (! parentNet.isCompensatoryLearningOn()) return 1.0;
      if (Strength < 0) Strength *= -1.0;
      double compensatoryModifier = (parentNet.getSaturationBase()-Strength)/
        parentNet.getCompensatoryDivisor();
      double result = Math.pow(compensatoryBase,compensatoryModifier);
      return (result);
    }

    //If you are weakening (closer to 0), move a lot if the total strengths are
    //large and a little if they are small.
    protected double getWeakCompensatoryModifier(double Strength){

      if (! parentNet.isCompensatoryLearningOn()) return 1.0;
      if (Strength < 0) Strength *= -1.0;
      double compensatoryFromModifier =(Strength-parentNet.getSaturationBase())/
        parentNet.getCompensatoryDivisor();
      double result = Math.pow(compensatoryBase,compensatoryFromModifier);
      return (result);
    }

    //How much to reduce an association based on Sterngth of the Connection
    public double getDecreaseBase(double currentStrength) {
//      double modifiedStrength = currentStrength/(parentNet.getAxonalStrengthMedian()*2);
      return (currentStrength*parentNet.getLearningRate());
    }

    //How much to increase an association based on strenth of the connection.
    //TimesitDoesn'tFire*decrease_weight (1-Lij)*GetDecreaseBase

    public double getIncreaseBase(double currentStrength) {

      double modifiedStrength = currentStrength/(parentNet.getAxonalStrengthMedian()*2);
      double estimateNeuronNotFires = (1-modifiedStrength);
      return (estimateNeuronNotFires*parentNet.getLearningRate()*
     (parentNet.getAxonalStrengthMedian()*2));
    }

    //post not pre forgetting only with excitatory
    //using compensatory learning
    public void OldLearn2(){
      double modification;
      double toCompensatoryStrengthModifier;
      double toCompensatoryWeakModifier;

      //Test each Synapse from the active neuron
      for (int synap=0; synap < currentSynapses; synap++) {
        double connectionStrength = synapses[synap].getWeight();
        CANTNeuron ToNeuron = synapses[synap].getTo();

        //If both Neurons were active,
        if ((ToNeuron.fired) && (this.fired)) {
          modification = getIncreaseBase(connectionStrength);
          toCompensatoryStrengthModifier = getStrengthCompensatoryModifier(
              totalIncomingSynapticStrength);
          modification *= toCompensatoryStrengthModifier;
          connectionStrength = connectionStrength+modification;
          synapses[synap].setWeight(connectionStrength);
        } //end of to neuron active

        //Weaken connections (anti-hebbian rule)
        else if (ToNeuron.fired) {
          //Weaken activation
          modification = getDecreaseBase(connectionStrength);
          toCompensatoryWeakModifier = getWeakCompensatoryModifier(
              this.totalIncomingSynapticStrength);
          modification *= toCompensatoryWeakModifier;
          connectionStrength = connectionStrength-modification;
          synapses[synap].setWeight(connectionStrength);
        }
      }
    }
 
    //this is currently only called by CANTNet subclasses 5/12/07
    public void modifySynapticWeight (int synapseNum) {
      double totalConnectionStrength = getTotalConnectionStrength();
      double modification;
      CANTNeuron toNeuron= synapses[synapseNum].toNeuron;
      double connectionStrength = synapses[synapseNum].getWeight();

      if (toNeuron.fired) {
        if (!isInhibitory){
          modification = getIncreaseBase(connectionStrength);
          modification *= 
            getStrengthCompensatoryModifier(totalConnectionStrength);
          connectionStrength = connectionStrength+modification;
          //System.out.println("Inc Exc "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }

        else{//decrease inhibition
          modification =getDecreaseBase(connectionStrength);
          modification *= getWeakCompensatoryModifier(totalConnectionStrength);
          connectionStrength = connectionStrength-modification;
          //System.out.println("dec Inh "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
      } //end of to neuron active

      //if to Neuron is inactive
      else {
        if (!isInhibitory){
          modification =getDecreaseBase(connectionStrength);
          modification *= getWeakCompensatoryModifier(totalConnectionStrength);
          connectionStrength = connectionStrength-modification;
          //System.out.println("dec Exc "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
        else {
          modification = getIncreaseBase(connectionStrength);
          modification *= 
            getStrengthCompensatoryModifier(totalConnectionStrength);
          connectionStrength = (connectionStrength)-modification;
          //System.out.println("Inc Inh"+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
      }
      synapses[synapseNum].setWeight(connectionStrength);
    }



/*
//post not pre forgetting only with excitatory
    public void OldLearn(){
      double modification;
      double fromCompensatoryStrengthModifier,fromCompensatoryWeakModifier;
      double toCompensatoryStrengthModifier = 1.0;
      double toCompensatoryWeakModifier = 1.0;
      double totalConnectionStrength = getTotalConnectionStrength();
      fromCompensatoryWeakModifier = getWeakCompensatoryModifier(totalConnectionStrength);
      fromCompensatoryStrengthModifier = getStrengthCompensatoryModifier(totalConnectionStrength);

      //Test each Synapse from the active neuron
      for (int synap=0; synap < currentSynapses; synap++) {
        double connectionStrength = synapses[synap].getWeight();
        CANTNeuron toNeuron = synapses[synap].getTo();

        //If both Neurons were active,
        if ((toNeuron.fired) && (this.fired)) {
          modification = getIncreaseBase(connectionStrength);
          modification *= fromCompensatoryStrengthModifier *
                          toCompensatoryStrengthModifier;
          connectionStrength = connectionStrength+modification;
          synapses[currentSynapses].setWeight(connectionStrength);
        } //end of to neuron active

        //Weaken connections (anti-hebbian rule)
        else if (toNeuron.fired) {
          //Weaken activation
          modification = getDecreaseBase(connectionStrength);
          modification *= fromCompensatoryWeakModifier *
                          toCompensatoryWeakModifier;
          connectionStrength = connectionStrength-modification;
          synapses[synap].setWeight(connectionStrength);
        }
      }
    }

    //pre not post learning
public void learn(){

    double modification;
    double fromCompensatoryStrengthModifier,fromCompensatoryWeakModifier;
    double toCompensatoryStrengthModifier = 1.0;
    double toCompensatoryWeakModifier = 1.0;

    //Only learn when the from neuron is active
    if (!fired) return;

    fromCompensatoryWeakModifier = getWeakCompensatoryModifier(
           getTotalConnectionStrength());
    fromCompensatoryStrengthModifier = getStrengthCompensatoryModifier(
      getTotalConnectionStrength());

    //Test each Synapse from the active neuron
    for (int synap=0; synap < currentSynapses; synap++) {
        double connectionStrength = synapses[synap].getWeight();
        CANTNeuron toNeuron = synapses[synap].getTo();

        //If both Neurons were active,
        if (toNeuron.fired) {
          if (this.isInhibitory()){
            modification =
               getIncreaseBase(connectionStrength+(parentNet.getAxonalStrengthMedian()*2));
            modification *= fromCompensatoryWeakModifier *
                           toCompensatoryStrengthModifier;
            connectionStrength = connectionStrength+modification;
            synapses[synap].setWeight(connectionStrength);
          }
          else {
            modification = getIncreaseBase(connectionStrength);
            modification *= fromCompensatoryStrengthModifier *
                           toCompensatoryStrengthModifier;
            connectionStrength = connectionStrength+modification;
            synapses[synap].setWeight(connectionStrength);
//            System.out.println(id+ "---"+synapses[synap].getTo().id+" = "+connectionStrength);
          }
        } //end of to neuron active

        //Weaken connections (anti-hebbian rule)
        else {
           if (isInhibitory()){
            modification =
               getDecreaseBase(connectionStrength+(parentNet.getAxonalStrengthMedian()*2));
            modification *= fromCompensatoryStrengthModifier *
                           toCompensatoryWeakModifier;
            connectionStrength = connectionStrength-modification;
            synapses[synap].setWeight(connectionStrength);
            }
          else {
            //Weaken activation
            modification = getDecreaseBase(connectionStrength);
            modification *= fromCompensatoryWeakModifier *
                           toCompensatoryWeakModifier;
            connectionStrength = connectionStrength-modification;
            synapses[synap].setWeight(connectionStrength);
          }
        }
    }
}


//:::::::::::::::::::::::::::::::::::::::::::
//Learning depends on a function, and it's inverse
//The above learning is implicitly based on a linear function
//This learning (below) is based on the learning function LF


double HEAT = -5.0;
double LNE = 2.718;

  double LF (double prob){
    double adjProb = (prob*2) - 1.0;  //convert from 0-1 to -1-1
    double base = Math.pow(LNE,HEAT*adjProb);
    double result = 1/(1+base);
    return result;
  }

  double LFInverse(double weight){
    double base = Math.log((1/weight)-1);
    double result = (base)/HEAT;
    result = (result + 1)/2;
    if (result < 0.01) result = 0.01;
    else if (result > .99) result = 0.99;
    return result;
  }

//A variant of learn that works on absolute value of synapses instead of deltas
  public void learn2() {
    double oldProb, newProb, newWeight;

    //Only learn when the from neuron is active
    if (!fired) return;
    //Test each Synapse from the active neuron
    for (int synap=0; synap < currentSynapses; synap++) {

      CANTNeuron toNeuron = synapses[synap].getTo();
      if (toNeuron.fired) {
        if (!isInhibitory()){
          oldProb = LFInverse(synapses[synap].getWeight());
          newProb = oldProb + ((1-oldProb)*parentNet.getLearningRate());
          newWeight = LF(newProb);
          synapses[synap].setWeight(newWeight);
        }
        else{
          oldProb = LFInverse(1+synapses[synap].getWeight());
          newProb = oldProb + ((1-oldProb)*parentNet.getLearningRate());
          newWeight = LF(newProb);
          synapses[synap].setWeight(newWeight-1);
        }
      }//if fired
      else {
        if (!isInhibitory()){
          oldProb = LFInverse(synapses[synap].getWeight());
          newProb = oldProb * (1-parentNet.getLearningRate());
          newWeight = LF(newProb);
          synapses[synap].setWeight(newWeight);
        }
        //make inhibition further from 0
        else{
          oldProb = LFInverse(1+synapses[synap].getWeight());
          newProb = oldProb * (1-parentNet.getLearningRate());
          newWeight = LF(newProb);
          synapses[synap].setWeight(newWeight-1);
        }
      }
    }//for all synapses
  }

*/
 

  //just copied learn4 and modified it so that it only learns
  //if the toNeurons net is of learntype 1.
  public void restrictedLearn(){
    double totalConnectionStrength;
    double modification;
    double fromCompensatoryStrengthModifier,fromCompensatoryWeakModifier;

    //Only learn when the from neuron is active
    if (!fired) return;

    totalConnectionStrength = getTotalConnectionStrength();
    fromCompensatoryWeakModifier = 
       getWeakCompensatoryModifier(totalConnectionStrength);
    fromCompensatoryStrengthModifier = 
       getStrengthCompensatoryModifier(totalConnectionStrength);
    //System.out.println("mods "+totalConnectionStrength+" "+fromCompensatoryWeakModifier+" "+fromCompensatoryStrengthModifier);


    //Test each Synapse from the active neuron
    for (int synap=0; synap < currentSynapses; synap++) {
      double connectionStrength = synapses[synap].getWeight();
      CANTNeuron toNeuron = synapses[synap].getTo();
      if (toNeuron.parentNet.getLearningOn() != 1)
	continue;

      //If both Neurons were active,
      else if (toNeuron.fired) {
        if (!isInhibitory){
          modification = getIncreaseBase(connectionStrength);
          //System.out.println("Inc Exc1 "+connectionStrength+" "+modification+" "+connectionStrength);
          modification *= fromCompensatoryStrengthModifier;
          connectionStrength = connectionStrength+modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("Inc Exc "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }

        else{//decrease inhibition
          modification =getDecreaseBase(connectionStrength);
          modification *= fromCompensatoryWeakModifier;
          connectionStrength = connectionStrength-modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("dec Inh "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
      } //end of to neuron active


      //if to Neuron is inactive
      else {
        if (!isInhibitory){
          modification =getDecreaseBase(connectionStrength);
		  modification *= fromCompensatoryWeakModifier;
          connectionStrength = connectionStrength-modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("dec Exc "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }

        else {
          modification = getIncreaseBase(connectionStrength);
          modification *= fromCompensatoryStrengthModifier;
          connectionStrength = (connectionStrength)-modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("Inc Inh"+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
      } // end of to Neuron inactive
    }
  }
  
  public void learn4(){
    double totalConnectionStrength;
    double modification;
    double fromCompensatoryStrengthModifier,fromCompensatoryWeakModifier;

    //Only learn when the from neuron is active
    if (!fired) return;

    totalConnectionStrength = getTotalConnectionStrength();
    fromCompensatoryWeakModifier = 
	   getWeakCompensatoryModifier(totalConnectionStrength);
    fromCompensatoryStrengthModifier = 
	   getStrengthCompensatoryModifier(totalConnectionStrength);
    //System.out.println("mods "+totalConnectionStrength+" "+fromCompensatoryWeakModifier+" "+fromCompensatoryStrengthModifier);


    //Test each Synapse from the active neuron
    for (int synap=0; synap < currentSynapses; synap++) {
      double connectionStrength = synapses[synap].getWeight();
      CANTNeuron toNeuron = synapses[synap].getTo();

      //If both Neurons were active,
      if (toNeuron.fired) {
        if (!isInhibitory){
          modification = getIncreaseBase(connectionStrength);
          modification *= fromCompensatoryStrengthModifier;
          connectionStrength = connectionStrength+modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("Inc Exc "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }

        else{//decrease inhibition
          modification =getDecreaseBase(connectionStrength);
          modification *= fromCompensatoryWeakModifier;
          connectionStrength = connectionStrength-modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("dec Inh "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
      } //end of to neuron active


      //if to Neuron is inactive
      else {
        if (!isInhibitory){
          modification =getDecreaseBase(connectionStrength);
          modification *= fromCompensatoryWeakModifier;
          connectionStrength = connectionStrength-modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("dec Exc "+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }

        else {
          modification = getIncreaseBase(connectionStrength);
          modification *= fromCompensatoryStrengthModifier;
          connectionStrength = (connectionStrength)-modification;
          synapses[synap].setWeight(connectionStrength);
          //System.out.println("Inc Inh"+this.getId()+" "+toNeuron.getId()+" "+synapses[synap].getWeight()+" "+modification);
        }
      } // end of to Neuron inactive
    }
  }

}//end Neuron Class



