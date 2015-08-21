/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.efficiencies;

/**
 *
 * @author chris
 */
public class VolumeError extends EfficiencyCalculator {

    enum VolumeErrorType {

        Absolute, Relative
    };

    VolumeErrorType v;

    VolumeError(VolumeErrorType v) {
        this.v = v;
    }

    @Override
    public double calc(double m[], double s[]) {
        checkInputs(m, s);
        boolean filterMap[] = createFilterMap(m, s);

        int n = m.length;

        double ve = 0;
        for (int i = 0; i < m.length; i++) {
            if (filterMap[i]) {
                continue;
            }
            ve += (s[i] - m[i]);
        }
        
        if (v == VolumeErrorType.Relative) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                if (filterMap[i]) {
                    continue;
                }
                sum += m[i];
            }
            return ve / sum;
        }
        return ve;
    }

    @Override
    public double calcNormative(double t1[], double t2[]) {
        return Math.abs(calc(t1, t2));
    }
    
    @Override
    public String toString(){
        return this.v.name() + " Volume Error";
    }
}
