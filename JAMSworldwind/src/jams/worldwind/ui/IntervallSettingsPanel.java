package jams.worldwind.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class IntervallSettingsPanel extends JPanel {

    private final List dataValues;
    private JSpinner numClassesSpinner;
    private JComboBox classifierComboBox;
    private SummaryStatisticsPanel summaryStatisticsPanel;

    public IntervallSettingsPanel(List dataValues) {
        this.dataValues = dataValues;
        this.createPanelGUI();
    }

    private void createPanelGUI() {
        GridBagLayout gbl = new GridBagLayout();
        this.setLayout(gbl);

        JLabel label = new JLabel("Number of Classes:");
        this.addComponent(this, gbl, label, 0, 0, 1, 1, 0, 0);

        this.numClassesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));
        this.addComponent(this, gbl, this.numClassesSpinner, 1, 0, 1, 1, 1.0, 0);

        label = new JLabel("Classifier:");
        this.addComponent(this, gbl, label, 0, 1, 1, 1, 0, 0);

        String[] items = {"Equal Intervall", "Defined Intervall", "Quantil"};
        this.classifierComboBox = new JComboBox(items);
        this.classifierComboBox.setSelectedIndex(-1);
        this.addComponent(this, gbl, this.classifierComboBox, 1, 1, 1, 1, 1.0, 0);

        this.summaryStatisticsPanel = new SummaryStatisticsPanel(this.dataValues);
        this.addComponent(this, gbl, this.summaryStatisticsPanel, 0, 2, 2, 1, 1.0, 0);

        
        final JFreeChart chart = ChartFactory.createHistogram("Histogram", null, null, this.createHistogramDataSet(), PlotOrientation.VERTICAL, false, true, false);
        final ChartPanel chartPanel = new ChartPanel(chart);
        XYPlot xyPlot = chart.getXYPlot();
        
        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); 
        
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        
        this.addComponent(this, gbl, chartPanel, 0, 4, 2, 1, 1.0, 1.0);
        
    }
    
    private double[] convertToDoublePrimitiv() {
        double[] values = new double[this.dataValues.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (double)this.dataValues.get(i);
        }
        return values;
    }
    
    private IntervalXYDataset createHistogramDataSet() {
        HistogramDataset dataSet = new HistogramDataset();
        dataSet.addSeries("HISTOGRAM", this.convertToDoublePrimitiv(), 1000);
        return dataSet;
    }

    private void addComponent(Container container,
            GridBagLayout gbl,
            Component c,
            int x, int y,
            int width, int height,
            double weightx, double weighty) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbl.setConstraints(c, gbc);
        container.add(c);
    }
}
