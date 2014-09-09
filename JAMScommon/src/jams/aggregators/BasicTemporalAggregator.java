/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author christian
 */
public class BasicTemporalAggregator<T> extends TemporalAggregator<T>{    
    Aggregator<T> aggregator;
    
    protected BasicTemporalAggregator(BasicTemporalAggregator<T> original){
        super(original);
        this.aggregator = original.aggregator.copy();
    }
    
    public BasicTemporalAggregator(Aggregator aggregator, AggregationTimePeriod timePeriod){
        this(aggregator, timePeriod, null);        
    }
    
    public BasicTemporalAggregator(Aggregator aggregator, 
            AggregationTimePeriod timePeriod,
            Collection<TimeInterval> customTimePeriods){
        super(timePeriod, customTimePeriods);
        this.aggregator = aggregator;
    }
    
    @Override
    public void init() {   
        aggregator.init();
    }
    
    @Override
    public TemporalAggregator<T> copy() {   
        return new BasicTemporalAggregator(this);
    }
    
    @Override
    public void aggregate(Calendar timeStep, T next){
        if (isNextTimeStep(timeStep)){
            aggregator.finish();
            consume(currentTimeStep(), aggregator.get());          
            aggregator.init();  
            setTimeStep(timeStep);
        }
        aggregator.consider(next);
    }
            
    @Override
    public void finish(){    
        aggregator.finish();
        consume(currentTimeStep(), aggregator.get());  
        super.finish();
    }
    
    public static void main(String[] args) {
        DoubleArrayAggregator aggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.SUM, 2);
        ArrayList<TimeInterval> list = new ArrayList<TimeInterval>();
        TimeInterval ti = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti.getStart().set(2001, 0, 1, 0, 1, 1);
        ti.getEnd().set(2001, 11, 31, 0, 1, 1);
        list.add(ti);
        TemporalAggregator<double[]> tempAggr = new BasicTemporalAggregator(aggr, AggregationTimePeriod.CUSTOM, list);
        
        Calendar c = DefaultDataFactory.getDataFactory().createCalendar();
        c.set(2001, 0, 1, 0, 1, 1);
        
        tempAggr.addConsumer(new Consumer<double[]>() {

            @Override
            public void consume(Calendar c, double[] v) {
                System.out.println(c.toString() + " " + Arrays.toString(v));
            }
        });
        
        double v[] = new double[2];
        for (int i=0;i<10000;i++){            
            v[0] = 1;
            v[1] = i;
            tempAggr.aggregate(c, v);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        tempAggr.finish();
    }
}
