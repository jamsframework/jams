/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class CorrelationError extends EfficiencyCalculator {

    double pow = 2.0;
    public CorrelationError(){

    }

    public CorrelationError(double pow){
        this.pow = pow;
    }

    @Override
    public double calc(double m[], double s[]){
        checkInputs(m, s);
        
        double meanx = 0, meany=0;
        boolean filterMap[] = createFilterMap(m, s);
        
        int n = m.length;
        int c = 0;
        for (int i=0;i<n;i++){
            if (filterMap[i])
                continue;
            c++;
            
            meanx += m[i];
            meany += s[i];
        }
        
        meanx /= c;
        meany /= c;

        double sumx=0,sumy=0,prod=0;
        for(int i = 0; i < n; i++){
            if (filterMap[i])
                continue;
            
            sumx += Math.pow((m[i] - meanx), 2);
            sumy += Math.pow((s[i] - meany), 2);
            prod += ((m[i] - meanx)*(s[i] - meany));

        }
        double r2 = Math.pow((prod / Math.sqrt(sumx * sumy)), 2);
        return r2;
    }

    @Override
    public double calcNormative(double t1[], double t2[]){
        return 1.0 - Math.abs(calc(t1,t2));
    }
    
    @Override
    public String toString(){
        return "r²";
    }
}
