/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;

import optas.core.ObjectiveAchievedException;
import jams.data.Attribute;
import jams.model.JAMSVarDescription;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import optas.core.AbstractFunction;
import optas.gui.wizard.Objective;
import optas.gui.wizard.Optimization;
import optas.gui.wizard.Parameter;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.core.SampleLimitException;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public abstract class OptimizationController extends OptimizerWrapper {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "parameter for relaxation control",
    defaultValue = "-1.0")
    public Attribute.Double relaxationParameter;

    private static int id = 0;
        
    File logFile = null;
    transient BufferedWriter writer = null;

    protected void log(String msg) {
        if (writer != null) {
            try {
                writer.write(msg);
                this.getModel().getRuntime().println(msg);
                writer.newLine();
            } catch (IOException ioe) {
            }
        }else{
            this.getModel().getRuntime().println(msg);
        }
    }

    @Override
    public void init(){
        super.init();

        if (logFile == null) {
            log("Warning: output file was not setup! Set to optimization.log");
            this.logFile = new File(this.getModel().getWorkspacePath()  + "/" + "optimization.log");
        }
        try {
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    public void cleanup(){
        try{
            if (writer!=null){
                writer.close();
                writer = null;
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe);
        }
    }
    public class OptimizationConfiguration implements Serializable {
        protected Optimization o;
        protected AbstractFunction evaluate = null;

        protected double lowerBound[], upperBound[];
        protected double startValue_transposed[][];
                
        private String parameterNames[],objectiveNames[];
        private int n,m;

        protected OptimizationConfiguration(Optimization o){
            this.o = o;

            n = o.getParameter().size();
            m = o.getObjective().size();

            startValue_transposed = new double[o.getParameter().size()][];
            parameterNames = new String[n];

            this.lowerBound = new double[n];
            this.upperBound = new double[n];

            int i=0;
            for (Parameter p : o.getParameter()) {                
                lowerBound[i] = p.getLowerBound();
                upperBound[i] = p.getUpperBound();                
                startValue_transposed[i] = p.getStartValue();
                parameterNames[i] = p.getAttributeName();
                i++;
            }

            m = o.getObjective().size();
            objectiveNames = new String[m];

            i=0;
            for (Objective obj : o.getObjective()) {         
                objectiveNames[i++] = obj.toString();
            }
        }

        public void log(String msg) {
            if (evaluate != null)
                this.evaluate.log(msg);
        }

        public double[] evaluate(double[] x) throws ObjectiveAchievedException, SampleLimitException {
            return this.evaluate.evaluate(x);
        }

        public double[] getLowerBound() {
            return this.lowerBound;
        }

        public double[] getUpperBound() {
            return this.upperBound;
        }

        public boolean getLocalSearchDuringRelaxation(){
            return this.o.getOptimizerDescription().getLocalSearchDuringRelaxation().isValue();
        }

        public boolean getAdaptiveRelaxation(){
            return this.o.getOptimizerDescription().getAdaptiveRelaxation().isValue();
        }
        public int n() {
            return n;
        }
        public int m() {
            return m;
        }

        protected void setOptimizerParameter(Optimizer optimizer, Optimization o) {
            /*optimizer.setBoundaries(lowerBound, upperBound);
            optimizer.setInputDimension(n);
            optimizer.setOutputDimension(m);
            optimizer.setParameterNames(parameterNames);
            optimizer.setObjectiveNames(objectiveNames);*/
            optimizer.setWorkspace(getModel().getWorkspace().getDirectory());

            int c = 100000;
            int id_cpy = id;
            String counter = "";
            while (c > 0) {
                counter += (int) (id_cpy / c);
                c /= 10;
            }
            optimizer.setOutputFile("optimization_" + counter);
            
            if (startValue_transposed.length>0){
                double startValue[][] = new double[startValue_transposed[0].length][startValue_transposed.length];
                for (int i=0;i<startValue.length;i++){
                    for (int j=0;j<startValue[i].length;j++)
                        startValue[i][j] = startValue_transposed[j][i];
                }
                optimizer.setStartValue(startValue);
            }

            optimizer.setDebugMode(OptimizationController.this.debugMode.getValue());
            for (OptimizerParameter key : o.getOptimizerDescription().getPropertyMap().values()) {
                optimizer.setSetup(key);
            }

            this.evaluate = new AbstractFunction(){
                @Override
                public void log(String msg) {
                    OptimizationController.this.getModel().getRuntime().println(msg);
                }

                @Override
                public String[] getOutputFactorNames(){
                    return objectiveNames;
                }
                
                @Override
                public String[] getInputFactorNames(){
                    return parameterNames;
                }
                
                @Override
                public int getInputDimension(){
                    return n;
                }
                
                @Override
                public int getOutputDimension(){
                    return m;
                }
                
                @Override
                public double[][] getRange(){
                    double range[][] = new double[n][2];
                    for (int i=0;i<n;i++){
                        range[i][0] = lowBound[i];
                        range[i][1] = upBound[i];
                    }
                    return range;
                }
                
                @Override
                public double[] evaluate(double[] x) throws SampleLimitException, ObjectiveAchievedException {
                    Sample s = OptimizationController.this.getSample(x);

                    return s.F();
                }
            };
            optimizer.setFunction(evaluate);
        }

        public Optimizer loadOptimizer(String className) {
            log("Load optimizer: Class: "+ o.getOptimizerDescription().getOptimizerClassName());
            if (className == null) {
                Optimizer optimizer = OptimizerLibrary.loadOptimizer(OptimizationController.this.getModel().getRuntime(),
                        o.getOptimizerDescription().getOptimizerClassName());
                setOptimizerParameter(optimizer, o);
                optimizer.setModel(OptimizationController.this.getModel());
                return optimizer;
            } else {
                Optimizer optimizer = OptimizerLibrary.loadOptimizer(OptimizationController.this.getModel().getRuntime(),
                        className);
                setOptimizerParameter(optimizer, o);
                optimizer.setModel(OptimizationController.this.getModel());
                return optimizer;
            }
        }

        public int getIterationCount(){
            return OptimizationController.this.getIterationCount();
        }
    }
    abstract public void procedure();
}
