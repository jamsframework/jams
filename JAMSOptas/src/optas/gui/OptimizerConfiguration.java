/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.gui;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import optas.metamodel.ModelModifier2;
import optas.metamodel.Optimization;
import optas.metamodel.Tools;
import optas.optimizer.Optimizer;
import optas.optimizer.OptimizerLibrary;
import optas.optimizer.management.BooleanOptimizerParameter;
import optas.optimizer.management.NumericOptimizerParameter;
import optas.optimizer.management.OptimizerDescription;
import optas.optimizer.management.OptimizerParameter;
import optas.optimizer.management.StringOptimizerParameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author christian
 */
public class OptimizerConfiguration extends JPanel {

    OptimizerDescription availableOptimizer[];
    Optimization optimizationScheme = new Optimization();
    NumericKeyListener stdNumericKeyListener = new NumericKeyListener();
    NumericFocusListener stdFocusListener = new NumericFocusListener();
    Document doc = null, newDoc = null;
    JDialog dialog = null;
    JPanel optimizerConfigurationPanel = new JPanel(new GridBagLayout());
    Dimension prefSize = new Dimension(700, 260);
    HashSet<ActionListener> listeners = new HashSet<ActionListener>();

    public void addActionListener(ActionListener listener) {
        this.listeners.add(listener);
    }

    public Document getModifiedDocument() {
        return newDoc;
    }

    private Optimization getSchemeFromOptimizer(Element node) {
        Optimization scheme = new Optimization();

        initOptimizerDesc(null);

        String optimizerClass = null;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeName().equals("var")) {
                Element e = (Element) child;
                if (e.getAttribute("name").equals("optimizationClassName")) {
                    optimizerClass = e.getAttribute("value");
                }
            }

        }
        if (optimizerClass == null) {
            return null;
        }
        for (int i = 0; i < this.availableOptimizer.length; i++) {
            if (this.availableOptimizer[i].getOptimizerClassName().equals(optimizerClass)) {
                scheme.setOptimizerDescription(availableOptimizer[i]);
            }
        }

        NodeList list = node.getElementsByTagName("var");
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i) instanceof Element) {
                Element e = (Element) list.item(i);
                if (!e.hasAttribute("name")) {
                    continue;
                }
                if (e.getAttribute("name").equals("parameterization")) {
                    String parameterValue = e.getAttribute("value");
                    String parameterList[] = parameterValue.split(";");
                    for (String parameterValuePair : parameterList) {
                        String entry[] = parameterValuePair.split("=");
                        String name = entry[0];
                        String value = entry[1];

                        if (entry.length != 2) {
                            continue;
                        }
                        OptimizerParameter op = scheme.getOptimizerDescription().getPropertyMap().get(name);
                        if (op!=null)
                            op.setString(value);
                    }
                }
            }
        }

        if (Tools.getNodeByClass(node, "optas.optimizer.HRUReducer").size() > 0) {
            scheme.getOptimizerDescription().getDoSpatialRelaxation().setValue(true);
        } else {
            scheme.getOptimizerDescription().getDoSpatialRelaxation().setValue(false);
        }

        return scheme;
    }

    public OptimizerConfiguration(Document doc) {
        this.doc = doc;
        Node root = Tools.getModelNode(doc);
        ArrayList<Element> optimizerNodes = Tools.getNodeByName(root, "optimizer");
        if (optimizerNodes.size() > 0) {
            Element optimizer = optimizerNodes.get(0);
            optimizationScheme = getSchemeFromOptimizer(optimizer);
            if (optimizationScheme==null){
                optimizationScheme = new Optimization();
            }
        }
        initOnce(optimizationScheme);
        init();
    }

    private void initOnce(Optimization defaultOptimizer) {
        int selection = this.initOptimizerDesc(defaultOptimizer);

        this.removeAll();
        this.setLayout(new BorderLayout());
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
                            init();
                        }
                    });
                }
            }
        });
        if (selection != -1) {
            selectOptimizer.setSelectedIndex(selection);
        } else {
            optimizationScheme.setOptimizerDescription((OptimizerDescription) selectOptimizer.getSelectedItem());
        }

        scrollPaneOptimizerSpecificationPanel.setPreferredSize(new Dimension(700, 200));

        this.add(selectOptimizer, BorderLayout.NORTH);
        this.add(scrollPaneOptimizerSpecificationPanel, BorderLayout.CENTER);

    }

    public Optimization getOptimizer() {
        return optimizationScheme;
    }

    private void init() {
        newDoc = null;
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
                        OptimizerConfiguration.this.newDoc = ModelModifier2.addOptimizationContext(OptimizerConfiguration.this.doc, optimizationScheme);
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

    private int initOptimizerDesc(Optimization defaultOptimizer) {
        Set<Optimizer> set = OptimizerLibrary.getAvailableOptimizer();
        availableOptimizer = new OptimizerDescription[set.size()];
        int selection = -1;
        int c = 0;
        for (Optimizer o : set) {
            availableOptimizer[c] = o.getDescription();
            if (optimizationScheme != null) {
                if (this.optimizationScheme.getOptimizerDescription() != null) {
                    if (this.optimizationScheme.getOptimizerDescription().getOptimizerClassName().equals(availableOptimizer[c].getOptimizerClassName())) {
                        availableOptimizer[c] = this.optimizationScheme.getOptimizerDescription();
                        if (defaultOptimizer != null) {
                            if (availableOptimizer[c].getOptimizerClassName().equals(defaultOptimizer.getOptimizerDescription().getOptimizerClassName()));
                            selection = c;
                        }
                    }
                }
            }
            c++;
        }
        return selection;
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
