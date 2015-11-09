/*
 * JAMSCmdLine.java
 * Created on 5. Februar 2007, 17:19
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package jamsui.cmdline;

import jams.io.*;
import jams.*;
import jams.tools.StringTools;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSCmdLine {
    
    private String configFileName;
    private String modelFileName = null;
    private String parameterValues = null;
    private String snapshotFileName = null;
    private String jmpFileName = null;
    
    private String[] otherArgs = null;
    private boolean nogui = false;
    private static final String USAGE_STRING = JAMS.i18n("[Options]") +
            JAMS.i18n("__-h,_--help_________________________________________Print_help") +
            JAMS.i18n("__-c,_--config_<config_file_name>____________________Provide_config_file_name") +
            JAMS.i18n("__-m,_--model_<model_definition_file_name>___________Provide_model_file_name") +
            JAMS.i18n("__-s,_--snapshot_<save_snapshot_file>________________Provide_model_snapshot_name") +
            JAMS.i18n("__-n,_--nogui________________________________________Suppress_all_GUI") +
            JAMS.i18n("__-p,_--parametervalue_<list_of_parameter_values>____Provide_initial_parameter_values_divided_by_semicolons") +
            JAMS.i18n("__-j,_--jams_parameterfile_<parameter_file_name>___Provide_initial_parameter_values_by_jmpfile");
    
    /**
     * Creates a new JAMSCmdLine object
     * @param args The argument list as String array
     * @param appTitle The title of the application
     */
    public JAMSCmdLine(String [] args, String appTitle) {
        
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option configOption = parser.addStringOption('c', "config");
        CmdLineParser.Option modelOption = parser.addStringOption('m', "model");
        CmdLineParser.Option pValueOption = parser.addStringOption('p', "parametervalue");
        CmdLineParser.Option jmpValueOption = parser.addStringOption('j', "jmpfile");
        CmdLineParser.Option snapshotOption = parser.addStringOption('s', "snapshot");
        CmdLineParser.Option noguiOption = parser.addBooleanOption('n', "nogui");
        CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");

        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            System.err.println(JAMS.i18n("Usage:_") + appTitle + " " + USAGE_STRING);
            System.exit(2);
        }
        
        boolean usage = ((Boolean) parser.getOptionValue(helpOption, Boolean.FALSE)).booleanValue();
        if (usage) {
            System.out.println(JAMS.i18n("Usage:_") + appTitle + " " + USAGE_STRING);
            System.exit(0);
        }

        this.nogui = ((Boolean) parser.getOptionValue(noguiOption, Boolean.FALSE)).booleanValue();
        this.configFileName = (String) parser.getOptionValue(configOption, null);
        this.modelFileName = (String) parser.getOptionValue(modelOption, null);
        this.snapshotFileName = (String) parser.getOptionValue(snapshotOption, null);
        this.parameterValues = (String) parser.getOptionValue(pValueOption, null);
        this.jmpFileName = (String) parser.getOptionValue(jmpValueOption, null);
        this.otherArgs = parser.getRemainingArgs();
        
        if (StringTools.isEmptyString(this.modelFileName) && otherArgs.length > 0 && !otherArgs[0].startsWith("-")) {
            this.modelFileName = otherArgs[0];
        }
    }
    
    /**
     * Returns the name of the config file
     * @return The name of the config file
     */
    public String getConfigFileName() {
        return configFileName;
    }
    
    /**
     * Returns the name of the model file
     * @return The name of the model file
     */
    public String getModelFileName() {
        return modelFileName;
    }

    public String getSnapshotFileName() {
        return this.snapshotFileName;
    }
    
    /**
     * Return all additional arguments
     * @return The list of additional arguments as String array
     */
    public String[] getOtherArgs() {
        return otherArgs;
    }

    /**
     * Return the list of parameter values
     * @return The String representing a list of parameter values
     */
    public String getParameterValues() {
        return parameterValues;
    }

    /**
     * Returns the name of the config file
     * @return The name of the config file
     */
    public String getJmpFileName() {
        return jmpFileName;
    }

    /**
     * @return the nogui
     */
    public boolean isNogui() {
        return nogui;
    }
}
