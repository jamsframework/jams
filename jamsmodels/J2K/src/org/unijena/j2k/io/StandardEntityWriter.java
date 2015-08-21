/*
 * StandardEntityWriter.java
 * Created on 15. Febuary 2006, 11:05
 *
 * This file is part of JAMS
 * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.unijena.j2k.io;

import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;
import jams.io.*;

/**
 *
 * @author S. Kralisch
 */
public class StandardEntityWriter extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "EntitySet")
    public Attribute.EntityCollection entities;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Output file name")
    public Attribute.String fileName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Output file header descriptions")
    public Attribute.String header;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "Output file attribute names")
    public Attribute.String attributeName;

    @JAMSVarDescription (access = JAMSVarDescription.AccessType.READ,
                         description = "entity attribute name for weight [attName | none]")
    public Attribute.String weight;

    private GenericDataWriter writer;

    private boolean headerWritten;
    /*
     *  Component runstages
     */

    public void init() {
        writer = new GenericDataWriter(JAMSTools.CreateAbsoluteFileName(getModel().getWorkspaceDirectory().getPath(), fileName.getValue()));

        writer.addComment("J2K model output" + header.getValue());

        writer.addComment("");


    }

    public void run() {

        EntityEnumerator ee = entities.getEntityEnumerator();

        if (!this.headerWritten) {
            //always write time
            writer.addColumn("date/time");
            Object ob = entities.getCurrent().getObject(this.attributeName.getValue());
            int length = 0;
            if (ob.getClass().getName().contains("DoubleArray")) {
                //System.out.getRuntime().println("JAMSArray");
                length = ((Attribute.DoubleArray) entities.getCurrent().getObject(this.attributeName.getValue())).getValue().length;
            } else {
                //System.out.getRuntime().println("Primitive");
            }

            ee.reset();
            boolean cont = true;
            while (cont) {
                for (int i = 0; i < length; i++) {
                    writer.addColumn("HRU_" + (int) entities.getCurrent().getId() + "[" + i + "]");
                }
                if (length == 0) {
                    writer.addColumn("HRU_" + (int) entities.getCurrent().getId());
                }
                if (ee.hasNext()) {
                    ee.next();
                    cont = true;
                } else {
                    cont = false;
                }
            }

            writer.writeHeader();
            this.headerWritten = true;
        }
        //always write time
        //the time also knows a toString() method with additional formatting parameters
        //e.g. time.toString("%1$tY-%1$tm-%1$td %1$tH:%1$tM")
        writer.addData(time);

        ee.reset();
        boolean cont = true;
        while (cont) {
            Object ob = entities.getCurrent().getObject(this.attributeName.getValue());
            if (ob.getClass().getName().contains("DoubleArray")) {
                //System.out.getRuntime().println("HRUNo: " +((Attribute.Double)entitySet.getCurrent().getObject("ID")).getValue());
                double[] da = ((Attribute.DoubleArray) entities.getCurrent().getObject(this.attributeName.getValue())).getValue();
                for (int i = 0; i < da.length; i++) {
                    double val = 0;
                    if (this.weight.getValue().equals("none")) {
                        val = da[i];
                    } else {
                        double weight = (((Attribute.Double) entities.getCurrent().getObject(this.weight.getValue())).getValue());
                        val = da[i] / weight;
                    }
                    writer.addData("" + val);
                }
            } else {
                //System.out.getRuntime().println("Primitive");
                double val = 0;
                double da = ((Attribute.Double) entities.getCurrent().getObject(this.attributeName.getValue())).getValue();
                if (this.weight.getValue().equals("none")) {
                    val = da;
                } else {
                    double weight = (((Attribute.Double) entities.getCurrent().getObject(this.weight.getValue())).getValue());
                    val = da / weight;
                }
                writer.addData("" + val);
            }
            //writer.addData(""+entitySet.getCurrent().getDouble(this.attributeName.getValue()));
            if (ee.hasNext()) {
                ee.next();
                cont = true;
            } else {
                cont = false;
            }
        }

        try {

            writer.writeData();

        } catch (jams.runtime.RuntimeException jre) {
            getModel().getRuntime().handle(jre);
        }
    }

    public void cleanup() {

        writer.close();
    }
}
