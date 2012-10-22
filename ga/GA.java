/*
To do: implement sort procedure
This class implements the operations of the Genetic Algorithm
Author: Alex Ksikes
*/
import java.util.*;

public class GA
{
  // p is the number of hypotheses in population
  int p;
  // r is the fraction of the population to be replaced by Crossover at each step
  double r;
  // m is the rate of mutation
  double m;
  // Holds the current population
  private Hypothesis[] population;
  // Holds the successor population
  private Hypothesis[] successorPopulation;

  // Create initial population here
  public GA(int p, double r, double m)
  {
    this.p=p;
    this.r=r;
    this.m=m;

    createInitialPopulation();
  }

  // Create initial population by randomly generating p hypotheses
  private void createInitialPopulation()
  {
    population=new Hypothesis[p];
    successorPopulation=new Hypothesis[p];
    Hypothesis h;
    for (int i=0;i<p;i++)
    {
      h=new Hypothesis();
      h.setToRandom();
      population[i]=h;
      successorPopulation[i]=new Hypothesis();    // initialize the successor array
    }
  }

  // Compute fitness for the population
  // and rank the population based on their fitness function
  public void computeFitness()
  {
    for (int i=0;i<p;i++)
    {
      population[i].computeFitness();
    }
    HypothesisComparator c=new HypothesisComparator();
    Arrays.sort(population,c);
  }

  // Returns the average fitness of the population
  public double computeAveFitness()
  {
    int sum=0;
    for (int i=0;i<p;i++)
    {
      sum=sum + population[i].getFitness();
    }
    return sum/(p*1.0);
  }

  // Probabilisticaly select the (1-r)*p best hypotheses
  // Assumes the population has been ranked
  public void select()
  {
    Hypothesis h;
    BitSet representation;
    int rank;
    int numSelected=0;
    double Prh;

    // Operate rank selection
    while (numSelected<(1-r)*p)
    {
      rank=(int) (Math.random()*p);
      h=population[rank];
      Prh= (p-rank) / ( p*(p+1.0)/2.0 );
      if (Math.random()<Prh)
      {
        representation=h.getRepresentation();
        successorPopulation[numSelected].setRepresentation(representation);
        numSelected++;
      }
    }
  }

  // Probabilisticaly choose r*p/2 pairs of the best hypotheses
  // and apply crossover on each pair
  // Assumes the population has been ranked
  public void crossover()
  {
    int j=(int) ((1-r)*p);
    Hypothesis parent1=new Hypothesis();
    Hypothesis parent2=new Hypothesis();
    int rankP1, rankP2;   // rank of the parent 1 and 2
    double PrP1, PrP2;    // proba of selection of parent 1 and 2
    int numReproduced=0;

    // Operate rank selection
    while (numReproduced < r*p)
    {
      // choosing the first parent
      rankP1=(int) (Math.random()*p);
      PrP1= (p-rankP1) / ( p*(p+1.0)/2.0 );
      // choosing the second parent
      rankP2=(int) (Math.random()*p);
      PrP2= (p-rankP2) / ( p*(p+1.0)/2.0 );
      if (Math.random()<PrP1 && Math.random()<PrP2)
      {
        // apply crossover between parent 1 and  parent 2
        parent1.setRepresentation(population[rankP1].getRepresentation());
        parent2.setRepresentation(population[rankP2].getRepresentation());
        parent1.crossover(parent2);
        successorPopulation[j].setRepresentation(parent1.getRepresentation());
        numReproduced++;
        // apply crossover between parent 2 and parent 1
        parent1.setRepresentation(population[rankP1].getRepresentation());
        parent2.setRepresentation(population[rankP2].getRepresentation());
        parent2.crossover(parent1);
        successorPopulation[j+1].setRepresentation(parent2.getRepresentation());
        numReproduced++;
        j=j+2;
      }
    }
  }

  // Mutate m*p random members of the successor population
  public void mutate()
  {
    int index;
    for (int i=0;i<m*p;i++)
    {
      index=(int)(Math.random()*p);
      successorPopulation[index].mutate();
    }
  }

  // Returns the array that holds the population
  public Hypothesis[] getPopulation()
  {
    return population;
  }

  // Make the successor population the current population
  public void setNextGeneration()
  {
    for (int i=0;i<p;i++)
      population[i].setRepresentation(successorPopulation[i].getRepresentation());
  }
}
