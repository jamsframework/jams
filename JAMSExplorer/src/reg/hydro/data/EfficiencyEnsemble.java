/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.hydro.data;

import java.util.Arrays;

/**
 *
 * @author chris
 */
public class EfficiencyEnsemble extends SimpleEnsemble{
    boolean isPostiveBest = true;

    public EfficiencyEnsemble(SimpleEnsemble s, boolean isPositiveBest) {
        super(s);
        this.isPostiveBest = isPositiveBest;
    }
    public EfficiencyEnsemble(String name, int size) {
        super(name,size);
    }

    public EfficiencyEnsemble(String name, int size, boolean isPositiveBest) {
        super(name,size);

        this.isPostiveBest = isPositiveBest;
    }

    public int findArgBest(){
        if (isPostiveBest)
            return super.findArgMax();
        return super.findArgMin();
    }

    
    public int findArgWorst(){
        if (isPostiveBest)
            return super.findArgMin();
        return super.findArgMax();
    }

    public void setPositiveBest(boolean flag){
        this.isPostiveBest = flag;
    }
    public boolean isPositiveBest(){
        return this.isPostiveBest;
    }

    public EfficiencyEnsemble CalculateLikelihood(){
        double Lmin = this.getMin();
        double Lmax = this.getMax();

        double[] likelihood = Arrays.copyOf(this.value, size);

        if (this.isPostiveBest) {
            for (int i = 0; i < size; i++) {
                likelihood[i] -= Lmin;
            }
        } else {
            for (int i = 0; i < size; i++) {
                likelihood[i] = -likelihood[i];
                likelihood[i] += Lmax;
            }
        }
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += likelihood[i];
        }
        for (int i = 0; i < size; i++) {
            likelihood[i] /= sum;
        }

        EfficiencyEnsemble eff = new EfficiencyEnsemble(this.name,this.size);
        eff.id = this.id;
        eff.isPostiveBest = true;
        eff.parent = this.parent;
        eff.value = likelihood;
        eff.update();

        return eff;
    }

    @Override
    public Integer[] sort(){
        return sort(!this.isPostiveBest);
    }
}
