/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.explorer.spreadsheet;

import jams.gui.WorkerDlg;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
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
    
    public JAMSStackedPlot(XYPlot[] xyplots, int[] weights, DateAxis timeAxis, String title){
        //final JFreeChart chart = createStackedChart(xyplots, DateAxis);
        int no_of_plots = xyplots.length;
        this.title = title;
        CombinedDomainXYPlot parentplot = new CombinedDomainXYPlot(timeAxis); //final?
        
        parentplot.setGap(10.0);
        
        //add subplots
        for(int i = 0; i< no_of_plots; i++){
//            parentplot.add(xyplots[i], xyplots[i].getWeight());
            parentplot.add(xyplots[i], weights[i]);
        }
        parentplot.setOrientation(PlotOrientation.VERTICAL);
        
        //create JFreeChart
        chart = new JFreeChart(this.title, JFreeChart.DEFAULT_TITLE_FONT, parentplot, true);
        chartpanel = new ChartPanel(chart, true, true, true, false, true);
        
        chartpanel.setBackground(Color.WHITE);
        
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

                  }catch(IOException fnfe){fnfe.printStackTrace();}
          
        
   } 
    
    public void setTitle(String title){
        this.title = title;
        chart.setTitle(title);
    }
    
   
    
}
