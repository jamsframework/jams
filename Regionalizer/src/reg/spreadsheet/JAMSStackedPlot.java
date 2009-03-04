/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.spreadsheet;

import java.awt.Color;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
/**
 *
 * @author Developement
 */
public class JAMSStackedPlot {
    
    JFreeChart chart;
    ChartPanel chartpanel;
    String title;
    
    public JAMSStackedPlot(XYPlot[] xyplots, DateAxis timeAxis, String title){
        //final JFreeChart chart = createStackedChart(xyplots, DateAxis);
        int no_of_plots = xyplots.length;
        this.title = title;
        final CombinedDomainXYPlot parentplot = new CombinedDomainXYPlot(timeAxis);
        
        parentplot.setGap(10.0);
        
        //add subplots
        for(int i = 0; i< no_of_plots; i++){
            parentplot.add(xyplots[i], 1);
        }
        parentplot.setOrientation(PlotOrientation.VERTICAL);
        
        //create JFreeChart
        chart = new JFreeChart(this.title, JFreeChart.DEFAULT_TITLE_FONT, parentplot, true);
        chartpanel = new ChartPanel(chart, true, true, true, false, true);
        chartpanel.setBackground(Color.white);
        
    }
    
    public ChartPanel getChartPanel(){
        return chartpanel;
    }
    
    public void setTitle(String title){
        this.title = title;
        chart.setTitle(title);
    }
    
   
    
}
