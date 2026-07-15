/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.management;

import java.util.ArrayList;
import optas.optimizer.management.SampleFactory.Sample;




/**
 *
 * @author chris
 */
public class SOStatistics extends Statistics{    
    public SOStatistics(ArrayList<Sample> sampleList) {
        super(sampleList);
    }
    
    public double calcVariance(){
        return calcVariance(0, size(), 0);
    }

    public double calcVariance(int start, int last){
        return calcVariance(start, last, 0);

    }

    public double calcMean(){
        return calcMean(0, size(), 0);
    }

    public double calcMean(int start, int last){
        return calcMean(start, last, 0);
    }
   
    public double calcImprovement(int last){
        return calcImprovement(last,0);
    }

    public Sample getMin(){
        return getMinimumInRange(0, size(), 0);
    }

    public Sample getMax(){
        return getMaximumInRange(0, size(), 0);
    }

    public Sample getMinimumInRange(int start, int last){
        return getMinimumInRange(start, last, 0);
    }

    public Sample getMaximumInRange(int start, int last){
        return getMaximumInRange(start, last, 0);
    }
    //TODO: implement this function .. 
    @Override
    public ArrayList<Sample> getSamplesByRank(int rk){
        return null;
    }
    
    public Sample getBestSample(){
        return getMin();
    }


}
