/*
 * NNOptimizer.java
 *
 * Created on 8. November 2007, 11:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;

import Jama.Matrix;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import org.unijena.jams.JAMS;
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.model.JAMSComponentDescription;
import org.unijena.jams.model.JAMSVarDescription;

//import jams.components.optimizer.
@JAMSComponentDescription(
        title="NNOptimizer",
        author="Christian Fischer",
        description="under construction!!"
        )
public class Sandbox extends Optimizer{
    /*
     *  Component variables
     */    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
           
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "objective function name"
            )
            public JAMSString effMethodName;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "the prediction series"
            )
            public JAMSDouble effValue;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode"
            )
            public JAMSInteger mode;
          
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximum runs"
            )
            public JAMSInteger maxn;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling/disabling this sampler"
            )
            public JAMSBoolean enable;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString outputFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString modelGridFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString SampleDumpFileName;
           
    BufferedWriter writer = null;
    final double Version = 1.0;
    
    final int initalSampleSize = 50;
            
    public void init(){
        super.init();
                        
        try {
            writer = new BufferedWriter(new FileWriter(this.dirName + "/" + SampleDumpFileName.getValue()));
        } catch (IOException ioe) {
            JAMS.handle(ioe);
        }
    }
     
    double TransformAndEvaluate(double []in){
        double value[] = new double[in.length];
        for (int i=0;i<in.length;i++){
            value[i] = in[i]*(this.upBound[i]-this.lowBound[i]) + this.lowBound[i];
        }        
        return this.funct(value);
    }
    
    public void initalPhase(){                
        for (int i=0;i<n*initalSampleSize;i++){
            double nextSample[] = this.RandomSampler();
            for (int j=0;j<n;j++){
                nextSample[j] = (nextSample[j] - lowBound[j])/(upBound[j]-lowBound[j]);
            }
            this.getSample(nextSample);            
        }
        
    }
            
    public class DoubleIndex{
        public double value;
        public int index;
        
        DoubleIndex(double value,int index){
            this.value = value;
            this.index = index;
        }
    }
    
    double norm(double x[],double y[]){
        double norm = 0;
        for (int i=0;i<x.length;i++){
            norm += (x[i]-y[i])*(x[i]-y[i]);
        }
        return norm;
    }
    
    double localFactor = 1.0;
    
    public class SandBoxEffFunction extends AbstractFunction{                       
        public double f(double x[]){
            final int neighbours = n+7;
            //find next points            
            DoubleIndex nearest[] = new DoubleIndex[neighbours];
                  
            for (int i=0;i<neighbours;i++){
                nearest[i] = new DoubleIndex(1000000000.0,i);
            }
            
            for (int i=0;i<sampleList.size();i++){
                double dist = norm(x,sampleList.get(i).x);
                DoubleIndex cmp = new DoubleIndex(0,-1);
                                
                for (int j=0;j<neighbours;j++){
                    double d = dist - nearest[j].value;
                    if (d < cmp.value){
                        cmp.value = d;
                        cmp.index = j;
                    }
                }
                if (cmp.index != -1){
                    nearest[cmp.index].index = i;
                    nearest[cmp.index].value = dist;
                }
            }
            
            Vector<Sample> tmp_list = new Vector<Sample>();
            for (int i=0;i<neighbours;i++){
                boolean isIn = false;
                for (int j=0;j<tmp_list.size();j++){
                    double d = norm(tmp_list.get(j).x,sampleList.get(i).x);
                    if (d < 0.000001)
                        isIn = true;
                }
                if (!isIn)
                    tmp_list.add(sampleList.get(i));                
            }
            Matrix L = null;                
            Matrix V = new Matrix(tmp_list.size(),n+1);
            Matrix y = new Matrix(tmp_list.size(),1);
            for (int i=0;i<tmp_list.size();i++){                        
                Sample r = tmp_list.get(i);
                for (int j=0;j<n;j++){
                    V.set(i,j,r.x[j]);
                }
                V.set(i,n,1.0);
                y.set(i,0,r.fx);
            }                    
            try{
                L = (((V.transpose()).times(V)).inverse()).times(V.transpose().times(y));
            }catch(Exception e){
                //mat is sing. 
                System.out.println("singl. Matrix");                 
            }
                  
            
            double fctValue = 0;
            double normFct = 0;
            for (int i=0;i<neighbours;i++){                
                Sample b = tmp_list.get(i);
                double diff = 0;
                for (int j=0;j<x.length;j++){
                    diff += Math.abs((x[j]-b.x[j])*L.get(j,0));
                }
                fctValue += (b.fx - diff)*(1.0/Math.pow(nearest[i].value,localFactor));
                normFct += 1.0/Math.pow(nearest[i].value,localFactor);
            }
            
            return fctValue / normFct;
        }
    }
    
    public Sample FindMostProbablePoint(){
        SandBoxEffFunction function = new SandBoxEffFunction();
        
        SimpleSCE sce = new SimpleSCE();
        double[] normedLowBound = new double[n];
        double[] normedUpBound = new double[n];
        for (int i=0;i<n;i++){
            normedLowBound[i] = 0.0;
            normedUpBound[i] = 1.0;
        }
            
        return sce.offlineRun(null,normedLowBound,normedUpBound,3,Optimizer.MODE_MAXIMIZATION,10000,12,0.05,0.0001,function);                                
    }
    
    public void run() {     
        initalPhase();
        double best = 1000.0;
        int counter = 0;
        while (true) {
            if (counter % 3 == 0)
                localFactor = 1.0;
            if (counter % 3 == 1)
                localFactor = 2.0;
            if (counter % 3 == 2)
                localFactor = 0.5;
            double next[] = FindMostProbablePoint().x;
            Sample test = this.getSample(next);            
            best = Math.min(test.fx, best);
            System.out.println("BestValue:" + best + "\nk:" + sampleList.size() + "\nMyPoint:" + test.fx);
            counter++;
        }
    }
               
}
