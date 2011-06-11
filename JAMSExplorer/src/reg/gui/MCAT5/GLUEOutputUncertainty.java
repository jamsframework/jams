/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5.MCAT5Plot.SimpleRequest;
import reg.hydro.data.Efficiency;
import reg.hydro.data.EfficiencyEnsemble;
import reg.hydro.data.Measurement;
import reg.hydro.data.TimeSerie;
import reg.hydro.data.TimeSerieEnsemble;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GLUEOutputUncertainty extends MCAT5Plot{
    XYPlot plot1 = new XYPlot();
    XYPlot plot2 = new XYPlot();
            
    ChartPanel chartPanel1 = null;
    ChartPanel chartPanel2 = null;

    JPanel mainPanel = null;
    JTextField thresholdField;
    JTextField percentilField;

    double threshold = 0.5;
    double percentil = 0.95;

    public GLUEOutputUncertainty() {
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"), Efficiency.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OBSERVED_TIMESERIE"),Measurement.class));

        init();
    }

    private double getThreshold() {
        try {
            threshold = Double.parseDouble(thresholdField.getText());
            if (threshold < 0) {
                threshold = 0.0;
                thresholdField.setText("0.0");
            } else if (threshold > 1) {
                threshold = 1.0;
                thresholdField.setText("1.0");
            }
            refresh();
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.toString());
            nfe.printStackTrace();
        }
        return threshold;
    }

    private double getPercentil() {
        try {
            percentil = Double.parseDouble(percentilField.getText());
            if (percentil < 0) {
                percentil = 0.0;
                percentilField.setText("0.0");
            } else if (percentil > 1) {
                percentil = 1.0;
                percentilField.setText("1.0");
            }
            refresh();
        } catch (NumberFormatException nfe) {
            System.out.println(nfe.toString());
            nfe.printStackTrace();
        }
        return percentil;
    }

    private void init(){
        JFreeChart chart1 =  ChartFactory.createTimeSeriesChart(
                java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT_UNCERTAINTY_PLOT"),
                "time",
                "discharge",
                null,
                true,
                true,
                false);

        plot1 = chart1.getXYPlot();

        XYDifferenceRenderer renderer1 = new XYDifferenceRenderer(Color.LIGHT_GRAY,Color.LIGHT_GRAY,false);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(); 
                
        renderer1.setBaseFillPaint(Color.LIGHT_GRAY);                
        renderer1.setPaint(Color.BLACK);
                        
        renderer2.setBaseLinesVisible(true);
        renderer2.setBaseShapesVisible(false);
        renderer2.setOutlinePaint(Color.BLUE);
        renderer2.setPaint(Color.BLUE);
        renderer2.setStroke(new BasicStroke(1));
                               
        plot1.setRenderer(1,renderer1);
        plot1.setRenderer(0,renderer2);        
        plot2.setRenderer(0,renderer1);

        plot1.getDomainAxis().setLabel(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME"));
        DateAxis axis = (DateAxis) plot1.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        plot1.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT")));
        plot2.setDomainAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME")));
        plot2.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT")));

        
        JFreeChart chart2 = new JFreeChart(plot2);
        chart1.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT_UNCERTAINTY_PLOT"));
        chart2.setTitle("");                
        chart2.removeLegend();
        
        chartPanel1 = new ChartPanel(chart1, true);

        mainPanel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new FlowLayout());

        mainPanel.add(chartPanel1,BorderLayout.NORTH);

        this.thresholdField = new JTextField("0.5",5);
        thresholdField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getThreshold();
            }
        });

        this.percentilField = new JTextField("0.95",5);
        percentilField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                getPercentil();
            }
        });

        panel2.add(new JLabel("percentage of data used"));
        panel2.add(thresholdField);
        panel2.add(new JLabel("percentil"));
        panel2.add(percentilField);
        mainPanel.add(panel2,BorderLayout.SOUTH);
        chartPanel2 = new ChartPanel(chart2, true);
                        
        refresh();
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

    public double[][] sortbyEff(double data[],double likelihood[]) {
        int n = data.length;        
        double tmp_data[][] = new double[n][2];

        for (int i = 0; i < n; i++) {            
            tmp_data[i][0] = data[i];
            tmp_data[i][1] = likelihood[i];
        }

        Arrays.sort(tmp_data, new ArrayComparator(1, true));
        return tmp_data;
    }
    
    public void refresh() {
        if (!this.isRequestFulfilled()){
            return;

        }
        TimeSerieEnsemble ts = (TimeSerieEnsemble) getData(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble) getData(1);
        Measurement obs = (Measurement) getData(2);

        TimeSeries dataset1 = new TimeSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("LOWER_CONFIDENCE_BOUND"));
        TimeSeries dataset2 = new TimeSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("UPPER_CONFIDENCE_BOUND"));
        TimeSeries dataset3 = new TimeSeries(obs.name);

        XYSeries dataset4 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NO_DESCRIPTION"));
        XYSeries dataset5 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NO_DESCRIPTION"));
                                    
        int T = ts.getTimesteps();
        int n = ts.getSize();
        
        double low_conf[] = new double[T];
        double high_conf[] = new double[T];
        double conf = 1.0-percentil;
        double max_diff = 0;

        double map[] = new double[n];

        Integer sortedIds[] = eff.sort();
        int limit = (int)(threshold*n);
        /*for (int i=0;i<n;i++)
            map[i] = i;
        double eff_sorted_data[][] = sortbyEff(map,this.eff.set);*/
        
        for (int i=0;i<T;i++){
            
            double reducedOutputSet[] = new double[limit];
            for (int j=0;j<limit;j++){
                reducedOutputSet[j] = ts.get(i, sortedIds[j]);
            }
            Arrays.sort(reducedOutputSet);
            double likelihood[] = Efficiencies.CalculateLikelihood(reducedOutputSet);
            double sorted_data[][] = sortbyEff(likelihood,reducedOutputSet);
            
            //search for conf low and upbound
            double sum = 0;
            low_conf[i] = sorted_data[0][1];
            high_conf[i] = sorted_data[likelihood.length-1][1];
            for (int j=0;j<likelihood.length;j++){
                if (sum < conf && sum + sorted_data[j][0] > conf){
                    low_conf[i] = sorted_data[j][1];
                }
                if (sum < 1.0-conf && sum + sorted_data[j][0] > 1.0-conf){
                    high_conf[i] = sorted_data[j][1];
                }        
                sum += sorted_data[j][0];
            }
            max_diff = Math.max(high_conf[i]-low_conf[i], max_diff);

            if (low_conf[i]>high_conf[i]){
                double tmp = low_conf[i];
                low_conf[i] = high_conf[i];
                high_conf[i] = tmp;
            }
            dataset1.add(new Day(obs.getTime((int) i)), low_conf[i]);
            dataset2.add(new Day(obs.getTime((int) i)), high_conf[i]);
            dataset3.add(new Day(obs.getTime((int) i)), obs.getValue(i));
        }
        for (int i=0;i<T;i++){
            dataset4.add(i,(high_conf[i]-low_conf[i])/max_diff);
            dataset5.add(i,0);
        }
        TimeSeriesCollection interval = new TimeSeriesCollection();
        interval.addSeries(dataset1);
        interval.addSeries(dataset2);
        
        TimeSeriesCollection obs_runoff = new TimeSeriesCollection();
        obs_runoff.addSeries(dataset3);
        
        XYSeriesCollection difference = new XYSeriesCollection();
        difference.addSeries(dataset4);
        difference.addSeries(dataset5);
                
        plot1.setDataset(0, obs_runoff);        
        plot1.setDataset(1, interval);        
        plot2.setDataset(0, difference);
                                                                
        if (plot1.getRangeAxis() != null) plot1.getRangeAxis().setAutoRange(true);
        if (plot1.getDomainAxis() != null)plot1.getDomainAxis().setAutoRange(true);
    }

    public JPanel getPanel(){
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
