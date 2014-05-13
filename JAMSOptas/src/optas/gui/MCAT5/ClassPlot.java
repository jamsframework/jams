/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class ClassPlot extends MCAT5Plot {

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    XYPlot plot = new XYPlot();
    JTextField groupCount = new JTextField(2);
    JPanel mainPanel = new JPanel();
    int GROUPS = 10;

    public ClassPlot() {
        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));
        init();
    }

    private class CustomXYToolTipGenerator implements XYToolTipGenerator{

        int index=0;
        String tooltip = "";
        public CustomXYToolTipGenerator(int index, String tooltip){
            this.tooltip = tooltip;
            this.index = index;
        }
        @Override
        public String generateToolTip(XYDataset xyd, int i, int i1) {
            return tooltip;
        }
        
    }
    
    private void init() {
        plot.setRenderer(renderer);
        plot.setDomainAxis(new DateAxis(JAMS.i18n("TIME")));
        plot.setRangeAxis(new NumberAxis(JAMS.i18n("OUTPUT")));

        JFreeChart chart = new JFreeChart(plot);
        
        chart.setTitle(JAMS.i18n("CLASS_PLOT"));
        chart.getPlot().setBackgroundPaint(Color.white);
        chart.getXYPlot().setDomainGridlinePaint(Color.black);
        
        PatchedChartPanel chartPanel = new PatchedChartPanel(chart, true);

        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        JPanel sliderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sliderPanel.setMaximumSize(new Dimension(250, 100));
        sliderPanel.setPreferredSize(new Dimension(250, 100));
        sliderPanel.setMinimumSize(new Dimension(250, 100));

        JSlider slider = new JSlider();
        slider.setMinimum(2);
        slider.setMaximum(30);
        slider.setValue(GROUPS);
        groupCount.setText(Integer.toString(GROUPS));
        groupCount.setEnabled(false);
        
        slider.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                
                JSlider slider = (JSlider) e.getSource();
                if (!slider.getValueIsAdjusting()) {
                    ClassPlot.this.GROUPS = slider.getValue();
                    groupCount.setText(Integer.toString(GROUPS));
                    redraw();
                }
            }
        });
        sliderPanel.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("number_of_groups")));
        sliderPanel.add(slider);
        sliderPanel.add(groupCount);
        sliderPanel.setMaximumSize(new Dimension(500, 60));
        sliderPanel.setMinimumSize(new Dimension(500, 60));
        groupCount.setMaximumSize(new Dimension(60, 60));
        mainPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(chartPanel)
                .addComponent(sliderPanel)
                );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(chartPanel)
                .addComponent(sliderPanel)
                );
        
        redraw();
        
    }

    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        TimeSerieEnsemble ts = (TimeSerieEnsemble) p[0].get(0);
        EfficiencyEnsemble eff = (EfficiencyEnsemble) p[1].get(0);


        for (int i = 0; i < GROUPS; i++) {
            renderer.setSeriesShapesVisible(i, false);
            int c = (int) (i * 255.0 / GROUPS);
            renderer.setSeriesPaint(i, new Color(255 - c, 0, c));
            renderer.setSeriesVisibleInLegend(i, false);
            if (i == 0 || i == GROUPS-1){
                renderer.setSeriesStroke(i, new BasicStroke(3));
            }else{
                renderer.setSeriesStroke(i, new BasicStroke(1));
            }
        }
        renderer.setSeriesVisibleInLegend(0, true);
        renderer.setSeriesVisibleInLegend(GROUPS - 1, true);

        EfficiencyEnsemble likelihood = eff.CalculateLikelihood();
        Integer sortedIds[] = likelihood.sort();

        int n = eff.getSize();
        int T = ts.getTimesteps();

        TimeSeriesCollection series = new TimeSeriesCollection();
        for (int i = 0; i < GROUPS; i++) {
            TimeSeries dataset = new TimeSeries("");
            if (i == 0) {
                dataset = new TimeSeries(JAMS.i18n("HIGH_LIKELIHOOD"));
            }
            if (i == GROUPS - 1) {
                dataset = new TimeSeries(JAMS.i18n("LOW_LIKELIHOOD"));
            }
            
            int index_low = (int) (((double)n / (double) GROUPS) * (double)i);
            int index_high= (int) (((double)n / (double) GROUPS) * (double)(i+1));
            if (index_high >= n){
                index_high = n-1;
            }
            
            for (int j = 0; j < T; j++) {
                Day d = new Day(ts.getDate((int) j));
                double mean = 0;
                for (int k=index_low;k<index_high;k++){
                    mean += ts.get(j, sortedIds[k]);
                }
                mean /= (double)(index_high-index_low);
                dataset.add(d, mean);
            }
            double mean_eff = 0;
            for (int k=index_low;k<index_high;k++){
                mean_eff += eff.getValue(sortedIds[k]);
            }
            mean_eff /= (double)(index_high-index_low);
            series.addSeries(dataset);
            
            String tooltip = "<html><body>";                        
            tooltip += eff.getName() + ":" + String.format("%.2f&nbsp;", mean_eff);            
            tooltip+="</body></html>";
                        
            plot.getRenderer().setSeriesToolTipGenerator(i, new CustomXYToolTipGenerator(i, tooltip) );
        }

        plot.setDataset(series);

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis().setAutoRange(true);
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setAutoRange(true);
        }
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }
}
