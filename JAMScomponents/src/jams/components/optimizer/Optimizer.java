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
import jams.components.optimizer.SampleFactory.Sample;
import jams.workspace.stores.Filter;
import jams.workspace.stores.OutputDataStore;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.Double[] parameterIDs;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.String startValue;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public Attribute.String boundaries;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum numer of function evaluations",
            defaultValue = "1000"
            )
            public Attribute.Integer maxn;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Flag for enabling/disabling this sampler",
            defaultValue = "true"
            )
            public Attribute.Boolean enable;

     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "file for saving sample data"
            )
            public Attribute.String sampleDumpFile;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "current iteration"
            )
            public Attribute.Integer iterationCounter;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "file for saving sample data",
            defaultValue = "false"
            )
            public Attribute.Boolean debugMode;
                     
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

    public SampleFactory factory = new SampleFactory();

    protected Attribute.Double[] parameters;
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
        factory = new SampleFactory();
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
        return new AbstractTracer(this, store, Attribute.Integer.class) {
            @Override
            public void trace() {
                // check for filters on other contexts first
                for (Filter filter : store.getFilters()) {
                    String s = filter.getContext().getTraceMark();
                    //Matcher matcher = filter.getPattern().matcher(s);
                    if (!filter.isFiltered(s)) {
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
                for (Sample s : this.factory.sampleList)
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
