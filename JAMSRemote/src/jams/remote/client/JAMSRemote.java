/*
 * RemoteRuntime.java
 * Created on 20. Juni 2007, 10:01
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena
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
package jams.remote.client;

import jams.ui.gui.JAMSSplash;
import jams.io.JAMSCmdLine;
import jams.ui.JAMSui;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSRemote extends JAMSui {
    
    public JAMSRemote(JAMSCmdLine cmdLine) {   
        super(cmdLine);
    }
    
    @Override
    protected void startGUI() {
        JAMSSplash splash = new JAMSSplash();
        splash.show(new JAMSRemoteLauncher(properties), splashTimeout);
    }

    @Override
    protected void startGUI(String modelFileName, String cmdLineParameterValues) {
        JAMSSplash splash = new JAMSSplash();
        splash.show(new JAMSRemoteLauncher(modelFileName, properties, cmdLineParameterValues), splashTimeout);
    }

    public static void main(String[] args) {

        new JAMSRemote(new JAMSCmdLine(args));

    }

//    private static JAMSCmdLine cmdLine;
//
//    public static void startJAMS(JAMSProperties properties) {
//        JAMSSplash splash = new JAMSSplash();
//        splash.show(new JAMSRemoteLauncher(properties), JAMS.SPLASH_DISPLAY_TIME);
//    }
//
//    public static void startJAMS(JAMSProperties properties, String modelFilename, String cmdLineParameterValues) {
//
//        JAMSSplash splash = new JAMSSplash();
//        splash.show(new JAMSRemoteLauncher(modelFilename, properties, cmdLineParameterValues), SPLASH_DISPLAY_TIME);
//
//    }
//
//    public static void main(String[] args) {
//
//        cmdLine = new JAMSCmdLine(args);
//
//        try {
//            if (System.getProperty("os.name").contains("Windows")) {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            }
//        } catch (Exception evt) {
//        }
//
//        //create a JAMS default set of property values
//        JAMSProperties properties = JAMSProperties.createJAMSProperties();
//
//        //try to load property values from file
//        if (cmdLine.getConfigFileName() != null) {
//            //check for file provided at command line
//            try {
//                properties.load(cmdLine.getConfigFileName());
//            } catch (IOException ioe) {
//                System.out.println(JAMS.resources.getString("Error_while_loading_config_from") + cmdLine.getConfigFileName());
//                handle(ioe);
//            }
//        } else {
//            //check for default file
//            String defaultFile = System.getProperty("user.dir") + System.getProperty("file.separator") + JAMS.DEFAULT_PARAMETER_FILENAME;
//            File file = new File(defaultFile);
//            if (file.exists()) {
//                try {
//                    properties.load(defaultFile);
//                } catch (IOException ioe) {
//                    System.out.println(JAMS.resources.getString("Error_while_loading_config_from") + defaultFile);
//                    handle(ioe);
//                }
//            }
//        }
//
//
//        if ((cmdLine.getModelFileName() == null)) {
//            //see if at least GUI is enabled
//            startJAMS(properties);
//        } else {
//            if (cmdLine.getParameterValues() == null) {
//                startJAMS(properties, cmdLine.getModelFileName(), null);
//            } else {
//                startJAMS(properties, cmdLine.getModelFileName(), cmdLine.getParameterValues());
//            }
//        }
//    }
}
