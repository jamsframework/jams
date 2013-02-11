/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import optas.metamodel.Objective;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;


import optas.optimizer.management.OptimizationController.OptimizationConfiguration;

/**
 *
 * @author chris
 */
@JAMSComponentDescription(title = "OptimizationScheduler",
author = "Christian Fischer",
description = "Performs a chain of optimizations")
public class SimpleOptimizationController extends OptimizationController {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "optimizer class")
    public Attribute.String optimizationClassName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "parameterization of optimization method",
    defaultValue="")
    public Attribute.String parameterization;
    
    Optimization optimization = null;
        
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
            double x0i[] = new double[x0.length];
            for (int j=0;j<x0.length;j++)
                x0i[j] = x0[j][i];
            p.setStartValue(x0i);
            o.addParameter(p);
        }
        o.setName("opt1");
        try{
            OptimizerDescription desc = Tools.getStandardOptimizerDesc(parameterization.getValue());
            desc.setOptimizerClassName(this.optimizationClassName.getValue());
            o.setOptimizerDescription(desc);
        }catch(Exception e){
            e.printStackTrace();            
            System.out.println(e.toString());
            getModel().getRuntime().sendHalt(e.toString());
        }
        optimization = o;
    }
    
    @Override
    public void procedure() {

        OptimizationConfiguration conf = new OptimizationConfiguration(optimization);

        if (optimization.getOptimizerDescription().getDoSpatialRelaxation().isValue()) {
            //relaxationProcedure(o);
            SpatialRelaxation relaxation = new SpatialRelaxation();            
            relaxation.setRelaxationParameter(relaxationParameter);
            relaxation.applyProcedure(conf);
        } else {
            conf.loadOptimizer(null).optimize();
        }


        //ausgabe samples
        this.solution = JAMSDataFactory.getDataFactory().createEntity();
        solution.setObject("solution", getStatistics().getParetoFront());
    }
}
