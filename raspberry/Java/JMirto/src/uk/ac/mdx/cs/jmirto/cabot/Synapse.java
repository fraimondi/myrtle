package uk.ac.mdx.cs.jmirto.cabot;

import java.io.*;

public class Synapse {

  //change it to private later CANTNet.write needs it
  public double weight;
  //change it to private later CANTNet.write needs it
  public CANTNeuron fromNeuron;
  //change it to private later CANTNet.write needs it
  public CANTNeuron toNeuron;
  private CANTNet parentNet; //undone do I actually need this

  public CANTNeuron getTo () {return toNeuron; }
  public double getWeight() {return weight;}

  public Synapse (CANTNet net,CANTNeuron from,CANTNeuron to,double wt) {
    parentNet = net;
    fromNeuron = from;
    toNeuron = to;
    weight = wt;
  }

  public void clear() {
    weight = 0;
  }
	
  public void setWeight(double Wt) {
    double median = parentNet.getAxonalStrengthMedian();

    if (parentNet.getLearningOn() > 0)
	  {	
      if (fromNeuron.isInhibitory()) 
        {
        if (Wt < ((-2*median) + 0.01))
          weight = (-2*median) + 0.01;
        else if (Wt > -0.01)
          weight = -0.01;
        else weight = Wt;
        }
      else // if (!fromNeuron.isInhibitory())
        { 
	    //        if (Wt < 0.01)
	    //weight = 0.01;
        if (Wt < 0.0)
          weight = 0.0;
        else if (Wt > ((2*median) - 0.01))
          weight = ((2*median) - 0.01);
        else weight = Wt;
        }
	  }
	else weight = Wt;
  }


}
