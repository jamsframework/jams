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
import java.util.StringTokenizer;
import jams.data.*;
import jams.model.JAMSVarDescription;
import jams.JAMS;
import jams.components.optimizer.Optimizer.Sample;
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
            public JAMSString bestParameterSets;
                   
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
            public JAMSDouble[] effValue;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode, 1 - minimization, 2 - maximization, 3 - max |f(x)|, 4 - min |f(x)|",
            defaultValue = "1"
            )
            public JAMSString mode;              
    /*************************
     * first some very useful nested classes     
     *************************/ 
    public static abstract class AbstractMOFunction {
        public abstract double[] f(double x[]);
    }
        
    //class for representing samples
    public class SampleMO extends Sample{
        public SampleMO(double[] x, double[] fx) {
            super(x,fx);
        }
        double [] getFX(){
            return this.fx;
        }
        @Override
        public SampleMO clone(){
            Sample x = super.clone();
            return new SampleMO(x.getParameter(),x.fx);            
        }
    }
    SampleMO getFromSampleList(int i){
        return (SampleMO)this.sampleList.get(i);
    }
    //compare samples
    static public class SampleMOComperator implements Comparator {

        private int order = 1;

        public SampleMOComperator(boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
        }

        public int compare(Object d1, Object d2) {
            int m = ((Sample)d1).fx.length;
            if (((Sample)d1).fx.length != ((Sample)d2).fx.length){
                return 0;
            }
            int ord = -1000;
            for (int i=0;i<m;i++){
                int nextOrd;
                if (((Sample) d1).fx[i] < ((Sample) d2).fx[i]) {
                    nextOrd = -1 * order;
                } else if (((Sample) d1).fx == ((Sample) d2).fx) {
                    nextOrd = 0 * order;
                } else {
                    nextOrd = 1 * order;
                }
                if (ord == -1000)
                    ord = nextOrd;
                else{
                    if (ord != nextOrd)
                        return 0;                    
                }
            }
            return ord;
        }
    }            
    protected String[] efficiencyNames;    
    protected Set<SampleMO> bestSamples;        
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
        
        if (this.effMethodName == null)
            stop(JAMS.resources.getString("effMethod_not_specified"));
        if (this.effValue == null)
            stop(JAMS.resources.getString("effValue_not_specified"));
        if (this.mode == null)
            stop(JAMS.resources.getString("mode_not_specified"));
               
        m = effValue.length;
                
        StringTokenizer tok = new StringTokenizer(this.mode.getValue(),";");
        if (tok.countTokens() != m)
            stop(JAMS.resources.getString("efficiency_count_does_not_match_mode_count"));
        
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
                stop(JAMS.resources.getString("efficiency_count_does_not_effMethodName_mode_count"));
            }
        }           
        bestSamples = new HashSet<SampleMO>();
    }
                
    public SampleMO getSample(double[]x){
        return new SampleMO(x,funct(x));
    }
                  
    public Set<SampleMO> getParetoOptimalSet(Set<SampleMO> set){        
        SampleMOComperator comparer = new SampleMOComperator(true);
        HashSet<SampleMO> result = new HashSet<SampleMO>();
        Iterator<SampleMO> iter = set.iterator();
        while(iter.hasNext()){
            SampleMO candidate = iter.next();
            boolean isDominated = false;
            Iterator<SampleMO> iter2 = set.iterator();            
            while(iter2.hasNext()){
                SampleMO rivale = iter2.next();
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
    
    public double[] funct(double x[]) {     
        
        double value[] = new double[m];        
        if (GoalFunction == null) {          
            this.setParameters(x);         
            singleRun();       
            for (int i=0;i<m;i++)
                value[i] = effValue[i].getValue();                        
        } else {            
            value = GoalFunction.f(x);
        }
        currentSampleCount++;
        for (int i=0;i<m;i++)
            value[i] = this.transformByMode(value[i], iMode[i]);
        
        this.bestSamples.add(new SampleMO(x,value));
        this.bestSamples = this.getParetoOptimalSet(this.bestSamples);
        return value;
    }
}
