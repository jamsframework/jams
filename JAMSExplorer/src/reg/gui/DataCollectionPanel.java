/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package reg.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import reg.gui.MCAT5.MCAT5Toolbar;
import reg.hydro.data.DataCollection;
import reg.hydro.data.Efficiency;
import reg.hydro.data.Measurement;
import reg.hydro.data.Parameter;
import reg.hydro.data.StateVariable;
import reg.hydro.data.TimeSerie;


/**
 *
 * @author chris
 */
public class DataCollectionPanel extends JPanel{
    JTable availableMCDatasetList = null;
    ArrayList<DataCollection> ensembleList = new ArrayList<DataCollection>();

    JPanel northPanel = new JPanel(new BorderLayout());

    JFrame owner;

    final String[] columnNames = {"Parameter","Objectives",
            "State Variables","Measurements","Timeseries","Sample Method",
            "Simulations", "Timesteps", "Remarks"};

    public DataCollectionPanel(JFrame owner, File dataCollectionFile, Dimension defaultContentPaneDimension){        
        this.owner = owner;
        init(defaultContentPaneDimension);
        this.addEnsemble(DataCollection.createFromFile(dataCollectionFile));
    }

    public DataCollectionPanel(JFrame owner, Dimension defaultContentPaneDimension){
        this.owner = owner;
        init(defaultContentPaneDimension);
    }

    public JFrame getOwner(){
        return owner;
    }

    final public void addEnsemble(DataCollection en){
        ensembleList.add(en);

        DefaultTableModel model = new DefaultTableModel();
        
        for (DataCollection e : ensembleList){
            String sampleMethod = e.getSamplerClass().getName();
            String simulations = Integer.toString(e.getSimulationCount());
            String timesteps = "null";
            if (e.getTimeDomain()!=null)
                timesteps = e.getTimeDomain().toString();
            String remarkString = "<html><body>";
            remarkString += "</body></html>";

            model.addColumn(e.name,new Object[]{  Arrays.toString(e.getDatasets(Parameter.class).toArray()),
                                        Arrays.toString(e.getDatasets(Efficiency.class).toArray()),
                                        Arrays.toString(e.getDatasets(StateVariable.class).toArray()),
                                        Arrays.toString(e.getDatasets(Measurement.class).toArray()),
                                        Arrays.toString(e.getDatasets(TimeSerie.class).toArray()),
                                        sampleMethod, simulations, timesteps, remarkString,e});

        }
        availableMCDatasetList.setModel(model);
        availableMCDatasetList.updateUI();
        
    }

    public DataCollection getCurrentCollection(){        
        return ensembleList.get(0);
    }

    class RowHeaderRenderer extends JLabel implements ListCellRenderer {

        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private void init(Dimension defaultContentPaneDimension){
        availableMCDatasetList = new JTable( null, columnNames ){
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;   //Disallow the editing of any cell
            }
            @Override
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                this.setRowHeight(40);
            }
        };
        availableMCDatasetList.setModel(new DefaultTableModel());

        JList rowHeader = new JList(columnNames);
        rowHeader.setFixedCellWidth(150);

        rowHeader.setFixedCellHeight(availableMCDatasetList.getRowHeight()
                                    + availableMCDatasetList.getRowMargin()-1
                             /*+ availableMCDatasetList.getIntercellSpacing().height*/);
        rowHeader.setCellRenderer(new RowHeaderRenderer(availableMCDatasetList));

        JScrollPane availableMCDatasetListScroll = new JScrollPane(availableMCDatasetList);
        availableMCDatasetListScroll.setRowHeaderView(rowHeader);
        if (defaultContentPaneDimension != null)
            availableMCDatasetListScroll.setSize(defaultContentPaneDimension);
        availableMCDatasetList.setRowHeight(0, 50);
        availableMCDatasetList.setTableHeader(null);
        availableMCDatasetList.addMouseListener(new MouseListener() {

            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultTableModel model = (DefaultTableModel) availableMCDatasetList.getModel();
                    DataCollection en = (DataCollection) model.getValueAt(9, 0);
                    ImportMonteCarloDataPanel dialog = new ImportMonteCarloDataPanel(DataCollectionPanel.this.getOwner(), en);
                    northPanel.add(dialog, BorderLayout.EAST);
                    northPanel.invalidate();
                    northPanel.updateUI();
                }
            }
        });


        JButton createMonteCarloDataSet = new JButton("Add Monte Carlo Dataset");
        createMonteCarloDataSet.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                ImportMonteCarloDataPanel dialog = new ImportMonteCarloDataPanel(DataCollectionPanel.this.getOwner(),null);
                northPanel.add(dialog,BorderLayout.EAST);
                northPanel.invalidate();
                northPanel.updateUI();                
            }
        });
        JButton removeMonteCarloDataSet = new JButton("Remove Monte Carlo Dataset");
        JButton showMonteCarloDataSet = new JButton("Show Monte Carlo Dataset");
        JButton exportMonteCarloDataSet = new JButton("Show Monte Carlo Dataset");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());        
        buttonPanel.add(createMonteCarloDataSet);
        buttonPanel.add(removeMonteCarloDataSet);
        buttonPanel.add(showMonteCarloDataSet);
        buttonPanel.add(exportMonteCarloDataSet);        
        buttonPanel.add(new MCAT5Toolbar(this));

        JPanel analysePanel = new JPanel();
              
        northPanel.add(availableMCDatasetListScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(analysePanel, BorderLayout.SOUTH);
    }
}
