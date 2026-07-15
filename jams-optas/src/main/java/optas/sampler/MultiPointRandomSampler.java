/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.sampler;

import optas.core.SampleLimitException;
import jams.model.JAMSComponentDescription;
import java.util.Arrays;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.OptimizerDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class MultiPointRandomSampler extends Optimizer{
     private double k;
    private int pfd;

     public OptimizerDescription getDescription() {
        return OptimizerLibrary.getDefaultOptimizerDescription(MultiPointRandomSampler.class.getSimpleName(), MultiPointRandomSampler.class.getName(), 500, true);
    }

    private static final long serialVersionUID = -41284433223496L;
        
    @Override
    public void procedure()throws SampleLimitException, ObjectiveAchievedException{        
        double d[] = new double[n];
        for (int j=0;j<n;j++){
            d[j] = this.upBound[j] - this.lowBound[j];
        }

        //draw random points
        
        int iterations = this.getMaximumIterationCount() / (n * pfd + 1);

        for (int j = 0; j < iterations; j++) {
            double set[] = this.randomSampler();
            try {
                this.getSample(set);
            } catch (SampleLimitException sle) {
                break;
            }            
            double nextPoint[] = Arrays.copyOf(set, set.length);
            
            for (int i = 0; i < n * pfd; i++) {
                for (int l = 0; l < n; l++) {
                    double p = set[l];
                    if (generator.nextBoolean()) {
                        nextPoint[l] = p + (this.upBound[l]-this.lowBound[l])*getK();
                    } else {
                        nextPoint[l] = p - (this.upBound[l]-this.lowBound[l])*getK();
                    }
                }
                try {
                    this.getSample(nextPoint);
                } catch (SampleLimitException sle) {
                    break;
                }
            }
        }
        log("*********************************************************");
        log("Maximum_number_of_iterations_reached_finished_optimization");
        log("*********************************************************");
    }

    /**
     * @return the k
     */
    public double getK() {
        return k;
    }

    /**
     * @param k the k to set
     */
    public void setK(double k) {
        this.k = k;
    }

    /**
     * @return the pdf
     */
    public double getPfd() {
        return pfd;
    }

    /**
     * @param pdf the pdf to set
     */
    public void setPfd(double pfd) {
        this.pfd = (int)pfd;
    }
}
