/*
 * ShuffleComplexEvolution.java
 * Created on 30. Juni 2006, 15:12
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package optas.optimizer;

import optas.core.SampleLimitException;
import Jama.Matrix;
import java.util.Arrays;
import jams.JAMS;
import jams.model.JAMSComponentDescription;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import optas.data.DataCollection;
import optas.optimizer.directsearch.ImplicitFiltering;
import optas.optimizer.directsearch.MDS;
import optas.optimizer.directsearch.NelderMead;
import optas.optimizer.directsearch.PatternSearch;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.SampleFactory.SampleSO;
import optas.optimizer.management.SampleFactory.SampleSOComperator;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.StringOptimizerParameter;
import optas.optimizer.parallel.ParallelSequence;

/**
 *
 * @author Christian Fischer, based on the original MatLab sources
 */
@JAMSComponentDescription(title = "SCE Optimization routine",
author = "Christian Fischer",
description = "optimization routine based on Duan et al. 1992")

@SuppressWarnings("unchecked")
public class SCE extends Optimizer {

    static public class InputData implements Serializable {
        public SCE context;

        int nspl;
        int nps;
        int npg;
        int nopt;        
        double []bu;
        double []bl;
        SampleSO[] c;
        SampleSO[] x;
        int[] k1;
        int[] k2;

        public InputData(SCE context, int nspl, int nps, int npg, int nopt, SampleSO[] c, SampleSO[] x, int[] k1, int[] k2, double[] bu, double[] bl) {
            this.context = context;
            this.nspl = nspl;
            this.nps = nps;
            this.npg = npg;
            this.nopt = nopt;
            this.bu = bu;
            this.bl = bl;
            this.x = x;
            this.c = c;
            this.k1 = k1;
            this.k2 = k2;
        }
    }
    
    //"A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don^t specify A and B the unconstrained problem will be solved"
    public String linearConstraintMatrixA;
    public String linearConstraintVectorB;
    public double complexesCount;
    public double pcento;
    public double kstop;
    public double peps;
    public boolean parallelExecution = false;
    public double threadCount = 12.0; 
    public String excludeFiles = "(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log";
    transient DataCollection collection = null;
    int N; //parameter dimension
    int p; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    int iterationCounter = 0;
    PatternSearch SearchMethod = null;
    Matrix LinearConstraints_A = null, LinearConstraints_b = null;
    ParallelSequence pseq = null;

    public String getExcludeFiles(){
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles){
        this.excludeFiles = excludeFiles;
    }

    @Override
    public boolean init() {
        if (!super.init()) {
            return false;
        }

        iterationCounter = 0;
        if (linearConstraintMatrixA != null && linearConstraintVectorB != null) {
            StringTokenizer tok = new StringTokenizer(linearConstraintMatrixA, ";");

            int n = tok.countTokens(), m = -1;
            LinearConstraints_A = null;
            for (int i = 0; i < n; i++) {
                StringTokenizer line_tok = new StringTokenizer(tok.nextToken(), ",");
                if (m == -1) {
                    m = line_tok.countTokens();
                    LinearConstraints_A = new Matrix(n, m);
                }
                if (m != line_tok.countTokens()) {
                    log(JAMS.i18n("Linear_Constraint_Matrix_dimension_mismatch"));
                    return false;
                }
                for (int j = 0; j < m; j++) {
                    String number = line_tok.nextToken();
                    double value = 0.0;
                    try {
                        value = Double.parseDouble(number);
                    } catch (NumberFormatException e) {
                        log(JAMS.i18n("Cant_read_Linear_Constraint_Matrix_because_there_are_unparseable_elements") + ":" + e.toString());
                        return false;
                    }
                    LinearConstraints_A.set(i, j, value);
                }
            }

            tok = new StringTokenizer(linearConstraintVectorB, ";");
            LinearConstraints_b = new Matrix(n, 1);
            n = tok.countTokens();
            for (int i = 0; i < n; i++) {
                String number = tok.nextToken();
                double value = 0.0;
                try {
                    value = Double.parseDouble(number);
                } catch (NumberFormatException e) {
                    log(JAMS.i18n("Cant_read_Linear_Constraint_Matrix_because_there_are_unparseable_elements") + ":" + e.toString());
                }
                LinearConstraints_b.set(i, 0, value);
            }
        }

        if (LinearConstraints_A != null && LinearConstraints_b != null) {
            if (LinearConstraints_A.getRowDimension() != LinearConstraints_b.getRowDimension()) {
                log(JAMS.i18n("LinearConstraintMatrixA_must_have_the_same_number_of_rows_as_LinearConstraintVectorB"));
                return false;
            }
            if (LinearConstraints_A.getColumnDimension() != n) {
                log(JAMS.i18n("LinearConstraintMatrixA_must_have_the_same_number_of_columns_as_there_are_parameters"));
                return false;
            }
        }

        if (x0 == null){
            x0 = new double[][]{randomSampler()};
        }

        if (complexesCount <= 0) {
            log("warning: NumberOfComplexes_value_not_specified, set to default of 2");
            complexesCount = 2;
        }
        if (pcento <= 0) {
            log("warning: pcento_value_not_specified, set to default of 0.1");
            pcento = 0.1;
        }
        if (peps <= 0) {
            log("warning: peps_value_not_specified, set to default of 0.00001");
            peps=(0.00001);
        }
        if (kstop <= 0) {
            log("warning: kstop_value_not_specified, set to default of 10");
            kstop=(10);
        }
        
        if (this.parallelExecution) {
            pseq = new ParallelSequence(this);
            pseq.setExcludeFiles(excludeFiles);
            pseq.setThreadCount((int) this.threadCount);
        }
        return true;
    }
           
    private double NormalizedgeometricRange(SampleSO x[], double bound[]) {
        if (x.length == 0) {
            return 0;
        }
        double mean = 0;

        for (int i = 0; i < n; i++) {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            for (int j = 0; j < x.length; j++) {
                min = Math.min(min, x[j].x[i]);
                max = Math.max(max, x[j].x[i]);
            }

            mean += (max - min) / bound[i];
        }
        mean /= n;
        return (mean);
    }

    private int find(int lcs[], int startindex, int endindex, int value) {
        for (int i = startindex; i < endindex; i++) {
            if (lcs[i] == value) {
                return i;
            }
        }
        return -1;
    }

    //this method is superseeded by the pattern search algorithms, but its not a bad idea to keep this meothd
    //s forms the simplex
    //sf function values of simplex
    /*bl lower bound, bu upper bound */
    private SampleSO cceua2(SampleSO[] s, double bl[], double bu[])  throws SampleLimitException, ObjectiveAchievedException{
        int nps = s.length;
        int nopt = s[0].x.length;

        int n = nps;
        int m = nopt;

        double alpha = 1.0;
        double beta = 0.5;        
        // Assign the best and worst points:                        
        SampleSO sw = s[n-1];
        
        // Compute the centroid of the simplex excluding the worst point:
        double ce[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            ce[i] = 0;
            for (int j = 0; j < n - 1; j++) {
                ce[i] += s[j].x[i];
            }
            ce[i] /= (n - 1);
        }

        // Attempt a reflection point
        double snew1[] = new double[nopt];
        double snew2[] = new double[nopt];
        double snew3[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            snew1[i] = ce[i] + alpha * (ce[i] - sw.x[i]);
        }

        // Check if is outside the bounds:
        int ibound = 0;
        for (int i = 0; i < nopt; i++) {
            if ((snew1[i] - bl[i]) < 0) {
                ibound = 1;
            }
            if ((bu[i] - snew1[i]) < 0) {
                ibound = 2;
            }
        }

        if (ibound >= 1) {
            snew1 = this.randomSampler();
        }
        
        // Reflection failed; now attempt a contraction point:
        //if (fnew.f() > sw.f()) {
        for (int i = 0; i < nopt; i++) {
            snew2[i] = sw.x[i] + beta * (ce[i] - s[n - 1].x[i]);
        }
        snew3 = this.randomSampler();
        

        if (parallelExecution) {
            double x_pseq[][] = new double[][]{snew1, snew2, snew3};
            ParallelSequence.OutputData result = pseq.procedure(x_pseq);
            if (collection == null) {
                collection = result.dc;
            } else {
                synchronized (collection) {
                    if (result.dc != null) {
                        collection.mergeDataCollections(result.dc);
                    }
                }
            }
            if (result.list.get(0).F()[0] <= result.list.get(1).F()[0] && result.list.get(0).F()[0] <= result.list.get(2).F()[0]) {
                return this.factory.getSampleSO(result.list.get(0).x, result.list.get(0).F()[0]);
            } else if (result.list.get(1).F()[0] <= result.list.get(0).F()[0] && result.list.get(1).F()[0] <= result.list.get(2).F()[0]) {
                return this.factory.getSampleSO(result.list.get(1).x, result.list.get(1).F()[0]);
            } else {
                return this.factory.getSampleSO(result.list.get(2).x, result.list.get(2).F()[0]);
            }
        } else {
            SampleSO x0 = this.getSampleSO(snew1);
            SampleSO x1 = this.getSampleSO(snew2);
            SampleSO x2 = this.getSampleSO(snew3);
            
            if (x0.F()[0] <= x1.F()[0] && x0.F()[0] <= x2.F()[0]) {
                return x0;
            } else if (x1.F()[0] <= x0.F()[0] && x1.F()[0] <= x2.F()[0]) {
                return x1;
            } else {
                return x2;
            }
        }
        //do this lately becaus it can throw an exception ..
        /*if (result.list!=null)
            this.injectSamples(result.list);*/
        //inject passiert hier .. 
        
    }
    
    private SampleSO cceua(SampleSO[] simplex, double bl[], double bu[]) throws SampleLimitException, ObjectiveAchievedException {
        //convert boundary constraints to linear constraints
        Matrix A = new Matrix(bl.length + bu.length, n);
        Matrix b = new Matrix(bl.length + bu.length, 1);

        for (int i = 0; i < bl.length; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    A.set(i, j, -1);
                    A.set(i + n, j, 1);
                } else {
                    A.set(i, j, 0);
                    A.set(i + n, j, 0);
                }
            }
            b.set(i, 0, -bl[i]);
            b.set(i + n, 0, bu[i]);
        }
        return SearchMethod.step(this, simplex, A, b, lowBound, upBound);    
    }

    private void EvolveSubPopulation(int nspl, int nps, int npg, int nopt, SampleSO c[], double[] bu, double[] bl) {
        //Evolve sub-population igs for nspl steps:
        for (int loop = 0; loop < nspl; loop++) {
            // Select simplex by sampling the complex according to a linear
            // probability distribution
            int lcs[] = new int[nps];
            lcs[0] = 0;
            for (int k3 = 1; k3 < nps; k3++) {
                int lpos = 0;
                for (int iter = 0; iter < 1000; iter++) {
                    lpos = (int) Math.floor(npg + 0.5 - Math.sqrt((npg + 0.5) * (npg + 0.5) - npg * (npg + 1) * this.randomValue()));
                    //wirklich n^tig??
                    int idx = find(lcs, 0, k3, lpos);
                    if (idx == -1) {
                        break;
                    }
                }
                lcs[k3] = lpos;
            }
            Arrays.sort(lcs);

            // Construct the simplex:            
            SampleSO s[] = new SampleSO[nps];

            for (int i = 0; i < nps; i++) {                
                s[i] = c[lcs[i]].clone();   //?necessary to clone?
            }            
            // Replace the worst point in Simplex with the new point:
            try{
                s[nps - 1] = cceua2(s, bl, bu);
            }catch(Exception e){
                e.printStackTrace();
                return;
            }

            //Replace the simplex into the complex;            
            for (int i = 0; i < nps; i++) {
                c[lcs[i]] = s[i];
            }

            // Sort the complex;
            Arrays.sort(c, new SampleSOComperator(false));
        // End of Inner Loop for Competitive Evolution of Simplexes
        }
    }

    private static abstract class ParamRunnable<Y> implements Runnable{
        Y in;

        ParamRunnable(Y d){
            in = d;
        }
    }

    public SampleSO sceua(double[][] x0, double[] bl, double[] bu, int maxn, int kstop, double pcento, double peps, int ngs, int iseed){
        int method = 1;
                
        SearchMethod = null;
        if (method == 1) {
            SearchMethod = new NelderMead();
        } else if (method == 2) {
            SearchMethod = new ImplicitFiltering();
        } else if (method == 3) {
            SearchMethod = new MDS();
        }
        int nopt = bl.length;
        int npg = 2 * nopt + 1;
        int nps = nopt + 1;
        int nspl = npg;
        int npt = npg * ngs;

        double bound[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            bound[i] = bu[i] - bl[i];
        }
        int i=0;
        SampleSO x[] = new SampleSO[npt];
        try{         
            double initParameter[][] = new double[npt][];

            for (i = 0; i < npt; i++) {
                if (this.x0 != null && this.x0.length > i) {
                    initParameter[i] = this.x0[i].clone();
                } else {
                    initParameter[i] = randomSampler();
                }
            }
            
            if (!this.parallelExecution) {
                for (i = 0; i < npt; i++) {
                    x[i] = getSampleSO(initParameter[i]);
                }
            } else {
                ParallelSequence.OutputData result = pseq.procedure(initParameter);
                for (int j = 0; j < initParameter.length; j++) {
                    try {
                        x[j] = factory.getSampleSO(result.list.get(j).x,result.list.get(j).F()[0]);
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        System.out.println("For an unknown reason parallel sampling did not succeed completly .. switching to sequential execution");
                        for (int k = j; k < initParameter.length; k++) {
                            x[k] = getSampleSO(initParameter[k]);
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
                //this.injectSamples(result.list);
            }            
        }catch(SampleLimitException e){
            System.out.println("Sample Limit Reached: " + e);
            return x[i];
        }catch(Exception e){            
            e.printStackTrace();
            return x[i];
        }

        int nloop = 0;        
        // Sort the population in order of increasing function values;
        Arrays.sort(x, new SampleSOComperator(false));
                        
        // Computes the normalized geometric range of the parameters
        double gnrng = NormalizedgeometricRange(x, bound); //exp(mean(log((max(x)-min(x))./bound)));

        log(JAMS.i18n("The_Inital_Loop_0"));
        log(JAMS.i18n("Best") + x[0].toString());
        log(JAMS.i18n("Worst") + x[npt-1].toString());
                                
        if (gnrng < peps) {            
            log(JAMS.i18n("THE_POPULATION_HAS_CONVERGED_TO_A_PRESPECIFIED_SMALL_PARAMETER_SPACE"));
        }

        // Begin evolution loops:
        nloop = 0;
        double criter[] = new double[kstop];
        double criter_change = 100000;

        while (criter_change > pcento) {
            nloop++;
            ArrayList<SampleSO[]> complexes = new ArrayList<SampleSO[]>();
            ArrayList<int[]> k1list = new ArrayList<int[]>();
            ArrayList<int[]> k2list = new ArrayList<int[]>();

            BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(64);
            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(ngs, ngs, 30, TimeUnit.SECONDS, workQueue);
            // Loop on complexes (sub-populations);
            for (int igs = 0; igs < ngs; igs++) {
                // Partition the population into complexes (sub-populations);
                int k1[] = new int[npg];
                int k2[] = new int[npg];

                for ( i = 0; i < npg; i++) {
                    k1[i] = i;
                    k2[i] = k1[i] * ngs + igs;
                }

                SampleSO c[] = new SampleSO[npg];                
                for ( i = 0; i < npg; i++) {
                    c[k1[i]] = x[k2[i]].clone();
                }
                complexes.add(c);
                k1list.add(k1);
                k2list.add(k2);

                if (parallelExecution) {
                    InputData param = new InputData(this, nspl, npg, npg, nopt, c, x, k1, k2, bu, bl);

                    ParamRunnable r = new ParamRunnable<InputData>(param) {
                        @Override
                        public void run() {
                            synchronized (SCE.this) { //really strict. something less strict would be better .. 
                                EvolveSubPopulation(this.in.nspl, this.in.nps, this.in.npg, this.in.nopt, this.in.c, this.in.bu, this.in.bl);
                            }
                            for (int i = 0; i < in.npg; i++) {
                                in.x[in.k2[i]] = in.c[in.k1[i]];
                            }
                        }
                    };
                    threadPool.execute(r);
                }else{
                    EvolveSubPopulation(nspl, nps, npg, nopt, c, bu, bl);
                    for (int j = 0; j < npg; j++) {
                        x[k2[j]] = c[k1[j]];
                    }
                }
            }
            if (parallelExecution) {
                threadPool.shutdown();

                try {
                    threadPool.awaitTermination(100, TimeUnit.HOURS);
                } catch (InterruptedException ie) {
                    System.out.println("Serious problem with thread pool .. was interrupted");
                    ie.printStackTrace();
                }
            }
            Arrays.sort(x, new SampleSOComperator(false));
                  
            //Compute the standard deviation for each parameter            
            gnrng = NormalizedgeometricRange(x, bound);

            // Record the best and worst points;            
            log(JAMS.i18n("Evolution_Loop") + ":" + nloop + JAMS.i18n("Trial") + " " + getIterationCounter());
            log(JAMS.i18n("Best") + x[0]);
            log(JAMS.i18n("Worst") + x[x.length-1]);
                        
            if (gnrng < peps) {
                log(JAMS.i18n("THE_POPULATION_HAS_CONVERGED_TO_A_PRESPECIFIED_SMALL_PARAMETER_SPACE"));
            }
            if (getIterationCounter()>this.maxn){
                break;
            }
            for ( i = 0; i < kstop - 1; i++) {
                criter[i] = criter[i + 1];
            }
            criter[kstop - 1] = x[0].f();
            if (nloop >= kstop) {
                criter_change = Math.abs(criter[0] - criter[kstop - 1]) * 100.0;
                double criter_mean = 0;
                for ( i = 0; i < kstop; i++) {
                    criter_mean += Math.abs(criter[i]);
                }
                criter_mean /= kstop;
                criter_change /= criter_mean;

                if (criter_change < pcento) {
                    log(JAMS.i18n("THE_BEST_POINT_HAS_IMPROVED_IN_LAST") + " " + kstop + " " +  JAMS.i18n("LOOPS_BY"));
                    log(JAMS.i18n("LESS_THAN_THE_THRESHOLD") + " " + pcento + "%");
                    log(JAMS.i18n("CONVERGENCY_HAS_ACHIEVED_BASED_ON_OBJECTIVE_FUNCTION_CRITERIA"));
                }
            }
        }
        log(JAMS.i18n("SEARCH_WAS_STOPPED_AT_TRIAL_NUMBER") + " " +getIterationCounter());
        log(JAMS.i18n("NORMALIZED_GEOMETRIC_RANGE") + " " + gnrng);
        log(JAMS.i18n("THE_BEST_POINT_HAS_IMPROVED_IN_LAST") + kstop + " "+JAMS.i18n("LOOPS_BY") + " "+ criter_change + "%");

        if (collection != null) {
            collection.dump(getModel().getWorkspace().getOutputDataDirectory(),false);
        }
        
        return x[0];
    }

    @Override
    public void procedure()  throws SampleLimitException, ObjectiveAchievedException {                                                     
        sceua(x0, this.lowBound, this.upBound, (int)getMaxn(), (int)kstop, pcento, peps, (int)complexesCount, 10);
    }

    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(SCE.class.getSimpleName(), SCE.class.getName(), 6, false);

        desc.addParameter(new NumericOptimizerParameter("complexesCount",
                JAMS.i18n("number_of_complexes"), 2, 1, 100));

        desc.addParameter(new NumericOptimizerParameter(
                "pcento", JAMS.i18n("worst_acceptable_improvement"),
                0.05, 0.000001, 1));

        desc.addParameter(new NumericOptimizerParameter(
                "peps", JAMS.i18n("minimal_geometric_population"),
                0.00001, 0.000001, 1));

        desc.addParameter(new NumericOptimizerParameter(
                "kstop", JAMS.i18n("kStop"),
                10, 1, 100));
        
        desc.addParameter(new BooleanOptimizerParameter(
                "parallelExecution", JAMS.i18n("parallelExecution"),
                false));
        
        desc.addParameter(new NumericOptimizerParameter(
                "threadCount", JAMS.i18n("threadCount"),
                12, 1, 20));
        
        desc.addParameter(new StringOptimizerParameter(
                "excludeFiles", JAMS.i18n("exclude_files_list"),
                "(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log"));

        return desc;
    }
}