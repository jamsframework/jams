/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import jams.gui.input.CalendarInput;
import jams.gui.input.ValueChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.SortedSet;
import java.util.TimeZone;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import optas.efficiencies.EfficiencyCalculator;
import optas.efficiencies.UniversalEfficiencyCalculator;
import optas.metamodel.AttributeWrapper;

import optas.metamodel.Objective;

/**
 *
 * @author chris
 */
public class WizardObjectivePanel extends JPanel implements Comparable{
    JPanel timeIntervalList = new JPanel(new BorderLayout());    
    JList measurementList = new JList();
    JList simulationList = new JList();

    String methods[] = null;
    
    JTextField nameField = new JTextField(30);
    JTextField customNameField = new JTextField(10);
        
    Objective objective;

    AttributeWrapper simulationData[] = null;
    AttributeWrapper observationData[] = null;
    
    public WizardObjectivePanel(Objective o, SortedSet<AttributeWrapper> objectiveSet ) {
        objective = o;
        methods = UniversalEfficiencyCalculator.availableEfficiencies;
        setData(objectiveSet);
        measurementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        simulationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        updateMainPanel();
        measurementList.setSelectedValue(objective.getMeasurement(), true);
        simulationList.setSelectedValue(objective.getSimulation(), true);
    }

    private void updateMainPanel(){
        JPanel objectiveConfigurationPanel = new JPanel(new BorderLayout());
        JPanel dataLists = new JPanel(new BorderLayout());

        removeAll();
        setLayout(new BorderLayout());

        JScrollPane scrollPaneMeasurementList = new JScrollPane(measurementList);
        JScrollPane scrollPaneSimulationList = new JScrollPane(simulationList);

        scrollPaneSimulationList.setPreferredSize(new Dimension(350,300));
        scrollPaneMeasurementList.setPreferredSize(new Dimension(350,300));
        scrollPaneMeasurementList.setBorder(BorderFactory.createTitledBorder(
                "Select Observation"));

        scrollPaneSimulationList.setBorder(BorderFactory.createTitledBorder(
                "Select Simulation"));

        dataLists.add(scrollPaneMeasurementList, BorderLayout.CENTER);
        dataLists.add(scrollPaneSimulationList, BorderLayout.EAST);

        JButton addTimeInterval = new JButton("Add time interval");
        addTimeInterval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TimeInterval t = JAMSDataFactory.getDataFactory().createTimeInterval();
                t.setStart(JAMSDataFactory.getDataFactory().createCalendar());
                t.setEnd(JAMSDataFactory.getDataFactory().createCalendar());
                WizardObjectivePanel.this.objective.addTimeDomain(t);
                WizardObjectivePanel.this.syncData();
                WizardObjectivePanel.this.updateMainPanel();
            }
        });
        JPanel namePanel = new JPanel(new FlowLayout());
        namePanel.add(new JLabel("Name"));
        namePanel.add(this.customNameField);
        customNameField.setText(objective.getCustomName());
        namePanel.add(new JLabel("Description"));
        namePanel.add(nameField);
        nameField.setEditable(false);
        
        customNameField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                WizardObjectivePanel.this.objective.setCustomName(customNameField.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                WizardObjectivePanel.this.objective.setCustomName(customNameField.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                WizardObjectivePanel.this.objective.setCustomName(customNameField.getText());
            }
        });
        JPanel itemPanel = new JPanel(new FlowLayout());
        JComboBox methodBox = new JComboBox(methods);
        itemPanel.add(methodBox);
        methodBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {                
                WizardObjectivePanel.this.objective.setMethod((String)e.getItem());
            }
        });
        objective.setMethod(methods[0]);
        itemPanel.add(addTimeInterval);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(namePanel, BorderLayout.NORTH);
        northPanel.add(itemPanel, BorderLayout.SOUTH);

        objectiveConfigurationPanel.add(northPanel, BorderLayout.NORTH);
        objectiveConfigurationPanel.add(getTimeIntervalList(), BorderLayout.CENTER);

        add(dataLists, BorderLayout.NORTH);
        add(objectiveConfigurationPanel, BorderLayout.CENTER);

        syncData();

        updateUI();
    }

    private String generateName(){
        AttributeWrapper a1=(AttributeWrapper)measurementList.getSelectedValue();
        AttributeWrapper a2=(AttributeWrapper)simulationList.getSelectedValue();

        if (a1==null || a2 == null){
            return "-------";
        }
        String name = objective.getId() + ":" + a1.getAttributeName() + "<->" + a2.getAttributeName();

        return name;
    }

    public Objective getObjective() {
        return objective;
    }

    private void syncData() {
        for (Component c : timeIntervalList.getComponents()) {
            if (c instanceof CalendarInput) {
                CalendarInput ci = (CalendarInput) c;

                Calendar data = (Calendar) ci.getClientProperty("data");                
                data.setTimeZone(TimeZone.getTimeZone("GMT"));
                ci.getCalendarValue().setTimeZone(TimeZone.getTimeZone("GMT"));
                data.setTime(ci.getCalendarValue().getTime());
                
            }
        }
    }
    
    private JComponent getTimeIntervalList() {
        JScrollPane scrollPaneTimeIntervalList = new JScrollPane(timeIntervalList);

        timeIntervalList.removeAll();
        timeIntervalList.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        for (int i = 0; i < objective.getTimeDomain().size(); i++) {
            TimeInterval interval = objective.getTimeDomain().get(i);

            CalendarInput inputStart = new CalendarInput(true);
            CalendarInput inputEnd = new CalendarInput(true);
            JButton remove = new JButton("remove");

            Calendar t1 = interval.getStart().getValue();            
            Calendar t2 = interval.getEnd().getValue();
            t1.setTimeZone(TimeZone.getTimeZone("GMT"));
            t2.setTimeZone(TimeZone.getTimeZone("GMT"));

            inputStart.setValue(interval.getStart());
            
            inputEnd.setValue(interval.getEnd());

            inputStart.putClientProperty("data", interval.getStart());
            inputEnd.putClientProperty("data", interval.getEnd());
            remove.putClientProperty("data", interval);

            remove.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Object data = ((JButton) e.getSource()).getClientProperty("data");
                    objective.removeTimeDomain((TimeInterval) data);
                    updateMainPanel();
                }
            });

            inputStart.setDate(interval.getStart().getTime());
            inputEnd.setDate(interval.getEnd().getTime());

            inputStart.addValueChangeListener(new ValueChangeListener() {
                public void valueChanged() {
                    syncData();
                }
            });
            inputEnd.addValueChangeListener(new ValueChangeListener() {
                public void valueChanged() {
                    syncData();
                }
            });

            c.gridx = 0;
            c.gridy = i;
            timeIntervalList.add(inputStart,c);

            c.gridx = 1;
            c.gridy = i;
            timeIntervalList.add(inputEnd,c);

            c.gridx = 2;
            c.gridy = i;
            timeIntervalList.add(remove,c);
        }

        return scrollPaneTimeIntervalList;
    }

    private void setData(SortedSet<AttributeWrapper> objectiveSet) {
        observationData = objectiveSet.toArray(new AttributeWrapper[objectiveSet.size()]);
        simulationData = objectiveSet.toArray(new AttributeWrapper[objectiveSet.size()]);

        DefaultListModel obsModel = new DefaultListModel();
        for (AttributeWrapper a : observationData) {
            if (a.isIsSetByValue()) {
                continue;
            }
            if (a.getContextName() != null) {
                obsModel.addElement(a);
            }

        }        
        this.measurementList.setModel(obsModel);
        this.measurementList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                AttributeWrapper a = (AttributeWrapper) measurementList.getSelectedValue();
                DefaultListModel simModel = new DefaultListModel();
                for (AttributeWrapper sim : WizardObjectivePanel.this.simulationData) {
                    if (sim.isIsSetByValue()) {
                        continue;
                    }
                    if (sim.getContextName() != null && sim.getContextName().compareTo(a.getContextName())==0) {
                        simModel.addElement(sim);
                    }
                }
                simulationList.setModel(simModel);

                WizardObjectivePanel.this.objective.setMeasurement(a);
                WizardObjectivePanel.this.nameField.setText(generateName());
            }
        });
        this.simulationList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                AttributeWrapper a = (AttributeWrapper) simulationList.getSelectedValue();
                nameField.setText(generateName());
                WizardObjectivePanel.this.objective.setSimulation(a);
            }
        });
    }

    private boolean isApproved;
    public boolean showDialog(JFrame owner){
        final JDialog dialog = new JDialog(owner,"Objective configurator",true);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(this,BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                isApproved = true;
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                isApproved = false;
            }
        });
        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(okButton);
        southPanel.add(cancelButton);
        rootPanel.add(southPanel,BorderLayout.SOUTH);

        dialog.getContentPane().add(rootPanel);
        dialog.setSize(new Dimension(700,700));
        dialog.invalidate();
        dialog.setVisible(true);
        return isApproved;
    }

    @Override
    public String toString(){
        if (nameField.getText().isEmpty()){
            return "-------------";
        }
        return this.nameField.getText();
    }

    public int compareTo(Object o){
        return this.toString().compareTo(o.toString());
    }
}


/*
 *
        objectiveList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent evt) {
                Object objectives[] = objectiveList.getSelectedValuesList();

                GridBagConstraints c = new GridBagConstraints();
                int counter = 0;
                objectiveConfigurationPanel.removeAll();
                c.gridx = 0;
                c.gridy = counter;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.insets = new Insets(5, 0, 5, 0);
                c.anchor = GridBagConstraints.NORTHWEST;
                objectiveConfigurationPanel.add(new JLabel(JAMS.i18n("Objective")), c);
                c.gridx = 1;
                c.anchor = GridBagConstraints.CENTER;
                objectiveConfigurationPanel.add(new JLabel(JAMS.i18n("mode")), c);

                for (Object o : objectives) {
                    String obj = (String) o;
                    counter++;
                    c.insets = new Insets(0, 0, 2, 0);
                    c.gridx = 0;
                    c.gridy = counter;
                    c.anchor = GridBagConstraints.NORTHWEST;
                    objectiveConfigurationPanel.add(new JLabel(obj), c);

                    c.gridx = 1;
                    c.anchor = GridBagConstraints.NORTH;

                    JComboBox selectObjectiveMode = new JComboBox(optimizationModes);
                    selectObjectiveMode.putClientProperty("name", obj);
                    Object selection = objectiveConfigurationPanel.getClientProperty(obj);
                    if (selection != null) {
                        selectObjectiveMode.setSelectedIndex(((Integer) selection).intValue());
                    } else {
                        objectiveConfigurationPanel.putClientProperty(obj, new Integer(0));
                    }
                    System.out.println("changed");
                    selectObjectiveMode.addItemListener(new ItemListener() {

                        public void itemStateChanged(ItemEvent e) {
                            JComboBox src = (JComboBox) e.getSource();
                            objectiveConfigurationPanel.putClientProperty(
                                    src.getClientProperty("name"),
                                    src.getSelectedIndex());

                        }
                    });

                    if (counter == objectives.length) {
                        if (140 > objectives.length * 24) {
                            c.insets = new Insets(0, 0, 140 - objectives.length * 24, 0);
                        }
                    }
                    objectiveConfigurationPanel.add(selectObjectiveMode, c);
                }
                objectiveConfigurationPanel.updateUI();
                mainPanel.validate();
            }
        });
 */