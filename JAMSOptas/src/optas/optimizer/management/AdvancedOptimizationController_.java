/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.management;

import jams.data.Attribute;
import jams.data.JAMSDataFactory;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import optas.metamodel.Optimization;
import optas.metamodel.OptimizationDescriptionDocument;
import optas.metamodel.Parameter;


import optas.optimizer.Optimizer;
import optas.optimizer.Optimizer.AbstractFunction;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.SampleLimitException;

/**
 *
 * @author chris
 */
@JAMSComponentDescription(title = "OptimizationScheduler",
author = "Christian Fischer",
description = "Performs a chain of optimizations")
public class AdvancedOptimizationController_ extends OptimizationController {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String optimizationDescriptionFile;
   
    OptimizationDescriptionDocument schedule;
    ArrayList<Optimizer> optimizerList = new ArrayList<Optimizer>();
    
    public class Evaluate extends AbstractFunction {

        int subn, subm;
        int parameterIndexMap[];
        int objectiveIndexMap[];
        double objectiveSet[];
        Optimizer owner;

        public Evaluate(int subn, int subm, int pIndexMap[], int oIndexMap[]) {
            this.subn = subn;
            this.subm = subm;

            objectiveSet = new double[subm];

            parameterIndexMap = pIndexMap;
            objectiveIndexMap = oIndexMap;
        }

        public void logging(String msg) {
            AdvancedOptimizationController_.this.getModel().getRuntime().println(msg);
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
            double fillingParameterSet[] = new double[n];

            if (!getStatistics().getParetoFront().isEmpty()) {
                fillingParameterSet = Arrays.copyOf(getStatistics().getParetoFront().get(0).x, n);
            } else {
                fillingParameterSet = new double[n];
                for (int i = 0; i < n; i++) {
                    fillingParameterSet[i] = (upBound[i] + lowBound[i]) / 2.0;
                }
            }

            double xSuper[] = projectParametersetToSuperSpace(x, fillingParameterSet);


            Sample s = AdvancedOptimizationController_.this.getSample(xSuper);

            return s;
        }

        @Override
        public double[] f(double[] x) throws ObjectiveAchievedException, SampleLimitException {
            Sample s = getSample(x);

            double results[] = s.F();
            for (int i = 0; i < subm; i++) {
                objectiveSet[i] = results[objectiveIndexMap[i]];
            }

            return objectiveSet;
        }
    }
    
    public class AdvancedOptimizationConfiguration extends OptimizationConfiguration{
        private int parameterIndexMap[],objectiveIndexMap[];        
        private int subn,subm;

        private String parameterNames[],objectiveNames[];

        private AdvancedOptimizationConfiguration(Optimization o){
            super(o);
            
            subn = n;
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

            subm = m;
            objectiveIndexMap = new int[subm];
            objectiveNames = new String[subm];

            for (int i = 0; i < subm; i++) {
                objectiveIndexMap[i] = schedule.getObjective().headMap(o.getObjective().get(i).getId()).size();
                objectiveNames[i] = o.getObjective().get(i).toString();
            }
        }
                
        @Override
        protected void setOptimizerParameter(Optimizer optimizer, Optimization o) {
            super.setOptimizerParameter(optimizer, o);
            
            this.evaluate = new Evaluate(subn, subm, parameterIndexMap, objectiveIndexMap);
            optimizer.setFunction(evaluate);
        }        
    }

    @Override
    public void init() {
        super.init();

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

            HashMap<Integer,Integer> oldNewIdMap = new HashMap<Integer,Integer>();
            SortedMap<Integer, Parameter> parameterMap = schedule.getParameter();

            for (Parameter p : parameterMap.values())
            for (int i=0;i<n;i++){
                if (this.names[i].compareTo(p.getChildName())==0){
                    oldNewIdMap.put(p.getId(),i);
                    p.setId(i);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Could not load optimization scheme XML:" + ioe.toString());
            return;
        }
    }
    
    @Override
    public void procedure() {
        for (Optimization o : schedule.getOptimization()) {            
            OptimizationConfiguration conf = new AdvancedOptimizationConfiguration(o);

            if (o.getOptimizerDescription().getDoSpatialRelaxation().isValue()) {
                //relaxationProcedure(o);
                SpatialRelaxation relaxation = new SpatialRelaxation();                
                relaxation.setRelaxationParameter(relaxationParameter);
                relaxation.applyProcedure(conf);
            } else {
                conf.loadOptimizer(null).optimize();
            }


        }
        //ausgabe samples
        this.solution = JAMSDataFactory.createEntity();
        solution.setObject("solution", getStatistics().getParetoFront());
    }
}
