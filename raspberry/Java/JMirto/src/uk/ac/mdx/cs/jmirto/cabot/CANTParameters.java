package uk.ac.mdx.cs.jmirto.cabot;

import java.util.*;

public class CANTParameters {

  public static final String ACTIVATION_THRESHOLD ="0";
  public static final String AXONAL_STRENGTH_MEDIAN ="1";
  public static final String CHANGE_EACH_TIME ="2";
  public static final String COMPENSATORY_DIVISOR ="3";
  public static final String COMPENSATORY_LEARNING_ON ="4";
  public static final String CONNECTION_STRENGTH ="5";
  public static final String CONNECTIVITY ="6";
  public static final String DECAY ="7";
  public static final String FATIGUE_RECOVERY_RATE ="8";
  public static final String LEARNING_ON ="9";
  public static final String LEARNING_RATE ="10";
  public static final String LIKELIHOOD_OF_INHIBITORY_NEURON ="11";
  public static final String NEURONS_FATIGUE ="12";
  public static final String NEURONS_TO_STIMULATE ="13";
  public static final String SATURATION_BASE ="14";
  public static final String SPONTANEOUS_ACTIVATION_ON ="15";
  public static final String FATIGUE_RATE ="16";
  public static final String CYCLES_PER_RUN ="17";

  private Hashtable parameters = new Hashtable();

  public void setParameter(String parameterId, String value){
    parameters.put(parameterId,value);
  }
  public String getParameter(String parameterId) {
    return (String)parameters.get(parameterId);
  }

}
