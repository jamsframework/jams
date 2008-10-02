/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import Jama.Matrix;
import jams.components.optimizer.LinearConstraintDirectPatternSearch;
import jams.components.optimizer.Optimizer.AbstractFunction;
import jams.components.optimizer.Optimizer.Sample;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;
import org.unijena.jams.JAMS;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSEntity;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.io.SerializableBufferedWriter;
import org.unijena.jams.model.JAMSComponentDescription;
import org.unijena.jams.model.JAMSVarDescription;
import sun.nio.cs.Surrogate.Generator;
import sun.nio.cs.Surrogate.Generator;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="NelderMead",
        author="Christian Fischer",
        description="Performs a nelder mead optimization. Advantage: Derivative free optimization method. Disadvantage: only local convergence"
        )
public class NelderMead extends Optimizer{           
    SerializableBufferedWriter writer = null;
            
    public Sample[] initialSimplex = null;
    
    public void sort(Sample[] array){
        Arrays.sort(array,new SampleComperator(false));
    }
    
    public double NormalizedgeometricRange(Sample x[]) {
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
    public void init(){
        super.init();
    }
        
    public void run(){
        boolean stop = false;
    
        //first draw n+1 random points        
        Sample simplex[] = new Sample[n+1];
        if (initialSimplex != null){
            simplex = initialSimplex;
        }else{
            for (int i=0;i<n+1;i++){
                simplex[i] = this.getSample(this.RandomSampler());
            }   
        }
        int m = simplex.length;
        
        double alpha = 1.0,gamma = 2.0,rho = 0.5,sigma = 0.5,epsilon = 0.01,max_restart_count = 5;;
        
        int restart_counter = 0;
        int iterationcounter = 0;
        while(!stop){        
            if (iterationcounter++ > maxn.getValue()){
                getModel().getRuntime().println("*********************************************************");
                getModel().getRuntime().println("Maximum number of iterations reached, finished optimization");
                getModel().getRuntime().println("Bestpoint:" + simplex[0]);
                getModel().getRuntime().println("*********************************************************");
                return;
            }
            if (this.NormalizedgeometricRange(simplex)<epsilon){
                if (max_restart_count < ++restart_counter){
                    getModel().getRuntime().println("*********************************************************");
                    getModel().getRuntime().println("Maximum number of restarts reached, finished optimization");
                    getModel().getRuntime().println("Bestpoint:" + simplex[0]);
                    getModel().getRuntime().println("*********************************************************");
                    return;
                }
                getModel().getRuntime().println("restart");
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
            getModel().getRuntime().println("reflection step");
            Sample reflection_sample = this.getSample(reflection);
            
            if (simplex[0].fx < reflection_sample.fx && reflection_sample.fx < simplex[m-1].fx){
                simplex[m-1] = reflection_sample;
                continue;
            }
            //expand
            if (simplex[0].fx >= reflection_sample.fx){
                double expansion[] = new double[n];
                for (int i=0;i<n;i++){
                    expansion[i] = centroid[i] + gamma*(centroid[i]-simplex[m-1].x[i]);
                }
                getModel().getRuntime().println("expansion step");
                Sample expansion_sample = this.getSample(expansion);
                if (expansion_sample.fx < reflection_sample.fx){
                    simplex[m-1] = expansion_sample;
                    continue;
                }
            }
            //contraction
            if (simplex[m-1].fx <= reflection_sample.fx){                
                double contraction[] = new double[n];
                for (int i=0;i<n;i++){
                    contraction[i] = centroid[i] + rho*(centroid[i]-simplex[m-1].x[i]);
                }
                getModel().getRuntime().println("contraction step");
                Sample contraction_sample = this.getSample(contraction);
                if (contraction_sample.fx < simplex[m-1].fx){
                    simplex[m-1] = contraction_sample;
                    continue;
                }
            }
            //shrink
            for (int i=1;i<m;i++){
                double shrink[] = new double[n];
                for(int j=0;j<n;j++){
                    shrink[j] = simplex[0].x[j] + sigma*(simplex[i].x[j]-simplex[0].x[j]);
                }
                getModel().getRuntime().println("shrink step");
                simplex[i] = this.getSample(shrink);
            }                                                
        }        
    }        
}
