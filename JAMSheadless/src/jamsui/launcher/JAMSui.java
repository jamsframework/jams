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
import jams.logging.NotificationLogHandler;
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

    public static final String APP_TITLE = "JAMS";
    protected JAMSProperties jamsProperties;
    static final Logger logger = Logger.getLogger(JAMSui.class.getName());

    /**
     * JAMSui constructor
     *
     * @param cmdLine A JAMSCmdLine object containing the command line arguments
     */
    public JAMSui(JAMSCmdLine cmdLine) {

        //create a JAMS default set of property values
        jamsProperties = JAMSProperties.createProperties();

        //try to load property values from file
        String fileName = null;
        try {
            if (cmdLine.getConfigFileName() != null) {

                //check for  file provided at command line
                fileName = cmdLine.getConfigFileName();
                JAMS.initBaseDir(fileName);
                jamsProperties.load(fileName);

            } else {

                JAMS.initBaseDir();

                //check for the default file
                File file = new File(JAMS.getBaseDir(), JAMS.DEFAULT_PROPERTY_FILENAME);
                if (file.exists()) {
                    fileName = file.getAbsolutePath();
                    jamsProperties.load(fileName);
                }

            }

        } catch (IOException ioe) {
            logger.log(Level.SEVERE, JAMS.i18n("Error_while_loading_config_from") + fileName, ioe);
        }

        JAMSTools.configureLocaleEncoding(jamsProperties);

        if (cmdLine.isNogui() || GraphicsEnvironment.isHeadless()) {
            jamsProperties.setProperty(JAMSProperties.GUICONFIG_IDENTIFIER, "false");
            jamsProperties.setProperty(JAMSProperties.WINDOWENABLE_IDENTIFIER, "false");
            jamsProperties.setProperty(JAMSProperties.VERBOSITY_IDENTIFIER, "true");
            jamsProperties.setProperty(JAMSProperties.ERRORDLG_IDENTIFIER, "false");
        }

        boolean guiConfig = Boolean.parseBoolean(jamsProperties.getProperty(SystemProperties.GUICONFIG_IDENTIFIER, "false"));

        String modelFileName = cmdLine.getModelFileName();
        if (modelFileName != null) {
            modelFileName = new File(modelFileName).getAbsolutePath();
        }

        String floatFormat = jamsProperties.getProperty(SystemProperties.FLOAT_FORMAT, "%f");
        JAMS.setFloatFormat(floatFormat);

        // check if there is a model file provided
        if ((modelFileName == null)) {

            logger.severe(JAMS.i18n("You_must_provide_a_model_file_name_(see_JAMS_--help)_when_disabling_GUI_config!"));
            System.exit(-1);

        } else {

            boolean os64 = JAMSTools.is64Bit();
            boolean vm64 = System.getProperty("os.arch").endsWith("64");

            if (os64 != vm64) {
                String osArch = (os64 ? "64 bit" : "32 bit");
                String vmArch = (vm64 ? "64 bit" : "32 bit");
                logger.warning(String.format("Architectures of OS (%s) and Java VM (%s) used with JAMS seem to differ.\n"
                        + "You should update your Java VM to a matching version to avoid strange model behaviour.\n"
                        + "Continue anyway?", osArch, vmArch));
            }

            String cmdLineParameterValues = cmdLine.getParameterValues();

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
                        xmlString = xmlString.replaceAll("%" + i + "%", args[i]);
                    }
                }

                Document modelDoc = XMLTools.getDocumentFromString(xmlString);

                String jmpFileName = cmdLine.getJmpFileName();
                if (jmpFileName != null) {
                    modelDoc = ParameterProcessor.loadParams(modelDoc, new File(jmpFileName));
                }

                // try to determine the default workspace directory
                String defaultWorkspacePath = null;
                if (Boolean.parseBoolean(jamsProperties.getProperty(JAMSProperties.USE_DEFAULT_WS_PATH)) && !StringTools.isEmptyString(modelFileName)) {
                    defaultWorkspacePath = new File(modelFileName).getParent();
                }

                runtime = new StandardRuntime(jamsProperties);

                runtime.loadModel(modelDoc, defaultWorkspacePath, modelFileName);

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

    private void setLaF() {
        String desiredLookAndFeel = jamsProperties.getProperty("lookandfeel");
        try {
            boolean successful = false;
            if (desiredLookAndFeel != null) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    System.out.println(info.getName());
                    if (desiredLookAndFeel.equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        successful = true;
                        break;
                    }
                }
            }
            if (!successful) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception lnfe) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                JAMSTools.handle(ex);
            }
        }
    }

    /**
     * JAMSui main method
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {

        new JAMSui(new JAMSCmdLine(args, APP_TITLE));

    }

    public static void registerLogger(LogOption option, Logger log) {
        switch (option) {
            case CollectAndShow:
                log.addHandler(NotificationLogHandler.getInstance());
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
            log.removeHandler(NotificationLogHandler.getInstance());
            log.removeHandler(MsgBoxLogHandler.getInstance());
        } else {
            switch (option) {
                case CollectAndShow:
                    log.removeHandler(NotificationLogHandler.getInstance());
                    break;
                case Show:
                    log.removeHandler(MsgBoxLogHandler.getInstance());
                    break;
            }
        }
        log.setUseParentHandlers(true);
    }
}
