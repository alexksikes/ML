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
 * A class encapsulating the predictions of a model.
 * @todo
 * - we'd like to make this class independant from the shotgun (ie Model class):
 *  - we would need the compareTo, report and numModels.
 *  - the rest of Model is part of shotgun; performance, mode to hillclimb, etc ...
 *  - it seems that creating a Perf class could solve the issue.
 * -  fix all
 **/
public class Predictions extends Model
{

  private int size;                            // number of examples
  private double[] probaClass;                 // accumulated sum of probabilities of each example
  private Targets targets;                     // the labels of each example

  private int predictedClass;                  // used in N-class classification

  private double a,b,c,d;                      // used to compute the confusion matrix

  private static double threshold=0.5;         // threshold for perf measures
  private static double prcdata=-1;            // threshold as chosen percent of 1 of the data
  private static double costa=0, costb=0.5,    // cost for the confusion matrix
                        costc=0.5, costd=0;
  private static double norm=1;                // exponent of the norm perf measure

  private static int numBsp;                   // num of bootstrap samples
  private static int numPts;                   // num of randomly chosen instances of each bsp
  private static long seed;                    // seed used to generate the bootstrap samples

  /**
   * Builds a new set of predictions given the probabilities and the labels of each example.
   *
   * @param predictions The file containing the predictions of each example.
   * @param targets The labels of each example.
   * @param id A given id for these predictions.
  **/
  public Predictions(File predictions, Targets targets, int id)
  {
    this.numModels=1;
    this.name=predictions.getName();
    this.targets=targets;
    this.id=id;
    // read probability classes from the file
    try
    {
      BufferedReader bf=new BufferedReader(new FileReader(predictions));
      String newLine=bf.readLine();
      int i=0;
      while (newLine!=null)
      {
        i++;
        newLine=bf.readLine();
      }
      this.size=i;
      this.probaClass=new double[size];
      bf=new BufferedReader(new FileReader(predictions));
      newLine=bf.readLine();
      StringTokenizer st;
      i=0;
      while (newLine!=null)
      {
        st=new StringTokenizer(newLine);
        this.probaClass[i]=Double.parseDouble(st.nextToken());
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
   * Builds a new set of predictions given the probabilities and the labels of each example.
   *
   * @param predictions An array containing the predictions of each example.
   * @param targets An array of the labels of each example.
  **/
  public Predictions(double[] predictions, Targets targets)
  {
    this.numModels=1;
    this.size=predictions.length;
    this.probaClass=new double[size];
    for (int i=0; i<size; i++)
      probaClass[i]=predictions[i];
    this.targets=targets;
  }

  /**
   * Create an empty set of predictions.
   *
   * @param targets The targets of the predictions.
  **/
  public Predictions(Targets targets)
  {
    this.targets=targets;
    this.numModels=0;
    this.size=targets.getSize();
    this.probaClass=new double[size];
    for (int i=0; i<size; i++)
      this.probaClass[i]=0;
  }

  /**
   * Create a vacuous set of predictions of a given performance.
   *
   * @param perf The performance of the predictions.
  **/
  public Predictions(double perf)
  {
    this.performance=perf;
  }

  /**
   * Returns a model that only has a performance to be compared.
  **/
  public Model justPerf()
  {
    computePerformance();
    return new Predictions(getPerformance());
  }

  /**
   * Add the predictions of a model to these predictions.
   *
   * @param model The predictions to be added.
  **/
  public void add(Predictions model)
  {
    for (int i=0; i<size; i++)
    {
      probaClass[i]+=model.getProba(i)*weight;
    }
    numModels+=model.getNumModels();
    name=model.getName();
  }

  /**
   * Substract the predictions of a model from these predictions.
   *
   * @param model The predicitions to be substracted.
  **/
  public void sub(Predictions model)
  {
    for (int i=0; i<size; i++)
    {
      probaClass[i]-=model.getProba(i)*weight;
    }
    numModels-=model.getNumModels();
    name=model.getName();
  }

  /**
   * Compute a chosen performance of this set of predictions.
   *
   * @param i The performance measure.
   * @return The value of the performance.
  **/
  public double compute(int perf)
  {
    switch (perf)
    {
      case ACC:
        return computeACC();
      case RMS:
        return computeRMS();
      case ROC:
        return computeROC();
      case ALL:
        return computeALL();
      case BEP:
        return computeBEP();
      case PRE:
        return computePRE();
      case REC:
        return computeREC();
      case FSC:
        return computeFSC();
      case APR:
        return computeAPR();
      case LFT:
        return computeLFT();
      case CST:
        return computeCST();
      case NRM:
        return computeNRM();
      case MXE:
        return computeMXE();
      case BSP:
        return computeBSP();
      default:
        return 0;
    }
  }

  /**
   * Compute the confusion matrix.
  **/
  public void computeCMatrix()
  {
    a = 0;
    b = 0;
    c = 0;
    d = 0;
    if(prcdata<0)
    {
      for (int item=0; item<size; item++)
      {
        if ( getLabel(item) == 1 )
        /* true outcome = 1 */
        {
        if ( probaClass[item]/numModels >= threshold )
          a++;
        else
          b++;
      }
      else
        /* true outcome = 0 */
      {
        if ( probaClass[item]/numModels >= threshold )
          c++;
        else
          d++;
      }
      }
    }
    /* set the threshold to predict x% of the cases to be 1's */
    else
    {
      // Make a copy of the true values and of the predictions to be sorted
      double[] pred=new double[size];
      int[] trueVal=new int[size];
      for (int i=0; i<size; i++)
      {
        pred[i]=probaClass[i]/numModels;
        trueVal[i]=getLabel(i);
      }

      /* sort data by predicted value */
      Predictions.quicksort (0,(size-1),pred,trueVal);
      double pred_thresh=0;
      int data_split=size - (int)(size*prcdata/100);
      if(data_split <= 0)
        pred_thresh = pred[0] - 1.0;
      else if (data_split >= size)
        pred_thresh = pred[size-1]+1;
      else
        pred_thresh = (pred[data_split]+pred[data_split-1])/2.0;

      a=b=c=d=0;
      int item = size-1;
      while (pred[item] > pred_thresh && item >= 0)
      {
        if (trueVal[item] == 1)
          a++;
        else
          c++;
        item--;
      }
      int no_remaining = item - data_split + 1;
      int cnt = 0; int onecnt = 0;
      while ( Math.abs(pred[item] - pred_thresh)<=eps && item >= 0)  // I think this is safe
      {
        cnt++;
        if (trueVal[item] == 1)
          onecnt++;
        item--;
      }
      if (cnt>0)
      {
        a+= 1.0*no_remaining*onecnt/cnt;
        c+= 1.0*no_remaining*(cnt-onecnt)/cnt;
      }
      b=getTotalTrue1()-a;
      d=getTotalTrue0()-c;
    }
  }

  /**
   * Compute the accuracy performance.
  **/
  public double computeACC()
  {
    computeCMatrix();
    return (a+d)/(a+b+c+d);
  }

  /**
   * Compute the cost performance.
  **/
  public double computeCST()
  {
    computeCMatrix();
    return (costa*a+costb*b+costc*c+costd*d);
  }

  /**
   * Compute the RMSE performance.
  **/
  public double computeRMS()
  {
    double sum=0;
    for (int i=0;i<size;i++)
    {
      sum+=(probaClass[i]/numModels-getLabel(i))*
           (probaClass[i]/numModels-getLabel(i));
    }
    return Math.sqrt(sum/size);
  }

  /**
   * Compute the norm performance.
   **/
  public double computeNRM()
  {
    double sum=0;
    for (int i=0;i<size;i++)
    {
      sum+=Math.pow(Math.abs(probaClass[i]/numModels-getLabel(i)),norm);
    }
    return Math.pow(sum/size,1.0/norm);
  }

  /**
   * Compute the mean cross entropy performance.
  **/
  public double computeMXE()
  {
    double loge=Math.log(Math.E);
    double sum=0;
    for (int i=0;i<size;i++)
    {
      sum+= getLabel(i)*Math.log(probaClass[i]/numModels+0.00000001)/loge +
               (1.0-getLabel(i))*Math.log(1.0-probaClass[i]/numModels+0.00000001)/loge; //cross-entropy
    }
    return -1*sum/size;
  }

  /**
   * Compute the ROC performance.
   **/
  public double computeROC()
  {
    // Make a copy of the true values and of the predictions to be sorted
    double[] pred=new double[size];
    int[] trueVal=new int[size];
    for (int i=0; i<size; i++)
    {
      pred[i]=probaClass[i]/numModels;
      trueVal[i]=getLabel(i);
    }

      /* sort data by predicted value */
    Predictions.quicksort (0,(size-1),pred,trueVal);

      /* now let's do the ROC curve and area */
    int tt = 0;
    int tf = getTotalTrue1();
    int ft = 0;
    int ff = getTotalTrue0();

    double sens = ((double) tt) / ((double) (tt+tf));
    double spec = ((double) ff) / ((double) (ft+ff));
    double tpf = sens;
    double fpf = 1.0 - spec;
    double roc_area = 0.0;
    double tpf_prev = tpf;
    double fpf_prev = fpf;

    for (int item=size-1; item>-1; item--)
    {
      tt+= trueVal[item];
      tf-= trueVal[item];
      ft+= 1 - trueVal[item];
      ff-= 1 - trueVal[item];
      sens = ((double) tt) / ((double) (tt+tf));
      spec = ((double) ff) / ((double) (ft+ff));
      tpf  = sens;
      fpf  = 1.0 - spec;
      if ( item > 0 )
      {
        if ( pred[item] != pred[item-1] )
        {
          roc_area+= 0.5*(tpf+tpf_prev)*(fpf-fpf_prev);
          tpf_prev = tpf;
          fpf_prev = fpf;
        }
      }
      if ( item == 0 )
      {
        roc_area+= 0.5*(tpf+tpf_prev)*(fpf-fpf_prev);
      }
    }
    return roc_area;
  }

  /**
   * Compute break even point performance.
  **/
  public double computeBEP()
  {
      /* now do mean average PRECISION and find the BREAK_EVEN point */
      /* note: the approach followed here may not be standard when it comes to ties */
      /* note: the approach followed here computes the area under the precision/recall
       curve, not just the average precision at recall = 0,.1,.2,..,1.0 */

    // Make a copy of the true values and of the predictions to be sorted
    double[] pred=new double[size];
    int[] trueVal=new int[size];
    for (int i=0; i<size; i++)
    {
      pred[i]=probaClass[i]/numModels;
      trueVal[i]=getLabel(i);
    }

      /* sort data by predicted value */
    Predictions.quicksort (0,(size-1),pred,trueVal);

    int tt = 0;
    int tf = getTotalTrue1();
    int ft = 0;
    int ff = getTotalTrue0();

    double precision = -1;
    double recall = 0.0;
    double precision_prev = precision;
    double recall_prev = recall;

    int cnt = 0;
    int onecnt = 0;
    int lasttt=tt;
    int lasttf=tf;
    int lastft=ft;
    int lastff=ff;
    double pr_break_even=0;

    for (int item=size-1; item>-1; item--)
    {
      cnt++;
      if ( trueVal[item] == 1)
        onecnt++;

      tt+= trueVal[item];
      tf-= trueVal[item];
      ft+= 1 - trueVal[item];
      ff-= 1 - trueVal[item];

      if ( ( item > 0 && pred[item] != pred[item-1] ) || item == 0 )
      {
        double prcones = ((double)onecnt/cnt);
        for (int i=1;i<=cnt;i++)
        {
          precision = (lasttt+i*prcones)/(lasttt+lastft+i);
          recall    = (lasttt+i*prcones)/getTotalTrue1();
          if ( precision == recall && pr_break_even == 0 )
          {
            pr_break_even = precision;
            return pr_break_even;
          }
          if ( precision < recall && precision_prev > recall_prev && pr_break_even == 0 )
            if (recall != recall_prev)
            {
              pr_break_even = (precision - (precision_prev - precision)/(recall_prev - recall)*recall)/(1.0 - (precision_prev - precision)/(recall_prev - recall));
              return pr_break_even;
            }
            else
            {
              pr_break_even = recall;
              return pr_break_even;
            }
            precision_prev = precision;
            recall_prev = recall;
        }
        cnt = 0;
        onecnt = 0;
        lasttt=tt;
        lasttf=tf;
        lastft=ft;
        lastff=ff;
      }
    }
    return pr_break_even;
  }

  /**
   * Compute the precision performance.
  **/
  public double computePRE()
  {
    computeCMatrix();
    if (a+c<eps)
      return 0;
    return a/(a + c);
  }

  /**
   * Compute the recall performance.
  **/
  public double computeREC()
  {
    computeCMatrix();
    if (a+b<eps)
      return 0;
    return a/(a + b);
  }

  /**
   * Compute the fscore performance.
  **/
  public double computeFSC()
  {
    double eps=1.0e-99;
    double precision=computePRE();
    double recall=computeREC();
    if (precision + recall <= eps)
      return 0;
    else
      return 2*precision*recall/(precision+recall);
  }

  /**
   * Compute the average precision performance.
  **/
  public double computeAPR()
  {
      /* now do mean average PRECISION and find the BREAK_EVEN point */
      /* note: the approach followed here may not be standard when it comes to ties */
      /* note: the approach followed here computes the area under the precision/recall
       curve, not just the average precision at recall = 0,.1,.2,..,1.0 */

    // Make a copy of the true values and of the predictions to be sorted
    double[] pred=new double[size];
    int[] trueVal=new int[size];
    for (int i=0; i<size; i++)
    {
      pred[i]=probaClass[i]/numModels;
      trueVal[i]=getLabel(i);
    }

      /* sort data by predicted value */
    Predictions.quicksort (0,(size-1),pred,trueVal);

    int tt = 0;
    int tf = getTotalTrue1();
    int ft = 0;
    int ff = getTotalTrue0();

    double apr = 0.0;

    double precision = -1;
    double recall = 0.0;
    double precision_prev = precision;
    double recall_prev = recall;

    int cnt = 0;
    int onecnt = 0;
    int lasttt=tt;
    int lasttf=tf;
    int lastft=ft;
    int lastff=ff;

    for (int item=size-1; item>-1; item--)
    {
      cnt++;
      if ( trueVal[item] == 1)
        onecnt++;

      tt+= trueVal[item];
      tf-= trueVal[item];
      ft+= 1 - trueVal[item];
      ff-= 1 - trueVal[item];

      if ( ( item > 0 && pred[item] != pred[item-1] ) || item == 0 )
      {
        double prcones = ((double)onecnt/cnt);
        for (int i=1;i<=cnt;i++)
        {
          precision = (lasttt+i*prcones)/(lasttt+lastft+i);
          recall    = (lasttt+i*prcones)/getTotalTrue1();
          if ( precision_prev > 0.0)
            apr += 0.5*(precision+precision_prev)*(recall-recall_prev);
          precision_prev = precision;
          recall_prev = recall;
        }
        cnt = 0;
        onecnt = 0;
        lasttt=tt;
        lasttf=tf;
        lastft=ft;
        lastff=ff;
      }
    }
    return apr;
  }

  /**
   * Compute the lift performance.
  **/
  public double computeLFT()
  {
    computeCMatrix();
    if (a+c<eps)
      return 0;
    return (a/getTotalTrue1()) * (size/(a+c));
  }

  /**
   * Computes a weighted combinaison of performances.
  **/
  public double computeALL()
  {
    if (mode==ALL)
      return w[0]*computeACC()+(1-w[1])*computeRMS()+w[2]*computeROC();
    else
    {
      computePerformance();
      return getPerformance();
    }
  }

  /**
   * Computes bootstrapped performance.
  **/
  public double computeBSP()
  {
    double avePerf=0;
    Predictions bootstrap;
    for (int i=0; i<numBsp; i++)
    {
      bootstrap=getBootstrap(seed+i);
      avePerf+=bootstrap.compute(bspMode);
    }
    return avePerf/numBsp;
  }

  /**
   * Returns a bootstrap sample of these predictions.
   *
   * @param seed A seed used to sample each point.
  **/
  public Predictions getBootstrap(long seed)
  {
    double[] preds=new double[numPts];
    int[] targets=new int[numPts];
    Random index=new Random(seed);
    int pt;
    for (int i=0; i<numPts; i++)
    {
      pt=index.nextInt(size);
      preds[i]=probaClass[pt]/numModels;
      targets[i]=getLabel(pt);
    }
    return new Predictions(preds,new Targets(targets));
  }

  /**
   * Updates the seed used for bootrapping.
  **/
  public static void updateSeed()
  {
    Predictions.seed=seed+numBsp;
  }

  /**
   * Check if these predictions have the same number of examples as their targets.
   *
   * @return True if they have the same number of example, false otherwise.
  **/
  public boolean check()
  {
    return (size==targets.getSize());
  }

  /**
   * Return the probability class of an a given example.
   * We need to divide the accumulated sum of probabilities by the number models
   * that has been added.
   *
   * @param i The index of the example.
   * @return The probability the example.
  **/
  public double getProba(int i)
  {
    return probaClass[i]/numModels;
  }

  /**
   * Return the label of a given example.
   *
   * @param i The index of the example.
   * @return The label the example.
  **/
  public int getLabel(int i)
  {
    return targets.getTrueValue(i);
  }

  /**
   * Return the total number of true zeroes.
  **/
  public int getTotalTrue0()
  {
    return targets.getTotal_true_0();
  }

  /**
   * Return the total number of true ones.
  **/
  public int getTotalTrue1()
  {
    return targets.getTotal_true_1();
  }

  /**
   * Return the number of examples.
  **/
  public int getSize()
  {
    return size;
  }

  /**
   * Return the targets of these predictions.
  **/
  public Targets getTargets()
  {
    return targets;
  }

  /**
   * Returns the class that these predictions predict.
   * This method is only used for N-class classification problems.
  **/
  public int getPredictedClass()
  {
    return predictedClass;
  }

  /**
   * Set the probability of an example.
   * This method is only used for N-class classification problems.
   *
   * @param i The index of the example.
   * @param proba The probability of the example.
  **/
  public void setProbaClass(int i, double proba)
  {
    this.probaClass[i]=proba;
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
    this.targets.setTarget(i,label);
  }

  /**
   * Write this set of predictions to a file.
   *
   * @param out The file writer of the file.
  **/
  public void write(FileWriter out)
  {
    String s="";
    for (int i=0; i<size; i++)
    {
      try
      {
        out.write(getProba(i)+"\n");
        //out.flush();
      }
      catch (IOException e) {}
    }
  }

  public Model copy()
  {
    Predictions pred=new Predictions(this.getTargets());
    pred.id=this.id;
    pred.predictedClass=this.predictedClass;
    pred.performance=this.performance;
    pred.numModels=this.numModels;
    for (int i=0; i<size; i++)
      pred.probaClass[i]=this.probaClass[i];
    return pred;
  }

  public static void setThreshold(double threshold)
  {
    Predictions.threshold=threshold;
  }

  public static void setPrcdata(double prcdata)
  {
    Predictions.prcdata=prcdata;
  }

  public static void setCost(double[] cost)
  {
    Predictions.costa=cost[0];
    Predictions.costb=cost[1];
    Predictions.costc=cost[2];
    Predictions.costd=cost[3];
  }

  public static void setNorm(double norm)
  {
    Predictions.norm=norm;
  }

  public static void setBsp(int numBsp, int numPts, long seed)
  {
    Predictions.numBsp=numBsp;
    Predictions.numPts=numPts;
    Predictions.seed=seed;
  }

  public static void setBspMode(int perfMode)
  {
    Predictions.bspMode=perfMode;
  }

  /**
   * Helper method.
  **/
  private static void quicksort(int p, int  r, double[] pred, int[] trueVal)
  {
    int q;
    if (p < r)
    {
      q = partition (p,r,pred,trueVal);
      quicksort (p,q,pred,trueVal);
      quicksort (q+1,r,pred,trueVal);
    }
  }

  /**
   * Helper method used by quicksort.
   **/
  private static int partition(int p, int r, double[] pred, int[] trueVal)
  {
    int i, j;
    double tempf;
    int tempf2;
    double x = pred[p];
    i = p - 1;
    j = r + 1;
    while (true)
    {
      do j--; while (!(pred[j] <= x));
      do i++; while (!(pred[i] >= x));
      if (i < j)
      {
        tempf = pred[i];
        pred[i] = pred[j];
        pred[j] = tempf;
        tempf2 = trueVal[i];
        trueVal[i] = trueVal[j];
        trueVal[j] = tempf2;
      }
      else
        return j;
    }
  }

}
