package jams.worldwind.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.time.Minute;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author Ronny Berndt <ronny.berndt@uni-jena.de>
 */
public class IntervallSettingsPanel extends JPanel {

    private final List dataValues;
    private JSpinner numClassesSpinner;
    private JComboBox<String> classifierComboBox;
    private JComboBox<String> attributeNameComboBox;
    private String[] attribs;
    private SummaryStatisticsPanel summaryStatisticsPanel;
    private JList<Double> breakPoints;
    private JButton calculateButton;
    private JButton applyButton;

    public IntervallSettingsPanel(List dataValues, String[] attribs) {
        this.dataValues = dataValues;
        this.attribs = attribs;
        this.createPanelGUI();
    }

    private void createPanelGUI() {
        GridBagLayout gbl = new GridBagLayout();
        this.setLayout(gbl);

        JLabel numClassesLabel = new JLabel("Number of Classes:");
        this.numClassesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 50, 1));

        JLabel classifierLabel = new JLabel("Classifier:");
        String[] items = {"Equal Intervall", "Defined Intervall", "Quantil"};
        this.classifierComboBox = new JComboBox(items);
        //this.classifierComboBox.setSelectedIndex(-1);

        JLabel attributeLabel = new JLabel("Attribute:");
        this.attributeNameComboBox = new JComboBox<>(this.attribs);

        this.summaryStatisticsPanel = new SummaryStatisticsPanel(this.dataValues);

        DefaultListModel<Double> listModel = new DefaultListModel<>();
        listModel.addElement(1.0);
        listModel.addElement(5.0);
        listModel.addElement(10.0);
        listModel.addElement(15.0);
        listModel.addElement(20.0);
        this.breakPoints = new JList<>(listModel);
        this.breakPoints.setBackground(this.getBackground());
        this.breakPoints.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return RIGHT;
            }
        });
        this.breakPoints.setBorder(new TitledBorder("Intervall breakpoints"));

        final JFreeChart chart = ChartFactory.createHistogram("Histogram", null, null, this.createHistogramDataSet(), PlotOrientation.VERTICAL, false, true, false);
        final ChartPanel chartPanel = new ChartPanel(chart);
        XYPlot xyPlot = chart.getXYPlot();

        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        //Marker
        final Marker currentEnd = new ValueMarker(50.5);
        currentEnd.setPaint(Color.black);
        currentEnd.setLabel("Test Marker");
        currentEnd.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        currentEnd.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        xyPlot.addDomainMarker(currentEnd);
        
        this.calculateButton = new JButton("CALCULATE");
        this.applyButton = new JButton("APPLY");
        

        this.addComponent(this, gbl, numClassesLabel,               0, 0, 1, 1, 0, 0);
        this.addComponent(this, gbl, this.numClassesSpinner,        1, 0, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, classifierLabel,               0, 1, 1, 1, 0, 0);
        this.addComponent(this, gbl, this.classifierComboBox,       1, 1, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, attributeLabel,                0, 2, 1, 1, 0, 0);
        this.addComponent(this, gbl, this.attributeNameComboBox,    1, 2, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.summaryStatisticsPanel,   0, 3, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.breakPoints,              1, 3, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, chartPanel,                    0, 4, 2, 1, 1.0, 1.0);
        this.addComponent(this, gbl, calculateButton,               0, 5, 1, 1, 1.0, 1.0);
        this.addComponent(this, gbl, applyButton,                   1, 5, 1, 1, 1.0, 1.0);
        

    }

    private double[] convertToDoublePrimitiv() {
        double[] values = new double[this.dataValues.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (double) this.dataValues.get(i);
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
