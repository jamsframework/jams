/*
 * JAMSui.java
 * Created on 2. Oktober 2005, 16:05
 *
 * This file is part of JAMSui
 * Copyright (C) 2005 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Publiccc License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */
package jamsui.launcher;

import jamsui.cmdline.*;
import jams.tools.XMLTools;
import jams.tools.JAMSTools;
import java.io.*;
import javax.swing.UIManager;
import jams.runtime.*;
import jams.io.*;
import jams.JAMS;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.model.JAMSFullModelState;
import jams.model.Model;
import jams.tools.FileTools;
import jams.tools.StringTools;
import java.util.Properties;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSui {

    private static File baseDir = new File(System.getProperty("user.dir"));
    public static final String APP_TITLE = "JAMS";
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
                System.out.println(JAMS.i18n("Error_while_loading_config_from") + cmdLine.getConfigFileName());
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
                    System.out.println(JAMS.i18n("Error_while_loading_config_from") + defaultFile);
                    handle(ioe);
                }
            }
        }

        JAMSTools.configureLocaleEncoding(properties);

        if (cmdLine.isNogui()) {
            properties.setProperty(JAMSProperties.GUICONFIG_IDENTIFIER, "false");
            properties.setProperty(JAMSProperties.WINDOWENABLE_IDENTIFIER, "false");
            properties.setProperty(JAMSProperties.VERBOSITY_IDENTIFIER, "true");
            properties.setProperty(JAMSProperties.ERRORDLG_IDENTIFIER, "false");
        }

        boolean guiConfig = Boolean.parseBoolean(properties.getProperty(SystemProperties.GUICONFIG_IDENTIFIER, "false"));
        String modelFileName = cmdLine.getModelFileName();
        
        String floatFormat = properties.getProperty(SystemProperties.FLOAT_FORMAT, "%f");
        JAMS.setFloatFormat(floatFormat);

        // check if there is a model file provided
        if ((modelFileName == null)) {

            //check if at least GUI is enabled
            if (guiConfig) {
                startGUI();
            } else {
                System.out.println(JAMS.i18n("You_must_provide_a_model_file_name_(see_JAMS_--help)_when_disabling_GUI_config!"));
                System.exit(-1);
            }

        } else {

            String cmdLineParameterValues = cmdLine.getParameterValues();

            // if there is a model file, check if the user wants to use GUI
            if (guiConfig) {

                try {
                    Properties props = null;
                    if (cmdLine.getJmpFileName() != null) {
                        props = new Properties();
                        FileInputStream fis = new FileInputStream(new File(cmdLine.getJmpFileName()));
                        props.load(fis);
                        fis.close();
                    }
                    startGUI(modelFileName, cmdLineParameterValues, props);

                } catch (Exception e) {
                    JAMSui.handle(e);
                }

            } else {

                // if GUI is disabled and a model file provided, then run
                // the model directly

                String info = "";

                //check if file exists
                File file = new File(modelFileName);
                if (!file.exists()) {
                    System.out.println(JAMS.i18n("Model_file_") + modelFileName + JAMS.i18n("_could_not_be_found_-_exiting!"));
                    return;
                }

                // do some search and replace on the input file and create new file if necessary
                String newModelFilename = XMLProcessor.modelDocConverter(modelFileName);
                if (!newModelFilename.equalsIgnoreCase(modelFileName)) {
                    info = JAMS.i18n("The_model_definition_in_") + modelFileName + JAMS.i18n("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.i18n("_while_your_original_file_was_left_untouched.");
                    modelFileName = newModelFilename;
                }

                JAMSRuntime runtime = null;
                try {
                    String xmlString = FileTools.fileToString(modelFileName);
                    String[] args = StringTools.toArray(cmdLineParameterValues, ";");
                    if (args != null) {
                        for (int i = 0; i < args.length; i++) {
                            xmlString = xmlString.replaceAll("%" + i, args[i]);
                        }
                    }

                    Document modelDoc = XMLTools.getDocumentFromString(xmlString);

                    String jmpFileName = cmdLine.getJmpFileName();
                    if (jmpFileName != null) {
                        modelDoc = ParameterProcessor.loadParams(modelDoc, new File(jmpFileName));
                    }

                    // try to determine the default workspace directory
                    String defaultWorkspacePath = null;
                    if (Boolean.parseBoolean(properties.getProperty(JAMSProperties.USE_DEFAULT_WS_PATH)) && !StringTools.isEmptyString(modelFileName)) {
                        defaultWorkspacePath = new File(modelFileName).getParent();
                    }

                    runtime = new StandardRuntime(properties);

                    runtime.loadModel(modelDoc, defaultWorkspacePath);

                    // if workspace has not been provided, check if the document has been
                    // read from file and try to use parent directory instead
//                    if (StringTools.isEmptyString(runtime.getModel().getWorkspacePath())
//                            && !StringTools.isEmptyString(modelFileName)) {
//                        String dir = new File(modelFileName).getParent();
//                        runtime.getModel().setWorkspacePath(dir);
//                        runtime.sendInfoMsg(JAMS.i18n("no_workspace_defined_use_loadpath") + dir);
//                    }

                    if (!info.equals("")) {
                        runtime.println(info);
                    }
                    String snapshotFileName = cmdLine.getSnapshotFileName();
                    if (snapshotFileName != null) {
                        File snapshotFile = new File(snapshotFileName);
                        if (!snapshotFile.exists()) {
                            final JAMSFullModelState state = new JAMSFullModelState(snapshotFile);

                            Model model = state.getModel();
                            try {
                                model.getRuntime().resume(state.getSmallModelState());
                            } catch (Exception e) {
                                e.printStackTrace();
                                JAMSTools.handle(e);
                            }
                            // collect some garbage ;)
                            Runtime.getRuntime().gc();
                        }
                    } else {
                        runtime.runModel();
                    }

                } catch (IOException ioe) {
                    System.out.println(JAMS.i18n("The_model_definition_file_") + modelFileName + JAMS.i18n("_could_not_be_loaded,_because:_") + ioe.toString());
                } catch (SAXException se) {
                    System.out.println(JAMS.i18n("The_model_definition_file_") + modelFileName + JAMS.i18n("_contained_errors!"));
                } catch (Exception ex) {
                    ex.printStackTrace();
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
        new JAMSFrame(null, properties).setVisible(true);
    }

    protected void startGUI(String modelFileName, String cmdLineParameterValues, Properties jmpParameters) {
        new JAMSFrame(null, properties, modelFileName, cmdLineParameterValues, jmpParameters).setVisible(true);
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
