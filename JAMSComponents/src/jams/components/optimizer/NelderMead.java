/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.components.optimizer.SOOptimizer.SampleSO;
import java.util.Arrays;
import jams.JAMS;
import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="NelderMead",
        author="Christian Fischer",
        description="Performs a nelder mead optimization. Advantage: Derivative free optimization method. Disadvantage: only local convergence"
        )
public class NelderMead extends SOOptimizer{           
    SerializableBufferedWriter writer = null;
            
    public SampleSO[] initialSimplex = null;
    
    public void sort(SampleSO[] array){
        Arrays.sort(array,new SampleSOComperator(false));
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
    
    @SuppressWarnings("unchecked")    
    @Override
    public void init(){
        super.init();
    }
        
    public boolean feasible(double point[]){
        for (int i=0;i<point.length;i++)
            if (point[i] < this.lowBound[i] || point[i] > this.upBound[i])
                return false;
        return true;
    }
    
    @Override
    public void run(){
        if (enable!=null)
            if (!enable.getValue()){
                singleRun();
                return;
            }
        boolean stop = false;
    
        //first draw n+1 random points        
        SampleSO simplex[] = new SampleSO[n+1];
        if (initialSimplex != null){
            simplex = initialSimplex;
        }else{
            for (int i=0;i<n+1;i++){
                if (i==0&&x0!=null)
                    simplex[i] = this.getSample(x0);
                else
                    simplex[i] = this.getSample(this.RandomSampler());
            }   
        }
        int m = simplex.length;
        
        double alpha = 1.0,gamma = 2.0,rho = 0.5,sigma = 0.5,epsilon = 0.01,max_restart_count = 5;
        
        int restart_counter = 0;
        int iterationcounter = 0;
        while(!stop){        
            if (iterationcounter++ > maxn.getValue()){
                getModel().getRuntime().println("*********************************************************");                
                getModel().getRuntime().println(JAMS.resources.getString("Maximum_number_of_iterations_reached_finished_optimization"));
                getModel().getRuntime().println(JAMS.resources.getString("bestpoint") + simplex[0]);
                getModel().getRuntime().println("*********************************************************");
                return;
            }
            if (this.NormalizedgeometricRange(simplex)<epsilon){
                if (max_restart_count < ++restart_counter){
                    getModel().getRuntime().println("*********************************************************");                    
                    getModel().getRuntime().println(JAMS.resources.getString("Maximum_number_of_restarts_reached_finished_optimization"));
                    getModel().getRuntime().println(JAMS.resources.getString("bestpoint") + simplex[0]);
                    getModel().getRuntime().println("*********************************************************");
                    return;
                }
                getModel().getRuntime().println(JAMS.resources.getString("restart"));
                for (int i=1;i<m;i++){                    
                    simplex[i] = this.getSample(this.RandomSampler());
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
                getModel().getRuntime().println(JAMS.resources.getString("reflection_step"));
                reflection_SampleSO = this.getSample(reflection);
            
                if (simplex[0].fx < reflection_SampleSO.fx && reflection_SampleSO.fx < simplex[m-1].fx){
                    simplex[m-1] = reflection_SampleSO;
                    continue;
                }
            }
            //expand
            if (this.feasible(reflection) && simplex[0].fx >= reflection_SampleSO.fx){
                double expansion[] = new double[n];
                for (int i=0;i<n;i++){
                    expansion[i] = centroid[i] + gamma*(centroid[i]-simplex[m-1].x[i]);
                }
                getModel().getRuntime().println(JAMS.resources.getString("expansion_step"));
                
                SampleSO expansion_SampleSO = this.getSample(expansion);
                if (this.feasible(expansion) && expansion_SampleSO.fx < reflection_SampleSO.fx){
                    simplex[m-1] = expansion_SampleSO;
                }else{
                    simplex[m-1] = reflection_SampleSO;                    
                }                    
                continue;                
            }
            //contraction
            if (!this.feasible(reflection) || simplex[m-1].fx <= reflection_SampleSO.fx){                
                double contraction[] = new double[n];
                for (int i=0;i<n;i++){
                    contraction[i] = centroid[i] + rho*(centroid[i]-simplex[m-1].x[i]);
                }
                getModel().getRuntime().println(JAMS.resources.getString("contraction_step"));
                //this should not happen .. 
                SampleSO contraction_SampleSO = null;
                if (!this.feasible(contraction)){
                    getModel().getRuntime().println(JAMS.resources.getString("not_feasible_after_contraction_step"));
                    contraction_SampleSO = this.getSample(this.RandomSampler());
                }else
                    contraction_SampleSO = this.getSample(contraction);
                if (contraction_SampleSO.fx < simplex[m-1].fx){
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
                getModel().getRuntime().println(JAMS.resources.getString("shrink_step"));
                simplex[i] = this.getSample(shrink);
            }                                                
        }        
    }        
}
