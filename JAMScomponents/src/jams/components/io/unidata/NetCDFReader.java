/*
 * NetCDFReader.java
 * Created on 17.01.2023, 22:04:20
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package jams.components.io.unidata;

import jams.JAMS;
import jams.data.*;
import jams.model.*;
import jams.tools.FileTools;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
        title = "NetCDFReader",
        author = "Sven Kralisch",
        description = "Reader for NetCDF files",
        date = "2023-01-29",
        version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class NetCDFReader extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
    )
    public Attribute.Calendar currentTime;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current entity"
    )
    public Attribute.Entity currentEntity;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NetCDF file name"
    )
    public Attribute.String fileName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "name of temporal dimension"
    )
    public Attribute.String timeDimName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "name of spatial dimension"
    )
    public Attribute.String spaceDimName;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Date from which time units are counted. If not set, the value will be extracted from the NetCDF."
    )
    public Attribute.Calendar baseDate;   

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Apply data chaching? Will read data of all entities "
            + "on changing time steps in one go. Not meaningful if "
            + "spatial iteration is outside temporal iteration.",
            defaultValue = "false"
    )
    public Attribute.Boolean dataCaching;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "names of variables to read"
    )
    public Attribute.String[] varNames;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "output attributes"
    )
    public Attribute.Double[] values;

    NetcdfFile ncfile;
    List<Variable> vars = new ArrayList();
    Dimension timeDim, spaceDim;
    Map<Long, Integer> timeMap = new HashMap();
    Map<Long, Integer> spaceMap = new HashMap();
    Array[] dataArray;
    double[] missingDataValues;
    int[] spaceIndices, timeIndices;
    long oldMillis = -1;
    int tIndex, sIndex;
    Runnable runner;


    /*
     *  Component run stages
     */
    @Override
    public void init() {

        if (values == null || varNames == null || values.length != varNames.length) {
            getModel().getRuntime().sendHalt("Output values and input var names must be defined and must have same number!");
            return;
        }

        dataArray = new Array[values.length];
        missingDataValues = new double[values.length];
        spaceIndices = new int[values.length];
        timeIndices = new int[values.length];

        try {
            
            //read hru parameter
            String fileName_ = fileName.getValue();
            if (!new File(fileName_).exists() && getModel().getWorkspaceDirectory() != null) {
                fileName_ = FileTools.createAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), fileName_);
            }

            if (!new File(fileName_).exists()) {
                getModel().getRuntime().sendErrorMsg("Couldn't load NetCDF file " + fileName + "!\nIf you are not using an absolute path, "
                        + "please ensure you have defined a workspace directory!");
            }            
            
            ncfile = NetcdfFiles.open(fileName_);

            if (timeDimName != null && spaceDimName != null) {
                timeDim = ncfile.findDimension(timeDimName.getValue());
                spaceDim = ncfile.findDimension(spaceDimName.getValue());
            }

            if (timeDim == null || spaceDim == null) {
                List<Dimension> dimensions = ncfile.getDimensions();
                String error = "Please choose one of the following dimensions:";
                for (Dimension dimension : dimensions) {
                    String unit = ncfile.findVariable(dimension.getName()).getUnitsString();
                    error += "\nDimension: " + dimension + " [" + unit + "]";
                }
                getModel().getRuntime().sendHalt("Wrong dimension name. " + error);
                return;
            }

            Variable timeVar = ncfile.findVariable(timeDim.getShortName());
            Array timeValues = timeVar.read();
            Variable spaceVar = ncfile.findVariable(spaceDim.getShortName());
            Array spaceValues = spaceVar.read();
            
            if (baseDate == null) {           
                String units = timeVar.getUnitsString();
                String[] splitUnits = units.split("since ");
                String baseDateString = splitUnits[1];
                baseDate = getModel().getRuntime().getDataFactory().createCalendar();
                baseDate.setValue(baseDateString);            
            }            

            long baseMillis = baseDate.getTimeInMillis();
            for (int i = 0; i < timeValues.getSize(); i++) {
                long millis = Math.round(timeValues.getDouble(i) * 24 * 60 * 60 * 1000);
                timeMap.put(baseMillis + millis, i);
            }

            for (int i = 0; i < spaceValues.getSize(); i++) {
                spaceMap.put(spaceValues.getLong(i), i);
            }

            if (varNames == null) {
                String error = "Please choose one or more of the following variables:";
                List<Variable> allVars = ncfile.getVariables();
                for (Variable variable : allVars) {
                    error += "\nVariable: " + variable.getFullName() + " (Dimensions: " + variable.getDimensionsString() + ")";
                }
                getModel().getRuntime().sendHalt("Wrong variable name. " + error);

                return;
            }

            int i = 0;
            for (Attribute.String varName : varNames) {
                Variable var = ncfile.findVariable(varName.getValue());
                if (var == null) {
                    String error = "Please choose one or more of the following variables:";
                    List<Variable> allVars = ncfile.getVariables();
                    for (Variable variable : allVars) {
                        error += "\nVariable: " + variable.getFullName() + " (Dimensions: " + variable.getDimensionsString() + ")";
                    }
                    getModel().getRuntime().sendHalt("Wrong variable name. " + error);

                    return;
                }

                vars.add(var);
                
                List<Dimension> dimensions = var.getDimensions();
                int j = 0;
                for (Dimension dimension : dimensions) {
                    if (dimension == timeDim) {
                        timeIndices[i] = j;
                    } else if (dimension == spaceDim) {
                        spaceIndices[i] = j;
                    }
                    j++;
                }

                if (dataCaching.getValue()) {
                    shape[spaceIndices[i]] = spaceDim.getLength();
                }

                ucar.nc2.Attribute fillValue = var.findAttribute("_FillValue");
                if (fillValue != null) {
                    missingDataValues[i] = fillValue.getNumericValue().doubleValue();
                }

                i++;
            }

            if (dataCaching.getValue()) {
                runner = run_cached;
            } else {
                runner = run_normal;
            }

        } catch (FileNotFoundException ex) {
            getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
        } catch (IOException ex) {
            getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
        }
    }

    @Override
    public void run() throws IOException, InvalidRangeException {

        runner.run();

    }

    int[] origin = {0, 0};
    int[] shape = {1, 1};

    Runnable run_normal = new Runnable() {
        @Override
        public void run() {
            long millis = currentTime.getTimeInMillis();

            tIndex = timeMap.get(currentTime.getTimeInMillis());
            sIndex = spaceMap.get(currentEntity.getId());
           
            for (int i = 0; i < vars.size(); i++) {
                double value = 0;

                origin[spaceIndices[i]] = sIndex;
                origin[timeIndices[i]] = tIndex;

                try {
                    value = vars.get(i).read(origin, shape).getDouble(0);
                } catch (IOException ex) {
                    getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
                } catch (InvalidRangeException ex) {
                    getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
                }
                if (value == missingDataValues[i]) {
                    value = JAMS.getMissingDataValue();
                }
                values[i].setValue(value);
            }
        }
    };

    Runnable run_cached = new Runnable() {
        @Override
        public void run() {
            long millis = currentTime.getTimeInMillis();
            if (millis != oldMillis) {
                oldMillis = millis;
                tIndex = timeMap.get(currentTime.getTimeInMillis());
                
                for (int i = 0; i < vars.size(); i++) {

                    origin[timeIndices[i]] = tIndex;

                    try {
                        dataArray[i] = vars.get(i).read(origin, shape);
                    } catch (IOException ex) {
                        getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
                    } catch (InvalidRangeException ex) {
                        getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
                    }
                }
            }

            sIndex = spaceMap.get(currentEntity.getId());

            for (int i = 0; i < vars.size(); i++) {
                double value = dataArray[i].getDouble(sIndex);
                if (value == missingDataValues[i]) {
                    value = JAMS.getMissingDataValue();
                }
                values[i].setValue(value);
            }
        }
    };

    @Override
    public void cleanup() {
    }

}
