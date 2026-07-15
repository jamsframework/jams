package jams.explorer.gui;

import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import optas.data.DataCollection;
import optas.data.DataSet;
import optas.data.EfficiencyEnsemble;
import optas.data.Ensemble;
import optas.data.Measurement;
import optas.data.SimpleEnsemble;
import optas.data.TimeFilter;
import optas.data.TimeFilterFactory;
import optas.data.TimeSerieEnsemble;
import optas.gui.MCAT5.DataCollectionPanel;
import optas.gui.MCAT5.MCAT5Toolbar;
import optas.tools.PatchedChartPanel;
import org.jdesktop.swingx.JXDatePicker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;
import jams.explorer.DataCollectionViewController;

public class DataCollectionView extends JComponent implements DataCollectionPanel {

    public enum DataType {

        TIME_SERIES,
        MEASUREMENT,
        OBJECTIVE,
        VARIABLE,
        PARAMETER;

        @Override
        public String toString() {
            switch (this) {
                case TIME_SERIES:
                    return "Time Series";
                case MEASUREMENT:
                    return "Measurement";
                case OBJECTIVE:
                    return "Objective";
                case VARIABLE:
                    return "Variable";
                case PARAMETER:
                    return "Parameter";
                default:
                    return null;
            }
        }
    }
    private DataCollectionViewController delegate;
    private TimeInterval maximumInterval = null;

    public DataCollectionView(DataCollectionViewController delegate) {
        this.delegate = delegate;
        initComponents();
        layoutComponents();
    }
    private JTable ensembleList = null;
    private JTable dataSetList = null;
    private JScrollPane ensembleListScrollPane = null;
    private JScrollPane dataSetListScrollPane = null;
    private JButton closeButton = new JButton("Close Tab");
    //Display Panel
    private JPanel displayPanel = null;
    private JButton displayDataButton = null;
    private JButton sumTSData = null;
    private JPanel timeIntervalPanel = null;
    private JLabel startDateLabel = null;
    private JLabel finalDateLabel = null;
    private JXDatePicker startDatePicker = null;
    private JXDatePicker finalDatePicker = null;
    private TitledBorder enabledTimeIntervalPanelBorder = null;
    private TitledBorder disabledTimeIntervalPanelBorder = null;
    private JPanel simIDPanel = null;
    private JLabel fromIDLabel = null;
    private JLabel toIDLabel = null;
    private JSpinner fromIDSpinner = null;
    private JSpinner toIDSpinner = null;
    private TitledBorder enabledIDPanelBorder = null;
    private TitledBorder disabledIDPanelBorder = null;
    private JPanel filterPanel = null;
    private JPanel filterSimPanel = null;
    private JLabel filterFromValueLabel = null;
    private JLabel filterToValueLabel = null;
    private JTextField filterFromValueField = null;
    private JTextField filterToValueField = null;
    private TitledBorder enabledValueFilterPanelBorder = null;
    private TitledBorder disabledValueFilterPanelBorder = null;
    private JPanel filterTimeIntervalPanel = null;
    private JLabel filterStartDateLabel = null;
    private JLabel filterFinalDateLabel = null;
    private JXDatePicker filterStartDatePicker = null;
    private JXDatePicker filterFinalDatePicker = null;
    private TitledBorder enabledTimeIntervalFilterPanelBorder = null;
    private TitledBorder disabledTimeIntervalFilterPanelBorder = null;
    private JPanel filterPercentilPanel = null;
    private JLabel filterFromPercentilLabel = null;
    private JLabel filterToPercentilLabel = null;
    private JTextField filterFromPercentilField = null;
    private JTextField filterToPercentilField = null;
    private TitledBorder enabledPercentilFilterPanelBorder = null;
    private TitledBorder disabledPercentilFilterPanelBorder = null;
    private JButton filterButton = null;
    private JButton inverseFilterButton = null;
    private JButton clearTimeFilterButton = null;    
    private JButton clearValueFilterButton = null;
    private JButton deleteFilteredValuesButton = null;
    private JButton deleteAttribute = null;
    private JXDatePicker extractDatePicker = null;
    private JButton extractButton;
    private JTable table = null;
    private TableModel defaultTableModel = null;
    private TableModel tableModel = null;
    private JScrollPane tableScrollPane = null;
    private JButton showGraphButton = null;
    private MCAT5Toolbar mcat5Toolbar;

    private String[] getSelectedDataSets() {
        int row = ensembleList.getSelectedRow();
        if (row == -1) {
            return null;
        }
        DataType type = (DataType) ensembleList.getValueAt(row, 0);
        return delegate.getItemIdentifiersForDataType(type);
    }


    /* enable simulation id panel */


    private void switchSelectedIDComponentState(boolean enabled, Object item) {
        if (enabled) {
            simIDPanel.setBorder(enabledIDPanelBorder);
            fromIDLabel.setForeground(Color.BLACK);
            toIDLabel.setForeground(Color.BLACK);

            /* enable id spinners */
            fromIDSpinner.setEnabled(true);
            toIDSpinner.setEnabled(true);

            /* configure id spinners */
            Integer ids[] = delegate.getSimulationIDs();

            final SpinnerListModel fromIDSpinnerListModel = new SpinnerListModel(ids);
            fromIDSpinnerListModel.setValue(ids[0]);
            fromIDSpinner.setModel(fromIDSpinnerListModel);

            final SpinnerListModel toIDSpinnerListModel = new SpinnerListModel(ids);
            toIDSpinnerListModel.setValue(ids[ids.length - 1]);
            toIDSpinner.setModel(toIDSpinnerListModel);

            ChangeListener changeListener = new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    /* make sure values have a valid range */
                    int fromID = (Integer) fromIDSpinnerListModel.getValue();
                    int toID = (Integer) toIDSpinnerListModel.getValue();
                    boolean validRange = fromID <= toID;
                    if (!validRange) {
                        if (e.getSource().equals(fromIDSpinnerListModel)) {
                            fromIDSpinnerListModel.setValue(toID);
                        } else {
                            toIDSpinnerListModel.setValue(fromID);
                        }
                    }
                }
            };
            fromIDSpinnerListModel.addChangeListener(changeListener);
            toIDSpinnerListModel.addChangeListener(changeListener);
        } else {
            simIDPanel.setBorder(disabledIDPanelBorder);
            fromIDLabel.setForeground(Color.GRAY);
            toIDLabel.setForeground(Color.GRAY);

            /* enable id spinners */
            fromIDSpinner.setEnabled(false);
            toIDSpinner.setEnabled(false);
        }
    }
    private void switchTimeIntervalComponentState(boolean enabled, Object item) {
        if (enabled && item != null) {
            timeIntervalPanel.setBorder(enabledTimeIntervalPanelBorder);
            startDateLabel.setForeground(Color.BLACK);
            finalDateLabel.setForeground(Color.BLACK);

            /* enable date pickers */
            startDatePicker.setEnabled(true);
            finalDatePicker.setEnabled(true);

            /* retrieve time interval from dataset */
            maximumInterval = delegate.getTimeInterval(item);

            /* set timezone for date pickers to match the one from the dataset's date objects */
            startDatePicker.setTimeZone(((java.util.Calendar) maximumInterval.getStart()).getTimeZone());
            finalDatePicker.setTimeZone(((java.util.Calendar) maximumInterval.getEnd()).getTimeZone());

            /* reset date pickers to the given time interval */
            startDatePicker.setDate(maximumInterval.getStart().getTime());
            finalDatePicker.setDate(maximumInterval.getEnd().getTime());

        } else {
            /* disable time interval panel */
            timeIntervalPanel.setBorder(disabledTimeIntervalPanelBorder);
            startDateLabel.setForeground(Color.GRAY);
            finalDateLabel.setForeground(Color.GRAY);

            /* enable date pickers */
            startDatePicker.setEnabled(false);
            finalDatePicker.setEnabled(false);
        }
    }

    private void switchValueFilterComponentState(boolean enabled, Object item) {
        Object o = null;
        if (enabled && item != null) {
            o = delegate.getItemForIdentifier(item);
        }
        if (enabled && o != null && o instanceof SimpleEnsemble) {
            SimpleEnsemble ensemble = (SimpleEnsemble) o;
            double min = ensemble.getMin();
            double max = ensemble.getMax();

            //could happen because Effs are sorted based on the objective
            if (min > max) {
                double tmp = max;
                max = min;
                min = tmp;
            }
            min = Math.round(min*1000)/1000.0;
            max = Math.round(max*1000)/1000.0;
            filterFromValueField.setEnabled(true);
            filterToValueField.setEnabled(true);
            filterFromValueField.setText(Double.toString(min));
            filterToValueField.setText(Double.toString(max));
            filterToValueField.setEnabled(true);
            filterFromValueField.setEnabled(true);
            filterSimPanel.setBorder(enabledValueFilterPanelBorder);
            filterButton.setEnabled(true);
            inverseFilterButton.setEnabled(true);
        } else {
            filterSimPanel.setBorder(disabledValueFilterPanelBorder);
            filterFromValueField.setEnabled(false);
            filterToValueField.setEnabled(false);
            filterFromValueField.setText("");
            filterToValueField.setText("");
            inverseFilterButton.setEnabled(false);
        }
    }
    private void switchPercentilFilterComponentState(boolean enabled, Object item) {
        Object o = null;
        if (enabled && item != null) {
            o = delegate.getItemForIdentifier(item);
        }
        if (enabled && o != null && o instanceof SimpleEnsemble) {
            filterPercentilPanel.setBorder(enabledPercentilFilterPanelBorder);

            filterFromPercentilField.setEnabled(true);
            filterToPercentilField.setEnabled(true);
            filterFromPercentilField.setText("0.0");
            filterToPercentilField.setText("1.0");
            filterToPercentilField.setEnabled(true);
            filterFromPercentilField.setEnabled(true);            
            filterButton.setEnabled(true);
            inverseFilterButton.setEnabled(true);
        } else {
            filterPercentilPanel.setBorder(disabledPercentilFilterPanelBorder);
            filterFromPercentilField.setEnabled(false);
            filterToPercentilField.setEnabled(false);
            filterFromPercentilField.setText("");
            filterToPercentilField.setText("");
            inverseFilterButton.setEnabled(false);
        }
    }
    private void switchTimeFilterComponentState(boolean enabled, Object item) {
        sumTSData.setEnabled(enabled & item != null);
        sumTSData.putClientProperty("item", item);
        if (enabled && item != null) {
            filterTimeIntervalPanel.setBorder(enabledTimeIntervalFilterPanelBorder);
            filterFinalDatePicker.setEnabled(true);
            filterStartDatePicker.setEnabled(true);
            filterStartDateLabel.setForeground(Color.BLACK);
            filterFinalDateLabel.setForeground(Color.BLACK);

            /* retrieve time interval from dataset */
            maximumInterval = delegate.getTimeInterval(item);

            filterStartDatePicker.setTimeZone(((java.util.Calendar) maximumInterval.getStart()).getTimeZone());
            filterFinalDatePicker.setTimeZone(((java.util.Calendar) maximumInterval.getEnd()).getTimeZone());

            filterStartDatePicker.setDate(maximumInterval.getStart().getTime());
            filterFinalDatePicker.setDate(maximumInterval.getEnd().getTime());

            filterButton.setEnabled(true);
        } else {
            filterTimeIntervalPanel.setBorder(disabledTimeIntervalFilterPanelBorder);
            filterFinalDatePicker.setEnabled(false);
            filterStartDatePicker.setEnabled(false);
            filterStartDateLabel.setForeground(Color.GRAY);
            filterFinalDateLabel.setForeground(Color.GRAY);
        }
    }

    private void initComponents() {

        /* ensemble list */
        String[] columns = new String[]{"Ensembles"};
        DataType[] types = delegate.getAvailableDataTypes();
        DataType[][] entries = new DataType[types.length][1];
        int i;
        for (i = 0; i < types.length; i++) {
            entries[i][0] = types[i];
        }
        ensembleList = new JTable(entries, columns) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ensembleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ensembleList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                table.setModel(defaultTableModel);
                ((AbstractTableModel) dataSetList.getModel()).fireTableDataChanged();
            }
        });
        ensembleListScrollPane = new JScrollPane(ensembleList);

        /* data set list */
        dataSetList = new JTable(new DefaultTableModel() {

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public String getColumnName(int column) {
                return "Data Sets";
            }

            @Override
            public int getRowCount() {
                String[] selectedDataSets = getSelectedDataSets();
                if (selectedDataSets == null) {
                    return 1;
                } else {
                    return selectedDataSets.length;
                }
            }

            @Override
            public Object getValueAt(int row, int column) {
                String[] selectedDataSets = getSelectedDataSets();
                if (selectedDataSets == null) {
                    return "Select Ensemble...";
                } else {
                    return selectedDataSets[row];
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                String[] selectedDataSets = getSelectedDataSets();
                if (selectedDataSets.length > row){
                    return true;
                }
                return false;
            }
            
            @Override
            public void setValueAt(Object aValue, int row, int column){
                String[] selectedDataSets = getSelectedDataSets();
                if (selectedDataSets.length > row){
                    String name = selectedDataSets[row];
                    delegate.getDataCollection().renameDataset(name, aValue.toString());
                }
                
            }
        });

        dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                filterButton.setEnabled(false);
                inverseFilterButton.setEnabled(false);
                if (dataSetList.getSelectedRow() == -1) {
                    switchTimeFilterComponentState(false, null);
                    switchValueFilterComponentState(false, null);
                    switchPercentilFilterComponentState(false, null);
                    switchTimeIntervalComponentState(false, null);
                    switchSelectedIDComponentState(false, null);

                    displayDataButton.setEnabled(false);
                    filterButton.setEnabled(false);
                    inverseFilterButton.setEnabled(false);
                    return;
                }

                /* reset value table and disable graph button */
                table.setModel(defaultTableModel);
                showGraphButton.setEnabled(false);

                Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);

                filterButton.setEnabled(false);
                inverseFilterButton.setEnabled(false);

                if (delegate.hasTimeInterval(item)) {
                    switchTimeFilterComponentState(true, item);
                    switchTimeIntervalComponentState(true, item);

                    switchValueFilterComponentState(false,item);
                    switchPercentilFilterComponentState(false,item);
                } else {
                    switchTimeFilterComponentState(false, item);
                    switchTimeIntervalComponentState(false, item);
                }

                if (delegate.isMultirun(item)) {
                    switchSelectedIDComponentState(true,item);
                } else {
                    switchSelectedIDComponentState(false,item);
                }

                if (!delegate.hasTimeInterval(item) && delegate.isMultirun(item)) {
                    switchValueFilterComponentState(true,item);
                    switchPercentilFilterComponentState(true,item);
                } 
                displayDataButton.setEnabled(true);
            }
        });
        dataSetListScrollPane = new JScrollPane(dataSetList);

        displayPanel = new JPanel();
        displayPanel.setBorder(new TitledBorder(null, " Display Data ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
        enabledTimeIntervalPanelBorder = new TitledBorder(null, " Time interval ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledTimeIntervalPanelBorder = new TitledBorder(null, " Time interval ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);

        filterTimeIntervalPanel = new JPanel();
        enabledTimeIntervalFilterPanelBorder = new TitledBorder(null, " Time interval ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledTimeIntervalFilterPanelBorder = new TitledBorder(null, " Time interval ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);

        enabledValueFilterPanelBorder = new TitledBorder(null, " Value filter ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledValueFilterPanelBorder = new TitledBorder(null, " Value filter ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);

        enabledPercentilFilterPanelBorder = new TitledBorder(null, " Percentil filter ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledPercentilFilterPanelBorder = new TitledBorder(null, " Percentil filter ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);

        filterPanel = new JPanel();
        filterPanel.setBorder(new TitledBorder(null, " Filter Data ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK));
        filterSimPanel = new JPanel();
        filterSimPanel.setBorder(disabledValueFilterPanelBorder);
        filterPercentilPanel = new JPanel();
        filterPercentilPanel.setBorder(disabledPercentilFilterPanelBorder);

        filterTimeIntervalPanel.setBorder(disabledTimeIntervalFilterPanelBorder);

        timeIntervalPanel = new JPanel();
        timeIntervalPanel.setBorder(disabledTimeIntervalPanelBorder);

        startDateLabel = new JLabel("From:");
        startDateLabel.setForeground(Color.GRAY);
        finalDateLabel = new JLabel("To:");
        finalDateLabel.setForeground(Color.GRAY);

        filterStartDateLabel = new JLabel("From:");
        filterStartDateLabel.setForeground(Color.GRAY);
        filterFinalDateLabel = new JLabel("To:");
        filterFinalDateLabel.setForeground(Color.GRAY);

        startDatePicker = new JXDatePicker(System.currentTimeMillis());
        finalDatePicker = new JXDatePicker(System.currentTimeMillis());
        startDatePicker.setEnabled(false);
        finalDatePicker.setEnabled(false);

        filterStartDatePicker = new JXDatePicker(System.currentTimeMillis());
        filterFinalDatePicker = new JXDatePicker(System.currentTimeMillis());
        filterStartDatePicker.setEnabled(false);
        filterFinalDatePicker.setEnabled(false);

        sumTSData = new JButton("Sum up TimeSerie");
        sumTSData.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object item = ((JButton)e.getSource()).getClientProperty("item");
                if (item != null){
                    DataSet ts = delegate.getDataCollection().getDataSet((String)item);
                    if (ts instanceof TimeSerieEnsemble){
                        SimpleEnsemble se = ((TimeSerieEnsemble)ts).sumTS();
                        delegate.getDataCollection().addEnsemble(se);
                    }
                }
            }
        });

        deleteAttribute = new JButton("Delete Attribute");
        deleteAttribute.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e){
               Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
               delegate.getDataCollection().removeDataset(item.toString());
               ((AbstractTableModel) dataSetList.getModel()).fireTableDataChanged();   
           }
        });

        ActionListener datePickerActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                /* set actual time values to 0 to avoid mismatch with JXDatePicker */
                java.util.Calendar startCal = (java.util.Calendar) maximumInterval.getStart().clone();
                java.util.Calendar endCal = (java.util.Calendar) maximumInterval.getEnd().clone();
                startCal.set(java.util.Calendar.HOUR, 0);
                startCal.set(java.util.Calendar.MINUTE, 0);
                endCal.set(java.util.Calendar.HOUR, 0);
                endCal.set(java.util.Calendar.MINUTE, 0);

                /* check for valid range and enable or disable button */
                boolean validStartDate = startDatePicker.getDateInMillis() >= startCal.getTimeInMillis();
                boolean validFinalDate = finalDatePicker.getDateInMillis() <= endCal.getTimeInMillis();

                boolean validStartDate2 = filterStartDatePicker.getDateInMillis() >= startCal.getTimeInMillis();
                boolean validFinalDate2 = filterFinalDatePicker.getDateInMillis() <= endCal.getTimeInMillis();
                if (!validStartDate){
                    startDatePicker.setDate(startCal.getTime());
                }
                if (!validFinalDate){
                    finalDatePicker.setDate(endCal.getTime());
                }
                if (!validStartDate2){
                    filterStartDatePicker.setDate(startCal.getTime());
                }
                if (!validFinalDate2){
                    filterFinalDatePicker.setDate(endCal.getTime());
                }                
            }
        };
        startDatePicker.addActionListener(datePickerActionListener);
        finalDatePicker.addActionListener(datePickerActionListener);
        filterStartDatePicker.addActionListener(datePickerActionListener);
        filterFinalDatePicker.addActionListener(datePickerActionListener);

        extractDatePicker = new JXDatePicker(System.currentTimeMillis());
        extractButton = new JButton("Extract");
        extractButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Date d = extractDatePicker.getDate();
                Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                DataSet dataset = DataCollectionView.this.delegate.getDataCollection().getDataSet(item.toString());
                if (dataset instanceof TimeSerieEnsemble) {
                    TimeSerieEnsemble ts = (TimeSerieEnsemble) dataset;
                    int r = -1;
                    for (int i = 0; i < ts.getTimesteps(); i++) {
                        if (ts.getDate(i).after(d) || ts.getDate(i).equals(d)) {
                            r = i;
                            break;
                        }
                    }
                    if (r != -1) {
                        Set<String> s = DataCollectionView.this.delegate.getDataCollection().getDatasets(Measurement.class);
                        String first = s.iterator().next();
                        Measurement m = (Measurement) DataCollectionView.this.delegate.getDataCollection().getDataSet(first);
                        double value = m.getValue(r);
                        EfficiencyEnsemble eff = new EfficiencyEnsemble(ts.get(r), false);
                        eff.calcPlus(-value);
                        eff.calcAbs();
                        DataCollectionView.this.delegate.getDataCollection().addEnsemble(eff);
                    }
                }
            }
        });

        enabledIDPanelBorder = new TitledBorder(null, " Simulation ID ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledIDPanelBorder = new TitledBorder(null, " Simulation ID ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);
        simIDPanel = new JPanel();
        simIDPanel.setBorder(disabledIDPanelBorder);

        fromIDLabel = new JLabel("From:");
        fromIDLabel.setForeground(Color.GRAY);
        toIDLabel = new JLabel("To:");
        toIDLabel.setForeground(Color.GRAY);

        filterFromValueLabel = new JLabel("From:");
        filterFromValueLabel.setForeground(Color.GRAY);
        filterToValueLabel = new JLabel("To:");
        filterToValueLabel.setForeground(Color.GRAY);

        filterFromPercentilLabel = new JLabel("From:");
        filterFromPercentilLabel.setForeground(Color.GRAY);
        filterToPercentilLabel = new JLabel("To:");
        filterToPercentilLabel.setForeground(Color.GRAY);

        fromIDSpinner = new JSpinner();
        fromIDSpinner.setEnabled(false);
        toIDSpinner = new JSpinner();
        toIDSpinner.setEnabled(false);

        filterFromValueField = new JTextField("");
        filterFromValueField.setEnabled(false);
        filterToValueField = new JTextField("");
        filterToValueField.setEnabled(false);

        filterFromPercentilField = new JTextField("");
        filterFromPercentilField.setEnabled(false);
        filterToPercentilField = new JTextField("");
        filterToPercentilField.setEnabled(false);


        displayDataButton = new JButton(new AbstractAction("Display") {

            @Override
            public void actionPerformed(ActionEvent e) {

                Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                showGraphButton.setEnabled(!delegate.hasTimeInterval(item));

                displayData(dataSetList.getValueAt(dataSetList.getSelectedRow(), 0));
            }
        });
        displayDataButton.setEnabled(false);

        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Component c = DataCollectionView.this.getParent();
                if (c != null) {
                    if (c instanceof JTabbedPane) {
                        JTabbedPane pane = (JTabbedPane) c;
                        pane.remove(DataCollectionView.this);
                    }
                }
            }
        });

        columns = new String[]{"Simulation ID", "Timestep"};
        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        defaultTableModel = new AbstractTableModel() {

            @Override
            public String getColumnName(int column) {
                switch (column) {
                    case 0:
                        return "Simulation ID";
                    default:
                        return "Values";
                }
            }

            @Override
            public int getRowCount() {
                return 0;
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return null;
            }
        };
        table.setModel(defaultTableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                showGraphButton.setEnabled(true);
            }
        });
        tableScrollPane = new JScrollPane(table);

        showGraphButton = new JButton("Show graph...");
        showGraphButton.setEnabled(false);
        showGraphButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {

                            /* create chart window */
                            Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                            String itemName = (String) item;
                            JDialog chartWindow = new JDialog((JFrame) getRootPane().getParent(), itemName);
                            chartWindow.setSize(800, 600);
                            chartWindow.setLocation(100, 100);

                            /* create chart */
                            JFreeChart chart = null;
                            if (delegate.hasTimeInterval(item)) {
                                
                                TimeSeriesCollection collection = new TimeSeriesCollection();
                                
                                int numberOfCols = tableModel.getColumnCount();
                                int[] rows = table.getSelectedRows();
                                for (int row : rows) {
                                    TimeSeries series = new TimeSeries("time series #" + row, Day.class);
                                    int i;
                                    for (i = 1; i < numberOfCols; i++) {
                                        Date date = DateFormat.getDateInstance().parse(tableModel.getColumnName(i));
                                        series.add(new Day(date), (Double) tableModel.getValueAt(row, i));
                                    }
                                    collection.addSeries(series);
                                }
                                chart = ChartFactory.createTimeSeriesChart(itemName, null, null, collection, false, false, false);
                            } else {
                                
                                DefaultXYDataset dataset = new DefaultXYDataset();
                                
                                int[] rows = null;
                                if (table.getSelectedRow() == -1) {
                                    rows = new int[tableModel.getRowCount()];
                                    int i;
                                    for (i = 0; i < rows.length; i++) {
                                        rows[i] = i;
                                    }
                                } else {
                                    rows = table.getSelectedRows();
                                }
                                double data[][] = new double[2][rows.length];
                                int index = 0;
                                for (int row : rows) {
                                    Object o = tableModel.getValueAt(row, 0);
                                    if (o != null) {
                                        int id = (Integer) tableModel.getValueAt(row, 0);
                                        double value = (Double) tableModel.getValueAt(row, 1);
                                        data[0][index] = id;
                                        data[1][index] = value;
                                        index++;
                                    }
                                }
                                dataset.addSeries(itemName, data);
                                chart = ChartFactory.createScatterPlot(itemName, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
                            }

                            /* setup chart view and show window */
                            PatchedChartPanel chartPanel = new PatchedChartPanel(chart);
                            chartWindow.getContentPane().add(chartPanel);
                            chartWindow.setVisible(true);
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                };
                SwingUtilities.invokeLater(r);
            }
        });

        filterButton = new JButton("Filter");
        filterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (filterStartDatePicker.isEnabled() && filterFinalDatePicker.isEnabled()) {
                    Date startDate = filterStartDatePicker.getDate();
                    Date endDate = filterFinalDatePicker.getDate();

                    TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
                    interval.getStart().setTime(startDate);
                    interval.getEnd().setTime(endDate);

                    Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                    
                    getDataCollection().filterTimeDomain(TimeFilterFactory.getRangeFilter(interval));
                }
                if (filterFromValueField.isEnabled() && filterToValueField.isEnabled()) {
                    try {
                        double minValue = Double.parseDouble(filterFromValueField.getText());
                        double maxValue = Double.parseDouble(filterToValueField.getText());

                        Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                        Object obj = delegate.getItemForIdentifier(item);
                        if (obj instanceof SimpleEnsemble) {
                            delegate.filter(item, minValue, maxValue, false);
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(ensembleList, "Unrecongnized value " + nfe.getLocalizedMessage());
                    }
                }
                if (filterFromPercentilField.isEnabled() && filterToPercentilLabel.isEnabled()) {
                    try {
                        double minValue = Double.parseDouble(filterFromPercentilField.getText());
                        double maxValue = Double.parseDouble(filterToPercentilField.getText());

                        Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                        Object obj = delegate.getItemForIdentifier(item);
                        if (obj instanceof SimpleEnsemble) {
                            delegate.filterPercentil(item, minValue, maxValue, false);
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(ensembleList, "Unrecongnized value " + nfe.getLocalizedMessage());
                    }
                }
            }
        });

        inverseFilterButton = new JButton("Inverse Filter");
        inverseFilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (filterStartDatePicker.isEnabled() && filterFinalDatePicker.isEnabled()) {
                    Date startDate = filterStartDatePicker.getDate();
                    Date endDate = filterFinalDatePicker.getDate();

                    TimeInterval interval = DefaultDataFactory.getDataFactory().createTimeInterval();
                    interval.getStart().setTime(startDate);
                    interval.getEnd().setTime(endDate);

                    TimeFilter f = TimeFilterFactory.getRangeFilter(interval);
                    f.setInverted(true);
                    getDataCollection().filterTimeDomain(f);
                }
                if (filterFromValueField.isEnabled() && filterToValueField.isEnabled()) {
                    try {
                        double minValue = Double.parseDouble(filterFromValueField.getText());
                        double maxValue = Double.parseDouble(filterToValueField.getText());

                        Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                        Object obj = delegate.getItemForIdentifier(item);
                        if (obj instanceof SimpleEnsemble) {
                            delegate.filter(item, minValue, maxValue, true);
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(ensembleList, "Unrecongnized value " + nfe.getLocalizedMessage());
                    }
                }
                if (filterFromPercentilField.isEnabled() && filterToPercentilLabel.isEnabled()) {
                    try {
                        double minValue = Double.parseDouble(filterFromPercentilField.getText());
                        double maxValue = Double.parseDouble(filterToPercentilField.getText());

                        Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);
                        Object obj = delegate.getItemForIdentifier(item);
                        if (obj instanceof SimpleEnsemble) {
                            delegate.filterPercentil(item, minValue, maxValue, true);
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(ensembleList, "Unrecongnized value " + nfe.getLocalizedMessage());
                    }
                }
            }
        });

        clearTimeFilterButton = new JButton("Clear Time-Filter");
        clearTimeFilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                delegate.clearTimeFilter();
            }
        });
        
        clearValueFilterButton = new JButton("Clear ID-Filters");
        clearValueFilterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                delegate.clearIDFilter();
            }
        });

        deleteFilteredValuesButton = new JButton("Delete");
        deleteFilteredValuesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(ensembleList, "Do you really want to delete all filtered items") == JOptionPane.YES_OPTION){
                    delegate.commitFilter();
                }
            }
        });

        mcat5Toolbar = new MCAT5Toolbar(this);
    }

    public void refreshView() {
        try {
            String[] columns = new String[]{"Ensembles"};
            DataType[] types = delegate.getAvailableDataTypes();
            DataType[][] entries = new DataType[types.length][1];
            int i;
            for (i = 0; i < types.length; i++) {
                entries[i][0] = types[i];
            }

            DefaultTableModel ensembleTableModel = new DefaultTableModel();
            ensembleTableModel.setDataVector(entries, columns);
            ensembleList.setModel(ensembleTableModel);
        } catch (Throwable t) {
            t.printStackTrace();
        }        
    }

    private void layoutComponents() {

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        mcat5Toolbar.setPreferredSize(new Dimension(250, 300));
        mcat5Toolbar.setSize(new Dimension(250, 300));

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(mcat5Toolbar)
                    .addGap(0, 1000, 100000)
                    .addComponent(closeButton)
                )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(ensembleListScrollPane)
                    .addComponent(dataSetListScrollPane)
                    .addGap(25)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(displayDataButton)
                            //.addComponent(closeButton)
                            .addComponent(sumTSData)
                            .addComponent(deleteAttribute)
                            )
                        .addGroup(layout.createSequentialGroup()
                            //.addComponent(displayPanel)
                            .addComponent(filterPanel)
                            )
                        )
                    //.addGap(50)
                    )
                    .addComponent(tableScrollPane)
                    .addComponent(showGraphButton));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(mcat5Toolbar)
                    .addComponent(closeButton)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(ensembleListScrollPane)
                    .addComponent(dataSetListScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(displayDataButton)
                            //.addComponent(closeButton)
                            .addComponent(sumTSData)
                            .addComponent(deleteAttribute)
                        )
                        .addGroup(layout.createParallelGroup()
                            //.addComponent(displayPanel)
                            .addComponent(filterPanel)
                            )
                        )
                    .addGap(25)
                    )
                    .addComponent(tableScrollPane)
                    .addComponent(showGraphButton)
                );

        layout = new GroupLayout(timeIntervalPanel);
        timeIntervalPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(startDateLabel)
                .addComponent(startDatePicker)
                .addComponent(finalDateLabel)
                .addComponent(finalDatePicker)
                .addGap(25)
                );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(startDateLabel)
                .addComponent(startDatePicker)
                .addGap(10)
                .addComponent(finalDateLabel)
                .addComponent(finalDatePicker)
                );

        layout = new GroupLayout(simIDPanel);
        simIDPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(fromIDLabel)
                .addComponent(fromIDSpinner)
                .addGap(25)
                .addComponent(toIDLabel)
                .addComponent(toIDSpinner));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(fromIDLabel)
                .addComponent(fromIDSpinner)
                .addComponent(toIDLabel)
                .addComponent(toIDSpinner));

        layout = new GroupLayout(displayPanel);
        displayPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(timeIntervalPanel)
                .addComponent(simIDPanel)
                .addComponent(displayDataButton));
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(timeIntervalPanel)
                .addComponent(simIDPanel)
                .addComponent(displayDataButton));

        layout = new GroupLayout(filterPanel);
        filterPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(filterTimeIntervalPanel)
                .addComponent(filterSimPanel)
                .addComponent(filterPercentilPanel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(filterButton)                    
                    .addComponent(inverseFilterButton)
                    .addComponent(deleteFilteredValuesButton)
                )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(clearTimeFilterButton)
                    .addComponent(clearValueFilterButton)
                )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(filterTimeIntervalPanel)
                .addComponent(filterSimPanel)
                .addComponent(filterPercentilPanel)
                .addGroup(layout.createParallelGroup()
                    .addComponent(filterButton)
                    .addComponent(inverseFilterButton)
                    .addComponent(deleteFilteredValuesButton)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(clearTimeFilterButton)
                    .addComponent(clearValueFilterButton)
                )
        );

        layout = new GroupLayout(filterTimeIntervalPanel);
        filterTimeIntervalPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(filterStartDateLabel)
                .addComponent(filterStartDatePicker)
                .addComponent(filterFinalDateLabel)
                .addComponent(filterFinalDatePicker)
        );

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(filterStartDateLabel)
                .addComponent(filterStartDatePicker)
                .addComponent(filterFinalDateLabel)
                .addComponent(filterFinalDatePicker)
        );

        layout = new GroupLayout(filterSimPanel);
        filterSimPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(filterFromValueLabel)
                .addComponent(filterFromValueField)
                .addComponent(filterToValueLabel)
                .addComponent(filterToValueField)
        );

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(filterFromValueLabel)
                .addComponent(filterFromValueField)
                .addComponent(filterToValueLabel)
                .addComponent(filterToValueField)
        );

        layout = new GroupLayout(filterPercentilPanel);
        filterPercentilPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(filterFromPercentilLabel)
                .addComponent(filterFromPercentilField)
                .addComponent(filterToPercentilLabel)
                .addComponent(filterToPercentilField)
        );

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(filterFromPercentilLabel)
                .addComponent(filterFromPercentilField)
                .addComponent(filterToPercentilLabel)
                .addComponent(filterToPercentilField)  
        );
    }

    public DataCollection getDataCollection() {
        if (this.delegate instanceof DataCollectionViewController) {
            DataCollectionViewController controller = (DataCollectionViewController) delegate;
            return controller.getDataCollection();
        }
        return null;
    }

    private void displayData(Object identifier) {

        delegate.itemIsBeingDisplayed(identifier);

        Object item = delegate.getItemForIdentifier(identifier);
        final Ensemble ensemble = (Ensemble) item;

        long tmp = 0;
        if (ensemble instanceof TimeSerieEnsemble){
            tmp = ((TimeSerieEnsemble) ensemble).getTimesteps();
        
        }
        
        /* calculate offset and number of timesteps for selected interval */
        final long offset=0;
        final long numberOfSteps = tmp;
        

        tableModel = new AbstractTableModel() {

            @Override
            public String getColumnName(int column) {
                if (ensemble instanceof TimeSerieEnsemble) {
                    switch (column) {
                        case 0:
                            return "Simulation ID";
                        default:
                            /*java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(startDatePicker.getDate());
                            cal.add(java.util.Calendar.DAY_OF_MONTH, column - 1);*/
                            return new SimpleDateFormat("dd.MM.yyyy").format(((TimeSerieEnsemble)ensemble).getDate(column-1));
                    }
                } else {
                    switch (column) {
                        case 0:
                            return "Simulation ID";
                        case 1:
                            return "Values";
                        default:
                            return null;
                    }
                }
            }

            @Override
            public int getRowCount() {
                return ensemble.getIds().length;
            }

            @Override
            public int getColumnCount() {
                if (ensemble instanceof TimeSerieEnsemble) {
                    /* number of timesteps plus one for ID column */
                    return (int) numberOfSteps + 1;
                } else {
                    return 2;
                }
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                int simIDOffset = (Integer) fromIDSpinner.getValue();

                if (ensemble.getSize()<=rowIndex + simIDOffset){
                    return null;
                }
                if (ensemble instanceof TimeSerieEnsemble) {
                    switch (columnIndex) {
                        case 0:
                            return ((TimeSerieEnsemble) ensemble).getId(rowIndex);
                        default:
                            return ((TimeSerieEnsemble) ensemble).get(columnIndex - 1 + (int) offset, ((TimeSerieEnsemble) ensemble).getId(rowIndex));
                    }
                } else {
                    switch (columnIndex) {
                        case 0:
                            return ((SimpleEnsemble) ensemble).getId(rowIndex);
                        case 1:
                            return ((SimpleEnsemble) ensemble).getValue(((SimpleEnsemble) ensemble).getId(rowIndex));
                        default:
                            return 0;
                    }
                }
            }
        };
        table.setModel(tableModel);
    }
}
