/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package reg.gui;

import jams.JAMS;
import jams.gui.WorkerDlg;
import jams.workspace.dsproc.AbstractDataStoreProcessor.AttributeData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import jams.workspace.dsproc.Processor;
import javax.swing.JOptionPane;
import optas.data.DataCollection;
import optas.io.ImportMonteCarloData;
import optas.io.ImportMonteCarloData.EnsembleType;
import optas.io.ImportMonteCarloException;


/**
 *
 * @author chris
 */
public class ImportMonteCarloDataPanel extends JPanel {

    enum MergeMode {

        ATTACH, //attaches the new datacollection at the end of the old one
        UNIFY       //unifies the attributes of both datacollections
    };
    Dimension defaultDatasetTable = new Dimension(500, 200);
    Dimension defaultWindowSize = new Dimension(550, 290);
    JPanel dataPanel = null;
    JComboBox mergeModeBox = new JComboBox(new String[]{JAMS.i18n("Attach_Mode"), JAMS.i18n("Unify_Mode")});
    ArrayList<ActionListener> listenerList = new ArrayList<ActionListener>();
    HashMap<AttributeData, JComboBox> attributeComboBoxMap = new HashMap<AttributeData, JComboBox>();
    JFrame owner;
    JDialog ownerDlg = null;
    DataCollection importedCollection = null;
    ImportMonteCarloData importer = null;
    DataCollection finalEnsemble = null;

    private class EnsembleTypeStringMap {

        EnsembleType t;

        public EnsembleTypeStringMap(EnsembleType t) {
            this.t = t;
        }

        public EnsembleType get() {
            return t;
        }

        @Override
        public String toString() {
            switch (t) {
                case Parameter:
                    return JAMS.i18n("Parameter");
                case PosEfficiency:
                    return JAMS.i18n("Efficiency(Positive)");
                case NegEfficiency:
                    return JAMS.i18n("Efficiency(Negative)");
                case Measurement:
                    return JAMS.i18n("Measurement");
                case Timeserie:
                    return JAMS.i18n("Timeserie-Ensemble");
                case StateVariable:
                    return JAMS.i18n("State-Variable");
                case Ignore:
                    return "";
            }
            return "";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EnsembleTypeStringMap) {
                if (((EnsembleTypeStringMap) obj).get().equals(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    public ImportMonteCarloDataPanel(JFrame owner) {
        this.owner = owner;
        importer = new ImportMonteCarloData();

        init();
    }

    public ImportMonteCarloDataPanel(JFrame owner, DataCollection dc) {
        this.owner = owner;
        importer = new ImportMonteCarloData();
        finalEnsemble = dc;
        init();
    }

    public ImportMonteCarloDataPanel(JFrame owner, DataCollection dc, File file) {
        this.owner = owner;
        importer = new ImportMonteCarloData();
        finalEnsemble = dc;
        if (file.getAbsolutePath().endsWith("cdat")) {
            importedCollection = DataCollection.createFromFile(file);
        }
        init();
        if (!file.getAbsolutePath().endsWith("cdat")) {
            addFile(file);
        }
    }

    private void showError(ImportMonteCarloException imce) {
        JOptionPane.showMessageDialog(dataPanel, imce.toString(), JAMS.i18n("An_error_occured..."), JOptionPane.ERROR_MESSAGE);
    }

    public JDialog getDialog() {
        ownerDlg = new JDialog(this.owner, JAMS.i18n("Import_Ensemble_Data"));
        ownerDlg.add(this);
        ownerDlg.setPreferredSize(defaultWindowSize);
        ownerDlg.setMinimumSize(defaultWindowSize);

        return ownerDlg;
    }

    private MergeMode getMergeMode() {
        if (this.mergeModeBox.getSelectedIndex() == 0) {
            return MergeMode.ATTACH;
        } else {
            return MergeMode.UNIFY;
        }
    }

    public boolean isEmpty() {
        return importer.isEmpty();
    }

    private void updateDataTable() {
        dataPanel.removeAll();
        dataPanel.setLayout(new GridBagLayout());

        TreeSet<AttributeData> attributes = importer.getAttributeData();

        int counter = 0;
        for (AttributeData a : attributes) {
            Processor p = importer.getProcessorForAttribute(a);

            JComboBox typeSelection = this.attributeComboBoxMap.get(a);
            if (typeSelection == null) {
                EnsembleType options[] = importer.getValidProcessingOptions(p);
                EnsembleTypeStringMap maps[] = new EnsembleTypeStringMap[options.length];
                for (int i = 0; i < options.length; i++) {
                    maps[i] = new EnsembleTypeStringMap(options[i]);
                }
                typeSelection = new JComboBox(maps);
                if (importer.getDefaultAttributeType(a) != null){
                    typeSelection.setSelectedItem(new EnsembleTypeStringMap(importer.getDefaultAttributeType(a)));
                }
                typeSelection.setPreferredSize(new Dimension(175, 25));
                typeSelection.setMaximumSize(new Dimension(175, 25));
                this.attributeComboBoxMap.put(a, typeSelection);
            }
            typeSelection.putClientProperty("attribute", a);
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = counter;

            c.gridx = 0;
            c.anchor = GridBagConstraints.WEST;
            c.ipadx = 10;
            JLabel lbl = new JLabel(a.getName());
            lbl.setHorizontalTextPosition(SwingConstants.LEFT);
            dataPanel.add(lbl, c);
            c.gridx = 1;
            dataPanel.add(new JLabel(p.getDataStoreProcessor().getFile().getName()), c);

            c.gridx = 2;
            dataPanel.add(typeSelection, c);
            counter++;
        }
        dataPanel.invalidate();
        dataPanel.updateUI();
    }

    private abstract class CustomRunnable implements Runnable {

        private Object customData;

        public CustomRunnable(Object o) {
            setCustomData(o);
        }

        public void setCustomData(Object o) {
            this.customData = o;
        }

        public Object getCustomData() {
            return this.customData;
        }
    }

    private void addFile(File file) {
        WorkerDlg progress = new WorkerDlg(ImportMonteCarloDataPanel.this.owner, "Import Data");
        progress.setInderminate(true);

        progress.setTask(new CustomRunnable(file) {
            public void run() {
                try {
                    importer.addFile((File) this.getCustomData());
                    //updateFileTable();
                    updateDataTable();
                } catch (ImportMonteCarloException imce) {
                    showError(imce);
                }

            }
        });
        progress.execute();
    }

    public DataCollection getEnsemble() {
        return finalEnsemble;
    }

    private Component createDataSetOverview() {
        dataPanel = new JPanel(new BorderLayout());
        JScrollPane datasetScroll = new JScrollPane(dataPanel);
        datasetScroll.setSize(defaultDatasetTable);
        datasetScroll.setMinimumSize(defaultDatasetTable);
        datasetScroll.setPreferredSize(defaultDatasetTable);
        return datasetScroll;
    }

    public void addActionEventListener(ActionListener listener) {
        this.listenerList.add(listener);
    }

    private void init() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonBar = new JPanel(new FlowLayout());

        panel.add(createDataSetOverview(), BorderLayout.CENTER);

        mergeModeBox.setSelectedIndex(1);
        buttonBar.add(mergeModeBox);

        JButton okButton = new JButton(JAMS.i18n("OK"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WorkerDlg progress = new WorkerDlg(ImportMonteCarloDataPanel.this.owner, JAMS.i18n("Import_Data"));
                progress.setInderminate(true);
                progress.setTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (AttributeData a : attributeComboBoxMap.keySet()) {
                                JComboBox b = attributeComboBoxMap.get(a);
                                importer.setType(a, ((EnsembleTypeStringMap) b.getSelectedItem()).get());
                            }
                            DataCollection newCollection = importer.getEnsemble();

                            if (finalEnsemble == null) {
                                finalEnsemble = newCollection;
                            } else {
                                switch (getMergeMode()) {
                                    case ATTACH: {
                                        finalEnsemble.mergeDataCollections(newCollection);
                                        break;
                                    }
                                    case UNIFY: {
                                        finalEnsemble.unifyDataCollections(newCollection);
                                        break;
                                    }
                                }
                            }

                            ImportMonteCarloDataPanel.this.setVisible(false);
                            if (ImportMonteCarloDataPanel.this.ownerDlg != null) {
                                ImportMonteCarloDataPanel.this.ownerDlg.setVisible(false);
                            }
                            for (ActionListener listener : listenerList) {
                                listener.actionPerformed(new ActionEvent(ImportMonteCarloDataPanel.this, ActionEvent.ACTION_PERFORMED, "cmd"));
                            }
                            importer.finish();
                        } catch (Throwable t) {
                            t.printStackTrace();
                            showError(new ImportMonteCarloException(JAMS.i18n("An_error_occured_while_building_the_final_ensemble"), t));
                        }
                    }
                });
                progress.execute();
            }
        });

        buttonBar.add(okButton);
        buttonBar.add(new JButton(JAMS.i18n("CANCEL")) {
            {
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (ImportMonteCarloDataPanel.this.ownerDlg != null) {
                            ImportMonteCarloDataPanel.this.ownerDlg.setVisible(false);
                        }
                    }
                });
            }
        });
        panel.add(buttonBar, BorderLayout.SOUTH);
        this.setLayout(new BorderLayout());
        this.add(panel, BorderLayout.CENTER);
        setSize(defaultWindowSize);
    }

    /*public static void main(String[] args) throws Exception {
        String filename = "E:/ModelData/testWrite.nc";
        NetcdfFileWriteable ncfile = NetcdfFileWriteable.createNew(filename, false);
        // add dimensions
        ucar.nc2.Dimension latDim = ncfile.addDimension("lat", 64);
        ucar.nc2.Dimension lonDim = ncfile.addDimension("lon", 128);
        // define Variable
        ArrayList dims = new ArrayList();
        dims.add(latDim);
        dims.add(lonDim);
        ncfile.addVariable("temperature", DataType.DOUBLE, dims);
        ncfile.addVariableAttribute("temperature", "units", "K");
        // add a 1D attribute of length 3
        Array data = Array.factory(int.class, new int[]{3}, new int[]{1, 2, 3});
        ncfile.addVariableAttribute("temperature", "scale", data);
        // add a string-valued variable: char svar(80)
        ucar.nc2.Dimension svar_len = ncfile.addDimension("svar_len", 80);
        dims = new ArrayList();
        dims.add(svar_len);
        ncfile.addVariable("svar", DataType.CHAR, dims);
        // string array: char names(3, 80)
        ucar.nc2.Dimension names = ncfile.addDimension("names", 3);
        ArrayList dima = new ArrayList();
        dima.add(names);
        dima.add(svar_len);
        ncfile.addVariable("names", DataType.CHAR, dima);
        // how about a scalar variable?
        ncfile.addVariable("scalar", DataType.DOUBLE, new ArrayList());
        // add global attributes
        ncfile.addGlobalAttribute("yo", "face");
        ncfile.addGlobalAttribute("versionD", new Double(1.2));
        ncfile.addGlobalAttribute("versionF", new Float(1.2));
        ncfile.addGlobalAttribute("versionI", new Integer(1));
        ncfile.addGlobalAttribute("versionS", new Short((short) 2));
        ncfile.addGlobalAttribute("versionB", new Byte((byte) 3));
        // create the file
        try {
            ncfile.create();
        } catch (IOException e) {
            System.err.println("ERROR creating file " + ncfile.getLocation() + "\n" + e);
        }

        ArrayDouble A = new ArrayDouble.D2(latDim.getLength(), lonDim.getLength());
        int i, j;
        Index ima = A.getIndex();
        for (i = 0; i < latDim.getLength(); i++) {
            for (j = 0; j < lonDim.getLength(); j++) {
                A.setDouble(ima.set(i, j), (double) (i * 1000000 + j * 1000));
            }
        }
        int[] origin = new int[2];
        try {
            ncfile.write("temperature", origin, A);
        } catch (IOException e) {
            System.err.println("ERROR writing file");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        // write char variable as String
        try {
            ArrayChar ac2 = new ArrayChar.D1(svar_len.getLength());
            ac2.setString("Two pairs of ladies stockings!");
            ncfile.write("svar", ac2);
        } catch (IOException e) {
            System.err.println("ERROR writing Achar2");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }

        // write String array
        try {
            ArrayChar ac2 = new ArrayChar.D2(names.getLength(), svar_len.getLength());
            ac2.setString(0, "0 pairs of ladies stockings!");
            ac2.setString(1, "1 pair of ladies stockings!");
            ac2.setString(2, "2 pairs of ladies stockings!");
            ncfile.write("names", ac2);
        } catch (IOException e) {
            System.err.println("ERROR writing Achar4");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        // write scalar data
        try {
            ArrayDouble.D0 datas = new ArrayDouble.D0();
            datas.set(222.333);
            ncfile.write("scalar", datas);
        } catch (IOException e) {
            System.err.println("ERROR writing scalar");
        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
        try {
            ncfile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}