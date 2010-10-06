/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui.MCAT5;

import reg.gui.MCAT5Toolbar.ParameterSet;

/**
 *
 * @author chris
 */
public class MCAT5Tools {
    static public double FindMinimalParameterValue(ParameterSet data){
            int n = data.set.length;
            double min = Double.POSITIVE_INFINITY;
            for (int i=0;i<n;i++){
                min = Math.min(min, data.set[i]);
            }
            return min;
        }
    static public double FindMaximalParameterValue(ParameterSet data){
            int n = data.set.length;
            double max = Double.NEGATIVE_INFINITY;
            for (int i=0;i<n;i++){
                max = Math.max(max, data.set[i]);
            }
            return max;
        }
}
