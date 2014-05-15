package uk.ac.mdx.cs.jmirto.cabot;

import java.io.*;
import java.util.*;
import java.net.*;  //for URL

public class CANTNet {

  private static int LONGEST_TIME_NEURON_ACTIVE =0;

  protected int cols = 10;
  protected int rows = 10;
  private String name;
  protected int topology;

  protected Vector patterns = new Vector();
  public CANTNeuron neurons[];

  protected int totalNeurons =0;
  private int curPattern = -1;
  boolean associationTest = false;
  boolean allowRunOn=true;
  public String netFileName="Net.dat";

  private double activationThreshold;
  private double axonalStrengthMedian;
  private boolean changeEachTime;
  private int compensatoryDivisor;
  private boolean compensatoryLearningOn;
  private double connectionStrength;
  private double connectivity;
  private float decay;
  private float fatigueRecoveryRate;
  private int learningOn; // 0 no, 1 yes, 2 only to nets with 1, 3 subclass
  private float learningRate;
  private int likelihoodOfInhibitoryNeuron;
  private boolean neuronsFatigue;
  private int neuronsToStimulate;
  private float saturationBase;
  private boolean spontaneousActivationOn;
  private float fatigueRate;
  private int cyclesPerRun;
  protected int cyclesToStimulatePerRun=10;
  private boolean isBaseNet;
  public boolean recordingActivation=true;
	
  public int getTotalNeurons() {return totalNeurons;}
  public int getCurrentPattern() {return curPattern;}
  public void setCurrentPattern(int newPattern) 
    {curPattern = newPattern;}
  public int getTotalPatterns() {return patterns.size();}
  public CANTPattern getPattern(int index ) 
	  {return (CANTPattern)patterns.get(index);}
  public int getRows() {return rows;}
  public int getCols() {return cols;}
  public int size() {return (cols*rows);}
  public int getSize() {return (cols*rows);}
  public void setName(String Name) {name = Name;}
  public String getName() {return name;}
  public void setLearningOn(int newLearningOn) 
    {learningOn=newLearningOn;}
  public void setLearningOn(boolean newLearningOn) {
	if (newLearningOn)
	  learningOn = 1;
	else
	  learningOn = 0;
    }	
  public int getLearningOn() {return learningOn;}
  public boolean isLearningOn() {
	  if (learningOn == 1) return true;
	  else return false;
	  }
  public void setCompensatoryLearningOn(boolean newCompensatoryLearningOn) 
    {compensatoryLearningOn = newCompensatoryLearningOn;}
  public boolean isCompensatoryLearningOn() 
	{return compensatoryLearningOn;}
    public void setSpontaneousActivationOn(boolean newSpontaneousActivationOn) {
      spontaneousActivationOn = newSpontaneousActivationOn;}
    public boolean isSpontaneousActivationOn() {return spontaneousActivationOn;}
    public void setChangeEachTime(boolean newChangeEachTime) {
      changeEachTime = newChangeEachTime;}
    public boolean isChangeEachTime() {return changeEachTime;}
    public void setNeuronsFatigue(boolean newNeuronsFatigue) {
      neuronsFatigue = newNeuronsFatigue;}
    public boolean isNeuronsFatigue() {return neuronsFatigue;}
    public void setLikelihoodOfInhibitoryNeuron(int newLikelihoodOfInhibitoryNeuron) {
      likelihoodOfInhibitoryNeuron = newLikelihoodOfInhibitoryNeuron;}
    public int getLikelihoodOfInhibitoryNeuron() {
	 return likelihoodOfInhibitoryNeuron;}
    public void setDecay(float newDecay) {decay = newDecay;}
    public float getDecay() {return decay;}
    public void setFatigueRate(float newFatigueRate) {
      fatigueRate = newFatigueRate;}
    public float getFatigueRate() {return fatigueRate;}
    public void setFatigueRecoveryRate(float newFatigueRecoveryRate) {
      fatigueRecoveryRate = newFatigueRecoveryRate;}
    public float getFatigueRecoveryRate() {
      return fatigueRecoveryRate;}
    public void setLearningRate(float newLearningRate) {
      learningRate = newLearningRate;}
    public float getLearningRate() {
      return learningRate;}
    public void setCompensatoryDivisor(int newCompensatoryDivisor) {
      compensatoryDivisor = newCompensatoryDivisor;}
    public int getCompensatoryDivisor() {
      return compensatoryDivisor;}
    public void setSaturationBase(float newSaturationBase) {
      saturationBase = newSaturationBase;}
    public float getSaturationBase() {
      return saturationBase;}
    public void setAxonalStrengthMedian(double newAxonalStrengthMedian) {
      axonalStrengthMedian = newAxonalStrengthMedian;}
    public double getAxonalStrengthMedian() {
      return axonalStrengthMedian;}
    public void setActivationThreshold(double newActivationThreshold) {
      activationThreshold = newActivationThreshold;}
    public double getActivationThreshold() {return activationThreshold;}
    public void setConnectivity(double newConnectivity) {
	  connectivity = newConnectivity;}
    public double getConnectivity() {return connectivity;}
    public void setConnectionStrength(double newConnectionStrength) 
	  {connectionStrength = newConnectionStrength;}
    public double getConnectionStrength() {return connectionStrength;}
    public void setNeuronsToStimulate(int newNeuronsToStimulate) 
	  {neuronsToStimulate = newNeuronsToStimulate;}
    public int getNeuronsToStimulate() {return neuronsToStimulate;}
    public int getCyclesPerRun() {return cyclesPerRun;}
    public void setCyclesPerRun(int cycles) {cyclesPerRun = cycles;}
    public void setCyclesToStimulatePerRun(int cycles) 
	   {cyclesToStimulatePerRun = cycles;}
    public int getCyclesToStimulatePerRun() 
       {return (cyclesToStimulatePerRun);}
  public void setRecordingActivation(boolean rA) {recordingActivation = rA;}



	//----real code starts here----
  public CANTNet(){}

  public CANTNet(String name,int cols, int rows,int topology){
    this.cols = cols;
    this.rows = rows;
    this.name = name;
    this.topology = topology;
    netFileName = name + ".dat";
  }
  
  public CANTNet getNewNet(String name,int cols, int rows,int topology){
//System.out.println("get new base net ");
  	CANTNet net = new CANTNet (name,cols,rows,topology);
    return (net);
  }
  
	
  public void initializeNeurons() {
    createNeurons();
    if (topology < 0){
      setConnections(0,size());
    } 
    else System.out.println("bad topology specified "+ topology);
  }
	
  //---------------IO Functions -------
  protected void createNeurons() {
    totalNeurons = 0;
    neurons = new CANTNeuron[cols*rows];
    for(int i=0;i< cols*rows;i++)
      neurons[i] = new CANTNeuron(totalNeurons++,this);
  }

  protected void createNeurons(int synapsesPerNeuron) {
    totalNeurons = 0;
    neurons = new CANTNeuron[cols*rows];
    for(int i=0;i< cols*rows;i++)
      neurons[i] = new CANTNeuron(totalNeurons++,this,synapsesPerNeuron);
  }

  public void readBetweenAllNets() {
    System.out.println("CANTNet read Between Connections Called: None Will Be Read");
  //if you get this message and you want to read connections between nets,
  //you need to put it in the subclass.
  }
  
  private LineNumberReader inputFile;
  private void openReadFile() {
    DataInputStream dIS;
    InputStreamReader inputSR;
 
   try{
     dIS = new DataInputStream(new FileInputStream(netFileName));
     inputSR = new InputStreamReader(dIS);
     inputFile = new LineNumberReader (inputSR);
   }
   
   catch (IOException e) {
     System.err.println("input file not opened properly\n" +
                         e.toString());
     System.exit(1);
   }
  }

  public static void readAllNets() {
    System.out.println("read all nets");

    Enumeration eNum = CANT23.nets.elements();
    while (eNum.hasMoreElements()) {
      CANTNet net = (CANTNet)eNum.nextElement();
      net.readNet(false);
    }	
  }
  
  protected void readConnectTo(CANTNet toNet) {
    System.out.println("read Between " +getName() + " and " + toNet.getName());
    String inputLine;
    openReadFile();  
    try {
      inputLine = inputFile.readLine(); //read row
      inputLine= inputFile.readLine();  //read col


      //read the neurons
      for (int cNeurons=0; cNeurons < (rows*cols); cNeurons++) {
      	neurons[cNeurons].readNeuronConnectTo(inputFile,toNet);
      }
      
      inputFile.close();
      }
     catch (IOException e) {
      System.err.println("input file not read properly\n" +
                           e.toString());
      System.exit(1);}
  }
  
  public void readNet(boolean readInterConnections){
    System.out.println("read net " + netFileName);  
    String inputLine;

    openReadFile();
   
    try {
      inputLine = inputFile.readLine();
      rows=Integer.parseInt(inputLine);
      System.out.print ( Integer.toString(rows) + "\n");

      inputLine= inputFile.readLine();
      cols=Integer.parseInt(inputLine);
      System.out.print ( Integer.toString(cols) + "\n");

      //Create new neurons
      createNeurons();

      //read the neurons
      for (int cNeurons=0; cNeurons < (rows*cols); cNeurons++) {
 	neurons[cNeurons].readNeuron(inputFile,readInterConnections);
      }
	  
      inputFile.close();
      }
     catch (IOException e) {
      System.err.println("input file not read properly\n" +
                           e.toString());
      System.exit(1);}
  }

  public void write(){
    DataOutputStream output;
   
    try {    
      output = new DataOutputStream(new FileOutputStream(netFileName));

      output.writeBytes(Integer.toString(rows)+"\n"+Integer.toString(cols) +"\n");

      for ( int i=0;i<(size()); i++ )  {
        output.writeBytes( Integer.toString(i) + " Neuron\n");
	  
	    if (neurons[i].getCurrentSynapses()==0)
	      output.writeBytes("0 Axons\n");
        else
          for (int j=0; j< neurons[i].getCurrentSynapses() ; j++ ) {
            if (j==0)
              output.writeBytes(Integer.toString(neurons[i].getCurrentSynapses()) + " Axons\n");
	          CANTNeuron toNeuron = neurons[i].synapses[j].toNeuron;
              output.writeBytes(toNeuron.parentNet.getName() + " ");
              output.writeBytes(Integer.toString(toNeuron.id) +
                " " +  Double.toString(neurons[i].synapses[j].weight) + "\n");
          }
	}
    System.out.println("Network saved");
    output.close();
  }
  catch (IOException e) {
      System.err.println("output file not opened properly\n" +
                           e.toString());
      System.exit(1);  }
}


    // kailash
    public void addNewPattern(CANTPattern pattern) {
    	patterns = new Vector();
    	patterns.add(pattern);
    }

    public void addPattern(CANTPattern pattern) {
      patterns.add(pattern);
    }
		
  public void clear() {
    //System.out.println("clear " + getName());
    for (int cNeuron = 0 ; cNeuron < size(); cNeuron++)
      neurons[cNeuron].clear();
  }
	
  protected int getLeftNeighbor(int neuronID) {
    if ((neuronID % cols) == 0)
      return (neuronID + cols - 1);
    else
      return (neuronID - 1);
  }
  
  protected int getRightNeighbor(int neuronID) {
    if ((neuronID % cols) == (cols -1))
       return (neuronID - cols + 1);
    else
       return (neuronID + 1);
  }
  
  protected int getTopNeighbor(int neuronID) {
    if (neuronID < cols)
      return (neuronID + ((rows-1) * cols));
    else
      return (neuronID - cols);
  }
  
  protected int getBottomNeighbor(int neuronID) {
    if ((neuronID / cols) == (rows -1))
      return (neuronID - ((rows-1) * cols));
    else
      return (neuronID + cols);
  }
  protected void addConnection(int fromNeuron, int toNeuron, double weight) {
    if (toNeuron == fromNeuron) return;
    Assert(toNeuron < size());

    weight = neurons[fromNeuron].isInhibitory()? weight*-1:weight;
    neurons[fromNeuron].addConnection(neurons[toNeuron],weight);
  }

  //Set the connection strength of this axon, then recursively
  //call to set up connections (at a lower likelihood) to other connections.
  protected void recursiveSetConnections (int fromNeuron, int toNeuron, int distance) {
    int N1,N2,N3,N4;
    double weight;

    //Set up the initial Weight
    weight = (float)(((CANT23.random.nextFloat()) + 1) * connectionStrength);

    //with a probability lessening as distance increases
    if (CANT23.random.nextFloat() < (1.0 / (distance * connectivity)))
      addConnection(fromNeuron,toNeuron,weight);

    //call for children if they're close.
    if (distance < 4) {
      N1 = getLeftNeighbor(toNeuron);
      N2 = getRightNeighbor(toNeuron);
      N3 = getTopNeighbor(toNeuron);
      N4 = getBottomNeighbor(toNeuron);
      recursiveSetConnections(fromNeuron,N1,distance + 1);
      recursiveSetConnections(fromNeuron,N2,distance + 1);
      recursiveSetConnections(fromNeuron,N3,distance + 1);
      recursiveSetConnections(fromNeuron,N4,distance + 1);
    }
  }

  //Set up a distance bias set of
  //connections.  Assume that the Neurons are
  //in a 2-D space.  Those next to it (lr and td) are
  //likely to be connected, next step away less so.
  protected void setConnections(int startNeuron, int endNeuron) {
    int currentFromNeuron;
    int N1 = -1,N2 = -1,N3 = -1,N4 = -1;
    int cNeurons = size();

    Assert(cNeurons >= endNeuron);

    //For Each Neuron
    for (currentFromNeuron = startNeuron ; currentFromNeuron < endNeuron;
         currentFromNeuron ++) {

      //Get it's neighbors Make N1 a long distance connection
      N1 = (int)(N1 + (CANT23.random.nextFloat() * size()))%size();
      N2 = getRightNeighbor(currentFromNeuron);
      N3 = getTopNeighbor(currentFromNeuron);
      N4 = getBottomNeighbor(currentFromNeuron);
      recursiveSetConnections(currentFromNeuron,N1,1);
      recursiveSetConnections(currentFromNeuron,N2,1);
      recursiveSetConnections(currentFromNeuron,N3,1);
      recursiveSetConnections(currentFromNeuron,N4,1);
    }
  }
  
  protected void setConnectionsRandomly(int neuronNum,int numConnections, double weight) {
    for (int connection=0; connection < numConnections; connection++)
      {
      int curConnections=neurons[neuronNum].getCurrentSynapses();
      addConnection(neuronNum,(int)(CANT23.random.nextFloat()*size()),weight);
      if (curConnections==neurons[neuronNum].getCurrentSynapses())
	    connection--;
      }
  }

  public int getActives() {
    int totalActives = 0;
    for (int index = 0; index < size(); index++) {
      if (neurons[index].getFired())
        totalActives++;
    }
    return totalActives;
  }
  
  //call measure for recording the state of the net.
  public void setMeasure(int cantStep) {
  }
  
  //This is called from step in frame and makes
  //all of the nets run one step
  public void runAllOneStep(int cantStep) {
    Enumeration eNum = CANT23.nets.elements();
  
    while (eNum.hasMoreElements()) {
      CANTNet net = (CANTNet)eNum.nextElement();
	  net.runOneStep(cantStep);
    }
  }

  public void runOneStep(int cantStep) {
    changePattern(cantStep);
    setExternalActivation(cantStep);
    propogateChange();
    learn();

    if (recordingActivation) setMeasure(cantStep);
  }

  public void subclassLearn() {
   System.out.println(getName() + " Youve got learningOn = 3, subclass it or no learning");
  }
	
  public void learn() {
    if (learningOn == 0) return;
    //System.out.println(getName() + " " +  axonalStrengthMedian + " " + saturationBase);
 
    //This is relatively new.  If learning is 3, we call learn in the subclass.
    if (learningOn == 3) {
        subclassLearn();
        return;
    }

    int totalNeurons = size();
    for (int neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++) 
      {
      if (learningOn == 2)//only learn if the to neurons net is of learn type 1
	  neurons[neuronIndex].restrictedLearn();
       else
      neurons[neuronIndex].learn4();
      }
  }
  
  public void spontaneousActivate () {
    int neuronIndex;
    int totalNeurons = size();

    //spontaneously activate
    for (neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++) {
      if (neurons[neuronIndex].spontaneouslyActivate())
	activate(neuronIndex,(activationThreshold*2));
    }
  }

  public void setNeuronsFired () {
    int neuronIndex;
    int totalNeurons = size();
    //Set whether neuron has fired.
    for (neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++)
      {
      neurons[neuronIndex].setFired();
      //if (neurons[neuronIndex].getFired())
      //  System.out.println(getName() + " " + neuronIndex + " Fired " + CANT23.CANTStep);
      }
  }

  public void setDecay () {
    int neuronIndex;
    int totalNeurons = size();
    //resetActivation and apply decay
    for (neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++)
         neurons[neuronIndex].resetActivation();
  } 

  public void spreadActivation () {
    int neuronIndex;
    int totalNeurons = size();
    //for each formally active neuron spread activation
    for (neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++){
     if ((!associationTest) && (neurons[neuronIndex].getFired())) 
        neurons[neuronIndex].spreadActivation();
    }
  }

  public void setFatigue () {
    int neuronIndex;
    int totalNeurons = size();
    //modify fatigue
    if (neuronsFatigue)
      for (neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++) {
        neurons[neuronIndex].modifyFatigue();
      }
  }


  //New activation may have come externally.  Current is that activation +
  //any remaining from last time.
  public void propogateChange(){
    spontaneousActivate();
    setNeuronsFired();
    setDecay ();
    spreadActivation();
    setFatigue();
  }
  
  protected void activate (int neuronNumber, double activation) {
    Assert(neuronNumber < size());
    if (neuronNumber >= size()) 
       System.out.println(getName());
	
    double currentActivation = neurons[neuronNumber].getActivation(); 
    activation += currentActivation; 
    neurons[neuronNumber].setActivation(activation);
  }
	
  public void setExternalActivation(int cantStep){
	if ((!changeEachTime) &&
	  (cantStep% cyclesPerRun > cyclesToStimulatePerRun) &&
      (allowRunOn))
      return;
	  
    CANTPattern pattern = (CANTPattern)patterns.get(curPattern);
	int neuronsToStimulateNow = neuronsToStimulate > pattern.size()?pattern.size():neuronsToStimulate;
    for (int i= 0; i < neuronsToStimulateNow; i++) {
      activate(pattern.getPatternIndex(i),
        (activationThreshold+(CANT23.random.nextFloat()*activationThreshold)));
    }
  }

public void changePattern(int cantStep)
  {
  if (changeEachTime || (cantStep %cyclesPerRun)==0){	 
    CANT23.experiment.endEpoch();
    curPattern = CANT23.experiment.selectPattern(curPattern, patterns.size(),
      this);
    ((CANTPattern)patterns.get(curPattern)).arrange(neuronsToStimulate);
  }
}
  
  //read in a new file of patterns and set the patterns for this
  //net to them.
  public void getNewPatterns(String fileName) {
  	patterns = new Vector();
	NetManager.readPatternFile(fileName,this);
  }
  

    private void checkExcitatoryConnections() {
  	int totalNeurons = size();
  	int inhibitoryCount =0;
  	for (int neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++)
    	if (!neurons[neuronIndex].isInhibitory()) {
      	inhibitoryCount++;
     CANTNeuron neuron = neurons[neuronIndex];
     for (int synIndex =0; synIndex < neuron.getCurrentSynapses(); synIndex++ ) {
       double weight = neuron.synapses[synIndex].getWeight();
       CANTNeuron toNeuron = neuron.synapses[synIndex].getTo();
       System.out.println("Exc "+neuronIndex+ "---"+toNeuron.getId()+" = "+ weight);
     }
    }
System.out.println("total excitatory neurons = "+inhibitoryCount);
}

    private void checkInhibitoryConnections() {
      int totalNeurons = size();
      int inhibitoryCount =0;
      for (int neuronIndex = 0; neuronIndex < totalNeurons; neuronIndex++)
        if (neurons[neuronIndex].isInhibitory()) {
          inhibitoryCount++;
         CANTNeuron neuron = neurons[neuronIndex];
         for (int synIndex =0; synIndex < neuron.getCurrentSynapses(); synIndex++ ) {
           double weight = neuron.synapses[synIndex].getWeight();
           CANTNeuron toNeuron = neuron.synapses[synIndex].getTo();
           System.out.println("Inh "+neuronIndex+ "---"+toNeuron.getId()+" = "+ weight);
         }
        }
 System.out.println("total inhibitory neurons = "+inhibitoryCount);
    }

  //Set Connections from this net to another net.
  public void setOtherConnections(CANTNet otherNet, int connectionsPerNeuron) {

    int toSize = otherNet.size();
    for (int neuronIndex = 0; neuronIndex < size(); neuronIndex++){
      for (int newConnection = 0; newConnection < connectionsPerNeuron; newConnection++){
        int toNeuron = (int)(CANT23.random.nextFloat()*toSize);
        double weight  = neurons[neuronIndex].isInhibitory()? -0.1:0.1;
        neurons[neuronIndex].addConnection(otherNet.neurons[toNeuron],weight);
      }
    }
  }

    public void recordNeuronActiveTime(int time){
      if (time > LONGEST_TIME_NEURON_ACTIVE)
        LONGEST_TIME_NEURON_ACTIVE = time;
    }
    private boolean Assert(boolean test) {
      int x = -1;
      if (! test)
      try{
        x = (1 / (1 +x));
      }
      catch(Exception e){
        System.out.println("Nettest = "+test);
        return false;
      }
      return true;
    }

  public void printNeuronsFired(){
  	int neuronsFired=0;
  	for (int i = 0; i < size(); i++) 
	  {
	  	if (neurons[i].getFired()) neuronsFired++;
	  }
	  
    System.out.println(getName() + " "+neuronsFired+ " ");
	}
	
  public void kludge() {
  //this is just a function for debugging purposes.  Subclass it so you can
  //call it from the interface.
  System.out.println("CANTNet kludge ");
  }

private void printAverageFatigue(){
  float averageFatigue=0;
  float maxfatigue =0;
  int maxI=-1;
  for(int i=0;i<size();i++){
    if (maxfatigue< neurons[i].getFatigue()){
      maxfatigue = neurons[i].getFatigue();
      maxI = i;
    }
  }
//  System.out.println("Max Fatigue = "+maxfatigue+ "Neuron= "+maxI);
}


    public boolean selectPattern(int patternNum) {
      if (patternNum >= patterns.size() || patternNum<0)
        return false ;
      curPattern = patternNum;
      ((CANTPattern)patterns.get(curPattern)).arrange(neuronsToStimulate);
      return true;
    }
	
    public void writeParameters() {
           int LearningValue;
           DataOutputStream OutputFile;
           String outString="";

           outString=Integer.toString(likelihoodOfInhibitoryNeuron);
           outString=outString + " Likelihood of Inhibitory Neuron\n";
           outString=outString + Float.toString(decay);
           outString=outString + " Decay\n";
           outString=outString + Float.toString(fatigueRate);
           outString=outString +" Fatigue Rate\n";
           outString=outString + Float.toString(fatigueRecoveryRate);
           outString=outString + " Fatigue Recovery RAte\n";
           outString=outString + Float.toString(learningRate);
           outString=outString + " Learning Rate\n";
           outString=outString + Integer.toString(compensatoryDivisor);
           outString=outString + " Compensatory Divisor\n";
           outString=outString + Float.toString(saturationBase);
           outString=outString + " Saturation Base\n";
           outString=outString + Double.toString(axonalStrengthMedian);
           outString=outString + " Axonal Strength Median\n";
           outString=outString + Double.toString(activationThreshold);
           outString=outString + " Activation Threshold\n";
           outString=outString + Double.toString(connectivity);
           outString=outString + " Connectivity\n";
           outString=outString + Double.toString(connectionStrength);
           outString=outString + " Connection Strength\n";

           if (learningOn == 0) LearningValue = 0;
           else if (!compensatoryLearningOn) LearningValue = 1;
           else LearningValue = 2;

           outString=outString + Integer.toString(LearningValue);
           outString=outString + " Learning On\n";
           outString=outString + Integer.toString(neuronsToStimulate);
           outString=outString + " Neurons To Stimulate\n";
           outString=outString + changeEachTime;
           outString=outString + " Change Each Time\n";
           outString=outString + neuronsFatigue;
           outString=outString + " Neurons Fatigue\n";
           outString=outString + spontaneousActivationOn;
           outString=outString + " Spontaneous Activation\n";
           outString=outString + cyclesPerRun;
           outString=outString + " Cycles Per Run\n";
           System.out.println(outString);
   }
   
   
}
