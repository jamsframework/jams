// Zeitschritte nicht abspeichern, aber Start und Enddatum, globale time domain über updateTimeDomain (public setzen)
// gui implementieren

package optas.io;

import optas.data.ensemble.DefaultSimpleEnsemble;
import optas.data.time.MeasuredTimeSerie;
import optas.data.ensemble.DefaultEfficiencyEnsemble;
import optas.data.DefaultDataCollection;
import optas.data.ensemble.DefaultTimeSerieEnsemble;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.core.OPTASException;
import optas.data.time.api.TimeSerie;
import optas.data.view.ViewFactory;
import optas.data.time.DefaultTimeSerie;
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
    
    public DataCollection read() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, OPTASException {
        
        // create empty collection and set sampler class
        DataCollection collection = DataCollection.getInstance("My Ensemble");
        //collection.setSamplerClass(file.findGlobalAttribute("samplerClass").getStringValue());
        
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
            
            if (c.equals(DefaultTimeSerieEnsemble.class)) {
                
                int timeUnit = dataset.findAttribute("timeunit").getNumericValue().intValue();
                int timeUnitCount = dataset.findAttribute("timeunitcount").getNumericValue().intValue();
                long start = Long.valueOf(dataset.findAttribute("start").getStringValue());
                long end = Long.valueOf(dataset.findAttribute("end").getStringValue());
                TimeInterval interval = this.createTimeInterval(start, end, timeUnit, timeUnitCount);
                DefaultTimeSerieEnsemble ensemble = new DefaultTimeSerieEnsemble(dataset.getFullName(), modelRuns.size(), interval);
                
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
                collection.addDataSet(ensemble);
                
            } else if (c.equals(MeasuredTimeSerie.class)) {
                
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
                DataView data = ViewFactory.createView(values);
                MeasuredTimeSerie measurement = MeasuredTimeSerie.getInstance(
                        new DefaultTimeSerie(dataset.getFullName(), interval, data)
                );
                collection.addGlobalDataSet(measurement);
                
            } else if (c.equals(DefaultSimpleEnsemble.class)) {
                
                DefaultSimpleEnsemble ensemble = new DefaultSimpleEnsemble(dataset.getFullName(), modelRuns.size());
                Array values = dataset.read();
                int i = 0;
                for (Integer run : modelRuns) {
                    ensemble.add(run, values.getDouble(i));
                    i++;
                }
                collection.addDataSet(ensemble);
                
            } else if (c.equals(DefaultEfficiencyEnsemble.class)) {
                
                boolean isPositiveBest = Boolean.valueOf(dataset.findAttribute("isPositiveBest").getStringValue());
                DefaultEfficiencyEnsemble ensemble = new DefaultEfficiencyEnsemble(dataset.getFullName(), modelRuns.size(), isPositiveBest);
                Array values = dataset.read();
                int i = 0;
                for (Integer run : modelRuns) {
                    ensemble.add(run, values.getDouble(i));
                    i++;
                }
                collection.addDataSet(ensemble);
                
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
        } catch (OPTASException ex) {
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
