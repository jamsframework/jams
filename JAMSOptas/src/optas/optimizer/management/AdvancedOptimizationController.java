/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.SortedMap;
import optas.metamodel.Optimization;
import optas.metamodel.OptimizationDescriptionDocument;
import optas.metamodel.Parameter;


import optas.optimizer.Optimizer;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.ParallelHaltonSequenceSampling;
import optas.optimizer.ParallelPostSampling2;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.SampleLimitException;

/**
 *
 * @author chris
 */
@JAMSComponentDescription(title = "OptimizationScheduler",
author = "Christian Fischer",
description = "Performs a chain of optimizations")
public class AdvancedOptimizationController extends OptimizationController {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String optimizationDescriptionFile;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double state;
    //ABDDDDDDCCCCCCCC
    final int SPATIAL_RELAXATION = 0x8000; //A
    final int SAMPLING = 0x4000;    //B
    final int MULTIMODECOUNTMASK = 0x00FF; //C
    final int MULTIMODECOUNTFACTOR = 0x0000;
    final int GROUPMASK = 0x3F00; //D
    final int GROUPFACTOR = 0x0100; //D
    OptimizationDescriptionDocument schedule;
    ArrayList<Optimizer> optimizerList = new ArrayList<Optimizer>();
    Random rnd = new Random();

    public class TabuZone implements Serializable{

        double[] midPoint;
        double[][] range;
        double[] value;

        public TabuZone(double[] midPoint, double[][] range, double[] value) {
            this.midPoint = midPoint;
            this.range = range;
            this.value = value;
        }
    }
    double[] fillingParamterSet = null;
    double quality = Double.POSITIVE_INFINITY;
    ArrayList<TabuZone> tabuZones = null;

    public class Evaluate extends AbstractFunction {

        int subn, subm;
        int parameterIndexMap[];
        int objectiveIndexMap[];
        double objectiveSet[];
        Optimizer owner;
        SampleFactory dummyFactory = new SampleFactory();
        public Evaluate(int subn, int subm, int pIndexMap[], int oIndexMap[]) {
            this.subn = subn;
            this.subm = subm;

            objectiveSet = new double[subm];

            parameterIndexMap = pIndexMap;
            objectiveIndexMap = oIndexMap;
        }

        public void logging(String msg) {
            AdvancedOptimizationController.this.getModel().getRuntime().println(msg);
        }

        public double[] projectParametersetToSuperSpace(double p[], double pDefault[]) {
            double image[] = new double[n];
            image = Arrays.copyOf(pDefault, n);

            for (int i = 0; i < subn; i++) {
                image[parameterIndexMap[i]] = p[i];
            }

            return image;
        }

        public double[] projectParametersetToSubSpace(double p[]) {
            double image[] = new double[subn];

            for (int i = 0; i < subn; i++) {
                image[i] = p[parameterIndexMap[i]];
            }

            return image;
        }

        protected Sample getSample(double[] x) throws ObjectiveAchievedException, SampleLimitException {
            double fillingParameterSet[] = Arrays.copyOf(fillingParamterSet, n);

            double xSuper[] = projectParametersetToSuperSpace(x, fillingParameterSet);
            
            Sample s = AdvancedOptimizationController.this.getSample(xSuper);

            return s;
        }

        @Override
        public double[] f(double[] x) throws ObjectiveAchievedException, SampleLimitException {
            //bestrafen
            double penalty[] = new double[subm];
            double PENALTY_VALUE = 50.0;
            for (TabuZone ngz : tabuZones) {
                //double dist = d(ngz.midPoint, xSuper);
                double min = Double.POSITIVE_INFINITY; //ngz ist rechteck!!
                for (int i = 0; i < ngz.midPoint.length; i++) {
                    double d = ngz.range[1][i] - ngz.range[0][i];
                    if (d==0)
                        d = 1;
                    double min2 = Math.min((x[i]-ngz.range[0][i])/d,(ngz.range[1][i]-x[i])/d);
                    min  = Math.min(min, min2);
                }
                if (min > -0.05) {
                    for (int i=0;i<subm;i++)
                        penalty[i] = ngz.value[i]+Math.atan(min+0.05)*PENALTY_VALUE;//((ngz.mainAxis[i]/dist)-1.0)*PENALTY_VALUE;
                }
            }

            if (penalty[0]>0){
                return penalty;
            }
            
            Sample s = getSample(x);

            double results[] = s.F();
            for (int i = 0; i < subm; i++) {
                objectiveSet[i] = results[objectiveIndexMap[i]];
            }

            return objectiveSet;
        }
    }

    public class AdvancedOptimizationConfiguration extends OptimizationConfiguration {

        private int parameterIndexMap[], objectiveIndexMap[];
        private int subn, subm;
        private String parameterNames[], objectiveNames[];

        private AdvancedOptimizationConfiguration(Optimization o) {
            super(o);

            subn = super.n();
            parameterNames = new String[subn];
            parameterIndexMap = new int[subn];

            for (int i = 0; i < o.getParameter().size(); i++) {
                Parameter p = o.getParameter().get(i);
                parameterNames[i] = p.getChildName();
                int id = p.getId();
                SortedMap<Integer, Parameter> headMap = schedule.getParameter().headMap(id);
                int index = headMap.size();
                parameterIndexMap[i] = index;
            }

            subm = super.m();
            objectiveIndexMap = new int[subm];
            objectiveNames = new String[subm];

            for (int i = 0; i < subm; i++) {
                objectiveIndexMap[i] = schedule.getObjective().headMap(o.getObjective().get(i).getId()).size();
                objectiveNames[i] = o.getObjective().get(i).toString();
            }
            //this is necessary because we need the projection sometimes
            this.evaluate = new Evaluate(subn, subm, parameterIndexMap, objectiveIndexMap);
        }

        @Override
        protected void setOptimizerParameter(Optimizer optimizer, Optimization o) {
            super.setOptimizerParameter(optimizer, o);
            //this is necessary because setOptimiterParameter überschreibt evaluate
            this.evaluate = new Evaluate(subn, subm, parameterIndexMap, objectiveIndexMap);
            optimizer.setFunction(evaluate);
        }

        public Evaluate getEvaluationProcedure() {
            return (Evaluate) this.evaluate;
        }
    }

    public OptimizationDescriptionDocument getSchedule() {
        try {
            File inFile = new File(getModel().getWorkspacePath() + "/" + this.optimizationDescriptionFile.getValue());
            XMLDecoder encoder = new XMLDecoder(
                    new BufferedInputStream(
                    new FileInputStream(inFile)));
            encoder.setExceptionListener(new ExceptionListener() {

                public void exceptionThrown(Exception e) {
                    getModel().getRuntime().sendHalt("Could not load optimization scheme XML:" + e.toString());
                }
            });

            schedule = (OptimizationDescriptionDocument) encoder.readObject();

            encoder.close();

            return schedule;
        } catch (IOException ioe) {
            System.out.println("Could not load optimization scheme XML:" + ioe.toString());
            return null;
        }
    }

    @Override
    public void init() {
        super.init();

        schedule = getSchedule();

        HashMap<Integer, Integer> oldNewIdMap = new HashMap<Integer, Integer>();
        SortedMap<Integer, Parameter> parameterMap = schedule.getParameter();

        this.fillingParamterSet = new double[n];
        for (Parameter p : parameterMap.values()) {
            for (int i = 0; i < n; i++) {
                if (this.names[i].compareTo(p.getChildName()) == 0) {
                    oldNewIdMap.put(p.getId(), i);
                    p.setId(i);
                    fillingParamterSet[i] = (p.getLowerBound() + p.getUpperBound()) / 2.0;
                }
            }
        }
        this.tabuZones = new ArrayList<TabuZone>();
    }

    @Override
    public void procedure() {
        this.log("Starting Schedule");

        Statistics lastStatistics = null;

        int iterationStart = 0;
        int iterationEnd   = 0;

        schedule = this.getSchedule();
        double bestSample[] = null;
        double bestSampleY[] = null;
        double startValue[][] = null;
        ArrayList<Sample> list = null;

        int groupIndex = 0;

        for (Optimization o : schedule.getOptimization()) {            
            AdvancedOptimizationConfiguration conf = new AdvancedOptimizationConfiguration(o);

            list = this.getStatistics().sampleList;
            startValue = new double[conf.subn][iterationEnd - iterationStart + 1];
            //transfer best parameter sets to next optimization group!
            if (bestSample != null) {
                for (int i = 0; i < conf.getEvaluationProcedure().subn; i++) {
                    startValue[i][0] = conf.getEvaluationProcedure().projectParametersetToSubSpace(bestSample)[i];
                    for (int j = iterationStart; j < iterationEnd; j++) {
                        double x0j[] = Arrays.copyOf(list.get(j).x, n);

                        for (int k = 0; k < n; k++) {
                            //check for default value and replace. this is important
                            if ((x0j[k] - (lowBound[k] + upBound[k]) / 2.0) < 1E-8) {
                                x0j[k] = this.rnd.nextDouble() * (upBound[k] - lowBound[k]) + lowBound[k];
                            }
                        }
                        double value[] = conf.getEvaluationProcedure().projectParametersetToSubSpace(list.get(j).x);
                        startValue[i][j + 1 - iterationStart] = value[i];
                    }
                }
            }
            //give them as start value
            for (int i = 0; i < startValue.length; i++) {
                o.getParameter().get(i).setStartValue(startValue[i]);
            }

            //reconfigurate optimizer, because of starting values
            conf = new AdvancedOptimizationConfiguration(o);

            iterationStart = this.getStatistics().size();

            this.log("Starting Optimization of Group #" + groupIndex + "!");

            if (o.getOptimizerDescription().getDoSpatialRelaxation().isValue()) {
                state.setValue(this.SPATIAL_RELAXATION + this.MULTIMODECOUNTFACTOR * 0 + this.GROUPFACTOR * groupIndex);
                SpatialRelaxation relaxation = new SpatialRelaxation();
                relaxation.setRelaxationParameter(relaxationParameter);
                lastStatistics = relaxation.applyProcedure(conf);

            } else {
                state.setValue(this.GROUPFACTOR * groupIndex + this.MULTIMODECOUNTFACTOR * 0);
                Optimizer optimizer = conf.loadOptimizer(null);
                optimizer.optimize();
                lastStatistics = optimizer.getStatistics();
            }

            iterationEnd = this.getStatistics().size();
            
            this.log("Finished Optimization of Group #" + groupIndex + "!");
            Sample min = lastStatistics.getMin(0); //is null korrekt??
            Sample max = lastStatistics.getMax(0);
            this.log("Best Sample:" + min.toString());
            this.log("Worst Sample:" + max.toString());

            bestSample = conf.getEvaluationProcedure().projectParametersetToSuperSpace(min.x, this.fillingParamterSet);
            bestSampleY = min.F();
                                    
            groupIndex++;
        }

/*        int multiModeMax = 20;
        int multiModeCount = 0;        
        
        while (multiModeCount < multiModeMax) {            
            Optimization lastOptimization = this.getSchedule().getOptimization().get(this.schedule.getOptimization().size() - 1);
            //step 1 find boundaries of optimal region
            this.log("Starting Assessment of optimal area in Mode " + multiModeCount + "!");

            state.setValue(multiModeCount + this.MULTIMODECOUNTFACTOR * multiModeCount + this.SAMPLING);
            
            try{
                OptimizerDescription desc = Tools.getStandardOptimizerDesc("threshold=0.1;maxn=6000");
                lastOptimization.setOptimizerDescription(desc);
            }catch(Exception e){
                e.printStackTrace();
                this.getModel().getRuntime().sendErrorMsg(e.toString());
            }

            AdvancedOptimizationConfiguration conf = new AdvancedOptimizationConfiguration(lastOptimization);
            //transfer best parameter sets to next optimization group!
            startValue = new double[conf.subn][1];
            for (int i = 0; i < conf.getEvaluationProcedure().subn; i++) {
                startValue[i][0] = conf.getEvaluationProcedure().projectParametersetToSubSpace(bestSample)[i];
            }

            //give them as start value
            for (int i = 0; i < startValue.length; i++) {
                lastOptimization.getParameter().get(i).setStartValue(startValue[i]);
            }
            lastOptimization.getOptimizerDescription().setOptimizerClassName(ParallelPostSampling2.class.getName());
            //recreate conf to take parameter startvalues into account
            conf = new AdvancedOptimizationConfiguration(lastOptimization);

            ParallelPostSampling2 sampler = (ParallelPostSampling2)conf.loadOptimizer(null);
            sampler.excludeFiles = "(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log";
            sampler.analyzeQuality = false;
            ArrayList<Sample> postSamplerResult = sampler.optimize();
            double l[][] = sampler.getFinalRange();

            bestSample = postSamplerResult.get(0).x;
            bestSampleY = postSamplerResult.get(0).fx;

            this.log("Finished Assessment of boundaries of optimal area in mode" + multiModeCount + "!");
            double projectedBestSet[] = conf.getEvaluationProcedure().projectParametersetToSuperSpace(bestSample,fillingParamterSet);
            double projectedLowBound[] = conf.getEvaluationProcedure().projectParametersetToSuperSpace(l[0],fillingParamterSet);
            double projectedUpBound[] = conf.getEvaluationProcedure().projectParametersetToSuperSpace(l[1],fillingParamterSet);

            double threshold[] = new double[bestSampleY.length];
            for (int i=0;i<threshold.length;i++)
                threshold[i] = bestSampleY[i]*1.1;

            this.log("Next Tabu - Zone is:\nnames:"+Arrays.toString(this.names)+
                    "\nx0:" + Arrays.toString(projectedBestSet) + "y:" + Arrays.toString(bestSampleY) +
                    "\nrectangle_min:" + Arrays.toString(projectedLowBound) +
                    "\nrectangle_max:" + Arrays.toString(projectedUpBound)  +
                    "threshold:" + Arrays.toString(threshold));

            //step 2 analyse optimal region
            //lastOptimization = this.getSchedule().getOptimization().get(this.schedule.getOptimization().size() - 1);
            
            //state.setValue(multiModeCount + this.MULTIMODECOUNTFACTOR * multiModeCount + this.SAMPLING);

            /*try{
                OptimizerDescription desc = Tools.getStandardOptimizerDesc("threadCount=32;maxn=2000");
                lastOptimization.setOptimizerDescription(desc);
                lastOptimization.getOptimizerDescription().setOptimizerClassName(ParallelHaltonSequenceSampling.class.getName());
            }catch(Exception e){
                e.printStackTrace();
                this.getModel().getRuntime().sendErrorMsg(e.toString());
            }
            for (int i=0;i<conf.subn;i++){
                lastOptimization.getParameter().get(i).setLowerBound(l[0][i]);
                lastOptimization.getParameter().get(i).setUpperBound(l[1][i]);
            }

            //recreate conf to take parameter startvalues into account
            //conf = new AdvancedOptimizationConfiguration(lastOptimization);
            //conf.loadOptimizer(null).optimize();

            this.log("Finished Assessment of optimal area in mode" + multiModeCount + "!");

            //step 3 add tabu zone
            tabuZones.add(new TabuZone(bestSample, l, bestSampleY));
            
            //step 4 find next mode
            this.log("Searching for next optimal mode!");
            lastOptimization = this.getSchedule().getOptimization().get(this.schedule.getOptimization().size() - 1);
            conf = new AdvancedOptimizationConfiguration(lastOptimization);

            startValue = new double[conf.subn][iterationEnd - iterationStart + 1];
            //transfer best parameter sets to next optimization group!
            if (bestSample != null && startValue != null) {
                for (int i = 0; i < conf.getEvaluationProcedure().subn; i++) {
                    startValue[i][0] = conf.getEvaluationProcedure().projectParametersetToSubSpace(bestSample)[i];
                    for (int j = iterationStart; j < iterationEnd; j++) {
                        double x0j[] = Arrays.copyOf(list.get(j).x, n);

                        for (int k = 0; k < n; k++) {
                            //check for default value and replace. this is important
                            if ((x0j[k] - (lowBound[k] + upBound[k]) / 2.0) < 1E-8) {
                                x0j[k] = this.rnd.nextDouble() * (upBound[k] - lowBound[k]) + lowBound[k];
                            }
                        }
                        double value[] = conf.getEvaluationProcedure().projectParametersetToSubSpace(list.get(j).x);
                        startValue[i][j + 1 - iterationStart] = value[i];
                    }
                }
            }
            //give them as start value
            for (int i = 0; i < startValue.length; i++) {
                lastOptimization.getParameter().get(i).setStartValue(startValue[i]);
            }
            conf = new AdvancedOptimizationConfiguration(lastOptimization);

            iterationStart = list.size();
            multiModeCount++;
            
            if (lastOptimization.getOptimizerDescription().getDoSpatialRelaxation().isValue()) {
                state.setValue(this.SPATIAL_RELAXATION + this.MULTIMODECOUNTFACTOR * multiModeCount);
                SpatialRelaxation relaxation = new SpatialRelaxation();
                relaxation.setRelaxationParameter(relaxationParameter);
                lastStatistics = relaxation.applyProcedure(conf);

            } else {
                state.setValue(this.MULTIMODECOUNTFACTOR * multiModeCount);
                Optimizer optimizer = conf.loadOptimizer(null);
                optimizer.optimize();
                lastStatistics = optimizer.getStatistics();
            }
            iterationEnd = list.size();
            this.log("Finished Optimization of mode" + multiModeCount + "!");

            Sample min = lastStatistics.getMin(0); //is null korrekt??
            Sample max = lastStatistics.getMax(0);
            this.log("Best Sample:" + min.toString());
            this.log("Worst Sample:" + max.toString());

            bestSample = conf.getEvaluationProcedure().projectParametersetToSuperSpace(min.x, this.fillingParamterSet);
            bestSampleY = min.F();                        
        }*/
        //ausgabe samples
        this.solution = DefaultDataFactory.getDataFactory().createEntity();
        solution.setObject("solution", getStatistics().getParetoFront());
    }
}
