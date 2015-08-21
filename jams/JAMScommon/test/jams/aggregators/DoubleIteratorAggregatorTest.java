/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.aggregators;

import jams.aggregators.Aggregator.AggregationMode;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author christian
 */
public class DoubleIteratorAggregatorTest {
    
    public DoubleIteratorAggregatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void DoubleArrayAggregatorSumTest() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.SUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            t.consider(Arrays.asList(new Double[]{(double)i, (double)i*i}));            
        }
        t.finish();
        t.finish();
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == (10*10+10)/2 && iter.next() == 385;
    }
    @Test
    public void DoubleArrayAggregatorSumTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.SUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(Arrays.asList(new Double[]{(double)i, (double)i*i}));                     
            else
                t.consider(Arrays.asList(new Double[]{Double.NaN, (double)i*i}));            
        }
        t.finish();
        t.finish();
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == (0+2+4+6+8+10) && iter.next() == 385;
    }
    @Test
    public void DoubleArrayAggregatorAverageTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.AVERAGE, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(Arrays.asList(new Double[]{(double)i, (double)i*i}));                     
            else
                t.consider(Arrays.asList(new Double[]{Double.NaN, (double)i*i}));            
        }
        t.finish();
        t.finish();
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == (0+2+4+6+8+10)/6 && iter.next() == 385/11.;
    }
    
    @Test
    public void DoubleArrayAggregatorMinimumTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.MINIMUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(Arrays.asList(new Double[]{(double)i, (double)(i-5)*(i-5)}));                     
            else
                t.consider(Arrays.asList(new Double[]{Double.NaN, (double)(i-5)*(i-5)}));            
        }
        t.finish();
        t.finish();
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == 0 && iter.next() == 0;
    }
    @Test
    public void DoubleArrayAggregatorMaximumTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.MAXIMUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(Arrays.asList(new Double[]{(double)i, (double)(i-5)*(i-5)}));                     
            else
                t.consider(Arrays.asList(new Double[]{Double.NaN, (double)(i-5)*(i-5)}));            
        }
        t.finish();
        t.finish();
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == 10 && iter.next() == 25;
    }
    @Test
    public void DoubleArrayAggregatorVarianceTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.VARIANCE, 1);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider(Arrays.asList(new Double[]{(double)i}));
            else
                t.consider(Arrays.asList(new Double[]{Double.NaN}));
        }
        t.finish();
        double mw = (0+2+4+6+8+10)/6;
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == ((0-mw)*(0-mw) + (2-mw)*(2-mw) + (4-mw)*(4-mw) + (6-mw)*(6-mw) + (8-mw)*(8-mw) + (10-mw)*(10-mw))/5;
    }
    @Test
    public void DoubleArrayAggregatorLastTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.LAST, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(Arrays.asList(new Double[]{(double)i, (double)(i-5)*(i-5)}));                     
            else
                t.consider(Arrays.asList(new Double[]{Double.NaN, (double)(i-5)*(i-5)}));            
        }
        t.finish();
        t.finish();
        Iterator<Double> iter = t.get().iterator();
        assert iter.next() == 10 && iter.next() == 25;
    }
    @Test
    public void DoubleAggregatorAvgNullTestWithNaN() {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(AggregationMode.AVERAGE,5);
        t.init();        
        t.finish(); 
        t.finish();  
        Iterator<Double> iter = t.get().iterator();
        while(iter.hasNext()){
            assert Double.isNaN(iter.next());
        }
    }
}
