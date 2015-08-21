/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.sampler;

import jams.model.JAMSComponentDescription;
import jams.model.Model;
import optas.core.ObjectiveAchievedException;
import optas.core.SampleLimitException;
import optas.data.DataCollection;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.SampleFactory.Sample;
import optas.optimizer.management.StringOptimizerParameter;
import optas.optimizer.parallel.ParallelSequence;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title = "Random Sampler",
author = "Christian Fischer",
description = "Performs a random search")
public class HaltonSequence extends Optimizer {

    public double offset = 0;
    public boolean analyzeQuality = true;
    public double targetQuality = 0.8;
    public double minn = 0;
    public String excludeFiles = "";    
    public double threadCount = 12;
    public boolean parallelExecution = false;
    transient DataCollection collection = null;
    ParallelSequence pSeq = null;
    
    public void setThreadCount(double threadCount) {
        this.threadCount = (int) threadCount;
    }

    public double getThreadCount() {
        return this.threadCount;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public void setModel(Model model) {
        this.model = model;
    }

    public String getExcludeFiles() {
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles) {
        this.excludeFiles = excludeFiles;
    }

    public double getMinn() {
        return minn;
    }

    public void setMinn(double minn) {
        this.minn = minn;
    }

    public double getOffset() {
        return this.offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setAnalyzeQuality(boolean analyzeQuality) {
        this.analyzeQuality = analyzeQuality;
    }

    public boolean isAnalyzeQuality() {
        return this.analyzeQuality;
    }
    
    public void setParallelExecution(boolean parallelExecution) {
        this.parallelExecution = parallelExecution;
    }

    public boolean isParallelExecution() {
        return this.parallelExecution;
    }

    public void setTargetQuality(double targetQuality) {
        this.targetQuality = targetQuality;
    }

    public double getTargetQuality() {
        return this.targetQuality;
    }
    private int primTable[] = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43,
        47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107,
        109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181,
        191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263,
        269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349,
        353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433,
        439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521,
        523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613,
        617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701,
        709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809,
        811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887,
        907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997,
        1009, 1013, 1019, 1021, 1031, 1033, 1039, 1049, 1051, 1061, 1063, 1069, 1087, 1091,
        1093, 1097, 1103, 1109, 1117, 1123, 1129, 1151, 1153, 1163, 1171, 1181, 1187, 1193,
        1201, 1213, 1217, 1223, 1229, 1231, 1237, 1249, 1259, 1277, 1279, 1283, 1289, 1291,
        1297, 1301, 1303, 1307, 1319, 1321, 1327, 1361, 1367, 1373, 1381, 1399, 1409, 1423,
        1427, 1429, 1433, 1439, 1447, 1451, 1453, 1459, 1471, 1481, 1483, 1487, 1489, 1493,
        1499, 1511, 1523, 1531, 1543, 1549, 1553, 1559, 1567, 1571, 1579, 1583, 1597, 1601,
        1607, 1609, 1613, 1619, 1621, 1627, 1637, 1657, 1663, 1667, 1669, 1693, 1697, 1699,
        1709, 1721, 1723, 1733, 1741, 1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811,
        1823, 1831, 1847, 1861, 1867, 1871, 1873, 1877, 1879, 1889, 1901, 1907, 1913, 1931,
        1933, 1949, 1951, 1973, 1979, 1987, 1993, 1997, 1999, 2003, 2011, 2017, 2027, 2029,
        2039, 2053, 2063, 2069, 2081, 2083, 2087, 2089, 2099, 2111, 2113, 2129, 2131, 2137,
        2141, 2143, 2153, 2161, 2179, 2203, 2207, 2213, 2221, 2237, 2239, 2243, 2251, 2267,
        2269, 2273, 2281, 2287, 2293, 2297, 2309, 2311, 2333, 2339, 2341, 2347, 2351, 2357,
        2371, 2377, 2381, 2383, 2389, 2393, 2399, 2411, 2417, 2423, 2437, 2441, 2447, 2459,
        2467, 2473, 2477, 2503, 2521, 2531, 2539, 2543, 2549, 2551, 2557, 2579, 2591, 2593,
        2609, 2617, 2621, 2633, 2647, 2657, 2659, 2663, 2671, 2677, 2683, 2687, 2689, 2693,
        2699, 2707, 2711, 2713, 2719, 2729, 2731, 2741, 2749, 2753, 2767, 2777, 2789, 2791
    };
    public Sample[] initialSimplex = null;

    @Override
    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(HaltonSequence.class.getSimpleName(), HaltonSequence.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("offset",
                "offset", 0, 0, Integer.MAX_VALUE));

        desc.addParameter(new BooleanOptimizerParameter("analyzeQuality",
                "analyzeQuality", false));
                                
        desc.addParameter(new NumericOptimizerParameter("targetQuality",
                "targetQuality", 0.8, -100.0, 1.0));
        
        desc.addParameter(new BooleanOptimizerParameter("parallelExecution",
                "parallelExecution", false));
        
        desc.addParameter(new StringOptimizerParameter("excludeFiles",
                "excludeFiles","(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log"));

        desc.addParameter(new NumericOptimizerParameter("threadCount",
                "threadCount", 8, 2, 100.0));
        
        
        desc.addParameter(new NumericOptimizerParameter("minn",
                "minn", 100, 0, Integer.MAX_VALUE));
        
        return desc;
    }

    private long iexp(int base, int exp) {
        long result = 1;
        while (exp > 0) {
            result *= base;
            exp--;
        }
        return result;
    }

    private int[] generatePermutation(int base) {
        int map[] = new int[base];
        int set[] = new int[base];

        for (int i = 0; i < base; i++) {
            set[i] = i;
        }
        for (int i = 0; i < base; i++) {
            int index = generator.nextInt(base - i);
            map[i] = set[index];
            set[index] = set[base - i - 1];
        }
        return map;
    }

    private int[] scramble(int[] p, int base) {
        int map[] = generatePermutation(base);
        for (int i = 0; i < p.length; i++) {
            p[i] = map[p[i]];
        }
        return p;
    }

    private int[] toRadix(long value, int base) {
        int exp = 0;
        while (iexp(base, exp + 1) <= value) {
            exp++;
        }

        long radix = 0;
        int result[] = new int[exp + 1];

        while (exp >= 0) {
            radix = iexp(base, exp);
            result[exp] = (int) (value / radix);
            value -= result[exp] * radix;
            exp--;
        }
        return result;
    }

    private double toFractional(int[] number, int base) {
        double result = 0;

        for (int i = 0; i < number.length; i++) {
            long radix = iexp(base, i + 1);
            result += (1.0 / (double) radix) * number[i];
        }
        return result;
    }

    @Override
    public boolean init() {
        if (!super.init()) {
            return false;
        }

        if (parallelExecution){
            pSeq = new ParallelSequence(this);
            pSeq.setExcludeFiles(excludeFiles);
            pSeq.setThreadCount((int)this.threadCount);
        }
        return true;
    }

    

    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException {
        double meanTime = 0;
        double totalExecutionTime = 0;
        double executionCounter = 0;
        double remainingTime = 0;

        long startTime = System.currentTimeMillis();

        int initalSampleSize = this.factory.getSize();

        int samplesPerIteration2 = (int) (threadCount * 6);
        if (samplesPerIteration2 == 0){
            samplesPerIteration2 = 100;
        }
        int i=0;
        while(i<maxn){
            //int currentOffset = (int) offset + (i * samplesPerIteration2);
            //simplex[i] = this.getSample(x);
            
            int sampleCount = (int) Math.min(samplesPerIteration2, this.maxn - i);
            
            double x[][] = new double[sampleCount][];
            for (int j = 0; j < sampleCount; j++) {
                x[j] = new double[n];
                final long L = 409;
                for (int k = 0; k < n; k++) {
                    int base = primTable[k];
                    //generate radix presentation of i with base prim[j]
                    //e.g 11, p=3 -> 11_3 = 102
                    int radix[] = toRadix(L * (long) (i + offset), base);
                    //interpret representation as fractional number
                    //102 -> 0.201_3
                    //convert it to double
                    //0.201_3 = 0.704_10
                    double w = toFractional(/*scramble(radix, base)*/radix, base);
                    x[j][k] = this.lowBound[k] + w * (this.upBound[k] - this.lowBound[k]);
                }
                i++;
            }
                  
            if (parallelExecution) {
                ParallelSequence.OutputData result = pSeq.procedure(x);

                if (collection == null) {
                    collection = result.dc;
                } else {
                    synchronized (collection) {
                        if (result.dc != null) {
                            collection.mergeDataCollections(result.dc);
                        }
                    }
                }
                if (result.list!=null){
                    this.injectSamples(result.list);
                }
                if (collection != null) {                    
                    collection.dump(getModel().getWorkspace().getOutputDataDirectory(), true);
                }
            } else {
                for (int j=0;j<x.length;j++){
                    getSample(x[j]);
                }
            }
            long time2 = System.currentTimeMillis();

            totalExecutionTime = (double) (time2 - startTime) / 1000.0;
            executionCounter = this.factory.getSize() - initalSampleSize;
            meanTime = (double) (totalExecutionTime / executionCounter);

            remainingTime = (this.maxn - executionCounter) * meanTime;

            if (analyzeQuality) {
                double quality = this.factory.getStatistics().calcQuality();
                this.log("Estimating Quality of sampling (prior optimization) with " + this.getStatistics().size() + " samples");
                this.log("Average Quality based on E2 is: " + quality);
                this.log("Target quality is " + targetQuality + " minn:" + this.minn);
                this.log("Mean time per execution is " + meanTime);
                if (remainingTime > 86400) {
                    this.log("Estimated time of finish is in " + remainingTime / 86400 + " days");
                } else if (remainingTime > 3600) {
                    this.log("Estimated time of finish is in " + remainingTime / 3600 + " hrs");
                } else if (remainingTime > 60) {
                    this.log("Estimated time of finish is in " + remainingTime / 60.0 + " min");
                } else {
                    this.log("Estimated time of finish is in " + remainingTime + " sec");
                }

                if (targetQuality <= quality && executionCounter >= this.minn) {
                    this.log("Finish sampling");
                    break;
                }
            }
        }
        if (collection != null) {
            collection.dump(getModel().getWorkspace().getOutputDataDirectory(), false);
        }
    }

    
}
