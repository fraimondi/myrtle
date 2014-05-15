package uk.ac.mdx.cs.jmirto.cabot;


public class Robot2Experiment extends CANTExperiment {
	
  public Robot2Experiment () {
		Robot2Net inputNet = (Robot2Net) getNet("BaseNet");
  }
  
  public boolean experimentDone(int CANTStep) {
    return false;  
  }
  
  public boolean isEndEpoch(int Cycle) {
	Robot2Net inputNet = (Robot2Net) getNet("BaseNet");
		
	if ((Cycle % inputNet.getCyclesPerRun() == 0)) {
		return (true);
	}
	return (false);
  }

	public void printExpName() {
		System.out.println("Robot2 Exp");
	}

    private int measureNeuronsFiringInArea(int area) {
	Robot2Net inputNet = (Robot2Net) getNet("BaseNet");
        int result = 0;
        for (int cNeuron = 0; cNeuron < 100; cNeuron ++) {
	    int offset = (area*100) + cNeuron;

            if (inputNet.neurons[offset].getFired()) result++;
        }
        return result;
    }
	public void measure(int currentStep) {
	    int IR1Neurons = measureNeuronsFiringInArea(0);
	    int IR2Neurons = measureNeuronsFiringInArea(1);
	    int IR3Neurons = measureNeuronsFiringInArea(2);
	    int topNeurons = measureNeuronsFiringInArea(3);
	    int bottomNeurons = measureNeuronsFiringInArea(4);

            System.out.println("measure " + currentStep + " " + IR1Neurons +
            " " +IR2Neurons + " " +IR3Neurons + " " + topNeurons
	    + " " + bottomNeurons);
	}
   
    public int getRightWheelOn() {
      int topNeurons = measureNeuronsFiringInArea(3);
      return topNeurons;
    }

    public int getLeftWheelOn() {
      int topNeurons = measureNeuronsFiringInArea(4);
      return topNeurons;
    }

} //end class

