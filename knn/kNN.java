/**
 * @author Alex Ksikes
 **/

import java.util.*;
import ann.*;

/**
 * Implementation of the k-nearest neighbor algorithm
 * In classification assumes 0 or 1 class labels
 * In regression assumes real labels are distributed between 0 or 1
*/
public class kNN
{

  private DataSet dataFile;         // the data set
  private int[] k;                  // holds the number of nearest neighbors
  private int[] kernel;             // holds the kernel widths
  private int trainSetSize;         // size of the training set
  private int testSetSize;          // size of the final test set
  private kNNExample[] trainSet;    // the training set
  private kNNExample[] testSet;     // the final test set
  private int[][] numCorrect;       // keeps track of the number correctly classified instances
  private double[][] squaredError;  // keeps track of the squared error
  private double[][][] predictions;

  /**
   * Initialize the training set and the test set of the kNN algorithm
   * The test set is made from the all the other examples not taken for the training
  **/
  public kNN(DataSet dataFile, int trainSetSize)
  {
    // Fix the data set from which the training set and test are made
    this.dataFile = dataFile;

    // Set up the training set
    this.trainSetSize = trainSetSize;
    trainSet = new kNNExample[trainSetSize];
    for (int i=0; i < trainSetSize; i++)
      trainSet[i] = new kNNExample(dataFile.getExample(i), i);

    // Set up the test set
    this.testSetSize = dataFile.size() - trainSetSize;
    testSet = new kNNExample[testSetSize];
    int index = 0;
    for (int i=trainSetSize; i < dataFile.size(); i++)
    {
      testSet[index] = new kNNExample(dataFile.getExample(i), i);
      index++;
    }

    // Scale feature weights (use the train set for that)
    scaleFeatureWeights(1);
  }

  /**
   * Use LOOCV to select the best values of k and of the kernel width
   * Train using values of k taken from k[] and kernel widths from kernel[]
   * Assumes k[] and kernelWidth[] are sorted
   * Returns the best k and kernel width (first coordinate and second resptively)
  **/
  public int[] train(int k[], int kernel[])
  {
    // Initialize numCorrect and squaredError arrays
    numCorrect = new int[kernel.length][k.length];
    squaredError = new double[kernel.length][k.length];
    predictions = new double[trainSetSize][kernel.length][k.length];

    // Set up the number of nearest neighbors and the kernel widths
    this.k =k;
    this.kernel = kernel;

    // Repeteadly test each query example on the remaining part of the training set
    // and update the correclty classified examples and their squared error
    kNNExample queryExample;                                       // the query example
    kNNExample[] subTrainSet = new kNNExample[trainSetSize - 1];   // the remaing train set
    int index = 0;
    for (int i=0; i < trainSetSize; i++)
    {
      index = 0;
      queryExample = trainSet[i];
      // set up the sub-training set on wich the query example will be tested
      for (int j=0; j < trainSetSize; j++)
      {
        if (!queryExample.equals(trainSet[j]))
        {
          subTrainSet[index] = trainSet[j];
          index++;
        }
      }
      // test the query example on this sub-training set
      predictions[i]=testSingle(queryExample, subTrainSet);
      // print the results with smallest value of kernel width and smallest k
      if (i%25 == 0)
        printSimple(i);
    }

    // Print the final results
    printResults(trainSetSize-1);
    System.out.println("-------------------------------------------------------");
    //printPredictions();

    // Return the best value of k and of the kernel width
    return getBestValues();

  }

  /**
   * Test the test set with values with given values of k[] and kernel[].
  **/
  public void test(int[] bestK, int [] bestKernel)
  {
    // Initialize numCorrect and squaredError arrays
    numCorrect = new int[bestKernel.length][bestK.length];
    squaredError = new double[bestKernel.length][bestK.length];
    predictions = new double[testSetSize][bestKernel.length][bestK.length];

    // Set up the number of nearest neighbors and the kernel widths
    this.k = bestK;
    this.kernel = bestKernel;

    // Evaluate each example from the test set onto the training set
    kNNExample testExample;
    for (int i=0; i < testSetSize; i++)
    {
      testExample = testSet[i];   // see about evaluation set if makes more sense
      predictions[i]=testSingle(testExample, trainSet);
      if (i%25==0)
        printSimple(i);
    }
    printResults(testSetSize-1);
    System.out.println();
    System.out.println("-------------------------------------------------------");
    printPredictions();
  }

  /**
   * Test the single example on a train set
   * Updates the squared error and the number of correctly classified examples
  **/
  private double[][] testSingle (Example testExample, kNNExample[] trainSet)
  {
    // Sort the train set to get the nearest neighbors
    // first set the distance each example is from the test example
    for (int i=0; i < trainSet.length; i++)
      trainSet[i].setRelativeDist(testExample);
    // only sort if the maximum of neighbors < the size of the train set
    if (k[0] < trainSetSize - 1)
    {
      kNNComparator comparator = new kNNComparator();
      Arrays.sort(trainSet, comparator);
    }

    // Update the squared error and the number of correctly classified examples
    // for each kernel width and each number of nearest neighbors considered
    int targetLabel = testExample.getClassLabel();
    kNNExample neighbor;     // neighbor considered
    double neighborWeight;   // its weight
    double neighborValue;    // its value (ie class label)
    double sumWeightedValue; // weighted sum of each neighbor so far
    double sumAllWeight;     // sum of the weights of each neighbor so far
    double probaLabel;
    double[][] predictions=new double[kernel.length][k.length];
    int kIndex;              // indexes k[] array
    // for each kernel width from kernel[]
    for (int kernelIndex=0; kernelIndex < kernel.length; kernelIndex++)
    {
      sumWeightedValue = 0;
      sumAllWeight = 0;
      kIndex = 0;
      // for each neighbor from 0 to max k[]
      for (int neighborNo=0; neighborNo < k[k.length-1]; neighborNo++)
      {
        // compute the probability of each neighbor
        neighbor = trainSet[neighborNo];
        neighbor.setWeight(kernel[kernelIndex]);
        neighborWeight = neighbor.getWeight();
        neighborValue = neighbor.getClassLabel();
        sumWeightedValue = sumWeightedValue + neighborValue * neighborWeight;
        sumAllWeight = sumAllWeight + neighborWeight;
        probaLabel = sumWeightedValue / sumAllWeight;
        // update results after having seen k[kIndex] - 1 neighbors
        if (neighborNo == k[kIndex]-1)
        {
          if (Math.abs(targetLabel - probaLabel) <= 0.5)
          {
            (numCorrect[kernelIndex][kIndex])++;
          }
          squaredError[kernelIndex][kIndex] = squaredError[kernelIndex][kIndex] + Math.pow(targetLabel - probaLabel,2);
          predictions[kernelIndex][kIndex] = probaLabel;
          kIndex++;
        }
      }
    }
    return predictions;
  }

  /**
   * Returns as first coordinate the best kernel.
   * Returns as second coordinate the best number of nearest neighbors.
   * Assumes train or test method has been called before.
  **/
  private int[] getBestValues()
  {
    double lowestError = 0;
    int[] bestValues = new int[2];
    for (int kernelIndex=0; kernelIndex < kernel.length; kernelIndex++)
    {
      for (int kIndex=0; kIndex < k.length; kIndex++)
      {
        if (squaredError[kernelIndex][kIndex] < lowestError)
        {
          lowestError = squaredError[kernelIndex][kIndex];
          bestValues[0] = kernel[kernelIndex];
          bestValues[1] = k[kIndex];
        }
      }
    }
    return bestValues;
  }

  /**
   * Scale the feature weights by 1/(max-min) or 1/var depending on mode
   * using a train set.
  **/
  private void scaleFeatureWeights(int mode)
  {
    double max, min, var, sum, sumSquared;
    int numAttributeVal = dataFile.getAttributeNum();
    double[] attributeVal = new double[trainSetSize];
    double[] featureWeights = new double[numAttributeVal];
    double[] featureWeights1 = new double[numAttributeVal];
    double[] featureWeights2 = new double[numAttributeVal];

    // Find max min and variance of each attribute value
    for (int i=0; i < numAttributeVal; i++)
    {
      sum = 0;
      sumSquared = 0;
      for (int j=0; j < trainSetSize; j++)
      {
        attributeVal[j] = dataFile.getAttribute(j,i);
        sum = sum + attributeVal[j];
        sumSquared = sumSquared + Math.pow(attributeVal[j],2);
      }
      Arrays.sort(attributeVal);
      max = attributeVal[trainSetSize-1];
      min = attributeVal[0];
      var = sumSquared/trainSetSize - Math.pow(sum/trainSetSize,2);
      featureWeights[i] = 1;
      if (max - min == 0)
        featureWeights1[i] = 1;
      else
        featureWeights1[i] = 1/Math.exp(max-min);
      if (var == 0)
        featureWeights2[i] = 1;
      else
        featureWeights2[i] = 1/var;
    }

    // Default feature weights to 1
    if (mode == 0)
      kNNExample.setFeatureWeights(featureWeights);
    // 1/(max-min)
    else if (mode == 1)
      kNNExample.setFeatureWeights(featureWeights1);
    // 1/var
    else if (mode == 2)
      kNNExample.setFeatureWeights(featureWeights2);
  }

  /**
   * Print the RMSE and accuracy after having seen a number of examples.
  **/
  private void printResults(int iteration)
  {
    System.out.println();
    System.out.println("Iteration =  " + iteration);
    double[] accuracy = new double[k.length];
    double[] RMSE = new double[k.length];
    double[] predictions;
    double accuracy1;
    double RMSE1;
    for (int kernelIndex=0; kernelIndex < kernel.length; kernelIndex++)
    {
      System.out.println("Kernel Width =  " + kernel[kernelIndex]);
      System.out.println("------------------------------------------------");
      for (int kIndex=0; kIndex < k.length; kIndex++)
      {
        accuracy[kIndex] = 100.0 * numCorrect[kernelIndex][kIndex]/(iteration+1);
        RMSE[kIndex] = Math.sqrt(squaredError[kernelIndex][kIndex]/(iteration+1));
        System.out.println("k =  " + k[kIndex] + " , Accuracy =  " + accuracy[kIndex] + "% , RMSE =  " + RMSE[kIndex]);

//        predictions=getPredictions(kernelIndex,kIndex);
//        accuracy1=dataFile.returnAccuracy(predictions);
//        RMSE1=dataFile.returnRMSE(predictions);
//        System.out.println("Other k =  " + k[kIndex] + " , Accuracy =  " + accuracy1 + "% , RMSE =  " + RMSE1);
      }
    }
    // used for experiments...
    // kNN experiments
    System.out.print(trainSetSize + " ");

    for (int kIndex=0; kIndex < k.length; kIndex++)
    {
      accuracy[kIndex] = 100.0 * numCorrect[0][kIndex]/(iteration+1);
      RMSE[kIndex] = Math.sqrt(squaredError[0][kIndex]/(iteration+1));
      System.out.print(accuracy[kIndex] + " " + RMSE[kIndex] + " ");
    }

    // locally weighted experiments
//    for (int kernelIndex=0; kernelIndex < kernel.length; kernelIndex++)
//    {
//      accuracy[0] = 100.0 * numCorrect[kernelIndex][0]/(iteration+1);
//      RMSE[0] = Math.sqrt(squaredError[kernelIndex][0]/(iteration+1));
//      System.out.print(accuracy[0] + " " + RMSE[0] + " ");
//    }
  }

  /**
   * Modeified...
   *
   * Return predictions
  **/
  public double[] getPredictions(int kernel,int k)
  {
    double[] pred=new double[testSetSize];
    for (int i=0;i<testSetSize;i++)
      pred[i]=this.predictions[i][kernel][k];

    return pred;
  }

  public void printPredictions()
  {
    System.out.println("*********************Predictions**************************");
    for (int i=0;i<predictions.length;i++)
    {
      for (int j=0;j<k.length;j++)
        System.out.print(predictions[i][0][j]+" ");
      System.out.print("\n");
    }
  }

  /**
   * Print the 1-nearest neighbor results.
   * Used to see how fast the algorithm performs.
   *
   * Need to get predictions on the test set need to rethink all that...
   *
  **/
  private void printSimple(int iteration)
  {
    double accuracy;
    double RMSE;
    int kIndex = 0;
    int kernelIndex = 0;

    System.out.println("Iteration =  " + iteration);
    accuracy = (100.0 * numCorrect[kernelIndex][kIndex])/(iteration+1);
    RMSE = Math.sqrt(squaredError[kernelIndex][kIndex]/(iteration+1));
    System.out.println("k =  " + k[kIndex] + " , Accuracy =  " + accuracy + "% , RMSE =  " + RMSE);

    // not good here does
//    double[] predictions=getPredictions(kernelIndex,kIndex);
//    double accuracy1=dataFile.returnAccuracy(predictions);
//    double RMSE1=dataFile.returnRMSE(predictions);
    //System.out.println("Other k =  " + k[kIndex] + " , Accuracy =  " + accuracy1 + "% , RMSE =  " + RMSE1);
  }

  public void prettyPrintParameters()
  {
  }

  /**
   * Report the baseline accuracy of the training set
  */
  private void reportBaseline()
  {
    int[] count = new int[2];
    int bestCount = 0;
    int label;
    for (int i=0; i < trainSetSize; i++)
    {
      label = trainSet[i].getClassLabel();
      count[label]++;
      if (count[label] > bestCount)
      {
        bestCount = count[label];
      }
    }
    System.out.println("Size of the training Set: " + trainSetSize);
    System.out.println("Baseline Accuracy of Training Set =  " + 100.0 * bestCount/trainSetSize + "%");
  }

  /**
   * Start of this program.
   * Mode is 0 for Unweighted kNN
   *         1 for Weighted kNN
   *         2 for Locally weighted Averaging
   * Param is either the maximum number of neighbors to consider or the maximum kernel width.
  **/
  public static void main(String[] args)
  {
    if (args.length!=4)
    {
      System.out.println("Wrong usage. Type java kNN [data file] [training set size] [mode] [param]");
    }
    else
    {
      // Read in argument
      DataSet dataFile = new DataSet(args[0]);
      int trainSetSize = Integer.parseInt(args[1]);
      int mode = Integer.parseInt(args[2]);
      int lastParam = Integer.parseInt(args[3]);

      // Feature selection
      int  numFeatures=58;
      int[] features=new int[numFeatures];
//      // ...with ANN
//      System.out.println("Proceeding with feature selection...");
//      DataSet featureSet=dataFile.cut(0,4000);
//      features=featureSet.ANNFeatureSelector(numFeatures);
      // ...with DT
//      int[] DTFeatures={1,15,18,24,42,5,6,56,44,29,45,58,31,
//                       35,3,39,16,43,54,4,46,33,40,41,23,11,
//                       30,57,7,20,28,55,49,47,13,52,9,51,19,
//                       12,8,38,22,17,21,10,14,27,2,50,48,53,
//                       32,26,25,34,36,37};
//      for (int i=0;i<features.length;i++)
//        features[i]=DTFeatures[i]-1;
//      dataFile=dataFile.select(features);

      // Init the kNN algorithm by setting up the train and test set
      kNN mykNN = new kNN(dataFile, trainSetSize);

      // Set up k[] and kernel[] based on the mode
      // we consider values of k from 1 to lastParam (note we could have considered smtg else)
      int k[] = new int[lastParam];
      int kernel[] = new int[lastParam+1];
      for (int i=0; i < lastParam; i++)
        k[i] = i+1;
      // we consider values of the kernel width from 1 to lastParam (note we could have considered smtg else)
      for (int i=0; i <= lastParam; i++)
        kernel[i] = i;
      // depending on mode we change some specs
      switch (mode)
      {
        // unweighted kNN
        case 0:
          kernel = new int[1];
          kernel[0] = 0;
          break;
          // weighted kNN
        case 1:
          kernel = new int[1];
          kernel[0] = 1;
          break;
          // locally weighted averaging
        case 2:
          k = new int[1];
          k[0] = trainSetSize-1;
          break;
      }

      // Train with these values of k[] and kernel[]
      mykNN.reportBaseline();
      int[] bestValues = new int[2];
      //bestValues = mykNN.train(k, kernel);
      // Test with the best values of k[] and kernel[]
      int[] bestKernel = new int[1];
      bestKernel[0] = 0;
      int[] bestK = {2,4,6,8,12,17,18,19};
      //System.out.println("Best k = " + bestK[0]);
      mykNN.test(bestK, bestKernel);
    }
  }
}
