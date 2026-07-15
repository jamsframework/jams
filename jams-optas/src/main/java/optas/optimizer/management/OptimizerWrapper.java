/*
 * Optimizer.java
 *
 * Created on 8. Februar 2008, 10:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package optas.optimizer.management;

import optas.core.ObjectiveAchievedException;
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
import java.util.ArrayList;
import java.util.NoSuchElementException;

import java.util.regex.Matcher;
import optas.optimizer.management.SampleFactory.Sample;
import optas.core.SampleLimitException;

/**
 *
 * @author Christian Fischer
 */
public abstract class OptimizerWrapper extends JAMSContext {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "List of parameter identifiers to be sampled")
    public JAMSString parameterNames;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "List of parameter identifiers to be sampled")
    public JAMSDouble[] parameterIDs;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "List of parameter identifiers to be sampled",
    defaultValue="")
    public JAMSString startValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "List of parameter value bounaries corresponding to parameter identifiers")
    public JAMSString boundaries;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "current iteration")
    public JAMSInteger iterationCounter;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file for saving sample data",
    defaultValue = "false")
    public JAMSBoolean debugMode;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "objective function name")
    public Attribute.String effMethodName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "the prediction series")
    public Attribute.Double[] effValue;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "known optimal value",
    defaultValue = "-Infinity")
    public Attribute.DoubleArray target;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "stopping criterion if target is met",
    defaultValue = "-1")
    public Attribute.Double epsilonToTarget;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "solutions")
    public Attribute.Entity solution;

    protected String[] efficiencyNames;

    protected JAMSDouble[] parameter;
    protected String[] names;
    protected double[] lowBound;
    protected double[] upBound;
    //number of parameters!!
    public int n,m;
    protected double x0[][] = null;

    private SampleFactory factory = new SampleFactory();

    public OptimizerWrapper() {
    }
    
    @Override
    public void initAll() {
    }    

    @Override
    public void init() {
        if (getModel() == null) {
            System.out.println("Failure: Optimization Wrapper is not located inside a model!");
            return;
        }
        super.init();
        if (this.parameterIDs == null) {
            this.getModel().getRuntime().sendHalt(JAMS.i18n("parameterIDs_not_specified"));
        }
        if (this.boundaries == null) {
            this.getModel().getRuntime().sendHalt(JAMS.i18n("parameter_boundaries_not_specified"));
        }
        if (this.parameterNames == null) {
            this.getModel().getRuntime().sendInfoMsg("names of parameters are not provided!");
        }

        n = parameterIDs.length;
        parameter = parameterIDs;
        lowBound = new double[n];
        upBound = new double[n];

        StringTokenizer tok = new StringTokenizer(this.boundaries.getValue(), ";");
        int i = 0;
        while (tok.hasMoreTokens()) {
            if (i >= n) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("too_many_boundaries"));
                return;
            }
            String key = tok.nextToken();
            key = key.substring(1, key.length() - 1);

            StringTokenizer boundTok = new StringTokenizer(key, ">");
            try {
                lowBound[i] = Double.parseDouble(boundTok.nextToken());
                upBound[i] = Double.parseDouble(boundTok.nextToken());
            } catch (NumberFormatException e) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("unsupported_number_format_found_for_lower_or_upper_bound"));
                return;
            }
            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("Component") + " " + this.getInstanceName() + ": " + JAMS.i18n("upBound_must_be_higher_than_lowBound"));
                return;
            }

            i++;
        }
        if (parameterNames != null) {
            tok = new StringTokenizer(parameterNames.getValue(), ";");
            if (tok.countTokens() != n) {
                this.getModel().getRuntime().sendInfoMsg("wrong number of parameter names");
            }
            int c = 0;
            names = new String[n];
            while (tok.hasMoreTokens()) {
                names[c++] = tok.nextToken();
            }
        }
        if (this.startValue != null) {

            int counter = 0;
            StringTokenizer tokStartValue = new StringTokenizer(startValue.getValue(), ";");
            ArrayList<double[]> x0List = new ArrayList<double[]>();
            while (tokStartValue.hasMoreTokens()) {
                String param = tokStartValue.nextToken();
                param = param.replace("[", "");
                param = param.replace("]", "");
                StringTokenizer subTokenizer = new StringTokenizer(param, ",");
                double x0i[] = new double[subTokenizer.countTokens()];
                int subCounter = 0;
                if (x0i.length != n) {
                    this.getModel().getRuntime().sendHalt(JAMS.i18n("Component") + " " + JAMS.i18n("startvalue_too_many_parameter"));
                }
                while (subTokenizer.hasMoreTokens()) {
                    try {
                        x0i[subCounter] = Double.valueOf(subTokenizer.nextToken()).doubleValue();
                        subCounter++;
                    } catch (NumberFormatException e) {
                        this.getModel().getRuntime().sendHalt(JAMS.i18n("Component") + " " + this.getInstanceName() + ": " + JAMS.i18n("unparseable_number") + param);
                    }
                }
                x0List.add(x0i);
                counter++;
            }
            
            x0 = new double[x0List.size()][n];
            for (int j=0;j<x0List.size();j++){
                x0[j] = x0List.get(j);                
            }
        }
        if (this.effMethodName == null) {
            getModel().getRuntime().sendHalt(JAMS.i18n("effMethod_not_specified"));
        }
        if (this.effValue == null) {
            getModel().getRuntime().sendHalt(JAMS.i18n("effValue_not_specified"));
        }

        m = effValue.length;

        StringTokenizer effTok = new StringTokenizer(this.effMethodName.getValue(), ";");
        efficiencyNames = new String[m];
        for (int j = 0; j < m; j++) {
            try {
                efficiencyNames[j] = effTok.nextToken();
            } catch (NoSuchElementException e) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("efficiency_count_does_not_effMethodName_mode_count"));
            }
        }

        iterationCounter.setValue(0);
    }

    public Statistics getStatistics(){
        return this.factory.getStatistics();
    }

    public int getIterationCount(){
        return this.factory.getSize();
    }

    @Override
    public void setupDataTracer() {
        super.setupDataTracer();
        for (DataTracer dataTracer : dataTracers) {
            dataTracer.startMark();
        }
    }

    String buildMark() {
        return Integer.toString(iterationCounter.getValue()) + "\t";
    }

    @Override
    protected AbstractTracer createDataTracer(OutputDataStore store) {
        return new AbstractTracer(this, store, JAMSInteger.class) {

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

    public Sample getSample(int index){
        return this.factory.getSampleList().get(index);
    }

    public Sample getSample(double[] x) throws SampleLimitException, ObjectiveAchievedException {
        /*if (iterationCounter.getValue() >= this.maxn.getValue()) {
            throw new SampleLimitException("maximum sample count reached");
        }*/
        Sample s = factory.getSample(x, funct(x));

        return s;
    }

    private double[] funct(double x[]) throws ObjectiveAchievedException {

        double value[] = new double[m];

        this.setParameters(x);
        singleRun();
        for (int i = 0; i < m; i++) {
            value[i] = effValue[i].getValue();
        }


        this.iterationCounter.setValue(this.iterationCounter.getValue() + 1);

        if (target != null && this.target.getValue().length == effValue.length) {
            boolean objectiveAchieved = true;
            for (int i = 0; i < this.effValue.length; i++) {
                if (effValue[i].getValue() - target.getValue()[i] >= this.epsilonToTarget.getValue()) {
                    objectiveAchieved = false;
                }
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
    
    protected void setParameters(double x[]) {
        for (int j = 0; j < parameter.length; j++) {
            try {
                parameter[j].setValue(x[j]);
            } catch (Exception e) {
                e.printStackTrace();
                this.getModel().getRuntime().sendHalt(JAMS.i18n("Error_Parameter_No") + " " + j + " " + JAMS.i18n("wasnt_found") + " " + e.toString());
            }
        }
    }

    @Override
    public long getRunCount() {
        return this.iterationCounter.getValue();
    }    
    
    @Override
    public void run() {
        try {
            procedure();
        } catch (SampleLimitException e1) {
            System.out.println(e1);
        } catch (ObjectiveAchievedException e2) {
            System.out.println(e2);
        }
    }

    protected abstract void procedure() throws SampleLimitException, ObjectiveAchievedException;

    protected void singleRun() {
        if (!doRun) {
            return;
        }

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
                comp.initAll();
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

        for (DataTracer dataTracer : dataTracers) {
            dataTracer.trace();
        }
    }

    @Override
    public void cleanup() {
        for (DataTracer dataTracer : dataTracers) {
            dataTracer.endMark();
            dataTracer.close();
        }
    }
}
