/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import jams.gui.input.ListInput;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import optas.hydro.data.DataCollection;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.EfficiencyEnsemble.Method;
import optas.hydro.data.Measurement;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeSerie;
import optas.hydro.data.TimeSerieEnsemble;

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

    class TimeFilterListInput extends ListInput {

        @Override
        protected void editItem() {
            //get the current selection
            JOptionPane.showMessageDialog(methodList, "Unsupported");
        }

        @Override
        protected void addItem() {
            tfd.setVisible(true);
            if (tfd.getApproval()){
                TimeFilter filter = tfd.getFilter();
                // add this item to the list and refresh
                if (filter != null && !listData.getValue().contains(filter)) {
                    listData.addElement(filter);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }
        }
    }

    public ObjectiveConstructorDialog(DataCollection dc){
        tfd = new TimeFilterDialog(dc);
        this.dc = dc;
        init();
    }

    private void init(){
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        int yCounter = 0;
        c.gridx = 0;
        c.gridy = yCounter;
        c.gridwidth = 1;
        c.gridheight = 1;

        c.fill = GridBagConstraints.BOTH;

        //name
        mainPanel.add(new JLabel("Name"),c);
        c.gridx = 1;
        mainPanel.add(name,c);

        //eff
        c.gridx = 0;
        c.gridy = ++yCounter;
        mainPanel.add(new JLabel("Efficiency Method"),c);
        c.gridx = 1;
        
        for (Method m : EfficiencyEnsemble.Method.values())
            methodList.addItem(m);
            
        mainPanel.add(methodList,c);

        //simdata
        JLabel label1 = new JLabel("Simulation Data");
        c.gridx = 0;
        c.gridy = ++yCounter;
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
        c.gridy = ++yCounter;
        mainPanel.add(label2,c);
        Set<String> msDataSets = dc.getDatasets(Measurement.class);
        if (msDataSets.isEmpty()){
            JOptionPane.showMessageDialog(mainPanel, "There are no measurements in the data collection!");
        }
        msDataBox = new JComboBox(msDataSets.toArray());
        c.gridx = 1;        
        mainPanel.add(msDataBox,c);

        TimeFilterListInput filterList = new TimeFilterListInput();
        c.gridx = 0;
        c.gridy = ++yCounter;
        c.gridwidth = 2;
        c.gridheight = 5;
        mainPanel.add(filterList,c);

        c.gridx = 0;
        c.gridy = (yCounter+=5);
        c.gridwidth = 2;
        c.gridheight = 1;

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
                        (Method)methodList.getSelectedItem());
                isApproved = true;
                ObjectiveConstructorDialog.this.setVisible(false);
                }
        });
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
