package jams.math.distributions;


/**
*
*This class tests the CDF_Normal class.
*
*@author Steve Verrill
*@version .5 --- April 9, 2002
*
*/


public class CDF_Normal_Test extends Object {

   public static void main (String args[]) {

      double x,ans;

      int another = 1;

      while (another == 1) { 

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
         x = Console.readDouble("\nWhat is the x value?  ");

         ans = CDF_Normal.normp(x);

         System.out.print("\nThe normp value is " + ans + "\n");

         ans = CDF_Normal.xnormi(ans);

         System.out.print("\nThe xnormi(normp(x)) value is " + ans + "\n");

         another = Console.readInt("\nAnother test?" +
         "   0 - no   1 - yes\n\n");

      }

      System.out.print("\n");

   }

}
