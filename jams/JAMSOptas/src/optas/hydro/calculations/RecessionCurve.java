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
public class RecessionCurve extends HydrographSection implements Comparable {

    public RecessionCurve(int startIndex, double value) {
        super(startIndex, value);
    }

    private double getAmount() {
        return value.get(0) - value.get(endIndex - startIndex - 1);
    }

    @Override
    public int compareTo(Object obj) {
        if (!(obj instanceof RecessionCurve)) {
            return 0;
        }
        RecessionCurve r = (RecessionCurve) obj;
        if (r.getIntervalLength() < this.getIntervalLength()) {
            return -1;
        } else if (r.getIntervalLength() > this.getIntervalLength()) {
            return 1;
        } else {
            if (r.getAmount() < this.getAmount()) {
                return -1;
            } else if (r.getAmount() < this.getAmount()) {
                return 1;
            } else {
                if (r.startIndex < this.startIndex) {
                    return -1;
                } else if (r.startIndex < this.startIndex) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    public static ArrayList<RecessionCurve> findRecessionCurves(TimeSerie hydrograph) {
        TreeSet<RecessionCurve> curveList = new TreeSet<RecessionCurve>();

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();

        double filterResponse[] = new double[(int) n];

        int windowSize = 5;
        double weights[] = {0.15, 0.15, 0.4, 0.15, 0.15};

        for (int i = 2; i < n - 2; i++) {
            int counter = 0;
            for (int j = -(windowSize - 1) / 2; j < (windowSize / 2); j++) {
                filterResponse[i] += weights[counter] * hydrograph.getValue(i + j);
                counter++;
            }
        }

        int i = 2;
        while (i < n - 2) {
            double v1 = filterResponse[i];
            double v2 = filterResponse[++i];
            if (v2 <= v1) {
                RecessionCurve r = new RecessionCurve(i - 1, hydrograph.getValue(i - 1));
                r.add(hydrograph.getValue(i));
                double v3 = filterResponse[++i];
                while (v3 <= v2 && i < n - 1) {
                    r.add(hydrograph.getValue(i));
                    v2 = v3;
                    v3 = filterResponse[++i];
                }
                curveList.add(r);
            }
        }
        return new ArrayList<RecessionCurve>(curveList);
    }
}
