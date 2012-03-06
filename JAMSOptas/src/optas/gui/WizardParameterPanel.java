/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui;

import jams.JAMS;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import optas.metamodel.Optimization;
import optas.metamodel.Parameter;

/**
 *
 * @author chris
 */
public class WizardParameterPanel extends JPanel{
    JList parameterList = new JList();

    NumericKeyListener stdNumericKeyListener = new NumericKeyListener();
    NumericFocusListener stdFocusListener = new NumericFocusListener();

    Optimization optimizationScheme=null;

    public WizardParameterPanel(Optimization optimizationScheme) {
        setLayout(new BorderLayout());

        this.optimizationScheme = optimizationScheme;

        final JPanel parameterConfigurationPanel;
        /*parameterPanel.setBorder(BorderFactory.createTitledBorder(
                JAMS.i18n("Parameter_Configuration")));*/

        JScrollPane scrollPaneParameterList = new JScrollPane(parameterList);

        scrollPaneParameterList.setPreferredSize(new Dimension(300, 250));

        scrollPaneParameterList.setBorder(BorderFactory.createTitledBorder(
                JAMS.i18n("Parameter_Configuration")));
        
        add(scrollPaneParameterList, BorderLayout.CENTER);

        //parameterPanel.setMinimumSize(new Dimension(500, 500));
        //parameterPanel.setPreferredSize(new Dimension(500, 500));
        
        parameterConfigurationPanel = new JPanel(new GridBagLayout());
        JScrollPane scrollPaneParameterConfiguration = new JScrollPane(parameterConfigurationPanel);
        scrollPaneParameterConfiguration.setPreferredSize(new Dimension(440, 250));

        add(scrollPaneParameterConfiguration, BorderLayout.EAST);
        parameterList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        parameterList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                Object params[] = parameterList.getSelectedValues();

                GridBagConstraints c = new GridBagConstraints();
                int counter = 0;
                parameterConfigurationPanel.removeAll();
                c.gridx = 0;
                c.gridy = counter;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.insets = new Insets(5, 0, 5, 0);
                c.anchor = GridBagConstraints.NORTHWEST;
                parameterConfigurationPanel.add(new JLabel(JAMS.i18n("parameter")), c);
                c.gridx = 1;
                c.anchor = GridBagConstraints.CENTER;
                parameterConfigurationPanel.add(new JLabel(JAMS.i18n("lower_bound")), c);
                c.gridx = 2;
                parameterConfigurationPanel.add(new JLabel(JAMS.i18n("upper_bound")), c);
                c.gridx = 3;
                parameterConfigurationPanel.add(new JLabel(JAMS.i18n("start_value")), c);

                for (Object o : params) {
                    Parameter p = (Parameter) o;
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
                    if (counter == params.length) {
                        if (250 > params.length * 25) {
                            c.insets = new Insets(0, 0, 250 - params.length * 25, 0);
                        }
                    }

                    JTextField startValue = new JTextField(5);
                    if (p.isStartValueValid()) {
                        startValue.setText(Double.toString(p.getStartValue()));
                    }
                    startValue.addKeyListener(stdNumericKeyListener);
                    startValue.addFocusListener(stdFocusListener);
                    startValue.putClientProperty("parameter", p);
                    startValue.putClientProperty("mode", NumericKeyListener.MODE_STARTVALUE);
                    parameterConfigurationPanel.add(startValue, c);
                }
                sync();
                parameterConfigurationPanel.updateUI();
                parameterConfigurationPanel.invalidate();
            }
        });       
    }

    public void fillParameterList(SortedSet<Parameter> parameterSet) {
        DefaultListModel model = new DefaultListModel();
        for (Parameter parameter : parameterSet) {
            model.addElement(parameter);
        }
        parameterList.setModel(model);

        ArrayList<Parameter> list = optimizationScheme.getParameter();
        int selIndexList[] = new int[list.size()];
        int counter = 0;
        for (int i=0;i<model.getSize();i++){
            if (list.contains(model.getElementAt(i))){
                selIndexList[counter++] = i;
                Parameter pSrc = list.get(list.indexOf(model.getElementAt(i)));
                Parameter pDst = (Parameter)model.getElementAt(i);
                
                pDst.setId(pSrc.getId());
                pDst.setLowerBound(pSrc.getLowerBound());
                pDst.setUpperBound(pSrc.getUpperBound());
                pDst.setStartValue(pSrc.getStartValue());
                pDst.setStartValueValid(pSrc.isStartValueValid());
            }
        }
        parameterList.setSelectedIndices(selIndexList);
        
        updateUI();
        invalidate();
    }

    private void sync(){
        Object list[] = this.parameterList.getSelectedValues();
        ArrayList<Parameter> p = this.optimizationScheme.getParameter();
        p.clear();
        for (Object o : list){
            p.add((Parameter)o);
        }
        
    }
}
