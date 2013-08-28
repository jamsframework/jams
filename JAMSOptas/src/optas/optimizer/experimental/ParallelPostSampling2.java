/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.experimental;

import optas.core.SampleLimitException;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSModel;
import jams.runtime.StandardRuntime;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import optas.datamining.FindLargestEmptyRectangle;
import optas.data.DataCollection;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.SampleFactory.Sample;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.parallel.ParallelExecution;
import optas.optimizer.parallel.ParallelJob;
import optas.optimizer.parallel.ParallelTask;
import optas.io.ImportMonteCarloData;
import optas.io.ImportMonteCarloException;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class ParallelPostSampling2 extends Optimizer{

    /**
     * @return the peps
     */
    public double getPeps() {
        return peps;
    }

    /**
     * @param peps the peps to set
     */
    public void setPeps(double peps) {
        this.peps = peps;
    }

    static public class InputData implements Serializable {
        public int sampleCount;
        public int offset;
        public long seed;
        public double[] lb,ub;
        public ParallelPostSampling2 context;

        public InputData(int offset, int sampleCount, double lowBound[], double upBound[], ParallelPostSampling2 context) {
            this.offset = offset;
            this.seed = generator.nextLong();
            this.sampleCount = sampleCount;
            this.lb = lowBound;
            this.ub = upBound;
            this.context = context;
        }
    }

    static public class OutputData implements Serializable {

        public DataCollection dc;
        public ArrayList<Sample> list;

        public OutputData(ArrayList<Sample> list) {
            dc = null;
            this.list = list;
        }

        public void add(String context, File dataStore) {
            ImportMonteCarloData importer = new ImportMonteCarloData();
            try {
                importer.addFile(dataStore);
                if (dc == null) {
                    dc = importer.getEnsemble();
                } else {
                    dc.unifyDataCollections(importer.getEnsemble());
                }
            } catch (ImportMonteCarloException imce) {
                imce.printStackTrace();
                System.out.println(imce);
            }
        }
    }

    static public class EstimateBoundsInputData implements Serializable {
        public ParallelPostSampling2 context;
        public int index;
        public EstimateBoundsInputData(ParallelPostSampling2 context, int index) {
            this.context = context;
            this.index   = index;
        }
    }

    static public class EstimateBoundsOutputData implements Serializable {

        public DataCollection dc;
        public ArrayList<Sample> list;
        public double minBound[];
        public double maxBound[];
        public int index;
        public EstimateBoundsOutputData(ArrayList<Sample> list, int index, double minBound[], double maxBound[]) {
            dc = null;
            this.list = list;
            this.minBound = minBound;
            this.maxBound = maxBound;
            this.index = index;
        }

        public void add(String context, File dataStore) {
            ImportMonteCarloData importer = new ImportMonteCarloData();
            try {
                importer.addFile(dataStore);
                if (dc == null) {
                    dc = importer.getEnsemble();
                } else {
                    dc.unifyDataCollections(importer.getEnsemble());
                }
            } catch (ImportMonteCarloException imce) {
                imce.printStackTrace();
                System.out.println(imce);
            }
        }
    }
    
    public double offset = 0;
    public boolean analyzeQuality = true;
    public double targetQuality = 0.8;
    public double minn = 100;
    public String excludeFiles = "";    
    public double threadCount = 12;
    public double successRate = 0.6;

    public double thresholdMul = 0.1;
    public double thresholdAdd = 0;//100;

    //range erstimation
    private double range[][];
    public double peps = 0.001;
    private double finalRange[][] = null;

    public void setThreadCount(double threadCount){
        this.threadCount = (int)threadCount;
    }

    public double getThreadCount(){
        return this.threadCount;
    }
    
    public String getExcludeFiles(){
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles){
        this.excludeFiles = excludeFiles;
    }

    public double getMinn(){
        return minn;
    }
    public void setMinn(double minn){
        this.minn = minn;
    }
    
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

    public void setThresholdMul(double threshold){
        this.thresholdMul = threshold;
    }

    public double getThresholdMul(){
        return thresholdMul;
    }

    public void setThresholdAdd(double threshold){
        this.thresholdAdd = threshold;
    }

    public double getThresholdAdd(){
        return thresholdAdd;
    }

    public double[][] getFinalRange(){
        return this.finalRange;
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
    
    @Override
    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(ParallelPostSampling2.class.getSimpleName(), ParallelPostSampling2.class.getName(), 500, false);

        desc.addParameter(new NumericOptimizerParameter("offset",
                "offset", 0, 0, Integer.MAX_VALUE));

        desc.addParameter(new BooleanOptimizerParameter("analyzeQuality",
                "analyzeQuality", false));

        desc.addParameter(new NumericOptimizerParameter("targetQuality",
                "targetQuality", 0.8, -100.0, 1.0));
        
        return desc;
    }

    private long iexp(int base, int exp){
        long result = 1;
        while(exp>0){
            result*=base;
            exp--;
        }
        return result;
    }

    private int[] toRadix(long value, int base) {
        int exp = 0;
        while (iexp(base, exp+1) <= value){
            exp++;
        }

        long radix = 0;
        int result[] = new int[exp+1];

        while (exp>=0) {
            radix = iexp(base, exp);
            result[exp] = (int)(value/radix);            
            value -= result[exp] * radix;
            exp--;
        }
        return result;
    }

    private double toFractional(int[] number, int base){
        double result = 0;

        for (int i=0;i<number.length;i++){
            long radix = iexp(base, i+1);
            result += (1.0 / (double)radix) * number[i];
        }
        return result;
    }
    
    Sample y0 = null;
    private boolean isSampleValid(Sample s){
        if (y0 == null){
            try{
                y0 = this.getSample(x0[0]);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        for (int j=0;j<m;j++){
            double threshold_j = this.thresholdAdd + (1.0+this.thresholdMul)*this.y0.F()[j];
            if ( s.F()[j] > threshold_j ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean init(){
        if (!super.init())
            return false;

        String libs[] = null;
        if (this.getModel().getRuntime() instanceof StandardRuntime)
             libs = ((StandardRuntime)this.getModel().getRuntime()).getLibs();

        if (libs != null) {
            for (int i = 0; i < libs.length; i++) {
                String lib = libs[i];
                File fileToLib = new File(lib);
                if (fileToLib.exists()) {
                    ParallelExecution.addJarsToClassPath(ClassLoader.getSystemClassLoader(), fileToLib);
                }
            }
        }else{
            log("Warning: no libary path was specified");
        }

        return true;
    }

    public static class ParallelEstimateBoundsTask extends ParallelTask<EstimateBoundsInputData, EstimateBoundsOutputData> implements Serializable{

        public ArrayList<ParallelJob> split(EstimateBoundsInputData taskArgument, int gridSize) {
            ArrayList<ParallelJob> jobs = new ArrayList<ParallelJob>(gridSize);

            EstimateBoundsInputData jobsData[] = new EstimateBoundsInputData[taskArgument.context.n];

            for (int i = 0; i < taskArgument.context.n; i++) {
                jobsData[i] = new EstimateBoundsInputData(taskArgument.context,i);
            }

            for (int i = 0; i < jobsData.length; i++) {
                // Pass in value to check, and minimum/maximum range boundaries
                // into job as arguments.

                jobs.add(new ParallelJob<EstimateBoundsInputData, EstimateBoundsOutputData>(jobsData[i]) {

                    @Override
                    public void moveWorkspace(File newWorkspace) {
                        ((JAMSModel) arg.context.getModel()).moveWorkspaceDirectory(newWorkspace.getAbsolutePath());
                    }

                    @Override
                    public EstimateBoundsOutputData execute() {
                        EstimateBoundsOutputData result = ParallelPostSampling2.parallelExecuteEstimateBounds(arg);

                        return result;
                    }
                });
            }

            // List of jobs to be executed on the grid.
            return jobs;
        }

        @Override
        public EstimateBoundsOutputData reduce(ArrayList<EstimateBoundsOutputData> results) {
            System.out.println("reduce_function_started with " + results.size() + " results!");

            if (results.isEmpty())
                return new EstimateBoundsOutputData(new ArrayList<Sample>(),-1,null, null);

            EstimateBoundsOutputData merged = new EstimateBoundsOutputData(new ArrayList<Sample>(),-1,new double[results.get(0).minBound.length], new double[results.get(0).minBound.length]);
            for (int i = 0; i < results.size(); i++) {
                EstimateBoundsOutputData result = results.get(i);
                if (merged.dc == null) {
                    merged.dc = result.dc;
                } else {
                    if (result.dc != null)
                        merged.dc.mergeDataCollections(result.dc);
                }
                merged.list.addAll(result.list);
                System.out.println("putting bounds for index:" + result.index);
                merged.minBound[result.index] = result.minBound[result.index];
                merged.maxBound[result.index] = result.maxBound[result.index];
            }
            System.out.println("reduce_function_finished");
            return merged;
        }
    }

    public static class ParallelHaltonSequenceSamplingTask extends ParallelTask<InputData, OutputData> implements Serializable{

        @Override
        public ArrayList<ParallelJob> split(InputData taskArgument, int gridSize) {
            ArrayList<ParallelJob> jobs = new ArrayList<ParallelJob>(gridSize);

            int iterationsPerJob = (int)Math.ceil((double)taskArgument.sampleCount / (double)gridSize);
            while(iterationsPerJob*(gridSize-1) >= taskArgument.sampleCount){
                gridSize--;
            }
            InputData jobsData[] = new InputData[gridSize];

            int start = taskArgument.offset;
            int end   = start;

            for (int i = 0; i < gridSize; i++) {                
                end = Math.min(start+iterationsPerJob, taskArgument.offset+taskArgument.sampleCount);

                jobsData[i] = new InputData(start, end-start, taskArgument.lb, taskArgument.ub, taskArgument.context);
                start = end;
            }

            for (int i = 0; i < jobsData.length; i++) {
                // Pass in value to check, and minimum/maximum range boundaries
                // into job as arguments.

                jobs.add(new ParallelJob<InputData, OutputData>(jobsData[i]) {

                    @Override
                    public void moveWorkspace(File newWorkspace) {
                        ((JAMSModel) arg.context.getModel()).moveWorkspaceDirectory(newWorkspace.getAbsolutePath());
                    }

                    @Override
                    public OutputData execute() {
                        OutputData result = ParallelPostSampling2.parallelExecuteSampling(arg);

                        return result;
                    }
                });
            }

            // List of jobs to be executed on the grid.
            return jobs;
        }

        @Override
        public OutputData reduce(ArrayList<OutputData> results) {
            System.out.println("reduce_function_started_");
            
            OutputData merged = new OutputData(new ArrayList<Sample>());
            for (int i = 0; i < results.size(); i++) {
                if (merged.dc == null) {
                    merged.dc = results.get(i).dc;
                } else {
                    if (results.get(i).dc!=null)
                        merged.dc.mergeDataCollections(results.get(i).dc);
                }
                merged.list.addAll(results.get(i).list);
            }
            System.out.println("reduce_function_finished");
            return merged;
        }
    }

    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException {
        double bounds[][] = stageEstimateInitalBounds();
        System.out.println("Initial Bounds:");
        System.out.println("lb:" + Arrays.toString(bounds[0]));
        System.out.println("ub:" + Arrays.toString(bounds[1]));

        double success = 0.0;
        do{
            System.out.println("Current bounds are inacceptable .. begin sampling data!");
            success = stageSample(bounds[0],bounds[1],100);
            System.out.println("Current success rate is " + success*100 + "%" + " required are at least 60%");
            double bounds_next[][] = findLargestEmptyRectangle(bounds[0],bounds[1]);
            bounds[0] = bounds_next[0];
            bounds[1] = bounds_next[1];
            System.out.println("Next bounds are:");
            System.out.println("lb:" + Arrays.toString(bounds[0]));
            System.out.println("ub:" + Arrays.toString(bounds[1]));
        }while(success < 0.6);
        this.finalRange = bounds;
        System.out.println("Current bounds are acceptable .. stop sampling data!");
    }

    public double[][] stageEstimateInitalBounds() throws SampleLimitException, ObjectiveAchievedException {
        //first step estimate bounds
        EstimateBoundsOutputData result = null;
        DataCollection collection = null;
        ParallelExecution<EstimateBoundsInputData, EstimateBoundsOutputData> executor = new ParallelExecution<EstimateBoundsInputData, EstimateBoundsOutputData>(getWorkspace(), excludeFiles);

        EstimateBoundsInputData param = new EstimateBoundsInputData(this, -1);
        result = executor.execute(param, new ParallelEstimateBoundsTask(), n);

        try {
            this.injectSamples(result.list);
        } catch (SampleLimitException sle) {
            throw sle;
        } catch (ObjectiveAchievedException sle) {
            throw sle;
        }finally{
            if (collection != null) {
                collection.dump(getModel().getWorkspace().getOutputDataDirectory(),false);
            }
        }
        return new double[][]{result.minBound, result.maxBound};
    }

    public double stageSample(double minBound[], double maxBound[], int maxn) throws SampleLimitException, ObjectiveAchievedException {
        double meanTime = 0;
        double totalExecutionTime = 0;
        double executionCounter = 0;
        double remainingTime = 0;

        long startTime = System.currentTimeMillis();
        double validSampleCount = 0;

        OutputData result = null;
        DataCollection collection = null;
        ParallelExecution<InputData, OutputData> executor = new ParallelExecution<InputData, OutputData>(getWorkspace(), excludeFiles);

        int initalSampleSize = this.factory.getSize();

        int samplesPerIteration = (int)(threadCount*6);

        for (int i = 0; i < Math.ceil((double)maxn / (double)samplesPerIteration); i++) {

            int currentOffset      = (int)offset + (i * samplesPerIteration) + this.getSamples().size();
            int sampleCount        = (int)Math.min( samplesPerIteration, maxn - i * samplesPerIteration);

            InputData param = new InputData(currentOffset, sampleCount, minBound, maxBound, this);

            result = executor.execute(param, new ParallelHaltonSequenceSamplingTask(), (int)threadCount);
            try{
                for (int j=0;j<result.list.size();j++){
                    if (isSampleValid(result.list.get(j)))
                        validSampleCount += 1.0;
                }
                this.injectSamples(result.list);
            }catch(SampleLimitException sle){
                throw sle;
            }catch(ObjectiveAchievedException sle){
                throw sle;
            }finally{
                if (collection != null) {
                    collection.dump(getModel().getWorkspace().getOutputDataDirectory(),false);
                }
            }

            if (collection == null) {
                collection = result.dc;
            } else {
                collection.mergeDataCollections(result.dc);
            }

            long time2 = System.currentTimeMillis();

            totalExecutionTime = (double) (time2 - startTime) / 1000.0;
            executionCounter = this.factory.getSize() - initalSampleSize;
            meanTime = (double) (totalExecutionTime / executionCounter);

            remainingTime = (maxn - executionCounter) * meanTime;

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
            if (result != null) {
                //result.dc.dump(getModel().getWorkspace().getOutputDataDirectory());
            }
        }
        if (collection != null) {
            collection.dump(getModel().getWorkspace().getOutputDataDirectory(),false);
        }

        return validSampleCount/maxn;
    }

    
    public void procedureSampling(long seed, int offset)throws SampleLimitException, ObjectiveAchievedException{
        Sample simplex[] = new Sample[(int)this.getMaxn()];
                        
        int N = (int)this.getMaxn();

        if (generator == null){
            generator = new Random(seed);
        }else{
            generator.setSeed(seed);
        }

        for (int i=0;i<N;i++){
            if (x0 != null && i<x0.length && offset == 0){
                simplex[i] = this.getSample(x0[i]);
                continue;
            }            
            
            double x[] = new double[n];
            final long L = 409;
            for (int j=0;j<n;j++){
                int base = primTable[j];
                //generate radix presentation of i with base prim[j]
                //e.g 11, p=3 -> 11_3 = 102
                int radix[] = toRadix(L*(long)(i+offset),base);
                //interpret representation as fractional number
                //102 -> 0.201_3
                //convert it to double
                //0.201_3 = 0.704_10
                double w = toFractional(/*scramble(radix, base)*/radix,base);
                x[j] = this.lowBound[j] + w*(this.upBound[j] - this.lowBound[j]);
            }
                        
            simplex[i] = this.getSample(x);
            System.out.println("Sample (" + offset + "):" + simplex[i].toString());
        }
    }

    public void procedureEstimateBounds(int index) throws SampleLimitException, ObjectiveAchievedException {
        System.out.println("Starting estimate bounds for index " + index + " threshold is +" + this.thresholdAdd + " *" + this.thresholdMul);

        double minBound=0;
        double maxBound=0;

        try {                       
            double lplus = 0.5, lminus = 0.5;
            int ITER = -(int)Math.ceil(Math.log(this.peps) / Math.log(2.0));

            minBound = x0[0][index];
            maxBound = x0[0][index];

            for (int k = 1; k <= ITER; k++) {
                double xiplus[] = new double[n];
                double ximinus[] = new double[n];
                xiplus  = reTransform(Arrays.copyOf(x0[0], n));
                ximinus = reTransform(Arrays.copyOf(x0[0], n));

                xiplus[index] = reTransform(x0[0])[index] + lplus*(1.0-reTransform(x0[0])[index]);
                ximinus[index] = reTransform(x0[0])[index] - lminus*(reTransform(x0[0])[index]);

                Sample sPlus = this.getSample(transform(xiplus));
                Sample sMinus = this.getSample(transform(ximinus));
                
                boolean isValidPlus = isSampleValid(sPlus);
                boolean isValidMinus = isSampleValid(sMinus);
                
                if (isValidPlus) {
                    System.out.println("Accept +");
                    lplus = lplus + Math.pow(2.0,-k-1);
                    maxBound = transform(xiplus)[index];
                } else {
                    lplus = lplus - Math.pow(2.0,-k-1);
                }

                if (isValidMinus) {
                    System.out.println("Accept -");
                    lminus = lminus + Math.pow(2.0,-k-1);
                    minBound = transform(ximinus)[index];
                } else {
                    lminus = lminus - Math.pow(2.0,-k-1);
                }
            }
        } catch (SampleLimitException sle) {
            sle.printStackTrace();
        } catch (Throwable sle) {
            sle.printStackTrace();
        }

        if (range == null)
            this.range = new double[2][n];
        else
            System.out.println("range allready there?!");
        System.out.println("Accept ranges for index " + index + "[" + minBound + "," + maxBound + "]");
        this.range[0][index] = minBound;
        this.range[1][index] = maxBound;
    }

    static public OutputData parallelExecuteSampling(InputData in) {
        in.context.offset = in.offset;
        in.context.maxn = in.sampleCount;
        in.context.minn = in.sampleCount;
        in.context.setMaxn(0);
        //in.context.sampleCount = 0;
        //in.context.setBoundaries(in.lb, in.ub);
        
        try{
            in.context.procedureSampling(in.seed,in.offset);
        }catch(SampleLimitException sle){
            
        }catch(ObjectiveAchievedException oae){

        }
        
        in.context.log("finished myjob");
        OutputData data = new OutputData(in.context.factory.getSampleList());
        //nach output files suchen
        File outputDataDir = in.context.getModel().getWorkspace().getOutputDataDirectory();
        File list[] = outputDataDir.listFiles();
        if (list!=null){
            for (int i = 0; i < list.length; i++) {
                if (list[i].getName().endsWith("dat"))
                    data.add(list[i].getName(), list[i]);
            }
        }       
        return data;
    }
        
     private double[] transform(double x[]){
        double[] y = new double[n];
        for (int i=0;i<n;i++){
            y[i] = lowBound[i] + x[i]*(upBound[i]-lowBound[i]);
        }
        return y;
    }

    private double[] reTransform(double y[]){
        double[] x = new double[n];
        for (int i=0;i<n;i++){
            if (upBound[i]-lowBound[i] > 0)
                x[i] = (y[i]-lowBound[i])/(upBound[i]-lowBound[i]);
            else
                x[i] = (y[i]-lowBound[i]);
        }
        return x;
    }
    
    static public EstimateBoundsOutputData parallelExecuteEstimateBounds(EstimateBoundsInputData in) {
        in.context.maxn = -2*in.context.n*(int)Math.ceil(Math.log(in.context.peps) / Math.log(2.0)) + 50;
        try{
            in.context.procedureEstimateBounds(in.index);
        }catch(SampleLimitException sle){

        }catch(ObjectiveAchievedException oae){

        }

        in.context.log("finished myjob");
        //finish this thread ..
        try{
            in.context.getModel().cleanup();
            in.context.getModel().getWorkspace().close();
        }catch(Exception e){
            e.printStackTrace();
        }
        EstimateBoundsOutputData data = new EstimateBoundsOutputData(in.context.factory.getSampleList(),in.index,in.context.range[0],in.context.range[1]);
        //nach output files suchen
        File outputDataDir = in.context.getModel().getWorkspace().getOutputDataDirectory();
        File list[] = outputDataDir.listFiles();
        if (list!=null){
            for (int i = 0; i < list.length; i++) {
                if (list[i].getName().endsWith("dat"))
                    data.add(list[i].getName(), list[i]);
            }
        }
        return data;
    }
        
    
    public double[][] findLargestEmptyRectangle(double FLERlowBound[], double FLERupBound[]){
        Sample best = this.getSolution().get(0);
        
        ArrayList<Sample> excludeList = new ArrayList<Sample>();
        for (Sample s: this.getSamples()){
            if (!isSampleValid(s)){
                excludeList.add(s);
                System.out.println(s);
            }
        }
        System.out.println("Exclude list contains:" + excludeList.size() + " samples");
                        
        double pointList[][] = new double[excludeList.size()][];
        for (int i=0;i<excludeList.size();i++){
            pointList[i] = excludeList.get(i).x;
        }                
        FindLargestEmptyRectangle FLER = new FindLargestEmptyRectangle(best.x, FLERlowBound, FLERupBound, pointList);
        return FLER.getFLER();
    }

    /*public static void main(String[] args) {
        ParallelPostSampling2 hss = new ParallelPostSampling2();
        hss.maxn = 500;

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
    }*/
}
