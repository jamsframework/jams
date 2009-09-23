/*
 * ListInput.java
 * Created on 11. April 2006, 20:46
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package jams.gui.input;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import jams.gui.*;
import jams.JAMSConstants;

/**
 *
 * @author S. Kralisch
 */
public class ListInput extends JPanel {

    private static ImageIcon UP_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowup.png")).getImage().getScaledInstance(10, 5, Image.SCALE_SMOOTH));
    private static ImageIcon DOWN_ICON = new ImageIcon(new ImageIcon(ClassLoader.getSystemResource("resources/images/arrowdown.png")).getImage().getScaledInstance(10, 5, Image.SCALE_SMOOTH));
    static final int BUTTON_SIZE = 20;
    private static final Dimension BUTTON_DIMENSION = new Dimension(BUTTON_SIZE, BUTTON_SIZE);
    private JList listbox;
    private JButton addButton,  removeButton,  upButton,  downButton,  editButton;
    protected JScrollPane scrollPane;
    private JFileChooser jfc;
    protected ListData listData = new ListData();
    private boolean orderButtons = true;

    public ListInput() {
        this(true);
    }

    public ListInput(boolean orderButtons) {

        jfc = GUIHelper.getJFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        // create a panel to hold all other components
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);

        // create a new listbox control
        listbox = new JList(listData.getValue());
        listbox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        listbox.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    editItem();
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });

        // add the listbox to a scrolling pane
        scrollPane = new JScrollPane();
        scrollPane.getViewport().add(getListbox());
        add(scrollPane, BorderLayout.CENTER);

        // create a panel to hold all other components
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        buttonPanel.setLayout(new FlowLayout());
        add(buttonPanel, BorderLayout.EAST);

        // create some function buttons
        addButton = new JButton("+");
        addButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        addButton.setPreferredSize(BUTTON_DIMENSION);
        buttonPanel.add(addButton);
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                addItem();
            }
        });

        removeButton = new JButton("-");
        removeButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        removeButton.setPreferredSize(BUTTON_DIMENSION);
        buttonPanel.add(removeButton);
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                removeItem();
            }
        });

        editButton = new JButton("...");
        editButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        editButton.setPreferredSize(BUTTON_DIMENSION);
        editButton.setToolTipText(JAMSConstants.resources.getString("Edit"));
        buttonPanel.add(editButton);
        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                editItem();
            }
        });

        if (orderButtons) {
            upButton = new JButton();
            upButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
            upButton.setPreferredSize(BUTTON_DIMENSION);
            upButton.setToolTipText(JAMSConstants.resources.getString("Move_up"));
            upButton.setIcon(UP_ICON);
            upButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    moveUp();
                }
            });

            downButton = new JButton();
            downButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
            downButton.setPreferredSize(BUTTON_DIMENSION);
            downButton.setToolTipText(JAMSConstants.resources.getString("Move_down"));
            downButton.setIcon(DOWN_ICON);
            downButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    moveDown();
                }
            });

            buttonPanel.add(upButton);
            buttonPanel.add(downButton);
        }
    }

    private void moveUp() {
        int index = listbox.getSelectedIndex();
        if (index > 0) {
            String tmp = listData.getElementAt(index - 1);
            listData.setElementAt(index - 1, listData.getElementAt(index));
            listData.setElementAt(index, tmp);
            listbox.setSelectedIndex(index - 1);
            listbox.updateUI();
        }
    }

    private void moveDown() {
        int index = listbox.getSelectedIndex();
        if (index < listData.getValue().size() - 1) {
            String tmp = listData.getElementAt(index + 1);
            listData.setElementAt(index + 1, listData.getElementAt(index));
            listData.setElementAt(index, tmp);
            listbox.setSelectedIndex(index + 1);
            listbox.updateUI();
        }
    }

    public void addListDataObserver(Observer obs) {
        listData.addObserver(obs);
    }

    public void setListData(Vector<String> listData) {
        this.listData.setValue(listData);
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    public Vector<String> getListData() {
        return listData.getValue();
    }

    public String getSelectedString() {
        int selection = getListbox().getSelectedIndex();
        if (selection >= 0) {
            return listData.getValue().elementAt(selection);
        } else {
            return null;
        }
    }

    public JList getListbox() {
        return listbox;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getListbox().setEnabled(enabled);
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        editButton.setEnabled(enabled);
        if (upButton != null) {
            upButton.setEnabled(enabled);
        }
        if (downButton != null) {
            downButton.setEnabled(enabled);
        }
    }

    protected void addItem() {
        // Get the text field value
        String stringValue = GUIHelper.showInputDlg(ListInput.this, null, JAMSConstants.resources.getString("New_value"), null);

        // add this item to the list and refresh
        if (stringValue != null && !listData.getValue().contains(stringValue)) {
            listData.addElement(stringValue);
            scrollPane.revalidate();
            scrollPane.repaint();
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

    protected void editItem() {
        //get the current selection
        int selection = getListbox().getSelectedIndex();
        if (selection >= 0) {
            // edit this item
            String value = listData.getElementAt(selection);
            value = GUIHelper.showInputDlg(ListInput.this, null, JAMSConstants.resources.getString("New_value"), value);
            if (value != null) {
                listData.setElementAt(selection, value);
                scrollPane.revalidate();
                scrollPane.repaint();
            }
        }
    }

    protected class ListData extends Observable {

        private Vector<String> listData = new Vector<String>();

        public void addElement(String s) {
            listData.addElement(s);
            getListbox().setListData(listData);
            getListbox().setSelectedValue(s, true);
            this.setChanged();
            this.notifyObservers();
        }

        public void removeElementAt(int selection) {
            listData.removeElementAt(selection);
            getListbox().setListData(listData);
            this.setChanged();
            this.notifyObservers();
        }

        public String getElementAt(int selection) {
            return listData.get(selection);
        }

        public void setElementAt(int selection, String s) {
            listData.set(selection, s);
        }

        public Vector<String> getValue() {
            return listData;
        }

        public void setValue(Vector<String> listData) {
            this.listData = listData;
            getListbox().setListData(listData);
            this.setChanged();
            this.notifyObservers();
        }
    };
}
