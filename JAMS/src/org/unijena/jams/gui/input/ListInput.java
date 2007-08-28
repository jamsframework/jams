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

package org.unijena.jams.gui.input;


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import org.unijena.jams.JAMSFileFilter;
import org.unijena.jams.gui.*;

/**
 *
 * @author S. Kralisch
 */
public class ListInput extends JPanel {
    
    private static final long serialVersionUID = 3205284532135463961L;
    
    static final int BUTTON_SIZE = 21;
    public static final int FILE_LIST = 0;
    public static final int STRING_LIST = 1;
    
    private JList listbox;
    
    private JButton addButton;
    private JButton removeButton;
    private JScrollPane scrollPane;
    private JFileChooser jfc;
    private int type = FILE_LIST;
    private ListData listData = new ListData();
    
    public ListInput(int type) {
        this();
        this.type = type;
    }
    
    // constructor of main frame
    public ListInput() {
        
        jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
        // create a panel to hold all other components
        BorderLayout layout = new BorderLayout();
        layout.setHgap(5);
        setLayout(layout);
        
        // create a new listbox control
        listbox = new JList( listData.getValue() );
        getListbox().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // add the listbox to a scrolling pane
        scrollPane = new JScrollPane();
        scrollPane.getViewport().add(getListbox());
        add( scrollPane, BorderLayout.CENTER );
        
        // create a panel to hold all other components
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
        buttonPanel.setLayout( new FlowLayout() );
        add( buttonPanel, BorderLayout.EAST );
        
        // create some function buttons
        addButton = new JButton("+");
        addButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        addButton.setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
        buttonPanel.add(addButton);
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent event ) {
                // Get the text field value
                String stringValue = null;
                
                if (type == FILE_LIST) {
                    Object o = getListbox().getSelectedValue();
                    if (o != null) {
                        File file = new File((String) o);
                        
                        jfc.setSelectedFile(file);
                        jfc.setFileFilter(JAMSFileFilter.getJarFilter());
                    }
                    
                    int result = jfc.showOpenDialog(ListInput.this);
                    
                    if (result == JFileChooser.APPROVE_OPTION) {
                        stringValue = jfc.getSelectedFile().getAbsolutePath();
                    }
                } else {
                    stringValue = LHelper.showInputDlg(ListInput.this, null, null);
                }
                // add this item to the list and refresh
                if (stringValue != null && !listData.getValue().contains(stringValue)) {
                    listData.addElement(stringValue);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                }
                
            }
        });
        
        removeButton = new JButton("-");
        removeButton.setMargin(new java.awt.Insets(0, 1, 1, 0));
        removeButton.setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
        buttonPanel.add(removeButton);
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                //get the current selection
                int selection = getListbox().getSelectedIndex();
                if (selection >= 0) {
                    // Add this item to the list and refresh
                    listData.removeElementAt(selection);
                    scrollPane.revalidate();
                    scrollPane.repaint();
                    
                    //select the next item
                    if(selection >= listData.getValue().size())
                        selection = listData.getValue().size() - 1;
                    getListbox().setSelectedIndex(selection);
                }
            }
        });
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
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getListbox().setEnabled(enabled);
        addButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }
    
    private class ListData extends Observable {
        private Vector<String> listData = new Vector<String>();;
        
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