/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import jams.meta.ComponentField;
import jams.meta.ContextAttribute;
import java.io.Serializable;
import optas.metamodel.Tools.Range;

/**
 *
 * @author chris
 */
public class Parameter2 implements Serializable, Comparable {

    private double lowerBound;
    private double upperBound;    
    private double startValue[];

    Object field = null;    
           
    public Parameter2(ContextAttribute ca) {
       field = ca;
    }

    public Parameter2(ComponentField cf) {
       field = cf;
    }
    
    public Parameter2(ContextAttribute ca, Range range) {   
        field = ca;     
        if (range != null) {
            this.lowerBound = range.lowerBound;
            this.upperBound = range.upperBound;
        }
    }
     
    public Parameter2(ComponentField cf, Range range) {   
        field = cf;     
        if (range != null) {
            this.lowerBound = range.lowerBound;
            this.upperBound = range.upperBound;
        }
    }
    
    /**
     * @return the lowerBound
     */
    public double getLowerBound() {
        return lowerBound;
    }

    /**
     * @param lowerBound the lowerBound to set
     */
    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * @return the upperBound
     */
    public double getUpperBound() {
        return upperBound;
    }

    /**
     * @param upperBound the upperBound to set
     */
    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
    }
    
    /**
     * @return the startValue
     */
    public double[] getStartValue() {
        return startValue;
    }

    /**
     * @param startValue the startValue to set
     */
    public void setStartValue(double startValue[]) {
        this.startValue = startValue;
    }
    
    public void setStartValue(String startvalue) throws NumberFormatException{
        String startvalue2 = startvalue.replace("[", "");
        startvalue2 = startvalue2.replace("]", "");
        String values[] = startvalue2.split(",");
        int m = values.length;
        this.startValue = new double[m];
        for (int i=0;i<m;i++){
            this.startValue[i] = Double.parseDouble(values[i]);
        }
    }
    
    @Override
    public String toString(){
        if (this.field instanceof ComponentField){
            ComponentField cf = (ComponentField)field;
            return cf.getParent().getInstanceName() + "." + cf.getName();
        }else if (this.field instanceof ContextAttribute){
            ContextAttribute ca = (ContextAttribute)this.field;
            return ca.getContext().getInstanceName() + "." + ca.getName();
        }
        return null;
    }
    
    @Override
    public int compareTo(Object o){
        return this.toString().compareToIgnoreCase(o.toString());
    }
}
