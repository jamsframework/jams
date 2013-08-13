/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.experimental;


import optas.core.SampleLimitException;
import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;
import java.util.Random;
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

public class PostSampling extends Optimizer{           
    SerializableBufferedWriter writer = null;
    Random rnd = new Random();
    
    public Sample[] initialSimplex = null;

    public double threshold = 0.1;
    double minBound[] = null;
    double maxBound[] = null;

    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    public double getThreshold(){
        return threshold;
    }

    public double[][] getRange(){
        return new double[][]{minBound,maxBound};
    }

    @Override
    public OptimizerDescription getDescription() {
        return OptimizerLibrary.getDefaultOptimizerDescription(PostSampling.class.getSimpleName(), PostSampling.class.getName(), 500, false);
    }

    private double[] transform(double x[]){
        double[] y = new double[n];
        for (int i=0;i<n;i++){
            y[i] = lowBound[i] + x[i]*(upBound[i]-lowBound[i]);
        }
        return y;
    }

    private double[] reTransform(double y[]){
        double[] x = new double[n];
        for (int i=0;i<n;i++){
            if (upBound[i]-lowBound[i] > 0)
                x[i] = (y[i]-lowBound[i])/(upBound[i]-lowBound[i]);
            else
                x[i] = (y[i]-lowBound[i]);
        }
        return x;
    }
    
    @Override
    public void procedure() throws ObjectiveAchievedException {
        minBound = new double[n];
        maxBound = new double[n];

        try {
            Sample y0 = this.getSample(x0[0]);

            for (int i=0;i<n;i++){
                double lplus=0.5,lminus=0.5;

                for (int k=0;k<8;k++){
                    double xiplus[] = new double[n];
                    double ximinus[] = new double[n];
                    for (int j = 0; j < n; j++) {
                        xiplus[j] = reTransform(x0[0])[j] + lplus;
                        if (xiplus[j]>1.0)  xiplus[j]=1.0;

                        ximinus[j] = reTransform(x0[0])[j] - lminus;
                        if (ximinus[j]<0.0)  ximinus[j]=0.0;
                    }
                    Sample sPlus = this.getSample(transform(xiplus));
                    Sample sMinus = this.getSample(transform(ximinus));

                    boolean isValidPlus = true;
                    boolean isValidMinus = true;
                    for (int j = 0; j < m; j++) {
                        if (!(sPlus.F()[j] < (1.0 + threshold) * y0.F()[j])) {
                            isValidPlus = false;
                        }
                        if (!(sMinus.F()[j] < (1.0 + threshold) * y0.F()[j])) {
                            isValidMinus = false;
                        }
                    }
                    if (isValidPlus){
                        lplus=lplus + lplus/2.0;
                        maxBound[i] = transform(xiplus)[i];
                    }else
                        lplus=lplus - lplus/2.0;

                    if (isValidMinus){
                        lminus=lminus + lminus/2.0;
                        minBound[i] = transform(ximinus)[i];
                    }else
                        lminus=lminus - lminus/2.0;
                }
            }

            while (true) {
                double v[] = new double[n];
                for (int j = 0; j < n; j++) {
                    v[j] = rnd.nextDouble()*(maxBound[j]-minBound[j])+minBound[j];
                    
                }
                this.getSample(v);
            }
        } catch (SampleLimitException sle) {
        }        
    }        
}
