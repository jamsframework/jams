/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui.wizard;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import optas.data.DataCollection;
import optas.data.EfficiencyEnsemble;
import optas.data.EfficiencyEnsemble.Method;
import optas.data.Measurement;
import optas.data.TimeSerie;
import optas.data.TimeSerieEnsemble;
import optas.tools.PatchedChartPanel;

/**
 *
 * @author chris
 */
public class ObjectiveConstructorDialog extends JDialog{
    DataCollection dc;

    JComboBox methodList = new JComboBox();
    TimeFilterDialog tfd = null;

    EfficiencyEnsemble result = null;
    boolean isApproved = false;

    JComboBox simDataBox = null;
    JComboBox msDataBox  = null;
    JTextField name = new JTextField();
    TimeFilterTableInput filterList = null;

    HydrographChart chart = new HydrographChart();

    

    private Measurement getSelectedMeasurement() {
        Object o = ObjectiveConstructorDialog.this.msDataBox.getSelectedItem();
        if (o == null) {
            return null;
        }
        
        Measurement m = (Measurement) dc.getDataSet(o.toString());
        return m;
    }
    
    public ObjectiveConstructorDialog(DataCollection dc){        
        this.dc = dc;
        init();
    }

    static public boolean isApplicable(DataCollection dc){
        return TimeFilterDialog.isApplicable(dc);
    }

    private void init(){
        this.setResizable(false);
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int yCounter = 0;
        c.gridx = 0;
        c.ipadx = 5;
        c.ipady = 5;
        c.insets = new Insets(5, 5, 5, 5);
        c.gridy = yCounter++;
        c.gridwidth = 1;
        c.gridheight = 1;

        c.fill = GridBagConstraints.BOTH;

        //name
        mainPanel.add(new JLabel("Name"),c);
        c.gridx = 1;
        mainPanel.add(name,c);

        //eff
        c.gridx = 0;
        c.gridy = yCounter++;
        mainPanel.add(new JLabel("Efficiency Method"),c);
        c.gridx = 1;
        
        for (Method m : EfficiencyEnsemble.Method.values())
            methodList.addItem(m);
            
        mainPanel.add(methodList,c);

        //simdata
        JLabel label1 = new JLabel("Simulation Data");
        c.gridx = 0;
        c.gridy = yCounter++;
        mainPanel.add(label1,c);

        Set<String> tsDataSets = dc.getDatasets(TimeSerie.class);
        if (tsDataSets.isEmpty()){
            JOptionPane.showMessageDialog(mainPanel, "There are no simulated timeserie in the data collection!");
        }
        simDataBox = new JComboBox(tsDataSets.toArray());
        c.gridx = 1;        
        mainPanel.add(simDataBox,c);

        JLabel label2 = new JLabel("Measurement Data");
        c.gridx = 0;
        c.gridy = yCounter++;
        mainPanel.add(label2,c);
        Set<String> msDataSets = dc.getDatasets(Measurement.class);
        if (msDataSets.isEmpty()){
            JOptionPane.showMessageDialog(mainPanel, "There are no measurements in the data collection!");
        }
        msDataBox = new JComboBox(msDataSets.toArray());
        msDataBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object o = msDataBox.getSelectedItem();
                if (o != null){
                    chart.setHydrograph((TimeSerie)dc.getDataSet((String)o));
                }
            }
        });        
        chart.setHydrograph((TimeSerie)dc.getDataSet((String)msDataBox.getItemAt(0)));
        c.gridx = 1;        
        mainPanel.add(msDataBox,c);

        c.gridx = 0;
        c.gridy = yCounter;
        c.gridwidth = 2;
        c.gridheight = 8;
        yCounter+=8;
        PatchedChartPanel chartPanel = new PatchedChartPanel(chart.getChart(), true);
        mainPanel.add(chartPanel,c);

        filterList = new TimeFilterTableInput(getSelectedMeasurement());
        filterList.addChangeListener(new TimeFilterTableInput.TimeFilterTableInputListener() {

            @Override
            public void tableChanged(TimeFilterTableInput tfti) {
                chart.setTimeFilters(filterList.getTimeFilters());                
            }

            @Override
            public void itemChanged(TimeFilterTableInput tfti) {
                chart.setTimeFilters(filterList.getTimeFilters());
            }
        });
        
        c.gridx = 0;
        c.gridy = yCounter;
        c.gridwidth = 2;
        c.gridheight = 5;
        yCounter+=5;
        mainPanel.add(filterList,c);

        c.gridx = 0;
        c.gridy = yCounter;
        c.gridwidth = 1;
        c.gridheight = 1;

        JButton exportTimeFilterButton = new JButton("Export Time-Filter");

        exportTimeFilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                    JFileChooser jfc = new JFileChooser();
                    int approved = jfc.showSaveDialog(ObjectiveConstructorDialog.this);
                    if (approved == jfc.APPROVE_OPTION) {
                        result = new EfficiencyEnsemble("blubb",
                        (Measurement)dc.getDataSet((String)msDataBox.getSelectedItem()),
                        (TimeSerieEnsemble)dc.getDataSet((String)simDataBox.getSelectedItem()),
                        (Method)methodList.getSelectedItem(), filterList.getTimeFilters().combine());
                        result.exportTimeFilter(jfc.getSelectedFile(), ObjectiveConstructorDialog.this.chart.hydrograph.getTimeDomain());
                    }
            }
        });
                
        mainPanel.add(exportTimeFilterButton, c);
        
        JButton button = new JButton("Ok");
        button.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String strName = name.getText();
                if (strName.isEmpty()){
                    JOptionPane.showMessageDialog(methodList, "Please give the new objective a name");
                    return;
                }
                result = new EfficiencyEnsemble(strName, 
                        (Measurement)dc.getDataSet((String)msDataBox.getSelectedItem()),
                        (TimeSerieEnsemble)dc.getDataSet((String)simDataBox.getSelectedItem()),
                        (Method)methodList.getSelectedItem(), filterList.getTimeFilters().combine());
                isApproved = true;
                ObjectiveConstructorDialog.this.setVisible(false);
                }
        });
        c.gridx = 1;
        c.gridy = yCounter++;
        c.gridwidth = 1;
        c.gridheight = 1;
        
        mainPanel.add(button, c);
        this.getContentPane().add(mainPanel);
        this.pack();

        // Größe des Bildschirms ermitteln
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        // Position des JFrames errechnen
        int top = (screenSize.height - getPreferredSize().height) / 2;
        int left = (screenSize.width - getPreferredSize().width) / 2;
        setLocation(left, top);

        this.setModal(true);
    }

    public boolean getApproved(){
        return isApproved;
    }
    public EfficiencyEnsemble getResult(){
        return result;
    }
}
