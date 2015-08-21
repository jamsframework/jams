
package org.jams.j2k.s_n.wq.DissolvedOxygen;

import org.jams.j2k.s_n.wq.*;
import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "Reaeration Rate",
        author = "Marcel Wetzel",
        description = "calculates the contribution of reaeration rate for dissolved oxygen amount in reaches",
        version="1.0_0",
        date="2010-09-07"
        )

public class Reaeration extends JAMSComponent {

    /*
     *  Component variables
     */


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable wind",
            unit = "m/s",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble wind;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation",
            unit = "m",
            lowerBound= -300,
            upperBound = 10000
            )
            public JAMSDouble elevation;

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
            description = "mean water velocity for specific reach",
            unit = "m/s",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble velocity;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "dissolved oxygen",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_O2;
    
    /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "dissolved oxygen concentration in water body",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble disOxy;
     * 
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the oxygen saturation concentration",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble DOsat;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "contribution of reaeration rate for dissolved oxygen amount",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble RateReaer;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "contribution of reaeration rate for dissolved oxygen amount",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ReaerRate;
  
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
            

         // calculation of temperature and elevation depended oxygen saturation concentration
            // DOsatT the temperature depended oxygen saturation concentration (mg/l)
            // DOsatTE the temperature and elevation depended oxygen saturation concentration (mg/l)
            // T the mean water temperature of specific reach (°C)
            // elev the mean elavation of specific reach (m)


        double DOsatT,DOsatTE,T,elev = 0;
        T = watertempavg.getValue();
        elev = elevation.getValue();

        DOsatT = Math.log(14.652 - 0.41022 * T + 0.007991 * Math.pow(T,2) - 0.000077774 * Math.pow(T,3));
        DOsatTE = Math.exp(DOsatT) * (1 - 0.0001148 * elev);
        DOsat.setValue(DOsatTE);
        
        // hydraulic-based formulas to compute the reaeration coefficient at 20°C
            // Kah20 the reaeration rate at 20°C based on hydraulic charakteristics (1/d)
            // U the mean water velocity (m/s)
            // H the mean water depth (m)

        double Kah20,H,U,Klw,Uw10 = 0;
        H = waterdepth.getValue();
        U = velocity.getValue();

        if (H < 0.61){
           // Owens-Gibbs (Owens et al. 1964)
           Kah20 = 5.32 * (Math.pow(U, 0.67)/ Math.pow(H, 1.85));
        } else if (H > 3.45 * Math.pow(U, 2.5)) {
           // O'Connor-Dobbins (O'Connor and Dobbins 1958)
           Kah20 = 3.93 * (Math.pow(U, 0.5)/ Math.pow(H, 1.5));
        } else {
           // Churchill (Churchill et al. 1962)
           Kah20 = 5.026 * (U / Math.pow(H, 1.67));
        }

        // calculation of wind speed measured 10 meters above water surface
            // Uw10 the wind speed measured 10 meters above the water surface (m/s)
        double z = 10;
        Uw10 = wind.getValue() / (4.2/(Math.log(z) + 3.5));
        
        // wind-based formulas for computation of the wind effects into the reaeration coefficient
            // Klw the reaeration mass-transfer coefficient based on wind velocity (m/d)

        // Banks-Herrera formula (Banks 1975, Banks and Herrera 1977)        
        Klw = 0.728 * Math.pow(Uw10, 0.5) - 0.317 * Uw10 + 0.0372 * Math.pow(Uw10, 2);
        
        // Wanninkhof formula (Wanninkhof 1991)
        // Klw = 0.0986 * Math.pow(Uw10, 1.64);


        // calculation of the reaeration coefficient at 20°C
            // Ka20 the reaeration coefficient at 20°C

        double Ka20 = 0;
        Ka20 = Kah20 + Klw / H;
        //Ka20 = Kah20;

         // calculation of the contribution of reaeration to the conversion rate of dissolved oxygen
            // Reaeration the daily reaeration rate (mg/l)
            // Ktemp the temperature-dependent oxygen reaeration coefficient (1/d)
            // k temperature constant (0.069 1/°C)
            // T the mean water temperature of specific reach (°C)
            // DOsatTE the temperature and elevation depended oxygen saturation concentration (mg/l)
            // DO the dissolved oxygen concentration in water body (mg/l)
            

            double Reaeration,KaTemp = 0;
            double k = 0.069;
            double DO = S_O2.getValue();
            KaTemp = Ka20 * Math.exp(k * (T - 20));
            KaTemp = Math.min(KaTemp,1);
            Reaeration = KaTemp * (DOsatTE - DO);
            RateReaer.setValue(Reaeration);
            ReaerRate.setValue(Reaeration);
            
            //System.out.println("RateReaeration: " + Reaeration);


    }

     public void cleanup() {

    }

}


    
