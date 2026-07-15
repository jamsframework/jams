package jams.math.distributions;


/**
*
*This class tests the CDF_Weibull3 class.
*
*@author Steve Verrill
*@version .5 --- April 9, 2002
*
*/


public class CDF_Weibull3_Test extends Object {

   public static void main (String args[]) {

      double x,lambda,beta,mu,ans;

      int another = 1;

      while (another == 1) { 

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
         x = Console.readDouble("\nWhat is the x value?  ");
         lambda = Console.readDouble("\nWhat is the lambda value?  ");
         beta = Console.readDouble("\nWhat is the beta value?  ");
         mu = Console.readDouble("\nWhat is the mu value?  ");

         ans = CDF_Weibull3.w3cdf(lambda,beta,mu,x);

         System.out.print("\nThe w3cdf value is " + ans + "\n");

         ans = CDF_Weibull3.w3inv(lambda,beta,mu,ans);

         System.out.print(
         "\nThe w3inv(lambda,beta,mu,w3cdf(lambda,beta,mu,x)) value is " + ans + "\n");

         another = Console.readInt("\nAnother test?" +
         "   0 - no   1 - yes\n\n");

      }

      System.out.print("\n");

   }

}
