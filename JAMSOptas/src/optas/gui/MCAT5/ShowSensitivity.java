/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.MCAT5;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import optas.SA.VarianceBasedSensitivityIndex.Measure;
import optas.hydro.data.DataSet;
import optas.hydro.data.Efficiency;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.Parameter;
import optas.hydro.data.SimpleEnsemble;
import optas.regression.NeuralNetwork;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 *
 * @author chris
 */
public class ShowSensitivity extends MCAT5Plot {
    JPanel panel = null;

    JTextField sampleCountField = new JTextField(10);
    JTextField regressionErrorField = new JTextField(10);
    JTextField sampleCountFieldRegression = new JTextField("1000");
    JCheckBox doVarianceEstimation = new JCheckBox("Estimate Uncertainty of Sensitivity");

    JComboBox regressionMethod = new JComboBox(new String[]{"Neural Network"});

    String rsaString = "Regional Sensitivity Analysis",
           mgeString = "Maximum Gradient Estimation",
           eemString = "Elementary Effects Method",
           fosiString = "First Order Sensitivity by FAST",
           fosiString2 = "First Order Sensitivity by Satelli(2008)(-)",
           tosiString = "Total Sensitivity Index by Satelli(2008)(-)",
           linearRegString = "Linear Regression";



    JComboBox sensitivityMethod = new JComboBox(new String[]{rsaString, mgeString, eemString, fosiString,tosiString,linearRegString});

    CategoryDataset dataset1 = null, dataset2 = null;
    JFreeChart chart = null;

    public ShowSensitivity(){
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(java.util.ResourceBundle.getBundle("reg/resources/JADEBundle").getString("Efficiency"), Efficiency.class));

        init();
    }

    private void init(){
        panel = new JPanel(new BorderLayout());

        JPanel centerPanel = new JPanel();

        JPanel southPanel = new JPanel(new GridBagLayout());

        int rowCounter = 0;
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = c.WEST;
        c.insets = new Insets(5,5,5,5);
        c.ipadx = 5;
        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Number of Samples(total)"),c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        sampleCountField.setEditable(false);
        southPanel.add(sampleCountField, c);

        c.anchor = c.WEST;
        c.ipadx = 5;
        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Number of Samples(Regression) "),c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        sampleCountField.setEditable(false);
        southPanel.add(sampleCountFieldRegression, c);
        
        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Method for Regression"),c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        southPanel.add(this.regressionMethod,c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Method for Sensitivity Analysis"),c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        southPanel.add(this.sensitivityMethod,c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Quality of Regression (E2)"),c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        regressionErrorField.setEditable(false);
        southPanel.add(this.regressionErrorField,c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Quality of Regression (E2)"),c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        regressionErrorField.setEditable(false);
        southPanel.add(this.doVarianceEstimation,c);
        doVarianceEstimation.setSelected(false);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.gridwidth = 2;
        c.fill = c.NONE;
        c.anchor = c.CENTER;
        southPanel.add(new JButton(new AbstractAction("Recalculate Regression"){
            public void actionPerformed(ActionEvent e){
                try{
                    refresh();
                }catch(NoDataException nde){
                    JOptionPane.showMessageDialog(panel, "Unsufficient data to recalculate regression!");
                }
            }
        }),c);
        
        chart = ChartFactory.createStackedBarChart(
            "Sensitivity of Parameters",         // chart title
            "Parameter",                 // domain axis label
            "Sensitivity (%)",                // range axis label
            dataset1,                    // data
            PlotOrientation.HORIZONTAL, // orientation
            false,                       // include legend
            true,
            false
        );

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.lightGray);

        // get a reference to the plot for further customisation...
        chart.getCategoryPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        ChartPanel chartPanel = new ChartPanel(chart);
        centerPanel.add(chartPanel);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
    }

    public JPanel getPanel() {
        return this.panel;
    }

    public void refresh() throws NoDataException{
        if (!this.isRequestFulfilled()) {
            return;
        }

        Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
        ArrayList<DataSet> p[] = getData(new int[]{0, 1});
        SimpleEnsemble p1 = (SimpleEnsemble) p[0].get(0);
        EfficiencyEnsemble p2 = (EfficiencyEnsemble) p[1].get(0);

        SimpleEnsemble xData[] = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet){
            xData[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }

        optas.SA.SensitivityAnalyzer sa = null;

        if (sensitivityMethod.getSelectedItem().equals(rsaString)){
            sa = new optas.SA.RegionalSensitivityAnalysis();
        }else if (sensitivityMethod.getSelectedItem().equals(mgeString)){
            sa = new optas.SA.GradientSensitivityAnalysis();
        }else if (sensitivityMethod.getSelectedItem().equals(eemString)){
            sa = new optas.SA.ElementaryEffects();
        }else if (sensitivityMethod.getSelectedItem().equals(fosiString)){
            sa = new optas.SA.FAST(optas.SA.FAST.Measure.FirstOrder);
        }else if (sensitivityMethod.getSelectedItem().equals(fosiString2)){
            sa = new optas.SA.VarianceBasedSensitivityIndex(Measure.FirstOrder);
        }else if (sensitivityMethod.getSelectedItem().equals(tosiString)){            
            sa = new optas.SA.VarianceBasedSensitivityIndex(Measure.Total);
        }else if (sensitivityMethod.getSelectedItem().equals(linearRegString)){
            sa = new optas.SA.LinearRegression();
        }

        int n = counter;
        sa.setData(xData, p2);

        double sampleSize = 0;
        try{
            sampleSize = Double.parseDouble(this.sampleCountFieldRegression.getText());
        }catch(NumberFormatException nfe){
            JOptionPane.showMessageDialog(panel, "Please enter a real valued number for sample size!");
            return;
        }

        sa.setSampleSize((int)sampleSize);

        sa.setInterpolationMethod(new NeuralNetwork());
        sa.init();
        this.regressionErrorField.setText(Double.toString(sa.getCVError()));
        this.sampleCountField.setText(Integer.toString(p2.getSize()));

        double data[][]= new double[2][n];

        String categoryName[] = new String[n];
        for (int i=0;i<n;i++){
            if (doVarianceEstimation.isSelected()){
                data[0][i] = sa.getSensitivity(i)-sa.getVariance(i);
                data[1][i] = 2*sa.getVariance(i);
            }else{
                data[0][i] = sa.getSensitivity(i);
                data[1][i] = 0;
            }

            categoryName[i] = xData[i].getName();
        }

        dataset1 = DatasetUtilities.createCategoryDataset(new String[]{"Sensitivity", "Uncertainty"}, categoryName, data);
        
        ((CategoryPlot)chart.getPlot()).setDataset(0,dataset1);

        ((CategoryPlot)chart.getPlot()).getRenderer().setSeriesPaint(0, new Color(0, 255, 0));
        ((CategoryPlot)chart.getPlot()).getRenderer().setSeriesPaint(1, new Color(255, 0, 0));
    }
}
