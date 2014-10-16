/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.climate;

import jams.component.indices.TemperatureIndices;
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
public class TemperatureIndicesTest {

    TemperatureIndices T = new TemperatureIndices();

    static ArrayList<double[]> JenaClimateData;

    public TemperatureIndicesTest() {
    }

    @BeforeClass
    static public void setUpClass() throws IOException {
        JenaClimateData = new ArrayList<double[]>();
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("resources/temp_jena.dat");
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("#"))
                continue;
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
        T.coolingDegreeDays = DefaultDataFactory.getDataFactory().createDouble();
        T.frostDayThreshold = DefaultDataFactory.getDataFactory().createDouble();
        T.frostDayThreshold.setValue(0.0);
        T.hotDayThreshold = DefaultDataFactory.getDataFactory().createDouble();
        T.hotDayThreshold.setValue(30.0);
        T.iceDayThreshold = DefaultDataFactory.getDataFactory().createDouble();
        T.iceDayThreshold.setValue(0.0);
        T.isBeginningOfHotPeriod = DefaultDataFactory.getDataFactory().createDouble();
        T.isBeginningOfPermanentFrostPeriod = DefaultDataFactory.getDataFactory().createDouble();
        T.isForestVegetationPeriodTrigger = DefaultDataFactory.getDataFactory().createDouble();
        T.isFrostDay = DefaultDataFactory.getDataFactory().createDouble();
        T.isFrostDefrostChange = DefaultDataFactory.getDataFactory().createDouble();
        T.isHeatDay = DefaultDataFactory.getDataFactory().createDouble();
        T.isHotDay = DefaultDataFactory.getDataFactory().createDouble();
        T.isHotPeriod = DefaultDataFactory.getDataFactory().createDouble();
        T.isIceDay = DefaultDataFactory.getDataFactory().createDouble();
        T.isPermanentFrostPeriod = DefaultDataFactory.getDataFactory().createDouble();
        T.isSummerDay = DefaultDataFactory.getDataFactory().createDouble();
        T.isTempBelowZero = DefaultDataFactory.getDataFactory().createDouble();
        T.isThermalVegetationPeriodTrigger = DefaultDataFactory.getDataFactory().createDouble();
        T.isThermalVegetationPeriodTrigger2 = DefaultDataFactory.getDataFactory().createDouble();
        T.isTouristDay = DefaultDataFactory.getDataFactory().createDouble();
        T.permanentFrostDayThreshold = DefaultDataFactory.getDataFactory().createDouble();
        T.permanentFrostDayThreshold.setValue(-5);
        T.successiveHotDays = DefaultDataFactory.getDataFactory().createDouble();
        T.successivePermanentFrostDay = DefaultDataFactory.getDataFactory().createDouble();
        T.summerDayThreshold = DefaultDataFactory.getDataFactory().createDouble();
        T.summerDayThreshold.setValue(25);
        T.time = DefaultDataFactory.getDataFactory().createCalendar();
        T.tmax = DefaultDataFactory.getDataFactory().createDouble();
        T.tmean = DefaultDataFactory.getDataFactory().createDouble();
        T.tmin = DefaultDataFactory.getDataFactory().createDouble();
        T.tmp = DefaultDataFactory.getDataFactory().createDoubleArray();
        T.tropicalNightThreshold = DefaultDataFactory.getDataFactory().createDouble();
        T.tropicalNightThreshold.setValue(20);
        T.isTropicalNight = DefaultDataFactory.getDataFactory().createDouble();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void TemperaturIndiciesTest() throws Exception{

        int summerDays = 0, hotDays = 0, frostDays = 0, iceDays = 0, tropicalNights=0;
        int successiveHotDays = 0, hotPeriods = 0, permanentFrostPeriods = 0;
        T.init();
        
        for (double dataset[] : JenaClimateData) {
            double tmin = dataset[5];
            double tmean = dataset[4];
            double tmax = dataset[3];
            int year = (int) dataset[2];
            int month = (int) dataset[1];
            int day = (int) dataset[0];
            T.time.set(year, month-1, day, 0, 0, 0);
            T.tmax.setValue(tmax);
            T.tmean.setValue(tmean);
            T.tmin.setValue(tmin);

            T.run();

            summerDays += (int) T.isSummerDay.getValue();
            hotDays += (int) T.isHotDay.getValue();
            frostDays += (int) T.isFrostDay.getValue();
            iceDays += (int) T.isIceDay.getValue();
            tropicalNights += (int)T.isTropicalNight.getValue();
            successiveHotDays += (int)T.successiveHotDays.getValue();
            hotPeriods += (int)T.isBeginningOfHotPeriod.getValue();
            permanentFrostPeriods += (int)T.isBeginningOfPermanentFrostPeriod.getValue();
        }

        assert summerDays == 2393+hotDays;
        assert hotDays == 730;
        assert frostDays == 4609;
        assert iceDays == 1113;
        assert tropicalNights == 11;
        assert successiveHotDays == 1529;
        assert hotPeriods == 20;
        assert permanentFrostPeriods == 189;
    }
}
