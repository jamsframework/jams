/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.SA;

import java.util.Random;

/**
 *
 * @author chris
 */
public class FAST extends SensitivityAnalyzer{
    public enum Measure{FirstOrder, Total};
    
    Random rnd = new Random();
    Measure measure = Measure.FirstOrder;

    // ok die frequenzen müssen wohl in Fm linear unabhängig sein. da m irgendwie von der parameterzahl abhängt sollten wir auf der sicheren seite
    //sein, wenn wir mit 191 beginnen
    int omega[] = new int[]{/*2,     3,     5,     7,    11,    13,    17,    19,    23,    29,    31,    37,    41,    43,
   47,    53,    59,    61,    67,    71,    73,    79,    83,    89,    97,   101,   103,   107,
  109,   113,   127,   131,   137,   139,   149,   151,   157,   163,   167,   173,   179,   181,*/
  191,   193,   197,   199,   211,   223,   227,   229,   233,   239,   241,   251,   257,   263,
  269,   271,   277,   281,   283,   293,   307,   311,   313,   317,   331,   337,   347,   349,
  353,   359,   367,   373,   379,   383,   389,   397,   401,   409,   419,   421,   431,   433,
  439,   443,   449,   457,   461,   463,   467,   479,   487,   491,   499,   503,   509,   521,
  523,   541,   547,   557,   563,   569,   571,   577,   587,   593,   599,   601,   607,   613,
  617,   619,   631,   641,   643,   647,   653,   659,   661,   673,   677,   683,   691,   701,
  709,   719,   727,   733,   739,   743,   751,   757,   761,   769,   773,   787,   797,   809,
  811,   821,   823,   827,   829,   839,   853,   857,   859,   863,   877,   881,   883,   887,
  907,   911,   919,   929,   937,   941,   947,   953,   967,   971,   977,   983,   991,   997,
 1009,  1013,  1019,  1021,  1031,  1033,  1039,  1049,  1051,  1061,  1063,  1069,  1087,  1091,
 1093,  1097,  1103,  1109,  1117,  1123,  1129,  1151,  1153,  1163,  1171,  1181,  1187,  1193,
 1201,  1213,  1217,  1223,  1229,  1231,  1237,  1249,  1259,  1277,  1279,  1283,  1289,  1291};

    public FAST(Measure measure){
        this.measure = measure;
    }

    @Override
    public void calculate(){
        super.calculate();

        if (this.measure == Measure.FirstOrder){
            calcFirstOrderSensitivity();
        }else{
            calcTotalVariance();
        }
    }

    private double[] X(double s){
        double y[] = new double[n];

        for (int j=0;j<n;j++){
            y[j] = Math.IEEEremainder(omega[j]*s, 2.0*Math.PI) / 2.0*Math.PI;
        }
        return y;
    }
//this is Fourier ampitude sensitivity testing (FAST)
//the formulas here are based on this article: http://en.wikipedia.org/wiki/Fourier_amplitude_sensitivity_testing#Variance-based_sensitivity
    private void calcFirstOrderSensitivity() {
        //this should hold
        int N = 2;
        int q = 2*N*omega[n]+1;        //but N*omega[n]+1 should be enough
        double sum = 0;

        for (int j=0;j<n;j++){
            double Aj[] = new double[N+1];
            double Bj[] = new double[N+1];

            for (int m = 1; m <= N; m++) {
                Aj[m] = 0.0;
                Bj[m] = 0.0;

                for (int k = -q; k <= q; k++) {
                    double sk = Math.PI * k / (2 * q + 1);

                    double Xsk[] = X(sk);
                    double y = this.evaluateModel(Xsk);//this.getInterpolation(this.transformFromUnitCube(Xsk));

                    Aj[m] += y * Math.cos(omega[j] * m * sk);
                    Bj[m] += y * Math.sin(omega[j] * m * sk);
                }
            }
            sensitivityIndex[j] = 2.0*Aj[2]*Aj[2]+Bj[1]*Bj[1];
            sum += sensitivityIndex[j];
        }
        for (int j=0;j<n;j++){
            sensitivityIndex[j] /= sum;
        }
    }

    //funktioniert nicht. das ist mir irgendwie zu hoch
    private void calcTotalVariance() {
        //this should hold
        int N = 2;
        int q = 2*N*omega[n]+1;        //but N*omega[n]+1 should be enough
        double sum = 0;

        for (int j=0;j<n;j++){
            double Aj[] = new double[N+1];
            double Bj[] = new double[N+1];

            for (int m = 0; m <= N; m++) {
                Aj[m] = 0.0;
                Bj[m] = 0.0;

                for (int k = -q; k <= q; k++) {
                    double sk = Math.PI * k / (2 * q + 1);

                    double Xsk[] = X(sk);
                    double y = this.evaluateModel(Xsk);//this.getInterpolation(this.transformFromUnitCube(Xsk));

                    Aj[m] += y*y * Math.cos(omega[j] * m * sk);
                    Bj[m] += y * Math.cos(omega[j] * m * sk);
                }
            }
            sensitivityIndex[j] = Aj[0]-Bj[0]*Bj[0];
            sum += sensitivityIndex[j];
        }
        for (int j=0;j<n;j++){
            sensitivityIndex[j] /= sum;
        }
    }
}
