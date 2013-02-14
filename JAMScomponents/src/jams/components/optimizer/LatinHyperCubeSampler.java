/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.JAMS;
import jams.components.optimizer.SampleFactory.Sample;

import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class LatinHyperCubeSampler extends MOOptimizer{
    SerializableBufferedWriter writer = null;

    public Sample[] initialSimplex = null;

    @SuppressWarnings("unchecked")
    @Override
    public void init(){
        super.init();
    }

    int availableIndexSet[][];
    int divisions=0;
    int freeIndexCount=0;

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

    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException{
        if (enable!=null)
            if (!enable.getValue()){
                singleRun();
                return;
            }
        divisions = this.maxn.getValue();

        initIndexSet();

        double d[] = new double[n];
        for (int j=0;j<n;j++){
            d[j] = this.upBound[j] - this.lowBound[j];
        }
        //first draw random points
        Sample simplex[] = new Sample[divisions];
        for (int i = 0; i < divisions; i++) {
            int indexSet[] = getFreeIndexSet();
            double[] sample = new double[divisions];
            for (int j = 0; j < n; j++) {
                sample[j] = (((double) indexSet[j] + 0.5) * (d[j] / (double) divisions)) + lowBound[j];
            }
            try {
                simplex[i] = this.getSample(sample);
            } catch (SampleLimitException e) {
                break;
            }
        }

        getModel().getRuntime().println("*********************************************************");
        getModel().getRuntime().println(JAMS.i18n("Maximum_number_of_iterations_reached_finished_optimization"));
        getModel().getRuntime().println("*********************************************************");
    }
}
