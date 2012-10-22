/**
 * Implementation of the k-means algorithm
  * Author: Alex Ksikes
**/

import java.util.*;
import java.io.*;

public class kMeans
{

  private int n;                          // number of instances to classify
  private int d;                          // number of coordinates of each point
  private int k;                          // number of clusters
  private PointND[] mu;                   // coordinate of means mu[j] of each cluster j
  private Vector[] w;                     // holds the points classified into each class w[j]
  private PointND[] sigma;                // holds the standard deviation of each class i
  private double[] prior;                 // holds the prior of each class i
  private double logLikelihood;           // holds the log likelihood of each of the k Gaussians
  private double MDL;                     // the minimum description length of the model
  private int numIterations;

  /**
   * Default constructor
  **/
  public kMeans()
  {
  }

  /**
   * Intialize the parameters of the k-means algorithm
   * Randomly assign a point in x to each mean mu[j]
  **/
  private void init(PointND[] x,int k)
  {
    this.n=x.length;
    this.d=x[0].getDimension();
    this.k=k;
    this.mu=new PointND[k];
    this.w=new Vector[k];
    this.numIterations=0;
    this.sigma=new PointND[k];
    this.prior=new double[k];

    // randomly assign a point in x to each mean mu[j]
    PointND randomPoint;
    for (int j=0;j<k;j++)
    {
      randomPoint=x[(int)(Math.random()*(n-1))];
      mu[j]=new PointND(randomPoint);
      // each prior and standard deviation are set to zero
      sigma[j]=new PointND(d);
      prior[j]=0;
    }
  }

  /**
   * Runs the k-means algorithm with k clusters on the set of instances x
   * Then find the quality of the model
  **/
  public void run(PointND[] x,int k,double epsilon)
  {
    double maxDeltaMeans=epsilon+1;
    PointND[] oldMeans=new PointND[k];
    // initialize n,k,mu[j]
    init(x,k);
    // iterate until there is no change in mu[j]
    while (maxDeltaMeans > epsilon)
    {
      // remember old values of the each mean
      for (int j=0;j<k;j++)
      {
        oldMeans[j]=new PointND(mu[j]);
      }
      // classify each instance x[i] to its nearest class
      // first we need to clear the class array since we are reclassifying
      for (int j=0;j<k;j++)
      {
        w[j]=new Vector();        // could use clear but then have to init...
      }

      for (int i=0;i<n;i++)
      {
        classify(x[i]);
      }
      // recompute each mean
      computeMeans();
      // compute the largest change in mu[j]
      maxDeltaMeans=maxDeltaMeans(oldMeans);
      numIterations++;
    }
    // now we find the quality of the model
    modelQuality(x);
  }

  /**
   * Find the quality of the model
  **/
  private void modelQuality(PointND[] x)
  {
    // compute the standard deviation of each cluster
    computeDeviation();
    // compute the prior of each cluster
    computePriors();
    // compute the log likelihood of each cluster
    computeLogLikelihood(x);
    // find the minimum description length of the model
    computeMDL();
  }


  /**
   * Classifies the point x to the nearest class
  **/
  private void classify(PointND x)
  {
    double dist=0;
    double smallestDist;
    int nearestClass;

    // compute the distance x is from mean mu[0]
    smallestDist=x.dist(mu[0]);
    nearestClass=0;

    // compute the distance x is from the other classes
    for(int j=1;j<k;j++)
    {
      dist=x.dist(mu[j]);
      if (dist<smallestDist)
      {
        smallestDist=dist;
        nearestClass=j;
      }
    }
    // classify x into class its nearest class
    w[nearestClass].add(x);
  }

  /**
   * Recompute mu[j] as the average of all points classified to the class w[j]
  **/
  private void computeMeans()
  {
    int numInstances;               // number of instances in each class w[j]
    PointND instance;

    // init the means to zero
    for (int j=0;j<k;j++)
      mu[j].setToOrigin();

    // recompute the means of each cluster
    for (int j=0;j<k;j++)
    {
      numInstances=w[j].size();
      for (int i=0;i<numInstances;i++)
      {
        instance=(PointND) (w[j].get(i));
        mu[j].add(instance);
      }
      mu[j].multiply(1.0/numInstances);
    }
  }

  /**
   * Compute the maximum change over each mean mu[j]
  **/
  private double maxDeltaMeans(PointND[] oldMeans)
  {
    double delta;
    oldMeans[0].subtract(mu[0]);
    double maxDelta=oldMeans[0].max();
    for (int j=1;j<k;j++)
    {
      oldMeans[j].subtract(mu[j]);
      delta=oldMeans[j].max();
      if (delta > maxDelta)
        maxDelta=delta;
    }
    return maxDelta;
  }

  /**
   * Report the results
   * Assumes the algorithm was run
  **/
  public void printResults()
  {
    System.out.println("********************************************");
    System.out.println("Trying " + k + " clusters...");
    System.out.println("Converged after " + numIterations + " iterations");
    for (int j=0;j<k;j++)
    {
      System.out.println();
      System.out.println("Gaussian no. " + (j+1));
      System.out.println("---------------");
      System.out.println("mean " + mu[j]);
      System.out.println("sigma " + sigma[j]);
      System.out.println("prior " + prior[j]);
    }
    System.out.println();
    System.out.println("Model quality:");
    System.out.println("Log-Likelihood " + logLikelihood);
    System.out.println("MdL " + MDL);
  }

  /**
   * Write into a file the k Gaussians (one for each column)
   * Only works for 2 dimensional points
  **/
  public void writeFile(FileWriter out) throws IOException
  {
    //save the MDL of this model
    out.write(MDL + "\r");
    for (int j=0;j<k;j++)
    {
      out.write("Gaussian" + (j+1) + " ");
    }
    out.write("\r");
    // save the means of each Gaussian
    for (int j=0;j<k;j++)
    {
      out.write(mu[j] + " ");
    }
    out.write("\r");
    // save the points in each Gaussian for each column
    int numInstances=0;
    for (int i=0;i<n;i++)
    {
      for (int j=0;j<k;j++)
      {
        numInstances=w[j].size();
        if (i<numInstances)
          out.write(w[j].get(i) + " ");
        else
          out.write("" + " " + "" + " ");
      }
      out.write("\r");
    }
  }

  /**
   * Compute the standard deviation of the k Gaussians
  **/
  private void computeDeviation()
  {
    int numInstances;               // number of instances in each class w[j]
    PointND instance;
    PointND temp;

    // set the standard deviation to zero
    for (int j=0;j<k;j++)
      sigma[j].setToOrigin();

    // for each cluster j...
    for (int j=0;j<k;j++)
    {
      numInstances=w[j].size();
      for (int i=0;i<numInstances;i++)
      {
        instance=(PointND) (w[j].get(i));
        temp=new PointND(instance);
        temp.subtract(mu[j]);
        temp.pow(2.0);                        // (x[i]-mu[j])^2
        temp.multiply(1.0/numInstances);      // multiply by proba of having x[i] in cluster j
        sigma[j].add(temp);                   // sum i (x[i]-mu[j])^2 * p(x[i])
      }
      sigma[j].pow(1.0/2);                    // because we want the standard deviation
    }
  }

  /**
   * Compute the priors of the k Gaussians
  **/
  private void computePriors()
  {
    double numInstances;               // number of instances in each class w[j]
    for (int j=0;j<k;j++)
    {
      numInstances=w[j].size()*(1.0);
      prior[j]=numInstances/n;
    }
  }

  /**
   * Assume the standard deviations and priors of each cluster have been computed
  **/
  private void computeLogLikelihood(PointND[] x)
  {
    double temp1=0;
    double temp2=0;
    PointND variance;
    double ln2=Math.log(2);
    // for each instance x
    for (int i=0;i<n;i++)
    {
      // for each cluster j
      temp1=0;
      for (int j=0;j<k;j++)
      {
        temp1=temp1 + ( x[i].normal(mu[j],sigma[j]) *  prior[j] );
      }
      temp2=temp2 + Math.log(temp1)/ln2;
    }
    logLikelihood=temp2;
  }

  /**
   * Assume the log likelihood and priors have been computed
  **/
  private void computeMDL()
  {
    double temp=0;
    double numInstances;
    double ln2=Math.log(2);
    for (int j=0;j<k;j++)
    {
      numInstances=w[j].size();
      for (int i=0;i<d;i++)
      {
        temp=temp - Math.log( sigma[j].getCoordinate(i)/Math.sqrt(numInstances) )/ln2;
      }
    }
    MDL=temp - logLikelihood;
  }

  public double getMDL()
  {
    return MDL;
  }

  /**
   * Takes the data filename of instances to classify into a number of cluster k
   * Runs the k-means algorithm with 1 to maxk clusters
  **/
  public static void main(String[] args) throws IOException
  {
    if (args.length!=2)
    {
      System.out.println("Wrong usage. Type java kMeans [data file] [maximum number of clusters]");
      System.out.println("Make sure the data file contains the number of instances and the number of attributes");
    }
    else
    {
      // read in the arguments
      // the data file must have as its first line the number of instances and for
      // each instance the number of attributes (see data.txt)
      DataSet dataFile = new DataSet(args[0]);
      int maxk = Integer.parseInt(args[1]);

      // make the instance array
      int numInstances=dataFile.size();
      int d=dataFile.getAttributeNum();
      PointND[] x=new PointND[numInstances];
      Example instance;
      for (int i=0;i<numInstances;i++)
      {
        instance=dataFile.getExample(i);
        x[i]=new PointND(d);
        for (int k=0;k<d;k++)
        {
          x[i].setCoordinate(k,instance.getAttribute(k));
        }
      }

      // used to write into files
      File outputFile=new File("testClustering.txt");
      FileWriter out=new FileWriter(outputFile);

      // choose threshold for when to stop
      double epsilon=0.01;                    // make it data driven (right now absolute..)

      // run the k-means algorithm on this datafile with a max number of clusters as maxk
      kMeans algorithm=new kMeans();
      // Try with k clusters....till maxk clusters
      int bestModel=1;
      double bestMDL=1000000000;              // change this
      for (int k=1;k<=maxk;k++)
      {
        algorithm.run(x,k,epsilon);
        algorithm.printResults();
        if (algorithm.getMDL() < bestMDL)
        {
          bestModel=k;
          bestMDL=algorithm.getMDL();
        }
      }
      // Report the best model
      System.out.println("********************************************");
      System.out.println("The most likely model is " + bestModel + " Gaussians");

      // write into file testClustering.txt the most likely model
      algorithm.run(x,bestModel,epsilon);
      algorithm.writeFile(out);
      out.close();
    }
  }

}