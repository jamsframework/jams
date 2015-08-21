/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.sampler;

import optas.core.SampleLimitException;
import jams.JAMS;
import jams.model.JAMSComponentDescription;
import optas.optimizer.management.SampleFactory.Sample;
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
public class LatinHyperCubeSampler extends Optimizer{
    public Sample[] initialSimplex = null;
    int availableIndexSet[][];
    int divisions=0;
    int freeIndexCount=0;

    public OptimizerDescription getDescription(){
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(LatinHyperCubeSampler.class.getSimpleName(), LatinHyperCubeSampler.class.getName(), 250, true);
        return desc;
    }

    /*public int[] generateRandomPermutation(int M){
        int src_set[] = new int[M];
        int perm[] = new int[M];

        for (int i=0;i<M;i++){
            src_set[i] = i;
        }
        for (int i=M;i>0;i++){
            int index = generator.nextInt(i);
            perm[M-i] = index;
        }
        return perm;
    }*/

    void initIndexSet(){
        availableIndexSet = new int[n][divisions];
        for (int i=0;i<n;i++){
            for (int j=0;j<divisions;j++){
                availableIndexSet[i][j] = j;
            }
        }
        freeIndexCount = divisions;
    }

    int[] getFreeIndexSet(){
        if (freeIndexCount<=0)
            return null;

        int indexSet[] = new int[n];
        for (int i=0;i<n;i++){
            int index = generator.nextInt(freeIndexCount);
            indexSet[i] = availableIndexSet[i][index];
            availableIndexSet[i][index] = availableIndexSet[i][freeIndexCount-1];
        }
        freeIndexCount--;
        return indexSet;
    }

    public void procedure(){
        divisions = (int)this.getMaxn();

        initIndexSet();

        double d[] = new double[n];
        for (int j=0;j<n;j++){
            d[j] = this.upBound[j] - this.lowBound[j];
        }
        //first draw random points
        Sample simplex[] = new Sample[divisions];
        for (int i = 0; i < divisions; i++) {
            int indexSet[] = getFreeIndexSet();
            double[] sample = new double[n];
            for (int j = 0; j < n; j++) {
                sample[j] = (((double) indexSet[j] + 0.5) * (d[j] / (double) divisions)) + lowBound[j];
            }
            try {
                simplex[i] = this.getSample(sample);
            } catch (SampleLimitException e) {
                break;
            } catch (ObjectiveAchievedException e) {
                break;
            }
        }

        log("*********************************************************");
        log(JAMS.i18n("Maximum_number_of_iterations_reached_finished_optimization"));
        log("*********************************************************");
    }
}
