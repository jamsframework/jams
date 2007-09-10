/*
 * CTSViewer.java
 *
 * Created on 5. September 2007, 14:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.gui.spreadsheet;

import jams.components.gui.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;
import java.awt.event.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import java.io.*;

import java.util.Iterator;

import org.unijena.jams.JAMS;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;
/**
 *
 * @author Robert Riedel
 */
public class CTSViewer extends JDialog{
    
    Frame parent;
    JTabbedPane ctsTabPane;
    JPanel panel;
    int index = 0;
    Vector<CTSConfigurator> ctsvector;
    
    /** Creates a new instance of CTSViewer */
    public CTSViewer(Frame parent) {
        super(parent, "JAMS CTS Viewer");
        this.parent = parent;
        showCTSViewer();
    }
    
    private void showCTSViewer(){
        
        setLayout(new FlowLayout());
        Point parentloc = parent.getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);
        
        ctsTabPane = new JTabbedPane();
        add(ctsTabPane);
    }
    
    public void addGraph(JTable table){
        CTSConfigurator ctsConf = new CTSConfigurator(table);
        //ctsConf.setIndex(index);
        ctsConf.timePlot();
        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(ctsConf.getPanel());
        

        //ctsConf.timePlot();
        //panel.add(ctsConf.getCTSPlot(), BorderLayout.CENTER); 
        //panel.add(ctsConf.timePlot(), BorderLayout.CENTER);
        ctsTabPane.addTab("title", panel);
        
        
        
        pack();
        setVisible(true);
        

        /* Parameter festlegen */

    }
    
    /**** EVENT HANDLING ************/
    

    
        ActionListener deletebuttonclick = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
                
            } 
    };
    
    
    
}
