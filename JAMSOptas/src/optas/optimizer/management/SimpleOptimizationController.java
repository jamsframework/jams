/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import jams.data.Attribute;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import optas.gui.wizard.OPTASWizardException;
import optas.gui.wizard.Objective;
import optas.gui.wizard.Optimization;
import optas.gui.wizard.Parameter;

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

        try {
            optimization = new Optimization(null);

            for (int i = 0; i < this.m; i++) {
                Objective obj = new Objective(this.efficiencyNames[i]);
                optimization.addObjective(obj);
            }
            for (int i = 0; i < this.n; i++) {
                Parameter p = new Parameter("param_" + i);
                p.setLowerBound(this.lowBound[i]);
                p.setUpperBound(this.upBound[i]);
                double x0i[] = new double[x0.length];
                for (int j = 0; j < x0.length; j++) {
                    x0i[j] = x0[j][i];
                }
                p.setStartValue(x0i);
                optimization.addParameter(p);
            }
            optimization.setName("opt1");

            OptimizerDescription desc = Tools.getStandardOptimizerDesc(parameterization.getValue());
            desc.setOptimizerClassName(this.optimizationClassName.getValue());
            optimization.setOptimizerDescription(desc);


        } catch (OPTASWizardException e) {
            e.printStackTrace();
            System.out.println(e.toString());
            getModel().getRuntime().sendHalt(e.toString());
        }
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
        this.solution = DefaultDataFactory.getDataFactory().createEntity();
        solution.setObject("solution", getStatistics().getParetoFront());
    }
}
