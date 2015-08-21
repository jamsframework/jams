/*
 * JAMSDataWriter.java
 * Created on 8. September 2008, 14:55
 *
 * This file is a JAMS component
 * Copyright (C) FSU Jena
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
package jams.components.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import jams.data.JAMSData;
import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(title = "JAMSData Writer",
author = "Sven Kralisch",
description = "Write values of arbitrary JAMSData objects to file.")
public class JAMSDataWriter extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Name of output file")
    public Attribute.String outputFile;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Header of output file (i.e. attribute names)")
    public Attribute.StringArray header;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Attributes to be written")
    public JAMSData[] attributes;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Comment")
    public Attribute.String comment;

    public void cleanup() {

        BufferedWriter writer;

        try {
            writer = new BufferedWriter(new FileWriter(getModel().getWorkspaceDirectory().getPath() + File.separator + outputFile.getValue()));

            writer.write("#JAMSDataWriter output file");
            writer.newLine();

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            writer.write("#Date: " + sdf.format(cal.getTime()));
            writer.newLine();
            if (comment != null) {
                writer.write("#Comment: " + comment.getValue());
                writer.newLine();
            }

            for (String h : header.getValue()) {
                writer.write(h + "\t");
            }

            writer.newLine();

            for (JAMSData attribute : attributes) {
                writer.write(attribute.toString() + "\t");
            }

            writer.close();

        } catch (IOException ioe) {
            getModel().getRuntime().handle(ioe);
        }
    }
}
