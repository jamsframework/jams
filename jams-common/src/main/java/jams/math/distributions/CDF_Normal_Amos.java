package jams.math.distributions;

/**
*
*This class contains a Java translation of a FORTRAN
*routine written by Don Amos and S.L. Daniel to calculate the
*normal cumulative distribution function (CDF).
*
*@version .5 --- February 7, 2002
*
*/

public class CDF_Normal_Amos extends Object {

/**
*
*This method calculates the normal cumulative distribution function.
*It is a Java translation of the FORTRAN
*routine fnorm written by Don Amos and S.L. Daniel.  The documentation
*in the FORTRAN code states that
*"significant digits are retained over the full exponent range
*with a nominal accuracy max(unit roundoff,1.0e-13)."  For IEEE 754
*double precision arithmetic, this max value is 1.0e-13.<p>
*Steve Verrill translated the FORTRAN code (dated October, 1974)
*into Java.  This translation was performed on February 7, 2001.
*
*@param   x     If kode = 1, the method returns the value of the normal
*               cumulative distribution function at x.
*               If kode = 2, the method returns 1 minus the value of the normal
*               cumulative distribution function at x.
*@param   kode
*@param   nz[0] Underflow flag.
*               nz[0] = 0, a normal return
*               nz[0] = 1, underflow, fnorm = 0.0 returned 
*
*@version .5 --- February 8, 2002
*
*/


   public static double fnorm(double x, int kode, int nz[]) {


/*

Here is a copy of the documentation in the FORTRAN code:

      FUNCTION FNORM(X,KODE,NZ)                                          
C                                                                        
C     WRITTEN BY D.E. AMOS AND S.L. DANIEL, OCTOBER, 1974                
C                                                                        
C     REFERENCE SC-DR-72-0918                                            
C                                                                        
C     ABSTRACT                                                           
C         COMPUTES THE CUMULATIVE NORMAL DISTRIBUTION F(X) OR
C         ITS COMPLEMENT 1.0-F(X). CHEBYSHEV EXPANSIONS FOR ERF(Z) ON
C         0.LE.Z.LT.2 AND ERFC(Z) ON 2.LE.Z.LE.4 AND Z.GT.4 ARE          
C         USED FOR EVALUATION.  THE RELATIONS                            
C
C          F(X)=0.5*ERFC(Z)    , Z=-X/SQRT(2) , X.LT.-2.0*SQRT(2)
C          F(X)=0.5-0.5*ERF(Z) , Z=-X/SQRT(2) ,-2.0*SQRT(2).LE.X.LT.0.0
C          F(X)=0.5+0.5*ERF(Z) , Z= X/SQRT(2) , 0.0.LE.X.LT.2.0*SQRT(2)
C          F(X)=1.0-0.5*ERFC(Z), Z= X/SQRT(2) , 2.0.LE.Z.LT.6.0
C          F(X)=1.0 ,                           X.GE.6.0*SQRT(2)
C          F(-X)=1.0-F(X) ,
C
C         ARE USED TO COMPLETE THE DEFINITION ON THE REAL LINE SO THAT   
C         SIGNIFICANT DIGITS ARE RETAINED OVER THE FULL EXPONENT RANGE
C         WITH AN NOMINAL ACCURACY MAX(UNIT ROUNDOFF,1.0E-13).
C                                                                        
C     DESCRIPTION OF ARGUMENTS                                           
C                                                                        
C         INPUT                                                          
C           X      - ARGUMENT OF THE DISTRIBUTION                        
C           KODE   - A SELECTION PARAMETER                               
C                    KODE=1 RETURNS =F(X)                           
C                    KODE=2 RETURNS =1.0-F(X)
C                                                                        
C         OUTPUT                                                         
C           FNORM  - F(X) OR 1.0-F(X) DEPENDING ON KODE.
C           NZ     - UNDERFLOW FLAG                                      
C                    NZ=0, A NORMAL RETURN                               
C                    NZ=1, UNDERFLOW, FNORM=0.0 RETURNED                 
C                                                                        
C     ERROR CONDITIONS                                                   
C         IMPROPER INPUT FOR KODE- A FATAL ERROR                         
C         UNDERFLOW - A NON-FATAL ERROR, X IS LESS THAN THE CRITICAL
C         VALUE XLIM.
C                                                                        
C     FNORM USES SUBROUTINES XERROR,I1MACH,R1MACH
C-----------------------------------------------------------------------

*/



      final double rtwo = 1.41421356237310E+00;
      final double trtwo = 2.82842712474619E+00;
      final double frtwo = 5.65685424949238E+00;
      final double srtwo = 8.48528137423857E+00;

      double p,xx;
      double elim,xlim,z,tz,b1,b2,temp,ans,xs;

      int kk,j,i;

      final int n1 = 21;
      final int n2 = 23;
      final int n3 = 17;
      final int m1 = 19;
      final int m2 = 21;
      final int m3 = 15;

      final double a1[] = {2.94268192595158E-01,
      -1.20794002859252E-01,
      -5.38155411612267E-03,
       9.61245872309754E-03,
      -1.56928442055175E-03,
      -3.13379686339925E-04,
       1.34539944432857E-04,
      -2.01886311941572E-06,
      -6.02924420904726E-06,
       7.33514212717164E-07,
       1.68200375041707E-07,
      -4.21496636122487E-08,
      -2.34089537886964E-09,
       1.54397950861409E-09,
      -3.83910453258562E-11,
      -4.18791755643448E-11,
       3.66254323806330E-12,
       8.67662501706706E-13,
      -1.38490737068408E-13,
      -1.30609215123467E-14,
       3.76420840390550E-15};


      final double a2[] = {3.93098827656776E-01,
       0.0,
      -5.72072868438617E-03,
       0.0,
       1.18630151591342E-04,
       0.0,
      -3.91103704629101E-06,
       0.0,
       1.72795234431416E-07,
       0.0,
      -9.42361518118889E-09,
       0.0,
       6.04595735693237E-10,
       0.0,
      -4.42225118426537E-11,
       0.0,
       3.60747153118711E-12,
       0.0,
      -3.22932023145379E-13,
       0.0,
       3.13323522258447E-14,
       0.0,
      -3.26302072101379E-15};


       final double a3[] = {2.66657721330163E-01,
       8.94380310492471E-03,
      -1.90087646908402E-03,
       3.48555717528185E-04,
      -5.81844230476253E-05,
       9.06838380458210E-06,
      -1.33859970500872E-06,
       1.88850668170541E-07,
      -2.56245596590501E-08,
       3.35935312400552E-09,
      -4.27010392442662E-10,
       5.27726756655456E-11,
      -6.35545872359585E-12,
       7.47249710210314E-13,
      -8.59121451944801E-14,
       9.67175305486972E-15,
      -1.06741339515971E-15};


      if (kode < 1 || kode > 2) {

         p = -1.0;

         System.out.print("\nError: In fnorm, the kode value " +
         "was not 1 or 2.\n\n");

         return p;

      }

      if (kode == 1) {

//   Calculate the normal cdf at x

         xx = x;

      } else {

//   Calculate 1 - the normal cdf at x

         xx = -x;

      }

//      kk = Math.min(Math.abs(i1mach[15]),Math.abs(i1mach[16]);
//  For IEEE 754 arithmetic, the double precision smallest exponent
//  is -1022, the double precision largest exponent is 1023.
//  This yields kk = 1022.  zzz = log_10(2) = .3010299957.
//  Presumably we don't need many digits here, but ...

//      elim = 2.303*(kk*zzz - 3.0);

      elim = 2.303*(1022.0*.3010299957 - 3.0);

//  xlim is used to test for underflow

      xlim = -Math.sqrt(2.0*elim);
      nz[0] = 0;

// 150

      if (xx < xlim) {

//   Underflow

         p = 0.0;
         nz[0] = 1;

         return p;

      }

// 102

      if (xx >= srtwo) {

         p = 1.0;

         return p;

      }

// 104

      if (xx > frtwo) {

// 107

         z = frtwo/xx;

         tz = z + z;

         j = n2 - 1;

         b1 = a2[j];

         b2 = 0.0;

         for (i = 1; i <= m2; i++) {

            j--;

            temp = b1;

            b1 = tz*b1 - b2 + a2[j];

            b2 = temp;

         }

         ans = z*b1 - b2 + a2[0];

         p = (Math.exp(-xx*xx*.5)/xx)*ans;

// 110
         p = 1.0 - p;

         return p;

      }

// 106

      if (xx > trtwo) {

// 112

         xs = xx/rtwo;

// 113

         z = xs - 3.0;

         tz = z + z;

         j = n3 - 1;

         b1 = a3[j];

         b2 = 0.0;

         for (i = 1; i <= m3; i++) {

            j--;

            temp = b1;

            b1 = tz*b1 - b2 + a3[j];

            b2 = temp;

         }

         ans = z*b1 - b2 + a3[0];

         p = Math.exp(-xs*xs)*ans/xs;

         if (xx > 0.0) {

            p = 1.0 - p;

         }

         return p;

      }

      if (xx >= -trtwo) {

         z = Math.abs(xx)/rtwo - 1.0;

         tz = z + z;

         j = n1 - 1;

         b1 = a1[j];

         b2 = 0.0;

         for (i = 1; i <= m1; i++) {

            j--;

            temp = b1;

            b1 = tz*b1 - b2 + a1[j];

            b2 = temp;

         }

         p = xx*(z*b1 - b2 + a1[0]) + 0.5;

         return p;

      } 
        

// 108

      if (xx <= -frtwo) {

         xx = -xx;

// 107 (again)

         z = frtwo/xx;

         tz = z + z;

         j = n2 - 1;

         b1 = a2[j];

         b2 = 0.0;

         for (i = 1; i <= m2; i++) {

            j--;

            temp = b1;

            b1 = tz*b1 - b2 + a2[j];

            b2 = temp;

         }

         ans = z*b1 - b2 + a2[0];

         p = (Math.exp(-xx*xx*.5)/xx)*ans;

         return p;

      }

// 109

      xs = -xx/rtwo;

// 113 (again)

      z = xs - 3.0;

      tz = z + z;

      j = n3 - 1;

      b1 = a3[j];

      b2 = 0.0;

      for (i = 1; i <= m3; i++) {

         j--;

         temp = b1;

         b1 = tz*b1 - b2 + a3[j];

         b2 = temp;

      }

      ans = z*b1 - b2 + a3[0];

      p = Math.exp(-xs*xs)*ans/xs;

      if (xx > 0.0) {

         p = 1.0 - p;

      }

      return p;

   }

}
