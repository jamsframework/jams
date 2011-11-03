package reg.gui;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import optas.gui.DataCollectionPanel;
import optas.gui.MCAT5.MCAT5Toolbar;
import org.jdesktop.swingx.JXDatePicker;
import optas.hydro.data.Ensemble;
import optas.hydro.data.SimpleEnsemble;
import optas.hydro.data.TimeSerieEnsemble;
import optas.hydro.data.DataCollection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import reg.DataCollectionViewController;

public class DataCollectionView extends JComponent implements DataCollectionPanel {
    
    public enum DataType {
        TIME_SERIES,
        MEASUREMENT,
        OBJECTIVE,
        VARIABLE,
        PARAMETER;
        
        @Override
        public String toString() {
            if (name().equals("TIME_SERIES")) {
                return "Time Series";
            } else if (name().equals("MEASUREMENT")) {
                return "Measurement";
            } else if (name().equals("OBJECTIVE")) {
                return "Objective";
            } else if (name().equals("VARIABLE")) {
                return "Variable";
            } else if (name().equals("PARAMETER")) {
                return "Parameter";
            } else {
                return null;
            }
        }
    }
    
    private DataCollectionViewController delegate;
    
    private Object[] fetchedDataSets = null;
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
    
    private JButton displayDataButton = null;
    private JButton closeButton = new JButton("Close Tab");
    
    private JLabel startDateLabel = null;
    private JLabel finalDateLabel = null;
    private JLabel minimumDateLabel = null;
    private JLabel maximumDateLabel = null;
    private JXDatePicker startDatePicker = null;
    private JXDatePicker finalDatePicker = null;
    private TitledBorder enabledTimeIntervalPanelBorder = null;
    private TitledBorder disabledTimeIntervalPanelBorder = null;
    private JPanel timeIntervalPanel = null;

    private JLabel fromIDLabel = null;
    private JLabel toIDLabel = null;
    private JSpinner fromIDSpinner = null;
    private JSpinner toIDSpinner = null;
    private TitledBorder enabledIDPanelBorder = null;
    private TitledBorder disabledIDPanelBorder = null;
    private JPanel simIDPanel = null;

    private JTable table = null;
    private TableModel defaultTableModel = null;
    private TableModel tableModel = null;
    private JScrollPane tableScrollPane = null;
    private JButton showGraphButton = null;

    private MCAT5Toolbar mcat5Toolbar;
    
    private void initComponents() {

        /* ensemble list */
        String [] columns = new String[]{"Ensembles"};
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
                int row = ensembleList.getSelectedRow();

                final DataType type = (DataType) ensembleList.getValueAt(row, 0);
                fetchedDataSets = delegate.getItemIdentifiersForDataType(type);
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
                if (ensembleList.getSelectedRow() == -1) {
                    return 1;
                } else {
                    if (fetchedDataSets==null)
                        return 0;
                    return fetchedDataSets.length;
                }
            }
            
            @Override
            public Object getValueAt(int row, int column) {
                if (ensembleList.getSelectedRow() == -1) {
                    return "Select Ensemble...";
                } else {
                    return fetchedDataSets[row];
                }
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (dataSetList.getSelectedRow() != -1) {

                    /* reset value table and disable graph button */
                    table.setModel(defaultTableModel);
                    showGraphButton.setEnabled(false);

                    Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);

                    if (delegate.hasTimeInterval(item)) {

                        /* enable time interval panel */
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

                        /* display boundaries of given time interval */
                        minimumDateLabel.setText(maximumInterval.getStart().toString(new SimpleDateFormat("dd.MM.yyyy")));
                        maximumDateLabel.setText(maximumInterval.getEnd().toString(new SimpleDateFormat("dd.MM.yyyy")));
                    }

                    /* enable simulation id panel */
                    simIDPanel.setBorder(enabledIDPanelBorder);
                    fromIDLabel.setForeground(Color.BLACK);
                    toIDLabel.setForeground(Color.BLACK);

                    /* enable id spinners */
                    fromIDSpinner.setEnabled(true);
                    toIDSpinner.setEnabled(true);

                    /* configure id spinners */
                    Ensemble ensemble = (Ensemble) delegate.getItemForIdentifier(item);
                    Object[] values = new Object[ensemble.getSize()];
                    int i;
                    for (i = 0; i < ensemble.getSize(); i++) {
                        values[i] = ensemble.getId(i);
                    }

                    final SpinnerListModel fromIDSpinnerListModel = new SpinnerListModel(values);
                    fromIDSpinnerListModel.setValue(values[0]);
                    fromIDSpinner.setModel(fromIDSpinnerListModel);

                    final SpinnerListModel toIDSpinnerListModel = new SpinnerListModel(values);
                    toIDSpinnerListModel.setValue(values[values.length - 1]);
                    toIDSpinner.setModel(toIDSpinnerListModel);

                    ChangeListener changeListener  = new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {

                            /* reset value table on change */
                            table.setModel(defaultTableModel);
                            showGraphButton.setEnabled(false);

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

                    displayDataButton.setEnabled(true);
                }
            }
        });
        dataSetListScrollPane = new JScrollPane(dataSetList);

        enabledTimeIntervalPanelBorder = new TitledBorder(null, " Time interval ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledTimeIntervalPanelBorder = new TitledBorder(null, " Time interval ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);
        timeIntervalPanel = new JPanel();
        timeIntervalPanel.setBorder(disabledTimeIntervalPanelBorder);

        startDateLabel = new JLabel("From:");
        startDateLabel.setForeground(Color.GRAY);
        finalDateLabel = new JLabel("To:");
        finalDateLabel.setForeground(Color.GRAY);

        startDatePicker = new JXDatePicker(System.currentTimeMillis());
        finalDatePicker = new JXDatePicker(System.currentTimeMillis());
        startDatePicker.setEnabled(false);
        finalDatePicker.setEnabled(false);
        ActionListener datePickerActionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                /* reset table and disable show graph button */
                table.setModel(defaultTableModel);
                showGraphButton.setEnabled(false);

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
                if (validStartDate && validFinalDate) {
                    displayDataButton.setEnabled(true);
                } else {
                    displayDataButton.setEnabled(false);
                }

                /* highlight mismatch */
                minimumDateLabel.setForeground(validStartDate ? Color.GRAY : Color.RED);
                maximumDateLabel.setForeground(validFinalDate ? Color.GRAY : Color.RED);
            }
        };
        startDatePicker.addActionListener(datePickerActionListener);
        finalDatePicker.addActionListener(datePickerActionListener);

        minimumDateLabel = new JLabel("--.--.----");
        maximumDateLabel = new JLabel("--.--.----");
        minimumDateLabel.setForeground(Color.GRAY);
        maximumDateLabel.setForeground(Color.GRAY);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Simulation ID panel">
        enabledIDPanelBorder = new TitledBorder(null, " Simulation ID ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.BLACK);
        disabledIDPanelBorder = new TitledBorder(null, " Simulation ID ", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, Color.GRAY);
        simIDPanel = new JPanel();
        simIDPanel.setBorder(disabledIDPanelBorder);

        fromIDLabel = new JLabel("From:");
        fromIDLabel.setForeground(Color.GRAY);
        toIDLabel = new JLabel("To:");
        toIDLabel.setForeground(Color.GRAY);

        fromIDSpinner = new JSpinner();
        fromIDSpinner.setEnabled(false);
        toIDSpinner = new JSpinner();
        toIDSpinner.setEnabled(false);

        displayDataButton = new JButton(new AbstractAction("Display") {

            @Override
            public void actionPerformed(ActionEvent e) {

                Object item = dataSetList.getValueAt(dataSetList.getSelectedRow(), 0);

                if (!delegate.hasTimeInterval(item)) {
                    showGraphButton.setEnabled(true);
                }

                displayData(dataSetList.getValueAt(dataSetList.getSelectedRow(), 0));
            }
        });
        displayDataButton.setEnabled(false);

        closeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Component c = DataCollectionView.this.getParent();
                if (c!=null){
                    if (c instanceof JTabbedPane){
                        JTabbedPane pane = (JTabbedPane)c;
                        pane.remove(DataCollectionView.this);
                    }
                }
            }
        });
                        
        columns = new String[]{"Simulation ID", "Timestep"};
        String[][] data = new String[][]{};
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

                        int numberOfColumns = tableModel.getColumnCount();
                        int[] rows = table.getSelectedRows();
                        for (int row : rows) {
                            TimeSeries series = new TimeSeries("time series #" + row, Day.class);
                            int i;
                            for (i = 1; i < numberOfColumns; i++) {
                                Date date = DateFormat.getDateInstance().parse(tableModel.getColumnName(i));
                                series.add(new Day(date), (Double) tableModel.getValueAt(row, i));
                            }
                            collection.addSeries(series);
                        }
                        chart = ChartFactory.createTimeSeriesChart(itemName, null, null, collection, false, false, false);
                    } else {

                        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

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
                        for (int row : rows) {
                            int id = (Integer) tableModel.getValueAt(row, 0);
                            double value = (Double) tableModel.getValueAt(row, 1);
                            dataset.addValue(value, itemName, String.valueOf(id));
                        }

                        chart = ChartFactory.createBarChart(itemName, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
                    }

                    /* setup chart view and show window */
                    ChartPanel chartPanel = new ChartPanel(chart);
                    chartWindow.getContentPane().add(chartPanel);
                    chartWindow.setVisible(true);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });

        mcat5Toolbar = new MCAT5Toolbar(this);
    }

    public void refreshView(){
        try{
        String [] columns = new String[]{"Ensembles"};
        DataType[] types = delegate.getAvailableDataTypes();
        DataType[][] entries = new DataType[types.length][1];
        int i;
        for (i = 0; i < types.length; i++) {
            entries[i][0] = types[i];
        }

        DefaultTableModel ensembleTableModel = new DefaultTableModel();
        ensembleTableModel.setDataVector(entries,columns);
        ensembleList.setModel(ensembleTableModel);
        }catch(Throwable t){
            t.printStackTrace();
        }
    }

    private void layoutComponents() {

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        Dimension d = timeIntervalPanel.getSize();
        mcat5Toolbar.setPreferredSize(new Dimension(250,300));
        mcat5Toolbar.setSize(new Dimension(250,300));
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(mcat5Toolbar)
            .addGroup(layout.createSequentialGroup()
                .addComponent(ensembleListScrollPane)
                .addComponent(dataSetListScrollPane)
                .addGap(50)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(closeButton)
                .addComponent(timeIntervalPanel)
                .addComponent(simIDPanel)
                .addComponent(displayDataButton)
                )
                
                .addGap(50)
            )            
            .addComponent(tableScrollPane)
            .addComponent(showGraphButton)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(mcat5Toolbar)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(ensembleListScrollPane)
                .addComponent(dataSetListScrollPane)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(closeButton)
                    .addComponent(timeIntervalPanel)
                    .addComponent(simIDPanel)
                    .addComponent(displayDataButton)
                )

            )
            .addGap(25)
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
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(minimumDateLabel)
                    .addComponent(maximumDateLabel)
                    )
        );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
               .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(startDateLabel)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(startDatePicker)
                    .addComponent(minimumDateLabel)
                )
                .addGap(10)
                .addComponent(finalDateLabel)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(finalDatePicker)
                    .addComponent(maximumDateLabel)
                )
                
        );

        layout = new GroupLayout(simIDPanel);
        simIDPanel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(fromIDLabel)
                    .addComponent(fromIDSpinner)
                )
                .addGap(25)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(toIDLabel)
                    .addComponent(toIDSpinner)
                )
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fromIDLabel)
                    .addComponent(toIDLabel)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fromIDSpinner)
                    .addComponent(toIDSpinner)
                )
        );
    }

    public DataCollection getDataCollection(){
        if (this.delegate instanceof DataCollectionViewController){
            DataCollectionViewController controller = (DataCollectionViewController)delegate;
            return controller.getDataCollection();
        }
        return null;
    }

    private void displayData(Object identifier) {
        
        delegate.itemIsBeingDisplayed(identifier);
        
        Object item = delegate.getItemForIdentifier(identifier);
        final Ensemble ensemble = (Ensemble) item;

        /* calculate offset and number of timesteps for selected interval */
        final long offset;
        final long numberOfSteps;
        if (maximumInterval != null) {
            java.util.Calendar startCal = (java.util.Calendar) maximumInterval.getStart();
            startCal.set(java.util.Calendar.HOUR, 0);
            startCal.set(java.util.Calendar.MINUTE, 0);
            offset = (startDatePicker.getDateInMillis() - startCal.getTimeInMillis()) / 1000 / 60 / 60 / 24;
            numberOfSteps = (finalDatePicker.getDateInMillis() - startDatePicker.getDateInMillis()) / 1000 / 60 / 60 / 24 + 1;
        } else {
            offset = -1;
            numberOfSteps = -1;
        }

        tableModel = new AbstractTableModel() {

            @Override
            public String getColumnName(int column) {
                if (ensemble instanceof TimeSerieEnsemble) {
                    switch (column) {
                        case 0:
                            return "Simulation ID";
                        default:
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(startDatePicker.getDate());
                            cal.add(java.util.Calendar.DAY_OF_MONTH, column - 1);
                            return new SimpleDateFormat("dd.MM.yyyy").format(cal.getTime());
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
                int fromID = (Integer) fromIDSpinner.getValue();
                int toID = (Integer) toIDSpinner.getValue();
                return toID - fromID + 1;
            }

            @Override
            public int getColumnCount() {
                if (ensemble instanceof TimeSerieEnsemble) {
                    /* number of timesteps plus one for ID column */
                    return (int)numberOfSteps + 1;
                } else {
                    return 2;
                }
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                int simIDOffset = (Integer) fromIDSpinner.getValue();
                if (ensemble instanceof TimeSerieEnsemble) {
                    switch (columnIndex) {
                        case 0:
                            return ((TimeSerieEnsemble) ensemble).getId(rowIndex + simIDOffset);
                        default:
                            return ((TimeSerieEnsemble) ensemble).get(columnIndex - 1 + (int)offset, rowIndex + simIDOffset);
                    }
                } else {
                    switch (columnIndex) {
                        case 0:
                            return ((SimpleEnsemble) ensemble).getId(rowIndex + simIDOffset);
                        case 1:
                            return ((SimpleEnsemble) ensemble).getValue(rowIndex + simIDOffset);
                        default:
                            return 0;
                    }
                }
            }
        };
        table.setModel(tableModel);
    }
}
