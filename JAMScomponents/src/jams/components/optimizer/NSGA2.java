/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.optimizer;

import jams.JAMS;
import jams.components.optimizer.SampleFactory.Sample;
import jams.components.optimizer.SampleFactory.SampleComperator;
import jams.data.Attribute;
import jams.data.JAMSBoolean;
import jams.data.JAMSDataFactory;
import jams.data.JAMSDouble;
import jams.data.JAMSString;
import jams.model.JAMSVarDescription;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * @author Christian Fischer
 */
public class NSGA2 extends MOOptimizer {
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "population size",
    defaultValue="30")
    public Attribute.Integer populationSize;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "probability for population crossover, range between 0.5 and 1",
            defaultValue="0.9")
    public Attribute.Double crossoverProbability;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "probability for population mutation, range between 0.0 and 1/nvar",
            defaultValue="1.0")
    public Attribute.Double mutationProbability;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "crossover distribution index, range between 0.5 and 100",
            defaultValue="20")
    public Attribute.Double crossoverDistributionIndex;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "mutation distribution index, range between 0.5 and 100",
            defaultValue="20")
    public Attribute.Double mutationDistributionIndex;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "maximum number of generations",
            defaultValue="10000")
    public Attribute.Integer maxGeneration;

    int crossoverCount = 0;
    int mutationCount = 0;

    SampleComperator moComparer = new SampleComperator(false);
    CustomRand generator = null;

    static private class Individual {
        Sample sample;
        int rank;/*Rank of the individual*/
        double cub_len;/*crowding distance of the individual*/

        public Individual(Sample sample) {
            this.sample = sample;
            this.rank = 0;
            this.cub_len = 0.0;
        }
    }
    private class Population {
        int maxrank;        /*Maximum rank present in the population*/
        int rankar[][];     /*record of array of individual numbers at a particular rank */
        int rankno[];       /*Individual at different ranks*/
        Individual ind[];   /*Different Individuals*/
        int size;
        
        public Population(int size) {
            this.size = size;
            rankno = new int[size+1];
            ind = new Individual[size];
            rankar = new int[size][size];
        }
    }

    public static class CustomRand {
        double oldrand[] = new double[55];
        int jrand = 0;

        CustomRand(double random_seed) {
            double new_random  = 0.000000001,
                   prev_random = random_seed;
            oldrand[54] = random_seed;

            for (int j1 = 1; j1 <= 54; j1++) {
                int ii = (21 * j1) % 54;
                oldrand[ii] = new_random;
                new_random = prev_random - new_random;
                if (new_random < 0.0) {
                    new_random = new_random + 1.0;
                }
                prev_random = oldrand[ii];
            }
            advance_random();
            advance_random();
            advance_random();
            jrand = 0;
        }

        /* Create next batch of 55 random numbers */
        private void advance_random() {
            double new_random;
            for (int j1 = 0; j1 < 24; j1++) {
                new_random = oldrand[j1] - oldrand[j1 + 31];
                if (new_random < 0.0) {
                    new_random = new_random + 1.0;
                }
                oldrand[j1] = new_random;
            }
            for (int j1 = 24; j1 < 55; j1++) {
                new_random = oldrand[j1] - oldrand[j1 - 24];
                if (new_random < 0.0) {
                    new_random = new_random + 1.0;
                }
                oldrand[j1] = new_random;
            }
        }

        public double rand() {            
            if (++jrand >= 55) {
                jrand = 1;
                advance_random();
            }
            return oldrand[jrand];
        }
    }  
    /* Fetch a single random number between 0.0 and 1.0 - Subtractive Method */
    /* See Knuth, D. (1969), v. 2 for details */
    /* name changed from random() to avoid library conflicts on some machines*/
    @Override
    protected double randomValue() {
       return super.generator.nextDouble();// this.generator.rand();
    }

    private void ranking(Population population) {
        /*Initializing the ranks to zero*/
        int rnk = 0;
        int nondom = 0;
        /*Initializing all the flags to 2*/       
        int flag[] = new int[population.size];        
        Arrays.fill(flag, 2);
                
        for (int k = 0,q=0; k < population.size; k++, q = 0) {
            int j;
            for (j = 0; j < population.size; j++) {                
                if (flag[j] != 1)
                    break;
            }/*Break if all the individuals are assigned a rank*/
            if (j == population.size)
                break;
            rnk++;
            /*Set the flag of dominated individuals to 2*/
            for (j = 0; j < population.size; j++) {
                if (flag[j] == 0) {
                    flag[j] = 2;
                }                
            }

            for (int i = 0; i < population.size; i++) {
                /*Select an individual which rank to be assigned*/
                if (flag[i] != 1 && flag[i] != 0) {
                    /*Select the other individual which has not got a rank*/
                    for (j = 0; j < population.size; j++) {
                        if (i == j) {
                            continue;
                        }
                        if (flag[j] != 1) {
                            /*Compare the two individuals for fitness*/
                            int val = moComparer.compare(population.ind[i].sample, population.ind[j].sample);
                            
                            /*VAL =  1 for dominated individual which rank to be given*/
                            /*VAL = -1 for dominating individual which rank to be given*/
                            /*VAL =  0 for non comparable individuals*/
                            if (val == 1) {
                                flag[i] = 0;/* individual 1 is dominated */
                                break;
                            }
                            if (val == -1) {
                                flag[j] = 0;/* individual 2 is dominated */
                            }
                            if (val == 0) {
                                nondom++;/* individual 1 & 2 are non dominated */
                                if (flag[j] != 0) {
                                    flag[j] = 3;
                                }
                            }
                        }
                    } 
                    if (j == population.size) {
                        /*Assign the rank and set the flag*/
                        population.ind[i].rank = rnk;
                        flag[i] = 1;                        
                        q++;
                    }
                }       /*Loop over flag check ends*/
            }
            population.rankno[rnk - 1] = q;
        }
        population.maxrank = rnk;
        /* Find Max Rank of the population    */
        for (int i = 0; i < population.size; i++) {
            if (population.ind[i].rank > population.maxrank) {
                population.maxrank = population.ind[i].rank;
            }
        }
    }

    @Override
    public void init() {
        super.init();
        this.crossoverCount = 0;
        this.mutationCount  = 0;

        if (this.mutationProbability.getValue()> 1.0 / this.parameters.length)
            this.mutationProbability.setValue(1.0 / this.parameters.length);
    }

    void nselect(Population old_pop, Population pop2) {        
        for (int n = 0, k = 0; n < old_pop.size; n++, k++) {
            int rnd1 = (int) Math.floor(randomValue() * (double) old_pop.size);
            int rnd2 = (int) Math.floor(randomValue() * (double) old_pop.size);

            if (rnd1 == 0)              rnd1 = old_pop.size - k;
            if (rnd2 == 0)              rnd2 = old_pop.size - n;
            
            if (rnd1 == old_pop.size)   rnd1 = (old_pop.size - 2) / 2;
            if (rnd2 == old_pop.size)   rnd2 = (old_pop.size - 4) / 2;
            
            /*Select parents randomly*/
            int j  = rnd1 - 1;
            int j1 = rnd2 - 1;
            /*------------------SELECTION PROCEDURE------------------------------------*/
            /*Comparing the fitnesses*/
            if (old_pop.ind[j].rank > old_pop.ind[j1].rank) {
                pop2.ind[k] = old_pop.ind[j1];
            } else {
                if (old_pop.ind[j].rank < old_pop.ind[j1].rank) {
                    pop2.ind[k] = old_pop.ind[j];
                } else {
                    if (old_pop.ind[j].cub_len < old_pop.ind[j1].cub_len) {
                        pop2.ind[k] = old_pop.ind[j1];
                    } else {
                        pop2.ind[k] = old_pop.ind[j];
                    }
                }
            }
        }
    }

    double[][] realcross(Population mate_pop) {        
        int nvar = mate_pop.ind[0].sample.getParameter().length;
        double newParameter[][] = new double[mate_pop.size][nvar];

        double y1, y2;
        double beta;
        int k = 0, y = 0;
        double chld1, chld2;

        for (int i = 0; i < mate_pop.size / 2; i++) {
            double rnd = this.randomValue();
            /*Check Whether the cross-over to be performed*/
            if (rnd <= this.crossoverProbability.getValue()) {
                /*Loop over no of variables*/
                for (int j = 0; j < nvar; j++) {
                    /*Selected Two Parents*/
                    double par1 = mate_pop.ind[y].sample.getParameter()[j];
                    double par2 = mate_pop.ind[y + 1].sample.getParameter()[j];
                    double yl = this.lowBound[j];
                    double yu = this.upBound[j];

                    y1 = par1;
                    y2 = par2;
                    rnd = randomValue();
                    /* Check whether variable is selected or not*/
                    if (rnd <= 0.5) {
                        /*Variable selected*/
                        this.crossoverCount++;
                        double betaq = 1.0;
                        if (Math.abs(par1 - par2) > 1E-8) { // changed by Deb (31/10/01)
                            if (par2 <= par1) {
                                y1 = par2;
                                y2 = par1;                                
                            }
                            /*Find beta value*/
                            if ((y1 - yl) > (yu - y2)) {
                                beta = 1.0 / (1 + (2 * (yu - y2) / (y2 - y1)));
                            } else {
                                beta = 1.0 / (1 + (2 * (y1 - yl) / (y2 - y1)));
                            }
                            /*Find alpha*/
                            double expp = this.crossoverDistributionIndex.getValue() + 1.0;
                            double alpha = 2.0 - Math.pow(beta, expp);

                            if (alpha < 0.0) {
                                sayThis("ERRRROR: " + alpha + " " + y + " " + k + " " + par1 + " " + par2);
                                System.exit(-1);
                            }
                            alpha = alpha * randomValue();
                            expp = 1.0 / (this.crossoverDistributionIndex.getValue() + 1.0);
                            if (alpha > 1.0) {
                                alpha = 1.0 / (2.0 - alpha);
                            }
                            betaq = Math.pow(alpha, expp);
                        }
                        /*Generation two children*/
                        chld1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
                        chld2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));
                        // added by deb (31/10/01)
                        if (chld1 < yl)     chld1 = yl;
                        if (chld1 > yu)     chld1 = yu;
                        if (chld2 < yl)     chld2 = yl;
                        if (chld2 > yu)     chld2 = yu;

                    } else {
                        /*Copying the children to parents*/
                        chld1 = par1;
                        chld2 = par2;
                    }
                    newParameter[k][j] = chld1;
                    newParameter[k + 1][j] = chld2;
                }
            } else {
                for (int j = 0; j < nvar; j++) {
                    newParameter[k][j] = mate_pop.ind[y].sample.getParameter()[j];
                    newParameter[k + 1][j] = mate_pop.ind[y + 1].sample.getParameter()[j];
                }
            }
            k = k + 2;
            y = y + 2;
        }
        return newParameter;
    }

    void real_mutate(double new_pop[][]) {
        int popSize = new_pop.length;
        int nvar = new_pop[0].length;
        double indi = 1.0 / (this.mutationDistributionIndex.getValue() + 1.0);

        for (int j = 0; j < popSize; j++) {
            for (int i = 0; i < nvar; i++) {
                double rnd = randomValue();
                /*For each variable find whether to do mutation or not*/
                if (rnd <= this.mutationProbability.getValue()) {
                    double y = new_pop[j][i];
                    double yl = this.lowBound[i];
                    double yu = this.upBound[i];
                    double delta = 0;
                    double val, deltaq;

                    if (y > yl) {
                        /*Calculate delta*/
                        if ((y - yl) < (yu - y)) {
                            delta = 1.0 - (y - yl) / (yu - yl);
                        } else {
                            delta = 1.0 - (yu - y) / (yu - yl);
                        }

                        rnd = randomValue();

                        if (rnd <= 0.5) {                            
                            val = 2.0 * rnd + (1 - 2 * rnd) * (Math.pow(delta, (mutationDistributionIndex.getValue() + 1)));
                            deltaq = Math.pow(val, indi) - 1.0;
                        } else {                            
                            val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(delta, (mutationDistributionIndex.getValue() + 1)));
                            deltaq = 1.0 - (Math.pow(val, indi));
                        }
                        /*Change the value for the parent */
                        y = y + deltaq * (yu - yl);
                        if (y < yl)     y = yl;
                        if (y > yu)     y = yu;
                        
                        new_pop[j][i] = y;
                    } else { // y == yl
                        new_pop[j][i] = randomValue() * (yu - yl) + yl;
                    }
                    this.mutationCount++;
                }
            }
        }
    }

    void grank(int gen, Population globalPopulation) {
        sayThis("Genration no. = " + gen);
        /*----------------------------* RANKING *---------------------------------*/
        int gflg[] = new int[globalPopulation.size];
        int rnk = 0;
        int nondom = 0;

        Arrays.fill(gflg, 2);
        
        for (int k = 0; k < globalPopulation.size; k++) {
            int j = 0, q = 0;

            for (j = 0; j < globalPopulation.size; j++) {
                if (gflg[j] != 1) {
                    break;
                }
            }
            if (j == globalPopulation.size) {
                break;
            }
            rnk = rnk + 1;
            for (j = 0; j < globalPopulation.size; j++) {
                if (gflg[j] == 0) {
                    gflg[j] = 2;
                }
            }
            for (int i = 0; i < globalPopulation.size; i++) {
                if (!(gflg[i] != 1 && gflg[i] != 0)) {
                    continue;
                }
                for (j = 0; j < globalPopulation.size; j++) {
                    if (i == j || gflg[j] == 1) {
                        continue;
                    }                    
                    int val = moComparer.compare(globalPopulation.ind[i].sample, globalPopulation.ind[j].sample);
                    if (val == 1) {
                        gflg[i] = 0;/* individual 1 is dominated */
                        break;
                    } else if (val == -1) {
                        gflg[j] = 0;/* individual 2 is dominated */
                    } else if (val == 0) {
                        nondom++;/* individual 1 & 2 are non dominated */
                        if (gflg[j] != 0) {
                            gflg[j] = 3;
                        }
                    }
                }
                if (j == globalPopulation.size) {
                    globalPopulation.ind[i].rank = rnk;
                    gflg[i] = 1;
                    globalPopulation.rankar[rnk - 1][q] = i;
                    q++;
                }
            }
            globalPopulation.rankno[rnk - 1] = q;
        }
        globalPopulation.maxrank = rnk;

        /*sayThis("   RANK     No Of Individuals");
        String text = "";
        for (int i = 0; i < rnk; i++) 
            text += ("\t" + (i + 1) + "\t" + globalPopulation.rankno[i]) + "\n";
        sayThis(text);*/
    }
    //simple bubblesort
    void sort(int m1, int index[], double value[]) {
        double temp;int temp1;
        for (int k1 = 0; k1 < m1 ; k1++) {
            for (int i1 = k1 + 1; i1 < m1; i1++) {
                if (value[k1] > value[i1]) {
                    temp = value[k1];
                    temp1 = index[k1];
                    value[k1] = value[i1];
                    index[k1] = index[i1];
                    value[i1] = temp;
                    index[i1] = temp1;
                }
            }
        }
    }

    void gshare(int rnk, Population globalPopulation) {
        int m1 = globalPopulation.rankno[rnk - 1];
        int nfunc = globalPopulation.ind[0].sample.fx.length;
        int index[] = new int[globalPopulation.size];
        double value[] = new double[globalPopulation.size];
        
        for (int j = 0; j < nfunc; j++) {
            for (int i = 0; i < m1; i++) {
                index[i] = globalPopulation.rankar[rnk - 1][i];
                value[i] = globalPopulation.ind[index[i]].sample.fx[j];
            }
            sort(m1, index, value); /*Sort the arrays in ascending order of the fitness*/

            double max = value[m1 - 1];
            double min = value[0];
            
            for (int i = 0; i < m1; i++) {                
                if (i == 0 || i == (m1 - 1)) {
                    globalPopulation.ind[index[i]].cub_len += 100 * max;
                } else {                    
                    globalPopulation.ind[index[i]].cub_len += Math.abs(value[i+1] - value[i-1]) / (max - min); // crowding distances are normalized
                }
            }            
        }
    }
    /* This is the method used to sort the dummyfitness arrays */
    void gsort(int rnk, int sel, Population globalPopulation, boolean flag[]) {
        int index[] = new int[globalPopulation.size];
        double value[] = new double[globalPopulation.size];

        int q = globalPopulation.rankno[rnk - 1];

        for (int i = 0; i < q; i++) {
            index[i] = globalPopulation.rankar[rnk - 1][i];
            value[i] = -globalPopulation.ind[index[i]].cub_len;
        }
        sort(q,index,value);

        for (int i = 0; i < sel; i++) {
            flag[index[i]] = true;
        }
    }

    void keepalive(Population pop1, Population pop2, Population pop3, int gen) {
        Population globalPopulation = new Population(pop1.size * 2);
        boolean flag[] = new boolean[pop1.size*2];
        int lastRank = 0;
        /*Forming the global mating pool*/
        /*Initial;ising the dummyfitness to zero */
        for (int i = 0; i < pop1.size; i++) {
            globalPopulation.ind[i] = pop1.ind[i];
            globalPopulation.ind[i].cub_len = 0;
            globalPopulation.ind[i + pop1.size] = pop2.ind[i];
            globalPopulation.ind[i + pop1.size].cub_len = 0;
            flag[i] = false;
            flag[i + pop1.size] = false;
        }
        /*Finding the global ranks */
        grank(gen, globalPopulation);
        /* Sharing the fitness to get the dummy fitness */
        for (int i = 0; i < globalPopulation.maxrank; i++) {
            gshare(i + 1, globalPopulation);
        }                
        // decide which all solutions belong to the pop3
        int st = 0,pool=0;
        for (int i = 0; i < globalPopulation.maxrank; i++) {
            /*    Elitism Applied Here     */
            st = pool;
            pool += globalPopulation.rankno[i];
            if (pool <= pop1.size) {
                for (int k = 0; k < 2 * pop1.size; k++)
                    if (globalPopulation.ind[k].rank == i + 1) 
                        flag[k] = true;
                pop3.rankno[i] = globalPopulation.rankno[i];
            } else {
                int sel = pop1.size - st;
                lastRank = i + 1;
               /* if (i >= pop3.size)
                    System.out.println("NSGA2 error, pop3 array out of bounds .. ");
                else*/
                    pop3.rankno[i] = sel;
                gsort(i + 1, sel, globalPopulation, flag);
                break;
            }
        }
        for (int i = 0, k = 0; i < 2 * pop1.size && k < pop1.size; i++) {
            if (flag[i]) {
                pop3.ind[k] = globalPopulation.ind[i];
                k++;
            }
        }
        pop3.maxrank = lastRank;
    }

    void report(int t, Population pop1, Population pop2) {
        sayThis("\n-----------------------------------------------------------------------------------");
        sayThis("Generation No.     ->" + (t + 1));
        sayThis("-----------------------------------------------------------------------------------");
        int nvar = pop1.ind[0].sample.getParameter().length;
        int nfunc = pop1.ind[0].sample.fx.length;
        int popSize = pop1.ind.length;
        DecimalFormat f = new DecimalFormat("#0.00000");
        String text="";

        for (int j = 0; j < nvar; j++)
            text += ("x" + j) + "\t\t";
        for (int j = 0; j < nfunc; j++)
            text += ("y" + j) + "\t\t";
        sayThis(text + "\tcublen\trank");

        text = "";
        for (int i = 0; i < popSize; i++) {            
            for (int j = 0; j < nvar; j++)
                text += (f.format(pop1.ind[i].sample.getParameter()[j]) + "\t");
            for (int j = 0; j < nfunc; j++)
                text += (f.format(pop1.ind[i].sample.F()[j]) + "\t");
            text += (pop1.ind[i].cub_len + "\t" + pop1.ind[i].rank);
            sayThis(text);
            text="";
        }
        sayThis(text);
        text = "";
        sayThis("-----------------------------------------------------------------------------------");
        for (int i = 0; i < popSize; i++) {            
            for (int j = 0; j < nvar; j++)
                text += f.format(pop2.ind[i].sample.getParameter()[j]) + "\t";
            for (int j = 0; j < nfunc; j++) 
                text += f.format(pop2.ind[i].sample.F()[j]) + "\t";

            text += (pop2.ind[i].cub_len + "\t" + pop2.ind[i].rank);
            sayThis(text);
            text="";
        }
        sayThis("-----------------------------------------------------------------------------------");
        sayThis("-----------------------------------------------------------------------------------");
        sayThis("-----------------------------------------------------------------------------------");
    }

    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException {
        if (this.populationSize == null || this.populationSize.getValue() < 1){
            sayThis(JAMS.i18n("size_of_population_not_specified_or_out_of_bounds"));
            return;
        }
        if (this.crossoverDistributionIndex == null ||
            this.crossoverDistributionIndex.getValue() < 0.5 ||
            this.crossoverDistributionIndex.getValue() > 100.0){
            sayThis(JAMS.i18n("crossoverDistributionIndex_not_specified_or_out_of_bounds"));
            return;
        }
        if (this.mutationDistributionIndex == null ||
            this.mutationDistributionIndex.getValue() < 0.5 ||
            this.mutationDistributionIndex.getValue() > 500.0){
            sayThis(JAMS.i18n("mutationDistributionIndex_not_specified_or_out_of_bounds"));
            return;
        }
        if (this.crossoverProbability == null ||
            this.crossoverProbability.getValue() < 0.0 ||
            this.crossoverProbability.getValue() > 1.0){
            sayThis(JAMS.i18n("crossoverProbability_not_specified_or_out_of_bounds"));
            return;
        }
        if (this.mutationProbability == null ||
            this.mutationProbability.getValue() < 0.0 ||
            this.mutationProbability.getValue() > 1.0){
            sayThis(JAMS.i18n("mutationProbability_not_specified_or_out_of_bounds"));
            return;
        }
        mutationProbability.setValue(1.0 / (double)this.n);
        if (this.maxGeneration == null ||
            this.maxGeneration.getValue() < 0.0){
            sayThis(JAMS.i18n("maxGeneration_not_specified_or_out_of_bounds"));
            return;
        }

        this.generator = new CustomRand(0.5);
        Population oldPopulation = new Population(populationSize.getValue());
        Population matePopulation = new Population(populationSize.getValue());
        Population newPopulation = new Population(populationSize.getValue());

        for (int i = 0; i < this.populationSize.getValue(); i++) {
            oldPopulation.ind[i] = new Individual(this.getSample(RandomSampler()));
        }
        ranking(oldPopulation);
        /********************************************************************/
        /*----------------------GENERATION STARTS HERE----------------------*/        
        for (int i = 0; i < this.maxGeneration.getValue(); i++) {
            matePopulation = new Population(populationSize.getValue());
            newPopulation = new Population(populationSize.getValue());
            /*--------SELECT----------------*/
            nselect(oldPopulation, matePopulation);
            /*CROSSOVER----------------------------*/
            double newParameter[][] = realcross(matePopulation);
            /*------MUTATION-------------------*/
            real_mutate(newParameter);
            /*----------FUNCTION EVALUATION-----------*/
            for (int j = 0; j < newParameter.length; j++) {
                newPopulation.ind[j] = new Individual(this.getSample(newParameter[j]));                
            }
            ranking(newPopulation);
            /*-------------------SELECTION KEEPING FRONTS ALIVE--------------*/
            /*Elitism And Sharing Implemented*/
            keepalive(oldPopulation, newPopulation, matePopulation, i + 1);
            /*------------------REPORT PRINTING--------------------------------*/
            //skip report because its toooo much information
            //report(i, oldPopulation, matePopulation);
            /*==================================================================*/
            newPopulation = matePopulation;
            oldPopulation = newPopulation;                     
        }
        /*                   Generation Loop Ends                                */
        /************************************************************************/
        sayThis(JAMS.i18n("NO_OF_MUTATION") + this.crossoverCount);
        sayThis(JAMS.i18n("NO_OF_CROSSOVER") + this.mutationCount);
        sayThis("-----------------------------------------------------------------------------------");
    }

    public static void main(String arg[]){
        NSGA2 nsga = new NSGA2();

        class TestFunction extends AbstractMOFunction{
            public double[] f(double x[]){
                double y[] = new double[1];
                int n = x.length;

                double r = Math.pow((x[0]-1),2.0);
                for (int i=1;i<n;i++){
                    r += (i+1)*Math.pow((2*x[i]*x[i]-x[i-1]), 2);
                }

                /*double a = 20, b = 0.2, c = 2*Math.PI;
                double shift = 1.0/3.0;
                double s1 = 0, s2 = 0;
                for (int i=0;i<n;i++){
                    s1 += (x[i]+shift)*(x[i]+shift);
                    s2 += Math.cos(2.0*Math.PI*(x[i]+shift));
                }
                s1 /= n;
                s2 /= n;

                double r = -a*Math.exp(-b*Math.sqrt(s1))-Math.exp(s2)+a+Math.exp(1);*/

                y[0] = r;
                return y;
            }
        }

        nsga.GoalFunction = new TestFunction();
        nsga.boundaries = (JAMSString)JAMSDataFactory.getDataFactory().createString();
        nsga.boundaries.setValue("[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];[-10.0>10.0];");
        nsga.parameterIDs = new JAMSDouble[16];

        nsga.crossoverDistributionIndex = JAMSDataFactory.getDataFactory().createDouble();
        nsga.crossoverDistributionIndex.setValue(20);
        nsga.crossoverProbability = JAMSDataFactory.getDataFactory().createDouble();
        nsga.crossoverProbability.setValue(0.9);
        nsga.enable = (JAMSBoolean)JAMSDataFactory.getDataFactory().createBoolean();
        nsga.enable.setValue(true);
        nsga.maxGeneration = JAMSDataFactory.getDataFactory().createInteger();
        nsga.maxGeneration.setValue(100000);
        nsga.mode = (JAMSString)JAMSDataFactory.getDataFactory().createString();
        nsga.mode.setValue("1");
        nsga.target = JAMSDataFactory.getDataFactory().createDoubleArray();
        nsga.target.setValue(new double[]{0.00});
        nsga.epsilonToTarget =JAMSDataFactory.getDataFactory().createDouble();
        nsga.epsilonToTarget.setValue(0.018);
        nsga.effMethodName = (JAMSString)JAMSDataFactory.getDataFactory().createString();
        nsga.effMethodName.setValue("f1");
        nsga.mutationDistributionIndex = JAMSDataFactory.getDataFactory().createDouble();
        nsga.mutationDistributionIndex.setValue(20);
        nsga.mutationProbability = JAMSDataFactory.getDataFactory().createDouble();
        nsga.mutationProbability.setValue(1.0);
        nsga.populationSize = JAMSDataFactory.getDataFactory().createInteger();
        nsga.populationSize.setValue(30);
        nsga.iterationCounter = JAMSDataFactory.getDataFactory().createInteger();
        nsga.maxn = JAMSDataFactory.getDataFactory().createInteger();
        nsga.maxn.setValue(10000000);
        nsga.bestParameterSets = JAMSDataFactory.getDataFactory().createEntityCollection();
        nsga.init();
        nsga.run();
        for (Sample s : nsga.factory.getSampleList())
            System.out.println(s);
        System.out.println(nsga.iterationCounter);
    }
}
