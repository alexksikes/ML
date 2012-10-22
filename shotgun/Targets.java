/**
 * <p>Title: Shotgun Project</p>
 * <p>Description: </p>
 * <p>Copyright: </p>
 * <p>Company: </p>
 * @author Alex Ksikes
 * @version 2.1
**/

package shotgun;

import java.util.*;
import java.io.*;

/**
 * A class encapsulating the targets of a set of examples.
**/
public class Targets
{

  private int[] trueValue;       // holds the targets of each example
  private int total_true_0;      // total number of true value 0
  private int total_true_1;      // total number of true value 1

  /**
   * Builds a set of targets given a file of label for each example.
   *
   * @param trueValue The file containing the labels.
  **/
  public Targets(File trueValue)
  {
    int i=0;
    try
    {
      BufferedReader bf=new BufferedReader(new FileReader(trueValue));
      String newLine=bf.readLine();
      while (newLine!=null)
      {
        i++;
        newLine=bf.readLine();
      }
      bf=new BufferedReader(new FileReader(trueValue));
      newLine=bf.readLine();
      this.trueValue=new int[i];
      i=0;
      StringTokenizer st;
      while (newLine!=null)
      {
        st=new StringTokenizer(newLine);
        this.trueValue[i]=Integer.parseInt(st.nextToken());
        if (this.trueValue[i]==0)
          this.total_true_0++;
        else
          this.total_true_1++;
        i++;
        newLine=bf.readLine();
      }
    }
    catch (IOException e)
    {
      System.err.println(e.toString());
      e.printStackTrace();
    }
  }

  /**
   * Builds a set of targets given an array of labels for each example.
   *
   * @param targets The array of labels.
  **/
  public Targets(int[] targets)
  {
    this.trueValue=new int[targets.length];
    for (int i=0; i<targets.length; i++)
    {
      this.trueValue[i]=targets[i];
      if (this.trueValue[i]==0)
        this.total_true_0++;
      else
        this.total_true_1++;
    }
  }

  /**
   * Set the true value of a particular example.
   * This method is only used for N-class classification problems.
   *
   * @param i The index of the example.
   * @param label The new label of the example.
  **/
  public void setTarget(int i, int label)
  {
    int oldLabel=trueValue[i];
    this.trueValue[i]=label;
    if (label==0 && oldLabel==1)
    {
      this.total_true_0++;
      this.total_true_1--;
    }
    else if (label==1 && oldLabel==0)
    {
      this.total_true_1++;
      this.total_true_0--;
    }
  }

  /**
   * Returns the set of labels.
  **/
  public int[] getTrueValue()
  {
    return trueValue;
  }

  /**
   * Returns the label of a particular example.
   *
   * @param i The index of the example.
  **/
  public int getTrueValue(int i)
  {
    return trueValue[i];
  }

  /**
   * Returns the number of positive labels.
  **/
  public int getTotal_true_0()
  {
    return total_true_0;
  }

  /**
   * Returns the number of negative labels.
  **/
  public int getTotal_true_1()
  {
    return total_true_1;
  }

  /**
   * Returns the number of examples.
  **/
  public int getSize()
  {
    return trueValue.length;
  }

}