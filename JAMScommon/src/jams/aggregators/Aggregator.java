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
public abstract class Aggregator<T> {
    public enum AggregationMode{
        MINIMUM, 
        MAXIMUM, 
        AVERAGE, 
        SUM, 
        VARIANCE, 
        INDEPENDENT, 
        LAST;
    
        public String toAbbreviation(){
            switch(this){
                case MINIMUM: return "min";
                case MAXIMUM: return "max";
                case AVERAGE: return "avg";
                case LAST: return "last";
                case SUM: return "sum";
                case INDEPENDENT: return "ind";
                case VARIANCE: return "var";
                default: return null;
            }
        }    
                        
        public static AggregationMode fromAbbreviation(String value) {            
            if (value == null) {
                return AggregationMode.AVERAGE;
            }
            for (AggregationMode iter : AggregationMode.values()){
                if (value.compareToIgnoreCase(iter.toAbbreviation()) == 0)
                    return iter;
            }
            return null;
        }
    };
        
    abstract public void init();
    abstract public Aggregator<T> copy();
    abstract public void consider(T v);
    abstract public void finish();
    abstract public T get();
}
