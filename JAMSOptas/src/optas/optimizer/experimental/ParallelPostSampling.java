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
import optas.data.DataCollection;
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
public class ParallelPostSampling extends Optimizer{    

    static public class EstimateBoundsInputData implements Serializable {
        public ParallelPostSampling context;
        public int index;
        public EstimateBoundsInputData(ParallelPostSampling context, int index) {
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

    final int COUNT = 8;
        
    public String excludeFiles = "";
    public double threshold = 0.1;
    public double range[][];

    public double[][] getRange(){
        return range;
    }
    
    public String getExcludeFiles(){
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles){
        this.excludeFiles = excludeFiles;
    }
    
    public OptimizerDescription getDescription() {
        OptimizerDescription desc = OptimizerLibrary.getDefaultOptimizerDescription(ParallelPostSampling.class.getSimpleName(), ParallelPostSampling.class.getName(), 500, false);
                
        return desc;
    }

    public void setThreshold(double threshold){
        this.threshold = threshold;
    }

    public double getThreshold(){
        return threshold;
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

                    public void moveWorkspace(File newWorkspace) {
                        ((JAMSModel) arg.context.getModel()).moveWorkspaceDirectory(newWorkspace.getAbsolutePath());
                    }

                    public EstimateBoundsOutputData execute() {
                        EstimateBoundsOutputData result = ParallelPostSampling.parallelExecute(arg);

                        return result;
                    }
                });
            }

            // List of jobs to be executed on the grid.
            return jobs;
        }

        public EstimateBoundsOutputData reduce(ArrayList<EstimateBoundsOutputData> results) {
            System.out.println("reduce_function_started_");

            if (results.isEmpty())
                return new EstimateBoundsOutputData(new ArrayList<Sample>(),-1,null, null);

            EstimateBoundsOutputData merged = new EstimateBoundsOutputData(new ArrayList<Sample>(),-1,new double[results.size()], new double[results.size()]);
            for (int i = 0; i < results.size(); i++) {
                EstimateBoundsOutputData result = results.get(i);
                if (merged.dc == null) {
                    merged.dc = result.dc;
                } else {
                    merged.dc.mergeDataCollections(result.dc);
                }
                merged.list.addAll(results.get(i).list);
                merged.minBound[result.index] = result.minBound[result.index];
                merged.maxBound[result.index] = result.maxBound[result.index];
            }
            System.out.println("reduce_function_finished");
            return merged;
        }
    }

    @Override
    public void procedure() throws SampleLimitException, ObjectiveAchievedException {
        //first step estimate bounds
        EstimateBoundsOutputData result = null;
        DataCollection collection = null;
        ParallelExecution<EstimateBoundsInputData, EstimateBoundsOutputData> executor = new ParallelExecution<EstimateBoundsInputData, EstimateBoundsOutputData>(getWorkspace(), excludeFiles);

        EstimateBoundsInputData param = new EstimateBoundsInputData(this, -1);
        result = executor.execute(param, new ParallelEstimateBoundsTask(), n);

        this.range = new double[][]{result.minBound, result.maxBound};
                
        this.injectSamples(result.list);        
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
    
    public void procedure2(int index) throws SampleLimitException, ObjectiveAchievedException {
        double minBound=0;
        double maxBound=0;

        try {
            Sample y0 = this.getSample(x0[0]);

            double lplus = 0.5, lminus = 0.5;

            for (int k = 0; k < 8; k++) {
                double xiplus[] = new double[n];
                double ximinus[] = new double[n];
                xiplus  = reTransform(Arrays.copyOf(x0[0], n));
                ximinus = reTransform(Arrays.copyOf(x0[0], n));

                xiplus[index] = reTransform(x0[0])[index] + lplus*(1.0-reTransform(x0[0])[index]);
                ximinus[index] = reTransform(x0[0])[index] - lminus*(reTransform(x0[0])[index]);
                                                
                Sample sPlus = this.getSample(transform(xiplus));
                Sample sMinus = this.getSample(transform(ximinus));
                System.out.println(Arrays.toString(ximinus));
                boolean isValidPlus = true;
                boolean isValidMinus = true;
                
                for (int j = 0; j < m; j++) {
                    if (!(sPlus.F()[j] < (1.0 + threshold) * y0.F()[j])) {
                        isValidPlus = false;
                    }
                    if (!(sMinus.F()[j] < (1.0 + threshold) * y0.F()[j])) {
                        isValidMinus = false;
                    }
                }
                if (isValidPlus) {
                    lplus = lplus + Math.pow(2.0,-k-2);
                    maxBound = transform(xiplus)[index];
                } else {
                    lplus = lplus - Math.pow(2.0,-k-2);
                }

                if (isValidMinus) {
                    lminus = lminus + Math.pow(2.0,-k-2);
                    minBound = transform(ximinus)[index];
                } else {
                    lminus = lminus - Math.pow(2.0,-k-2);
                }
            }
        } catch (SampleLimitException sle) {
            sle.printStackTrace();
        } catch (Throwable sle) {
            sle.printStackTrace();
        }
        this.range = new double[2][n];
        this.range[0][index] = minBound;
        this.range[1][index] = maxBound;
    }
    
    static public EstimateBoundsOutputData parallelExecute(EstimateBoundsInputData in) {
        in.context.maxn = 100;
        try{
            in.context.procedure2(in.index);
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
        EstimateBoundsOutputData data = new EstimateBoundsOutputData(in.context.factory.getSampleList(),in.index,in.context.getRange()[0],in.context.getRange()[1]);
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
        
    /*public static void main(String[] args) {
        ParallelPostSampling hss = new ParallelPostSampling();
        hss.maxn = 500;

        int n = 10;
        int m = 1;

        hss.n = n;
        hss.m = m;
        hss.lowBound = new double[]{0,0,0,0,0,0,0,0,0,0};
        hss.upBound = new double[]{1,1,1,1,1,1,1,1,1,1};
        hss.objNames = new String[]{"y"};

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
