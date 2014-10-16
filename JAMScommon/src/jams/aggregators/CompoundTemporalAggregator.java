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
        if (isNextTimeStep(timeStep)){
            innerAggregator.finish();
            aggregator.finish();
            consume(currentTimeStep(), aggregator.get());
            innerAggregator.init();
            aggregator.init();              
            setTimeStep(timeStep);
        }        
        innerAggregator.aggregate(timeStep, next);
    }
    
    @Override
    public void finish(){
        innerAggregator.finish();        
        aggregator.finish();
        consume(currentTimeStep(), aggregator.get());
        super.finish();
    }    
}
