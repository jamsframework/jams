/*
 * SearchDlg.java
 * Created on 10. November 2008, 16:32
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.juice.gui;

import jams.data.JAMSString;
import jams.gui.tools.GUIHelper;
import jams.gui.input.InputComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.Enumeration;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;
import jamsui.juice.gui.tree.JAMSNode;
import jamsui.juice.gui.tree.JAMSTree;
import jamsui.juice.gui.tree.LibTree;
import jamsui.juice.gui.tree.ModelTree;
import jamsui.juice.JUICE;
import jams.gui.input.InputComponentFactory;
import jams.JAMS;
import jams.meta.ComponentDescriptor;
import jams.meta.ComponentField;
import jams.meta.ContextDescriptor;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class SearchDlg extends JDialog {

    private static final int TEXTFIELD_WIDTH = 35;
    private static final int FOUND_IN_CLASS = 1,  FOUND_IN_INSTANCE = 2,  FOUND_IN_CONTEXT_ATTRIBS = 3,  FOUND_IN_COMPONENT_ATTRIBS = 4,  FOUND_IN_COMPONENT_VALUES = 5,  FOUND_IN_COMPONENT_METADATA = 6;
    private JAMSTree tree;
    private Enumeration treeEnum;
    private JCheckBox inClassName,  inInstanceName,  inContextAttribs,  inComponentAttribs,  inComponentValues,  inComponentMetadata,  caseSensitive,  wholeString;
    private InputComponent searchText;
    private JRadioButton repo,  model;
    private boolean modelSelect = true,  foundResult = false;

    public SearchDlg(Frame owner) {
        super(owner);
        setLocationRelativeTo(owner);
        setModal(false);
        setResizable(false);
        setLocationByPlatform(true);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        GridBagLayout mainLayout = new GridBagLayout();
        contentPanel.setLayout(mainLayout);

        inClassName = new JCheckBox(JAMS.i18n("inClassName"), true);
        inInstanceName = new JCheckBox(JAMS.i18n("inInstanceName"), true);
        inContextAttribs = new JCheckBox(JAMS.i18n("inContextAttribs"), true);
        inComponentAttribs = new JCheckBox(JAMS.i18n("inComponentAttribs"), true);
        inComponentValues = new JCheckBox(JAMS.i18n("inComponentValues"), true);
        inComponentMetadata = new JCheckBox(JAMS.i18n("inComponentMetadata"), true);

        caseSensitive = new JCheckBox(JAMS.i18n("caseSensitiveSearch"), false);
        wholeString = new JCheckBox(JAMS.i18n("wholeStringSearch"), false);

        searchText = InputComponentFactory.createInputComponent(JAMSString.class);
        searchText.setLength(TEXTFIELD_WIDTH);

        repo = new JRadioButton(JAMS.i18n("Search_in_Repo"));
        model = new JRadioButton(JAMS.i18n("Search_in_Model"));
        model.setSelected(true);

        repo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (repo.isSelected() && modelSelect) {
                    modelSelect = false;
                    setTree(JUICE.getLibTree());
                }
            }
        });
        model.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.isSelected() && !modelSelect) {
                    modelSelect = true;
                    setTree(JUICE.getJuiceFrame().getCurrentView().getTree());
                }
            }
        });

        ButtonGroup group = new ButtonGroup();
        group.add(repo);
        group.add(model);

        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Search_text")), 1, 0, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, searchText.getComponent(), 1, 1, 2, 1, 0, 0);

        GUIHelper.addGBComponent(contentPanel, mainLayout, model, 1, 2, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, repo, 2, 2, 1, 1, 0, 0);

        GUIHelper.addGBComponent(contentPanel, mainLayout, caseSensitive, 1, 5, 1, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, wholeString, 2, 5, 1, 1, 0, 0);

        GUIHelper.addGBComponent(contentPanel, mainLayout, new JLabel(JAMS.i18n("Where_to_search")), 1, 10, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, inClassName, 1, 11, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, inComponentAttribs, 1, 12, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, inComponentMetadata, 1, 14, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, inInstanceName, 1, 16, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, inComponentValues, 1, 18, 2, 1, 0, 0);
        GUIHelper.addGBComponent(contentPanel, mainLayout, inContextAttribs, 1, 20, 2, 1, 0, 0);

        JButton findButton = new JButton(JAMS.i18n("Find"));
        findButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                processFind();
            }
        });

        JButton resetButton = new JButton(JAMS.i18n("ResetSearch"));
        resetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                processFind();
            }
        });

        JButton closeButton = new JButton(JAMS.i18n("Close"));
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                processClose();
            }
        });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(findButton);
            buttonPanel.add(resetButton);
            buttonPanel.add(closeButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            this.pack();
            getRootPane().setDefaultButton(findButton);
    }

    private void reset() {
        JAMSNode rootNode = (JAMSNode) tree.getModel().getRoot();
        treeEnum = rootNode.preorderEnumeration();
        foundResult = false;
    }

    public void setTree(JAMSTree tree) {
        this.tree = tree;

        if (tree instanceof LibTree) {
            this.setTitle(JAMS.i18n("Search_in_Repo"));
            inInstanceName.setEnabled(false);
            inComponentValues.setEnabled(false);
            inContextAttribs.setEnabled(false);
        } else if (tree instanceof ModelTree) {
            this.setTitle(JAMS.i18n("Search_in_Model"));
            inInstanceName.setEnabled(true);
            inComponentValues.setEnabled(true);
            inContextAttribs.setEnabled(true);
        }

        reset();
    }

    private void processFind() {

        while (treeEnum.hasMoreElements()) {

            JAMSNode node = (JAMSNode) treeEnum.nextElement();
            if ((node.getType() == JAMSNode.COMPONENT_TYPE) || (node.getType() == JAMSNode.CONTEXT_TYPE) || (node.getType() == JAMSNode.MODEL_TYPE)) {

                ComponentDescriptor cd = (ComponentDescriptor) node.getUserObject();

                if (find(cd, searchText.getValue(), caseSensitive.isSelected(), wholeString.isSelected()) != -1) {
                    TreePath resultPath = new TreePath(node.getPath());
                    tree.scrollPathToVisible(resultPath);
                    tree.setSelectionPath(resultPath);
                    foundResult = true;
                    return;
                }
            }
        }

        // check if we have found anything
        if (!foundResult) {
            GUIHelper.showInfoDlg(JUICE.getJuiceFrame(), JAMS.i18n("No_searchresults_txt"), JAMS.i18n("Search_finished"));
            reset();
            return;
        }

        // we've found all results, ask what to do next
        if (GUIHelper.showYesNoDlg(JUICE.getJuiceFrame(), JAMS.i18n("No_further_searchresults_txt"), JAMS.i18n("Search_finished")) == JOptionPane.YES_OPTION) {
            reset();
            processFind();
        }
    }

    private int find(ComponentDescriptor cd, String needle, boolean caseSensitive, boolean wholeString) {

        if (inClassName.isSelected()) {
            if (contains(cd.getClazz().getName(), needle, caseSensitive, wholeString)) {
                return FOUND_IN_CLASS;
            }
        }

        if (inInstanceName.isSelected()) {
            if (contains(cd.getInstanceName(), needle, caseSensitive, wholeString)) {
                return FOUND_IN_INSTANCE;
            }
        }

        if (inComponentAttribs.isSelected()) {
            for (ComponentField ca : cd.getComponentFields().values()) {

                // check for component attribute name
                if (contains(ca.getName(), needle, caseSensitive, wholeString)) {
                    return FOUND_IN_COMPONENT_ATTRIBS;
                }
            }
        }

        if (inComponentValues.isSelected()) {
            for (ComponentField ca : cd.getComponentFields().values()) {

                // check for component attribute values
                if (ca.getValue() != null) {
                    if (contains(ca.getValue().toString(), needle, caseSensitive, wholeString)) {
                        return FOUND_IN_COMPONENT_VALUES;
                    }
                }

                // check for context attribute name
                if (ca.getAttribute() != null) {
                    if (contains(ca.getAttribute(), needle, caseSensitive, wholeString)) {
                        return FOUND_IN_COMPONENT_VALUES;
                    }
                }
            }
        }

        if (inContextAttribs.isSelected() && (cd instanceof  ContextDescriptor)) {
            for (String hay : ((ContextDescriptor) cd).getStaticAttributes().keySet()) {
                if (contains(hay, needle, caseSensitive, wholeString)) {
                    return FOUND_IN_CONTEXT_ATTRIBS;
                }
            }
        }

        if (inComponentMetadata.isSelected()) {
            Class<?> clazz = cd.getClazz();
            JAMSComponentDescription jcd = (JAMSComponentDescription) clazz.getAnnotation(JAMSComponentDescription.class);
            if (jcd != null) {
                String[] hayArray = {jcd.author(), jcd.title(), jcd.description()};
                for (String hay : hayArray) {
                    if (contains(hay, needle, caseSensitive, wholeString)) {
                        return FOUND_IN_COMPONENT_METADATA;
                    }
                }
            }
            for (Field field : clazz.getFields()) {
                JAMSVarDescription jvd = (JAMSVarDescription) field.getAnnotation(JAMSVarDescription.class);
                if (jvd != null) {
                    if (contains(jvd.description(), needle, caseSensitive, wholeString)) {
                        return FOUND_IN_COMPONENT_METADATA;
                    }
                }
            }
        }

        return -1;
    }

    private boolean contains(String hay, String needle, boolean caseSensitive, boolean wholeString) {
        if (!caseSensitive) {
            hay = hay.toLowerCase();
            needle = needle.toLowerCase();
        }
        if (wholeString) {
            return hay.equals(needle);
        }
        if (hay.indexOf(needle) != -1) {
            return true;
        } else {
            return false;
        }
    }

    private void processClose() {
        setVisible(false);
    }

    @Override
    public void setVisible(boolean b) {

        // check if we have a model opened
        if (JUICE.getJuiceFrame().getCurrentView() == null) {
            repo.setSelected(true);
            model.setEnabled(false);
            modelSelect = false;
        } else {
            model.setEnabled(true);
        }

        // make sure the correct tree is selected
        if (modelSelect) {
            setTree(JUICE.getJuiceFrame().getCurrentView().getTree());
        } else {
            setTree(JUICE.getLibTree());
        }

        super.setVisible(b);
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane pane = super.createRootPane();
        Action cancelAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                processClose();
            }
        };
        InputMap inputMap = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "ESCAPE");
        pane.getActionMap().put("ESCAPE", cancelAction);

        return pane;
    }
}
