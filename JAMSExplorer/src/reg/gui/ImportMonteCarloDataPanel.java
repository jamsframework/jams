/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import reg.dsproc.DataMatrix;
import reg.dsproc.DataStoreProcessor;
import reg.dsproc.DataStoreProcessor.AttributeData;
import reg.dsproc.DataStoreProcessor.ContextData;
import reg.dsproc.EnsembleTimeSeriesProcessor;
import reg.dsproc.Processor;
import reg.dsproc.SimpleSerieProcessor;
import reg.dsproc.TimeSpaceProcessor;
import reg.hydro.data.DataCollection;
import reg.hydro.data.DataSet.MismatchException;
import reg.hydro.data.Efficiency;
import reg.hydro.data.Measurement;
import reg.hydro.data.Modelrun;
import reg.hydro.data.Parameter;
import reg.hydro.data.SimpleDataSet;
import reg.hydro.data.StateVariable;
import reg.hydro.data.TimeSerie;

/**
 *
 * @author chris
 */
public class ImportMonteCarloDataPanel extends JPanel {

    Dimension defaultFilesTable = new Dimension(500, 150);
    Dimension defaultDatasetTable = new Dimension(500, 200);
    Dimension defaultWindowSize = new Dimension(550, 500);

    ArrayList<Processor> fileProcessors = new ArrayList<Processor>();
    HashMap<AttributeData, Processor> attributeDataMap = new HashMap<AttributeData, Processor>();
    HashMap<AttributeData, JComboBox> attributeComboBoxMap = new HashMap<AttributeData, JComboBox>();
    HashMap<String, Class> simpleDatasetClasses = new HashMap<String, Class>();
    HashMap<String, Class> timeSerieDatasetClasses = new HashMap<String, Class>();
    JTable fileTable = null;
    JPanel dataPanel = null;
    String parameterString = "Parameter";
    String stateVariableString = "State-Variable";
    String measurementString = "Measurement";
    String efficiencyStringNeg = "Efficiency(Negative)";
    String efficiencyStringPos = "Efficiency(Postive)";
    String timeseriesString = "Timeserie - Ensemble";
    String emptyString = "";
    DataCollection ensemble = null;
    JFileChooser chooser = new JFileChooser();

    ArrayList<ActionListener> listenerList =  new ArrayList<ActionListener>();
    JFrame owner;
    JDialog ownerDlg = null;

    final HashMap<String, String> defaultAttributeTypes = new HashMap<String, String>();

    public ImportMonteCarloDataPanel(JFrame owner) {
        this.owner = owner;
        init();
    }
    public ImportMonteCarloDataPanel(JFrame owner, DataCollection dc) {
        this.owner = owner;        
        init();
    }

    public JDialog getDialog(){
        ownerDlg = new JDialog(this.owner,"Import Ensemble Data");
        ownerDlg.add(this);
        ownerDlg.setPreferredSize(defaultWindowSize);
        ownerDlg.setMinimumSize(defaultWindowSize);

        return ownerDlg;
    }

    private void loadDataStore(File file) {
        Processor proc = null;
        DataStoreProcessor dsdb = new DataStoreProcessor(file);
        try{
            dsdb.createDB();

        }catch(IOException ioe){
            System.out.println(ioe.toString());
        }catch(SQLException ioe){
            System.out.println(ioe.toString());
        }catch(ClassNotFoundException ioe){
            System.out.println(ioe.toString());
        }
        switch (DataStoreProcessor.getDataStoreType(file)) {
            case DataStoreProcessor.UnsupportedDataStore:
                JOptionPane.showMessageDialog(ImportMonteCarloDataPanel.this, "unsupported datastore");
                return;
            case DataStoreProcessor.EnsembleTimeSeriesDataStore:
                proc = new EnsembleTimeSeriesProcessor(dsdb);
                break;
            case DataStoreProcessor.TimeSpaceDataStore:
                proc = new TimeSpaceProcessor(dsdb);
                break;
            case DataStoreProcessor.SimpleDataSerieDataStore:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            case DataStoreProcessor.SimpleTimeSerieDataStore:
                proc = new SimpleSerieProcessor(dsdb);
                break;
            default:
                JOptionPane.showMessageDialog(ImportMonteCarloDataPanel.this, "unsupported datastore");
                return;
        }        
        fileProcessors.add(proc);
    }
    static int badIDCounter = 1000000;
    private DataCollection buildEnsemble() {
        ensemble = new DataCollection();
        String samplerClass = null;

        for (AttributeData a : this.attributeDataMap.keySet()) {
            JComboBox b = this.attributeComboBoxMap.get(a);
            Processor p = this.attributeDataMap.get(a);
            String selection = (String) b.getSelectedItem();

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
                        String[] namedTimesteps = new String[timesteps.length];
                        for (int i = 0; i < timesteps.length; i++) {
                            namedTimesteps[i] = timesteps[i].toString();
                        }

                        ensembleTime = JAMSDataFactory.createTimeInterval();
                        ensembleTime.setStart(timesteps[0]);
                        ensembleTime.setEnd(timesteps[timesteps.length - 1]);
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
            } catch (Throwable t){
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
        return types[0];
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
                case DataStoreProcessor.SimpleDataSerieDataStore:
                    processorTypeMap.put(p, ensembleVariableTypes);
                    break;
                case DataStoreProcessor.SimpleTimeSerieDataStore:
                    processorTypeMap.put(p, timeserieTypes);
                    break;
            }

            for (AttributeData a : p.getDataStoreProcessor().getAttributes()) {
                attributeDataMap.put(a, p);
            }
        }
        TreeSet<AttributeData> t = new TreeSet<AttributeData>(attributeDataMap.keySet());
        dataPanel.removeAll();
        dataPanel.setLayout(new GridBagLayout());

        int counter = 0;
        for (AttributeData a : t) {
            Processor p = attributeDataMap.get(a);

            JComboBox typeSelection = this.attributeComboBoxMap.get(a);
            if (typeSelection == null) {
                typeSelection = new JComboBox(processorTypeMap.get(p));
                typeSelection.setSelectedItem(getAttributeTypeDefault(a,processorTypeMap.get(p)));
                typeSelection.setPreferredSize(new Dimension(175,25));
                typeSelection.setMaximumSize(new Dimension(175,25));
                this.attributeComboBoxMap.put(a, typeSelection);
            }
            typeSelection.putClientProperty("attribute", a);

            GridBagConstraints c = new GridBagConstraints();
            c.gridy = counter;

            c.gridx = 0;
            c.anchor = GridBagConstraints.WEST;
            c.ipadx = 10;
            JLabel lbl = new JLabel(a.getName());
            lbl.setHorizontalTextPosition(SwingConstants.LEFT);
            dataPanel.add(lbl, c);
            c.gridx = 1;
            dataPanel.add(new JLabel(p.getDataStoreProcessor().getFile().getName()), c);
            
            c.gridx = 2;
            dataPanel.add(typeSelection, c);
            counter++;
        }
        dataPanel.invalidate();
        dataPanel.updateUI();
    }

    private void updateFileTable() {
        String[] columnNames = {
            "Filename", "Type", "Number of Columns"
        };

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);

        for (Processor p : fileProcessors) {
            File file = p.getDataStoreProcessor().getFile();
            String type = null;
            switch (DataStoreProcessor.getDataStoreType(file)) {
                case DataStoreProcessor.EnsembleTimeSeriesDataStore:
                    type = "Ensemble Timeserie";
                    break;
                case DataStoreProcessor.TimeSpaceDataStore:
                    type = "Time/Space";
                    break;
                case DataStoreProcessor.SimpleDataSerieDataStore:
                    type = "Ensemble Value";
                    break;
                case DataStoreProcessor.SimpleTimeSerieDataStore:
                    type = "Simple Timeserie";
                    break;
            }
            String columns = Integer.toString(p.getDataStoreProcessor().getAttributes().size());
            model.addRow(new String[]{file.getName(), type, columns});

            fileTable.setModel(model);
            fileTable.getColumnModel().getColumn(0).setWidth(150);
        }

    }

    private JPanel createFileTable() {
        JPanel overviewLoadedFiles = new JPanel(new BorderLayout());

        String[][] rowData = {
            {"", "", ""}
        };
        String[] columnNames = {
            "Filename", "Type", "Number of Columns"
        };
        fileTable = new JTable(rowData, columnNames) {

            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                //adjust row height

            }
        };

        JScrollPane loadedFilesScroll = new JScrollPane(fileTable);
        loadedFilesScroll.setSize(defaultFilesTable);
        loadedFilesScroll.setMinimumSize(defaultFilesTable);
        loadedFilesScroll.setPreferredSize(defaultFilesTable);
        //loadedFilesScroll.setRowHeight(0, 30);

        overviewLoadedFiles.add(loadedFilesScroll, BorderLayout.NORTH);

        JPanel buttonBar = new JPanel(new FlowLayout());
        JButton loadFileButton = new JButton("Load File");
        loadFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                chooser.setFileFilter(new FileFilter() {

                    public boolean accept(File f) {
                        if (f.getName().toLowerCase().endsWith(".dat")) {
                            return true;
                        }
                        if (f.isDirectory()) {
                            return true;
                        }
                        return false;
                    }

                    public String getDescription() {
                        return "data file";
                    }
                });
                int returnValue = chooser.showOpenDialog(ImportMonteCarloDataPanel.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    WorkerDlg progress = new WorkerDlg(ImportMonteCarloDataPanel.this.owner, "Import Data");
                    progress.setInderminate(true);
                    progress.setTask(new Runnable() {

                        File file = null;

                        {
                            //this have to be done in constructor because chooser is final
                            file = chooser.getSelectedFile();
                        }

                        public void run() {
                            loadDataStore(file);
                            updateFileTable();
                            updateDataTable();
                        }
                    });
                    progress.execute();
                }
            }
        });
        buttonBar.add(loadFileButton);

        JButton removeFileButton = new JButton("Remove Dataset");

        removeFileButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int selectedRow = ImportMonteCarloDataPanel.this.fileTable.getSelectedRow();
                ImportMonteCarloDataPanel.this.fileProcessors.remove(selectedRow);
                updateFileTable();
                updateDataTable();
            }
        });

        buttonBar.add(removeFileButton);

        overviewLoadedFiles.add(loadedFilesScroll, BorderLayout.NORTH);
        overviewLoadedFiles.add(buttonBar, BorderLayout.SOUTH);

        return overviewLoadedFiles;
    }
    TreeSet<String> ensembleIDs = new TreeSet<String>();
    TreeSet<String> ensembleTimesteps = new TreeSet<String>();
    TimeInterval ensembleTime = null;

    public DataCollection getEnsemble() {
        return ensemble;
    }

    private Component createDataSetOverview() {
        dataPanel = new JPanel(new BorderLayout());
        JScrollPane datasetScroll = new JScrollPane(dataPanel);
        datasetScroll.setSize(defaultDatasetTable);
        datasetScroll.setMinimumSize(defaultDatasetTable);
        datasetScroll.setPreferredSize(defaultDatasetTable);
        return datasetScroll;
    }
   
    public void addActionEventListener(ActionListener listener){
        this.listenerList.add(listener);
    }

    private void init() {
        simpleDatasetClasses.put(parameterString, Parameter.class);
        simpleDatasetClasses.put(measurementString, Measurement.class);
        simpleDatasetClasses.put(efficiencyStringNeg, Efficiency.class);
        simpleDatasetClasses.put(efficiencyStringPos, Efficiency.class);
        simpleDatasetClasses.put(stateVariableString, StateVariable.class);

        timeSerieDatasetClasses.put(timeseriesString, TimeSerie.class);
        timeSerieDatasetClasses.put(measurementString, Measurement.class);

        Properties prop = new Properties();
        try{
            prop.load(ClassLoader.getSystemResourceAsStream("reg/resources/DefaultAttributeTypes.properties"));
        }catch(IOException ioe){
            System.out.println("Could not load DefaultAttributeTypes.properties!");
            ioe.printStackTrace();
        }
        Set<Object> keys = prop.keySet();
        for (Object key : keys){
            this.defaultAttributeTypes.put(key.toString(), prop.getProperty(key.toString()));
        }
        JPanel panel = new JPanel(new BorderLayout());

        panel.add(createFileTable(), BorderLayout.NORTH);
        panel.add(createDataSetOverview(), BorderLayout.CENTER);

        JPanel buttonBar = new JPanel(new FlowLayout());
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                WorkerDlg progress = new WorkerDlg(ImportMonteCarloDataPanel.this.owner, "Import Data");
                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    public void run() {
                        ImportMonteCarloDataPanel.this.buildEnsemble();
                        ImportMonteCarloDataPanel.this.setVisible(false);
                        if (ImportMonteCarloDataPanel.this.ownerDlg!=null){
                            ImportMonteCarloDataPanel.this.ownerDlg.setVisible(false);
                        }
                        for (ActionListener listener : listenerList){
                            listener.actionPerformed(new ActionEvent(ImportMonteCarloDataPanel.this,ActionEvent.ACTION_PERFORMED, "cmd"));
                        }
                    }
                });
                progress.execute();
            }
        });
        buttonBar.add(okButton);
        buttonBar.add(new JButton("Cancel") {

            {
                addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        if (ImportMonteCarloDataPanel.this.ownerDlg!=null){
                            ImportMonteCarloDataPanel.this.ownerDlg.setVisible(false);
                        }
                    }
                });
            }
        });
        panel.add(buttonBar, BorderLayout.SOUTH);
        this.add(panel);
        setSize(defaultWindowSize);
    }
}
