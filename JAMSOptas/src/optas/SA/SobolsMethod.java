/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.SA;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import optas.optimizer.management.SampleFactory.Sample;

/**
 *
 * @author chris
 */
public class SobolsMethod extends SensitivityAnalyzer {

    public enum Measure {

        FirstOrder, Total, Interaction
    };
    Random rnd = new Random();
    Measure measure = Measure.FirstOrder;

    double A[][] = null;
    double B[][] = null;    
    double yA[] = null;
    double yB[] = null;
    int Lh = 0;
    double deltaY_AB = 0, EyA = 0, VyA = 0;

    public SobolsMethod(Measure measure) {
        this.measure = measure;
    }
   
/*
    private void calcSensitivity2() {
        double Mi[][] = new double[Lh][n];
        double MTi[][] = new double[Lh][n];
        double x0[] = new double[n];
        
        double yMi[] = new double[Lh];
        double yMTi[] = new double[Lh];

        double varYA = 0;

        double f0 = 0;


        for (int i = 0; i < L; i++) {
            int id_i = x[0].getId(i);

            for (int j = 0; j < n; j++) {
                x0[j] = x[j].getValue(id_i);
            }
            if (i < Lh) {                
                f0 += yA[i];
            }
        }
        f0 /= Lh;

        for (int i = 0; i < Lh; i++) {
            varYA += (yA[i]) * (yA[i]);
        }
        varYA /= Lh;
        varYA -= f0 * f0;

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < Lh; i++) {
                for (int j = 0; j < n; j++) {
                    if (j == k) {
                        Mi[i][j] = B[i][j];
                        MTi[i][j] = A[i][j];
                    } else {
                        Mi[i][j] = A[i][j];
                        MTi[i][j] = B[i][j];
                    }
                }
                yMi[i] = this.getInterpolation(transformFromUnitCube(Mi[i]));
                yMTi[i] = this.getInterpolation(transformFromUnitCube(MTi[i]));
            }

            double U1 = 0, UT = 0;
            for (int i = 0; i < Lh; i++) {
                U1 += yA[i] * yMi[i];
                UT += yA[i] * yMTi[i];
            }
            U1 /= Lh;
            UT /= Lh;

            double V1 = U1 - f0 * f0;
            double VT = UT - f0 * f0;

            double S1 = VT / varYA;
            double ST = 1.0 - (V1 / varYA);

            if (measure == Measure.FirstOrder) {
                sensitivityIndex[k] = S1;
            } else if (measure == Measure.Total) {
                sensitivityIndex[k] = ST;
            } else if (measure == Measure.Interaction) {
                sensitivityIndex[k] = ST - S1;
            }
        }
        double sum = 0;
        for (int k = 0; k < n; k++) {
            sum += sensitivityIndex[k];
        }
        for (int k = 0; k < n; k++) {
            //sensitivityIndex[k] /= sum;
        }
    }
*/
    public void calculate() {
        super.calculate();
        
        double x0A[];
        double x0B[];

        Lh = sampleSize/2;
        
        A = new double[Lh][];
        B = new double[Lh][n];
        x0A = new double[n];
        x0B = new double[n];
        yA = new double[Lh];
        yB = new double[Lh];

        ArrayList<Sample> x = getRandomSampling();
        
        EyA = VyA = 0;
        for (int i = 0; i < Lh; i++) {
            /*int id_iA = x[0].getId(i);
            int id_iB = x[0].getId(i + Lh);
            */
            for (int j = 0; j < n; j++) {
                x0A[j] = x.get(i).x[j];//x[j].getValue(id_iA);
                x0B[j] = x.get(i+Lh).x[j];//x[j].getValue(id_iB);
            }
            A[i] = transformToUnitCube(x0A);
            yA[i] = x.get(i).F()[0];//this.y.getValue(id_iA);
            B[i] = transformToUnitCube(x0B);
            yB[i] = x.get(i+Lh).F()[0];//this.y.getValue(id_iB);

            deltaY_AB += Math.abs(yA[i] - yB[i]);
            EyA += yA[i];
        }

        deltaY_AB = (deltaY_AB / Lh);// - EY_AB*EY_AB; //verschiebungssatz
        EyA /= Lh;

        /*for (int i = 0; i < Lh; i++) {
            VyA += (yA[i]-EyA)*(yA[i]-EyA);
        }
        VyA /= Lh;*/



        for (int i = 0; i < Lh; i++) {
            VyA += yA[i]*yA[i];
        }
        VyA = (VyA/ Lh) - EyA*EyA;
        
        
        for (int k = 0; k < n; k++) {
            Set<Integer> set = new TreeSet<Integer>();
            set.add(k);
            double Si = calcSensitivity(set)[0];
            double ST = calcSensitivity(set)[1];

            if (measure == Measure.FirstOrder) {
                sensitivityIndex[k] = Si;
            } else if (measure == Measure.Total) {
                sensitivityIndex[k] = ST;
            } else if (measure == Measure.Interaction) {
                sensitivityIndex[k] = ST - Si;
            }
        }
        double sum = 0;
        for (int k = 0; k < n; k++) {
            sum += sensitivityIndex[k];
        }
        for (int k = 0; k < n; k++) {
            //sensitivityIndex[k] /= sum;
        }
    }
/*
    public double[] calcSensitivityFine(Set<Integer> indexSet) {
        double sensitivityIndex[] = new double[3];
        
        double C[][] = new double[Lh][n];
        double D[][] = new double[Lh][n];
        double yC[] = new double[Lh];
        double yD[] = new double[Lh];
        
        for (int i = 0; i < Lh; i++) {
            for (int j = 0; j < n; j++) {
                if (indexSet.contains(j)) {
                    C[i][j] = A[i][j];
                    D[i][j] = B[i][j];
                } else {
                    C[i][j] = B[i][j];
                    D[i][j] = A[i][j];
                }
            }
            yC[i] = this.getInterpolation(transformFromUnitCube(C[i]));
            yD[i] = this.getInterpolation(transformFromUnitCube(D[i]));
        }

        double Si = 0;
        double STi = 0;

        double deltaY_AC = 0, deltaY_AD = 0;
        for (int i = 0; i < Lh; i++) {
            deltaY_AC += Math.abs(yA[i] - yC[i]);//*(yA[i] - yC[i]);
            deltaY_AD += Math.abs(yA[i] - yD[i]);//*(yA[i] - yD[i]);
        }

        deltaY_AC = deltaY_AC / Lh;// - EY_AC*EY_AC;
        deltaY_AD = deltaY_AD / Lh;// - EY_BC*EY_BC;

        Si = (deltaY_AD / deltaY_AB);
        STi = 1.0 - (deltaY_AC / deltaY_AB);

        sensitivityIndex[0] = Si;
        sensitivityIndex[1] = STi;

        return sensitivityIndex;
    }*/

    public void calcAll() {
        int twoPowN = 1 << n;
        TreeSet<Integer> indexSet = new TreeSet<Integer>();
        for (int i = 1; i < twoPowN; i++) {
            indexSet.clear();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    indexSet.add(j);
                }
            }
            calcSensitivity(indexSet);
        }
    }

    public double[] calcSensitivity(Set<Integer> indexSet) {
        double sensitivityIndex[] = new double[3];
        double C[][] = new double[Lh][n];
        double D[][] = new double[Lh][n];
        double yC[] = new double[Lh];
        double yD[] = new double[Lh];

        for (int i = 0; i < Lh; i++) {
            for (int j = 0; j < n; j++) {
                if (indexSet.contains(j)) {
                    C[i][j] = A[i][j];
                    D[i][j] = B[i][j];
                } else {
                    C[i][j] = B[i][j];
                    D[i][j] = A[i][j];
                }
            }
            yC[i] = evaluateModel(C[i]);//this.getInterpolation(transformFromUnitCube(C[i]));
            yD[i] = evaluateModel(D[i]);//this.getInterpolation(transformFromUnitCube(D[i]));
        }

        double Si = 0;
        double STi = 0;

        double ti1 = 0, ti2 = 0;
        double tc1 = 0, tc2 = 0;
        for (int i = 0; i < Lh; i++) {
            ti1 += yA[i]*yC[i];
            tc1 += 0.25*(yD[i]+yA[i])*(yD[i]+yA[i]);

            ti2 += yA[i]*yD[i];
            tc2 += (yD[i]+yA[i]);
        }
        ti1/=Lh;
        ti2/=Lh;

        double VyAC = ti1 - EyA*EyA;
        double VyBC = ti2 - EyA*EyA;

        Si = (VyAC / VyA);
        STi = 1.0 - (VyBC / VyA);

        sensitivityIndex[0] = Math.max(Si,0);
        sensitivityIndex[1] = Math.max(STi,0);
        sensitivityIndex[2] = STi - Si;
        
        System.out.println("Total Variance:" + VyA);
        System.out.println("S_" + indexSet.toString() + ":" + Si);
        System.out.println("ST_" + indexSet.toString() + ":" + STi);

        return sensitivityIndex;
    }
}
