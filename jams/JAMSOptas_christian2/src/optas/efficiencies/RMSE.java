/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class RMSE extends EfficiencyCalculator {
    
    @Override
    public double calc(double t1[], double t2[]){
        checkInputs(t1, t2);
        boolean filterMap[] = createFilterMap(t1, t2);
        
        double rsme = 0;
        int c=0,n = t1.length;
        for (int i=0;i<n;i++){
            if (filterMap[i])
                continue;
            rsme += (t1[i]-t2[i])*(t1[i]-t2[i]);
            c++;
        }
        return Math.sqrt(rsme)/(double)c;
    }

    @Override
    public double calcNormative(double t1[], double t2[]){
        return calc(t1,t2);
    }
    
    @Override
    public String toString(){
        return "RMSE";
    }
}
