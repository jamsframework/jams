/*
 * Context.java
 * Created on 21.03.2010, 20:45:41
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui;

import jams.gui.tools.GUIHelper;
import jamsui.juice.JUICE;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jams.JAMS;
import jams.gui.input.ListInput;
import jams.meta.ComponentDescriptor;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.OutputDSDescriptor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSDlg extends JDialog {

    private JButton okButton, cancelButton;
    private ModelView view;
    private DSListInput dslist;
    private ListInput filterList, attributeList;
    private NewDSDlg newDSDlg;

    public OutputDSDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        setModal(false);
        setResizable(false);
        setLocationByPlatform(true);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setBorder(BorderFactory.createTitledBorder("Datastore detail"));
        GridBagLayout mainLayout = new GridBagLayout();
        contentPanel.setLayout(mainLayout);

        JPanel storesPanel = new JPanel();
        storesPanel.setBorder(BorderFactory.createTitledBorder("Datastores"));
        getContentPane().add(storesPanel, BorderLayout.WEST);

        dslist = new DSListInput(false);
        dslist.setPreferredSize(new Dimension(200, 200));
        storesPanel.add(dslist);

        dslist.getListbox().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                updateContextAttributes();
            }
        });

        filterList = new ListInput(false);
        attributeList = new ListInput(false);

        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.resources.getString("Filters")), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, filterList, 1, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.resources.getString("Attributes")), 1, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, attributeList, 1, 30, 1, 1, 0, 0);
//        GUIHelper.addGBComponent(contentPanel, mainLayout, contextCombo, 1, 40, 1, 1, 0, 0);

        okButton = new JButton(JAMS.resources.getString("OK"));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        cancelButton = new JButton(JAMS.resources.getString("Cancel"));
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
        getRootPane().setDefaultButton(okButton);

    }

    @Override
    public void setVisible(boolean isVisible) {
        if (isVisible) {
            view = JUICE.getJuiceFrame().getCurrentView();
            HashMap<String, ComponentDescriptor> cdMap = view.getModelDescriptor().getComponentDescriptors();

            // get the stores
            HashMap<String, OutputDSDescriptor> stores = view.getModelDescriptor().getDatastores();

            // create a list of all stores
            ArrayList<OutputDSDescriptor> storesList = new ArrayList<OutputDSDescriptor>();
            for (OutputDSDescriptor store : stores.values()) {
                storesList.add(store);
            }

            // sort the stores list
            Collections.sort(storesList, new Comparator<OutputDSDescriptor>() {

                @Override
                public int compare(OutputDSDescriptor a1, OutputDSDescriptor a2) {
                    return a1.getName().compareTo(a2.getName());
                }
            });

            OutputDSDescriptor[] storesArray = storesList.toArray(new OutputDSDescriptor[storesList.size()]);


//            dsList.setModel(new ComboBoxModel() {
//
//                public int getSize() {
//                    HashMap<String, OutputDSDescriptor> stores = view.getModelDescriptor().getDatastores();
//                    return stores.size();
//                }
//
//                public Object getElementAt(int index) {
//
//
//
//                    return storesList.get(index);
//                }
//
//                public void setSelectedItem(Object anItem) {
//                }
//
//                public Object getSelectedItem() {
//                }
//
//                public void addListDataListener(ListDataListener l) {
//                }
//
//                public void removeListDataListener(ListDataListener l) {
//                }
//            });
        }

        super.setVisible(isVisible);
    }

    private void updateContextAttributes() {
//        System.out.println("updating list");
//        ContextDescriptor cd = (ContextDescriptor) contextCombo.getSelectedItem();
//
//        for (ContextAttribute ca : cd.getDynamicAttributes().values()) {
//            System.out.println(ca.getName());
//        }
    }

    class DSListInput extends ListInput {

        public DSListInput(boolean orderButtons) {
            super(orderButtons);
        }

        protected void addItem() {

            if (newDSDlg == null) {
                newDSDlg = new NewDSDlg(OutputDSDlg.this.getOwner());
            }

            view = JUICE.getJuiceFrame().getCurrentView();
            HashMap<String, ComponentDescriptor> cdMap = view.getModelDescriptor().getComponentDescriptors();

            // display the dialog
            newDSDlg.setVisible(true, cdMap);

            // Get the text field value
            if (newDSDlg.getResult() == NewDSDlg.RESULT_OK) {

                String stringValue = newDSDlg.getDsName() + " (" + newDSDlg.getContextName() + ")";

                // add this item to the list and refresh
                if (stringValue != null && !listData.getValue().contains(stringValue)) {
                    listData.addElement(stringValue);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }


            }

        }

        protected void editItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            if (selection >= 0) {
                // edit this item
                String value = listData.getElementAt(selection);
                value = GUIHelper.showInputDlg(DSListInput.this, null, JAMS.resources.getString("New_value"), value);
                if (value != null) {
                    listData.setElementAt(selection, value);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }
        }

        protected void removeItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            if (selection >= 0) {
                // remove this item from the list and refresh
                listData.removeElementAt(selection);
                scrollPane.revalidate();
                scrollPane.repaint();

                //select the next item
                if (selection >= listData.getValue().size()) {
                    selection = listData.getValue().size() - 1;
                }
                getListbox().setSelectedIndex(selection);
            }
        }
    }

    class NewDSDlg extends JDialog {

        private JTextField nameText = new JTextField();
        private JComboBox contextCombo = new JComboBox();
        public static final int RESULT_OK = 1, RESULT_CANCEL = 0;
        private int result = RESULT_CANCEL;
        private String dsName, contextName;

        public NewDSDlg(Window owner) {
            super(owner);
            setLocationRelativeTo(owner);
            setModal(true);
            setTitle("Datastore details");
            setResizable(false);
            setLocationByPlatform(true);
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel();
            getContentPane().add(contentPanel, BorderLayout.CENTER);
            GridBagLayout mainLayout = new GridBagLayout();
            contentPanel.setLayout(mainLayout);

            GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel("Name"), 1, 0, 1, 1, 0, 0);
            GUIHelper.addGBComponent(contentPanel, mainLayout, nameText, 1, 10, 1, 1, 0, 0);
            GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel("Context"), 1, 20, 1, 1, 0, 0);
            GUIHelper.addGBComponent(contentPanel, mainLayout, contextCombo, 1, 30, 1, 1, 0, 0);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    result = RESULT_OK;
                    dsName = nameText.getText();
                    contextName = contextCombo.getSelectedItem().toString();
                    setVisible(false);
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    result = RESULT_CANCEL;
                    dsName = null;
                    contextName = null;
                    setVisible(false);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);

            this.pack();
        }

        public void setVisible(boolean isVisible, HashMap<String, ComponentDescriptor> cdMap) {

            if (isVisible) {
                // create a list containing all contexts of this model
                ArrayList<ComponentDescriptor> contextList = new ArrayList<ComponentDescriptor>();
                for (ComponentDescriptor cd : cdMap.values()) {
                    if (cd instanceof ContextDescriptor) {
                        contextList.add(cd);
                    }
                }

                // sort the context list
                Collections.sort(contextList, new Comparator<ComponentDescriptor>() {

                    @Override
                    public int compare(ComponentDescriptor a1, ComponentDescriptor a2) {
                        return a1.toString().compareTo(a2.toString());
                    }
                });

                this.contextCombo.setModel(new DefaultComboBoxModel(contextList.toArray(new ComponentDescriptor[contextList.size()])));
            }
            super.setVisible(isVisible);

        }

        /**
         * @return the result
         */
        public int getResult() {
            return result;
        }

        /**
         * @return the dsName
         */
        public String getDsName() {
            return dsName;
        }

        /**
         * @return the contextName
         */
        public String getContextName() {
            return contextName;
        }
    }
}
