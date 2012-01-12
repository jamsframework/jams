/*
 * Context.java
 * Created on 21.03.2010, 20:45:41
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui;

import jams.gui.tools.GUIHelper;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jams.JAMS;
import jams.gui.input.ListInput;
import jams.gui.input.TableInput;
import jams.meta.ComponentDescriptor;
import jams.meta.ContextAttribute;
import jams.meta.ContextDescriptor;
import jams.meta.ModelDescriptor;
import jams.meta.OutputDSDescriptor;
import jams.meta.OutputDSDescriptor.FilterDescriptor;
import jams.tools.StringTools;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class OutputDSDlg extends JDialog {

    static final int BUTTON_SIZE = 20;
    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowup.png")).getImage().getScaledInstance(10, 5, Image.SCALE_SMOOTH));
    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowdown.png")).getImage().getScaledInstance(10, 5, Image.SCALE_SMOOTH));
    private static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
    private JButton okButton;
    private ModelDescriptor md;
    private DSTableInput dslist;
    private FilterListInput filterList;
    private AttributeListInput attributeList;

    public OutputDSDlg(Frame owner, ModelDescriptor md) {
        super(owner);
        setTitle(JAMS.i18n("Datastore_editor"));
        setLocationRelativeTo(owner);
        setModal(false);
        setResizable(false);
        setLocationByPlatform(true);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Datastore_details")));
        GridBagLayout mainLayout = new GridBagLayout();
        contentPanel.setLayout(mainLayout);

        JPanel storesPanel = new JPanel();
        storesPanel.setBorder(BorderFactory.createTitledBorder(JAMS.i18n("Datastores")));
        getContentPane().add(storesPanel, BorderLayout.WEST);

        dslist = new DSTableInput();
        dslist.setPreferredSize(new Dimension(200, 200));
        storesPanel.add(dslist);

        dslist.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateContextAttributes(dslist);
                }
            }
        });

        filterList = new FilterListInput();
        filterList.setPreferredSize(new Dimension(200, 100));
        filterList.setEnabled(false);

        attributeList = new AttributeListInput();
        attributeList.setPreferredSize(new Dimension(200, 300));
        attributeList.setEnabled(false);

        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Attributes")), 1, 0, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, attributeList, 1, 10, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Filters")), 1, 20, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, filterList, 1, 30, 1, 1, 0, 0);
//        GUIHelper.addGBComponent(contentPanel, mainLayout, contextCombo, 1, 40, 1, 1, 0, 0);

        okButton = new JButton(JAMS.i18n("OK"));
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);

        this.md = md;

        this.pack();
    }

    @Override
    public void setVisible(boolean b) {
        dslist.setValue(md);
        super.setVisible(b);
    }

    private void updateContextAttributes(DSTableInput value) {

        int selection = value.getTable().getSelectedRow();
        if (selection == -1) {
            return;
        }
        OutputDSDescriptor ods = (OutputDSDescriptor) value.getTableData().get(selection)[1];

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
        private DSDlg newDSDlg;

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
                newDSDlg = new DSDlg(OutputDSDlg.this, JAMS.i18n("Add_filter"), true, false);
            }

//            view = JUICE.getJuiceFrame().getCurrentView();
            HashMap<String, ComponentDescriptor> cdMap = md.getComponentDescriptors();

            // create a list containing all contexts of this model
            ArrayList<Object> contextList = new ArrayList<Object>();
            for (ComponentDescriptor cd : cdMap.values()) {
                if (cd instanceof ContextDescriptor) {
                    contextList.add(cd);
                }
            }

            // display the dialog
            newDSDlg.update(contextList);
            newDSDlg.setVisible(true);


            // Get the text field value
            if (newDSDlg.getResult() == DSDlg.RESULT_OK) {

                if (StringTools.isEmptyString(newDSDlg.getDsName())) {
                    GUIHelper.showErrorDlg(this, JAMS.i18n("Filter_expression_must_not_be_empty!"), JAMS.i18n("Error_creating_new_filter"));
                    addItem();
                    return;
                }

                if (newDSDlg.getValue().length < 1) {
                    GUIHelper.showErrorDlg(this, JAMS.i18n("You_must_choose_a_context!"), JAMS.i18n("Error_creating_new_filter"));
                    addItem();
                    return;
                }

                ContextDescriptor context = (ContextDescriptor) newDSDlg.getValue()[0];

                FilterDescriptor f = ods.addFilter(context, newDSDlg.getDsName());

                setValue(ods);
            }
        }

        @Override
        protected void editItem() {
            //get the current selection
            int selection = getListbox().getSelectedIndex();
            if (selection >= 0) {
                // edit this item
                FilterDescriptor f = (FilterDescriptor) getListbox().getSelectedValue();

                String value = GUIHelper.showInputDlg(FilterListInput.this, null, JAMS.i18n("New_value"), f.expression);
                if (value != null) {

                    if (StringTools.isEmptyString(value)) {
                        GUIHelper.showErrorDlg(this, JAMS.i18n("Filter_expression_must_not_be_empty!"), JAMS.i18n("Error_creating_new_filter"));
                        editItem();
                        return;
                    }

                    f.expression = value;
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
            }
        }

        @Override
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
        private DSDlg newDSDlg;

        public AttributeListInput() {
            super(true, false);
            getListbox().removeMouseListener(editListener);
            this.getListbox().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }

        public void setValue(OutputDSDescriptor ods) {
            this.ods = ods;

            Vector<Object> aVector = new Vector<Object>();

            if (ods == null) {
                this.setListData(aVector);
                return;
            }

            // sort the context attributes list
//            Collections.sort(ods.getContextAttributes(), new Comparator<ContextAttribute>() {
//
//                @Override
//                public int compare(ContextAttribute a1, ContextAttribute a2) {
//                    return a1.getName().compareTo(a2.getName());
//                }
//            });

            for (ContextAttribute ca : ods.getContextAttributes()) {
                aVector.add(ca);
            }

            this.setListData(aVector);
        }

        @Override
        protected void addItem() {

            if (newDSDlg == null) {
                newDSDlg = new DSDlg(OutputDSDlg.this, JAMS.i18n("Add_attributes"), false, false);
            }

            HashMap<String, ContextAttribute> caMap = ods.getContext().getDynamicAttributes();

            // create a list containing all contexts of this model
            ArrayList<Object> caList = new ArrayList<Object>();
            Vector<Object> ld = getListData();
            for (ContextAttribute ca : caMap.values()) {
                if (!ld.contains(ca)) {
                    caList.add(ca);
                }
            }

            // display the dialog
            newDSDlg.update(caList);
            newDSDlg.setVisible(true);


            // Get the text field value
            if (newDSDlg.getResult() == DSDlg.RESULT_OK) {

                Object[] attributes = newDSDlg.getValue();

                for (Object attribute : attributes) {

                    ContextAttribute ca = (ContextAttribute) attribute;

                    if (ca == null) {
                        GUIHelper.showErrorDlg(this, JAMS.i18n("Could_not_add_attribute"), JAMS.i18n("ERROR"));
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
            Object[] values = getListbox().getSelectedValues();

            if (values.length > 0) {

                for (int i = 0; i < values.length; i++) {
                    ods.getContextAttributes().remove((ContextAttribute) values[i]);
                }
                setValue(ods);

                //select the next item
                if (selection >= listData.getValue().size()) {
                    selection = listData.getValue().size() - 1;
                }
                getListbox().setSelectedIndex(selection);
            }
        }

        @Override
        protected void moveUp() {
            int[] indices = getListbox().getSelectedIndices();
            for (int i = 0; i < indices.length; i++) {
                int index = indices[i];
                if (index > 0) {
                    ArrayList<ContextAttribute> al = ods.getContextAttributes();
                    ContextAttribute tmp = al.get(index - 1);
                    al.set(index - 1, al.get(index));
                    al.set(index, tmp);
                    indices[i]--;
                }
            }
            setValue(ods);
            getListbox().setSelectedIndices(indices);

        }

        @Override
        protected void moveDown() {
            int[] indices = getListbox().getSelectedIndices();
            for (int i = indices.length - 1; i >= 0; i--) {
                int index = indices[i];
                if (index < listData.getValue().size() - 1) {
                    ArrayList<ContextAttribute> al = ods.getContextAttributes();
                    ContextAttribute tmp = al.get(index + 1);
                    al.set(index + 1, al.get(index));
                    al.set(index, tmp);
                    indices[i]++;
                }
            }
            setValue(ods);
            getListbox().setSelectedIndices(indices);
        }
    }

    class DSTableInput extends TableInput {

        private ModelDescriptor md;
        private DSDlg newDSDlg, editDSDlg;

        public DSTableInput() {
            super(new String[]{"enabled", "store"}, new Class[]{Boolean.class, String.class}, new boolean[]{true, false}, false);

            getTable().setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

            int vColIndex = 0;
            TableColumn col = getTable().getColumnModel().getColumn(vColIndex);
            int width = 50;
            col.setPreferredWidth(width);


            ((AbstractTableModel) this.getTable().getModel()).addTableModelListener(new TableModelListener() {

                public void tableChanged(TableModelEvent e) {
                    for (Object row[] : tableData.getValue()) {
                        if (row[0] instanceof Boolean) {
                            if ((Boolean) row[0]) {
                                ((OutputDSDescriptor) row[1]).setEnabled(true);
                            } else {
                                ((OutputDSDescriptor) row[1]).setEnabled(false);
                            }
                        }
                    }
                }
            });
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

            ArrayList<Object[]> storesVector = new ArrayList<Object[]>();
            for (OutputDSDescriptor ods : storesList) {
                Object row[] = new Object[2];
                row[0] = new Boolean(ods.isEnabled());
                row[1] = ods;
                storesVector.add(row);
            }
            this.setTableData(storesVector);

        }

        @Override
        protected void addItem() {

            if (newDSDlg == null) {
                newDSDlg = new DSDlg(OutputDSDlg.this, JAMS.i18n("Add_datastore"), true, true);
            }

//            view = JUICE.getJuiceFrame().getCurrentView();
            HashMap<String, ComponentDescriptor> cdMap = md.getComponentDescriptors();

            // create a list containing all contexts of this model
            ArrayList<Object> contextList = new ArrayList<Object>();
            for (ComponentDescriptor cd : cdMap.values()) {
                if (cd instanceof ContextDescriptor) {
                    contextList.add(cd);
                }
            }

            // display the dialog
            newDSDlg.update(contextList);
            newDSDlg.setVisible(true);

            // Get the text field value
            if (newDSDlg.getResult() == DSDlg.RESULT_OK) {

                if (StringTools.isEmptyString(newDSDlg.getDsName())) {
                    GUIHelper.showErrorDlg(this, JAMS.i18n("Datastore_name_must_not_be_empty!"), JAMS.i18n("Error_creating_new_datastore"));
                    addItem();
                    return;
                }

                if (newDSDlg.getValue().length < 1) {
                    GUIHelper.showErrorDlg(this, JAMS.i18n("You_must_choose_a_context!"), JAMS.i18n("Error_creating_new_datastore"));
                    addItem();
                    return;
                }

                ContextDescriptor context = (ContextDescriptor) newDSDlg.getValue()[0];

                OutputDSDescriptor ods = new OutputDSDescriptor(context);
                ods.setName(newDSDlg.getDsName());
                ods.setEnabled(newDSDlg.isDsEnabled());

                md.addOutputDataStore(ods);

                setValue(md);
            }
        }

        @Override
        protected void editItem() {
            //get the current selection
            int selection = getTable().getSelectedRow();
            if (selection >= 0) {
                // edit this item
                OutputDSDescriptor ods = (OutputDSDescriptor) tableData.getElementAt(selection)[1];

                if (editDSDlg == null) {
                    editDSDlg = new DSDlg(OutputDSDlg.this, JAMS.i18n("Edit_datastore"), true, true, false);
                }

                editDSDlg.update(ods);
                editDSDlg.setVisible(true);

                if (editDSDlg.getResult() == DSDlg.RESULT_OK) {

                    if (StringTools.isEmptyString(editDSDlg.getDsName())) {
                        GUIHelper.showErrorDlg(this, JAMS.i18n("Datastore_name_must_not_be_empty!"), JAMS.i18n("Error_creating_new_datastore"));
                        editItem();
                        return;
                    }

                    ods.setName(editDSDlg.getDsName());
                    ods.setEnabled(editDSDlg.isDsEnabled());
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
                setValue(md);
            }
        }

        @Override
        protected void removeItem() {
            //get the current selection
            int selection = getTable().getSelectedRow();

            OutputDSDescriptor value = (OutputDSDescriptor) tableData.getValue().get(selection)[1];
            if (value != null) {

                md.removeOutputDataStore(value);
                setValue(md);

                //select the next item
                if (selection >= tableData.getValue().size()) {
                    selection = tableData.getValue().size() - 1;
                    getTable().setRowSelectionInterval(selection, selection);
                }

            }
        }
    }

    class DSDlg extends JDialog {

        private JTextField nameText;
        private JList objectList = new JList();
        public static final int RESULT_OK = 1, RESULT_CANCEL = 0;
        private int result = RESULT_CANCEL;
        private JCheckBox enableBox;

        public DSDlg(Dialog owner, String title, boolean showTextField, boolean showEnabledBox) {
            this(owner, title, showTextField, showEnabledBox, true);
        }

        public DSDlg(Dialog owner, String title, boolean showTextField, boolean showEnabledBox, boolean showList) {
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

            enableBox = new JCheckBox(JAMS.i18n("Enabled"));
            if (showEnabledBox) {
                GUIHelper.addGBComponent(contentPanel, mainLayout, enableBox, 1, 40, 1, 1, 0, 0);
            }
            enableBox.setSelected(true);

            nameText = new JTextField();
            nameText.setPreferredSize(new Dimension(200, 20));

            String listLabel;
            if (showTextField) {
                GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Name")), 1, 0, 1, 1, 0, 0);
                GUIHelper.addGBComponent(contentPanel, mainLayout, nameText, 1, 10, 1, 1, 0, 0);
                objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                listLabel = JAMS.i18n("Context");
            } else {
                objectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                listLabel = JAMS.i18n("Attribute");
            }

            if (showList) {
                JScrollPane listScroll = new JScrollPane(objectList);

                if (showTextField) {
                    listScroll.setPreferredSize(new Dimension(200, 200));
                } else {
                    listScroll.setPreferredSize(new Dimension(200, 400));
                }

                GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(listLabel), 1, 20, 1, 1, 0, 0);
                GUIHelper.addGBComponent(contentPanel, mainLayout, listScroll, 1, 30, 1, 1, 0, 0);
            }

            JButton okButton = new JButton(JAMS.i18n("OK"));
            okButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    result = RESULT_OK;
                    setVisible(false);
                }
            });

            JButton cancelButton = new JButton(JAMS.i18n("Cancel"));
            cancelButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    result = RESULT_CANCEL;
                    setVisible(false);
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);

            this.pack();
        }

        public void update(OutputDSDescriptor ods) {
            this.nameText.setText(ods.getName());
            this.nameText.requestFocus();
            this.enableBox.setSelected(ods.isEnabled());
        }

        @SuppressWarnings("unchecked")
        public void update(ArrayList<Object> values) {

            this.nameText.setText("");
            this.nameText.requestFocus();
            this.enableBox.setSelected(false);

            // sort the context list
            Collections.sort(values, new Comparator<Object>() {

                @Override
                public int compare(Object a1, Object a2) {
                    return a1.toString().compareTo(a2.toString());
                }
            });

            this.objectList.setModel(new DefaultComboBoxModel(values.toArray(new Object[values.size()])));

            pack();

        }

        /**
         * @return the result
         */
        public int getResult() {
            return result;
        }

        public String getDsName() {
            return nameText.getText();
        }

        public boolean isDsEnabled() {
            return enableBox.isSelected();
        }

        public Object[] getValue() {
            return objectList.getSelectedValues();
        }
    }
}
