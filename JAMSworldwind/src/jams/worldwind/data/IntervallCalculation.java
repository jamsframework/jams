package jams.worldwind.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class IntervallCalculation {

    private static final Logger logger = LoggerFactory.getLogger(IntervallCalculation.class);

    private final ArrayList<Double> values;
    private final DescriptiveStatistics statistics;

    public IntervallCalculation(List dvalues) {
        this.values = new ArrayList<>(dvalues);
        this.statistics = new DescriptiveStatistics();
        this.calculateStatistics();
    }

    private void calculateStatistics() {
        for(int i=0; i<this.values.size();i++) {
            this.statistics.addValue(this.values.get(i));
        }
    }

    public double getValue(int index) {
        if (index >= 0 && index < this.values.size()) {
            return this.values.get(index);
        } else {
            return Double.NaN;
        }
    }

    public List<Double> getValues() {
        return this.values;
    }

    public double getMinimumValue() {
        return this.statistics.getMin();
    }

    public double getMaximumValue() {
        return this.statistics.getMax();
    }

    public double getRange() {
        return this.getMaximumValue()-this.getMinimumValue();
    }

    public double getMean() {
        return this.statistics.getMean();
    }

    public double getMedian() {
        return this.statistics.getPercentile(50);
    }

    public double getVariance() {
        return this.statistics.getVariance();
    }

    public double getStandardDeviation() {
        return this.statistics.getStandardDeviation();
    }

    private List<Double> standardMinimumMaximumIntervall() {
        List<Double> breakPoints = new ArrayList<>(2);
        breakPoints.add(this.getMinimumValue());
        breakPoints.add(this.getMaximumValue());
        return breakPoints;
    }

    public List<Double> getEqualIntervall(int numberOfClasses) {
        if (numberOfClasses > 0) {
            List<Double> breakPoints = new ArrayList<>(numberOfClasses + 1);
            double intervallWidth = this.getRange() / numberOfClasses;
            double sum = 0;
            breakPoints.add(this.getMinimumValue());
            for (int i = 1; i < numberOfClasses; i++) {
                breakPoints.add(Math.nextUp(breakPoints.get(i - 1)) + intervallWidth);
            }
            breakPoints.add(this.getMaximumValue());
            return breakPoints;
        } else {
            logger.warn("Intervall classes must be greater zero! Getting standard intervall!");
            return this.standardMinimumMaximumIntervall();
        }
    }

    public List<Double> getEqualIntervall(double min, double max, int numberOfClasses) {
        if (numberOfClasses > 0) {
            List<Double> breakPoints = new ArrayList<>(numberOfClasses + 1);
            double intervallWidth = (max - min) / numberOfClasses;
            double sum = 0;
            breakPoints.add(this.getMinimumValue());
            for (int i = 1; i < numberOfClasses; i++) {
                breakPoints.add(Math.nextUp(breakPoints.get(i - 1)) + intervallWidth);
            }
            breakPoints.add(this.getMaximumValue());
            return breakPoints;
        } else {
            logger.warn("Intervall classes must be greater zero! Getting standard intervall!");
            return this.standardMinimumMaximumIntervall();
        }
    }

    public List<Double> getDefinedIntervall(double intervallSize) {
        if (intervallSize > 0) {
            int numberOfClasses = (int) Math.round(this.getRange() / intervallSize);
            return getEqualIntervall(numberOfClasses);
        } else {
            logger.warn("Intervall size must be greater zero! Getting standard intervall!");
            return this.standardMinimumMaximumIntervall();
        }
    }

    public List<Double> getQuantilIntervall(int numberOfClasses) {
        if (numberOfClasses > 0) {
            List<Double> breakPoints = new ArrayList<>(numberOfClasses + 1);
            int numberPerClass = (int) Math.round(this.values.size() / numberOfClasses);

            TreeMap<Double, Integer> countOccurences = new TreeMap<>();
            breakPoints.add(this.getMinimumValue());
            for (int i = 0; i < this.values.size(); i++) {
                if (!countOccurences.containsKey(this.values.get(i))) {
                    countOccurences.put(this.values.get(i), 1);
                } else {
                    Integer count = countOccurences.get(this.values.get(i));
                    count++;
                    countOccurences.put(this.values.get(i), count);
                }
            }
            boolean last = false;
            int remainingObjectsCount = this.values.size();
            int sum = 0;
            int newNumberOfClasses = numberOfClasses;
            Entry<Double, Integer> ent = countOccurences.pollFirstEntry();
            while (ent != null) {
                int count = ent.getValue();
                sum += count;
                remainingObjectsCount-=count;
                if (sum >= numberPerClass) {
                    double d = Math.nextUp(ent.getKey());
                    if (d < this.getMaximumValue()) {
                        breakPoints.add(d);
                    }
                    System.out.println("Remain: " + remainingObjectsCount);
                    newNumberOfClasses--;
                    System.out.println("Left Classes: " + newNumberOfClasses);
                    if(newNumberOfClasses>0) {
                    numberPerClass = remainingObjectsCount / newNumberOfClasses;
                    System.out.println("PER CLASS: " + numberPerClass);
                    }
                    sum = 0;
                }
                ent = countOccurences.pollFirstEntry();
            }
            breakPoints.add(this.getMaximumValue());
            return breakPoints;
        } else {
            logger.warn("Intervall classes must be greater zero! Getting standard intervall!");
            return this.standardMinimumMaximumIntervall();
        }
    }

    public void printHistogramm(List<?> intervall) {
        ArrayList<Double> tmp = new ArrayList(this.values);
        Collections.sort(tmp);
        int start = 0;
        int count = 0;
        int sum = 0;
        //System.out.println("Size: " + intervall.size());
        for (int h = 0; h < intervall.size() - 1; h++) {
            System.out.print("[" + intervall.get(h) + "," + intervall.get(h + 1) + "] : ");
            for (int i = start; i < tmp.size(); i++) {
                if (tmp.get(i).compareTo((Double) intervall.get(h + 1)) <= 0) {
                    System.out.print("*");
                    count++;
                    start++;
                } else {
                    break;
                }
            }
            System.out.print(" | COUNT: " + count);
            sum += count;
            count = 0;
            System.out.println();
        }
        System.out.println("SUMMARY: TOTAL STARS: " + sum + " | TOTAL ELEMENTS: " + tmp.size());
    }

    public int getIntervallIndex(List<?> intervall, double d) {
        for (int j = 0; j < intervall.size() - 1; j++) {
            //System.out.println("[" + j + "," + (j+1) + "] (" + d + ")");
            if ((d >= (Double) intervall.get(j) && d < (Double) intervall.get(j + 1))
                    || (d == (Double) intervall.get(intervall.size() - 1))) {
                //System.out.println("FOUND: " + d + " INDEX: " + j);
                return j;
            }
        }
        throw new NoSuchElementException("VALUE (" + d + ") NOT FOUND IN INTERVALL " + this.values);
    }
}
