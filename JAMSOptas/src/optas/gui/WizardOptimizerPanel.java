/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui;

import jams.gui.input.TableInput;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import optas.optimizer.management.StringOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.OptimizerParameter;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import optas.metamodel.AttributeWrapper;
import optas.metamodel.Objective;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;
import optas.optimizer.Optimizer;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.OptimizerLibrary;

/**
 *
 * @author chris
 */
public class WizardOptimizerPanel extends JPanel {

    public static SortedSet<Objective> availableObjectives = new TreeSet<Objective>();

    SortedSet<AttributeWrapper> objectiveData = null;
    WizardParameterPanel parameterPanel = null;

    OptimizerDescription availableOptimizer[];    
    NumericKeyListener stdNumericKeyListener = new NumericKeyListener();
    NumericFocusListener stdFocusListener = new NumericFocusListener();
    JFrame owner = null;
    JPanel objectiveSpecificationPanel = new JPanel(new GridBagLayout());

    Optimization optimizationScheme = new Optimization();

    public WizardOptimizerPanel(JFrame owner) {
        init(owner,null);
    }
    public WizardOptimizerPanel(JFrame owner, Optimization o) {
        init(owner,o);
    }

    private void init(JFrame owner, Optimization o){
        if (o!=null){
            this.optimizationScheme = o;
            availableObjectives.addAll(o.getObjective());
        }
        this.owner = owner;
        this.parameterPanel = new WizardParameterPanel(optimizationScheme);
        this.setLayout(new GridBagLayout());
        this.setMinimumSize(new Dimension(800, 700));

        updateMainPanel();
    }

    private void updateMainPanel() {
        removeAll();

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getOptimizerConfigurationPanel(), c);

        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(parameterPanel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(getObjectiveSpecificationPanel(), c);

        updateUI();
    }

    
    private class CreateObjectiveDialog extends JDialog {        
        JComboBox objList = null;
        JButton createButton = null;

        boolean isApproved;
        DefaultComboBoxModel model = null;

        public CreateObjectiveDialog() {
            super(owner, "Create Objective");
            objList = new JComboBox();

            model = new DefaultComboBoxModel(WizardOptimizerPanel.availableObjectives.toArray());
            objList.setModel(model);

            createButton = new JButton(new AbstractAction("Create Objective") {

                public void actionPerformed(ActionEvent e) {
                    Objective o = new Objective();
                    WizardObjectivePanel wizard = new WizardObjectivePanel(o, objectiveData);
                    if (wizard.showDialog(owner)){
                        availableObjectives.add(o);
                        model = new DefaultComboBoxModel(WizardOptimizerPanel.availableObjectives.toArray());
                        objList.setModel(model);
                        objList.setSelectedItem(o);
                    }
                }
            });

            JPanel contentPanel = new JPanel(new BorderLayout());

            JPanel centerPanel = new JPanel(new FlowLayout());

            centerPanel.add(objList);
            centerPanel.add(createButton);

            JPanel southPanel = new JPanel(new FlowLayout());

            southPanel.add(new JButton(new AbstractAction("Ok") {

                public void actionPerformed(ActionEvent e) {
                    isApproved = true;
                    setVisible(false);
                }
            }));

            southPanel.add(new JButton(new AbstractAction("Cancel") {

                public void actionPerformed(ActionEvent e) {
                    isApproved = false;
                    setVisible(false);
                }
            }));

            contentPanel.add(centerPanel, BorderLayout.CENTER);
            contentPanel.add(southPanel, BorderLayout.SOUTH);

            this.getContentPane().add(contentPanel);
            this.pack();

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension preferredSize = this.getPreferredSize();
            preferredSize.width = 450;
            this.setPreferredSize(preferredSize);
            this.setSize(preferredSize);

            int top = (screenSize.height - this.getPreferredSize().height) / 2;
            int left = (screenSize.width - this.getPreferredSize().width) / 2;
            this.setLocation(left, top);
            this.setModal(true);
            
        }

        public Objective getObjective(){
            if (objList.getSelectedItem()!=null)
                return (Objective)objList.getSelectedItem();
            return null;
        }
    }

    private class ObjectiveTableList extends TableInput{
        public ObjectiveTableList() {
            super(new String[]{"name", "simulation", "measurement"}, new Class[]{String.class,String.class, String.class}, new boolean[]{false, false, false}, true);

            getTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            
            ((AbstractTableModel) this.getTable().getModel()).addTableModelListener(new TableModelListener() {

                public void tableChanged(TableModelEvent e) {
                    ArrayList<Objective> list = new ArrayList<Objective>();
                    for (Object row[] : tableData.getValue()) {
                        if (row[0] instanceof Objective){
                            list.add((Objective)row[0]);
                        }
                    }
                    WizardOptimizerPanel.this.optimizationScheme.setObjective(list);
                }
            });

            setPreferredSize(new Dimension(450, 200));
        }

        @Override
        protected void editItem() {
            //get the current selection
            int selection = getTable().getSelectedRow();
            Object selectedData = tableData.getElementAt(selection)[0];

            if (selectedData instanceof Objective){
                Objective o = (Objective)selectedData;
                WizardObjectivePanel panel = new WizardObjectivePanel(o, objectiveData);
                panel.showDialog(WizardOptimizerPanel.this.owner);
                tableData.setElementAt(selection, new Object[]{selectedData, o.getSimulation(), o.getMeasurement()});
                this.setTableData(tableData.getValue());
            }
        }

        @Override
        protected void addItem() {
            CreateObjectiveDialog createObjectiveDialog = new CreateObjectiveDialog();
            createObjectiveDialog.setVisible(true);
            if (createObjectiveDialog.isApproved){
                Objective o = createObjectiveDialog.getObjective();
                tableData.addElement( new Object[]{o, o.getSimulation(), o.getMeasurement()});
                this.setTableData(tableData.getValue());
            }
        }
    }

    private JComponent getObjectiveSpecificationPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Objectives"));
        contentPanel.add(new ObjectiveTableList(), BorderLayout.CENTER);
        return contentPanel;        
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

    private JComponent getNumericField(NumericOptimizerParameter p) {
        JTextField field = new JTextField(Double.toString(((NumericOptimizerParameter) p).getValue()), 5);
        field.putClientProperty("property", p);
        field.putClientProperty("mode", new Integer(NumericKeyListener.MODE_PARAMETERVALUE));
        field.addKeyListener(stdNumericKeyListener);
        field.addFocusListener(stdFocusListener);

        return field;
    }

    int oldState = 0;
    private JComponent getOptimizerConfigurationPanel() {
        JPanel superPanel = new JPanel(new BorderLayout());
        JPanel optimizerConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneOptimizerSpecificationPanel = new JScrollPane(optimizerConfigurationPanel);

        int curSelection = this.initOptimizerDesc();

        JComboBox selectOptimizer = new JComboBox(availableOptimizer);
        selectOptimizer.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == e.SELECTED){
                    optimizationScheme.setOptimizerDescription((OptimizerDescription) e.getItem());
                   SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            updateMainPanel();
                        }
                    });
                    
                }
            }
        });
        selectOptimizer.setSelectedIndex(curSelection);
        optimizationScheme.setOptimizerDescription((OptimizerDescription) selectOptimizer.getSelectedItem());

        scrollPaneOptimizerSpecificationPanel.setPreferredSize(new Dimension(700, 200));

        //configuration panel
        optimizerConfigurationPanel.removeAll();
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

        for (OptimizerParameter p : optimizationScheme.getOptimizerDescription().getPropertyMap()) {
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
                        getStringField((StringOptimizerParameter)p), c);
            }
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
        
        superPanel.add(selectOptimizer, BorderLayout.NORTH);
        superPanel.add(scrollPaneOptimizerSpecificationPanel, BorderLayout.CENTER);

        return superPanel;
    }

    private int initOptimizerDesc() {
        Set<Optimizer> set = OptimizerLibrary.getAvailableOptimizer();
        availableOptimizer = new OptimizerDescription[set.size()];
        int selection = 0;
        int c = 0;
        for (Optimizer o : set) {
            availableOptimizer[c] = o.getDescription();
            if (this.optimizationScheme.getOptimizerDescription()!=null){
                    if (this.optimizationScheme.getOptimizerDescription().getOptimizerClassName().equals(availableOptimizer[c].getOptimizerClassName())){
                        availableOptimizer[c] = this.optimizationScheme.getOptimizerDescription();
                        selection = c;
                    }
            }
            c++;
        }
        return selection;
    }
    
    public void setObjectiveList(SortedSet<AttributeWrapper> objectiveSet) {
        this.objectiveData = objectiveSet;
    }

    public void setParameterList(SortedSet<Parameter> p) {
        this.parameterPanel.fillParameterList(p);
    }

    public Optimization getDescription(){
        return optimizationScheme;
    }
}
