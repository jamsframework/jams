/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.optimizer;

import jams.JAMS;
import jams.data.Attribute;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class MultiPointRandomSampler extends MOOptimizer{
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "points for derivation"
            )
            public Attribute.Integer pfd;

     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "distance used in gradient estimation",
            defaultValue = "0.01"
            )
            public Attribute.Double k;

    private static final long serialVersionUID = -41284433223496L;

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

        double d[] = new double[n];
        for (int j=0;j<n;j++){
            d[j] = this.upBound[j] - this.lowBound[j];
        }

        //draw random points
        
        int iterations = this.maxn.getValue() / (n * pfd.getValue() + 1);

        for (int j = 0; j < iterations; j++) {
            double set[] = this.RandomSampler();
            try {
                this.getSample(set);
            } catch (SampleLimitException sle) {
                break;
            }            
            double nextPoint[] = Arrays.copyOf(set, set.length);
            
            for (int i = 0; i < n * pfd.getValue(); i++) {
                for (int l = 0; l < n; l++) {
                    double p = set[l];
                    if (generator.nextBoolean()) {
                        nextPoint[l] = p + (this.upBound[l]-this.lowBound[l])*k.getValue();
                    } else {
                        nextPoint[l] = p - (this.upBound[l]-this.lowBound[l])*k.getValue();
                    }
                }
                try {
                    this.getSample(nextPoint);
                } catch (SampleLimitException sle) {
                    break;
                }
            }
        }
        getModel().getRuntime().println("*********************************************************");
        getModel().getRuntime().println(JAMS.i18n("Maximum_number_of_iterations_reached_finished_optimization"));
        getModel().getRuntime().println("*********************************************************");
    }
}
