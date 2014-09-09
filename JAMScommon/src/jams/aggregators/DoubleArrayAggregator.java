/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.aggregators;

import java.util.Arrays;

/**
 *
 * @author christian
 */
public abstract class DoubleArrayAggregator extends Aggregator<double[]> {

    double v[];
    DoubleArrayAggregator(int n){
        v = new double[n];
    }
    
    public DoubleArrayAggregator(DoubleArrayAggregator copy){
        this.v = (double[])copy.v.clone();
    }
    
    public int n(){
        return v.length;
    }
    
    @Override
    public abstract DoubleArrayAggregator copy();
    
    @Override
    public void init() {}

    @Override
    public double[] get() {
        return v;
    }
    
    @Override
    public void finish() {}

    public static DoubleArrayAggregator create(AggregationMode mode, int n){
        switch(mode){
            case SUM: return new SumAggregator(n);
            case AVERAGE: return new AverageAggregator(n);
            case LAST: return new LastAggregator(n);
            case MINIMUM: return new MinimumAggregator(n);
            case MAXIMUM: return new MaximumAggregator(n);
            case VARIANCE: return new VarianceAggregator(n);
            case INDEPENDENT: return null;
        }
        return null;
    }
    
    //implementations
    //Sum up values
    static class SumAggregator extends DoubleArrayAggregator {
        public SumAggregator(int n){
            super(n);            
        }
        public SumAggregator(SumAggregator copy){
            super(copy);
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new SumAggregator(this);
        }
        @Override
        public void init(){
            super.init();
            Arrays.fill(v, 0);
        }
        
        @Override
        public void consider(double x[]) {
            for (int i = 0; i < v.length; i++) {
                v[i] += x[i];
            }
        }
    }
    //minimum
    static class MinimumAggregator extends DoubleArrayAggregator {
        public MinimumAggregator(MinimumAggregator copy){
            super(copy);
        }
        public MinimumAggregator(int n){
            super(n);
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new MinimumAggregator(this);
        }
        @Override
        public void init(){
            super.init();
            Arrays.fill(v, Double.POSITIVE_INFINITY);
        }
        @Override
        public void consider(double x[]) {
            for (int i = 0; i < v.length; i++) {
                v[i] = Math.min(x[i], v[i]);
            }
        }
    }
    //maximum
    static class MaximumAggregator extends DoubleArrayAggregator {
        public MaximumAggregator(MaximumAggregator copy){
            super(copy);
        }
        public MaximumAggregator(int n){
            super(n);
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new MaximumAggregator(this);
        }
        @Override
        public void init(){
            super.init();
            Arrays.fill(v, Double.NEGATIVE_INFINITY);
        }
        @Override
        public void consider(double x[]) {
            for (int i = 0; i < v.length; i++) {
                v[i] = Math.max(x[i], v[i]);
            }
        }
    }
    
    //Take averages of values
    static class AverageAggregator extends DoubleArrayAggregator {
        int counter = 0;
        public AverageAggregator(AverageAggregator copy){
            super(copy);
        }
        public AverageAggregator(int n){
            super(n);
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new AverageAggregator(this);
        }
        @Override
        public void init() {
            super.init();
            Arrays.fill(v, 0);
            counter = 0;
        }
        
        @Override
        public void consider(double x[]) {
            for (int i = 0; i < v.length; i++) {
                v[i] += x[i];
            }
            counter++;
        }
        
        @Override
        public void finish() {
            super.finish();
            for (int i = 0; i < v.length; i++) {
                v[i] /= (double)counter;
            }
            counter = 1;
        }
    }
    
    //calculates the variance of the values
    static class VarianceAggregator extends DoubleArrayAggregator {
        double mean[];
        int counter=0;
        public VarianceAggregator(VarianceAggregator copy){
            super(copy);
            mean = Arrays.copyOf(copy.mean, copy.mean.length);
            counter=copy.counter;
        }
        
        public VarianceAggregator(int n){
            super(n);
            mean = new double[n];
            counter=0;
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new VarianceAggregator(this);
        }
        
        @Override
        public void init() {
            super.init();
            Arrays.fill(v, 0);            
            Arrays.fill(mean, 0);            
        }
        
        @Override
        public void consider(double x[]) {
            counter++;
            for (int i=0;i<n();i++){
                double delta = x[i]-mean[i];
                mean[i] = mean[i] + delta / counter;
                v[i] = v[i] + delta*(x[i]-mean[i]);
            }
        }
        
        @Override
        public void finish() {
            super.finish();
            for (int i=0;i<n();i++){
                v[i] /= (double)(counter-1.);
            }
        }
    }
            
    //Take first
    static class FirstAggregator extends DoubleArrayAggregator {
        boolean isFirst = true;
        public FirstAggregator(FirstAggregator copy){
            super(copy);
        }
        public FirstAggregator(int n){
            super(n);
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new FirstAggregator(this);
        }
        @Override
        public void init() {
            super.init();
            Arrays.fill(v, 0);
            isFirst = true;
        }
        @Override
        public void consider(double x[]) {
            if (isFirst) {
                for (int i = 0; i < v.length; i++) {
                    v[i] += x[i];
                }
                isFirst = false;
            }
        }
    }
    //Take last value
    static class LastAggregator extends DoubleArrayAggregator {
        public LastAggregator(LastAggregator copy){
            super(copy);
        }
        public LastAggregator(int n){
            super(n);
        }
        @Override
        public DoubleArrayAggregator copy(){
            return new LastAggregator(this);
        }
        @Override
        public void init(){
            Arrays.fill(v, 0);
        }
        @Override
        public void consider(double x[]) {
            System.arraycopy(x, 0, v, 0, v.length);
        }
    }
    
    public static void main(String[] args) {
        DoubleArrayAggregator t = DoubleArrayAggregator.create(Aggregator.AggregationMode.VARIANCE, 2);
        t.init();
        for (int i=0;i<20;i++){
            t.consider(new double[]{1,i*i});
        }
        t.finish();
        System.out.println(Arrays.toString(t.get()));
    }
}
