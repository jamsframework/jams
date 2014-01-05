package jams.worldwind.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class IntervallCalculation {

    private static final Logger logger = LoggerFactory.getLogger(IntervallCalculation.class);

    private final ArrayList<Double> values;
    private double minimumValue;
    private double maximumValue;

    public IntervallCalculation(List dvalues) {
        this.values = new ArrayList<>(dvalues);
        this.minimumValue = Double.POSITIVE_INFINITY;
        this.maximumValue = Double.NEGATIVE_INFINITY;
        this.calculateMinimumAndMaximum();
    }
    /*
    private void fill(List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            this.values.add(values[i]);
        }
    }   
    */
    
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
        return this.minimumValue;
    }

    public double getMaximumValue() {
        return this.maximumValue;
    }

    private void calculateMinimumAndMaximum() {
        for (int i = 0; i < this.values.size(); i++) {
            if (values.get(i) < this.minimumValue) {
                this.minimumValue = values.get(i);
            } else if (values.get(i) > this.maximumValue) {
                this.maximumValue = values.get(i);
            }
        }
    }

    public double getRange() {
        return this.getMaximumValue() - this.getMinimumValue();
    }

    public double getMean() {
        double sum = 0;
        for (int i = 0; i < this.values.size(); i++) {
            sum += this.values.get(i);
        }
        return sum / this.values.size();
    }

    public double getMedian() {
        ArrayList<Double> tmp = new ArrayList(this.values);
        Collections.sort(tmp);
        //collection size is even than median is mean of lower and upper median
        if (tmp.size() % 2 == 0) {
            return 0.5 * (tmp.get(tmp.size() / 2) + tmp.get(tmp.size() / 2 - 1));
        } else {
            return tmp.get(tmp.size() / 2);
        }
    }

    public double getVariance() {
        double mean = this.getMean();
        int n = this.values.size();
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += Math.pow((this.values.get(i) - mean), 2);
        }
        return sum / (n - 1);
    }

    public double getStandardDeviation() {
        return Math.sqrt(this.getVariance());
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
            double intervallWidth = (max-min) / numberOfClasses;
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
            //System.out.println("Classes: " + numberPerClass);
            ArrayList<Double> tmp = new ArrayList(this.values);
            Collections.sort(tmp);
            breakPoints.add(this.getMinimumValue());
            for (int i = 1; i < numberOfClasses; i++) {
                //System.out.println("i: " + i + " index: " + (i * numberPerClass - 1));
                breakPoints.add(Math.nextUp(tmp.get(i * numberPerClass - 1)));
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
        int sum   = 0;
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
        for (int j = 0; j < intervall.size()-1; j++) {
           //System.out.println("[" + j + "," + (j+1) + "] (" + d + ")");
           if ((d >= (Double)intervall.get(j) && d < (Double)intervall.get(j + 1)) || 
               (d==(Double)intervall.get(intervall.size()-1))) {
               //System.out.println("FOUND: " + d + " INDEX: " + j);
               return j;
           }
        }
        throw new NoSuchElementException("VALUE ("+ d + ") NOT FOUND IN INTERVALL " + this.values);
    }
}