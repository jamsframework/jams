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
package jams;

import java.awt.Font;
import java.io.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jams.gui.JAMSFrame;
import jams.gui.JAMSSplash;
import jams.runtime.*;
import jams.io.*;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Kralisch
 */
public class JAMS {

    /**
     * Verbosity level 0 of 3
     */
    public static final int SILENT = 0;
    /**
     * Verbosity level 1 of 3
     */
    public static final int STANDARD = 1;
    /**
     * Verbosity level 2 of 3
     */
    public static final int VERBOSE = 2;
    /**
     * Verbosity level 3 of 3
     */
    public static final int VVERBOSE = 3;
    public static ResourceBundle resources = java.util.ResourceBundle.getBundle("resources/JAMSBundle");
    public static final Font STANDARD_FONT = new java.awt.Font("Courier", 0, 11);
    //public static final int TOOLBAR_HEIGHT = 38;
    public static final int SPLASH_DISPLAY_TIME = 1000;
    public static final String WIKI_URL = "http://jams.uni-jena.de/jamswiki";
    /**
     * Default name of model output file
     */
    public static final String DEFAULT_MODEL_FILENAME = "model.jmp";
    /**
     * Default name of parameter output file
     */
    public static final String DEFAULT_PARAMETER_FILENAME = "default.jap";
    private static JAMSCmdLine cmdLine;
    private static File baseDir = null;
    private static String versionString = null;

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
                System.out.println(JAMS.resources.getString("Error_during_look_and_feel_initialization"));
                ule.printStackTrace();
            }
        }

        //create a JAMS default set of property values
        JAMSProperties properties = JAMSProperties.createJAMSProperties();

        //try to load property values from file
        if (cmdLine.getConfigFileName() != null) {
            //check for file provided at command line
            properties.load(cmdLine.getConfigFileName());
            baseDir = new File(cmdLine.getConfigFileName()).getParentFile();
        } else {
            //check for default file
            String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;
            baseDir = new File(System.getProperty("user.dir"));
            File file = new File(defaultFile);
            if (file.exists()) {
                properties.load(defaultFile);
            }
        }

        String forcelocale = properties.getProperty("forcelocale");
        if ((forcelocale != null) && !forcelocale.equals("")) {
            Locale.setDefault(new Locale(forcelocale));
            resources = java.util.ResourceBundle.getBundle("resources/JAMSBundle");
        }

        int guiConfig = Integer.parseInt(properties.getProperty("guiconfig", "0"));

        if ((cmdLine.getModelFileName() == null)) {
            //see if at least GUI is enabled
            if (guiConfig == 1) {
                startJAMS(properties);
            } else {
                System.out.println(JAMS.resources.getString("You_must_provide_a_model_file_name_(see_JAMS_--help)_when_disabling_GUI_config!"));
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
        splash.show(new JAMSFrame(properties), SPLASH_DISPLAY_TIME);
    }

    public static void startJAMS(JAMSProperties properties, String modelFilename, String cmdLineParameterValues) {
        int guiConfig = Integer.parseInt(properties.getProperty("guiconfig", "0"));

        if (guiConfig == 1) {

            JAMSSplash splash = new JAMSSplash();
//            splash.show(new LauncherFrame(modelFilename, properties, cmdLineParameterValues), SPLASH_DISPLAY_TIME);
            try {
                splash.show(new JAMSFrame(properties, modelFilename, cmdLineParameterValues), SPLASH_DISPLAY_TIME);
            } catch (Exception e) {
            }

        } else {

            String info = "";

            //check if file exists
            File file = new File(modelFilename);
            if (!file.exists()) {
                System.out.println(JAMS.resources.getString("Model_file_") + modelFilename + JAMS.resources.getString("_could_not_be_found_-_exiting!"));
                return;
            }

            // do some search and replace on the input file and create new file if necessary
            String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
            if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
                info = JAMS.resources.getString("The_model_definition_in_") + modelFilename + JAMS.resources.getString("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.resources.getString("_while_your_original_file_was_left_untouched.");
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
                System.out.println(JAMS.resources.getString("The_model_definition_file_") + modelFilename + JAMS.resources.getString("_could_not_be_loaded,_because:_") + ioe.toString());
            } catch (SAXException se) {
                System.out.println(JAMS.resources.getString("The_model_definition_file_") + modelFilename + JAMS.resources.getString("_contained_errors!"));
            } catch (Exception ex) {
                if (runtime != null) {
                    runtime.handle(ex);
                } else {
                    ex.printStackTrace();
                }
            }

        }
    }

    public static File getBaseDir() {
        return baseDir;
    }

}
