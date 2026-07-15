/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import jams.data.JAMSCalendar;
import jams.gui.ObserverWorkerDlg;
import jams.gui.WorkerDlg;
import jams.gui.tools.GUIHelper;
import jams.gui.tools.GUIState;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import optas.gui.wizard.HydrographChart;
import optas.hydro.OptimizationScheme;
import optas.SA.SobolsMethodTemporal;
import optas.hydro.GreedyOptimizationScheme;
import optas.hydro.SimilarityBasedOptimizationScheme;
import optas.hydro.VarianceBasedGreedyOptimizationScheme;
import optas.data.DataCollection;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.TimeFilter;
import optas.data.TimeFilterFactory;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.hydro.gui.WeightChart;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author chris
 */
public class TemporalSAAnalysisANN extends MCAT5Plot {

    int MAX_WEIGHTS = 40;

    public static Color[] getDifferentColors(int n) {
        Color[] cols = new Color[n];

        for (int i = 0; i < n; i++) {
            cols[i] = Color.getHSBColor((float) ((113 * i) % n) / (float) n, (float) ((223 * i) % n) / (float) n, 1.0f);
        }
        return cols;
    }
    Color standardColorList[] = getDifferentColors(MAX_WEIGHTS);
    HydrographChart hydrographChart;
    WeightChart weightChart;
            
    String parameterIDs[] = null;
    XYLineAndShapeRenderer weightRenderer[] = new XYLineAndShapeRenderer[MAX_WEIGHTS];
    JLabel step1Label = new JLabel("Step 1: Data Setup");
    JLabel step2Label = new JLabel("Step 2: Calculate Optimization Scheme");
    JLabel step3Label = new JLabel("Step 3: Export");
    
    JButton trainNetworkBn = new JButton("Train KN - Network");
    JButton loadNetworkBn = new JButton("Load KN - Network");
    JButton saveNetworkBn = new JButton("Save KN - Network");

    JButton calcGreedy1SchemeBn = new JButton("Greedy (VB)");
    JButton calcGreedy2SchemeBn = new JButton("Greedy (Weights only)");
    JButton calcSimilartySchemeBn = new JButton("Similarity");

    //JButton calcSimilartySchemeBn = new JButton("Similarity");
    JButton calcOptimalSchemeBn = new JButton("Optimal");

    JButton exportSchemeBn = new JButton("Export Scheme");
    JTable parameterTable = new JTable(new Object[][]{{Boolean.TRUE, Boolean.TRUE, "test", Color.black}}, new String[]{"x", "y", "z", "a"});
    JLabel infoLabel = new JLabel("Dominance:?");
    
    SimpleEnsemble p[] = null;
    EfficiencyEnsemble e = null;
    TimeSerieEnsemble ts = null;
    Measurement obs = null;    
    JPanel mainPanel = null;

    SobolsMethodTemporal tsa = null;

    VarianceBasedGreedyOptimizationScheme varianceGreedyScheme = new VarianceBasedGreedyOptimizationScheme();
    GreedyOptimizationScheme greedyScheme = new GreedyOptimizationScheme();
    SimilarityBasedOptimizationScheme simBasedScheme = new SimilarityBasedOptimizationScheme();

    OptimizationScheme currentScheme = null;

    JFileChooser jfc = new JFileChooser();

    JPanel progressPanel = new JPanel();

    final GroupConfigurator groupConfigurator = new GroupConfigurator();

    public TemporalSAAnalysisANN() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"), Measurement.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        
        calcGreedy1SchemeBn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcOptimizationScheme(currentScheme = varianceGreedyScheme);
                setButtonEnableState(AnalysisState.Grouped);
            }
        });
        calcGreedy2SchemeBn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcOptimizationScheme(currentScheme = greedyScheme);
                setButtonEnableState(AnalysisState.Grouped);
            }
        });
        calcSimilartySchemeBn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcOptimizationScheme(currentScheme = simBasedScheme);
                setButtonEnableState(AnalysisState.Grouped);
            }
        });

        trainNetworkBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {                
                WorkerDlg progress = new WorkerDlg(GUIState.getMainWindow(), "Calculating Sensitivity Indicies");
                tsa.deleteObservers();
                tsa.addObserver(new ObserverWorkerDlg(progress));

                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            tsa.calculate();
                            //double quality[] = tsa.getCVError(4);
                            //weightChart.update(tsa.calculate(), quality, p, obs, getEnableList(), getShowList(), standardColorList);
                            weightChart.update(tsa.calculate(), null, p, obs, getEnableList(), getShowList(), standardColorList);
                            setButtonEnableState(AnalysisState.Trained);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println(e);
                        }
                    }
                });
                progress.execute();
            }
        });

        loadNetworkBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jfc.showOpenDialog(TemporalSAAnalysisANN.this.mainPanel) == jfc.APPROVE_OPTION){
                    File f = jfc.getSelectedFile();
                    tsa.loadNetworkState(f);
                    for (ActionListener l : trainNetworkBn.getListeners(ActionListener.class)){
                        l.actionPerformed(null);
                    }
                }
            }
        });

        saveNetworkBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (jfc.showSaveDialog(TemporalSAAnalysisANN.this.mainPanel) == jfc.APPROVE_OPTION){
                    File f = jfc.getSelectedFile();
                    tsa.saveNetworkState(f);
                }
            }
        });

        exportSchemeBn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                exportOptimizationScheme(currentScheme);
            }
        });

        groupConfigurator.addUpdateListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GroupConfigurator conf = (GroupConfigurator)e.getSource();
                if (e.getActionCommand().equals("update")){
                    currentScheme.setSolutionGroups(conf.getGroupConfiguration());
                    int Tdom = currentScheme.getDominatedTimeSteps(conf.getSelectedGroup()).length;
                    int Ttotal = TemporalSAAnalysisANN.this.ts.getTimesteps();
                    groupConfigurator.setDominanceInfo((double)Tdom/(double)Ttotal);
                }else{
                    int k = conf.getGroupConfiguration().size();
                    TemporalSAAnalysisANN.this.hydrographChart.clearTimeFilter();
                    //int index = conf.getSelectedGroup();
                    for (int j = 0; j < k; j++) {
                        int[] dominatedTimeSteps = currentScheme.dominatedTimeStepsForGroup.get(j);
                        Date dates[] = new Date[dominatedTimeSteps.length];

                        for (int i = 0; i < dominatedTimeSteps.length; i++) {
                            dates[i] = TemporalSAAnalysisANN.this.obs.getTime(dominatedTimeSteps[i]);
                        }

                        TimeFilter f = TimeFilterFactory.getSelectiveTimeFilter(dates);
                        TemporalSAAnalysisANN.this.hydrographChart.addTimeFilter(f);
                    }
                    int Tdom = currentScheme.getDominatedTimeSteps(conf.getSelectedGroup()).length;
                    int Ttotal = TemporalSAAnalysisANN.this.ts.getTimesteps();
                    groupConfigurator.setDominanceInfo((double)Tdom/(double)Ttotal);
                }
            }
        });
        

        init();
        setButtonEnableState(AnalysisState.Start);
    }

    enum AnalysisState{Start, Trained, Grouped, Finish};
    private void setButtonEnableState(AnalysisState state){
        switch(state){
            case Start:
                trainNetworkBn.setEnabled(true);
                calcGreedy1SchemeBn.setEnabled(false);
                calcGreedy2SchemeBn.setEnabled(false);
                calcOptimalSchemeBn.setEnabled(false);
                calcSimilartySchemeBn.setEnabled(false);
                saveNetworkBn.setEnabled(false);
                loadNetworkBn.setEnabled(true);
                exportSchemeBn.setEnabled(false);
                break;
            case Trained:
                trainNetworkBn.setEnabled(true);
                calcGreedy1SchemeBn.setEnabled(true);
                calcGreedy2SchemeBn.setEnabled(true);
                calcOptimalSchemeBn.setEnabled(false);
                calcSimilartySchemeBn.setEnabled(true);
                saveNetworkBn.setEnabled(true);
                loadNetworkBn.setEnabled(true);
                exportSchemeBn.setEnabled(false);
                break;
            case Grouped:
                trainNetworkBn.setEnabled(true);
                calcGreedy1SchemeBn.setEnabled(true);
                calcGreedy2SchemeBn.setEnabled(true);
                calcOptimalSchemeBn.setEnabled(false);
                calcSimilartySchemeBn.setEnabled(true);
                saveNetworkBn.setEnabled(true);
                loadNetworkBn.setEnabled(true);
                exportSchemeBn.setEnabled(true);
                break;
            case Finish:
                trainNetworkBn.setEnabled(true);
                calcGreedy1SchemeBn.setEnabled(true);
                calcGreedy2SchemeBn.setEnabled(true);
                calcOptimalSchemeBn.setEnabled(false);
                calcSimilartySchemeBn.setEnabled(true);
                saveNetworkBn.setEnabled(true);
                loadNetworkBn.setEnabled(true);
                exportSchemeBn.setEnabled(true);
                break;
        }
    }

    public class ColorRenderer extends JLabel
            implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public ColorRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object color,
                boolean isSelected, boolean hasFocus,
                int row, int column) {
            Color newColor = (Color) color;
            setBackground(newColor);
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            return this;
        }
    }

    public class ColorEditor extends AbstractCellEditor
            implements TableCellEditor,
            ActionListener {

        Color currentColor;
        JButton button;
        JColorChooser colorChooser;
        JDialog dialog;
        protected static final String EDIT = "edit";

        public ColorEditor() {
            button = new JButton();
            button.setActionCommand(EDIT);
            button.addActionListener(this);
            button.setBorderPainted(false);

            //Set up the dialog that the button brings up.
            colorChooser = new JColorChooser();
            dialog = JColorChooser.createDialog(button,
                    "Pick a Color",
                    true, //modal
                    colorChooser,
                    this, //OK button handler
                    null); //no CANCEL button handler
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (EDIT.equals(e.getActionCommand())) {
                //The user has clicked the cell, so
                //bring up the dialog.
                button.setBackground(currentColor);
                colorChooser.setColor(currentColor);
                dialog.setVisible(true);

                fireEditingStopped(); //Make the renderer reappear.

            } else { //User pressed dialog's "OK" button.
                currentColor = colorChooser.getColor();
            }
        }

        //Implement the one CellEditor method that AbstractCellEditor doesn't.
        @Override
        public Object getCellEditorValue() {
            return currentColor;
        }

        //Implement the one method defined by TableCellEditor.
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            currentColor = (Color) value;
            return button;
        }
    }

    class ParameterTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{"show", "name", "enabled", "color"};
        private Object[][] data = null;

        ParameterTableModel(SimpleEnsemble p[]) {
            data = new Object[p.length][3];
            for (int i = 0; i < p.length; i++) {
                data[i][0] = Boolean.TRUE;
                data[i][1] = p[i];
                data[i][2] = Boolean.TRUE;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (data != null) {
                return data.length;
            } else {
                return 0;
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (col == 3) {
                return TemporalSAAnalysisANN.this.standardColorList[row];
            } else {
                return data[row][col];
            }
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return Boolean.class;
                case 3:
                    return Color.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:
                    return true;
                case 1:
                    return false;
                case 2:
                    return true;
                case 3:
                    return true;
            }
            return false;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 2 && ((Boolean) value).booleanValue() == false) {
                setValueAt(Boolean.FALSE, row, 0);
            }
            if (col == 0 && ((Boolean) value).booleanValue() == true) {
                setValueAt(Boolean.TRUE, row, 2);
            }
            if (col == 3) {
                TemporalSAAnalysisANN.this.standardColorList[row] = (Color) value;
            } else {
                data[row][col] = value;
            }
            fireTableCellUpdated(row, col);
        }
    }

    private void init() {
        weightChart = new WeightChart();
        hydrographChart = new HydrographChart();

        hydrographChart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            @Override
            public void axisChanged(AxisChangeEvent e) {
                weightChart.getXYPlot().setDomainAxis(hydrographChart.getXYPlot().getDomainAxis());
            }
        });

        weightChart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            @Override
            public void axisChanged(AxisChangeEvent e) {
                hydrographChart.getXYPlot().setDomainAxis(weightChart.getXYPlot().getDomainAxis());
            }
        });

        for (int i = 0; i < MAX_WEIGHTS; i++) {
            weightRenderer[i] = new XYLineAndShapeRenderer();
            weightRenderer[i].setBaseFillPaint(this.standardColorList[i]);
            weightRenderer[i].setBaseLinesVisible(true);
            weightRenderer[i].setBaseShapesVisible(false);
            weightRenderer[i].setBaseSeriesVisible(true);
            weightRenderer[i].setDrawSeriesLineAsPath(true);
            weightRenderer[i].setStroke(new BasicStroke(1.0f));
        }

        PatchedChartPanel weightChartPanel = new PatchedChartPanel(weightChart.getChart(), true);
        
        weightChartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity e = event.getEntity();
                if (e != null && e instanceof XYItemEntity) {
                    XYItemEntity xy = (XYItemEntity) e;
                    int index = xy.getSeriesIndex();
                    int data = xy.getItem();

                    System.out.println("index:" + index);
                    System.out.println("data:" + data);

                    DataCollection dc = new DataCollection();
                    dc.addEnsemble(new EfficiencyEnsemble(ts.get(data),false));
                    for (int i = 0; i < p.length; i++) {
                        dc.addEnsemble(p[i]);
                    }
                    try {
                        DataRequestPanel d = new DataRequestPanel(new DottyPlot(), dc);
                        JFrame plotWindow = MCAT5Toolbar.getDefaultPlotWindow("test");
                        plotWindow.add(d, BorderLayout.CENTER);
                        plotWindow.setVisible(true);
                    } catch (NoDataException nde) {
                        System.out.println(nde.toString());
                    }

                    TimeSeriesCollection collection = (TimeSeriesCollection) hydrographChart.getXYPlot().getDataset(index);
                    System.out.println(collection.getSeries(0).getDataItem(data).getPeriod());
                    System.out.println(collection.getSeries(0).getDataItem(data).getValue());
                }

            }

            public void chartMouseMoved(ChartMouseEvent event) {
            }
        });

        PatchedChartPanel hydrographChartPanel = new PatchedChartPanel(hydrographChart.getChart(), true);

        JScrollPane parameterTablePane = new JScrollPane(parameterTable);
        
        JPanel sideBar = new JPanel();
        GroupLayout sideLayout = new GroupLayout(sideBar);
        sideLayout.setAutoCreateGaps(true);
        sideLayout.setAutoCreateContainerGaps(true);
        sideBar.setLayout(sideLayout);
        sideLayout.setHorizontalGroup(
                sideLayout.createParallelGroup()
                .addComponent(parameterTablePane)
                .addComponent(infoLabel)
                .addGroup(sideLayout.createParallelGroup()
                    .addGroup(sideLayout.createSequentialGroup()
                        .addComponent(step1Label)
                        .addComponent(trainNetworkBn)
                        .addComponent(loadNetworkBn)
                        .addComponent(saveNetworkBn)
                    )
                    .addGroup(sideLayout.createSequentialGroup()
                        .addComponent(step2Label)
                        .addComponent(calcGreedy1SchemeBn)
                        .addComponent(calcGreedy2SchemeBn)
                        .addComponent(calcOptimalSchemeBn)
                        .addComponent(calcSimilartySchemeBn)
                    )
                    .addGroup(sideLayout.createSequentialGroup()
                        .addComponent(step3Label)
                        .addComponent(exportSchemeBn)                        
                    )
                    .addComponent(groupConfigurator.getPanel())
                )

                .addGap(0, 10, Short.MAX_VALUE)
                );

        sideLayout.setVerticalGroup(
                sideLayout.createSequentialGroup()
                .addComponent(parameterTablePane)
                .addComponent(infoLabel)
                .addGroup(sideLayout.createSequentialGroup()
                    .addGroup(sideLayout.createParallelGroup()
                        .addComponent(step1Label)
                        .addComponent(trainNetworkBn)
                        .addComponent(loadNetworkBn)
                        .addComponent(saveNetworkBn)
                    )
                    .addGroup(sideLayout.createParallelGroup()
                        .addComponent(step2Label)
                        .addComponent(calcGreedy1SchemeBn)
                        .addComponent(calcGreedy2SchemeBn)
                        .addComponent(calcOptimalSchemeBn)
                        .addComponent(calcSimilartySchemeBn)
                    )
                    .addGroup(sideLayout.createParallelGroup()
                        .addComponent(step3Label)
                        .addComponent(exportSchemeBn)                        
                    )
                    .addComponent(groupConfigurator.getPanel())
                )
                .addGap(0, 10, Short.MAX_VALUE)
                );

        mainPanel = new JPanel();
        GroupLayout mainLayout = new GroupLayout(mainPanel);
        mainLayout.setAutoCreateGaps(true);
        mainLayout.setAutoCreateContainerGaps(true);
        mainPanel.setLayout(mainLayout);

        mainLayout.setHorizontalGroup(
                mainLayout.createSequentialGroup().addGroup(mainLayout.createParallelGroup().addComponent(weightChartPanel).addComponent(hydrographChartPanel)).addComponent(sideBar));
        mainLayout.setVerticalGroup(mainLayout.createParallelGroup().addGroup(mainLayout.createSequentialGroup().addComponent(weightChartPanel).addComponent(hydrographChartPanel)).addComponent(sideBar));

        jfc.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory())
                    return true;
                if (f.getName().endsWith("enc"))
                    return true;
                return false;
            }

            @Override
            public String getDescription() {
                return "Encog Serialized Artificial Neural Network";
            }
        });
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> data[] = getData(new int[]{0, 1, 2});
        ts = (TimeSerieEnsemble) data[0].get(0);
        obs = (Measurement) data[1].get(0);
        e = (EfficiencyEnsemble) data[2].get(0);

        Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
        p = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet) {
            p[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }

        parameterTable.setModel(new ParameterTableModel(p));
        parameterTable.setDefaultEditor(Color.class, new ColorEditor());
        parameterTable.getColumnModel().getColumn(3).setCellRenderer(new ColorRenderer(true));
        parameterTable.getModel().addTableModelListener(new TableModelListener() {

            public void tableChanged(TableModelEvent e) {
                /*ObserverWorkerDlg progress = new ObserverWorkerDlg(null, "Updating plot");
                temporalAnalysis.addObserver(progress);
                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    public void run() {
                        //weightChart.update(temporalAnalysis.calculate(), p, obs, getEnableList(), getShowList(), standardColorList);
                    }
                });
                progress.execute();*/
            }
        });
        tsa = new SobolsMethodTemporal(p, e, ts, obs);
        groupConfigurator.setData(p, p.length);
        this.hydrographChart.setHydrograph(obs);

    }

    private boolean[] getEnableList() {
        ParameterTableModel model = (ParameterTableModel) this.parameterTable.getModel();

        boolean parameterActive[] = new boolean[model.getRowCount()];
        for (int i = 0; i < parameterActive.length; i++) {
            if (((Boolean) model.getValueAt(i, 2)) == true) {
                parameterActive[i] = true;
            } else {
                parameterActive[i] = false;
            }
        }
        return parameterActive;
    }

    private boolean[] getShowList() {
        ParameterTableModel model = (ParameterTableModel) this.parameterTable.getModel();

        boolean parameterActive[] = new boolean[model.getRowCount()];
        for (int i = 0; i < parameterActive.length; i++) {
            if (((Boolean) model.getValueAt(i, 0)) == true) {
                parameterActive[i] = true;
            } else {
                parameterActive[i] = false;
            }
        }
        return parameterActive;
    }

    private void calcOptimizationScheme(final OptimizationScheme scheme) {
        if (scheme instanceof VarianceBasedGreedyOptimizationScheme)
            ((VarianceBasedGreedyOptimizationScheme)scheme).setData(tsa, p, e, obs);
        else
            scheme.setData(tsa.calculate(), p, e, obs);
                        
        WorkerDlg progress = new WorkerDlg(GUIState.getMainWindow(), "Calculating Optimization Scheme");
        scheme.addObserver(new ObserverWorkerDlg(progress));

        progress.setInderminate(true);
        progress.setTask(new Runnable() {

            @Override
            public void run() {
                try {
//                    scheme.calcOptimizationScheme();                    
                    synchronized (groupConfigurator) {
                        groupConfigurator.setSolutionGroup(scheme.getSolutionGroups());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                }
            }
        });
        progress.execute();
    }

    private void exportOptimizationScheme(OptimizationScheme scheme) {
        //OptimizationDescriptionDocument document = scheme.getOptimizationDocument();

        JFileChooser chooser = GUIHelper.getJFileChooser();
        chooser.setFileFilter(new FileFilter() {

            public String getDescription() {
                return "optimization scheme";
            }

            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.getAbsolutePath().endsWith("xml")) {
                    return true;
                }
                return false;
            }
        });
        try {
            BeanInfo info = Introspector.getBeanInfo(JAMSCalendar.class);
            PropertyDescriptor[] propertyDescriptors =
                    info.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length; ++i) {
                PropertyDescriptor pd = propertyDescriptors[i];
                if (!pd.getName().equals("milliSeconds")
                        && !pd.getName().equals("dateFormat")) {
                    pd.setValue("transient", Boolean.TRUE);
                }
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        int result = chooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                XMLEncoder encoder = new XMLEncoder(
                        new BufferedOutputStream(
                        new FileOutputStream(chooser.getSelectedFile())));
                //encoder.writeObject(document);
                encoder.close();
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(null, "Optimization scheme was not saved!\n" + ioe.toString());
                return;
            }
        }
    }
}
