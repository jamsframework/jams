/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.calculations;

import java.util.ArrayList;
import optas.data.DataSet.MismatchException;
import optas.data.TimeSerie;

/**
 *
 * @author chris
 */
public class BaseFlow {

    //this is adapted from local minimum method of hysep
    public static double[] groundwaterWindowMethod(TimeSerie hydrograph) {
        return groundwaterWindowMethod(hydrograph, 150);
    }
    public static double[] groundwaterWindowMethod(TimeSerie hydrograph, int winSize) {
        int t = (int) hydrograph.getTimeDomain().getNumberOfTimesteps();

        double filteredSerie[] = new double[t];
//default 150
        //int winSize = 30;

        ArrayList<Integer> minList = new ArrayList<Integer>();

        int oldMin = -1;

        for (int c = 0; c < t; c++) {
            double winMin = Double.POSITIVE_INFINITY;
            int winArgMin = 0;
            for (int d = c; d < Math.min(c + winSize, t); d++) {
                if (hydrograph.getValue(d) < winMin) {
                    winMin = hydrograph.getValue(d);
                    winArgMin = d;
                }
            }
            if (winArgMin != oldMin) {
                minList.add(new Integer(winArgMin));
                oldMin = winArgMin;
            }
        }
        int firstIndex = minList.get(0);
        int lastIndex = minList.get(minList.size() - 1);
        double firstValue = hydrograph.getValue(firstIndex);
        double lastValue = hydrograph.getValue(lastIndex);
        for (int init = 0; init < firstIndex; init++) {
            filteredSerie[init] = firstValue;
        }
        for (int i = 0; i < minList.size() - 1; i++) {
            int index1 = minList.get(i);
            double value1 = hydrograph.getValue(index1);
            int index2 = minList.get(i + 1);
            double value2 = hydrograph.getValue(index2);

            double d = index2 - index1;
            for (int j = index1; j < index2; j++) {
                filteredSerie[j] = value1 * (index2 - j) / d + value2 * (j - index1) / d;
            }
        }
        for (int post = lastIndex; post < t; post++) {
            filteredSerie[post] = lastValue;
        }
        return filteredSerie;
    }

    public static TimeSerie calculateGroundwater(TimeSerie hydrograph) {
        return calculateGroundwater(hydrograph, 150);
    }
    public static TimeSerie calculateGroundwater(TimeSerie hydrograph, int windowSize) {
        try {
            TimeSerie t = new TimeSerie(groundwaterWindowMethod(hydrograph, windowSize), hydrograph.getTimeDomain(), "groundwater", null);
            return t;
        } catch (MismatchException e) {
            System.out.println(e);
        }
        return null;
    }

    public static ArrayList<HydrographSection> calculateBaseFlowPeriods(TimeSerie hydrograph, double threshold) {
        return calculateBaseFlowPeriods(hydrograph, threshold, 150);
    }
    public static ArrayList<HydrographSection> calculateBaseFlowPeriods(TimeSerie hydrograph, double threshold, int windowSize) {
        ArrayList<HydrographSection> list = new ArrayList<HydrographSection>();
        int n = (int) hydrograph.getTimeDomain().getNumberOfTimesteps();
        TimeSerie groundwater = calculateGroundwater(hydrograph, windowSize);
        int i = 0;
        while (i < n) {
            if (Math.abs(hydrograph.getValue(i) - groundwater.getValue(i)) < threshold) {
                HydrographSection sec = new HydrographSection(i, hydrograph.getValue(i));
                i++;
                while (i < n && Math.abs(hydrograph.getValue(i) - groundwater.getValue(i)) < threshold) {
                    sec.add(hydrograph.getValue(i));
                    i++;
                }
                list.add(sec);
            }
            i++;
        }
        return list;
    }
}
