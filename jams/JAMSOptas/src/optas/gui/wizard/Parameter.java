/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.meta.ComponentField;
import jams.meta.ContextAttribute;

/**
 *
 * @author chris
 */
public class Parameter extends Attribute{

    static public class Range{
        public double lowerBound;
        public double upperBound;

        public Range(double a,double b){
            lowerBound = a;
            upperBound = b;
        }

        public Range(String s) throws NumberFormatException{
            String s2 = s.replace("[", "");
            s2 = s2.replace("]", "");
            String bounds[] = null;
            if (s2.contains(">")){
                bounds = s2.split(">");
            }else{
                bounds = s2.split("&gt;");
            }
            if (bounds.length!=2){
                throw new NumberFormatException();
            }else{
                lowerBound = Double.parseDouble(bounds[0]);
                upperBound = Double.parseDouble(bounds[1]);
            }
        }
        
        @Override
        public String toString(){
            return lowerBound + "\t" + upperBound;
        }
    }
    
    private double lowerBound;
    private double upperBound;    
    private double startValue[];
                 
    public Parameter(String name){
        super(name);
    }
    public Parameter(Attribute a) {
       super(a);
    }
    
    public Parameter(ContextAttribute ca) {
       super(ca);
    }

    public Parameter(ComponentField cf) {
       super(cf);
    }
    
    public Parameter(ContextAttribute ca, Range range) {   
        super(ca);    
        if (range != null) {
            this.lowerBound = range.lowerBound;
            this.upperBound = range.upperBound;
        }
    }
     
    public Parameter(ComponentField cf, Range range) {   
        super(cf);
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
    public int compareTo(Object o){
        return this.toString().compareToIgnoreCase(o.toString());
    }
    
    @Override
    public boolean equals(Object o){
        return compareTo(o)==0;
        /*if (o instanceof Parameter){
            Parameter other = (Parameter)o;
            if (other.getAttributeName()==null){                
                return this.getAttributeName()==null;
            }
            if (other.getParentName()==null){                
                return this.getParentName()==null;
            }
            if (other.getAttributeName().equals(this.getAttributeName()))
                if (other.getParentName().equals(this.getParentName()))
                    return true;
        }
        return false;*/
    }
}
