/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.MCAT5;

import jams.JAMS;
import optas.tools.VerticalTableHeaderCellRenderer;
import optas.tools.TableRowHeader;
import jams.gui.WorkerDlg;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import optas.SA.UniversalSensitivityAnalyzer;
import optas.data.DataSet;
import optas.data.Efficiency;
import optas.data.EfficiencyEnsemble;
import optas.data.Parameter;
import optas.data.SimpleEnsemble;
import optas.regression.SimpleInterpolation;

/**
 *
 * @author chris
 */
public class ParameterInteractionAnalyser extends MCAT5Plot {

    JPanel panel = null;
    JLabel sampleCountLabel = new JLabel("Samples in dataset:");
    JTextField sampleCountField = new JTextField(10);
    JLabel regressionErrorLabel = new JLabel("Regression Error:");
    JTextField regressionErrorField = new JTextField(10);
    JLabel sampleCountRegression = new JLabel("Samples drawn from regression:");
    JTextField sampleCountFieldRegression = new JTextField("1000");
    JLabel resultLabel = new JLabel("Result:");
    JTextField resultField = new JTextField("0");
    JButton refreshButton = new JButton("Refresh");
    JTable parameterTable = new JTable(new Object[][]{{Boolean.TRUE,"test"}},new String[]{"x","y"});

    JCheckBox multiIteration = new JCheckBox("Multiple Iterations");
    JTable interactionTable = new JTable(new Object[][]{{"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","y","z","x","y","z"}},new String[]{"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","1","1","1","1","1"});

    TreeSet<Integer> indexSet = new TreeSet<Integer>();

    UniversalSensitivityAnalyzer uniSA = null;
    TableRowHeader rowHeader ;

    class ParameterTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{"enabled", "name", "main effect"};
        private Object[][] data = null;

        ParameterTableModel(SimpleEnsemble p[]) {
            data = new Object[p.length][3];
            for (int i=0;i<p.length;i++){
                data[i][0] = Boolean.FALSE;
                data[i][1] = p[i];
                data[i][2] = 0.0;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (data != null) {
                return data.length;
            } else {
                return 0;
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                case 2:
                    return Double.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:
                    return true;                
            }
            return false;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0){
                if (((Boolean)value).booleanValue()==false){
                    indexSet.remove(row);
                }else{
                    indexSet.add(row);
                }
                WorkerDlg progress = new WorkerDlg(null, "Updating plot");
                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    public void run() {
                        double sensitivity[] = uniSA.getInteraction(indexSet);
                        resultField.setText(sensitivity[0] + "/" + sensitivity[1]);
                    }
                });
                progress.execute();
            data[row][col] = value;
            }else if (col == 2){
                this.data[row][col] = value;
            }
            
            fireTableCellUpdated(row, col);
        }
    }

    public class InteractionRenderer extends JLabel
            implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;

        public InteractionRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object obj,
                boolean isSelected, boolean hasFocus,
                int row, int column) {

            Double value = (Double)obj;
            value = ((double)Math.round(value*100))/100.0;
            Color color = null;
            if (row > column) {
                if (Math.abs(value) <= 1.0){
                    double red = Math.abs(value) * (double) Color.RED.getRed() + (1.0 - Math.abs(value)) * (double) Color.WHITE.getRed();
                    double green = Math.abs(value) * (double) Color.RED.getGreen() + (1.0 - Math.abs(value)) * (double) Color.WHITE.getGreen();
                    double blue = Math.abs(value) * (double) Color.RED.getBlue() + (1.0 - Math.abs(value)) * (double) Color.WHITE.getBlue();
                    color  = new Color((int) red, (int) green, (int) blue);
                } else{
                    color = Color.RED;
                }
                setText(value.toString());
            } else if (row < column) {
                if (Math.abs(value) <= 1.0){
                    double red = Math.abs(value) * (double) Color.RED.getRed() + (1.0 - Math.abs(value)) * (double) Color.WHITE.getRed();
                    double green = Math.abs(value) * (double) Color.RED.getGreen() + (1.0 - Math.abs(value)) * (double) Color.WHITE.getGreen();
                    double blue = Math.abs(value) * (double) Color.RED.getBlue() + (1.0 - Math.abs(value)) * (double) Color.WHITE.getBlue();
                    color  = new Color((int) red, (int) green, (int) blue);
                } else{
                    color = Color.RED;
                }
                setText(value.toString());
            } else {
                color = Color.BLACK;
            }
            setBackground(color);
            
            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                                table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            return this;
        }
    }

    class InteractionTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{"enabled", "name"};
        private Object[][] data = null;

        InteractionTableModel(SimpleEnsemble p[], Double interaction[][]) {
            int n = interaction.length;
            int m = interaction[0].length;
            data = new Object[n][m];
            columnNames = new String[m];            
            for (int i=0;i<n;i++){
                columnNames[i] = p[i].name;
                for (int j=0;j<m;j++){
                    data[i][j] = interaction[i][j];
                }
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (data != null) {
                return data.length;
            } else {
                return 0;
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return String.class;
            }
            return Double.class;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    public ParameterInteractionAnalyser() {
        this.addRequest(new SimpleRequest(JAMS.i18n("PARAMETER"), Parameter.class));
        this.addRequest(new SimpleRequest(JAMS.i18n("Efficiency"), Efficiency.class));

        init();
    }

    private void init() {
        panel = new JPanel(new BorderLayout());

        refreshButton = new JButton(new AbstractAction("Recalculate Regression") {

            @Override
            public void actionPerformed(ActionEvent e) {
                WorkerDlg progress = new WorkerDlg(null, "Updating plot");
                progress.setInderminate(true);
                progress.setTask(new Runnable() {

                    @Override
                    public void run() {
                        ParameterInteractionAnalyser.this.redraw();
                    }
                });
                if (!((JButton)e.getSource()).isSelected())
                    progress.execute();
            }
        });

        JScrollPane parameterTablePane = new JScrollPane(parameterTable);
        JScrollPane interactionTablePane = new JScrollPane(interactionTable);
        interactionTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        interactionTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        rowHeader = new TableRowHeader( interactionTable, interactionTablePane );
        interactionTablePane.setRowHeader( rowHeader );

        JPanel centerPanel = new JPanel();
        GroupLayout layout = new GroupLayout(centerPanel);
        centerPanel.setLayout(layout);

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                    .addComponent(parameterTablePane,100,GroupLayout.PREFERRED_SIZE,300)
                    .addComponent(interactionTablePane))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(sampleCountLabel)
                    .addComponent(sampleCountField))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(regressionErrorLabel)
                    .addComponent(regressionErrorField)
                    .addComponent(multiIteration))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(sampleCountRegression)
                    .addComponent(sampleCountFieldRegression))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(resultLabel)
                    .addComponent(resultField))
                .addComponent(refreshButton)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    .addComponent(parameterTablePane)
                    .addComponent(interactionTablePane))
                .addGroup(layout.createParallelGroup()
                    .addComponent(sampleCountLabel,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(sampleCountField,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup()
                    .addComponent(regressionErrorLabel,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(regressionErrorField,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(multiIteration))
                .addGroup(layout.createParallelGroup()
                    .addComponent(sampleCountRegression,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(sampleCountFieldRegression,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup()
                    .addComponent(resultLabel,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
                    .addComponent(resultField,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE))
                .addComponent(refreshButton)
        );
        panel.add(centerPanel);
    }

    public JPanel getPanel() {
        return this.panel;
    }

    public void refresh() throws NoDataException {
        if (!this.isRequestFulfilled()) {
            return;
        }

        Set<String> xSet = this.getDataSource().getDatasets(Parameter.class);
        ArrayList<DataSet> p[] = getData(new int[]{0, 1});        
        EfficiencyEnsemble p2 = (EfficiencyEnsemble) p[1].get(0);

        SimpleEnsemble xData[] = new SimpleEnsemble[xSet.size()];
        int counter = 0;
        for (String name : xSet) {
            xData[counter++] = this.getDataSource().getSimpleEnsemble(name);
        }

        this.indexSet.clear();

        parameterTable.setModel(new ParameterTableModel(xData));

        uniSA = new UniversalSensitivityAnalyzer();
        uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.FOSI2);
        uniSA.setUsingRegression(true);
        uniSA.setParameterNormalizationMethod(SimpleInterpolation.NormalizationMethod.Linear);
        uniSA.setObjectiveNormalizationMethod(SimpleInterpolation.NormalizationMethod.Linear);

        int n = counter;
        
        double sampleSize = 0;
        try {
            sampleSize = Double.parseDouble(this.sampleCountFieldRegression.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(panel, "Please enter a real valued number for sample size!");
            return;
        }

        uniSA.setSampleCount((int)sampleSize);

        int K = 1;
        if (this.multiIteration.isSelected())
            K=10;

        Double interaction[][] = new Double[n][n];
        Double interactionC[][] = new Double[n][n];
        Double mainEffect[] = new Double[n];
        for (int i=0;i<n;i++){
            mainEffect[i] = new Double(0.0);
            for (int j=0;j<n;j++){
                interaction[i][j] = new Double(0.0);
                interactionC[i][j] = new Double(0.0);
            }
        }
        
        for (int k = 0; k < K; k++) {
            uniSA.setup(xData, p2);

            if (k==0){
                this.regressionErrorField.setText(Double.toString(uniSA.calculateError()));
                this.sampleCountField.setText(Integer.toString(p2.getSize()));
                double sensitivity[] = uniSA.getInteraction(indexSet);
                this.resultField.setText(sensitivity[0] + "/" + sensitivity[1]);
            }
            TreeSet<Integer> set = new TreeSet<Integer>();
            TreeSet<Integer> set_i = new TreeSet<Integer>();
            TreeSet<Integer> set_j = new TreeSet<Integer>();
            for (int i = 0; i < n; i++) {
                set_i.clear();
                set_i.add(i);

                for (int j = 0; j < n; j++) {
                    set_j.clear();
                    set_j.add(j);

                    double sa_i = Math.max(uniSA.getInteraction(set_i)[0], 0);
                    double sa_j = Math.max(uniSA.getInteraction(set_j)[0], 0);

                    if (j == i) {
                        interaction[i][j] = 0.0;
                        mainEffect[i] += sa_i/K;
                    } else if (j < i) {
                        set.add(j);
                        
                        set.clear();
                        set.add(i);
                        set.add(j);
                                                                        
                        interaction[i][j] += Math.max(uniSA.getInteraction(set)[0], 0)/K;
                        double max = Math.max(sa_i, sa_j);
                        double min = Math.min(sa_i, sa_j);
                        double sum = sa_i + sa_j;
                        double w = 0;
                        if (Math.abs(sum - max) >= 0.01) {
                            if (interaction[i][j] > max) {
                                w = interaction[i][j];//(interaction[i][j] - max) / (sum-max);
                            } else {
                                w = interaction[i][j];//(interaction[i][j] - max) / (max-min);
                            }
                        }
                        //interaction[i][j] = w;
                        set.remove(j);
                    } else {
                        set.add(j);
                        set_i.clear();
                        set_i.add(i);
                        set_j.clear();
                        set_j.add(j);
                        
                        interactionC[i][j] += Math.max(uniSA.getInteraction(set)[0], 0) / K;
                        double max = Math.max(sa_i, sa_j);
                        double min = Math.min(sa_i, sa_j);
                        double sum = sa_i + sa_j;
                        double w = 0;
                        if (Math.abs(sum - max) >= 0.01) {
                            if (interaction[i][j] > max) {
                                w = (interaction[i][j] - max) / (sum - max);
                            } else {
                                w = (interaction[i][j] - max) / (max - min);
                            }
                        }
                        //interaction[i][j] = 0.0;//w;
                        set.remove(j);
                    }
                }
            }
        }
        for (int i=0;i<n;i++){
            parameterTable.getModel().setValueAt(mainEffect[i], i, 2);
        }
        TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();
        interactionTable.setModel(new InteractionTableModel(xData, interaction));
        interactionTable.setPreferredSize(new Dimension((xData.length)*45,(xData.length)*45));
        for (int i = 0; i < n; i++) {
            interactionTable.getColumnModel().getColumn(i).setCellRenderer(new InteractionRenderer(true));
            interactionTable.getColumnModel().getColumn(i).setWidth(45);
            interactionTable.getColumnModel().getColumn(i).setPreferredWidth(45);
            interactionTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);

        }
        rowHeader.setRowHeaderName(xData);
        interactionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        interactionTable.revalidate();
        interactionTable.doLayout();        
    }
}
