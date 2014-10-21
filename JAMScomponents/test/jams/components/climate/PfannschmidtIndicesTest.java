/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.climate;

import jams.aggregators.Aggregator;
import jams.aggregators.BasicTemporalAggregator;
import jams.aggregators.CompoundTemporalAggregator;
import jams.aggregators.DoubleAggregator;
import jams.aggregators.TemporalAggregator;
import jams.components.indices.PfannschmidtIndices;
import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author christian
 */
public class PfannschmidtIndicesTest {

    PfannschmidtIndices T = new PfannschmidtIndices();

    static ArrayList<double[]> JenaClimateData;

    public PfannschmidtIndicesTest() {
    }

    @BeforeClass
    static public void setUpClass() throws IOException {
        JenaClimateData = new ArrayList<double[]>();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("resources/temp_jena.dat");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            String substrings[] = line.split("\t");
            double subData[] = new double[6];
            for (int i = 0; i < substrings.length; i++) {
                subData[i] = Double.parseDouble(substrings[i]);
            }
            JenaClimateData.add(subData);
        }
        in.close();
    }

    @Before
    public void setUp() {
        T.histogrammCalculation = DefaultDataFactory.getDataFactory().createBoolean();
        T.histogrammCalculation.setValue(true);
        T.time = DefaultDataFactory.getDataFactory().createCalendar();
        T.tmax = DefaultDataFactory.getDataFactory().createDouble();
        T.tmean = DefaultDataFactory.getDataFactory().createDouble();
        T.tmin = DefaultDataFactory.getDataFactory().createDouble();
        T.internalState = DefaultDataFactory.getDataFactory().createObject();
        T.summerIndex = DefaultDataFactory.getDataFactory().createDouble();
        T.winterIndex = DefaultDataFactory.getDataFactory().createDouble();
        T.summerStartDay = DefaultDataFactory.getDataFactory().createDouble();
        T.winterStartDay = DefaultDataFactory.getDataFactory().createDouble();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    double summerIndex = 0, winterIndex = 0;
    double summerStartDay = 0, winterStartDay = 0;       
    double tmp2 = -999;
    double tmp4 = -999;
    int counter1 = 0, counter2 = 0;
    @Test
    public void TemperaturIndiciesTest() throws Exception {
        summerIndex = 0; winterIndex = 0;
        summerStartDay = 0; winterStartDay = 0;       
        tmp2 = -999;
        tmp4 = -999;
        counter1 = 0; counter2 = 0;
        
        T.init();
               
        CompoundTemporalAggregator<Double> summerAggregator1 = new CompoundTemporalAggregator<Double>(DoubleAggregator.create(Aggregator.AggregationMode.AVERAGE), 
                new BasicTemporalAggregator<Double>(DoubleAggregator.create(Aggregator.AggregationMode.LAST), TemporalAggregator.AggregationTimePeriod.YEARLY), TemporalAggregator.AggregationTimePeriod.YEARLY);
        
        CompoundTemporalAggregator<Double> summerAggregator2 = new CompoundTemporalAggregator<Double>(DoubleAggregator.create(Aggregator.AggregationMode.AVERAGE), 
                new BasicTemporalAggregator<Double>(DoubleAggregator.create(Aggregator.AggregationMode.LAST), TemporalAggregator.AggregationTimePeriod.YEARLY), TemporalAggregator.AggregationTimePeriod.YEARLY);
        
        summerAggregator1.addConsumer(new TemporalAggregator.Consumer<Double>() {

            @Override
            public void consume(Attribute.Calendar c, Double v) {
                if (v != 0){
                    summerIndex += v;
                    counter1++;
                }
            }
        });
        
        summerAggregator2.addConsumer(new TemporalAggregator.Consumer<Double>() {

            @Override
            public void consume(Attribute.Calendar c, Double v) {
                if (v != 0){
                    summerStartDay += v;
                }
            }
        });
        
        for (double dataset[] : JenaClimateData) {
            double tmin = dataset[5];
            double tmean = dataset[4];
            double tmax = dataset[3];
            int year = (int) dataset[2];
            int month = (int) dataset[1];
            int day = (int) dataset[0];
            T.time.set(year, month - 1, day, 0, 0, 0);
            T.tmax.setValue(tmax);
            T.tmean.setValue(tmean);
            T.tmin.setValue(tmin);

            T.run();
                       
            if (T.summerStartDay.getValue() != 0) {
                summerAggregator1.aggregate(T.time, T.summerIndex.getValue());
                summerAggregator2.aggregate(T.time, T.summerStartDay.getValue());
            }

            if (T.winterStartDay.getValue() == 0) {
                if (tmp2 != -999) {
                    winterIndex += tmp2;
                    winterStartDay += tmp4;
                    counter2++;
                }
                tmp2 = -999;
            } else {
                tmp2 = T.winterIndex.getValue();
                tmp4 = T.winterStartDay.getValue();
            }
        }
        summerAggregator1.finish();
        summerAggregator2.finish();
                
        winterIndex += tmp2;
        winterStartDay += tmp4;
        counter2++;

        summerIndex /= counter1;
        winterIndex /= counter2;

        assert summerStartDay == 7682;
        assert winterStartDay == 18885;
        assert Math.abs(summerIndex - 22.846) < 0.01;
        assert Math.abs(winterIndex - -1.857) < 0.01;
        //assert winterIndex == 730;        
    }
}
