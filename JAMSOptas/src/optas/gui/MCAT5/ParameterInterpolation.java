/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import optas.data.DataSet;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.tools.PatchedChartPanel;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ParameterInterpolation extends MCAT5Plot {

    XYPlot plot = new XYPlot();
    PatchedChartPanel chartPanel = null;
    JPanel panel = null;    
    JSlider slider = new JSlider();

    TimeSerieEnsemble timeserie;
    SimpleEnsemble[] params;

    double interpolatedTS[][];    
    final int RESOLUTION = 100;

    int currentIndex = -1;
    double paramMin, paramMax;
    double point[];

    double  globalMin = Double.MAX_VALUE,
            globalMax = Double.MIN_VALUE;

    public ParameterInterpolation() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"),TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"),Parameter.class,1,10));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"),Measurement.class));

        init();
    }

    private void init(){
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(0,0,255));
        renderer.setSeriesVisibleInLegend(0, true);
        renderer.setSeriesPaint(1, new Color(255,0,0));
        renderer.setSeriesVisibleInLegend(1, true);
        renderer.setBaseShapesVisible(false);

        plot.setRenderer(renderer);
        plot.setDomainAxis(new NumberAxis(JAMS.i18n("TIME")));
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));
    }

    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled())
            return;

        ArrayList<DataSet> p[] = getData(new int[]{0,1,2});
        timeserie = (TimeSerieEnsemble)p[0].get(0);
        ArrayList<DataSet>  dataInParam   = (ArrayList<DataSet>)p[1];
        Measurement obs = (Measurement) p[2].get(0);

        params = new SimpleEnsemble[dataInParam.size()];
        int timesteps = timeserie.getTimesteps();

        interpolatedTS = new double[RESOLUTION][timesteps];
        point = new double[params.length];

        for (int t=0;t<timesteps;t++){
            for (int j=0;j<timeserie.getSize();j++){
                if (timeserie.get(t, j)<globalMin)
                    globalMin = timeserie.get(t, j);
                if (timeserie.get(t, j)>globalMax)
                    globalMax = timeserie.get(t, j);
            }
        }
        
        if (plot.getDomainAxis() != null) plot.getDomainAxis().setAutoRange(true);
        if (plot.getRangeAxis() != null) plot.getRangeAxis().setRange(globalMin, globalMax);
        
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle("Interpolation");
        chartPanel = new PatchedChartPanel(chart, true);

        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
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
            @Override
           public void stateChanged(ChangeEvent evt){
               point[currentIndex] = (((paramMax-paramMin)*(double)slider.getValue())/(double)RESOLUTION)+paramMin;
               redraw();
            }
        });
        adjustmentPanel.add(slider, BorderLayout.CENTER);

        Object[] listItem = new Object[params.length];
        for (int i=0;i<params.length;i++){
            listItem[i] = params[i].name;
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
                    paramMin = params[currentIndex].getMin();
                    paramMax = params[currentIndex].getMax();
                    Dictionary labels = new Hashtable<Integer,JLabel>();
                    for (int i=0;i<=100;i+=10){
                        labels.put(i, new JLabel(f.format( (double)i*((paramMax - paramMin)/100.0)+paramMin )));
                    }
                    slider.setLabelTable(labels);
                    slider.setValue( (int)Math.round((point[currentIndex]-paramMin)/(paramMax-paramMin)*(double)RESOLUTION));
                    slider.setEnabled(true);
                    doInterpolation(currentIndex);
                    redraw();
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

        XYSeriesCollection series = new XYSeriesCollection();

        XYSeries dataset1 = new XYSeries(JAMS.i18n("HIGH_LIKELIHOOD"));
        XYSeries dataset2 = new XYSeries(JAMS.i18n("HIGH_LIKELIHOOD"));

        for (int j = 0; j < timesteps; j++) {
            dataset1.add(j, obs.getValue(j));
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
    }

    public void doInterpolation(int parameter) {
        double weights[][] = new double[RESOLUTION][this.timeserie.getSize()];
        double sum[] = new double[RESOLUTION];

        for (int r = 0; r < RESOLUTION; r++) {
            double p = paramMin + (paramMax - paramMin) * (double) r / (double) RESOLUTION;
            sum[r] = 0;

            for (int i = 0; i < this.timeserie.getSize(); i++) {
                double dist = 0;
                for (int j = 0; j < params.length; j++) {
                    if (j != parameter) {
                        dist += (params[j].getValue(i) - this.point[j]) * (params[j].getValue(i) - this.point[j]);
                    } else {
                        dist += (params[j].getValue(i) - p) * (params[j].getValue(i) - p);
                    }
                }
                weights[r][i] = 1.0/Math.sqrt(dist);
                sum[r] += weights[r][i];
            }
        }
        for (int r = 0; r < RESOLUTION; r++) {
            for (int i = 0; i < this.timeserie.getSize(); i++) {
                weights[r][i] /= sum[r];
            }
            for (int t = 0; t < this.timeserie.getTimesteps();t++){
                interpolatedTS[r][t] = 0;
                for (int i = 0; i < this.timeserie.getSize(); i++) {
                    interpolatedTS[r][t] += timeserie.get(t, i)*weights[r][i];
                }
            }

        }

    }
    
    public JPanel getPanel() {
        return panel;
    }
}
