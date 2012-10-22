/**
 * This class is the entry point to the program
  * Author: Alex Ksikes
 */

import java.io.*;

public class EANN {

  // This is the main function
  public static void main(String[] args) throws IOException
  {
    if (args.length!=3)
    {
      System.out.println("Wrong usage. Type java EANN [population size] [selection size]" +
      "[mutation rate] [mode] [initial number of hidden neurons] [training set]");
    }
    // p is the number of hypotheses in population
    int p=Integer.valueOf(args[0]).intValue();
    // r is the fraction of the population to be replaced by Crossover at each step
    double r=Double.valueOf(args[1]).doubleValue();
    // m is the rate of mutation
    double m=Double.valueOf(args[2]).doubleValue();
    // mode is 0 if the number of hidden neurons is fixed and 1 otherwise
    int mode=Integer.valueOf(args[3]).intValue();
    // initNumHiddenNeurons is the maximum number of hidden neurons allowed for an initial population
    int initNumHiddenNeurons=Integer.valueOf(args[4]).intValue();
    // the training set
    DataSet trainingSet=new DataSet(args[5]);
    //
    DataSet evaluationSet=new DataSet(args[6]);
    // Write into files...
    File outputFile=new File("testEANN.txt");
    FileWriter out=new FileWriter(outputFile);

    //////////////////////// to be erased //////////////////////////////
//    p=50;
//    r=0.6;
//    m=0.15;
//    mode=0;
//    initNumHiddenNeurons=5;
    //////////////////////// to be erased //////////////////////////////

    // Report the population size, crossover rate and mutation rate
    System.out.println();
    System.out.println("Evolutionary Artificial Neural Networks");
    System.out.println("Population Size: " + p);
    System.out.println("Crossover Rate: " + r);
    System.out.println("Mutation Rate: " + m);
    if (mode==0)
    {
      System.out.println("The Neural Nets have a fixed number of hidden neurons.");
      System.out.println("Number of Hidden Neurons: " + initNumHiddenNeurons);
    }
    else
    {
      System.out.println("The Neural Nets have a varying number of hidden neurons.");
      System.out.println("Initial Maximun Number of Hidden Neurons: " + initNumHiddenNeurons);
    }
    System.out.println();
    out.write("Parameters are p=" + p + ", " + "r=" + r + " and m=" + m + "\r");

    // The genetic algorithm
    int iterationNum=0;
    Hypothesis bestIndividual;
    double bestFitness;
    double bestAcc=0;
    double evalAcc=0;
    double aveFitness;
    double aveNumNeurons;
    double aveTrainingAcc;
    double numNeurons=0;
    // Randomly generate initial population
    GA algorithm = new GA(p, r, m, mode, initNumHiddenNeurons, trainingSet, evaluationSet);
    // For each individual compute fitness
    algorithm.computeFitness();
    // Get the best individual
    bestIndividual=(algorithm.getPopulation())[0];
    bestFitness=bestIndividual.getFitness();
    aveFitness=algorithm.computeAveFitness();
    //aveTrainingAcc=algorithm.computeAveTrainingAcc();
    // Iterate till we get the very best individual
    while (iterationNum!=100)                       // change this to number of iteration
    {
      // Select the very best members of the population to survive
      algorithm.select();
      // Make the best members reproduce themselves
      algorithm.crossover();
      // Add some mutations to new population
      algorithm.mutate();
      // The successor population becomes the current population
      algorithm.setNextGeneration();
      // For each individual compute fitness
      algorithm.computeFitness();
      // Get the best individual
      bestIndividual=(algorithm.getPopulation())[0];
      bestFitness=bestIndividual.getFitness();
      bestAcc=bestIndividual.getTrainingAcc();
      aveFitness=algorithm.computeAveFitness();
      //aveTrainingAcc=algorithm.computeAveTrainingAcc();
      evalAcc=bestIndividual.getEvalAcc();
      numNeurons=bestIndividual.getNumHiddenNeurons();
      // Report best results to the user at every five iterations
      if (iterationNum%5==0)
      {
        aveNumNeurons=algorithm.computeAveNumHiddenNeurons();
        System.out.println("Iteration: " + iterationNum);
        System.out.println("Average Number of Hidden Neurons: " + aveNumNeurons);
        System.out.println("Best Fitness: " + bestFitness);
        System.out.println("Average Fitness: " + aveFitness);
        System.out.println("Best solution: " + bestIndividual.toString());
        System.out.println("Training Acc: " + bestAcc);
        //System.out.println("Average Training Acc: " + aveTrainingAcc);
        System.out.println("Evaluation Acc: "+ evalAcc);
        System.out.println();
        out.write("\n" + iterationNum + "," + bestAcc + "," + evalAcc + "," + numNeurons + "\r");
      }
      iterationNum++;
    }
    System.out.println("---------------------------------------------------------");
    aveNumNeurons=algorithm.computeAveNumHiddenNeurons();
    System.out.println("Training Acc: " + bestAcc);
    System.out.println("Evaluation Acc: "+ evalAcc);
    System.out.println("Solution is " + bestIndividual.toString());
    out.write("\n" + iterationNum + "," + bestAcc + "," + evalAcc + "," + numNeurons + "\r");
    out.close();
  }

}