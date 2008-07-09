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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import org.unijena.jams.data.*;
import org.unijena.jams.io.GenericDataWriter;
import org.unijena.jams.model.*;
import java.util.Arrays.*;
import java.util.StringTokenizer;
import org.unijena.jams.runtime.StandardRuntime;

/**
 *
 * @author Christian Fischer, based on the original MatLab sources
 */
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
        public class SimpleSCE extends Optimizer {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSString parameterIDs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter value bounaries corresponding to parameter identifiers"
            )
            public JAMSString boundaries;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don´t specify A and B the unconstrained problem will be solved"
            )
            public JAMSString LinearConstraintMatrixA;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "A and B specify linear constraints, so that for every x the condition Ax = B is satisfied. if you don´t specify A and B the unconstrained problem will be solved"
            )
            public JAMSString LinearConstraintVectorB;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "efficiency methods"
            )
            public JAMSString effMethodName;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble effValue;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization mode, 1 - minimization, 2 - maximization, 3 - max |f(x)|, 4 - min |f(x)|"
            )
            public JAMSInteger mode;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "optimization parameter: number of complex, a common value for this is 2 or 3"
            )
            public JAMSInteger NumberOfComplexes;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling/disabling this sampler"
            )
            public JAMSBoolean enable;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Data file directory name"
            )
            public JAMSString dirName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Maximum number of function evaluations"
            )
            public JAMSInteger maxn;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString sceFileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "if you dont want to execute the jams model completly in every iteration, you can specify a JAMS - Snapshot which is loaded before execution"
            )
            public JAMSEntity snapshot;
                
    GenericDataWriter writer = null;
            
    int N; //parameter dimension
    int p; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    
    PatternSearch SearchMethod = null;
    Matrix LinearConstraints_A = null, LinearConstraints_b = null;
    
    public void init() {
        if (!enable.getValue())
            return;
        super.init(parameterIDs.getValue(), boundaries.getValue(), dirName.getValue(), effValue, this.mode.getValue(),snapshot);
                    
        if (LinearConstraintMatrixA != null && LinearConstraintVectorB != null){
            StringTokenizer tok = new StringTokenizer(LinearConstraintMatrixA.getValue(),";");
        
            int n = tok.countTokens(),m = -1;
            LinearConstraints_A = null;
            for (int i=0;i<n;i++){
                StringTokenizer line_tok = new StringTokenizer(tok.nextToken(),",");
                if (m == -1){
                    m = line_tok.countTokens();
                    LinearConstraints_A = new Matrix(n,m);
                }
                if (m != line_tok.countTokens()){
                    this.getModel().getRuntime().sendHalt("SimpleSCE: Linear Constraint Matrix, dimension mismatch!");
                }
                for (int j=0;j<m;j++){
                    String number = line_tok.nextToken();
                    double value = 0.0;
                    try{
                        value = Double.parseDouble(number);
                    }catch(NumberFormatException e){
                        this.getModel().getRuntime().sendHalt("SimpleSCE: Can´t read Linear Constraint Matrix, because there are unparseable elements:" + e.toString());
                    }
                    LinearConstraints_A.set(i, j, value);
                }
            }
                
            tok = new StringTokenizer(LinearConstraintVectorB.getValue(),";");
            LinearConstraints_b = new Matrix(n,1);
            n = tok.countTokens();
            for (int i=0;i<n;i++){
                String number = tok.nextToken();
                double value = 0.0;
                try{
                    value = Double.parseDouble(number);
                }catch(NumberFormatException e){
                    this.getModel().getRuntime().sendHalt("SimpleSCE: Can´t read Linear Constraint Matrix, because there are unparseable elements:" + e.toString());
                }
                LinearConstraints_b.set(i, 0, value);
            }
        }
        
        if (LinearConstraints_A != null && LinearConstraints_b != null){
            if (LinearConstraints_A.getRowDimension() != LinearConstraints_b.getRowDimension()){
                this.getModel().getRuntime().sendHalt("SimpleSCE: LinearConstraintMatrixA must have the same number of rows as LinearConstraintVectorB");
            }
            if (LinearConstraints_A.getColumnDimension() != n){
                this.getModel().getRuntime().sendHalt("SimpleSCE: LinearConstraintMatrixA must have the same number of columns as thera are parameters");
            }
        }
        //initialising output file
        writer = new GenericDataWriter(dirName.getValue()+"/"+sceFileName.getValue());
        writer.addComment("SCE output");
        for(int p = 0; p < this.parameterNames.length; p++){
            writer.addColumn(this.parameterNames[p]);
        }
        writer.addColumn(this.effMethodName.getValue());
        writer.writeHeader();
        writer.flush();            
    }
    public double Custom_rand() {
        return this.generator.nextDouble();
    }
              
    private boolean IsSampleValid(double[] sample) {
        JAMSDouble conv_sample[] = new JAMSDouble[sample.length];
        for (int i = 0;i<sample.length;i++) {
            conv_sample[i] = new JAMSDouble(sample[i]);
        }
        return IsSampleValid(conv_sample);
    }
    private boolean IsSampleValid(JAMSDouble [] sample) {
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
    public void sort(double x[][],double xf[]) {
        if (x.length == 0)
            return;
        int n = x[0].length;
        double t[][] = new double[x.length][n+1];
        for (int i=0;i<x.length;i++) {
            for (int j=0;j<n;j++) {
                t[i][j] = x[i][j];
            }
            t[i][n] = xf[i];
        }
        
        SCE_Comparator comparator = new SCE_Comparator(n,false);
        java.util.Arrays.sort(t,comparator);
        
        for (int i=0;i<x.length;i++) {
            for (int j=0;j<n;j++) {
                x[i][j] = t[i][j];
            }
            xf[i] = t[i][n];
        }
    }
    
    public void sort(int x[]) {
        Arrays.sort(x);
    }
    
    
    public double NormalizedgeometricRange(double x[][],double bound[]) {
        if (x.length == 0)
            return 0;
        int n = x[0].length;
        
        double min[] = new double[n];
        double max[] = new double[n];
        
        double mean = 0;
        
        for (int i=0;i<n;i++) {
            min[i] = Double.POSITIVE_INFINITY;
            max[i] = Double.NEGATIVE_INFINITY;
            
            for (int j=0;j<x.length;j++) {
                if (x[j][i] < min[i])
                    min[i] = x[j][i];
                if (x[j][i] > max[i])
                    max[i] = x[j][i];
            }
            
            mean += Math.log(max[i] - min[i])/bound[i];
        }
        mean/=n;
        return Math.exp(mean);
    }
    
    public double[] std(double x[][]) {
        if (x.length == 0)
            return null;
        if (x.length == 1)
            return null;
        
        int n = x[0].length;
        
        double mean[] = new double[n];
        double var[] = new double[n];
        
        for (int i=0;i<n;i++) {
            mean[i] = 0;
            for (int j=0;j<x.length;j++) {
                mean[i] += x[j][i];
            }
            mean[i] /= n;
        }
        
        for (int i=0;i<n;i++) {
            var[i] = 0;
            for (int j=0;j<x.length;j++) {
                var[i] += (mean[i] - x[j][i])*(mean[i] - x[j][i]);
            }
            var[i] = Math.sqrt(var[i])/(n-1);
        }
        
        return var;
    }
    
    public int find(int lcs[],int startindex,int endindex,int value) {
        for (int i=startindex;i<endindex;i++) {
            if (lcs[i] == value)
                return i;
        }
        return -1;
    }
    
    class SCEFunctionEvaluator extends AbstractFunction{
        SimpleSCE myOptimizer = null;
        
        SCEFunctionEvaluator(SimpleSCE mySCE){
            myOptimizer = mySCE;
        }
        
        public double f(double x[]){            
            return myOptimizer.funct(x);
        }
    }
    
    //s forms the simplex
    //sf function values of simplex
    //bl lower bound, bu upper bound
    public double[] cceua2( double s[][],double sf[],double bl[],double bu[]) {
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
	double fw = sf[n-1];
	
	for (int i=0;i<nopt;i++) {
	    sb[i] = s[0][i];
	    sw[i] = s[n-1][i];
	}
	
	// Compute the centroid of the simplex excluding the worst point:
	double ce[] = new double[nopt];
	for (int i=0;i<nopt;i++) {
	    ce[i] = 0;
	    for (int j=0;j<n-1;j++) {
		ce[i] += s[j][i];
	    }
	    ce[i] /= (n-1);
	}

	// Attempt a reflection point
	double snew[] = new double[nopt];
	for (int i=0;i<nopt;i++) {
	    snew[i] = ce[i] + alpha*(ce[i]-sw[i]);
	}
	
	// Check if is outside the bounds:
	int ibound=0;
	for (int i=0;i<nopt;i++) {
	    if ( (snew[i]-bl[i]) < 0 ) 
		ibound = 1;
	    if ( (bu[i]-snew[i]) < 0 ) 
		ibound = 2;
	}
	
	if (ibound >=1) {
	    snew = this.RandomSampler();
	}
	    
	double fnew = funct(snew);
	
	// Reflection failed; now attempt a contraction point:
	if (fnew > fw) {
	    for (int i=0;i<nopt;i++) {
		snew[i] = sw[i] + beta*(ce[i]-sw[i]);		
	    }
	    fnew = funct(snew);
	}
	// Both reflection and contraction have failed, attempt a random point;
	if (fnew > fw) {
	    snew = this.RandomSampler();
	    fnew = funct(snew);
	}
    
	double result[] = new double[nopt+1];
	for (int i=0;i<nopt;i++) {
	    result[i] = snew[i];
	}
	result[nopt] = fnew;
	return result;
    }
    
    public double[] cceua( double s[][],double sf[],double bl[],double bu[]) {
        SCEFunctionEvaluator eval = new SCEFunctionEvaluator(this);
        
        Sample[] Simplex = new Sample[s.length];
        
        for (int i=0;i<s.length;i++){
            Simplex[i] = new Sample(s[i],sf[i]);
        }
        
        //convert boundary constraints to linear constraints
        Matrix A = new Matrix(bl.length+bu.length,n);
        Matrix b = new Matrix(bl.length+bu.length,1);
        
        for (int i=0;i<bl.length;i++){
            for (int j=0;j<n;j++){
                if (i == j){
                    A.set(i, j,  -1);
                    A.set(i+n, j, 1);
                }
                else{
                    A.set(i, j,  0);
                    A.set(i+n, j,  0);
                }
            }
            b.set(i, 0,  -bl[i]);
            b.set(i+n, 0, bu[i]);
        }
        
        Sample nextSample = SearchMethod.step(eval, Simplex, A, b, lowBound, upBound);
        
        double result[] = new double[n+1];
        for (int i=0;i<n;i++){
            result[i] = nextSample.x[i];
        }
        result[n] = nextSample.fx;
        return result;               
    }
    
    public double[] sceua(double[] x0,double[] bl,double []bu,int maxn,int kstop,double pcento,double peps,int ngs,int iseed,int iniflg) {
        int method = 1;
        
        SearchMethod = null;
        if (method == 1)
            SearchMethod = new NelderMead();
        else if (method == 2)
            SearchMethod = new ImplicitFiltering();
        else if (method == 3)
            SearchMethod = new MDS();
                
        try {
            int nopt = x0.length;
            int npg = 2*nopt+1;
            int nps = nopt+1;
            int nspl = npg;
            int mings = ngs;
            int npt = npg*ngs;
            
            double bound[] = new double[nopt];
            for (int i=0;i<nopt;i++) {
                bound[i] = bu[i] - bl[i];
            }
            
            // Create an initial population to fill array x(npt,nopt):
            //this.generator.setSeed(iseed);
            
            double x[][] = new double[npt][nopt];
            
            for (int i=0;i<npt;i++) {
                x[i] = this.RandomSampler();
            }
            
            if (iniflg==1) {
                x[0] = x0;
            }
            
            int nloop=0;
            
            double xf[] = new double[npt];
            for (int i=0;i<npt;i++) {
                xf[i] = funct(x[i]);
            }
            double f0 = xf[0];
            
            // Sort the population in order of increasing function values;
            sort(x,xf);
            
            // Record the best and worst points;
            double bestx[] = new double[nopt];
            double worstx[] = new double[nopt];
            double bestf,worstf;
            for (int i=0;i<nopt;i++) {
                bestx[i] = x[0][i];
                worstx[i] = x[npt-1][i];
            }
            bestf = xf[0];
            worstf = xf[npt-1];
            
            // Compute the standard deviation for each parameter
            double xnstd[] = std(x);
            
            // Computes the normalized geometric range of the parameters
            double gnrng = NormalizedgeometricRange(x,bound); //exp(mean(log((max(x)-min(x))./bound)));
            
            System.out.println("The Inital Loop: 0");
            System.out.println("BestF: " + bestf);
            System.out.print("BestX");
            
            for (int i=0;i<nopt;i++) {
                System.out.print("\t\t" + bestx[i]);
            }
            
            System.out.println("");
            System.out.println("WorstF: " + worstf);
            System.out.print("WorstX");
            
            for (int i=0;i<nopt;i++) {
                System.out.print("\t\t" + worstx[i]);                
            }
            System.out.println("");
                
            if (writer!=null){                       
                for (int i=0;i<nopt;i++) {
                    writer.addData(bestx[i]);
                }
                writer.addData(bestf);
                writer.writeData();
                writer.flush();
                                                    
                writer.writeLine("");
                writer.flush();
            }
            //Check for convergency;
            if (currentSampleCount >= maxn) {
                System.out.println("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
                System.out.println("ON THE MAXIMUM NUMBER OF TRIALS" + maxn);
                System.out.println("HAS BEEN EXCEEDED.  SEARCH WAS STOPPED AT TRIAL NUMBER:" + currentSampleCount);
                System.out.println("OF THE INITIAL LOOP!");
                if (writer!=null){
                    writer.writeLine("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
                    writer.writeLine("ON THE MAXIMUM NUMBER OF TRIALS" + maxn);
                    writer.writeLine("HAS BEEN EXCEEDED.  SEARCH WAS STOPPED AT TRIAL NUMBER:" + currentSampleCount);
                    writer.writeLine("OF THE INITIAL LOOP!");
                    writer.flush();
                }
            }
            
            if (gnrng < peps) {
                if (writer!=null){
                    writer.writeLine("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");
                    writer.flush();
                }
                System.out.println("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");                
            }
            
            // Begin evolution loops:
            nloop = 0;
            double criter[] =new double[kstop];
            double criter_change=100000;
            
            while (currentSampleCount<maxn && gnrng>peps && criter_change>pcento) {
                nloop++;
                
                // Loop on complexes (sub-populations);
                for (int igs=0;igs<ngs;igs++) {
                    
                    // Partition the population into complexes (sub-populations);
                    int k1[] = new int[npg];
                    int k2[] = new int[npg];
                    
                    for (int i=0;i<npg;i++) {
                        k1[i] = i;
                        k2[i] = k1[i]*ngs+igs;
                    }
                    double cx[][] = new double[npg][nopt];
                    double cf[] = new double[npg];
                    for (int i=0;i<npg;i++) {
                        for (int j=0;j<nopt;j++) {
                            cx[k1[i]][j] = x[k2[i]][j];
                        }
                        cf[k1[i]] = xf[k2[i]];
                    }
                    
                    //Evolve sub-population igs for nspl steps:
                    for (int loop=0;loop<nspl;loop++) {
                        // Select simplex by sampling the complex according to a linear
                        // probability distribution
                        int lcs[] = new int[nps];
                        lcs[0] = 0;
                        for (int k3=1;k3<nps;k3++) {
                            int lpos = 0;
                            for (int iter=0;iter<1000;iter++) {
                                lpos = (int)Math.floor(npg+0.5-Math.sqrt((npg+0.5)*(npg+0.5) - npg*(npg+1)*Custom_rand()));
                                //wirklich nÃ¶tig??
                                int idx = find(lcs,0,k3,lpos);
                                if (idx == -1) {
                                    break;
                                }
                            }
                            lcs[k3] = lpos;
                        }
                        sort(lcs);
                        
                        // Construct the simplex:
                        double s[][] = new double[nps][nopt];
                        double sf[]  = new double[nps];
                        for (int i=0;i<nps;i++) {
                            for (int j=0;j<nopt;j++) {
                                s[i][j] = cx[lcs[i]][j];
                            }
                            sf[i] = cf[lcs[i]];
                        }
                        
                        double snew[] = new double[nopt];
                        double fnew;
                        
                        double xnew[] = cceua(s,sf,bl,bu);
                        //double xnew[] = cceua2(s,sf,bl,bu);
                        
                        //System.out.println(xnew2[0]);
                        
                        //icall aktualisieren!!!
                        
                        for (int i=0;i<nopt;i++) {
                            snew[i] = xnew[i];
                        }
                        fnew = xnew[nopt];
                        
                        // Replace the worst point in Simplex with the new point:
                        s[nps-1] = snew;
                        sf[nps-1] = fnew;
                        
                        //Replace the simplex into the complex;
                        for (int i=0;i<nps;i++) {
                            for (int j=0;j<nopt;j++) {
                                cx[lcs[i]][j] = s[i][j];
                            }
                            cf[lcs[i]] = sf[i];
                        }
                        
                        // Sort the complex;
                        sort(cx,cf);
                        // End of Inner Loop for Competitive Evolution of Simplexes
                    }
                    // Replace the complex back into the population;
                    for (int i=0;i<npg;i++) {
                        for (int j=0;j<nopt;j++) {
                            x[k2[i]][j] = cx[k1[i]][j];
                        }
                        xf[k2[i]] = cf[k1[i]];
                    }
                    // End of Loop on Complex Evolution;
                }
                // Shuffled the complexes;
                sort(x,xf);
                
                // Record the best and worst points;
                for (int i=0;i<nopt;i++) {
                    bestx[i] = x[0][i];
                    worstx[i] = x[nopt-1][i];
                }
                bestf = xf[0];
                worstf = xf[npt-1];
                
                //Compute the standard deviation for each parameter
                xnstd = std(x);
                
                gnrng = NormalizedgeometricRange(x,bound);
                
                System.out.println("Evolution Loop:" + nloop + " - Trial - " + currentSampleCount);
                System.out.println("BESTF:" + bestf);
                System.out.print("BESTX:");
                
                for (int i = 0;i<nopt;i++) {
                    System.out.print("\t" + bestx[i]);                    
                }
                
                System.out.println("\nWORSTF:" + worstf);
                System.out.print("WORSTX:");                
                for (int i = 0;i<nopt;i++) {
                    System.out.print("\t" + worstx[i]);                    
                }
                System.out.println("");
                
                if (writer!=null){
                    writer.writeLine("Evolution Loop:" + nloop + " - Trial - " + currentSampleCount);
                    writer.writeLine("BESTF:" + bestf);
                    writer.writeLine("BESTX:");
                    
                    for (int i=0;i<nopt;i++) {                    
                        writer.addData(bestx[i]);
                    }
                    writer.addData(bestf);
                    writer.writeData();
                    writer.flush();
                }                                                                                                                                
                                                                
                // Check for convergency;
                if (currentSampleCount >= maxn) {
                    System.out.println("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
                    System.out.println("ON THE MAXIMUM NUMBER OF TRIALS " +  maxn + " HAS BEEN EXCEEDED!");
                    if (writer!=null){
                        writer.writeLine("*** OPTIMIZATION SEARCH TERMINATED BECAUSE THE LIMIT");
                        writer.writeLine("ON THE MAXIMUM NUMBER OF TRIALS " +  maxn + " HAS BEEN EXCEEDED!");
                        writer.flush();
                    }
                }
                if (gnrng < peps) {
                    System.out.println("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");
                    if (writer!=null){
                        writer.writeLine("THE POPULATION HAS CONVERGED TO A PRESPECIFIED SMALL PARAMETER SPACE");
                        writer.flush();
                    }
                }
                
                for (int i=0;i<kstop-1;i++) {
                    criter[i] = criter[i+1];
                }
                criter[kstop-1] = bestf;
                if (nloop >= kstop) {
                    criter_change=Math.abs(criter[0]-criter[kstop-1])*100.0;
                    double criter_mean = 0;
                    for (int i=0;i<kstop;i++) {
                        criter_mean += Math.abs(criter[i]);
                    }
                    criter_mean /= kstop;
                    criter_change /= criter_mean;
                    
                    if (criter_change < pcento) {
                        System.out.println("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY");
                        System.out.println("LESS THAN THE THRESHOLD " + pcento + "%");
                        System.out.println("CONVERGENCY HAS ACHIEVED BASED ON OBJECTIVE FUNCTION CRITERIA!!!");
                        
                        if (writer!=null){
                            writer.writeLine("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY");
                            writer.writeLine("LESS THAN THE THRESHOLD " + pcento + "%");
                            writer.writeLine("CONVERGENCY HAS ACHIEVED BASED ON OBJECTIVE FUNCTION CRITERIA!!!");
                            writer.flush();
                        }
                    }
                }
            }
            System.out.println("SEARCH WAS STOPPED AT TRIAL NUMBER: " + currentSampleCount);
            System.out.println("NORMALIZED GEOMETRIC RANGE = " + gnrng);
            System.out.println("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY " + criter_change + "%");
            
            if (writer!=null){
                writer.writeLine("SEARCH WAS STOPPED AT TRIAL NUMBER: " + currentSampleCount);
                writer.writeLine("NORMALIZED GEOMETRIC RANGE = " + gnrng);
                writer.writeLine("THE BEST POINT HAS IMPROVED IN LAST " + kstop + " LOOPS BY " + criter_change + "%");
                writer.flush();
            }
            double result[] = new double[bestx.length+1];
            for (int i=0;i<bestx.length;i++)
                result[i] = bestx[i];
            result[bestx.length] = bestf;
            return result;
        } catch (org.unijena.jams.runtime.RuntimeException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    public void run() {
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        if(!enable.getValue()){
            singleRun();
            return;
        }
        
        int maxn=10000;
        int kstop=10;
        double  pcento=0.1;
        double peps=0.00001;
        int iseed=10;
        int iniflg=0;
        
        double bestpoint[],bestx[],bestf;
        
        double x0[] = RandomSampler();
        
        //double x0[] = {-1.295,2.659,1.1,0.1649};
        
        int iNumberOfComplexes = 0;
        if (NumberOfComplexes != null)
            iNumberOfComplexes = NumberOfComplexes.getValue();
        
        if (iNumberOfComplexes < 0){
            this.getModel().getRuntime().sendErrorMsg("NumberofComplexes: value not specified or out of bounds, set to default value");
            iNumberOfComplexes = 2;
        }
        if (this.maxn != null){
            maxn = this.maxn.getValue();
        }
        bestpoint = sceua(x0,this.lowBound,this.upBound,maxn,kstop,pcento,peps,iNumberOfComplexes,iseed,iniflg);
       
        bestx = new double[this.parameters.length];
        for (int i=0;i<this.parameters.length;i++) {
            bestx[i] = bestpoint[i];
        }
        bestf = bestpoint[this.parameters.length];
    }
    
    public double[] offlineRun(double[] start,double lowBound[],double upBound[],int NumberOfComplexes,int MaximizeEff,int maxn,int kstop,double pcento,double peps,Optimizer.AbstractFunction destFunction){
        int iseed=10;
        int iniflg=0;        
        double bestpoint[],bestx[],bestf;
        this.lowBound = lowBound;
        this.upBound = upBound;
        this.N = lowBound.length;
        this.n = this.N;
        this.mode = new JAMSInteger(MaximizeEff);
                                        
        double x0[] = RandomSampler();
        if (start != null)
            x0 = start;
        
        this.GoalFunction = destFunction;
        
        bestpoint = sceua(x0,lowBound,upBound,maxn,kstop,pcento,peps,NumberOfComplexes,iseed,iniflg);
        
        return bestpoint;
    }
    
    public void cleanup() {
        
        if (enable.getValue()) {
            if (writer!=null){
                writer.close();
            }
        }
    }
}