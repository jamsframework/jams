/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import optas.metamodel.Objective;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;


import optas.optimizer.Optimizer;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.SampleLimitException;
import optas.optimizer.management.OptimizationController.OptimizationConfiguration;

/**
 *
 * @author chris
 */
@JAMSComponentDescription(title = "OptimizationScheduler",
author = "Christian Fischer",
description = "Performs a chain of optimizations")
public class SimpleOptimizationController extends OptimizationController {    
    Optimization optimization = null;
        
    @Override
    public void init() {
        super.init();

        Optimization o = new Optimization();
        for (int i=0;i<this.m;i++){
            Objective obj = new Objective();

            //o.addObjective();
        }

    }
    
    public void procedure() {

        OptimizationConfiguration conf = new OptimizationConfiguration(optimization);

        if (optimization.getOptimizerDescription().getDoSpatialRelaxation().isValue()) {
            //relaxationProcedure(o);
            SpatialRelaxation relaxation = new SpatialRelaxation();
            relaxation.setMainObjectiveIndex(mainObjIndex);
            relaxation.setRelaxationParameter(relaxationParameter);
            relaxation.applyProcedure(conf);
        } else if (optimization.getOptimizerDescription().getAssessNonUniqueness().isValue()) {
            NonUniquenessAssessor assessNonUniqueness = new NonUniquenessAssessor();
            assessNonUniqueness.applyProcedure(conf);

        } else {
            conf.loadOptimizer(null).optimize();
        }


        //ausgabe samples
        this.solution = JAMSDataFactory.createEntity();
        solution.setObject("solution", getStatistics().getParetoFront());
    }
}
