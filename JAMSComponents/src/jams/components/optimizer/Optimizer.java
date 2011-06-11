/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package jams.components.optimizer;

import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import jams.data.*;
import jams.dataaccess.DataAccessor;
import jams.io.datatracer.*;
import jams.model.Component;
import jams.model.JAMSContext;
import jams.model.JAMSVarDescription;
import jams.JAMS;
import jams.workspace.stores.Filter;
import jams.workspace.stores.OutputDataStore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum numer of function evaluations",
            defaultValue = "1000"
            )
            public Attribute.Integer maxn;

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
            description = "file for saving sample data"
            )
            public JAMSString sampleDumpFile;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "current iteration"
            )
            public JAMSInteger iterationCounter;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "file for saving sample data",
            defaultValue = "false"
            )
            public JAMSBoolean debugMode;
           
    protected List<Sample> sampleList = new ArrayList<Sample>();

    public class SampleLimitException extends Exception{
        String msg;
        SampleLimitException(String msg){
            this.msg = msg;
        }
        @Override
        public String toString(){
            return msg;
        }
    }
  
    public class ObjectiveAchievedException extends Exception{
        String msg;
        ObjectiveAchievedException(double value[], double target[]){
            this.msg = "Objectives " + Arrays.toString(target) + " is achieved with value " + Arrays.toString(value);
        }
        ObjectiveAchievedException(double value, double target){
            this.msg = "Objective " + target + " is achieved with value " + value;
        }
        @Override
        public String toString(){
            return msg;
        }
    }



    //class for representing samples
    public class Sample implements Serializable {
        private double[] x;
        protected double[] fx;

        public Sample(){}

        @SuppressWarnings("LeakingThisInConstructor")
        public Sample(double[] x, double fx[]) {
            this.fx = fx;
            if(x == null)
                return;
            this.x = new double[x.length];
            System.arraycopy(x, 0, this.x, 0, x.length);

            sampleList.add(this);
        }

        public double[] getParameter(){
            return x;
        }
        
        @Override
        public Sample clone(){
            Sample cpy = new Sample();
            cpy.x = new double[x.length];
            cpy.fx = new double[fx.length];
            System.arraycopy(x, 0, cpy.x, 0, x.length);
            System.arraycopy(fx, 0, cpy.fx, 0, fx.length);
            return cpy;
        }

        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof Sample))
                return false;
            Sample s = (Sample)obj;
            if (s.x.length != this.x.length)
                return false;
            if (s.fx.length != this.fx.length)
                return false;

            for (int i=0;i<this.x.length;i++){
                if (s.x[i]!=x[i])
                    return false;
            }
            return true;
        }
        //automatically gemerated
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Arrays.hashCode(this.x);
            hash = 79 * hash + Arrays.hashCode(this.fx);
            return hash;
        }

        @Override
        public String toString() {
            String s = "";
            for (int i = 0; i < x.length; i++) {
                s += x[i] + "\t";
            }
            for (int i = 0; i < fx.length; i++) {
                s += fx[i] + "\t";
            }
            return s;
        }
    }
    protected JAMSDouble[] parameters;
    protected double[] lowBound;
    protected double[] upBound;
    protected String dirName;
    //number of parameters!!
    public int n;
    //number of drawn samples    
    static protected Random generator;
    
    protected double x0[] = null;

    BufferedWriter sampleWriter;

    public Optimizer() {
    }

    protected void sayThis(String wordsToSay){
        if (this.getModel()==null){
            System.out.println(wordsToSay);
        }else
            this.getModel().getRuntime().sendInfoMsg(wordsToSay);
    }

    protected void stop(String wordsToSay){
        if (this.getModel()==null){
            System.err.println(wordsToSay);
            System.exit(0);
        }else
            this.getModel().getRuntime().sendHalt(wordsToSay);
    }

    @Override
    public void init() {
        if (getModel()!=null)
            super.init();
        
        if (!enable.getValue())
            return;
        if (debugMode!=null && !debugMode.getValue())
            generator = new Random(System.nanoTime());
        else
            generator = new Random(0);
        
        if (this.parameterIDs == null)
            stop(JAMS.i18n("parameterIDs_not_specified"));
        if (this.boundaries == null)
            stop(JAMS.i18n("parameter_boundaries_not_specified"));

        n = parameterIDs.length;
        parameters = parameterIDs;
        lowBound = new double[n];
        upBound = new double[n];
        
        StringTokenizer tok = new StringTokenizer(this.boundaries.getValue(),";");
        int i = 0;
        while (tok.hasMoreTokens()) {
            if (i>=n){
               stop(JAMS.i18n("too_many_boundaries"));
               return;
            }
            String key = tok.nextToken();
            key = key.substring(1, key.length() - 1);

            StringTokenizer boundTok = new StringTokenizer(key, ">");
            try{
                lowBound[i] = Double.parseDouble(boundTok.nextToken());
                upBound[i] = Double.parseDouble(boundTok.nextToken());
            }catch(NumberFormatException e){
                stop(JAMS.i18n("unsupported_number_format_found_for_lower_or_upper_bound"));
                return;
            }
            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                stop(JAMS.i18n("Component") + " " + this.getInstanceName() + ": " + JAMS.i18n("upBound_must_be_higher_than_lowBound"));
                return;
            }

            i++;
        }
        if (this.getModel()!=null)
            dirName = this.getModel().getWorkspaceDirectory().getPath();

        if (this.startValue!=null){
            x0 = new double[n];
            StringTokenizer tokStartValue = new StringTokenizer(startValue.getValue(),";");
            int counter = 0;
            while(tokStartValue.hasMoreTokens()){
                String param = tokStartValue.nextToken();
                try{
                    if (counter >= n){
                        counter = n+1;
                        break;
                    }
                    x0[counter] = Double.valueOf(param).doubleValue();
                    counter++;
                }catch(NumberFormatException e){
                    stop(JAMS.i18n("Component") + " " + this.getInstanceName() + ": " + JAMS.i18n("unparseable_number") + param);
                }
            }
            if (counter != n){
                stop(JAMS.i18n("Component") + " " + JAMS.i18n("startvalue_too_many_parameter"));
            }
        }

        if (this.sampleDumpFile!=null && !this.sampleDumpFile.getValue().equals("")){
            try{
                this.sampleWriter = new BufferedWriter(new FileWriter(new File(this.getModel().getWorkspacePath() + "/" + this.sampleDumpFile.getValue())));
            }catch(IOException ioe){
                sayThis(ioe.toString());
                this.sampleWriter = null;
            }
        }
        this.sampleList.clear();
        iterationCounter.setValue(0);
    }

    @Override
    public void setupDataTracer(){
        super.setupDataTracer();
        for (DataTracer dataTracer : dataTracers) {
            dataTracer.startMark();
        }
    }

    protected double randomValue(){
        return generator.nextDouble();
    }

    protected double[] RandomSampler() {
        double[] sample = new double[n];
        for (int i = 0; i < n; i++) {
            sample[i] = (lowBound[i] + randomValue() * (upBound[i] - lowBound[i]));
        }
        return sample;
    }
    
    String buildMark(){
        return Integer.toString(iterationCounter.getValue()) + "\t";
    }

    @Override
    public long getNumberOfIterations() {
        return this.maxn.getValue();
    }

    @Override
    protected AbstractTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, JAMSInteger.class) {
            @Override
            public void trace() {
                // check for filters on other contexts first
                for (Filter filter : store.getFilters()) {
                    String s = filter.getContext().getTraceMark();
                    Matcher matcher = filter.getPattern().matcher(s);
                    if (!matcher.matches()) {
                        return;
                    }
                }

                output(buildMark());
                for (DataAccessor dataAccessor : getAccessorObjects()) {
                    output(dataAccessor.getComponentObject());
                }
                nextRow();
                flush();
            }
        };
    }

    @Override
    public String getTraceMark() {
        return buildMark();
    }

    protected double transformByMode(double value,int mode){
        double bigNumber = 10000000;
        
        value = (Math.max(value, -bigNumber));
        value = (Math.min(value, bigNumber));
        if (Double.isNaN(value)) {
            value = (-bigNumber);
        }
        switch (mode) {
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
    
    protected void setParameters(double x[]) {
        for (int j = 0; j < parameters.length; j++) {
            try {
                parameters[j].setValue(x[j]);
            } catch (Exception e) {
                stop(JAMS.i18n("Error_Parameter_No") + " " + j + JAMS.i18n("wasnt_found") + " " + e.toString());
            }
        }
    }

    @Override
    public void run(){
        try{
            procedure();
        }catch(SampleLimitException e1){
            System.out.println(e1);
        }catch(ObjectiveAchievedException e2){
            System.out.println(e2);
        }
    }

    protected abstract void procedure() throws SampleLimitException, ObjectiveAchievedException;

    protected void singleRun() {
        if (!doRun)
            return;
        
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
        //close all input data stores, otherwise thousands of files will
        //opened while optimizing
        //getModel().getWorkspace().
    }
            
    @Override
    public void cleanup(){
        if (!enable.getValue())
            return;
        if (this.sampleWriter!=null)
            try{
                for (Sample s : this.sampleList)
                    sampleWriter.write(s + "\n");
                this.sampleWriter.close();
            }catch(IOException e){
                sayThis(e.toString());
            }
        for (DataTracer dataTracer : dataTracers) {
            dataTracer.endMark();
            dataTracer.close();
        }
    }
}
