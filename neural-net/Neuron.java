import java.lang.Math;
import java.util.Vector;
import java.io.Serializable;

/**
  * Neuron class. Each neuron contains its own activation function and value.
  * Author: Alex Ksikes
 **/
public class Neuron implements Serializable
{

  /************ Variables *************/

  private double value;		// value in this neuron
  private double errorTerm;	// error term of this neuron
  private Vector fEdge;		// set of forward edges
  private Vector bEdge;		// set of backward edges
  private int id;		// id of the neuron

  /************ Methods *************/

  /**
  * Constructor for the neuron
  * @param id of the neuron
  **/
  public Neuron(int idd)
  {
    id=idd;
    fEdge=new Vector();
    bEdge=new Vector();
  }

  /**
  * Gets the id of the neuron
  **/
  public int getId()
  {
    return id;
  }

  public Vector getBackwardEdges()
  {
    return bEdge;
  }

  public Vector getForwardEdges()
  {
    return fEdge;
  }

  /**
  * Activation function
  * Here we simply call the sigmoid function
  * @param f is the input to this activation function
  **/
  public double actf(double f)
  {
    return sigmoid(f);
  }

  public double sigmoid(double y)
  {
    return 1.0 / ( 1 + Math.exp(-1*y) );
  }

  /**
  * Set the value for this neuron. Meant only for input neurons
  **/
  public void setValue(double x)
  {
    value=x;
  }

  /**
  * @return the value in this neuron
  **/
  public double getValue()
  {
    return value;
  }

  /**
  * calculates the value of this node based on weights of backedges and inputs of parents
  **/
  public void calValue()
  {
    // Compute weighted sum of its imput parents
    double net=0;
    Edge e;
    for (int i=0; i<getParentNum(); i++)
    {
      e=(Edge) bEdge.get(i);
      net=net + e.getWeight()* e.getSource().getValue();
    }
    // Apply activation function
    value=actf(net);
  }

  /**
  * @return the number of forward edges
  **/
  public int getChildNum()
  {
    return fEdge.size();
  }

  /**
  * @return the number of backwards edges
  **/
  public int getParentNum()
  {
    return bEdge.size();
  }

  /**
  * Add a new forward edge
  **/
  public void addForwardEdge(Edge e)
  {
    fEdge.add(e);
  }

  /**
  * Add a new backward edge
  **/
  public void addBackwardEdge(Edge e)
  {
    bEdge.add(e);
  }

  /**
  * Propagate the error back for output layer
  * Assumes sigmoid function as activation function
  * @param targetVal is value that this output layer should get
  **/
  public void backErrorTrack(double targetVal)
  {
    errorTerm=value*(1-value)*(targetVal-value);
  }

  /**
  * @return the current value of the error term
  **/
  public double getErrorTerm()
  {
    return errorTerm;
  }

  /**
  * Propagate the error back for hidden layer
  * Assumes sigmoid function as activation function
  **/
  public void backErrorTrack()
  {
    double sum=0;
    Edge e;
    for (int i=0; i<getChildNum(); i++)
    {
      e=(Edge) fEdge.get(i);
      sum=sum + e.getWeight()* e.getDest().getErrorTerm();
    }
    errorTerm=value*(1-value)*sum;
  }

}
