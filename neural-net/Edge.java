/**
 * Class Edge for the neural network
 * Each edge has an assoicated weight that changes along the way
 * Author: Alex Ksikes
**/

import java.lang.Math;
import java.io.Serializable;

public class Edge implements Serializable
{
  /*********** Variables **************/

  private double weight;            // the weight associated with this edge
  private Neuron source;            // the neuron it is connected to in layer i
  private Neuron dest;	            // the neuron it is connected to in layer i+1
  private int id;	            // identifier for the edge
  private int mode;	            // the mode to use for update rule
  private double deltaWeight;       // the change in weight of this edge
  private double deltaWeightOld;    // keep track of the old value of the change in weight

  // backpropation parameters
  private double momentum ;	// momentum term in order not to get stuck in local minima
  private double learningRate;	// learning rate changes the speed of how convergence takes place

  /*********** Methods *****************/

  /**
  * Edge Constructor
  * @param parent is the neuron it is connected to in layer i
  * @param child is the neuron it is connected to in layer i+1
  * @param lr is the learning rate it is initialized to
  * @param idd is the id of this Edge
  * @param momentum is the momentum rate this edge is initialized to
  * @param mode indicates the mode (Batch or Stochastic) to use for training
  **/
  public Edge(Neuron parent, Neuron child, double lr, int idd, double momentum, int mode)
  {
    this.source=parent;
    this.dest=child;
    this.learningRate=lr;
    this.id=idd;
    this.momentum=momentum;
    this.mode=mode;
    this.deltaWeight=0;
    this.deltaWeightOld=0;
  }

  /**
  * Resets the weight for the edge with some random value
  * Chooses the weights uniformly between w and -w
  * Where w=1/sqrt(n) and n=# of inputs going into the destination of this edge
  **/
  public void reset()
  {
    int n=getDest().getParentNum();
    double w=1.0/Math.sqrt(n);
    if (Math.random()>1/2.0)
      weight=Math.random()*w;
    else
      weight=-1*Math.random()*w;
  }

  /**
  * Sets the momentum of the neuron
  **/
  public void setMomentum(double mn)
  {
    this.momentum=mn;
  }

  /**
  * Sets the learning rate of the neuron
  **/
  public void setLearningRate(double lr)
  {
    this.learningRate=lr;
  }

  /**
  * Set the weight
  **/
  public void setWeight(double x)
  {
    this.weight=x;
  }

  /**
  * Update the change in weight of this edge according to the error term
  * @param errTerm is the error term that is propagated down
  **/
  public void updateDeltaWeight(double errterm)
  {
    Neuron s=getSource();
    deltaWeight=deltaWeight + learningRate*(1-momentum)*errterm*s.getValue()
                            + momentum*deltaWeightOld;
  }

  /**
  * The change in weight is saved and reinitialized
  **/
  public void initDeltaWeight()
  {
    deltaWeightOld=deltaWeight;   // for momentum...
    deltaWeight=0;
  }

  /**
  * Update the weight of this edge
  **/
  public void updateWeight()
  {
    weight=weight + deltaWeight;
  }

  /**
  * @return the weight for this edge
  **/
  public double getWeight()
  {
    return weight;
  }

  /**
  * @return the source neuron for this edge
  **/
  public Neuron getSource()
  {
    return source;
  }

  /**
  * @return the destination neuron for this edge
  **/
  public Neuron getDest()
  {
    return dest;
  }

}
