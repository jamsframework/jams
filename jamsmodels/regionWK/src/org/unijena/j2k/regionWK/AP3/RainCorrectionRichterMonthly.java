package org.unijena.j2k.regionWK.AP3;

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



import org.unijena.j2k.regionWK.AP3.*;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="RainCorrectionRichterMonthly",
        author="Peter Krause",
        description="Applies correction according to RICHTER 1985 for measured monthly precip sums"
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
            description = "the correction factor"
            )
            public Attribute.StringArray corrFac;
    
    double NODATA = -9999;
    double[] richter3_b = {0.233, 0.245, 0.203, 0.151, 0.111, 0.098, 0.100, 0.095, 0.115, 0.127, 0.168, 0.198};
    double[] richter3_c = {0.173, 0.179, 0.155, 0.127, 0.101, 0.088, 0.091, 0.085, 0.102, 0.110, 0.133, 0.150};
    double[] richter3_a = {0.316, 0.335, 0.269, 0.183, 0.125, 0.104, 0.108, 0.105, 0.126, 0.155, 0.218, 0.265};
    double[] richter3_d = {0.115, 0.118, 0.107, 0.100, 0.086, 0.077, 0.080, 0.075, 0.087, 0.088, 0.095, 0.103};
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        /*
         *right at the moment the module assumes that all station are
         *in the same zone should be enhanced later if necessary
         */
       String[] corrFact = this.corrFac.getValue();
       double[] corrFactor = richter3_d;

        double[] precip = this.inPrecip.getValue();
        double[] rcorr = new double[precip.length];
        int month = time.get(time.MONTH);

       for(int i = 0; i < rcorr.length; i++){

        if (corrFact[i].equals("a")){
            corrFactor = richter3_a;
        }
       if (corrFact[i].equals("b")){
            corrFactor = richter3_b;
        }
       if (corrFact[i].equals("c")){
           corrFactor = richter3_c;
        }
       if (corrFact[i].equals("d")){
            corrFactor = richter3_d;
        }
       }

       
      
        for(int i = 0; i < rcorr.length; i++){
            if(precip[i] == -9999){
                rcorr[i] = -9999;
            }else{
                //Applying the correction factors
                rcorr[i] = precip[i] + (precip[i] * corrFactor[month]);
            }
        }
        this.corrPrecip.setValue(rcorr);
        
    }
    
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {
        
    }
}
