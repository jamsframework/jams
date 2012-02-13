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
    description = "parameterization of optimization method")
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
            p.setStartValueValid(false);
            o.addParameter(p);
        }
        o.setName("opt1");
        OptimizerDescription desc = new OptimizerDescription();
        desc.setDoSpatialRelaxation(new BooleanOptimizerParameter("doSpatialRelaxation", "blubb", false));
        desc.setShortName("opt1");
        desc.setId(1);
        desc.setAssessNonUniqueness(new BooleanOptimizerParameter("AssessNonUniqueness", "blubb", false));
        desc.setOptimizerClassName(optimizationClassName.getValue());
        desc.setMultiObjective(false);

        String paramString = parameterization.getValue();
        String params[] = paramString.split(";");
        ArrayList<OptimizerParameter> list = new ArrayList<OptimizerParameter>();

        for (int i=0;i<params.length;i++){
            String entry[] = params[i].split("=");
            if (entry.length!=2){
                this.getModel().getRuntime().sendErrorMsg("Invalid parameterization of SimpleOptimizationController. The Parameter in question is" + params[i]);
            }else{
                if (entry[1].equals("true") || entry[1].equals("false")){
                    list.add(new BooleanOptimizerParameter(entry[0],"unknown", Boolean.getBoolean(entry[1])));
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
