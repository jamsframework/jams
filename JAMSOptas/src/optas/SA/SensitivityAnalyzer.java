/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import optas.hydro.data.Efficiency;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.SimpleEnsemble;
import optas.regression.IDW;
import optas.regression.Interpolation;

/**
 *
 * @author chris
 */
public abstract class SensitivityAnalyzer {
    SimpleEnsemble x[];
    EfficiencyEnsemble y;

    protected int n,m;
    protected int L=0;

    protected boolean initSuccessful = false;

    Interpolation I;

    protected double[][] getParameterRange() {
        double range[][] = new double[n][2];

        for (int j = 0; j < n; j++) {
            range[j][0] = x[j].getMin();
            range[j][1] = x[j].getMax();
        }
        return range;
    }

    public void setData(SimpleEnsemble x[], EfficiencyEnsemble y){
        this.x = x;
        this.y = y;

        n = x.length;
        if (n==0){
            return;
        }
        L = x[0].getSize();
        for (int i=0;i<n;i++){
            if (x[i].getSize()!=L)
                return;            
        }
        if (y.getSize()!=L)
            return;
        initSuccessful = true;

    }
    public void setInterpolationMethod(Interpolation I){
        this.I = I;
    }

    public void init(){
        if (I == null){
            I = new IDW();
        }
        I.setData(x, y);
        I.init();
    }

    abstract public double getSensitivity(int parameter);
}
