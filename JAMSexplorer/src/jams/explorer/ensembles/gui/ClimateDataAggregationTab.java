/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import jams.aggregators.Aggregator.AggregationMode;
import jams.data.Attribute.TimeInterval;
import jams.explorer.ensembles.implementation.ClimateEnsemble.ClimateDataSupplier;
import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author christian
 *
 * zeigt das Flächenmittel für alle Ensemblemember an
 */
public class ClimateDataAggregationTab extends AbstractClimateDataTab {

    JTable table;

    AggregationMode mode;
    Double modeParameter;
    EnsembleControlPanel parent;

    static final Logger logger = Logger.getLogger(ClimateDataAggregationTab.class.getName());
    {
        EnsembleControlPanel.registerLogHandler(logger);
    }

    public ClimateDataAggregationTab(String name, AggregationMode mode, Double modeParameter, EnsembleControlPanel parent) {
        super(name);

        this.mode = mode;
        this.modeParameter = modeParameter;
        this.parent = parent;
        
        table = new JTable();
        JScrollPane pane = new JScrollPane(table,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.setLayout(new BorderLayout());
        this.add(pane, BorderLayout.CENTER);
    }

    @Override
    public void calculate() {
        logger.entering(this.getClass().getName(), "calculate()");
        isChanged = false;

        if (output == null) {
            table.setModel(new DefaultTableModel(1, 1));
        }

        ClimateDataSupplier<Double>[] data = ensemble.aggregateEnsemble(output, mode, modeParameter, parent.getRefPeriod());
        if (data == null) {
            return;
        }
        int T = data.length;
        if (T == 0) {
            return;
        }
        int K = data[0].size();
        String columnNames[] = new String[1 + K];
        columnNames[0] = "Date";
        for (int i = 0; i < K; i++) {
            columnNames[i + 1] = Long.toString(data[0].getEntityIDs()[i]);
        }

        DefaultTableModel tableModel = new DefaultTableModel(columnNames, T) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class getColumnClass(int col) {
                return col == 0 ? String.class : Double.class;
            }
        };

        for (int i = 0; i < T; i++) {
            tableModel.setValueAt(data[i].getName(), i, 0);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < T; i++) {
            for (int j = 0; j < K; j++) {
                int id = (int) data[i].getEntityIDs()[j];
                tableModel.setValueAt(data[i].get(id), i, j + 1);
            }
        }

        table.setModel(tableModel);
        table.setDefaultRenderer(double.class, new DoubleRenderer());
        table.setDefaultRenderer(Double.class, new DoubleRenderer());

        Enumeration<TableColumn> e = table.getColumnModel().getColumns();
        if (e.hasMoreElements()) {
            TableColumn col = e.nextElement();
            col.setMinWidth(150);
        }
        while (e.hasMoreElements()) {
            TableColumn col = e.nextElement();
            col.setMinWidth(100);
        }
        logger.exiting(this.getClass().getName(), "calculate()");
    }

    public class DoubleRenderer extends DefaultTableCellRenderer {

        public DoubleRenderer() {
            super();
        }

        @Override
        public void setValue(Object value) {
            setText(value == null ? "" : String.format(Locale.ENGLISH, "%.2f", value));
        }

    }
}
