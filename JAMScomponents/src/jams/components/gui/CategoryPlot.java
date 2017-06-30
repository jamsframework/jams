/*
 * CategoryPlot.java
 * Created on 30.06.2017, 14:59:33
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
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import jams.data.*;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSGUIComponent;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(title = "Category plot",
        author = "Sven Kralisch",
        date = "2017-06-30",
        description = "This component creates a graphical plot of category data, "
        + "e.g. to compare attributes of different model entities.",
        version = "1.0_0")
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", date = "2017-06-30", comment = "Initial version")
})
public class CategoryPlot extends JAMSGUIComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for plot. Default: component name")
    public Attribute.String plotTitle;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Static title strings for graphs. Number of entries "
            + "must be identical to number of plottet values.",
            defaultValue = "title")
    public Attribute.StringArray graphTitle;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Colors for left graphs (yellow, orange, red, pink, "
            + "magenta, cyan, yellow, green, lightgray, gray, black). Number of "
            + "entries must be identical to number of plottet values (valueLeft).",
            defaultValue = "blue;red")
    public Attribute.StringArray colors;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for x axis",
            defaultValue = "xAxisTitle")
    public Attribute.String xAxisTitle;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Title string for y-axis",
            defaultValue = "yAxisTitle")
    public Attribute.String yAxisTitle;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Values to be plotted on y-axis")
    public Attribute.Double[] values;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Paint horizontal/vertical grid lines?",
            defaultValue = "true")
    public Attribute.Boolean paintGridLines;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Paint legend right of the plot?",
            defaultValue = "false")
    public Attribute.Boolean legendRight;

    HashMap<String, Color> colorTable = new HashMap<String, Color>();
    transient CategoryDataset dataset;
    transient JFreeChart chart;

    public CategoryPlot() {
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

        dataset = new DefaultCategoryDataset();

        String title = getInstanceName();
        if (this.plotTitle != null) {
            title = plotTitle.getValue();
        }

        chart = ChartFactory.createBarChart(
                title,
                xAxisTitle.getValue(),
                yAxisTitle.getValue(),
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(chart, true);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    @Override
    public void init() {

    }

    @Override
    public void run() {
        dataset = createDataset();
    }

    @Override
    public void cleanup() {
    }

    private CategoryDataset createDataset() {
        final String fiat = "FIAT";
        final String audi = "AUDI";
        final String ford = "FORD";
        final String speed = "Speed";
        final String millage = "Millage";
        final String userrating = "User Rating";
        final String safety = "safety";
        final DefaultCategoryDataset dataset
                = new DefaultCategoryDataset();

        dataset.addValue(1.0, fiat, speed);
        dataset.addValue(3.0, fiat, userrating);
        dataset.addValue(5.0, fiat, millage);
        dataset.addValue(5.0, fiat, safety);

        dataset.addValue(5.0, audi, speed);
        dataset.addValue(6.0, audi, userrating);
        dataset.addValue(10.0, audi, millage);
        dataset.addValue(4.0, audi, safety);

        dataset.addValue(4.0, ford, speed);
        dataset.addValue(2.0, ford, userrating);
        dataset.addValue(3.0, ford, millage);
        dataset.addValue(6.0, ford, safety);

        return dataset;
    }
}
