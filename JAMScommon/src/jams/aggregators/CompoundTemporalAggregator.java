/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

import jams.aggregators.TemporalAggregator.AggregationTimePeriod;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.util.Arrays;
import java.util.Collection;


/**
 *
 * @author christian
 */
public class CompoundTemporalAggregator<T> extends TemporalAggregator<T>{    
    Aggregator<T> aggregator;
    TemporalAggregator<T> innerAggregator;
    
    public CompoundTemporalAggregator(Aggregator<T> aggregator, 
                                     TemporalAggregator<T> innerAggregator,
                                     AggregationTimePeriod timePeriod){        
        this(aggregator, innerAggregator, timePeriod, null);
    }
    
    public CompoundTemporalAggregator(Aggregator<T> aggregator, 
                                     TemporalAggregator<T> innerAggregator,
                                     AggregationTimePeriod timePeriod,
                                     Collection<TimeInterval> timePeriods){
        super(timePeriod, timePeriods);
        
        this.aggregator = aggregator;
        this.innerAggregator = innerAggregator;
        
        innerAggregator.addConsumer(new Consumer<T>() {
            @Override
            public void consume(Calendar c, T v) {
                CompoundTemporalAggregator.this.aggregator.consider(v);
            }
        });
    }
    
    protected CompoundTemporalAggregator(CompoundTemporalAggregator<T> original){
        super(original);
        this.aggregator = original.aggregator.copy();
        this.innerAggregator = original.innerAggregator.copy();
    }
    
    @Override
    public void init() {
        aggregator.init();
        innerAggregator.init();
    }
    
    @Override
    public TemporalAggregator<T> copy() {   
        return new CompoundTemporalAggregator(this);
    }
    
    @Override
    public void aggregate(Calendar timeStep, T next){        
        innerAggregator.aggregate(timeStep, next);
        if (isNextTimeStep(timeStep)){
            aggregator.finish();
            consume(currentTimeStep(), aggregator.get());
            aggregator.init();              
            setTimeStep(timeStep);
        }        
    }
    
    @Override
    public void finish(){
        innerAggregator.finish();        
        aggregator.finish();
        consume(currentTimeStep(), aggregator.get());
        super.finish();
    }
    
    public static void main(String[] args) {
        DoubleArrayAggregator innerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.AVERAGE, 2);
        DoubleArrayAggregator outerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.SUM, 2);
        TemporalAggregator<double[]> InnerTempAggr = 
                new BasicTemporalAggregator(innerAggr, AggregationTimePeriod.MONTHLY);
        
        CompoundTemporalAggregator<double[]> OuterTempAggr = 
                new CompoundTemporalAggregator<double[]>(outerAggr, InnerTempAggr, AggregationTimePeriod.YEARLY);
        
        Calendar c = DefaultDataFactory.getDataFactory().createCalendar();
        c.set(2001, 0, 1, 0, 1, 1);
        
        OuterTempAggr.addConsumer(new Consumer<double[]>() {

            @Override
            public void consume(Calendar c, double[] v) {
                System.out.println(c.toString() + " " + Arrays.toString(v));
            }
        });
        
        double v[] = new double[2];
        for (int i=0;i<10000;i++){            
            v[0] = 1;
            v[1] = i;
            //System.out.println("i:" + i);
            OuterTempAggr.aggregate(c, v);
            c.add(Calendar.MONTH, 1);
        }
        OuterTempAggr.finish();
    }
}
