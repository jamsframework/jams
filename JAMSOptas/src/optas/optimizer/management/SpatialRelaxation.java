/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;

import jams.data.Attribute;
import java.util.Arrays;
import optas.optimizer.NelderMead;
import optas.optimizer.SampleLimitException;
import optas.optimizer.management.OptimizationController.OptimizationConfiguration;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class SpatialRelaxation {
    Attribute.Double relaxationParameter = null;
    int mainObjectiveIndex;

    public void setRelaxationParameter(Attribute.Double relaxationParameter){
        this.relaxationParameter = relaxationParameter;
    }
    public Attribute.Double getRelaxationParameter(){
        return relaxationParameter;
    }

    public void setMainObjectiveIndex(int mainObjectiveIndex){
        this.mainObjectiveIndex = mainObjectiveIndex;
    }
    public int getMainObjectiveIndex(){
        return mainObjectiveIndex;
    }
    //s1: find min
    //s2: find min - threshold

    //define rectangle of s2 and take all points which lie in this rectangle
    //s3: construct regression tree
    //s4: sample around min .. 100 points (better refining scheme)
    //s5: if classification successful .. finish goto s6 otherwise s3
    //s6: construct reg. tree
    //s7: if there are open borders of the tree extend area goto s4
    //s8: mark optimal area as NOGO goto s1
    public void applyProcedure(OptimizationConfiguration conf) {
        double relaxationValue = 1.0;

        Sample last = null;

        int count = 0;
        int n = conf.n();

        conf.log("########################################################");
        conf.log("Relaxation Optimization");
        conf.log("########");

        Sample currentBest = null;
        double currentSpace[][] = null;

        double defaultValue[] = new double[n];
        for (int i = 0; i < n; i++) {
            defaultValue[i] = Double.NaN;
        }

        while (relaxationValue > 0.0) {
            conf.log("#Using relaxaxtion value:" + relaxationValue);
            conf.log("#Optimize now...");
            conf.log("#Current iteration is:" + conf.getIterationCount());
            relaxationParameter.setValue(relaxationValue);

            optas.optimizer.Optimizer optimizer = null;
            if (relaxationValue < 0.8 && conf.getLocalSearchDuringRelaxation()) {
                optimizer = conf.loadOptimizer(NelderMead.class.getName());

                NelderMead nmOptimizer = (NelderMead) optimizer;
                nmOptimizer.setMax_restart_count(1.0);
                nmOptimizer.setEpsilon(0.01);
            } else {
                optimizer = conf.loadOptimizer(null);
            }

            if (currentSpace != null) {
                //Evaluate e = (Evaluate) optimizer.getFunction();
                /*optimizer.setBoundaries(
                (e.projectParametersetToSubSpace(currentSpace[0])),
                (e.projectParametersetToSubSpace(currentSpace[1])));*/ //changed for test
            }
            if (currentBest != null) {
                optimizer.setStartValue(currentBest.getParameter());
            }

            optimizer.optimize();

            last = (last == null) ? optimizer.getSamples().get(0) : last;

            conf.log("#...Finished");

            int lastIterations = conf.getIterationCount() - count;


            Statistics stat = optimizer.getStatistics();

            //Sample min = stat.getMinimumInRange(count, stat.size(), 0);
            Sample min = stat.getMin(0);
            currentBest = min;

            //currentSpace = stat.getParameterSpace(count, stat.size(), this.mainObjectiveIndex, 0.7);
            currentSpace = stat.getParameterSpace(0, stat.size(), this.mainObjectiveIndex, 0.7);

            //extend space a little bit
            for (int i = 0; i < n; i++) {
                double r = currentSpace[1][i] - currentSpace[0][i];
                currentSpace[0][i] = currentSpace[0][i] - 0.1 * r;
                currentSpace[1][i] = currentSpace[1][i] + 0.1 * r;
            }

            //öhm das müsste man mal testen ob das im allgemeinen auch so funktioniert, normalerweise interessiert für die parameter außerhalb
            //des subspaces die obere und untere grenze nicht
            conf.log("#new lower range:" + Arrays.toString((currentSpace[0])));
            conf.log("#new upper range:" + Arrays.toString((currentSpace[1])));

            conf.log("#Minimum:" + Arrays.toString((min.x)) + "Objective:" + Arrays.toString(min.F()));
            conf.log("#Geometric range:" + stat.calcGeometricRange(lastIterations));

            if (count != 0) {
                double dist = 0;
                for (int i = 0; i < n; i++) {
                    dist += (min.getParameter()[i] - last.getParameter()[i]) * (min.getParameter()[i] - last.getParameter()[i]);
                }
                dist = Math.sqrt(dist);


                conf.log("#Distance to last minimum:" + dist);
                conf.log("#Improvement:" + stat.calcImprovement(lastIterations, 0));

                //this is a very good question
                //optimizer.maxn =
            }

            double step = 0;

            if (conf.getAdaptiveRelaxation()) {
                //find step
                step = -relaxationValue / 2.0;
                double stepLength = Math.abs(step / 2.0);

                double adaption_best = 0.2, adaption_cur = 0;
                double epsilon_low = 0.02, epsilon_up = relaxationValue - 0.03, epsilon = 0.05;

                if (relaxationValue <= 0.000002) {
                    relaxationValue = 0;
                    step = 0;
                } else if (relaxationValue < epsilon) {
                    relaxationValue = 0.000001;
                    step = 0;
                } else {
                    while (Math.abs(step) > epsilon_low && Math.abs(step) < epsilon_up && stepLength > epsilon) {
                        this.relaxationParameter.setValue(relaxationValue + step);
                        try {//ich denke das ist falsch hier .. currentBest ist bereits im superspace ..
                            double result[] = conf.evaluate((currentBest.getParameter()));

                            adaption_cur = Math.abs(1.0 - (result[mainObjectiveIndex] / currentBest.F()[mainObjectiveIndex]));
                            if (adaption_cur > adaption_best) {
                                step = step + stepLength;
                            } else {
                                step = step - stepLength;
                            }
                            stepLength /= 2.0;
                        } catch (SampleLimitException sle) {
                            return;
                        } catch (ObjectiveAchievedException oae) {
                            return;
                        }
                    }
                    conf.log("Relexation was adapted to " + relaxationValue + " adapted rate was " + adaption_cur);
                }
            } else {
                step = -0.1;
            }

            relaxationValue = relaxationValue + step;
            count = stat.size();
        }
    }
}
