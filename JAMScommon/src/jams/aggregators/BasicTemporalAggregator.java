/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
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
    
    public BasicTemporalAggregator(Aggregator<T> aggregator, AggregationTimePeriod timePeriod){
        this(aggregator, timePeriod, null);        
    }
    
    public BasicTemporalAggregator(Aggregator<T> aggregator, 
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
            //System.out.println("Next->" + timeStep);
            aggregator.finish();
            consume(currentTimeStep(), aggregator.get());          
            aggregator.init();  
            setTimeStep(timeStep);            
        }
        //System.out.println("Add->" + timeStep + "("+ next +")");
        aggregator.consider(next);
    }
            
    @Override
    public void finish(){    
        aggregator.finish();
        consume(currentTimeStep(), aggregator.get());  
        super.finish();
    }   
}
