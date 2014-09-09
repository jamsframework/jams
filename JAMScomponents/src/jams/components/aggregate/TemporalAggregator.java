/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.io.IOException;

/**
 *
 * @author christian
 */
@JAMSComponentDescription(
        title = "TimePeriodAggregator",
        author = "Christian Fischer",
        description = "Aggregates timeseries values to a given time period of day, month, year or dekade")

public class TemporalAggregator extends TemporalAggregatorBase {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "current aggregation interval start time")
    public Attribute.Calendar aggregationTime;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "The be aggregated results")
    public Attribute.Double[] aggregate;
    
    @Override
    protected void writeData(Calendar c, double [] values ) throws IOException{        
        if (this.aggregationTime!=null){
            this.aggregationTime.setValue(c);
        }
        for (int i = 0; i < values.length; i++) {
            if (!isEnabled[i]) {
                continue;
            }
            this.aggregate[i].setValue(values[i]);
        }        
    }
}
