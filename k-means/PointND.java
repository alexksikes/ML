/**
 * This class describes a n dimensional point
  * Author: Alex Ksikes
**/
public class PointND
{

  private int dimension;                // number of coordinates of a point
  private double[] coordinates;         // the coordinates of a point

  /**
   * Create a point centered at the origin of the specific dimension
  **/
  public PointND(int dimension)
  {
    this.dimension=dimension;
    coordinates=new double[dimension];
  }

  /**
   * Create a new point identical to point p
  **/
  public PointND(PointND p)
  {
    this.dimension=p.getDimension();
    this.coordinates=new double[dimension];
    for (int i=0;i<dimension;i++)
      this.coordinates[i]=p.getCoordinate(i);
  }

  /**
   * Create a new point identical to point p
  **/
  public void setToOrigin()
  {
    for (int i=0;i<dimension;i++)
      coordinates[i]=0;
  }

  /**
   * Return the euclidian norm of this point
  **/
  public double norm()
  {
    double sum=0;
    for (int i=0;i<dimension;i++)
      sum=sum + Math.pow(coordinates[i],2);
    return Math.sqrt(sum);
  }

  /**
   * Add point p to this point
  **/
  public void add(PointND p)
  {
    for (int i=0;i<dimension;i++)
      coordinates[i]=coordinates[i] + p.getCoordinate(i);
  }

  /**
   * Subtract point p to this point
  **/
  public void subtract(PointND p)
  {
    for (int i=0;i<dimension;i++)
      coordinates[i]=coordinates[i] - p.getCoordinate(i);
  }

  /**
   * Multiply this point by a scalar
  **/
  public void multiply(double scalar)
  {
    for (int i=0;i<dimension;i++)
      coordinates[i]=scalar * coordinates[i];
  }

  /**
   * Exponentiate this point by exp
  **/
  public void pow(double exp)
  {
    for (int i=0;i<dimension;i++)
      coordinates[i]=Math.pow(coordinates[i],exp);
  }

  /**
   * Compute the euclidian distance of this point to point p2
  **/
  public double dist(PointND p2)
  {
    PointND p1=new PointND(this);
    (p1.subtract(p2));
    return p1.norm();
  }

  /**
   * Return the coordinate of maximum value of this point
  **/
  public double max()
  {
    double value;
    double max=coordinates[0];
    for (int i=1;i<dimension;i++)
    {
      value=coordinates[i];
      if (value > max)
        max=value;
    }
    return max;
  }

 /**
  * Return the probability of this point given it is normally distributed with a
  * a diagonal covariance matrix of coefficients sigma
 **/
 public double normal(PointND mean,PointND sigma)
  {
    double mahalanobis;
    double productSigma=1;
    PointND temp=new PointND(this);
    temp.subtract(mean);
    // compute the product of the deviations and the mahalanobis distance
    for (int i=0;i<dimension;i++)
    {
      productSigma=sigma.getCoordinate(i) * productSigma;
      temp.setCoordinate( i, temp.getCoordinate(i)/sigma.getCoordinate(i) );
    }
    mahalanobis=Math.pow(temp.norm(),2);
    return ( 1.0/( Math.pow((2*Math.PI),dimension/2.0) * productSigma )
            * Math.exp(-1.0/2 * mahalanobis) );
  }

  /**
   * Return coordinate i of this point
  **/
  public double getCoordinate(int i)
  {
    return coordinates[i];
  }

  /**
   * Set coordinate[i] to the specified value
  **/
  public void setCoordinate(int i,double value)
  {
    coordinates[i]=value;
  }

  /**
   * Return the number of coordinates of this point
  **/
  public int getDimension()
  {
    return dimension;
  }

  /**
   * Return the coordinate array of this point
  **/
  public double[] getCoordinates()
  {
    return coordinates;
  }

  /**
   * Return a string representation of this point
  **/
  public String toString()
  {
    String s="" + coordinates[0];
    for (int i=1;i<dimension;i++)
      s=s + " " + coordinates[i];
    return s;
  }



}