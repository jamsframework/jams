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
    public void run(){
        if (enable!=null)
            if (!enable.getValue()){
                singleRun();
                return;
            }
        //first draw random points        
        SampleMO simplex[] = new SampleMO[this.maxn.getValue()];        
        for (int i=0;i<this.maxn.getValue();i++){
            if (i==0&&x0!=null){
                try{
                    simplex[i] = this.getSample(x0);
                }catch(SampleLimitException e){
                    break;
                }
            }
            else{
                try{
                    simplex[i] = this.getSample(this.RandomSampler());
                }catch(SampleLimitException e){
                    break;
                }
            }
        }        
        getModel().getRuntime().println("*********************************************************");
        getModel().getRuntime().println(JAMS.i18n("Maximum_number_of_iterations_reached_finished_optimization"));
        getModel().getRuntime().println("*********************************************************");                                                      
    }        
}
