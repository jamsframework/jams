/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.JAMS;
import jams.components.optimizer.MOOptimizer.SampleMO;
import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class RandomSampler extends MOOptimizer{           
    SerializableBufferedWriter writer = null;
            
    public SampleMO[] initialSimplex = null;
           
    @SuppressWarnings("unchecked")    
    @Override
    public void init(){
        super.init();
    }   
    @Override
    public void procedure()throws SampleLimitException, ObjectiveAchievedException{
        if (enable!=null)
            if (!enable.getValue()){
                singleRun();
                return;
            }        
        SampleMO simplex[] = new SampleMO[this.maxn.getValue()];
        int i=0;
        if (x0!=null)
            simplex[i++] = this.getSample(x0);
        while(true){
            simplex[i++] = this.getSample(this.RandomSampler());              
        }                
    }        
}
