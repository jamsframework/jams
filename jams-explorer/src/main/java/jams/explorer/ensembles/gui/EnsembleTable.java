/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.explorer.ensembles.gui;

import jams.explorer.ensembles.api.Model;
import static jams.explorer.ensembles.gui.ClimateDataOverviewTab.logger;
import static jams.explorer.ensembles.gui.EnsembleControlPanel.logger;
import jams.explorer.ensembles.implementation.ClimateEnsemble;
import jams.explorer.ensembles.implementation.ClimateEnsembleProcessor;
import jams.explorer.ensembles.implementation.ClimateModel;
import java.awt.Color;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author christian
 */
public class EnsembleTable extends JTable {

    Logger logger = Logger.getLogger(EnsembleTable.class.getName());
    {
        EnsembleControlPanel.registerLogHandler(logger);
    }
    class EnsembleOutputFile {

        String outputFileName;

        Set<String> outputDirectories = new TreeSet<String>();

        EnsembleOutputFile(String outputFileName) {
            this.outputFileName = outputFileName;
        }

        void register(String outputDirectory) {
            outputDirectories.add(outputDirectory);
        }

        void remove(String outputDirectory) {
            outputDirectories.remove(outputDirectory);
        }

        Set<String> getOutputDirectories() {
            return Collections.unmodifiableSet(outputDirectories);
        }

        @Override
        public String toString() {
            return outputFileName;
        }
    }

    private class MyListSelectionModel extends DefaultListSelectionModel {

        public MyListSelectionModel() {
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        @Override
        public void fireValueChanged(int firstIndex, int lastIndex) {
            super.fireValueChanged(firstIndex, lastIndex);
        }
    }

    ClimateEnsemble ensemble;
    Object tableData[][] = null;

    public EnsembleTable(ClimateEnsemble ensemble) {
        setEnsemble(ensemble);
        this.setSelectionModel(new MyListSelectionModel());
        setSelectionBackground(new Color(128, 128, 255));
        this.setModel(getTableModel());
    }

    public void fireSelectionChangeNotification() {
        int index = getSelectedRow();
        ((MyListSelectionModel) getSelectionModel()).fireValueChanged(index, index);
    }

    public void setEnsemble(ClimateEnsemble ensemble) {
        logger.entering(getClass().getName(), "setEnsemble");
        this.ensemble = ensemble;
        this.setModel(getTableModel());
        logger.exiting(getClass().getName(), "setEnsemble");
    }

    public String getOutput(int row) {
        if (tableData == null) {
            return null;
        }
        return tableData[row][1].toString();
    }

    public Set<String> getSelectedOutputs() {
        TreeSet<String> selection = new TreeSet<String>();
        for (Object row[] : tableData) {
            if ((Boolean) row[0] == true) {
                selection.add(row[1].toString());
            }
        }
        return selection;
    }

    @Override
    public void selectAll() {
        for (Object[] tableData1 : this.tableData) {
            tableData1[0] = true;
        }
        ((DefaultTableModel) getModel()).fireTableDataChanged();
    }

    public void deselectAll() {
        for (Object[] tableData1 : this.tableData) {
            tableData1[0] = false;
        }
        ((DefaultTableModel) getModel()).fireTableDataChanged();
    }

    public void invertSelection() {
        for (Object[] tableData1 : this.tableData) {
            tableData1[0] = !(Boolean) tableData1[0];
        }
        ((DefaultTableModel) getModel()).fireTableDataChanged();
    }

    private TableModel getTableModel() {
        if (ensemble == null) {
            return new DefaultTableModel(new String[]{"", "", "", "", ""}, 1);
        }

        logger.entering(getClass().getName(), "getTableModel()");

        TreeMap<String, EnsembleOutputFile> outputs = new TreeMap<String, EnsembleOutputFile>();

        for (ClimateModel model : ensemble.getModelSet()) {
            for (String outputDir : model.getOutputs()) {
                for (File outputFile : model.getOutputFiles(outputDir)) {
                    EnsembleOutputFile output = outputs.get(outputFile.getName());
                    if (output == null) {
                        output = new EnsembleOutputFile(outputFile.getName());
                    }
                    output.register(outputDir);
                    outputs.put(output.toString(), output);
                }
            }

            model.addModelDataChangeListener(new Model.ModelDataChangeListener() {

                @Override
                public void changed(Model model, String key) {
                    if (key.compareTo("output") != 0) {
                        return;
                    }
                    logger.entering(getClass().getName(), "changed()");

                    int oldSelection = getSelectedRow();

                    TreeMap<String, EnsembleOutputFile> outputs = new TreeMap<String, EnsembleOutputFile>();
                    for (ClimateModel model2 : ensemble.getModelSet()) {
                        for (String outputDir : model2.getOutputs()) {
                            for (File outputFile : model2.getOutputFiles(outputDir)) {
                                EnsembleOutputFile output = outputs.get(outputFile.getName());
                                if (output == null) {
                                    output = new EnsembleOutputFile(outputFile.getName());
                                }
                                output.register(outputDir);
                                outputs.put(output.toString(), output);
                            }
                        }
                    }
                    int i = 0;
                    for (EnsembleOutputFile e : outputs.values()) {
                        tableData[i][0] = true;
                        tableData[i][1] = e;
                        try {
                            ClimateEnsembleProcessor proc = new ClimateEnsembleProcessor(ensemble, e.outputFileName);
                            tableData[i][2] = proc.getNettoModelCount(e.outputFileName);
                        } catch (Throwable t) {
                            tableData[i][2] = 0;
                            logger.log(Level.WARNING, "Oh, while reading the ensemble output, an error occured", t);
                        }
                        i++;
                    }
                    getSelectionModel().setSelectionInterval(oldSelection, oldSelection);

                    fireSelectionChangeNotification();

                    logger.exiting(getClass().getName(), "changed()");
                }
            });
        }
        tableData = new Object[outputs.size()][3];
        int i = 0;
        for (EnsembleOutputFile e : outputs.values()) {
            tableData[i][0] = true;
            tableData[i][1] = e;
            try {
                ClimateEnsembleProcessor proc = new ClimateEnsembleProcessor(ensemble, e.outputFileName);
                tableData[i][2] = proc.getNettoModelCount(e.outputFileName);
            } catch (Throwable t) {
                tableData[i][2] = 0;
                logger.log(Level.WARNING, "Oh, while reading the ensemble output, an error occured", t);
            }
            i++;
        }

        DefaultTableModel model = new DefaultTableModel(tableData, new String[]{"Selected", "Time Periode", "Group", "Name", "Datasets"}) {
            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                    case 0:
                        return Boolean.class;
                    case 4:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == 0) {
                    return true;
                }
                return false;
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                if (col == 0) {
                    try {
                        tableData[row][0] = Boolean.parseBoolean(value.toString());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == 0) {
                    return tableData[row][0];
                } else {
                    String s = super.getValueAt(row, 1).toString();
                    if (col == 1) {
                        if (s.contains("Jaehrlich")) {
                            return "jährlich";
                        }
                        if (s.contains("Halbjaehrliche")) {
                            return "halbjährlich";
                        }
                        if (s.contains("Jahreszeitlich")) {
                            return "jahreszeitliche";
                        }
                        if (s.contains("Taegliche")) {
                            return "tägliche";
                        }
                        if (s.contains("Dekadisch-jahreszeitliche")) {
                            return "dekadisch-jahreszeitlich";
                        }
                        if (s.contains("Dekadische")) {
                            return "dekadisch";
                        }
                        if (s.contains("monatliche")) {
                            return "monatlich";
                        }
                        if (s.contains("Aggregationsintervalle")) {
                            return "benutzerdefiniert";
                        }
                        return "";
                    }
                    if (col == 2) {
                        if (s.contains("Klimaparameter")) {
                            return "Klimaparameter";
                        }
                        if (s.contains("Klimakennwerte")) {
                            return "Klimakennwert";
                        }
                        if (s.contains("Tau-Wert")) {
                            return "Tau-Wert";
                        }
                        if (s.contains("Gleitendes Mittel")) {
                            return "Gleitendes Mittel";
                        }
                        if (s.contains("P-Werte")) {
                            return "P-Wert";
                        }
                    }
                    if (col == 3) {
                        int index1 = s.lastIndexOf("_");
                        if (index1 == -1) {
                            return s;
                        }
                        s = s.substring(0, index1);
                        int index2 = s.lastIndexOf("_");
                        if (index2 == -1) {
                            return s;
                        }
                        return s.substring(index2 + 1);
                    }
                    if (col == 4) {
                        return tableData[row][2].toString();
                    }
                }
                return "";
            }
        };

        logger.exiting(getClass().getName(), "getTableModel()");

        return model;
    }
}
