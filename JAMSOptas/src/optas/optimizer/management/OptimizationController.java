/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;

import jams.data.Attribute;
import jams.model.JAMSVarDescription;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;
import optas.optimizer.Optimizer;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.SampleLimitException;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public abstract class OptimizationController extends OptimizerWrapper {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "representative objective")
    public Attribute.Integer mainObjective;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "parameter for relaxation control",
    defaultValue = "-1.0")
    public Attribute.Double relaxationParameter;
    
    private static int id = 0;
    int mainObjIndex;
    
    public class OptimizationConfiguration {
        protected Optimization o;
        protected AbstractFunction evaluate = null;

        protected double lowerBound[], upperBound[];
        protected double startValue[];
                
        private String parameterNames[],objectiveNames[];
        private int n,m;

        protected OptimizationConfiguration(Optimization o){
            this.o = o;

            n = o.getParameter().size();
            m = o.getObjective().size();

            startValue = new double[o.getParameter().size()];
            parameterNames = new String[n];

            this.lowerBound = new double[n];
            this.upperBound = new double[n];

            for (int i = 0; i < o.getParameter().size(); i++) {
                Parameter p = o.getParameter().get(i);
                lowerBound[i] = p.getLowerBound();
                upperBound[i] = p.getUpperBound();
                if (p.isStartValueValid()) {
                    startValue[i] = p.getStartValue();
                } else {
                    startValue[i] = (p.getLowerBound() + p.getUpperBound()) / 2.0;
                }
                parameterNames[i] = o.getParameter().get(i).getChildName();
            }

            m = o.getObjective().size();
            objectiveNames = new String[m];

            for (int i = 0; i < m; i++) {
                objectiveNames[i] = o.getObjective().get(i).toString();
            }
        }

        public void log(String msg) {
            this.evaluate.logging(msg);
        }

        public double[] evaluate(double[] x) throws ObjectiveAchievedException, SampleLimitException {
            return this.evaluate.f(x);
        }

        public double[] getLowerBound() {
            return this.lowerBound;
        }

        public double[] geUpperBound() {
            return this.upperBound;
        }

        public boolean getLocalSearchDuringRelaxation(){
            return this.o.getOptimizerDescription().getLocalSearchDuringRelaxation().isValue();
        }

        public boolean getAdaptiveRelaxation(){
            return this.o.getOptimizerDescription().getAdaptiveRelaxation().isValue();
        }
        public int n() {
            return lowerBound.length;
        }

        protected void setOptimizerParameter(Optimizer optimizer, Optimization o) {
            optimizer.setBoundaries(lowerBound, upperBound);
            optimizer.setInputDimension(n);
            optimizer.setOutputDimension(m);
            optimizer.setParameterNames(parameterNames);
            optimizer.setObjectiveNames(objectiveNames);
            optimizer.setWorkspace(getModel().getWorkspace().getDirectory());

            int c = 100000;
            int id_cpy = id;
            String counter = "";
            while (c > 0) {
                counter += (int) (id_cpy / c);
                c /= 10;
            }
            optimizer.setOutputFile("optimization_" + counter);
            optimizer.setStartValue(startValue);

            optimizer.setDebugMode(OptimizationController.this.debugMode.getValue());
            for (OptimizerParameter key : o.getOptimizerDescription().getPropertyMap()) {
                optimizer.setSetup(key);
            }

            this.evaluate = new AbstractFunction(){
                public void logging(String msg) {
                    OptimizationController.this.getModel().getRuntime().println(msg);
                }

                public double[] f(double[] x) throws ObjectiveAchievedException, SampleLimitException {
                    Sample s = OptimizationController.this.getSample(x);

                    return s.F();
                }
            };
            optimizer.setFunction(evaluate);
        }

        public Optimizer loadOptimizer(String className) {
            if (className == null) {
                Optimizer optimizer = OptimizerLibrary.loadOptimizer(OptimizationController.this.getModel().getRuntime(),
                        o.getOptimizerDescription().getOptimizerClassName());
                setOptimizerParameter(optimizer, o);
                return optimizer;
            } else {
                Optimizer optimizer = OptimizerLibrary.loadOptimizer(OptimizationController.this.getModel().getRuntime(),
                        className);
                setOptimizerParameter(optimizer, o);
                return optimizer;
            }
        }

        public int getIterationCount(){
            return OptimizationController.this.getIterationCount();
        }
    }
    abstract public void procedure();
}
