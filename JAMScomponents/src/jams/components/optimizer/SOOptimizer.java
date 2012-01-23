/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package jams.components.optimizer;

import jams.data.*;
import jams.JAMS;
import jams.components.optimizer.SampleFactory.SampleSO;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;

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
            public Attribute.EntityCollection bestParameterSets;
                       
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

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "known optimal value",
            defaultValue = "0"
            )
            public Attribute.Double target;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "stopping criterion if target is met",
            defaultValue = "-Infinity"
            )
            public Attribute.Double epsilonToTarget;
            
    /*SampleSO getFromSampleList(int i){
        return (SampleSO)this.sampleList.get(i);
    }*/
            
    /*************************
     * first some very useful nested classes     
     *************************/ 
    public static abstract class AbstractFunction {
        public abstract double f(double x[]) throws SampleLimitException, ObjectiveAchievedException;
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
            stop(JAMS.i18n("effMethod_not_specified"));
        if (this.effValue == null)
            stop(JAMS.i18n("effValue_not_specified"));
        if (this.mode == null)
            stop(JAMS.i18n("mode_not_specified"));
        
        bestValue = Double.MAX_VALUE;                   
    }
    
    public SampleSO getSample(double[]x) throws ObjectiveAchievedException, SampleLimitException{
        if (this.factory.sampleList.size()>=this.maxn.getValue())
            throw new SampleLimitException("maximum sample count reached");
        
        return factory.getSampleSO(x,funct(x));
    }
        
    public double funct(double x[]) throws ObjectiveAchievedException, SampleLimitException {
        double value;        
        if (GoalFunction == null) {
            //RefreshDataHandles();
            setParameters(x);
            singleRun();
            value = effValue.getValue();
        } else {
            value = GoalFunction.f(x);
        }
        this.iterationCounter.setValue(this.iterationCounter.getValue()+1);

        double result = transformByMode(value,mode.getValue());
        /*if (bestParameterSet!=null && x.length == bestParameterSet.length-1){
            if (result < this.bestValue) {
                bestValue = result;
                int c=0;
                for (int j=0;j<x.length;j++){
                    if (this.bestParameterSet[c]!=null){
                        this.bestParameterSet[c].setValue(x[j]);
                    }
                    c++;
                }
                if (this.bestParameterSet[c]!=null){
                    this.bestParameterSet[c].setValue(value);
                }
                c++;
            }
        }*/
        //this.bestParameterSets = JAMSDataFactory.createEntityCollection();

        ArrayList<Attribute.Entity> list = new ArrayList<Attribute.Entity>();
        Attribute.Entity entity = JAMSDataFactory.createEntity();
        entity.setId(0);
        for (int j = 0; j < n; j++) {
            entity.setDouble("x_" + (j + 1), x[j]);
        }
        entity.setDouble("y_1", value);

        list.add(entity);

        this.bestParameterSets.setEntities(list);

        if ( (value-target.getValue()) < this.epsilonToTarget.getValue() ){
            throw new ObjectiveAchievedException(value, target.getValue());
        }
        return result;
    }   
}
