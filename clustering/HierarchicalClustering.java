/**
 * @author Alex Ksikes
 **/

import java.io.*;
import java.util.*;

/**
 * Implements agglomerative hirarchical clustering.
**/
public class HierarchicalClustering
{

  Vector clusters;      // keeps track of clusters
  int size;             // number of clusters considered at a given momment

  /**
   * Default constructor
  **/
  public HierarchicalClustering()
  {
  }

  /**
   * Initialize the algorithm.
  **/
  public void init(String filename)
  {
    Cluster.setDistances(filename);

//    // for experiments: scale the distances
//    Cluster.scaleDistances();

    this.clusters=new Vector();
    // each point is in its own cluster
    this.size=Cluster.distances.length;
    for (int i=0; i<size; i++)
      clusters.add(new Cluster(i,i));
  }

  /**
   * Returns the cluster at the specified index i.
  **/
  public Cluster getCluster(int i)
  {
    return ((Cluster) clusters.get(i));
  }

  /**
   * Returns the two closest clusters.
   * Assumes at least two clusters are left.
  **/
  public Cluster[] findNearest()
  {
    Cluster[] closest=new Cluster[2];
    Cluster c1=getCluster(0);
    Cluster c2=getCluster(1);
    double minMeanDist=c1.meanDistance(c2);

//    // for experiments
//    double minMeanDist=c1.minimumDistance(c2);

    double current;
    for (int i=0; i<size; i++)
    {
      for (int j=i+1; j<size; j++)
      {
        current=getCluster(i).meanDistance(getCluster(j));
        if (current<minMeanDist)
        {
          c1=getCluster(i);
          c2=getCluster(j);
          minMeanDist=current;
        }
      }
    }
    closest[0]=c1;
    closest[1]=c2;
    return closest;
  }

  /**
   * Merge two clusters.
  **/
  public void merge(Cluster c1, Cluster c2)
  {
    c1.merge(c2);
    clusters.remove(c2);
    size=clusters.size();
  }

  /**
   * Run the agglomerative clustering algorithm.
   * The input specifies the name of the file to write results in.
  **/
  public void run(String exp) throws IOException
  {
    File outputFile=new File(exp);
    FileWriter out=new FileWriter(outputFile);
    Cluster[] closest=new Cluster[2];
    for (int i=size; i>1; i--)
    {
      closest=findNearest();
      reportResults(out,i,closest[0],closest[1]);
      merge(closest[0],closest[1]);
      quickPrint(closest[0],closest[1]);
    }
    reportResults(out,1,closest[0],closest[0]);
    out.close();
  }

  /**
   * Print the merging of clusters together with the representation of the merged
   * cluster.
  **/
  public void quickPrint(Cluster c1, Cluster c2)
  {
    System.out.println("Number of Clusters = "+size+"   Merging... "+c2.getId()+" into "+c1.getId());
    System.out.println("Cluster "+c1.getId()+" is now = "+c1.toString());
  }

  /**
   * Write into a file the mean internal distance of the merged cluster,
   * the mean distance and minimum distance between two clusters and
   * the mean point happiness of all clusters.
  **/
  public void reportResults(FileWriter out, int numClusters, Cluster c1, Cluster c2) throws IOException
  {
    double meanInternal=c1.meanInternalDistance();
    double meanDist=c1.meanDistance(c2);
    double meanHappiness=computeMeanHappiness();
    double minDist=c1.minimumDistance(c2);
    out.write(numClusters + " " + meanInternal + " " + meanDist + " " + minDist
              + " " + meanHappiness + "\r");
  }

  /**
   * Return the mean distance between clusters.
  **/
  public double computeMeanHappiness()
  {
    double sum=0;
    int denom=0;      // may change that for closed form
    for (int i=0;i<size;i++)
    {
      for (int j=i+1;j<size;j++)
      {
        sum=sum + getCluster(i).meanDistance(getCluster(j));
        denom++;
      }
    }
    return sum/denom;
  }

  /**
   * Entry point to this program.
   * Input is a data set containing the distances between points in diagonal form
   * and the name of the file to store the results in.
  **/
  public static void main(String[] args)
  {
    if (args.length!=2)
    {
      System.out.println("Wrong usage. Type java HierarchicalClustering [dataFile] [saveFile]");
    }
    HierarchicalClustering algo=new HierarchicalClustering();
    algo.init(args[0]);
    try
    {
      algo.run(args[1]);
    }
    catch(IOException e){}
  }
}