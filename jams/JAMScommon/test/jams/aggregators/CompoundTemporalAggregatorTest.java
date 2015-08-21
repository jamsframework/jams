/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.aggregators;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import java.util.ArrayList;
import java.util.Arrays;
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
public class CompoundTemporalAggregatorTest {
    
    ArrayList<Attribute.TimeInterval> list = null;
    Attribute.Calendar c = null;
    
    public CompoundTemporalAggregatorTest() {
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
    public void CompoundTemporalAggregatorSumTest(){
        DoubleArrayAggregator innerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.AVERAGE, 2);
        DoubleArrayAggregator outerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.SUM, 2);
        
        
        TemporalAggregator<double[]> InnerTempAggr = 
                new BasicTemporalAggregator(innerAggr, TemporalAggregator.AggregationTimePeriod.DAILY);
                      
        CompoundTemporalAggregator<double[]> tempAggr = 
                new CompoundTemporalAggregator<double[]>(outerAggr, InnerTempAggr, TemporalAggregator.AggregationTimePeriod.YEARLY);
        
        final TreeMap<Attribute.Calendar, double[]> output1 = new TreeMap<Attribute.Calendar, double[]>();
                
        tempAggr.addConsumer(new TemporalAggregator.Consumer<double[]>() {
            @Override
            public void consume(Attribute.Calendar c, double[] v) {
                output1.put(c.getValue(), v.clone());
            }
        });
        
        c.set(2000, 0, 1, 0, 1, 1);
        
        for (int i=0;i<10000;i++){  
            if ( i % 2 == 1){
                tempAggr.aggregate(c, new double[]{1,Double.NaN});
            }else{
                tempAggr.aggregate(c, new double[]{1,i});
            }            
            c.add(Attribute.Calendar.DAY_OF_YEAR, 1);
        }
        tempAggr.finish();  
        //tempAggr.finish();  
        
        assert output1.size() == 28;
        Iterator<double[]> iter1 = output1.values().iterator();
                
        assert iter1.next()[0] == 365;        
    }    
    
    @Test
    public void CompoundTemporalAggregatorCustomYearlyTest(){
        DoubleArrayAggregator innerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.AVERAGE, 3);
        DoubleArrayAggregator outerAggr = DoubleArrayAggregator.create(Aggregator.AggregationMode.SUM, 3);
        
        
        TemporalAggregator<double[]> InnerTempAggr = 
                new BasicTemporalAggregator(innerAggr, TemporalAggregator.AggregationTimePeriod.YEARLY);
                      
        CompoundTemporalAggregator<double[]> tempAggr = 
                new CompoundTemporalAggregator<double[]>(outerAggr, InnerTempAggr, TemporalAggregator.AggregationTimePeriod.CUSTOM, list);
        
        final TreeMap<Attribute.Calendar, double[]> output1 = new TreeMap<Attribute.Calendar, double[]>();
                
        tempAggr.addConsumer(new TemporalAggregator.Consumer<double[]>() {
            @Override
            public void consume(Attribute.Calendar c, double[] v) {
                output1.put(c.getValue(), v.clone());
                System.out.println(c + "\t" + Arrays.toString(v));
            }
        });
        
        c.set(2001, 0, 1, 0, 1, 1);
        
        while(c.get(Attribute.Calendar.YEAR) < 2020){  
            int dayOfYear = c.get(Attribute.Calendar.DAY_OF_YEAR);
            if ( dayOfYear % 2 == 1){
                tempAggr.aggregate(c, new double[]{1,dayOfYear,Double.NaN});
            }else{
                tempAggr.aggregate(c, new double[]{1,dayOfYear,1});
            }            
            c.add(Attribute.Calendar.DAY_OF_YEAR, 1);
        }
        tempAggr.finish();  
        //tempAggr.finish();  
        
        assert output1.size() == 3;
        Iterator<double[]> iter1 = output1.values().iterator();
                
        double first[] = iter1.next();
        assert first[0] == 1;
        assert first[1] == ((31.+28.+31.)*91./2.)/90.;
        
        double second[] = iter1.next();
        assert second[1] == 166.5; //mean from 120 to 211
    }  
}
