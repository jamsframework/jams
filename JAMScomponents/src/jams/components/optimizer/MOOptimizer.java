/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package jams.components.optimizer;

import java.util.StringTokenizer;
import jams.data.*;
import jams.model.JAMSVarDescription;
import jams.JAMS;
import jams.components.optimizer.SampleFactory.Sample;
import jams.components.optimizer.SampleFactory.SampleComperator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @author Christian Fischer
 */
public abstract class MOOptimizer extends Optimizer {                   
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
            public Attribute.String effMethodName;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the prediction series"
            )
            public Attribute.Double[] effValue;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode, 1 - minimization, 2 - maximization, 3 - max |f(x)|, 4 - min |f(x)|",
            defaultValue = "1"
            )
            public Attribute.String mode;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "known optimal value",
            defaultValue = "0"
            )
            public Attribute.DoubleArray target;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "stopping criterion if target is met",
            defaultValue = "-1"
            )
            public Attribute.Double epsilonToTarget;
    /*************************
     * first some very useful nested classes     
     *************************/ 
    public static abstract class AbstractMOFunction {
        public abstract double[] f(double x[]);
    }
                     
    protected String[] efficiencyNames;    
    protected Set<Sample> bestSamples;        
    //number of efficiencies
    public int m;       
    protected AbstractMOFunction GoalFunction = null;
    protected int iMode[];
    public MOOptimizer() {
    }
           
    @Override
    public void init() {   
        super.init();        
        if (!enable.getValue())
            return;            

        if (this.GoalFunction == null){
            if (this.effMethodName == null)
                stop(JAMS.i18n("effMethod_not_specified"));
            if (this.effValue == null)
                stop(JAMS.i18n("effValue_not_specified"));
            if (this.mode == null)
                stop(JAMS.i18n("mode_not_specified"));
               
            m = effValue.length;
        }else{
            StringTokenizer tok = new StringTokenizer(this.mode.getValue(),";");
            m = tok.countTokens();
        }
        StringTokenizer tok = new StringTokenizer(this.mode.getValue(),";");
        if (tok.countTokens() != m)
            stop(JAMS.i18n("efficiency_count_does_not_match_mode_count"));
        
        int j=0;
        iMode = new int[m];
        while(tok.hasMoreTokens()){
            String sgnMode = tok.nextToken();
            iMode[j++] = Integer.parseInt(sgnMode);
        }
                
        StringTokenizer effTok = new StringTokenizer(this.effMethodName.getValue(),";");
        efficiencyNames = new String[m];
        for (int i=0;i<m;i++){
            try{
                efficiencyNames[i] = effTok.nextToken();            
            }catch(NoSuchElementException e){
                stop(JAMS.i18n("efficiency_count_does_not_effMethodName_mode_count"));
            }
        }           
        bestSamples = new HashSet<Sample>();
    }
                
    public Sample getSample(double[]x) throws SampleLimitException, ObjectiveAchievedException{
        if (factory.sampleList.size()>=this.maxn.getValue())
            throw new SampleLimitException("maximum sample count reached");
        Sample s = this.factory.getSample(x,funct(x));
        this.bestSamples.add(s);
        this.bestSamples = this.getParetoOptimalSet(this.bestSamples);

        return s;
    }
                  
    public Set<Sample> getParetoOptimalSet(Set<Sample> set){        
        SampleComperator comparer = new SampleComperator(true);
        HashSet<Sample> result = new HashSet<Sample>();
        Iterator<Sample> iter = set.iterator();
        while(iter.hasNext()){
            Sample candidate = iter.next();
            boolean isDominated = false;
            Iterator<Sample> iter2 = set.iterator();            
            while(iter2.hasNext()){
                Sample rivale = iter2.next();
                if (candidate == rivale)
                    continue;
                if (comparer.compare(candidate,rivale)<0){
                    isDominated = true;
                    break;
                }
            }
            if (!isDominated)
                result.add(candidate);
        }
        return result;
    }
    
    private double[] funct(double x[]) throws ObjectiveAchievedException {
        
        double value[] = new double[m];        
        if (GoalFunction == null) {          
            this.setParameters(x);         
            singleRun();       
            for (int i=0;i<m;i++)
                value[i] = effValue[i].getValue();                        
        } else {            
            value = GoalFunction.f(x);
        }
        //done in optimizer.java
        /*if (this.sampleWriter!=null){
            try{
                for (int i=0;i<x.length;i++)
                    sampleWriter.write(x[i]+"\t");

                for (int i=0;i<value.length;i++)
                    sampleWriter.write(value[i]+"\t");

                sampleWriter.write("\n");
                sampleWriter.flush();
            }catch(Exception e){

            }
        }*/
        this.iterationCounter.setValue(this.iterationCounter.getValue()+1);
        for (int i=0;i<m;i++)
            value[i] = this.transformByMode(value[i], iMode[i]);
                        
        Iterator<Sample> iter = this.bestSamples.iterator();
        for (int i=0;iter.hasNext();i++){
            Sample s = iter.next();
            
            ArrayList<Attribute.Entity> list = new ArrayList<Attribute.Entity>();
            Attribute.Entity entity = JAMSDataFactory.createEntity();
            entity.setId(i);
            for (int j=0;j<n;j++){
                entity.setDouble("x_"+(j+1), s.getParameter()[j]);
            }
            for (int j=0;j<m;j++){
                entity.setDouble("y_"+(j+1), s.F()[j]);
            }
            list.add(entity);

            this.bestParameterSets.setEntities(list);
        }
        
        if (target!=null && this.target.getValue().length == value.length){
            boolean objectiveAchieved = true;
            for (int i=0;i<value.length;i++){
                if ( value[i]-target.getValue()[i]  >=this.epsilonToTarget.getValue())
                    objectiveAchieved = false;
            }
            if (objectiveAchieved) {
                double targets[] = new double[target.getValue().length];
                for (int i = 0; i < targets.length; i++) {
                    targets[i] = target.getValue()[i];
                }
                throw new ObjectiveAchievedException(value, targets);
            }
        }
        return value;
    }
}
