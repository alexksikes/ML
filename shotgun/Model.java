/**
 * <p>Title: Shotgun Project</p>
 * <p>Description: </p>
 * <p>Copyright: </p>
 * <p>Company: </p>
 * @author Alex Ksikes
 * @version 2.1
**/

package shotgun;

import java.io.*;

/**
 * A model could either be a set of predictions or an N-class ensemble.
**/
public abstract class Model implements Comparable
{

  protected static final int ACC=0, RMS=1, ROC=2, ALL=3 , BEP=4 , PRE=5 , REC=6,
                             FSC=7, APR=8, LFT=9, CST=10, NRM=11, MXE=12, BSP=13;
  protected static final String[] measure={"ACC","RMS","ROC","ALL","BEP","PRE","REC",
                                           "FSC","APR","LFT","CST","NRM","MXE","BSP"};
  protected static final double eps=1.0e-99;

  protected int numModels;                    // number of models added
  protected double performance;               // performance of the model
  protected String name;                      // name of the model
  protected int id;                           // a unique id given to this model

  protected static int mode;                  // the performance measure we are hillclimbing on
  protected static int bspMode;               // performance measure to do bootstrapping on

  protected static double[] w;                // weights weighted combination of performances
  protected static double weight=1;           // weight for weight decay
  protected static double decayCst;           // decay cst for weight decay

  /**
   * Add a model to this model.
   *
   * @param model The model to be added.
  **/
  public abstract void add(Predictions model);

  /**
   * Substract a model to this model.
   *
   * @param model The model to be substracted.
  **/
  public abstract void sub(Predictions model);

  /**
   * Compute a particular performance of this model.
   *
   * @param i The performance measure.
   * @return The value of the performance.
  **/
  public abstract double compute(int i);

  /**
   * Returns a model that only has a performance to be compared.
   *
   * @param model The model to fetch the performance from.
  **/
  public abstract Model justPerf();

  /**
   * Compute the performance of this model.
  **/
  public void computePerformance()
  {
    this.performance=compute(mode);
  }

  /**
   * Print in a file and in stdout the performances of this model
   *
   * @param out The file writer of the file.
   * @param msg A message to be appended in front of each statement.
  **/
  public void report(FileWriter out, String msg)
  {
    double perf;
    int numPerf=measure.length;
    // bsp performance is expensive.
    if (mode!=BSP)
      numPerf=measure.length-1;
    try
    {
      for (int i=0; i<numPerf; i++)
      {
        perf=compute(i);
        System.out.println(msg+" "+numModels+" "+measure[i]+" "+perf+" "+name);
        out.write(numModels+" "+measure[i]+" "+perf+" "+name+"\n");
      }
      out.flush();
    } catch(IOException e){System.out.println(e);}
  }

  /**
   * Compare the performance of this model with another model.
   *
   * @param o The model to be compared.
   * @return 1 if this model has greater performance, 0 if equal and -1 otherwise.
  **/
  public int compareTo(Object o)
  {
    Model model=(Model) o;
    double delta=this.performance-model.getPerformance();
    int value;
    if (delta>0)
      value=1;
    else if (delta<=eps && delta>=eps)
      value=0;
    else
      value=-1;
    // for RMSE smaller is better
    if (mode==RMS || mode==CST || mode==NRM || mode==MXE)
      return -1*value;
    // if we are doing bootstrapping
    else if (mode==BSP && (bspMode==RMS || bspMode==CST || bspMode==NRM || bspMode==MXE))
      return -1*value;
    else
      return value;
  }

  /**
   * Return the performance of this model.
  **/
  public double getPerformance()
  {
    return performance;
  }

  /**
   * Return the name of this model.
  **/
  public String getName()
  {
    return name;
  }

  /**
   * Return the number of models that have been added.
   *
   * @return The number of models.
  **/
  public int getNumModels()
  {
    return numModels;
  }

  public void setNumModels(int i)
  {
    this.numModels=i;
  }

  /**
   * Decay the weight that scales each added model.
  **/
  public static void decay()
  {
    weight=weight*decayCst;
  }

  /**
   * Set the decay constant.
  **/
  public static void setDecay(double decayCst)
  {
    Model.decayCst=decayCst;
  }

  /**
   * Return the id of these predictions.
  **/
  public int getID()
  {
    return id;
  }

  /**
   * Write this model to a file.
   *
   * @param out The file writer of the file.
  **/
  public abstract void write(FileWriter out);

  public abstract Model copy();

  /**
   * Two models are equal if they have the same id.
   *
   * @return True if this model has the same id as the other model.
  **/
  public boolean equals(Object o)
  {
    Model model=(Model) o;
    return id==model.getID();
  }

  /**
   * Set the performance measure that we hillclimb on.
   *
   * @param mode The performance measure.
  **/
  public static void setMode(int mode)
  {
    Model.mode=mode;
  }

  /**
   * Set the weights for weigthed combination of performances.
   *
   * @param weight The weights in the following order: ACC, RMS, and ROC.
  **/
  public static void setWeight(double[] weight)
  {
    Model.w=new double[3];
    w[0]=weight[0];
    w[1]=weight[1];
    w[2]=weight[2];
  }

}