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
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import jams.data.*;
import jams.dataaccess.DataAccessor;
import jams.io.DataTracer.*;
import jams.model.Component;
import jams.model.JAMSContext;
import jams.model.Snapshot;
import jams.model.JAMSVarDescription;
import jams.workspace.stores.OutputDataStore;
import java.util.regex.Matcher;

/**
 *
 * @author Christian Fischer
 */
public abstract class Optimizer extends JAMSContext {   
    static final public int MODE_MINIMIZATION = 1;
    static final public int MODE_MAXIMIZATION = 2;    
    static final public int MODE_ABSMAXIMIZATION = 3;
    static final public int MODE_ABSMINIMIZATION = 4;

    public Vector<Sample> sampleList = new Vector<Sample>();
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSDouble[] parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString startValue;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
           
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
            public JAMSInteger mode;
          
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum numer of function evaluations",
            defaultValue = "1000"
            )
            public JAMSInteger maxn;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling/disabling this sampler",
            defaultValue = "true"
            )
            public JAMSBoolean enable;
                
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "if you dont want to execute the jams model completly in every iteration, you can specify a JAMS - Snapshot which is loaded before execution"
            )
            public JAMSEntity snapshot;
    /*************************
     * first some very useful nested classes     
     *************************/ 
    public static abstract class AbstractFunction {
        public abstract double f(double x[]);
    }
    
    //class for representing samples
    public class Sample {
        public double[] x;
        public double fx;
        
        public Sample(){}
        public Sample(double[] x, double fx) {
            this.fx = fx;
            if(x == null)
                return;
            this.x = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                this.x[i] = x[i];
            }                        
            sampleList.add(this);            
        }

        public Sample clone(){
            Sample cpy = new Sample();
            cpy.x = new double[x.length];            
            for (int i=0;i<x.length;i++)
                cpy.x[i] = x[i];
            cpy.fx = fx;
            return cpy;
        }
        
        public String toString() {
            String s = "";
            for (int i = 0; i < x.length; i++) {
                s += x[i] + "\t";
            }
            return (s += fx);
        }
    }
    //compare samples
    static public class SampleComperator implements Comparator {

        private int order = 1;

        public SampleComperator(boolean decreasing_order) {
            order = decreasing_order ? -1 : 1;
        }

        public int compare(Object d1, Object d2) {
            if (((Sample) d1).fx < ((Sample) d2).fx) {
                return -1 * order;
            } else if (((Sample) d1).fx == ((Sample) d2).fx) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }        
    protected JAMSDouble[] parameters;
    protected String[] parameterNames;
    protected double[] lowBound;
    protected double[] upBound;    
    protected String dirName;
    
    //number of parameters!!
    public int n;
    //optimization mode
    
    //number of drawn samples
    protected int currentSampleCount;
    
    static protected Random generator = new Random();
    protected AbstractFunction GoalFunction = null;
        
    protected int iterationCounter = 0;
    
    protected double x0[] = null;
    
    public Optimizer() {
    }
                    
    public void init() {   
        super.init();        
        if (!enable.getValue())
            return;            
        
        if (this.parameterIDs == null)
            getModel().getRuntime().sendHalt("parameterIDs not specified!");
        if (this.boundaries == null)
            getModel().getRuntime().sendHalt("boundaries not specified!");
        if (this.effMethodName == null)
            getModel().getRuntime().sendHalt("effMethod not specified!");
        if (this.effValue == null)
            getModel().getRuntime().sendHalt("effValue not specified!");
        if (this.mode == null)
            getModel().getRuntime().sendHalt("mode not specified!");
        
        currentSampleCount = 0;
        
        n = parameterIDs.length;
        parameters = parameterIDs;        
        parameterNames = new String[n];
        lowBound = new double[n];
        upBound = new double[n];
        
        StringTokenizer tok = new StringTokenizer(this.boundaries.getValue(),";");
        int i = 0;
        while (tok.hasMoreTokens()) {
            if (i>=n){
               getModel().getRuntime().sendHalt("too many boundaries!"); 
               return;
            }
            String key = tok.nextToken();
            key = key.substring(1, key.length() - 1);

            StringTokenizer boundTok = new StringTokenizer(key, ">");
            try{
                lowBound[i] = Double.parseDouble(boundTok.nextToken());
                upBound[i] = Double.parseDouble(boundTok.nextToken());
            }catch(NumberFormatException e){
                getModel().getRuntime().sendHalt("illegal number format found for lower or upper bound!");
                return;
            }

            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
                return;
            }

            i++;
        }  
        dirName = this.getModel().getWorkspaceDirectory().getPath();
        
        if (this.startValue!=null){
            x0 = new double[n];
            StringTokenizer tokStartValue = new StringTokenizer(startValue.getValue(),";");
            int counter = 0;
            while(tokStartValue.hasMoreTokens()){
                String param = tokStartValue.nextToken();
                try{
                    if (counter >= n){
                        getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": startvalue, too many parameter");
                        break;
                    }
                    x0[counter] = Double.valueOf(param).doubleValue();                                        
                    counter++;
                }catch(NumberFormatException e){
                    getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": unparseable number: " + param);
                }
            }
            if (counter != n){
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": startvalue, not enough parameters");
            }                        
        }
    }

    @Override
    public void setupDataTracer(){
        super.setupDataTracer();
        for (DataTracer dataTracer : dataTracers) {
            dataTracer.startMark();            
        }
    }
    
    protected double[] RandomSampler() {
        double[] sample = new double[n];

        for (int i = 0; i < n; i++) {
            sample[i] = (lowBound[i] + generator.nextDouble() * (upBound[i] - lowBound[i]));
        }
        return sample;
    }
    
    public Sample getSample(double[]x){
        return new Sample(x,funct(x));
    }

    String buildMark(){
        /*double parameter_double[] = new double[parameters.length];
        for (int i=0;i<parameter_double.length;i++)
            parameter_double[i] = parameters[i].getValue();
        return new Sample(parameter_double,effValue.getValue()).toString();        */
        return Integer.toString(currentSampleCount) + "\t";
    }
    
    @Override
    public long getNumberOfIterations() {
        return -1;
    }
    
    @Override
    protected AbstractTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, Sample.class) {
            @Override
            public void trace() {
                // check for filters on other contexts first
                for (OutputDataStore.Filter filter : store.getFilters()) {
                    String s = filter.getContext().getTraceMark();
                    Matcher matcher = filter.getPattern().matcher(s);
                    if (!matcher.matches()) {
                        return;
                    }
                }
                
                output(buildMark());
                for (DataAccessor dataAccessor : getAccessorObjects()) {
                    output(dataAccessor.getComponentObject());
                    output("\t");
                }
                output("\n");
                flush();
            }
        };
    }
    
    @Override
    public String getTraceMark() {
        return buildMark();
    }
    
    public double funct(double x[]) {     
        
        double value = 0.0;     
        
        if (snapshot != null) {
            if (!this.snapshot.existsAttribute("snapshot")){
                try {
                    this.getModel().setModelState((Snapshot) snapshot.getObject("snapshot"));
                } catch (Exception e) {
                    this.getModel().getRuntime().sendHalt(e.toString());                
                } 
            }            
        }        
        if (GoalFunction == null) {
            //RefreshDataHandles();
            for (int j = 0; j < parameters.length; j++) {
                try{
                    parameters[j].setValue(x[j]);
                }catch(Exception e){
                    getModel().getRuntime().sendHalt("Error! Parameter No. " + j + " wasn^t found" + e.toString());
                }
            }            
            singleRun();
            
            value = this.effValue.getValue();
            //sometimes its a bad idea to calculate with NaN or Infty
            double bigNumber = 10000000;
            
            effValue.setValue(Math.max(effValue.getValue(), -bigNumber));
            effValue.setValue(Math.min(effValue.getValue(),  bigNumber));
                        
            if (Double.isNaN(effValue.getValue())) {
                effValue.setValue(-bigNumber);
            }            
        } else {            
            value = GoalFunction.f(x);
        }
        currentSampleCount++;
        
        switch(mode.getValue()){
            case MODE_MINIMIZATION:
                return value;
            case MODE_MAXIMIZATION:
                return -value;
            case MODE_ABSMINIMIZATION:
                return Math.abs(value);
            case MODE_ABSMAXIMIZATION:
                return -Math.abs(value);
            default:
                return 0.0;
        }        
    }

    protected void singleRun() {
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }        
        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {                
                getModel().getRuntime().sendHalt(e.getMessage());
                e.printStackTrace();
            }
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                getModel().getRuntime().sendHalt(e.toString());
                e.printStackTrace();
            }
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                getModel().getRuntime().sendHalt(e.getMessage());
                e.printStackTrace();
            }
        }
        updateEntityData();
        if (enable.getValue())  { 
            for (DataTracer dataTracer : dataTracers) {
                dataTracer.trace();
            }
        }
    }
    @Override
    public void cleanup(){
        if (!enable.getValue())
            return;            
        for (DataTracer dataTracer : dataTracers) {
            dataTracer.endMark();
            dataTracer.close();
        }
    }
}
