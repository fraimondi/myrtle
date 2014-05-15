package uk.ac.mdx.cs.jmirto.cabot;

import java.util.Enumeration;

import uk.ac.mdx.cs.jmirto.JMirtoRobot;



public class Robot2CANT23 extends CANT23{
	public static String ExperimentXMLFile;
	public static Robot2Net nullNet;
	public static Robot2Experiment experiment; 
	public static int kludge = 3;

	private static JMirtoRobot robot;

	private static int PWR = 40; // the base wheel speed

	public static void main(String args[]){
		System.out.println("initialize CANT robot2 ");
		firmataSetup();

		//ExperimentXMLFile = "src/robot1/robot1.xml";
		ExperimentXMLFile = "robot2.xml";
		seed = 1;
		initRandom();
		readNewSystem();
		//positionWindows();
		delayBetweenSteps=0;
		isRunning = true;
	}

	private static void firmataSetup() {

		robot = new JMirtoRobot("/dev/ttyAMA0");

		try {
			System.out.println("Starting in 2 seconds...");
			Thread.sleep(2000);
			System.out.println("Starting now");

			robot.enableIR();

			Thread.sleep(500);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public static void setWheelOn(int wheel, int neuronsFiring) {
		int messageValue = wheel*10;

		// FIXME: CHeck with Chris!
		if (neuronsFiring > 45) {
			messageValue = PWR + 21;
		}
		else if (neuronsFiring > 30) {
			messageValue = PWR + 14;
		}
		else if (neuronsFiring > 15) {
			messageValue = PWR + 7;
		}
		
		if (wheel == 5) {
			robot.setMotor(0,messageValue);
		} else if (wheel == 6) {
			robot.setMotor(1,messageValue);
		}
		
		
	}
	
	public static int getIRValue(int pin) {
		int IRVal = robot.getIR(pin);
		return IRVal;
	}

	protected static void readNewSystem() {
		// System.out.println("readNewSystem");
		nullNet = new Robot2Net();

		nets = NetManager.readNets(ExperimentXMLFile, nullNet);
		workerThread = new Robot2CANT23.WorkerThread();
		initializeExperiment();
		// experiment.printExpName();
		workerThread.start();
		connectAllNets();

	}

	private static void connectAllNets() {
	}

	//set up the experiment specific parameters.
	private static void initializeExperiment() {
		experiment = new Robot2Experiment();
		//System.out.println("initialize robot2 Experiment");
		experiment.printExpName();
	}

	public static void runOneStepStart() {
		if (experiment.trainingLength == CANTStep ) 
			experiment.switchToTest();

		experiment.measure(CANTStep);

		//Call experiment to see if the wheels should be on
		int rightWheel = experiment.getRightWheelOn();
		int leftWheel = experiment.getLeftWheelOn();
		System.out.println("Wheels " + rightWheel + " " + leftWheel + " " + CANTStep);

		//set the wheels.
		setWheelOn(5,rightWheel);
		setWheelOn(6,leftWheel);


		if (experiment.isEndEpoch(CANTStep))
			experiment.endEpoch();
	}

	public static synchronized void resetForNewTest() {
		if(!experiment.getInTest()) return;

		Enumeration <?> eNum = nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			//      net.setInitialFatigue(-1.0);
			for (int i = 0; i < net.getSize(); i++) {
				net.neurons[i].setFatigue((float)0.0);
				net.neurons[i].setActivation(0.0);
			}
		}
	}

	private static int numSystems = 1;
	public static synchronized int getNumSystems() {return numSystems;}

	public static synchronized void runOneStep() {
		//runOneStepStart();

		//    System.out.println("Step " + CANTStep);

		Enumeration <?> eNum = nets.elements();
		eNum = nets.elements();
		while (eNum.hasMoreElements()) {
			Robot2Net net = (Robot2Net)eNum.nextElement();
			if (net.getName().compareTo("BaseNet") == 0)
			{
				net.runAllOneStep(CANTStep); 
				CANTStep++;
			}
		}

		//System.out.println("Incremenet cantvis1step"+CANTStep);

		if (experiment.experimentDone(CANTStep)) 
		{
			System.out.println("experiment done "+CANTStep + " " + numSystems);
			closeSystem();
			numSystems++;
			readNewSystem();
			//makeNewSystem(numSystems);
		}
	}


	/* private  static void positionWindows() {
    Robot2Net baseNet = (Robot2Net)experiment.getNet("BaseNet");

    baseNet.cantFrame.setLocation(0,0);
    baseNet.cantFrame.setSize (500,400);
    baseNet.cantFrame.setVisible(true);  
  }
	 */


	//embedded Thread class
	public static class WorkerThread extends CANT23.WorkerThread{
		public void run(){
			//System.out.println("xor Thread ");
			while(true){
				if(isRunning){
					runOneStep();
				}
				else{
					try{sleep(delayBetweenSteps);}
					catch(InterruptedException ie){ie.printStackTrace();}
				}//else
			}//while
		}//run
	}//WorkerThread class
}
