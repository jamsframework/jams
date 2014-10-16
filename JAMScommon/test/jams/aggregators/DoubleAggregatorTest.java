
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.aggregators;

import jams.aggregators.Aggregator.AggregationMode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author christian
 */
public class DoubleAggregatorTest {
    
    public DoubleAggregatorTest() {
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
    public void DoubleAggregatorSumTest() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.SUM);
        t.init();
        for (int i = 0; i <= 10; i++) {
            t.consider((double)(i*i));
        }
        t.finish();
        t.finish();
        assert t.get() == 385;
    }
    @Test
    public void DoubleAggregatorSumTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.SUM);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider((double)(i*i));
            else
                t.consider(Double.NaN);
        }
        t.finish();
        t.finish();
        assert t.get() == (0+2*2+4*4+6*6+8*8+10*10);
    }
    @Test
    public void DoubleAggregatorAverageTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.AVERAGE);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider((double)(i*i));
            else
                t.consider(Double.NaN);
        }
        t.finish();
        t.finish();
        assert t.get() == (0+2*2+4*4+6*6+8*8+10*10)/6.0;
    }
    @Test
    public void DoubleAggregatorMinimumTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.MINIMUM);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider((double)((i-5)*(i-5)));
            else
                t.consider(Double.NaN);
        }
        t.finish();
        t.finish();
        assert t.get() == 1;
    }
    @Test
    public void DoubleAggregatorMaximumTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.MAXIMUM);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider((double)((i-5)*(i-5)));
            else
                t.consider(Double.NaN);
        }
        t.finish();
        t.finish();
        assert t.get() == 25;
    }
    @Test
    public void DoubleAggregatorVarianceTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.VARIANCE);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider((double)i);
            else
                t.consider(Double.NaN);
        }
        t.finish();
        t.finish();
        double mw = (0+2+4+6+8+10)/6;
        assert t.get() == ((0-mw)*(0-mw) + (2-mw)*(2-mw) + (4-mw)*(4-mw) + (6-mw)*(6-mw) + (8-mw)*(8-mw) + (10-mw)*(10-mw))/5;
    }
    @Test
    public void DoubleAggregatorLastTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.LAST);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider((double)i);
            else
                t.consider(Double.NaN);
        }
        t.finish();     
        t.finish();      
        assert t.get() == 10;
    }
    @Test
    public void DoubleAggregatorAvgNullTestWithNaN() {
        DoubleAggregator t = DoubleAggregator.create(AggregationMode.AVERAGE);
        t.init();        
        t.finish();
        t.finish();       
        assert Double.isNaN(t.get());
    }
}
