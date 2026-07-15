// Zeitschritte nicht abspeichern, aber Start und Enddatum, globale time domain über updateTimeDomain (public setzen)
// gui implementieren

package optas.io;

import optas.data.TimeSerie;
import optas.data.SimpleEnsemble;
import optas.data.Measurement;
import optas.data.DataSet;
import optas.data.EfficiencyEnsemble;
import optas.data.DataCollection;
import optas.data.TimeSerieEnsemble;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import optas.data.DataSet.MismatchException;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class NetCDFFileReader {
    
    private NetcdfFile file = null;
    
    public NetCDFFileReader(String file) throws IOException {
        
        this.file = NetcdfFile.open(file);
    }
    
    private TimeInterval createTimeInterval(long start, long end, int timeUnit, int timeUnitCount) {
        
        TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
        interval.setTimeUnit(timeUnit);
        interval.setTimeUnitCount(timeUnitCount);
        Calendar startCalendar = DefaultDataFactory.getDataFactory().createCalendar();
        startCalendar.setTimeInMillis(start);
        interval.setStart(startCalendar);
        Calendar endCalendar = DefaultDataFactory.getDataFactory().createCalendar();
        endCalendar.setTimeInMillis(end);
        interval.setEnd(endCalendar);
        
        return interval;
    }
    
    public DataCollection read() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, DataSet.MismatchException {
        
        // create empty collection and set sampler class
        DataCollection collection = new DataCollection();
        collection.setSamplerClass(file.findGlobalAttribute("samplerClass").getStringValue());
        
        // retrieve all variables
        List<Variable> variables = file.getVariables();
        List<Integer> modelRuns = new ArrayList<Integer>();
        List<Variable> datasets = new ArrayList<Variable>();
        for (Variable v : variables) {
            if (v.getFullName().equals("modelRunID")) {
                int length = v.getDimension(0).getLength();
                Array values = v.read();
                int i;
                for (i = 0; i < length; i++) {
                    modelRuns.add(values.getInt(i));
                }
            } else {
                datasets.add(v);
            }
        }
        
        // create datasets and add to collection
        for (Variable dataset : datasets) {
            
            String className = dataset.findAttribute("className").getStringValue();
            Class c = ClassLoader.getSystemClassLoader().loadClass(className);
            
            if (c.equals(TimeSerieEnsemble.class)) {
                
                int timeUnit = dataset.findAttribute("timeunit").getNumericValue().intValue();
                int timeUnitCount = dataset.findAttribute("timeunitcount").getNumericValue().intValue();
                long start = Long.valueOf(dataset.findAttribute("start").getStringValue());
                long end = Long.valueOf(dataset.findAttribute("end").getStringValue());
                TimeInterval interval = this.createTimeInterval(start, end, timeUnit, timeUnitCount);
                TimeSerieEnsemble ensemble = new TimeSerieEnsemble(dataset.getFullName(), modelRuns.size(), interval);
                
                Array array = dataset.read();
                Index index = array.getIndex();
                for (Integer id : modelRuns) {
                    double[] values = new double[(int) interval.getNumberOfTimesteps()];
                    int t;
                    for (t = 0; t < interval.getNumberOfTimesteps(); t++) {
                        index.set(id, t);
                        values[t] = array.getDouble(index);
                    }
                    ensemble.add(id, values);
                }
                collection.addTimeSeriesEnsemble(ensemble);
                
            } else if (c.equals(Measurement.class)) {
                
                int timeUnit = dataset.findAttribute("timeunit").getNumericValue().intValue();
                int timeUnitCount = dataset.findAttribute("timeunitcount").getNumericValue().intValue();
                long start = Long.valueOf(dataset.findAttribute("start").getStringValue());
                long end = Long.valueOf(dataset.findAttribute("end").getStringValue());
                TimeInterval interval = this.createTimeInterval(start, end, timeUnit, timeUnitCount);
                
                Array array = dataset.read();
                double[] values = new double[(int) interval.getNumberOfTimesteps()];
                int t;
                for (t = 0; t < interval.getNumberOfTimesteps(); t++) {
                    values[t] = array.getDouble(t);
                }
                TimeSerie series = new TimeSerie(values, interval, dataset.getFullName(), collection);
                Measurement measurement = new Measurement(series);
                collection.addTimeSerie(measurement);
                
            } else if (c.equals(SimpleEnsemble.class)) {
                
                SimpleEnsemble ensemble = new SimpleEnsemble(dataset.getFullName(), modelRuns.size());
                Array values = dataset.read();
                int i = 0;
                for (Integer run : modelRuns) {
                    ensemble.add(run, values.getDouble(i));
                    i++;
                }
                collection.addEnsemble(ensemble);
                
            } else if (c.equals(EfficiencyEnsemble.class)) {
                
                boolean isPositiveBest = Boolean.valueOf(dataset.findAttribute("isPositiveBest").getStringValue());
                EfficiencyEnsemble ensemble = new EfficiencyEnsemble(dataset.getFullName(), modelRuns.size(), isPositiveBest);
                Array values = dataset.read();
                int i = 0;
                for (Integer run : modelRuns) {
                    ensemble.add(run, values.getDouble(i));
                    i++;
                }
                collection.addEnsemble(ensemble);
                
            } else {
                throw new UnsupportedOperationException();
            }
        }
        
        if (file.findGlobalAttribute("timeDomainStart") != null) {
            
            Attribute.Calendar startCal = DefaultDataFactory.getDataFactory().createCalendar();
            long start = Long.valueOf(file.findGlobalAttribute("timeDomainStart").getStringValue());
            startCal.setTimeInMillis(start);
            Attribute.Calendar endCal = DefaultDataFactory.getDataFactory().createCalendar();
            long end = Long.valueOf(file.findGlobalAttribute("timeDomainEnd").getStringValue());
            endCal.setTimeInMillis(end);
            int timeUnit = Integer.valueOf(file.findGlobalAttribute("timeUnit").getStringValue());
            int timeUnitCount = Integer.valueOf(file.findGlobalAttribute("timeUnitCount").getStringValue());
            TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
            interval.setStart(startCal);
            interval.setEnd(endCal);
            interval.setTimeUnit(timeUnit);
            interval.setTimeUnitCount(timeUnitCount);
            collection.setGlobalTimeDomain(interval);
        }
        
        return collection;
    }
    
    public void close() throws IOException {
        file.close();
    } 
    
    public static void main(String[] args) {
        
        NetCDFFileReader stream = null;
        try {
            stream = new NetCDFFileReader("/Users/Tilo/Desktop/test2.cdf");
            stream.read();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (MismatchException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Unable to read file!");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    System.out.println("Unable to close open file!");
                }
            }
        }
    }
}
