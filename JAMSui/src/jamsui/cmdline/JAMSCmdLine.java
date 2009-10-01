/*
 * JAMSCmdLine.java
 * Created on 5. Februar 2007, 17:19
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena
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

package jamsui.cmdline;

import jams.io.*;
import jams.*;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSCmdLine {
    
    private String configFileName;
    private String modelFileName = null;
    private String parameterValues = null;
    private String[] otherArgs = null;
    private static final String USAGE_STRING = JAMS.resources.getString("[Options]") +
            JAMS.resources.getString("__-h,_--help_________________________________________Print_help") +
            JAMS.resources.getString("__-c,_--config_<config_file_name>____________________Provide_config_file_name") +
            JAMS.resources.getString("__-m,_--model_<model_definition_file_name>___________Provide_model_file_name") +
            JAMS.resources.getString("__-p,_--parametervalue_<list_of_parameter_values>____Provide_initial_parameter_values_divided_by_semicolons");
    
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
        CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");

        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            System.err.println(JAMS.resources.getString("Usage:_") + appTitle + " " + USAGE_STRING);
            System.exit(2);
        }
        
        boolean usage = ((Boolean) parser.getOptionValue(helpOption, Boolean.FALSE)).booleanValue();
        if (usage) {
            System.out.println(JAMS.resources.getString("Usage:_") + appTitle + " " + USAGE_STRING);
            System.exit(0);
        }
        
        this.configFileName = (String) parser.getOptionValue(configOption, null);
        this.modelFileName = (String) parser.getOptionValue(modelOption, null);
        this.parameterValues = (String) parser.getOptionValue(pValueOption, null);
        this.otherArgs = parser.getRemainingArgs();
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
}
