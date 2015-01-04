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
import jams.JAMSLogging.LogOption;
import static jams.JAMSLogging.LogOption.CollectAndShow;
import jams.JAMSProperties;
import jams.SystemProperties;
import jams.logging.MsgBoxLogHandler;
import jams.model.JAMSFullModelState;
import jams.model.Model;
import jams.tools.FileTools;
import jams.tools.StringTools;
import jams.logging.NotificationLog;
import java.awt.GraphicsEnvironment;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    static final Logger logger = Logger.getLogger(JAMSui.class.getName());

    /**
     * JAMSui contructor
     *
     * @param cmdLine A JAMSCmdLine object containing the command line arguments
     */
    public JAMSui(JAMSCmdLine cmdLine) {
        //create a JAMS default set of property values
        properties = JAMSProperties.createProperties();

        String propertyFileName;
        if (cmdLine.getConfigFileName() != null) {
            //check for file provided at command line
            propertyFileName = cmdLine.getConfigFileName();
            baseDir = new File(propertyFileName).getParentFile();
        } else {
            //check for default file
            propertyFileName = new File(baseDir, JAMS.DEFAULT_PARAMETER_FILENAME).getAbsolutePath();
        }

        //try to load property values from file
        try {
            if (new File(propertyFileName).exists()) {
                properties.load(propertyFileName);
            } else {
                properties.save();
            }
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, JAMS.i18n("Error_while_loading_config_from") + propertyFileName, ioe);
        }

        JAMSTools.configureLocaleEncoding(properties);

        if (cmdLine.isNogui() || GraphicsEnvironment.isHeadless()) {
            properties.setProperty(JAMSProperties.GUICONFIG_IDENTIFIER, "false");
            properties.setProperty(JAMSProperties.WINDOWENABLE_IDENTIFIER, "false");
            properties.setProperty(JAMSProperties.VERBOSITY_IDENTIFIER, "true");
            properties.setProperty(JAMSProperties.ERRORDLG_IDENTIFIER, "false");
        }

        boolean guiConfig = Boolean.parseBoolean(properties.getProperty(SystemProperties.GUICONFIG_IDENTIFIER, "false"));

        String modelFileName = cmdLine.getModelFileName();
        if (modelFileName != null) {
            modelFileName = new File(modelFileName).getAbsolutePath();
        }

        String floatFormat = properties.getProperty(SystemProperties.FLOAT_FORMAT, "%f");
        JAMS.setFloatFormat(floatFormat);

        // check if there is a model file provided
        if ((modelFileName == null)) {

            //check if at least GUI is enabled
            if (guiConfig) {
                startGUI();
            } else {
                logger.severe(JAMS.i18n("You_must_provide_a_model_file_name_(see_JAMS_--help)_when_disabling_GUI_config!"));
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

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }

            } else {

                // if GUI is disabled and a model file provided, then run
                // the model directly
                //check if file exists
                File file = new File(modelFileName);
                if (!file.exists()) {
                    logger.severe(JAMS.i18n("Model_file_") + modelFileName + JAMS.i18n("_could_not_be_found_-_exiting!"));
                    System.exit(-1);
                }

                String info = "";

                // do some search and replace on the input file and create new file if necessary
                String newModelFilename = XMLProcessor.modelDocConverter(modelFileName);
                if (!newModelFilename.equalsIgnoreCase(modelFileName)) {
                    info = JAMS.i18n("The_model_definition_in_") + modelFileName + JAMS.i18n("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.i18n("_while_your_original_file_was_left_untouched.");
                    modelFileName = newModelFilename;
                }

                jams.runtime.JAMSRuntime runtime = null;
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
                            java.lang.Runtime.getRuntime().gc();
                            System.exit(0);
                        }
                    } else {

                        runtime.runModel();

//                        System.exit(0);
                    }

                } catch (IOException ioe) {
                    logger.severe(JAMS.i18n("The_model_definition_file_") + modelFileName + JAMS.i18n("_could_not_be_loaded,_because:_") + ioe.toString());
                } catch (SAXException se) {
                    logger.severe(JAMS.i18n("The_model_definition_file_") + modelFileName + JAMS.i18n("_contained_errors!"));
                } catch (Exception ex) {
                    if (runtime != null) {
                        runtime.handle(ex);
                    } else {
                        logger.log(Level.SEVERE, ex.getMessage(), ex);
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
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        new JAMSui(new JAMSCmdLine(args, APP_TITLE));

    }

    /**
     * Get the JAMSui base directory
     *
     * @return The JAMSui base directory
     */
    public static File getBaseDir() {
        return baseDir;
    }

    public static void registerLogger(LogOption option, Logger log) {
        switch (option) {
            case CollectAndShow:
                log.addHandler(NotificationLog.getInstance());
                log.setUseParentHandlers(false);
                break;
            case Show:
                log.addHandler(MsgBoxLogHandler.getInstance());
                log.setUseParentHandlers(true);
                break;
        }

    }

    public static void unregisterLogger(LogOption option, Logger log) {
        if (option == null) {
            log.removeHandler(NotificationLog.getInstance());
            log.removeHandler(MsgBoxLogHandler.getInstance());
        } else {
            switch (option) {
                case CollectAndShow:
                    log.removeHandler(NotificationLog.getInstance());
                    break;
                case Show:
                    log.removeHandler(MsgBoxLogHandler.getInstance());
                    break;
            }
        }
        log.setUseParentHandlers(true);
    }
}
