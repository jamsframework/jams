/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro;

import jams.data.JAMSCalendar;
import jams.gui.tools.GUIHelper;
import jams.workspace.stores.J2KTSDataStore;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import optas.gui.MCAT5.DataRequestPanel;
import optas.gui.MCAT5.MCAT5Plot.NoDataException;
import optas.gui.MCAT5.MCAT5Toolbar;
import optas.hydro.calculations.SlopeCalculations;

import optas.data.DataCollection;
import optas.data.Efficiency;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.hydro.gui.GllobalSensitivityEfficiencyComparison;
import optas.gui.wizard.HydrographChart;
import optas.hydro.gui.SelectionDialog;
import optas.hydro.gui.SimpleGlobalSensitivityAtPoint;
import optas.hydro.gui.WeightChart;
import optas.io.TSDataReader;
import optas.tools.PatchedChartPanel;

/**
 *
 * @author chris
 */
@Deprecated
public class HydroAnalysisFrame extends JFrame {

    final int MAX_WEIGHTS = 9;
    final Color weightColor[] = new Color[]{
        Color.blue, Color.black, Color.CYAN, Color.DARK_GRAY,
        Color.GREEN, Color.MAGENTA, Color.PINK, Color.ORANGE,
        Color.YELLOW
    };
    HydrographChart chart;
    WeightChart weightChart;
    JFreeChart dominantParameterChart;
    J2KTSDataStore store;
    JSlider peakSlider = new JSlider();
    JTextField peakNumberField = new JTextField(4);
    JSlider recessionSlider = new JSlider();
    JTextField recessionNumberField = new JTextField(4);
    JSlider groundwaterSlider = new JSlider();
    JTextField groundwaterThresholdField = new JTextField(4);
    JButton loadEnsemble = new JButton("Load Ensemble");
    JComboBox obsDatasets = new JComboBox();
    JComboBox simDatasets = new JComboBox();
    JComboBox effDatasets = new JComboBox();
    JComboBox paramDatasets = new JComboBox();
    JComboBox optimizationSchemes = new JComboBox();
    JList parameterGroups = new JList();
    JTextArea optimizationSchemeDesc = new JTextArea(15, 30);
    double[][] weights = null;
    String parameterIDs[] = null;
    DataCollection ensemble;
    XYLineAndShapeRenderer weightRenderer[] = new XYLineAndShapeRenderer[MAX_WEIGHTS];
    OptimizationScheme scheme[] = new OptimizationScheme[3];
    JFileChooser j2kFile_chooser = GUIHelper.getJFileChooser();
    JMenuItem startCalibrationWizard = new JMenuItem("Start Calibration Wizard");
    JMenuItem exportOptimizationScheme = new JMenuItem("Export");
    JMenuItem openEnsemble = new JMenuItem("open ensemble");
    JMenuItem openObservation = new JMenuItem("open hydrograph");

    private void calcOptimationScheme(int index) {
        String obsItem = (String) obsDatasets.getSelectedItem();
        String effItem = (String) effDatasets.getSelectedItem();

        if (weights == null) {
            calcWeights();
        }/*
        switch (index) {
            case 0: {
                scheme[index] = new OptimalOptimizationScheme(weights, SlopeCalculations.getParameterEnsembles(ensemble),
                        (SimpleEnsemble) HydroAnalysisFrame.this.ensemble.getDataSet(effItem),
                        (TimeSerie) HydroAnalysisFrame.this.ensemble.getDataSet(obsItem));
                optimizationSchemes.removeItem(scheme[index]);
                optimizationSchemes.addItem(scheme[index]);
                break;
            }
            case 1: {
                scheme[index] = new GreedyOptimizationScheme(weights, SlopeCalculations.getParameterEnsembles(ensemble),
                        (SimpleEnsemble) HydroAnalysisFrame.this.ensemble.getDataSet(effItem),
                        (TimeSerie) HydroAnalysisFrame.this.ensemble.getDataSet(obsItem));
                optimizationSchemes.removeItem(scheme[index]);
                optimizationSchemes.addItem(scheme[index]);
                break;
            }
            case 2: {
                scheme[index] = new SimilarityBasedOptimizationScheme(weights, SlopeCalculations.getParameterEnsembles(ensemble),
                        (SimpleEnsemble) HydroAnalysisFrame.this.ensemble.getDataSet(effItem),
                        (TimeSerie) HydroAnalysisFrame.this.ensemble.getDataSet(obsItem));
                optimizationSchemes.removeItem(scheme[index]);
                optimizationSchemes.addItem(scheme[index]);

                break;
            }
        }*/
//        scheme[index].calcOptimizationScheme();
        ((DefaultListModel) parameterGroups.getModel()).clear();
        int counter = 0;
        for (ParameterGroup p : scheme[index].solutionGroups) {
            ((DefaultListModel) parameterGroups.getModel()).addElement("Group" + counter++);
        }
        parameterGroups.clearSelection();
        optimizationSchemeDesc.setText("");
    }

    public HydroAnalysisFrame() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        j2kFile_chooser.setFileFilter(new FileFilter() {

            public String getDescription() {
                return "j2k data";
            }

            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }
                if (file.getAbsolutePath().endsWith("dat")) {
                    return true;
                }
                return false;
            }
        });

        this.setTitle("Optas - Hydro");
        setSize(new Dimension(1400, 1000));

        JMenu fileMenu = new JMenu("File");
        JMenu calculationMenu = new JMenu("Calculations");


        JMenuItem exit = new JMenuItem("exit");

        JMenu calcCalibrationSchema = new JMenu("Calculate Calibration Schema");
        JMenuItem calcGreedy = new JMenuItem("Greedy");
        calcGreedy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                calcOptimationScheme(1);
            }
        });
        JMenuItem calcOptimal = new JMenuItem("Optimal");
        calcOptimal.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                calcOptimationScheme(0);
            }
        });
        JMenuItem calcSimilarity = new JMenuItem("Similarity");
        calcSimilarity.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                calcOptimationScheme(2);
            }
        });
        calcCalibrationSchema.add(calcGreedy);
        calcCalibrationSchema.add(calcOptimal);
        calcCalibrationSchema.add(calcSimilarity);

        calculationMenu.add(calcCalibrationSchema);

        fileMenu.add(openEnsemble);
        fileMenu.add(openObservation);
        fileMenu.add(startCalibrationWizard);
        fileMenu.add(exportOptimizationScheme);

        fileMenu.add(exit);

        exit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                HydroAnalysisFrame.this.setVisible(false);
            }
        });

        openObservation.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int result = j2kFile_chooser.showOpenDialog(HydroAnalysisFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    TSDataReader reader = null;
                
                    try{
                        reader = new TSDataReader(j2kFile_chooser.getSelectedFile());
                    }catch(IOException ioe){
                        JOptionPane.showMessageDialog(null, ioe.toString());
                        ioe.printStackTrace();
                        return;
                    }
                    ArrayList<Object> attr = reader.getNames();
                    SelectionDialog selectionDialog = new SelectionDialog(null, new TreeSet<Object>(attr));

                    String selectedName = (String) selectionDialog.getSelection();
                    int index = 0;
                    for (index = 0; index < attr.size(); index++) {
                        if (attr.get(index).equals(selectedName)) {
                            break;
                        }
                    }
                    //TODO
                    //chart.setHydrograph(reader.getData(index));
                }
            }
        });

        openEnsemble.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = GUIHelper.getJFileChooser();
                chooser.setFileFilter(new FileFilter() {

                    public String getDescription() {
                        return "data collection";
                    }

                    public boolean accept(File file) {
                        if (file.isDirectory()) {
                            return true;
                        }
                        if (file.getAbsolutePath().endsWith("cdat")) {
                            return true;
                        }
                        return false;
                    }
                });
                int result = chooser.showOpenDialog(HydroAnalysisFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    HydroAnalysisFrame.this.ensemble = DataCollection.createFromFile(chooser.getSelectedFile());
                    Set<String> obsDatasets = ensemble.getDatasets(Measurement.class);
                    Set<String> paramDatasets = ensemble.getDatasets(Parameter.class);
                    Set<String> effDatasets = ensemble.getDatasets(Efficiency.class);
                    Set<String> simDatasets = ensemble.getDatasets(TimeSerie.class);
                    HydroAnalysisFrame.this.obsDatasets.removeAll();
                    HydroAnalysisFrame.this.paramDatasets.removeAll();
                    HydroAnalysisFrame.this.effDatasets.removeAll();
                    HydroAnalysisFrame.this.simDatasets.removeAll();
                    for (String s : obsDatasets) {
                        HydroAnalysisFrame.this.obsDatasets.addItem(s);
                    }
                    for (String s : paramDatasets) {
                        HydroAnalysisFrame.this.paramDatasets.addItem(s);
                    }
                    for (String s : effDatasets) {
                        HydroAnalysisFrame.this.effDatasets.addItem(s);
                    }
                    for (String s : simDatasets) {
                        HydroAnalysisFrame.this.simDatasets.addItem(s);
                    }
                    if (!obsDatasets.isEmpty()) {
                        Measurement measurement = (Measurement) HydroAnalysisFrame.this.ensemble.getDataSet(obsDatasets.iterator().next());
                        chart.setHydrograph(measurement);
                    }
                    optimizationSchemes.removeAllItems();
                    JOptionPane.showMessageDialog(HydroAnalysisFrame.this, "Ensemble loaded successfully!");
                }
                for (int i = 0; i < scheme.length; i++) {
                    scheme[i] = null;
                }
                weights = null;
                ((DefaultListModel) parameterGroups.getModel()).clear();
                optimizationSchemeDesc.setText("");

            }
        });

        parameterGroups.setModel(new DefaultListModel());

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(calculationMenu);
        this.setJMenuBar(menuBar);

        init();

        invalidate();
    }

    private void calcWeights() {
        int n = 0;

        String simItem = (String) simDatasets.getSelectedItem();
        String obsItem = (String) obsDatasets.getSelectedItem();

        TimeSerieEnsemble tsEnsemble = HydroAnalysisFrame.this.ensemble.getTimeserieEnsemble(simItem);
        TimeSerie obsTS = (TimeSerie) HydroAnalysisFrame.this.ensemble.getDataSet(obsItem);

        weights = SlopeCalculations.calcParameterSensitivityTimeserie(HydroAnalysisFrame.this.ensemble, tsEnsemble, obsTS, 0.33);
        n = weights.length;

        TimeSeriesCollection collections[] = new TimeSeriesCollection[n];
        TimeSeries timeseries[] = new TimeSeries[n];
        SimpleEnsemble p[] = SlopeCalculations.getParameterEnsembles(ensemble);

        ArrayList<int[]> dominantParameters = OptimizationScheme.calcDominantParameters(weights, 0.8);

        for (int j = 0; j < n; j++) {
            collections[j] = new TimeSeriesCollection();
            timeseries[j] = new TimeSeries(p[j].getName());
            timeseries[j].setMaximumItemCount(dominantParameters.size());
            collections[j].addSeries(timeseries[j]);
        }

        parameterIDs = new String[n];
        for (int j = 0; j < n; j++) {
            dominantParameterChart.getXYPlot().setDataset(j, collections[j]);
            dominantParameterChart.getXYPlot().setRenderer(j, HydroAnalysisFrame.this.weightRenderer[0]);
            parameterIDs[j] = p[j].getName();
        }

        //this is veeery sloooow .. find workaround!!
        for (int i = 0; i < dominantParameters.size(); i++) {
            int list[] = dominantParameters.get(i);
            Arrays.sort(list);
            int c = 0;
            Day di = new Day(obsTS.getTime(i));

            for (int j = 0; j < n; j++) {
                if (list[c] < j) {
                    //            timeseries[j].add(di, Double.NaN);
                } else if (list[c] == j) {
                    //          timeseries[j].add(di, j);
                    if (c < list.length - 1) {
                        c++;
                    }
                } else {
                    //        timeseries[j].add(di, Double.NaN);
                }
            }
        }

        SymbolAxis symAxis = new SymbolAxis("parameter", parameterIDs);
        symAxis.setRange(-1, n + 1);
        symAxis.setTickUnit(new NumberTickUnit(1.0));
        dominantParameterChart.getXYPlot().setRangeAxis(symAxis);
        dominantParameterChart.removeLegend();

        //this.weightChart.update(weights, obsTS, ensemble);
    }

    private void init() {
        weightChart = new WeightChart();
        chart = new HydrographChart();

        chart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            public void axisChanged(AxisChangeEvent e) {
                weightChart.getXYPlot().setDomainAxis(chart.getXYPlot().getDomainAxis());
                dominantParameterChart.getXYPlot().setDomainAxis(chart.getXYPlot().getDomainAxis());
            }
        });

        weightChart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            public void axisChanged(AxisChangeEvent e) {
                chart.getXYPlot().setDomainAxis(weightChart.getXYPlot().getDomainAxis());
                dominantParameterChart.getXYPlot().setDomainAxis(weightChart.getXYPlot().getDomainAxis());
            }
        });

        this.dominantParameterChart = ChartFactory.createTimeSeriesChart(
                "Dominant Parameters",
                "time",
                "dominant parameters",
                null,
                true,
                true,
                false);

        dominantParameterChart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            public void axisChanged(AxisChangeEvent e) {
                chart.getXYPlot().setDomainAxis(dominantParameterChart.getXYPlot().getDomainAxis());
                weightChart.getXYPlot().setDomainAxis(dominantParameterChart.getXYPlot().getDomainAxis());
            }
        });

        for (int i = 0; i < MAX_WEIGHTS; i++) {
            weightRenderer[i] = new XYLineAndShapeRenderer();
            weightRenderer[i].setBaseFillPaint(weightColor[i]);
            weightRenderer[i].setBaseLinesVisible(true);
            weightRenderer[i].setBaseShapesVisible(false);
            weightRenderer[i].setBaseSeriesVisible(true);
            weightRenderer[i].setDrawSeriesLineAsPath(true);
            weightRenderer[i].setStroke(new BasicStroke(1.0f));
        }



        PatchedChartPanel weightChartPanel = new PatchedChartPanel(weightChart.getChart(), true);

        PatchedChartPanel chartPanel = new PatchedChartPanel(chart.getChart(), true);
        chartPanel.addChartMouseListener(new ChartMouseListener() {

            public void chartMouseClicked(ChartMouseEvent event) {
                ChartEntity e = event.getEntity();
                if (e != null && e instanceof XYItemEntity) {
                    XYItemEntity xy = (XYItemEntity) e;
                    int index = xy.getSeriesIndex();
                    int data = xy.getItem();

                    System.out.println("index:" + index);
                    System.out.println("data:" + data);

                    SimpleGlobalSensitivityAtPoint sgsat = new SimpleGlobalSensitivityAtPoint(data);
                    try {
                        DataRequestPanel d = new DataRequestPanel(sgsat, HydroAnalysisFrame.this.ensemble);
                        JFrame plotWindow = MCAT5Toolbar.getDefaultPlotWindow("test");
                        plotWindow.add(d, BorderLayout.CENTER);
                        plotWindow.setVisible(true);
                    } catch (NoDataException nde) {
                        System.out.println(nde.toString());
                    }

                    TimeSeriesCollection collection = (TimeSeriesCollection) chart.getXYPlot().getDataset(index);
                    System.out.println(collection.getSeries(0).getDataItem(data).getPeriod());
                    System.out.println(collection.getSeries(0).getDataItem(data).getValue());
                }

            }

            public void chartMouseMoved(ChartMouseEvent event) {
            }
        });

        PatchedChartPanel dominantParametersChartPanel = new PatchedChartPanel(dominantParameterChart, true);


        /*JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(chartPanel, BorderLayout.NORTH);
        westPanel.add(weightChartPanel, BorderLayout.CENTER);*/

        JPanel eastPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;

        JCheckBox showPeaks = new JCheckBox();
        showPeaks.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
//                peakSlider.setMaximum(chart.getPeakCount());
//                chart.showPeaks(src.isSelected());
            }
        });
        JPanel peakPanel = new JPanel();
        peakPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Peaks"));
        peakPanel.add(showPeaks);
        //peakPanel.add(peakSlider);
        peakSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                peakNumberField.setText(Integer.toString(peakSlider.getValue()));
//                chart.setVisiblePeaks(peakSlider.getValue());
            }
        });
        peakNumberField.setEditable(false);
        peakPanel.add(peakNumberField);

        JCheckBox showRecessions = new JCheckBox();
        showRecessions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
//                recessionSlider.setMaximum(chart.getRecessionCount());
  //              chart.showRecessionCurve(src.isSelected());
            }
        });
        JPanel recessionPanel = new JPanel();
        recessionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "recessions"));
        recessionPanel.add(showRecessions);
        recessionPanel.add(recessionSlider);
        recessionSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                recessionNumberField.setText(Integer.toString(recessionSlider.getValue()));
//                chart.setVisibleRecessions(recessionSlider.getValue());
            }
        });
        recessionNumberField.setEditable(false);
        recessionPanel.add(recessionNumberField);

        JCheckBox showGroundwater = new JCheckBox();
        showGroundwater.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
   //             chart.showGroundwaterCurve(src.isSelected());
            }
        });
        JPanel groundwaterPanel = new JPanel();
        groundwaterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "groundwater"));
        groundwaterPanel.add(showGroundwater);

        JCheckBox showBaseFlowPeriods = new JCheckBox();
        showBaseFlowPeriods.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
     //           chart.showBaseFlowPeriods(src.isSelected());
            }
        });

        JPanel baseFlowPanel = new JPanel();
        baseFlowPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "baseflow periods"));
        baseFlowPanel.add(showBaseFlowPeriods);
        baseFlowPanel.add(groundwaterSlider);
        groundwaterSlider.setMinimum(0);
        groundwaterSlider.setMaximum(1000);
        groundwaterSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                groundwaterThresholdField.setText(Double.toString((groundwaterSlider.getValue() / 1000.0) * 5.0));
//                double groundwaterThreshold = (groundwaterSlider.getValue() / 1000.0) * 5.0;
  //              chart.setGroundwaterThreshold(groundwaterThreshold);
            }
        });
        groundwaterThresholdField.setEditable(false);
        baseFlowPanel.add(groundwaterThresholdField);

        eastPanel.add(peakPanel, c);

        c.gridx = 1;
        c.gridy = 0;
        eastPanel.add(recessionPanel, c);
        c.gridx = 2;
        c.gridy = 0;
        eastPanel.add(groundwaterPanel, c);

        c.gridx = 0;
        c.gridy = 1;
        eastPanel.add(baseFlowPanel, c);

        /*c.gridx = 0;
        c.gridy = 4;
        eastPanel.add(new JButton("Export Selections"), c);*/

        obsDatasets.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    Measurement measurement = (Measurement) HydroAnalysisFrame.this.ensemble.getDataSet((String) e.getItem());
                    chart.setHydrograph(measurement);
                }
            }
        });

        JButton efficiencySensitivityComparison = new JButton("Compare Efficiency with Sensitivity");
        efficiencySensitivityComparison.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GllobalSensitivityEfficiencyComparison comparingPlot = new GllobalSensitivityEfficiencyComparison();
                try {
                    DataRequestPanel d = new DataRequestPanel(comparingPlot, ensemble);
                    JFrame plotWindow = MCAT5Toolbar.getDefaultPlotWindow("test");
                    plotWindow.add(d, BorderLayout.CENTER);
                    plotWindow.setVisible(true);
                } catch (NoDataException nde) {
                    System.out.println(nde.toString());
                }
            }
        });

        parameterGroups.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int index = ((JList) e.getSource()).getSelectedIndex();
                if (index==-1)
                    return;
                Object schemeItem = optimizationSchemes.getSelectedItem();
                if (schemeItem==null)
                    return;
                OptimizationScheme o = (OptimizationScheme)schemeItem;
                String text = o.solutionGroups.get(index).toString();

                optimizationSchemeDesc.setText(text);
                                              
//                HydroAnalysisFrame.this.chart.setMarkSerie(o.dominatedTimeStepsForGroup.get(index));
                
            }
        });

        optimizationSchemes.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.DESELECTED)
                    return;
                OptimizationScheme myScheme = (OptimizationScheme) optimizationSchemes.getSelectedItem();
                ((DefaultListModel) parameterGroups.getModel()).clear();
                if (myScheme == null) {
                    JOptionPane.showMessageDialog(HydroAnalysisFrame.this, "First select a method and start calculation!");
                    return;
                }

                int counter = 0;
                for (ParameterGroup p : myScheme.solutionGroups) {
                    ((DefaultListModel) parameterGroups.getModel()).addElement("Group" + counter++);
                }
                parameterGroups.clearSelection();
                optimizationSchemeDesc.setText("");
            }
        });

        startCalibrationWizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                OptimizationScheme myScheme = (OptimizationScheme) optimizationSchemes.getSelectedItem();
                if (myScheme == null) {
                    JOptionPane.showMessageDialog(HydroAnalysisFrame.this, "First select a method and start calculation!");
                    return;
                }

//                optas.gui.OptimizationWizard.createFrame((Document)null, null, myScheme.getOptimizationDocument());
            }
        });

        exportOptimizationScheme.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                OptimizationScheme myScheme = (OptimizationScheme) optimizationSchemes.getSelectedItem();
                if (myScheme == null) {
                    JOptionPane.showMessageDialog(HydroAnalysisFrame.this, "First select a method and start calculation!");
                    return;
                }

//                OptimizationDescriptionDocument document = myScheme.getOptimizationDocument();

                JFileChooser chooser = GUIHelper.getJFileChooser();
                chooser.setFileFilter(new FileFilter() {

                    public String getDescription() {
                        return "data collection";
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
                int result = chooser.showSaveDialog(HydroAnalysisFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        XMLEncoder encoder = new XMLEncoder(
                                new BufferedOutputStream(
                                new FileOutputStream(chooser.getSelectedFile())));
                        //encoder.writeObject(document);
                        encoder.close();
                    } catch (IOException ioe) {
                        System.out.println("Could not save scheme:" + ioe.toString());
                        return;
                    }
                }
            }
        });

        JPanel subPanel = new JPanel(new FlowLayout());
        //subPanel.add(loadEnsemble);
        //subPanel.add(exportOptimizationScheme);
        subPanel.add(efficiencySensitivityComparison);


        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 4;
        c.anchor = c.WEST;
        eastPanel.add(subPanel, c);

        JPanel subPanel2 = new JPanel(new FlowLayout());
        subPanel2.add(obsDatasets);
        subPanel2.add(effDatasets);
        subPanel2.add(paramDatasets);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.anchor = c.WEST;
        eastPanel.add(subPanel2, c);

        //schemePanel
        JScrollPane optimizationSchemePane = new JScrollPane(optimizationSchemeDesc);
        JPanel schemePanel = new JPanel(new GridBagLayout());

        GridBagConstraints con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 1;
        con.fill = con.BOTH;
        schemePanel.add(optimizationSchemes, con);
        con.gridx = 0;
        con.gridy = 2;
        con.gridwidth = 1;
        con.gridheight = 10;
        schemePanel.add(parameterGroups, con);
        con.gridx = 1;
        con.gridy = 0;
        con.gridwidth = 1;
        con.gridheight = 12;
        schemePanel.add(optimizationSchemePane, con);

        c.gridx = 0;
        c.gridy = 5;
        c.gridheight = 6;
        c.gridwidth = 4;
        eastPanel.add(schemePanel, c);


        JPanel mainPanel = new JPanel(new GridLayout(2, 2));
        mainPanel.add(chartPanel, 0, 0);
        mainPanel.add(weightChartPanel, 0, 1);
        mainPanel.add(dominantParametersChartPanel, 1, 0);
        mainPanel.add(eastPanel, 1, 1);

        mainPanel.updateUI();
        this.add(mainPanel);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception evt) {
        }

        HydroAnalysisFrame frame = new HydroAnalysisFrame();
        frame.setVisible(true);
    }
}
