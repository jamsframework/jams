/*
 * JAMS.java
 * Created on 2. Oktober 2005, 16:05
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
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.jams;

import java.awt.Font;
import java.io.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.unijena.jams.gui.GUILauncher;
import org.unijena.jams.gui.JAMSSplash;
import org.unijena.jams.gui.LauncherFrame;
import org.unijena.jams.gui.PlainGUILauncher;
import org.unijena.jams.runtime.*;
import org.unijena.jams.io.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Kralisch
 */
public class JAMS {

    public static final int SILENT = 0;
    public static final int STANDARD = 1;
    public static final int VERBOSE = 2;
    public static final int VVERBOSE = 3;
    public static final int RUNSTATE_STOP = 0;
    public static final int RUNSTATE_RUN = 1;
    public static final Font STANDARD_FONT = new java.awt.Font("Courier", 0, 11);
    //public static final int TOOLBAR_HEIGHT = 38;
    public static final int SPLASH_DISPLAY_TIME = 0;
    public static final String WIKI_URL = "http://jams.uni-jena.de/jamswiki";
    private static JAMSCmdLine cmdLine;

    public static void handle(Exception ex) {
        handle(ex, true);
    }

    public static void handle(Exception ex, boolean proceed) {
        ex.printStackTrace();
        if (!proceed) {
            System.exit(-1);
        }
    }

    public static void main(String[] args) throws Exception {

        cmdLine = new JAMSCmdLine(args);

        if (System.getProperty("os.name").contains("Windows")) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (UnsupportedLookAndFeelException ule) {
                System.out.println("Error during look and feel initialization");
                ule.printStackTrace();
            }
        }

        //create a JAMS default set of property values
        JAMSProperties properties = JAMSProperties.createJAMSProperties();

        //try to load property values from file
        if (cmdLine.getConfigFileName() != null) {
            //check for file provided at command line
            properties.load(cmdLine.getConfigFileName());
        } else {
            //check for default file
            String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMSProperties.DEFAULT_FILENAME;
            File file = new File(defaultFile);
            if (file.exists()) {
                properties.load(defaultFile);
            }
        }

        int guiConfig = Integer.parseInt(properties.getProperty("guiconfig", "0"));

        if ((cmdLine.getModelFileName() == null)) {
            //see if at least GUI is enabled
            if (guiConfig == 1) {
                startJAMS(properties);
            } else {
                System.out.println("You must provide a model file name (see JAMS --help) when disabling GUI config!");
            }
        } else {
            if (cmdLine.getParameterValues() == null) {
                startJAMS(properties, cmdLine.getModelFileName(), null);
            } else {
                startJAMS(properties, cmdLine.getModelFileName(), cmdLine.getParameterValues());
            }
        }
    }

    public static void startJAMS(JAMSProperties properties) {
        JAMSSplash splash = new JAMSSplash();
        splash.show(new LauncherFrame(properties), SPLASH_DISPLAY_TIME);
    }

    public static void startJAMS(JAMSProperties properties, String modelFilename, String cmdLineParameterValues) {

        int guiConfig = Integer.parseInt(properties.getProperty("guiconfig", "0"));

        if (guiConfig == 1) {

            JAMSSplash splash = new JAMSSplash();
//            splash.show(new LauncherFrame(modelFilename, properties, cmdLineParameterValues), SPLASH_DISPLAY_TIME);
            try {
                splash.show(new GUILauncher(properties, modelFilename, cmdLineParameterValues), SPLASH_DISPLAY_TIME);
            } catch (Exception e) {
            }

        } else {

            String info = "";

            //check if file exists
            File file = new File(modelFilename);
            if (!file.exists()) {
                System.out.println("Model file " + modelFilename + " could not be found - exiting!");
                return;
            }

            // do some search and replace on the input file and create new file if necessary
            String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
            if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
                info = "The model definition in \"" + modelFilename + "\" has been adapted in order to meet modifications of the JAMS model schema.\nThe new definition has been stored in \"" + newModelFilename + "\" while your original file was left untouched.";
                modelFilename = newModelFilename;
            }

            String xmlString = JAMSTools.fileToString(modelFilename);
            String[] args = JAMSTools.toArray(cmdLineParameterValues, ";");
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    xmlString = xmlString.replaceAll("%" + i, args[i]);
                }
            }

            JAMSRuntime runtime = null;
            try {
                Document modelDoc = XMLIO.getDocumentFromString(xmlString);
                runtime = new StandardRuntime();
                runtime.loadModel(modelDoc, properties);

                if (!info.equals("")) {
                    runtime.println(info);
                }
                runtime.runModel();
            } catch (IOException ioe) {
                System.out.println("The model definition file " + modelFilename + " could not be loaded, because: " + ioe.toString());                
            } catch (SAXException se) {
                System.out.println("The model definition file " + modelFilename + " contained errors!");
            } catch (Exception ex) {
                if (runtime != null) {
                    runtime.handle(ex);
                } else {
                    ex.printStackTrace();
                }
            }

        }
    }
}
