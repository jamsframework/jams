/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.sampler;


import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;
import java.util.ArrayList;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.data.DataCollection;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.StringOptimizerParameter;
import optas.optimizer.parallel.ParallelSequence;
import umontreal.iro.lecuyer.hups.PointSetIterator;
import umontreal.iro.lecuyer.hups.SobolSequence;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class SobolsSequence extends Optimizer{           
    SerializableBufferedWriter writer = null;

    public String excludeFiles = "";    
    public double threadCount = 12;
    public boolean parallelExecution = false;
    public Sample[] initialSimplex = null;

    @Override
    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(SobolsSequence.class.getSimpleName(), SobolsSequence.class.getName(), 500, false);
        
        desc.addParameter(new BooleanOptimizerParameter("parallelExecution",
                "parallelExecution", false));
        
        desc.addParameter(new StringOptimizerParameter("excludeFiles",
                "excludeFiles","(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log"));

        desc.addParameter(new NumericOptimizerParameter("threadCount",
                "threadCount", 8, 2, 100.0));
        
        return desc;
    }
    
    ParallelSequence pSeq = null;
    transient DataCollection collection = null;
        
    @Override
    public boolean init() {
        if (!super.init()) {
            return false;
        }

        if (parallelExecution){
            pSeq = new ParallelSequence(this);
            pSeq.setExcludeFiles(excludeFiles);
            pSeq.setThreadCount((int)this.threadCount);
        }
        return true;
    }
    
    @Override
    public void procedure()throws SampleLimitException, ObjectiveAchievedException{
        SobolSequence s = new SobolSequence((int)Math.ceil(maxn), n);

        PointSetIterator iter = s.iterator();
        
        int i=0;
        ArrayList<double[]> set = new ArrayList<double[]>();
        boolean first = true;
        while(iter.hasNextPoint()){
            double x0[] = new double[n];
            iter.nextPoint(x0, n);
            for (int j=0;j<n;j++){
                x0[j] = this.lowBound[j]+x0[j]*(this.upBound[j]-this.lowBound[j]);
            }
            if (first)
                first = false;
            else
                set.add(x0);
        }

        int samplesPerIteration2 = (int) (threadCount * 6);
        if (samplesPerIteration2 == 0) {
            samplesPerIteration2 = 100;
        }
                
        while(i<maxn){
            //int currentOffset = (int) offset + (i * samplesPerIteration2);
            //simplex[i] = this.getSample(x);
            
            int sampleCount = (int) Math.min(samplesPerIteration2, this.maxn - i);
            
            double x[][] = new double[sampleCount][];
            for (int j = 0; j < sampleCount; j++) {
                x[j] = set.remove(0);
                i++;
            }
                  
            if (parallelExecution) {
                ParallelSequence.OutputData result = pSeq.procedure(x);

                if (collection == null) {
                    collection = result.dc;
                } else {
                    synchronized (collection) {
                        if (result.dc != null) {
                            collection.mergeDataCollections(result.dc);
                        }
                    }
                }
                if (result.list!=null){
                    this.injectSamples(result.list);
                }
                if (collection != null) {                    
                    collection.dump(getModel().getWorkspace().getOutputDataDirectory(), true);
                }
            } else {
                for (int j=0;j<x.length;j++){
                    getSample(x[j]);
                }
            }
        }
        if (collection != null) {
            collection.dump(getModel().getWorkspace().getOutputDataDirectory(), false);
        }
    }
}