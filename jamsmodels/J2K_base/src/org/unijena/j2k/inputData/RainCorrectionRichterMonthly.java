/*
 * RainCorrectionRichterMonthly.java
 * Created on 24. November 2005, 09:48
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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

package org.unijena.j2k.inputData;

import jams.JAMS;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="RainCorrectionRichterMonthly",
        author="Peter Krause",
        description="Corrects measured precipitation values with correction" +
        "factors according to Richter 1995, table X, page Y"
        )
        public class RainCorrectionRichterMonthly extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the uncorrected precip values"
            )
            public Attribute.DoubleArray inPrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the corrected precip values"
            )
            public Attribute.DoubleArray corrPrecip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the geographical zone according to Richter 1995"
            )
            public Attribute.Integer precipZone;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precipitation adjustment factor"
            )
            public Attribute.Double precipAdj;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Output file name"
            )
            public Attribute.String outFileName;
    
    
    double[] richter3_b = {0.233, 0.245, 0.203, 0.151, 0.111, 0.098, 0.100, 0.095, 0.115, 0.127, 0.168, 0.198};
    double[] richter3_c = {0.173, 0.179, 0.155, 0.127, 0.101, 0.088, 0.091, 0.085, 0.102, 0.110, 0.133, 0.150};
    double[] richter5_b = {0.233, 0.245, 0.203, 0.151, 0.111, 0.098, 0.100, 0.095, 0.115, 0.127, 0.168, 0.198};
    double[] richter5_c = {0.173, 0.179, 0.155, 0.127, 0.101, 0.088, 0.091, 0.085, 0.102, 0.110, 0.133, 0.150};
    
    double adj = 1;
    
    private GenericDataWriter writer;
    private boolean headerWritten;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        adj = this.precipAdj.getValue();
        if(adj == 0)
            adj = 1;
        
        if (this.outFileName != null) {
            writer = new GenericDataWriter(outFileName.getValue());
        }
    }
    
    public void run() {
        double[] corrFactor = null;
        if (this.outFileName != null) {
            int nstat = inPrecip.getValue().length;
            if (!this.headerWritten) {
                //always write time
                writer.addColumn("date/time");
                for (int i = 0; i < nstat; i++) {
                    writer.addColumn("stat" + (i + 1));
                }
                writer.writeHeader();
                this.headerWritten = true;
            }
        }
        
        if(this.precipZone.getValue() == 3)
            corrFactor = richter3_c;
        double[] precip = this.inPrecip.getValue();
        double[] rcorr = new double[precip.length];
        int month = time.get(Attribute.Calendar.MONTH);
        for(int i = 0; i < rcorr.length; i++){
            if(precip[i] == JAMS.getMissingDataValue()){
                rcorr[i] = JAMS.getMissingDataValue();
            }else{
                //Applying the correction factors
                rcorr[i] = precip[i] + (precip[i] * (corrFactor[month] * adj));
            }
            
        }
        this.corrPrecip.setValue(rcorr);
        
        if (this.outFileName != null) {
            writer.addData(time);
            for (int i = 0; i < rcorr.length; i++) {
                writer.addData(rcorr[i]);
            }
            try {
                writer.writeData();
            } catch (jams.runtime.RuntimeException jre) {
                this.getModel().getRuntime().handle(jre);
            }
        }
        
    }
    
    public void cleanup() {
        if (this.outFileName != null) {
            writer.write("#eof");
            writer.close();
        }
    }
}
