/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui.MCAT5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.GrayPaintScale;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import reg.gui.MCAT5Toolbar.ArrayComparator;
import reg.gui.MCAT5Toolbar.ObservationDataSet;
import reg.gui.MCAT5Toolbar.ParameterSet;
import reg.gui.MCAT5Toolbar.SimulationTimeSeriesDataSet;

/**
 *
 * @author Christian Fischer
 */
@SuppressWarnings({"unchecked"})
public class DYNIA {

    SimulationTimeSeriesDataSet timeserie = null;
    ParameterSet timeserie_param = null;
    ObservationDataSet observation = null;
        
    int window_size = 10;
    XYPlot plot = new XYPlot();    
    ChartPanel chartPanel = null;   
    
    JTextField winsize_box = new JTextField(10);
    final int BOX_COUNT = 10;        
    
    private double FindMinObservation(ObservationDataSet data) {
        int n = data.timeLength;
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            min = Math.min(min, data.set[i]);
        }
        return min;
    }

    private double FindMaxObservation(ObservationDataSet data) {
        int n = data.timeLength;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            max = Math.max(max, data.set[i]);
        }
        return max;
    }

    public DYNIA(SimulationTimeSeriesDataSet timeserie, ParameterSet param, ObservationDataSet observation) {
        this.timeserie = timeserie;
        this.observation = observation;
        this.timeserie_param = param;

        plot.setRangeAxis(new NumberAxis(timeserie_param.name));        
        plot.setDomainAxis(new NumberAxis("time"));
        plot.setRangeAxis(1,new NumberAxis(observation.name));
        plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setBaseShapesVisible(false);
        plot.setRenderer(0, renderer);
        
        JFreeChart chart = new JFreeChart(plot);
        chart.setTitle("DYNIA");
        
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setLayout(new BorderLayout());
        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.X_AXIS));
        
        winsize_box.setText("10");
        
        text.add(new JLabel("Window Size"));
        text.add(winsize_box);
        
        chartPanel.add(text, BorderLayout.SOUTH);        
        
        winsize_box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateData();
            }
        });

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
        String input = winsize_box.getText();        
        try{
            int number = Integer.parseInt(input);                
            if (number >= 1)
                window_size = number;
        }catch(Exception e){}
        XYSeries dataset = new XYSeries(observation.name);
                            
        int MCparam = timeserie.parent.numberOfRuns;
        int ts_length = timeserie.timeLength;        
        double pixel_map[][] = new double[3][ts_length*this.BOX_COUNT];
                
        double maxParam = Double.NEGATIVE_INFINITY,
               minParam = Double.POSITIVE_INFINITY;
        
        for (int j=0;j<MCparam;j++){
            maxParam = Math.max(maxParam, this.timeserie_param.set[j]);
            minParam = Math.min(minParam, this.timeserie_param.set[j]);
        }
        
        for (int i=0;i<ts_length;i++){
            double cur_obs[] = new double[2*window_size+1];
            double cur_sim[] = new double[2*window_size+1];
            double cur_eff[] = new double[MCparam];            
            
            //calculate efficiency
            for (int k=0;k<MCparam;k++){
                int counter = 0;
                for (int j=i-window_size;j<=i+window_size;j++){                
                    if (j < 0 || j >= ts_length)
                        cur_obs[counter] = cur_sim[counter] = 0;
                    else{
                        cur_obs[counter] = this.observation.set[j];
                        cur_sim[counter] = this.timeserie.set[j].set[k];
                    }
                    counter++;
                }
                cur_eff[k] = Efficiencies.CalculateE(cur_obs,cur_sim,2);
            }
            //transform to likelihood
            double cur_likelihood[] = Efficiencies.CalculateLikelihood(cur_eff);
            //and sort it
            double sortedData[][] = sortbyEff(timeserie_param.set,cur_likelihood);
            double limit = sortedData.length * 0.1;
            double best[][] = new double[(int) limit][];
            
            double boxes[] = new double[BOX_COUNT];
            
            for (int j = 0; j < (int)limit; j++) {
                best[j] = sortedData[j];
            }            
            Arrays.sort(best, new ArrayComparator(0, false));

            
            for (int j = 0; j < (int)limit; j++) {            
                int index = (int) Math.round((best[j][0] - minParam) / (maxParam - minParam) * (boxes.length - 1));
                boxes[index] += 1.0 / limit;
            }
            
            for (int j = 0; j < BOX_COUNT; j++) {
                pixel_map[0][i*BOX_COUNT+j] = i;
                pixel_map[1][i*BOX_COUNT+j] = minParam + (maxParam-minParam)*(double)j/(double)BOX_COUNT;
                pixel_map[2][i*BOX_COUNT+j] = 1.0 - boxes[j];
            }
        }

        XYBlockRenderer bg_renderer = new XYBlockRenderer();
        bg_renderer.setPaintScale(new GrayPaintScale(0,1));
        bg_renderer.setBlockHeight((maxParam-minParam)/BOX_COUNT);
        bg_renderer.setBlockWidth(1.00);
        bg_renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        DefaultXYZDataset xyz_dataset = new DefaultXYZDataset();                
        xyz_dataset.addSeries(0, pixel_map);                
        plot.setDataset(1, xyz_dataset);
        plot.setRenderer(1, bg_renderer);       
        bg_renderer.setSeriesVisibleInLegend(0, false);
        //at last plot observed data                
        double obs_min = this.FindMinObservation(this.observation);
        double obs_max = this.FindMaxObservation(this.observation);
        
        for(int i = 0; i < ts_length; i++) {
            dataset.add(i,((observation.set[i]-obs_min)/(obs_max-obs_min))*(maxParam-minParam)+minParam);
        }        
        plot.setDataset(0, new XYSeriesCollection(dataset));
                
        if (plot.getRangeAxis() != null) plot.getRangeAxis().setRange(new Range(minParam,maxParam));
        if (plot.getDomainAxis() != null)plot.getDomainAxis().setRange(new Range(0,ts_length));
       
    }

    public JPanel getPanel() {
        return chartPanel;
    }
}
