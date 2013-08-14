/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.JAMS;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.gui.input.ValueChangeListener;
import jams.meta.ComponentDescriptor;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.tools.JAMSTools;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import optas.data.TimeFilter;
import optas.data.TimeSerie;
import optas.efficiencies.UniversalEfficiencyCalculator;
import optas.io.TSDataReader;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.XYItemEntity;

/**
 *
 * @author christian
 */
public class ObjectiveConfiguration extends JPanel{
    final Dimension preferredDimension = new Dimension(1220,700);
    
    JComboBox contextList     = new JComboBox(),
              measurementList = new JComboBox(),
              simulationList = new JComboBox(),              
              objectivesList = new JComboBox();
                                     
    TimeFilterTableInput filterList = new TimeFilterTableInput(null);
    HydrographChart hydroChart = new HydrographChart();
    ChartPanel chartPanel = null;
    
    JPanel timeIntervalPanel = new JPanel();
    JButton loadTimeSerie = new JButton(JAMS.i18n("Load_Timeserie"));
    JButton okButton = new JButton(JAMS.i18n("OK"));                        
    JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
        
    JComboBox recentTimeSeries = new JComboBox();
    
    jams.gui.input.TimeintervalInput modelTimeIntervalInput = new jams.gui.input.TimeintervalInput(true);
    JAMSProperties systemProperties = JAMSProperties.createProperties();
    File defaultPropertyFile = null;
    
    DefaultComboBoxModel<ObjectiveDescription> objectives = new DefaultComboBoxModel();
    HashSet<ActionListener> listeners = new HashSet<ActionListener>();
    ModelDescriptor md = null;
    
    JDialog dialog = null;
    
    JFileChooser timeseriesFileChooser = new JFileChooser();
    
    private ActionListener measurementListUpdateListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ( measurementList.getSelectedItem() instanceof Attribute ){
                    ObjectiveDescription od = (ObjectiveDescription)objectivesList.getSelectedItem();
                    od.setMeasurementAttribute((Attribute)measurementList.getSelectedItem());
                }                
            }
        };
    
    private ActionListener simulationListUpdateListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ( simulationList.getSelectedItem() instanceof Attribute ){
                    ObjectiveDescription od = (ObjectiveDescription)objectivesList.getSelectedItem();
                    od.setSimulationAttribute((Attribute)simulationList.getSelectedItem());
                }                
            }
        };
    
    public ObjectiveConfiguration(ModelDescriptor md, Logger logger){
        this.md = md;
        //load default property file, if its not existing, never mind as it is only used for the recent files entry
        String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;        
        defaultPropertyFile = new File(defaultFile);
        if (defaultPropertyFile.exists()) {
            try{
                systemProperties.load(defaultFile);
            }catch(IOException ioe){
                //not serious
            }
        }
        
        updateRecentTimeseriesList(null);
        
        initGUI();
        
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                applyChanges();
                dialog.setVisible(false);
                ActionEvent evt = new ActionEvent(ObjectiveConfiguration.this, 0, "objective updated");
                for (ActionListener l : ObjectiveConfiguration.this.listeners){                    
                    l.actionPerformed(evt);
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        
        recentTimeSeries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {                                
                File f = new File((String)recentTimeSeries.getSelectedItem());
                if (f.exists())
                    loadTimeseries(f);
            }
        });
        
        objectivesList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {                                
                if ( objectivesList.getSelectedItem() instanceof ObjectiveDescription ){
                    ObjectiveDescription od = (ObjectiveDescription)objectivesList.getSelectedItem();    
                    contextList.setSelectedItem(od.getMeasurementAttribute().getParentName());
                    measurementList.setSelectedItem(od.getMeasurementAttribute());
                    simulationList.setSelectedItem(od.getSimulationAttribute());
                    
                    filterList.setTimeFilters(od.getTimeFilters());                                        
                }
                if ( objectivesList.getSelectedItem() instanceof String ){
                    String s = (String)objectivesList.getSelectedItem();
                    ObjectiveDescription od = new ObjectiveDescription(s);
                    ((DefaultComboBoxModel)objectivesList.getModel()).removeElement(s);
                    ((DefaultComboBoxModel)objectivesList.getModel()).addElement(od);
                    objectivesList.getModel().setSelectedItem(od);
                    measurementList.setSelectedIndex(-1);
                    simulationList.setSelectedIndex(-1);
                    
                    filterList.clear();                    
                }
            }
        });
        
        contextList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String context = (String) contextList.getSelectedItem();
                if (context != null) {
                    ModelAnalyzer analyzer = new ModelAnalyzer(ObjectiveConfiguration.this.md);
                    Set<Objective> allAttributes = analyzer.getObjectives();
                    TreeSet<Objective> attributesInContext = new TreeSet<Objective>();
                    for (Objective o : allAttributes) {
                        if (o.getParentName().equals(context)) {
                            attributesInContext.add(o);
                        }
                    }
                    DefaultComboBoxModel measurementModel = new DefaultComboBoxModel(attributesInContext.toArray(new Attribute[0]));
                    DefaultComboBoxModel simulationModel = new DefaultComboBoxModel(attributesInContext.toArray(new Attribute[0]));
                    measurementList.setModel(measurementModel);
                    simulationList.setModel(simulationModel);      
                    for (ActionListener l : measurementList.getActionListeners()){
                        measurementList.removeActionListener(l);
                    }
                    for (ActionListener l : simulationList.getActionListeners()){
                        simulationList.removeActionListener(l);
                    }
                    simulationList.addActionListener(simulationListUpdateListener);
                    simulationList.setSelectedIndex(-1);                    
                    measurementList.addActionListener(measurementListUpdateListener);
                    measurementList.setSelectedIndex(-1);
                }
            }
        });
        
        measurementList.addActionListener(measurementListUpdateListener);
        simulationList.addActionListener(simulationListUpdateListener);
                
        loadTimeSerie.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int result = timeseriesFileChooser.showOpenDialog(ObjectiveConfiguration.this.dialog);
                if (result == JFileChooser.APPROVE_OPTION){
                    File f = timeseriesFileChooser.getSelectedFile();   
                    loadTimeseries(f);
                }
            }
        });
        
        filterList.addChangeListener(new TimeFilterTableInput.TimeFilterTableInputListener() {

            @Override
            public void tableChanged(TimeFilterTableInput tfti) {                                
                ObjectiveDescription od = (ObjectiveDescription)objectivesList.getSelectedItem();
                od.setTimeFilters(filterList.getTimeFilters());
                hydroChart.setTimeFilters(filterList.getTimeFilters()); //1                
            }

            @Override
            public void itemChanged(TimeFilterTableInput tfti) {
                ObjectiveDescription od = (ObjectiveDescription)objectivesList.getSelectedItem();
                od.setTimeFilters(filterList.getTimeFilters());                
                hydroChart.setTimeFilters(filterList.getTimeFilters());                
            }
        });
        
        filterList.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()){
                    DefaultListSelectionModel source = (DefaultListSelectionModel)e.getSource();
                    int index = source.getMinSelectionIndex();
                    if (index != -1){
                        TimeFilter f = filterList.getTimeFilters().get(index);
                        hydroChart.setSelectedTimeFilter(f);
                    }
                }
            }
        });
        chartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent cme) {  
                if (cme.getTrigger().getButton() != 1){
                    return;
                }
                if (cme.getTrigger().getClickCount() != 1){
                    return;
                }

                XYItemEntity entity = ((XYItemEntity)cme.getEntity());
                if (entity == null){
                    return;
                }
                int index = entity.getItem();
                Date time = hydroChart.hydrograph.getTime(index);
                TimeFilter filter = null;
                if (!hydroChart.filters.combine().isFiltered(time)){
                    for (int i=0;i<hydroChart.filters.size();i++){
                        TimeFilter f = hydroChart.filters.get(i);
                        boolean isFiltered = f.isFiltered(time);
                        if (!f.isEnabled()){
                            continue;
                        }
                        if (f.isInverted()){
                            isFiltered = !isFiltered;
                        }
                        if (!isFiltered){
                            filter = f;
                            break;
                        }
                    }
                }
                hydroChart.setSelectedTimeFilter(filter);
                filterList.setSelectedItem(filter);
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent cme) {
                //do nothing
            }
        });
    
        
        if (md != null){
            Enumeration e = md.getRootNode().breadthFirstEnumeration();
            objectives.addElement(null);
            while(e.hasMoreElements()){
                Object nodeObj = e.nextElement();
                if (!(nodeObj instanceof DefaultMutableTreeNode)){
                    continue;
                }
                Object o = ((DefaultMutableTreeNode)nodeObj).getUserObject();
                 
                if (!(o instanceof ComponentDescriptor)){
                    continue;
                }
                ComponentDescriptor cd = (ComponentDescriptor)o;
                if (!UniversalEfficiencyCalculator.class.isAssignableFrom(cd.getClazz())){
                    continue;
                }
                try{
                    ObjectiveDescription od = ObjectiveDescription.importFromComponentDescriptor(cd);
                    objectives.addElement(od);                                        
                }catch(OPTASWizardException owe){
                    System.out.println("TODO");
                    owe.printStackTrace();
                }
            }            
            objectivesList.setModel(objectives);                
            objectivesList.setSelectedIndex(0);
            ModelAnalyzer analyzer = new ModelAnalyzer(md);
            DefaultComboBoxModel measurementModel = new DefaultComboBoxModel(analyzer.getObjectives().toArray(new Attribute[0]));
            DefaultComboBoxModel simulationModel = new DefaultComboBoxModel(analyzer.getObjectives().toArray(new Attribute[0]));
            DefaultComboBoxModel contextModel = new DefaultComboBoxModel();
            for (ComponentDescriptor c : md.getComponentDescriptors().values()){
                if (c instanceof ContextDescriptor){
                    contextModel.addElement(c.getInstanceName());
                }
            }
            
            this.contextList.setModel(contextModel);
            this.contextList.setSelectedIndex(-1);
            
            modelTimeIntervalInput.setValue("1900-11-01 00:00 2100-11-01 00:00 6 1");
            modelTimeIntervalInput.addValueChangeListener(new ValueChangeListener() {

                @Override
                public void valueChanged() {
                    ObjectiveDescription od = (ObjectiveDescription)objectives.getSelectedItem();
                    if (od != null){
                        od.setModelTimeInterval(modelTimeIntervalInput.getTimeInterval());
                    }
                }
            });
        }
    }
               
    private void loadTimeseries(File f) {
        TSDataReader tsr = null;
        try {
            tsr = new TSDataReader(f);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(ObjectiveConfiguration.this.dialog, JAMS.i18n("An error occured .. ") + ioe.toString());
        }        
        if (tsr != null) {
            //TODO errorhandling!!!!                    
            TimeSerie ts = null;
            try{
                ts = tsr.getData(0);
            }catch(OPTASWizardException owe){
                JOptionPane.showMessageDialog(ObjectiveConfiguration.this.dialog, owe.toString());
            }
            modelTimeIntervalInput.setValue(ts.getTimeDomain().getValue());
            if (ts != null) {
                hydroChart.setTimeFilters(filterList.getTimeFilters());
                ObjectiveConfiguration.this.hydroChart.setHydrograph(ts);                
                
                JAMSTools.addToRecentFiles(systemProperties, SystemProperties.RECENT_TIMESERIES_OF_OBJECTIVE_CONFIGURATION, f.getAbsolutePath());
                try {
                    systemProperties.save(defaultPropertyFile.getAbsolutePath());
                } catch (IOException ioe) {
                }
                updateRecentTimeseriesList(f);
            }
            filterList.setTimeSeries(ts);
            filterList.setEnabled(true);            
        }
    }
                
    private void updateRecentTimeseriesList(File lastFile){
        DefaultComboBoxModel recentTimeSeriesModel = new DefaultComboBoxModel(JAMSTools.getRecentFiles(systemProperties, SystemProperties.RECENT_TIMESERIES_OF_OBJECTIVE_CONFIGURATION));
        recentTimeSeriesModel.insertElementAt("",0);
        recentTimeSeries.setModel(recentTimeSeriesModel);
        if (lastFile == null)
            recentTimeSeries.setSelectedIndex(0);
        else
            recentTimeSeries.setSelectedItem(lastFile.getAbsolutePath());
            
    }
    
    public ModelDescriptor getModelDescriptor() {        
        return md;
    }
    
    public void addActionListener(ActionListener listener) {
        this.listeners.add(listener);
    }
    
    private void applyChanges(){
        ModelModifier modifier = new ModelModifier(md);
        ObjectiveDescription odList[] = new ObjectiveDescription[objectives.getSize()];
        for (int i=0;i<odList.length;i++){
            odList[i] = objectives.getElementAt(i);            
        }
        try{
            modifier.updateObjectiveCalculators(odList);
        }catch(OPTASWizardException owe){
            owe.printStackTrace();
        }
    }
            
    private void initGUI(){
        GroupLayout layout = new GroupLayout(this);
        
        hydroChart.setFilterMode(HydrographChart.FilterMode.SINGLE_ROW);
                            
        objectivesList.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Name")));
        measurementList.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Measured_Property")));
        simulationList.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Simulated_Property")));        
        contextList.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Context")));        
        
        objectivesList.setMaximumSize(new Dimension(200, 40));
        objectivesList.setEditable(true);
        measurementList.setMaximumSize(new Dimension(200, 40));
        simulationList.setMaximumSize(new Dimension(200, 40));  
        contextList.setMaximumSize(new Dimension(200, 40));  
        recentTimeSeries.setMaximumSize(new Dimension(200, 25));        
        
        modelTimeIntervalInput.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Model_time_interval")));
        modelTimeIntervalInput.setEnabled(false);
        
        JScrollPane scrollbar = new JScrollPane(filterList);
                
        scrollbar.setPreferredSize(new Dimension(520, 400));
        scrollbar.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("time_filters")));
        timeIntervalPanel.setLayout(new BorderLayout());
        timeIntervalPanel.add(scrollbar, BorderLayout.WEST);
        chartPanel = new ChartPanel(hydroChart.getChart(), true);
        JPanel hydroChartPanel = new JPanel();
        GroupLayout hydroChartLayout = new GroupLayout(hydroChartPanel);
        hydroChartPanel.setLayout(hydroChartLayout);
        hydroChartLayout.setHorizontalGroup(hydroChartLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(chartPanel)
                .addGroup(hydroChartLayout.createSequentialGroup()
                    .addComponent(loadTimeSerie)                    
                    .addComponent(recentTimeSeries)                    
                
                )
        );
        hydroChartLayout.setVerticalGroup(hydroChartLayout.createSequentialGroup()
                .addComponent(chartPanel)
                .addGroup(hydroChartLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(loadTimeSerie)
                    .addComponent(recentTimeSeries)                    
                )
        );
        //timeIntervalPanel.add(hydroChartPanel, BorderLayout.EAST);
        
                
        JPanel generalInformationPanel = new JPanel();
        GroupLayout layout2 = new GroupLayout(generalInformationPanel);
        generalInformationPanel.setLayout(layout2);
        
        layout2.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout2.createSequentialGroup()  
                    .addGroup(layout2.createParallelGroup()  
                    .addComponent(objectivesList)                    
                    .addGap(5)
                    .addComponent(contextList)       
                    .addGap(5)
                    .addComponent(simulationList)                    
                    .addGap(5)
                    .addComponent(measurementList)       
                    .addGap(5)
                )
                    .addComponent(modelTimeIntervalInput)
                )                
            );
        
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.CENTER)      
                    .addGroup(layout2.createSequentialGroup()  
                    .addComponent(objectivesList)
                    .addGap(10)    
                    .addComponent(contextList)  
                    .addGap(10)  
                    .addComponent(simulationList)
                    .addGap(10)                   
                    .addComponent(measurementList)
                    .addGap(10)  
                )
                    .addComponent(modelTimeIntervalInput)
                )
            );
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                    .addComponent(generalInformationPanel)                    
                    .addComponent(timeIntervalPanel)
                    )
                    .addComponent(hydroChartPanel)
                )                                
                .addGroup(layout.createSequentialGroup()                    
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                ));
        
         layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(generalInformationPanel)                    
                    .addComponent(timeIntervalPanel)
                    )
                    .addComponent(hydroChartPanel)
                )                                
                .addGroup(layout.createParallelGroup()                    
                    .addComponent(okButton)
                    .addComponent(cancelButton)
                )
                .addGap(15));
                
        this.setLayout(layout);        
        this.invalidate();
    }
                
    public JDialog showDialog(JFrame parent) {        
        dialog = new JDialog(parent, JAMS.i18n("Efficiency_Configuration"));
        dialog.getContentPane().add(this);
        dialog.revalidate();
        dialog.setSize(preferredDimension);
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
        return dialog;
    }            
}
