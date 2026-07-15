package jams.math.distributions;


/**
*
*This class tests the Derf class.
*
*@author Steve Verrill
*@version .5 --- February 22, 2002
*
*/


public class DerfTest extends Object {

   public static void main (String args[]) {

      double x,ans;

      int another;


      another = 1;

      while (another == 1) { 

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
         x = Console.readDouble("\nWhat is the x value?  ");

         ans = Derf.derf(x);

         System.out.print("\nThe derf value is " + ans + "\n");      

         another = Console.readInt("\nAnother test?" +
         "   0 - no   1 - yes\n\n");

      }

      System.out.print("\n");

   }

}
