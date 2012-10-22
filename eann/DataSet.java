import java.io.BufferedReader;		
import java.io.Serializable;
import java.io.FileReader;
import java.util.StringTokenizer;	
import java.io.IOException;											     

/**  This class takes in a txt file and creates a dataset that is easily accessible
  *  from the rest of the program
 **/
public class DataSet implements Serializable
{
	/********** Variables *************/
	private int size;
	private int attributeNum;
	Example examples[];

	/********** Methods ***************/

	/** Constructor
	  * @param filename is the name of the file that contains the textual information
	 **/
	public DataSet(String filename) 
	{
	  try
	  {
		 BufferedReader bf = new BufferedReader(new FileReader(filename));
		 
		 // read the first line that contains the size of the dataset and number of attributes in each example
		 String newLine = bf.readLine();

		 StringTokenizer st = new StringTokenizer(newLine);
		 size = Integer.parseInt(st.nextToken());
		 attributeNum = Integer.parseInt(st.nextToken());
		 examples = new Example[size];

	  	 int classLabel;
		 double attributes[] = new double[attributeNum];
		
		 // read in the each of the example
		 for (int k=0;k<size;k++)
		 {
			newLine = bf.readLine();
			st = new StringTokenizer(newLine);
			classLabel = Integer.parseInt(st.nextToken());
			for (int i=0;i<attributeNum;i++)
			{
				attributes[i] = Double.parseDouble(st.nextToken());
			}
			examples[k] = new Example(classLabel,attributes);
		 }
	  }
	  catch (IOException e)
	  {
	  	 System.err.println(e.toString());
		 e.printStackTrace();
	  }
	}

	/** Accessor for the number of examples 
     * @return number of examples
	 **/
	public int size()
   {
	 	return size;	 
	}

	/** Gets a particular example
	  * @param index of the example to retrieve
	 **/
	public Example getExample(int index)
	{
		return examples[index];
	}

	/** Accessor for the classLabel
	  * @return classLabel for example index
	 **/
	public int getClassLabel(int index)
	{
		return examples[index].getClassLabel();
   }

  /** Accessor for the number of attribute
	 *  @return number of attribues in each example
   **/
  public int getAttributeNum()
  {
	 return attributeNum;
  }

  /** Accessor for attributes
	 * @param i contains the numbering for the example in the dataset
	 * @param j contains the numbering for the attribute
	 * @return an attribute of example i attribute j
   **/
  public double getAttribute(int i, int j)
  {
  	  return examples[i].getAttribute(j);
  } 

}