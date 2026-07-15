/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package optas.optimizer;

import jams.JAMS;
import optas.core.SampleLimitException;
import jams.model.Model;
import optas.optimizer.management.StringOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import optas.core.AbstractFunction;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory;
import optas.optimizer.management.Statistics;

/**
 *
 * @author Christian Fischer
 */
public abstract class Optimizer implements Serializable, Comparable{

    private String parameterNames[];
    protected double[] lowBound;
    protected double[] upBound;
    protected double x0[][];  //start value
    protected String objNames[];
    protected int n; //number of parameters
    protected int m; //number of obejectives
    static protected Random generator;
    private boolean debugMode = false;
    //function to be optimized
    private AbstractFunction function;
    //maximum number of function evalutations allowed    
    public double maxn;
    public boolean verbose = true;
    int sampleCount = 0;
    File workspace = null;    
    File outputFile = null;
    
    private ArrayList<Sample> solution=null;
    protected Model model = null;

    transient protected SampleFactory factory = new SampleFactory();

    @Override
    public int compareTo(Object o){
        return this.toString().compareTo(o.toString());
    }
    
    protected void log(String msg) {
        if (verbose){
            function.log(msg);
        }
    }

    public void injectSamples(Collection<? extends Sample> list) throws SampleLimitException, ObjectiveAchievedException{
        for (Sample s : list){
            if (isObjectiveAchieved(s))
                throw new ObjectiveAchievedException(s.x,s.F()); //to do!!
            this.factory.injectSample(s);
        }
        if (factory.getSampleList().size()>this.maxn)
            throw new SampleLimitException();
    }

    protected boolean isObjectiveAchieved(Sample s){
        return false;
    }

    public AbstractFunction getFunction(){
        return function;
    }

    public Statistics getStatistics(){
        return factory.getStatistics();
    }

    public Model getModel(){
        return model;
    }

    public void setModel(Model model){
        this.model = model;
    }

    public ArrayList<Sample> getSamples(){
        return factory.getSampleList();
    }

    public ArrayList<Sample> getSolution(){
        if (solution!=null)
            return solution;
        else{
            solution = this.factory.getStatistics().getParetoFront();
        }
        return solution;
    }

    public SampleSO getSampleSO(double x[]) throws SampleLimitException, ObjectiveAchievedException {
        return factory.getSampleSO(x, evaluate(x)[0]);
    }

    public Sample getSample(double x[]) throws SampleLimitException, ObjectiveAchievedException {
        return factory.getSample(x, evaluate(x));
    }

    private double[] evaluate(double x[]) throws SampleLimitException, ObjectiveAchievedException {
        if (sampleCount++ >= getMaxn()) {
            throw new SampleLimitException();
        }
        return function.evaluate(x);
    }

    public void setFunction(AbstractFunction function) {
        this.function = function;
        this.parameterNames = function.getInputFactorNames();
        this.objNames = function.getOutputFactorNames();
        this.n = function.getInputDimension();
        this.m = function.getOutputDimension();
        this.lowBound = new double[n];
        this.upBound  = new double[n];
        
        for (int i=0;i<n;i++){
            lowBound[i] = function.getRange()[i][0];
            upBound[i] = function.getRange()[i][1];
        }
    }


    public void setVerbose(boolean verbose){
        this.verbose = verbose;
    }

    public boolean getVerbose(){
        return this.verbose;
    }

    //{x00,x01,..x0n}
    //{x10,x11,..x1n}
    //...

    public void setStartValue(double startValue[][]) {
        x0 = startValue;
    }

    public void setStartValue(double startValue[]){
        x0 = new double[][]{startValue};
    }

    /*public void setInputDimension(int n) {
        this.n = n;
    }

    public void setOutputDimension(int m) {
        this.m = m;
    }

    public void setBoundaries(double lowBound[], double upBound[]) {
        this.lowBound = lowBound;
        this.upBound = upBound;
    }

    public void setParameterNames(String names[]) {
        this.parameterNames = names;
    }

    public void setObjectiveNames(String names[]) {
        this.objNames = names;
    }*/

    private Field getField(Class o, String name) {
        try {
            return o.getField(name);
        } catch (NoSuchFieldException e) {
            if (o.getSuperclass() == null) {
                return null;
            }
            return getField(o.getSuperclass(), name);
        }
    }

    public void setSetup(OptimizerParameter p) {
        Field f = getField(this.getClass(), p.getName());
        if (f==null){
            System.out.println("Field " + p.getName() + " not found!");
            return;
        }
        try {
            if (p instanceof BooleanOptimizerParameter) {
                f.setBoolean(this, ((BooleanOptimizerParameter) p).isValue());
            }
            if (p instanceof NumericOptimizerParameter) {
                try{
                f.setDouble(this, ((NumericOptimizerParameter) p).getValue());
                }catch(IllegalArgumentException iea){
                    try{
                        f.setFloat(this, (float)((NumericOptimizerParameter) p).getValue());
                    }catch(IllegalArgumentException iae){
                        f.setInt(this, (int)((NumericOptimizerParameter) p).getValue());
                    }
                }
            }
            if (p instanceof StringOptimizerParameter) {
                f.set(this, ((StringOptimizerParameter) p).getValue());
            }
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        }
    }

    public double getMaxn() {
        return maxn;
    }


    public void setMaxn(double maxn) {
        this.maxn = (int)maxn;
    }

    public void setDebugMode(boolean flag) {
        this.debugMode = flag;
    }
    
    protected boolean checkConfiguration() {
        log("Checking optimizer configuration");
        if (n == 0) {
            log("Failure: optimization cannot start, because the number of parameters in unknown");
            return false;
        }
        if (m == 0) {
            log("Failure: optimization cannot start, because the number of objectives in unknown");
            return false;
        }
        if (parameterNames == null || parameterNames.length != n) {
            parameterNames = new String[n];
            for (int i = 0; i < n; i++) {
                parameterNames[i] = "param_" + i;
            }
        }
        if (objNames == null || objNames.length != m) {
            objNames = new String[m];
            for (int i = 0; i < m; i++) {
                objNames[i] = "obj_" + i;
            }
        }
        if (lowBound.length != n || upBound.length != n) {
            log("Failure: optimization cannot start, because the number of bounds does not match");
            return false;
        }
        for (int i = 0; i < n; i++) {
            if (lowBound[i] > upBound[i]) {
                log("Failure: optimization cannot start, because an upper bound is lower than a lower bound!");
                return false;
            }
        }
        if (getMaxn() < 0) {
            setMaxn(500);
        }
        if (workspace == null) {
            log("Warning: workspace was not setup! Set to current directory");
            workspace = new File(System.getProperty("user.dir"));
        }        
        return true;
    }

    public boolean init() {
        log("Initialize Optimizer");
        if (!checkConfiguration())
            return false;

        if (!debugMode) {
            generator = new Random(System.nanoTime());
        } else {
            generator = new Random(0);
        }       
        return true;
    }

    protected double randomValue() {
        return generator.nextDouble();
    }

    protected double[] randomSampler() {    
        
        double[] sample = new double[n];
        for (int i = 0; i < n; i++) {
            sample[i] = (lowBound[i] + randomValue() * (upBound[i] - lowBound[i]));
        }
        return sample;
    }

    protected int getIterationCounter() {
        return this.factory.getSize();
    }

    public File getWorkspace() {
        return workspace;
    }

    public void setWorkspace(File ws){
        this.workspace = ws;
    }

    public void setOutputFile(String name){
        this.outputFile = new File(workspace.getAbsolutePath() + "/" + name);
    }

    protected int getMaximumIterationCount(){
        return (int)this.maxn;
    }

    public void printSamples() {
        log("Listing of all samples drawn during optimization");
        String line1="";
        String header = "";

        for (int i=0;i<n;i++){
            if (parameterNames[i]==null)
                parameterNames[i] = "p" + i;
            for (int j=0;j<parameterNames[i].length();j++)
                line1 += "-";
            line1 += "---";
            header += parameterNames[i] + "\t";
        }
        for (int i=0;i<m;i++){
            if (objNames[i]==null)
                objNames[i] = "obj" + "i";
            
            for (int j=0;j<objNames[i].length();j++){
                line1 += "-";
            }
            line1 += "---";
            header += objNames[i] + "\t";
        }
        log(line1);
        log(header);
        for (Sample s : this.factory.getSampleList()) {
            log(s.toString());
        }
        log(line1);
    }

    protected abstract void procedure() throws SampleLimitException, ObjectiveAchievedException;

    public ArrayList<Sample> optimize() {
        if (!init()){
            return null;
        }

        try {
            procedure();
        } catch (SampleLimitException sle) {
            log(JAMS.i18n("Optimization_finished_because_of_the_number_of_allowed_samples_is_reached"));
        } catch (ObjectiveAchievedException oae) {
            log(JAMS.i18n("Optimization_finished_because_the_objective_was_archieved!"));
        }
        printSamples();

        return getSolution();
    }

    public abstract OptimizerDescription getDescription();

    private void readObject(ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        objIn.defaultReadObject();

        factory = new SampleFactory();
    }
}
