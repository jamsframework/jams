/*
 * JAMSui.java
 * Created on 2. Oktober 2005, 16:05
 *
 * This file is part of JAMSui
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
package jamsui.launcher;

import jamsui.cmdline.*;
import jams.tools.XMLIO;
import jams.tools.JAMSTools;
import java.io.*;
import javax.swing.UIManager;
import jams.gui.JAMSSplash;
import jams.runtime.*;
import jams.io.*;
import jams.JAMS;
import jams.JAMSProperties;
import jams.SystemProperties;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSui {

    private static File baseDir = new File(System.getProperty("user.dir"));

    public static final String APP_TITLE = "JAMS";

    protected int splashTimeout;

    protected SystemProperties properties;

    /**
     * JAMSui contructor
     * @param cmdLine A JAMSCmdLine object containing the command line arguments
     */
    public JAMSui(JAMSCmdLine cmdLine) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception lnfe) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                handle(ex);
            }
        }

        //create a JAMSui default set of property values
        properties = JAMSProperties.createProperties();

        //try to load property values from file
        if (cmdLine.getConfigFileName() != null) {
            //check for file provided at command line
            try {
                properties.load(cmdLine.getConfigFileName());
            } catch (IOException ioe) {
                System.out.println(JAMS.resources.getString("Error_while_loading_config_from") + cmdLine.getConfigFileName());
                handle(ioe);
            }
            baseDir = new File(cmdLine.getConfigFileName()).getParentFile();
        } else {
            //check for default file
            String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;
            File file = new File(defaultFile);
            if (file.exists()) {
                try {
                    properties.load(defaultFile);
                } catch (IOException ioe) {
                    System.out.println(JAMS.resources.getString("Error_while_loading_config_from") + defaultFile);
                    handle(ioe);
                }
            }
        }

        JAMSTools.configureLocale(properties);

        splashTimeout = Integer.parseInt(properties.getProperty("splashtimeout", "1000"));

        int guiConfig = Integer.parseInt(properties.getProperty("guiconfig", "0"));
        String modelFileName = cmdLine.getModelFileName();

        // check if there is a model file provided
        if ((modelFileName == null)) {

            //check if at least GUI is enabled
            if (guiConfig == 1) {
                startGUI();
            } else {
                System.out.println(JAMS.resources.getString("You_must_provide_a_model_file_name_(see_JAMS_--help)_when_disabling_GUI_config!"));
                System.exit(-1);
            }

        } else {

            String cmdLineParameterValues = cmdLine.getParameterValues();

            // if there is a model file, check if the user wants to use GUI
            if (guiConfig == 1) {

                try {
                    startGUI(modelFileName, cmdLineParameterValues);
                } catch (Exception e) {
                    JAMSui.handle(e);
                }

            } else {

                // if no GUI is disabled and a model file provided, then run
                // the model directly

                String info = "";

                //check if file exists
                File file = new File(modelFileName);
                if (!file.exists()) {
                    System.out.println(JAMS.resources.getString("Model_file_") + modelFileName + JAMS.resources.getString("_could_not_be_found_-_exiting!"));
                    return;
                }

                // do some search and replace on the input file and create new file if necessary
                String newModelFilename = XMLProcessor.modelDocConverter(modelFileName);
                if (!newModelFilename.equalsIgnoreCase(modelFileName)) {
                    info = JAMS.resources.getString("The_model_definition_in_") + modelFileName + JAMS.resources.getString("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.resources.getString("_while_your_original_file_was_left_untouched.");
                    modelFileName = newModelFilename;
                }

                String xmlString = JAMSTools.fileToString(modelFileName);
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
                    System.out.println(JAMS.resources.getString("The_model_definition_file_") + modelFileName + JAMS.resources.getString("_could_not_be_loaded,_because:_") + ioe.toString());
                } catch (SAXException se) {
                    System.out.println(JAMS.resources.getString("The_model_definition_file_") + modelFileName + JAMS.resources.getString("_contained_errors!"));
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

    protected void startGUI() {
        JAMSSplash splash = new JAMSSplash();
        splash.show(new JAMSFrame(null, properties), splashTimeout);
    }

    protected void startGUI(String modelFileName, String cmdLineParameterValues) {
        JAMSSplash splash = new JAMSSplash();
        splash.show(new JAMSFrame(null, properties, modelFileName, cmdLineParameterValues), splashTimeout);
    }

    /**
     * JAMSui main method
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        new JAMSui(new JAMSCmdLine(args, APP_TITLE));

    }

    /**
     * Exception handling method
     * @param ex Exception to be handled
     */
    public static void handle(Throwable t) {
        handle(t, true);
    }

    /**
     * Exception handling method
     * @param ex Exception to be handled
     * @param proceed Proceed or not?
     */
    public static void handle(Throwable t, boolean proceed) {
        t.printStackTrace();
        if (!proceed) {
            System.exit(-1);
        }
    }

    /**
     * Get the JAMSui base directory
     * @return The JAMSui base directory
     */
    public static File getBaseDir() {
        return baseDir;
    }
}
