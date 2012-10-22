
import java.util.*;

public class HypothesisComparator implements Comparator
{

  public HypothesisComparator() {}

  // Compare two hypotheses based on their fitness value
  public int compare(Object o1,Object o2)
  {
    Hypothesis h1=(Hypothesis) o1;
    Hypothesis h2=(Hypothesis) o2;
    int fitness1=h1.getFitness();
    int fitness2=h2.getFitness();
    if (fitness1>fitness2)
      return 1;
    else if (fitness1==fitness2)
      return 0;
    else
      return -1;
  }

  public boolean equals(Object o)
  {
    return(this.equals(o));
  }
}