/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import optas.SA.UniversalSensitivityAnalyzer;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.regression.SimpleInterpolation;
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
public class SensitivityToolbox extends MCAT5Plot {

    JPanel panel = null;
    JTextField sampleCountField = new JTextField(10);
    JTextField regressionErrorField = new JTextField(10);
    JTextField sampleCountFieldRegression = new JTextField("1000");
    JCheckBox doVarianceEstimation = new JCheckBox("Estimate Uncertainty of Sensitivity");
    JCheckBox doQualityEstimation = new JCheckBox("Estimate Quality");
    JCheckBox useANNRegression = new JCheckBox("Use ANN Regression");
    JComboBox regressionMethod = new JComboBox(new String[]{"Neural Network"});
    JComboBox parameterNormalizationMethod = new JComboBox(SimpleInterpolation.NormalizationMethod.values());
    JComboBox objectiveNormalizationMethod = new JComboBox(SimpleInterpolation.NormalizationMethod.values());
    String rsaString = "Regional Sensitivity Analysis",
            mgeString = "Maximum Gradient Estimation",
            eemString = "Elementary Effects Method",
            eem2String = "Elementary Effects Method (not absolute)",
            eem3String = "Elementary Effects Method (Variance)",
            fosiString = "First Order Sensitivity by FAST",
            fosiString2 = "First Order Sensitivity by Satelli(2008)(-)",
            tosiString = "Total Sensitivity Index by Satelli(2008)(-)",
            interactionString = "Interaction Analysis",
            linearRegString = "Linear Regression";
    JComboBox sensitivityMethod = new JComboBox(new String[]{rsaString, mgeString, eemString, eem2String, eem3String, fosiString, fosiString2, tosiString, interactionString, linearRegString});
    CategoryDataset dataset1 = null, dataset2 = null;
    JFreeChart chart = null;

    public SensitivityToolbox() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    private void init() {
        panel = new JPanel(new BorderLayout());

        JPanel centerPanel = new JPanel();

        JPanel southPanel = new JPanel(new GridBagLayout());

        int rowCounter = 0;
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = c.WEST;
        c.insets = new Insets(5, 5, 5, 5);
        c.ipadx = 5;
        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Number of Samples(total)"), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        sampleCountField.setEditable(false);
        southPanel.add(sampleCountField, c);

        c.anchor = c.WEST;
        c.ipadx = 5;
        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Number of Samples(Regression) "), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        sampleCountField.setEditable(false);
        southPanel.add(sampleCountFieldRegression, c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        southPanel.add(this.useANNRegression, c);
        useANNRegression.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (useANNRegression.isSelected()) {
                    sampleCountFieldRegression.setEditable(true);
                    regressionMethod.setEnabled(true);
                    doVarianceEstimation.setEnabled(true);
                    parameterNormalizationMethod.setEnabled(true);
                    objectiveNormalizationMethod.setEnabled(true);
                    sensitivityMethod.setModel(new DefaultComboBoxModel(new String[]{rsaString, mgeString, eem2String, eem3String, eemString, fosiString, fosiString2, tosiString, interactionString, linearRegString}));
                } else {
                    sampleCountFieldRegression.setEditable(false);
                    regressionMethod.setEnabled(false);
                    regressionErrorField.setText("--");
                    doVarianceEstimation.setEnabled(false);
                    parameterNormalizationMethod.setEnabled(false);
                    objectiveNormalizationMethod.setEnabled(false);
                    sensitivityMethod.setModel(new DefaultComboBoxModel(new String[]{rsaString, linearRegString}));
                }

                WorkerDlg progress = new WorkerDlg(null, "Updating plot");
                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    public void run() {
                        try {
                            SensitivityToolbox.this.refresh();
                        } catch (NoDataException nde) {
                        }
                    }
                });
                progress.execute();
            }
        });
        c.gridwidth = 1;
        useANNRegression.setSelected(true);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Method for Regression"), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        southPanel.add(this.regressionMethod, c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Parameter normalization method"), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        southPanel.add(this.parameterNormalizationMethod, c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Objective normalization method"), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        southPanel.add(this.objectiveNormalizationMethod, c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Method for Sensitivity Analysis"), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        southPanel.add(this.sensitivityMethod, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = rowCounter++;
        c.fill = c.NONE;
        southPanel.add(new JLabel("Quality of Regression (E2)"), c);
        c.gridx = 1;
        c.fill = c.HORIZONTAL;
        regressionErrorField.setEditable(false);
        southPanel.add(regressionErrorField, c);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.gridwidth = 2;
        c.fill = c.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        regressionErrorField.setEditable(false);
        southPanel.add(this.doVarianceEstimation, c);
        doVarianceEstimation.setSelected(false);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.gridwidth = 2;
        c.fill = c.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        regressionErrorField.setEditable(false);
        southPanel.add(this.doQualityEstimation, c);
        doQualityEstimation.setSelected(false);

        c.gridx = 0;
        c.gridy = rowCounter++;
        c.gridwidth = 2;
        c.fill = c.NONE;
        c.anchor = c.CENTER;
        southPanel.add(new JButton(new AbstractAction("Recalculate Regression") {

            public void actionPerformed(ActionEvent e) {
                WorkerDlg progress = new WorkerDlg(null, "Updating plot");
                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    public void run() {
                        try {
                            SensitivityToolbox.this.refresh();
                        } catch (NoDataException nde) {
                        }
                    }
                });
                if (!((JButton)e.getSource()).isSelected())
                    progress.execute();
            }
        }), c);

        chart = ChartFactory.createStackedBarChart(
                "Sensitivity of Parameters", // chart title
                "Parameter", // domain axis label
                "Sensitivity (%)", // range axis label
                dataset1, // data
                PlotOrientation.HORIZONTAL, // orientation
                false, // include legend
                true,
                false);

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.lightGray);

        // get a reference to the plot for further customisation...
        chart.getCategoryPlot().setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        ChartPanel chartPanel = new ChartPanel(chart);
        centerPanel.add(chartPanel);

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);
    }

    @Override
    public JPanel getPanel() {
        return this.panel;
    }

    @Override
    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
        ArrayList<DataSet> p[] = getData(new int[]{0, 1});        
        EfficiencyEnsemble p2 = (EfficiencyEnsemble) p[1].get(0);

        SimpleEnsemble xData[] = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet) {
            xData[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }

        UniversalSensitivityAnalyzer uniSA = new UniversalSensitivityAnalyzer();

        if (sensitivityMethod.getSelectedItem().equals(rsaString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.RSA);
        } else if (sensitivityMethod.getSelectedItem().equals(mgeString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.MaximumGradient);
        } else if (sensitivityMethod.getSelectedItem().equals(eemString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.ElementaryEffects);
        } else if (sensitivityMethod.getSelectedItem().equals(eem2String)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.ElementaryEffectsNonAbs);
        } else if (sensitivityMethod.getSelectedItem().equals(eem3String)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.ElementaryEffectsVariance);
        } else if (sensitivityMethod.getSelectedItem().equals(fosiString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.FOSI1);
        } else if (sensitivityMethod.getSelectedItem().equals(fosiString2)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.FOSI2);
        } else if (sensitivityMethod.getSelectedItem().equals(tosiString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.TOSI);
        } else if (sensitivityMethod.getSelectedItem().equals(interactionString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.Interaction);
        }else if (sensitivityMethod.getSelectedItem().equals(linearRegString)) {
            uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.LinearRegression);
        }
        uniSA.setUsingRegression(useANNRegression.isSelected());
        uniSA.setParameterNormalizationMethod((SimpleInterpolation.NormalizationMethod) this.parameterNormalizationMethod.getSelectedItem());
        uniSA.setObjectiveNormalizationMethod((SimpleInterpolation.NormalizationMethod) this.objectiveNormalizationMethod.getSelectedItem());

        int n = counter;
        
        double sampleSize = 0;
        try {
            sampleSize = Double.parseDouble(this.sampleCountFieldRegression.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(panel, "Please enter a real valued number for sample size!");
            return;
        }

        uniSA.setSampleCount((int)sampleSize);

        double data[][] = new double[2][n];
        
        String categoryName[] = new String[n];
        int K=1;
        if (doVarianceEstimation.isSelected()){
            K = 10;
        }

        uniSA.setup(xData, p2);
        
        if (doQualityEstimation.isSelected()) {
            this.regressionErrorField.setText(Double.toString(uniSA.calculateError()));            
        }
        this.sampleCountField.setText(Integer.toString(p2.getSize()));
        
        double sensitivity[][] = uniSA.getUncertaintyOfSensitivity(K);
                
        for (int i = 0; i < n; i++) {
            data[0][i] = sensitivity[i][0];
            data[1][i] = sensitivity[i][2] - sensitivity[i][0];

            categoryName[i] = xData[i].getName();
            
            System.out.println("Sensitivity for:\t" + xData[i].getName() + "\t" + data[0][i]);
            System.out.println("Variance for:\t" + xData[i].getName() + "\t" + data[1][i]);
        }
        
        /*for (int k = 0; k < K; k++) {
            
            
            sensitivity[k] = uniSA.getSensitivity();
            
        }

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < K; k++) {
                data[1][i] += 1.0/n*Math.pow(sensitivity[k][i][0] - data[0][i],2);
            }
            System.out.println("Sensitivity for:\t" + xData[i].getName() + "\t" + data[0][i]);
            System.out.println("Variance for:\t" + xData[i].getName() + "\t" + data[1][i]);
        }*/

        dataset1 = DatasetUtilities.createCategoryDataset(new String[]{"Sensitivity", "Uncertainty"}, categoryName, data);

        ((CategoryPlot) chart.getPlot()).setDataset(0, dataset1);

        ((CategoryPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, new Color(0, 255, 0));
        ((CategoryPlot) chart.getPlot()).getRenderer().setSeriesPaint(1, new Color(255, 0, 0));
    }
}
