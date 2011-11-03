/*
 * ShuffleComplexEvolution.java
 * Created on 30. Juni 2006, 15:12
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.components.optimizer;

import Jama.Matrix;
import jams.components.optimizer.DirectSearchMethods.ImplicitFiltering;
import jams.components.optimizer.DirectSearchMethods.MDS;
import jams.components.optimizer.DirectSearchMethods.NelderMead;
import jams.components.optimizer.DirectSearchMethods.PatternSearch;
import jams.components.optimizer.SOOptimizer.AbstractFunction;
import java.util.Arrays;
import jams.data.*;
import jams.JAMS;
import jams.components.optimizer.SampleFactory.SampleSO;
import jams.components.optimizer.SampleFactory.SampleSOComperator;
import jams.model.*;
import java.util.Arrays.*;
import java.util.StringTokenizer;

/**
 *
 * @author Christian Fischer, based on the original MatLab sources
 */
@JAMSComponentDescription(title = "Title",
author = "Author",
description = "Description")
@SuppressWarnings("unchecked")
public class SimpleSCE extends SOOptimizer {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.INIT,
        description = "A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don^t specify A and B the unconstrained problem will be solved")
        public JAMSString LinearConstraintMatrixA;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.INIT,
        description = "A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don^t specify A and B the unconstrained problem will be solved")
        public JAMSString LinearConstraintVectorB;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "optimization parameter: number of complex, a common value for this is 2 or 3",
        defaultValue = "2")
        public JAMSInteger NumberOfComplexes;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "stopping parameter: pcento, optimization is stopped if objective function does not improve by pcento percent in the last kstop iterations",
        defaultValue = "0.01")
        public JAMSDouble pcento;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "stopping parameter: kstop, for further description see pcento",
        defaultValue = "5")
        public JAMSInteger kstop;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "stopping parameter: peps, optimization is stopped if the parameter - space has converged to a volume less than peps",
        defaultValue = "0.0001")
        public JAMSDouble peps;
    
    int N; //parameter dimension
    int p; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    PatternSearch SearchMethod = null;
    Matrix LinearConstraints_A = null, LinearConstraints_b = null;

    @Override
    public void init() {               
        super.init();
        if (!enable.getValue())
            return;            
        
        if (LinearConstraintMatrixA != null && LinearConstraintVectorB != null) {
            StringTokenizer tok = new StringTokenizer(LinearConstraintMatrixA.getValue(), ";");

            int n = tok.countTokens(), m = -1;
            LinearConstraints_A = null;
            for (int i = 0; i < n; i++) {
                StringTokenizer line_tok = new StringTokenizer(tok.nextToken(), ",");
                if (m == -1) {
                    m = line_tok.countTokens();
                    LinearConstraints_A = new Matrix(n, m);
                }
                if (m != line_tok.countTokens()) {
                    stop(JAMS.i18n("Linear_Constraint_Matrix_dimension_mismatch"));
                }
                for (int j = 0; j < m; j++) {
                    String number = line_tok.nextToken();
                    double value = 0.0;
                    try {
                        value = Double.parseDouble(number);
                    } catch (NumberFormatException e) {
                        stop(JAMS.i18n("Cant_read_Linear_Constraint_Matrix_because_there_are_unparseable_elements") + ":" + e.toString());
                    }
                    LinearConstraints_A.set(i, j, value);
                }
            }

            tok = new StringTokenizer(LinearConstraintVectorB.getValue(), ";");
            LinearConstraints_b = new Matrix(n, 1);
            n = tok.countTokens();
            for (int i = 0; i < n; i++) {
                String number = tok.nextToken();
                double value = 0.0;
                try {
                    value = Double.parseDouble(number);
                } catch (NumberFormatException e) {
                    stop(JAMS.i18n("Cant_read_Linear_Constraint_Matrix_because_there_are_unparseable_elements") + ":" + e.toString());
                }
                LinearConstraints_b.set(i, 0, value);
            }
        }

        if (LinearConstraints_A != null && LinearConstraints_b != null) {
            if (LinearConstraints_A.getRowDimension() != LinearConstraints_b.getRowDimension()) {
                stop(JAMS.i18n("LinearConstraintMatrixA_must_have_the_same_number_of_rows_as_LinearConstraintVectorB"));
            }
            if (LinearConstraints_A.getColumnDimension() != n) {
                stop(JAMS.i18n("LinearConstraintMatrixA_must_have_the_same_number_of_columns_as_there_are_parameters"));
            }
        }
    }
           
    public double NormalizedgeometricRange(SampleSO x[], double bound[]) {
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

    public double[] std(SampleSO x[]) {
        if (x.length <= 1) {
            return null;
        }                
        double mean[] = new double[n];
        double var[] = new double[n];

        for (int i = 0; i < n; i++) {
            mean[i] = 0;
            for (int j = 0; j < x.length; j++) {
                mean[i] += x[j].x[i];
            }
            mean[i] /= x.length;
        }

        for (int i = 0; i < n; i++) {
            var[i] = 0;
            for (int j = 0; j < x.length; j++) {
                var[i] += (mean[i] - x[j].x[i]) * (mean[i] - x[j].x[i]);
            }
            var[i] = Math.sqrt(var[i]) / (x.length - 1);
        }

        return var;
    }

    public int find(int lcs[], int startindex, int endindex, int value) {
        for (int i = startindex; i < endindex; i++) {
            if (lcs[i] == value) {
                return i;
            }
        }
        return -1;
    }

    class SCEFunctionEvaluator extends AbstractFunction {
        SimpleSCE myOptimizer = null;

        SCEFunctionEvaluator(SimpleSCE mySCE) {
            myOptimizer = mySCE;
        }

        public double f(double x[])  throws SampleLimitException, ObjectiveAchievedException {
            return myOptimizer.funct(x);
        }
    }
    //s forms the simplex
    //sf function values of simplex
    /*bl lower bound, bu upper bound */
    public SampleSO cceua2(SampleSO[] s, double bl[], double bu[])  throws SampleLimitException, ObjectiveAchievedException{
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
        double snew[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            snew[i] = ce[i] + alpha * (ce[i] - sw.x[i]);
        }

        // Check if is outside the bounds:
        int ibound = 0;
        for (int i = 0; i < nopt; i++) {
            if ((snew[i] - bl[i]) < 0) {
                ibound = 1;
            }
            if ((bu[i] - snew[i]) < 0) {
                ibound = 2;
            }
        }

        if (ibound >= 1) {
            snew = this.RandomSampler();
        }
        
        SampleSO fnew = this.getSample(snew);
        
        // Reflection failed; now attempt a contraction point:
        if (fnew.f() > sw.f()) {
            for (int i = 0; i < nopt; i++) {
                snew[i] = sw.x[i] + beta * (ce[i] - s[n-1].x[i]);
            }
            fnew = this.getSample(snew);
        }
        // Both reflection and contraction have failed, attempt a random point;
        if (fnew.f() > sw.f()) {
            snew = this.RandomSampler();
            fnew = this.getSample(snew);
        }
        
        return fnew;
    }
    
    public SampleSO cceua(SampleSO[] simplex, double bl[], double bu[]) throws SampleLimitException, ObjectiveAchievedException {
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

    void EvolveSubPopulation(int nspl, int nps, int npg, int nopt, SampleSO c[], double[] bu, double[] bl) {
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
                s[nps - 1] = cceua(s, bl, bu);
            }catch(Exception e){
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
       
    public SampleSO sceua(double[] x0, double[] bl, double[] bu, int maxn, int kstop, double pcento, double peps, int ngs, int iseed){
        int method = 1;
                
        SearchMethod = null;
        if (method == 1) {
            SearchMethod = new NelderMead();
        } else if (method == 2) {
            SearchMethod = new ImplicitFiltering();
        } else if (method == 3) {
            SearchMethod = new MDS();
        }
        int nopt = x0.length;
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
            x[0] = getSample(x0);
            for (i = 0; i < npt; i++) {
                x[i] = getSample(RandomSampler());
            }
        }catch(Exception e){
            System.out.println(e);
            return x[i];
        }

        int nloop = 0;        
        // Sort the population in order of increasing function values;
        Arrays.sort(x, new SampleSOComperator(false));
                        
        // Computes the normalized geometric range of the parameters
        double gnrng = NormalizedgeometricRange(x, bound); //exp(mean(log((max(x)-min(x))./bound)));

        sayThis(JAMS.i18n("The_Inital_Loop_0"));
        sayThis(JAMS.i18n("Best") + x[0].toString());
        sayThis(JAMS.i18n("Worst") + x[npt-1].toString());
                                
        if (gnrng < peps) {            
            sayThis(JAMS.i18n("THE_POPULATION_HAS_CONVERGED_TO_A_PRESPECIFIED_SMALL_PARAMETER_SPACE"));
        }

        // Begin evolution loops:
        nloop = 0;
        double criter[] = new double[kstop];
        double criter_change = 100000;

        while (criter_change > pcento) {
            nloop++;
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
                EvolveSubPopulation(nspl, nps, npg, nopt, c, bu, bl);

                // Replace the complex back into the population;
                for ( i = 0; i < npg; i++) {                    
                    x[k2[i]] = c[k1[i]];
                }
            // End of Loop on Complex Evolution;
            }            
            Arrays.sort(x, new SampleSOComperator(false));
                  
            //Compute the standard deviation for each parameter            
            gnrng = NormalizedgeometricRange(x, bound);

            // Record the best and worst points;            
            sayThis(JAMS.i18n("Evolution_Loop") + ":" + nloop + JAMS.i18n("Trial") + " " + iterationCounter.getValue());
            sayThis(JAMS.i18n("Best") + x[0]);
            sayThis(JAMS.i18n("Worst") + x[x.length-1]);
                        
            if (gnrng < peps) {
                sayThis(JAMS.i18n("THE_POPULATION_HAS_CONVERGED_TO_A_PRESPECIFIED_SMALL_PARAMETER_SPACE"));
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
                    sayThis(JAMS.i18n("THE_BEST_POINT_HAS_IMPROVED_IN_LAST") + " " + kstop + " " +  JAMS.i18n("LOOPS_BY"));
                    sayThis(JAMS.i18n("LESS_THAN_THE_THRESHOLD") + " " + pcento + "%");
                    sayThis(JAMS.i18n("CONVERGENCY_HAS_ACHIEVED_BASED_ON_OBJECTIVE_FUNCTION_CRITERIA"));                   
                }
            }
        }
        sayThis(JAMS.i18n("SEARCH_WAS_STOPPED_AT_TRIAL_NUMBER") + " " + this.iterationCounter.getValue());
        sayThis(JAMS.i18n("NORMALIZED_GEOMETRIC_RANGE") + " " + gnrng);
        sayThis(JAMS.i18n("THE_BEST_POINT_HAS_IMPROVED_IN_LAST") + kstop + " "+JAMS.i18n("LOOPS_BY") + " "+ criter_change + "%");
                
        return x[0];
    }

    @Override
    public void procedure()  throws SampleLimitException, ObjectiveAchievedException {
        int iseed = 10;
        
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        if (!enable.getValue()){
            singleRun();
            return;            
        }
                
        if (x0 == null){
            x0 = RandomSampler(); 
        }
                                
        if (NumberOfComplexes.getValue() <= 0) {
            sayThis("NumberOfComplexes_value_not_specified");
            NumberOfComplexes.setValue(2);
        }                                   
        if (pcento.getValue() <= 0) {
            sayThis("pcento_value_not_specified");
            pcento.setValue(0.1);
        }                        
        if (peps.getValue() <= 0) {
            sayThis("peps_value_not_specified");
            peps.setValue(0.00001);
        }                        
        if (kstop.getValue() <= 0) {
            sayThis("kstop_value_not_specified");
            peps.setValue(10);
        }                        
        if (maxn.getValue() <= 0) {
            sayThis("maxn_value_not_specified");
            peps.setValue(10000);
        }        
        sceua(x0, this.lowBound, this.upBound, maxn.getValue(), kstop.getValue(), pcento.getValue(), peps.getValue(), NumberOfComplexes.getValue(), iseed);    
    }

    public SampleSO offlineRun(double[] start, double lowBound[], double upBound[], int NumberOfComplexes, int MaximizeEff, int maxn, int kstop, double pcento, double peps, SOOptimizer.AbstractFunction destFunction){
        int iseed = 10;
        this.lowBound = lowBound;
        this.upBound = upBound;
        this.N = lowBound.length;
        this.n = this.N;
        this.mode = JAMSDataFactory.createInteger();
        this.mode.setValue(MaximizeEff);

        double x0[] = RandomSampler();
        if (start != null) {
            x0 = start;
        }
        this.GoalFunction = destFunction;

        return sceua(x0, lowBound, upBound, maxn, kstop, pcento, peps, NumberOfComplexes, iseed);
    }   
}