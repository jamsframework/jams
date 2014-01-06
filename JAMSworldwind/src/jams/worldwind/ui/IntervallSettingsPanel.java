package jams.worldwind.ui;

import jams.data.JAMSCalendar;
import jams.worldwind.data.DataTransfer3D;
import jams.worldwind.data.IntervallCalculation;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author Ronny Berndt <ronny.berndt at uni-jena.de>
 */
public class IntervallSettingsPanel extends JPanel implements PropertyChangeListener {

    private final DataTransfer3D dataValues;
    private JSpinner numClassesSpinner;
    private JComboBox<String> classifierComboBox;
    private JFormattedTextField widthTextField;
    private double intervallWidth = Double.NaN;
    private JComboBox<String> attributeNameComboBox;
    private final String[] attribs;
    private SummaryStatisticsPanel summaryStatisticsPanel;
    private JList<Double> breakPoints;
    private JCheckBox includeMaxCountingValue;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private JButton calculateButton;
    private JButton applyButton;

    public IntervallSettingsPanel(DataTransfer3D dataValues, String[] attribs) {
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

        this.summaryStatisticsPanel = new SummaryStatisticsPanel();

        /*
         DefaultListModel<Double> listModel = new DefaultListModel<>();
         listModel.addElement(1.0);
         listModel.addElement(5.0);
         listModel.addElement(10.0);
         listModel.addElement(15.0);
         listModel.addElement(20.0);
         this.breakPoints = new JList<>(listModel);*/
        this.breakPoints = new JList<>();
        this.breakPoints.setBackground(this.getBackground());
        this.breakPoints.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public int getHorizontalAlignment() {
                return RIGHT;
            }
        });
        this.breakPoints.setBorder(new TitledBorder("Intervall breakpoints"));

        this.includeMaxCountingValue = new JCheckBox("Include Value (n/a) with most occurrences in Histogramm");
        this.includeMaxCountingValue.setEnabled(false);
        this.includeMaxCountingValue.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                includeMaxCountingValueActionListener(e);
            }
        });
        
        //HistogramDataset dataSet = new HistogramDataset();
        //dataSet.addSeries("HISTOGRAM", new double[]{0.0,10.0}, 1000);
        //chart = ChartFactory.createHistogram("Histogram", null, null, dataSet, PlotOrientation.VERTICAL, false, true, false);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        this.calculateButton = new JButton("CALCULATE");
        this.applyButton = new JButton("APPLY");

        this.calculateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                calculateButtonActionListener(e);
            }
        });

        this.addComponent(this, gbl, numClassesLabel, 0, 0, 1, 1, 0, 0);
        this.addComponent(this, gbl, this.numClassesSpinner, 1, 0, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, classifierLabel, 0, 1, 1, 1, 0, 0);
        this.addComponent(this, gbl, this.classifierComboBox, 1, 1, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, widthLabel, 0, 2, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, widthTextField, 1, 2, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, attributeLabel, 0, 3, 1, 1, 0, 0);
        this.addComponent(this, gbl, this.attributeNameComboBox, 1, 3, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.summaryStatisticsPanel, 0, 4, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.breakPoints, 1, 4, 1, 1, 1.0, 0);
        this.addComponent(this, gbl, this.includeMaxCountingValue, 0, 5, 2, 1, 1.0, 0.0);
        this.addComponent(this, gbl, chartPanel, 0, 6, 2, 1, 1.0, 1.0);
        this.addComponent(this, gbl, calculateButton, 0, 7, 1, 1, 1.0, 1.0);
        this.addComponent(this, gbl, applyButton, 1, 7, 1, 1, 1.0, 1.0);

    }

    private double[] convertToDoublePrimitiv(List<Double> list) {
        double[] values = new double[list.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = (double) list.get(i);
        }
        return values;
    }

    private IntervalXYDataset createHistogramDataSet(List<Double> list) {
        HistogramDataset dataSet = new HistogramDataset();
        dataSet.setType(HistogramType.FREQUENCY);
        System.out.println("MIN:"+this.summaryStatisticsPanel.getMin());
        System.out.println("MAX:"+ this.summaryStatisticsPanel.getMax());
        
        double cubeRootofSize = Math.pow(list.size(),1.0/3.0);
        System.out.println("3-Root:" + cubeRootofSize);
        double inverse = 1.0/cubeRootofSize;
        System.out.println("Inverse:" + inverse);
        DescriptiveStatistics s = this.summaryStatisticsPanel.getStatistics();
        double val = inverse*s.getStandardDeviation()*3.49;
        System.out.println("Value: "+val);
        
        int binwidth = Math.round(((float)s.getMax()-(float)s.getMin())/(float)val);
        System.out.println("binwidth: "+binwidth);
        
        
        
        
        dataSet.addSeries("HISTOGRAM", convertToDoublePrimitiv(list), list.size(), this.summaryStatisticsPanel.getMin(), this.summaryStatisticsPanel.getMax());
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

    private void printHistogramm(List<Double> values) {

        chart = ChartFactory.createHistogram("Histogram", null, null, createHistogramDataSet(values), PlotOrientation.VERTICAL, false, true, false);
        chartPanel.setChart(chart);

        //System.out.println("DATA RANGE: " + chart.getXYPlot().getDataRange(null).toString());
        //System.out.println("DATA RANGE: " + chart.getXYPlot().getDomain(null).toString());

        XYPlot xyPlot = chart.getXYPlot();

        NumberAxis range = (NumberAxis) xyPlot.getRangeAxis();
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        //chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        //Marker
        /*final Marker currentEnd = new ValueMarker(5.5);
         currentEnd.setPaint(Color.black);
         currentEnd.setLabel("Test Marker");
         currentEnd.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
         currentEnd.setLabelTextAnchor(TextAnchor.TOP_LEFT);
         xyPlot.addDomainMarker(currentEnd);
         */
    }

    //<editor-fold defaultstate="collapsed" desc="ActionListener">
    private void calculateButtonActionListener(ActionEvent e) {

        String attribute = (String) this.attributeNameComboBox.getSelectedItem();
        int numberOfClasses = (Integer) this.numClassesSpinner.getValue();
        String intervallSelection = (String) this.classifierComboBox.getSelectedItem();

        JAMSCalendar[] dates = this.dataValues.getSortedTimeSteps();
        String[] ids = this.dataValues.getSortedIds();

        ArrayList<Double> values = new ArrayList<>(dates.length * ids.length);

        int numZeros = 0;
        for (String id : ids) {
            for (JAMSCalendar d : dates) {
                double value = this.dataValues.getValue(id, attribute, d);
                    values.add(value);
                //System.out.println(this.dataValues.getValue(id, attribute, d));
            }
        }
        //System.out.println("Zeros in DataSet: " + numZeros);

        this.summaryStatisticsPanel.calculateStatistics(values);
        
        this.printHistogramm(values);

        IntervallCalculation iCalculation = new IntervallCalculation(values);

        ArrayList<Double> sortedValues = new ArrayList<>(iCalculation.getValues());
        Collections.sort(sortedValues);
        
        int index=0;
        for(int i=0;i<sortedValues.size();i++) {
            if(sortedValues.get(i)>0.0) {
                index=i;
                break;
            }
        }
        
        ArrayList<Double> sortedValuesNonZero = new ArrayList<>(sortedValues.subList(index, sortedValues.size()-1));
        //this.printHistogramm(sortedValuesNonZero);
        

        List<Double> intervall = new ArrayList<>(numberOfClasses);
        switch (intervallSelection) {
            case "Equal Intervall":
                intervall = iCalculation.getEqualIntervall(numberOfClasses);
                iCalculation.printHistogramm(intervall);
                break;
            case "Defined Intervall":
                System.out.println(this.intervallWidth);
                intervall = iCalculation.getDefinedIntervall(this.intervallWidth);
                iCalculation.printHistogramm(intervall);
                break;
            case "Quantil":
                intervall = iCalculation.getQuantilIntervall(numberOfClasses);
                iCalculation.printHistogramm(intervall);
                break;
            default:
                break;
        }

        System.out.println("Intervall:" + intervall);
        this.includeMaxCountingValue.setEnabled(true);


    }

    private void classifierComboBoxActionListener(ActionEvent e) {
        if (((JComboBox) e.getSource()).getSelectedItem().equals("Defined Intervall")) {
            this.widthTextField.setEnabled(true);
        } else {
            this.widthTextField.setEnabled(false);
            this.widthTextField.setBackground(this.getBackground());
        }
    }
    
    private void includeMaxCountingValueActionListener(ActionEvent e) {
        if(chart!=null) {
            XYDataset dataset = chart.getXYPlot().getDataset();
            Range range = DatasetUtilities.findRangeBounds(dataset);
            double v = 0.0,k =0.0;
            for(int i=0;i<dataset.getItemCount(0);i++) {
                v = dataset.getYValue(0, i);
                k = dataset.getXValue(0, i);
                //System.out.println(k + "|" + v);
                if(v==range.getUpperBound()) {
                    break;
                }
            }
            //System.out.println("MIN:" + range.getLowerBound() + " MAX:" + range.getUpperBound());
            this.includeMaxCountingValue.setText("Include Value ("+k+"|"+v+") with most occurrences in Histogram");
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
