/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package jams.components.optimizer;

import java.util.Comparator;
import jams.data.*;
import jams.JAMS;
import jams.model.JAMSVarDescription;
import java.io.Serializable;

/**
 *
 * @author Christian Fischer
 */
public abstract class SOOptimizer extends Optimizer {                
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "best paramter values found so far"
            )
            public Attribute.String bestParameterSet;
                   
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "objective function name"
            )
            public JAMSString effMethodName;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the prediction series"
            )
            public JAMSDouble effValue;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode, 1 - minimization, 2 - maximization, 3 - max |f(x)|, 4 - min |f(x)|",
            defaultValue = "1"
            )
            public Attribute.Integer mode;    
            
    /*SampleSO getFromSampleList(int i){
        return (SampleSO)this.sampleList.get(i);
    }*/
            
    /*************************
     * first some very useful nested classes     
     *************************/ 
    public static abstract class AbstractFunction {
        public abstract double f(double x[]);
    }        
    //class for representing samples
    public class SampleSO extends Sample {        
        public double fx;
        public double x[];
        
        public SampleSO(double[] x, double fx) {
            super(x,new double[]{fx});            
            this.fx = fx;
            this.x = super.getParameter();
        }    
        @Override
        public SampleSO clone(){
            Sample x = super.clone();
            return new SampleSO(x.getParameter(),x.fx[0]);            
        }
    }
    //compare samples
    static public class SampleSOComperator implements Comparator {
        private int order = 1;
        public SampleSOComperator(boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
        }
        public int compare(Object d1, Object d2) {
            if (((SampleSO) d1).fx < ((SampleSO) d2).fx) {
                return -1 * order;
            } else if (((SampleSO) d1).fx == ((SampleSO) d2).fx) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }        
    protected double bestValue;                
    protected AbstractFunction GoalFunction = null;
            
    public SOOptimizer() {
    }
               
    @Override
    public void init() {   
        super.init();        
        if (!enable.getValue())
            return;            
        
        if (this.effMethodName == null)
            stop(JAMS.resources.getString("effMethod_not_specified"));
        if (this.effValue == null)
            stop(JAMS.resources.getString("effValue_not_specified"));
        if (this.mode == null)
            stop(JAMS.resources.getString("mode_not_specified"));
        
        bestValue = Double.MAX_VALUE;                   
    }
    
    public SampleSO getSample(double[]x){
        return new SampleSO(x,funct(x));
    }
        
    public double funct(double x[]) {
        double value;        
        if (GoalFunction == null) {
            //RefreshDataHandles();
            setParameters(x);
            singleRun();
            value = effValue.getValue();
        } else {
            value = GoalFunction.f(x);
        }
        currentSampleCount++;
                
        double result = transformByMode(value,mode.getValue());
        
        if (result < this.bestValue) {
            bestValue = result;
            if (bestParameterSet == null)//this can happen in offline mode
                bestParameterSet = jams.data.JAMSDataFactory.createString();
            this.bestParameterSet.setValue("");
            for (int i = 0; i < x.length; i++) {
                bestParameterSet.setValue(bestParameterSet.getValue() + x[i] + ";");
            }
        }
        return result;
    }   
}
