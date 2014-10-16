/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author christian
 */
public abstract class DoubleIteratorAggregator extends Aggregator<Iterable<Double>> {

    double v[];

    private class ArrayIterator implements Iterator<Double> {

        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < v.length;
        }

        @Override
        public Double next() {
            return v[pos++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    DoubleIteratorAggregator(int n) {
        v = new double[n];
    }

    public DoubleIteratorAggregator(DoubleIteratorAggregator copy) {
        this.v = (double[]) copy.v.clone();
    }

    public int n() {
        return v.length;
    }

    @Override
    public abstract DoubleIteratorAggregator copy();

    @Override
    public void init() {
    }

    @Override
    public Iterable<Double> get() {
        return new Iterable<Double>() {
            @Override
            public Iterator<Double> iterator() {
                return new ArrayIterator();
            }
        };
    }

    @Override
    public void finish() {
    }

    public static DoubleIteratorAggregator create(AggregationMode mode, int n) {
        switch (mode) {
            case SUM:
                return new SumAggregator(n);
            case AVERAGE:
                return new AverageAggregator(n);
            case LAST:
                return new LastAggregator(n);
            case MINIMUM:
                return new MinimumAggregator(n);
            case MAXIMUM:
                return new MaximumAggregator(n);
            case VARIANCE:
                return new VarianceAggregator(n);
            case INDEPENDENT:
                return null;
        }
        return null;
    }

    //implementations
    //Sum up values
    static class SumAggregator extends DoubleIteratorAggregator {

        public SumAggregator(int n) {
            super(n);
        }

        public SumAggregator(SumAggregator copy) {
            super(copy);
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new SumAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            Arrays.fill(v, 0);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;
            for (Double x : in) {
                if (!Double.isNaN(x)){
                    v[i] += x;
                }
                i++;
            }
        }
    }

    //minimum
    static class MinimumAggregator extends DoubleIteratorAggregator {

        public MinimumAggregator(MinimumAggregator copy) {
            super(copy);
        }

        public MinimumAggregator(int n) {
            super(n);
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new MinimumAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            Arrays.fill(v, Double.POSITIVE_INFINITY);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;
            for (Double x : in) {
                if (!Double.isNaN(x))
                    v[i] = Math.min(x, v[i]);
                i++;
            }
        }
    }

    //maximum
    static class MaximumAggregator extends DoubleIteratorAggregator {

        public MaximumAggregator(MaximumAggregator copy) {
            super(copy);
        }

        public MaximumAggregator(int n) {
            super(n);
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new MaximumAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            Arrays.fill(v, Double.NEGATIVE_INFINITY);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;
            for (Double x : in) {
                if (!Double.isNaN(x))
                    v[i] = Math.max(x, v[i]);
                i++;
            }
        }
    }

    //Take averages of values
    static class AverageAggregator extends DoubleIteratorAggregator {

        int counter[];

        public AverageAggregator(AverageAggregator copy) {
            super(copy);
        }

        public AverageAggregator(int n) {
            super(n);
            counter = new int[n];
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new AverageAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            Arrays.fill(v, 0);
            Arrays.fill(counter, 0);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;
            for (Double x : in) {
                if (!Double.isNaN(x)){
                    v[i] += x;
                    counter[i]++;
                }
                i++;
            }            
        }

        @Override
        public void finish() {
            super.finish();
            for (int i = 0; i < v.length; i++) {
                if (counter[i]!=0)
                    v[i] /= (double) counter[i];
                else
                    v[i] = Double.NaN;
            }
            Arrays.fill(counter, 1);
        }
    }

    //calculates the variance of the values
    static class VarianceAggregator extends DoubleIteratorAggregator {

        double mean[];
        int counter[];

        public VarianceAggregator(VarianceAggregator copy) {
            super(copy);
            mean = Arrays.copyOf(copy.mean, copy.mean.length);
            counter = copy.counter;
        }

        public VarianceAggregator(int n) {
            super(n);
            mean = new double[n];
            counter = new int[n];
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new VarianceAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            Arrays.fill(v, 0);
            Arrays.fill(mean, 0);
            Arrays.fill(counter, 0);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;            
            for (Double x : in) {
                if (!Double.isNaN(x)) {
                    counter[i]++;
                    double delta = x - mean[i];
                    mean[i] = mean[i] + delta / counter[i];
                    v[i] = v[i] + delta * (x - mean[i]);                    
                }
                i++;
            }            
        }
                
        @Override
        public void finish() {
            super.finish();
            for (int i = 0; i < n(); i++) {
                v[i] /= (double) (counter[i] - 1.);
                counter[i]=2;
            }
        }
    }

    //Take first
    static class FirstAggregator extends DoubleIteratorAggregator {

        boolean isFirst = true;

        public FirstAggregator(FirstAggregator copy) {
            super(copy);
        }

        public FirstAggregator(int n) {
            super(n);
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new FirstAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            Arrays.fill(v, Double.NaN);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;
            for (Double x : in) {
                if (Double.isNaN(v[i]) && !Double.isNaN(x))
                    v[i++] = x;
            }
        }
    }

    //Take last value
    static class LastAggregator extends DoubleIteratorAggregator {

        public LastAggregator(LastAggregator copy) {
            super(copy);
        }

        public LastAggregator(int n) {
            super(n);
        }

        @Override
        public DoubleIteratorAggregator copy() {
            return new LastAggregator(this);
        }

        @Override
        public void init() {
            Arrays.fill(v, 0);
        }

        @Override
        public void consider(Iterable<Double> in) {
            int i = 0;
            for (Double x : in) {
                if (!Double.isNaN(x)){
                    v[i] = x;
                }
                i++;
            }
        }
    }

    public static void main(String[] args) {
        DoubleIteratorAggregator t = DoubleIteratorAggregator.create(Aggregator.AggregationMode.VARIANCE, 2);
        t.init();
        for (int i = 0; i < 20; i++) {
            //t.consider(new Double[]{1., (double)(i * i)});
        }
        t.finish();
        //System.out.println(Arrays.toString(t.get()));
    }
}
