/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer;


import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.SampleFactory.Sample;
import umontreal.iro.lecuyer.hups.PointSetIterator;
import umontreal.iro.lecuyer.hups.SobolSequence;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class SobolsSequenceSampling extends Optimizer{           
    SerializableBufferedWriter writer = null;


    public double offset = 0;
    public  boolean analyzeQuality = true;
    public  double targetQuality = 0.8;
    public double minn = 0;

    public double getMinn(){
        return minn;
    }
    public void setMinn(double minn){
        this.minn = minn;
    }
    
    public double getOffset(){
        return this.offset;
    }

    public void setOffset(double offset){
        this.offset = offset;
    }

    public void setAnalyzeQuality(boolean analyzeQuality){
        this.analyzeQuality = analyzeQuality;
    }

    public boolean isAnalyzeQuality(){
        return this.analyzeQuality;
    }

    public void setTargetQuality(double targetQuality){
        this.targetQuality = targetQuality;
    }

    public double getTargetQuality(){
        return this.targetQuality;
    }

    public Sample[] initialSimplex = null;

    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(SobolsSequenceSampling.class.getSimpleName(), SobolsSequenceSampling.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("offset",
                "offset", 0, 0, Integer.MAX_VALUE));

        desc.addParameter(new BooleanOptimizerParameter("analyzeQuality",
                "analyzeQuality", false));

        desc.addParameter(new NumericOptimizerParameter("targetQuality",
                "targetQuality", 0.8, -100.0, 1.0));
        
        return desc;
    }
        
    @Override
    public void procedure()throws SampleLimitException, ObjectiveAchievedException{
        int k = (int)Math.ceil(Math.log(maxn)/Math.log(2.0));
        SobolSequence s = new SobolSequence(k, 31, n);

        PointSetIterator iter = s.iterator();
        Sample simplex[] = new Sample[(int)this.getMaxn()];
        int i=0;
        ArrayList<double[]> set = new ArrayList<double[]>();
        while(iter.hasNextPoint()){
            double x0[] = new double[n];
            iter.nextPoint(x0, n);
            for (int j=0;j<n;j++){
                x0[j] = this.lowBound[j]+x0[j]*(this.upBound[j]-this.lowBound[j]);
            }
            set.add(x0);
        }

        while(set.size()>0){
            int v = this.generator.nextInt(set.size());
            simplex[i] = this.getSample(set.get(v));
            set.set(v, set.get(set.size()-1));
            set.remove(set.size()-1);
            i++;
        }
    }

    public static void main(String[] args) {
        SobolsSequenceSampling hss = new SobolsSequenceSampling();
        hss.maxn = 2048;

        int n = 20;
        int m = 1;

        hss.n = n;
        hss.m = m;
        hss.lowBound = new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        hss.upBound = new double[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        hss.objNames = new String[]{"y"};
        hss.offset = 0;

        hss.x0 = null;
        hss.setParameterNames(new String[]{"x0,x1,x2,x3,x4,x5,x6,x7,x8,x9"});
        hss.setWorkspace(new File("C:/Arbeit/"));
        hss.setFunction(new AbstractFunction() {

            @Override
            public double[] f(double[] x) {
                return new double[]{1.0};
            }

            @Override
            public void logging(String msg) {
                System.out.println(msg);
            }
        });

        hss.init();

        Arrays.toString(hss.optimize().toArray());
    }
}
