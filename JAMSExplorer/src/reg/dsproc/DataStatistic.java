/*
 *  Description:
 *
 *  This class holds statistic data for one data array
 *
 */
package reg.dsproc;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author hbusch
 */
public class DataStatistic {

    private String name;
    private double[] data;
    double min;
    double max;
    double varianz;
    double stabw;
    double spannweite;
    double da;
    double kurt;
    double skew;
    double mean;
    double median;
    double quartil1;
    double quartil3;
    private DescriptiveStatistics stats;

    private String newLine = "\n";

    public DataStatistic(String name, double[] data) {
        this.name = name;
        this.data = data;
        init();
    }

    private void init() {
        this.stats = new DescriptiveStatistics();

        // Add the data from the array
        for (int i = 0; i < data.length; i++) {
            stats.addValue(data[i]);
        }
        min = StatUtils.min(data);
        max = StatUtils.max(data);

        //Lageparameter
        mean = StatUtils.mean(data);
        median = StatUtils.percentile(data, 50);
        quartil1 = StatUtils.percentile(data, 25);
        quartil3 = StatUtils.percentile(data, 75);

        //Streuung
        varianz = StatUtils.variance(data);
        stabw = Math.sqrt(varianz);
        spannweite = max - min;

        da = 0;

        double[] F = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            F[i] = Math.abs(data[i] - mean);
            da = da + F[i];
        }
        da = da / (data.length);

    }

    public double getDa() {
        return da;
    }

    public double getKurt() {
        return kurt;
    }

    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getQuartil3() {
        return quartil3;
    }

    public double getStabw() {
        return stabw;
    }

    public String getName() {
        return name;
    }

    public double getQuartil1() {
        return quartil1;
    }

    public double getSkew() {
        return skew;
    }

    public double getSpannweite() {
        return spannweite;
    }

    public double getVarianz() {
        return varianz;
    }


    public String toString() {
        String retString = "Statistik von " + name + ":" + newLine;
        retString += "Minimum    :" + getMin() + newLine;
        retString += "Maximum    :" + getMax() + newLine;
        retString += "Mittel    :" + getMean() + newLine;

        return retString;
    }
}
