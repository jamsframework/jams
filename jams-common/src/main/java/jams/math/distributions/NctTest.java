package jams.math.distributions;


/**
*
*This class tests the noncentral t cdf in CDF_nct_Amos.
*
*@author Steve Verrill
*@version .5 --- February 26, 2002
*
*/


public class NctTest extends Object {

   public static void main (String args[]) {

      double x,delta,dof,ans;

      int another;


      another = 1;

      while (another == 1) { 

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
         x = Console.readDouble("\nWhat is the x value?  ");
         delta = Console.readDouble("\nWhat is the noncentrality parameter?  ");
         dof = Console.readDouble("\nWhat is the dof?  ");

         ans = CDF_nct_Amos.fnct(x,delta,dof);

         System.out.print("\nThe fnct value is " + ans + "\n");      

         another = Console.readInt("\nAnother test?" +
         "   0 - no   1 - yes\n\n");

      }

      System.out.print("\n");

   }

}
