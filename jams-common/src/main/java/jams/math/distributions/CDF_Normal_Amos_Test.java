package jams.math.distributions;


/**
*
*This class tests the CDF_Normal_Amos class.
*
*@author Steve Verrill
*@version .5 --- February 25, 2002
*
*/


public class CDF_Normal_Amos_Test extends Object {

   public static void main (String args[]) {

      double x,ans;

      int another,kode;

      int nz[] = new int[1];

      another = 1;

      kode = 1;

      while (another == 1) { 

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
         x = Console.readDouble("\nWhat is the x value?  ");

         ans = CDF_Normal_Amos.fnorm(x,kode,nz);

         System.out.print("\nThe fnorm value is " + ans + "\n");      

         another = Console.readInt("\nAnother test?" +
         "   0 - no   1 - yes\n\n");

      }

      System.out.print("\n");

   }

}
