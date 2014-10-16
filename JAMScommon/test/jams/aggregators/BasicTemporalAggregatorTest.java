/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.aggregators;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author christian
 */
public class BasicTemporalAggregatorTest {
    
    ArrayList<Attribute.TimeInterval> list = null;
    Attribute.Calendar c = null;
    
    public BasicTemporalAggregatorTest() {
    }
    
    @Before
    public void setUp() {
        list = new ArrayList<Attribute.TimeInterval>();
        Attribute.TimeInterval ti1 = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti1.getStart().set(2001, 0, 1, 0, 1, 1);
        ti1.getEnd().set(2001, 2, 31, 0, 1, 1);
        
        Attribute.TimeInterval ti2 = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti2.getStart().set(2001, 4, 1, 0, 1, 1);
        ti2.getEnd().set(2001, 6, 31, 0, 1, 1);
        
        Attribute.TimeInterval ti3 = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti3.getStart().set(2001, 7, 1, 0, 1, 1);
        ti3.getEnd().set(2001, 10, 30, 0, 1, 1);
        
        list.add(ti1);
        list.add(ti2);
        list.add(ti3);
        
        c = DefaultDataFactory.getDataFactory().createCalendar();
        c.set(2001, 0, 1, 0, 1, 1);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    //@Test
    public void BasicTemporalAggregatorSumTest(){
        DoubleIteratorAggregator aggr = DoubleIteratorAggregator.create(Aggregator.AggregationMode.SUM, 2);                        
        TemporalAggregator<Iterable<Double>> tempAggr = new BasicTemporalAggregator<Iterable<Double>>(aggr, TemporalAggregator.AggregationTimePeriod.YEARLY, null);
                        
        final TreeMap<Attribute.Calendar, Double> output1 = new TreeMap<Attribute.Calendar, Double>();
        final TreeMap<Attribute.Calendar, Double> output2 = new TreeMap<Attribute.Calendar, Double>();
        
        tempAggr.addConsumer(new TemporalAggregator.Consumer<Iterable<Double>>() {
            @Override
            public void consume(Attribute.Calendar c, Iterable<Double> v) {
                Iterator<Double> iter = v.iterator();
                output1.put(c, iter.next());
                output2.put(c, iter.next());
            }
        });
        
        c.set(2001, 2, 1, 0, 1, 1);
        LinkedList<Double> input = new LinkedList<Double>();
        for (int i=0;i<10000;i++){  
            input.clear();
            input.add(1.);
            if ( i % 2 == 1){
                input.add(Double.NaN);
            }else{
                input.add((double)i);
            }
            tempAggr.aggregate(c, input);
            c.add(Attribute.Calendar.DAY_OF_YEAR, 1);
        }
        tempAggr.finish();  
        tempAggr.finish();  
        
        assert output1.size() == 28 && output2.size() == 28;
        Iterator<Double> iter1 = output1.values().iterator();
        Iterator<Double> iter2 = output2.values().iterator();
        
        assert iter1.next() == 365-59;
        assert iter1.next() == 365;
        assert iter1.next() == 365;
        assert iter1.next() == 366;
        
        assert iter2.next() == 152*153;
        assert iter2.next() == 335*336-152*153;
    }
    
    @Test
    public void BasicTemporalAggregatorCustomTimeIntervalSumTest(){
        DoubleIteratorAggregator aggr = DoubleIteratorAggregator.create(Aggregator.AggregationMode.SUM, 2);                        
        TemporalAggregator<Iterable<Double>> tempAggr = new BasicTemporalAggregator<Iterable<Double>>(aggr, TemporalAggregator.AggregationTimePeriod.CUSTOM, list);
                        
        final TreeMap<Attribute.Calendar, Double> output1 = new TreeMap<Attribute.Calendar, Double>();
                
        tempAggr.addConsumer(new TemporalAggregator.Consumer<Iterable<Double>>() {
            @Override
            public void consume(Attribute.Calendar c, Iterable<Double> v) {
                Iterator<Double> iter = v.iterator();
                output1.put(c, iter.next());
            }
        });
        
        c.set(2001, 0, 1, 0, 1, 1);
        LinkedList<Double> input = new LinkedList<Double>();
        for (int i=0;i<10000;i++){  
            if (c.get(Attribute.Calendar.MONTH) == 3 && c.get(Attribute.Calendar.DAY_OF_MONTH) == 30){
                c = c.clone();
            }
            input.clear();
            input.add(1.);            
            tempAggr.aggregate(c, input);
            c.add(Attribute.Calendar.DAY_OF_YEAR, 1);            
        }
        tempAggr.finish();
        tempAggr.finish();  
        
        assert output1.size() == 3;
        Iterator<Double> iter1 = output1.values().iterator();
                
        assert iter1.next() == 90;
        assert iter1.next() == 92;
        assert iter1.next() == 122;                
    }
    
    //@Test
    public void BasicTemporalAggregatorCustomTimeIntervalSumTest_Overlapping(){
        DoubleIteratorAggregator aggr = DoubleIteratorAggregator.create(Aggregator.AggregationMode.SUM, 2);                        
        list.get(1).getStart().set(2001, 2, 0, 0, 1, 1);
        try{
        TemporalAggregator<Iterable<Double>> tempAggr = new BasicTemporalAggregator<Iterable<Double>>(aggr, TemporalAggregator.AggregationTimePeriod.CUSTOM, list);
        }catch(IllegalArgumentException iae){
            return;
        }
        //if this line is reached overlapping is not working correctly
        assert true == false;        
    }
}
