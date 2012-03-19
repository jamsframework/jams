/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.hydro;

import jams.gui.tools.GUIHelper;
import jams.workspace.stores.J2KTSDataStore;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.CategoryTableXYDataset;
import reg.gui.MCAT5.DataRequestPanel;
import reg.gui.MCAT5.DataRequestPanel.NoDataException;
import reg.hydro.gui.GllobalSensitivityEfficiencyComparison;
import reg.gui.MCAT5.MCAT5Toolbar;
import reg.hydro.calculations.SlopeCalculations;

import reg.hydro.gui.SimpleGlobalSensitivityAtPoint;
import reg.hydro.data.DataCollection;
import reg.hydro.data.DataSet.MismatchException;
import reg.hydro.data.Efficiency;
import reg.hydro.data.Measurement;
import reg.hydro.data.Parameter;
import reg.hydro.data.SimpleEnsemble;
import reg.hydro.data.TimeSerie;
import reg.hydro.data.TimeSerieEnsemble;

/**
 *
 * @author chris
 */
public class HydroAnalysisPanel extends JPanel {

    final int MAX_WEIGHTS = 9;
    final Color weightColor[] = new Color[]{
        Color.blue, Color.black, Color.CYAN, Color.DARK_GRAY,
        Color.GREEN, Color.MAGENTA, Color.PINK, Color.ORANGE,
        Color.YELLOW
    };
    final Dimension windowSize = new Dimension(800, 400);
    TimeSerie hydrograph;
    TimeSerie groundwater;
    JFrame owner;
    JFreeChart chart;
    JFreeChart weightChart;
    JFreeChart dominantParameterChart;
    TimeSeriesCollection dataset1, datasetPeaks, datasetRecessionCurves, datasetGroundwater, datasetBaseFlow;
    J2KTSDataStore store;
    ArrayList<Peak> peaks = null;
    ArrayList<RecessionCurve> recessionCurves = null;
    ArrayList<HydrographSection> groundwaterSections = null;
    JSlider peakSlider = new JSlider();
    JTextField peakNumberField = new JTextField(4);
    int peakCount = 0;
    JSlider recessionSlider = new JSlider();
    JTextField recessionNumberField = new JTextField(4);
    int recessionCount = 0;
    JSlider groundwaterSlider = new JSlider();
    JTextField groundwaterThresholdField = new JTextField(4);
    double groundwaterThreshold = 0;
    JButton loadEnsemble = new JButton("Load Ensemble");
    JComboBox obsDatasets = new JComboBox();
    JComboBox simDatasets = new JComboBox();
    JComboBox effDatasets = new JComboBox();
    JComboBox paramDatasets = new JComboBox();
    JComboBox optimizationSchemes = new JComboBox(new String[]{"Optimal", "Greedy", "Similarity"});
    JList parameterGroups = new JList();
    JTextArea optimizationSchemeDesc = new JTextArea(15, 30);
    double[][] weights = null;
    String parameterIDs[] = null;
    DataCollection ensemble;
    XYLineAndShapeRenderer weightRenderer[] = new XYLineAndShapeRenderer[MAX_WEIGHTS];
    StackedXYBarRenderer weightBarRenderer = new StackedXYBarRenderer(0.33);
    OptimizationScheme scheme[] = new OptimizationScheme[3];

    class HydrographSection {

        int startIndex;
        int endIndex;
        ArrayList<Double> value;

        HydrographSection(int startIndex, double value) {
            this.startIndex = startIndex;
            this.value = new ArrayList<Double>();
            this.value.add(new Double(value));
            this.endIndex = startIndex + 1;
        }

        public void add(double value) {
            this.value.add(new Double(value));
            endIndex++;
        }

        protected int getIntervalLength() {
            return endIndex - startIndex;
        }

        public double at(int index) {
            return this.value.get(index - startIndex);
        }
    }

    class RecessionCurve extends HydrographSection implements Comparable {

        RecessionCurve(int startIndex, double value) {
            super(startIndex, value);
        }

        private double getAmount() {
            return value.get(0) - value.get(endIndex - startIndex - 1);
        }

        @Override
        public int compareTo(Object obj) {
            if (!(obj instanceof RecessionCurve)) {
                return 0;
            }
            RecessionCurve r = (RecessionCurve) obj;
            if (r.getIntervalLength() < this.getIntervalLength()) {
                return -1;
            } else if (r.getIntervalLength() > this.getIntervalLength()) {
                return 1;
            } else {
                if (r.getAmount() < this.getAmount()) {
                    return -1;
                } else if (r.getAmount() < this.getAmount()) {
                    return 1;
                } else {
                    if (r.startIndex < this.startIndex) {
                        return -1;
                    } else if (r.startIndex < this.startIndex) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }
    }

    class Peak implements Comparable {

        int index;
        double value;

        public Peak(int index, double value) {
            this.index = index;
            this.value = value;
        }

        public int compareTo(Object obj) {
            if (obj instanceof Peak) {
                Peak p2 = (Peak) obj;
                if (value < p2.value) {
                    return 1;
                } else if (value > p2.value) {
                    return -1;
                } else if (index < p2.index) {
                    return 1;
                } else if (index > p2.index) {
                    return -1;
                } else {
                    return 0;
                }
            }
            return 0;
        }
    }

    private ArrayList<Peak> findPeaks() {
        TreeSet<Peak> peakList = new TreeSet<Peak>();

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();
        for (int i = 1; i < (int) n - 1; i++) {
            double v1 = hydrograph.getValue(i - 1);
            double v2 = hydrograph.getValue(i);
            double v3 = hydrograph.getValue(i + 1);
            if (v1 < v2 && v3 < v2) {
                peakList.add(new Peak(i, v2));
            }
        }

        return new ArrayList<Peak>(peakList);
    }

    private ArrayList<RecessionCurve> findRecessionCurves() {
        TreeSet<RecessionCurve> curveList = new TreeSet<RecessionCurve>();

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();

        double filterResponse[] = new double[(int) n];

        int windowSize = 5;
        double weights[] = {0.15, 0.15, 0.4, 0.15, 0.15};

        for (int i = 2; i < n - 2; i++) {
            int counter = 0;
            for (int j = -(windowSize - 1) / 2; j < (windowSize / 2); j++) {
                filterResponse[i] += weights[counter] * hydrograph.getValue(i + j);
                counter++;
            }
        }

        int i = 2;
        while (i < n - 2) {
            double v1 = filterResponse[i];
            double v2 = filterResponse[++i];
            if (v2 <= v1) {
                RecessionCurve r = new RecessionCurve(i - 1, hydrograph.getValue(i - 1));
                r.add(hydrograph.getValue(i));
                double v3 = filterResponse[++i];
                while (v3 <= v2 && i < n - 1) {
                    r.add(hydrograph.getValue(i));
                    v2 = v3;
                    v3 = filterResponse[++i];
                }
                curveList.add(r);
            }
        }
        return new ArrayList<RecessionCurve>(curveList);
    }

    private double[] groundwaterWindowMethod() {
        int t = (int) hydrograph.getTimeDomain().getNumberOfTimesteps();

        double filteredSerie[] = new double[t];

        int winSize = 150;

        ArrayList<Integer> minList = new ArrayList<Integer>();

        int oldMin = -1;

        for (int c = 0; c < t; c++) {
            double winMin = Double.POSITIVE_INFINITY;
            int winArgMin = 0;
            for (int d = c; d < Math.min(c + winSize, t); d++) {
                if (hydrograph.getValue(d) < winMin) {
                    winMin = hydrograph.getValue(d);
                    winArgMin = d;
                }
            }
            if (winArgMin != oldMin) {
                minList.add(new Integer(winArgMin));
                oldMin = winArgMin;
            }
        }
        int firstIndex = minList.get(0);
        int lastIndex = minList.get(minList.size() - 1);
        double firstValue = hydrograph.getValue(firstIndex);
        double lastValue = hydrograph.getValue(lastIndex);
        for (int init = 0; init < firstIndex; init++) {
            filteredSerie[init] = firstValue;
        }
        for (int i = 0; i < minList.size() - 1; i++) {
            int index1 = minList.get(i);
            double value1 = hydrograph.getValue(index1);
            int index2 = minList.get(i + 1);
            double value2 = hydrograph.getValue(index2);

            double d = index2 - index1;
            for (int j = index1; j < index2; j++) {
                filteredSerie[j] = value1 * (index2 - j) / d + value2 * (j - index1) / d;
            }
        }
        for (int post = lastIndex; post < t; post++) {
            filteredSerie[post] = lastValue;
        }
        return filteredSerie;
    }

    private TimeSerie calculateGroundwater() {
        try {
            TimeSerie t = new TimeSerie(groundwaterWindowMethod(), hydrograph.getTimeDomain(), "groundwater", null);
            return t;
        } catch (MismatchException e) {
            System.out.println(e);
        }
        return null;
    }

    private ArrayList<HydrographSection> calculateBaseFlowPeriods() {
        ArrayList<HydrographSection> list = new ArrayList<HydrographSection>();
        int n = (int) hydrograph.getTimeDomain().getNumberOfTimesteps();

        int i = 0;
        while (i < n) {
            if (Math.abs(hydrograph.getValue(i) - groundwater.getValue(i)) < this.groundwaterThreshold) {
                HydrographSection sec = new HydrographSection(i, hydrograph.getValue(i));
                i++;
                while (i < n && Math.abs(hydrograph.getValue(i) - groundwater.getValue(i)) < this.groundwaterThreshold) {
                    sec.add(hydrograph.getValue(i));
                    i++;
                }
                list.add(sec);
            }
            i++;
        }
        return list;
    }

    public HydroAnalysisPanel(JFrame owner, J2KTSDataStore store) {
        this.owner = owner;

        parameterGroups.setModel(new DefaultListModel());
        ArrayList<Object> attr = store.getDataSetDefinition().getAttributeValues("NAME");
        SelectionDialog selectionDialog = new SelectionDialog(this.owner, new TreeSet<Object>(attr));

        String selectedName = (String) selectionDialog.getSelection();
        int index = 0;
        for (index = 0; index < attr.size(); index++) {
            if (attr.get(index).equals(selectedName)) {
                break;
            }
        }
        this.hydrograph = TimeSerie.createFromJ2KTSDataStore(store, index + 1, store.getDisplayName());

        init();

        doCalculations();
    }

    private void doCalculations() {
        peaks = findPeaks();
        recessionCurves = findRecessionCurves();
        groundwater = calculateGroundwater();
        groundwaterSections = calculateBaseFlowPeriods();

        update();
    }

    public JFrame getOwner() {
        return owner;
    }

    private class SelectionDialog extends JDialog {

        private Object selectedAttribute = null;

        public SelectionDialog(Frame owner, Set<Object> attr) {
            super(owner, "Select Attribute", true);
            setLayout(new GridBagLayout());
            final JComboBox attrSelection = new JComboBox();
            for (Object name : attr) {
                attrSelection.addItem(name);
            }
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.ipadx = 10;
            c.ipady = 10;
            add(new JLabel("Select Attribute"), c);


            selectedAttribute = attrSelection.getSelectedItem();
            c.gridx = 1;
            add(attrSelection, c);
            JButton okButton = new JButton("Ok");
            okButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    selectedAttribute = attrSelection.getSelectedItem();
                    SelectionDialog.this.setVisible(false);
                }
            });
            c.gridx = 0;
            c.gridwidth = 2;
            c.gridy = 1;
            c.anchor = c.CENTER;
            add(okButton, c);

            setPreferredSize(new Dimension(200, 100));
            setMinimumSize(new Dimension(200, 100));

            setLocationRelativeTo(null);

            setVisible(true);
        }

        public Object getSelection() {
            return this.selectedAttribute;
        }
    }

    private void update() {
        //update peaks
        dataset1.removeAllSeries();

        TimeSeries series = new TimeSeries("hydrograph");

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();
        for (long i = 0; i < n; i++) {
            series.add(new Day(hydrograph.getTime((int) i)), hydrograph.getValue((int) i));
        }
        dataset1.addSeries(series);

        datasetPeaks.removeAllSeries();

        TimeSeries seriesPeaks = new TimeSeries("peaks");

        for (int i = 0; i < this.peakCount; i++) {
            int index = this.peaks.get(i).index;
            double value = this.peaks.get(i).value;
            seriesPeaks.add(new Day(hydrograph.getTime(index)), value);
        }
        datasetPeaks.addSeries(seriesPeaks);

        datasetRecessionCurves.removeAllSeries();

        TimeSeries seriesRecession = new TimeSeries("recession");

        for (int i = 0; i < this.recessionCount; i++) {
            for (int index = this.recessionCurves.get(i).startIndex; index < this.recessionCurves.get(i).endIndex; index++) {
                double value = this.recessionCurves.get(i).at(index);
                seriesRecession.addOrUpdate(new Day(hydrograph.getTime(index)), value);
            }
            seriesRecession.addOrUpdate(new Day(hydrograph.getTime(this.recessionCurves.get(i).endIndex)), Double.NaN);
        }
        datasetRecessionCurves.addSeries(seriesRecession);

        datasetGroundwater.removeAllSeries();

        TimeSeries groundwaterSeries = new TimeSeries("groundwater(est)");

        n = groundwater.getTimeDomain().getNumberOfTimesteps();
        for (long i = 0; i < n; i++) {
            groundwaterSeries.add(new Day(groundwater.getTime((int) i)), groundwater.getValue((int) i));
        }
        datasetGroundwater.addSeries(groundwaterSeries);

        datasetBaseFlow.removeAllSeries();

        TimeSeries groundwaterPeriodSeries = new TimeSeries("baseflowPeriod");

        for (int i = 0; i < this.groundwaterSections.size(); i++) {
            for (int index = this.groundwaterSections.get(i).startIndex; index < this.groundwaterSections.get(i).endIndex; index++) {
                double value = this.groundwaterSections.get(i).at(index);
                groundwaterPeriodSeries.addOrUpdate(new Day(hydrograph.getTime(index)), value);
            }
            groundwaterPeriodSeries.addOrUpdate(new Day(hydrograph.getTime(this.groundwaterSections.get(i).endIndex)), Double.NaN);
        }
        datasetBaseFlow.addSeries(groundwaterPeriodSeries);
    }

    private void showPeaks(boolean show) {
        peakSlider.setMaximum(this.peaks.size());
        if (show) {
            chart.getXYPlot().setDataset(1, datasetPeaks);
        } else {
            chart.getXYPlot().setDataset(1, null);
        }
    }

    private void showRecessionCurve(boolean show) {
        recessionSlider.setMaximum(this.recessionCurves.size());
        if (show) {
            chart.getXYPlot().setDataset(2, datasetRecessionCurves);
        } else {
            chart.getXYPlot().setDataset(2, null);
        }
    }

    private void showGroundwaterCurve(boolean show) {
        if (show) {
            chart.getXYPlot().setDataset(3, datasetGroundwater);
        } else {
            chart.getXYPlot().setDataset(3, null);
        }
    }

    private void showBaseFlowPeriods(boolean show) {
        if (show) {
            chart.getXYPlot().setDataset(4, datasetBaseFlow);
        } else {
            chart.getXYPlot().setDataset(4, null);
        }
    }

    private void calcWeights() {
        int dsCount = weightChart.getXYPlot().getDatasetCount();
        int n = 0;
        for (int i = 0; i < dsCount; i++) {
            weightChart.getXYPlot().setDataset(i, null);
        }
        String simItem = (String) simDatasets.getSelectedItem();
        String obsItem = (String) obsDatasets.getSelectedItem();

        TimeSerieEnsemble tsEnsemble = HydroAnalysisPanel.this.ensemble.getTimeserieEnsemble(simItem);
        TimeSerie obsTS = (TimeSerie) HydroAnalysisPanel.this.ensemble.getDataSet(obsItem);

        weights = SlopeCalculations.calcParameterSensitivityTimeserie(HydroAnalysisPanel.this.ensemble, tsEnsemble, obsTS, 0.33);
        n = weights.length;
        CategoryTableXYDataset dataset = SlopeCalculations.buildCategoryDataset(weights, obsTS, ensemble);

        weightChart.getXYPlot().setDataset(10, dataset);
        weightChart.getXYPlot().setRenderer(10, weightBarRenderer);

        TimeSeriesCollection collections[] = new TimeSeriesCollection[n];
        TimeSeries timeseries[] = new TimeSeries[n];
        SimpleEnsemble p[] = SlopeCalculations.getParameterEnsembles(ensemble);
        for (int j = 0; j < n; j++) {
            collections[j] = new TimeSeriesCollection();
            timeseries[j] = new TimeSeries(p[j].getName());
            collections[j].addSeries(timeseries[j]);
        }

        parameterIDs = new String[n];
        for (int j = 0; j < n; j++) {
            dominantParameterChart.getXYPlot().setDataset(j, collections[j]);
            dominantParameterChart.getXYPlot().setRenderer(j, HydroAnalysisPanel.this.weightRenderer[0]);
            parameterIDs[j] = p[j].getName();
        }

        ArrayList<int[]> dominantParameters = OptimizationScheme.calcDominantParameters(weights, 0.8);

        for (int i = 0; i < dominantParameters.size(); i++) {
            int list[] = dominantParameters.get(i);
            Arrays.sort(list);
            int c = 0;
            for (int j = 0; j < n; j++) {
                if (list[c] < j) {
                    timeseries[j].add(new Day(obsTS.getTime(i)), Double.NaN);
                } else if (list[c] == j) {
                    timeseries[j].add(new Day(obsTS.getTime(i)), j);
                    if (c < list.length - 1) {
                        c++;
                    }
                } else {
                    timeseries[j].add(new Day(obsTS.getTime(i)), Double.NaN);
                }
            }
        }

        SymbolAxis symAxis = new SymbolAxis("parameter", parameterIDs);
        symAxis.setRange(-1, n + 1);
        symAxis.setTickUnit(new NumberTickUnit(1.0));
        dominantParameterChart.getXYPlot().setRangeAxis(symAxis);
        dominantParameterChart.removeLegend();
    }

    private void init() {
        dataset1 = new TimeSeriesCollection();
        datasetPeaks = new TimeSeriesCollection();
        datasetRecessionCurves = new TimeSeriesCollection();
        datasetGroundwater = new TimeSeriesCollection();
        datasetBaseFlow = new TimeSeriesCollection();

        chart = ChartFactory.createTimeSeriesChart(
                "Hydrograph",
                "time",
                "runoff",
                dataset1,
                true,
                true,
                false);
        chart.getXYPlot().getDomainAxis().addChangeListener(new AxisChangeListener() {

            public void axisChanged(AxisChangeEvent e) {
                weightChart.getXYPlot().setDomainAxis(chart.getXYPlot().getDomainAxis());
                dominantParameterChart.getXYPlot().setDomainAxis(chart.getXYPlot().getDomainAxis());
            }
        });

        this.weightChart = ChartFactory.createTimeSeriesChart(
                "Hydrograph",
                "time",
                "weights",
                dataset1,
                true,
                true,
                false);

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

        XYDotRenderer peakRenderer = new XYDotRenderer();
        peakRenderer.setBaseFillPaint(new Color(0, 0, 255));
        peakRenderer.setDotHeight(5);
        peakRenderer.setDotWidth(5);
        peakRenderer.setShape(new Rectangle2D.Double(0.0, 0.0, 5.0, 5.0));

        XYLineAndShapeRenderer recessionRenderer = new XYLineAndShapeRenderer();
        recessionRenderer.setBaseFillPaint(new Color(0, 255, 0));
        recessionRenderer.setBaseLinesVisible(true);
        recessionRenderer.setDrawSeriesLineAsPath(true);
        recessionRenderer.setBaseOutlinePaint(new Color(0, 255, 0));
        recessionRenderer.setBaseSeriesVisible(true);
        recessionRenderer.setDrawOutlines(true);
        recessionRenderer.setBaseShapesVisible(false);
        recessionRenderer.setStroke(new BasicStroke(5.0f));

        XYLineAndShapeRenderer groundwaterRenderer = new XYLineAndShapeRenderer();
        groundwaterRenderer.setPaint(new Color(255, 200, 0));
        groundwaterRenderer.setBaseLinesVisible(true);
        groundwaterRenderer.setBaseShapesVisible(false);
        groundwaterRenderer.setDrawSeriesLineAsPath(true);
        groundwaterRenderer.setBaseSeriesVisible(true);

        XYLineAndShapeRenderer baseFlowRenderer = new XYLineAndShapeRenderer();
        baseFlowRenderer.setBaseFillPaint(new Color(128, 128, 128));
        baseFlowRenderer.setBaseLinesVisible(true);
        baseFlowRenderer.setDrawSeriesLineAsPath(true);
        baseFlowRenderer.setBaseOutlinePaint(new Color(128, 128, 128));
        baseFlowRenderer.setBaseSeriesVisible(true);
        baseFlowRenderer.setDrawOutlines(true);
        baseFlowRenderer.setBaseShapesVisible(false);
        baseFlowRenderer.setStroke(new BasicStroke(5.0f));

        for (int i = 0; i < MAX_WEIGHTS; i++) {
            weightRenderer[i] = new XYLineAndShapeRenderer();
            weightRenderer[i].setBaseFillPaint(weightColor[i]);
            weightRenderer[i].setBaseLinesVisible(true);
            weightRenderer[i].setBaseShapesVisible(false);
            weightRenderer[i].setBaseSeriesVisible(true);
            weightRenderer[i].setDrawSeriesLineAsPath(true);
            weightRenderer[i].setStroke(new BasicStroke(1.0f));
        }


        chart.getXYPlot().setRenderer(WIDTH, null);
        chart.getXYPlot().setDataset(1, datasetPeaks);
        chart.getXYPlot().setRenderer(1, peakRenderer);
        chart.getXYPlot().setDataset(2, datasetRecessionCurves);
        chart.getXYPlot().setRenderer(2, recessionRenderer);
        chart.getXYPlot().setDataset(3, datasetGroundwater);
        chart.getXYPlot().setRenderer(3, groundwaterRenderer);
        chart.getXYPlot().setDataset(4, datasetBaseFlow);
        chart.getXYPlot().setRenderer(4, baseFlowRenderer);

        ChartPanel weightChartPanel = new ChartPanel(weightChart, true);

        ChartPanel chartPanel = new ChartPanel(chart, true);
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
                        DataRequestPanel d = new DataRequestPanel(sgsat, HydroAnalysisPanel.this.ensemble);
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

        ChartPanel dominantParametersChartPanel = new ChartPanel(dominantParameterChart, true);


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
                showPeaks(src.isSelected());
            }
        });
        JPanel peakPanel = new JPanel();
        peakPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Peaks"));
        peakPanel.add(showPeaks);
        peakPanel.add(peakSlider);
        peakSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                peakNumberField.setText(Integer.toString(peakSlider.getValue()));
                peakCount = peakSlider.getValue();
                update();
            }
        });
        peakNumberField.setEditable(false);
        peakPanel.add(peakNumberField);

        JCheckBox showRecessions = new JCheckBox();
        showRecessions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
                showRecessionCurve(src.isSelected());
            }
        });
        JPanel recessionPanel = new JPanel();
        recessionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "recessions"));
        recessionPanel.add(showRecessions);
        recessionPanel.add(recessionSlider);
        recessionSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                recessionNumberField.setText(Integer.toString(recessionSlider.getValue()));
                recessionCount = recessionSlider.getValue();
                update();
            }
        });
        recessionNumberField.setEditable(false);
        recessionPanel.add(recessionNumberField);

        JCheckBox showGroundwater = new JCheckBox();
        showGroundwater.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
                showGroundwaterCurve(src.isSelected());
            }
        });
        JPanel groundwaterPanel = new JPanel();
        groundwaterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "groundwater"));
        groundwaterPanel.add(showGroundwater);

        JCheckBox showBaseFlowPeriods = new JCheckBox();
        showBaseFlowPeriods.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
                showBaseFlowPeriods(src.isSelected());
            }
        });

        JPanel baseFlowPanel = new JPanel();
        baseFlowPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "baseflow periods"));
        baseFlowPanel.add(showBaseFlowPeriods);
        baseFlowPanel.add(groundwaterSlider);
        groundwaterSlider.setMinimum(0);
        groundwaterSlider.setMaximum(1000);
//        System.out.println(scheme[0].toString());
        groundwaterSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                groundwaterThresholdField.setText(Double.toString((groundwaterSlider.getValue() / 1000.0) * 5.0));
                groundwaterThreshold = (groundwaterSlider.getValue() / 1000.0) * 5.0;
                groundwaterSections = calculateBaseFlowPeriods();
                update();
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

        loadEnsemble.addActionListener(new ActionListener() {

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
                int result = chooser.showOpenDialog(HydroAnalysisPanel.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    HydroAnalysisPanel.this.ensemble = DataCollection.createFromFile(chooser.getSelectedFile());
                    Set<String> obsDatasets = ensemble.getDatasets(Measurement.class);
                    Set<String> paramDatasets = ensemble.getDatasets(Parameter.class);
                    Set<String> effDatasets = ensemble.getDatasets(Efficiency.class);
                    Set<String> simDatasets = ensemble.getDatasets(TimeSerie.class);
                    HydroAnalysisPanel.this.obsDatasets.removeAll();
                    HydroAnalysisPanel.this.paramDatasets.removeAll();
                    HydroAnalysisPanel.this.effDatasets.removeAll();
                    HydroAnalysisPanel.this.simDatasets.removeAll();
                    for (String s : obsDatasets) {
                        HydroAnalysisPanel.this.obsDatasets.addItem(s);
                    }
                    for (String s : paramDatasets) {
                        HydroAnalysisPanel.this.paramDatasets.addItem(s);
                    }
                    for (String s : effDatasets) {
                        HydroAnalysisPanel.this.effDatasets.addItem(s);
                    }
                    for (String s : simDatasets) {
                        HydroAnalysisPanel.this.simDatasets.addItem(s);
                    }
                    if (!obsDatasets.isEmpty()) {
                        Measurement measurement = (Measurement) HydroAnalysisPanel.this.ensemble.getDataSet(obsDatasets.iterator().next());
                        HydroAnalysisPanel.this.hydrograph = measurement;
                        HydroAnalysisPanel.this.doCalculations();
                    }
                }
                for (int i = 0; i < scheme.length; i++) {
                    scheme[i] = null;
                }
                weights = null;
                ((DefaultListModel) parameterGroups.getModel()).clear();
                optimizationSchemeDesc.setText("");
            }
        });

        obsDatasets.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    Measurement measurement = (Measurement) HydroAnalysisPanel.this.ensemble.getDataSet((String) e.getItem());
                    HydroAnalysisPanel.this.hydrograph = measurement;
                    HydroAnalysisPanel.this.doCalculations();
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

        JButton calcOptimizationScheme = new JButton("Do Calculation");
        calcOptimizationScheme.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String obsItem = (String) obsDatasets.getSelectedItem();
                String effItem = (String) effDatasets.getSelectedItem();
                int index = optimizationSchemes.getSelectedIndex();
                if (weights == null) {
                    calcWeights();
                }
                switch (index) {
                    case 0: {
                        scheme[index] = new OptimalOptimizationScheme(weights, SlopeCalculations.getParameterEnsembles(ensemble),
                                (SimpleEnsemble)HydroAnalysisPanel.this.ensemble.getDataSet(effItem),
                                (TimeSerie) HydroAnalysisPanel.this.ensemble.getDataSet(obsItem));
                        break;
                    }
                    case 1: {
                        scheme[index] = new GreedyOptimizationScheme(weights, SlopeCalculations.getParameterEnsembles(ensemble),
                                (SimpleEnsemble)HydroAnalysisPanel.this.ensemble.getDataSet(effItem),
                                (TimeSerie) HydroAnalysisPanel.this.ensemble.getDataSet(obsItem));
                        break;
                    }
                    case 2: {
                        scheme[index] = new SimilarityBasedOptimizationScheme(weights, SlopeCalculations.getParameterEnsembles(ensemble),
                                (SimpleEnsemble)HydroAnalysisPanel.this.ensemble.getDataSet(effItem),
                                (TimeSerie) HydroAnalysisPanel.this.ensemble.getDataSet(obsItem));
                        break;
                    }
                }
                scheme[index].calcOptimizationScheme();
                ((DefaultListModel) parameterGroups.getModel()).clear();
                int counter = 0;
                for (ParameterGroup p : scheme[index].solutionGroups) {
                    ((DefaultListModel) parameterGroups.getModel()).addElement("Group" + counter++);
                }
                parameterGroups.clearSelection();
                optimizationSchemeDesc.setText("");
            }
        });

        parameterGroups.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int index = e.getFirstIndex();

                int schemeIndex = optimizationSchemes.getSelectedIndex();
                String text = scheme[schemeIndex].solutionGroups.get(index).toString();
                optimizationSchemeDesc.setText(text);
            }
        });

        optimizationSchemes.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                int index = optimizationSchemes.getSelectedIndex();
                ((DefaultListModel) parameterGroups.getModel()).clear();
                if (scheme[index] == null) {
                    return;
                }

                int counter = 0;
                for (ParameterGroup p : scheme[index].solutionGroups) {
                    ((DefaultListModel) parameterGroups.getModel()).addElement("Group" + counter++);
                }
                parameterGroups.clearSelection();
                optimizationSchemeDesc.setText("");
            }
        });

        JButton calcMaxSlopeCurve = new JButton("Calc Weights");
        calcMaxSlopeCurve.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                calcWeights();
            }
        });



        JPanel subPanel = new JPanel(new FlowLayout());
        subPanel.add(loadEnsemble);
        subPanel.add(calcMaxSlopeCurve);
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
        con.gridy = 1;
        con.gridwidth = 1;
        con.gridheight = 1;
        schemePanel.add(calcOptimizationScheme, con);
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
}
