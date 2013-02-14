/*
 * JAMSExecInfo.java
 * Created on 1. Dezember 2005, 19:46
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena
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

package jams.components.gui;

import jams.model.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import jams.JAMS;
import jams.data.Attribute;
import java.io.Serializable;

/**
 *
 * @author S. Kralisch
 */

@JAMSComponentDescription(
        title="JAMS execution info frame",
        author="Sven Kralisch",
        date="17. June 2006",
        description="This visual component creates a panel with progress bar and log information")
        public class JAMSExecInfo extends JAMSGUIComponent implements Serializable {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Number of invocations of this component"
            )
            public Attribute.Integer counter;
    
    private JProgressBar jamsProgressBar;
    private JPanel progressPanel;
    transient private Runnable updatePBar;
    transient private Observer infoLogObserver;
    transient private Observer errorLogObserver;
    private JScrollPane scrollPanel;
    private JTextArea logArea;
    private int c = 0;
    
    private JPanel panel;
    
    
    public JPanel getPanel() {
        if (this.panel==null)
            createPanel();
        return panel;
    }
    
    @Override
    public void init() {           
        if (panel != null) {
            jamsProgressBar.setMaximum((int)this.getContext().getNumberOfIterations());
        } else {
            updatePBar = new Runnable() {
                public void run() {}
            };
        }
    }

    private void createObserver(){
        if (infoLogObserver==null){
            infoLogObserver = new Observer() {
            public void update(Observable obs, Object obj) {
                logArea.append(obj.toString());
                //logArea.setCaretPosition(logArea.getText().length());
            }
            };}
        if (errorLogObserver==null){
            errorLogObserver = new Observer() {
            public void update(Observable obs, Object obj) {
                logArea.append(obj.toString());
                //logArea.setCaretPosition(logArea.getText().length());
            }
        };}
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
                jamsProgressBar.setValue(++c);
                counter.setValue(c);
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
        logArea.setFont(JAMS.STANDARD_FONT);
        scrollPanel.setViewportView(logArea);
        
        panel.add(scrollPanel, BorderLayout.CENTER);
        
        logArea.append(this.getModel().getRuntime().getInfoLog());
        logArea.append(this.getModel().getRuntime().getErrorLog());

        createObserver();
        this.getModel().getRuntime().addInfoLogObserver(infoLogObserver);
        this.getModel().getRuntime().addErrorLogObserver(errorLogObserver);
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(updatePBar);
    }
    
    @Override
    public void cleanup() {
    }      
    
    @Override
    public void restore(){
        updatePBar = new Runnable() {
            public void run() {                
                if (jamsProgressBar != null){
                    int c = counter.getValue();
                    jamsProgressBar.setValue(++c);
                    counter.setValue(c);
                    jamsProgressBar.setString(Math.round(jamsProgressBar.getPercentComplete()*100) + "%");
                }
            }            
        };
        if (this.logArea!=null){
            createObserver();
            this.getModel().getRuntime().addInfoLogObserver(infoLogObserver);
            this.getModel().getRuntime().addErrorLogObserver(errorLogObserver);
        }
    }    
}
