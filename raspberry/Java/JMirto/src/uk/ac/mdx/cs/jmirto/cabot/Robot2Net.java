package uk.ac.mdx.cs.jmirto.cabot;



import java.util.Enumeration;

public class Robot2Net extends CANTNet {
	public Robot2Net() {
	}

	public Robot2Net(String name, int cols, int rows, int topology) {
		super(name, cols, rows, topology);
		cyclesToStimulatePerRun = 40;
		recordingActivation = true;
	}


	private int getSensorNeuronsFiring (int sensorVal) {
		if (sensorVal < 20 ) return 0;
		else if (sensorVal < 100 ) return 20;
		else if (sensorVal < 200 ) return 40;
		else if (sensorVal < 500 ) return 60;
		else if (sensorVal < 1000 ) return 80;
		else return 100;
	}

	private void makeNewPattern(int IR1, int IR2, int IR3) 
	{
		Robot2Net inputNet = (Robot2Net) CANT23.experiment.getNet("BaseNet");

		int newPoints;
		int totalPoints;
		int[] points; // points is a global used by these next few functions
		points = new int[300];

		newPoints = getSensorNeuronsFiring(IR1);
		for (int newPoint = 0; newPoint < newPoints; newPoint++) {
			points[newPoint] = (int)(CANT23.random.nextFloat()*100);
		}
		totalPoints = newPoints;

		newPoints = getSensorNeuronsFiring(IR2);
		for (int newPoint = 0; newPoint < newPoints; newPoint++) {
			points[totalPoints+newPoint] = newPoint+100;
		}
		totalPoints += newPoints;

		newPoints = getSensorNeuronsFiring(IR3);
		for (int newPoint = 0; newPoint < newPoints; newPoint++) {
			points[totalPoints+newPoint] = 
					(int)(CANT23.random.nextFloat()*100)+200;
		}
		totalPoints += newPoints;

		CANTPattern newPat= new CANTPattern(inputNet,"bob",0,totalPoints,points);
		inputNet.addNewPattern(newPat);
		System.out.println("New Pat " + inputNet.getTotalPatterns());

		setNeuronsToStimulate(totalPoints);
	}

	// need this to subclass experiment
	public void changePattern(int cantStep) {
		//      if ((cantStep%10) == 0) {
		int IR1,IR2,IR3;
		IR1 = Robot2CANT23.getIRValue(1); 
		IR2 = Robot2CANT23.getIRValue(2);
		IR3 = Robot2CANT23.getIRValue(3);
		System.out.println("Cur Pat " + IR1 + " " +IR2 + " " +IR3);
		setCurrentPattern(0);
		//((CANTPattern) patterns.get(0).arrange(getNeuronsToStimulate());
		makeNewPattern(IR1,IR2,IR3);
		//}
	}


	public void runAllOneStep(int CANTStep) {  
		//This series of loops is really chaotic, but I needed to
		//get all of the propagation done in each net in step.
		Robot2CANT23.runOneStepStart();

		Enumeration <?> eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			//net.runOneStep(CANTStep);
			net.changePattern(CANTStep);
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			net.setExternalActivation(CANTStep);
		}

		//net.propogateChange();  
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			net.setNeuronsFired();
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			net.setDecay ();
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			net.spreadActivation();
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			net.setFatigue();
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			net.learn();
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			//net.cantFrame.runOneStep(CANTStep+1);
		}
		eNum = CANT23.nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			if (net.recordingActivation) net.setMeasure(CANTStep); 	  
		}
	}


	public CANTNet getNewNet(String name, int cols, int rows, int topology) {
		Robot2Net net = new Robot2Net(name, cols, rows, topology);
		return (net);
	}

	protected void createNeurons() {
		if (topology == 1) {
			totalNeurons = 0;
			neurons = new CANTNeuron[cols * rows];
			for (int i = 0; i < cols * rows; i++) {
				neurons[i] = new CANTNeuron(totalNeurons++, this);
				// neurons[i] = new
				// CANTNeuronSpontaneousFatigue(totalNeurons++,this);
				neurons[i].setCompensatoryBase(10.0);
			}
		}
		else
			System.out.println("error in Robot2Net.createNuerons ");
	}


	protected void setConnections() {
		for(int i=0;i< cols*rows;i++)
		{
			if (neurons[i].isInhibitory())
				setConnectionsRandomly(i,20,0.01);
			else
				setConnectionsRandomly(i,20,0.01);
		}
	}



	private void setInputTopology() {
		double weight = 0.3;
		int toNeuron;

		//connect sensor 1 to right wheel
		for (int neuronNum = 0; neuronNum < 100; neuronNum++) {
			for (int synapse = 0; synapse < 10; synapse ++) {
				toNeuron = neuronNum + (int)(CANT23.random.nextFloat()*100);
				toNeuron %= 100;
				toNeuron += 300;
				addConnection(neuronNum,toNeuron,weight);
			}
		}
		//connect sensor 3 to left wheel
		for (int neuronNum = 0; neuronNum < 100; neuronNum++) {
			for (int synapse = 0; synapse < 10; synapse ++) {
				toNeuron = neuronNum + (int)(CANT23.random.nextFloat()*100);
				toNeuron %= 100;
				toNeuron += 400;
				addConnection(neuronNum+200,toNeuron,weight);
			}
		}
	}


	public void initializeNeurons() {
		// set up topologies.
		createNeurons();
		if (topology == 1) {
			// System.out.println("Robot 2 Topology ");
			setInputTopology();
		} else
			System.out.println("bad toppology specified " + topology);
		//write();
	}


	public void kludge () {
		System.out.println("kludge " + Robot2CANT23.kludge );
		Robot2Net net = (Robot2Net) CANT23.experiment.getNet("SomNet");


		if (Robot2CANT23.kludge == 0) {
			net = (Robot2Net) CANT23.experiment.getNet("BaseNet");
		}

		else if (Robot2CANT23.kludge == 3) {
			for (int i= 0; i < 10; i++) 
			{
				System.out.println(i + " " +  net.neurons[i].getFatigue()); 
			}
		}
		else {
			for (int i= 0; i < 10; i++) 
			{
				System.out.println(i + " act " +  net.neurons[i].getActivation()); 
			}
		}
		/*
	           double avgWeights;
	      double totalWeights = getTotalWeights(net.neurons[i]);
	      avgWeights = getTotalWeights(net.neurons[i],"OutputNet");
	      System.out.println(i + " " + avgWeights + " " + totalWeights); 
	      //System.out.println(i + " " + this.neurons[i].getActivation() + 
	      //                      " " + this.neurons[i].getFatigue());
		 */
	}

	public void measure(int currentStep) {
		System.out.println("measure " + neurons[0].getActivation() + " " + 
				neurons[0].getFired() + " " + 
				currentStep);
	}
}
