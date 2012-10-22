
import java.util.*;

public class HypothesisComparator implements Comparator
{

  public HypothesisComparator() {}

  // Compare two hypotheses based on their fitness value
  public int compare(Object o1,Object o2)
  {
    Hypothesis h1=(Hypothesis) o1;
    Hypothesis h2=(Hypothesis) o2;
    double fitness1=h1.getFitness();
    double fitness2=h2.getFitness();
    if (fitness1>fitness2)
    {
      return 1;
    }
    else if (fitness1==fitness2)
    {
      // we prefer smaller hypothesis
      if(h1 instanceof varyANNHypothesis)
      {
        int sizeh1=((varyANNHypothesis) h1).getNumHiddenNeurons();
        int sizeh2=((varyANNHypothesis) h2).getNumHiddenNeurons();
        if (sizeh1>sizeh2)
        {
          return 1;
        }
      }
      return 0;
    }
    else
    {
      return -1;
    }
  }

  public boolean equals(Object o)
  {
    return(this.equals(o));
  }
}