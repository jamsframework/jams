/*
 * CalcVariables.java
 *
 * Created on 7. Februar 2006, 12:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.refET;

/**
 *
 * @author c0krpe
 */
public class CalcVariables {
    
    /** Creates a new instance of CalcVariables */
    public CalcVariables() {
    }
    
    public static double calcAirPressure(double elevation){
        final double R = 287; //Gas constant [J kg-1 K-1]
        final double P0 = 103.1; //Atmospheric pressure at sea level [kPa]
        final double LR = 0.0065; //Lapse rate [K m-1]
        final double TK0 = 293.16; //Reference absolute temperature at sea level [°C]
        final double G = 9.807; //Gravitational acceleration at sea level [m s-2]
        
        //air pressure at given temperature
        double airPressure = P0 * Math.pow((TK0 - LR * elevation) / TK0, G / (LR * R));
        
        return airPressure;
    }
    
    public static double calcAtmosphericDensity(double temperature, double airPressure){
        final double C = 1.01; //Temperature adjustment parameter [-]
        final double R = 287; //Gas constant [J kg-1 K-1]
        //absolute temperature
        double absTemp = C * (temperature + 273.16);
        //atmospheric density
        double atmDens = (1000 * airPressure) / (R * absTemp);
        
        return atmDens;
    }
    
    public static double calcSaturationVapourPressure(double maxTemp, double minTemp){
        double esTmax = 0.6108 * Math.exp((17.27 * maxTemp)/(237.3 + maxTemp));
        double esTmin = 0.6108 * Math.exp((17.27 * minTemp)/(237.3 + minTemp));
        double esT = 0.5 * (esTmax + esTmin);
        return esT;
    }
    
    public static double calcSaturationVapourPressure(double temperature){
        double esT = 0.6108 * Math.exp((17.27 * temperature)/(237.3 + temperature));
        return esT;
    }
    
    public static double calcActualVapourPressure(double maxEst, double minEst, double maxRhum, double minRhum){
        //vapour pressure e [kPa]
        double ea = 0.5 * (minEst * (maxRhum / 100) + maxEst * (maxRhum / 100));
        return ea;
    }
    
    public static double calcActualVapourPressure(double esT, double rhum){
        //vapour pressure e [kPa]
        double ea = esT * (rhum / 100.0);
        return ea;
    }
    
    /**
     * calculates the slope of the saturation vapour pressure curve at given temperature
     * @param temperature the air temperature in °C
     * @return slope of saturation vapour pressure curve [kPa/°C]
     */    
    public static double calc_slopeOfSaturationPressureCurve(double temperature){
        double sospc = (4098*(0.6108*Math.exp((17.27 * temperature)/(temperature + 237.3))))/(Math.pow((temperature + 237.3),2));
        return sospc;
    }
    
    /**
     * calculated latent heat of vaporization depending from temperature in MJ / kg
     * @param temperature the air temperature in °C
     * @return latent heat of vaporization in [KJ/kg]
     */    
     public static double calc_latentHeatOfVaporization(double temperature){
        //-------------------------------------
        // Latent heat of vaporization L MJ/kg=l=mm 
        //-------------------------------------
        double L = (2501 - (2.361 * temperature)) / 1000;
        return L;
    }
     
    /**
      * calculates the psychrometric constant using:
      * atmospheric pressure in [hPa]
      * latent heat of vaporisation [MJ/kg]
      * @param airPressure - atmospheric pressure [hPa]
      * @param latHeat - latent heat of vaporisation [MJ/kg]
      * @return psychrometric constant [kPa / °C]
      */     
    public static double calc_psyConst(double airPressure, double latHeat){
        /**
         *specif. heat capacity of air [MJ kg-1 °C-1]
         */
        double CP = 0.001013;
        /**
         *Ratio of mol weights wet air/dry air[-]
         */
        double VM = 0.622;
        //----------------------------------
        // Psychrometric constant psy [hPa/°C]
        //----------------------------------
        double psyConst = (CP * airPressure)   / (VM * latHeat);
        
        return psyConst;
    }
    
    public static double calcAerodynamicResistance(double windSpeed){
        if(windSpeed < 0.5)
            windSpeed = 0.5;
        double ra = 208 / windSpeed;
        
        return ra;
    }
    
    public static double calcNetRadiation(double maxTemp, double minTemp, double solRad, double extRad, double actVP, double albedo, double clearSkyTrans, String tempRes){
        final double Sday  = 4.9032E-9; //Boltzmann constant [MJ m-2 K-1 d-1]
        final double Shour = 2.043E-10;
        double fh = 0.34 - 0.14 * Math.sqrt(actVP); //Air humidity factor
        double ghc = extRad * clearSkyTrans; //Clear sky global solar radiation
        double fc = 1.35 * (solRad / ghc) - 0.35; //Cloudiness factor
        double swNetRad = (1 - albedo) * solRad;
        double S = 0;
        if(tempRes.equals("d"))
            S = Sday;
        else if(tempRes.equals("h"))
            S = Shour;
        double lwNetRad = S * fc * fh * (0.5 * (Math.pow((maxTemp + 273.15),4)+Math.pow((minTemp + 273.15),4)));
        double netRad = swNetRad - lwNetRad;
        
        return netRad;
    }
    
   
    
}
