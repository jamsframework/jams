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

package jams.components.optimizer;

import java.util.Random;
import java.util.Vector;

import java.util.StringTokenizer;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;
import Jama.*;
import java.util.Comparator;
/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
        public class SCEM_UA extends JAMSContext {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.String parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public Attribute.String boundaries;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Number of samples to be taken"
            )
            public Attribute.Integer sampleCount;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "efficiency methods"
            )
            public Attribute.String effMethodNames;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "efficiency values"
            )
            public Attribute.Double[] effValues;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "optimization mode, 1 - minimization, 2 - maximization, 3 - max |f(x)|, 4 - min |f(x)|"
            )
            public Attribute.IntegerArray mode;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "number of points in population, common values for are in the range [10;100]"
            )
            public Attribute.Integer Population;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "optimization parameter: number of complex, a common value for this is 2 or 3"
            )
            public Attribute.Integer Complexes;
            
    public class SCE_Comparator implements Comparator {
        private int col = 0;
        private int order = 1;
    
        public SCE_Comparator(int col,boolean decreasing_order) {
            this.col = col;
            if (decreasing_order)
                order = -1;
            else
                order = 1;
        }

        public int compare(Object d1, Object d2) {

            double[] b1 = (double [])d1;
            double[] b2 = (double [])d2;
        
            if (b1[col] < b2[col])
                return -1*order;
            else if (b1[col] == b2[col])
                return 0*order;
            else
                return 1*order;
        }        
    } 
    
    Attribute.Double[] parameters;
    String[] parameterNames;
    String[] effNames;
    double[] lowBound;
    double[] upBound;
    
    int currentCount;
    Random generator;
    
    GenericDataWriter writer;
    
    int N; //parameter dimension
    int M;
    int q; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    int L; //number of offspring generated per iteration
    double gamma; //Kurtosis parameter Bayesian Inference Scheme (Thiemann et al., 2001)
    double ndraw; //Number of accepted draws to infer posterior distribution on (?)
    
    public void init() {
//generalise this!!
        
        
//add more checks!!!
        //retreiving parameter names
        int i;
        StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
        String key;
        parameters = new Attribute.Double[tok.countTokens()];
        parameterNames = new String[tok.countTokens()];
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            parameterNames[i] = key;
            parameters[i] = (Attribute.Double) getModel().getRuntime().getDataHandles().get(key);
            i++;
        }
        
        setEntities((Attribute.EntityCollection)getModel().getRuntime().getDataHandles().get("hrus"));
        
        //retreiving boundaries
        tok = new StringTokenizer(boundaries.getValue(), ";");
        int n = tok.countTokens();
        lowBound = new double[n];
        upBound = new double[n];
        
        //check if number of parameter ids and boundaries match
        if (n != i) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": Different number of parameterIDs and boundaries!");
        }
        
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            key = key.substring(1, key.length()-1);
            
            StringTokenizer boundTok = new StringTokenizer(key, ">");
            lowBound[i] = Double.parseDouble(boundTok.nextToken());
            upBound[i] = Double.parseDouble(boundTok.nextToken());
            
            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": upBound must be higher than lowBound!");
            }
            
            i++;
        }
        
        //retreiving effMethodNames
        i = 0;
        tok = new StringTokenizer(effMethodNames.getValue(), ";");
        effNames = new String[tok.countTokens()];
        i = 0;
        while (tok.hasMoreTokens()) {
            key = tok.nextToken();
            effNames[i] = key;
            i++;
        }
        M = effNames.length;
    }
    
    private double[] RandomSampler(){
        int paras = this.parameterNames.length;
        double[] sample = new double[paras];
        
        for(int i = 0; i < paras; i++){
            double d = generator.nextDouble();
            
            sample[i] = (lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        return sample;
    }
    
    private void resetValues() {
        //set parameter values to initial values corresponding to their boundaries
        generator = new Random(System.currentTimeMillis());
        for (int i = 0; i < parameters.length; i++) {
            double d = generator.nextDouble();
            parameters[i].setValue(lowBound[i] + d * (upBound[i]-lowBound[i]));
        }
        currentCount = 0;
    }
    
    private void singleRun() {
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            Component comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    private boolean IsSampleValid(Attribute.Double [] sample) {
        int paras = this.parameterNames.length;
        boolean criticalPara = false;
        double criticalParaValue = 0;
        
        for(int i = 0; i < paras; i++){
            if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i] )
                return false;
        }
        return true;
    }
    
    @SuppressWarnings("unchecked") 
    public void sort(double data[][],int col,boolean decreasing_order) {
        SCE_Comparator comparator = new SCE_Comparator(col,decreasing_order);
        java.util.Arrays.sort(data,comparator);
    }
    
    public double[] getMean(double data[][]) {	    //no error here :)
        double[] mean = new double[N];
        
        java.util.Arrays.fill(mean,0);
        
        for (int j=0;j<N;j++) {
            for (int i=0;i<data.length;i++) {
                mean[j] += data[i][j];
            }
            if (data.length == 0)
                getModel().getRuntime().sendInfoMsg("Complex in SCEM_UA ist leer! - Kritischer Fehler!!");
            
            mean[j] /= data.length;
        }
        return mean;
    }
    
    public double[][] getCoVarMatrix(double data[][]) { //no error here :)
        double[][] coV = new double[N][N];
        double[] mean = getMean(data);
        double vecLength = data.length;
        
        if (data.length == 0)
            getModel().getRuntime().sendInfoMsg("Complex in SCEM_UA hat nur die Größe von 1! - Kritischer Fehler!!");
        
        for (int i=0;i<N;i++) {
            java.util.Arrays.fill(coV[i],0);
        }
        
        for (int i=0;i<N;i++) {
            for (int j=0;j<N;j++) {
                for (int k=0;k<vecLength;k++) {
                    coV[i][j] += data[k][i]*data[k][j];
                }
                coV[i][j] -= vecLength*mean[i]*mean[j];
                coV[i][j] /= (vecLength - 1);
            }
        }
        
        return coV;
    }
    
    public double[][] latin(int s,int n) {
        double x[][] = new double[s][n];
        
        for (int i=0;i<s;i++) {
            x[i] = RandomSampler();
        }
        return x;
    }
    
    public double[] ComputeDensity(double x[]) {
        double pset[] = new double[N+M+1];
        
        for (int j=0;j<x.length;j++) {
            parameters[j].setValue(x[j]);
            pset[j] = x[j];
        }
        
        singleRun();
        
        for (int i=0;i<M;i++) {
            if (mode.getValue()[i] == Optimizer.MODE_MINIMIZATION)
                pset[N+i] = this.effValues[i].getValue();
            else if (mode.getValue()[i] == Optimizer.MODE_ABSMINIMIZATION)
                pset[N+i] = Math.abs(this.effValues[i].getValue());
            else if (mode.getValue()[i] == Optimizer.MODE_ABSMAXIMIZATION)
                pset[N+i] = -Math.abs(this.effValues[i].getValue());
            else
                pset[N+i] = -this.effValues[i].getValue();
        }
        
        return pset;
    }
    
    public double[][] ComputeDensity(double x[][]) {
        int e = 0;
        double pset[][] = new double[x.length][N + M + 1];
        double tmp[];
        
        for (int i=0;i<x.length;i++) {
            tmp = ComputeDensity(x[i]);
            for (int j=0;j<N+M+1;j++) {
                pset[i][j] = tmp[j];
            }
        }
        return pset;
    }
    
    public double[][][] PartComplexes(double D[][]) {
        double C[][][] = new double[q][m][N+M+1];
        
        for (int kk=0;kk<q;kk++) {
            for (int j=0;j<m;j++) {
                int idx = q*j + kk;
                for (int i=0;i<N+M+1;i++) {
                    C[kk][j][i] = D[idx][i];
                }
            }
        }
        return C;
    }
    
    public void SEM(double D[][],double C[][][],Vector<Vector<double[]>> Sequences, double x[][]) {
        for (int kk=0;kk<q;kk++) {
            for (int bb=0;bb<L;bb++) {
                OffMetro(Sequences.get(kk),C[kk],kk,bb);
            }
        }
    }
    
    public double [] randn(int n) {
        double randn[] = new double[n];
        
        for (int i=0;i<n;i++) {
            randn[i] = generator.nextGaussian();
        }
        
        return randn;
    }
    
    //Generates offspring using METROPOLIS HASTINGS monte-carlo markov chain
    public void OffMetro(Vector<double[]> Seq, double C[][],int kk,int bb) {
        int s = Seq.size();
        
        double offspring[] = null;
        double b[];
        //draw point from sequence
        s = generator.nextInt(s);
        b = Seq.get(s);
        
        //Compute the covariance of complex k
        double coV[][] = getCoVarMatrix(C);
        double mean[]  = getMean(C);
        //convert to matrix
        Matrix MatrixCoV = new Matrix(coV);
        CholeskyDecomposition sqrtCoV = new CholeskyDecomposition(MatrixCoV);
        
/*	if ( sqrtCoV.isSPD() == false)
            getModel().getRuntime().sendInfoMsg("Covarianzmatrix NICHT Cholesky - Zerlegbar. Das ist sehr schlecht und sollte nicht passieren!!");*/
        //Step 4b, compute new candidate point
        Attribute.Double tmpArray[] = new Attribute.Double[N];
        
        int critical_counter = 0;
        
        do {
            //forget it!!
            if (critical_counter++ > 100)
                return;
            
            Matrix ru = new Matrix(randn(N),N);
            //generate new point
            offspring = sqrtCoV.getL().times(ru).getColumnPackedCopy();
            for (int i=0;i<N;i++) {
                offspring[i] += b[i];
                if (Double.isNaN(offspring[i]))
                    System.out.println("Fehler -> Parameter ist NaN!!");
            }
            for (int i=0;i<N;i++) {
                tmpArray[i] = getModel().getRuntime().getDataFactory().createDouble();
                tmpArray[i].setValue(offspring[i]);
            }
        }while (!IsSampleValid(tmpArray));
        
        //Step 4c, compute posterior probability
        double newgen[] = ComputeDensity(offspring);
        
        if ( (newgen[M+N] = IsPointNonDominated(C,newgen)) != -1)
            newgen[M+N] /= (double)C.length;
        else
            newgen[M+N] = ComputeFitnessOfDominatedPoint(C,newgen);
        
        double fitness1 = b[N+M];
        double fitness2 = newgen[N+M];
        
        double ratio = (double)(fitness1) / (double)(fitness2);
        
        ratio = Math.pow(ratio,0.5*(double)(fitness2));
        
        //METROPOLIS HASTINGS selection step
        double Z = generator.nextDouble();
        
        System.out.println("newgen:");
        
        for (int j=0;j<N;j++) {
            System.out.print(parameterNames[j] + ":" + newgen[j] + " ,");
        }
        for (int j=0;j<M;j++) {
            System.out.print(effNames[j] + ":" + newgen[N+j] + " ,");
        }
        System.out.println("Fitness" + ":" + newgen[N+M]);
        
        if (Z < ratio) {
            Seq.add(newgen);
            
            for (int i=0;i<N+M+1;i++) {
                C[m-1][i] = newgen[i];
            }
        }
        
    }
    
    public void reshuffle(double C[][][],double D[][]) {
        int counter = 0;
        
        for (int qq = 0; qq < q; qq++) {
            for (int ii = 0; ii < m; ii++) {
                for (int j = 0; j < N+M+1; j++) {
                    D[counter][j] = C[qq][ii][j];
                }
                counter++;
            }
        }
        ComputeFitness(D);
        //D sortieren...
        sort(D,N+M,false);
        
        
        System.out.println("Current D:");
        for (int i=0;i<D.length;i++) {
            for (int j=0;j<N;j++) {
                System.out.print(parameterNames[j] + ":" + D[i][j] + " ,");
            }
            for (int j=0;j<M;j++) {
                if (mode.getValue()[j] == Optimizer.MODE_MINIMIZATION || mode.getValue()[j] == Optimizer.MODE_ABSMINIMIZATION) {
                    System.out.print(effNames[j] + ":" + D[i][N+j] + " ,");
                } else {
                    double outdata = -D[i][N+j];
                    System.out.print(effNames[j] + ":" + outdata + " ,");
                }
            }
            System.out.println("Fitness" + ":" + D[i][N+M]);
            
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
    
    public double ComputeFitnessOfDominatedPoint(double D[][],double Point[]) {
        Point[M+N] = 1.00001;
        
        for (int j=0;j<D.length;j++) {
            if (D[j] == Point)
                continue;
            
            if (D[j][M+N] > 1 || D[j][M+N] == -1)
                continue;
            
            boolean j_DominatesPoint = true;
            
            for (int k=N;k<N+M;k++) {
                if (Point[k] > D[j][k]) {
                    j_DominatesPoint = false;
                }
            }
            if (j_DominatesPoint) {
                Point[M+N] += D[j][M+N];
            }
        }
        return Point[M+N];
    }
    
    public int IsPointNonDominated(double D[][],double Point[]) {
        int domCount = 0;
        boolean PointIsDominated = false;
        
        for (int j=0;j<D.length;j++) {
            if (D[j] == Point)
                continue;
            
            boolean j_DominatesPoint = true;
            boolean PointDominates_j = true;
            
            for (int k=N;k<N+M;k++) {
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
        
        if (PointIsDominated)
            return -1;
        return domCount;
    }
    
    public void ComputeFitness(double D[][]) {
        for (int i=0;i<D.length;i++) {
            D[i][M+N] = -1;
        }
        
        //determine nondominated points
        for (int i=0;i<D.length;i++) {
            int numOfDominatedPoints = IsPointNonDominated(D,D[i]);
            
            if (numOfDominatedPoints != -1) {
                D[i][M+N] = (double)numOfDominatedPoints/(double)(D.length);
            }
        }
        
        for (int i=0;i<D.length;i++) {
            if (D[i][M+N] == -1)
                D[i][M+N] = ComputeFitnessOfDominatedPoint(D,D[i]);
        }
        
        for (int i=0;i<D.length;i++) {
            if (D[i][M+N] == -1)
                D[i][M+N] = 1.0;
        }
    }
    
    public void run() {
        
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        
        resetValues();
        
        // Step 1. Complete the initialization of the process by calculating the number
        // of points in each complex, m and the number of offspring generated per iteration, L
        N = parameters.length;
        
        q = Complexes.getValue();
        s = Population.getValue();
        
        s = (s/q)*q;
        
        m = s/q;
        L = Math.max(1,m/5);
        
        double D[][] = new double[s][N+M+1];
        
        Vector<Vector<double[]>> Sequences = new Vector<Vector<double[]>>();
        double x[][] = new double[s][N];
        //Step 2a. Sample s points in the parameter space
        x = latin(s,N);
        
        // Calculate densities pset associated with the sampled parameter set
        // pset will be an array with the probability in the first column and the index of
        // the sample in the second column;
        
        D = ComputeDensity(x);
        ComputeFitness(D);
        
        // Step 3: Sort the points in order of increasing fitness
        sort(D,M+N,false);
        
        // Step 4: Initialize starting points of sequences which the Metropolis Hastings algorithm will use...
        for (int i = 0; i<q; i++) {
            Vector<double[]> Sequence_k = new Vector<double[]>();
            double extX[] = new double[N+M+1];
            
            for (int j=0;j<N+M+1;j++) {
                extX[j] = D[i][j];
            }
            Sequence_k.add(extX);
            Sequences.add(Sequence_k);
        }
        
        // Iterate until the Gelman-Rubin convergence criterium is satisfied...
        int converged = -1,convergence = 2;
        int iter = 0;
        
        while (iter < sampleCount.getValue()) {
            double C[][][] = PartComplexes(D);
            
            SEM(D,C,Sequences,x);
            
            reshuffle(C,D);
            
            iter++;
            //double convergence[] = gelman(Sequences);
        }
    }
}