/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import jams.data.Attribute;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import java.util.Arrays;
import optas.hydro.data.Calculations;
import optas.metamodel.Objective;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;
import optas.optimizer.Optimizer;
import optas.optimizer.ParallelHaltonSequenceSampling;


import optas.optimizer.management.OptimizationController.OptimizationConfiguration;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
@JAMSComponentDescription(title = "OptimizationScheduler",
author = "Christian Fischer",
description = "Performs a chain of optimizations")
public class ParameterSpaceReducer extends OptimizationController {        
    Optimization optimization = null;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "minimal allowed change in volume")
    public Attribute.Double epsilon1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "minimal allowed change in volume")
    public Attribute.Double threshold;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "minimal number of samplings")
    public Attribute.Double minSamplingPerIteration;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "minimal number of samplings")
    public Attribute.Double maxSamplingPerIteration;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "number of cores to use",
    defaultValue = "2")
    public Attribute.Integer cores;

    double initialLowerBound[], initialUpperBound[];

    @Override
    public void init() {
        super.init();

        Optimization o = new Optimization();
        for (int i=0;i<this.m;i++){
            Objective obj = new Objective();
            obj.setCustomName(this.efficiencyNames[i]);
            o.addObjective(obj);
        }
        for (int i=0;i<this.n;i++){
            Parameter p = new Parameter();
            p.setLowerBound(this.lowBound[i]);
            p.setUpperBound(this.upBound[i]);
            p.setAttributeName("param_" + i);
            p.setId(i);
            p.setStartValueValid(false);
            o.addParameter(p);
        }
        o.setName("opt1");
        OptimizerDescription desc = new OptimizerDescription();
        desc.setDoSpatialRelaxation(new BooleanOptimizerParameter("doSpatialRelaxation", "blubb", false));
        desc.setShortName("opt1");
        desc.setId(1);
        desc.setAssessNonUniqueness(new BooleanOptimizerParameter("AssessNonUniqueness", "blubb", false));
        //desc.setOptimizerClassName(HaltonSequenceSampling.class.getName());
        desc.setOptimizerClassName(ParallelHaltonSequenceSampling.class.getName());
        desc.setMultiObjective(false);

        String paramString = "maxn=" + this.maxSamplingPerIteration.getValue() + ";" +
                             "minn=" + this.minSamplingPerIteration.getValue() +  ";analyzeQuality=true;targetQuality=0.6";

        String params[] = paramString.split(";");
        ArrayList<OptimizerParameter> list = new ArrayList<OptimizerParameter>();

        for (int i=0;i<params.length;i++){
            String entry[] = params[i].split("=");
            if (entry.length!=2){
                this.getModel().getRuntime().sendErrorMsg("Invalid parameterization of SimpleOptimizationController. The Parameter in question is" + params[i]);
            }else{
                if (entry[1].equals("true")) {
                    list.add(new BooleanOptimizerParameter(entry[0],"unknown", true));
                }else if (entry[1].equals("false")){
                    list.add(new BooleanOptimizerParameter(entry[0],"unknown", false));
                }else{
                    try{
                        double value = Double.parseDouble(entry[1]);
                        list.add(new NumericOptimizerParameter(entry[0], "unknown", value, Double.MIN_VALUE, Double.MAX_VALUE));
                    }catch(NumberFormatException nfe){
                        list.add(new StringOptimizerParameter(entry[0], "unknown", entry[1]));
                    }
                }
            }
        }
        desc.setPropertyMap(list);

        o.setOptimizerDescription(desc);

        optimization = o;
    }

    private double calcVolume(double lowerBound[], double upperBound[]){
        double parameterSpaceVolume = 1;
        for (int i=0;i<n;i++){
            parameterSpaceVolume *= (upperBound[i]-lowerBound[i]);
        }
        return parameterSpaceVolume;
    }
    
    public double[][] calcBounds(Statistics s, double threshold){
        double bounds[][] = new double[s.n()][2];
        int Boxes = 10;

        for (int i = 0; i < s.n(); i++) {
            bounds[i][0] = Double.NEGATIVE_INFINITY;
            bounds[i][1] = Double.POSITIVE_INFINITY;
        }
        for (int j = 0; j < s.m();j++) {
            for (int i = 0; i < s.n(); i++) {
                double currentBounds[] = Calculations.calcBounds(s.sampleList, i, j, Boxes, 1.25);
                bounds[i][0] = Math.max(bounds[i][0], currentBounds[0]);
                bounds[i][1] = Math.min(bounds[i][1], currentBounds[1]);
            }
        }
        //make sure that at least the best point is inside the boundary
        for (int j = 0; j < s.m();j++) {
            Sample best = s.getMin(j);
            if (best != null){
                for (int i = 0; i < s.n(); i++) {
                    bounds[i][0] = Math.min(best.x[i], bounds[i][0]);
                    bounds[i][1] = Math.max(best.x[i], bounds[i][1]);
                }
            }
        }
        //make sure that a maximum shrink of 80% is permitted
        for (int i = 0; i < s.n(); i++) {
            double min = s.getMinimalParameter(i);
            double max = s.getMaximalParameter(i);

            if (bounds[i][1] - bounds[i][0] < 0.15*(max-min)){
                double mean = (bounds[i][1] + bounds[i][0]) / 2.0;
                bounds[i][0] = Math.min(mean - 0.075*(max-min), bounds[i][0]);
                bounds[i][1] = Math.max(mean + 0.075*(max-min), bounds[i][1]);
            }
        }
        return bounds;
    }

    public void procedure() {        
        double parameterSpaceVolume;

        double meanImprovedRatio = 1.0;
        
        ArrayList<Sample> retainList = new ArrayList<Sample>();

        while (meanImprovedRatio > epsilon1.getValue()) {
            log("################################################################");
            log("Start new Halton Sequence Sampling with the following boundaries");
            for (int j=0;j<n;j++){
                Parameter p = optimization.getParameter().get(j);
                log( p.getName() + "["+p.getLowerBound() + "<" + p.getUpperBound() +"]");
            }

            OptimizationConfiguration conf = new OptimizationConfiguration(optimization);


            double lowerBound[] = conf.getLowerBound();
            double upperBound[] = conf.getUpperBound();

            if (initialLowerBound == null){
                initialLowerBound = Arrays.copyOf(lowBound, n);
            }
            if (initialUpperBound == null){          
                initialUpperBound = Arrays.copyOf(upperBound, n);
            }
            
            parameterSpaceVolume = calcVolume(lowerBound, upperBound);

            log("loading Halton Sequence Sampler .. ");
            Optimizer o = conf.loadOptimizer(null);
            if (o instanceof ParallelHaltonSequenceSampling){
                ParallelHaltonSequenceSampling p = (ParallelHaltonSequenceSampling)o;
                p.setExcludeFiles("(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log");
                p.setModel(this.getModel());
                p.setWorkspace(this.getModel().getWorkspaceDirectory());
                p.setThreadCount(this.cores.getValue());
            }
            o.setMaxn((double)(this.maxSamplingPerIteration.getValue()-retainList.size()));
                    
            log("injecting samples .. ");
            o.injectSamples(retainList);
            log("start sampling .. ");
            o.optimize();

            ArrayList<Sample> list = o.getSamples();


            double nextLowerBound[] = new double[n];
            double nextUpperBound[] = new double[n];

            double bounds[][] = calcBounds(o.getStatistics(), threshold.getValue());

            for (int j = 0; j < n; j++) {                
                nextLowerBound[j] = bounds[j][0];
                nextUpperBound[j] = bounds[j][1];

                nextLowerBound[j] = Math.max(nextLowerBound[j]-(nextUpperBound[j]-nextLowerBound[j])*0.04,initialLowerBound[j]);
                nextUpperBound[j] = Math.min(nextUpperBound[j]+(nextUpperBound[j]-nextLowerBound[j])*0.04,initialUpperBound[j]);
            }
            
            double V = calcVolume(nextLowerBound, nextUpperBound);

            for (int j=0;j<n;j++){
                optimization.getParameter().get(j).setLowerBound(nextLowerBound[j]);
                optimization.getParameter().get(j).setUpperBound(nextUpperBound[j]);
            }
            
            retainList.clear();
            for (Sample s : list) {
                boolean addSample = true;
                for (int j=0;j<n;j++){
                    if (s.x[j] < nextLowerBound[j] || s.x[j] > nextUpperBound[j]){
                        addSample = false;
                        break;
                    }
                }
                if (addSample)
                    retainList.add(s);
            }

            meanImprovedRatio = 0.9*meanImprovedRatio + 0.1*(1.0 - V / parameterSpaceVolume);
            
            log("Finish Sampling with the following boundaries");
            for (int j=0;j<n;j++){
                Parameter p = optimization.getParameter().get(j);
                log( this.names[j] + "["+p.getLowerBound() + "<" + p.getUpperBound() +"]");
            }
            log("Volume is now:" + V + "(" + (1.0 - meanImprovedRatio) + "% mean improvement of last 10 runs)");
            for (int i=0;i<m;i++){
                log("Best sample is now (" + this.efficiencyNames[i] +  "):" + o.getStatistics().getMin(i));
            }
            String result = "";
            for (int j=0;j<n;j++){
                Parameter p = optimization.getParameter().get(j);
                result += ( "["+p.getLowerBound() + "&gt" + p.getUpperBound() +"]") + ";";
            }
            log(result);
        }
    }
}
