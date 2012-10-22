import java.util.*;
import java.io.*;
import java.util.zip.*;

/**
  * NeuralNet Class : A neural network consists of edges and neuron
  * Author: Alex Ksikes
**/
public class NeuralNet implements Serializable
{

  /**************** Variables ********************/

  final static int MAXNEURONS     = 60;	// maximum number of neurons in each layer
  final static int BATCHMODE      = 0;  // batch mode for training
  final static int STOCHASTICMODE = 1;	// stochastic mode for training
  Vector neurons;		        // set of neurons in this neural network
  Vector edges;			        // set of edges in this neural network
  int [][] neuronsIndex;	        // index of neurons. First component in this 2D array is the layer,
                                        // second component is the layer numbering
  int inputNum;				// number of input neurons
  int outputNum;			// number of output neurons
  int layerNo;			        // number of layers
  double learningRate;			// learning rate for the entire neural network
  double momentum;			// momentum term
  double errAcc;		        // global error in the network
  int mode;				// the mode to use for training

  /**************** Methods **********************/

  /**
  * Main Constructor
  * Sets up the Multi-layer Neural Network
  * @param layerNo indicates the number of layers
  * @param []numInLayer contains the number of neurons in each layer,
  * from input layer to hidden layers to output layer
  * @param learningRate sets how fast the networks changes its weight
  * @param momentum determine how much the weight change is based on past update
  * @param mode indicates the mode (Batch or Stochastic) to use for training
  * Step 1 : Creates all the neurons in the network, ordered by layers,
  * keeping the index of the neuron in neuronsIndex[][]
  * Step 2 : Calls MLPNetworkSetup to set up the edges between the neurons,
  * determined by the layers they are in.
  **/
  public NeuralNet(int layerNo, int[] numInLayer, double learningRate, double momentum, int mode)
  {
    // Set up the neuron index array
    neuronsIndex=new int[layerNo][];
    for (int i=0; i<layerNo; i++)
      neuronsIndex[i]=new int[numInLayer[i]];

    this.layerNo=layerNo;
    this.learningRate=learningRate;
    this.momentum=momentum;
    this.mode=mode;
    this.inputNum=numInLayer[0];
    this.outputNum=numInLayer[layerNo-1];
    this.neurons=new Vector();

    // Step 1
    int id=0;
    // in layer i
    for (int i=0;i<layerNo;i++)
    {
      // and layer numbering j
      for (int j=0;j<numInLayer[i];j++)
      {
        neurons.add(id,new Neuron(id));
        neuronsIndex[i][j]=id;          // keeping track of the index of each neuron
        id++;
      }
    }
    // Step 2
    MLPNetworkSetup(numInLayer);
  }

  /**
  * Set up the forward backward edges relationship in the neural network in MLP fashion
  * @param numInLayer indicates the number of neuron in each layer
  * Steps  : Create an edge for each neuron in layer i and neuron in layer i+1.
  *          Add this edge to the set of forward edges for the neuron in layer i
  *	     Add this edge to the set of backward edges for the neuron in layer i+1
  *          Call the edge reset function to set random initial weights.
  **/
  private  void MLPNetworkSetup(int [] numInLayer)
  {
    this.edges=new Vector();
    Neuron parent;
    Neuron child;
    Edge e;
    int id=0;
    // in layer i
    for (int i=0;i<layerNo-1;i++)
    {
      // and layer numbering j
      for (int j=0;j<numInLayer[i];j++)
      {
        parent=(Neuron) neurons.get(neuronsIndex[i][j]);  // get neuron at this location
        for (int k=0;k<numInLayer[i+1];k++)
        {
          child=(Neuron) neurons.get(neuronsIndex[i+1][k]);
          e=new Edge(parent,child,learningRate,id,momentum,mode);
          parent.addForwardEdge(e);
          child.addBackwardEdge(e);
          edges.add(id,e);
          id++;
        }
      }
    }
  }

  // Call the reset function on each edge
  // Assumes a network has been built
  public void init()
  {
    for (int i=0;i<edges.size();i++)
      ((Edge) edges.get(i)).reset();
  }

  // Set the weights of the neuron at the specified layer and index in this neural net
  // This method is used for the genetic algorithm in the decoding process
  public void setNeuronWeights(int layer, int index, double[][] weights)
  {
    Neuron neuron=((Neuron) neurons.get(neuronsIndex[layer][index]));
    Edge edge;
    for (int i=0;i<inputNum;i++)
    {
      edge=((Edge) neuron.getBackwardEdges().get(i));
      edge.setWeight(weights[i][0]);
    }
    for (int i=0;i<outputNum;i++)
    {
      edge=((Edge) neuron.getForwardEdges().get(i));
      edge.setWeight(weights[0][i]);
    }
  }

  /**
  * Print out all the weights of all the edges
  * A useful debugging tool to see whether your neural network is indeed changing the weights
  **/
  public void printWeight()
  {
    for (int i=0;i<edges.size();i++)
    {
      System.out.print("Weight of edge "+i+": "+ ((Edge) edges.get(i)) .getWeight()+"  ");
    }
  }

  /**
  * run the network given an array of attributes from an example
  * @param Example example contains the input attributes
  * Step 1: Set all the input neurons [neurons in layer 0] with the attributes in this example
  * Step 2: Calculate the value of each neuron beginning from the input layer to the output layer.
  **/
  private void runNetwork(Example example)
  {
    // Step 1
    Neuron input;
    double x;
    for (int j=0;j<inputNum;j++)
    {
      input=(Neuron) neurons.get(neuronsIndex[0][j]);
      x=example.getAttribute(j);
      input.setValue(x);
    }
    // Step 2
    Neuron n;
    for (int i=1;i<layerNo;i++)
    {
      for (int j=0;j<neuronsIndex[i].length;j++)
      {
        n=(Neuron) neurons.elementAt(neuronsIndex[i][j]);
        n.calValue();
      }
    }
  }


  /**
  * Train the network using this example
  * @param example contains the input attributes
  * Step 1: run network on this training example
  * Step 2: perform backpropagation based on the class label and network output for this example
  **/
  private void trainSingle(Example example)
  {
    runNetwork(example);
    // compute the change in weight
    backPropagation(example);
    // update the weights after having seen this single example
    updateWeights();
  }

  /**
  * Train the network using all the examples in the training set
  * @param example contains the input attributes
  **/
  private void trainAll(DataSet trainingSet)
  {
    Example example;
    for (int i=0;i<trainingSet.size();i++)
    {
      example=trainingSet.getExample(i);
      runNetwork(example);
      // update the change in weight of each edge
      backPropagation(example);
    }
    // now update the weights of each edge
    updateWeights();
  }

  /**
  * Update the weights of this ANN and reinitialize the change in weights
  **/
  public void updateWeights()
  {
    Edge e;
    for (int i=0;i<edges.size();i++)
    {
      e=(Edge) edges.get(i);
      e.updateWeight();
      e.initDeltaWeight();
    }
  }

  /**
  * To test a single element
  * Assume there is only one output neuron, so the class label is simply based on whether the output value
  * is more than 0.5 or less than 0.5. If output neuron >0.5 return 1 else return 0.
  * @param example contains the input attribute
  * @return the class it should be in
  **/
  public int testSingle(Example example)
  {
    Neuron output;
    runNetwork(example);
    output=(Neuron) neurons.elementAt(neuronsIndex[layerNo-1][0]);
    if (output.getValue()>0.5)
      return 1;
    else
      return 0;
  }

  /**
  * To test the accuracy level of the entire data set
  * @param testSet contains all the example to test for accuracy
  * @param return the percentage of correct classification
  **/
  public double testDataSet(DataSet testSet)
  {
    Example example;
    int label;
    int testValue;
    int numCorrect=0;
    int numExamples=testSet.size();
    for (int i=0;i<numExamples;i++)
    {
      example=testSet.getExample(i);
      testValue=testSingle(example);
      label=example.getClassLabel();
      if (testValue==label)
        numCorrect++;
    }
    return ((1.0*numCorrect)/numExamples);
  }

  // Compute the root mean squared error of this network
  public double computeRMS(DataSet trainingSet)
  {
    Example example;
    int networkOutput;
    int targetOutput;
    int numExamples=trainingSet.size();
    double sum=0;
    for (int i=0;i<numExamples;i++)
    {
      example=trainingSet.getExample(i);
      networkOutput=testSingle(example);
      targetOutput=example.getClassLabel();
      sum=Math.pow(networkOutput-targetOutput,2) + sum;
    }
    return Math.sqrt(sum/numExamples);
  }

  // Report the accuracy (RMS error and training and validation accuracy after every n epoch)
  // and write into these data
  public void reportAccuracy(DataSet trainingSet,DataSet evaluationSet,int epoch,int n,FileWriter out) throws IOException
  {
    double trainingAcc=0;
    double validationAcc=0;
    // report RMS error
    System.out.println("RMS : " + computeRMS(trainingSet));
    // report training and validation accuracy after every n epochs
    if (epoch%n==0)
    {
      trainingAcc=testDataSet(trainingSet);
      System.out.println("Training Acc : " + trainingAcc);
      validationAcc=testDataSet(evaluationSet);
      System.out.println("Validation Acc : " + validationAcc);
      out.write("\n"+epoch+","+validationAcc+","+trainingAcc+"\r");
    }
  }

  /**
  * Allow user to use the entire data set to train for specified number of iterations
  * @param trainingSet contains all the examples to be used for training
  * @param evaluationSet contains all the examples for validation purpose
  * @param epochNum indicates the number of iterations to train for
  **/
  public void iterativeTrain(DataSet trainingSet, DataSet evaluationSet, int epochNum, FileWriter out) throws IOException
  {
    int examplesNum=trainingSet.size();
    Example example;
    if (mode==STOCHASTICMODE)
    {
      for (int epoch=0;epoch<epochNum;epoch++)
      {
          for (int i=0;i<trainingSet.size();i++)
          {
            example=trainingSet.getExample(i);
            trainSingle(example);
          }
          reportAccuracy(trainingSet,evaluationSet,epoch,20,out);
      }
    }
    if (mode==BATCHMODE)
    {
      for (int epoch=0;epoch<epochNum;epoch++)
      {
        trainAll(trainingSet);
        reportAccuracy(trainingSet,evaluationSet,epoch,20,out);
      }
    }
  }

  /**
  * Perform back propagation : gradient descent search to learn the weights of the network
  * Stochastic gradient descent
  * @param example contains all the class label for this example
  **/
  public void backPropagation(Example example)
  {
    Neuron output;
    Neuron n;
    Edge e;
    double targetVal;
    double errorTerm;
    // from output-to-hidden units
    targetVal=example.getClassLabel();
    for (int j=0;j<outputNum;j++)
    {
      output=(Neuron) neurons.get(neuronsIndex[layerNo-1][j]);
      output.backErrorTrack(targetVal);
      errorTerm=output.getErrorTerm();
      for (int k=0;k<output.getParentNum();k++)
      {
        e=(Edge) (output.getBackwardEdges()).get(k);
        e.updateDeltaWeight(errorTerm);
      }
    }
    // from hidden-to-hidden units till hidden-to-input units
    for (int i=layerNo-2; i>0; i--)
    {
      for (int j=0;j<neuronsIndex[i].length;j++)
      {
        n=(Neuron) neurons.elementAt(neuronsIndex[i][j]);
        n.backErrorTrack();
        errorTerm=n.getErrorTerm();
        for (int k=0;k<n.getParentNum();k++)
        {
          e=(Edge) (n.getBackwardEdges()).get(k);
          e.updateDeltaWeight(errorTerm);
        }
      }
    }
  }

  /**
    * Save NeuralNetwork to file
   **/
  public void save(String filename)
  {
    //  Create a file dialog to query the user for a filename.
    try {
      // Create the necessary output streams to midistruct.
      FileOutputStream fos = new FileOutputStream(filename);
      GZIPOutputStream gzos = new GZIPOutputStream(fos);
      ObjectOutputStream out = new ObjectOutputStream(gzos);
      out.writeObject(this);
      out.flush();
      out.close();
    }
    catch (IOException e)
    {
      System.out.println(e);
    }
  }

  /**
    * Load NeuralNetwork from file
   **/
  public static NeuralNet load(String filename)
  {
    NeuralNet x;
    try {
      // Create necessary input streams
      FileInputStream fis = new FileInputStream(filename);
      GZIPInputStream gzis = new GZIPInputStream(fis);
      ObjectInputStream in = new ObjectInputStream(gzis);
      x = (NeuralNet)in.readObject();
      in.close();                    // Close the stream.
      return x;
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
    return null;
  }


  public Vector getNeurons()
  {
    return neurons;
  }

  public Vector getEdges()
  {
    return edges;
  }

  /** Start off point of this program **/
  public static void main(String args[]) throws IOException
  {
    if (args.length!=3)
    {
      System.out.println("Wrong usage. Type java NeuralNet [trainingFile] [evaluationFile] [mode]");
    }
    else
    {
      // read in the datasets
      DataSet trainingData = new DataSet(args[0]);
      DataSet evaluationData = new DataSet(args[1]);
      int mode = Integer.parseInt(args[2]);

      // decide network topology
      int layers[] = new int[3];
      layers[0] = trainingData.getAttributeNum();
      layers[1] = 9;
      layers[2] = 1;
      //layers[3] = 7;
      //layers[4] = 3;
      //layers[5] = 1;

      // choose the learning rate and momentum parameters
      double learningRate=0.1;    // for batchmode take learning rate less than 0.05
      double momentum=0.01;       // for batchmode take momentum less than 0.001

      // create a network
      NeuralNet myNetwork = new NeuralNet(layers.length,layers, learningRate, momentum, mode);
      // initial the weights
      myNetwork.init();
      System.out.println("MLP Neural Network Created");

      // report parameters to user and write into files for graphs...
      File outputFile=new File("testANN.txt");
      FileWriter out=new FileWriter(outputFile);
      for (int i=0;i<layers.length;i++)
      {
        System.out.println("Layer "+i+" has "+layers[i]+" elements");
        out.write("\nLayer "+i+" has "+layers[i]+" elements\r");
      }
      System.out.println("Total number of neurons: " + myNetwork.getNeurons().size());
      out.write("\nTotal number of neurons: " + myNetwork.getNeurons().size()+"\r");
      System.out.println("Total number of edges: " + myNetwork.getEdges().size());
      out.write("\nTotal number of edges: " + myNetwork.getEdges().size()+"\r");
      if (mode==BATCHMODE)
      {
        System.out.println("Batchmode selected");
        out.write("\nBatchmode selected \r");
      }
      else
      {
        System.out.println("Stochastic selected");
        out.write("\nStochastic selected\r");
      }
      System.out.println("Learning Rate is "+learningRate);
      out.write("\nLearning Rate is "+learningRate+"\r");
      System.out.println("Momentum is "+momentum);
      out.write("\nMomentum is "+momentum+"\r");

      // training the network....
      myNetwork.iterativeTrain(trainingData, evaluationData,69,out);
      myNetwork.reportAccuracy(trainingData,evaluationData,1,1,out);
      //myNetwork.save("myANN.dat");
      out.close();
    }
  }
}
