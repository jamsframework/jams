/*
 * MiniRuntime.java
 * Created on 7. November 2007, 14:51
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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

package org.unijena.jams.runtime;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.data.JAMSData;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSModel;
import org.w3c.dom.Document;

/**
 *
 * @author Sven Kralisch
 */
public class MiniRuntime implements JAMSRuntime {
    
    private JFrame frame;
    
    /** Creates a new instance of MiniRuntime */
    public MiniRuntime(JAMSComponent component) {
        JAMSModel model = new JAMSModel(this);
        component.setModel(model);
        component.setContext(model);
        if (component instanceof JAMSGUIComponent) {
            frame = new JFrame();
            frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            frame.setTitle("MiniRuntime");
            frame.setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
            frame.setPreferredSize(new java.awt.Dimension(800, 600));
            frame.getContentPane().add(((JAMSGUIComponent) component).getPanel(), BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        }
        
    }
    
    public int getDebugLevel() {
        return -1;
    }
    
    public void setDebugLevel(int aDebugLevel) {
    }
    
    public void println(String s, int debugLevel) {
        println(s);
    }
    
    public void println(String s) {
    }
    
    public void handle(Exception ex) {
        ex.printStackTrace();
    }
    
    public HashMap<String, JAMSData> getDataHandles() {
        return new HashMap<String, JAMSData>();
    }
    
    public void handle(Exception ex, String cName) {
        handle(ex);
    }
    
    public void handle(Exception ex, boolean proceed) {
        handle(ex);
    }
    
    public void sendHalt() {
        System.exit(0);
    }
    
    public void sendHalt(String str) {
        System.out.print(str);
        System.exit(0);
    }
    
    public int getRunState() {
        return -1;
    }
    
    public void addRunStateObserver(Observer o) {
    }
    
    public void addInfoLogObserver(Observer o) {
    }
    
    public void addErrorLogObserver(Observer o) {
    }
    
    public String getErrorLog() {
        return "";
    }
    
    public String getInfoLog() {
        return "";
    }
    
    public void sendErrorMsg(String str) {
        println(str);
    }
    
    public void sendInfoMsg(String str) {
        println(str);
    }
    
    public void addGUIComponent(JAMSGUIComponent component) {
    }
    
    public void initGUI(String title, boolean ontop, int width, int height) {
    }
    
    public JFrame getFrame() {
        return null;
    }
    
    public void runModel() {
    }
    
    public void loadModel(Document modelDocument, JAMSProperties properties) {
    }
    
    public ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
    
}
