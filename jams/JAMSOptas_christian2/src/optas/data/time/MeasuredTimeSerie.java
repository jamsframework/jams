/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data.time;

import optas.data.time.api.TimeSerie;
import optas.data.time.DefaultComparableTimeSerie;

/**
 *
 * @author chris
 */
public class MeasuredTimeSerie extends DefaultComparableTimeSerie<Double> {
    
    
    private MeasuredTimeSerie(TimeSerie<Double> set){
        super(set);
    }
    
    static public MeasuredTimeSerie getInstance(TimeSerie<Double> set){
        return new MeasuredTimeSerie(set);
    }
}
