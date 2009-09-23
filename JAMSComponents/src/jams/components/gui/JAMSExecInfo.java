/*
 * JAMSExecInfo.java
 * Created on 1. Dezember 2005, 19:46
 *
 * This file is part of JAMSConstants
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

package jams.components.gui;

import jams.model.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import jams.JAMSConstants;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS execution info frame",
        author="Sven Kralisch",
        date="17. June 2006",
        description="This visual component creates a panel with progress bar and log information")
        public class JAMSExecInfo extends JAMSGUIComponent {
    
    
    private JProgressBar jamsProgressBar;
    private JPanel progressPanel;
    private Runnable updatePBar;
    private JScrollPane scrollPanel;
    private JTextArea logArea;
    private int counter;
    private JPanel panel;
    
    
    public JPanel getPanel() {
        createPanel();
        return panel;
    }
    
    public void init() {
        if (panel != null) {
            counter = 0;
            jamsProgressBar.setMaximum((int)this.getContext().getNumberOfIterations());
            jamsProgressBar.setValue(counter);
        } else {
            updatePBar = new Runnable() {
                public void run() {}
            };
        }
    }
    
    private void createPanel() {
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        
        jamsProgressBar = new JProgressBar();
        jamsProgressBar.setMinimum(0);
        jamsProgressBar.setString("Execution progress");
        jamsProgressBar.setStringPainted(true);
        jamsProgressBar.setIndeterminate(false);
        
        updatePBar = new Runnable() {
            public void run() {
                jamsProgressBar.setValue(++counter);
                jamsProgressBar.setString(Math.round(jamsProgressBar.getPercentComplete()*100) + "%");
            }
        };
        
        progressPanel.add(jamsProgressBar, BorderLayout.CENTER);
        
        progressPanel.setPreferredSize(new Dimension(0, 40));
        panel.add(progressPanel, BorderLayout.NORTH);
        
        scrollPanel = new JScrollPane();
        logArea = new JTextArea();
        logArea.setColumns(20);
        logArea.setRows(5);
        logArea.setLineWrap(false);
        logArea.setEditable(false);
        logArea.setFont(JAMSConstants.STANDARD_FONT);
        scrollPanel.setViewportView(logArea);
        
        panel.add(scrollPanel, BorderLayout.CENTER);
        
        logArea.append(this.getModel().getRuntime().getInfoLog());
        logArea.append(this.getModel().getRuntime().getErrorLog());
        
        this.getModel().getRuntime().addInfoLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                logArea.append(obj.toString());
                //logArea.setCaretPosition(logArea.getText().length());
            }
        });
        this.getModel().getRuntime().addErrorLogObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                logArea.append(obj.toString());
                //logArea.setCaretPosition(logArea.getText().length());
            }
        });
    }

    public void run() {
        SwingUtilities.invokeLater(updatePBar);
    }
    
    public void cleanup() {

    }
   
}
