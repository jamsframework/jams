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
public class MultiTemporalAggregator<T> extends TemporalAggregator<T> {

    TemporalAggregator innerAggregator[];
    int K = 0;
    
    private class MultiTemporalConsumer implements Consumer<T> {
        int k=0;
        MultiTemporalConsumer(int k){
            this.k=k;
        }
        @Override
        public void consume(Calendar c, T v) {      
            Calendar c2 = c.clone();
            switch(MultiTemporalAggregator.this.timePeriod){
                case DAILY: c2.add(Calendar.DAY_OF_YEAR, k); break;
                case MONTHLY: c2.add(Calendar.MONTH, k); break;
                case SEASONAL: c2.add(Calendar.MONTH, 3*k); break;
                case HALFYEAR: c2.add(Calendar.MONTH, 6*k); break;
                case DECADLY: c2.add(Calendar.YEAR, 10*k); break;
                case HOURLY: c2.add(Calendar.HOUR_OF_DAY, k); break;
                case YEARLY: c2.add(Calendar.YEAR, k); break;
            }
                    
            MultiTemporalAggregator.this.consume(c2, v);
        }
    }
                
    public MultiTemporalAggregator(
            TemporalAggregator innerAggregator,
            AggregationTimePeriod timePeriod) {
        this(innerAggregator, timePeriod, null);
    }
    
    public MultiTemporalAggregator(
            TemporalAggregator innerAggregator,
            AggregationTimePeriod timePeriod, Collection<TimeInterval> timePeriods) {
        super(timePeriod, timePeriods);
        
        K = -1;
        switch (timePeriod) {
            case DAILY: {
                switch (innerAggregator.getTimePeriod()) {
                    case MONTHLY:
                        K = 31;
                        break;
                    case YEARLY:
                        K = 366;
                        break;
                }
            }
            case SEASONAL: {
                K = 4;
                break;
            }
            case MONTHLY: {
                K = 12;
            }
            case YEARLY: {
                TimeInterval interval = getTotalTimePeriod();
                if (interval != null){
                    K  = interval.getEnd().get(java.util.Calendar.YEAR)
                       - interval.getStart().get(java.util.Calendar.YEAR);                     
                }
                break;
            }
        }
        if (K == -1) {
            System.out.println("Error unsupported combination of aggregation options!");
        }

        this.innerAggregator = new TemporalAggregator[K];
        for (int k = 0; k < K; k++) {
            this.innerAggregator[k] = innerAggregator.copy();
            this.innerAggregator[k].addConsumer(new MultiTemporalConsumer(k));
        }
        this.timePeriod = timePeriod;
    }
    
    protected MultiTemporalAggregator(MultiTemporalAggregator copy){
        super(copy);
        this.K = copy.K;
        this.innerAggregator = new TemporalAggregator[K];
        for (int k=0;k<K;k++){
            innerAggregator[k] = copy.innerAggregator[k].copy();
        }
    }
    
    @Override
    public void init() {
        for (int k = 0; k < K; k++) {            
            innerAggregator[k].init();
        }
    }
    
    @Override
    public TemporalAggregator<T> copy() {   
        return new MultiTemporalAggregator(this);
    }
    

    private int getIndex(Calendar timeStep) {
        switch (timePeriod) {
            case DAILY: {
                switch (innerAggregator[0].getTimePeriod()) {
                    case MONTHLY:
                        return timeStep.get(Calendar.DAY_OF_MONTH) - 1;
                    case YEARLY:
                        return timeStep.get(Calendar.DAY_OF_YEAR) - 1;
                }
            }
            case SEASONAL: {
                int month = timeStep.get(Calendar.MONTH);
                if (month < 2) {
                    return 0;
                } else if (month < 5) {
                    return 1;
                } else if (month < 8) {
                    return 2;
                } else if (month < 11) {
                    return 3;
                } else if (month < 12) {
                    return 0;
                }
            }

            case MONTHLY: {
                int month = timeStep.get(Calendar.MONTH);
                return month;
            }
            case YEARLY: {      
                TimeInterval interval = getTotalTimePeriod();
                if (interval != null){
                    return currentTimeStep().get(java.util.Calendar.YEAR) - 
                         interval.getStart().get(java.util.Calendar.YEAR);                     
                }                
            }
        }
        return -1;
    }

    @Override
    public void aggregate(Calendar timeStep, T next) {
        int index = getIndex(timeStep);        
        innerAggregator[index].aggregate(timeStep, next);
    }

    //TODO Problem tritt auf, wenn alle Ergebnisse des Finish durch Attribute weiterverarbeitet werden sollen.
    //Das Attribut kann nur ein Ergebnis speichern, aber nicht alle .. 
    //d.h. streng genommen muss für die Anwendung des MTA das Modellierungszeitintervall mit dem äußeren Zeitintervall
    //zusammen fallen.
    @Override
    public void finish() {
        for (int k=0;k<K;k++){
            innerAggregator[k].finish();
        }
        super.finish();
    }
    
    public static void main(String[] args) {
        DoubleArrayAggregator innerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.SUM, 2);        
        TemporalAggregator<double[]> InnerTempAggr = 
                new BasicTemporalAggregator(innerAggr, AggregationTimePeriod.DECADLY);
        
        MultiTemporalAggregator<double[]> OuterTempAggr = 
                new MultiTemporalAggregator<double[]>(InnerTempAggr, AggregationTimePeriod.SEASONAL);
        
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
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        OuterTempAggr.finish();
    }
}
