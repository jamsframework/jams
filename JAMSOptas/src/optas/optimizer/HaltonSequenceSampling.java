/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer;


import jams.io.SerializableBufferedWriter;
import jams.model.JAMSComponentDescription;
import java.io.File;
import java.util.Arrays;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class HaltonSequenceSampling extends Optimizer{           
    SerializableBufferedWriter writer = null;


    public double offset = 0;
    public  boolean analyzeQuality = false;
    public  double targetQuality = 0.8;

    public double getOffset(){
        return this.offset;
    }

    public void setOffset(double offset){
        this.offset = offset;
    }

    public void setAnalyzeQuality(boolean analyzeQuality){
        this.analyzeQuality = analyzeQuality;
    }

    public boolean isAnalyzeQuality(){
        return this.analyzeQuality;
    }

    public void setTargetQuality(double targetQuality){
        this.targetQuality = targetQuality;
    }

    public double getTargetQuality(){
        return this.targetQuality;
    }

    private int primTable[] = {2,     3,     5,     7,    11,    13,    17,    19,    23,    29,    31,    37,    41,    43,
   47,    53,    59,    61,    67,    71,    73,    79,    83,    89,    97,   101,   103,   107,
  109,   113,   127,   131,   137,   139,   149,   151,   157,   163,   167,   173,   179,   181,
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
 1201,  1213,  1217,  1223,  1229,  1231,  1237,  1249,  1259,  1277,  1279,  1283,  1289,  1291,
 1297,  1301,  1303,  1307,  1319,  1321,  1327,  1361,  1367,  1373,  1381,  1399,  1409,  1423,
 1427,  1429,  1433,  1439,  1447,  1451,  1453,  1459,  1471,  1481,  1483,  1487,  1489,  1493,
 1499,  1511,  1523,  1531,  1543,  1549,  1553,  1559,  1567,  1571,  1579,  1583,  1597,  1601,
 1607,  1609,  1613,  1619,  1621,  1627,  1637,  1657,  1663,  1667,  1669,  1693,  1697,  1699,
 1709,  1721,  1723,  1733,  1741,  1747,  1753,  1759,  1777,  1783,  1787,  1789,  1801,  1811,
 1823,  1831,  1847,  1861,  1867,  1871,  1873,  1877,  1879,  1889,  1901,  1907,  1913,  1931,
 1933,  1949,  1951,  1973,  1979,  1987,  1993,  1997,  1999,  2003,  2011,  2017,  2027,  2029,
 2039,  2053,  2063,  2069,  2081,  2083,  2087,  2089,  2099,  2111,  2113,  2129,  2131,  2137,
 2141,  2143,  2153,  2161,  2179,  2203,  2207,  2213,  2221,  2237,  2239,  2243,  2251,  2267,
 2269,  2273,  2281,  2287,  2293,  2297,  2309,  2311,  2333,  2339,  2341,  2347,  2351,  2357,
 2371,  2377,  2381,  2383,  2389,  2393,  2399,  2411,  2417,  2423,  2437,  2441,  2447,  2459,
 2467,  2473,  2477,  2503,  2521,  2531,  2539,  2543,  2549,  2551,  2557,  2579,  2591,  2593,
 2609,  2617,  2621,  2633,  2647,  2657,  2659,  2663,  2671,  2677,  2683,  2687,  2689,  2693,
 2699,  2707,  2711,  2713,  2719,  2729,  2731,  2741,  2749,  2753,  2767,  2777,  2789,  2791
};

    public Sample[] initialSimplex = null;

    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(HaltonSequenceSampling.class.getSimpleName(), HaltonSequenceSampling.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("offset",
                "offset", 0, 0, Integer.MAX_VALUE));

        desc.addParameter(new BooleanOptimizerParameter("analyzeQuality",
                "analyzeQuality", false));

        desc.addParameter(new NumericOptimizerParameter("targetQuality",
                "targetQuality", 0.8, -100.0, 1.0));
        
        return desc;
    }

    private int iexp(int base, int exp){
        int result = 1;
        while(exp>0){
            result*=base;
            exp--;
        }
        return result;
    }

    private int[] toRadix(int value, int base) {
        int exp = 0;
        while (iexp(base, exp+1) <= value){
            exp++;
        }

        int radix = 0;
        int result[] = new int[exp+1];

        while (exp>=0) {
            radix = iexp(base, exp);
            result[exp] = value/radix;
            value -= result[exp] * radix;
            exp--;
        }
        return result;
    }

    private double toFractional(int[] number, int base){
        double result = 0;

        for (int i=0;i<number.length;i++){
            int radix = iexp(base, i+1);
            result += (1.0 / (double)radix) * number[i];
        }
        return result;
    }

    @Override
    public void procedure()throws SampleLimitException, ObjectiveAchievedException{
        Sample simplex[] = new Sample[(int)this.getMaxn()];

        int offset = 0;

        if (x0!=null)
            simplex[0] = this.getSample(x0);

        int N = (int)this.getMaxn();

        for (int i=0;i<N;i++){
            if (i==0 && x0 != null){
                simplex[i] = this.getSample(x0);
                continue;
            }

            if (i % 100 == 0 && i > 0 && analyzeQuality){
                this.log("Estimating Quality of sampling (prior optimization).. ");
                double quality = this.factory.getStatistics().calcQuality();
                this.log("Averaged LOO Quality based on E2 is: " + quality);
                this.log("Optimizing interpolation");
                this.factory.getStatistics().optimizeInterpolation();
                quality = this.factory.getStatistics().calcQuality();
                this.log("Estimating Quality of sampling (post optimization).. ");
                this.log("Averaged LOO Quality based on E2 is: " + quality);
                this.log("Target quality is " + targetQuality);

                /*if (targetQuality <= quality){
                    this.log("Finish sampling");
                    break;
                }*/
            }

            double x[] = new double[n];
            
            for (int j=0;j<n;j++){
                int base = primTable[j];
                //generate radix presentation of i with base prim[j]
                //e.g 11 -> p=3 -> 11_3 = 102
                int radix[] = toRadix(i+offset,base);
                //interpret representation as fractional number
                //102 -> 0.201_3
                //convert it to double
                //0.201_3 = 0.704_10
                double w = toFractional(radix,base);
                x[j] = this.lowBound[j] + w*(this.upBound[j] - this.lowBound[j]);
            }

            simplex[i] = this.getSample(x);
        }
    }

    public static void main(String[] args) {
        HaltonSequenceSampling hss = new HaltonSequenceSampling();
        hss.maxn = 100;

        int n = 10;
        int m = 1;

        hss.n = n;
        hss.m = m;
        hss.lowBound = new double[]{0,0,0,0,0,0,0,0,0,0};
        hss.upBound = new double[]{1,1,1,1,1,1,1,1,1,1};
        hss.objNames = new String[]{"y"};
        hss.offset = 0;

        hss.x0 = null;
        hss.setParameterNames(new String[]{"x0,x1,x2,x3,x4,x5,x6,x7,x8,x9"});
        hss.setWorkspace(new File("C:/Arbeit/"));
        hss.setFunction(new AbstractFunction() {

            @Override
            public double[] f(double[] x) {
                return new double[]{1.0};
            }

            @Override
            public void logging(String msg) {
                System.out.println(msg);
            }
        });

        hss.init();

        Arrays.toString(hss.optimize().toArray());
    }
}
