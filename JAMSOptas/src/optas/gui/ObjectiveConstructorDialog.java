/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.gui;

import jams.gui.input.TableInput;
import jams.meta.OutputDSDescriptor;
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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import optas.hydro.data.DataCollection;
import optas.hydro.data.EfficiencyEnsemble;
import optas.hydro.data.EfficiencyEnsemble.Method;
import optas.hydro.data.Measurement;
import optas.hydro.data.TimeFilter;
import optas.hydro.data.TimeFilterFactory;
import optas.hydro.data.TimeSerie;
import optas.hydro.data.TimeSerieEnsemble;
import org.jfree.chart.ChartPanel;

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

    class TimeFilterTableInput extends TableInput {

        public TimeFilterTableInput() {
            super(new String[]{"enabled","inverted", "additive", "filter"}, new Class[]{Boolean.class,Boolean.class,Boolean.class, String.class}, new boolean[]{true, true, true, false}, true);

            getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

            int vColIndex = 0;
            TableColumn col = getTable().getColumnModel().getColumn(vColIndex);
            int width = 150;
            col.setPreferredWidth(width);
            col = getTable().getColumnModel().getColumn(1);
            col.setPreferredWidth(300);
            
            ((AbstractTableModel) this.getTable().getModel()).addTableModelListener(new TableModelListener() {

                public void tableChanged(TableModelEvent e) {
                    for (Object row[] : tableData.getValue()) {
                        if (row[0] instanceof Boolean) {
                            ((TimeFilter) row[3]).setEnabled((Boolean) row[0]);
                        }
                        if (row[1] instanceof Boolean) {
                            ((TimeFilter) row[3]).setInverted((Boolean) row[1]);
                        }
                        if (row[2] instanceof Boolean) {
                            ((TimeFilter) row[3]).setAdditive((Boolean) row[2]);
                        }
                    }
                    ObjectiveConstructorDialog.this.chart.setFilter(constructCombinedTimeFilter());
                }
            });

            setPreferredSize(new Dimension(450, 200));
        }

        @Override
        protected void editItem() {
            //get the current selection
            int selection = getTable().getSelectedRow();
            Object selectedData = tableData.getElementAt(selection)[3];

            if (selectedData instanceof TimeFilter){
                if (getSelectedMeasurement() == null)
                    return;
                tfd = new TimeFilterDialog(getSelectedMeasurement());
                tfd.init(getSelectedMeasurement(), (TimeFilter)selectedData);
                tfd.setVisible(true);
            }
        }

        @Override
        protected void addItem() {
            if (getSelectedMeasurement() == null)
                return;
            tfd = new TimeFilterDialog(getSelectedMeasurement());
            tfd.init(getSelectedMeasurement(), null);
            tfd.setVisible(true);
            if (tfd.getApproval()){                
                TimeFilter filter = tfd.getFilter();
                // add this item to the list and refresh
                if (filter != null) {
                    this.tableData.addElement(new Object[]{new Boolean(filter.isEnabled()),new Boolean(filter.isInverted()),new Boolean(filter.isAdditive()), filter});
                    this.setTableData(tableData.getValue());
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
                ObjectiveConstructorDialog.this.chart.setFilter(constructCombinedTimeFilter());
            }
        }
    }

    private Measurement getSelectedMeasurement() {
        Object o = ObjectiveConstructorDialog.this.msDataBox.getSelectedItem();
        if (o == null) {
            return null;
        }
        
        Measurement m = (Measurement) dc.getDataSet(o.toString());
        return m;
    }
    private TimeFilter constructCombinedTimeFilter(){
        TimeFilter filters[] = new TimeFilter[filterList.getTable().getRowCount()];
        int i=0;
        for (Object[] o : filterList.getTableData()){
            filters[i++] = (TimeFilter)o[3];
        }
        return TimeFilterFactory.getCombinedTimeFilter(filters);
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
        ChartPanel chartPanel = new ChartPanel(chart.getChart(), true);
        mainPanel.add(chartPanel,c);

        filterList = new TimeFilterTableInput();
        c.gridx = 0;
        c.gridy = yCounter;
        c.gridwidth = 2;
        c.gridheight = 5;
        yCounter+=5;
        mainPanel.add(filterList,c);

        c.gridx = 0;
        c.gridy = yCounter++;
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
                        (Method)methodList.getSelectedItem(), ObjectiveConstructorDialog.this.constructCombinedTimeFilter());
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
