/*
 * ABCGradientDescent.java
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

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.*;

import java.util.StringTokenizer;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;
import java.util.Arrays.*;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
title="Title",
        author="Author",
        description="Description"
        )
        public class MOCOM extends JAMSContext {

    //matlab zufallszahlen .. 
    int randcounter = 0;
  
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
            description = "efficiency methods"
            )
            public JAMSString effMethodNames;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "efficiency values"
            )
            public JAMSDouble[] effValues;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "maximize efficiency?"
            )
            public JAMSIntegerArray MaximizeEff;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for enabling/disabling this sampler"
            )
            public JAMSBoolean enable;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Output file name"
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "population size"
            )
            public JAMSInteger populationSize;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "maximum number of iterations"
            )
            public JAMSInteger maxIter;
    
    JAMSDouble[] parameters;
    String[] parameterNames;
    String[] effNames;
    
    double[] lowBound;
    double[] upBound;
    
    int currentCount;
    Random generator = new Random();
    
    GenericDataWriter writer;
    
    static final int MAXIMIZATION = 1;
    static final int MINIMIZATION = 2;
    static final int ABSMAXIMIZATION = 3;
    static final int ABSMINIMIZATION = 4;
    
    int N; //parameter dimension
    int M; //output dimension
    int n; //number of complexes
    int s; //population size
    int m; //complex size; floor(s/q)
    int icall=0;
    
    int MaxIter;
    
    boolean continousOutput = false;
    long start, end = 0;
    
    public void init() {
        start = System.currentTimeMillis();
        if(enable.getValue()){
            icall = 0;
//generalise this!!
            
//add more checks!!!
            //retreiving parameter names
            int i;
            StringTokenizer tok = new StringTokenizer(parameterIDs.getValue(), ";");
            String key;
            parameters = new JAMSDouble[tok.countTokens()];
            parameterNames = new String[tok.countTokens()];
            
            i = 0;
            while (tok.hasMoreTokens()) {
                key = tok.nextToken();
                parameterNames[i] = key;
                parameters[i] = (JAMSDouble) getModel().getRuntime().getDataHandles().get(key);
                i++;
            }
            
            entities = (JAMSEntityCollection)getModel().getRuntime().getDataHandles().get("hrus");
            
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
            while (tok.hasMoreTokens()) {
                key = tok.nextToken();
                effNames[i] = key;
                i++;
            }
            N = parameterNames.length;
            M = effNames.length;
            //initialising output file
            
            writer = new GenericDataWriter(getModel().getWorkspaceDirectory().getPath()+"/"+fileName.getValue());
            writer.addComment("MOCOM output start: ");
            
            for(int p = 0; p < this.parameterNames.length; p++){
                writer.addColumn(this.parameterNames[p]);
            }
            for(int p = 0; p < this.effNames.length; p++) {
                writer.addColumn(this.effNames[p]);
            }
            
            writer.writeHeader();
            writer.flush();
            
        }
        
        
    }
    public double Custom_rand() {
        randcounter++;
        //return MatlabRand[randcounter-1];	
        return this.generator.nextDouble();
    }
      
    private void singleRun() {
        icall++;
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.init();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.run();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    private void disabledRun() {
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.updateInit();
            try {
                comp.init();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            //comp.updateRun();
            try {
                comp.run();
            } catch (Exception e) {
                
            }
        }
        
        runEnumerator.reset();
        while(runEnumerator.hasNext() && doRun) {
            JAMSComponent comp = runEnumerator.next();
            try {
                comp.cleanup();
            } catch (IOException e) {
                System.out.println(comp.getInstanceName());
                e.printStackTrace();
            } catch (Exception e) {
                
            }
        }
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
    
    public double[] compob(double x[]) {
        double value = 0;
        
        for (int j=0;j<parameters.length;j++) {
            parameters[j].setValue(x[j]);
        }
        
        singleRun();
        
        double F[] = new double[M];
        for (int i = 0; i<M;i++) {
            if (MaximizeEff.getValue()[i] == MINIMIZATION)
                F[i] =  this.effValues[i].getValue();
            else if (MaximizeEff.getValue()[i] == ABSMINIMIZATION)
                F[i] = Math.abs(this.effValues[i].getValue());
            else if (MaximizeEff.getValue()[i] == ABSMAXIMIZATION)
                F[i] =  -Math.abs(this.effValues[i].getValue());
            else if (MaximizeEff.getValue()[i] == MAXIMIZATION)
                F[i] = -this.effValues[i].getValue();
        }
        return F;
    }
    
    @SuppressWarnings("unchecked") public void sort(double x[][],int col) {
        MOCOM_Comparator comparator = new MOCOM_Comparator(col,false);
        java.util.Arrays.sort(x,comparator);
    }
    
    public int[] randperm(int upto) {
        int perm[] = new int[upto];
        int freenums[] = new int[upto];
        
        for (int i=0;i<upto;i++) {
            freenums[i] = i+1;
        }
        
        for (int i=1;i<=upto;i++) {
            int num = (int)Math.floor(Custom_rand()*(upto + 1 - i) + 1.0);
            perm[i-1] = freenums[num-1];
            freenums[num-1] = freenums[upto-i];
        }
	    
        return perm;
    }
    
    public double[][] lhsu(double[]xmin,double[] xmax,int nsample) {
        int nvar = xmin.length;
        double s[][] = new double[nsample][nvar];
		
        int idx[];
        for (int j=0;j<nvar;j++) {
            idx = randperm(nsample);
            
            for (int i=0;i<nsample;i++) {
                double P = (idx[i] - Custom_rand()) / (double)nsample;
                s[i][j] = xmin[j] + P*(xmax[j]-xmin[j]);
            }
        }
        return s;
    }
    
    public double[][] compf(double [][]D, int m) {
        int s = D.length,r = 0;
        if (s == 0)
            return null;
        else
            r = D[0].length;
        
        double F[][] = new double[s][];
        
        for (int i=0;i<s;i++) {
            F[i] = compob(D[i]);
        }
        return F;
    }
    
    public int IntMax(int R[]) {
        int RMax = Integer.MIN_VALUE;
        for (int i=0;i<R.length;i++) {
            if (RMax < R[i])
                RMax = R[i];
        }
        return RMax;
    }
    
    @SuppressWarnings("unchecked") public Object[] parrank(double[][]ObjVals,int nmbOfObjs) {
        // Pareto ranking of individuals in population
        int nmbOfIndivs = ObjVals.length;
        // set of individuals a particular individual dominates
        Vector<Integer> Dominated[] = new Vector[nmbOfIndivs];
        // Pareto-optimal fronts
        Vector<Vector<Integer>> Front = new Vector<Vector<Integer>>();
        
        // number of Pareto-optimal front for each individual; 2nd highest priority sorting key
        int NmbOfFront[] = new int[nmbOfIndivs];
        // number of individuals by which a particular individual is dominated
        int NmbOfDominating[] = new int[nmbOfIndivs];
        
        for (int i=0;i<nmbOfIndivs;i++) {
            NmbOfFront[i] = 0;
            NmbOfDominating[i] = 0;
            Dominated[i] = new Vector<Integer>();
        }
        
        for (int p=0;p<nmbOfIndivs;p++) {
            for (int q=0;q<nmbOfIndivs;q++) {
                int sumA1 = 0,sumA2 = 0;
                int sumB1 = 0,sumB2 = 0;
                for (int k=0;k<nmbOfObjs;k++) {
                    if (ObjVals[p][k] <= ObjVals[q][k])
                        sumA1++;
                    if (ObjVals[p][k] < ObjVals[q][k])
                        sumA2++;
                }
                for (int k=0;k<nmbOfObjs;k++) {
                    if (ObjVals[q][k] <= ObjVals[p][k])
                        sumB1++;
                    if (ObjVals[q][k] < ObjVals[p][k])
                        sumB2++;
                }
                if (sumA1 == nmbOfObjs && sumA2 > 0) {
                    Dominated[p].addElement(new Integer(q));
                } else if (sumB1 == nmbOfObjs && sumB2 > 0) {
                    NmbOfDominating[p]++;
                }
            }
            if (NmbOfDominating[p] == 0) {
                NmbOfFront[p] = 1;
                if (Front.size() == 0) {
                    Front.add(new Vector<Integer>());
                }
                Front.get(0).add(new Integer(p));
            }
        }
        
        int i = 0;
        while (Front.get(i).size() != 0) {
            Vector<Integer> NextFront = new Vector<Integer>();
            for (int k=0;k<Front.get(i).size();k++) {
                int p = Front.get(i).get(k).intValue();
                for (int l=0;l<Dominated[p].size();l++) {
                    int q = Dominated[p].get(l);
                    NmbOfDominating[q]--;
                    if (NmbOfDominating[q] == 0) {
                        NmbOfFront[q] = i + 2;
                        NextFront.add(new Integer(q));
                    }
                }
            }
            i++;
            Front.add(NextFront);
        }
        
        Integer RMax = IntMax(NmbOfFront);
        
        return new Object[]{NmbOfFront,RMax};
    }
    
    public double[] asswght(int R[],int RMax,int s) {
        double P[] = new double[s];
        double sum = 0;
        for (int i=0;i<R.length;i++) {
            sum += R[i];
        }	
        for (int i=0;i<s;i++) {
            P[i] = (RMax - R[i] + 1)/((double)(RMax+1)*s - sum);
        }
        return P;
    }
    
    public Object[] worst(double D[][],int n,int R[],int Rmax) {
        //[L] = find(R==Rmax);
        Vector<Integer> Ltmp = new Vector<Integer>();
        
        for (int i=0;i<R.length;i++) {
            if (R[i] == Rmax) {
                Ltmp.add(new Integer(i));
            }
        }
        int L[] = new int [Ltmp.size()];
        for (int i=0;i<Ltmp.size();i++) {
            L[i] = Ltmp.get(i).intValue();
        }
        
        int nA = L.length;
        double A[][] = new double[nA][n];
        for (int i=0;i<L.length;i++) {
            for (int j=0;j<n;j++) {
                A[i][j] = D[L[i]][j];
            }
        }
        return new Object[]{A,L,new Integer(nA)};
    }
    
    public Object[] choose(double P[],double D[][],double F[][],int Rank[],int n) {
        double sP[] = new double[P.length];
        double sum = 0;
        int counter = 0;
        int Selected[] = new int[n];
        double S1[][] = new double[n][D[0].length];
        double F1[][] = new double[n][F[0].length];
        int R1[] = new int[n];
        
        for (int i=0;i<P.length;i++) {
            sP[i] = (sum += P[i]);
        }
        
        while ( counter < n ) {
            double U;
            int R = -1;
            boolean multipleOccurrences = false;
		    
            do {
                multipleOccurrences = false;
                // Draw random number U between 0 and 1 using a uniform distribution
                U = this.Custom_rand(); //generator.nextDouble();
                // Combine labelled U with trapezoidal probability
                for (int i=0;i<P.length;i++) {
                    if (U < sP[i]) {
                        R = i;
                        break;
                    }
                }
                
                for (int i=0;i<counter;i++) {
                    if (Selected[i] == R) {
                        multipleOccurrences = true;		    
			break;
		    }
                }
            }while (multipleOccurrences == true);
            
            Selected[counter] = R;
            for (int j=0;j<D[0].length;j++) {
                S1[counter][j] = D[R][j];
            }
            for (int j=0;j<F[0].length;j++) {
                F1[counter][j] = F[R][j];
            }
            R1[counter] = Rank[R];
            counter++;
        }
        return new Object[]{S1,F1,R1};
    }
       
    // Function performs multi objective downhill simplex
    public Object[] mosim(double S[][],double SF[][],int SR[],int n,double minn[],double maxn[]) {
        int lenS = S[0].length;
        int lenSF = SF[0].length;
        
        int e = S.length;
        int r = lenS + lenSF + 1;
        // Define Simplex .. Simplex = [S SF SR];
        double Simplex[][] = new double[S.length][lenS + lenSF + 1];
        
        for (int i=0;i<S.length;i++) {
            for (int j=0;j<lenS;j++)
                Simplex[i][j] = S[i][j];
            for (int j=0;j<lenSF;j++)
                Simplex[i][lenS + j] = SF[i][j];
            Simplex[i][lenS + lenSF] = (double)SR[i];
        }
        // Sort Simplex
        sort(Simplex,r-1);
        //Assing function values for worst point in Simplex
        double Fw[] = new double[lenSF];
        for (int i=0;i<lenSF;i++) {
            Fw[i] = Simplex[Simplex.length-1][n+i];
        }
        // Assing parameter values worst ranked point in Simplex
        double Sw[] = new double[n];
        for (int i=0;i<n;i++) {
            Sw[i] = Simplex[Simplex.length-1][i];
        }
        double Sg[] = new double[n];
        // Compute centroid of Simplex after excluding the worst ranked point
        for (int i=0;i<n;i++) {
            Sg[i] = 0;
            for (int j=0;j<Simplex.length-1;j++) {
                Sg[i] += Simplex[j][i];
            }
            Sg[i] /= (double)(Simplex.length-1);
        }
        // Compute Reflection step
        double Sref[] = new double[n];
        for (int i=0;i<n;i++) {
            Sref[i] = 2*Sg[i] - Sw[i];
        }
        boolean accept = IsSampleValid(Sref);
        double Snew[],Fnew[];
        if (!accept) {
            // Compute contraction step
            double Scon[] = new double[n];
            for (int i=0;i<n;i++) {
                Scon[i] = 0.5 * Sg[i] + 0.5 * Sw[i];
            }
	    accept = IsSampleValid(Scon);
	    //if it fails again, try a mutation
	    while (!accept) {
		double newpar[][] = lhsu(this.lowBound,this.upBound,1);
		Scon = newpar[0];
		accept = IsSampleValid(Scon);
	    }
            double Fcon[] = compob(Scon);
            Snew = Scon;
            Fnew = Fcon;            
            // Update number of function evaluations
        } else {
            // Compute corresponding objective function values
            double Fref[] = compob(Sref);
            double SimplexTmp[][] = new double[Simplex.length][lenSF];
            for (int j=0;j<lenSF;j++) {
                for (int i=0;i<Simplex.length-1;i++) {
                    SimplexTmp[i][j] = Simplex[i][n+j];
                }
                SimplexTmp[Simplex.length-1][j] = Fref[j];
            }
            // Test for non dominance
            Object ret[] = parrank(SimplexTmp,2); // 2 ist possible wrong
            int Rref[] = (int[])ret[0];
            
            int Rrefmax = Integer.MIN_VALUE;
            for (int i=0;i<Rref.length-1;i++) {
                if (Rref[i] > Rrefmax) {
                    Rrefmax = Rref[i];
                }
            }
            
            if (Rref[Rref.length-1] <= Rrefmax) {
                Snew = Sref;
                Fnew = Fref;
            } else {
                // Compute contraction step
                double Scon[] = new double[n];
                for (int i=0;i<n;i++) {
                    Scon[i] = 0.5 * Sg[i] + 0.5 * Sw[i];
                }
		accept = IsSampleValid(Scon);
		//if it fails again, try a mutation
		while (!accept) {
		    double newpar[][] = lhsu(this.lowBound,this.upBound,1);
		    Scon = newpar[0];
		    accept = IsSampleValid(Scon);
		}
                // Compute corresponding objective function values
                double Fcon[] = compob(Scon);
                Snew = Scon;
                Fnew = Fcon;                
            }
        }
        for (int i=0;i<n;i++) {
            S[S.length-1][i] = Snew[i];
        }
        for (int i=0;i<SF[0].length;i++) {
            SF[SF.length-1][i] = Fnew[i];
        }
        
        return new Object[]{S,SF};
    }
    
    public Object[] update(double D[][],int L[],double A[][],int n,int nA,double F[][],double FA[][]) {
        // Replace A into D and FA into F using the indices stored in L,
        for (int i=0;i<nA;i++) {
            for (int j=0;j<n;j++) {
                D[L[i]][j] = A[i][j];
            }
            for (int j=0;j<FA[0].length;j++) {
                F[L[i]][j] = FA[i][j];
            }
        }
        return new Object[]{D,F};
    }
    
    // n - number of params
    // s - populationsize
    // minn/maxn - define feasible space
    // m - dont know
    // MaxIter - maximum iteration count
    public Object[] mocom(int n,int s,double minn[],double maxn[],int m,int MaxIter) {
        // Start with generating the initial population
        double D[][] = lhsu(minn,maxn,s);
        // Compute the objective function value for each point
        double F[][] = compf(D,m);
        // Now save some important variables
        int nobj = F[0].length;
        // Now do Pareto ranking
        Object ret[] = parrank(F,nobj);
        int R[] = (int[])ret[0];
        int Rmax = ((Integer)ret[1]).intValue();
        // Now start optimization loop
        int loopcounter = 1;
        while (Rmax > 1) {
            // Assign selection probability P(i) to each of the members
            double P[] = asswght(R,Rmax,s);
            // Construct A to be the points having largest ranks
            ret = worst(D,n,R,Rmax);
            double A[][] = (double[][])ret[0]; //A <- worst Points
            int L[] = (int[])ret[1];
            int nA = ((Integer)ret[2]).intValue();
            // Select n points from D to generate Simplex
            ret = choose(P,D,F,R,n);
            double S1[][] = (double[][])ret[0];
            double F1[][] = (double[][])ret[1];
            int R1[] = (int[])ret[2];
            
            double FA[][] = new double[nA][F[0].length];
            
            for (int j=0;j<nA;j++) {
                //addsim
                if (S1.length != F1.length || F1.length != R1.length) {
                    getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": interner Fehler");
                }
                //build S,SF,SR
                double S[][] = new double[S1.length+1][S1[0].length];
                double SF[][] = new double[F1.length+1][F1[0].length];
                int SR[] = new int[R1.length+1];
                
                for (int c1=0;c1<S1.length;c1++) {
                    for (int c2=0;c2<S1[0].length;c2++) {
                        S[c1][c2] = S1[c1][c2];
                    }
                    for (int c2=0;c2<F1[0].length;c2++) {
                        SF[c1][c2] = F1[c1][c2];
                    }
                    SR[c1] = R1[c1];
                }
                
                for (int c1=0;c1<S1[0].length;c1++) {
                    S[S1.length][c1] = A[j][c1];
                }
                
                for (int c1=0;c1<F1[0].length;c1++) {
                    SF[F1.length][c1] = F[L[j]][c1];
                }
                
                SR[R1.length] = R[L[j]];
                
                //mosim
                Object res[] = mosim(S,SF,SR,n,minn,maxn);
                S = (double[][])res[0];
                SF = (double[][])res[1];
                
                for (int c1=0;c1<n;c1++) {
                    A[j][c1] = S[S.length-1][c1];
                }
		
                for (int c1=0;c1<FA[j].length;c1++) {
                    FA[j][c1] = SF[SF.length-1][c1];
                }
            }
            ret = update(D,L,A,n,nA,F,FA);
            D = (double[][])ret[0];
            F = (double[][])ret[1];
            // Compute paretorank for each of the parameter sets according to Goldberg, 1989
            ret = parrank(F,nobj);
            R = (int[])ret[0];
            Rmax = ((Integer)ret[1]).intValue();
            //System.out.println("Evolution Loop:" + loopcounter + " - Trial - " + icall);
            
            double currentResult[][] = new double[s][N+M+1];
            for (int i=0;i<s;i++) {
                for (int j=0;j<N;j++)
                    currentResult[i][j] = D[i][j];
                for (int j=0;j<M;j++)
                    currentResult[i][N+j] = F[i][j];
                currentResult[i][N+M] = R[i];
            }
            this.sort(currentResult,N+M);
            
            int c=0;
            if(continousOutput){
                while(c < s && loopcounter % 10 == 0) {
                    //writer.write("Parameter: ");
                    writer.write("" + currentResult[c][N+M] +"\t");
                    for (int i=0;i<N;i++) {
                        writer.write("" + currentResult[c][i] + "\t");
                    }
                    //System.out.print("Function - Values:");
                    for (int i=0;i<M;i++) {
                        writer.write("" + currentResult[c][N+i] + "\t");
                    }
                    //System.out.print("Rank:");
                    //
                    //System.out.println("");
                    c++;
                    writer.writeLine("");
                    writer.flush();
                }
            }
            loopcounter++;
            if (MaxIter < loopcounter) {
                System.out.println("********************************************");
                System.out.println("---------->OPTIMIZATION STOP<---------------");
                System.out.println("--->MAXIMUM NUMBER OF LOOPS HAS REACHED<----");
                System.out.println("********************************************");
                c=0;
                System.out.print("Rank\t");
                for (int i=0;i<N;i++) {
                    System.out.print(this.parameterNames[i] + "\t");
                }
                for (int i=0;i<M;i++) {
                    System.out.print(this.effNames[i] + "\t");
                }
                System.out.println("");
                
                while(c < s) {
                    System.out.print("" + currentResult[c][N+M] + "\t");
                    for (int i=0;i<N;i++) {
                        System.out.print("" + currentResult[c][i] + "\t");
                    }
                    for (int i=0;i<M;i++) {
                        System.out.print("" + currentResult[c][N+i] + "\t");
                    }
                    
                    System.out.println("");
                    c++;
                }
                c=0;
                writer.writeLine("Number of model runs: " + this.icall);
                writer.write("Rank\t");
                for (int i=0;i<N;i++) {
                    writer.write(this.parameterNames[i] + "\t");
                }
                for (int i=0;i<M;i++) {
                    writer.write(this.effNames[i] + "\t");
                }
                writer.write("\n");
                while(c < s) {
                    writer.write("" + currentResult[c][N+M] +"\t");
                    for (int i=0;i<N;i++) {
                        writer.write("" + currentResult[c][i] + "\t");
                    }
                    for (int i=0;i<M;i++) {
                        writer.write("" + currentResult[c][N+i] + "\t");
                    }
                    c++;
                    writer.writeLine("");
                    writer.flush();
                }
                writer.flush();
                break;
            }
            if (Rmax <= 1) {
                System.out.println("********************************************");
                System.out.println("---------->OPTIMIZATION STOP<---------------");
                System.out.println("-------------->SUCCESSFUL<------------------");
                System.out.println("********************************************");
                c=0;
                System.out.print("Rank\t");
                for (int i=0;i<N;i++) {
                    System.out.print(this.parameterNames[i] + "\t");
                }
                for (int i=0;i<M;i++) {
                    System.out.print(this.effNames[i] + "\t");
                }
                System.out.println("");
                
                while(c < s) {
                    System.out.print("" + currentResult[c][N+M] + "\t");
                    for (int i=0;i<N;i++) {
                        System.out.print("" + currentResult[c][i] + "\t");
                    }
                    for (int i=0;i<M;i++) {
                        System.out.print("" + currentResult[c][N+i] + "\t");
                    }
                    
                    System.out.println("");
                    c++;
                }
                
                c=0;
                writer.writeLine("Number of model runs: " + this.icall);
                writer.write("Rank\t");
                for (int i=0;i<N;i++) {
                    writer.write(this.parameterNames[i] + "\t");
                }
                for (int i=0;i<M;i++) {
                    writer.write(this.effNames[i] + "\t");
                }
                writer.write("\n");
                while(c < s) {
                    
                    writer.write("" + currentResult[c][N+M] +"\t");
                    for (int i=0;i<N;i++) {
                        writer.write("" + currentResult[c][i] + "\t");
                    }
                    
                    for (int i=0;i<M;i++) {
                        writer.write("" + currentResult[c][N+i] + "\t");
                    }
                    
                    c++;
                    writer.write("\n");
                    writer.flush();
                }
                writer.flush();
            }
        }
        
        return new Object[]{D,F,R,new Integer(Rmax)};
    }
    
    public void run() {
        if (runEnumerator == null) {
            runEnumerator = getChildrenEnumerator();
        }
        if(!enable.getValue()){
            disabledRun();
            return;
        }
        
        if (this.maxIter == null) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": maxIter unknown. please set maxIter");
        }
        if (this.populationSize == null) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": populationsize unknown. please set populationsize");
        }
        int maxn = this.maxIter.getValue();
        this.s = this.populationSize.getValue();
        
        if (this.s <= 0) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": populationsize ist zero");
        }
        if (maxn <= 0) {
            getModel().getRuntime().sendHalt("Component " + this.getInstanceName() + ": invalid maxIter");
        }
        
        Object ret[] = mocom(N,this.s,this.lowBound,this.upBound,m,maxn);
        System.out.println("Mocom has finished");
    }
    
    public void cleanup() {
        
        if (enable.getValue()) {
            end = System.currentTimeMillis();
            long duration = end - start;
            
            writer.writeLine("duration: " + duration / 1000 + " sec");
            writer.flush();    
            writer.close();
        }
    }
}