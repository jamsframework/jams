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
import jams.components.optimizer.Optimizer.AbstractFunction;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;
import java.util.Arrays.*;
import java.util.Comparator;
import java.util.StringTokenizer;
import jams.JAMS;
import jams.JAMSTools;
import jams.io.SerializableBufferedWriter;

/**
 *
 * @author Christian Fischer, based on the original MatLab sources
 */
@JAMSComponentDescription(title = "Title",
author = "Author",
description = "Description")
@SuppressWarnings("unchecked")
public class SimpleSCE extends Optimizer {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.INIT,
        description = "A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don´t specify A and B the unconstrained problem will be solved")
        public JAMSString LinearConstraintMatrixA;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.INIT,
        description = "A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don´t specify A and B the unconstrained problem will be solved")
        public JAMSString LinearConstraintVectorB;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "optimization parameter: number of complex, a common value for this is 2 or 3")
        public JAMSInteger NumberOfComplexes;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "stopping parameter: pcento")
        public JAMSDouble pcento;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "stopping parameter: kstop")
        public JAMSInteger kstop;
    
    @JAMSVarDescription(
        access = JAMSVarDescription.AccessType.READ,
        update = JAMSVarDescription.UpdateType.RUN,
        description = "stopping parameter: peps")
        public JAMSDouble peps;

    GenericDataWriter writer = null;
    SerializableBufferedWriter sampleWriter = null;
    int N; //parameter dimension
    int p; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    PatternSearch SearchMethod = null;
    Matrix LinearConstraints_A = null, LinearConstraints_b = null;

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
                    this.getModel().getRuntime().sendHalt("SimpleSCE: Linear Constraint Matrix, dimension mismatch!");
                }
                for (int j = 0; j < m; j++) {
                    String number = line_tok.nextToken();
                    double value = 0.0;
                    try {
                        value = Double.parseDouble(number);
                    } catch (NumberFormatException e) {
                        this.getModel().getRuntime().sendHalt("SimpleSCE: Can´t read Linear Constraint Matrix, because there are unparseable elements:" + e.toString());
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
                    this.getModel().getRuntime().sendHalt("SimpleSCE: Can´t read Linear Constraint Matrix, because there are unparseable elements:" + e.toString());
                }
                LinearConstraints_b.set(i, 0, value);
            }
        }

        if (LinearConstraints_A != null && LinearConstraints_b != null) {
            if (LinearConstraints_A.getRowDimension() != LinearConstraints_b.getRowDimension()) {
                this.getModel().getRuntime().sendHalt("SimpleSCE: LinearConstraintMatrixA must have the same number of rows as LinearConstraintVectorB");
            }
            if (LinearConstraints_A.getColumnDimension() != n) {
                this.getModel().getRuntime().sendHalt("SimpleSCE: LinearConstraintMatrixA must have the same number of columns as thera are parameters");
            }
        }
    }

    public double Custom_rand() {
        return this.generator.nextDouble();
    }
       
    public double NormalizedgeometricRange(Sample x[], double bound[]) {
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

            mean += Math.log(max - min) / bound[i];
        }
        mean /= n;
        return Math.exp(mean);
    }

    public double[] std(Sample x[]) {
        if (x.length == 0) {
            return null;
        }
        if (x.length == 1) {
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

        public double f(double x[]) {
            return myOptimizer.funct(x);
        }
    }
    //s forms the simplex
    //sf function values of simplex
    /*bl lower bound, bu upper bound 
    public double[] cceua2(double s[][], double sf[], double bl[], double bu[]) {
        int nps = s.length;
        int nopt = s[0].length;

        int n = nps;
        int m = nopt;

        double alpha = 1.0;
        double beta = 0.5;

        // Assign the best and worst points:
        double sb[] = new double[nopt];
        double sw[] = new double[nopt];
        double fb = sf[0];
        double fw = sf[n - 1];

        for (int i = 0; i < nopt; i++) {
            sb[i] = s[0][i];
            sw[i] = s[n - 1][i];
        }

        // Compute the centroid of the simplex excluding the worst point:
        double ce[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            ce[i] = 0;
            for (int j = 0; j < n - 1; j++) {
                ce[i] += s[j][i];
            }
            ce[i] /= (n - 1);
        }

        // Attempt a reflection point
        double snew[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            snew[i] = ce[i] + alpha * (ce[i] - sw[i]);
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

        double fnew = funct(snew);

        // Reflection failed; now attempt a contraction point:
        if (fnew > fw) {
            for (int i = 0; i < nopt; i++) {
                snew[i] = sw[i] + beta * (ce[i] - sw[i]);
            }
            fnew = funct(snew);
        }
        // Both reflection and contraction have failed, attempt a random point;
        if (fnew > fw) {
            snew = this.RandomSampler();
            fnew = funct(snew);
        }

        double result[] = new double[nopt + 1];
        for (int i = 0; i < nopt; i++) {
            result[i] = snew[i];
        }
        result[nopt] = fnew;
        return result;
    }*/
    
    public Sample cceua(Sample[] simplex, double bl[], double bu[]) {
        SCEFunctionEvaluator eval = new SCEFunctionEvaluator(this);
        
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

    void EvolveSubPopulation(int nspl, int nps, int npg, int nopt, Sample c[], double[] bu, double[] bl) {
        //Evolve sub-population igs for nspl steps:
        for (int loop = 0; loop < nspl; loop++) {
            // Select simplex by sampling the complex according to a linear
            // probability distribution
            int lcs[] = new int[nps];
            lcs[0] = 0;
            for (int k3 = 1; k3 < nps; k3++) {
                int lpos = 0;
                for (int iter = 0; iter < 1000; iter++) {
                    lpos = (int) Math.floor(npg + 0.5 - Math.sqrt((npg + 0.5) * (npg + 0.5) - npg * (npg + 1) * Custom_rand()));
                    //wirklich nötig??
                    int idx = find(lcs, 0, k3, lpos);
                    if (idx == -1) {
                        break;
                    }
                }
                lcs[k3] = lpos;
            }
            Arrays.sort(lcs);

            // Construct the simplex:            
            Sample s[] = new Sample[nps];

            for (int i = 0; i < nps; i++) {                
                s[i] = c[lcs[i]].clone();   //?necessary to clone?
            }            
            // Replace the worst point in Simplex with the new point:
            s[nps - 1] = cceua(s, bl, bu);

            //Replace the simplex into the complex;            
            for (int i = 0; i < nps; i++) {
                c[lcs[i]] = s[i];
            }

            // Sort the complex;
            Arrays.sort(c, new SampleComperator(false));
        // End of Inner Loop for Competitive Evolution of Simplexes
        }
    }

    public Sample sceua(double[] x0, double[] bl, double[] bu, int maxn, int kstop, double pcento, double peps, int ngs, int iseed, int iniflg) {
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
        int mings = ngs;
        int npt = npg * ngs;

        double bound[] = new double[nopt];
        for (int i = 0; i < nopt; i++) {
            bound[i] = bu[i] - bl[i];
        }

        // Create an initial population to fill array x(npt,nopt):
        this.generator.setSeed(iseed);        
        Sample x[] = new Sample[npt];
        for (int i = 0; i < npt; i++) {
            if (iniflg == 1 && i == 0) {
                x[i] = getSample(x0);
            } else {
                x[i] = getSample(RandomSampler());
            }
        }

        int nloop = 0;        
        // Sort the population in order of increasing function values;
        Arrays.sort(x, new SampleComperator(false));
                
        // Compute the standard deviation for each parameter
        double xnstd[] = std(x);

        // Computes the normalized geometric range of the parameters
        double gnrng = NormalizedgeometricRange(x, bound); //exp(mean(log((max(x)-min(x))./bound)));

        System.out.println("The Inital Loop: 0");
        System.out.println("Best: " + x[0].toString());
        System.out.println("Worst: " + x[npt-1].toString());

        //Check for convergency;
        if (currentSampleCount >= maxn) {
            System.out.println("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
            System.out.println("ON THE MAXIMUM NUMBER OF TRIALS" + maxn);
            System.out.println("HAS BEEN EXCEEDED.  SEARCH WAS STOPPED AT TRIAL NUMBER:" + currentSampleCount);
            System.out.println("OF THE INITIAL LOOP!");            
        }

        if (gnrng < peps) {            
            System.out.println("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");
        }

        // Begin evolution loops:
        nloop = 0;
        double criter[] = new double[kstop];
        double criter_change = 100000;

        while (currentSampleCount < maxn && gnrng > peps && criter_change > pcento) {
            nloop++;
            // Loop on complexes (sub-populations);
            for (int igs = 0; igs < ngs; igs++) {
                // Partition the population into complexes (sub-populations);
                int k1[] = new int[npg];
                int k2[] = new int[npg];

                for (int i = 0; i < npg; i++) {
                    k1[i] = i;
                    k2[i] = k1[i] * ngs + igs;
                }

                Sample c[] = new Sample[npg];                
                for (int i = 0; i < npg; i++) {
                    c[k1[i]] = x[k2[i]].clone();
                }
                EvolveSubPopulation(nspl, nps, npg, nopt, c, bu, bl);

                // Replace the complex back into the population;
                for (int i = 0; i < npg; i++) {                    
                    x[k2[i]] = c[k1[i]];
                }
            // End of Loop on Complex Evolution;
            }            
            Arrays.sort(x, new SampleComperator(false));
                  
            //Compute the standard deviation for each parameter
            xnstd = std(x);
            gnrng = NormalizedgeometricRange(x, bound);

            // Record the best and worst points;            
            System.out.println("Evolution Loop:" + nloop + " - Trial - " + currentSampleCount);
            System.out.println("BEST:" + x[0]);
            System.out.println("WORST:" + x[x.length-1]);
            
            // Check for convergency;
            if (currentSampleCount >= maxn) {
                System.out.println("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
                System.out.println("ON THE MAXIMUM NUMBER OF TRIALS " + maxn + " HAS BEEN EXCEEDED!");                
            }
            if (gnrng < peps) {
                System.out.println("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");                
            }

            for (int i = 0; i < kstop - 1; i++) {
                criter[i] = criter[i + 1];
            }
            criter[kstop - 1] = x[0].fx;
            if (nloop >= kstop) {
                criter_change = Math.abs(criter[0] - criter[kstop - 1]) * 100.0;
                double criter_mean = 0;
                for (int i = 0; i < kstop; i++) {
                    criter_mean += Math.abs(criter[i]);
                }
                criter_mean /= kstop;
                criter_change /= criter_mean;

                if (criter_change < pcento) {
                    System.out.println("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY");
                    System.out.println("LESS THAN THE THRESHOLD " + pcento + "%");
                    System.out.println("CONVERGENCY HAS ACHIEVED BASED ON OBJECTIVE FUNCTION CRITERIA!!!");                   
                }
            }
        }
        System.out.println("SEARCH WAS STOPPED AT TRIAL NUMBER: " + currentSampleCount);
        System.out.println("NORMALIZED GEOMETRIC RANGE = " + gnrng);
        System.out.println("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY " + criter_change + "%");
                
        return x[0];
    }

    public void run() {
        int iseed = 10;
        int iniflg = 0;
        
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        if (!enable.getValue()){
            singleRun();
            return;            
        }
        
        double x0[] = RandomSampler();

        int iNumberOfComplexes = 0;
        if (NumberOfComplexes != null) {
            iNumberOfComplexes = NumberOfComplexes.getValue();
        }
        if (iNumberOfComplexes <= 0) {
            this.getModel().getRuntime().sendInfoMsg("NumberOfComplexes: value not specified or out of bounds, set to default value: 2");
            iNumberOfComplexes = 2;
        }
        
        double pcento = 0.0;
        if (this.pcento != null) {
            pcento = this.pcento.getValue();
        }
        if (pcento <= 0) {
            this.getModel().getRuntime().sendInfoMsg("pcento: value not specified or out of bounds, set to default value: 0.1");
            pcento = 0.1;
        }
        
        double peps = 0.0;
        if (this.peps != null) {
            peps = this.peps.getValue();
        }
        if (peps <= 0) {
            this.getModel().getRuntime().sendInfoMsg("pcento: value not specified or out of bounds, set to default value: 0.00001");
            peps = 0.00001;
        }
        
        int kstop = 0;        
        if (this.kstop != null) {
            kstop = this.kstop.getValue();
        }
        if (kstop <= 0) {
            this.getModel().getRuntime().sendInfoMsg("kstop: value not specified or out of bounds, set to default value: 10");
            kstop = 10;
        }
        
        int maxn = 10;        
        if (this.maxn != null) {
            maxn = this.maxn.getValue();
        }
        if (maxn <= 0) {
            this.getModel().getRuntime().sendInfoMsg("kstop: value not specified or out of bounds, set to default value: 10000");
            maxn = 10000;
        }
        
        sceua(x0, this.lowBound, this.upBound, maxn, kstop, pcento, peps, iNumberOfComplexes, iseed, iniflg);    
    }

    public Sample offlineRun(double[] start, double lowBound[], double upBound[], int NumberOfComplexes, int MaximizeEff, int maxn, int kstop, double pcento, double peps, Optimizer.AbstractFunction destFunction) {
        int iseed = 10;
        int iniflg = 0;
        this.lowBound = lowBound;
        this.upBound = upBound;
        this.N = lowBound.length;
        this.n = this.N;
        this.mode = new JAMSInteger(MaximizeEff);

        double x0[] = RandomSampler();
        if (start != null) {
            x0 = start;
        }
        this.GoalFunction = destFunction;

        return sceua(x0, lowBound, upBound, maxn, kstop, pcento, peps, NumberOfComplexes, iseed, iniflg);
    }   
}