/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import jams.JAMS;
import jams.JAMSProperties;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import jams.workspace.dsproc.AbstractDataStoreProcessor;
import jams.workspace.dsproc.AbstractDataStoreProcessor.AttributeData;
import jams.workspace.dsproc.AbstractDataStoreProcessor.ContextData;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.EnsembleTimeSeriesProcessor;
import jams.workspace.dsproc.Processor;
import jams.workspace.dsproc.SimpleSerieProcessor;
import jams.workspace.dsproc.TimeSpaceProcessor;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import optas.data.DataCollection;
import optas.data.DataSet.MismatchException;
import optas.data.Measurement;
import optas.data.Modelrun;
import optas.data.NegativeEfficiency;
import optas.data.Parameter;
import optas.data.PositiveEfficiency;
import optas.data.SimpleDataSet;
import optas.data.SimpleEnsemble;
import optas.data.StateVariable;
import optas.data.TimeSerie;

/**
 *
 * @author chris
 */
public class ImportMonteCarloData implements Serializable {

    ArrayList<Processor> fileProcessors = new ArrayList<Processor>();
    HashMap<AttributeData, Processor> attributeDataMap = new HashMap<AttributeData, Processor>();
    HashMap<AttributeData, EnsembleType> typeMap = new HashMap<AttributeData, EnsembleType>();
    EnumMap<EnsembleType, Class> simpleDatasetClasses = new EnumMap<EnsembleType, Class>(EnsembleType.class);
    EnumMap<EnsembleType, Class> timeSerieDatasetClasses = new EnumMap<EnsembleType, Class>(EnsembleType.class);
    
    public enum EnsembleType{Parameter, StateVariable, Measurement, NegEfficiency, PosEfficiency, Timeserie, Unknown, Ignore};
    
    final EnsembleType ensembleVariableTypes[] = {EnsembleType.Ignore, EnsembleType.Parameter, EnsembleType.StateVariable, EnsembleType.NegEfficiency, EnsembleType.PosEfficiency};
    final EnsembleType ensembleTimeserieTypes[] = {EnsembleType.Ignore, EnsembleType.Timeserie, EnsembleType.Measurement};
    final EnsembleType timeserieTypes[] = {EnsembleType.Ignore, EnsembleType.Measurement};
            
    DataCollection ensemble = null;
    final TreeMap<String, EnsembleType> defaultAttributeTypes = new TreeMap<String, EnsembleType>();
    boolean isValid = false;

    public ImportMonteCarloData() {
        init();
    }

    private void init() {
        simpleDatasetClasses.put(EnsembleType.Parameter, Parameter.class);
        simpleDatasetClasses.put(EnsembleType.Measurement, Measurement.class);
        simpleDatasetClasses.put(EnsembleType.NegEfficiency, NegativeEfficiency.class);
        simpleDatasetClasses.put(EnsembleType.PosEfficiency, PositiveEfficiency.class);
        simpleDatasetClasses.put(EnsembleType.StateVariable, StateVariable.class);
        simpleDatasetClasses.put(EnsembleType.Unknown, Parameter.class);
        timeSerieDatasetClasses.put(EnsembleType.Timeserie, TimeSerie.class);
        timeSerieDatasetClasses.put(EnsembleType.Measurement, Measurement.class);

        JAMSProperties prop = JAMSProperties.createProperties();

        for (EnsembleType e : EnsembleType.values()){
            String value = prop.getProperty(e.toString());
            if (value != null){
                String parameterAttributes[] = value.split(";");        
                for (String parameterAttribute : parameterAttributes){
                    defaultAttributeTypes.put(parameterAttribute, e);
                }
            }
        }
    }

    public EnsembleType[] getValidProcessingOptions(Processor p) {                
        switch (AbstractDataStoreProcessor.getDataStoreType(p.getDataStoreProcessor().getFile())) {
            case TimeDataSerie:
                return ensembleTimeserieTypes;
            case DataSerie1D:
                return ensembleVariableTypes;
            case Timeserie:
                return timeserieTypes;
        }
        return null;
    }

    public boolean addFile(File file) throws ImportMonteCarloException {
        if (!loadDataStore(file)){
            return isValid=false;
        }
        updateDataTable();
        return isValid = true;
    }

    public Processor getProcessorForAttribute(AttributeData a) {
        return this.attributeDataMap.get(a);
    }

    private boolean loadDataStore(File file) throws ImportMonteCarloException {
        Processor proc;
        AbstractDataStoreProcessor dsdb = AbstractDataStoreProcessor.getProcessor(file);
        if (dsdb.isEmpty()) {
            return false;
        }
        try {
            dsdb.createDB();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ImportMonteCarloException(JAMS.i18n("Could_not_load_data_store_") + file, ioe);
        } catch (SQLException ioe) {
            ioe.printStackTrace();
            throw new ImportMonteCarloException(JAMS.i18n("Could_not_create_database"), ioe);
        } catch (ClassNotFoundException ioe) {
            ioe.printStackTrace();
            throw new ImportMonteCarloException(JAMS.i18n("Could_not_load_data_store_"), ioe);
        }
        
        switch (AbstractDataStoreProcessor.getDataStoreType(file)) {
            case Unsupported:
                return false;
            case TimeDataSerie:
                proc = new EnsembleTimeSeriesProcessor(dsdb);
                break;
            case SpatioTemporal:
                proc = new TimeSpaceProcessor(dsdb);
                break;
            case DataSerie1D:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            case Timeserie:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            default:
                return false;
        }
        fileProcessors.add(proc);
        return true;
    }
    static int badIDCounter = 1000000;

    private DataCollection buildEnsemble() throws ImportMonteCarloException {
        ensemble = new DataCollection();
        String samplerClass = null;

        for (AttributeData a : this.attributeDataMap.keySet()) {
            EnsembleType dataSetClassName = this.typeMap.get(a);
            Processor p = this.attributeDataMap.get(a);

            //make defaults!!
            if (dataSetClassName == null){
                if (p instanceof SimpleSerieProcessor){                
                    dataSetClassName = EnsembleType.StateVariable;
                }else if (p instanceof EnsembleTimeSeriesProcessor){
                    dataSetClassName = EnsembleType.Timeserie;
                }else{
                    //never know??
                }
            }
            
            for (ContextData c : p.getDataStoreProcessor().getContexts()) {
                if (c.getType().startsWith("jams.components.optimizer") || c.getType().contains("optas")) {
                    if (samplerClass == null) {
                        samplerClass = c.getType();
                        ensemble.setSamplerClass(samplerClass);
                    } else if (!c.getType().equals(samplerClass)) {
                        samplerClass = "jams.components.optimizer.Optimizer";
                    }
                }
            }
            try {
                if (simpleDatasetClasses.containsKey(dataSetClassName) && p instanceof SimpleSerieProcessor) {
                    if (p instanceof SimpleSerieProcessor) {
                        SimpleSerieProcessor s = ((SimpleSerieProcessor) p);
                        String[] ids = s.getIDs();
                        for (AttributeData ad : s.getDataStoreProcessor().getAttributes()) {
                            ad.setSelected(false);
                        }
                        a.setSelected(true);
                        DataMatrix m = s.getData(ids);
                        a.setSelected(false);
                        int row = 0;
                        if (s.isTimeSerie()){
                            double values[] = m.getCol(0);
                            Attribute.TimeInterval timeInterval = DefaultDataFactory.getDataFactory().createTimeInterval();
                            timeInterval.setStart(s.getTimeSteps()[0]);
                            timeInterval.setEnd(s.getTimeSteps()[values.length-1]);
                            timeInterval.setTimeUnit(s.getTimeUnit());
                            
                            TimeSerie ts = new TimeSerie(values, timeInterval, a.getName(), ensemble);
                            ensemble.addTimeSerie(new Measurement(ts));
                        } else {
                            for (String id : ids) {
                                Integer intID = null;
                                try {
                                    intID = Integer.parseInt(id);
                                } catch (NumberFormatException nfe) {
                                    nfe.printStackTrace();
                                    //fallback (there should be a list of all used ids)
                                    intID = new Integer(badIDCounter++);
                                }
                                Modelrun r = new Modelrun(intID, null);
                                Class datasetClass = simpleDatasetClasses.get(dataSetClassName);
                                Constructor c = datasetClass.getConstructor(SimpleDataSet.class);
                                SimpleDataSet nonTypedSDS = new SimpleDataSet(m.get(row, 0), a.getName(), r);
                                row++;
                                SimpleDataSet typedSDS = (SimpleDataSet) c.newInstance(nonTypedSDS);
                                r.addDataSet(typedSDS);
                                ensemble.addModelRun(r);
                            }
                        }
                    }
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), sqle);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), ioe);
            } catch (MismatchException me) {
                me.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), me);
            } catch (ClassCastException cce) {
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), cce);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), t);
            }
            try {
                if (timeSerieDatasetClasses.containsKey(dataSetClassName) && p instanceof EnsembleTimeSeriesProcessor) {
                    EnsembleTimeSeriesProcessor s = ((EnsembleTimeSeriesProcessor) p);
                    long[] ids = s.getModelRuns();
                    Calendar[] timesteps = s.getTimeSteps();
                    if (timesteps == null) {
                        continue;
                    }
                    String[] namedTimesteps = new String[timesteps.length];
                    for (int i = 0; i < timesteps.length; i++) {
                        namedTimesteps[i] = timesteps[i].toString();
                    }

                    ensembleTime = DefaultDataFactory.getDataFactory().createTimeInterval();
                    ensembleTime.setStart(timesteps[0]);
                    ensembleTime.setEnd(timesteps[timesteps.length - 1]);
                    ensembleTime.setTimeUnit(s.getTimeUnit());

                    for (AttributeData ad : s.getDataStoreProcessor().getAttributes()) {
                        ad.setSelected(false);
                    }
                    a.setSelected(true);
                    DataMatrix m = s.getCrossProduct(ids, namedTimesteps);
                    a.setSelected(false);

                    if (this.timeSerieDatasetClasses.get(dataSetClassName).equals(TimeSerie.class)) {
                        int col = 0;
                        for (Long id : ids) {
                            Modelrun r = new Modelrun(id.intValue(), null);
                            r.addDataSet(new TimeSerie(m.getCol(col), ensembleTime, a.getName(), r));
                            col++;
                            ensemble.addModelRun(r);
                        }
                    } else if (this.timeSerieDatasetClasses.get(dataSetClassName).equals(Measurement.class)) {
                        int col = 0;
                        Measurement ts = null;
                        for (Long id : ids) {
                            Modelrun r = new Modelrun(id.intValue(), null);
                            Measurement ts2 = new Measurement(new TimeSerie(m.getCol(col), ensembleTime, a.getName(), r));
                            if (ts == null) {
                                ts = ts2;
                            } else {
                                for (int i = 0; i < ts.getTimeDomain().getNumberOfTimesteps(); i++) {
                                    if (ts.getValue(i) != ts2.getValue(i)) {
                                        throw new MismatchException(JAMS.i18n("timeserie_ensemble_could_not_be_used_as_measurement"));
                                    }
                                }
                            }
                            col++;
                        }
                        ensemble.addTimeSerie(ts);
                    }
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), sqle);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), ioe);
            } catch (MismatchException me) {
                me.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), me);
            } catch (ClassCastException cce) {
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), cce);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_build_ensemble_"), t);
            }
        }
        return ensemble;
    }

    public EnsembleType getDefaultAttributeType(AttributeData a) {
        EnsembleType type = (EnsembleType)this.defaultAttributeTypes.get(a.getName());
        //best guess
        if (type == null){
            for (String key : this.defaultAttributeTypes.keySet()){
                if (a.getName().contains(key)){
                    return (EnsembleType)this.defaultAttributeTypes.get(key);
                }
            }
            return null;
        }else{
            return type;
        }
    }

    public boolean isEmpty() {
        return this.attributeDataMap.isEmpty();
    }

    /*private boolean isEmptyFile(File file) throws ImportMonteCarloException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            boolean isDataStart = false;
            int dataCounter = 0;

            while ((line = reader.readLine()) != null) {
                if (line.contains("@data")) {
                    isDataStart = true;
                } else if (isDataStart) {
                    dataCounter++;
                }
            }
            reader.close();
            return dataCounter <= 1;

        } catch (IOException fnfe) {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ImportMonteCarloException(JAMS.i18n("Could_not_read_file_") + file.toString(), ioe);
            }
            return true;
        }
    }*/
    
    TreeSet<String> ensembleIDs = new TreeSet<String>();
    TreeSet<String> ensembleTimesteps = new TreeSet<String>();
    TimeInterval ensembleTime = null;

    public DataCollection getEnsemble() throws ImportMonteCarloException {
        if (!isValid) {
            return null;
        }
        buildEnsemble();
        return ensemble;
    }

    public void setEnsemble(DataCollection dc) {
        this.ensemble = dc;
    }

    public void setType(AttributeData a, EnsembleType type) {
        if (type != null) {
            typeMap.put(a, type);
        } else {
            typeMap.put(a, EnsembleType.Unknown);
        }
    }

    public ArrayList<Processor> getFileProcessors() {
        return this.fileProcessors;
    }

    public void removeFileProcessor(int index) {
        this.fileProcessors.remove(index);
    }

    public TreeSet<AttributeData> getAttributeData() {
        if (attributeDataMap == null) {
            return null;
        }

        return new TreeSet(this.attributeDataMap.keySet());
    }

    private void updateDataTable() {        
        HashMap<Processor, EnsembleType[]> processorTypeMap = new HashMap<Processor, EnsembleType[]>();

        for (Processor p : fileProcessors) {
            switch (AbstractDataStoreProcessor.getDataStoreType(p.getDataStoreProcessor().getFile())) {
                case TimeDataSerie:
                    processorTypeMap.put(p, ensembleTimeserieTypes);
                    break;
                case DataSerie1D:
                    processorTypeMap.put(p, ensembleVariableTypes);
                    break;
                case Timeserie:
                    processorTypeMap.put(p, timeserieTypes);
                    break;
            }

            for (AttributeData a : p.getDataStoreProcessor().getAttributes()) {
                attributeDataMap.put(a, p);
                this.typeMap.put(a, (EnsembleType)getDefaultAttributeType(a));
            }
        }
    }

    public void finish() {
        for (AttributeData a : this.typeMap.keySet()){
            defaultAttributeTypes.put(a.getName(),typeMap.get(a));
        }

        HashMap<EnsembleType, ArrayList<String>> reverseMapping = new HashMap<EnsembleType, ArrayList<String>>();
        for (String s : defaultAttributeTypes.keySet() ){
            EnsembleType type = defaultAttributeTypes.get(s);
            if (reverseMapping.containsKey(type)){
                reverseMapping.get(type).add(s);
            }else{
                ArrayList<String> list = new ArrayList<String>();
                list.add(s);
                reverseMapping.put(type, list);
            }
        }
        
        JAMSProperties prop = JAMSProperties.createProperties();
        for (EnsembleType e : reverseMapping.keySet()){
            
            String valueString = "";
            for (String value : reverseMapping.get(e)){
                valueString += value + ";";
            }
            prop.setProperty(e.toString(), valueString);
        }
        try{
            prop.save();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        try {
            for (Processor p : fileProcessors) {
                p.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.fileProcessors = null;
        this.typeMap = null;
        this.ensemble = null;
        this.ensembleIDs = null;
    }
}