/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.data;

import optas.data.time.api.TemporalSpatialDataSet;
import optas.data.time.DefaultTemporalSpatialDataSet;

/**
 *
 * @author chris
 */
public class MeasuredTemporalSpatialDataSet extends DefaultTemporalSpatialDataSet<Double> {
    
    
    private MeasuredTemporalSpatialDataSet(TemporalSpatialDataSet<Double> set){
        super(set);
    }
    
    static public MeasuredTemporalSpatialDataSet getInstance(TemporalSpatialDataSet<Double> set){
        return new MeasuredTemporalSpatialDataSet(set);
    }
}
