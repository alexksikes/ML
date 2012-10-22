/**
 * @author Alex Ksikes
 **/

import java.awt.*;
import java.io.*;

/**
 * This class makes a tree out of a balanced parenthesis string.
 * Example of a balanced parenthesis string: ((1)((2)(4))).
 * This class is used to draw a tree out of a cluster representation.
 * Still need improvements (did this very quickly)
**/
public class TreeDrawing extends Canvas
{

  String balancedString;      // the string we wish to draw as a tree.
  int xInit,yInit,yInc;
  Dimension d;                // dimension of the canvas to draw a tree.

  /**
   * Default constructor
  **/
  public TreeDrawing(String balancedString)
  {
    if (!checkBalanced(balancedString))
    {
      System.out.println("String is not balanced!");
    }
    else
    {
      System.out.println("String balanced okay.");
      this.balancedString=new String(balancedString);
      this.d=new Dimension(1000,1000);
      this.setSize(d);
      xInit=800;
      yInit=20;
      yInc=10;
    }
  }

  /**
   * Check if string s is well balanced.
  **/
  public boolean checkBalanced(String s)
  {
    int count=0;
    for (int i=0;i<s.length();i++)
    {
      if(s.charAt(i)=='(')
        count++;
      if(s.charAt(i)==')')
        count--;
    }
    return count==0;
  }

  /**
   * Get the left part of string s.
  **/
  public String getLeft(String s)
  {
    if (s.charAt(1)!='(')
    {
      return s;
    }
    else
    {
      int open=1;
      int index=1;
      char c;
      while(open!=0)
      {
        index++;
        c=s.charAt(index);
        if (c=='(')
          open++;
        if (c==')')
          open--;
      }
      //System.out.println("Left = "+s.substring(1,index+1));
      return s.substring(1,index+1);
    }
  }

  /**
   * Get the right part of string s.
  **/
  public String getRight(String s)
  {
    if (s.charAt(s.length()-2)!=')')
    {
      return s;
    }
    else
    {
      String left=getLeft(s);
      //System.out.println("Right = "+s.substring(left.length()+1,s.length()-1));
      return s.substring(left.length()+1,s.length()-1);
    }
  }

  /**
   * Recursively draw a tree of string s at the coordinates (x,y).
  **/
  public void drawTree(Graphics g,String s,int x,int y)
  {
    g.drawOval(x,y,2,2);

    if (s.charAt(1)!='(')
    {
      //System.out.println("I'm out with length= "+s.length());
      return;
    }
    String left=getLeft(s);
    String right=getRight(s);

    // scales coordinates to fit the tree.
    int xLeft=x-left.length()/25;
    int xRight=x+right.length()/25;
    //int yNew=y+yInc;
    int yLeft=y+(int)Math.log(left.length()/3)+yInc;
    int yRight=y+(int)Math.log(right.length()/3)+yInc;

    System.out.println("Left = "+left);
    System.out.println("Right = "+right);

    g.drawLine(x,y,xLeft,yLeft);
    g.drawLine(x,y,xRight,yRight);

    drawTree(g,left,xLeft,yLeft);
    drawTree(g,right,xRight,yRight);
  }

  public void paint(Graphics g)
  {
    drawTree(g,balancedString,xInit,yInit);
  }



  /**
   * Entry point to this program.
   * Input is a well formed balanced parenthesis string.
  **/
  public static void main(String[] args)
  {
    if (args.length!=1)
    {
      System.out.println("Wrong usage. Type java TreeDrawing [file]");
    }
    else
    {
      String data=new String();
      try
      {
        BufferedReader bf=new BufferedReader(new FileReader(args[0]));
        data=bf.readLine();
      }
      catch(IOException e){}
      String test="(((((5)((1)(2)))((3)(4)))1)(((0)((5)(8)))(((9)(0))(3))))";
      //String test2=((((165)(442))((396)(397)))(244));
      System.out.println(data);

      TreeDrawing tree=new TreeDrawing(data);
      Frame f=new Frame();
      f.setSize(1000,1000);
      f.add(tree);
      f.show();
    }
  }
}