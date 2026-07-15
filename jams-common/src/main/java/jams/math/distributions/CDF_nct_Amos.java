package jams.math.distributions;


/**
*
*This class contains Java translations of FORTRAN
*routines written by Don Amos and S.L. Daniel to calculate the
*noncentral T cumulative distribution function.
*
*@version .5 --- February 17, 2002
*
*/

public class CDF_nct_Amos extends Object implements Gaus8_fcn, Fzero_methods {

      double h2o2,p_inv,delta_inv,dof_inv;

/**
*
*This method calculates the noncentral T cumulative distribution function.
*It is a Java translation of the public domain FORTRAN
*routine fnct written by Don Amos and S.L. Daniel.
*Steve Verrill translated the FORTRAN code (a write date of September, 1972,
*and a modification date of July, 1991)
*into Java.  This translation was performed on February 17, 2001.
*
*@param   x     The method returns the value of the noncentral T
*               cumulative distribution function at x.
*@param   delta The noncentrality parameter
*@param   dof   The degrees of freedom
*
*@version .5 --- February 17, 2002
*
*/


   public static double fnct(double x, double delta, double dof) {

/*

Here is a copy of the documentation in the FORTRAN code:

      FUNCTION FNCT(X,DELTA,F)
C 
C     WRITTEN BY D.E. AMOS AND S.L. DANIEL, SEPTEMBER, 1972.
C     MODIFIED, JULY, 1991
C 
C     REFERENCES
C         A SURVEY OF PROPERTIES AND APPLICATIONS OF THE NON-CENTRAL T
C         DISTRIBUTION BY D.B. OWEN, TECHNOMETRICS, VOL. 10, NO. 3, 
C         AUGUST, 1968, PP. 464-465.
C 
C         SLA-73-0333 
C 
C     ABSTRACT
C         FNCT COMPUTES THE CUMULATIVE NON-CENTRAL T-DISTRIBUTION FOR 
C         T=U/SQRT(V/F) WHERE U IS N(DELTA,1) AND V IS CHI-SQUARE 
C         WITH F DEGREES OF FREEDOM. FINITE SUMS WHOSE FIRST
C         TERMS CONTAIN THE CUMULATIVE NORMAL AND/OR THE T(H,A) 
C         INTEGRAL OF OWEN ARE EVALUATED FOR EVEN AND ODD F.
C 
C     DESCRIPTION OF ARGUMENTS
C 
C         INPUT 
C           X      - T-DISTRIBUTION VARIABLE,  UNRESTRICTED
C           DELTA  - NON-CENTRALITY PARAMETER, UNRESTRICTED 
C           F      - DEGREES OF FREEDOM,       F.GE.1.0E0
C 
C         OUTPUT
C           FNCT   - CUMULATIVE DISTRIBUTION AT X, FNCT=PR(T.LE.X)
C 
C     ERROR CONDITIONS
C         IMPROPER INPUT PARAMETER IS A FATAL ERROR 
C 
C     FNCT   USES COMMON BLOCKS MLTHA
c
c                               x    x   x    x      x      x      x
c   need to compile with xerror.f and machcon.f
c
C     FNCT   USES SUBROUTINES FNORM,THA,FTHA,RERF,I1MACH,R1MACH,XERROR
C-----------------------------------------------------------------------

*/

      final double srtp = 2.50662827463100E0;
      final double osrtp = 3.98942280401432E-1;

      double rel[] = new double[1];

      double t2[] = new double[1];

      double p,tol,a,b,srb,x1,xn,t1,asrb,x2,x3,g2,
             gprime,cm0,cm1,cm2,fk,ak,ss,cmk,y1,arg;

      int nz[] = new int[1];

      int ndof,kode,n,j,nt;

      if (dof < 1.0) {

         p = -1.0;

         System.out.print("\nError in call to fnct:" +
         " The degrees of freedom" +
         " was less than 1.\n\n");

         return p;

      }

/*

From the FORTRAN code:

C-----------------------------------------------------------------------
C     SET PARAMETERS RELATED TO MACHINE CONSTANTS.
C     TOL IS THE MAXIMUM OF UNIT ROUNDOFF, 1.0E-15
C     ELIM IS THE APPROXIMATE EXPONENTIAL OVER- AND UNDERFLOW LIMIT.
C     K1 = I1MACH(12)
C     K2 = I1MACH(13)
C     K = MIN0(IABS(K1),IABS(K2))
C     ELIM = 2.303E0*(FLOAT(K)*R1MACH(5)-3.0E0)
C-----------------------------------------------------------------------

In the FORTRAN code "unit roundoff" is taken to be
SLATEC's r1mach(4).  It appears that for IEEE 754
double precision aritmetic this should be replaced
by 2^{-53} x 2 = 2.22e-16.  The maximum of this and
1.0e-15 is 1.0e-15, so:

*/

      tol = 1.0e-15;
      rel[0] = tol*10.0;

/*

SLATEC's i1mach(12) = the smallest exponent of single precision reals
         i1mach(13) = the largest exponent of single precision reals
         r1mach(5) = log, base 10, of 2 = .3010299957

For IEEE 754 doubles, the smallest exponent is -1022.
The largest exponent is 1023.  So:

      elim = 2.303*(1022*.3010299957 - 3.0);

*/

      a = x/Math.sqrt(dof);
      b = dof/(dof + x*x);
      srb = Math.sqrt(b);
      ndof = (int)dof;

      if (ndof % 2 == 1) {

//   dof is odd

         x1 = delta*srb;
         xn = -x1;
         kode = 1;

         t1 = CDF_Normal_Amos.fnorm(xn,kode,nz);

         CDF_nct_Amos.tha(x1,a,rel,t2);        

         if (ndof == 1) {

            p = t1 + t2[0] + t2[0];

            return p;

         }

// 102

         asrb = a*srb;
         x2 = x1*a;
         x3 = delta*a;

         kode = 1;

         g2 = CDF_Normal_Amos.fnorm(x2,kode,nz);

         gprime = osrtp*Math.exp(-x1*x1/2.0);

         cm0 = asrb*gprime*g2;

         gprime = osrtp*Math.exp(-delta*delta/2.0);

         cm1 = b*(x3*cm0 + (a/srtp)*gprime);

         cm2 = 0.5*b*(x3*cm1 + cm0);

         fk = 3.0;

         ak = 1.0;

         n = (ndof - 3)/2;

         if (n - 1 < 0) {

// 103

            p = t1 + t2[0] + t2[0] + cm1 + cm1;

            return p;

         }

         if (n - 1 == 0) {

// 104

            x1 = b/3.0;

            x2 = cm1 + (x1 + x1)*(x3*cm2 + cm1);

            p = t1 + t2[0] + t2[0] + x2 + x2;

            return p;

         }

//   n - 1 > 0
//   105

         ss = cm1;

         for (j = 1; j <= n; j++) {

            cmk = ((fk - 1.0)/fk)*b*(ak*x3*cm2 + cm1);
            ss += cmk;
            fk++;

            ak = 1.0/((fk - 2.0)*ak);

//   update and skip even values

            cm1 = cm2;
            cm2 = cmk;
            cmk = ((fk - 1.0)/fk)*b*(ak*x3*cm2 + cm1);
            fk++;

            ak = 1.0/((fk - 2.0)*ak);

            cm1 = cm2;
            cm2 = cmk;

         }

         p = t1 + t2[0] + t2[0] + ss + ss;    

         return p;

      } else {

//   dof is even

         y1 = -delta;

         kode = 1;

         t1 = CDF_Normal_Amos.fnorm(y1,kode,nz);

         x1 = a*srb;
         x2 = delta*x1;
         x3 = delta*a;

         kode = 1;

         g2 = CDF_Normal_Amos.fnorm(x2,kode,nz);

         arg = -delta*delta*srb*srb/2.0;

         gprime = osrtp*Math.exp(arg);

         cm0 = x1*gprime*g2;

         ndof = (int)dof;

         n = ndof/2;

         if (n == 1) {

            p = t1 + srtp*cm0;

            return p;

         }

         gprime = osrtp*Math.exp(-delta*delta/2.0);

         cm1 = b*(x3*cm0 + (a/srtp)*gprime);

         cm2 = (b/2.0)*(x3*cm1 + cm0);

         if (n == 2) {

            p = t1 + srtp*(cm0 + cm2);

            return p;

         }

         fk = 3.0;
         ak = 1.0;
         ss = cm0 + cm2;
         nt = n - 2;

         for (j = 1; j <= nt; j++) {

            cmk = ((fk - 1.0)/fk)*b*(ak*x3*cm2 + cm1);

            fk++;

            ak = 1.0/((fk - 2.0)*ak);

            cm1 = cm2;

            cm2 = cmk;

            cmk = ((fk - 1.0)/fk)*b*(ak*x3*cm2 + cm1);

            ss += cmk;

            fk++;

            ak = 1.0/((fk - 2.0)*ak);

            cm1 = cm2;

            cm2 = cmk;

         }

         p = t1 + srtp*ss;

         return p;        

      }

   }








/**
*
*This method calculates the inverse of the
*noncentral T cumulative distribution function.
*It uses the fzero zero-finding routine and the
*fnct noncentral T cumulative distribution function.
*It was written by a statistician rather than
*a numerical analyst.  When a noncentral T inverse
*function written in Java by a numerical analyst becomes
*available, it should be used rather than this routine.
*
*
*@param   p     The method returns the x value
*               for which CDF_nct(delta,dof)(x) = p.
*@param   delta The noncentrality parameter
*@param   dof   The degrees of freedom
*
*@version .5 --- February 26, 2002
*
*/


   public static double fnct_inv(double p, double delta, double dof) {

      double norminv,add,add0,start,pdelta,pp,plow,phigh,xx;

      double b[] = new double[2];
      double c[] = new double[2];
      double r,re,ae;

      int iflag[] = new int[2];

      CDF_nct_Amos cdf_nct_amos = new CDF_nct_Amos();

      cdf_nct_amos.p_inv = p;
      cdf_nct_amos.delta_inv = delta;
      cdf_nct_amos.dof_inv = dof;

      norminv = CDF_Normal.xnormi(p);
      add = Math.sqrt(delta*delta/(2.0*dof) + 1.0);
      add0 = norminv*add;

// an initial estimate of the inverse:

      xx = delta + add0;

      pp = CDF_nct_Amos.fnct(xx,delta,dof);

// obtaining a region in which the inverse lies:

      if (pp < p) {

         plow = pp;
         b[1] = xx;

         while (true) {

            xx += add;

            phigh = CDF_nct_Amos.fnct(xx,delta,dof);

            if (phigh >= p) break;

         }

         c[1] = xx;

      } else {

         phigh = pp;
         c[1] = xx;

         while (true) {

            xx -= add;

            plow = CDF_nct_Amos.fnct(xx,delta,dof);

            if (plow <= p) break;

         }

         b[1] = xx;

      }

      r = b[1] + ((p - plow)/(phigh - plow))*(c[1] - b[1]);

      re = .000001;
      ae = .00001;

      Fzero.fzero(cdf_nct_amos,b,c,r,re,ae,iflag);

      return b[1];

   }



/**
*
*This method calculates the t(h,a) integral of Owen.
*It is a Java translation of the public domain FORTRAN
*routine tha (dated January, 1975) written by Don Amos and S.L. Daniel.
*Steve Verrill translated the FORTRAN code 
*into Java in February of 2002.
*
*@param   h     
*@param   a
*@param   rel[1]   Relative error parameter for gaus8
*                  rel = 5.0e-(s+1) for s significant digits
*@param   ans[1]
*
*@version .5 --- February 17, 2002
*
*/


   public static void tha(double h, double a, double rel[], double ans[]) {


/*

Here is a copy of the documentation in the FORTRAN code:

      SUBROUTINE THA(H,A,REL,ANS) 
C 
C     WRITTEN BY D.E. AMOS AND S.L. DANIEL, JANUARY, 1975.
C 
C     REFERENCE SLA-73-0333 
C 
C     ABSTRACT
C         SUBROUTINE THA COMPUTES THE T(H,A) INTEGRAL OF OWEN,
C         T(H,A)=INTEGRAL ON (0,A) OF EXP(-H*H*(1+X*X)/2)/(1+X*X).
C         THE IDENTITIES
C                     T(-H,A)=T(H,A) ,  T(H,-A)=-T(H,A) 
C         T(H,A)=G(U)+G(V)-4.0*G(U)*G(V)-T(V,1/A)   ,   A.GT.1
C                   G(X)=0.25*(1+ERF(X/SQRT(2)) 
C                           U=H , V=A*H 
C         AND THEIR VARIANTS ARE USED TO KEEP THE VARIABLES POSITIVE
C         AND LIMIT THE INTERVAL OF INTEGRATION TO A SUBSET OF (0,1). 
C         THE INTEGRATION IS PERFORMED BY GAUS8 ON FUNCTION FTHA.  THE
C         CASES H=0 AND A=0 ARE EVALUATED EXPLICITLY. 
C 
C     DESCRIPTION OF ARGUMENTS
C 
C         INPUT 
C           H      - ARGUMENT, UNRESTRICTED 
C           A      - ARGUMENT, UNRESTRICTED 
C           REL    - RELATIVE ERROR PARAMETER FOR GAUS8 
C                    REL=5.E-(S+1) FOR S SIGNIFICANT DIGITS 
C         OUTPUT
C           ANS    - A VALUE FOR T(H,A) 
C 
C     ERROR CONDITIONS
C         ERROR MESSAGE FROM GAUS8 - A FATAL ERROR
C 
C     THA    USES COMMON BLOCKS MLTHA 
C     THA    USES SUBROUTINES RERF,FTHA,GAUS8,I1MACH,R1MACH,XERROR
C-----------------------------------------------------------------------

*/

      final double otpi = 1.59154943091895E-1;
      final double srt2 = 1.41421356237310E0;

      double aa,hh,ab,elim,h2,c1,c2,t1,t2,t3,t4,t5;

      int ierr[] = new int[1];
      int nr[] = new int[1];

      CDF_nct_Amos cdf_nct_amos = new CDF_nct_Amos();


      aa = a;
      hh = h;

      if (aa == 0.0) {

         ans[0] = 0.0;

         return;

      }

      ab = Math.abs(aa);

      if (hh == 0.0) {

         ans[0] = otpi*Math.atan(ab);

         if (aa < 0.0) ans[0] = -ans[0];

         return;

      }

      if (hh < 0.0) hh = Math.abs(hh);

//      kk = Math.min(Math.abs(i1mach[15]),Math.abs(i1mach[16]);
//  For IEEE 754 arithmetic, the double precision smallest exponent
//  is -1022, the double precision largest exponent is 1023.
//  This yields kk = 1022.  zzz = log_10(2) = .3010299957.
//  Presumably we don't need many digits here, but ...

//      elim = 2.303*(kk*zzz - 3.0);

      elim = 2.303*(1022.0*.3010299957 - 3.0);

      if (ab <= 1.0) {

         h2 = hh*hh;

         cdf_nct_amos.h2o2 = 0.5*h2;

         if (cdf_nct_amos.h2o2 >= elim) {

            ans[0] = 0.0;

            return;

         }

         ans[0] = Gaus8.gaus8(cdf_nct_amos,0.0,ab,rel,ierr);

         if (ierr[0] == 2) {

            System.out.print("\n\nERROR: In tha, gaus8 returned ierr[0]" +
            " equal to 2.\n\n");

            return;

         }

         ans[0] *= otpi;

         if (aa < 0.0) ans[0] = -ans[0];

         return;

      } else {

// 14

         c1 = hh/srt2;

         hh *= ab;

         c2 = hh/srt2;

         h2 = hh*hh;

         cdf_nct_amos.h2o2 = 0.5*h2;

         ab = 1.0/ab;

         if (cdf_nct_amos.h2o2 < elim) {

            t3 = Gaus8.gaus8(cdf_nct_amos,0.0,ab,rel,ierr);

            if (ierr[0] == 2) {

               System.out.print("\n\nERROR: In tha, gaus8 returned ierr[0]" +
               " equal to 2.\n\n");

               return;

            }

         } else {

            t3 = 0.0;

         }

// 17

         if (c1 < 2.0 || c2 < 2.0) {

// Note that we are using an error function different from
// the one included in Amos and Daniel's fnct file.

            t1 = Derf.derf(c1);
            t2 = Derf.derf(c2);

            ans[0] = 0.25*(2.0 + t1 + t2 
                   - (1.0 + t1)*(1.0 + t2)) - t3*otpi;

            if (aa < 0.0) ans[0] = -ans[0];

            return;

         }

// 30

         t4 = Derf.derfc(c1);
         t5 = Derf.derfc(c2);

         ans[0] = 0.25*(t4 + t5 - t4*t5) - t3*otpi;

         if (aa < 0.0) ans[0] = -ans[0];

         return;

      }

   }



   public double f_to_integrate(double x) {

//   for tha

      double opx2,ftha;

      opx2 = 1.0 + x*x;

      ftha = Math.exp(-h2o2*opx2)/opx2;

      return ftha;

   }



   public double f_to_zero(double x) {

      double diff;

      diff = p_inv - CDF_nct_Amos.fnct(x,delta_inv,dof_inv);

      return diff;         

   }
      





}
