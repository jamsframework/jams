/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class LogarithmicNashSutcliffe extends EfficiencyCalculator {

    double pow = 2.0;
    public LogarithmicNashSutcliffe(){

    }

    public LogarithmicNashSutcliffe(double pow){
        this.pow = pow;
    }

    @Override
    protected boolean[] createFilterMap(double m[], double s[]){
        boolean filterMap[] = super.createFilterMap(m, s);
        for (int i=0;i<m.length;i++){
            filterMap[i] = filterMap[i] || m[i]<=0 || s[i]<=0;
        }
        return filterMap;
    }
    
    @Override
    public double calc(double m[], double s[]){
        checkInputs(m, s);
        boolean filterMap[] = createFilterMap(m, s);
        
        double rsme = 0;
        double var  = 0;
        double avg  = 0;
        
        int n = m.length;
        int c = 0;
        for (int i=0;i<n;i++){
            if (filterMap[i])
                continue;
            avg += Math.log(m[i]);
            c++;
        }
        
        avg /= c;

        for (int i=0;i<n;i++){
            if (filterMap[i])
                continue;
            
            rsme += Math.pow(Math.abs(Math.log(m[i])-Math.log(s[i])),pow);
            var  += Math.pow(Math.abs(Math.log(m[i])-avg),pow);
           
        }
        return 1.0 - (rsme / var);
    }

    @Override
    public double calcNormative(double t1[], double t2[]){
        return 1.0 - calc(t1,t2);
    }
    
    @Override
    public String toString(){
        return "logE" + String.format("%d", (int)pow);
    }
}
