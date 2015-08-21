/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package climate;

import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import lib.Regression;

/**
 *
 * @author od
 */
public class TmeanCalc {

    @In    public double[] tmin;
    @In    public double[] tmax;
    @In    public double[] elevation;  //elevation array (maybe from tmin/tmax)
    
    @Out   public double[] dataArray;
    @Out   public double[] regCoeff;

    @Execute
    public void execute() {
        if (dataArray == null) {
            dataArray = new double[tmin.length];
        }
        for (int i = 0; i < dataArray.length; i++) {
            dataArray[i] = (tmax[i] + tmin[i]) / 2;
        }
        regCoeff = Regression.calcLinReg(elevation, dataArray);
    }
}
