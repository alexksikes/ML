/*
To do: implement sort procedure
This class implements the operations of the Genetic Algorithm
 * Author: Alex Ksikes
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
  // mode is 0 if the size of the number of hidden neurons is kept fixed or 1 o.w
  int mode;
  // Holds the current population
  private Hypothesis[] population;
  // Holds the successor population
  private Hypothesis[] successorPopulation;

  // Create initial population here
  public GA(int p, double r, double m, int mode, int initNumHiddenNeurons, DataSet trainingSet, DataSet evaluationSet)
  {
    this.p=p;
    this.r=r;
    this.m=m;
    this.mode=mode;

    Hypothesis.trainingSet=trainingSet;
    Hypothesis.evaluationSet=evaluationSet;
    int numInputNeurons=trainingSet.getAttributeNum();
    int numOutputNeurons=1;

    // Set the number of input and output neurons
    Hypothesis.numInputNeurons=numInputNeurons;
    Hypothesis.numOutputNeurons=1;

    // If mode is 0 we initialize the neural network used to evaluate each hypothesis
    if(mode==0)
    {
      int[] numInLayer=new int[3];
      numInLayer[0]=numInputNeurons;
      numInLayer[1]=initNumHiddenNeurons;
      numInLayer[2]=numOutputNeurons;
      fixedANNHypothesis.numHiddenNeurons=initNumHiddenNeurons;
      fixedANNHypothesis.neuralNet=new NeuralNet(3,numInLayer,0,0,1);
    }

    createInitialPopulation(initNumHiddenNeurons);
  }

  // Create initial population by randomly generating p hypotheses
  // If mode is 0 creates p hypotheses of fixed size (maxNumberHiddenNeurons)
  // If mode is 1 creates p hypotheses of size randomly varying between 2 and maxNumHiddenNeurons
  private void createInitialPopulation(int initNumHiddenNeurons)
  {
    population=new Hypothesis[p];
    successorPopulation=new Hypothesis[p];
    Hypothesis h;
    if (mode==0)
    {
      for (int i=0;i<p;i++)
      {
        h=new fixedANNHypothesis();
        h.setToRandom();
        population[i]=h;
        successorPopulation[i]=new fixedANNHypothesis();    // initialize the successor array
      }
    }
    if(mode==1)
    {
      Random value=new Random();
      int randomSize;
      for (int i=0;i<p;i++)
      {
        randomSize=value.nextInt(initNumHiddenNeurons-1)+2;
        h=new varyANNHypothesis(randomSize);
        h.setToRandom();
        population[i]=h;
        successorPopulation[i]=new varyANNHypothesis();    // initialize the successor array
      }
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
    double sum=0;
    for (int i=0;i<p;i++)
    {
      sum=sum + population[i].getFitness();
    }
    return sum/(p*1.0);
  }

  // Returns the average training accuracy
  public double computeAveTrainingAcc()
  {
    double sum=0;
    for (int i=0;i<p;i++)
    {
      sum=sum + population[i].getTrainingAcc();
    }
    return sum/(p*1.0);
  }

  // Returns the average number of hidden neurons
  public double computeAveNumHiddenNeurons()
  {
    double sum=0;
    for (int i=0;i<p;i++)
    {
      sum=sum + population[i].getNumHiddenNeurons();
    }
    return sum/(p*1.0);
  }

  // Probabilisticaly select the (1-r)*p best hypotheses
  // Assumes the population has been ranked
  public void select()
  {
    Hypothesis h;
    double[][][] representation;
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
    Hypothesis parent1;
    Hypothesis parent2;
    if (mode==0)
    {
      parent1=new fixedANNHypothesis();
      parent2=new fixedANNHypothesis();
    }
    else
    {
      parent1=new varyANNHypothesis();
      parent2=new varyANNHypothesis();
    }
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
