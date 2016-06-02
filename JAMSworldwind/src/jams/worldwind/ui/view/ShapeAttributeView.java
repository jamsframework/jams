/*
 * ShapeAttributeView.java
 * Created on 01.06.2016, 16:53:01
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
package jams.worldwind.ui.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
public class ShapeAttributeView extends JDialog {
    
    private JTable t;

    public ShapeAttributeView(Frame owner, String title, String[][] data) {
        super(owner, title);
        init();
        setData(data);
    }
    
    public void setData(String[][] data) {
        t.setModel(new javax.swing.table.DefaultTableModel(
            data,
            new String [] {
                "Attribute", "Value"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
    }

    private void init() {
        this.setModal(false);
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.setLocationByPlatform(true);
        
        t = new JTable();
        
        JScrollPane sp = new JScrollPane();
        sp.setViewportView(t);
        getContentPane().add(sp, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
//        setPreferredSize(new Dimension(100, 150));
        this.pack();
    }
    
    public static void main(String[] args) {
        
        String[][] o = {{"0", "1"}, {"2", "3"}};
        ShapeAttributeView d = new ShapeAttributeView(null, "TEST", o);
        d.setVisible(true);
        String[][] o2 = {{"0", "1"}, {"4", "5"}};
        d.setData(o2);
        
    }

}
