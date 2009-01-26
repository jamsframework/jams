/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.components.optimizer.Optimizer.Sample;
import java.util.Arrays;
import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class RandomSampler extends Optimizer{           
    SerializableBufferedWriter writer = null;
            
    public Sample[] initialSimplex = null;
           
    @SuppressWarnings("unchecked")    
    public void init(){
        super.init();
    }
                
    public void run(){
        if (enable!=null)
            if (!enable.getValue()){
                singleRun();
                return;
            }
        boolean stop = false;
    
        //first draw random points        
        Sample simplex[] = new Sample[this.maxn.getValue()];
        
        for (int i=0;i<this.maxn.getValue();i++){
            if (i==0&&x0!=null)
                simplex[i] = this.getSample(x0);
            else
                simplex[i] = this.getSample(this.RandomSampler());
        }
        
        getModel().getRuntime().println("*********************************************************");
        getModel().getRuntime().println("Maximum number of iterations reached, finished optimization");
        getModel().getRuntime().println("*********************************************************");
                                                      
    }        
}
