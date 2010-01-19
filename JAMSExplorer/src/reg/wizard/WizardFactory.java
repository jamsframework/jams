/*
 * WizardFactory.java
 * Created on 18. January 2010, 11:40
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
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
package reg.wizard;

import jams.tools.JAMSTools;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import reg.wizard.tlug.panels.BaseDataPanel;
import reg.wizard.tlug.panels.RegMethodPanel;

/**
 *
 * @author hbusch
 */
public class WizardFactory {

    /**
     * mapping of  wizardKey -> key(s) used in model
     **/
    private static final Map<String, String[]> KEY_MODEL_MAPPING = createMapping();

    private static Map<String, String[]> createMapping() {
        Map<String, String[]> resultMap = new HashMap<String, String[]>();

        resultMap.put(BaseDataPanel.KEY_INTERVAL, new String[]{"Interpolation.timeInterval"});
        resultMap.put(BaseDataPanel.KEY_REGIONALIZATION, new String[]{"Interpolation.inputDataStore"});
        resultMap.put(RegMethodPanel.KEY_SCHWELLENWERT,
                new String[]{"Regionaliser.rsqThreshold"
                });
        resultMap.put(RegMethodPanel.KEY_GEWICHTUNG,
                new String[]{"Weights.pidw"
                });
        resultMap.put(RegMethodPanel.KEY_STATION,
                new String[]{"Regionaliser.nidw"
                });

        return Collections.unmodifiableMap(resultMap);
    }

    /**
     * copy model and related files to workspace
     * @param sourceDir
     * @return name of model file or null
     * @throws IOException
     */
    public static String copyModelFiles(String sourceDir, String targetDir)
            throws IOException {

        String outputTargetDir = targetDir + File.separator + "output";
        deleteModelFiles(targetDir);
        deleteModelFiles(outputTargetDir);

        String modelSourceDir = sourceDir + File.separator + "workspace";
        File[] modelFiles = JAMSTools.getFiles(modelSourceDir, null);
        if (modelFiles == null || modelFiles.length == 0) {
            System.out.println("no model files found ind " + modelSourceDir);
        } else {
            File modelFile = modelFiles[0];


            String completeModelFileName = modelFile.getAbsolutePath();
            String modelFileName = modelFile.getName();
            String targetFileName = targetDir + File.separator + modelFileName;
            JAMSTools.copyFile(completeModelFileName, targetFileName);


            // copy output files
            String outputSourceDir = sourceDir + File.separator + "output";
            File[] outputFiles = JAMSTools.getFiles(outputSourceDir, "xml");
            if (outputFiles == null || outputFiles.length == 0) {
                System.out.println("no output files found in " + outputSourceDir);
            } else {
                for (File outputFile : outputFiles) {
                    String outputFileName = outputFile.getAbsolutePath();
                    targetFileName = outputTargetDir + File.separator + outputFile.getName();
                    System.out.println("outputFile file found: " + outputFileName);
                    JAMSTools.copyFile(outputFileName, targetFileName);
                }
            }
            return modelFileName;
        }
        return null;

    }

    /**
     * delete all xml files of directory
     * @param theDirectoryName
     * @throws IOException
     */
    public static void deleteModelFiles(String theDirectoryName)
            throws IOException {
        File[] modelFiles = JAMSTools.getFiles(theDirectoryName, "xml");
        JAMSTools.deleteFiles(modelFiles);
    }

    /**
     * convert wizardSettings into properties usable for models
     * @param wizardSettings
     * @return properties
     */
    public static Properties getModelPropertiesFromWizardResult(Map wizardSettings) {
        // put parameter to model
        Properties properties = new Properties();
        Set<String> wizardKeys = KEY_MODEL_MAPPING.keySet();
        for (String wizardKey : wizardKeys) {
            String value = (String) wizardSettings.get(wizardKey);
            String[] modelKeys = KEY_MODEL_MAPPING.get(wizardKey);
            for (String modelKey : modelKeys) {
                properties.put(modelKey, value);
            }
        }
        return properties;
    }
}
