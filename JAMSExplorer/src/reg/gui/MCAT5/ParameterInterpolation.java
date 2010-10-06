/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import reg.gui.MCAT5Toolbar.ObservationDataSet;
import reg.gui.MCAT5Toolbar.ParameterSet;
import reg.gui.MCAT5Toolbar.SimulationTimeSeriesDataSet;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ParameterInterpolation {

    XYPlot plot = new XYPlot();
    ChartPanel chartPanel = null;
    JPanel panel = null;
    SimulationTimeSeriesDataSet timeserie;
    ParameterSet param[];
    ObservationDataSet observation;
    JSlider slider = new JSlider();

    double interpolatedTS[][];
    int timesteps = 0;
    final int RESOLUTION = 100;

    int currentIndex = -1;
    double paramMin, paramMax;
    double point[];

    double globalMin = Double.MAX_VALUE,
               globalMax = Double.MIN_VALUE;

    public ParameterInterpolation(SimulationTimeSeriesDataSet timeserie, ParameterSet param[], ObservationDataSet observation) {
        this.timeserie = timeserie;
        this.param = param;
        this.observation = observation;

        timesteps = timeserie.timeLength;
        interpolatedTS = new double[RESOLUTION][timesteps];
        point = new double[param.length];



        for (int t=0;t<timesteps;t++){
            for (int j=0;j<timeserie.parent.numberOfRuns;j++){
                if (timeserie.set[t].set[j]<globalMin)
                    globalMin = timeserie.set[t].set[j];
                if (timeserie.set[t].set[j]>globalMax)
                    globalMax = timeserie.set[t].set[j];
            }
        }

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(0,0,255));
        renderer.setSeriesVisibleInLegend(0, true);
        renderer.setSeriesPaint(1, new Color(255,0,0));
        renderer.setSeriesVisibleInLegend(1, true);
        renderer.setBaseShapesVisible(false);
        
        plot.setRenderer(renderer);
        plot.setDomainAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("TIME")));
        plot.setRangeAxis(new NumberAxis(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("OUTPUT")));

        if (plot.getDomainAxis() != null) plot.getDomainAxis().setAutoRange(true);
        if (plot.getRangeAxis() != null) plot.getRangeAxis().setRange(globalMin, globalMax);
        
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("CLASS_PLOT"));
        chartPanel = new ChartPanel(chart, true);

        panel = new JPanel(new BorderLayout());
        panel.add(chartPanel,BorderLayout.WEST);

        JPanel adjustmentPanel = new JPanel(new BorderLayout());

        slider.setBorder(BorderFactory.createTitledBorder("Parameter Space"));
        slider.setMaximum(99);
        slider.setMinimum(0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(50);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener(){
           public void stateChanged(ChangeEvent evt){
               point[currentIndex] = (((paramMax-paramMin)*(double)slider.getValue())/(double)RESOLUTION)+paramMin;
               updateData();
           }
        });
        adjustmentPanel.add(slider, BorderLayout.CENTER);

        Object[] listItem = new Object[param.length];
        for (int i=0;i<param.length;i++){
            listItem[i] = param[i].name;
        }
        final JList list = new JList(listItem);

        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);

        list.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e){
                currentIndex = list.getSelectedIndex();
                NumberFormat f = NumberFormat.getInstance();

                if (currentIndex != -1){
                    paramMin = MCAT5Tools.FindMinimalParameterValue(ParameterInterpolation.this.param[currentIndex]);
                    paramMax = MCAT5Tools.FindMaximalParameterValue(ParameterInterpolation.this.param[currentIndex]);
                    Dictionary labels = new Hashtable<Integer,JLabel>();
                    for (int i=0;i<=100;i+=10){
                        labels.put(i, new JLabel(f.format( (double)i*((paramMax - paramMin)/100.0)+paramMin )));
                    }
                    slider.setLabelTable(labels);
                    slider.setValue( (int)Math.round((point[currentIndex]-paramMin)/(paramMax-paramMin)*(double)RESOLUTION));
                    slider.setEnabled(true);
                    doInterpolation(currentIndex);
                    updateData();
                }else{
                    slider.setEnabled(false);
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 200));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(new JLabel("Parameter"),BorderLayout.NORTH);
        listPanel.add(listScroller,BorderLayout.SOUTH);
        adjustmentPanel.add(listPanel,BorderLayout.NORTH);
        

        panel.add(adjustmentPanel,BorderLayout.EAST);

        updateData();
    }

    public void doInterpolation(int parameter) {
        double weights[][] = new double[RESOLUTION][this.timeserie.parent.numberOfRuns];
        double sum[] = new double[RESOLUTION];

        for (int r = 0; r < RESOLUTION; r++) {
            double p = paramMin + (paramMax - paramMin) * (double) r / (double) RESOLUTION;
            sum[r] = 0;

            for (int i = 0; i < this.timeserie.parent.numberOfRuns; i++) {
                double dist = 0;
                for (int j = 0; j < param.length; j++) {
                    if (j != parameter) {
                        dist += (param[j].set[i] - this.point[j]) * (param[j].set[i] - this.point[j]);
                    } else {
                        dist += (param[j].set[i] - p) * (param[j].set[i] - p);
                    }
                }
                weights[r][i] = 1.0/Math.sqrt(dist);
                sum[r] += weights[r][i];
            }
        }
        for (int r = 0; r < RESOLUTION; r++) {
            for (int i = 0; i < this.timeserie.parent.numberOfRuns; i++) {
                weights[r][i] /= sum[r];
            }
            for (int t = 0; t < this.timeserie.timeLength;t++){
                interpolatedTS[r][t] = 0;
                for (int i = 0; i < this.timeserie.parent.numberOfRuns; i++) {
                    interpolatedTS[r][t] += timeserie.set[t].set[i]*weights[r][i];
                }
            }

        }

    }

    public void updateData() {                
        XYSeriesCollection series = new XYSeriesCollection();
        
        XYSeries dataset1 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("HIGH_LIKELIHOOD"));
        XYSeries dataset2 = new XYSeries(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("HIGH_LIKELIHOOD"));

        for (int j = 0; j < timesteps; j++) {
            dataset1.add(j, observation.set[j]);
        }
        series.addSeries(dataset1);

        if (currentIndex != -1){
            int k = (int)Math.round((  (point[currentIndex]-paramMin)/(paramMax-paramMin)*(double)RESOLUTION));
            for (int j=0;j<timesteps;j++){
                dataset2.add(j,interpolatedTS[k][j]);
            }
            series.addSeries(dataset2);
        }
        plot.setDataset(series);

        //if (plot.getRangeAxis() != null)  plot.getRangeAxis().setAutoRange(true);        
    }

    public JPanel getPanel() {
        return panel;
    }
}
