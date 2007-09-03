/*
 * StandardRuntime.java
 * Created on 2. Juni 2006, 13:24
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import org.omg.SendingContext.RunTime;
import org.unijena.jams.JAMS;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.ModelConfig;
import org.unijena.jams.ModelPreprocessor;
import org.unijena.jams.data.JAMSData;
import org.unijena.jams.io.JAMSModelLoader;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSModel;
import org.w3c.dom.Document;

/**
 *
 * @author S. Kralisch
 */
public class StandardRuntime implements JAMSRuntime {
    
    private HashMap <String, JAMSData> dataHandles = new HashMap <String, JAMSData>();
    private JAMSLog errorLog = new JAMSLog();
    private JAMSLog infoLog = new JAMSLog();
    private int debugLevel = JAMS.STANDARD;
    private RunState runState = new RunState();
    private ArrayList <JAMSGUIComponent> guiComponents = new ArrayList <JAMSGUIComponent>();
    private JButton stopButton, closeButton;
    private JFrame frame;
    private JAMSModel model;
    private PrintStream infoStream, errorStream;
    private boolean guiEnabled = false;
    private ClassLoader classLoader;
    
    public void loadModel(Document modelDocument, JAMSProperties properties) {
        
        long start = System.currentTimeMillis();
        
        // set the debug (i.e. output verbosity) level
        int debugLevel = Integer.parseInt(properties.getProperty("debug", "1"));
        this.setDebugLevel(debugLevel);
        
        // add log observers for output to system.out if needed
        int verbose = Integer.parseInt(properties.getProperty("verbose", "1"));
        if (verbose != 0) {
            
            // add info and error log output
            this.addInfoLogObserver(new Observer() {
                public void update(Observable obs, Object obj) {
                    System.out.print(obj);
                }
            });
            this.addErrorLogObserver(new Observer() {
                public void update(Observable obs, Object obj) {
                    System.out.print(obj);
                }
            });
        }
        
        try {
            if ((properties.getProperty("infolog") != null) && (!properties.getProperty("infolog").equals(""))) {
                infoStream = new PrintStream(properties.getProperty("infolog"));
            }
        } catch (FileNotFoundException fnfe) {
            this.handle(fnfe);
        }
        
        try {
            if ((properties.getProperty("errorlog") != null)  && (!properties.getProperty("errorlog").equals(""))) {
                errorStream = new PrintStream(properties.getProperty("errorlog"));
            }
        } catch (FileNotFoundException fnfe) {
            this.handle(fnfe);
        }
        
        
        // get libraries specified in properties
        String[] libs = JAMSTools.toArray(properties.getProperty("libs", ""), ";");
        
        // load the libraries and create the class loader
        classLoader = JAMSClassLoader.createClassLoader(libs, this);
        
        /*
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        try {
            ClassManager.addLibs(libs, this);
        } catch (IOException ioe) {
            this.handle(ioe);
        }
         */
        
        // create model config object from config document
        ModelConfig config = new ModelConfig(modelDocument);
        
        // create preprocessor
        ModelPreprocessor preProc = new ModelPreprocessor(modelDocument, config, this);
        
        // run preprocessor
        preProc.process();
        
        // load the model
        JAMSModelLoader modelLoader = new JAMSModelLoader(modelDocument, null, this);
        this.model = modelLoader.getModel();
        
        // create GUI if needed
        int wEnable = Integer.parseInt(properties.getProperty("windowenable", "1"));
        if (wEnable != 0) {
            int width = Integer.parseInt(properties.getProperty("windowwidth", "600"));
            int height = Integer.parseInt(properties.getProperty("windowheight", "400"));
            int ontop = Integer.parseInt(properties.getProperty("windowontop", "0"));
            this.initGUI(model.getName(), (ontop==1?true:false), width, height);
            this.guiEnabled = true;
        }
        
        long end = System.currentTimeMillis();
        this.println("JAMS model setup time: " + (end-start) + " ms", JAMS.STANDARD);
    }
    
    
    public void runModel() {
        
        //check if runstate is on "run"
        if (this.getRunState() != JAMS.RUNSTATE_RUN) {
            return;
        }
        
        if (guiEnabled) {
            frame.setVisible(true);
        }
        
        long start = System.currentTimeMillis();
        
        if (this.getRunState() == JAMS.RUNSTATE_RUN)
            model.init();
        if (this.getRunState() == JAMS.RUNSTATE_RUN)
            model.run();
        if (this.getRunState() == JAMS.RUNSTATE_RUN)
            model.cleanup();
        
        long end = System.currentTimeMillis();
        this.println("JAMS model execution time: " + (end-start) + " ms", JAMS.STANDARD);
        
        this.sendHalt();
        
        if (infoStream != null) {
            infoStream.print(this.getInfoLog());
            infoStream.close();
        }
        
        if (errorStream != null) {
            errorStream.print(this.getErrorLog());
            errorStream.close();
        }
        
        model = null;
        Runtime.getRuntime().gc();
    }
    
    public void initGUI(String title, boolean ontop, int width, int height) {
        
        if (guiComponents.size() == 0) {
            return;
        }
        
        frame = new JFrame();
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frame.setTitle(title);
        frame.setName(title);
        frame.setAlwaysOnTop(ontop);
        frame.setIconImage(new ImageIcon(ClassLoader.getSystemResource("resources/images/JAMSicon16.png")).getImage());
        frame.setPreferredSize(new java.awt.Dimension(width, height));
        
        ListIterator<JAMSGUIComponent> i = guiComponents.listIterator();
        if (guiComponents.size() > 1) {
            JTabbedPane tabbedPane = new JTabbedPane();
            
            while (i.hasNext()) {
                JAMSGUIComponent comp = i.next();
                tabbedPane.addTab(comp.getInstanceName(), comp.getPanel());
            }
            frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
            
        } else {
            
            while (i.hasNext()) {
                frame.getContentPane().add(i.next().getPanel(), BorderLayout.CENTER);
            }
            
        }
        
        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(0, 50));
        
        stopButton = new JButton();
        //stopButton.setText("Stop");
        stopButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelStop.png")));
        stopButton.setEnabled(true);
        //stopButton.setPreferredSize(new Dimension(100, 30));
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                StandardRuntime.this.sendHalt();
                stopButton.setEnabled(false);
            }
        });
        toolBar.add(stopButton);
        
        closeButton = new JButton();
        //closeButton.setText("Close");
        closeButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Shutdown.png")));
        closeButton.setEnabled(false);
        //closeButton.setPreferredSize(new Dimension(100, 30));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                StandardRuntime.this.sendHalt();
                frame.dispose();
                Runtime.getRuntime().gc();
            }
        });
        toolBar.add(closeButton);
        frame.add(toolBar, BorderLayout.NORTH);
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                StandardRuntime.this.sendHalt();
                frame.dispose();
                Runtime.getRuntime().gc();
            }
        });
        
        this.addRunStateObserver(new Observer() {
            public void update(Observable obs, Object obj) {
                stopButton.setEnabled(false);
                if (StandardRuntime.this.getRunState() == JAMS.RUNSTATE_STOP)
                    closeButton.setEnabled(true);
            }
        });
        
        frame.pack();
        frame.setLocation((int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()-width)/2, (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()-height)/2);
        
    }
    
    public HashMap<String, JAMSData> getDataHandles() {
        return dataHandles;
    }
    
    public void setDataHandles(HashMap<String, JAMSData> dataHandles) {
        this.dataHandles = dataHandles;
    }
    
    public void println(String s, int debugLevel) {
        if (debugLevel <= getDebugLevel())
            sendInfoMsg(s);
    }
    
    public void println(String s) {
        sendInfoMsg(s);
    }
    
    public int getDebugLevel() {
        return debugLevel;
    }
    
    public void setDebugLevel(int aDebugLevel) {
        debugLevel = aDebugLevel;
    }
    
    public void handle(Exception ex) {
        handle(ex, false);
    }
    
    public void handle(Exception ex, String cName) {
        sendErrorMsg("Exception occured in component " + cName);
        handle(ex, false);
    }
    
    public void handle(Exception ex, boolean proceed) {
        if (getDebugLevel() <= JAMS.STANDARD) {
            sendErrorMsg(ex.toString());
        } else {
            sendErrorMsg(ex.toString());
            sendErrorMsg(JAMSTools.getStackTraceString(ex.getStackTrace()));
        }
        if (!proceed)
            sendHalt();
    }
    
    public void sendHalt() {
        runState.setState(JAMS.RUNSTATE_STOP);
    }
    
    public void sendHalt(String str) {
        sendErrorMsg(str);
        sendHalt();
    }
    
    public void sendErrorMsg(String str) {
        errorLog.print("ERROR: " + str + "\n");
    }
    
    public void sendInfoMsg(String str) {
        infoLog.print("INFO: " + str + "\n");
    }
    
    public void addRunStateObserver(Observer o) {
        runState.addObserver(o);
    }
    
    public int getRunState() {
        return runState.getState();
    }
    
    public void addInfoLogObserver(Observer o) {
        infoLog.addObserver(o);
    }
    
    public void addErrorLogObserver(Observer o) {
        errorLog.addObserver(o);
    }
    
    public String getErrorLog() {
        return errorLog.getLogString();
    }
    
    public String getInfoLog() {
        return infoLog.getLogString();
    }
    
    public void addGUIComponent(JAMSGUIComponent component) {
        guiComponents.add(component);
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    
    class RunState extends Observable {
        
        private int state = JAMS.RUNSTATE_RUN;
        
        public int getState() {
            return state;
        }
        
        public void setState(int state) {
            this.state = state;
            this.setChanged();
            this.notifyObservers();
        }
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    
    
}
