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
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5Toolbar.ArrayComparator;
import reg.gui.MCAT5Toolbar.EfficiencyDataSet;
import reg.gui.MCAT5Toolbar.ObservationDataSet;
import reg.gui.MCAT5Toolbar.SimulationTimeSeriesDataSet;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class GLUEOutputUncertainty {   
    XYPlot plot1 = new XYPlot();
    XYPlot plot2 = new XYPlot();
            
    ChartPanel chartPanel1 = null;
    ChartPanel chartPanel2 = null;

    JPanel mainPanel = null;

    SimulationTimeSeriesDataSet output = null;
    ObservationDataSet observation = null;
    EfficiencyDataSet eff = null;

    double threshold = 0.5;
    double percentil = 0.95;

    public GLUEOutputUncertainty(SimulationTimeSeriesDataSet output,ObservationDataSet observation, EfficiencyDataSet eff) {
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
        
        
        this.output = output;
        this.observation = observation;
        this.eff = eff;
        
        plot1.setDomainAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME")));
        plot1.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT")));
        plot2.setDomainAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME")));
        plot2.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT")));

        JFreeChart chart1 = new JFreeChart(plot1);
        JFreeChart chart2 = new JFreeChart(plot2);
        chart1.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT_UNCERTAINTY_PLOT"));
        chart2.setTitle("");                
        chart2.removeLegend();
        
        chartPanel1 = new ChartPanel(chart1, true);
        mainPanel = new JPanel(new BorderLayout());
        JPanel panel2 = new JPanel(new FlowLayout());

        mainPanel.add(chartPanel1,BorderLayout.NORTH);

        JTextField numberField = new JTextField("0.5",5);
        numberField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JTextField field = (JTextField)e.getSource();
                try{
                    threshold = Double.parseDouble(field.getText());
                    if (threshold < 0){
                        threshold = 0.0;
                        field.setText("0.0");
                    }else if (threshold > 1){
                        threshold = 1.0;
                        field.setText("1.0");
                    }
                    updateData();
                }catch(NumberFormatException nfe){
                    System.out.println(nfe.toString());
                    nfe.printStackTrace();
                }
            }
        });

        JTextField numberField2 = new JTextField("0.95",5);
        numberField2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JTextField field = (JTextField)e.getSource();
                try{
                    percentil = Double.parseDouble(field.getText());
                    if (percentil < 0){
                        percentil = 0.0;
                        field.setText("0.0");
                    }else if (percentil > 1){
                        percentil = 1.0;
                        field.setText("1.0");
                    }
                    updateData();
                }catch(NumberFormatException nfe){
                    System.out.println(nfe.toString());
                    nfe.printStackTrace();
                }
            }
        });

        panel2.add(new JLabel("percentage of data used"));
        panel2.add(numberField);
        panel2.add(new JLabel("percentil"));
        panel2.add(numberField2);
        mainPanel.add(panel2,BorderLayout.SOUTH);
        chartPanel2 = new ChartPanel(chart2, true);
                        
        updateData();
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
    
    public void updateData() {
        XYSeries dataset1 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("LOWER_CONFIDENCE_BOUND"));
        XYSeries dataset2 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("UPPER_CONFIDENCE_BOUND"));
        XYSeries dataset3 = new XYSeries(this.observation.name);
        XYSeries dataset4 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NO_DESCRIPTION"));
        XYSeries dataset5 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("NO_DESCRIPTION"));
                                    
        int time_length = this.observation.timeLength;                
        int MCparam = this.output.parent.numberOfRuns;
        
        double low_conf[] = new double[time_length];
        double high_conf[] = new double[time_length];
        double conf = 1.0-percentil;
        double max_diff = 0;

        double map[] = new double[MCparam];

        for (int i=0;i<MCparam;i++)
            map[i] = i;
        double eff_sorted_data[][] = sortbyEff(map,this.eff.set);
        
        for (int i=0;i<time_length;i++){
            
            double reducedOutputSet[] = new double[(int)(threshold*MCparam)];
            for (int j=0;j<(int)(threshold*MCparam);j++){
                reducedOutputSet[j] = output.set[i].set[(int)eff_sorted_data[j][0]];
            }

            double likelihood[] = Efficiencies.CalculateLikelihood(reducedOutputSet);
            double sorted_data[][] = sortbyEff(likelihood,reducedOutputSet);
            
            //search for conf low and upbound
            double sum = 0;
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
            dataset1.add(i,low_conf[i]);
            dataset2.add(i,high_conf[i]);
            dataset3.add(i,this.observation.set[i]);
        }
        for (int i=0;i<time_length;i++){
            dataset4.add(i,(high_conf[i]-low_conf[i])/max_diff);
            dataset5.add(i,0);
        }
        XYSeriesCollection interval = new XYSeriesCollection();
        interval.addSeries(dataset1);
        interval.addSeries(dataset2);
        
        XYSeriesCollection obs_runoff = new XYSeriesCollection();
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

    public JPanel getPanel1() {
        return mainPanel;
    }
    public JPanel getPanel2() {
        return chartPanel2;
    }
}
