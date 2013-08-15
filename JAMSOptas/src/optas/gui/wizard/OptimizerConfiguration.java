/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui.wizard;

import jams.JAMS;
import jams.meta.ModelDescriptor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.StringOptimizerParameter;

/**
 *
 * @author christian
 */
public class OptimizerConfiguration extends JPanel {
    final int TOTAL_WIDTH = 800;
    final int TOTAL_HEIGHT = 550;
    OptimizerDescription availableOptimizer[] = null;
    Set<Parameter> availableParameters = null;
    Set<Objective> availableObjectives = null;
    Optimization optimizationScheme = null;
    NumericKeyListener stdNumericKeyListener = new NumericKeyListener();
    NumericFocusListener stdFocusListener = new NumericFocusListener();
    JDialog dialog = null;
    JPanel optimizerConfigurationPanel = new JPanel(new GridBagLayout());
    JPanel parameterConfigurationPanel = new JPanel(new GridBagLayout());
    JPanel objectiveConfigurationPanel = new JPanel(new GridBagLayout());
    
    OptimizerDescription activeOptimizer = null;
    
    Logger logger = null;
    JButton okButton = new JButton(JAMS.i18n("OK"));
            
    JTable parameterTable = new JTable() {
        {
            setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);

            ParameterTableModel model = (ParameterTableModel)this.getModel();
            
            if (getSelectedRow() != row) {
                if ((Boolean) model.getValueAt(row, 0)) {
                    if ((Double) model.getValueAt(row, 3) >= (Double) model.getValueAt(row, 4)){
                        c.setBackground(Color.RED);
                    }else{
                        c.setBackground(Color.GREEN);
                    }                    
                } else {
                    //c.setBackground(Color.WHITE);
                    c.setBackground(Color.WHITE);
                }
            } else {
                c.setBackground(Color.BLUE);
            }

            return c;
        }
    };
    
    JTable objectiveTable = new JTable() {
        {
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);            
        }
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);

            ObjectiveTableModel model = (ObjectiveTableModel)this.getModel();
            
            if (getSelectedRow() != row) {
                if ((Boolean) model.getValueAt(row, 0)) {
                    c.setBackground(Color.GREEN);
                } else {
                    //c.setBackground(Color.WHITE);
                    c.setBackground(Color.WHITE);
                }
            } else {
                c.setBackground(Color.BLUE);
            }

            return c;
        }
    };
    
    Dimension prefSize = new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT);
    HashSet<ActionListener> listeners = new HashSet<ActionListener>();


    public OptimizerConfiguration(ModelDescriptor md, Logger logger) throws OPTASWizardException {
        optimizationScheme = new Optimization(md);

        this.logger = logger;
        initData();
        initGUI();

        updateOptimizerPanel();
        
        parameterTable.setModel(new ParameterTableModel(optimizationScheme.getParameter(), optimizationScheme.getModelParameters()));
        parameterTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        parameterTable.getColumnModel().getColumn(1).setPreferredWidth(115);
        parameterTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        parameterTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        parameterTable.getColumnModel().getColumn(4).setPreferredWidth(50);
        parameterTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        parameterTable.getTableHeader().setPreferredSize(new Dimension(parameterTable.getTableHeader().getPreferredSize().width,40));
        parameterTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        objectiveTable.setModel(new ObjectiveTableModel(optimizationScheme.getObjective(), optimizationScheme.getModelObjectives()));        
        objectiveTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        objectiveTable.getColumnModel().getColumn(1).setPreferredWidth(115);
        objectiveTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        objectiveTable.getTableHeader().setPreferredSize(new Dimension(objectiveTable.getTableHeader().getPreferredSize().width,40));                
    }

    public void addActionListener(ActionListener listener) {
        this.listeners.add(listener);
    }

    public ModelDescriptor getModelDescriptor() {
        if (optimizationScheme != null) {
            return optimizationScheme.getModelDescriptor();
        }
        return null;
    }

    private void initGUI() {
        this.removeAll();
        //create optimizer panel
        JScrollPane scrollPaneOptimizerSpecificationPanel = new JScrollPane(optimizerConfigurationPanel);

        JComboBox selectOptimizer = new JComboBox(availableOptimizer);
        selectOptimizer.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED) {
                    activeOptimizer = (OptimizerDescription) e.getItem();
                    optimizationScheme.setOptimizerDescription(activeOptimizer);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateOptimizerPanel();
                        }
                    });
                }
            }
        });        
        scrollPaneOptimizerSpecificationPanel.setPreferredSize(new Dimension(TOTAL_WIDTH, 200));

        //create parameter panel
        parameterConfigurationPanel = new JPanel(new GridBagLayout());       
        JScrollPane scrollPaneParameterConfiguration = new JScrollPane(parameterTable);
        scrollPaneParameterConfiguration.setPreferredSize(new Dimension(350, 250));
        scrollPaneParameterConfiguration.setBorder(BorderFactory.createTitledBorder(
                JAMS.i18n("Parameter_Configuration")));
        
        objectiveConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneObjectiveConfiguration = new JScrollPane(objectiveTable);
        scrollPaneObjectiveConfiguration.setPreferredSize(new Dimension(200, 250));
        scrollPaneObjectiveConfiguration.setBorder(BorderFactory.createTitledBorder(
                JAMS.i18n("Objective_Configuration")));
               
        GroupLayout mainLayout = new GroupLayout(this);
        this.setLayout(mainLayout);

        mainLayout.setHorizontalGroup(mainLayout.createParallelGroup()
                .addComponent(selectOptimizer)
                .addComponent(scrollPaneOptimizerSpecificationPanel)
                .addGroup(mainLayout.createSequentialGroup()                
                .addComponent(scrollPaneParameterConfiguration)
                .addComponent(scrollPaneObjectiveConfiguration)));           
                

        mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
                .addComponent(selectOptimizer)
                .addComponent(scrollPaneOptimizerSpecificationPanel)
                .addGap(20)
                .addGroup(mainLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(scrollPaneParameterConfiguration)
                .addComponent(scrollPaneObjectiveConfiguration)));        
                

        this.revalidate();
    }

    public Optimization getOptimizer() {
        return optimizationScheme;
    }

    private void updateOptimizerPanel() {
        //configuration panel
        optimizerConfigurationPanel.removeAll();
        optimizerConfigurationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        int counter = 0;
        c.gridx = 0;
        c.gridy = counter++;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5, 0, 5, 0);
        optimizerConfigurationPanel.add(new JLabel(optimizationScheme.getOptimizerDescription().getShortName()) {
            {
                Font f = this.getFont();
                setFont(f.deriveFont(20.0f));
            }
        }, c);

        c.gridwidth = 1;
        c.insets = new Insets(0, 0, 2, 0);

        for (OptimizerParameter p : optimizationScheme.getOptimizerDescription().getPropertyMap().values()) {
            c.gridx = 0;
            c.gridy = counter;
            c.anchor = GridBagConstraints.NORTHWEST;

            JLabel lbl = new JLabel("<HTML><BODY>" + p.getDescription() + "</BODY></HTML>");
            lbl.setVerticalTextPosition(SwingConstants.TOP);
            lbl.setPreferredSize(new Dimension(440, 40));
            optimizerConfigurationPanel.add(lbl, c);

            c.gridx = 1;
            c.anchor = GridBagConstraints.NORTH;

            if (p instanceof NumericOptimizerParameter) {
                optimizerConfigurationPanel.add(
                        getNumericField((NumericOptimizerParameter) p), c);
            }
            if (p instanceof BooleanOptimizerParameter) {
                optimizerConfigurationPanel.add(
                        getBooleanField((BooleanOptimizerParameter) p), c);
            }
            if (p instanceof StringOptimizerParameter) {
                optimizerConfigurationPanel.add(
                        getStringField((StringOptimizerParameter) p), c);
            }
            c.gridx = 1;
            c.anchor = GridBagConstraints.NORTH;
            counter++;
        }

        if (600 > counter * 40) {
            c.insets = new Insets(0, 0, 560 - counter * 40, 0);
        }

        c.gridx = 0;
        c.gridy = counter;
        c.anchor = GridBagConstraints.NORTHWEST;

        BooleanOptimizerParameter doSpatial = (BooleanOptimizerParameter) optimizationScheme.getOptimizerDescription().getDoSpatialRelaxation();

        JLabel lbl = new JLabel("<HTML><BODY>" + doSpatial.getDescription() + "</BODY></HTML>");
        lbl.setVerticalTextPosition(SwingConstants.TOP);
        lbl.setPreferredSize(new Dimension(440, 40));
        optimizerConfigurationPanel.add(lbl, c);

        c.gridx = 1;
        c.gridy = counter;
        c.anchor = GridBagConstraints.NORTH;

        optimizerConfigurationPanel.add(getBooleanField(doSpatial), c);
        this.revalidate();
        
        updateGUI();
    }

    class ParameterTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{JAMS.i18n("selected"), JAMS.i18n("Name"), JAMS.i18n("component"), "<HTML>"+JAMS.i18n("lower_bound").replace(" ", "<br>")+"</HTML>", "<HTML>"+JAMS.i18n("upper_bound").replace(" ", "<br>")+"</HTML>", "<HTML>"+JAMS.i18n("start_value").replace(" ", "<br>")+"</HTML>"};
        private Parameter dataObjects[] = null;

        ParameterTableModel(Set<Parameter> selectedParameters, Set<Parameter> modelParameters) {           
            dataObjects = new Parameter[modelParameters.size()];
            int i = 0;
            for (Parameter p : modelParameters) {                
                dataObjects[i] = p;
                i++;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (dataObjects != null) {
                return dataObjects.length;
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
            switch(col){
                case 0: return optimizationScheme.getParameter().contains(dataObjects[row]);                     
                case 1: return dataObjects[row].getAttributeName(); 
                case 2: return dataObjects[row].getParentName(); 
                case 3: return dataObjects[row].getLowerBound(); 
                case 4: return dataObjects[row].getUpperBound(); 
                case 5: return dataObjects[row].getStartValue(); 
                default: return null;
            }            
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0: return Boolean.class;                
                case 3: return Double.class;
                case 4: return Double.class;
                case 5: return Double.class;
                default: return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0: return true;                
                case 3: return true;
                case 4: return true;
                case 5: return true;
                default : return false;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0 && ((Boolean) value).booleanValue() == false) {                
                optimizationScheme.removeParameter(dataObjects[row]);                
            }
            else if (col == 0 && ((Boolean) value).booleanValue() == true) {                
                optimizationScheme.addParameter(dataObjects[row]);                                
            }
            else if (col == 3){
                dataObjects[row].setLowerBound((Double)value);
                optimizationScheme.update();
            }
            else if (col == 4){
                dataObjects[row].setUpperBound((Double)value);
                optimizationScheme.update();
            }
            else if (col == 5){
                dataObjects[row].setStartValue(new double[]{(Double)value});
                optimizationScheme.update();
            }  
            for (int i = 0; i < 6; i++) {
                fireTableCellUpdated(row, i);
            }      
            updateGUI();
        }
    }
    
    class ObjectiveTableModel extends AbstractTableModel {

        private String[] columnNames = new String[]{JAMS.i18n("selected"), JAMS.i18n("Name"), JAMS.i18n("component")};
        private Objective dataObjects[] = null;

        ObjectiveTableModel(Set<Objective> selectedObjectives, Set<Objective> modelObjectives) {           
            dataObjects = new Objective[modelObjectives.size()];
            int i = 0;
            for (Objective p : modelObjectives) {                
                dataObjects[i] = p;
                i++;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            if (dataObjects != null) {
                return dataObjects.length;
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
            switch(col){
                case 0: return optimizationScheme.getObjective().contains(dataObjects[row]);                     
                case 1: return dataObjects[row].getAttributeName(); 
                case 2: return dataObjects[row].getParentName();                 
                default: return null;
            }            
        }

        @Override
        public Class getColumnClass(int c) {
            switch (c) {
                case 0: return Boolean.class;    
                default: return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0: return true;
                case 1: return false;
                case 2: return false;                
                default : return false;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 0 && ((Boolean) value).booleanValue() == false) {                
                optimizationScheme.removeObjective(dataObjects[row]);                
            }
            else if (col == 0 && ((Boolean) value).booleanValue() == true) {                
                optimizationScheme.addObjective(dataObjects[row]);                                
            }            
            for (int i = 0; i < 3; i++) {
                fireTableCellUpdated(row, i);
            }          
            updateGUI();
        }
    }
   
    public JDialog showDialog(JFrame parent) {
        dialog = new JDialog(parent);
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonBar = new JPanel(new FlowLayout());
        panel.add(this, BorderLayout.CENTER);
        panel.add(buttonBar, BorderLayout.SOUTH);

        buttonBar.add(okButton);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optimizationScheme.setOptimizerDescription(OptimizerConfiguration.this.activeOptimizer);
                optimizationScheme.finish();
                dialog.setVisible(false);
                for (ActionListener listener : listeners) {
                    listener.actionPerformed(new ActionEvent(OptimizerConfiguration.this, 1, "doc_modified"));
                }
            }
        });

        buttonBar.add(new JButton("Cancel") {
            {
                addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.setVisible(false);
                    }
                });
            }
        });
        dialog.getContentPane().add(panel);
        dialog.revalidate();
        dialog.setSize(prefSize);
        dialog.setLocationByPlatform(true);
        dialog.setVisible(true);
        return dialog;
    }

    private void initData() {
        Set<Optimizer> set = OptimizerLibrary.getAvailableOptimizer();
        availableOptimizer = new OptimizerDescription[set.size()];
        int c = 0;
        for (Optimizer o : set) {
            if (optimizationScheme.getOptimizerDescription().getOptimizerClassName().equals(o.getDescription().getOptimizerClassName())){
                availableOptimizer[c++] = optimizationScheme.getOptimizerDescription();
                activeOptimizer = optimizationScheme.getOptimizerDescription();
            }else{
                availableOptimizer[c++] = o.getDescription();
            }
        }

        availableParameters = this.optimizationScheme.getModelParameters();
        availableObjectives = this.optimizationScheme.getModelObjectives();
        
        updateGUI();
    }

    private JComponent getNumericField(NumericOptimizerParameter p) {
        JTextField field = new JTextField(Double.toString(((NumericOptimizerParameter) p).getValue()), 5);
        field.putClientProperty("property", p);
        field.putClientProperty("mode", new Integer(NumericKeyListener.MODE_PARAMETERVALUE));
        field.addKeyListener(stdNumericKeyListener);
        field.addFocusListener(stdFocusListener);

        return field;
    }

    private JComponent getBooleanField(BooleanOptimizerParameter p) {
        JCheckBox checkBox = new JCheckBox("", ((BooleanOptimizerParameter) p).isValue());
        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBox src = (JCheckBox) e.getSource();
                BooleanOptimizerParameter p = (BooleanOptimizerParameter) src.getClientProperty("property");
                if (src.isSelected()) {
                    p.setValue(true);
                } else {
                    p.setValue(false);
                }
            }
        });
        checkBox.putClientProperty("property", p);
        checkBox.putClientProperty("mode", new Integer(NumericKeyListener.MODE_PARAMETERVALUE));
        return checkBox;
    }

    private JComponent getStringField(StringOptimizerParameter p) {
        JTextField field = new JTextField(((StringOptimizerParameter) p).getValue(), 15);
        field.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTextField src = (JTextField) e.getSource();
                StringOptimizerParameter p = (StringOptimizerParameter) src.getClientProperty("property");
                p.setValue(src.getText());
            }
        });
        field.putClientProperty("property", p);
        field.putClientProperty("mode", new Integer(NumericKeyListener.MODE_PARAMETERVALUE));

        return field;
    }
    
    private void updateGUI(){
        okButton.setEnabled(isValidConfiguration());
    }
    
    private boolean isValidConfiguration(){
        if (this.optimizationScheme.getParameter().isEmpty())
            return false;
        if (this.optimizationScheme.getObjective().isEmpty())
            return false;
        if (activeOptimizer == null)
            return false;
        for (Parameter p : this.optimizationScheme.getParameter()){
            if (p.getLowerBound() >= p.getUpperBound())
                return false;
        }
        return true;
    }
}
