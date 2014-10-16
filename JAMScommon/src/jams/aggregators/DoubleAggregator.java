/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

/**
 *
 * @author christian
 */
public abstract class DoubleAggregator extends Aggregator<Double> {

    double v;

    DoubleAggregator() {
    }

    public DoubleAggregator(DoubleAggregator copy) {
        this.v = copy.v;
    }

    @Override
    public abstract DoubleAggregator copy();

    @Override
    public void init() {
    }

    @Override
    public Double get() {
        return v;
    }

    @Override
    public void finish() {
    }

    public static DoubleAggregator create(AggregationMode mode) {
        switch (mode) {
            case SUM:
                return new SumAggregator();
            case AVERAGE:
                return new AverageAggregator();
            case LAST:
                return new LastAggregator();
            case MINIMUM:
                return new MinimumAggregator();
            case MAXIMUM:
                return new MaximumAggregator();
            case VARIANCE:
                return new VarianceAggregator();
            case INDEPENDENT:
                throw new UnsupportedOperationException();
        }
        throw new IllegalArgumentException(mode + " is not a valid aggregation mode");
    }

    //implementations
    //Sum up values
    static class SumAggregator extends DoubleAggregator {

        public SumAggregator() {
            super();
        }

        public SumAggregator(SumAggregator copy) {
            super(copy);
        }

        @Override
        public DoubleAggregator copy() {
            return new SumAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            v = 0;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x))
                v += x;
        }
    }

    //minimum
    static class MinimumAggregator extends DoubleAggregator {

        public MinimumAggregator(MinimumAggregator copy) {
            super(copy);
        }

        public MinimumAggregator() {
            super();
        }

        @Override
        public DoubleAggregator copy() {
            return new MinimumAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            v = Double.POSITIVE_INFINITY;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x))
                v = Math.min(x, v);
        }
    }

    //maximum
    static class MaximumAggregator extends DoubleAggregator {

        public MaximumAggregator(MaximumAggregator copy) {
            super(copy);
        }

        public MaximumAggregator() {
            super();
        }

        @Override
        public DoubleAggregator copy() {
            return new MaximumAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            v = Double.NEGATIVE_INFINITY;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x))
                v = Math.max(x, v);
        }
    }

    //Take averages of values
    static class AverageAggregator extends DoubleAggregator {

        int counter = 0;

        public AverageAggregator(AverageAggregator copy) {
            super(copy);
        }

        public AverageAggregator() {
            super();
        }

        @Override
        public DoubleAggregator copy() {
            return new AverageAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            v = 0;
            counter = 0;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x)){
                v += x;
                counter++;
            }
        }

        @Override
        public void finish() {
            super.finish();
            if (counter!=0){
                v /= (double) counter;
            }else{
                v = Double.NaN;
            }
            counter = 1;
        }
    }

    //calculates the variance of the values
    static class VarianceAggregator extends DoubleAggregator {

        double mean;
        int counter = 0;

        public VarianceAggregator(VarianceAggregator copy) {
            super(copy);
            mean = copy.mean;
            counter = copy.counter;
        }

        public VarianceAggregator() {
            super();
            counter = 0;
        }

        @Override
        public DoubleAggregator copy() {
            return new VarianceAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            v = 0;
            mean = 0;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x)) {
                counter++;
                double delta = x - mean;
                mean = mean + delta / counter;
                v = v + delta * (x - mean);
            }
        }

        @Override
        public void finish() {
            super.finish();
            if (counter>1){
                v /= (double) (counter - 1.);
            }else{
                v = Double.NaN;
            }
            counter = 2;
        }
    }

    //Take first
    static class FirstAggregator extends DoubleAggregator {

        boolean isFirst = true;

        public FirstAggregator(FirstAggregator copy) {
            super(copy);
        }

        public FirstAggregator(int n) {
            super();
        }

        @Override
        public DoubleAggregator copy() {
            return new FirstAggregator(this);
        }

        @Override
        public void init() {
            super.init();
            v=0;
            isFirst = true;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x)) {
                if (isFirst) {
                    v += x;
                    isFirst = false;
                }
            }
        }
    }

    //Take last value
    static class LastAggregator extends DoubleAggregator {

        public LastAggregator(LastAggregator copy) {
            super(copy);
        }

        public LastAggregator() {
            super();
        }

        @Override
        public DoubleAggregator copy() {
            return new LastAggregator(this);
        }

        @Override
        public void init() {
            v=0;
        }

        @Override
        public void consider(Double x) {
            if (!Double.isNaN(x))
                v=x;
        }
    }    
}
