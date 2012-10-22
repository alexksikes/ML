/**
 * This class describes an ANN hypothesis where the number of hidden neurons vary.
 * We decided not to make this class a subclass of fixedSizeANNHypothesis after carefull consideration
 * We chose to use an array for the representation instead of a vector because after consideration
 * having a vector does help much (still need to set the representation...)
 * Author: Alex Ksikes
*/
import java.util.*;

public class varyANNHypothesis extends Hypothesis
{

  /**************** Variables ********************/

  // Each coordinate i represents the weights of the neuron in position i in the hidden layer
  // The weights of each neuron i are represented as a 2d array
  // The first coordinate of the array represents the weights (from left to right)
  // of the backward edges, the second coordinate represents the forward edges (from left to right)
  private double[][][] representation;

  // Number of hidden neurons
  int numHiddenNeurons;

  // The fitness of this hypothesis is the RMS of the corressponding neural net
  double fitness;

  // Training accuracy      // may be removed
  double trainingAcc;
  double evalAcc;

  /**************** Methods **********************/

  // Create an hypothesis of size 3 by default
  public varyANNHypothesis()
  {
    this.numHiddenNeurons=3;
    representation=new double[numHiddenNeurons][][];
    for (int i=0;i<numHiddenNeurons;i++)
    {
      representation[i]=new double[numInputNeurons][numOutputNeurons];
    }
  }

  // Constructor
  public varyANNHypothesis(int numHiddenNeurons)
  {
    this.numHiddenNeurons=numHiddenNeurons;
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
  // The fitness represents 3 digits of the RMS of this hypothesis
  // So that we can keep the number of hidden neurons small by promoting smaller hypotheses
  public void computeFitness()
  {
    double[][] newWeights;
    // We need to create a new neural net (this is the computationly expensive part
    // of the algorithm and I think there is not really a way around this)
    int[] numInLayer=new int[3];
    numInLayer[0]=numInputNeurons;
    numInLayer[1]=numHiddenNeurons;
    numInLayer[2]=numOutputNeurons;
    NeuralNet neuralNetEvaluator=new NeuralNet(3,numInLayer,0,0,1);
    for (int index=0;index<numHiddenNeurons;index++)
    {
      newWeights=getWeights(index);
      neuralNetEvaluator.setNeuronWeights(1,index,newWeights);
    }
    fitness=neuralNetEvaluator.computeRMS(trainingSet);
    fitness=Math.floor(fitness*1000);
    fitness=fitness/1000;

    trainingAcc=neuralNetEvaluator.testDataSet(trainingSet);       // don't need to compute twice (will be removed)
    evalAcc=neuralNetEvaluator.computeRMS(trainingSet);
  }

  // Crossover with another Hypothesis
  public void crossover(Hypothesis otherParent)
  {
    Random value=new Random();
    double newWeights[][];
    int numNeuronsOtherParent=((varyANNHypothesis) (otherParent)).getNumHiddenNeurons();
    // We keep the part between 0 and split1 included of this hypothesis
    int split1=value.nextInt(numHiddenNeurons);
    // We keep the part between split2 included and otherParent.numHiddenNeurons-1
    int split2=value.nextInt(numNeuronsOtherParent);
    //int split1=numHiddenNeurons/2;              // test with a standard crossover
    //int split2=numNeuronsOtherParent/2;
    // Save the weights of this hypothesis
    varyANNHypothesis savedHypothesis=new varyANNHypothesis(numHiddenNeurons);
    savedHypothesis.setRepresentation(this.getRepresentation());
    // Reset the size of this hypothesis
    setNumHiddenNeurons(split1 + 1 + numNeuronsOtherParent - split2);
    // Merge the two parts together
    int index;
    for (index=0;index<split1+1;index++)
    {
      setWeights(index,savedHypothesis.getWeights(index));
    }
    for (int otherParentIndex=split2;otherParentIndex<numNeuronsOtherParent;otherParentIndex++)
    {
      newWeights=otherParent.getWeights(otherParentIndex);
      setWeights(index,newWeights);
      index++;
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
    // First we need to reset the size of the representation to the new representation
    setNumHiddenNeurons(newRepresentation.length);
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

  public int getNumHiddenNeurons()
  {
    return numHiddenNeurons;
  }

  // Reset the size of the representation
  // All data in the representation array will be lost
  public void setNumHiddenNeurons(int numHiddenNeurons)
  {
    // only need to init if the size is different
    //if (this.numHiddenNeurons!=numHiddenNeurons)
    {
      this.numHiddenNeurons=numHiddenNeurons;
      // and we need to reinitialize the representation
      representation=new double[numHiddenNeurons][][];
      for (int i=0;i<numHiddenNeurons;i++)
      {
        representation[i]=new double[numInputNeurons][numOutputNeurons];
      }
    }
  }

  // will be removed
  public double getTrainingAcc()
  {
    return trainingAcc;
  }

  public double getEvalAcc()
  {
    return evalAcc;
  }

}
