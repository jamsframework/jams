/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer;


import optas.core.SampleLimitException;
import java.util.Arrays;
import jams.JAMS;

import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.optimizer.management.SampleFactory.SampleSOComperator;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="NelderMead",
        author="Christian Fischer",
        description="Performs a nelder mead optimization. Advantage: Derivative free optimization method. Disadvantage: only local convergence"
        )
public class NelderMead extends Optimizer{           
    SerializableBufferedWriter writer = null;
            
    public SampleSO[] initialSimplex = null;
    public double epsilon = 0.01;
    public double max_restart_count = 5;

    public void sort(SampleSO[] array){
        Arrays.sort(array,new SampleSOComperator(false));
    }

    public OptimizerDescription getDescription(){
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(NelderMead.class.getSimpleName(), NelderMead.class.getName(), 250, false);

        desc.addParameter(new NumericOptimizerParameter("max_restart_count",JAMS.i18n("Number_of_restarts"),5,0,1000));
        desc.addParameter(new NumericOptimizerParameter("epsilon",JAMS.i18n("stopping_criterion_minimal_geometric_range"),0.01,0,1));
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
    
    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException{
        boolean stop = false;
    this.generator.setSeed(0);
        //first draw n+1 random points        
        SampleSO simplex[] = new SampleSO[n+1];
        if (initialSimplex != null){
            simplex = initialSimplex;
        }else{
            for (int i=0;i<n+1;i++){
                if (x0!=null&&i<x0.length)
                    simplex[i] = this.getSampleSO(x0[i]);
                else
                    simplex[i] = this.getSampleSO(this.randomSampler());
            }   
        }
        int m = simplex.length;
        
        double alpha = 1.0,gamma = 2.0,rho = 0.5,sigma = 0.5;
        
        int restart_counter = 0;        
        while(!stop){        
            if (this.factory.getSize() > getMaxn()){
                this.log("*********************************************************");
                this.log(JAMS.i18n("Maximum_number_of_iterations_reached_finished_optimization"));
                this.log(JAMS.i18n("bestpoint") + simplex[0]);
                this.log("*********************************************************");
                return;
            }
            if (this.NormalizedgeometricRange(simplex)<getEpsilon()){
                if (getMax_restart_count() < ++restart_counter){
                    this.log("*********************************************************");
                    this.log(JAMS.i18n("Maximum_number_of_restarts_reached_finished_optimization"));
                    this.log(JAMS.i18n("bestpoint") + simplex[0]);
                    this.log("*********************************************************");
                    return;
                }
                this.log(JAMS.i18n("restart"));
                for (int i=1;i<m;i++){                    
                    simplex[i] = this.getSampleSO(this.randomSampler());
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
            //reflect
            double reflection[] = new double[n];
            for (int i=0;i<n;i++){
                reflection[i] = centroid[i] + alpha*(centroid[i]-simplex[m-1].x[i]);
            }
            SampleSO reflection_SampleSO = null;
            if (this.feasible(reflection)){
                log(JAMS.i18n("reflection_step"));
                reflection_SampleSO = this.getSampleSO(reflection);
            
                if (simplex[0].f() < reflection_SampleSO.f() && reflection_SampleSO.f() < simplex[m-1].f()){
                    simplex[m-1] = reflection_SampleSO;
                    continue;
                }
            }
            //expand
            if (this.feasible(reflection) && simplex[0].f() >= reflection_SampleSO.f()){
                double expansion[] = new double[n];
                for (int i=0;i<n;i++){
                    expansion[i] = centroid[i] + gamma*(centroid[i]-simplex[m-1].x[i]);
                }
                log(JAMS.i18n("expansion_step"));
                
                SampleSO expansion_SampleSO = this.getSampleSO(expansion);
                if (this.feasible(expansion) && expansion_SampleSO.f() < reflection_SampleSO.f()){
                    simplex[m-1] = expansion_SampleSO;
                }else{
                    simplex[m-1] = reflection_SampleSO;                    
                }                    
                continue;                
            }
            //contraction
            if (!this.feasible(reflection) || simplex[m-1].f() <= reflection_SampleSO.f()){
                double contraction[] = new double[n];
                for (int i=0;i<n;i++){
                    contraction[i] = centroid[i] + rho*(centroid[i]-simplex[m-1].x[i]);
                }
                log(JAMS.i18n("contraction_step"));
                //this should not happen .. 
                SampleSO contraction_SampleSO = null;
                if (!this.feasible(contraction)){
                    log(JAMS.i18n("not_feasible_after_contraction_step"));
                    contraction_SampleSO = this.getSampleSO(this.randomSampler());
                }else
                    contraction_SampleSO = this.getSampleSO(contraction);
                if (contraction_SampleSO.f() < simplex[m-1].f()){
                    simplex[m-1] = contraction_SampleSO;
                    continue;
                }
            }



            //shrink
            for (int i=1;i<m;i++){
                double shrink[] = new double[n];
                for(int j=0;j<n;j++){
                    shrink[j] = simplex[0].x[j] + sigma*(simplex[i].x[j]-simplex[0].x[j]);
                }
                log(JAMS.i18n("shrink_step"));
                simplex[i] = this.getSampleSO(shrink);
            }                                                
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
