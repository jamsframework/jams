/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;


import java.util.ArrayList;
import java.util.Arrays;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.SampleLimitException;
import optas.optimizer.management.OptimizationController.OptimizationConfiguration;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class NonUniquenessAssessor {

    public class NoGoZone{
        double[] midPoint;
        double[] mainAxis;
        double[] value;

        public NoGoZone(double[] midPoint, double[] mainAxis, double[] value){
            this.midPoint = midPoint;
            this.mainAxis = mainAxis;
            this.value = value;
        }
    }
    private ArrayList<NoGoZone> forbiddenArea = new ArrayList<NoGoZone>();

    double lowBound[], upBound[];

    private class EvaluateNGZs extends AbstractFunction  {
        OptimizationConfiguration conf;

        public EvaluateNGZs(OptimizationConfiguration conf) {
            this.conf = conf;
        }

        public void logging(String msg){
            conf.log(msg);
        }
        
        public double[] f(double[] x) throws ObjectiveAchievedException, SampleLimitException {
            double s[] = conf.evaluate(x);

            //bestrafen
            double penalty = 0;
            double PENALTY_VALUE = 10000.0;
            for (NoGoZone ngz : forbiddenArea){
                //double dist = d(ngz.midPoint, xSuper);

                for (int i=0;i<ngz.mainAxis.length;i++){
                    double dist = Math.abs((ngz.midPoint[i]-x[i])/(upBound[i]-lowBound[i]));
                    if (dist < ngz.mainAxis[i]){
                        penalty += ((ngz.mainAxis[i]/dist)-1.0)*PENALTY_VALUE;
                    }
                }
            }

            for (int i=0;i<s.length;i++)
                s[i]+=penalty;

            return s;
        }
    }

    public void applyProcedure(OptimizationConfiguration conf){

        double minimum = Double.POSITIVE_INFINITY;
        double currentMinimum = Double.NEGATIVE_INFINITY;
        double threshold = 0.8;

        lowBound = conf.getLowerBound();
        upBound = conf.getUpperBound();

        int n = conf.n();

        int maxAllowedFailures = 3;
        int failureCount = 0;
        while ( currentMinimum*threshold < minimum || failureCount < maxAllowedFailures){
            if (currentMinimum*threshold > minimum){
                failureCount++;
            }else{
                failureCount = 0;
            }

            optas.optimizer.Optimizer optimizer = conf.loadOptimizer(null);
            optimizer.setFunction(new EvaluateNGZs(conf));
            optimizer.optimize();

            ArrayList<Sample> optimizerSampleList = (ArrayList<Sample>)optimizer.getSamples().clone();
            Sample minSample = optimizer.getSolution().get(0);

            currentMinimum = minSample.fx[0];
            if (currentMinimum < minimum)
                minimum = currentMinimum;

            double limit = minimum/threshold;
            ArrayList<Sample> completeSampleList = new ArrayList<Sample>();
            //do only monte carlo sampling if minimum is good enough
            if (currentMinimum <= limit) {
                optas.optimizer.Optimizer mcSampler = conf.loadOptimizer(optas.optimizer.RandomSampler.class.getName());

                mcSampler.setMaxn(50.0 * n);

                double smallLowBound[] = new double[n];
                double smallUpBound[] = new double[n];

                for (int i = 0; i < n; i++) {
                    smallLowBound[i] = Math.max(minSample.x[i] - 0.1 * (upBound[i] - lowBound[i]), lowBound[i]);
                    smallUpBound[i] = Math.min(minSample.x[i] + 0.1 * (upBound[i] - lowBound[i]), upBound[i]);
                    if (smallLowBound[i] > smallUpBound[i]) {
                        smallLowBound[i] = smallUpBound[i];
                    }
                }

                mcSampler.setBoundaries(smallLowBound, smallUpBound);

                conf.log("#new lower range:" + Arrays.toString(smallLowBound));
                conf.log("#new upper range:" + Arrays.toString(smallUpBound));

                conf.log("#Minimum:"
                        + Arrays.toString(minSample.x)
                        + "Objective:" + Arrays.toString(minSample.F()));

                mcSampler.optimize();

                ArrayList<Sample> mcSampleList = (ArrayList<Sample>) mcSampler.getSamples().clone();


                completeSampleList.addAll(mcSampleList);
            }
            completeSampleList.addAll(optimizerSampleList);

            double mainAxis[] = new double[minSample.x.length];
            
            int counter=0;
            for (Sample x : completeSampleList){
                if (x.fx[0]>limit)
                    continue;

                for (int i=0;i<x.x.length;i++){
                    mainAxis[i] += Math.abs( (x.x[i] - minSample.x[i]) / (upBound[i]-lowBound[i]) );
                }
                counter++;
            }

            for (int i=0;i<mainAxis.length;i++){
                if (counter>0)
                    mainAxis[i] /= (double)counter;
                else
                    mainAxis[i]=0;
            }

            NoGoZone ngz = new NoGoZone(minSample.x, mainAxis, minSample.fx);
            forbiddenArea.add(ngz);

            System.out.println("Finished optimization run");
            System.out.println("Current Results:");
            for (NoGoZone ngz2 : forbiddenArea){
                System.out.println("Point:" + Arrays.toString(ngz2.midPoint));
                System.out.println("Radius:" + Arrays.toString(ngz2.mainAxis));
                System.out.println("Value:" + Arrays.toString(ngz2.value));
                System.out.println("");
            }

        }
    }
}
