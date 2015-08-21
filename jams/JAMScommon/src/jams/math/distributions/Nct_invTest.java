package jams.math.distributions;


/**
*
*This class tests the noncentral t cdf inverse in CDF_nct_Amos.
*
*@author Steve Verrill
*@version .5 --- February 26, 2002
*
*/


public class Nct_invTest extends Object {

   public static void main (String args[]) {

      double p,delta,dof,ans;

      int another;


      another = 1;

      while (another == 1) { 

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
         p = Console.readDouble("\nWhat is the p value?  ");
         delta = Console.readDouble("\nWhat is the noncentrality parameter?  ");
         dof = Console.readDouble("\nWhat is the dof?  ");

         ans = CDF_nct_Amos.fnct_inv(p,delta,dof);

         System.out.print("\nThe fnct_inv value is " + ans + "\n");      

         another = Console.readInt("\nAnother test?" +
         "   0 - no   1 - yes\n\n");

      }

      System.out.print("\n");

   }

}
