/*
 * EntitiesFromNetCDF.java
 * Created on 21.09.2023, 22:47:00
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
import java.util.List;
import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 *
 * @author Sven Kralisch <sven at kralisch.com>
 */
@JAMSComponentDescription(
    title="EntitiesFromNetCDF",
    author="Sven Kralisch",
    description = "Extract an entity collection object from a NetCDF file",
    date = "2023-09-21",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version")
})
public class EntitiesFromNetCDF extends JAMSComponent {

    /*
     *  Component attributes
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "NetCDF file name"
    )
    public Attribute.String fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "name of spatial dimension"
    )
    public Attribute.String spaceDimName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "The spatial units covered by the NetCDF file")
    public Attribute.EntityCollection entities;


    
    /*
     *  Component run stages
     */
    @Override
    public void init() {

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
            
            NetcdfFile ncfile = NetcdfFiles.open(fileName_);
            Dimension spaceDim = null;
 
            if (spaceDimName != null) {
                spaceDim = ncfile.findDimension(spaceDimName.getValue());
            }

            if (spaceDim == null) {
                List<Dimension> dimensions = ncfile.getDimensions();
                String error = "Please choose one of the following dimensions:";
                for (Dimension dimension : dimensions) {
                    String unit = ncfile.findVariable(dimension.getName()).getUnitsString();
                    error += "\nDimension: " + dimension + " [" + unit + "]";
                }
                getModel().getRuntime().sendHalt("Wrong dimension name. " + error);
                return;
            }

            Variable spaceVar = ncfile.findVariable(spaceDim.getShortName());
                        
//            Array spaceValues = spaceVar.read();
//            long baseMillis = baseDate.getTimeInMillis();
//            long startMillis = Math.round(timeValues.getDouble(0) * baseUnit * 60 * 60 * 1000);
//            long endMillis = Math.round(timeValues.getDouble((int) timeValues.getSize()-1) * baseUnit * 60 * 60 * 1000);
//            
//            Attribute.Calendar startDate = getModel().getRuntime().getDataFactory().createCalendar();
//            Attribute.Calendar endDate = getModel().getRuntime().getDataFactory().createCalendar();
//            
//            startDate.setTimeInMillis(baseMillis + startMillis);
//            endDate.setTimeInMillis(baseMillis + endMillis);
//            
//            for (Attribute.TimeInterval timeInterval : timeIntervals) {
//                timeInterval.setStart(startDate);
//                timeInterval.setEnd(endDate);
//                timeInterval.setTimeUnit(timeUnit.getValue());
//                timeInterval.setTimeUnitCount(timeUnitCount.getValue());
//            }
//            
//            getModel().getRuntime().println("Time interval identified from NetCDF: " + timeIntervals[0].toString(), JAMS.VERBOSE);

        } catch (FileNotFoundException ex) {
            getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
        } catch (IOException ex) {
            getModel().getRuntime().sendHalt("Error reading NetCDF file " + fileName.getValue() + "\n" + ex);
        }

    }

}
