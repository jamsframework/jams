/*
 *  Description:
 *
 *  This class holds statistic data for one data array
 *
 */
package jams.explorer.dsproc;

import jams.JAMS;
import java.util.ArrayList;
import java.util.HashMap;
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
    double deviation;
    double kurt;
    double skew;
    double mean;
    double sum;
    double median;
    double quartil1;
    double quartil3;

    public DataStatistic(String name, double[] data) {
        this.name = name;
        this.data = data;
        cleanData();
        init();
    }
    
    /*  
     * get rid of NaN values in data array
     */
    private void cleanData() {
        
        ArrayList<Double> cleanList = new ArrayList();
        for (double d : data) {
            if (!Double.isNaN(d)) {
                cleanList.add(d);
            }
        }
        data = new double[cleanList.size()];
        int i = 0;
        for (double d : cleanList) {
            data[i++] = d;
        }
    }

    /**
     * compute all statistic data
     */
    private void init() {
        DescriptiveStatistics stats = new DescriptiveStatistics();

        // add the data from the array
        for (int i = 0; i < data.length; i++) {
            stats.addValue(data[i]);
        }
        min = StatUtils.min(data);
        max = StatUtils.max(data);

        //Lageparameter
        mean = StatUtils.mean(data);
        sum = StatUtils.sum(data);
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

        skew = stats.getSkewness();
        kurt = stats.getKurtosis();
        deviation = stats.getStandardDeviation();

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

    public double getSum() {
        return sum;
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

    public double getDeviation() {
        return deviation;
    }


    /**
     * get result as hashmap
     * @return hashmap String -> double
     */
    public HashMap<String, Double> getResult() {
        HashMap<String, Double> result = new HashMap<String, Double>();
        result.put(JAMS.i18n("MINIMUM"), getMin());
        result.put(JAMS.i18n("MAXIMUM"), getMax());
        result.put(JAMS.i18n("MITTELWERT"), getMean());
        result.put(JAMS.i18n("SUMME"), getSum());
        result.put(JAMS.i18n("UNTERES_QUARTIL_(Q.25)"), getQuartil1());
        result.put(JAMS.i18n("MITTLERES_QUARTIL_(MEDIAN)"), getMedian());
        result.put(JAMS.i18n("OBERES_QUARTIL_(Q.75)"), getQuartil3());
        result.put(JAMS.i18n("SPANNWEITE"), getSpannweite());
        result.put(JAMS.i18n("VARIANZ"), getVarianz());
        result.put(JAMS.i18n("STANDARDABWEICHUNG"), getDeviation());
        result.put(JAMS.i18n("SCHIEFE_(SKEWNESS)"), getSkew());
        result.put(JAMS.i18n("WÖLBUNG_(KURTOSIS)"), getKurt());

        return result;
    }

    @Override
    public String toString() {
        String newLine = "\n";
        String retString = "========================";
        retString += JAMS.i18n("STATISTIK_VON_") + name + ":" + newLine;
        retString += JAMS.i18n("MINIMUM____:") + getMin() + newLine;
        retString += JAMS.i18n("MAXIMUM____:") + getMax() + newLine;
        retString += JAMS.i18n("MITTEL_____:") + getMean() + newLine;
        retString += JAMS.i18n("MEDIANWERT_:") + getMedian() + newLine;
        retString += JAMS.i18n("SPANNWEITE_:") + getSpannweite() + newLine;
        retString += JAMS.i18n("VARIANZ____:") + getVarianz() + newLine;

        return retString;
    }
}
