
package org.jams.j2k.s_n.wq.SedimentWaterHeatTransfer;

import org.jams.j2k.s_n.wq.*;
import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "CalcSedimentWaterHeatFlux",
        author = "Marcel Wetzel",
        description = "Calculates the heat flux from the sediment to the water",
        version="1.0_0",
        date="2010-12-12"
        )

public class SedimentWaterHeatFlux extends JAMSComponent {

    /*
     *  Component variables
     */


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "average water temperature for specific reach",
            unit = "°C",
            lowerBound= 0,
            upperBound = 100
            )
            public JAMSDouble watertempavg;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "mean water depth for specific reach",
            unit = "m",
            lowerBound= 0,
            upperBound = 100
            )
            public JAMSDouble waterdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "temperature of the bottom sediment for specific reach",
            unit = "°C",
            lowerBound= 0,
            upperBound = 100
            )
            public JAMSDouble bottomsedtemp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the sediment water heat flux",
            unit = "cal/(cm^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble sedheat1;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the sediment water heat flux",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble sedheat2;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "temperature of the bottom sediment for specific reach",
            unit = "°C",
            lowerBound= 0,
            upperBound = 100
            )
            public JAMSDouble sedtemp;
  
  
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {

            // Hwater the water level in specific reach in mm
            double Hwater = waterdepth.getValue() * 100;    
            double T = watertempavg.getValue();
            double Tsed = bottomsedtemp.getValue();

            // values suggested from Q2K manual (Chapra et al. 2006)
            double deltaSedT = 0;
            double SedT = 0;
            double p = 1.6;
            double Cps = 0.4;
            double Hsed = 10;
            double a = 0.005;
            double Jsed1 = 0;
            double Jsed2 = 0;
            
            // energy amounts in water and sediment pools
            double EW1 = T * Hwater;
            double ES1 = p * Cps * Tsed * Hsed;
            
            double E_gesamt = EW1 + ES1;
            double T_gesamt = E_gesamt / (Hwater + (Hsed * p * Cps));
            double E_max = (T_gesamt - T) * Hwater;
            
            
         // calculation of the heat flux from the sediment to the water
            // p the density of the sediment (g/cm^3)
            // Cps  the specific heat of the sediment (cal/(g°C))
            // Hsed the effective thickness of the sediment layer (cm)
            // a the sediment thermal diffusivity (cm^2/s)
            // Tsed the temperature of the bottom sediment (°C)
            // T the water temperature of specific reach (°C)
           
            // Jsed1 the flux from the sediment to the water in cal/ (cm^2*d)
            
            Jsed1 = p * Cps * (a / (Hsed/2)) * (Tsed - T) * 86400;

              if (Jsed1 < 0){

                Jsed1 = Math.max(Jsed1, E_max);
                }
                else{
                Jsed1 = Math.min(Jsed1, E_max);
              }
            sedheat1.setValue(Jsed1);
            // Jsed2 the flux from the sediment to the water in Kcal/ (m^2*d)
            Jsed2 = Jsed1 * 10;
            sedheat2.setValue(Jsed2);

            // calculation of a heat balance for bottom sediment underlying a specific reach
            // p the density of the sediment (g/cm^3)
            // Cps  the specific heat of the sediment (cal/(g°C))
            // Hsed the effective thickness of the sediment layer (cm)
            // sh the sediment water heat flux in cal/(cm^2 * d)
            // deltaSedT the timestep change (delta) of the sediment temperature in °C
            deltaSedT = (-1) * (Jsed1 / (p * Cps * Hsed));
            SedT = bottomsedtemp.getValue() + deltaSedT;
            bottomsedtemp.setValue(SedT);
            sedtemp.setValue(SedT);

            //sedheat1.setValue(0);

    }

     public void cleanup() {

    }

}


    
