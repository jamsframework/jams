package jams.math.distributions;

/**
*
*Currently, this class contains methods to calculate the
*cumulative distribution function (CDF) of a
*2-parameter Weibull distribution and the inverse of the CDF.
*
*@author Steve Verrill
*@version .5 --- January 10, 2001
*
*/

public class CDF_Weibull2 extends Object {

/**
*
*This method calculates the inverse
*of the 2-parameter Weibull cumulative distribution function.
*<p>
*
*@param   lambda    The 2-parameter Weibull scale parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda x)^{\beta}).
*@param   beta      The 2-parameter Weibull shape parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda x)^{\beta}).
*@param   p         p must lie between 0 and 1.  w2inv returns
*                   the 2-parameter Weibull cdf inverse evaluated
*                   at p.
*
*@author Steve Verrill
*@version .5 --- January 10, 2001
*
*/

//  FIX: Eventually I should build in a check that p lies in (0,1)

   public static double w2inv(double lambda, double beta, double p) {

      double power,x;

      power = 1.0/beta;
      x = Math.pow(-Math.log(1.0 - p),power)/lambda;

      return x;

   }


/**
*
*This method calculates the 2-parameter Weibull
*cumulative distribution function.
*<p>
*
*@param   lambda    The 2-parameter Weibull scale parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda x)^{\beta}).
*@param   beta      The 2-parameter Weibull shape parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda x)^{\beta}).
*@param   x         x must be greater than 0.  w2inv returns
*                   the 2-parameter Weibull cumulative
*                   distribution function evaluated
*                   at x.
*
*@author Steve Verrill
*@version .5 --- January 10, 2001
*
*/

//  FIX: Eventually I should build in a check that x is greater than 0

   public static double w2cdf(double lambda, double beta, double x) {

      double p;

      p = 1.0 - Math.exp(-Math.pow(lambda*x,beta));

      return p;

   }

}



