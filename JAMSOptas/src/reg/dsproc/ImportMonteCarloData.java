/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.dsproc;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import reg.dsproc.DataStoreProcessor.AttributeData;
import reg.dsproc.DataStoreProcessor.ContextData;
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
public class ImportMonteCarloData implements Serializable{
    ArrayList<Processor> fileProcessors = new ArrayList<Processor>();
    HashMap<AttributeData, Processor> attributeDataMap = new HashMap<AttributeData, Processor>();
    HashMap<AttributeData, String> attributeComboBoxMap = new HashMap<AttributeData, String>();
    HashMap<String, Class> simpleDatasetClasses = new HashMap<String, Class>();
    HashMap<String, Class> timeSerieDatasetClasses = new HashMap<String, Class>();
    String parameterString = "Parameter";
    String stateVariableString = "State-Variable";
    String measurementString = "Measurement";
    String efficiencyStringNeg = "Efficiency(Negative)";
    String efficiencyStringPos = "Efficiency(Postive)";
    String timeseriesString = "Timeserie - Ensemble";
    String unknownString = "UNKNOWN";
    String emptyString = "";
    DataCollection ensemble = null;

    boolean isImportCollection = false;
    DataCollection importedCollection = null;
    
    final TreeMap<String, String> defaultAttributeTypes = new TreeMap<String, String>();

    boolean isValid = false;
    public ImportMonteCarloData(File file) {        
        init();
        isValid = addFile(file);
    }
/*
DataCollection newCollection = null;
                            if (isImportCollection)
                                newCollection = importedCollection;
                            else
                                newCollection = ImportMonteCarloData.this.buildEnsemble();

                            if (ensemble == null) {
                                ensemble = newCollection;
                            } else {
                                switch (getMergeMode()) {
                                    case ATTACH: {
                                        ensemble.mergeDataCollections(newCollection);
                                        break;
                                    }
                                    case UNIFY: {
                                        ensemble.unifyDataCollections(newCollection);
                                        break;
                                    }
                                }
                            }
*/
    
    private void loadDataStore(File file) {
        Processor proc = null;
        DataStoreProcessor dsdb = new DataStoreProcessor(file);
        try{
            dsdb.createDB();

        }catch(IOException ioe){
            ioe.printStackTrace();
            System.out.println(ioe.toString());
        }catch(SQLException ioe){
            ioe.printStackTrace();
            System.out.println(ioe.toString());
        }catch(ClassNotFoundException ioe){
            ioe.printStackTrace();
            System.out.println(ioe.toString());
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
    private DataCollection buildEnsemble() {
        ensemble = new DataCollection();
        String samplerClass = null;

        for (AttributeData a : this.attributeDataMap.keySet()) {
            String selection = this.attributeComboBoxMap.get(a);
            Processor p = this.attributeDataMap.get(a);
            
            for (ContextData c : p.getDataStoreProcessor().getContexts()) {
                if (c.getType().startsWith("jams.components.optimizer")) {
                    if (samplerClass == null) {
                        samplerClass = c.getType();
                        ensemble.setSamplerClass(samplerClass);
                    } else if (!c.getType().equals(samplerClass)) {
                        samplerClass = "jams.components.optimizer.Optimizer";
                    }
                }
            }
            try {
                for (String dataSetClassName : simpleDatasetClasses.keySet()) {
                    if (selection.equals(dataSetClassName)) {
                        if (!(p instanceof SimpleSerieProcessor))
                            continue;
                        SimpleSerieProcessor s = ((SimpleSerieProcessor) p);
                        String[] ids = s.getIDs();
                        for (AttributeData ad : s.getDataStoreProcessor().getAttributes())
                            ad.setSelected(false);
                        a.setSelected(true);
                        DataMatrix m = s.getData(ids);
                        a.setSelected(false);
                        int row = 0;
                        for (String id : ids) {
                            Integer intID = null;
                            try{
                                intID = Integer.parseInt(id);
                            }catch(NumberFormatException nfe){
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
                System.out.println(sqle);
                sqle.printStackTrace();
            } catch (IOException ioe) {
                System.out.println(ioe);
                ioe.printStackTrace();
            } catch (MismatchException me) {
                System.out.println(me);
                me.printStackTrace();
            } catch (Throwable t){
                System.out.println(t.toString());
                t.printStackTrace();
            }
            try {
                for (String dataSetClassName : timeSerieDatasetClasses.keySet()) {
                    if (selection.equals(dataSetClassName)) {
                        EnsembleTimeSeriesProcessor s = ((EnsembleTimeSeriesProcessor) p);
                        long[] ids = s.getModelRuns();
                        Calendar[] timesteps = s.getTimeSteps();
                        if (timesteps == null){
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

                        for (AttributeData ad : s.getDataStoreProcessor().getAttributes())
                            ad.setSelected(false);
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
                        } else if (this.timeSerieDatasetClasses.get(dataSetClassName).equals(Measurement.class)){
                            int col = 0;
                            Measurement ts = null;
                            for (Long id : ids) {
                                Modelrun r = new Modelrun(id.intValue(), null);
                                Measurement ts2 = new Measurement(new TimeSerie(m.getCol(col), ensembleTime, a.getName(), r));
                                if (ts == null)
                                    ts = ts2;
                                else{
                                    for (int i=0;i<ts.getTimeDomain().getNumberOfTimesteps();i++){
                                        if (ts.getValue(i)!=ts2.getValue(i))
                                            System.out.println("timeserie ensemble could not be used as measurement");
                                    }
                                }
                                col++;
                            }
                            ensemble.addTimeSerie(ts);
                        }
                    }
                }
            } catch (SQLException sqle) {
                System.out.println(sqle);
                sqle.printStackTrace();
            } catch (IOException ioe) {
                System.out.println(ioe);
                ioe.printStackTrace();
            } catch (MismatchException me) {
                System.out.println(me);
                me.printStackTrace();
            } catch(ClassCastException cce){
                
            } catch (Throwable t) {
                System.out.println(t.toString());t.printStackTrace();
            }
        }
        return ensemble;
    }

    private Object getAttributeTypeDefault(AttributeData a, String[] types){
        String defaultType = this.defaultAttributeTypes.get(a.getName());
        for (String type : types){
            if (type.equals(defaultType))
                return type;
        }
        //unknown attribute --> fallback return first string
        return this.unknownString;
    }

    public boolean isEmpty(){
        return this.attributeDataMap.isEmpty();
    }

    private boolean isEmptyFile(File file){
        BufferedReader reader = null;
        try{
        reader = new BufferedReader(new FileReader(file));
        String line = null;
        boolean isDataStart = false;
        int dataCounter = 0;

        while((line = reader.readLine())!=null){
            if (line.contains("@data")){
                isDataStart = true;
            }else
                if (isDataStart)
                    dataCounter++;
        }
        reader.close();
        return dataCounter<=1;

        }catch(IOException fnfe){
            try{
                if (reader!=null)
                    reader.close();
            }catch(IOException ioe){
                ioe.printStackTrace();
            }
            return true;
        }
    }
    private boolean addFile(File file) {
        if (isEmptyFile(file))
            return false;
        loadDataStore(file);
        updateDataTable();
        return true;
    }
            
    TreeSet<String> ensembleIDs = new TreeSet<String>();
    TreeSet<String> ensembleTimesteps = new TreeSet<String>();
    TimeInterval ensembleTime = null;

    public DataCollection getEnsemble() {
        if (!isValid)
            return null;
        buildEnsemble();
        return ensemble;
    }

    private void updateDataTable() {
        String ensembleVariableTypes[] = {emptyString, parameterString, stateVariableString, efficiencyStringNeg, efficiencyStringPos};
        String timeserieTypes[] = {emptyString, measurementString};
        String ensembleTimeSerieTypes[] = {emptyString, timeseriesString, measurementString};

        HashMap<Processor, String[]> processorTypeMap = new HashMap<Processor, String[]>();
        /*attributeComboboxList.clear();
        attributeDataMap.clear();*/

        for (Processor p : fileProcessors) {
            switch (DataStoreProcessor.getDataStoreType(p.getDataStoreProcessor().getFile())) {

                case DataStoreProcessor.EnsembleTimeSeriesDataStore:
                    processorTypeMap.put(p, ensembleTimeSerieTypes);
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
                this.attributeComboBoxMap.put(a, (String)getAttributeTypeDefault(a,processorTypeMap.get(p)));
            }
        }
    }

    String typeMap = "initRG1=Parameter;"
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
            + "totQcmb=Timeserie - Ensemble;"
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
            + "ioa1=Efficiency(Postive);"
            + "ioa2=Efficiency(Postive);"
            + "e1=Efficiency(Postive);"
            + "e2=Efficiency(Postive);"
            + "le1=Efficiency(Postive);"
            + "le2=Efficiency(Postive);"
            + "grad=Efficiency(Postive);"
            + "rmse=Efficiency(Negative);"
            + "pbias=Efficiency(Negative);"
            + "dsgrad=Efficiency(Negative);"
            + "r2=Efficiency(Positive);"
            + "rsq=Efficiency(Postive);"
            + "catchmentSimRunoff_qm=Timeserie - Ensemble;"
            + "catchmentObsRunoff=Measurement;"
            + "e1_normalized=Efficiency(Negative);"
            + "e2_normalized=Efficiency(Negative);"
            + "le1_normalized=Efficiency(Negative);"
            + "le2_normalized=Efficiency(Negative);"
            + "bias_normalized=Efficiency(Negative);"
            + "ave_normalized=Efficiency(Negative);"
            + "x1=Parameter;"
            + "x2=Parameter;";

    private void init() {
        simpleDatasetClasses.put(parameterString, Parameter.class);
        simpleDatasetClasses.put(measurementString, Measurement.class);
        simpleDatasetClasses.put(efficiencyStringNeg, NegativeEfficiency.class);
        simpleDatasetClasses.put(efficiencyStringPos, PositiveEfficiency.class);
        simpleDatasetClasses.put(stateVariableString, StateVariable.class);
        simpleDatasetClasses.put(unknownString, Parameter.class);

        timeSerieDatasetClasses.put(timeseriesString, TimeSerie.class);
        //timeSerieDatasetClasses.put(unknownString, TimeSerie.class);
        timeSerieDatasetClasses.put(measurementString, Measurement.class);

        /*Properties prop = new Properties();
        try{
            prop.load(ClassLoader.getSystemResourceAsStream("optas/resources/DefaultAttributeTypes.properties"));
        }catch(IOException ioe){
            System.out.println("Could not load DefaultAttributeTypes.properties!");
            ioe.printStackTrace();
        }*/
        StringTokenizer tok = new StringTokenizer(typeMap,";");
        int n = tok.countTokens();
        for (int i=0;i<n;i++){
            String token = tok.nextToken();
            String keyValuePair[] = token.split("=");
            this.defaultAttributeTypes.put(keyValuePair[0], keyValuePair[1]);
        }
        /*
        Set<Object> keys = prop.keySet();
        for (Object key : keys){
            this.defaultAttributeTypes.put(key.toString(), prop.getProperty(key.toString()));
        } */
    }

    public void finish(){
        try{
        for (Processor p : fileProcessors)
            p.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        this.fileProcessors = null;
        this.attributeComboBoxMap = null;
        this.ensemble = null;
        this.ensembleIDs = null;
        this.importedCollection = null;
    }

    public static void main(String[] args) {
        ImportMonteCarloData imcd = new ImportMonteCarloData(new File("C:/Arbeit/optimization_wizard_optimizer.dat"));
        System.out.println(imcd.getEnsemble());
    }
}