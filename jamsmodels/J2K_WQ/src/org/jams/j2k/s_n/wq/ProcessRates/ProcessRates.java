

package org.jams.j2k.s_n.wq.ProcessRates;

import java.io.*;
import jams.data.*;
import jams.model.*;
import java.util.HashMap;
import org.jams.j2k.s_n.wq.ProcessRates.Table2D.NoSuchEntryException;


@JAMSComponentDescription(
        title = "ProcessRates",
        author = "Marcel Wetzel",
        description = "Calculates Growth, Respiration and Death Rates for Heterotrophs, Nitrifiers, Algae and Hydrolysis",
        version="1.0_0",
        date="2011-03-03"
        )

public class ProcessRates extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Channel storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble channelStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1_N (SurfaceN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD1_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2_N ((fast) InterflowN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD2_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1_N ((slow) InterflowN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG1_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2_N (GoundwaterN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG2_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach Channel storage N",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ChannelStorage_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Simulated N Runoff",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SimRunoff_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection",
            unit = "-"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of heterotrophic organisms",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_H;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of 1st stage nitrifiers",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of 2nd stage nitrifiers",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "algae and macrophytes",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "particulate organic material",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "inert particulate organic material",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_I;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum aerobic specific growth rate of X_H",
            unit = "1/d",
            lowerBound= 1.6,
            upperBound = 2.4,
            defaultValue = "2.0"
            )
            public JAMSDouble k_gro_aero;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum anoxic specific growth rate of X_H",
            unit = "1/d",
            lowerBound= 1.28,
            upperBound = 1.92,
            defaultValue = "1.6"
            )
            public JAMSDouble k_gro_anox;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum specific growth rate of X_N1",
            unit = "1/d",
            lowerBound= 0.64,
            upperBound = 0.96,
            defaultValue = "0.8"
            )
            public JAMSDouble k_gro_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum specific growth rate of X_N2",
            unit = "1/d",
            lowerBound= 0.88,
            upperBound = 1.32,
            defaultValue = "1.1"
            )
            public JAMSDouble k_gro_N2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum specific growth rate for X_ALG",
            unit = "1/d",
            lowerBound= 1.6,
            upperBound = 2.4,
            defaultValue = "2.0"
            )
            public JAMSDouble k_gro_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "hydrolysis rate constant",
            unit = "1/d",
            lowerBound= 1.5,
            upperBound = 4.5,
            defaultValue = "3.0"
            )
            public JAMSDouble k_HYD;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum aerobic specific respiration rate of X_H",
            unit = "1/d",
            lowerBound= 0.1,
            upperBound = 0.3,
            defaultValue = "0.2"
            )
            public JAMSDouble k_resp_aero;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum anoxic specific respiration rate of X_H",
            unit = "1/d",
            lowerBound= 0.05,
            upperBound = 0.15,
            defaultValue = "0.1"
            )
            public JAMSDouble k_resp_anox;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum specific respiration rate of X_N1",
            unit = "1/d",
            lowerBound= 0.025,
            upperBound = 0.075,
            defaultValue = "0.05"
            )
            public JAMSDouble k_resp_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum specific respiration rate of X_N2",
            unit = "1/d",
            lowerBound= 0.025,
            upperBound = 0.075,
            defaultValue = "0.05"
            )
            public JAMSDouble k_resp_N2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "maximum specific respiration rate of X_ALG",
            unit = "1/d",
            lowerBound= 0.05,
            upperBound = 0.015,
            defaultValue = "0.1"
            )
            public JAMSDouble k_resp_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "specific death rate for X_ALG",
            unit = "1/d",
            lowerBound= 0.02,
            upperBound = 0.6,
            defaultValue = "0.1"
            )
            public JAMSDouble k_death_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature correction factor for heterotrophs growth rate",
            unit = "1/°C",
            lowerBound= 0.056,
            upperBound = 0.084,
            defaultValue = "0.07"
            )
            public JAMSDouble Beta_H;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature correction factor for X_N1 growth rate",
            unit = "1/°C",
            lowerBound= 0.0784,
            upperBound = 0.1176,
            defaultValue = "0.098"
            )
            public JAMSDouble Beta_N1;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature correction factor for X_N2 growth rate",
            unit = "1/°C",
            lowerBound= 0.0552,
            upperBound = 0.0828,
            defaultValue = "0.069"
            )
            public JAMSDouble Beta_N2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature correction factor for X_ALG growth rate",
            unit = "1/°C",
            lowerBound= 0.0368,
            upperBound = 0.0552,
            defaultValue = "0.046"
            )
            public JAMSDouble Beta_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "temperature correction factor for hydrolysis",
            unit = "1/°C",
            lowerBound= 0.056,
            upperBound = 0.084,
            defaultValue = "0.07"
            )
            public JAMSDouble Beta_HYD;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved organic substances for rapid biodegradation",
            unit = "mgSS/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_O2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen concentration in water body",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble disOxy;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH4-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NH4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH3-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_NH3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO2-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO3-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "HPO4-P part of inorganic dissolved phosphorus",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_HPO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "H2PO4-P part of inorganic dissolved phosphorus",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H2PO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Hydrogen ions H^+",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "pH-Value",
            unit = "",
            lowerBound= 0,
            upperBound = 14
            )
            public JAMSDouble pH;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for aerobic growth of X_H on S_S",
            unit = "mgSS/l",
            lowerBound= 0.55866,
            upperBound = 1.67598,
            defaultValue = "1.11732"
            )
            public JAMSDouble K_Saero;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for anoxic growth of X_H on S_S",
            unit = "mgSS/l",
            lowerBound= 1,
            upperBound = 3,
            defaultValue = "2.0"
            )
            public JAMSDouble K_Sanox;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation/inhibition coefficient for respiration of X_H",
            unit = "mg/l",
            lowerBound= 0.1,
            upperBound = 0.3,
            defaultValue = "0.2"
            )
            public JAMSDouble K_O2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation/inhibition coefficient for respiration of X_N1",
            unit = "mg/l",
            lowerBound= 0.25,
            upperBound = 0.75,
            defaultValue = "0.5"
            )
            public JAMSDouble K_O2_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation/inhibition coefficient for respiration of X_N2",
            unit = "mg/l",
            lowerBound= 0.25,
            upperBound = 0.75,
            defaultValue = "0.5"
            )
            public JAMSDouble K_O2_N2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation/inhibition coefficient for respiration of X_ALG",
            unit = "mg/l",
            lowerBound= 0.1,
            upperBound = 0.3,
            defaultValue = "0.2"
            )
            public JAMSDouble K_O2_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for aerobic growth of X_H on nitrogen",
            unit = "mg/l",
            lowerBound= 0.1,
            upperBound = 0.3,
            defaultValue = "0.2"
            )
            public JAMSDouble K_N;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_ALG on nitrogen",
            unit = "mg/l",
            lowerBound= 0.05,
            upperBound = 0.15,
            defaultValue = "0.1"
            )
            public JAMSDouble K_N_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for anoxic growth of X_H on nitrate",
            unit = "mg/l",
            lowerBound= 0.25,
            upperBound = 0.75,
            defaultValue = "0.5"
            )
            public JAMSDouble K_NO3anox;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for anoxic growth of X_H on nitrite",
            unit = "mg/l",
            lowerBound= 0.1,
            upperBound = 0.3,
            defaultValue = "0.2"
            )
            public JAMSDouble K_NO2anox;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_N1 on ammonia",
            unit = "mg/l",
            lowerBound= 0.25,
            upperBound = 0.75,
            defaultValue = "0.5"
            )
            public JAMSDouble K_NH4_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_ALG on ammonia",
            unit = "mg/l",
            lowerBound= 0.05,
            upperBound = 0.15,
            defaultValue = "0.1"
            )
            public JAMSDouble K_NH4_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_N2 on nitrite",
            unit = "mg/l",
            lowerBound= 0.25,
            upperBound = 0.75,
            defaultValue = "0.5"
            )
            public JAMSDouble K_NO2_N2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for aerobic growth of X_H on phosphate",
            unit = "mg/l",
            lowerBound= 0.01,
            upperBound = 0.03,
            defaultValue = "0.02"
            )
            public JAMSDouble K_HPO4aero;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for anoxic growth of X_H on phosphate",
            unit = "mg/l",
            lowerBound= 0.01,
            upperBound = 0.03,
            defaultValue = "0.02"
            )
            public JAMSDouble K_HPO4anox;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_N1 on phosphate",
            unit = "mg/l",
            lowerBound= 0.01,
            upperBound = 0.03,
            defaultValue = "0.02"
            )
            public JAMSDouble K_HPO4_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_N2 on phosphate",
            unit = "mg/l",
            lowerBound= 0.01,
            upperBound = 0.03,
            defaultValue = "0.02"
            )
            public JAMSDouble K_HPO4_N2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of X_ALG on phosphate",
            unit = "mg/l",
            lowerBound= 0.01,
            upperBound = 0.03,
            defaultValue = "0.02"
            )
            public JAMSDouble K_HPO4_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "average water temperature for specific reach",
            unit = "°C",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble watertempavg;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "mean waterdepth for specific reach",
            unit = "m",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble waterdepth;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "saturation coefficient for growth of algae on light",
            unit = "W/m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "500"
            )
            public JAMSDouble K_I;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "state variable solar radiation",
            unit = "MJ/m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble solRad;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "photosynthetically available radiation at the water surface",
            unit = "W/m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble PAR_0;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "photosynthetically available radiation at waterdepth z",
            unit = "W/m^2",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble PAR_z;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "inorganic suspended solids",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble iSS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach amount of specific substance",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QActOutN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "reach statevar total Q_out",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QActOut;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "specific substance in Reach",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Substance;

    
      
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException, IOException, NoSuchEntryException {
        Attribute.Entity entity = entities.getCurrent();
                
        
        /*
        Table2D stoiCoeff = ProcessCoefficients.getStoichiometricCoefficients();

        double x = stoiCoeff.getCell("growthXH1", "S_H");
        x = stoiCoeff.getCell("growthXH1", "S_H1");
        x = stoiCoeff.getCell("growthXH1", "S_H2");
        x = stoiCoeff.getCell("growthXH1", "S_H3");

        HashMap<String, Double> row = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXH1");
        x = row.get("S_H");
        x = row.get("S_H1");
        x = row.get("S_H2");
        x = row.get("S_H3");

        HashMap<String, Double> col = ProcessCoefficients.getStoichiometricCoefficients().getCol("S_NH4");
        x = col.get("growthXH1");
        x = row.get("S_H1");
        x = row.get("S_H2");
        x = row.get("S_H3");
        */

        // T0 constant reaction temperature in °C
        // T  mean water temperature in °C

        double T0 = 20;
        double T = watertempavg.getValue();

        double limit_sS_aero,limit_sS_anox,limit_sO2_aero,limit_sO2_anox,limitNH4,limitNO3_aero,limitNO3_anox,limitNO2_anox,limitN,limitP_aero,limitP_anox;
        double limitN1_O2,limitN1_NH4,limitN1_P,limitN2_O2,limitN2_NO2,limitN2_P;
        double limitALG_N,limitALG_NH4,limitALG_NO3,limitALG_P,limitALG_I,limitALG_O2;

        double deltaXH_NH4 = 0,deltaXH_NO3 = 0,deltaXH_respaero = 0,deltaXHanox_NO3 = 0,deltaXHanox_NO2 = 0,deltaXH_respanox = 0;
        double deltaN1_growth = 0,deltaN1_resp = 0,deltaN2_growth = 0,deltaN2_resp = 0;
        double deltaALG_NH4 = 0,deltaALG_NO3 = 0,deltaALG_resp = 0,deltaALG_death = 0;
        double delta_HYD = 0;
        
        double xh,xn1,xn2,xalg,xcon,xs,xi,xp;
        double ss,snh4,snh3,sno2,sno3,shpo4,so2,shco3,sh;

         
        
        limit_sS_aero = S_S.getValue() / (K_Saero.getValue() + S_S.getValue());
        limit_sO2_aero = S_O2.getValue() / (K_O2.getValue() + S_O2.getValue());
        limitNH4 = (S_NH4.getValue() + S_NH3.getValue()) / (K_N.getValue() + S_NH4.getValue() + S_NH3.getValue());
        limitNO3_aero = S_NO3.getValue() / (K_N.getValue() + S_NO3.getValue());
        limitN = (K_N.getValue() / (K_N.getValue() + S_NH4.getValue() + S_NH3.getValue())) * limitNO3_aero;
        limitP_aero = (S_HPO4.getValue() + S_H2PO4.getValue()) / (K_HPO4aero.getValue() + S_HPO4.getValue() + S_H2PO4.getValue());

        limit_sS_anox = S_S.getValue() / (K_Sanox.getValue() + S_S.getValue());
        limit_sO2_anox = K_O2.getValue() / (K_O2.getValue() + S_O2.getValue());
        limitNO3_anox = S_NO3.getValue() / (K_NO3anox.getValue() + S_NO3.getValue());
        limitNO2_anox = S_NO2.getValue() / (K_NO2anox.getValue() + S_NO2.getValue());
        limitP_anox = (S_HPO4.getValue() + S_H2PO4.getValue()) / (K_HPO4anox.getValue() + S_HPO4.getValue() + S_H2PO4.getValue());

        limitN1_O2 = S_O2.getValue() / (K_O2_N1.getValue() + S_O2.getValue());
        limitN2_O2 = S_O2.getValue() / (K_O2_N2.getValue() + S_O2.getValue());
        limitN1_NH4 = (S_NH4.getValue() + S_NH3.getValue()) / (K_NH4_N1.getValue() + S_NH4.getValue() + S_NH3.getValue());
        limitN2_NO2 = S_NO2.getValue() / (K_NO2_N2.getValue() + S_NO2.getValue());
        limitN1_P = (S_HPO4.getValue() + S_H2PO4.getValue()) / (K_HPO4_N1.getValue() + S_HPO4.getValue() + S_H2PO4.getValue());
        limitN2_P = (S_HPO4.getValue() + S_H2PO4.getValue()) / (K_HPO4_N2.getValue() + S_HPO4.getValue() + S_H2PO4.getValue());

        limitALG_N = (S_NH4.getValue() + S_NH3.getValue() + S_NO3.getValue()) / (K_N_ALG.getValue() + S_NH4.getValue() + S_NH3.getValue() + S_NO3.getValue());
        limitALG_NH4 = (S_NH4.getValue() + S_NH3.getValue()) / (K_NH4_ALG.getValue() + S_NH4.getValue() + S_NH3.getValue());
        limitALG_NO3 = (K_NH4_ALG.getValue() / (K_NH4_ALG.getValue() + S_NH4.getValue() + S_NH3.getValue())) * limitALG_N;
        limitALG_P = (S_HPO4.getValue() + S_H2PO4.getValue()) / (K_HPO4_ALG.getValue() + S_HPO4.getValue() + S_H2PO4.getValue());
        limitALG_O2 = S_O2.getValue() / (K_O2_ALG.getValue() + S_O2.getValue());
        

    // Heterotrophs
        // 1a, aerobic growth of heterotrophs with NH4
        double growth_XH_aero = this.calc_specific_rate(T, T0, Beta_H.getValue(), k_gro_aero.getValue());
        deltaXH_NH4 = growth_XH_aero * limit_sS_aero * limit_sO2_aero * limitNH4 * limitP_aero * X_H.getValue();

        // 1b, aerobic growth of heterotrophs with NO3
        deltaXH_NO3 = growth_XH_aero * limit_sS_aero * limit_sO2_aero * limitN * limitP_aero * X_H.getValue();

        // 2, aerobic endogenous respiration of heterotrophs
        double resp_XH_aero =  this.calc_specific_rate(T, T0, Beta_H.getValue(), k_resp_aero.getValue());
        deltaXH_respaero = resp_XH_aero * limit_sO2_aero * X_H.getValue();

        // 3a, anoxic growth of heterotrophs with NO3
        double growth_XH_anox = this.calc_specific_rate(T, T0, Beta_H.getValue(), k_gro_anox.getValue());
        deltaXHanox_NO3 = growth_XH_anox * limit_sS_anox * limit_sO2_anox * limitNO3_anox * limitP_anox * X_H.getValue();

        // 3b, anoxic growth of heterotrophs with NO2
        deltaXHanox_NO2 = growth_XH_anox * limit_sS_anox * limit_sO2_anox * limitNO2_anox * limitP_anox * X_H.getValue();

        // 4, anoxic endogenous respiration of heterotrophs
        double resp_XH_anox = this.calc_specific_rate(T, T0, Beta_H.getValue(), k_resp_anox.getValue());
        deltaXH_respanox = resp_XH_anox * limit_sO2_anox * limitNO3_anox * X_H.getValue();


    // Nitrifiers
        // 5, growth of 1st-stage nitrifiers
        double growth_N1 = this.calc_specific_rate(T, T0, Beta_N1.getValue(), k_gro_N1.getValue());
        deltaN1_growth = growth_N1 * limitN1_O2 * limitN1_NH4 * limitN1_P * X_N1.getValue();

        // 6, aerobic endogenous respiration of 1st-stage nitrifiers
        double resp_N1 = this.calc_specific_rate(T, T0, Beta_N1.getValue(), k_resp_N1.getValue());
        deltaN1_resp = resp_N1 * limitN1_O2 * X_N1.getValue();

        // 7, growth of 2nd-stage nitrifiers
        double growth_N2 = this.calc_specific_rate(T, T0, Beta_N2.getValue(), k_gro_N2.getValue());
        deltaN2_growth = growth_N2 * limitN2_O2 * limitN2_NO2 * limitN2_P * X_N2.getValue();

        // 8, aerobic endogenous respiration of 2nd-stage nitrifiers
        double resp_N2 = this.calc_specific_rate(T, T0, Beta_N2.getValue(), k_resp_N2.getValue());
        deltaN2_resp = resp_N2 * limitN2_O2 * X_N2.getValue();



    // Algae
        // calculation of algae growth attenuation due to light by Steele's equation (Steele 1962)
        
            // I_0 photosynthetically available radiation at the water surface in W/m^2
               // solRad (MJ/m^2) converted to J/m^2 (* 1000000) and W/m^2 by dividing with seconds of timestep
               //double I_0 = (0.47 * solRad.getValue()) * (1000000 / 86400);

            // solar radiation at the water surface (W/m^2)
            double I_0 = solRad.getValue() * (1000000 / 86400);

            // I_z photosynthetically available radiation at waterdepth z in W/m^2
               // light attenuation through the water follows the Beer-Lambert law
               // ke      the light extinction coefficient (1/m)
               // keb     the backround coefficient accounting for extinction due to water and color (1/m)
               // Alpha_i light extinction coefficient accounting for the impact of inorganic suspended solids (-)
               // Alpha_o light extinction coefficient accounting for the impact of particulate organic matter (-)
               // Alpha_p linear light extinction coefficient accounting for the impact of chlorophyll (-)
               // Alpha_pn nonlinear light extinction coefficient accounting for the impact of chlorophyll (-)
               double ke,keb,Alpha_i,Alpha_o,Alpha_p,Alpha_pn;
               keb = 0;
               Alpha_i = 0.052;
               Alpha_o = 0.174;
               Alpha_p = 0.0088;
               Alpha_pn = 0.054;
               ke = keb + (Alpha_i * iSS.getValue()) + (Alpha_o * X_S.getValue()) + (Alpha_p * X_ALG.getValue()) + (Alpha_pn * Math.pow(X_ALG.getValue(), 2/3));

               //double I_z = I_0 * Math.exp((-1) * ke * waterdepth.getValue());
               double K = K_I.getValue();
               double z = waterdepth.getValue();

               //PAR_0.setValue(I_0);
               //PAR_z.setValue(I_z);

               //limitALG_I = (I_z / K) * Math.exp(1 - (I_z / K));
               limitALG_I = (Math.E / (ke * z)) * (Math.exp((-1) * (I_0 / K) * Math.exp((-1) * ke * z)) * ((-1) * Math.exp((-1) * (I_0 / K))));



        // 9a, growth of algae with NH4
        double growth_ALG = this.calc_specific_rate(T, T0, Beta_ALG.getValue(), k_gro_ALG.getValue());
        deltaALG_NH4 = growth_ALG * limitALG_N * limitALG_NH4 * limitALG_P * limitALG_I * X_ALG.getValue();

        // 9b, growth of algae with NO3
        deltaALG_NO3 = growth_ALG * limitALG_N * limitALG_NO3 * limitALG_P * limitALG_I * X_ALG.getValue();

        // 10, aerobic endogenous respiration of algae
        double resp_ALG = this.calc_specific_rate(T, T0, Beta_ALG.getValue(), k_resp_ALG.getValue());
        deltaALG_resp = resp_ALG * limitALG_O2 * X_ALG.getValue();

        // 11, death of algae
        double death_ALG = this.calc_specific_rate(T, T0, Beta_ALG.getValue(), k_death_ALG.getValue());
        deltaALG_death = death_ALG * X_ALG.getValue();



        // 15, hydrolysis
        double rate_HYD = this.calc_specific_rate(T, T0, Beta_HYD.getValue(), k_HYD.getValue());
        delta_HYD = rate_HYD * X_S.getValue();



// get stoichiometric Coefficients for balance equations

        double xh1a_1,xh1a_3,xh1a_7,xh1a_9,xh1a_11,xh1a_13,xh1a_16;
        HashMap<String, Double> row1a = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXH_1a");
        xh1a_1 = row1a.get("S_S");
        xh1a_3 = row1a.get("S_NH4");
        xh1a_7 = row1a.get("S_HPO4");
        xh1a_9 = row1a.get("S_O2");
        xh1a_11 = row1a.get("S_HCO3");
        xh1a_13 = row1a.get("S_H");
        xh1a_16 = row1a.get("X_H");

        double xh1b_1,xh1b_6,xh1b_7,xh1b_9,xh1b_11,xh1b_13,xh1b_16;
        HashMap<String, Double> row1b = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXH_1b");
        xh1b_1 = row1b.get("S_S");
        xh1b_6 = row1b.get("S_NO3");
        xh1b_7 = row1b.get("S_HPO4");
        xh1b_9 = row1b.get("S_O2");
        xh1b_11 = row1b.get("S_HCO3");
        xh1b_13 = row1b.get("S_H");
        xh1b_16 = row1b.get("X_H");

        double xh2_3,xh2_7,xh2_9,xh2_11,xh2_13,xh2_16,xh2_22;
        HashMap<String, Double> row2 = ProcessCoefficients.getStoichiometricCoefficients().getRow("respXH_2");
        xh2_3 = row2.get("S_NH4");
        xh2_7 = row2.get("S_HPO4");
        xh2_9 = row2.get("S_O2");
        xh2_11 = row2.get("S_HCO3");
        xh2_13 = row2.get("S_H");
        xh2_16 = row2.get("X_H");
        xh2_22 = row2.get("X_I");

        double xh3a_1,xh3a_5,xh3a_6,xh3a_7,xh3a_11,xh3a_13,xh3a_16;
        HashMap<String, Double> row3a = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXH_3a");
        xh3a_1 = row3a.get("S_S");
        xh3a_5 = row3a.get("S_NO2");
        xh3a_6 = row3a.get("S_NO3");
        xh3a_7 = row3a.get("S_HPO4");
        xh3a_11 = row3a.get("S_HCO3");
        xh3a_13 = row3a.get("S_H");
        xh3a_16 = row3a.get("X_H");

        double xh3b_1,xh3b_5,xh3b_7,xh3b_11,xh3b_13,xh3b_16;
        HashMap<String, Double> row3b = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXH_3b");
        xh3b_1 = row3b.get("S_S");
        xh3b_5 = row3b.get("S_NO2");
        xh3b_7 = row3b.get("S_HPO4");
        xh3b_11 = row3b.get("S_HCO3");
        xh3b_13 = row3b.get("S_H");
        xh3b_16 = row3b.get("X_H");

        double xh4_3,xh4_6,xh4_7,xh4_11,xh4_13,xh4_16,xh4_22;
        HashMap<String, Double> row4 = ProcessCoefficients.getStoichiometricCoefficients().getRow("resphXH_4");
        xh4_3 = row4.get("S_NH4");
        xh4_6 = row4.get("S_NO3");
        xh4_7 = row4.get("S_HPO4");
        xh4_11 = row4.get("S_HCO3");
        xh4_13 = row4.get("S_H");
        xh4_16 = row4.get("X_H");
        xh4_22 = row4.get("X_I");

        double xn5_3,xn5_5,xn5_7,xn5_9,xn5_11,xn5_13,xn5_17;
        HashMap<String, Double> row5 = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXN1_5");
        xn5_3 = row5.get("S_NH4");
        xn5_5 = row5.get("S_NO2");
        xn5_7 = row5.get("S_HPO4");
        xn5_9 = row5.get("S_O2");
        xn5_11 = row5.get("S_HCO3");
        xn5_13 = row5.get("S_H");
        xn5_17 = row5.get("X_N1");

        double xn6_3,xn6_7,xn6_9,xn6_11,xn6_13,xn6_17,xn6_22;
        HashMap<String, Double> row6 = ProcessCoefficients.getStoichiometricCoefficients().getRow("respXN1_6");
        xn6_3 = row6.get("S_NH4");
        xn6_7 = row6.get("S_HPO4");
        xn6_9 = row6.get("S_O2");
        xn6_11 = row6.get("S_HCO3");
        xn6_13 = row6.get("S_H");
        xn6_17 = row6.get("X_N1");
        xn6_22 = row6.get("X_I");

        double xn7_5,xn7_6,xn7_7,xn7_9,xn7_11,xn7_13,xn7_18;
        HashMap<String, Double> row7 = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXN2_7");
        xn7_5 = row7.get("S_NO2");
        xn7_6 = row7.get("S_NO3");
        xn7_7 = row7.get("S_HPO4");
        xn7_9 = row7.get("S_O2");
        xn7_11 = row7.get("S_HCO3");
        xn7_13 = row7.get("S_H");
        xn7_18 = row7.get("X_N2");

        double xn8_3,xn8_7,xn8_9,xn8_11,xn8_13,xn8_18,xn8_22;
        HashMap<String, Double> row8 = ProcessCoefficients.getStoichiometricCoefficients().getRow("respXN2_8");
        xn8_3 = row8.get("S_NH4");
        xn8_7 = row8.get("S_HPO4");
        xn8_9 = row8.get("S_O2");
        xn8_11 = row8.get("S_HCO3");
        xn8_13 = row8.get("S_H");
        xn8_18 = row8.get("X_N2");
        xn8_22 = row8.get("X_I");

        double xalg9a_3,xalg9a_7,xalg9a_9,xalg9a_11,xalg9a_13,xalg9a_19;
        HashMap<String, Double> row9a = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXALG_9a");
        xalg9a_3 = row9a.get("S_NH4");
        xalg9a_7 = row9a.get("S_HPO4");
        xalg9a_9 = row9a.get("S_O2");
        xalg9a_11 = row9a.get("S_HCO3");
        xalg9a_13 = row9a.get("S_H");
        xalg9a_19 = row9a.get("X_ALG");

        double xalg9b_6,xalg9b_7,xalg9b_9,xalg9b_11,xalg9b_13,xalg9b_19;
        HashMap<String, Double> row9b = ProcessCoefficients.getStoichiometricCoefficients().getRow("growthXALG_9b");
        xalg9b_6 = row9b.get("S_NO3");
        xalg9b_7 = row9b.get("S_HPO4");
        xalg9b_9 = row9b.get("S_O2");
        xalg9b_11 = row9b.get("S_HCO3");
        xalg9b_13 = row9b.get("S_H");
        xalg9b_19 = row9b.get("X_ALG");

        double xalg10_3,xalg10_7,xalg10_9,xalg10_11,xalg10_13,xalg10_19,xalg10_22;
        HashMap<String, Double> row10 = ProcessCoefficients.getStoichiometricCoefficients().getRow("respXALG_10");
        xalg10_3 = row10.get("S_NH4");
        xalg10_7 = row10.get("S_HPO4");
        xalg10_9 = row10.get("S_O2");
        xalg10_11 = row10.get("S_HCO3");
        xalg10_13 = row10.get("S_H");
        xalg10_19 = row10.get("X_ALG");
        xalg10_22 = row10.get("X_I");

        double xalg11_3,xalg11_7,xalg11_9,xalg11_11,xalg11_13,xalg11_19,xalg11_21,xalg11_22;
        HashMap<String, Double> row11 = ProcessCoefficients.getStoichiometricCoefficients().getRow("deathXALG_11");
        xalg11_3 = row11.get("S_NH4");
        xalg11_7 = row11.get("S_HPO4");
        xalg11_9 = row11.get("S_O2");
        xalg11_11 = row11.get("S_HCO3");
        xalg11_13 = row11.get("S_H");
        xalg11_19 = row11.get("X_ALG");
        xalg11_21 = row11.get("X_S");
        xalg11_22 = row11.get("X_I");

        double xh15_1,xh15_3,xh15_7,xh15_9,xh15_11,xh15_13,xh15_21;
        HashMap<String, Double> row15 = ProcessCoefficients.getStoichiometricCoefficients().getRow("hydrolysis_15");
        xh15_1 = row15.get("S_S");
        xh15_3 = row15.get("S_NH4");
        xh15_7 = row15.get("S_HPO4");
        xh15_9 = row15.get("S_O2");
        xh15_11 = row15.get("S_HCO3");
        xh15_13 = row15.get("S_H");
        xh15_21 = row15.get("X_S");


  //balance equations for model components/variables
               
        // Heterotrophs (gXH)
        xh = X_H.getValue() + (xh1a_16 * deltaXH_NH4) + (xh1b_16 * deltaXH_NO3) + (xh3a_16 * deltaXHanox_NO3)
             + (xh3b_16 * deltaXHanox_NO2) + (xh2_16 * deltaXH_respaero) + (xh4_16 * deltaXH_respanox);
        X_H.setValue(xh);

        // Nitrifiers (gN1, gN2)
        xn1 = X_N1.getValue() + (xn5_17 * deltaN1_growth) + (xn6_17 * deltaN1_resp);
        X_N1.setValue(xn1);
        xn2 = X_N2.getValue() + (xn7_18 * deltaN2_growth) + (xn8_18 * deltaN2_resp);
        X_N2.setValue(xn2);

        // Algaes (gALG)
        xalg = X_ALG.getValue() + (xalg9a_19 * deltaALG_NH4) + (xalg9b_19 * deltaALG_NO3) + (xalg10_19 * deltaALG_resp) 
               + (xalg11_19 * deltaALG_death);
        X_ALG.setValue(xalg);

        // Consumers (gCON)



        // Particulate organic material without consumers (gXS)
        xs = X_S.getValue() + (xalg11_21 * deltaALG_death) + (xh15_21 * delta_HYD);
        X_S.setValue(xs);

        // Inert particulate organic material (gXI)
        xi = X_I.getValue() + (xh2_22 * deltaXH_respaero) + (xh4_22 * deltaXH_respanox) + (xn6_22 * deltaN1_resp)
             + (xn8_22 * deltaN2_resp) + (xalg10_22 * deltaALG_resp) + (xalg11_22 * deltaALG_death);
        X_I.setValue(xi);

        // Dissolved organic substances (gSS)
        ss = S_S.getValue() + (xh15_1 * delta_HYD) + (xh1a_1 * deltaXH_NH4) + (xh1b_1 * deltaXH_NO3)
             + (xh3a_1 * deltaXHanox_NO3) + (xh3b_1 *deltaXHanox_NO2);
        S_S.setValue(ss);

        // Ammonium NH4^+ (gN)
        snh4 = S_NH4.getValue() + (xh2_3 * deltaXH_respaero) + (xh4_3 * deltaXH_respanox) + (xn6_3 * deltaN1_resp)
               + (xn8_3 * deltaN2_resp) + (xalg10_3 * deltaALG_resp) + (xalg11_3 * deltaALG_death) + (xh15_3 * delta_HYD)
               + (xh1a_3 * deltaXH_NH4) + (xn5_3 * deltaN1_growth) + (xalg9a_3 * deltaALG_NH4);
        S_NH4.setValue(snh4); 

        // Ammonia NH3 (gN)
        


        // Nitrite NO2^- (gN)
        sno2 = S_NO2.getValue() + (xh3a_5 * deltaXHanox_NO3) + (xn5_5 * deltaN1_growth)
               + (xh3b_5 * deltaXHanox_NO2) + (xn7_5 * deltaN2_growth);
        S_NO2.setValue(sno2);

        // Nitrate NO3^- (gN)
        sno3 = S_NO3.getValue() + (xn7_6 * deltaN2_growth) + (xh1b_6 * deltaXH_NO3) + (xh3a_6 * deltaXHanox_NO3) 
               + (xh4_6 * deltaXH_respanox) + (xalg9b_6 * deltaALG_NO3);
        S_NO3.setValue(sno3);

        // Part of inorganic dissolved phosphorus HPO4^2- (gP)
        shpo4 = S_HPO4.getValue() + (xh1a_7 * deltaXH_NH4) + (xh1b_7 * deltaXH_NO3) + (xh2_7 * deltaXH_respaero) 
                + (xh3a_7 * deltaXHanox_NO3) + (xh3b_7 *deltaXHanox_NO2) + (xh4_7 * deltaXH_respanox)
                + (xn5_7 * deltaN1_growth) + (xn6_7 * deltaN1_resp) + (xn7_7 * deltaN2_growth) + (xn8_7 * deltaN2_resp)
                + (xalg9a_7 * deltaALG_NH4) + (xalg9b_7 * deltaALG_NO3) + (xalg10_7 * deltaALG_resp)
                + (xalg11_7 * deltaALG_death) + (xh15_7 * delta_HYD);
        S_HPO4.setValue(shpo4);

        // Dissolved oxygen O2 (gO)
        so2 = disOxy.getValue() + (xh1a_9 * deltaXH_NH4) + (xh1b_9 * deltaXH_NO3) + (xh2_9 * deltaXH_respaero) 
              + (xn5_9 * deltaN1_growth) + (xn6_9 * deltaN1_resp) + (xn7_9 * deltaN2_growth) + (xn8_9 * deltaN2_resp)
              + (xalg9a_9 * deltaALG_NH4) + (xalg9b_9 * deltaALG_NO3) + (xalg10_9 * deltaALG_resp)
              + (xalg11_9 * deltaALG_death) + (xh15_9 * delta_HYD);
        S_O2.setValue(so2);

        // Bicarbonate HCO3^- (gC)



        // Hydrogen ions H^+ (gH)
        
        
       
    }

     public void cleanup() {

    }

      /**
     * calculates the specific process rate for biomass variable
     * @param T the mean water temperature in °C
     * @param T0 the constant reaction temperature in °C
     * @param Beta temperature correction factor 1/°C
     * @param MaxRate 1/d
     * @return the specific process rate for biomass variable
     */
    public double calc_specific_rate(double t, double t0, double Beta, double MaxRate){
        double Rate = MaxRate * Math.exp(Beta * (t - t0));
        return Rate;
    }


     

       
   

}


    
