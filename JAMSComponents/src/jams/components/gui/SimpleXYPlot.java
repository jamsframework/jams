/*
 * XYPlot.java
 * Created on 12. October 2006, 22:06
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */

package jams.components.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.*;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepAreaRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.unijena.jams.data.JAMSBoolean;
import org.unijena.jams.data.JAMSCalendar;
import org.unijena.jams.data.JAMSDouble;
import org.unijena.jams.data.JAMSInteger;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.data.JAMSStringArray;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSVarDescription;
import org.jfree.data.xy.*;

/**
 *
 * @author S. Kralisch
 */
public class SimpleXYPlot extends JAMSGUIComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for plot"
            )
            public JAMSString plotTitle;                              
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for x axis"
            )
            public JAMSString xAxisTitle;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Title string for x axis"
            )
            public JAMSString yAxisTitle;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Title string for x axis"
            )
            public JAMSDouble xValue;
  
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Title string for x axis"
            )
            public JAMSDouble yValue;
                                   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Flag for disabling or enabling the plot"
            )
            public JAMSBoolean paint;
    
    private XYPlot plot;
    private XYSeries dataset;
    private JPanel panel;
    JFreeChart chart;
    JButton saveButton;
    int i, graphCountLeft = 0, graphCountRight = 0;
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    public SimpleXYPlot() {
        colorTable.put("yellow", Color.yellow);
        colorTable.put("orange", Color.orange);
        colorTable.put("red", Color.red);
        colorTable.put("pink", Color.pink);
        colorTable.put("magenta", Color.magenta);
        colorTable.put("cyan", Color.cyan);
        colorTable.put("blue", Color.blue);
        colorTable.put("green", Color.green);
        colorTable.put("gray", Color.gray);
        colorTable.put("lightgray", Color.lightGray);
        colorTable.put("black", Color.black);
	
	panel = null;		
    }
    
    private JPanel CreatePanel() {	
	plot = new XYPlot();
	plot.setDomainAxis(new NumberAxis(xAxisTitle.getValue()));
	plot.setRangeAxis(new NumberAxis(yAxisTitle.getValue()));

	chart = new JFreeChart(plot);
        
	dataset = new XYSeries(plotTitle.getValue());
                
	plot.setDataset(0, new XYSeriesCollection(dataset));
	XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);        
	plot.setRenderer(0, renderer);

	ChartPanel chartPanel = new ChartPanel(chart, true);

	panel = new JPanel(new BorderLayout());		    		    	    
	panel.add(chartPanel, BorderLayout.CENTER);	   
	    
	return panel;
    }
    
    public JPanel getPanel() {  
	if(this.paint == null || this.paint.getValue()) {	   	
	    return CreatePanel();
	}
	return null;
    }
              
    public void init() {
    	
    }
    
    public void run() {
    	if(this.paint == null || this.paint.getValue()){
	    dataset.add(this.xValue.getValue(),this.yValue.getValue());
    	}
    }
    
    public void cleanup() {
//        saveButton.setEnabled(true);
    }
    
}