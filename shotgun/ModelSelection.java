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
 * Main class of the Shotgun project.
 *
 * Any method of this class should operate on Model that abstracts either a set of predictions
 * or an N-class ensemble.
 *
 * @todo
 * - it's not sorting (just doing fr)
 * - better parsing of command line (error messages...)
 * - folder of predictions ann/test? svm/test?\
 * - awkward perf file names
**/
public class ModelSelection
{

  private Vector models;                         // the models used by the shotgun
  private int numModels;                         // number of models used by the shotgun
  private int numTestSets;                       // number of test sets
  private String[] testName;                     // names of each test set
  private Model[] bag;                           // bag for each test set
  private int trainIndex;                        // indicates which bag is used for training
  private File[][] modelFiles;                   // handles to each model file
  private Targets[] targets;                     // the targets in each test folder
  private Model[] bestBag;

  private static FileWriter[] out;               // used to write into files
  private static FileWriter[] outPred;           // used to record the best predictions
  private static boolean writePred=false;
  private static boolean weightDecay=false;
  private static boolean bootstrap=false;

  /**
   * Default constructor.
  **/
  public ModelSelection()
  {
  }

  /**
   * Initialize the algorithm and performs a couple of sanity checks.
   *
   * @param predictionsFolder is a folder where to find all the predictions.
   * @param trainFolder is the name of the folder we are hillclimbing on.
   * @param output is the name of an optional output name.
   *
   * @return True if the initialization is successfull, false otherwise.
  **/
  public boolean init(File predictionsFolder, String trainFolder, String output)
  {
    // Init main parameters.
    File[] folder=predictionsFolder.listFiles();
    this.numTestSets=folder.length;
    this.models=new Vector();
    this.bag=new Predictions[numTestSets];
    this.testName=new String[numTestSets];
    this.modelFiles=new File[numTestSets][];
    this.targets=new Targets[numTestSets];
    this.trainIndex=-1;

    // Read training predictions and all targets from files.
    int[] count=new int[numTestSets];
    String fullName, name, extension;
    Arrays.sort(folder);
    for (int i=0; i<numTestSets; i++)
    {
      this.testName[i]=folder[i].getName();
      this.modelFiles[i]=folder[i].listFiles();

      // specifies the index of the folder we are hillclimbing on.
      if (testName[i].equals(trainFolder))
        this.trainIndex=i;

      // sort the files in each folder
      Arrays.sort(modelFiles[i]);

      // get the targets file located in this folder.
      boolean flag=false;
      for (int j=0; j<modelFiles[i].length; j++)
      {
        fullName=modelFiles[i][j].getName();
        name=fullName.substring(0,fullName.lastIndexOf('.'));
        if (name.equals("targets"))
        {
          targets[i]=new Targets(modelFiles[i][j]);
          flag=true;
        }
      }
      if (!flag)
      {
        helpMsg(1,folder[i].getName());
        return false;
      }

      // load the models that are in the train folder
      for (int j=0; j<modelFiles[i].length; j++)
      {
        fullName=modelFiles[i][j].getName();
        name=fullName.substring(0,fullName.lastIndexOf('.'));
        extension=fullName.substring(fullName.lastIndexOf('.')+1,fullName.length());
        // makes sure we load models with an extension that matches the folder name.
        if (extension.equals(folder[i].getName()) && !name.equals("targets"))
        {
          if (i==trainIndex)
          {
            this.models.add(new Predictions(modelFiles[i][j],targets[i],j));
            count[i]++;
          }
          else
          {
            count[i]++;
          }
        }
      }
    }

    if (writePred)
    {
      this.bestBag=new Predictions[numTestSets];
      for (int i=0; i<numTestSets; i++)
      {
        this.bestBag[i]=new Predictions(targets[i]);
        bestBag[i].setNumModels(1);
        this.bestBag[i].computePerformance();
      }
    }

    // Check if train folder is correct.
    if (trainIndex==-1)
    {
      helpMsg(2,"");
      return false;
    }

    // Check if the number of models in each test folder is the same.
    for (int i=0; i<numTestSets-1; i++)
    {
      if (count[i]!=count[i+1])
      {
        helpMsg(3,"");
        return false;
      }
    }
    this.numModels=count[0];

    // Create empty test bags.
    for (int i=0; i<numTestSets; i++)
    {
      bag[i]=new Predictions(targets[i]);
    }

    // Set up file writers.
    try
    {
      out=new FileWriter[numTestSets];
      outPred=new FileWriter[numTestSets];
      if (!output.equals(""))
      {
        for (int i=0; i<numTestSets; i++)
        {
          out[i]=new FileWriter(new File("perf."+output+"."+testName[i]));
          if (writePred)
            outPred[i]=new FileWriter(new File("preds."+output+"."+testName[i]));
        }
      }
      else
      {
        String fileName;
        int no=1;
        for (int i=0; i<numTestSets; i++)
        {
          if (i==trainIndex)
          {
            fileName="train";
          }
          else
          {
            fileName="test"+no;
            no++;
          }
          out[i]=new FileWriter(new File("perf."+fileName+".1"));
          if (writePred)
            outPred[i]=new FileWriter(new File("preds."+fileName));
        }
      }
    } catch (IOException e) {}

    return true;
  }

  /**
   * Perform backward elimination on models.
  **/
  public void backwardElimination()
  {
    // Sort the models (a simple heuristic to choose between ties)
    sortModels();

    // First we initialize all bags.
    for (int i=0; i<numModels; i++)
      addToBag(getModel(i));
    report();

    // Then we backward select the models.
    Predictions selectedModel, bestModel;
    Model currentBag, bestBag;
    while(numModels>=2)
    {
      // look fot the best subset of models on the training set.
      bestModel=getModel(0);
      bag[trainIndex].sub(bestModel);
      bestBag=bag[trainIndex].justPerf();
      bag[trainIndex].add(bestModel);
      for (int i=1; i<numModels; i++)
      {
        selectedModel=getModel(i);
        bag[trainIndex].sub(selectedModel);
        currentBag=bag[trainIndex].justPerf();
        if (currentBag.compareTo(bestBag)>0)
        {
          bestBag=currentBag;
          bestModel=selectedModel;
        }
        bag[trainIndex].add(selectedModel);
      }
      // remove the "best" model from the bags.
      removeFromBag(bestModel);
      report();
      remove(bestModel);
    }
  }

  /**
   * Perform forward selection on models.
   *
   * @param replacement Set to true if we allow ourself to reselect a model.
   * @param maxIterations The maximum number of iterations.
  **/
  public void forwardSelection(boolean replacement, int maxIterations)
  {
    // Sort the models (a simple heuristic to choose between ties)
    sortModels();

    // Forward select the models.
    Predictions selectedModel, bestModel;
    Model currentBag, bestBag;
    int iteration=0;
    while(numModels>=1 && iteration<maxIterations)
    {
      // look fot the best subset of bags on the training set.
      bestModel=getModel(0);
      bag[trainIndex].add(bestModel);
      bestBag=bag[trainIndex].justPerf();
      bag[trainIndex].sub(bestModel);
      for (int i=1; i<numModels; i++)
      {
        selectedModel=getModel(i);
        bag[trainIndex].add(selectedModel);
        currentBag=bag[trainIndex].justPerf();
        if (currentBag.compareTo(bestBag)>0)
        {
          bestBag=currentBag;
          bestModel=selectedModel;
        }
        bag[trainIndex].sub(selectedModel);
      }
      // add the best model to the bags.
      addToBag(bestModel);
      report();
      if (!replacement)
        remove(bestModel);
      iteration++;
    }
  }

  /**
   * Add models to the bag sorted by their performance.
   **/
  public void sort(int maxIterations)
  {
    // Sort the models by performance.
    sortModels();

    // And simply add them to the bag.
    for (int i=0; i<numModels && i<maxIterations; i++)
    {
      addToBag(getModel(i));
      report();
    }
  }

  /**
   * Find the shotgun set with optimal performance using sort and then apply
   * forward selection with replacement on this set.
   *
   * @param maxIterations The maximum number of iterations.
   **/
  public void sortAndSelect(int maxIterations)
  {
    // Sort the models by performance.
    sortModels();

    // Find the optimal subset of bags using sort.
    Predictions bag=new Predictions(targets[trainIndex]);
    Model bestBag=getModel(0).justPerf();
    int bestIter=0;
    for (int i=1; i<numModels; i++)
    {
      bag.add(getModel(i));
      bag.computePerformance();
      if (bag.compareTo(bestBag) > 0)
      {
        bestBag=bag.justPerf();
        bestIter=i+1;
      }
    }

    // Do sort with optimal number of steps.
    sort(bestIter);

    // Flip to fr mode for the rest of the steps.
    forwardSelection(true,maxIterations);
  }

  /**
   * Add to the bag the models in increasing order of increasing performance
   * on the sorted models.
   * Right now only works for ACC, and ROC (just needs slight changes...)
  **/
  public void greatestIncrease()
  {
    // Sort the models by performance.
    // replace that by sort models...
    Predictions[] sortedModels=new Predictions[numModels];
    for (int i=0; i<numModels; i++)
    {
      sortedModels[i]=getModel(i);
      sortedModels[i].computePerformance();
    }
    Arrays.sort(sortedModels);

    // Now sort the models by greatest increase
    Predictions model=getModel(0);
    Predictions x1=new Predictions(targets[trainIndex]);
    x1.add(sortedModels[numModels-1]);
    x1.computePerformance();
    Predictions x2=new Predictions(targets[trainIndex]);
    x2.add(sortedModels[numModels-1]);
    double[] increase=new double[numModels];
    int[] id=new int[numModels];
    increase[numModels-1]=x1.getPerformance();
    id[numModels-1]=sortedModels[numModels-1].getID();
    for (int i=numModels-2; i>=0; i--)
    {
      x2.add(sortedModels[i]);
      x2.computePerformance();
      increase[i]=x2.getPerformance()-x1.getPerformance();
      id[i]=sortedModels[i].getID();
      x1.add(sortedModels[i]);
      x1.computePerformance();
    }
    // use simple bubble sort.
    double temp1;
    int temp2;
    for (int i=0; i <numModels; i++)
    {
      for (int j=0; j<numModels-1-i; j++)
      {
        if (increase[j] < increase[j+1])
        {
        temp1=increase[j];
        temp2=id[j];
        increase[j]=increase[j+1];
        id[j]=id[j+1];
        increase[j+1]=temp1;
        id[j+1]=temp2;
      }
      }
    }

    // And simply add them to the bag.
    for (int i=0; i<numModels; i++)
    {
      addToBag(getModel(id[i]));
      report();
    }
  }

  /**
   * Sort the models by increasing performance.
  **/
  private void sortModels()
  {
    // Sort the models by performance.
    Predictions[] sortedModels=new Predictions[numModels];
    int k=0;
    for (int i=numModels-1; i>=0; i--)        // make the sort stable
    {
      sortedModels[k]=getModel(i);
      sortedModels[k].computePerformance();
      k++;
    }
    Arrays.sort(sortedModels);

    // Reinitialize the models in decreasing order of performance
    models=new Vector();
    for (int i=numModels-1; i>=0; i--)
    {
      models.add(sortedModels[i]);
    }
  }

  /**
   * Print the performance of each individual model.
  **/
  public void eachModel()
  {
    sortModels();
    int id;
    for (int i=0; i<numModels; i++)
    {
      id=getModel(i).getID();
      for (int j=0; j<numTestSets; j++)
      {
        new Predictions(modelFiles[j][id],targets[j],id).report(out[j],testName[j]);
      }
    }
  }

  /**
   * Report the performance of the shotgun on each test set.
  **/
  private void report()
  {
    for (int i=0; i<numTestSets; i++)
    {
      bag[i].report(out[i],testName[i]);
    }
    if (writePred)
    {
      updateBestModel();
    }
  }

  private void updateBestModel()
  {
    bag[trainIndex].computePerformance();
    if (bag[trainIndex].compareTo(bestBag[trainIndex])>0)
    {
      for (int i=0; i<numTestSets; i++)
        bestBag[i]=bag[i].copy();
    }
  }

  /**
   * Write into files the best set of predictions seen so far.
  **/
  private void writeBestPred()
  {
   for (int i=0; i<numTestSets; i++)
   {
     bestBag[i].write(outPred[i]);
     try {
       outPred[i].flush();
       outPred[i].close();
     }
     catch (IOException e)
     {
       System.out.println("Error with write predictions!");
       System.exit(-1);
     }
   }
  }

  /**
   * Add a model to the bag.
   *
   * @param model The model to be added.
   **/
  private void addToBag(Predictions model)
  {
    int id=model.getID();
    bag[trainIndex].add(model);
    for (int i=0; i<numTestSets; i++)
    {
      if (i!=trainIndex)
        bag[i].add(new Predictions(modelFiles[i][id],targets[i],id));
    }
    if (weightDecay)
      Predictions.decay();
    else if (bootstrap)
      Predictions.updateSeed();
  }

  /**
   * Remove a model from the bag.
   *
   * @param model The model to be removed.
   **/
  private void removeFromBag(Predictions model)
  {
    int id=model.getID();
    bag[trainIndex].sub(model);
    for (int i=0; i<numTestSets; i++)
    {
      if (i!=trainIndex)
        bag[i].sub(new Predictions(modelFiles[i][id],targets[i],id));
    }
    if (weightDecay)
      Predictions.decay();
    else if (bootstrap)
      Predictions.updateSeed();
  }

  /**
   * Remove a model from the training bag.
   * Used for methods with with no replacement of models.
   *
   * @param model The model to be removed.
   **/
  private void remove(Predictions model)
  {
    models.remove(model);
    numModels-=1;
  }

  /**
   * Get the model at specified index.
   *
   * @param index The index of the model.
   **/
  public Predictions getModel(int index)
  {
    return ((Predictions) models.get(index));
  }

  /**
   * Print an error message depending on the chosen mode.
   *
   * @param type The type of messages to be printed.
   * @param msg A string that could be displayed along with the error message.
   **/
  public static void helpMsg(int type, String msg)
  {
    switch (type)
    {
      // Usage message
      case 0:
        System.out.println("=============================================================================");
        System.out.println("Shotgun V2.1: Extreme Ensemble Selection Project");
        System.out.println();
        System.out.println("coded by Alex Ksikes, ak107@cs.cornell.edu");
        System.out.println("=============================================================================");
        System.out.println("Usage: ");
        System.out.println("  java -jar shotgun?.jar [options] pred_folder train_name");
        System.out.println();
        System.out.println("Command options: (default -sfr number of models)");
        System.out.println("  -s           -> sort selection");
        System.out.println("  -g           -> greatest increase");
        System.out.println("  -b           -> backward elimination");
        System.out.println("  -f           -> forward selection");
        System.out.println("  -fr  int     -> forward selection with replacement");
        System.out.println("  -sfr int     -> sort selection and procced with fr");
        System.out.println();
        System.out.println("Performance options: (default -rms)");
        System.out.println("  -acc         -> accuracy");
        System.out.println("  -rms         -> root mean square error");
        System.out.println("  -roc         -> roc area");
        System.out.println("  -all [0..1]  -> weighted combination of all performances");
        System.out.println("  -bep         -> break even point");
        System.out.println("  -pre         -> precision");
        System.out.println("  -rec         -> recall");
        System.out.println("  -fsc         -> f score");
        System.out.println("  -apr         -> average precition");
        System.out.println("  -lft         -> lift");
        System.out.println("  -cst a b c d -> cost");
        System.out.println("  -nrm float   -> norm");
        System.out.println("  -mxe         -> mean cross entropy");
        System.out.println();
        System.out.println("Other options:");
        System.out.println("  -d   float              -> weight decay");
        System.out.println("  -bsp numbsp numpts seed -> bootstrappping");
        System.out.println("  -o   string             -> optional output name");
        System.out.println("  -t   float              -> threshold");
        System.out.println("  -p   [0..100]           -> percent of data to predict one");
        System.out.println("  -wp                     -> write the predictions of the best ensemble");
        System.out.println("  -x                      -> output performance of each model only");
        System.out.println("  -?                      -> this help message");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  pred_folder -> path of the folder containing folders of predictions");
        System.out.println("  train_name  -> name of the folder to train shotgun on");
        System.out.println("=============================================================================");
        break;
      // No target files in folder.
      case 1:
        System.out.println("Error : No target file found in folder " +msg);
        break;
      // Wrong train folder.
      case 2:
        System.out.println("Error : could not find the train folder.");
        break;
      // Different number of models for each test folder.
      case 3:
        System.out.println("Error : predictions folder do not have the same number of models.");
        break;
      // Wrong command argument
      case 4:
        System.out.println("Error : wrong command argument.");
        break;
    }
  }

  /**
   * Checks if a given argument is an integer.
   *
   * @param arg The string to check.
   * @return True if it is an integer, false otherwise.
  **/
  private static boolean isInt(String arg)
  {
    try
    {
      Integer.parseInt(arg);
      return true;
    }
    catch (NumberFormatException e)
    {
      return false;
    }
  }

  /**
   * Entry point to this program.
  **/
  public static void main(String[] args) throws IOException
  {
    if (args.length==0)
    {
      helpMsg(0,"");
      System.exit(0);
    }

    // Set up default options.
    String cmd="-sfr";
    int perfMode=Predictions.RMS;
    int numIterations=-1;
    String output="";

    // Read in options
    String option;
    int argNo=0;
    while (args[argNo].startsWith("-"))
    {
      option=args[argNo++].substring(1);
      // performance measures
      if (option.equals("acc"))
        perfMode=Predictions.ACC;
      else if (option.equals("rms"))
        perfMode=Predictions.RMS;
      else if (option.equals("roc"))
        perfMode=Predictions.ROC;
      else if (option.equals("all"))
      {
        perfMode=Predictions.ALL;
        double[] weight=new double[3];
        weight[0]=Double.parseDouble(args[argNo++]);
        weight[1]=Double.parseDouble(args[argNo++]);
        weight[2]=Double.parseDouble(args[argNo++]);
        Predictions.setWeight(weight);
      }
      else if (option.equals("bep"))
        perfMode=Predictions.BEP;
      else if (option.equals("pre"))
        perfMode=Predictions.PRE;
      else if (option.equals("rec"))
        perfMode=Predictions.REC;
      else if (option.equals("fsc"))
        perfMode=Predictions.FSC;
      else if (option.equals("apr"))
        perfMode=Predictions.APR;
      else if (option.equals("lft"))
        perfMode=Predictions.LFT;
      else if (option.equals("cst"))
      {
        perfMode=Predictions.CST;
        double[] cost=new double[4];
        cost[0]=Double.parseDouble(args[argNo++]);
        cost[1]=Double.parseDouble(args[argNo++]);
        cost[2]=Double.parseDouble(args[argNo++]);
        cost[3]=Double.parseDouble(args[argNo++]);
        Predictions.setCost(cost);
      }
      else if (option.equals("nrm"))
      {
        perfMode=Predictions.NRM;
        Predictions.setNorm(Double.parseDouble(args[argNo++]));
      }
      else if (option.equals("mxe"))
        perfMode=Predictions.MXE;
      // selection methods
      else if (option.equals("s"))
        cmd="-s";
      else if (option.equals("g"))
        cmd="-g";
      else if (option.equals("b"))
        cmd="-b";
      else if (option.equals("f"))
        cmd="-f";
      else if (option.equals("fr"))
      {
        cmd="-fr";
        if (isInt(args[argNo]))
          numIterations=Integer.parseInt(args[argNo++]);
      }
      else if (option.equals("sfr"))
      {
        cmd="-sfr";
        if (isInt(args[argNo]))
          numIterations=Integer.parseInt(args[argNo++]);
      }
      // other options
      else if (option.equals("d"))
      {
        weightDecay=true;
        Predictions.setDecay(Double.parseDouble(args[argNo++]));
      }
      else if (option.equals("o"))
      {
        output=args[argNo++];
      }
      else if (option.equals("wp"))
      {
        writePred=true;
      }
      else if (option.equals("t"))
      {
        Predictions.setThreshold(Double.parseDouble(args[argNo++]));
      }
      else if (option.equals("p"))
      {
        Predictions.setPrcdata(Double.parseDouble(args[argNo++]));
      }
      else if (option.equals("bsp"))
      {
        bootstrap=true;
        int numBsp=Integer.parseInt(args[argNo++]);
        int numPts=Integer.parseInt(args[argNo++]);
        long seed=Long.parseLong(args[argNo++]);
        Predictions.setBsp(numBsp, numPts, seed);
      }
      else if (option.equals("x"))
      {
        cmd="-x";
      }
      else if (option.equals("?"))
      {
        helpMsg(0,"");
        System.exit(0);
      }
    }

    // Read in arguments
    File predictionsFolder=new File(args[argNo++]);
    String trainFolder=args[argNo++];

    // Init and launch the algorithm
    ModelSelection modelSelection=new ModelSelection();
    Model.setMode(perfMode);
    if (bootstrap)
    {
      Predictions.setMode(Predictions.BSP);
      Predictions.setBspMode(perfMode);
    }

    if (modelSelection.init(predictionsFolder, trainFolder, output))
    {
      if (numIterations==-1)
        numIterations=modelSelection.numModels;
      if (cmd.equals("-b"))
        modelSelection.backwardElimination();
      else if (cmd.equals("-f"))
        modelSelection.forwardSelection(false, numIterations);
      else if (cmd.equals("-fr"))
        modelSelection.forwardSelection(true, numIterations);
      else if (cmd.equals("-s"))
        modelSelection.sort(numIterations);
      else if (cmd.equals("-g"))
        modelSelection.greatestIncrease();
      else if (cmd.equals("-sfr"))
        modelSelection.sortAndSelect(numIterations);
      else if (cmd.equals("-x"))
        modelSelection.eachModel();
    }
    else
    {
      System.exit(0);
    }

    if (writePred)
      modelSelection.writeBestPred();

  }
}