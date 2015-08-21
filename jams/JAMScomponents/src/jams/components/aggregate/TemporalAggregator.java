/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.aggregate;

import jams.JAMS;
import jams.aggregators.Aggregator;
import jams.aggregators.BasicTemporalAggregator;
import jams.aggregators.DoubleAggregator;
import jams.aggregators.TemporalAggregator.AggregationTimePeriod;
import jams.aggregators.TemporalAggregator.Consumer;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

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
    description = "The be aggregated results")
    public Attribute.Double[] outputAttributes;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The value(s) to be aggregated")
    public Attribute.Double[] inputAttributes;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The value(s) to be aggregated",
    defaultValue="1.0")
    public Attribute.Double weight;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "The value(s) to be aggregated",
            defaultValue = "")
    public Attribute.Entity internalState;

    protected class DataConsumer implements Consumer<Double> {

        Attribute.Double outputAttribute;

        DataConsumer(Attribute.Double outputAttribute) {
            this.outputAttribute = outputAttribute;
        }

        @Override
        public void consume(Attribute.Calendar c, Double v) {
            if (aggregationTime != null) {
                aggregationTime.setValue(c);
            }
            outputAttribute.setValue(v / weight.getValue());
        }
    }

    @Override
    public void initAll() {
        aggregationTime.setTimeInMillis(JAMS.getMissingDataValue(Long.class));

        //create aggreagators
        jams.aggregators.TemporalAggregator<Double>[] aggregators;
        aggregators = new jams.aggregators.TemporalAggregator[getNumberOfAttributes()];
        for (int i = 0; i < getNumberOfAttributes(); i++) {
            if (!isEnabled(i)) {
                continue;
            }

            Aggregator.AggregationMode innerMode = getAggregationModeID(i);
            AggregationTimePeriod innerTimeUnitID = getInnerTimeUnitID();
            jams.aggregators.TemporalAggregator<Double> aggregator = null;
                    
            if (innerTimeUnitID.equals(AggregationTimePeriod.CUSTOM)) {
                aggregator = new BasicTemporalAggregator<Double>(
                        DoubleAggregator.create(innerMode),
                        innerTimeUnitID, customTimePeriods);
            } else {
                aggregator = new BasicTemporalAggregator<Double>(
                        DoubleAggregator.create(innerMode),
                        innerTimeUnitID);
            }
            
            aggregator.addConsumer(new DataConsumer(outputAttributes[i]));
            aggregator.init();
            aggregators[i] = aggregator;
        }
        this.internalState.setObject("aggregators", aggregators);
    }

    protected boolean isConsiderable(Calendar c) {
        return true;
    }

    @Override
    public void run() {
        jams.aggregators.TemporalAggregator<Double>[] aggregators
                = (jams.aggregators.TemporalAggregator<Double>[]) this.internalState.getObject("aggregators");

        for (int i = 0; i < getNumberOfAttributes(); i++) {
            if (!isEnabled(i)) {
                continue;
            }

            jams.aggregators.TemporalAggregator<Double> aggregator = aggregators[i];

            if (isConsiderable(time)) {
                aggregator.aggregate(time, inputAttributes[i].getValue());
            }
        }
        //recheck if this is the last timestep, if so output data        
        //avoid cloning calendars!!
        time.add(interval.getTimeUnit(), interval.getTimeUnitCount());
        boolean isLastTimeStep = time.after(interval.getEnd());
        if (isLastTimeStep) {
            for (int i = 0; i < getNumberOfAttributes(); i++) {
                if (!isEnabled(i)) {
                    continue;
                }
                aggregators[i].finish();
            }
        }
        time.add(interval.getTimeUnit(), -interval.getTimeUnitCount());
    }
}
