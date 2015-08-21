

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="J2KHRUTempWQ",
        author="Marcel Wetzel",
        description="Modul for the calculation of HRU runoff components water temperatures",
        version="1.0_0",
        date="2010-12-12"
        )
        public class J2KHRUTempWQ extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU statevar RD1 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRD1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU statevar RD2 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDoubleArray outRD2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRG1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRG2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil temperature in different layerdepths",
            unit = "°C",
            lowerBound= Double.NEGATIVE_INFINITY,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDoubleArray Soil_Temp_Layer;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil surface temperature",
            unit = "°C",
            lowerBound= Double.NEGATIVE_INFINITY,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Surfacetemp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "mean temperature of the simulation period",
            unit = "°C",
            lowerBound= Double.NEGATIVE_INFINITY,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble tmeanavg;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of RD1",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble cal_soil_rd1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of RD2",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble cal_soil_rd2;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of RG1",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble cal_gw_rg1;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of RG2",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble cal_gw_rg2;

    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        double rd1_out = outRD1.getValue();
        double[] rd2_out = outRD2.getValue();
        double rg1_out = outRG1.getValue();
        double rg2_out = outRG2.getValue();
        double surf_temp = Surfacetemp.getValue();
        double[] soil_temp = Soil_Temp_Layer.getValue();
        double meantemp_avg = tmeanavg.getValue();
        
        double cal_soilrd1 = 0;
        double cal_soilrd2 = 0;
        double cal_gwrg1 = 0;
        double cal_gwrg2 = 0;
        double gw1_temp = 0;

        cal_soilrd1 = surf_temp * rd1_out;

        int n =  soil_temp.length;
        double soillow_temp = soil_temp[n-1];

        int i = 0;
        while (i < n){
              cal_soilrd2 = cal_soilrd2 + (soil_temp[i] * rd2_out[i]);
              i=i+1;
        }

        gw1_temp = (meantemp_avg + soillow_temp) / 2;
        cal_gwrg1 = gw1_temp * rg1_out;

        cal_gwrg2 = meantemp_avg * rg2_out;
        
        cal_soil_rd1.setValue(cal_soilrd1);
        cal_soil_rd2.setValue(cal_soilrd2);
        cal_gw_rg1.setValue(cal_gwrg1);
        cal_gw_rg2.setValue(cal_gwrg2);
        
    }
    
    public void cleanup() {
        
    }

}