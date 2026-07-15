/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.parallel;

import optas.core.SampleLimitException;
import optas.optimizer.*;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSModel;
import jams.runtime.StandardRuntime;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import optas.data.DataCollection;
import optas.core.ObjectiveAchievedException;
import optas.optimizer.management.SampleFactory.Sample;
import optas.io.ImportMonteCarloData;
import optas.io.ImportMonteCarloException;

@SuppressWarnings("unchecked")
@JAMSComponentDescription(
        title="Random Sampler",
        author="Christian Fischer",
        description="Performs a random search"
        )
public class ParallelSequence implements Serializable {

    /**
     * @return the threadCount
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @param threadCount the threadCount to set
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    static public class InputData implements Serializable {        
        public Optimizer context;
        public double x[][];
        int start,end;
        public InputData(double x[][],int start,int end, Optimizer context) {
            this.context = context;
            this.start = start;
            this.end = end;
            this.x = x;
        }
    }

    static public class OutputData implements Serializable {

        public DataCollection dc;
        int start;
        public ArrayList<Sample> list;

        public OutputData(ArrayList<Sample> list) {
            dc = null;
            this.list = list;
        }

        public void add(String context, File dataStore) {
            try {
                ImportMonteCarloData importer = new ImportMonteCarloData();
                importer.addFile(dataStore);
                DataCollection nextDC = importer.getEnsemble();
                if (nextDC == null) {
                    return;
                }
                if (dc == null) {
                    dc = importer.getEnsemble();
                } else {
                    dc.unifyDataCollections(importer.getEnsemble());
                }
            }catch(ImportMonteCarloException imce){
                imce.printStackTrace();
                System.out.println(imce);
            }
        }
    }
        
    public String excludeFiles = "(.*\\.cache)|(.*\\.jam)|(.*\\.ser)|(.*\\.svn)|(.*output.*\\.dat)|.*\\.cdat|.*\\.log";
    private int threadCount = 12;
    Optimizer context;
        
    public String getExcludeFiles(){
        return excludeFiles;
    }

    public void setExcludeFiles(String excludeFiles){
        this.excludeFiles = excludeFiles;
    }

    public ParallelSequence(Optimizer context){
        this.context = context;
        init(context);
    }

    private boolean init(Optimizer context){
        String libs[] = null;
        if (context.getModel().getRuntime() instanceof StandardRuntime)
             libs = ((StandardRuntime)context.getModel().getRuntime()).getLibs();

        if (libs != null) {
            for (int i = 0; i < libs.length; i++) {
                String lib = libs[i];
                File fileToLib = new File(lib);
                if (fileToLib.exists()) {
                    ParallelExecution.addJarsToClassPath(ClassLoader.getSystemClassLoader(), fileToLib);
                }
            }
        }else{
            System.out.println("Warning: no libary path was specified");
        }

        return true;
    }

    public static class ParallelSequenceTask extends ParallelTask<InputData, OutputData> implements Serializable{

        public ArrayList<ParallelJob> split(InputData taskArgument, int gridSize) {
            ArrayList<ParallelJob> jobs = new ArrayList<ParallelJob>(gridSize);

            int iterationsPerJob = (int)Math.ceil((double)taskArgument.x.length / (double)gridSize);
            while(iterationsPerJob*(gridSize-1) >= taskArgument.x.length){
                gridSize--;
            }
            InputData jobsData[] = new InputData[gridSize];

            int start = 0;
            int end   = 0;

            for (int i = 0; i < gridSize; i++) {                
                end = Math.min(start+iterationsPerJob, taskArgument.x.length);

                jobsData[i] = new InputData(taskArgument.x,start, end, taskArgument.context);
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
                        OutputData result = ParallelSequence.parallelExecute(arg);

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
                    if (results.get(i).dc != null)
                        merged.dc.mergeDataCollections(results.get(i).dc);
                }
                OutputData result = results.get(i);
                //merged.list.addAll(results.get(i).list);
                for (int j=0;j<result.list.size();j++){
                    while(merged.list.size()<result.start+j+1){
                        merged.list.add(result.list.get(j));
                    }
                    merged.list.set(result.start+j, result.list.get(j));
                }
            }
            System.out.println("reduce_function_finished");            
            return merged;
        }
    }

    transient ParallelExecution<InputData, OutputData> executor = null;
    public OutputData procedure(double x[][]) throws SampleLimitException, ObjectiveAchievedException {
        OutputData result = null;
        if (executor==null) //das ist gut, aber muss noch gesichert werden und so ... 
            executor = new ParallelExecution<InputData, OutputData>(context.getModel().getWorkspaceDirectory(), excludeFiles);

        InputData param = new InputData(x, 0, x.length, context);
        result = executor.execute(param, new ParallelSequenceTask(), getThreadCount());
        return result;
    }
            
    static private OutputData parallelExecute(InputData in) {        
        ArrayList<Sample> sampleList = new ArrayList<Sample>();
        try{
            for (int i=in.start;i<in.end;i++)
                sampleList.add(in.context.getSample(in.x[i]));            
        }catch(SampleLimitException sle){
            sle.printStackTrace();
        }catch(ObjectiveAchievedException oae){
            oae.printStackTrace();
        }        
        //finish this thread ..
        try{
            in.context.getModel().cleanup();
            in.context.getModel().getWorkspace().close();
        }catch(Exception e){
            e.printStackTrace();
        }
        OutputData data = new OutputData(sampleList);
        data.start = in.start;
        //nach output files suchen
        File outputDataDir = in.context.getModel().getWorkspace().getOutputDataDirectory();
        
        File list[] = outputDataDir.listFiles();
        if (list!=null){
            for (int i = 0; i < list.length; i++) {
                if (list[i].getName().endsWith("dat")){
                    data.add(list[i].getName(), list[i]);
                }
            }
        }
        
        return data;
    }
}
