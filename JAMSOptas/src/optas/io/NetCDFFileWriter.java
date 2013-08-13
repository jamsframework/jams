package optas.io;

import optas.data.SimpleEnsemble;
import optas.data.Measurement;
import optas.data.DataSet;
import optas.data.EfficiencyEnsemble;
import optas.data.DataCollection;
import optas.data.TimeSerieEnsemble;
import jams.data.Attribute.TimeInterval;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import optas.data.DataSet.MismatchException;
import ucar.ma2.*;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

public class NetCDFFileWriter {
    
    private NetcdfFileWriteable file = null;
    
    public NetCDFFileWriter(String file) throws IOException {
            
        this.file = NetcdfFileWriteable.createNew(file);
    }
    
    private boolean hasSuitableTimeInterval(TimeInterval collectionInterval, TimeInterval dataSetInterval) {
        
        if (collectionInterval.getTimeUnit() != dataSetInterval.getTimeUnit()) {
            return false;
        } else if (collectionInterval.getTimeUnitCount() != dataSetInterval.getTimeUnitCount()) {
            return false;
        } else if (collectionInterval.getStart().getTimeInMillis() > dataSetInterval.getStart().getTimeInMillis()) {
            return false;
        } else if (collectionInterval.getEnd().getTimeInMillis() < dataSetInterval.getEnd().getTimeInMillis()) {
            return false;
        } else {
            return true;
        }
    }
    
    private boolean hasSuitableModelRuns(Integer[] collectionModelRuns, Integer[] dataSetModelRuns) {
        
        if (collectionModelRuns.length < dataSetModelRuns.length) {
            return false;
        } else if (collectionModelRuns[0] > dataSetModelRuns[0]) {
            return false;
        } else if (collectionModelRuns[collectionModelRuns.length - 1] < dataSetModelRuns[dataSetModelRuns.length - 1]) {
            return false;
        } else {
            return true;
        }
    }
    
    private int getTimeIntervalOffset(TimeInterval collectionInterval, TimeInterval dataSetInterval) {
        
        long intervalOffset = collectionInterval.getStartOffset(dataSetInterval);
        int offset = 0;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(collectionInterval.getStart().getTimeInMillis());
        while (cal.getTimeInMillis() < collectionInterval.getStart().getTimeInMillis() + intervalOffset) {
            cal.add(collectionInterval.getTimeUnit(), 1);
            offset++;
        }
        return offset;
    }
    
    private int getModelRunIDOffset(Integer[] collectionModelRuns, Integer[] dataSetModelRuns) {
        
        int offset = 0;
        while (collectionModelRuns[offset] != dataSetModelRuns[0]) {
            offset++;
        }
        return offset;
    }
    
    public void write(DataCollection collection) throws IOException, UnsupportedOperationException, MismatchException {
        
        // store meta information
        file.addAttribute(null, new Attribute("samplerClass", collection.getSamplerClass()));
        
        // create model run dimension
        Integer[] collectionModelRuns = collection.getModelrunIds();
        Dimension modelRunID = file.addDimension("modelRuns", collectionModelRuns.length, true, false, false);
        
        // create model run id variable
        List<Dimension> dimensions = new ArrayList<Dimension>();
        dimensions.add(modelRunID);
        file.addVariable("modelRunID", DataType.INT, dimensions);
        
        // create time dimension and time variable
        TimeInterval collectionInterval = null;
        Dimension time = null;
        if (collection.getTimeDomain() != null) {
            
            collectionInterval = collection.getTimeDomain();
            int numberOfTimeSteps = (int) collectionInterval.getNumberOfTimesteps();
            time = file.addDimension("time", numberOfTimeSteps, true, false, false);
            
            String timeUnit = String.valueOf(collectionInterval.getTimeUnit());
            String timeUnitCount = String.valueOf(collectionInterval.getTimeUnitCount());
            String start = String.valueOf(collectionInterval.getStart().getTimeInMillis());
            String end = String.valueOf(collectionInterval.getEnd().getTimeInMillis());
            file.addAttribute(null, new Attribute("timeUnit", timeUnit));
            file.addAttribute(null, new Attribute("timeUnitCount", timeUnitCount));
            file.addAttribute(null, new Attribute("timeDomainStart", start));
            file.addAttribute(null, new Attribute("timeDomainEnd", end));
        }

        // create dataset variables
        Set<String> dataSetNames = collection.getDatasets();
        for (String name : dataSetNames) {
            
            DataSet dataSet = collection.getDataSet(name);
            
            dimensions = new ArrayList<Dimension>();
            if (dataSet instanceof TimeSerieEnsemble) {
                dimensions.addAll(Arrays.asList(new Dimension[]{modelRunID, time}));
            } else if (dataSet instanceof Measurement) {
                dimensions.add(time);
            } else if (dataSet instanceof SimpleEnsemble || dataSet instanceof EfficiencyEnsemble) {
                dimensions.add(modelRunID);
            } else {
                throw new UnsupportedOperationException();
            }
            
            Variable var = file.addVariable(name, DataType.DOUBLE, dimensions);
            var.addAttribute(new Attribute("className", dataSet.getClass().getName()));
            
            // add time attributes if necessary
            if (dimensions.contains(time)) {
                TimeInterval interval = null;
                if (dataSet instanceof TimeSerieEnsemble) {
                    interval = ((TimeSerieEnsemble) dataSet).getTimeInterval();
                }
                if (dataSet instanceof Measurement) {
                    interval = ((Measurement) dataSet).getTimeDomain();
                }
                var.addAttribute(new Attribute("timeunit", interval.getTimeUnit()));
                var.addAttribute(new Attribute("timeunitcount", interval.getTimeUnitCount()));
                var.addAttribute(new Attribute("start", String.valueOf(interval.getStart().getTimeInMillis())));
                var.addAttribute(new Attribute("end", String.valueOf(interval.getEnd().getTimeInMillis())));
            }
            
            // add efficiency attribute if necessary
            if (dataSet instanceof EfficiencyEnsemble) {
                var.addAttribute(new Attribute("isPositiveBest", String.valueOf(((EfficiencyEnsemble) dataSet).isPositiveBest())));
            }
        }
        
        // end "define mode"
        file.create();
        
        // write model run ids
        ArrayInt.D1 modelRunArray = new ArrayInt.D1(collectionModelRuns.length);
        int i;
        for (i = 0; i < collectionModelRuns.length; i++) {
            modelRunArray.set(i, collectionModelRuns[i]);
        }
        try {
            file.write("modelRunID", modelRunArray);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(NetCDFFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // write datasets to file
        for (String name : dataSetNames) {
            
            DataSet dataSet = collection.getDataSet(name);
            
            if (dataSet instanceof TimeSerieEnsemble) {
                
                TimeSerieEnsemble ensemble = (TimeSerieEnsemble) dataSet;
                TimeInterval dataSetInterval = ensemble.getTimeInterval();
                Integer[] dataSetModelRuns = ensemble.getIds();
                
                if (!hasSuitableTimeInterval(collectionInterval, dataSetInterval)) {
                    throw new MismatchException("Time intervals don't match.");
                } else if (!hasSuitableModelRuns(collectionModelRuns, dataSetModelRuns)) {
                    throw new MismatchException("Model run IDs don't match.");
                } else {
                    ArrayDouble.D2 dataSetArray = new ArrayDouble.D2(dataSetModelRuns.length, (int) dataSetInterval.getNumberOfTimesteps());
                    for (i = 0; i < dataSetModelRuns.length; i++) {
                        int t;
                        for (t = 0; t < dataSetInterval.getNumberOfTimesteps(); t++) {
                            Index index = dataSetArray.getIndex();
                            dataSetArray.set(index.set(i, t), ensemble.get(t, dataSetModelRuns[i]));
                        }
                    }
                    int[] offset = new int[]{getModelRunIDOffset(collectionModelRuns, dataSetModelRuns), getTimeIntervalOffset(collectionInterval, dataSetInterval)};
                    try {
                        file.write(name, offset, dataSetArray);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(NetCDFFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                
            } else if (dataSet instanceof Measurement) {
                
                Measurement measurement = (Measurement) dataSet;
                TimeInterval dataSetInterval = measurement.getTimeDomain();
                
                if (!hasSuitableTimeInterval(collectionInterval, dataSetInterval)) {
                    throw new MismatchException("Time intervals don't match.");
                } else {
                    ArrayDouble.D1 dataSetArray = new ArrayDouble.D1((int) dataSetInterval.getNumberOfTimesteps());
                    int t;
                    for (t = 0; t < dataSetInterval.getNumberOfTimesteps(); t++) {
                        dataSetArray.set(t, measurement.getValue(t));
                    }
                    int[] offset = new int[]{getTimeIntervalOffset(collectionInterval, dataSetInterval)};
                    try {
                        file.write(name, offset, dataSetArray);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(NetCDFFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            
            } else if (dataSet instanceof SimpleEnsemble || dataSet instanceof EfficiencyEnsemble) {
                
                SimpleEnsemble ensemble = (SimpleEnsemble) dataSet;
                Integer[] dataSetModelRuns = ensemble.getIds();
                
                if (!hasSuitableModelRuns(collectionModelRuns, dataSetModelRuns)) {
                    throw new MismatchException("Model run IDs don't match.");
                } else {
                    ArrayDouble.D1 dataSetArray = new ArrayDouble.D1(dataSetModelRuns.length);
                    for (i = 0; i < dataSetModelRuns.length; i++) {
                        dataSetArray.set(i, ensemble.getValue(dataSetModelRuns[i]));
                    }
                    int[] offset = new int[]{getModelRunIDOffset(collectionModelRuns, dataSetModelRuns)};
                    try {
                        file.write(name, offset, dataSetArray);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(NetCDFFileWriter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    public void close() throws IOException {
        file.close();
    }
    
    public void flush() throws IOException {
        file.flush();
    }
    
    public static void main(String[] args) throws Exception{
        
        ImportMonteCarloData data = new ImportMonteCarloData();
        data.addFile(new File("E:/ModelData/JAMS-Gehlberg/output/mc3_1600Final_Nov2/optimization_wizard_optimizer.dat"));
        DataCollection collection = data.getEnsemble();
        
        NetCDFFileWriter stream = null;
        try {
            stream = new NetCDFFileWriter("E:/ModelData/test.nc");
            stream.write(collection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /*data = new ImportMonteCarloData(new File("/Users/Tilo/Dropbox/Java/JADE/workspace/output/optimizer/optimization_wizard_TimeLoop.dat"));
        collection = data.getEnsemble();
        
        try {
            stream = new NetCDFFileWriter("/Users/Tilo/Desktop/test2.cdf");
            stream.write(collection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }
}