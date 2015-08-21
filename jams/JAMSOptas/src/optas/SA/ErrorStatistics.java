/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.SortedMap;
import java.util.TreeMap;
import jams.aggregators.*;
import jams.aggregators.Aggregator.AggregationMode;

/**
 *
 * @author christian
 */
public class ErrorStatistics<T extends Comparable> {
    TreeMap<T, double[]> data;
    
    double lowQ;
    double upQ;
    int n;
    
    public ErrorStatistics(int n){
        this.n = n;
        data = new TreeMap<T, double[]>();
    }
    
    public void add(T key, double[] value){
        data.put(key, value);
    }
    
    public void setQuantileRange(double lowQ, double upQ){
        this.lowQ = lowQ;
        this.upQ = upQ;
    }
    
    private SortedMap<T, double[]> getInterQuantileMap(){
        T fromKey = null;
        T toKey = null;
        
        double q=0;
        double delta = 1.0 / data.size();

        for (T key : data.keySet()){
            q += delta;
            if ( q >= lowQ && q <= upQ){
                if (fromKey == null)
                    fromKey = key;
                toKey = key;
            }
        }  
        if (fromKey==null || toKey == null)
            return null;
        
        return data.subMap(fromKey, toKey);
    }
    
    private double[] aggregate(Aggregator<double[]> a){
        SortedMap<T, double[]> subMap = getInterQuantileMap();
        if (subMap == null){
            return null;
        }
        a.init();
        for (T key : subMap.keySet()){
            double value[] = subMap.get(key);
            a.consider(value);
        }    
        a.finish();
        return a.get();
    }
    
    public double[] getMin(){        
        return aggregate(DoubleArrayAggregator.create(AggregationMode.MINIMUM, n));
    }
    
    public double[] getMax(){
        return aggregate(DoubleArrayAggregator.create(AggregationMode.MAXIMUM, n));
    }
    
    public double[] getMean(){
        return aggregate(DoubleArrayAggregator.create(AggregationMode.AVERAGE, n));
    }
    
    public double[] getVariance(){
        return aggregate(DoubleArrayAggregator.create(AggregationMode.VARIANCE, n));
    }
    
    public int getSize(){
        SortedMap<T, double[]> subMap = getInterQuantileMap();
        if (subMap == null)
            return 0;
        return getInterQuantileMap().size();
    }
}
