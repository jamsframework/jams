/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui;

import jams.JAMS;
import jams.meta.ModelDescriptor;
import java.awt.BorderLayout;
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
import java.util.List;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import optas.metamodel.Optimization2;
import optas.metamodel.Parameter2;
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

    OptimizerDescription availableOptimizer[] = null;
    Set<Parameter2> availableParameters = null;
    
    Optimization2 optimizationScheme = null;
    NumericKeyListener stdNumericKeyListener = new NumericKeyListener();
    NumericFocusListener stdFocusListener = new NumericFocusListener();
    JDialog dialog = null;
    JPanel optimizerConfigurationPanel = new JPanel(new GridBagLayout());
    JPanel parameterConfigurationPanel = new JPanel(new GridBagLayout());
    Dimension prefSize = new Dimension(1024, 500);
    HashSet<ActionListener> listeners = new HashSet<ActionListener>();

    public OptimizerConfiguration(ModelDescriptor md) {
        optimizationScheme = new Optimization2(md);

        initData();
        initGUI();
        
        updateOptimizerPanel();
        updateParameterPanel();
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
                    optimizationScheme.setOptimizerDescription((OptimizerDescription) e.getItem());
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateOptimizerPanel();
                        }
                    });
                }
            }
        });
        selectOptimizer.setSelectedIndex(0);
        scrollPaneOptimizerSpecificationPanel.setPreferredSize(new Dimension(700, 200));
  
        //create parameter panel
        final JList parameterList = new JList(availableParameters.toArray());
        JScrollPane scrollPaneParameterList = new JScrollPane(parameterList);

        scrollPaneParameterList.setPreferredSize(new Dimension(300, 250));

        scrollPaneParameterList.setBorder(BorderFactory.createTitledBorder(
                JAMS.i18n("Parameter_Configuration")));

        parameterConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneParameterConfiguration = new JScrollPane(parameterConfigurationPanel);
        scrollPaneParameterConfiguration.setPreferredSize(new Dimension(440, 250));
        parameterList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       
        JButton addParameter = new JButton(">>");
        addParameter.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                 List selectedParameters = parameterList.getSelectedValuesList();
                 for (Object obj : selectedParameters){
                     Parameter2 p = (Parameter2)obj;
                     if (optimizationScheme.addParameter(p)){
                         //do nothing
                     }
                 }
                 updateParameterPanel();
            }
        });
        
        GroupLayout mainLayout = new GroupLayout(this);
        this.setLayout(mainLayout);

        mainLayout.setHorizontalGroup(mainLayout.createParallelGroup()
                .addComponent(selectOptimizer)
                .addComponent(scrollPaneOptimizerSpecificationPanel)
                .addGroup(mainLayout.createSequentialGroup()
                .addComponent(scrollPaneParameterList)
                .addComponent(addParameter)                
                .addComponent(scrollPaneParameterConfiguration)));

        mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
                .addComponent(selectOptimizer)
                .addComponent(scrollPaneOptimizerSpecificationPanel)
                .addGroup(mainLayout.createParallelGroup()
                .addComponent(scrollPaneParameterList)
                .addComponent(addParameter)                
                .addComponent(scrollPaneParameterConfiguration)));

        this.revalidate();
    }

    public Optimization2 getOptimizer() {
        return optimizationScheme;
    }

    private void updateOptimizerPanel() {
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
    }

    private void updateParameterPanel() {
        GridBagConstraints c = new GridBagConstraints();
        int counter = 0;
        parameterConfigurationPanel.removeAll();
        c.gridx = 0;
        c.gridy = counter;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(5, 0, 5, 0);
        c.anchor = GridBagConstraints.NORTH;
        parameterConfigurationPanel.add(new JLabel(JAMS.i18n("parameter")), c);
        c.gridx = 1;
        parameterConfigurationPanel.add(new JLabel(JAMS.i18n("lower_bound")), c);
        c.gridx = 2;
        parameterConfigurationPanel.add(new JLabel(JAMS.i18n("upper_bound")), c);
        c.gridx = 3;
        parameterConfigurationPanel.add(new JLabel(JAMS.i18n("start_value")), c);
        c.gridx = 4;
        parameterConfigurationPanel.add(new JLabel("remove"), c);
        
        int n = this.optimizationScheme.getParameter().size();
        
        for (Parameter2 p : this.optimizationScheme.getParameter()) {            
            counter++;
            c.insets = new Insets(0, 0, 2, 0);
            c.gridx = 0;
            c.gridy = counter;
            c.anchor = GridBagConstraints.NORTHWEST;

            parameterConfigurationPanel.add(new JLabel(p.toString()), c);
            c.gridx = 1;
            c.anchor = GridBagConstraints.NORTH;
            JTextField lowerBound = new JTextField(Double.toString(p.getLowerBound()), 5);
            lowerBound.addKeyListener(stdNumericKeyListener);
            lowerBound.addFocusListener(stdFocusListener);
            lowerBound.putClientProperty("parameter", p);
            lowerBound.putClientProperty("mode", NumericKeyListener.MODE_LOWERBOUND);
            parameterConfigurationPanel.add(lowerBound, c);

            c.gridx = 2;
            c.anchor = GridBagConstraints.NORTH;
            JTextField upperBound = new JTextField(Double.toString(p.getUpperBound()), 5);
            upperBound.addKeyListener(stdNumericKeyListener);
            upperBound.addFocusListener(stdFocusListener);
            upperBound.putClientProperty("parameter", p);
            upperBound.putClientProperty("mode", NumericKeyListener.MODE_UPPERBOUND);
            parameterConfigurationPanel.add(upperBound, c);

            c.gridx = 3;
            c.anchor = GridBagConstraints.NORTH;
            if (counter == n) {
                if (250 > n * 25) {
                    c.insets = new Insets(0, 0, 250 - n * 25, 0);
                }
            }

            JTextField startValue = new JTextField(5);
            if (p.getStartValue() != null && p.getStartValue().length > 0) {
                startValue.setText(Double.toString(p.getStartValue()[0]));
            }
            startValue.addKeyListener(stdNumericKeyListener);
            startValue.addFocusListener(stdFocusListener);
            startValue.putClientProperty("parameter", p);
            startValue.putClientProperty("mode", NumericKeyListener.MODE_STARTVALUE);
            parameterConfigurationPanel.add(startValue, c);
            
            JButton remButton = new JButton("x");
            c.gridx = 4;
            parameterConfigurationPanel.add(remButton, c);
        }
        
        parameterConfigurationPanel.updateUI();
        parameterConfigurationPanel.invalidate();
    }


public JDialog showDialog(JFrame parent) {
        dialog = new JDialog(parent);
        JPanel panel = new JPanel(new BorderLayout());
        JPanel buttonBar = new JPanel(new FlowLayout());
        panel.add(this, BorderLayout.CENTER);
        panel.add(buttonBar, BorderLayout.SOUTH);

        buttonBar.add(new JButton("OK") {
            {
                addActionListener(new ActionListener() {
                    @Override
        public void actionPerformed(ActionEvent e) {                        
                        dialog.setVisible(false);
                        for (ActionListener listener : listeners) {
                            listener.actionPerformed(new ActionEvent(OptimizerConfiguration.this, 1, "doc_modified"));
                        }
                    }
                });
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
        int c=0;
        for (Optimizer o : set) {
            availableOptimizer[c++] = o.getDescription();            
        }
        
        availableParameters = this.optimizationScheme.getModelParameters();
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
    }
