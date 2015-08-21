/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import cern.colt.Arrays;
import optas.data.api.DataView;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import optas.data.time.api.TimeSerie;
import optas.data.view.AbstractListView;
import optas.io.J2KTimeSerie;
import optas.io.J2KTimeSerieReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class TimeSerieTest {

    DataView<Double> values = new AbstractListView<Double, Double>(0.){

            @Override
            public Double getValue(int i) {
                return (double)i;
            }

            @Override
            public int getSize() {
                return 31;
            }

            @Override
            public Double setValue(int i, Double value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        };  
    
    DataView<Date> dates = new AbstractListView<Date, Double>(0.){

            @Override
            public Date getValue(int i) {
                return new Date(100000L + 10000000000L*i);
            }

            @Override
            public int getSize() {
                return 31;
            }

            @Override
            public Date setValue(int i, Date value) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        };  
    
    public TimeSerieTest() {
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
    public void RegularTimeSerieCreation() {
        TimeInterval ti = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti.getStart().set(2000, 0, 1, 0, 0, 0);
        ti.getEnd().set(2000, 0, 31, 0, 0, 0);
        
        TimeSerie<Double> s = new DefaultTimeSerie<>("Regular TimeSerie", ti, values);
 
        System.out.println("Writing day timeserie for Jan 2000");
        for (Entry<Date, Double> e : s.map()){
            System.out.println(e.toString());
        }
        
        TimeInterval ti2 = DefaultDataFactory.getDataFactory().createTimeInterval();
        ti2.getStart().set(2000, 0, 1, 0, 0, 0);
        ti2.getEnd().set(2000, 0, 10, 0, 0, 0);        
        
        System.out.println("Writing day timeserie for Jan 01 - 10. 2000");
        s = s.filter(TimeFilterFactory.getRangeFilter(ti2));
        assert s.getNumberOfTimesteps() == 10;
        for (Entry<Date, Double> e : s.map()){
            assert s.map().getSize() == 10;
            System.out.println(e.toString());
        }
    }
    
    @Test
    public void IrregularTimeSerieCreation() {                            
        TimeSerie<Double> s = new DefaultTimeSerie<>("Regular TimeSerie", dates, values);
 
        System.out.println("Writing irregular timeserie");
        for (Entry<Date, Double> e : s.map()){
            System.out.println(e.toString());
        }
                   
        s = s.filter(TimeFilterFactory.getYearlyFilter(new int[]{1975}, true));
        assert s.getNumberOfTimesteps() == 3;
        for (Entry<Date, Double> e : s.map()){
            assert s.map().getSize() == 3;
            System.out.println(e.toString());
        }
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    
    @Test
    public void RegularTimeSerieCreationFromFile() throws IOException {
        //InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("resources/temp_jena.dat");
        J2KTimeSerieReader reader = new J2KTimeSerieReader(new File(ClassLoader.getSystemClassLoader().getResource("resources/tmean.dat").getFile()));
        J2KTimeSerie ts = reader.getData();
        for (Entry<Date, double[]> e : ts.map()){
            System.out.println(e.getKey() + ":" + Arrays.toString(e.getValue()));
        }
    }
}
