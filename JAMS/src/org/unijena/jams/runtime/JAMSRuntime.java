/*
 * JAMSRuntime.java
 * Created on 2. Juni 2006, 14:15
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

package org.unijena.jams.runtime;

import java.util.*;
import javax.swing.JFrame;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.data.JAMSData;
import org.unijena.jams.model.JAMSGUIComponent;
import org.w3c.dom.Document;

/**
 *
 * @author S. Kralisch
 */
public interface JAMSRuntime {
    
    
    public int getDebugLevel();
    public void setDebugLevel(int aDebugLevel);
    public void println(String s, int debugLevel);
    public void println(String s);
    public void handle(Exception ex);
    public HashMap<String, JAMSData> getDataHandles();
    public void handle(Exception ex, String cName);
    public void handle(Exception ex, boolean proceed);
    public void sendHalt();
    public void sendHalt(String str);
    public int getRunState();
    public void addRunStateObserver(Observer o);
    public void addInfoLogObserver(Observer o);
    public void addErrorLogObserver(Observer o);
    public String getErrorLog();
    public String getInfoLog();
    public void sendErrorMsg(String str);
    public void sendInfoMsg(String str);
    public void addGUIComponent(JAMSGUIComponent component);
    public void initGUI(String title, boolean ontop, int width, int height);
    public JFrame getFrame();
    public void runModel();
    public void loadModel(Document modelDocument, JAMSProperties properties);
    public ClassLoader getClassLoader();
    
}
