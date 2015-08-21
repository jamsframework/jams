package jams.math.distributions;


/**
*
*This class contains a Java translation of a public domain FORTRAN
*routine, dgaus8, written by R. E. Jones that numerically integrates
*real functions of one variable over finite intervals.  The
*translation was performed by Steve Verrill in February of 2002.
*
*@version .5 --- February 8, 2002
*
*/


public class Gaus8 extends Object {

   static final double x1 = 1.83434642495649805E-01;     
   static final double x2 = 5.25532409916328986E-01;
   static final double x3 = 7.96666477413626740E-01;     
   static final double x4 = 9.60289856497536232E-01;

   static final double w1 = 3.62683783378361983E-01;     
   static final double w2 = 3.13706645877887287E-01;
   static final double w3 = 2.22381034453374471E-01;     
   static final double w4 = 1.01228536290376259E-01;

   static final double sq2 = 1.41421356E0;

   static final int nlmn = 1;

   static final int kmx = 5000;

   static final int kml = 6;


/**
*
*This method integrates real functions of one variable over finite
*intervals using an adaptive 8-point Legendre-Gauss algorithm.
*Gaus8 is intended primarily for high accuracy integration
*or integration of smooth functions.<p>
*This method is a Java translation of the FORTRAN
*routine gaus8 written by R. E. Jones.  The version translated
*is part of the SLATEC library of numerical routines, and can be
*downloaded at www.netlib.org.<p>
*The maximum number of significant digits obtainable in the integral
*is 16. (This is based on the translator's reading of the dgaus8.f
*documentation and the translator's understanding of Java numerics.
*Since the translator is a mathematical statistician rather than
*a numerical analyst, the 16 figure might not be quite correct.)<p>
*The translation was performed by Steve Verrill during 
*February of 2002.<p>
*
*@param   integclass   A class that defines a method, f_to_integrate,
*                      that returns a value that is to be integrated.
*                      The class must implement the Gaus8_fcn
*                      interface (see the definition in 
*                      Gaus8_fcn.java).
*                      See Gaus8Test.java for an example of such
*                      a class.  f_to_integrate must have one double
*                      valued argument.
*@param   a            The lower limit of the integral.
*@param   b            The upper limit of the integral (may be less
*                      than a).
*@param   err[0]       On input, err[0] is a requested pseudorelative
*                      error tolerance.  Normally pick a value
*                      of abs(err[0]) so that dtol < abs(err[0]) <= .001
*                      where dtol is the double precision unit
*                      roundoff (2.22e-16 for IEEE 754 numbers
*                      according to the SLATEC documentation).  The
*                      answer returned by gaus8 will normally have
*                      no more error than Math.abs(err[0]) times the
*                      integral of the absolute value of
*                      f_to_integrate.  Usually, smaller values for
*                      err[0] yield more accuracy and require more
*                      function evaluations.
*                      A negative value for err[0] causes an estimate
*                      of the absolute error in the integral to be
*                      returned in err[0].
*
*@param   ierr[0]
*<p>
*                      -1 -- a and b are too nearly equal to allow
*                            normal integration.  ans (the value
*                            obtained from the numerical integration)
*                            is set to 0.
*<p>
*                       1 -- ans most likely meets the requested
*                            error tolerance, or a = b
*<p>
*                       2 -- ans probably does not meet the
*                            requested error tolerance
*
*/

   public static double gaus8(Gaus8_fcn integclass, double a, 
                              double b, double err[],
                              int ierr[]) {


/*

Here is a copy of the dgaus8 FORTRAN documentation:

      SUBROUTINE DGAUS8 (FUN, A, B, ERR, ANS, IERR)
C***BEGIN PROLOGUE  DGAUS8
C***PURPOSE  Integrate a real function of one variable over a finite
C            interval using an adaptive 8-point Legendre-Gauss
C            algorithm.  Intended primarily for high accuracy
C            integration or integration of smooth functions.
C***LIBRARY   SLATEC
C***CATEGORY  H2A1A1
C***TYPE      DOUBLE PRECISION (GAUS8-S, DGAUS8-D)
C***KEYWORDS  ADAPTIVE QUADRATURE, AUTOMATIC INTEGRATOR,
C             GAUSS QUADRATURE, NUMERICAL INTEGRATION
C***AUTHOR  Jones, R. E., (SNLA)
C***DESCRIPTION
C
C     Abstract  *** a DOUBLE PRECISION routine ***
C        DGAUS8 integrates real functions of one variable over finite
C        intervals using an adaptive 8-point Legendre-Gauss algorithm.
C        DGAUS8 is intended primarily for high accuracy integration
C        or integration of smooth functions.
C
C        The maximum number of significant digits obtainable in ANS
C        is the smaller of 18 and the number of digits carried in
C        double precision arithmetic.
C
C     Description of Arguments
C
C        Input--* FUN, A, B, ERR are DOUBLE PRECISION *
C        FUN - name of external function to be integrated.  This name
C              must be in an EXTERNAL statement in the calling program.
C              FUN must be a DOUBLE PRECISION function of one DOUBLE
C              PRECISION argument.  The value of the argument to FUN
C              is the variable of integration which ranges from A to B.
C        A   - lower limit of integration
C        B   - upper limit of integration (may be less than A)
C        ERR - is a requested pseudorelative error tolerance.  Normally
C              pick a value of ABS(ERR) so that DTOL .LT. ABS(ERR) .LE.
C              1.0D-3 where DTOL is the larger of 1.0D-18 and the
C              double precision unit roundoff D1MACH(4).  ANS will
C              normally have no more error than ABS(ERR) times the
C              integral of the absolute value of FUN(X).  Usually,
C              smaller values of ERR yield more accuracy and require
C              more function evaluations.
C
C              A negative value for ERR causes an estimate of the
C              absolute error in ANS to be returned in ERR.  Note that
C              ERR must be a variable (not a constant) in this case.
C              Note also that the user must reset the value of ERR
C              before making any more calls that use the variable ERR.
C
C        Output--* ERR,ANS are double precision *
C        ERR - will be an estimate of the absolute error in ANS if the
C              input value of ERR was negative.  (ERR is unchanged if
C              the input value of ERR was non-negative.)  The estimated
C              error is solely for information to the user and should
C              not be used as a correction to the computed integral.
C        ANS - computed value of integral
C        IERR- a status code
C            --Normal codes
C               1 ANS most likely meets requested error tolerance,
C                 or A=B.
C              -1 A and B are too nearly equal to allow normal
C                 integration.  ANS is set to zero.
C            --Abnormal code
C               2 ANS probably does not meet requested error tolerance.
C
C***REFERENCES  (NONE)
C***ROUTINES CALLED  D1MACH, I1MACH, XERMSG
C***REVISION HISTORY  (YYMMDD)
C   810223  DATE WRITTEN
C   890531  Changed all specific intrinsics to generic.  (WRB)
C   890911  Removed unnecessary intrinsics.  (WRB)
C   890911  REVISION DATE from Version 3.2
C   891214  Prologue converted to Version 4.0 format.  (BAB)
C   900315  CALLs to XERROR changed to CALLs to XERMSG.  (THJ)
C   900326  Removed duplicate information from DESCRIPTION section.
C           (WRB)
C***END PROLOGUE  DGAUS8

*/

      int k,l,lmn,lmx,mxl,nbits,
          nib,nlmx;

      double ae,anib,area,c,ce,ee,ef,eps,est,
             gl,glr,tol,vr,x,h,g8xh,ans;

      double aa[] = new double[61];
      double hh[] = new double[61];
      double vl[] = new double[61];
      double gr[] = new double[61];
      
      int lr[] = new int[61];


//     Initialize

//  K = I1MACH(14) --- SLATEC's i1mach(14) is the number of base 2
//  digits for double precision on the machine in question.  For
//  IEEE 754 double precision numbers this is 53.

//  k = 53;

//  r1mach(5) is the log base 10 of 2 = .3010299957

//      anib = .3010299957*53.0/0.30102000E0;

//      nbits = anib;

      nbits = 53;

      nlmx = Math.min(60,(nbits*5)/8);

      ans = 0.0;
      ierr[0] = 1;
      ce = 0.0;

      if (a == b) {

// 140

         if (err[0] < 0.0) err[0] = ce;

         return ans;

      }

      lmx = nlmx;
      lmn = nlmn;

      if (b != 0.0) {

         if (Gaus8.sign(1.0,b)*a > 0.0) {

            c = Math.abs(1.0 - a/b);

            if (c <= 0.1) {

               if (c <= 0.0) {

// 140

                  if (err[0] < 0.0) err[0] = ce;

                  return ans;

               }

               anib = 0.5 - Math.log(c)/0.69314718E0;

               nib = (int)(anib);

               lmx = Math.min(nlmx,nbits-nib-7);

               if (lmx < 1) {

// 130

                  ierr[0] = -1;

                  System.out.print("\n\nGaus8 --- a and b are too nearly"
                  + " equal to allow normal integration.\n"
                  + "ans is set to 0 and ierr[0] is set to -1.\n\n");

                  if (err[0] < 0.0) err[0] = ce;

                  return ans;

               }

               lmn = Math.min(lmn,lmx);

            }

         }

      }

// 10

      tol = Math.max(Math.abs(err[0]),Math.pow(2.0,5-nbits))/2.0;

      if (err[0] == 0.0) {

// According to the SLATEC documentation 2.22e-16 is the
// appropriate value for IEEE 754 double precision unit
// roundoff.

         tol = Math.sqrt(2.22e-16);

      }

      eps = tol;

      hh[1] = (b - a)/4.0;
      aa[1] = a;
      lr[1] = 1;

      l = 1;

      x = aa[l] + 2.0*hh[l];
      h = 2.0*hh[l];

      g8xh = h*((w1*(integclass.f_to_integrate(x - x1*h) +
                     integclass.f_to_integrate(x + x1*h)) +
                 w2*(integclass.f_to_integrate(x - x2*h) +
                     integclass.f_to_integrate(x + x2*h))) +
                 (w3*(integclass.f_to_integrate(x - x3*h) +
                     integclass.f_to_integrate(x + x3*h)) +
                 w4*(integclass.f_to_integrate(x - x4*h) +
                     integclass.f_to_integrate(x + x4*h))));

      est = g8xh;
      k = 8;
      area = Math.abs(est);
      ef = 0.5;
      mxl = 0;

//
//    Compute refined estimates, estimate the error, etc.
//

//   20 loop

      while (true) {

         x = aa[l] + hh[l];
         h = hh[l];

         g8xh = h*((w1*(integclass.f_to_integrate(x - x1*h) +
                        integclass.f_to_integrate(x + x1*h)) +
                    w2*(integclass.f_to_integrate(x - x2*h) +
                        integclass.f_to_integrate(x + x2*h))) +
                    (w3*(integclass.f_to_integrate(x - x3*h) +
                        integclass.f_to_integrate(x + x3*h)) +
                    w4*(integclass.f_to_integrate(x - x4*h) +
                        integclass.f_to_integrate(x + x4*h))));

         gl = g8xh;         

         x = aa[l] + 3.0*hh[l];
         h = hh[l];

         g8xh = h*((w1*(integclass.f_to_integrate(x - x1*h) +
                        integclass.f_to_integrate(x + x1*h)) +
                    w2*(integclass.f_to_integrate(x - x2*h) +
                        integclass.f_to_integrate(x + x2*h))) +
                    (w3*(integclass.f_to_integrate(x - x3*h) +
                        integclass.f_to_integrate(x + x3*h)) +
                    w4*(integclass.f_to_integrate(x - x4*h) +
                        integclass.f_to_integrate(x + x4*h))));

         gr[l] = g8xh;         

         k += 16;

         area += (Math.abs(gl) + Math.abs(gr[l]) - Math.abs(est));

         glr = gl + gr[l];

         ee = Math.abs(est - glr)*ef;

         ae = Math.max(eps*area,tol*Math.abs(glr));

         if (ee - ae > 0.0) {

// 50:

            if (k > kmx) lmx = kml;

            if (l < lmx) {

               l++;

               eps *= 0.5;

               ef /= sq2;

               hh[l] = hh[l-1]*0.5;

               lr[l] = -1;

               aa[l] = aa[l-1];

               est = gl;

            } else {

// 30:

               mxl = 1;

// 40,2:

               ce += (est - glr);

               if (lr[l] <= 0) {

// 60,1:

                  vl[l] = glr;

// 70,1:

                  est = gr[l-1];
                  lr[l] = 1;
                  aa[l] += 4.0*hh[l];

               } else {

// 80,1:

                  vr = glr;

// 90,1:

                  while (true) {

                     if (l <= 1) {

// 120,1:

                        ans = vr;

                        if ((mxl != 0) && (Math.abs(ce) > 2.0*tol*area)) {

                           ierr[0] = 2;

                           System.out.print("\n\nans is probably" +
                           " insufficiently accurate.\n\n");

                        }

// 140,1:

                        if (err[0] < 0.0) err[0] = ce;

                        return ans;


                     }

                     l--;

                     eps *= 2.0;

                     ef *= sq2;

                     if (lr[l] <= 0.0) break;

// 110,1:

                     vr += vl[l+1];

// end of 90,1 loop:

                  }

// 100,1:

                  vl[l] = vl[l+1] + vr;

// 70,3:

                  est = gr[l-1];
                  lr[l] = 1;
                  aa[l] += 4.0*hh[l];

// end of 80,1:

               }

// end of 30 branch:

            }

// end of 50 branch:

         } else {

// 40,1:

            ce += (est - glr);

            if (lr[l] <= 0) {

// 60,2:

               vl[l] = glr;

// 70,2:

               est = gr[l-1];
               lr[l] = 1;
               aa[l] += 4.0*hh[l];

            } else {

// 80,2:

               vr = glr;

// 90,2:

               while (true) {

                  if (l <= 1) {

// 120,2:

                     ans = vr;

                     if ((mxl != 0) && (Math.abs(ce) > 2.0*tol*area)) {

                        ierr[0] = 2;

                        System.out.print("\n\nans is probably" +
                        " insufficiently accurate.\n\n");

                     }

// 140,2:

                     if (err[0] < 0.0) err[0] = ce;

                     return ans;


                  }

                  l--;

                  eps *= 2.0;

                  ef *= sq2;

                  if (lr[l] <= 0.0) break;

// 110,2:

                  vr += vl[l+1];

// end of 90,2 loop:

               }

// 100,2:

               vl[l] = vl[l+1] + vr;

// 70,4:

               est = gr[l-1];
               lr[l] = 1;
               aa[l] += 4.0*hh[l];

// end of 80,2:

            }

// end of 40,1:

         }

// end of 20 loop:

      }

   }



/**
*
*<p>
*This method implements the FORTRAN sign (not sin) function.
*See the code for details.
*
*Created by Steve Verrill, March 1997.
*
*@param  a   a
*@param  b   b
*
*/

   public static double sign (double a, double b) {

      if (b < 0.0) {

         return -Math.abs(a);

      } else {

         return Math.abs(a);      

      }

   }


}