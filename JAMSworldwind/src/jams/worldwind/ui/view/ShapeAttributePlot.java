/*
 * ShapeAttributePlot.java
 * Created on 03.06.2016, 08:39:44
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.worldwind.ui.view;

import jams.data.JAMSCalendar;
import jams.worldwind.data.DataTransfer3D;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ShapeAttributePlot extends JDialog {
    
    TimeSeriesCollection dataset;
    JFreeChart chart;
    XYPlot plot;
    XYItemRenderer leftRenderer;
    TimeSeries[] tsLeft;
    DataTransfer3D dataTransfer;
    Color colors[] = {Color.blue, Color.red, Color.green, Color.magenta, Color.pink, Color.cyan, Color.orange, Color.gray, Color.black, Color.yellow};
    String title, xTitle = "Time", yTitle = "Values", id;
    ShapeAttributeView owner;
    
    public ShapeAttributePlot(ShapeAttributeView owner, String title, String id, DataTransfer3D dataTransfer) {
        super(owner, title);
        this.owner = owner;
        this.title = title;
        this.dataTransfer = dataTransfer;
        this.id = id;
        init();
        
    }
    
    private void init() {

//        this.setAlwaysOnTop(true);
        this.setLocationRelativeTo(owner);
        
        dataset = new TimeSeriesCollection();
        
        chart = ChartFactory.createTimeSeriesChart(null,
                xTitle,
                yTitle,
                dataset,
                true,
                true,
                false);
        
        ChartPanel chartPanel = new ChartPanel(chart, true);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                owner.shapePlot = null;
                dispose();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        getContentPane().add(panel);
        
        plot = chart.getXYPlot();
        
        chart.getPlot().setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        
        chart.getLegend().setMargin(0, 10, 10, 10);
        
        DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
        
        XYLineAndShapeRenderer lsr = new XYLineAndShapeRenderer();
        lsr.setBaseShapesVisible(false);
        
        leftRenderer = lsr;
        plot.setRenderer(0, leftRenderer);
        
        String attributeNames[] = dataTransfer.getSortedAttributes();
        int graphCountLeft = attributeNames.length;
        tsLeft = new TimeSeries[graphCountLeft];
        for (int i = 0; i < graphCountLeft; i++) {
            leftRenderer.setSeriesPaint(i, colors[i]);
            tsLeft[i] = new TimeSeries(attributeNames[i]);
            dataset.addSeries(tsLeft[i]);
        }
        
        for (JAMSCalendar date : dataTransfer.getSortedTimeSteps()) {
            
            Second second = new Second(date.getTime());
            
            int i = 0;
            for (String attribute : attributeNames) {
                double dataValue = dataTransfer.getValue(id, attribute, date);
                tsLeft[i].add(second, dataValue);
                i++;
            }
        }
        
        this.pack();
    }
    
}
