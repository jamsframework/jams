/*
 *  Description:
 *
 *  This class holds statistic data for one data array
 *
 */
package reg.dsproc;

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
    double median;
    double quartil1;
    double quartil3;

    public DataStatistic(String name, double[] data) {
        this.name = name;
        this.data = data;
        init();
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
        result.put("Minimum", getMin());
        result.put("Maximum", getMax());
        result.put("Mittelwert", getMean());
        result.put("unteres Quartil (Q.25)", getQuartil1());
        result.put("mittleres Quartil (Median)", getMedian());
        result.put("oberes Quartil (Q.75)", getQuartil3());
        result.put("Spannweite", getSpannweite());
        result.put("Varianz", getVarianz());
        result.put("Standardabweichung", getDeviation());
        result.put("Schiefe (Skewness)", getSkew());
        result.put("Wölbung (Kurtosis)", getKurt());

        return result;
    }

    @Override
    public String toString() {
        String newLine = "\n";
        String retString = "========================";
        retString += "Statistik von " + name + ":" + newLine;
        retString += "Minimum    :" + getMin() + newLine;
        retString += "Maximum    :" + getMax() + newLine;
        retString += "Mittel     :" + getMean() + newLine;
        retString += "Medianwert :" + getMedian() + newLine;
        retString += "Spannweite :" + getSpannweite() + newLine;
        retString += "Varianz    :" + getVarianz() + newLine;

        return retString;
    }
}
