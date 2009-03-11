/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.spreadsheet;

import java.awt.Rectangle;
import java.io.File;
import java.io.OutputStream;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
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
        
    }
    
    public ChartPanel getChartPanel(){
        return chartpanel;
    }
    
    public void saveAsEPS(File outfile){
        
     try{ 
        
      OutputStream out = new java.io.FileOutputStream(outfile);
      EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
      g2d.setGraphicContext(new org.apache.xmlgraphics.java2d.GraphicContext());
      int width = 600;
      int height = 400;
      g2d.setupDocument(out, width, height); //400pt x 200pt
      this.chart.draw(g2d,new Rectangle(width,height));
      g2d.finish();
      out.flush();
      out.close();
      
      }catch(Exception fnfe){}
   } 
    
    public void setTitle(String title){
        this.title = title;
        chart.setTitle(title);
    }
    
   
    
}
