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
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
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
    JButton refreshButton = new JButton("Refresh");    
    JTable interactionTable = new JTable(new Object[][]{{"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","y","z","x","y","z"}},new String[]{"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","1","1","1","1","1"});
    
    JCheckBox multiIteration = new JCheckBox("Multiple Iterations");    

    TreeSet<Integer> indexSet = new TreeSet<Integer>();

    UniversalSensitivityAnalyzer uniSA = null;
    TableRowHeader rowHeader ;
    
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

            String values = (String)obj;
            String value[] = values.split("\\+\\-");
            double mean = Double.parseDouble(value[0]);
            double sigma2 = Double.parseDouble(value[1]);
            
            double sig = sigma2/(2.0*mean);
            
            Color color = null;
            
            if (mean <= 0.0) {
                color = Color.WHITE;
            }else if (mean <= 1.0) {
                double red = mean * (double) Color.RED.getRed() + (1.0 - mean) * (double) Color.WHITE.getRed();
                double green = mean * (double) Color.RED.getGreen() + (1.0 - mean) * (double) Color.WHITE.getGreen();
                double blue = mean * (double) Color.RED.getBlue() + (1.0 - mean) * (double) Color.WHITE.getBlue();
                color = new Color((int) red, (int) green, (int) blue);
            } else {
                color = Color.RED;
            }
            
            if (sig > 0.2){
                color = Color.YELLOW;
            }
            
            setText(values);
            
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
        private double[][][] data = null;

        InteractionTableModel(SimpleEnsemble p[], double interaction[][][]) {
            int n = interaction.length;
            int m = interaction[0].length;
            data = new double[n][n][3];
            columnNames = new String[m];            
            for (int i=0;i<n;i++){
                columnNames[i] = p[i].name;
                for (int j=0;j<m;j++){
                    data[i][j][0] = interaction[i][j][0];
                    data[i][j][1] = interaction[i][j][1];
                    data[i][j][2] = interaction[i][j][2];
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
            return String.format(Locale.ENGLISH,"%.3f +- %.3f", data[row][col][1], data[row][col][2]-data[row][col][1]);
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
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
                ParameterInteractionAnalyser.this.redraw();
            }
        });


        JScrollPane interactionTablePane = new JScrollPane(interactionTable);
        interactionTablePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        interactionTablePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        interactionTable.setBackground(this.panel.getBackground());
        interactionTablePane.setBackground(this.panel.getBackground());
        
        rowHeader = new TableRowHeader( interactionTable, interactionTablePane );
        interactionTablePane.setRowHeader( rowHeader );

        JPanel centerPanel = new JPanel();
        GroupLayout layout = new GroupLayout(centerPanel);
        centerPanel.setLayout(layout);

        JLabel interactionTableTitle = new JLabel("interaction effects",JLabel.CENTER);
        
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()                   
                    .addGroup(layout.createParallelGroup()
                        .addComponent(interactionTableTitle)
                        .addComponent(interactionTablePane))
                )                    
                .addGroup(layout.createSequentialGroup()
                    .addComponent(sampleCountLabel)
                    .addGap(1, 2, 3)
                    .addComponent(sampleCountField,50,75,100)
                    .addGap(5, 10, 15)
                    .addComponent(sampleCountRegression)
                    .addGap(1, 2, 3)
                    .addComponent(sampleCountFieldRegression,50,75,100)
                )                               
                .addGroup(layout.createSequentialGroup()
                    .addComponent(regressionErrorLabel)
                     .addGap(1, 2, 3)
                    .addComponent(regressionErrorField,50,75,100)                    
                )
                .addGroup(layout.createSequentialGroup()
                    .addComponent(refreshButton)
                    .addGap(5, 10, 15)
                    .addComponent(multiIteration)
                )                
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()                    
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(interactionTableTitle)
                        .addComponent(interactionTablePane))
                ) 
                .addGap(5, 10, 15)
                .addGroup(layout.createParallelGroup()
                    .addComponent(sampleCountLabel,25,30,35)
                    .addComponent(sampleCountField,25,30,35)
                    .addComponent(sampleCountRegression,25,30,35)
                    .addComponent(sampleCountFieldRegression,25,30,35))
                .addGap(5, 10, 15)
                .addGroup(layout.createParallelGroup()
                    .addComponent(regressionErrorLabel,25,30,35)
                    .addComponent(regressionErrorField,25,30,35)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(refreshButton)
                    .addComponent(multiIteration)
                )
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

        uniSA = new UniversalSensitivityAnalyzer();
        uniSA.setMethod(UniversalSensitivityAnalyzer.SAMethod.FOSI2);
        uniSA.setUsingRegression(true);
        uniSA.setParameterNormalizationMethod(SimpleInterpolation.NormalizationMethod.Linear);
        uniSA.setObjectiveNormalizationMethod(SimpleInterpolation.NormalizationMethod.Linear);
        uniSA.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                setState(arg.toString());
            }
        });
        
        int n = counter;
        
        double sampleSize = 0;
        try {
            sampleSize = Double.parseDouble(this.sampleCountFieldRegression.getText());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(panel, "Please enter a real valued number for sample size!");
            return;
        }

        uniSA.setSampleCount((int)sampleSize);
        uniSA.setup(xData, p2);
        
        this.regressionErrorField.setText(String.format(Locale.ENGLISH,"%.4f",uniSA.calculateError()));
        this.sampleCountField.setText(Integer.toString(p2.getSize()));        
         
        double sensitivity[][][] = null;
        if (this.multiIteration.isSelected()){
            sensitivity = uniSA.getInteractionsUncertainty();
        }else{
            double sens[][] = uniSA.getInteractions();
            sensitivity = new double[n][n][3];
            for (int i=0;i<n;i++){
                for (int j=0;j<n;j++){
                    sensitivity[i][j][0] = sens[i][j];
                    sensitivity[i][j][1] = sens[i][j];
                    sensitivity[i][j][2] = sens[i][j];
                }
            }
        }
                
        TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();
        interactionTable.setModel(new InteractionTableModel(xData, sensitivity));
        interactionTable.setPreferredSize(new Dimension((xData.length)*100,(xData.length)*100));
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
