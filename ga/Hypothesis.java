/*
Still needs to think about good representation (think mask and stuff...)
This class implements all operations that can be performed on a Hypothesis
Author: Alex Ksikes
*/
import java.util.*;

public class Hypothesis
{

  /**************** Variables ********************/

  final static int BITSETLENGTH=48; // The length of the bitSet is constant

  BitSet representation;  // Bit representation of the hypothesis (binary encoding
                          // of the coordinates of each queen)
  private int[] x;        // x coordinates of queen i
  private int[] y;        // y coordinates of queen i
  int fitness;            // Fitness of the hypothesis

  /**************** Methods **********************/

  // Constructor
  public Hypothesis()
  {
    this.representation=new BitSet(BITSETLENGTH);
    this.x=new int[8];
    this.y=new int[8];
  }

  // Give to this hypothesis a random bitset representation
  public void setToRandom()
  {
    // Generate a random bitSet of this hypothesis
    for (int i=0;i<BITSETLENGTH;i++)
    {
      if (Math.random()>1.0/2)
        representation.set(i);
    }
  }

  // Compute fitness for this Hypothesis
  // Uses its x,y coordinates to count the number of queens attacking each other
  public void computeFitness()
  {
    // Store the integer coordinates of this hypothesis
    bitSetToCoordinates();
    int x0, y0;             // coordinate of queen i
    int sum=0;
    for (int i=0;i<8;i++)
    {
      x0=x[i];
      y0=y[i];
      for (int j=0;j<8;j++)
      {
        // Inconsistent hypotheses (a different queen but with same coordinates!)
        if (j!=i && x[j]==x0 && y[j]==y0)
          sum=sum+1000;
        else
          // Queens in the same column as queen i
          if (j!=i && y[j]==y0)
            sum++;
          // Queens in the same row as queen i
          else if (j!=i && x[j]==x0)
            sum++;
          // Queens in the same diagonals as queen i (could made be more efficient)
          else
            for (int k=0;k<8;k++)
              if (j!=i && x[j]+k==x0 && y[j]+k==y0)
                sum++;
              else if (j!=i && x[j]+k==x0 && y[j]-k==y0)
                sum++;
              else if (j!=i && x[j]-k==x0 && y[j]+k==y0)
                sum++;
              else if (j!=i && x[j]-k==x0 && y[j]-k==y0)
                sum++;
        }
    }
    fitness=sum;
  }

  // Crossover with another Hypothesis
  // Apply a uniform crossover mask
  public void crossover(Hypothesis otherParent)
  {
    // Create the uniform cross over mask
    BitSet mask=new BitSet(BITSETLENGTH);
    for (int i=0;i<BITSETLENGTH/2.0;i++)
    {
      if (Math.random()>1.0/2)
        mask.set(i);
    }
    // Apply the mask
    representation.and(mask);
    otherParent.getRepresentation().andNot(mask);
    representation.or(otherParent.getRepresentation());
  }

  // Mutate this Hypothesis
  // Randomly flip one bit of the bitset this hypothesis
  public void mutate()
  {
    int index=(int) (Math.random()*BITSETLENGTH);
    if (representation.get(index))
      representation.clear(index);
    else
      representation.set(index);
  }

  // Returns the bit representation of this hypothesis
  public BitSet getRepresentation()
  {
    return representation;
  }

  //sets the bit representation of this hypothesis to new representation
  public void setRepresentation(BitSet newRepresentation)
  {
    for (int i=0;i<newRepresentation.size();i++)
    {
      if (newRepresentation.get(i))
        representation.set(i);
      else
        representation.clear(i);
    }
  }

  //returns the fitness of this hypothesis
  public int getFitness()
  {
    return fitness;
  }

  //this should give an easy to read representation of what your hypothesis means
  //(say by using coordinates from (1,1) to (8,8) to indicate positions on the board)
  public String toString()
  {
    String s="";
    for (int i=0;i<8;i++)
    {
      s=s+" ("+x[i]+","+y[i]+")";
    }
    return s;
  }

  // Convert the bit representation of this hypothesis to a coordinate representation
  // Perhaps this is not too elegant (we could use masks but don't see how)
  public void bitSetToCoordinates()
  {
    int bx[]=new int[3];    // binary encoding of coordinate x
    int by[]=new int[3];    // binary encoding of coordinate y
    int k=0;                // kth queen being considered
    int index=2;
    // For every queen
    for (int i=0;i<BITSETLENGTH;i=i+6)
    {
      // x coordinate
      index=2;
      for (int j=i;j<i+3;j++)
      {
        if (representation.get(j))
          bx[index]=1;
        else
          bx[index]=0;
        index--;
      }
      x[k]=bx[2]*4+bx[1]*2+bx[0]*1;   // x coordinate of queen k

      // y coordinate
      index=2;
      for (int j=i+3;j<i+6;j++)
      {
        if (representation.get(j))
          by[index]=1;
        else
          by[index]=0;
        index--;
      }
      y[k]=by[2]*4+by[1]*2+by[0]*1;   // y coordinate of queen k
      k++;
    }
  }

  // Useful for debugging
  public String bitSetToString()
  {
    String s="";
    for (int i=0;i<BITSETLENGTH;i++)
    {
      if (i%3==0)
        s=s+" ";
      if (representation.get(i))
        s=s+1;
      else
        s=s+0;
    }
    return s;
  }

  public int[] getX()
  {
    return x;
  }

  public int[] getY()
  {
    return y;
  }
}
