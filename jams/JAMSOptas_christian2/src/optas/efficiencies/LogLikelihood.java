/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

import umontreal.iro.lecuyer.util.Num;

/**
 *
 * @author chris
 */
public class LogLikelihood extends EfficiencyCalculator {

    double obsSigma = 1;
    
    public LogLikelihood(){

    }

    @Override
    public double calc(double m[], double s[]){
        checkInputs(m, s);
        boolean filterMap[] = this.createFilterMap(m, s);
        
        int n = m.length;
        
        double MCMCPar_Gamma = 0.0;
        
                // Calculate the parameters in the exponential power density function of
        // Box and Tiao (1973)
        double alpha1 = 3.0 * (1.0 + MCMCPar_Gamma) / 2.0;
        double alpha2 = (1.0 + MCMCPar_Gamma) / 2.0;
        //
        // double alpha1=(3*(1+MCMCPar_Gamma)/2);
        // double alpha2=((1+MCMCPar_Gamma)/2);

        double logA1 = Num.lnGamma(alpha1);
        double A1 = Math.exp(logA1);
        
        double logA2 = Num.lnGamma(alpha2);
        double A2 = Math.exp(logA2);
        
        double exp1 = 1 / (1 + MCMCPar_Gamma);
        
        double MCMCPar_Wb = Math.sqrt(A1) / ((1 + MCMCPar_Gamma) * (Math.pow(A2, 1.5)));
        double MCMCPar_Cb = Math.pow((A1 / A2), exp1);
        
        
        double sum1 = 0;
        double sum2 = 0;
        for (int i = 0; i < n; i++) {
            if (filterMap[i])
                continue;
            
            double ter1 = Math.log((MCMCPar_Wb / obsSigma));
            sum1 = sum1 + ter1;
            double val = Math.abs((m[i]-s[i]) / obsSigma);
            double pp = Math.pow(val, (2.0 / (1.0 + MCMCPar_Gamma)));
            sum2 = sum2 + pp;
        }
        return sum1 - MCMCPar_Cb * sum2;
        
    }

    @Override
    public double calcNormative(double t1[], double t2[]){
        return calc(t1,t2);
    }
    
    @Override
    public String toString(){
        return "logLiklihood";
    }
}
