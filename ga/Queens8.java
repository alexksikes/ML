/**
* This class is used to launch the Genetic Algorithm
**/

import java.io.*;

public class Queens8
{
  // This is the main function; pass p, r, and m as parameters
  public static void main(String[] args) throws IOException
  {
    // p is the number of hypotheses in population
    int p = Integer.valueOf(args[0]).intValue();
    // r is the fraction of the population to be replaced by Crossover at each step
    double r = Double.valueOf(args[1]).doubleValue();
    // m is the rate of mutation
    double m = Double.valueOf(args[2]).doubleValue();

    // Write into files...
    File outputFile=new File("testGA.txt");
    FileWriter out=new FileWriter(outputFile);

    ////////////////////////
    p=33;
    r=0.6;
    m=0.1;

    // Report the population size, crossover rate and mutation rate
    System.out.println();
    System.out.println("Learning the 8-queens problem with a Genetic Algorithm");
    System.out.println("Population Size: " + p);
    System.out.println("Crossover Rate: " + r);
    System.out.println("Mutation Rate: " + m);
    System.out.println();
    out.write("Parameters are p=" + p + ", " + "r=" + r + " and m=" + m + "\r");

    // The genetic algorithm
    int iterationNum=0;
    Hypothesis bestIndividual;
    int bestFitness;
    double aveFitness;
    // Randomly generate initial population
    GA algorithm = new GA(p, r, m);
    // For each individual compute fitness
    algorithm.computeFitness();
    // Get the best individual
    bestIndividual=(algorithm.getPopulation())[0];
    bestFitness=bestIndividual.getFitness();
    aveFitness=algorithm.computeAveFitness();
    // Iterate till we get the very best individual
    while (bestFitness!=0)
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
      aveFitness=algorithm.computeAveFitness();
      // Report best results to the user at every five iterations
      if (iterationNum%5==0)
      {
        System.out.println("Iteration: " + iterationNum + "  Best Fitness: " + bestFitness
                            + "  Average Fitness " + aveFitness);
        System.out.println("Best solution: " + bestIndividual.toString());
        System.out.println();
        out.write("\n" + iterationNum + "," + bestFitness + "," + aveFitness + "\r");
      }
      iterationNum++;
    }
    System.out.println("Iteration: " + iterationNum + "  Best Fitness: " + bestFitness
                            + "  Average Fitness " + aveFitness);
    System.out.println("Solution is " + bestIndividual.toString());
    out.write("\n" + iterationNum + "," + bestFitness + "," + aveFitness + "\r");
    out.close();
  }
}