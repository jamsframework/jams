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

package jams;

/**
 *
 * @author Sven Kralisch
 */
public class JAMSCmdLine {
    
    private String configFileName = null;
    private String modelFileName = null;
    private String parameterValues = null;
    private String[] otherArgs = null;
    private String appTitle;
    public static final String USAGE_STRING = java.util.ResourceBundle.getBundle("resources/Bundle").getString("[Options]") +
            java.util.ResourceBundle.getBundle("resources/Bundle").getString("__-h,_--help_________________________________________Print_help") +
            java.util.ResourceBundle.getBundle("resources/Bundle").getString("__-c,_--config_<config_file_name>____________________Provide_config_file_name") +
            java.util.ResourceBundle.getBundle("resources/Bundle").getString("__-m,_--model_<model_definition_file_name>___________Provide_model_file_name") +
            java.util.ResourceBundle.getBundle("resources/Bundle").getString("__-p,_--parametervalue_<list_of_parameter_values>____Provide_initial_parameter_values_divided_by_semicolons");
    
    public JAMSCmdLine(String [] args) {
        this(args, "JAMS");
    }
    
    public JAMSCmdLine(String [] args, String appTitle) {
        
        this.appTitle = appTitle;
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option configOption = parser.addStringOption('c', "config");
        CmdLineParser.Option modelOption = parser.addStringOption('m', "model");
        CmdLineParser.Option pValueOption = parser.addStringOption('p', "parametervalue");
        CmdLineParser.Option helpOption = parser.addBooleanOption('h', "help");
        
        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e) {
            System.err.println(e.getMessage());
            System.err.println(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Usage:_") + appTitle + " " + USAGE_STRING);
            System.exit(2);
        }
        
        boolean usage = ((Boolean) parser.getOptionValue(helpOption, Boolean.FALSE)).booleanValue();
        if (usage) {
            System.out.println(java.util.ResourceBundle.getBundle("resources/Bundle").getString("Usage:_") + appTitle + " " + USAGE_STRING);
            System.exit(0);
        }
        
        this.configFileName = (String) parser.getOptionValue(configOption, null);
        this.modelFileName = (String) parser.getOptionValue(modelOption, null);
        this.parameterValues = (String) parser.getOptionValue(pValueOption, null);
        this.otherArgs = parser.getRemainingArgs();
    }
    
    public String getConfigFileName() {
        return configFileName;
    }
    
    public String getModelFileName() {
        return modelFileName;
    }
    
    public String[] getOtherArgs() {
        return otherArgs;
    }
    
    public String getParameterValues() {
        return parameterValues;
    }
}
