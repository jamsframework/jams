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
package jams.runtime;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import jams.JAMS;
import jams.JAMSProperties;
import jams.JAMSTools;
import jams.data.JAMSData;
import jams.model.JAMSComponent;
import jams.model.JAMSGUIComponent;
import jams.model.JAMSModel;
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

    /** 
     * Creates a new instance of MiniRuntime. This Runtime can be used to
     * test a single component without model, e.g. for debugging purposes.
     * @param component The component to be tested
     */
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

    @Override
    public String[] getLibs(){
        return new String[0];
    }
    
    @Override
    public int getDebugLevel() {
        return this.debugLevel;
    }

    @Override
    public void setDebugLevel(int aDebugLevel) {
        this.debugLevel = aDebugLevel;
    }

    @Override
    public void sendInfoMsg(String str) {
        infoLog.print("INFO: " + str + "\n");
    }

    @Override
    public void sendErrorMsg(String str) {
        errorLog.print("ERROR: " + str + "\n");
    }

    @Override
    public void println(String s, int debugLevel) {
        if (debugLevel <= getDebugLevel()) {
            sendInfoMsg(s);
        }
    }

    @Override
    public void println(String s) {
        sendInfoMsg(s);
    }

    @Override
    public void handle(Exception ex) {
        handle(ex, null, false);
    }

    @Override
    public void handle(Exception ex, String cName) {
        handle(ex, cName, false);
    }

    @Override
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

    @Override
    public void sendHalt() {
        System.exit(0);
    }

    @Override
    public void sendHalt(String str) {
        sendErrorMsg(str);
        sendHalt();
    }

    @Override
    public int getRunState() {
        return -1;
    }

    @Override
    public void addRunStateObserver(Observer o) {
    }

    @Override
    public void addInfoLogObserver(Observer o) {
        infoLog.addObserver(o);
    }

    @Override
    public void deleteInfoLogObserver(Observer o) {
        infoLog.deleteObserver(o);
    }

    @Override
    public void deleteInfoLogObservers() {
        infoLog.deleteObservers();
    }

    @Override
    public void addErrorLogObserver(Observer o) {
        errorLog.addObserver(o);
    }

    @Override
    public void deleteErrorLogObserver(Observer o) {
        errorLog.deleteObserver(o);
    }

    @Override
    public void deleteErrorLogObservers() {
        errorLog.deleteObservers();
    }

    @Override
    public String getErrorLog() {
        return errorLog.getLogString();
    }

    @Override
    public String getInfoLog() {
        return infoLog.getLogString();
    }

    @Override
    public void addGUIComponent(JAMSGUIComponent component) {
    }

    @Override
    public void initGUI(String title, boolean ontop, int width, int height) {
    }

    @Override
    public JFrame getFrame() {
        return null;
    }

    @Override
    public void runModel() {
    }

    @Override
    public void loadModel(Document modelDocument, JAMSProperties properties) {
    }

    @Override
    public ClassLoader getClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    @Override
    public void saveModelParameter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
