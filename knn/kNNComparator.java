/**
 * @author Alex Ksikes
 **/

import java.util.Comparator;
import ann.*;

public class kNNComparator implements Comparator
{

  public kNNComparator()
  {
  }

  /**
   * Compare two kNNExample based on their realtive distance to a query point
  */
  public int compare(Object o1, Object o2)
  {
    kNNExample example1 = (kNNExample) o1;
    kNNExample example2 = (kNNExample) o2;
    double dist1 = example1.getRelativeDist();
    double dist2 = example2.getRelativeDist();
    if (dist1 < dist2)
      return -1;
    else if (dist1 == dist2)
      return 0;
    else
      return 1;
  }

  public boolean equals(Object o)
  {
    return(this.equals(o));
  }

}
