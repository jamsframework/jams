/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

/**
 *
 * @author chris
 */
public class NashSutcliffe extends EfficiencyCalculator {

    double pow = 2.0;
    public NashSutcliffe(){

    }

    public NashSutcliffe(double pow){
        this.pow = pow;
    }

    @Override
    public double calc(double m[], double s[]){
        checkInputs(m, s);
        
        int n = m.length;
        boolean filterMap[] = createFilterMap(m, s);
        
        double rsme = 0;
        double var  = 0;
        double avg  = 0;
        
        int c=0;
        for (int i=0;i<m.length;i++){
            if (filterMap[i]){
                continue;
            }
            avg += m[i];
            c++;
        }
        avg /= c;

        for (int i=0;i<n;i++){
            if (filterMap[i]){
                continue;
            }
            rsme += Math.pow(Math.abs(m[i]-s[i]),pow);
            var  += Math.pow(Math.abs(m[i]-avg),pow);
        }
        return 1.0 - (rsme / var);
    }

    @Override
    public double calcNormative(double t1[], double t2[]){
        return 1.0 - calc(t1,t2);
    }
    
    @Override
    public String toString(){
        return "E" + String.format("%d", (int)pow);
    }
}
