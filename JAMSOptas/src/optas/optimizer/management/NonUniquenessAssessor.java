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
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import optas.metamodel.Objective;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;
import optas.optimizer.Optimizer;
import optas.optimizer.ParallelPostSampling2;
import optas.optimizer.SampleLimitException;


import optas.optimizer.management.OptimizationController.OptimizationConfiguration;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
@JAMSComponentDescription(title = "OptimizationScheduler",
author = "Christian Fischer",
description = "Performs a chain of optimizations")
public class NonUniquenessAssessor extends OptimizationController {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "parameterization of optimization method",
    defaultValue = "")
    public Attribute.String parameterization;
    
    Optimization optimization = null;
    Tools.Rectangle regions[] = null;
    
    @Override
    public void init() {
        super.init();

        Optimization o = new Optimization();
        for (int i = 0; i < this.m; i++) {
            Objective obj = new Objective();
            obj.setCustomName(this.efficiencyNames[i]);
            o.addObjective(obj);
        }
        for (int i = 0; i < this.n; i++) {
            Parameter p = new Parameter();
            p.setLowerBound(this.lowBound[i]);
            p.setUpperBound(this.upBound[i]);
            p.setAttributeName("param_" + i);
            p.setId(i);
            double x0i[] = new double[x0.length];
            for (int j = 0; j < x0.length; j++) {
                x0i[j] = x0[j][i];
            }
            p.setStartValue(x0i);
            o.addParameter(p);
        }
        o.setName("opt1");
        try {
            OptimizerDescription desc = Tools.getStandardOptimizerDesc(parameterization.getValue());
            desc.setOptimizerClassName(ParallelPostSampling2.class.getName());
            o.setOptimizerDescription(desc);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            getModel().getRuntime().sendHalt(e.toString());
        }
        optimization = o;
    }
        
    @Override
    public void procedure() {

        OptimizationConfiguration conf = new OptimizationConfiguration(optimization);
        ParallelPostSampling2 sampler = (ParallelPostSampling2) conf.loadOptimizer(null);
        sampler.excludeFiles = "(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log";
        sampler.analyzeQuality = false;

        ArrayList<Sample> postSamplerResult = sampler.optimize();
        double l[][] = sampler.getFinalRange();

        Sample initialPoint = postSamplerResult.get(0);
        
        this.log("Region of optimality is:\nnames:" + Arrays.toString(this.names)
                + "\nx0:" + Arrays.toString(initialPoint.x) + "y:" + Arrays.toString(initialPoint.fx)
                + "\nrectangle_min:" + Arrays.toString(l[0])
                + "\nrectangle_max:" + Arrays.toString(l[1]));
        
    }
}
