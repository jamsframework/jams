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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import org.unijena.jams.JAMS;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.data.JAMSData;
import org.unijena.jams.data.JAMSEntityCollection;
import org.unijena.jams.io.ModelLoader;
import org.unijena.jams.io.ParameterProcessor;
import org.unijena.jams.model.JAMSGUIComponent;
import org.unijena.jams.model.JAMSModel;
import org.w3c.dom.Document;

/**
 *
 * @author S. Kralisch
 */
public class StandardRuntime implements JAMSRuntime {

    private HashMap<String, JAMSData> dataHandles = new HashMap<String, JAMSData>();
    private JAMSLog errorLog = new JAMSLog();
    private JAMSLog infoLog = new JAMSLog();
    private int debugLevel = JAMS.STANDARD;
    private RunState runState = new RunState();
    private ArrayList<JAMSGUIComponent> guiComponents = new ArrayList<JAMSGUIComponent>();
    private JButton stopButton,  closeButton;
    private JFrame frame;
    private JAMSModel model;
    private PrintStream infoStream,  errorStream;
    private boolean guiEnabled = false;
    private ClassLoader classLoader;
    private Document modelDocument = null;
    private JAMSProperties properties = null;

    @Override
    public void deleteErrorLogObservers() {
        errorLog.deleteObservers();
    }

    @Override
    public void deleteInfoLogObservers() {
        infoLog.deleteObservers();
    }

    @Override
    public void loadModel(Document modelDocument, JAMSProperties properties) {

        this.modelDocument = modelDocument;
        this.properties = properties;

        long start = System.currentTimeMillis();

        // set the debug (i.e. output verbosity) level
        this.setDebugLevel(Integer.parseInt(properties.getProperty("debug", "1")));

        // add log observers for output to system.out if needed
        int verbose = Integer.parseInt(properties.getProperty("verbose", "1"));
        if (verbose != 0) {

            // add info and error log output
            this.addInfoLogObserver(new Observer() {

                @Override
                public void update(Observable obs, Object obj) {
                    System.out.print(obj);
                }
            });
            this.addErrorLogObserver(new Observer() {

                @Override
                public void update(Observable obs, Object obj) {
                    System.out.print(obj);
                }
            });
        }

        int errorDlg = Integer.parseInt(properties.getProperty("errordlg", "0"));
        if (errorDlg != 0) {

            // add error log output via JDialog
            this.addErrorLogObserver(new Observer() {

                @Override
                public void update(Observable obs, Object obj) {

                    Object[] options = {"OK", "OK, skip other messages"};
                    int result = JOptionPane.showOptionDialog(frame, obj.toString(), "Model execution error",
                            JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

                    if (result == 1) {
                        StandardRuntime.this.deleteErrorLogObserver(this);
                    }

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
            if ((properties.getProperty("errorlog") != null) && (!properties.getProperty("errorlog").equals(""))) {
                errorStream = new PrintStream(properties.getProperty("errorlog"));
            }
        } catch (FileNotFoundException fnfe) {
            this.handle(fnfe);
        }

        // get libraries specified in properties
        String[] libs = JAMSTools.toArray(properties.getProperty("libs", ""), ";");

        // load the libraries and create the class loader
        classLoader = JAMSClassLoader.createClassLoader(libs, this);

        // create model config object from config document
        //ModelConfig config = new ModelConfig(modelDocument);

        // create preprocessor
        //ModelPreprocessor preProc = new ModelPreprocessor(modelDocument, config, this);

        // run preprocessor
        //preProc.process();
        ParameterProcessor.preProcess(modelDocument);

        // load the model
        ModelLoader modelLoader = new ModelLoader(modelDocument, null, this, properties);
        this.model = modelLoader.getModel();

        // create GUI if needed
        int wEnable = Integer.parseInt(properties.getProperty("windowenable", "1"));
        if (wEnable != 0) {
            int width = Integer.parseInt(properties.getProperty("windowwidth", "600"));
            int height = Integer.parseInt(properties.getProperty("windowheight", "400"));
            int ontop = Integer.parseInt(properties.getProperty("windowontop", "0"));
            this.initGUI(model.getName(), (ontop == 1 ? true : false), width, height);
            this.guiEnabled = true;
        }

        long end = System.currentTimeMillis();
        this.println("JAMS model setup time: " + (end - start) + " ms", JAMS.STANDARD);

        classLoader = null;
        Runtime.getRuntime().gc();
    }

    @Override
    public void runModel() {

        //check if runstate is on "run"
        if (this.getRunState() != JAMS.RUNSTATE_RUN) {
            return;
        }

        if (guiEnabled && (guiComponents.size() > 0)) {
            frame.setVisible(true);
        }

        long start = System.currentTimeMillis();

        if (this.getRunState() == JAMS.RUNSTATE_RUN) {
            model.init();
        }

        if (this.getRunState() == JAMS.RUNSTATE_RUN) {
            model.run();
        }

        if (this.getRunState() == JAMS.RUNSTATE_RUN) {
            model.cleanup();
        }

        long end = System.currentTimeMillis();
        this.println("JAMS model execution time: " + (end - start) + " ms", JAMS.STANDARD);

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
        classLoader = null;
        Runtime.getRuntime().gc();
    }

    @Override
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
            tabbedPane.setTabPlacement(JTabbedPane.LEFT);

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
//        toolBar.setPreferredSize(new Dimension(0, JAMS.TOOLBAR_HEIGHT));

        stopButton = new JButton();
        stopButton.setToolTipText("Stop model");
        stopButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/ModelStop.png")));
        stopButton.setEnabled(true);
        stopButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                StandardRuntime.this.sendHalt();
                stopButton.setEnabled(false);
            }
        });
        toolBar.add(stopButton);

        closeButton = new JButton();
        closeButton.setToolTipText("Close window");
        closeButton.setIcon(new ImageIcon(getClass().getResource("/resources/images/Shutdown.png")));
        closeButton.setEnabled(false);
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                StandardRuntime.this.sendHalt();
                frame.dispose();
                Runtime.getRuntime().gc();
            }
        });
        toolBar.add(closeButton);
        frame.add(toolBar, BorderLayout.NORTH);

        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                StandardRuntime.this.sendHalt();
                frame.dispose();
                Runtime.getRuntime().gc();
            }
        });

        this.addRunStateObserver(new Observer() {

            @Override
            public void update(Observable obs, Object obj) {
                stopButton.setEnabled(false);
                if (StandardRuntime.this.getRunState() == JAMS.RUNSTATE_STOP) {
                    closeButton.setEnabled(true);
                }
            }
        });

        frame.pack();
        frame.setLocation((int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() - width) / 2, (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height) / 2);

    }

    @Override
    public HashMap<String, JAMSData> getDataHandles() {
        return dataHandles;
    }

    public void setDataHandles(HashMap<String, JAMSData> dataHandles) {
        this.dataHandles = dataHandles;
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
    public int getDebugLevel() {
        return debugLevel;
    }

    @Override
    public void setDebugLevel(int aDebugLevel) {
        debugLevel = aDebugLevel;
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

    @Override
    public void sendHalt() {
        runState.setState(JAMS.RUNSTATE_STOP);
    }

    @Override
    public void sendHalt(String str) {
        sendErrorMsg(str);
        sendHalt();
    }

    @Override
    public void sendErrorMsg(String str) {
        errorLog.print("ERROR: " + str + "\n");
    }

    @Override
    public void sendInfoMsg(String str) {
        infoLog.print("INFO: " + str + "\n");
    }

    @Override
    public void addRunStateObserver(Observer o) {
        runState.addObserver(o);
    }

    @Override
    public int getRunState() {
        return runState.getState();
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
    public void addErrorLogObserver(Observer o) {
        errorLog.addObserver(o);
    }

    @Override
    public void deleteErrorLogObserver(Observer o) {
        errorLog.deleteObserver(o);
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
        guiComponents.add(component);
    }

    public void saveModelParameter() {
        // save the model's parameter set to the workspace output dir
        try {
            File modelFile = new File(this.model.getWorkspace().getOutputDataDirectory().getPath() +
                    File.separator + JAMS.DEFAULT_PARAMETER_FILE_NAME);
            ParameterProcessor.saveParams(this.modelDocument, modelFile, this.properties.getProperty("username"), null);
        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
    }

    public ByteArrayOutputStream GetRuntimeState(String fileName) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ObjectOutputStream objOut = null;

        try {
            objOut = new ObjectOutputStream(outStream);
            objOut.writeObject(this.model.getEntities());
        } catch (IOException e) {
            sendErrorMsg("Unable to save model state because," + e.toString());
        }

        try {
            if (fileName != null) {
                FileOutputStream fos = new FileOutputStream(fileName);
                outStream.writeTo(fos);
                fos.close();
            }
        } catch (Exception e) {
            sendErrorMsg("Could not write model state to file, because" + e.toString());
        }

        try {
            objOut.close();
            outStream.close();
        } catch (IOException e) {
            sendErrorMsg("Unable to save model state, because" + e.toString());
        }
        return outStream;
    }

    public JAMSModel getModel() {
        return this.model;
    }

    public void SetRuntimeState(ByteArrayInputStream inStream) {
        try {
            ObjectInputStream objIn = new ObjectInputStream(inStream);
            JAMSEntityCollection e = (JAMSEntityCollection) objIn.readObject();
            this.model.setEntities(e);
            objIn.close();
        } catch (Exception e) {
            sendErrorMsg("Unable to deserializing jamsentity collection, because" + e.toString());
        }
    }

    @Override
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

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
