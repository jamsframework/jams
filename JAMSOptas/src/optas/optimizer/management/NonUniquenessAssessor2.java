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
public class NonUniquenessAssessor2 extends OptimizationController {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "optimizer class")
    public Attribute.String optimizationClassName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "parameterization of optimization method",
    defaultValue="")
    public Attribute.String parameterization;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String lowBoundsOfRegions;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String upBoundsOfRegions;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Integer efficiencyCriteriaIndex;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double threshold;

    Optimization optimization = null;

    Tools.Rectangle regions[] = null;
    final double eta = 10;

    int critIndex = 0;
    double tau = 0;

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

        double[][] lowBounds = Tools.parseStringTo2dArray(this.lowBoundsOfRegions.getValue());
        double[][] upBounds  = Tools.parseStringTo2dArray(this.upBoundsOfRegions.getValue());

        this.regions = new Tools.Rectangle[lowBounds.length];
        for (int i=0;i<lowBounds.length;i++){
            regions[i] = new  Tools.Rectangle();
            regions[i].lb = lowBounds[i];
            regions[i].ub = upBounds[i];
        }
        critIndex = this.efficiencyCriteriaIndex.getValue();
        tau = this.threshold.getValue();
    }

    private boolean isConnected(double x[], double y[]) {
        for (int i = 0; i < eta; i++) {
            double x2y[] = new double[n];
            double w = (double) i / (double) (eta - 1);

            for (int j = 0; j < n; j++) {
                x2y[j] = (1.0 - w) * x[j] + w * y[j];
            }
            try {
                Sample testSample = this.getSample(x2y);
                if (testSample.fx[critIndex] > tau) {
                    return false;
                }
            } catch (SampleLimitException sle) {
            } catch (ObjectiveAchievedException oae) {
            }
        }
        return true;
    }

    public Map<Integer,ArrayList<Integer>> mapPointsToRegions(ArrayList<Sample> points, Tools.Rectangle[] regions, ArrayList<Integer> notMapped){
        TreeMap<Integer,ArrayList<Integer>> mapping = new TreeMap<Integer,ArrayList<Integer>>();

        for (int s = 0; s < points.size();s++){
            //try to categorize s
            boolean belongsToSomeCluster = false;
            for (int r = 0;r<regions.length;r++){
                double x[] = points.get(s).x;
                double y[] = Tools.clamp(regions[r], points.get(s).x);
                double d = Tools.dist(y, points.get(s).x,this.lowBound,this.upBound);
                if (d == 0){
                    ArrayList<Integer> list = mapping.get(s);
                    if (list==null){
                        list = new ArrayList<Integer>();
                        mapping.put(s, list);
                    }
                    list.add(r);
                    continue;
                }
                boolean belongsToCluster = isConnected(x,y);


                if (belongsToCluster){
                    ArrayList<Integer> list = mapping.get(s);
                    if (list==null){
                        list = new ArrayList<Integer>();
                        mapping.put(s, list);
                    }
                    list.add(r);
                    belongsToSomeCluster = true;
                }
            }
            if (!belongsToSomeCluster){
                /*Tools.Rectangle r = new Tools.Rectangle();
                r.lb = paretoFront.get(s).x.clone();
                r.ub = paretoFront.get(s).x.clone();*/

                notMapped.add(s);
            }
        }
        return mapping;
    }

    private boolean isInside(Tools.Rectangle r1, Tools.Rectangle r2){
        for (int i=0;i<n;i++){
            if (r2.lb[i] > r1.ub[i] || r2.ub[i] < r1.lb[i]){
                return false;
            }
        }
        return true;
    }

    @Override
    public void procedure() {

        OptimizationConfiguration conf = new OptimizationConfiguration(optimization);
        ArrayList<Sample> paretoFront = null;

        Optimizer o = conf.loadOptimizer(null);
        o.optimize();
        paretoFront = (ArrayList<Sample>) o.getSolution().clone();

        //ausgabe samples
        //this.solution = JAMSDataFactory.createEntity();
        //solution.setObject("solution", getStatistics().getParetoFront());
        //paretoFront = (ArrayList<Sample>)getStatistics().getParetoFront().clone();


        /* skip this

        ArrayList<Integer> notMappedPoints = new ArrayList<Integer>();
        //Map<Integer,ArrayList<Integer>> mapping = mapPointsToRegions(paretoFront, regions, notMappedPoints);
        for (int i=0;i<paretoFront.size();i++)
            if (paretoFront.get(i).fx[this.critIndex] < tau)
                notMappedPoints.add(i);

        //clustering
        HashSet<Tools.Rectangle> newRectangles = new HashSet<Tools.Rectangle>();
        for (int i=0;i<regions.length;i++){
            newRectangles.add(regions[i]);
        }
        while(notMappedPoints.size()>0){
            if (notMappedPoints.size()==1){
                break;
            }
            
            int index = notMappedPoints.get(0);
            Sample s = paretoFront.get(index);

            boolean isConnected = false;

            for (Tools.Rectangle r : newRectangles){
                double d = Tools.dist(Tools.clamp(r, s.x),s.x,this.lowBound, this.upBound);
                if (d == 0){
                    isConnected = true;
                    break;
                }
            }

            if (isConnected){
                notMappedPoints.remove(0);
                continue;
            }

            for (Tools.Rectangle r : newRectangles){
                if (this.isConnected(Tools.clamp(r, s.x),s.x)){
                    for (int i=0;i<n;i++){
                        r.lb[i] = Math.min(r.lb[i],s.x[i]);
                        r.ub[i] = Math.max(r.ub[i],s.x[i]);
                    }
                    isConnected = true;
                    break;
                }
            }

            if (isConnected){
                notMappedPoints.remove(0);
                continue;
            }

            Tools.Rectangle r = new Tools.Rectangle();
            r.lb = s.x.clone();
            r.ub = s.x.clone();
            newRectangles.add(r);

            HashSet<Tools.Rectangle> deleteList = new HashSet<Tools.Rectangle>();
            for (Tools.Rectangle r1 : newRectangles){
                for (Tools.Rectangle r2 : newRectangles){
                    if (deleteList.contains(r2))
                        continue;
                    if (deleteList.contains(r1))
                        continue;
                    if (isInside(r1,r2)){
                        for (int i=0;i<n;i++){
                            r1.lb[i] = Math.min(r1.lb[i], r2.lb[i]);
                            r1.ub[i] = Math.max(r1.ub[i], r2.ub[i]);
                        }
                        deleteList.add(r2);
                    }
                }
            }
        }
        Tools.Rectangle regions2[] = new Tools.Rectangle[newRectangles.size()];        
        int counter=0;
        for (Tools.Rectangle r : newRectangles){
            regions2[counter++] = r;
        }

        Map<Integer,ArrayList<Integer>> mapping2 = mapPointsToRegions(paretoFront, regions2, notMappedPoints);


        System.out.println("Finished Non Uniquness Assessor II");


        System.out.println("The following regions are existent");
        for (int r = 0; r <regions2.length;r++){
            System.out.println(r + "->\n" + Arrays.toString(regions2[r].lb) + "\n" + Arrays.toString(regions2[r].ub));
        }


        System.out.println("Found the following non dominated points");

        for (int s = 0; s < paretoFront.size();s++){
            ArrayList<Integer> rList = mapping2.get(s);
            if (rList!=null)
                System.out.println(s + "->" + rList.toString() + "\t:" + paretoFront.get(s).toString());
        }                */
    }
}
