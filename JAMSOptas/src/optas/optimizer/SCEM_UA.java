/*
 * ABCGradientDescent.java
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
import Jama.*;
import jams.JAMS;
import jams.model.JAMSComponentDescription;
import java.util.ArrayList;
import java.util.Comparator;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(title = "SCEM_UA",
author = "Christian Fischer",
description = "Optimization routine SCEM_UA")
public class SCEM_UA extends Optimizer {
    public int population;
    public int complexes;

    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(SCEM_UA.class.getSimpleName(), SCEM_UA.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("population", JAMS.i18n("size_of_population"), 500, 0, 100000));
        desc.addParameter(new NumericOptimizerParameter("complexes", JAMS.i18n("number_of_complexes"), 3, 1, 100));

        return desc;
    }

    /**
     * @return the population
     */
    public double getPopulation() {
        return population;
    }

    /**
     * @param population the population to set
     */
    public void setPopulation(double population) {
        this.population = (int)population;
    }

    /**
     * @return the complexes
     */
    public double getComplexes() {
        return complexes;
    }

    /**
     * @param complexes the complexes to set
     */
    public void setComplexes(double complexes) {
        this.complexes = (int)complexes;
    }

    public class SCE_Comparator implements Comparator {

        private int col = 0;
        private int order = 1;

        public SCE_Comparator(int col, boolean decreasing_order) {
            this.col = col;
            if (decreasing_order) {
                order = -1;
            } else {
                order = 1;
            }
        }

        public int compare(Object d1, Object d2) {

            double[] b1 = (double[]) d1;
            double[] b2 = (double[]) d2;

            if (b1[col] < b2[col]) {
                return -1 * order;
            } else if (b1[col] == b2[col]) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }

    int q; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    int L; //number of offspring generated per iteration
    double gamma; //Kurtosis parameter Bayesian Inference Scheme (Thiemann et al., 2001)
    double ndraw; //Number of accepted draws to infer posterior distribution on (?)
   
    private boolean isSampleValid(double[] sample) {
        for (int i = 0; i < n; i++) {
            if (sample[i] < lowBound[i] || sample[i] > upBound[i]) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public void sort(double data[][], int col, boolean decreasing_order) {
        SCE_Comparator comparator = new SCE_Comparator(col, decreasing_order);
        java.util.Arrays.sort(data, comparator);
    }

    public double[] getMean(double data[][]) {	    //no error here :)
        double[] mean = new double[n];

        java.util.Arrays.fill(mean, 0);

        for (int j = 0; j < n; j++) {
            for (int i = 0; i < data.length; i++) {
                mean[j] += data[i][j];
            }
            if (data.length == 0) {
                log("Complex in SCEM_UA ist leer! - Kritischer Fehler!!");
            }

            mean[j] /= data.length;
        }
        return mean;
    }

    public double[][] getCoVarMatrix(double data[][]) { //no error here :)
        double[][] coV = new double[n][n];
        double[] mean = getMean(data);
        double vecLength = data.length;

        if (data.length == 0) {
            log("Complex in SCEM_UA hat nur die Größe von 1! - Kritischer Fehler!!");
        }

        for (int i = 0; i < n; i++) {
            java.util.Arrays.fill(coV[i], 0);
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < vecLength; k++) {
                    coV[i][j] += data[k][i] * data[k][j];
                }
                coV[i][j] -= vecLength * mean[i] * mean[j];
                coV[i][j] /= (vecLength - 1);
            }
        }

        return coV;
    }

    public double[][] latin(int s, int n) {
        double x[][] = new double[s][n];

        for (int i = 0; i < s; i++) {
            x[i] = randomSampler();
        }
        return x;
    }

    public double[] ComputeDensity(double x[]) throws SampleLimitException, ObjectiveAchievedException {
        double pset[] = new double[n + m + 1];

        for (int j = 0; j < x.length; j++) {
            pset[j] = x[j];
        }

        Sample s = getSample(x);

        for (int i = 0; i < m; i++) {
            pset[n + i] = s.F()[i];
        }

        return pset;
    }

    public double[][] ComputeDensity(double x[][]) throws SampleLimitException, ObjectiveAchievedException{
        int e = 0;
        double pset[][] = new double[x.length][n + m + 1];
        double tmp[];

        for (int i = 0; i < x.length; i++) {
            tmp = ComputeDensity(x[i]);
            for (int j = 0; j < n + m + 1; j++) {
                pset[i][j] = tmp[j];
            }
        }
        return pset;
    }

    public double[][][] PartComplexes(double D[][]) {
        double C[][][] = new double[q][m][n + m + 1];

        for (int kk = 0; kk < q; kk++) {
            for (int j = 0; j < m; j++) {
                int idx = q * j + kk;
                for (int i = 0; i < n + m + 1; i++) {
                    C[kk][j][i] = D[idx][i];
                }
            }
        }
        return C;
    }

    public void SEM(double D[][], double C[][][], ArrayList<ArrayList<double[]>> Sequences, double x[][]) throws SampleLimitException, ObjectiveAchievedException {
        for (int kk = 0; kk < q; kk++) {
            for (int bb = 0; bb < L; bb++) {
                OffMetro(Sequences.get(kk), C[kk], kk, bb);
            }
        }
    }

    public double[] randn(int n) {
        double randn[] = new double[n];

        for (int i = 0; i < n; i++) {
            randn[i] = generator.nextGaussian();
        }

        return randn;
    }

    //Generates offspring using METROPOLIS HASTINGS monte-carlo markov chain
    public void OffMetro(ArrayList<double[]> Seq, double C[][], int kk, int bb) throws SampleLimitException, ObjectiveAchievedException{
        int s = Seq.size();

        double offspring[] = null;
        double b[];
        //draw point from sequence
        s = generator.nextInt(s);
        b = Seq.get(s);

        //Compute the covariance of complex k
        double coV[][] = getCoVarMatrix(C);
        double mean[] = getMean(C);
        //convert to matrix
        Matrix MatrixCoV = new Matrix(coV);
        CholeskyDecomposition sqrtCoV = new CholeskyDecomposition(MatrixCoV);

        /*	if ( sqrtCoV.isSPD() == false)
        getModel().getRuntime().sendInfoMsg("Covarianzmatrix NICHT Cholesky - Zerlegbar. Das ist sehr schlecht und sollte nicht passieren!!");*/
        //Step 4b, compute new candidate point
        double tmpArray[] = new double[n];

        int critical_counter = 0;

        do {
            //forget it!!
            if (critical_counter++ > 100) {
                return;
            }

            Matrix ru = new Matrix(randn(n), n);
            //generate new point
            offspring = sqrtCoV.getL().times(ru).getColumnPackedCopy();
            for (int i = 0; i < n; i++) {
                offspring[i] += b[i];
                if (Double.isNaN(offspring[i])) {
                    System.out.println("Fehler -> Parameter ist NaN!!");
                }
            }
            for (int i = 0; i < n; i++) {
                tmpArray[i] = (offspring[i]);
            }
        } while (!isSampleValid(tmpArray));

        //Step 4c, compute posterior probability
        double newgen[] = ComputeDensity(offspring);

        if ((newgen[m + n] = IsPointNonDominated(C, newgen)) != -1) {
            newgen[m + n] /= (double) C.length;
        } else {
            newgen[m + n] = ComputeFitnessOfDominatedPoint(C, newgen);
        }

        double fitness1 = b[n + m];
        double fitness2 = newgen[n + m];

        double ratio = (double) (fitness1) / (double) (fitness2);

        ratio = Math.pow(ratio, 0.5 * (double) (fitness2));

        //METROPOLIS HASTINGS selection step
        double Z = generator.nextDouble();

        System.out.println("newgen:");

        for (int j = 0; j < n; j++) {
            log(":" + newgen[j] + " ,");
        }
        for (int j = 0; j < m; j++) {
            log(":" + newgen[n + j] + " ,");
        }
        log("Fitness" + ":" + newgen[n + m]);

        if (Z < ratio) {
            Seq.add(newgen);

            for (int i = 0; i < n + m + 1; i++) {
                C[m - 1][i] = newgen[i];
            }
        }

    }

    public void reshuffle(double C[][][], double D[][]) {
        int counter = 0;

        for (int qq = 0; qq < q; qq++) {
            for (int ii = 0; ii < m; ii++) {
                for (int j = 0; j < n + m + 1; j++) {
                    D[counter][j] = C[qq][ii][j];
                }
                counter++;
            }
        }
        ComputeFitness(D);
        //D sortieren...
        sort(D, n + m, false);


        System.out.println("Current D:");
        for (int i = 0; i < D.length; i++) {
            for (int j = 0; j < n; j++) {
                log( ":" + D[i][j] + " ,");
            }
            for (int j = 0; j < m; j++) {
                
                    log(":" + D[i][n + j] + " ,");
            }
            log("Fitness" + ":" + D[i][n + m]);

        }
    }

    /*    public double[] gelman(double Sequences[][][] ) {
    int npts = Sequences.length;
    int nparp1 = Sequences[0].length;
    int mseq = Sequences[0][0].length;
    int nstart = 1;
    //Check the convergence on the the last 50% of the sequence
    if (npts > 10) {
    nstart = npts-(npts/2)+1;
    }
    int ntst = npts - nstart;
    //Calculate convergence parameter for the sequence of each parameter
    int NPar = nparp1-1;

    for (int pp=0;pp<=NPar; pp++) {
    double TestArr[][] = new double[ntst][mseq];
    for (int i=nstart;i<npts;i++) {
    for (int j=0;j<mseq;j++) {
    TestArr[i][j] = Sequences[i][pp][j];
    }
    }
    int n = ntst;
    int m = mseq;

    double mutot = 0;
    for (int i=0;i<n;i++) {
    for (int j=0;j<m;j++) {
    mutot += TestArr[i][j];
    }
    }
    mutot /= ntst*mseq;

    double museq[] = new double[m];
    java.util.Arrays.fill(museq,0);
    for (int i=0;i<n;i++) {
    for (int j=0;j<m;j++) {
    museq[j] += TestArr[i][j];
    }
    }
    double B = 0;
    for (int j=0;j<m;j++) {
    B += (museq[j] - mutot)*(museq[j] - mutot);
    }
    B *= n;
    B /= (m-1);

    double varseq[] = new double[m];
    java.util.Arrays.fill(varseq,0);

    for (int i=0;i<n;i++) {
    for (int j=0;j<m;j++) {
    varseq[j] += (TestArr[i][j] - museq[j])*(TestArr[i][j] - museq[j]);
    }
    }

    double W = 0;
    for (int j=0;j<m;j++) {
    W += (varseq[j] /= (n - 1));
    }
    W /= m;

    double varhat = ((double)(n - 1)/(double)(n))*W + B / n;


    }
    }*/
    public double ComputeFitnessOfDominatedPoint(double D[][], double Point[]) {
        Point[m + n] = 1.00001;

        for (int j = 0; j < D.length; j++) {
            if (D[j] == Point) {
                continue;
            }

            if (D[j][m + n] > 1 || D[j][m + n] == -1) {
                continue;
            }

            boolean j_DominatesPoint = true;

            for (int k = n; k < n + m; k++) {
                if (Point[k] > D[j][k]) {
                    j_DominatesPoint = false;
                }
            }
            if (j_DominatesPoint) {
                Point[m + n] += D[j][m + n];
            }
        }
        return Point[m + n];
    }

    public int IsPointNonDominated(double D[][], double Point[]) {
        int domCount = 0;
        boolean PointIsDominated = false;

        for (int j = 0; j < D.length; j++) {
            if (D[j] == Point) {
                continue;
            }

            boolean j_DominatesPoint = true;
            boolean PointDominates_j = true;

            for (int k = n; k < n + m; k++) {
                if (Point[k] > D[j][k]) {
                    j_DominatesPoint = false;
                }
                if (Point[k] < D[j][k]) {
                    PointDominates_j = false;
                }
            }
            if (j_DominatesPoint) {
                PointIsDominated = true;
                break;
            }
            if (PointDominates_j) {
                domCount++;
            }
        }

        if (PointIsDominated) {
            return -1;
        }
        return domCount;
    }

    public void ComputeFitness(double D[][]) {
        for (int i = 0; i < D.length; i++) {
            D[i][m + n] = -1;
        }

        //determine nondominated points
        for (int i = 0; i < D.length; i++) {
            int numOfDominatedPoints = IsPointNonDominated(D, D[i]);

            if (numOfDominatedPoints != -1) {
                D[i][m + n] = (double) numOfDominatedPoints / (double) (D.length);
            }
        }

        for (int i = 0; i < D.length; i++) {
            if (D[i][m + n] == -1) {
                D[i][m + n] = ComputeFitnessOfDominatedPoint(D, D[i]);
            }
        }

        for (int i = 0; i < D.length; i++) {
            if (D[i][m + n] == -1) {
                D[i][m + n] = 1.0;
            }
        }
    }

    public void procedure() throws SampleLimitException, ObjectiveAchievedException {

        q = (int)getComplexes();
        s = (int)getPopulation();

        s = (s / q) * q;

        m = s / q;
        L = Math.max(1, m / 5);

        double D[][] = new double[s][n + m + 1];

        ArrayList<ArrayList<double[]>> Sequences = new ArrayList<ArrayList<double[]>>();
        double x[][] = new double[s][n];
        //Step 2a. Sample s points in the parameter space
        x = latin(s, n);

        // Calculate densities pset associated with the sampled parameter set
        // pset will be an array with the probability in the first column and the index of
        // the sample in the second column;

        D = ComputeDensity(x);
        ComputeFitness(D);

        // Step 3: Sort the points in order of increasing fitness
        sort(D, m + n, false);

        // Step 4: Initialize starting points of sequences which the Metropolis Hastings algorithm will use...
        for (int i = 0; i < q; i++) {
            ArrayList<double[]> Sequence_k = new ArrayList<double[]>();
            double extX[] = new double[n + m + 1];

            for (int j = 0; j < n + m + 1; j++) {
                extX[j] = D[i][j];
            }
            Sequence_k.add(extX);
            Sequences.add(Sequence_k);
        }

        // Iterate until the Gelman-Rubin convergence criterium is satisfied...
        int converged = -1, convergence = 2;
        int iter = 0;

        while (iter < getMaxn()) {
            double C[][][] = PartComplexes(D);

            SEM(D, C, Sequences, x);

            reshuffle(C, D);

            iter++;
            //double convergence[] = gelman(Sequences);
        }
    }
}
