package uk.ac.mdx.cs.jmirto.cabot;


import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.net.*;

public class NetManager {
  private static CANTNet nullNet;

  public static Hashtable readNets(InputStream input) {
    try{
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
      return readNets(document);
    }
    catch(Exception e){
      e.printStackTrace();
    }
      return null;
  }

  public static Hashtable readNets(String fileName, CANTNet netType){
    nullNet = netType;
    try{
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setIgnoringComments(true);	// kailash
	DocumentBuilder builder = factory.newDocumentBuilder();
      
        Document document = builder.parse(new File(fileName));
      
      	return readNets(document);
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return null;
  }

  private static Hashtable readNets(Document document)  {
    Hashtable nets = new Hashtable();
    NodeList nodeList = document.getFirstChild().getChildNodes();

    for(int i=1; i<nodeList.getLength();i=i+2){
      CANTNet net = readNet(nodeList.item(i));
      nets.put(net.getName(),net);
    }
    return nets;
  }

  private static CANTNet readNet(Node node){
    NamedNodeMap nodeMap = node.getAttributes();
    CANTNet net = nullNet.getNewNet(nodeMap.getNamedItem("Name").getNodeValue(),
	Integer.parseInt(nodeMap.getNamedItem("Cols").getNodeValue()),
	Integer.parseInt(nodeMap.getNamedItem("Rows").getNodeValue()),
	Integer.parseInt(nodeMap.getNamedItem("Topology").getNodeValue()));
	

    //If you cant get the specialised net topology make sure you have
    //getNewNet defined in the net file
    //System.out.println(nullNet.getClass().getName());
    //System.out.println(net.getClass().getName());

    net.setActivationThreshold(Double.parseDouble(nodeMap.getNamedItem("activationThreshold").getNodeValue()));
    net.setAxonalStrengthMedian(Double.parseDouble(nodeMap.getNamedItem("axonalStrengthMedian").getNodeValue()));
    net.setChangeEachTime(getBoolFromString(nodeMap.getNamedItem("changeEachTime").getNodeValue()));
    net.setCompensatoryDivisor(Integer.parseInt(nodeMap.getNamedItem("compensatoryDivisor").getNodeValue()));

    int learningType = Integer.parseInt(nodeMap.getNamedItem("learningOn").getNodeValue());
    if (learningType>0) {
      net.setLearningOn(true);
      if (learningType==2)
         net.setCompensatoryLearningOn(true);
	  else if (learningType == 3)  
	    {
	    net.setLearningOn(3);
	    net.setCompensatoryLearningOn(false);
	    }
	  
    }
    else net.setLearningOn(false);

    net.setConnectionStrength(Double.parseDouble(nodeMap.getNamedItem("connectionStrength").getNodeValue()));
    net.setConnectivity(Double.parseDouble(nodeMap.getNamedItem("connectivity").getNodeValue()));
    net.setDecay(Float.parseFloat(nodeMap.getNamedItem("decay").getNodeValue()));
    net.setFatigueRate(Float.parseFloat(nodeMap.getNamedItem("fatigueRate").getNodeValue()));
    net.setFatigueRecoveryRate(Float.parseFloat(nodeMap.getNamedItem("fatigueRecoveryRate").getNodeValue()));
    net.setLearningRate(Float.parseFloat(nodeMap.getNamedItem("learningRate").getNodeValue()));
    net.setLikelihoodOfInhibitoryNeuron(Integer.parseInt(nodeMap.getNamedItem("likelihoodOfInhibitoryNeuron").getNodeValue()));
    net.setNeuronsFatigue(getBoolFromString(nodeMap.getNamedItem("neuronsFatigue").getNodeValue()));
    net.setNeuronsToStimulate(Integer.parseInt(nodeMap.getNamedItem("neuronsToStimulate").getNodeValue()));
    net.setSaturationBase(Float.parseFloat(nodeMap.getNamedItem("saturationBase").getNodeValue()));
    net.setSpontaneousActivationOn(getBoolFromString(nodeMap.getNamedItem("spontaneousActivationOn").getNodeValue()));
    net.setCyclesPerRun( Integer.parseInt(nodeMap.getNamedItem("cyclesPerRun").getNodeValue()));

    net.initializeNeurons();
    //Node newNode = node.getFirstChild();
    readPatterns(node.getFirstChild().getNextSibling(),net);
    return net;
  }

  //read an XML file that has just patterns in it.  Add these
  //patterns to the nets patterns.
  public static void readPatternFile(String fileName,CANTNet net){
    try{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);	// kailash
		DocumentBuilder builder = factory.newDocumentBuilder();
      
		Document document = builder.parse(new File(fileName));
	    
		Node node = document.getFirstChild();
		readPatterns(node,net);
    }
    catch(Exception e){ e.printStackTrace(); }
  }
  
    private static void readPatterns(Node node, CANTNet net){
//      try{
        NodeList nodeList = node.getChildNodes();
        for(int i=1; i<nodeList.getLength();i=i+2){
          net.addPattern(readPattern(nodeList.item(i),net));
        }
//      }catch(Exception e){ e.printStackTrace();}
    }

  private static CANTPattern readPattern(Node node,CANTNet net) {
    NamedNodeMap nodeMap = node.getAttributes();
    String name = nodeMap.getNamedItem("Name").getNodeValue();
    int number  = Integer.parseInt(nodeMap.getNamedItem("Number").getNodeValue());

    NodeList patternList = node.getChildNodes();
    String [] patternPoints = new String[(patternList.getLength()-1)/2];
    for (int k=0,j=1; j< patternList.getLength();j=j+2,k++){
      NamedNodeMap patternPointsMap = patternList.item(j).getAttributes();
      patternPoints[k] = patternPointsMap.getNamedItem("StartIndex").getNodeValue()+
        ","+patternPointsMap.getNamedItem("EndIndex").getNodeValue();
    }
    return new CANTPattern(net,name,number,patternPoints);
  }

    private static boolean getBoolFromString(String str)
    {
        if (str.equalsIgnoreCase("true"))
           return true;
        else
            return false;
    }

}