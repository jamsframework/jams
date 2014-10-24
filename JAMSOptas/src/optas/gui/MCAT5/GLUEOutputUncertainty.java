/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.tools.PatchedChartPanel;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GLUEOutputUncertainty extends MCAT5Plot {

    XYPlot plot1 = new XYPlot();
    XYPlot plot2 = new XYPlot();
    PatchedChartPanel chartPanel1 = null;
    PatchedChartPanel chartPanel2 = null;
    JPanel mainPanel = null;
    JTextField thresholdField;
    JTextField percentilField;
    JSlider thresholdSlider;
    JSlider percentileSlider;
    JLabel dataRange;
    JButton exportMeanButton;
    JButton exportMedianButton;
    double threshold = 0.0;
    double percentil = 0.95;
    double minValue, maxValue;
    boolean isShowMean = true, isShowMedian = false;
    EfficiencyEnsemble eff = null;

    double dataCount = 0;

    String meanString, medianString, lowBound, upBound;

    public GLUEOutputUncertainty() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"), Measurement.class, 0, 1));

        init();
    }

    public void setShowMean(boolean isShowMean) {
        this.isShowMean = isShowMean;
        redraw();
    }

    public void setShowMedian(boolean isShowMedian) {
        this.isShowMedian = isShowMedian;
        redraw();
    }

    boolean isCalled1 = false;
    
    private void updateThreshold(double threshold) {
        if (isCalled1) //call only once!!
            return;
        try{
        isCalled1 = true;
        double tOld = 0, rOld = 0;

        double r = (threshold - minValue) / (maxValue - minValue);
        double t = threshold;
        if (r < 0) {
            r = 0;
        };
        if (r > 1) {
            r = 1;
        };
                
        try {
            tOld = Double.parseDouble(thresholdField.getText());
        } catch (NumberFormatException nfe) {
        }        
        rOld = thresholdSlider.getValue() / 100.;

        if (Math.abs(r - rOld) > 0.01) {
            thresholdSlider.setValue((int) (r * 100.));
        }
        if (Math.abs(t - tOld) > 0.01) {
            thresholdField.setText(String.format("%.2f", t));
        }        
        this.threshold = t;
        }finally{isCalled1 = false;}        
    }

    boolean isCalled2 = false;
    private void updateQuantile(double q) {
        if (isCalled2) //call only once!!
            return;
        try{
        isCalled2 = true;
        double qOld = 0, pOld = 0;
        
        if (q < 0) {
            q = 0;
        };
        if (q > 1) {
            q = 1;
        };
                
        try {
            pOld = Double.parseDouble(percentilField.getText());
        } catch (NumberFormatException nfe) {
        }        
        qOld = percentileSlider.getValue() / 100.;
        double p = q*100;
        
        if (Math.abs(p - pOld) > 0.01) {
            percentileSlider.setValue((int) (p));
        }
        if (Math.abs(q - qOld) > 0.01) {
            percentilField.setText(String.format("%.2f", q));
        }        
        this.percentil = q;
        }finally{isCalled2 = false;}
    }

    private void init() {
        JFreeChart chart1 = ChartFactory.createTimeSeriesChart(
                JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"),
                "time",
                "discharge",
                null,
                true,
                true,
                false);

        plot1 = chart1.getXYPlot();
        chart1.getPlot().setBackgroundPaint(Color.white);
        chart1.getXYPlot().setDomainGridlinePaint(Color.black);

        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY, Color.LIGHT_GRAY, false);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer_mean = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer_median = new XYLineAndShapeRenderer();

        renderer1.setBaseFillPaint(Color.LIGHT_GRAY);
        renderer1.setPaint(Color.BLACK);

        renderer2.setBaseLinesVisible(true);
        renderer2.setBaseShapesVisible(false);
        renderer2.setOutlinePaint(Color.BLUE);
        renderer2.setPaint(Color.BLUE);
        renderer2.setStroke(new BasicStroke(2));

        renderer_mean.setBaseLinesVisible(true);
        renderer_mean.setBaseShapesVisible(false);
        renderer_mean.setOutlinePaint(Color.RED);
        renderer_mean.setPaint(Color.RED);
        renderer_mean.setStroke(new BasicStroke(0.25f));

        renderer_median.setBaseLinesVisible(true);
        renderer_median.setBaseShapesVisible(false);
        renderer_median.setOutlinePaint(Color.ORANGE);
        renderer_median.setPaint(Color.ORANGE);
        renderer_median.setStroke(new BasicStroke(1));

        plot1.setRenderer(3, renderer1);
        plot1.setRenderer(0, renderer2);

        plot1.setRenderer(1, renderer_mean);
        plot1.setRenderer(2, renderer_median);

        plot1.getDomainAxis().setLabel(JAMS.i18n("TIME"));
        DateAxis axis = (DateAxis) plot1.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        plot1.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));

        JFreeChart chart2 = new JFreeChart(plot2);
        chart1.setTitle(JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"));
        chart2.setTitle("");
        chart2.removeLegend();

        chartPanel1 = new PatchedChartPanel(chart1, true);

        chartPanel1.setMinimumDrawWidth(0);
        chartPanel1.setMinimumDrawHeight(0);
        chartPanel1.setMaximumDrawWidth(MAXIMUM_WIDTH);
        chartPanel1.setMaximumDrawHeight(MAXIMUM_HEIGHT);

        mainPanel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new FlowLayout());

        mainPanel.add(chartPanel1, BorderLayout.NORTH);

        this.thresholdField = new JTextField("0.0", 5);
        this.thresholdSlider = new JSlider(0, 100, 100);
        thresholdSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                double t = (source.getValue() * 0.01*(maxValue - minValue)) + minValue;
                updateThreshold(t);
                if (!source.getValueIsAdjusting() && !isCalled1) {
                    redraw();
                }
            }
        });
        thresholdField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double t = Double.parseDouble(thresholdField.getText());
                    updateThreshold(t);
                    if (!isCalled1) {
                        redraw();
                    }
                } catch (NumberFormatException nfe) {

                }
            }
        });

        this.percentilField = new JTextField("0.95", 5);
        this.percentileSlider = new JSlider(0, 100, 100);
        percentileSlider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                double t = source.getValue();
                updateQuantile(t / 100.);
                if (!source.getValueIsAdjusting() && !isCalled2) {
                    redraw();
                }
            }
        });
        percentilField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    double q = Double.parseDouble(percentilField.getText());
                    updateQuantile(q);
                    if (!isCalled2) {
                        redraw();
                    }
                } catch (NumberFormatException nfe) {

                }
            }
        });

        this.exportMeanButton = new JButton("copy mean to clipboard");
        exportMeanButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection ss = new StringSelection(meanString);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            }
        });

        this.exportMedianButton = new JButton("copy median to clipboard");
        exportMedianButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection ss = new StringSelection(medianString);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            }
        });
        this.dataRange = new JLabel("range");

        JLabel thresholdLabel = new JLabel("threshold");
        JLabel percentileLabel = new JLabel("quantile");

        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Settings"));
        GroupLayout layout = new GroupLayout(filterPanel);
        filterPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(thresholdLabel)
                        .addComponent(thresholdSlider)
                        .addComponent(thresholdField)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(percentileLabel)
                        .addComponent(percentileSlider)
                        .addComponent(percentilField)
                )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(thresholdLabel)
                        .addComponent(thresholdSlider)
                        .addComponent(thresholdField)
                )
                .addGroup(layout.createParallelGroup()
                        .addComponent(percentileLabel)
                        .addComponent(percentileSlider)
                        .addComponent(percentilField)
                )
        );

        JCheckBox meanBox = new JCheckBox("show mean");
        meanBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setShowMean(((JCheckBox) e.getSource()).isSelected());
            }
        });

        JCheckBox medianBox = new JCheckBox("show median");
        medianBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setShowMedian(((JCheckBox) e.getSource()).isSelected());
            }
        });

        JPanel displayPanel = new JPanel();
        displayPanel.setBorder(BorderFactory.createTitledBorder("Display Settings"));
        layout = new GroupLayout(displayPanel);
        displayPanel.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(meanBox)
                        .addComponent(exportMeanButton)
                )
                .addGroup(layout.createSequentialGroup()
                        .addComponent(medianBox)
                        .addComponent(exportMedianButton)
                )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(meanBox)
                        .addComponent(exportMeanButton)
                )
                .addGroup(layout.createParallelGroup()
                        .addComponent(medianBox)
                        .addComponent(exportMedianButton)
                )
        );

        meanBox.setSelected(isShowMean);
        medianBox.setSelected(isShowMedian);

        panel2.add(filterPanel);
        panel2.add(displayPanel);

        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(dataRange, BorderLayout.SOUTH);
        dataRange.setAlignmentX(0.5f);
        dataRange.setHorizontalAlignment(JLabel.CENTER);
        panel3.add(panel2, BorderLayout.NORTH);

        mainPanel.add(panel3, BorderLayout.SOUTH);

        redraw();

        if (plot1.getRangeAxis() != null) {
            plot1.getRangeAxis().setAutoRange(true);
        }
        if (plot1.getDomainAxis() != null) {
            plot1.getDomainAxis().setAutoRange(true);
        }
    }

    public static class ArrayComparator implements Comparator {

        private int col = 0;
        private int order = 1;

        public ArrayComparator(int col, boolean decreasing_order) {
            this.col = col;
            if (decreasing_order) {
                order = -1;
            } else {
                order = 1;
            }
        }

        @Override
        public int compare(Object d1, Object d2) {

            double[] b1 = (double[]) d1;
            double[] b2 = (double[]) d2;

            if (b1[col] < b2[col]) {
                return -1 * order;
            } else if (b1[col] == b2[col]) {
                return 0 * order;
            } else {
                return 1 * order;
            }
        }
    }

    public double[][] sortbyEff(double data[], double likelihood[]) {
        int n = data.length;
        double tmp_data[][] = new double[n][2];

        for (int i = 0; i < n; i++) {
            tmp_data[i][0] = data[i];
            tmp_data[i][1] = likelihood[i];
        }

        Arrays.sort(tmp_data, new ArrayComparator(1, true));
        return tmp_data;
    }

    boolean showAll = true;

    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        meanString = "";
        medianString = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ArrayList<DataSet> p[] = getData(new int[]{0, 1, 2});

        TimeSerieEnsemble ts = (TimeSerieEnsemble) p[0].get(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble) p[1].get(0);
        Measurement obs = null;
        if (p[2].size() > 0) {
            obs = (Measurement) p[2].get(0);
        }

        this.eff = eff;
        minValue = eff.getMin();
        maxValue = eff.getMax();

        TimeSeries dataset1 = new TimeSeries(JAMS.i18n("LOWER_CONFIDENCE_BOUND"));
        TimeSeries dataset2 = new TimeSeries(JAMS.i18n("UPPER_CONFIDENCE_BOUND"));
        if (obs != null) {
            TimeSeries dataset3 = new TimeSeries(obs.name);
            for (int i = 0; i < obs.getTimesteps(); i++) {
                Day d = new Day(obs.getTime((int) i));
                dataset3.add(d, obs.getValue(i));
            }
            TimeSeriesCollection obs_runoff = new TimeSeriesCollection();
            obs_runoff.addSeries(dataset3);
            plot1.setDataset(0, obs_runoff);
        } else {
            plot1.setDataset(0, null);
        }
        TimeSeries dataset4 = new TimeSeries(JAMS.i18n("MEAN"));
        TimeSeries dataset5 = new TimeSeries(JAMS.i18n("MEDIAN"));

        if (showAll) {
            TimeSeries dataset_full[] = new TimeSeries[ts.getSize()];
            TimeSeriesCollection ensemble_full = new TimeSeriesCollection();
            for (int i = 0; i < ts.getSize(); i++) {
                dataset_full[i] = new TimeSeries("data_" + i);
                int id = ts.getId(i);
                for (int j = 0; j < ts.getTimesteps(); j++) {
                    Day d = new Day(ts.getDate((int) j));
                    dataset_full[i].add(d, ts.get(j, id));
                }
                ensemble_full.addSeries(dataset_full[i]);
            }
            //plot1.setDataset(1, ensemble_full);
            chartPanel1.getChart().removeLegend();
            plot1.setDomainGridlinesVisible(false);
            plot1.setRangeGridlinesVisible(false);
        }
        double low_conf[] = new double[ts.getTimesteps()];
        double high_conf[] = new double[ts.getTimesteps()];
        double conf = 1.0 - percentil;
        double max_diff = 0;

        DecimalFormat df = new DecimalFormat("0.00");
        Integer sortedIds[] = eff.sort();
        int iter = 0;
        if (eff.isPositiveBest()) {
            while ((iter < sortedIds.length && eff.getValue(sortedIds[iter]) >= threshold) || iter == 0) {
                iter++;
            }
            dataRange.setText("<html><body>the data ranges from " + df.format(eff.getValue(sortedIds[sortedIds.length - 1])) + " to "
                + df.format(eff.getValue(sortedIds[0])) + "<br>" + iter + " datasets are taken into account</body></html>");
        } else {
            while ((iter < sortedIds.length && eff.getValue(sortedIds[iter]) <= threshold) || iter == 0) {
                iter++;
            }
            dataRange.setText("<html><body>the data ranges from " + df.format(eff.getValue(sortedIds[0])) + " to "
                + df.format(eff.getValue(sortedIds[sortedIds.length - 1])) + "<br>" + iter + " datasets are taken into account</body></html>");
        }

        int limit = iter;//(int) (threshold * n);
        dataCount = limit;

        
        

        for (int i = 0; i < ts.getTimesteps(); i++) {
            double mean = 0, median;
            int counter = 0;
            int index_low = 0, index_high = limit;

            double reducedOutputSet[] = new double[limit];
            for (int j = 0; j < limit; j++) {
                reducedOutputSet[j] = ts.get(i, sortedIds[j]);
            }
            Arrays.sort(reducedOutputSet);
            double likelihood[] = Efficiencies.CalculateLikelihood(reducedOutputSet);
            double sorted_data[][] = sortbyEff(likelihood, reducedOutputSet);

            //search for conf low and upbound
            double sum = 0;
            low_conf[i] = sorted_data[0][1];
            high_conf[i] = sorted_data[likelihood.length - 1][1];
            for (int j = 0; j < likelihood.length; j++) {
                if (sum < conf && sum + sorted_data[j][0] > conf) {
                    low_conf[i] = sorted_data[j][1];
                    index_low = j;
                }
                if (sum < 1.0 - conf && sum + sorted_data[j][0] > 1.0 - conf) {
                    high_conf[i] = sorted_data[j][1];
                    index_high = j;
                }
                sum += sorted_data[j][0];
                if (sum > conf && sum < 1.0 - conf) {
                    mean += sorted_data[j][1];
                    counter++;
                }
            }
            mean /= (double) counter;
            median = sorted_data[(int) ((index_low + index_high) / 2.0)][1];

            max_diff = Math.max(high_conf[i] - low_conf[i], max_diff);

            if (low_conf[i] > high_conf[i]) {
                double tmp = low_conf[i];
                low_conf[i] = high_conf[i];
                high_conf[i] = tmp;
            }

            Day d = new Day(ts.getDate((int) i));

            dataset1.add(d, low_conf[i]);
            dataset2.add(d, high_conf[i]);

            dataset4.add(d, mean);
            meanString += sdf.format(ts.getDate((int) i)) + "\t" + mean + "\n";
            medianString += sdf.format(ts.getDate((int) i)) + "\t" + median + "\n";
            System.out.println(sdf.format(ts.getDate((int) i)) + "\t" + low_conf[i] + "\t" + high_conf[i] + "\t" + mean);
            dataset5.add(d, median);
        }

        TimeSeriesCollection interval = new TimeSeriesCollection();
        interval.addSeries(dataset1);
        interval.addSeries(dataset2);

        TimeSeriesCollection mean_ensemble = new TimeSeriesCollection();
        mean_ensemble.addSeries(dataset4);

        TimeSeriesCollection median_ensemble = new TimeSeriesCollection();
        median_ensemble.addSeries(dataset5);

        plot1.setDataset(3, interval);
        if (!showAll) {
            plot1.setDataset(3, interval);
            if (this.isShowMean) {
                plot1.setDataset(1, mean_ensemble);
            } else {
                plot1.setDataset(1, null);
            }

            if (this.isShowMedian) {
                plot1.setDataset(2, median_ensemble);
            } else {
                plot1.setDataset(2, null);
            }
        }
    }

    public JPanel getPanel() {
        /*JPanel completePanel = new JPanel(new BorderLayout());
         completePanel.add(mainPanel,BorderLayout.NORTH);
         completePanel.add(chartPanel2,BorderLayout.SOUTH);*/
        return mainPanel;
    }

    public JPanel getPanel1() {
        return mainPanel;
    }

    public JPanel getPanel2() {
        return chartPanel2;
    }
}
