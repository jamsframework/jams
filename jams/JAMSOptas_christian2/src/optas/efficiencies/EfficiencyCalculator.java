/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.efficiencies;

import com.google.common.base.Preconditions;
import jams.JAMS;
import java.io.Serializable;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public abstract class EfficiencyCalculator implements Serializable {
            
    public abstract double calc(double t1[], double t2[]);
    public abstract double calcNormative(double t1[], double t2[]);
    
    protected void checkInputs(double t1[], double t2[]){
        Preconditions.checkNotNull(t1, "t1 must not be null!");
        Preconditions.checkNotNull(t2, "t2 must not be null!");
        Preconditions.checkArgument(t1.length == t2.length, "t1 must be the same length as t2");
    }
    
    protected boolean[] createFilterMap(double t1[], double t2[]){
        Preconditions.checkArgument(t1.length == t2.length, "t1 must be the same length as t2");
        int n = t1.length;
        
        boolean filterMap[] = new boolean[n];        
        double missingValue = JAMS.getMissingDataValue();
        
        for (int i=0;i<n;i++){
            filterMap[i] =  (t1[i] == missingValue || t2[i] == missingValue);
        }
        return filterMap;
    }
}
