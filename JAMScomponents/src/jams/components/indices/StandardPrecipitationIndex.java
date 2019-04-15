/*
 * StandardPrecipitationIndex.java
 * Created on 06.03.2019, 11:50:58
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.indices;

import jams.JAMS;
import jams.components.aggregate.TSAggregator;
import jams.data.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sven Kralisch <sven.kralisch@uni-jena.de>
 *
 * based on SPI Generator v1.7.5 National Drought Mitigation Center - UNL
 * 11/29/2018
 *
 */
public class StandardPrecipitationIndex {
    
    public static final double MISSING_DATA_VALUE = -9999;

    public static double[] parse(double[] precipitation) {

        double[] spi = new double[0];

        // check input data
        if (precipitation != null && precipitation.length > 0) {

            GammaDistributionEstimatedProbabilities.Parameters p = new GammaDistributionEstimatedProbabilities.Parameters();

            GammaDistributionEstimatedProbabilities.gamma_fit(precipitation, p);

            spi = new double[precipitation.length];

            // replace precipitation sums with SPI's 
            for (int i = 0; i < precipitation.length; i++) {

                double precipValue = precipitation[i];
                if (precipValue != JAMS.getMissingDataValue()) {

                    // get probability 
                    spi[i] = GammaDistributionEstimatedProbabilities.gamma_cdf(p.beta, p.gamma, p.pzero, precipValue);

                    // convert probability to z value
                    spi[i] = GammaDistributionEstimatedProbabilities.inv_normal(spi[i]);

                } else {
                    spi[i] = JAMS.getMissingDataValue();
                }
            }
        }
        return spi;
    }

    private static double[][] toMonthlyGrouped(double[] a) {

        int maxLength = (int) Math.ceil(a.length / 12f);
        int minLength = a.length / 12;
        int mod = a.length % 12;

        double[][] b = new double[12][];
        for (int i = 0; i < mod; i++) {
            b[i] = new double[maxLength];
        }
        for (int i = mod; i < b.length; i++) {
            b[i] = new double[minLength];
        }

        for (int i = 0; i < a.length; i++) {
            int m = i % 12;
            int n = i / 12;
            b[m][n] = a[i];
        }

        return b;
    }

    public static double[] calcSPI(double[] monthlyValues) {
        double[] a = monthlyValues;
        double[][] groups = toMonthlyGrouped(a);
        for (int i = 0; i < groups.length; i++) {
            groups[i] = parse(groups[i]);
        }
        a = new double[a.length];
        int c = 0;
        int minLength = groups[0].length - 1;
        for (int i = 0; i < minLength; i++) {
            for (int j = 0; j < groups.length; j++) {
                a[c++] = groups[j][i];
            }
        }
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].length > minLength) {
                a[c++] = groups[i][minLength];
            }
        }
        return a;
    }
    
    private static double[] getEmptyArray(int n) {
        double[] a = new double[n];
        for (int i = 0; i < a.length; i++) {
            a[i] = MISSING_DATA_VALUE;
        }
        return a;
    }

    public static double[] calcSPIn(double[] monthlyValues, int n) {

        double[] a = monthlyValues;
        
        if (a.length - n + 1 < 1) {
            return getEmptyArray(monthlyValues.length);
        }
        
        double[] m = new double[a.length - n + 1];

        for (int i = n - 1; i < a.length; i++) {
            double sum = 0;
            for (int j = i - n + 1; j <= i; j++) {
                sum += a[j];
            }
            m[i - n + 1] = sum /= n;
        }

        m = calcSPI(m);

        for (int i = 0; i < n - 1; i++) {
            a[i] = MISSING_DATA_VALUE;
        }
        for (int i = n - 1; i < a.length; i++) {
            a[i] = m[i - n + 1];
        }

        return a;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {

//        BufferedReader r = new BufferedReader(new FileReader("D:\\temp\\SPI\\commadelimited_monthly.csv"));
//        r.readLine();
//        r.readLine();
//        String s;
//        List<Double> list = new ArrayList();
//        while ((s = r.readLine()) != null) {
//            list.add(Double.parseDouble(s));
//        }
//        double[] data = new double[list.size()];
//        for (int i = 0; i < data.length; i++) {
//            data[i] = list.get(i);
//        }
//        for (double d : parse(data)) {
//            System.out.println(d);
//        }
        BufferedReader r = new BufferedReader(new FileReader("D:\\temp\\SPI\\commadelimited_daily_wdates.csv"));
//        BufferedReader r = new BufferedReader(new FileReader("D:\\temp\\SPI\\commadelimited_monthly_wdates.csv"));
        r.readLine();
        String s;
        List<Attribute.Calendar> dates = new ArrayList();
        Attribute.Calendar c = JAMSDataFactory.createCalendar();
        c.set(1913, 0, 1, 0, 0, 0);
//        c.set(1936, 0, 1, 0, 0, 0);
        List<Double> list = new ArrayList();
        while ((s = r.readLine()) != null) {
            s = s.split(",")[1];
            list.add(Double.parseDouble(s));
            dates.add(c.clone());
            c.add(Attribute.Calendar.DAY_OF_YEAR, 1);
//            c.add(Attribute.Calendar.MONTH, 1);
        }
        double[] data = new double[list.size()];
        for (int i = 0; i < data.length; i++) {
            data[i] = list.get(i);
        }

        TSAggregator agg = new TSAggregator(data, dates, 0);
        double[] a = agg.toMonthly().values;

        a = calcSPIn(a, 12);

        for (double d : a) {
            System.out.println(d);
        }

//        double[][] groups = toMonthlyGrouped(a);
//
//        for (int i = 0; i < groups.length; i++) {
//            groups[i] = parse(groups[i]);
//        }
//
//        for (int i = 0; i < groups[0].length - 1; i++) {
//            for (int j = 0; j < groups.length; j++) {
//                System.out.println(groups[j][i]);
//            }
//        }
//        for (int j = 0; j < groups.length; j++) {
//            System.out.println(groups[j][groups[j].length - 1]);
//        }
//
////        for (double d : parse(data)) {
////            System.out.println(d);
////        }
    }
}
