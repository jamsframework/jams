/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.calculations;

import java.util.ArrayList;
import java.util.TreeSet;
import optas.data.TimeSerie;

/**
 *
 * @author chris
 */
public class Peak implements Comparable {

    public int index;
    public double value;

    public double heightAboveBaseLine;
    public double width;

    static final int Window = 10;

    

    public Peak(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int compareTo(Object obj) {
        if (obj instanceof Peak) {
            Peak p2 = (Peak) obj;
            if (value < p2.value) {
                return 1;
            } else if (value > p2.value) {
                return -1;
            } else if (index < p2.index) {
                return 1;
            } else if (index > p2.index) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    

    public static ArrayList<Peak> findPeaks(TimeSerie hydrograph) {
        TreeSet<Peak> peakList = new TreeSet<Peak>();

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();


        for (int i = 1; i < (int) n - 1; i++) {

            double v1 = hydrograph.getValue(i - 1);
            double v2 = hydrograph.getValue(i);
            double v3 = hydrograph.getValue(i + 1);
            if (v1 < v2 && v3 < v2) {

                peakList.add(new Peak(i, v2));
            }
        }

        return new ArrayList<Peak>(peakList);
    }
}
