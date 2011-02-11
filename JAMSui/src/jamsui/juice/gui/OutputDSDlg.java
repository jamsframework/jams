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
import jams.meta.ModelDescriptor;
import jams.meta.OutputDSDescriptor;
import jams.meta.OutputDSDescriptor.FilterDescriptor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSDlg extends JDialog {

    private JButton okButton;
    private ModelView view;
    private DSListInput dslist;
    private FilterListInput filterList;
    private AttributeListInput attributeList;

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

        dslist = new DSListInput();
        dslist.setPreferredSize(new Dimension(200, 200));
        storesPanel.add(dslist);

        dslist.getListbox().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateContextAttributes(e.getSource());
                }
            }
        });

        filterList = new FilterListInput();
        filterList.setPreferredSize(new Dimension(200, 100));
        filterList.setEnabled(false);

        attributeList = new AttributeListInput();
        attributeList.setPreferredSize(new Dimension(200, 300));
        attributeList.setEnabled(false);

        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.resources.getString("Attributes")), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, attributeList, 1, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.resources.getString("Filters")), 1, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, filterList, 1, 30, 1, 1, 0, 0);
//        GUIHelper.addGBComponent(contentPanel, mainLayout, contextCombo, 1, 40, 1, 1, 0, 0);

        okButton = new JButton(JAMS.resources.getString("OK"));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.pack();
        getRootPane().setDefaultButton(okButton);
    }

    @Override
    public void setVisible(boolean isVisible) {
        if (isVisible) {

            // get the stores
            view = JUICE.getJuiceFrame().getCurrentView();
            dslist.setValue(view.getModelDescriptor());
        }

        super.setVisible(isVisible);
    }

    private void updateContextAttributes(Object value) {

        JList list = (JList) value;
        OutputDSDescriptor ods = (OutputDSDescriptor) list.getSelectedValue();

        if (ods != null) {

            filterList.setValue(ods);
            attributeList.setValue(ods);

            filterList.setEnabled(true);
            attributeList.setEnabled(true);

        } else {
            filterList.setValue(null);
            attributeList.setValue(null);

            filterList.setEnabled(false);
            attributeList.setEnabled(false);
        }
    }

    class FilterListInput extends ListInput {

        private OutputDSDescriptor ods;
        private NewDSDlg newDSDlg;

        public FilterListInput() {
            super(false);
        }

        public void setValue(OutputDSDescriptor ods) {
            this.ods = ods;

            Vector<Object> fVector = new Vector<Object>();

            if (ods == null) {
                this.setListData(fVector);
                return;
            }

            for (FilterDescriptor f : ods.getFilters()) {
                fVector.add(f);
            }
            this.setListData(fVector);
        }

        protected void addItem() {

            if (newDSDlg == null) {
                newDSDlg = new NewDSDlg(OutputDSDlg.this, "Filter details", true);
            }

            view = JUICE.getJuiceFrame().getCurrentView();
            HashMap<String, ComponentDescriptor> cdMap = view.getModelDescriptor().getComponentDescriptors();

            // create a list containing all contexts of this model
            ArrayList<Object> contextList = new ArrayList<Object>();
            for (ComponentDescriptor cd : cdMap.values()) {
                if (cd instanceof ContextDescriptor) {
                    contextList.add(cd);
                }
            }

            // display the dialog
            newDSDlg.setVisible(true, contextList);

            // Get the text field value
            if (newDSDlg.getResult() == NewDSDlg.RESULT_OK) {

                ContextDescriptor context = (ContextDescriptor) newDSDlg.getValue()[0];

                FilterDescriptor f = ods.addFilter(context, newDSDlg.getDsName());

                setValue(ods);
            }
        }

        protected void editItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            if (selection >= 0) {
                // edit this item
                FilterDescriptor f = (FilterDescriptor) getListbox().getSelectedValue();

                String value = GUIHelper.showInputDlg(FilterListInput.this, null, JAMS.resources.getString("New_value"), f.expression);
                if (value != null) {
                    f.expression = value;
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }
        }

        protected void removeItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            FilterDescriptor value = (FilterDescriptor) getListbox().getSelectedValue();
            if (value != null) {

                ods.removeFilter(value);
                setValue(ods);

                //select the next item
                if (selection >= listData.getValue().size()) {
                    selection = listData.getValue().size() - 1;
                }
                getListbox().setSelectedIndex(selection);
            }
        }
    }

    class AttributeListInput extends ListInput {

        private OutputDSDescriptor ods;
        private NewDSDlg newDSDlg;

        public AttributeListInput() {
            super(false, false);
        }

        public void setValue(OutputDSDescriptor ods) {
            this.ods = ods;

            Vector<Object> aVector = new Vector<Object>();

            if (ods == null) {
                this.setListData(aVector);
                return;
            }

            // sort the context attributes list
            Collections.sort(ods.getContextAttributes(), new Comparator<ContextAttribute>() {

                @Override
                public int compare(ContextAttribute a1, ContextAttribute a2) {
                    return a1.getName().compareTo(a2.getName());
                }
            });

            for (ContextAttribute ca : ods.getContextAttributes()) {
                aVector.add(ca);
            }

            this.setListData(aVector);
        }

        protected void addItem() {
//            // Get the text field value
//            String stringValue = GUIHelper.showInputDlg(AttributeListInput.this, null, JAMS.resources.getString("New_value"), null);
//
//            // add this item to the list and refresh
//            if (stringValue != null && !listData.getValue().contains(stringValue)) {
//
//                ContextDescriptor context = ods.getContext();
//                ContextAttribute ca = context.getDynamicAttributes().get(stringValue);
//
//                if (ca == null) {
//                    GUIHelper.showErrorDlg(this, MessageFormat.format("Could not find attribute with name \"{0}\"", stringValue), JAMS.resources.getString("ERROR"));
//                    return;
//                }
//
//                if (!ods.getContextAttributes().contains(ca)) {
//                    ods.getContextAttributes().add(ca);
//                }
//
//                setValue(ods);
//            }

            if (newDSDlg == null) {
                newDSDlg = new NewDSDlg(OutputDSDlg.this, "Datastore details", false);
            }


            HashMap<String, ContextAttribute> caMap = ods.getContext().getDynamicAttributes();

            // create a list containing all contexts of this model
            ArrayList<Object> caList = new ArrayList<Object>();
            for (ContextAttribute ca : caMap.values()) {
                caList.add(ca);
            }

            // display the dialog
            newDSDlg.setVisible(true, caList);

            // Get the text field value
            if (newDSDlg.getResult() == NewDSDlg.RESULT_OK) {

                Object[] attributes = newDSDlg.getValue();

                for (Object attribute : attributes) {

                    ContextAttribute ca = (ContextAttribute) attribute;

                    if (ca == null) {
                        GUIHelper.showErrorDlg(this, "Could not add attribute", JAMS.resources.getString("ERROR"));
                        return;
                    }

                    if (!ods.getContextAttributes().contains(ca)) {
                        ods.getContextAttributes().add(ca);
                    }
                }
                setValue(ods);
            }
        }

        protected void removeItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            ContextAttribute value = (ContextAttribute) getListbox().getSelectedValue();
            if (value != null) {

                ods.getContextAttributes().remove(value);
                setValue(ods);

                //select the next item
                if (selection >= listData.getValue().size()) {
                    selection = listData.getValue().size() - 1;
                }
                getListbox().setSelectedIndex(selection);
            }
        }
    }

    class DSListInput extends ListInput {

        private ModelDescriptor md;
        private NewDSDlg newDSDlg;

        public DSListInput() {
            super(false);
        }

        public void setValue(ModelDescriptor md) {
            this.md = md;
            HashMap<String, OutputDSDescriptor> stores = md.getDatastores();

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

            Vector<Object> storesVector = new Vector<Object>();
            for (OutputDSDescriptor ods : storesList) {
                storesVector.add(ods);
            }
            this.setListData(storesVector);

        }

        protected void addItem() {

            if (newDSDlg == null) {
                newDSDlg = new NewDSDlg(OutputDSDlg.this, "Datastore details", true);
            }

            view = JUICE.getJuiceFrame().getCurrentView();
            HashMap<String, ComponentDescriptor> cdMap = view.getModelDescriptor().getComponentDescriptors();

            // create a list containing all contexts of this model
            ArrayList<Object> contextList = new ArrayList<Object>();
            for (ComponentDescriptor cd : cdMap.values()) {
                if (cd instanceof ContextDescriptor) {
                    contextList.add(cd);
                }
            }

            // display the dialog
            newDSDlg.setVisible(true, contextList);

            // Get the text field value
            if (newDSDlg.getResult() == NewDSDlg.RESULT_OK) {

                ContextDescriptor context = (ContextDescriptor) newDSDlg.getValue()[0];

                OutputDSDescriptor ods = new OutputDSDescriptor(context);
                ods.setName(newDSDlg.getDsName());

                md.addOutputDataStore(ods);

                setValue(md);
            }
        }

        protected void editItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            if (selection >= 0) {
                // edit this item
                OutputDSDescriptor ods = (OutputDSDescriptor) listData.getElementAt(selection);
                String value = GUIHelper.showInputDlg(DSListInput.this, null, JAMS.resources.getString("New_value"), ods.getName());
                if (value != null) {
                    ods.setName(value);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }
        }

        protected void removeItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            OutputDSDescriptor value = (OutputDSDescriptor) getListbox().getSelectedValue();
            if (value != null) {

                md.removeOutputDataStore(value);
                setValue(md);

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
        private JList objectList = new JList();
        public static final int RESULT_OK = 1, RESULT_CANCEL = 0;
        private int result = RESULT_CANCEL;
        private String dsName;
        private Object[] value;

        public NewDSDlg(Dialog owner, String title, boolean showTextField) {
            super(owner);
            setLocationRelativeTo(owner);
            setModal(true);
            setTitle(title);
            setResizable(false);
//            setLocationByPlatform(true);
            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel();
            getContentPane().add(contentPanel, BorderLayout.CENTER);
            GridBagLayout mainLayout = new GridBagLayout();
            contentPanel.setLayout(mainLayout);

            String listLabel;
            if (showTextField) {
                GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel("Name"), 1, 0, 1, 1, 0, 0);
                GUIHelper.addGBComponent(contentPanel, mainLayout, nameText, 1, 10, 1, 1, 0, 0);
                objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                listLabel = "Context";
            } else {
                objectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                listLabel = "Attribute";
            }

            JScrollPane listScroll = new JScrollPane(objectList);
            listScroll.setPreferredSize(new Dimension(200, 300));

            GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(listLabel), 1, 20, 1, 1, 0, 0);
            GUIHelper.addGBComponent(contentPanel, mainLayout, listScroll, 1, 30, 1, 1, 0, 0);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    result = RESULT_OK;
                    dsName = nameText.getText();
                    value = objectList.getSelectedValues();
                    setVisible(false);
                }
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    result = RESULT_CANCEL;
                    dsName = null;
                    value = null;
                    setVisible(false);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);

            this.pack();
        }

        public void setVisible(boolean isVisible, ArrayList<Object> values) {

            if (isVisible) {

                this.nameText.setText("");
                this.nameText.requestFocus();

                // sort the context list
                Collections.sort(values, new Comparator<Object>() {

                    @Override
                    public int compare(Object a1, Object a2) {
                        return a1.toString().compareTo(a2.toString());
                    }
                });

                this.objectList.setModel(new DefaultComboBoxModel(values.toArray(new Object[values.size()])));
            }
            pack();
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
        public Object[] getValue() {
            return value;
        }
    }
}
