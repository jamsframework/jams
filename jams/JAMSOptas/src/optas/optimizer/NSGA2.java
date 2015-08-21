/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer;

import optas.core.SampleLimitException;
import jams.JAMS;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import optas.data.DataCollection;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.SampleFactory.SampleComperator;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.StringOptimizerParameter;
import optas.optimizer.parallel.ParallelSequence;

/**
 *
 * @author Christian Fischer
 */
public class NSGA2 extends Optimizer {

    public double populationSize = 30;
    public double crossoverProbability = 0.9;
    public double mutationProbability = 1.0;
    public double crossoverDistributionIndex = 20;
    public double mutationDistributionIndex = 20;
    public int maxGeneration = 1000;
    public boolean parallelExecution = false;
    public double threadCount = 12.0;
    public String excludeFiles = "(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log";
    int crossoverCount = 0;
    int mutationCount = 0;
    ParallelSequence pseq = null;
    SampleComperator moComparer = new SampleComperator(false);
    CustomRand generator = null;
    transient DataCollection collection = null;

    @Override
    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(NSGA2.class.getSimpleName(), NSGA2.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("populationSize", JAMS.i18n("size_of_population"), 30, 1, 100000));
        desc.addParameter(new NumericOptimizerParameter("crossoverProbability", JAMS.i18n("propability_for_crossover"), 0.9, 0, 1));
        desc.addParameter(new NumericOptimizerParameter("mutationProbability", JAMS.i18n("propability_for_mutations"), 1.0, 0.5, 1));
        desc.addParameter(new NumericOptimizerParameter("crossoverDistributionIndex", JAMS.i18n("distribution_index_for_crossover"), 20, 1, 100));
        desc.addParameter(new NumericOptimizerParameter("mutationDistributionIndex", JAMS.i18n("distribution_index_for_mutation"), 20, 1, 500));
        desc.addParameter(new NumericOptimizerParameter("maxGeneration", JAMS.i18n("maximum_number_of_generations"), 1000, 1, 10000));

        
        desc.addParameter(new StringOptimizerParameter("excludeFiles",
                JAMS.i18n("exclude_files_list"),"(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log"));

        desc.addParameter(new NumericOptimizerParameter("threadCount",
                JAMS.i18n("threadCount"), 8, 2, 100.0));
        
        desc.addParameter(new BooleanOptimizerParameter("parallelExecution",
                JAMS.i18n("parallelExecution"), false));
                
        return desc;
    }

    /**
     * @return the populationSize
     */
    public double getPopulationSize() {
        return populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(double populationSize) {
        this.populationSize = populationSize;
    }

    /**
     * @return the crossoverProbability
     */
    public double getCrossoverProbability() {
        return crossoverProbability;
    }

    /**
     * @param crossoverProbability the crossoverProbability to set
     */
    public void setCrossoverProbability(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    /**
     * @return the mutationProbability
     */
    public double getMutationProbability() {
        return mutationProbability;
    }

    /**
     * @param mutationProbability the mutationProbability to set
     */
    public void setMutationProbability(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    /**
     * @return the crossoverDistributionIndex
     */
    public double getCrossoverDistributionIndex() {
        return crossoverDistributionIndex;
    }

    /**
     * @param crossoverDistributionIndex the crossoverDistributionIndex to set
     */
    public void setCrossoverDistributionIndex(double crossoverDistributionIndex) {
        this.crossoverDistributionIndex = crossoverDistributionIndex;
    }

    /**
     * @return the mutationDistributionIndex
     */
    public double getMutationDistributionIndex() {
        return mutationDistributionIndex;
    }

    /**
     * @param mutationDistributionIndex the mutationDistributionIndex to set
     */
    public void setMutationDistributionIndex(double mutationDistributionIndex) {
        this.mutationDistributionIndex = mutationDistributionIndex;
    }

    /**
     * @return the maxGeneration
     */
    public double getMaxGeneration() {
        return maxGeneration;
    }

    /**
     * @param maxGeneration the maxGeneration to set
     */
    public void setMaxGeneration(double maxGeneration) {
        this.maxGeneration = (int) maxGeneration;
    }

    public void setThreadCount(double threadCount) {
        this.threadCount = (int) threadCount;
    }

    public double getThreadCount() {
        return this.threadCount;
    }
    
    /**
     * @return the parallelExecution
     */
    public boolean isParallelExecution() {
        return parallelExecution;
    }

    /**
     * @param parallelExecution the parallelExecution to set
     */
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }

    /**
     * @return the excludeFiles
     */
    public String getExcludeFiles() {
        return excludeFiles;
    }

    /**
     * @param excludeFiles the excludeFiles to set
     */
    public void setExcludeFiles(String excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    static private class Individual implements Serializable {

        Sample sample;
        int rank;/*Rank of the individual*/

        double cub_len;/*crowding distance of the individual*/


        public Individual(Sample sample) {
            this.sample = sample;
            this.rank = 0;
            this.cub_len = 0.0;
        }
    }

    private class Population implements Serializable {

        int maxrank;        /*Maximum rank present in the population*/

        int rankar[][];     /*record of array of individual numbers at a particular rank */

        int rankno[];       /*Individual at different ranks*/

        Individual ind[];   /*Different Individuals*/

        int size;

        public Population(int size) {
            this.size = size;
            rankno = new int[size + 1];
            ind = new Individual[size];
            rankar = new int[size][size];
        }
    }

    public static class CustomRand implements Serializable {

        double oldrand[] = new double[55];
        int jrand = 0;

        CustomRand(double random_seed) {
            double new_random = 0.000000001,
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

        for (int k = 0, q = 0; k < population.size; k++, q = 0) {
            int j;
            for (j = 0; j < population.size; j++) {
                if (flag[j] != 1) {
                    break;
                }
            }/*Break if all the individuals are assigned a rank*/
            if (j == population.size) {
                break;
            }
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
    public boolean init() {
        if (!super.init()) {
            return false;
        }
        this.crossoverCount = 0;
        this.mutationCount = 0;

        if (this.mutationProbability > 1.0 / n) {
            this.mutationProbability = (1.0 / n);
        }

        if (this.parallelExecution) {
            pseq = new ParallelSequence(this);
            pseq.setExcludeFiles(excludeFiles);
            pseq.setThreadCount((int) this.threadCount);
        }
        return true;
    }

    void nselect(Population old_pop, Population pop2) {
        for (int n = 0, k = 0; n < old_pop.size; n++, k++) {
            int rnd1 = (int) Math.floor(randomValue() * (double) old_pop.size);
            int rnd2 = (int) Math.floor(randomValue() * (double) old_pop.size);

            if (rnd1 == 0) {
                rnd1 = old_pop.size - k;
            }
            if (rnd2 == 0) {
                rnd2 = old_pop.size - n;
            }

            if (rnd1 == old_pop.size) {
                rnd1 = (old_pop.size - 2) / 2;
            }
            if (rnd2 == old_pop.size) {
                rnd2 = (old_pop.size - 4) / 2;
            }

            /*Select parents randomly*/
            int j = rnd1 - 1;
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
            if (rnd <= getCrossoverProbability()) {
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
                            double expp = getCrossoverDistributionIndex() + 1.0;
                            double alpha = 2.0 - Math.pow(beta, expp);

                            if (alpha < 0.0) {
                                log("ERRRROR: " + alpha + " " + y + " " + k + " " + par1 + " " + par2);
                                System.exit(-1);
                            }
                            alpha = alpha * randomValue();
                            expp = 1.0 / (getCrossoverDistributionIndex() + 1.0);
                            if (alpha > 1.0) {
                                alpha = 1.0 / (2.0 - alpha);
                            }
                            betaq = Math.pow(alpha, expp);
                        }
                        /*Generation two children*/
                        chld1 = 0.5 * ((y1 + y2) - betaq * (y2 - y1));
                        chld2 = 0.5 * ((y1 + y2) + betaq * (y2 - y1));
                        // added by deb (31/10/01)
                        if (chld1 < yl) {
                            chld1 = yl;
                        }
                        if (chld1 > yu) {
                            chld1 = yu;
                        }
                        if (chld2 < yl) {
                            chld2 = yl;
                        }
                        if (chld2 > yu) {
                            chld2 = yu;
                        }

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
        double indi = 1.0 / (this.getMutationDistributionIndex() + 1.0);

        for (int j = 0; j < popSize; j++) {
            for (int i = 0; i < nvar; i++) {
                double rnd = randomValue();
                /*For each variable find whether to do mutation or not*/
                if (rnd <= this.getMutationProbability()) {
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
                            val = 2.0 * rnd + (1 - 2 * rnd) * (Math.pow(delta, (getMutationDistributionIndex() + 1)));
                            deltaq = Math.pow(val, indi) - 1.0;
                        } else {
                            val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(delta, (getMutationDistributionIndex() + 1)));
                            deltaq = 1.0 - (Math.pow(val, indi));
                        }
                        /*Change the value for the parent */
                        y = y + deltaq * (yu - yl);
                        if (y < yl) {
                            y = yl;
                        }
                        if (y > yu) {
                            y = yu;
                        }

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
        log("Genration no. = " + gen);
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

        /*log("   RANK     No Of Individuals");
         String text = "";
         for (int i = 0; i < rnk; i++) 
         text += ("\t" + (i + 1) + "\t" + globalPopulation.rankno[i]) + "\n";
         log(text);*/
    }
    //simple bubblesort

    void sort(int m1, int index[], double value[]) {
        double temp;
        int temp1;
        for (int k1 = 0; k1 < m1; k1++) {
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
        int nfunc = globalPopulation.ind[0].sample.F().length;
        int index[] = new int[globalPopulation.size];
        double value[] = new double[globalPopulation.size];

        for (int j = 0; j < nfunc; j++) {
            for (int i = 0; i < m1; i++) {
                index[i] = globalPopulation.rankar[rnk - 1][i];
                value[i] = globalPopulation.ind[index[i]].sample.F()[j];
            }
            sort(m1, index, value); /*Sort the arrays in ascending order of the fitness*/

            double max = value[m1 - 1];
            double min = value[0];

            for (int i = 0; i < m1; i++) {
                if (i == 0 || i == (m1 - 1)) {
                    globalPopulation.ind[index[i]].cub_len += 100 * max;
                } else {
                    globalPopulation.ind[index[i]].cub_len += Math.abs(value[i + 1] - value[i - 1]) / (max - min); // crowding distances are normalized
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
        sort(q, index, value);

        for (int i = 0; i < sel; i++) {
            flag[index[i]] = true;
        }
    }

    void keepalive(Population pop1, Population pop2, Population pop3, int gen) {
        Population globalPopulation = new Population(pop1.size * 2);
        boolean flag[] = new boolean[pop1.size * 2];
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
        int st = 0, pool = 0;
        for (int i = 0; i < globalPopulation.maxrank; i++) {
            /*    Elitism Applied Here     */
            st = pool;
            pool += globalPopulation.rankno[i];
            if (pool <= pop1.size) {
                for (int k = 0; k < 2 * pop1.size; k++) {
                    if (globalPopulation.ind[k].rank == i + 1) {
                        flag[k] = true;
                    }
                }
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
        log("\n-----------------------------------------------------------------------------------");
        log("Generation No.     ->" + (t + 1));
        log("-----------------------------------------------------------------------------------");
        int popSize = pop1.ind.length;
        DecimalFormat f = new DecimalFormat("#0.00000");
        String text = "";

        for (int j = 0; j < n; j++) {
            text += ("x" + j) + "\t\t";
        }
        for (int j = 0; j < m; j++) {
            text += ("y" + j) + "\t\t";
        }
        log(text + "\tcublen\trank");

        text = "";
        for (int i = 0; i < popSize; i++) {
            for (int j = 0; j < n; j++) {
                text += (f.format(pop1.ind[i].sample.getParameter()[j]) + "\t");
            }
            for (int j = 0; j < m; j++) {
                text += (f.format(pop1.ind[i].sample.F()[j]) + "\t");
            }
            text += (pop1.ind[i].cub_len + "\t" + pop1.ind[i].rank);
            log(text);
            text = "";
        }
        log(text);
        text = "";
        log("-----------------------------------------------------------------------------------");
        for (int i = 0; i < popSize; i++) {
            for (int j = 0; j < n; j++) {
                text += f.format(pop2.ind[i].sample.getParameter()[j]) + "\t";
            }
            for (int j = 0; j < m; j++) {
                text += f.format(pop2.ind[i].sample.F()[j]) + "\t";
            }

            text += (pop2.ind[i].cub_len + "\t" + pop2.ind[i].rank);
            log(text);
            text = "";
        }
        log("-----------------------------------------------------------------------------------");
        log("-----------------------------------------------------------------------------------");
        log("-----------------------------------------------------------------------------------");
    }

    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException {
        try {
            if (this.getPopulationSize() < 1) {
                log(JAMS.i18n("size_of_population_not_specified_or_out_of_bounds"));
                return;
            }
            if (this.getCrossoverDistributionIndex() < 0.5
                    || this.getCrossoverDistributionIndex() > 100.0) {
                log(JAMS.i18n("crossoverDistributionIndex_not_specified_or_out_of_bounds"));
                return;
            }
            if (this.getMutationDistributionIndex() < 0.5
                    || this.getMutationDistributionIndex() > 500.0) {
                log(JAMS.i18n("mutationDistributionIndex_not_specified_or_out_of_bounds"));
                return;
            }
            if (this.getCrossoverProbability() < 0.0
                    || this.getCrossoverProbability() > 1.0) {
                log(JAMS.i18n("crossoverProbability_not_specified_or_out_of_bounds"));
                return;
            }
            if (this.getMutationProbability() < 0.0
                    || this.getMutationProbability() > 1.0) {
                log(JAMS.i18n("mutationProbability_not_specified_or_out_of_bounds"));
                return;
            }
            setMutationProbability((1.0 / (double) this.n));
            if (this.getMaxGeneration() < 0.0) {
                log(JAMS.i18n("maxGeneration_not_specified_or_out_of_bounds"));
                return;
            }

            this.generator = new CustomRand(0.5);
            Population oldPopulation = new Population((int) getPopulationSize());
            Population matePopulation = new Population((int) getPopulationSize());
            Population newPopulation = new Population((int) getPopulationSize());

            double initParameter[][] = new double[(int) this.getPopulationSize()][];

            for (int i = 0; i < this.getPopulationSize(); i++) {
                if (this.x0 != null && this.x0.length > i) {
                    initParameter[i] = this.x0[i].clone();
                } else {
                    initParameter[i] = randomSampler();
                }
            }

            if (!this.parallelExecution) {
                for (int i = 0; i < this.getPopulationSize(); i++) {
                    oldPopulation.ind[i] = new Individual(this.getSample(initParameter[i]));
                }
            } else {
                ParallelSequence.OutputData result = pseq.procedure(initParameter);
                for (int j = 0; j < initParameter.length; j++) {
                    try {
                        oldPopulation.ind[j] = new Individual(result.list.get(j));
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        System.out.println("For an unknown reason parallel sampling did not succeed completly .. switching to sequential execution");
                        for (int k = j; k < initParameter.length; k++) {
                            oldPopulation.ind[k] = new Individual(this.getSample(initParameter[k]));
                        }
                        j = initParameter.length;
                        break;
                    }
                }
                if (collection == null) {
                    collection = result.dc;
                } else {
                    synchronized (collection) {
                        collection.mergeDataCollections(result.dc);
                        collection.dump(getModel().getWorkspace().getOutputDataDirectory(),true);
                    }
                }                
                this.injectSamples(result.list);
            }

            /*for (int i = 0; i < this.getPopulationSize(); i++) {
                if (this.x0 != null && this.x0.length > i) {
                    oldPopulation.ind[i] = new Individual(this.getSample(this.x0[i]));
                } else {
                    oldPopulation.ind[i] = new Individual(this.getSample(randomSampler()));
                }
            }*/
            ranking(oldPopulation);
            /**
             * *****************************************************************
             */
            /*----------------------GENERATION STARTS HERE----------------------*/
            for (int i = 0; i < this.getMaxGeneration(); i++) {
                matePopulation = new Population((int) getPopulationSize());
                newPopulation = new Population((int) getPopulationSize());
                /*--------SELECT----------------*/
                nselect(oldPopulation, matePopulation);
                /*CROSSOVER----------------------------*/
                double newParameter[][] = realcross(matePopulation);
                /*------MUTATION-------------------*/
                real_mutate(newParameter);
                /*----------FUNCTION EVALUATION-----------*/
                if (!this.parallelExecution) {
                    for (int j = 0; j < newParameter.length; j++) {
                        newPopulation.ind[j] = new Individual(this.getSample(newParameter[j]));
                    }
                } else {
                    ParallelSequence.OutputData result = pseq.procedure(newParameter);
                    for (int j = 0; j < newParameter.length; j++) {
                        try {
                            newPopulation.ind[j] = new Individual(result.list.get(j));
                        } catch (ArrayIndexOutOfBoundsException aioobe) {
                            System.out.println("For an unknown reason parallel sampling did not succeed completly .. switching to sequential execution");
                            for (int k = j; k < newParameter.length; k++) {
                                newPopulation.ind[k] = new Individual(this.getSample(newParameter[k]));
                            }
                            j = newParameter.length;
                            break;
                        }
                    }
                    if (collection == null) {
                        collection = result.dc;
                    } else {
                        synchronized (collection) {
                            collection.mergeDataCollections(result.dc);
                            collection.dump(getModel().getWorkspace().getOutputDataDirectory(),true);
                        }
                    }
                    this.injectSamples(result.list);
                }
                ranking(newPopulation);
                /*-------------------SELECTION KEEPING FRONTS ALIVE--------------*/
                /*Elitism And Sharing Implemented*/
                keepalive(oldPopulation, newPopulation, matePopulation, i + 1);
                /*------------------REPORT PRINTING--------------------------------*/
                //skip report ..
                //report(i, oldPopulation, matePopulation);
            /*==================================================================*/
                newPopulation = matePopulation;
                oldPopulation = newPopulation;
            }
            /*                   Generation Loop Ends                                */
            /**
             * *********************************************************************
             */
            log(JAMS.i18n("NO_OF_MUTATION") + this.crossoverCount);
            log(JAMS.i18n("NO_OF_CROSSOVER") + this.mutationCount);
            log("-----------------------------------------------------------------------------------");
        } finally { //ensure writing the data
            if (collection != null) {
                collection.dump(getModel().getWorkspace().getOutputDataDirectory(),false);
            }
        }
    }
}
