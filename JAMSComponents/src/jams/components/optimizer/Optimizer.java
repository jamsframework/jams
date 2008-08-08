/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package jams.components.optimizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSContext;
import org.unijena.jams.model.Snapshot;

/**
 *
 * @author Christian Fischer
 */
public abstract class Optimizer extends JAMSContext {

    static final public int MODE_MAXIMIZATION = 2;
    static final public int MODE_MINIMIZATION = 1;
    static final public int MODE_ABSMAXIMIZATION = 3;
    static final public int MODE_ABSMINIMIZATION = 4;

    /*************************
     * first some very useful nested classes     
     *************************/    //capsulating class for goal functions
    public abstract class AbstractFunction {

        public abstract double f(double x[]);
    }
    //class for representing samples
    static public class Sample {

        public double[] x;
        public double fx;
        static public BufferedWriter writer = null;
        static public Vector<Sample> SampleList = null;

        public Sample(double[] x, double fx) {
            this.x = new double[x.length];
            for (int i = 0; i < x.length; i++) {
                this.x[i] = x[i];
            }
            this.fx = fx;
            if (SampleList != null) {
                SampleList.add(this);
            } else {
                SampleList = new Vector<Sample>();
            }
            try {
                if (writer != null) {
                    writer.write(this.toString() + "\n");
                    writer.flush();
                }
            } catch (Exception e) {
                System.out.println(e.toString());
            }
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
    }    //end of nested classes
    JAMSDouble[] parameters;
    String[] parameterNames;
    String dirName;
    JAMSDouble effValue;
    double[] lowBound;
    double[] upBound;    //number of parameters!!
    public int n;
    int mode;
    int currentSampleCount;
    Random generator = new Random();
    AbstractFunction GoalFunction = null;
    JAMSEntity snapshot = null;

    /** Creates a new instance of Optimizer */
    public Optimizer() {
    }

    public void init(String parameterIDs, String boundaries, String dirName, JAMSDouble effValue, int mode) {
        init( parameterIDs,  boundaries,  dirName,  effValue,  mode,null);
    }
    
    public void RefreshDataHandles(){
        for (int i=0;i<parameterNames.length;i++){
            parameters[i] = (JAMSDouble) getModel().getRuntime().getDataHandles().get(parameterNames[i]);
        }
    }
    
    public void init(String parameterIDs, String boundaries, String dirName, JAMSDouble effValue, int mode,JAMSEntity snapshot) {
        this.snapshot = snapshot;
        this.mode = mode;
        //retreiving parameter names
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs, ";");
        String key;
        parameters = new JAMSDouble[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];

        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;            
            i++;
        }
        RefreshDataHandles();
        //retreiving boundaries
        tok = new StringTokenizer(boundaries, ";");
        int n = tok.countTokens();
        lowBound = new double[n];
        upBound = new double[n];

        //check if number of parameter ids and boundaries match
        if (n != i) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": Different number of parameterIDs and boundaries!");
        }

        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            key = key.substring(1, key.length() - 1);

            StringTokenizer boundTok = new StringTokenizer(key, ">");
            lowBound[i] = Double.parseDouble(boundTok.nextToken());
            upBound[i] = Double.parseDouble(boundTok.nextToken());

            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
            }

            i++;
        }

        this.n = this.parameters.length;
        this.dirName = dirName;
        currentSampleCount = 0;
        this.effValue = effValue;
    }

    protected double[] RandomSampler() {
        double[] sample = new double[n];

        for (int i = 0; i < n; i++) {
            double d = generator.nextDouble();
            sample[i] = (lowBound[i] + d * (upBound[i] - lowBound[i]));
        }
        return sample;
    }

    public double funct(double x[]) {
        double value = 0;
        //unbedingt bessere variante finden!!                
        if (snapshot != null) {
            if (!this.snapshot.existsAttribute("snapshot")){
                if (this instanceof SimpleSCE){
                    SimpleSCE s = (SimpleSCE)this;
                    snapshot = s.snapshot;
                }
                if (this instanceof BranchAndBound){
                    BranchAndBound s = (BranchAndBound)this;
                    snapshot = s.snapshot;
                }
            }
            try {
                this.getModel().SetModelState((Snapshot) snapshot.getObject("snapshot"));
            } catch (Exception e) {
                this.getModel().getRuntime().sendHalt(e.toString());                
            }
        }
        RefreshDataHandles();
        if (GoalFunction == null) {
            for (int j = 0; j < parameters.length; j++) {
                parameters[j].setValue(x[j]);
            }            
            singleRun();
            
            value = this.effValue.getValue();
                        
            if (effValue.getValue() < -10000000.0) {
                effValue.setValue(-10000000.0);
            }
            if (effValue.getValue() > 10000000.0) {
                effValue.setValue(10000000.0);
            }
            if (Double.isNaN(effValue.getValue())) {
                effValue.setValue(-100000000000.0);
            }
            if (this instanceof SimpleSCE){
                SimpleSCE s = (SimpleSCE)this;
                value = s.effValue.getValue();
            }
            if (this instanceof BranchAndBound){
                BranchAndBound s = (BranchAndBound)this;
                value = s.effValue.getValue();
            }
        } else {
            this.currentSampleCount++;
            value = GoalFunction.f(x);
        }

        if (mode == MODE_MINIMIZATION) {
            return value;
        } else if (mode == MODE_ABSMINIMIZATION) {
            return Math.abs(value);
        } else if (mode == MODE_ABSMAXIMIZATION) {
            return -Math.abs(value);
        } else if (mode == MODE_MAXIMIZATION) {
            return -value;
        } else {
            return 0;
        }
    }
    /*
    public double funct(double x[]) {
    double value = 0;        
    for (int j=0;j<parameters.length;j++) {
    if (parameters[j] == null)
    System.out.println("Parameter " + j + " konnte nicht gefunden werden!");
    parameters[j].setValue(x[j]);            
    }
    
    //model run ... wert liegt zwische -unendl und 1 --> log transformieren
    singleRun();
    
    if (effValue.getValue() < -10000.0){
    effValue.setValue(-10000.0);
    }
    if (Double.isNaN(effValue.getValue())){
    effValue.setValue(-10000.0);
    }
    
    return effValue.getValue();//Math.log(-effValue.getValue() + 1.0);//effValue.getValue();
    
    /*
    if (this.mode.getValue() == MODE_MINIMIZATION)
    return this.effValue.getValue();
    else if (mode.getValue() == MODE_ABSMINIMIZATION)
    return Math.abs(this.effValue.getValue());
    else if (mode.getValue() == MODE_ABSMAXIMIZATION)
    return -Math.abs(this.effValue.getValue());
    else if (mode.getValue() == MODE_MAXIMIZATION)
    return -this.effValue.getValue();
    else
    return 0;
    }*/

    public void WriteRegularSampling(String file, int index1, int index2) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(this.dirName + "\\" + file));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
        double x[] = this.RandomSampler();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                x[index1] = this.lowBound[index1] + (double) i * (this.upBound[index1] - this.lowBound[index1]) / 50.0;
                x[index2] = this.lowBound[index2] + (double) j * (this.upBound[index2] - this.lowBound[index2]) / 50.0;
                double y = funct(x);
                try {
                    writer.write(y + "\t");
                } catch (Exception e) {
                    System.out.println("Fehler" + e.toString());
                }
            }
            try {
                writer.write("\n");
            } catch (Exception e) {
                System.out.println("Fehler" + e.toString());
            }
        }
        try {
            writer.close();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    protected void singleRun() {
        this.currentSampleCount++;
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        runEnumerator.reset();
        while (runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
