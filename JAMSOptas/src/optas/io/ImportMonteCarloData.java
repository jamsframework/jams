/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.DataStoreProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import jams.workspace.dsproc.DataStoreProcessor.AttributeData;
import jams.workspace.dsproc.DataStoreProcessor.ContextData;
import jams.workspace.dsproc.EnsembleTimeSeriesProcessor;
import jams.workspace.dsproc.Processor;
import jams.workspace.dsproc.SimpleSerieProcessor;
import jams.workspace.dsproc.TimeSpaceProcessor;
import java.util.EnumMap;
import optas.Optas;
import optas.hydro.data.DataCollection;
import optas.hydro.data.DataSet.MismatchException;
import optas.hydro.data.Measurement;
import optas.hydro.data.Modelrun;
import optas.hydro.data.NegativeEfficiency;
import optas.hydro.data.Parameter;
import optas.hydro.data.PositiveEfficiency;
import optas.hydro.data.SimpleDataSet;
import optas.hydro.data.StateVariable;
import optas.hydro.data.TimeSerie;

/**
 *
 * @author chris
 */
public class ImportMonteCarloData implements Serializable {

    String defaultTypes = "initRG1=Parameter;"
            + "initRG2=Parameter;"
            + "maxPercAdaptation=Parameter;"
            + "linETRed=Parameter;"
            + "ddf=Parameter;"
            + "ACAdaptation=Parameter;"
            + "FCAdaptation=Parameter;"
            + "latVertDist=Parameter;"
            + "k=Parameter;"
            + "petMult=Parameter;"
            + "t_thres=Parameter;"
            + "precipAdj=Parameter;"
            + "obsQ=Measurement;"
            + "totQcmb=Timeserie;"
            + "soilDiffMPSLPS=Parameter;"
            + "snow_trs=Parameter;"
            + "snow_trans=Parameter;"
            + "soilMaxInfWinter=Parameter;"
            + "soilOutLPS=Parameter;"
            + "gwRG2Fact=Parameter;"
            + "soilConcRD1=Parameter;"
            + "soilConcRD2=Parameter;"
            + "soilConcRD1flood=Parameter;"
            + "ccf_factor=Parameter;"
            + "r_factor=Parameter;"
            + "soilPolRed=Parameter;"
            + "gwRG1RG2dist=Parameter;"
            + "baseTemp=Parameter;"
            + "a_rain=Parameter;"
            + "soilImpLT80=Parameter;"
            + "soilImpGT80=Parameter;"
            + "soilLinRed=Parameter;"
            + "flowRouteTA=Parameter;"
            + "gwRG1Fact=Parameter;"
            + "gwCapRise=Parameter;"
            + "g_factor=Parameter;"
            + "soilMaxDPS=Parameter;"
            + "soilDistMPSLPS=Parameter;"
            + "a_snow=Parameter;"
            + "soilMaxPerc=Parameter;"
            + "t_factor=Parameter;"
            + "soilMaxInfSummer=Parameter;"
            + "soilMaxInfSnow=Parameter;"
            + "soilLatVertLPS=Parameter;"
            + "snowCritDens=Parameter;"
            + "ioa1=PosEfficiency;"
            + "ioa2=PosEfficiency;"
            + "e1=PosEfficiency;"
            + "e2=PosEfficiency;"
            + "le1=PosEfficiency;"
            + "le2=PosEfficiency;"
            + "grad=PosEfficiency;"
            + "rmse=NegEfficiency;"
            + "pbias=NegEfficiency;"
            + "dsgrad=NegEfficiency;"
            + "r2=Efficiency(Positive);"
            + "rsq=PosEfficiency;"
            + "catchmentSimRunoff_qm=Timeserie;"
            + "catchmentObsRunoff=Measurement;"
            + "e1_normalized=NegEfficiency;"
            + "e2_normalized=NegEfficiency;"
            + "le1_normalized=NegEfficiency;"
            + "le2_normalized=NegEfficiency;"
            + "bias_normalized=NegEfficiency;"
            + "ave_normalized=NegEfficiency;"
            + "x1=Parameter;"
            + "x2=Parameter;";
    
    ArrayList<Processor> fileProcessors = new ArrayList<Processor>();
    HashMap<AttributeData, Processor> attributeDataMap = new HashMap<AttributeData, Processor>();
    HashMap<AttributeData, EnsembleType> typeMap = new HashMap<AttributeData, EnsembleType>();
    EnumMap<EnsembleType, Class> simpleDatasetClasses = new EnumMap<EnsembleType, Class>(EnsembleType.class);
    EnumMap<EnsembleType, Class> timeSerieDatasetClasses = new EnumMap<EnsembleType, Class>(EnsembleType.class);
    
    public enum EnsembleType{Parameter, StateVariable, Measurement, NegEfficiency, PosEfficiency, Timeserie, Unknown, Ignore};
    
    final EnsembleType ensembleVariableTypes[] = {EnsembleType.Ignore, EnsembleType.Parameter, EnsembleType.StateVariable, EnsembleType.NegEfficiency, EnsembleType.PosEfficiency};
    final EnsembleType ensembleTimeserieTypes[] = {EnsembleType.Ignore, EnsembleType.Timeserie, EnsembleType.Measurement};
    final EnsembleType timeserieTypes[] = {EnsembleType.Ignore, EnsembleType.Measurement};
            
    
    /*String  parameterString = Optas.i18n("Parameter"),
            stateVariableString = Optas.i18n("State-Variable"),
            measurementString = Optas.i18n("Measurement"),
            efficiencyStringNeg = Optas.i18n("NegEfficiency"),
            efficiencyStringPos = Optas.i18n("PosEfficiency"),
            timeseriesString = Optas.i18n("Timeserie-Ensemble"),
            unknownString = Optas.i18n("UNKNOWN");
    String  emptyString = "";*/
    
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

        StringTokenizer tok = new StringTokenizer(defaultTypes, ";");
        int n = tok.countTokens();
        for (int i = 0; i < n; i++) {
            String token = tok.nextToken();
            String keyValuePair[] = token.split("=");
            try{
                this.defaultAttributeTypes.put(keyValuePair[0], EnsembleType.valueOf(keyValuePair[1]));
            }catch(IllegalArgumentException iae){
                this.defaultAttributeTypes.put(keyValuePair[0], EnsembleType.Unknown);
            }
        }
    }

    public EnsembleType[] getValidProcessingOptions(Processor p) {                
        switch (DataStoreProcessor.getDataStoreType(p.getDataStoreProcessor().getFile())) {
            case DataStoreProcessor.EnsembleTimeSeriesDataStore:
                return ensembleTimeserieTypes;
            case DataStoreProcessor.SimpleEnsembleDataStore:
                return ensembleVariableTypes;
            case DataStoreProcessor.SimpleDataSerieDataStore:
                return ensembleVariableTypes;
            case DataStoreProcessor.SimpleTimeSerieDataStore:
                return timeserieTypes;
        }
        return null;
    }

    public boolean addFile(File file) throws ImportMonteCarloException {
        if (isEmptyFile(file)) {
            return isValid = false;
        }
        loadDataStore(file);
        updateDataTable();
        return isValid = true;
    }

    public Processor getProcessorForAttribute(AttributeData a) {
        return this.attributeDataMap.get(a);
    }

    private void loadDataStore(File file) throws ImportMonteCarloException {
        Processor proc = null;
        DataStoreProcessor dsdb = new DataStoreProcessor(file);
        try {
            dsdb.createDB();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ImportMonteCarloException(Optas.i18n("Could_not_load_data_store_") + file, ioe);
        } catch (SQLException ioe) {
            ioe.printStackTrace();
            throw new ImportMonteCarloException(Optas.i18n("Could_not_create_database"), ioe);
        } catch (ClassNotFoundException ioe) {
            ioe.printStackTrace();
            throw new ImportMonteCarloException(Optas.i18n("Could_not_load_data_store_"), ioe);
        }
        
        switch (DataStoreProcessor.getDataStoreType(file)) {
            case DataStoreProcessor.UnsupportedDataStore:
                return;
            case DataStoreProcessor.EnsembleTimeSeriesDataStore:
                proc = new EnsembleTimeSeriesProcessor(dsdb);
                break;
            case DataStoreProcessor.TimeSpaceDataStore:
                proc = new TimeSpaceProcessor(dsdb);
                break;
            case DataStoreProcessor.SimpleEnsembleDataStore:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            case DataStoreProcessor.SimpleDataSerieDataStore:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            case DataStoreProcessor.SimpleTimeSerieDataStore:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            default:
                return;
        }
        fileProcessors.add(proc);
    }
    static int badIDCounter = 1000000;

    private DataCollection buildEnsemble() throws ImportMonteCarloException {
        ensemble = new DataCollection();
        String samplerClass = null;

        for (AttributeData a : this.attributeDataMap.keySet()) {
            EnsembleType dataSetClassName = this.typeMap.get(a);
            Processor p = this.attributeDataMap.get(a);

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
                if (simpleDatasetClasses.containsKey(dataSetClassName)) {
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
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), sqle);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), ioe);
            } catch (MismatchException me) {
                me.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), me);
            } catch (ClassCastException cce) {
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), cce);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), t);
            }
            try {
                if (timeSerieDatasetClasses.containsKey(dataSetClassName)) {
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

                    ensembleTime = JAMSDataFactory.createTimeInterval();
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
                                        throw new MismatchException(Optas.i18n("timeserie_ensemble_could_not_be_used_as_measurement"));
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
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), sqle);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), ioe);
            } catch (MismatchException me) {
                me.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), me);
            } catch (ClassCastException cce) {
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), cce);
            } catch (Throwable t) {
                t.printStackTrace();
                throw new ImportMonteCarloException(Optas.i18n("Could_not_build_ensemble_"), t);
            }
        }
        return ensemble;
    }

    public EnsembleType getAttributeTypeDefault(AttributeData a) {
        return (EnsembleType)this.defaultAttributeTypes.get(a.getName());
    }

    public boolean isEmpty() {
        return this.attributeDataMap.isEmpty();
    }

    private boolean isEmptyFile(File file) throws ImportMonteCarloException {
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
                throw new ImportMonteCarloException(Optas.i18n("Could_not_read_file_") + file.toString(), ioe);
            }
            return true;
        }
    }
    
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
            switch (DataStoreProcessor.getDataStoreType(p.getDataStoreProcessor().getFile())) {
                case DataStoreProcessor.EnsembleTimeSeriesDataStore:
                    processorTypeMap.put(p, ensembleTimeserieTypes);
                    break;
                case DataStoreProcessor.SimpleEnsembleDataStore:
                    processorTypeMap.put(p, ensembleVariableTypes);
                    break;
                case DataStoreProcessor.SimpleDataSerieDataStore:
                    processorTypeMap.put(p, ensembleVariableTypes);
                    break;
                case DataStoreProcessor.SimpleTimeSerieDataStore:
                    processorTypeMap.put(p, timeserieTypes);
                    break;
            }

            for (AttributeData a : p.getDataStoreProcessor().getAttributes()) {
                attributeDataMap.put(a, p);
                this.typeMap.put(a, (EnsembleType)getAttributeTypeDefault(a));
            }
        }
    }

    public void finish() {
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

    public static void main(String[] args) throws ImportMonteCarloException {
        //ImportMonteCarloData imcd = new ImportMonteCarloData(new File("C:/Arbeit/optimization_wizard_optimizer.dat"));
        //System.out.println(imcd.getEnsemble());
    }
}