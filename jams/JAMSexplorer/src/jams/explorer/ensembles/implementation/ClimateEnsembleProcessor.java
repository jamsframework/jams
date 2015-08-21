/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.implementation;

import jams.data.Attribute;
import jams.explorer.ensembles.api.*;
import jams.explorer.ensembles.gui.EnsembleControlPanel;
import jams.workspace.dsproc.AbstractDataStoreProcessor.AttributeData;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.DataStoreProcessor;
import jams.workspace.dsproc.TimeSpaceProcessor;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author christian
 * @param <T>
 */
public class ClimateEnsembleProcessor implements EnsembleProcessor<ClimateModel, ClimateEnsemble> {

    static final Logger logger = Logger.getLogger(ClimateEnsembleProcessor.class.getName());    
    {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    
    ClimateEnsemble ensemble = null;
    String outputDatastoreName = null;

    HashMap<ClimateModel, TimeSpaceProcessor> procs = new HashMap<ClimateModel, TimeSpaceProcessor>();

    public ClimateEnsembleProcessor(ClimateEnsemble ensemble, String output) {
        this.ensemble = ensemble;
        this.outputDatastoreName = output;
    }

    @Override
    public int getModelCount() {
        return ensemble.getSize();
    }

    public int getNettoModelCount(String outputName) {
        int count = 0;
        for (ClimateModel model : ensemble.getModelSet()) {
            for (String outputDirectory : model.outputFiles.keySet()) {
                if (!model.isOutputSelected(outputDirectory)) {
                    continue;
                }
                File outputDatastore = new File(new File(new File(model.getLocation(), "output"), outputDirectory), outputName);
                if (!outputDatastore.exists()) {
                    continue;
                }
                count++;
            }
        }
        return count;
    }

    public void init() {
        for (ClimateModel model : ensemble.getModelSet()) {
            DataStoreProcessor dsProcs[] = model.getDataStoreProcessors(outputDatastoreName);
            for (DataStoreProcessor dsProc : dsProcs) {
                if (dsProc != null && dsProc.isTimeSpaceDatastore()) {
                    try {
                        dsProc.createDB();
                    } catch (Exception e) {
                        if (dsProc != null && dsProc.getFile()!=null)
                            logger.log(Level.SEVERE, "During initialisation of %1 an error occured.".replace("%1", dsProc.getFile().getAbsolutePath())  );
                        else
                            logger.log(Level.SEVERE, "dsProc is null"  );
                    }
                    TimeSpaceProcessor tsp = new TimeSpaceProcessor(dsProc);
                    procs.put(model, tsp);
                }
            }
        }
    }

    public void close() throws SQLException {
        for (TimeSpaceProcessor tsp : procs.values()) {
            tsp.close();
        }
    }

    public boolean isModelOutputValid(ClimateModel model) {
        return procs.get(model) != null;
    }

    @Override
    public Attribute.Calendar[] getTimeDomain() throws SQLException {
        TreeSet<Attribute.Calendar> result = new TreeSet<Attribute.Calendar>();
        if (!procs.isEmpty()){
            result.addAll(Arrays.asList(procs.values().iterator().next().getTimeSteps()));
        
            for (ClimateModel model : procs.keySet()) {
                //result.addAll(Arrays.asList(procs.get(model).getTimeSteps()));
                result.retainAll(Arrays.asList(procs.get(model).getTimeSteps()));
            }
        }
        return result.toArray(new Attribute.Calendar[0]);
    }

    //@Override
    public double[][] getTimeSlice(long entityIds[], String dateIds) throws SQLException, IOException {
        double result[][] = new double[ensemble.getSize()][entityIds.length];
        int j = 0;
        
        for (ClimateModel model : procs.keySet()) {
            TimeSpaceProcessor tsp = this.procs.get(model);

            if (tsp != null) {
                for (AttributeData a : tsp.getDataStoreProcessor().getAttributes()) {
                    a.setSelected(true);
                }
                
                DataMatrix matrix = procs.get(model).getCrossProduct(entityIds, new String[]{dateIds});
                for (int i = 0; i < entityIds.length; i++) {
                    result[j][i] = matrix.get(0, i);
                }
            }

            j++;
        }
        return result;
    }

     private String date(){
        return "[" + (new Date()).getTime() + "]";
    }
     
    //@Override
    public double[][] getModelSlice(ClimateModel model) throws SQLException, IOException {
        System.out.println(date() + ":Working on getModelSlice ");
        System.out.println(date() + ":getEntityIDs");
        long entityIDs[] = getEntityIDs();
        System.out.println(date() + ":getTimeDomain");
        Attribute.Calendar dateIds[] = getTimeDomain();
        String dateIdsAsString[] = new String[dateIds.length];
        for (int i=0;i<dateIds.length;i++){
            dateIdsAsString[i] = dateIds[i].toString();
        }
        double result[][] = new double[dateIdsAsString.length][entityIDs.length];

        TimeSpaceProcessor tsp = this.procs.get(model);

        if (tsp != null) {
            for (AttributeData a : tsp.getDataStoreProcessor().getAttributes()) {
                a.setSelected(true);
            }
            System.out.println(date() + ":getCrossProduct");
            DataMatrix matrix = procs.get(model).getCrossProduct(entityIDs, dateIdsAsString);
            System.out.println(date() + ":finished");
            for (int i = 0; i < entityIDs.length; i++) {
                for (int j = 0; j < dateIdsAsString.length; j++) {
                    result[j][i] = matrix.get(j, i);
                }
            }
        }

        return result;
    }
    
    @Override
    public long[] getEntityIDs() throws SQLException, IOException {
        TreeSet<Long> result = new TreeSet<Long>();
        //do an AND over all Entity-IDs
        if (!procs.keySet().isEmpty()){
            result.addAll(Arrays.asList(procs.values().iterator().next().getEntityIDs()));
            for (ClimateModel model : procs.keySet()) {
                //much faster!
                TreeSet<Long> sortedList = new TreeSet(Arrays.asList(procs.get(model).getEntityIDs()));
                result.retainAll(sortedList);
            }
        }       
        long convResult[] = new long[result.size()];
        int i = 0;
        for (long l : result) {
            convResult[i++] = l;
        }
        return convResult;        
    }

    @Override
    public double[][] getSpatialMean() throws SQLException, IOException {

        long entityIDs[] = getEntityIDs();

        Attribute.Calendar timeIDsSource[] = getTimeDomain();
        String timeIDs[] = new String[timeIDsSource.length];
        for (int i = 0; i < timeIDsSource.length; i++) {
            timeIDs[i] = timeIDsSource[i].toString();
        }

        double result[][] = new double[ensemble.getSize()][timeIDs.length];

        int i = 0;
        for (ClimateModel model : ensemble.modelSet) {
            TimeSpaceProcessor tsp = this.procs.get(model);
            Arrays.fill(result[i], Double.NaN);
            if (tsp != null) {
                for (AttributeData a : tsp.getDataStoreProcessor().getAttributes()) {
                    a.setSelected(true);
                }

                DataMatrix m = tsp.getCrossProduct(entityIDs, timeIDs);               
                double avg[] = m.getAvgCol();
                System.arraycopy(avg, 0, result[i], 0, timeIDs.length);
            }
            i++;
        }
        return result;
    }

    @Override
    public double[] getSpatialMean(ClimateModel model) throws SQLException, IOException {
        TimeSpaceProcessor tsp = procs.get(model);
        if (tsp == null) {
            return null;
        }
        long entityIDs[] = getEntityIDs();

        Attribute.Calendar timeIDsSource[] = getTimeDomain();
        String timeIDs[] = new String[timeIDsSource.length];
        for (int i = 0; i < timeIDsSource.length; i++) {
            timeIDs[i] = timeIDsSource[i].toString();
        }

        DataMatrix m = tsp.getCrossProduct(entityIDs, timeIDs);
        return m.getAvgRow();
    }
    
    public DataMatrix calculate(String s){
        return null;
    }
}
