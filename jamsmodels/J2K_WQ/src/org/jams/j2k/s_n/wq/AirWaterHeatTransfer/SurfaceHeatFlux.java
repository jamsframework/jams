

package org.jams.j2k.s_n.wq.AirWaterHeatTransfer;

import java.io.*;
import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title = "SurfaceHeatFlux",
        author = "Marcel Wetzel",
        description = "Calculates Energy Amount of Surface Heat Flux components for Reaches",
        version="1.0_0",
        date="2010-12-12"
        )

public class SurfaceHeatFlux extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean temperature",
            unit = "°C",
            lowerBound= -70,
            upperBound = 70
            )
            public JAMSDouble tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable wind",
            unit = "m/s",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble wind;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable relative humidity",
            unit = "%",
            lowerBound= 0,
            upperBound = 100
            )
            public JAMSDouble rhum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable net radiation",
            unit = "MJ/m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble netRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "attribute elevation",
            unit = "m",
            lowerBound= -300,
            upperBound = 10000
            )
            public JAMSDouble elevation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit = "m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble area;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "potential ET",
            unit = "mm",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble potET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "potential ET",
            unit = "mm",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble potET_Reach;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "average water temperature for specific reach",
            unit = "°C",
            lowerBound= 0,
            upperBound = 100
            )
            public JAMSDouble watertempavg;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "potET energy amount of specific reach",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble potETenergy;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable sunshine hours",
            unit = "h/d",
            lowerBound= 0,
            upperBound = 24
            )
            public JAMSDouble sunh;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Maximum sunshine duration",
            unit = "h/d",
            lowerBound= 0,
            upperBound = 24
            )
            public JAMSDouble sunhmax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "energy amount of atmospheric longwave radiation",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble atloRadEnergy;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable albedo",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble albedo;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation",
            unit = "MJ/m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble solRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable net solar radiation",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Rns;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "back radiation from the water surface",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble waterbr;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "heat transfer due to conduction and convection",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble coco;
  
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    public void run() throws JAMSEntity.NoSuchAttributeException, IOException {
            

         // calculation of potET energy amount
            // PET the potET of specific reach [liter]
            // T the water temperature of specific reach [°C]
            // m   the molar mass of water [g/mol]
            // Hv  the molar evaporation enthalpy [KJ/mol]
            // sHv the specific evaporation enthalpy [KJ/g]
            // energy the energy amount of reach potET [KJ]
            // energyKcal the energy amount of reach potET [Kcal]

            double PET,T,Hv,sHv,energy,energyKcal = 0;
        
            PET = potET.getValue();
            potET_Reach.setValue(PET);
            T = watertempavg.getValue();
            double m = 18.0153;
            Hv = 50.09 - (0.9298 * (T/1000)) - (65.19 * Math.pow((T/1000), 2));
            sHv = Hv/m;
            energy = (PET * 1000) * sHv;
            energyKcal = (energy * 0.23892);

            potETenergy.setValue(energyKcal);


         // calculation of net atmospheric longwave radiation energy amount
            double temperature,relHum,S,S0,abs_temp,est,ea,Eclear,Esky,atloRad,netSWRadiation = 0;
            double boltzmann = 1.17e-7;
            double longwaveRefl = 0.03;
            temperature = tmean.getValue();
            relHum = rhum.getValue();
            S = sunh.getValue();
            S0 = sunhmax.getValue();
            
            abs_temp = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_absTemp(temperature, "degC");
            est = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_saturationVapourPressure(temperature);
            ea = org.unijena.j2k.physicalCalculations.ClimatologicalVariables.calc_vapourPressure(relHum, est);

            Eclear = 1.24 * Math.pow((ea/abs_temp), (1/7));
            Esky = Eclear * (1 + (0.17 * Math.pow((S/S0), 2)));

            // downward flux of net atmospheric longwave radiation in cal/(cm^2*d)
            atloRad = boltzmann * Math.pow(abs_temp, 4) * Esky * (1 - longwaveRefl);

            // downward flux of net atmospheric longwave radiation in Kcal/(m^2*d)
            atloRad = atloRad * 10;
            atloRadEnergy.setValue(atloRad);

            // netSWRadiation the daily net shortwave radiation resulting from the balance between incoming
            // and reflected solar radiation in MJ / m^2 day or hour]
            double sR = solRad.getValue();
            double alb = albedo.getValue();
            netSWRadiation = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_NetShortwaveRadiation(alb, sR);

            // Rns the daily net shortwave radiation in Kcal / m^2 day or hour]
            // Umrechnung MJ --> Kcal: netSWRadiation * 1000000 (in J) * 0.23892 / 1000 (in Kcal) --> netSWRadiation * 238.92
            netSWRadiation = netSWRadiation * 238.92;
            Rns.setValue(netSWRadiation);

            //System.out.println("netSWRadiation: " + netSWRadiation);

         // calculation of back radiation from the water surface in cal/(cm^2*d)
            // boltzmann (1.17e-7)
            // e the emissivity of water (0.97)
            // T the absolute water temperature [K]
            double br = 0;
            double e = 0.97;
            br = e * boltzmann * Math.pow(T + 273, 4);
            
            // calculation of back radiation from the water surface in Kcal/(m^2*d)
            br = br * 10;
            waterbr.setValue(br);

         // calculation of heat transfer due to conduction and convection
            // c Bowen's coefficient
            // w heat transfer due to wind velocity over the water surface
            // vz the wind speed at a height of z (7 m)
            // coco the heat transfer due to conduction and convection in Kcal/(m^2*d)
            double c = 0.47;
            double w = 0;
            double vz = 0;
            double z = 7;
            double condconv = 0;
            vz = wind.getValue() / (4.2/(Math.log(z) + 3.5));
            w = 19 + (0.95 * Math.pow(vz, 2));

            // calculation of heat transfer due to conduction and convection in cal/(cm^2*d)
            condconv = c * w * (T - temperature);
            // calculation of heat transfer due to conduction and convection in Kcal/(m^2*d)
            condconv = condconv * 10;
            coco.setValue(condconv);

    }

     public void cleanup() {

    }

}


    
