package jams.worldwind.ui;

import jams.data.JAMSCalendar;
import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.data.IntervallCalculation;
import jams.worldwind.events.Events;
import jams.worldwind.ui.view.GlobeView;
import jams.worldwind.ui.view.IntervallSettingsView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class IntervallSettingsPanel extends JPanel implements PropertyChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(IntervallSettingsPanel.class);
    
    private final IntervallSettingsView frame;
    private final int NUMBER_OF_BINS_FOR_HISTOGRAM = 100;
    
    
    private final DataTransfer3D dataValues;
    private JSpinner numClassesSpinner;
    private JComboBox<String> classifierComboBox;
    private JFormattedTextField widthTextField;
    private double intervallWidth = 0.0;
    private JComboBox<String> attributeNameComboBox;
    private final String[] attribs;
    private SummaryStatisticsPanel summaryStatisticsPanel;
    private JList<Double> breakPoints;
    private List<Double> intervall;
    private ColorRampPanel colorPanel;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private JButton calculateButton;
    private JButton applyButton;
    
    private boolean intervallCalculatedAndColorsSet;

    public IntervallSettingsPanel(IntervallSettingsView frame, DataTransfer3D dataValues, String[] attribs) {
        this.frame = frame;
        this.dataValues = dataValues;
        this.attribs = attribs;
        this.intervallCalculatedAndColorsSet = false;
        this.createPanelGUI();
    }

    private void createPanelGUI() {
        
        GridBagLayout gbl = new GridBagLayout();
        this.setLayout(gbl);

        JLabel numClassesLabel = new JLabel("Number of Classes:");
        this.numClassesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        this.numClassesSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (colorPanel != null) {
                    colorPanel.setButtonsEnabled(false);
                }
            }
        });

        JLabel classifierLabel = new JLabel("Classifier:");
        String[] items = {"Quantil", "Equal Intervall", "Defined Intervall"};
        this.classifierComboBox = new JComboBox(items);
        this.classifierComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                classifierComboBoxActionListener(e);
            }
        });

        //this.classifierComboBox.setSelectedIndex(-1);
        JLabel widthLabel = new JLabel("Intervall width:");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(1);

        this.widthTextField = new JFormattedTextField(nf);
        this.widthTextField.setEditable(true);
        this.widthTextField.setEnabled(false);
        this.widthTextField.setHorizontalAlignment(JTextField.RIGHT);
        this.widthTextField.setBackground(this.getBackground());
        this.widthTextField.setValue(0);
        this.widthTextField.addPropertyChangeListener("value", this);

        JLabel attributeLabel = new JLabel("Attribute:");
        this.attributeNameComboBox = new JComboBox<>(this.attribs);
        this.attributeNameComboBox.setSelectedIndex(0);

        this.summaryStatisticsPanel = new SummaryStatisticsPanel();

        DefaultListModel<Double> listModel = new DefaultListModel<>();
        this.breakPoints = new JList<>(listModel);

        this.breakPoints = new JList<>();
        this.breakPoints.setBackground(this.getBackground());
        this.breakPoints.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return RIGHT;
            }
        });
        //this.breakPoints.setBorder(new TitledBorder("Intervall breakpoints"));
        JScrollPane breakPointsScrollPane = new JScrollPane(this.breakPoints);
        breakPointsScrollPane.setBackground(this.getBackground());
        breakPointsScrollPane.setBorder(new TitledBorder("Interval Breakpoints"));

        this.colorPanel = new ColorRampPanel();
        this.colorPanel.setBorder(new TitledBorder("Color Settings"));

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        this.calculateButton = new JButton("Calculate");
        this.applyButton = new JButton("Apply");
        this.applyButton.setEnabled(false);

        this.calculateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        calculateButtonActionListener();
                        return null;
                    }
                };
                worker.execute();
            }
        });

        this.applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                applyButtonActionListener(e);
            }

        });

        this.addComponent(this, gbl, numClassesLabel, 0, 0, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.numClassesSpinner, 1, 0, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, classifierLabel, 0, 1, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.classifierComboBox, 1, 1, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, widthLabel, 0, 2, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, widthTextField, 1, 2, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, attributeLabel, 0, 3, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.attributeNameComboBox, 1, 3, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.summaryStatisticsPanel, 0, 4, 1, 2, 1.0, 0);
        this.addComponent(this, gbl, breakPointsScrollPane, 1, 4, 1, 2, 1.0, 0);
        this.addComponent(this, gbl, this.colorPanel, 0, 6, 2, 1, 1.0, 0.0);
        this.addComponent(this, gbl, chartPanel, 0, 8, 2, 2, 1.0, 1.0);
        this.addComponent(this, gbl, calculateButton, 0, 10, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, applyButton, 1, 10, 1, 1, 1.0, 0);

    }
    
    /*
    private double[] convertToDoublePrimitiv(List<Double> list) {
        double[] values = new double[list.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (double) list.get(i);
        }
        return values;
    }
    */
    
    public void setIntervallAndColorRamp(List<Double> intervall, ColorRamp colorRamp) {
        this.intervall = intervall;
        this.colorPanel.setColorRamp(colorRamp);
        //recalculate statistics
        
        String attribute = (String) this.attributeNameComboBox.getSelectedItem();
        //int numberOfClasses = (Integer) this.numClassesSpinner.getValue();
        String intervallSelection = (String) this.classifierComboBox.getSelectedItem();

        JAMSCalendar[] dates = this.dataValues.getSortedTimeSteps();
        String[] ids = this.dataValues.getSortedIds();

        
        //ArrayList<Double> values = new ArrayList<>(dates.length * ids.length);
        double [] values = new double[dates.length * ids.length];
        
        int i = 0;
        for (String id : ids) {
            for (JAMSCalendar d : dates) {
                double value = this.dataValues.getValue(id, attribute, d);
                values[i] = value;
                i++;
            }
        }
        
        this.numClassesSpinner.setValue(new Integer(intervall.size()-1));
        
        this.summaryStatisticsPanel.calculateStatistics(values);
        this.printHistogramm(values);
        
        this.fillBreakpointList();
        this.setHistogramMarkers();
        colorPanel.setNumberOfColors((Integer) this.numClassesSpinner.getValue());
        
    }

    private IntervalXYDataset createHistogramDataSet(double[] list) {
        logger.info("Creating Histogram-Series...");
        HistogramDataset dataSet = new HistogramDataset();
        dataSet.setType(HistogramType.FREQUENCY);
        dataSet.addSeries("HISTOGRAM", list, this.NUMBER_OF_BINS_FOR_HISTOGRAM);
        logger.info("...Histogram-Series done.");
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

    private void printHistogramm(double[] values) {
        logger.info("Creating Histogram...");
        chart = ChartFactory.createHistogram("Histogram", null, null, createHistogramDataSet(values), PlotOrientation.VERTICAL, false, true, false);
        chartPanel.setChart(chart);
        XYPlot xyPlot = chart.getXYPlot();
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        DescriptiveStatistics stat = this.summaryStatisticsPanel.getStatistics();
        double range = (stat.getMax() - stat.getMin()) / 10;
        domainAxis.setRange(stat.getMin() - range, stat.getMax() + range);
        logger.info("...Histogram done");
    }

    //<editor-fold defaultstate="collapsed" desc="ActionListener">
    private void calculateButtonActionListener() {

        this.applyButton.setEnabled(false);
        logger.info("Starting Classification...");
        String attribute = (String) this.attributeNameComboBox.getSelectedItem();
        int numberOfClasses = (Integer) this.numClassesSpinner.getValue();
        String intervallSelection = (String) this.classifierComboBox.getSelectedItem();

        JAMSCalendar[] dates = this.dataValues.getSortedTimeSteps();
        String[] ids = this.dataValues.getSortedIds();

        //ArrayList<Double> values = new ArrayList<>(dates.length * ids.length);
        double [] values = new double[dates.length * ids.length];

        //String format = "%-20s%s%n";
        
        //System.out.println("VALUES:");
        int i=0;
        for (String id : ids) {
            for (JAMSCalendar d : dates) {
                double value = this.dataValues.getValue(id, attribute, d);
                //values.add(value);
                values[i]=value;
                i++;
                //System.out.printf(format, d.toString(), value);
            }
        }

        logger.info("Calculating statistics...");
        this.summaryStatisticsPanel.calculateStatistics(values);
        this.printHistogramm(values);

        logger.info("Calculating Intervall");
        IntervallCalculation iCalculation = new IntervallCalculation(values);
        
        this.intervall = new ArrayList<>(numberOfClasses);

        switch (this.classifierComboBox.getSelectedIndex()) {
            case 1:
                intervall = iCalculation.getEqualIntervall(numberOfClasses);
                //iCalculation.printHistogramm(intervall);
                break;
            case 2:
                intervall = iCalculation.getDefinedIntervall(this.intervallWidth);
                //iCalculation.printHistogramm(intervall);
                break;
            case 0:
                intervall = iCalculation.getQuantilIntervall(numberOfClasses);
                //iCalculation.printHistogramm(intervall);
                break;
            default:

                break;
        }
        
        if (intervall.size() > 0) {
            this.fillBreakpointList();
            this.setHistogramMarkers();
            colorPanel.setNumberOfColors((Integer) this.numClassesSpinner.getValue());
            colorPanel.repaint();   
        }
    }
    
    public int getSelectedAttributeIndex() {
        return (Integer)this.attributeNameComboBox.getSelectedIndex();
    }

    private void applyButtonActionListener(ActionEvent e) {
        GlobeView.getInstance().getPCS().firePropertyChange(Events.INTERVALL_CALCULATED, null, intervall);
        GlobeView.getInstance().getPCS().firePropertyChange(Events.INTERVALL_COLORS_SET, null, colorPanel.getColorRamp());
        this.frame.hide();
    }

    public void fillBreakpointList() {
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < this.intervall.size(); i++) {
            listModel.addElement(this.intervall.get(i));
        }
        this.breakPoints.setModel(listModel);
        this.applyButton.setEnabled(true);
    }

    private void setHistogramMarkers() {
        //Marker
        XYPlot xyPlot = chart.getXYPlot();
        Marker marker;
        for (int i = 1; i < this.intervall.size() - 1; i++) {
            marker = new ValueMarker(this.intervall.get(i));
            marker.setPaint(Color.black);
            marker.setLabel("I" + i);
            marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
            xyPlot.addDomainMarker(marker);
        }
    }

    private void classifierComboBoxActionListener(ActionEvent e) {
        if (((JComboBox) e.getSource()).getSelectedItem().equals("Defined Intervall")) {
            this.widthTextField.setEnabled(true);
        } else {
            this.widthTextField.setEnabled(false);
            this.widthTextField.setBackground(this.getBackground());
        }
    }

    //</editor-fold>
    /**
     * Called when a field's "value" property changes.
     *
     * @param e
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        Object source = e.getSource();
        if (source == widthTextField) {
            try {
                this.intervallWidth = ((Double) widthTextField.getValue()).doubleValue();
            } catch (NumberFormatException ex) {
                System.out.println(ex);
            }
        }

    }
}
