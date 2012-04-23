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

    TimeFilter filter = null;

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

    public enum Method{NashSutcliffe, NashSutcliffe1, RootMeanSquareError, AbsoluteError, Bias, logNashSutcliffe, logNashSutcliffe1}

    @Override
    public void add(Integer id, double value){
        if (value > this.rangeMax)
            value = rangeMax;
        if (value < this.rangeMin)
            value = rangeMin;
        super.add(id,value);
    }

    boolean[] filterMap;

    public EfficiencyEnsemble(String name, Measurement obs, TimeSerieEnsemble sim, Method m, TimeFilter filter) {
        super(name, sim.size);        

        int K = sim.getTimesteps();

        this.filterMap = new boolean[K];

        int kStar = 0;
        if (filter!=null){
            this.filter = filter;
            for (int i=0;i<K;i++){
                filterMap[i] = filter.isFiltered(obs.getTime(i)) || obs.getValue(i)==-9999;
                if (!filterMap[i])
                    kStar++;
            }
        }else{
            kStar = K;
        }

        double filteredObservation[] = new double[kStar];
        double filteredSim[][] = new double[sim.size][kStar];
        int c=0;
        for (int i=0;i<K;i++){
            if (!filterMap[i])
                filteredObservation[c] = obs.getValue(i);
            for (int j=0;j<sim.size;j++){
                int id_i = sim.getId(j);
                if (!filterMap[i]){
                    filteredSim[j][c] = sim.get(i, id_i);
                }
            }
            if (!filterMap[i])
                c++;
        }


        switch(m){
            case logNashSutcliffe:{
                double aobs = 0;
                for (int i=0;i<kStar;i++){
                    if (filteredObservation[i]>0){
                        aobs += Math.log(filteredObservation[i]);
                    }
                }
                aobs /= filteredObservation.length;

                double denumerator = 0;
                for (int i=0;i<kStar;i++){
                    if (filteredObservation[i]>0){
                        double d = Math.log(filteredObservation[i])-aobs;
                        denumerator += d*d;
                    }
                }

                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);
                    double numerator = 0;
                    for (int j=0;j<kStar;j++){
                        if (filteredObservation[j]>0 && filteredSim[i][j]>0){
                            double d1 = (Math.abs(Math.log(filteredSim[i][j]) - Math.log(filteredObservation[j])));
                            numerator += d1*d1;
                        }
                    }
                    double e2 = 1.0 - (numerator / denumerator);
                    this.add(id_i, e2);
                }
                this.isPostiveBest = true;
                break;
            }
            case logNashSutcliffe1:{
                double aobs = 0;
                for (int i=0;i<kStar;i++){
                    if (filteredObservation[i]>0){
                        aobs += Math.log(filteredObservation[i]);
                    }
                }
                aobs /= filteredObservation.length;

                double denumerator = 0;
                for (int i=0;i<kStar;i++){
                    if (filteredObservation[i]>0){
                        double d = Math.log(filteredObservation[i])-aobs;
                        denumerator += Math.abs(d);
                    }
                }

                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);
                    double numerator = 0;
                    for (int j=0;j<kStar;j++){
                        if (filteredObservation[j]>0 && filteredSim[i][j]>0){
                            double d1 = Math.abs(Math.log(filteredSim[i][j]) - Math.log(filteredObservation[j]));
                            numerator += Math.abs(d1);
                        }
                    }
                    double e2 = 1.0 - (numerator / denumerator);
                    this.add(id_i, e2);
                }
                this.isPostiveBest = true;
                break;
            }
            case NashSutcliffe:{
                double aobs = 0;
                for (int i=0;i<kStar;i++){
                    aobs += filteredObservation[i];                    
                }
                aobs /= filteredObservation.length;

                double denumerator = 0;
                for (int i=0;i<kStar;i++){
                    double d = filteredObservation[i]-aobs;
                    denumerator += d*d;
                }

                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);
                    double numerator = 0;
                    for (int j=0;j<kStar;j++){
                        double d1 = filteredSim[i][j] - filteredObservation[j];
                        numerator += d1*d1;
                    }
                    double e2 = 1.0 - (numerator / denumerator);
                    this.add(id_i, e2);
                }
                this.isPostiveBest = true;
                break;
            }
            case NashSutcliffe1:{
                double aobs = 0;
                for (int i=0;i<kStar;i++){
                    aobs += filteredObservation[i];
                }
                aobs /= filteredObservation.length;

                double denumerator = 0;
                for (int i=0;i<kStar;i++){
                    double d = filteredObservation[i]-aobs;
                    denumerator += Math.abs(d);
                }

                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);
                    double numerator = 0;
                    for (int j=0;j<kStar;j++){
                        double d1 = filteredSim[i][j] - filteredObservation[j];
                        numerator += Math.abs(d1);
                    }
                    double e1 = 1.0 - (numerator / denumerator);
                    this.add(id_i, e1);
                }
                this.isPostiveBest = true;
                break;
            }
            case RootMeanSquareError:{
                for (int i=0;i<sim.size;i++){
                    int id_i = sim.getId(i);

                    double rsme = 0;

                    for (int j=0;j<kStar;j++){
                         double d1 = filteredSim[i][j] - filteredObservation[j];
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

                    for (int j=0;j<kStar;j++){
                        double d1 = filteredSim[i][j] - filteredObservation[j];
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

                    for (int j=0;j<kStar;j++){
                        double d1 = filteredSim[i][j] - filteredObservation[j];
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
        double[] likelihood = Arrays.copyOf(this.value, size);

        if (this.isPostiveBest) {
            double Lmin = this.getMin();

            for (int i = 0; i < size; i++) {
                likelihood[i] = likelihood[i] - Lmin;
            }

        }else{
            double Lmin = 1.0 - this.getMax();

            for (int i = 0; i < size; i++) {
                likelihood[i] = (1.0-likelihood[i]) - Lmin;
            }
        }
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += likelihood[i];
        }

        sum = 0;
        for (int i = 0; i < size; i++) {
            sum += (likelihood[i]);
        }
        for (int i = 0; i < size; i++) {
            likelihood[i] /= sum;
        }

        EfficiencyEnsemble eff = new EfficiencyEnsemble(this.name,this.size);
        for (int i=0;i<size;i++)
            eff.set(i, this.getId(i));
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
