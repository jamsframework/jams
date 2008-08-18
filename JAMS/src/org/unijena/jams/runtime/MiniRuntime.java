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
import org.unijena.jams.JAMS;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.JAMSTools;
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
    private int debugLevel = JAMS.VVERBOSE;
    private JAMSLog errorLog = new JAMSLog();
    private JAMSLog infoLog = new JAMSLog();

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
        return this.debugLevel;
    }

    public void setDebugLevel(int aDebugLevel) {
        this.debugLevel = aDebugLevel;
    }

    public void sendInfoMsg(String str) {
        infoLog.print("INFO: " + str + "\n");
    }
    
    public void sendErrorMsg(String str) {
        errorLog.print("ERROR: " + str + "\n");
    }    

    public void println(String s, int debugLevel) {
        if (debugLevel <= getDebugLevel()) {
            sendInfoMsg(s);
        }
    }

    public void println(String s) {
        sendInfoMsg(s);
    }

    public void handle(Exception ex) {
        handle(ex, null, false);
    }

    public void handle(Exception ex, String cName) {
        handle(ex, cName, false);
    }

    public void handle(Exception ex, boolean proceed) {
        handle(ex, null, proceed);
    }

    public void handle(Exception ex, String cName, boolean proceed) {

        String message = "";

        if (cName != null) {
            message += "Exception occured in component " + cName + "!\n";
        }

        message += ex.toString();
        if (getDebugLevel() > JAMS.STANDARD) {
            message += "\n" + JAMSTools.getStackTraceString(ex.getStackTrace());
        }
        sendErrorMsg(message);
        if (!proceed) {
            sendHalt();
        }
    }

    public HashMap<String, JAMSData> getDataHandles() {
        return new HashMap<String, JAMSData>();
    }

    public void sendHalt() {
        System.exit(0);
    }

    public void sendHalt(String str) {
        sendErrorMsg(str);
        sendHalt();
    }

    public int getRunState() {
        return -1;
    }

    public void addRunStateObserver(Observer o) {
    }

    public void addInfoLogObserver(Observer o) {
        infoLog.addObserver(o);
    }

    public void deleteInfoLogObserver(Observer o) {
        infoLog.deleteObserver(o);
    }
    
    public void deleteInfoLogObservers() {
        infoLog.deleteObservers();
    }

    public void addErrorLogObserver(Observer o) {
        errorLog.addObserver(o);
    }

    public void deleteErrorLogObserver(Observer o) {
        errorLog.deleteObserver(o);
    }
    
    public void deleteErrorLogObservers() {
        errorLog.deleteObservers();
    }

    public String getErrorLog() {
        return errorLog.getLogString();
    }

    public String getInfoLog() {
        return infoLog.getLogString();
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
