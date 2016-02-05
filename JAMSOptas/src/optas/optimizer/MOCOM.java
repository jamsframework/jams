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
import java.util.*;
import jams.data.*;
import jams.model.*;
import jams.JAMS;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;

/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(title = "MOCOM Optimization Component",
author = "Christian Fischer",
description = "performs multi objective optimization")
public class MOCOM extends Optimizer {

    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(MOCOM.class.getSimpleName(), MOCOM.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("populationSize", JAMS.i18n("size_of_population"), 500, 0, 100000));

        return desc;
    }
    /**
     * @return the populationSize
     */
    public int getPopulationSize() {
        return populationSize;
    }

    /**
     * @param populationSize the populationSize to set
     */
    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }
                                            
    public int populationSize = 250;
    int currentCount;
                    
    int s; //population size        
    int icall = 0;
    int MaxIter;
    boolean continuousOutput = false;
    long start, end = 0;
    String effNames[];
        
    //sometimes useful e.g for using matlab random numbers
    public double customRand() {        
        return generator.nextDouble();
    }

    private boolean isSampleValid(double[] sample) {
        Attribute.Double conv_sample[] = new JAMSDouble[sample.length];
        for (int i = 0; i < sample.length; i++) {
            conv_sample[i] = DefaultDataFactory.getDataFactory().createDouble();
            conv_sample[i].setValue(sample[i]);
        }
        return isSampleValid(conv_sample);
    }

    private boolean isSampleValid(Attribute.Double[] sample) {                
        for (int i = 0; i < super.n; i++) 
            if (sample[i].getValue() < lowBound[i] || sample[i].getValue() > upBound[i]) 
                return false;                    
        return true;
    }
   
    @SuppressWarnings("unchecked")
    public void sort(double x[][], int col) {
        ArrayColumnComparator comparator = new ArrayColumnComparator(col, false);
        java.util.Arrays.sort(x, comparator);
    }

    public int[] randperm(int upto) {
        int perm[] = new int[upto];
        int freenums[] = new int[upto];

        for (int i = 0; i < upto; i++) {
            freenums[i] = i + 1;
        }

        for (int i = 1; i <= upto; i++) {
            int num = (int) Math.floor(customRand() * (upto + 1 - i) + 1.0);
            perm[i - 1] = freenums[num - 1];
            freenums[num - 1] = freenums[upto - i];
        }

        return perm;
    }

    public double[][] lhsu(double[] xmin, double[] xmax, int nsample) {
        int nvar = xmin.length;
        double s[][] = new double[nsample][nvar];

        int idx[];
        for (int j = 0; j < nvar; j++) {
            idx = randperm(nsample);

            for (int i = 0; i < nsample; i++) {
                double P = (idx[i] - customRand()) / (double) nsample;
                s[i][j] = xmin[j] + P * (xmax[j] - xmin[j]);
            }
        }
        return s;
    }

    public double[][] compf(double[][] D)throws SampleLimitException, ObjectiveAchievedException {
        int s = D.length, r = 0;
        if (s == 0) {
            return null;
        } else {
            r = D[0].length;
        }
        double F[][] = new double[s][];
        
        for (int i = 0; i < s; i++) {            
            F[i] = this.getSample((D[i])).F();
        }
        return F;
    }

    public int IntMax(int R[]) {
        int RMax = Integer.MIN_VALUE;
        for (int i = 0; i < R.length; i++) {
            if (RMax < R[i]) {
                RMax = R[i];
            }
        }
        return RMax;
    }

    @SuppressWarnings("unchecked")
    public Object[] parrank(double[][] ObjVals, int nmbOfObjs) {
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

        for (int i = 0; i < nmbOfIndivs; i++) {
            NmbOfFront[i] = 0;
            NmbOfDominating[i] = 0;
            Dominated[i] = new Vector<Integer>();
        }

        for (int p = 0; p < nmbOfIndivs; p++) {
            for (int q = 0; q < nmbOfIndivs; q++) {
                int sumA1 = 0, sumA2 = 0;
                int sumB1 = 0, sumB2 = 0;
                for (int k = 0; k < nmbOfObjs; k++) {
                    if (ObjVals[p][k] <= ObjVals[q][k]) {
                        sumA1++;
                    }
                    if (ObjVals[p][k] < ObjVals[q][k]) {
                        sumA2++;
                    }
                }
                for (int k = 0; k < nmbOfObjs; k++) {
                    if (ObjVals[q][k] <= ObjVals[p][k]) {
                        sumB1++;
                    }
                    if (ObjVals[q][k] < ObjVals[p][k]) {
                        sumB2++;
                    }
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
            for (int k = 0; k < Front.get(i).size(); k++) {
                int p = Front.get(i).get(k).intValue();
                for (int l = 0; l < Dominated[p].size(); l++) {
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

        return new Object[]{NmbOfFront, RMax};
    }

    public double[] asswght(int R[], int RMax, int s) {
        double P[] = new double[s];
        double sum = 0;
        for (int i = 0; i < R.length; i++) {
            sum += R[i];
        }
        for (int i = 0; i < s; i++) {
            P[i] = (RMax - R[i] + 1) / ((double) (RMax + 1) * s - sum);
        }
        return P;
    }

    public Object[] worst(double D[][], int n, int R[], int Rmax) {
        //[L] = find(R==Rmax);
        Vector<Integer> Ltmp = new Vector<Integer>();

        for (int i = 0; i < R.length; i++) {
            if (R[i] == Rmax) {
                Ltmp.add(new Integer(i));
            }
        }
        int L[] = new int[Ltmp.size()];
        for (int i = 0; i < Ltmp.size(); i++) {
            L[i] = Ltmp.get(i).intValue();
        }

        int nA = L.length;
        double A[][] = new double[nA][n];
        for (int i = 0; i < L.length; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = D[L[i]][j];
            }
        }
        return new Object[]{A, L, new Integer(nA)};
    }

    public Object[] choose(double P[], double D[][], double F[][], int Rank[], int n) {
        double sP[] = new double[P.length];
        double sum = 0;
        int counter = 0;
        int Selected[] = new int[n];
        double S1[][] = new double[n][D[0].length];
        double F1[][] = new double[n][F[0].length];
        int R1[] = new int[n];

        for (int i = 0; i < P.length; i++) {
            sP[i] = (sum += P[i]);
        }

        while (counter < n) {
            double U;
            int R = -1;
            boolean multipleOccurrences = false;

            do {
                multipleOccurrences = false;
                // Draw random number U between 0 and 1 using a uniform distribution
                U = this.customRand(); //generator.nextDouble();
                // Combine labelled U with trapezoidal probability
                for (int i = 0; i < P.length; i++) {
                    if (U < sP[i]) {
                        R = i;
                        break;
                    }
                }

                for (int i = 0; i < counter; i++) {
                    if (Selected[i] == R) {
                        multipleOccurrences = true;
                        break;
                    }
                }
            } while (multipleOccurrences == true);

            Selected[counter] = R;
            for (int j = 0; j < D[0].length; j++) {
                S1[counter][j] = D[R][j];
            }
            for (int j = 0; j < F[0].length; j++) {
                F1[counter][j] = F[R][j];
            }
            R1[counter] = Rank[R];
            counter++;
        }
        return new Object[]{S1, F1, R1};
    }
    // Function performs multi objective downhill simplex
    public Object[] mosim(double S[][], double SF[][], int SR[], double minn[], double maxn[]) throws SampleLimitException, ObjectiveAchievedException{
        int lenS = S[0].length;
        int lenSF = SF[0].length;

        int e = S.length;
        int r = lenS + lenSF + 1;
        // Define Simplex .. Simplex = [S SF SR];
        double Simplex[][] = new double[S.length][lenS + lenSF + 1];

        for (int i = 0; i < S.length; i++) {
            for (int j = 0; j < lenS; j++) {
                Simplex[i][j] = S[i][j];
            }
            for (int j = 0; j < lenSF; j++) {
                Simplex[i][lenS + j] = SF[i][j];
            }
            Simplex[i][lenS + lenSF] = (double) SR[i];
        }
        // Sort Simplex
        sort(Simplex, r - 1);
        //Assing function values for worst point in Simplex
        double Fw[] = new double[lenSF];
        for (int i = 0; i < lenSF; i++) {
            Fw[i] = Simplex[Simplex.length - 1][n + i];
        }
        // Assing parameter values worst ranked point in Simplex
        double Sw[] = new double[n];
        for (int i = 0; i < n; i++) {
            Sw[i] = Simplex[Simplex.length - 1][i];
        }
        double Sg[] = new double[n];
        // Compute centroid of Simplex after excluding the worst ranked point
        for (int i = 0; i < n; i++) {
            Sg[i] = 0;
            for (int j = 0; j < Simplex.length - 1; j++) {
                Sg[i] += Simplex[j][i];
            }
            Sg[i] /= (double) (Simplex.length - 1);
        }
        // Compute Reflection step
        double Sref[] = new double[n];
        for (int i = 0; i < n; i++) {
            Sref[i] = 2 * Sg[i] - Sw[i];
        }
        boolean accept = isSampleValid(Sref);
        double Snew[], Fnew[];
        if (!accept) {
            // Compute contraction step
            double Scon[] = new double[n];
            for (int i = 0; i < n; i++) {
                Scon[i] = 0.5 * Sg[i] + 0.5 * Sw[i];
            }
            accept = isSampleValid(Scon);
            //if it fails again, try a mutation
            while (!accept) {
                double newpar[][] = lhsu(this.lowBound, this.upBound, 1);
                Scon = newpar[0];
                accept = isSampleValid(Scon);
            }
            double Fcon[] = getSample(Scon).F();
            Snew = Scon;
            Fnew = Fcon;
        // Update number of function evaluations
        } else {
            // Compute corresponding objective function values
            double Fref[] = getSample(Sref).F();
            double SimplexTmp[][] = new double[Simplex.length][lenSF];
            for (int j = 0; j < lenSF; j++) {
                for (int i = 0; i < Simplex.length - 1; i++) {
                    SimplexTmp[i][j] = Simplex[i][n + j];
                }
                SimplexTmp[Simplex.length - 1][j] = Fref[j];
            }
            // Test for non dominance
            Object ret[] = parrank(SimplexTmp, this.m); // 2 ist possible wrong
            int Rref[] = (int[]) ret[0];

            int Rrefmax = Integer.MIN_VALUE;
            for (int i = 0; i < Rref.length - 1; i++) {
                if (Rref[i] > Rrefmax) {
                    Rrefmax = Rref[i];
                }
            }

            if (Rref[Rref.length - 1] <= Rrefmax) {
                Snew = Sref;
                Fnew = Fref;
            } else {
                // Compute contraction step
                double Scon[] = new double[n];
                for (int i = 0; i < n; i++) {
                    Scon[i] = 0.5 * Sg[i] + 0.5 * Sw[i];
                }
                accept = isSampleValid(Scon);
                //if it fails again, try a mutation
                while (!accept) {
                    double newpar[][] = lhsu(this.lowBound, this.upBound, 1);
                    Scon = newpar[0];
                    accept = isSampleValid(Scon);
                }
                // Compute corresponding objective function values
                double Fcon[] = getSample(Scon).F();
                Snew = Scon;
                Fnew = Fcon;
            }
        }
        for (int i = 0; i < n; i++) {
            S[S.length - 1][i] = Snew[i];
        }
        for (int i = 0; i < SF[0].length; i++) {
            SF[SF.length - 1][i] = Fnew[i];
        }

        return new Object[]{S, SF};
    }

    public Object[] update(double D[][], int L[], double A[][], int n, int nA, double F[][], double FA[][]) {
        // Replace A into D and FA into F using the indices stored in L,
        for (int i = 0; i < nA; i++) {
            for (int j = 0; j < n; j++) {
                D[L[i]][j] = A[i][j];
            }
            for (int j = 0; j < FA[0].length; j++) {
                F[L[i]][j] = FA[i][j];
            }
        }
        return new Object[]{D, F};
    }    
    // s - populationsize
    // minn/maxn - define feasible space    
    // MaxIter - maximum iteration count
    public Object[] mocom(int s, double minn[], double maxn[], int MaxIter) throws SampleLimitException, ObjectiveAchievedException{
        // Start with generating the initial population
        double D[][] = lhsu(minn, maxn, s);
        // Compute the objective function value for each point
        double F[][] = compf(D);
        // Now save some important variables
        int nobj = F[0].length;
        // Now do Pareto ranking
        Object ret[] = parrank(F, nobj);
        int R[] = (int[]) ret[0];
        int Rmax = ((Integer) ret[1]).intValue();
        // Now start optimization loop
        int loopcounter = 1;
        while (Rmax > 1) {
            // Assign selection probability P(i) to each of the members
            double P[] = asswght(R, Rmax, s);
            // Construct A to be the points having largest ranks
            ret = worst(D, n, R, Rmax);
            double A[][] = (double[][]) ret[0]; //A <- worst Points
            int L[] = (int[]) ret[1];
            int nA = ((Integer) ret[2]).intValue();
            // Select n points from D to generate Simplex
            ret = choose(P, D, F, R, n);
            double S1[][] = (double[][]) ret[0];
            double F1[][] = (double[][]) ret[1];
            int R1[] = (int[]) ret[2];

            double FA[][] = new double[nA][F[0].length];

            for (int j = 0; j < nA; j++) {
                //addsim
                if (S1.length != F1.length || F1.length != R1.length) {
                    log(JAMS.i18n("interner_Fehler"));
                }
                //build S,SF,SR
                double S[][] = new double[S1.length + 1][S1[0].length];
                double SF[][] = new double[F1.length + 1][F1[0].length];
                int SR[] = new int[R1.length + 1];

                for (int c1 = 0; c1 < S1.length; c1++) {
                    for (int c2 = 0; c2 < S1[0].length; c2++) {
                        S[c1][c2] = S1[c1][c2];
                    }
                    for (int c2 = 0; c2 < F1[0].length; c2++) {
                        SF[c1][c2] = F1[c1][c2];
                    }
                    SR[c1] = R1[c1];
                }

                for (int c1 = 0; c1 < S1[0].length; c1++) {
                    S[S1.length][c1] = A[j][c1];
                }

                for (int c1 = 0; c1 < F1[0].length; c1++) {
                    SF[F1.length][c1] = F[L[j]][c1];
                }

                SR[R1.length] = R[L[j]];

                //mosim
                Object res[] = mosim(S, SF, SR, minn, maxn);
                S = (double[][]) res[0];
                SF = (double[][]) res[1];

                for (int c1 = 0; c1 < n; c1++) {
                    A[j][c1] = S[S.length - 1][c1];
                }

                for (int c1 = 0; c1 < FA[j].length; c1++) {
                    FA[j][c1] = SF[SF.length - 1][c1];
                }
            }
            ret = update(D, L, A, n, nA, F, FA);
            D = (double[][]) ret[0];
            F = (double[][]) ret[1];
            // Compute paretorank for each of the parameter sets according to Goldberg, 1989
            ret = parrank(F, nobj);
            R = (int[]) ret[0];
            Rmax = ((Integer) ret[1]).intValue();
            
            double currentResult[][] = new double[s][n + m + 1];
            for (int i = 0; i < s; i++) {
                for (int j = 0; j < n; j++) {
                    currentResult[i][j] = D[i][j];
                }
                for (int j = 0; j < m; j++) {
                    currentResult[i][n + j] = F[i][j];
                }
                currentResult[i][n + m] = R[i];
            }
            this.sort(currentResult, n + m);

            int c = 0;
            if (continuousOutput) {
                while (c < s && loopcounter % 10 == 0) {
                    String out = "";
                    out += ("" + currentResult[c][n+m] + "\t");
                    for (int i = 0; i < n; i++) {
                        out += ("" + currentResult[c][i] + "\t");
                    }
                    //System.out.print("Function - Values:");
                    for (int i = 0; i < m; i++) {
                        out += ("" + currentResult[c][n + i] + "\t");
                    }     
                    log(out);
                   c++;                                        
                }
            }
            loopcounter++;
            if (MaxIter < loopcounter) {
                log("********************************************");
                log("---------->OPTIMIZATION STOP<---------------");
                log(JAMS.i18n("MAXIMUM_NUMBER_OF_LOOPS_HAS_REACHED"));
                log("********************************************");
                c = 0;
                String out = JAMS.i18n("Rank")+"\t";
                
                for (int i = 0; i < n; i++) {
//                    out += this.parameterNames[i] + "\t";
                }
                for (int i = 0; i < m; i++) {
                    //out += efficiencyNames[i] + "\t";
                }
                log(out);

                while (c < s) {
                   out = "" + currentResult[c][n + m] + "\t";
                    for (int i = 0; i < n; i++) {
                        out+=("" + currentResult[c][i] + "\t");
                    }
                    for (int i = 0; i < m; i++) {
                        out+=("" + currentResult[c][n + i] + "\t");
                    }

                    log(out);
                    c++;
                }
                c = 0;
                log(JAMS.i18n("Number of model runs: ") + this.icall);
                out = JAMS.i18n("Rank")+"\t";
                for (int i = 0; i < n; i++) {
//                    out+=this.parameterNames[i] + "\t";
                }
                for (int i = 0; i < m; i++) {
                    out+=this.effNames[i] + "\t";
                }
                log(out);
                while (c < s) {
                    out = ("" + currentResult[c][n + m] + "\t");
                    for (int i = 0; i < n; i++) {
                        out += ("" + currentResult[c][i] + "\t");
                    }
                    for (int i = 0; i < m; i++) {
                        out += ("" + currentResult[c][n + i] + "\t");
                    }
                    log(out);
                    c++;                    
                }                
                break;
            }
            if (Rmax <= 1) {
                System.out.println("********************************************");
                System.out.println("---------->OPTIMIZATION STOP<---------------");
                log(JAMS.i18n("SUCCESSFUL"));
                System.out.println("********************************************");
                c = 0;
                String out = JAMS.i18n("Rank")+"\t";
                for (int i = 0; i < n; i++) {
//                    out+=(this.parameterNames[i] + "\t");
                }
                for (int i = 0; i < m; i++) {
                    out+=(this.effNames[i] + "\t");
                }
                log(out);
                try{
                while (c < s) {
                    out=("" + currentResult[c][n + m] + "\t");
                    for (int i = 0; i < n; i++) {
                        out+=("" + currentResult[c][i] + "\t");
                    }
                    for (int i = 0; i < m; i++) {
                        out+=("" + currentResult[c][n + i] + "\t");
                    }

                    log(out);
                    c++;
                }

                c = 0;
                log(JAMS.i18n("Number of model runs: ") + this.icall);
                out = JAMS.i18n("Rank")+"\t";
                for (int i = 0; i < n; i++) {
//                    out+=(this.parameterNames[i] + "\t");
                }
                for (int i = 0; i < m; i++) {
                    out+=(this.effNames[i] + "\t");
                }
                log(out);
                while (c < s) {

                    out=("" + currentResult[c][n + m] + "\t");
                    for (int i = 0; i < n; i++) {
                        out+=("" + currentResult[c][i] + "\t");
                    }

                    for (int i = 0; i < m; i++) {
                        out+="" + currentResult[c][n + i] + "\t";
                    }

                    c++;
                    log(out);
                }                
            }catch(Throwable t){
                t.printStackTrace();
            }}
        }

        return new Object[]{D, F, R, new Integer(Rmax)};
    }

    @Override
    public void procedure()  throws SampleLimitException, ObjectiveAchievedException{                
        this.s = this.populationSize;

        if (this.s <= 0) {
            log(JAMS.i18n("Component") + " " + JAMS.i18n("populationsize_is_zero"));
        }
        
        Object ret[] = mocom(this.s, this.lowBound, this.upBound, (int)getMaxn());
        log(JAMS.i18n("Mocom_has_finished"));
    }   
}