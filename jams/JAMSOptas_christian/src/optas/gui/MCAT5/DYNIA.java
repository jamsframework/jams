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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import optas.data.DataSet;
import optas.data.EfficiencyEnsemble;
import optas.data.Measurement;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.gui.MCAT5.MCAT5Plot.SimpleRequest;
import optas.tools.PatchedChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class DYNIA extends MCAT5Plot {

    int window_size = 10;
    XYPlot plot = null;
    PatchedChartPanel chartPanel = null;
    JTextField winsize_box = new JTextField(10);
    
    final int BOX_COUNT = 10;
    final int BOX_COUNT_INTERNAL = 100;

    public DYNIA() {
        JFreeChart chart1 = ChartFactory.createTimeSeriesChart(
                JAMS.i18n("OUTPUT_UNCERTAINTY_PLOT"),
                "time",
                "discharge",
                null,
                true,
                true,
                false);

        plot = chart1.getXYPlot();
        chart1.getPlot().setBackgroundPaint(Color.white);

        this.addRequest(new SimpleRequest(JAMS.i18n("SIMULATED_TIMESERIE"), TimeSerie.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("OBSERVED_TIMESERIE"), Measurement.class));

        init();
    }

    private void init() {
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle(JAMS.i18n("DYNIA"));

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setSeriesStroke(1, new BasicStroke(2));
        renderer.setSeriesStroke(2, new BasicStroke(2));
        renderer.setBaseShapesVisible(false);
        plot.setRenderer(0, renderer);

        plot.getDomainAxis().setLabel(JAMS.i18n("TIME"));

        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        winsize_box.setText("10");
        winsize_box.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                redraw();
            }
        });

        JPanel text = new JPanel();
        text.setLayout(new FlowLayout(FlowLayout.CENTER));
        text.add(new JLabel(JAMS.i18n("WINDOW_SIZE")));
        text.add(winsize_box);

        chartPanel = new PatchedChartPanel(chart, true);
        chartPanel.setLayout(new BorderLayout());
        chartPanel.add(text, BorderLayout.SOUTH);

        chartPanel.setMinimumDrawWidth( 0 );
        chartPanel.setMinimumDrawHeight( 0 );
        chartPanel.setMaximumDrawWidth( MAXIMUM_WIDTH );
        chartPanel.setMaximumDrawHeight( MAXIMUM_HEIGHT );
        
        redraw();
    }

    private int getWinSize() {
        String input = winsize_box.getText();
        try {
            int number = Integer.parseInt(input);
            if (number >= 1) {
                window_size = number;
            }
        } catch (Exception e) {
            Logger.getLogger(DYNIA.class.getName()).log(Level.SEVERE, null, e);
        }
        return window_size;
    }

    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        ArrayList<DataSet> p[] = getData(new int[]{0, 1, 2});
        TimeSerieEnsemble ts = (TimeSerieEnsemble) p[0].get(0);
        SimpleEnsemble param = (SimpleEnsemble) p[1].get(0);
        Measurement obs = (Measurement) p[2].get(0);

        plot.setRangeAxis(new NumberAxis(param.name));
        plot.setRangeAxis(1, new NumberAxis(obs.name));

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        getWinSize();

        XYSeries dataset = new XYSeries(obs.name);
        XYSeries dataset_lowBound = new XYSeries("Lower Bound");
        XYSeries dataset_highBound = new XYSeries("Upper Bound");

        int n = ts.getSize();
        int T = ts.getTimesteps();
        double maxParam = param.getMax(),
                minParam = param.getMin();
        double pixel_map[][] = new double[3][T * this.BOX_COUNT];

        for (int i = 0; i < T; i++) {
            EfficiencyEnsemble eff = new EfficiencyEnsemble("E1(window)", n);
            eff.setPositiveBest(true);

            double cur_obs[] = new double[2 * window_size + 1];
            double cur_sim[] = new double[2 * window_size + 1];

            int counter = 0;
            for (int j = i - window_size; j <= i + window_size; j++) {
                if (j < 0 || j >= T) {
                    cur_obs[counter++] = 0;
                } else {
                    cur_obs[counter++] = obs.getValue(j);
                }
            }
            //calculate efficiency
            for (int k = 0; k < n; k++) {
                counter = 0;
                for (int j = i - window_size; j <= i + window_size; j++) {
                    if (j < 0 || j >= T) {
                        cur_sim[counter++] = 0;
                    } else {
                        cur_sim[counter++] = ts.get(j, ts.getId(k));
                    }
                }
                eff.add(new Integer(ts.getId(k)), Efficiencies.CalculateE(cur_obs, cur_sim, 2));
            }
            //transform to likelihood
            EfficiencyEnsemble likelihood = eff.CalculateLikelihood();
            //double cur_likelihood[] = Efficiencies.CalculateLikelihood(cur_eff);
            //and sort it
            //double sortedData[][] = sortbyEff(timeserie_param.set,cur_likelihood);
            Integer sortedIds[] = likelihood.sort();
            int limit = (int) (n * 0.1);
            double boxes[] = new double[BOX_COUNT];
            double internal_boxes[] = new double[BOX_COUNT_INTERNAL];
            int low = 0;
            int high = 0;
            
            
            for (int j = 0; j < limit; j++) {
                double best = param.getValue(sortedIds[j]);
                int index = (int) ((best - minParam) / (maxParam - minParam) * boxes.length);
                if (index == boxes.length) {
                    index = boxes.length - 1;
                }
                boxes[index] += 1.0 / limit;   
                                
                int index2 = (int) ((best - minParam) / (maxParam - minParam) * internal_boxes.length);
                if (index2 == internal_boxes.length) {
                    index2 = internal_boxes.length - 1;
                }
                internal_boxes[index2] += 1.0 / limit;  
            }        
            double low_sum=0,high_sum=0;
            while(low_sum<0.05){
                low_sum += internal_boxes[low++];
            }
            while(high_sum<0.95){
                high_sum += internal_boxes[high++];
            }
            dataset_lowBound.add(obs.getTime(i).getTime(), ((double)low/internal_boxes.length)*(maxParam - minParam) + minParam  );
            dataset_highBound.add(obs.getTime(i).getTime(), ((double)high/internal_boxes.length)*(maxParam - minParam) + minParam  );
            
            for (int j = 0; j < BOX_COUNT; j++) {
                pixel_map[0][i * BOX_COUNT + j] = obs.getTime(i).getTime();
                pixel_map[1][i * BOX_COUNT + j] = minParam + (maxParam - minParam) * (double) j / (double) BOX_COUNT;
                pixel_map[2][i * BOX_COUNT + j] = 1.0 - boxes[j];
            }
        }

        XYBlockRenderer bg_renderer = new XYBlockRenderer();
        bg_renderer.setPaintScale(new GrayPaintScale(0, 1));
        bg_renderer.setBlockHeight((maxParam - minParam) / BOX_COUNT);
        if (obs.getTimeDomain().getTimeUnit() == 6){
            bg_renderer.setBlockWidth(84000*1000);
        }else if (obs.getTimeDomain().getTimeUnit() == 2){
            bg_renderer.setBlockWidth(84000*31.00*1000);
        }else if (obs.getTimeDomain().getTimeUnit() == 1){
            bg_renderer.setBlockWidth(84000*365.00*1000);
        }else if (obs.getTimeDomain().getTimeUnit() == 11){
            bg_renderer.setBlockWidth(3600*1000);
        }
        bg_renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        DefaultXYZDataset xyz_dataset = new DefaultXYZDataset();
        xyz_dataset.addSeries(0, pixel_map);
        plot.setDataset(1, xyz_dataset);
        plot.setRenderer(1, bg_renderer);
        bg_renderer.setSeriesVisibleInLegend(0, false);
        //at last plot observed data                
        double obs_min = obs.getMin();
        double obs_max = obs.getMax();

        for (int i = 0; i < T; i++) {
            dataset.add(obs.getTime(i).getTime(), ((obs.getValue(i) - obs_min) / (obs_max - obs_min)) * (maxParam - minParam) + minParam);
        }
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(dataset);
        collection.addSeries(dataset_lowBound);
        collection.addSeries(dataset_highBound);
        plot.setDataset(0, collection);

        if (plot.getRangeAxis() != null) {
            plot.getRangeAxis(0).setRange(new Range(minParam, maxParam));
        }
        if (plot.getDomainAxis() != null) {
            plot.getDomainAxis().setRange(new Range(obs.getTime(0).getTime(), obs.getTime(T - 1).getTime()));
        }
        if (plot.getRangeAxis(1) != null) {
            plot.getRangeAxis(1).setRange(new Range(obs_min, obs_max));
        }

    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
