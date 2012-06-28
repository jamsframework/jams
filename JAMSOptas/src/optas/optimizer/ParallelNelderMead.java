/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer;


import java.util.Arrays;
import jams.JAMS;

import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;
import optas.hydro.data.DataCollection;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.optimizer.management.SampleFactory.SampleSOComperator;
import optas.optimizer.management.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.parallel.ParallelSequence;
import optas.optimizer.parallel.ParallelSequence.OutputData;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="NelderMead",
        author="Christian Fischer",
        description="Performs a nelder mead optimization. Advantage: Derivative free optimization method. Disadvantage: only local convergence"
        )
public class ParallelNelderMead extends Optimizer{           
    SerializableBufferedWriter writer = null;
            
    public SampleSO[] initialSimplex = null;
    public double epsilon = 0.01;
    public double max_restart_count = 5;
    public String excludeFiles = "";
    
    DataCollection dc = null;

    public void sort(SampleSO[] array){
        Arrays.sort(array,new SampleSOComperator(false));
    }

    public String getExcludeFiles(){
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles){
        this.excludeFiles = excludeFiles;
    }
    @Override
    public OptimizerDescription getDescription(){
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(ParallelNelderMead.class.getSimpleName(), ParallelNelderMead.class.getName(), 250, false);

        desc.addParameter(new NumericOptimizerParameter("max_restart_count","Number of restarts",5,0,1000));
        desc.addParameter(new NumericOptimizerParameter("epsilon","stopping criterion minimal geometric range",0.01,0,1));
        return desc;
    }

    public double NormalizedgeometricRange(SampleSO x[]) {
        if (x.length == 0)
            return 0;
                
        double min[] = new double[n];
        double max[] = new double[n];
        
        double mean = 0;
        
        for (int i=0;i<n;i++) {
            min[i] = Double.POSITIVE_INFINITY;
            max[i] = Double.NEGATIVE_INFINITY;
            
            for (int j=0;j<x.length;j++) {
                min[i] = Math.min(x[j].x[i], min[i]);
                max[i] = Math.max(x[j].x[i], max[i]);
            }
            
            mean += Math.log(max[i] - min[i]);
        }
        mean/=n;
        return Math.exp(mean);
    }
                
    public boolean feasible(double point[]){
        for (int i=0;i<point.length;i++)
            if (point[i] < this.lowBound[i] || point[i] > this.upBound[i])
                return false;
        return true;
    }

    private void mergeOutputData(OutputData o) throws SampleLimitException, ObjectiveAchievedException{
//        this.injectSamples(o.list); //will happen by converting to sampleso
        if (this.dc == null)
            dc = o.dc;
        else
            dc.mergeDataCollections(o.dc);
    }
    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException{
        boolean stop = false;

        ParallelSequence pseq = new ParallelSequence(this);
        pseq.excludeFiles = this.excludeFiles;
this.generator.setSeed(0);
        //first draw n+1 random points        
        SampleSO simplex[] = new SampleSO[n+1];
        double x[][] = new double[n+1][];
        if (initialSimplex != null){
            simplex = initialSimplex;
        }else{
            for (int i=0;i<n+1;i++){

                if (x0!=null&&i<x0.length){
                    //simplex[i] = this.getSampleSO(x0[i]);
                    x[i] = x0[i];
                } else{
                    //simplex[i] = this.getSampleSO(this.randomSampler());
                    x[i] = this.randomSampler();
                }
            }   
        }
        OutputData o = pseq.procedure(x);
        for (int i=0;i<n+1;i++)
            simplex[i] = factory.getSampleSO(o.list.get(i).x,o.list.get(i).F()[0]);
        mergeOutputData(o);

        int m = simplex.length;
        
        double alpha = 1.0,gamma = 2.0,rho = 0.5,sigma = 0.5;


        int restart_counter = 0;        
        while(!stop){        
            if (this.factory.getSize() > getMaxn()){
                this.log("*********************************************************");
                this.log(JAMS.i18n("Maximum_number_of_iterations_reached_finished_optimization"));
                this.log(JAMS.i18n("bestpoint") + simplex[0]);
                this.log("*********************************************************");
                finish();
                return;
            }
            if (this.NormalizedgeometricRange(simplex)<getEpsilon()){
                if (getMax_restart_count() < ++restart_counter){
                    this.log("*********************************************************");
                    this.log(JAMS.i18n("Maximum_number_of_restarts_reached_finished_optimization"));
                    this.log(JAMS.i18n("bestpoint") + simplex[0]);
                    this.log("*********************************************************");
                    finish();
                    return;
                }
                this.log(JAMS.i18n("restart"));
                x = new double[m-1][];
                for (int i=1;i<m;i++){
                    x[i-1] = this.randomSampler();
                }
                o = pseq.procedure(x);
                mergeOutputData(o);
                for (int i=1;i<m;i++){
                    simplex[i] = factory.getSampleSO(o.list.get(i-1).x,o.list.get(i-1).F()[0]);
                }
            }            
            sort(simplex);
            // Compute the centroid of the simplex
            double centroid[] = new double[n];            
            for (int j=0;j<n;j++) {                
                centroid[j] = 0;
                for (int i=0;i<m-1;i++) {
                    centroid[j] += simplex[i].x[j]*(1.0/(double)(m-1.0));
                }                
            }
            
            double reflection[] = new double[n];
            double expansion[] = new double[n];
            double contraction[] = new double[n];
            for (int i=0;i<n;i++){
                reflection[i] = centroid[i] + alpha*(centroid[i]-simplex[m-1].x[i]);
                expansion[i] = centroid[i] + gamma * (centroid[i] - simplex[m - 1].x[i]);
                contraction[i] = centroid[i] + rho * (centroid[i] - simplex[m - 1].x[i]);
            }

            x = new double[4][];
            x[0] = reflection;
            x[1] = expansion;
            x[2] = contraction;
            x[3] = this.randomSampler();

            o = pseq.procedure(x);
            mergeOutputData(o);

            int best = -1;
            for (int i=0;i<4;i++){
                if (best == -1){
                    if (feasible(o.list.get(i).x))
                        best = i;
                }else{
                    if (feasible(o.list.get(i).x) && o.list.get(i).F()[0]<o.list.get(best).F()[0]){
                        best = i;
                    }
                }
            }
            if (best != -1){
                if (o.list.get(best).F()[0]<simplex[m-1].f()){
                    simplex[m-1] = factory.getSampleSO(o.list.get(best).x,o.list.get(best).F()[0]);
                    continue;
                }

            }/*
            SampleSO reflection_SampleSO = null;
            if (this.feasible(reflection)){
                log(JAMS.i18n("reflection_step"));
                reflection_SampleSO = o.list.get(0);
            
                if (simplex[0].f() < reflection_SampleSO.f() && reflection_SampleSO.f() < simplex[m-1].f()){
                    simplex[m-1] = reflection_SampleSO;
                    continue;
                }
            }
            //expand
            if (this.feasible(reflection) && simplex[0].f() >= reflection_SampleSO.f()){
                
                log(JAMS.i18n("expansion_step"));
                
                SampleSO expansion_SampleSO = o.list.get(1);
                if (this.feasible(expansion) && expansion_SampleSO.f() < reflection_SampleSO.f()){
                    simplex[m-1] = expansion_SampleSO;
                }else{
                    simplex[m-1] = reflection_SampleSO;                    
                }                    
                continue;                
            }
            //contraction
            if (!this.feasible(reflection) || simplex[m-1].f() <= reflection_SampleSO.f()){
                
                log(JAMS.i18n("contraction_step"));
                //this should not happen .. 
                SampleSO contraction_SampleSO = null;
                if (!this.feasible(contraction)){
                    log(JAMS.i18n("not_feasible_after_contraction_step"));
                    contraction_SampleSO = o.list.get(3);
                }else
                    contraction_SampleSO = o.list.get(2);
                if (contraction_SampleSO.f() < simplex[m-1].f()){
                    simplex[m-1] = contraction_SampleSO;
                    continue;
                }
            }*/


            //shrink.. tritt auf wenn die continues nicht eintreten ..
            double shrink[][] = new double[m-1][n];
            for (int i=1;i<m;i++){                
                for(int j=0;j<n;j++){
                    shrink[i-1][j] = simplex[0].x[j] + sigma*(simplex[i].x[j]-simplex[0].x[j]);
                }
                log(JAMS.i18n("shrink_step"));                
            }
            o = pseq.procedure(shrink);
            mergeOutputData(o);
            for (int i=1;i<m;i++)
                simplex[i] = factory.getSampleSO(o.list.get(i-1).x,o.list.get(i-1).F()[0]);
        }        
    }

    private void finish(){
        if (dc != null) {
            dc.dump(getModel().getWorkspace().getOutputDataDirectory());
        }
    }
    /**
     * @return the epsilon
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * @param epsilon the epsilon to set
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * @return the max_restart_count
     */
    public double getMax_restart_count() {
        return max_restart_count;
    }

    /**
     * @param max_restart_count the max_restart_count to set
     */
    public void setMax_restart_count(double max_restart_count) {
        this.max_restart_count = max_restart_count;
    }
}
