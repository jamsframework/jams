/*
 * JUICECmdLine.java
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

package org.unijena.juice;

import jams.CmdLineParser;

/**
 *
 * @author Sven Kralisch
 */
public class JUICECmdLine {
    
    private String configFileName = null;
    private String modelFileName = null;
    private String parameterValues = null;
    private String[] otherArgs = null;
    private String appTitle;
    public static final String USAGE_STRING = "[Options]\nOptions:\n" +
            "  -h, --help                                         Print help\n" +
            "  -c, --config <config file name>                    Provide config file name\n" +
            "  -m, --model <model definition file name>           Provide model file name\n" +
            "  -p, --parametervalue <list of parameter values>    Provide initial parameter values divided by semicolons";
    
    public JUICECmdLine(String [] args) {
        this(args, "JUICE");
    }
    
    public JUICECmdLine(String [] args, String appTitle) {
        
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
            System.err.println(USAGE_STRING);
            System.exit(2);
        }
        
        boolean usage = ((Boolean) parser.getOptionValue(helpOption, Boolean.FALSE)).booleanValue();
        if (usage) {
            System.out.println("Usage: " + appTitle + " " + USAGE_STRING);
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
