/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import Jama.LUDecomposition;
import Jama.Matrix;
import java.util.Random;

/**
 *
 * @author chris
 */
public class VarianceBasedSensitivityIndex extends SensitivityAnalyzer{
    public enum Measure{FirstOrder, Total};

    Random rnd = new Random();
    Measure measure = Measure.FirstOrder;

    public VarianceBasedSensitivityIndex(Measure measure){
        this.measure = measure;
    }

    @Override
    public void init(){
        super.init();
        
        sensitivityIndex =new double[n];

        calcSensitivity();
    }

    private void calcSensitivity() {
        int Lh = L/2;
        double A[][] = new double[Lh][];
        double B[][] = new double[Lh][n];
        double C[][] = new double[Lh][n];
        double x0[] = new double[n];

        double yA[] = new double[Lh];
        double yB[] = new double[Lh];
        double yC[] = new double[Lh];

        double varYA = 0;

        double f0 = 0;


        for (int i=0;i<L;i++){
            int id_i = x[0].getId(i);

            for (int j=0;j<n;j++){
                x0[j] = x[j].getValue(id_i);
            }
            if (i < Lh){
                A[i] = transformToUnitCube(x0);
                yA[i] = this.y.getValue(id_i);
                f0 += yA[i];
            }
            else if ( (i-Lh) < Lh ){
                B[i-Lh] = transformToUnitCube(x0);
                yB[i-Lh] = this.y.getValue(id_i);
            }
        }
        f0 /= Lh;
        //f0 *= f0;

        double fAB = 0;
        for (int i=0;i<Lh;i++){
            fAB += yA[i]*yB[i];
            varYA += (yA[i]-f0)*(yA[i]-f0);
        }
        varYA /= (Lh-1);
        fAB /= Lh;
        f0 *= f0;

        for (int k=0;k<n;k++){
            for (int i=0;i<Lh;i++){
                for (int j=0;j<n;j++){
                    if (j==k)
                        C[i][j] = B[i][j];
                    else
                        C[i][j] = A[i][j];
                }
                yC[i] = this.getInterpolation(transformFromUnitCube(C[i]));                
            }
            

            double numerator = 0;
            double denumerator = 0;
            if (measure == Measure.FirstOrder){
                for (int i=0;i<Lh;i++){
                    numerator += (yA[i]*yC[i]);
                    denumerator += (yA[i]*yA[i]);
                }
                numerator = (numerator/Lh)-fAB;
                denumerator = (denumerator/Lh)-f0;

                sensitivityIndex[k] = Math.abs(numerator / varYA);
            }else{
                for (int i=0;i<Lh;i++){
                    numerator += (yB[i]*yC[i]);
                    denumerator += (yA[i]*yA[i]);
                }
                numerator = (numerator/(Lh-1))-f0;
                denumerator = (denumerator/Lh)-f0;

                sensitivityIndex[k] = Math.abs(1.0 - (numerator / varYA));
            }
        }
        double sum = 0;
        for (int k=0;k<n;k++){
            sum += sensitivityIndex[k];
        }
        for (int k=0;k<n;k++){
            sensitivityIndex[k] /= sum;
        }
    }
}
