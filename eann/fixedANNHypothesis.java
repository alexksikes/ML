/**
 * This class describes an ANN hypothesis where the number of hidden neurons
 * are assumed to be constant.
  * Author: Alex Ksikes
*/

import java.util.*;

public class fixedANNHypothesis extends Hypothesis
{

  /**************** Variables ********************/

  static int numHiddenNeurons;            // Number of hidden neurons of this hypothesis

  // Used to evaluate the fitness
  static NeuralNet neuralNet;

  // Each coordinate i represents the weights of the neuron in position i in the hidden layer
  // The weights of each neuron i are represented as a 2d array
  // The first coordinate of the array represents the weights (from left to right)
  // of the backward edges, the second coordinate represents the forward edges (from left to right)
  private double[][][] representation;

  // The fitness of this hypothesis is the RMS of the corressponding neural net
  double fitness;

  // Training accuracy      // may be removed
  double trainingAcc;
  double evalAcc;

  /**************** Methods **********************/

  // Constructor
  public fixedANNHypothesis()
  {
    representation=new double[numHiddenNeurons][][];
    for (int i=0;i<numHiddenNeurons;i++)
    {
      representation[i]=new double[numInputNeurons][numOutputNeurons];
    }
  }

  // Give to this hypothesis random representation
  // Every weight is initialized to a random value between -1 and 1
  public void setToRandom()
  {
    double[][] weights;
    for (int index=0;index<numHiddenNeurons;index++)
    {
      weights=getWeights(index);
      for(int i=0;i<numInputNeurons;i++)
      {
        weights[i][0]=Math.random()*2 - 1;
      }
      for (int i=0;i<numOutputNeurons;i++)
      {
        weights[0][i]=Math.random()*2 - 1;
      }
    }
  }

  // Compute fitness for this Hypothesis
  // The fitness is the RMS of this hypothesis
  public void computeFitness()
  {
    double[][] newWeights;
    for (int index=0;index<numHiddenNeurons;index++)
    {
      newWeights=getWeights(index);
      neuralNet.setNeuronWeights(1,index,newWeights);
    }
    fitness=neuralNet.computeRMS(trainingSet);
    trainingAcc=neuralNet.testDataSet(trainingSet);        // don't need to compute twice (will be removed)
    evalAcc=neuralNet.testDataSet(evaluationSet);
  }

  // Crossover with another Hypothesis
  // Apply a uniform crossover
  public void crossover(Hypothesis otherParent)
  {
    double newWeights[][];
    for (int index=0;index<numHiddenNeurons;index++)
    {
      if (Math.random()>0.5)
      {
        newWeights=otherParent.getWeights(index);
        setWeights(index,newWeights);
      }
    }
  }

  // Mutate this Hypothesis
  // Add a number distributed normally between -1 and 1
  public void mutate()
  {
    Random value=new Random();
    double oldWeight;
    double weights[][];
    for (int index=0;index<numHiddenNeurons;index++)
    {
      weights=getWeights(index);
      for(int i=0;i<numInputNeurons;i++)
      {
        oldWeight=weights[i][0];
        weights[i][0]=oldWeight + value.nextGaussian();     // check this
      }
      for (int i=0;i<numOutputNeurons;i++)
      {
        oldWeight=weights[0][i];
        weights[0][i]=oldWeight + value.nextGaussian();     // check this
      }
    }
  }

  // Returns the representation of this hypothesis
  public double[][][] getRepresentation()
  {
    return representation;
  }

  // Sets the representation of this hypothesis to a new representation
  public void setRepresentation(double[][][] newRepresentation)
  {
    double[][] newWeights;
    double[][] weights;
    for (int index=0;index<numHiddenNeurons;index++)
    {
      newWeights=newRepresentation[index];
      weights=getWeights(index);
      for(int i=0;i<numInputNeurons;i++)
      {
        weights[i][0]=newWeights[i][0];
      }
      for (int i=0;i<numOutputNeurons;i++)
      {
        weights[0][i]=newWeights[0][i];
      }
    }
  }

  // Returns the fitness of this hypothesis
  public double getFitness()
  {
    return fitness;
  }

  // Pretty print this hypothesis
  public String toString()
  {
    return "" + numHiddenNeurons + " Hidden Neurons";
  }

  // Set the weights of a neuron at the specified index location on the hidden layer
  public void setWeights(int index, double[][] newWeights)
  {
    double[][] weights=getWeights(index);
    for (int i=0;i<numInputNeurons;i++)
    {
      weights[i][0]=newWeights[i][0];
    }
    for (int i=0;i<numOutputNeurons;i++)
    {
      weights[0][i]=newWeights[0][i];
    }
  }

  // Returns the set of weights at the specified index location of the hidden layer
  public double[][] getWeights(int index)
  {
    return representation[index];
  }

  // will be removed
  public double getTrainingAcc()
  {
    return trainingAcc;
  }

  public int getNumHiddenNeurons()
  {
    return numHiddenNeurons;
  }

  public double getEvalAcc()
  {
    return evalAcc;
  }
}
