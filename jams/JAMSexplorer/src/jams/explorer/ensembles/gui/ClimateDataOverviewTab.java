/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import jams.aggregators.Aggregator;
import jams.aggregators.DoubleAggregator;
import jams.aggregators.DoubleAggregator.MedianAggregator;
import jams.data.Attribute;
import jams.explorer.ensembles.implementation.ClimateEnsembleProcessor;
import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
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
 * zeigt das Flächenmittel für alle Ensembelmember an
 */
public class ClimateDataOverviewTab extends AbstractClimateDataTab {

    static final Logger logger = Logger.getLogger(ClimateDataOverviewTab.class.getName());

    {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    ClimateEnsembleProcessor proc = null;
    JTable table;

    public ClimateDataOverviewTab(String name) {
        super(name);
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

        proc = new ClimateEnsembleProcessor(ensemble, output);
        proc.init();

        try {
            Attribute.Calendar dates[] = proc.getTimeDomain();

            int offset = 7;
            String columnNames[] = new String[n + offset];
            columnNames[0] = "Date";
            columnNames[1] = "Mean";
            columnNames[2] = "Median";
            columnNames[3] = "Q5";
            columnNames[4] = "Q95";
            columnNames[5] = "Spannweite";
            columnNames[6] = "Varianz";

            for (int i = offset; i < n + offset; i++) {
                columnNames[i] = ensemble.getModel(i - offset).toString();
            }

            DefaultTableModel tableModel = new DefaultTableModel(columnNames, dates.length) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }

                @Override
                public Class getColumnClass(int col) {
                    return col == 0 ? String.class : Double.class;
                }
            };
            for (int i = 0; i < dates.length; i++) {
                tableModel.setValueAt(dates[i], i, 0);
            }

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            DoubleAggregator avgAggregator = DoubleAggregator.create(Aggregator.AggregationMode.AVERAGE);
            DoubleAggregator varAggregator = DoubleAggregator.create(Aggregator.AggregationMode.VARIANCE);
            MedianAggregator medianAggregator = (MedianAggregator) DoubleAggregator.create(Aggregator.AggregationMode.MEDIAN);

            double result[][] = proc.getSpatialMean();
            for (int i = 0; i < dates.length; i++) {
                tableModel.setValueAt(dates[i], i, 0);
                avgAggregator.init();
                medianAggregator.init();
                varAggregator.init();

                for (int j = 0; j < n; j++) {
                    avgAggregator.consider(result[j][i]);
                    medianAggregator.consider(result[j][i]);
                    varAggregator.consider(result[j][i]);
                    tableModel.setValueAt(result[j][i], i, j + offset);
                }
                avgAggregator.finish();
                medianAggregator.finish();
                varAggregator.finish();
                tableModel.setValueAt(avgAggregator.get(), i, 1);
                tableModel.setValueAt(medianAggregator.get(), i, 2);

                tableModel.setValueAt(medianAggregator.getQuantile(0.05), i, 3);
                tableModel.setValueAt(medianAggregator.getQuantile(0.95), i, 4);

                double spannweite = medianAggregator.getQuantile(1.00)
                        - medianAggregator.getQuantile(0.00);

                tableModel.setValueAt(spannweite, i, 5);
                tableModel.setValueAt(varAggregator.get(), i, 6);
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Sorry, an error occured during aggregation!", e);
        }

        logger.exiting(this.getClass().getName(), "calculate()");
    }

    public class DoubleRenderer extends DefaultTableCellRenderer {

        public DoubleRenderer() {
            super();
        }

        public void setValue(Object value) {
            setText(value == null ? "" : String.format(Locale.ENGLISH, "%.2f", value));

        }

    }
}
