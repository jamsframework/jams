package jams.math.distributions;

/**
*
*Currently, this class contains methods to calculate the
*cumulative distribution function (CDF) of a
*3-parameter Weibull distribution and the inverse of the CDF.
*
*@author Steve Verrill
*@version .5 --- January 10, 2001
*
*/

public class CDF_Weibull3 extends Object {

/**
*
*This method calculates the inverse
*of the 3-parameter Weibull cumulative distribution function.
*<p>
*
*@param   lambda    The 3-parameter Weibull scale parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda (x - \mu))^{\beta}).
*@param   beta      The 3-parameter Weibull shape parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda (x - \mu))^{\beta}).
*@param   mu        The 3-parameter Weibull location parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda (x - \mu))^{\beta}).
*@param   p         p must lie between 0 and 1.  w3inv returns
*                   the 3-parameter Weibull cdf inverse evaluated
*                   at p.
*
*@author Steve Verrill
*@version .5 --- January 10, 2001
*
*/

//  FIX: Eventually I should build in a check that p lies in (0,1)

   public static double w3inv(double lambda, double beta, 
                              double mu, double p) {

      double power,x;

      power = 1.0/beta;
      x = mu + Math.pow(-Math.log(1.0 - p),power)/lambda;

      return x;

   }


/**
*
*This method calculates the 3-parameter Weibull
*cumulative distribution function.
*<p>
*
*@param   lambda    The 3-parameter Weibull scale parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda (x - \mu))^{\beta}).
*@param   beta      The 3-parameter Weibull shape parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda (x - \mu))^{\beta}).
*@param   mu        The 3-parameter Weibull shape parameter.
*                   In \LaTeX notation,
*                   the distribution function is 
*                   1 - \exp(-(\lambda (x - \mu))^{\beta}).
*@param   x         x must be greater than mu.  w3inv returns
*                   the 3-parameter Weibull cumulative
*                   distribution function evaluated
*                   at x.
*
*@author Steve Verrill
*@version .5 --- January 10, 2001
*
*/

//  FIX: Eventually I should build in a check that x is greater than mu

   public static double w3cdf(double lambda, double beta, 
                              double mu, double x) {

      double p;

      p = 1.0 - Math.exp(-Math.pow(lambda*(x - mu),beta));

      return p;

   }

}



