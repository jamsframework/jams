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

import java.io.File;
import java.io.IOException;
import javax.swing.UIManager;
import org.unijena.jams.JAMS;
import org.unijena.jams.JAMSCmdLine;
import org.unijena.jams.JAMSProperties;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.gui.JAMSLauncher;
import org.unijena.jams.gui.JAMSSplash;
import org.unijena.jams.io.XMLIO;
import org.unijena.jams.io.XMLProcessor;
import org.unijena.jams.runtime.JAMSRuntime;
import org.unijena.jams.runtime.StandardRuntime;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSRemote extends JAMS {
    
    private static JAMSCmdLine cmdLine;
    
    public static void startJAMS(JAMSProperties properties) {
        JAMSSplash splash = new JAMSSplash();
        splash.show(new JAMSRemoteLauncher(properties), JAMS.SPLASH_DISPLAY_TIME);
    }
    
    public static void startJAMS(JAMSProperties properties, String modelFilename, String cmdLineParameterValues) {
        
        int guiConfig = Integer.parseInt(properties.getProperty("guiconfig", "0"));
        
        if (guiConfig == 1) {
            
            JAMSSplash splash = new JAMSSplash();
            splash.show(new JAMSRemoteLauncher(modelFilename, properties, cmdLineParameterValues), SPLASH_DISPLAY_TIME);
            
        } else {
            
            String info = "";
            
            // do some search and replace on the input file and create new file if necessary
            String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
            if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
                info = "The model definition in \"" + modelFilename + "\" has been adapted in order to meet modifications of the JAMS model DTD.\nThe new definition has been stored in \"" + newModelFilename + "\" while your original file was left untouched.";
                modelFilename = newModelFilename;
            }
            
            String xmlString = JAMSTools.fileToString(modelFilename);
            String[] args = JAMSTools.toArray(cmdLineParameterValues, ";");
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    xmlString = xmlString.replaceAll("%"+i, args[i]);
                }
            }
            
            try {
                
                Document modelDoc = XMLIO.getDocumentFromString(xmlString);
                JAMSRuntime runtime = new StandardRuntime();
                runtime.loadModel(modelDoc, properties);
                
                if (!info.equals("")) {
                    runtime.println(info);
                }
                runtime.runModel();
                
            } catch (IOException ioe) {
                System.out.println("The model definition file " + modelFilename + " could not be loaded!");
            } catch (SAXException se) {
                System.out.println("The model definition file " + modelFilename + " contained errors!");
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        cmdLine = new JAMSCmdLine(args);
        
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception evt) {}
        
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
    
}
