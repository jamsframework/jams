/*
 * TSPlot.java
 * Created on 21. Juni 2006, 22:06
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.gui;

import java.awt.BorderLayout;
import jams.JAMS;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
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
import jams.data.*;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSGUIComponent;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import org.jfree.data.time.TimeSeriesDataItem;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "Timeseries plot",
        author = "Sven Kralisch",
        date = "2013-04-26",
        description = "This component creates a graphical plot of time series data, "
        + "e.g. precipitation and runoff over time.",
        version = "1.0_1")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Changed default cache size")
})
public class TSPlot extends JAMSGUIComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for plot. Default: component name")
    public Attribute.String plotTitle;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Static title strings for left graphs. Number of entries "
            + "must be identical to number of plottet values (valueLeft).",
            defaultValue="titleLeft")
    public Attribute.StringArray titleLeft;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Dynamic addon title strings added after left static titles (titleLeft)")
    public Attribute.StringArray varTitleLeft;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Static title strings for right graphs",
            defaultValue = "titleRight")
    public Attribute.StringArray titleRight;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Colors for left graphs (yellow, orange, red, pink, "
            + "magenta, cyan, yellow, green, lightgray, gray, black). Number of "
            + "entries must be identical to number of plottet values (valueLeft).",
            defaultValue = "blue;red")
    public Attribute.StringArray colorLeft;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Colors for right graphs (yellow, orange, red, pink, "
            + "magenta, cyan, yellow, green, lightgray, gray, black). Number of "
            + "entries must be identical to number of plottet values (valueRight).",
            defaultValue = "red")
    public Attribute.StringArray colorRight;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Graph type for left y axis graphs",
            defaultValue = "0")
    public Attribute.Integer typeLeft;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Graph type for right y axis graphs",
            defaultValue = "0")
    public Attribute.Integer typeRight;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for x axis",
            defaultValue = "Time")
    public Attribute.String xAxisTitle;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for left y axis",
            defaultValue = "LeftTitle")
    public Attribute.String leftAxisTitle;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for right y axis",
            defaultValue = "RightTitle")
    public Attribute.String rightAxisTitle;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            defaultValue = "0",
            description = "Paint inverted right y axis?")
    public Attribute.Boolean rightAxisInverted;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Date format",
            defaultValue = "dd-MM-yyyy")
    public Attribute.String dateFormat;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Values to be plotted on left x-axis")
    public Attribute.Double[] valueLeft;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Values to be plotted on right x-axis")
    public Attribute.Double[] valueRight;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Value for \"No data\" (shouldn't be plotted)",
            defaultValue = "-9999")
    public Attribute.Double noDataValue;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Plot data, after cacheSize values have been collected",
            defaultValue = "1")
    public Attribute.Integer cacheSize;
    TimeSeries[] tsLeft, tsRight;
    transient TimeSeriesCollection dataset1, dataset2;
    transient XYItemRenderer rightRenderer, leftRenderer;
    transient XYPlot plot;
    transient JFreeChart chart;    
    int graphCountLeft = 0, graphCountRight = 0;
    
    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    
    long[] timeStamps;
    double[] dataValuesLeft;
    double[] dataValuesRight;
    int count;

    public TSPlot() {
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
    }

    @Override
    public JPanel getPanel() {

        dataset1 = new TimeSeriesCollection();
        dataset2 = new TimeSeriesCollection();

        String title = getInstanceName();
        if (this.plotTitle != null){
            title = plotTitle.getValue();
        }
        
        chart = ChartFactory.createTimeSeriesChart(
                title,
                xAxisTitle.getValue(),
                leftAxisTitle.getValue(),
                dataset1,
                true,
                true,
                false);

        ChartPanel chartPanel = new ChartPanel(chart, true);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private XYItemRenderer getRenderer(int type) {        
        switch (type) {
            case 0:
                XYLineAndShapeRenderer lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(false);
                return lsr;
            case 1:
                return new XYBarRenderer();

            case 2:
                return new XYAreaRenderer();

            case 3:
                lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(true);
                return lsr;

            case 4:
                XYDotRenderer dotR = new XYDotRenderer();
                dotR.setDefaultEntityRadius(2);
                return dotR;

            case 5:
                return new XYDifferenceRenderer();
                
            case 6:
                return new XYStepRenderer();
                
            case 7:
                return new XYStepAreaRenderer();
                
            default:
                lsr = new XYLineAndShapeRenderer();
                lsr.setBaseShapesVisible(false);
                return lsr;
        }
    }

    @Override
    public void init() {

        if (dataset1 != null) {
            dataset1.removeAllSeries();
        }
        if (dataset2 != null) {
            dataset2.removeAllSeries();
        }

        if (chart != null) {
            plot = chart.getXYPlot();

            DateAxis dateAxis = (DateAxis) plot.getDomainAxis();
            dateAxis.setDateFormatOverride(new SimpleDateFormat(dateFormat.getValue()));

            leftRenderer = getRenderer(typeLeft.getValue());
            plot.setRenderer(0, leftRenderer);

            if (valueLeft == null) {
                getModel().getRuntime().sendErrorMsg(JAMS.i18n("no_value_for_time_series_plot"));
                return;
            }
            graphCountLeft = valueLeft.length;
            tsLeft = new TimeSeries[graphCountLeft];
            for (int i = 0; i < graphCountLeft; i++) {
                String legendEntry = "";
                if (titleLeft != null && titleLeft.getValue().length >= i){
                    legendEntry = titleLeft.getValue()[i];
                }
                if (this.varTitleLeft != null && this.varTitleLeft.getValue().length > i) {
                    legendEntry += varTitleLeft.getValue()[i];//getModel().getRuntime().getDataHandles().get(varTitleLeft.getValue()[i]);
                }
                if (colorLeft != null && colorLeft.getValue().length>i){
                    leftRenderer.setSeriesPaint(i, colorTable.get(colorLeft.getValue()[i]));
                }
                
                tsLeft[i] = new TimeSeries(legendEntry, Second.class);
                dataset1.addSeries(tsLeft[i]);
            }

            if (valueRight != null) {
                ValueAxis axis2 = new NumberAxis(rightAxisTitle.getValue());
                axis2.setInverted(rightAxisInverted.getValue());
                plot.setRangeAxis(1, axis2);
                plot.setDataset(1, dataset2);
                plot.mapDatasetToRangeAxis(1, 1);

                rightRenderer = getRenderer(typeRight.getValue());
                plot.setRenderer(1, rightRenderer);

                plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

                graphCountRight = valueRight.length;
                tsRight = new TimeSeries[graphCountRight];
                for (int i = 0; i < graphCountRight; i++) {
                    if (colorRight != null && colorRight.getValue().length >= i)
                        rightRenderer.setSeriesPaint(i, colorTable.get(colorRight.getValue()[i]));
                    String title = "";
                    if (titleRight != null && titleRight.getValue().length > i){
                        title = titleRight.getValue()[i];
                    }
                    tsRight[i] = new TimeSeries(title, Second.class);
                    dataset2.addSeries(tsRight[i]);
                }
            }
        }

        int cacheSize = this.cacheSize.getValue();
        timeStamps = new long[cacheSize];
        dataValuesRight = new double[cacheSize * graphCountRight];
        dataValuesLeft = new double[cacheSize * graphCountLeft];
        count = 0;
    }

    @Override
    public void run() {
        if (time == null) {
            getModel().getRuntime().sendErrorMsg(JAMS.i18n("no_time_value_was_provided_for_time_series_plot"));
        }
        timeStamps[count] = time.getTimeInMillis();
        int offsetRight = count * graphCountRight;
        int offsetLeft = count * graphCountLeft;

        for (int i = 0; i < graphCountRight; i++) {
            double value = valueRight[i].getValue();
            if (value == noDataValue.getValue()) {
                value = Double.NaN;
            }
            dataValuesRight[offsetRight + i] = value;
        }

        for (int i = 0; i < graphCountLeft; i++) {
            double value = valueLeft[i].getValue();
            if (value == noDataValue.getValue()) {
                value = Double.NaN;
            }
            dataValuesLeft[offsetLeft + i] = value;
        }

        if (++count == this.cacheSize.getValue()) {
            plotData();
            count = 0;
        } 
    }

    private void plotData() {
        try {
            for (int i = 0; i < count; i++) {
                Second second = new Second(new Date(timeStamps[i]));
                for (int j = 0; j < graphCountRight; j++) {
                    tsRight[j].add(second, dataValuesRight[i * graphCountRight + j]);
                }
                for (int j = 0; j < graphCountLeft; j++) {
                    tsLeft[j].add(second, dataValuesLeft[i * graphCountLeft + j]);
                }
            }

        } catch (Exception e) {
        } //caused by bugs in JFreeChart
    }

    @Override
    public void cleanup() {
        plotData();
    }

    @Override
    public void restore() {

        List leftLists[] = null, rightLists[] = null;
        if (tsLeft != null) {
            leftLists = new List[tsLeft.length];
            for (int i = 0; i < tsLeft.length; i++) {
                leftLists[i] = this.tsLeft[i].getItems();
            }
        }
        if (tsRight != null) {
            rightLists = new List[tsRight.length];

            for (int i = 0; i < tsRight.length; i++) {
                rightLists[i] = this.tsRight[i].getItems();
            }
        }
        this.init();
        if (tsLeft != null) {
            for (int i = 0; i < tsLeft.length; i++) {
                Iterator iter = leftLists[i].iterator();
                while (iter.hasNext()) {
                    this.tsLeft[i].add((TimeSeriesDataItem) iter.next());
                }
            }
        }
        if (tsRight != null) {
            for (int i = 0; i < tsRight.length; i++) {
                Iterator iter = rightLists[i].iterator();
                while (iter.hasNext()) {
                    this.tsRight[i].add((TimeSeriesDataItem) iter.next());
                }
            }
        }
    }

    private void readObject(ObjectInputStream objIn) throws IOException, ClassNotFoundException {
        objIn.defaultReadObject();

        this.plotData();
    }

    private void writeObject(ObjectOutputStream objOut) throws IOException {
        this.plotData();
        this.count = 0;
        objOut.defaultWriteObject();
    }
}
