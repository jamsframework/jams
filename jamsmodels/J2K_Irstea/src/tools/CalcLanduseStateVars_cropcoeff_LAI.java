/*
 * CalcLanduseStateVars_cropcoeff.java
 * Created on 17. April 2012 after CalcLanduseStateVars.java by P. Krause
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
package tools;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="CalcLanduseStateVars_cropcoeff",
        author="Peter Krause + Francois Tilmant",
        description="Calculates landuse state variables for a modelling unit"
        + "For evapotranspiration calculation using crop coeff."
        + "The calculation is done for a standard year (i.e. 366 days or 8784 hours)."
        + "The module can be used in hourly, daily and monthly resolution."
        + "FT : change the approach of LAI computation. We take the same as Kc",
        version="1.0_1",
        date="2011-05-30 + 2013-08-07"
        )
        public class CalcLanduseStateVars_cropcoeff_LAI extends JAMSComponent {
    
    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current spatial entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Array with LAI values for a standard year"
            )
            public Attribute.DoubleArray LAIArray;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Array with eff. Height values for a standard year"
            )
            public JAMSDoubleArray effHArray;
            * 
            */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Monthly crop coefficient values",
            lowerBound = 0,
            upperBound = 2,
            unit = "-"
            )
            public Attribute.DoubleArray cropcoeffArray;
      
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temporal resolution [d | h | m]"
            )
            public Attribute.String tempRes;
    
    
    int[] monthMean = {15,45,74,105,135,166,196,227,258,288,319,349};
    int oldmonth = 0;
    double crop = 0;
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
      
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        
        double[] lai_vals = null; 
       // double[] effH_vals = null;
        if(this.tempRes == null || this.tempRes.getValue().equals("d") || this.tempRes.getValue().equals("h") || this.tempRes.getValue().equals("m")){
            lai_vals = new double[12];
            //effH_vals = new double[366];
        }
         
        String laiName = "LAI_";
        for(int j = 0; j < 12; j++){
            int count = j+1;
            String LAIloopName = laiName + count;
            lai_vals[j] = entity.getDouble(LAIloopName);
            }
        
        
        LAIArray.setValue(lai_vals);
        //effHArray.setValue(effH_vals);
        
        double[] cropcoeff = new double[12];
        String cropcoeffName = "Kc_";
        for(int i = 0; i < 12; i++){
            int count = i+1;
            String loopName = cropcoeffName + count;
            cropcoeff[i] = entity.getDouble(loopName);
        }
        cropcoeffArray.setValue(cropcoeff);

    }
    
    public void cleanup() {
        
    }
    
     
}

 
