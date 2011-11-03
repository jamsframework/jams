/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.hydro.data;

import java.util.Arrays;

/**
 *
 * @author chris
 */
public class EfficiencyEnsemble extends SimpleEnsemble{
    boolean isPostiveBest = true;
    double rangeMin = Double.NEGATIVE_INFINITY, rangeMax = Double.POSITIVE_INFINITY;

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

    public EfficiencyEnsemble(String name, int size, boolean isPositiveBest, double rangeMin, double rangeMax) {
        super(name,size);

        this.isPostiveBest = isPositiveBest;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }

    public enum Method{NashSutcliffe, RootMeanSquareError, AbsoluteError, Bias}

    @Override
    public void add(Integer id, double value){
        if (value > this.rangeMax)
            value = rangeMax;
        if (value < this.rangeMin)
            value = rangeMin;
        super.add(id,value);
    }

    public EfficiencyEnsemble(String name, Measurement obs, TimeSerieEnsemble sim, Method m) {
        super(name, sim.size);        

        int K = sim.getTimesteps();

        switch(m){
            case NashSutcliffe:{
                double aobs = 0;
                for (int i=0;i<K;i++){
                    aobs += obs.getValue(i);
                }
                aobs /= K;
                double denumerator = 0;
                for (int i=0;i<K;i++){
                    double d = obs.getValue(i)-aobs;
                    denumerator += d*d;
                }

                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);

                    double numerator = 0;


                    for (int j=0;j<K;j++){
                        double d1 = (sim.get(j, id_i) - obs.getValue(j));                        
                        numerator += d1*d1;
                    }
                    double e2 = 1.0 - (numerator / denumerator);
                    this.add(id_i, e2);
                }
                this.isPostiveBest = true;
                break;
            }
            case RootMeanSquareError:{
                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);

                    double rsme = 0;

                    for (int j=0;j<K;j++){
                        double d1 = (sim.get(j, id_i) - obs.getValue(j));
                        rsme += d1*d1;
                    }                    
                    this.add(id_i, rsme);
                }
                this.isPostiveBest = false;
                break;
            }
            case AbsoluteError:{
                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);

                    double abse = 0;

                    for (int j=0;j<K;j++){
                        double d1 = (sim.get(j, id_i) - obs.getValue(j));
                        abse += Math.abs(d1);
                    }                    
                    this.add(id_i, abse);
                }
                this.isPostiveBest = false;
                break;
            }
            case Bias:{
                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);

                    double bias = 0;

                    for (int j=0;j<K;j++){
                        double d1 = (sim.get(j, id_i) - obs.getValue(j));
                        bias += (d1);
                    }                    
                    this.add(id_i, bias);
                }
                this.isPostiveBest = false;
                break;
            }
        }
    }

    @Override
     public Ensemble clone(){
        EfficiencyEnsemble s = new EfficiencyEnsemble(name, size, isPostiveBest);
        for (int i=0;i<size;i++)
            s.set(i, this.getId(i));
        s.currentIndex = currentIndex;
        s.id = id.clone();
        s.parent = parent;
        s.value = value.clone();

        return s;
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
