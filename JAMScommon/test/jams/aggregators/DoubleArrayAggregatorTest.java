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
public class DoubleArrayAggregatorTest {
    
    public DoubleArrayAggregatorTest() {
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
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.SUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            t.consider(new double[]{i, i*i});            
        }
        t.finish();
        t.finish();
        assert t.get()[0] == (10*10+10)/2 && t.get()[1] == 385;
    }
    @Test
    public void DoubleArrayAggregatorSumTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.SUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(new double[]{i, i*i});            
            else
                t.consider(new double[]{Double.NaN, i*i});            
        }
        t.finish();
        t.finish();
        assert t.get()[0] == (0+2+4+6+8+10) && t.get()[1] == 385;
    }
    @Test
    public void DoubleArrayAggregatorAverageTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.AVERAGE, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(new double[]{i, i*i});            
            else
                t.consider(new double[]{Double.NaN, i*i});            
        }
        t.finish();
        t.finish();
        assert t.get()[0] == (0+2+4+6+8+10)/6 && t.get()[1] == 385/11.;
    }
    @Test
    public void DoubleArrayAggregatorMinimumTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.MINIMUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(new double[]{i, (i-5)*(i-5)});            
            else
                t.consider(new double[]{Double.NaN, (i-5)*(i-5)});            
        }
        t.finish();
        t.finish();
        assert t.get()[0] == 0 && t.get()[1] == 0;
    }
    @Test
    public void DoubleArrayAggregatorMaximumTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.MAXIMUM, 2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(new double[]{i, (i-5)*(i-5)});            
            else
                t.consider(new double[]{Double.NaN, (i-5)*(i-5)});            
        }
        t.finish();
        t.finish();
        assert t.get()[0] == 10 && t.get()[1] == 25;
    }
    @Test
    public void DoubleArrayAggregatorVarianceTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.VARIANCE,1);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i % 2 == 0)
                t.consider(new double[]{i});
            else
                t.consider(new double[]{Double.NaN});
        }
        t.finish();
        double mw = (0+2+4+6+8+10)/6;
        assert t.get()[0] == ((0-mw)*(0-mw) + (2-mw)*(2-mw) + (4-mw)*(4-mw) + (6-mw)*(6-mw) + (8-mw)*(8-mw) + (10-mw)*(10-mw))/5;
    }
    @Test
    public void DoubleArrayAggregatorLastTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.LAST,2);
        t.init();
        for (int i = 0; i <= 10; i++) {
            if (i%2 == 0)
                t.consider(new double[]{i, (i-5)*(i-5)});            
            else
                t.consider(new double[]{Double.NaN, (i-5)*(i-5)});            
        }
        t.finish();
        t.finish();
        assert t.get()[0] == 10 && t.get()[1] == 25;
    }
    @Test
    public void DoubleAggregatorAvgNullTestWithNaN() {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(AggregationMode.AVERAGE,5);
        t.init();        
        t.finish(); 
        t.finish();  
        assert Double.isNaN(t.get()[0]) && Double.isNaN(t.get()[1]);
    }
}
