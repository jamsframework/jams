/*
 * ClimatologicalVariables.java
 *
 * Created on 24. November 2005, 09:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.jams.j2k.s_n.wq.physicalCalculations;

/**
 *
 * @author c0krpe
 */
public class ClimatologicalVariables {
    
    /** Creates a new instance of ClimatologicalVariables */
    public ClimatologicalVariables() {
    }
    
    /**
     * calculates absolute temperature in K from temperatures in °C or F
     * @param temperature the temperature in °C or F
     * @param unit degC - for Celsius; F - for Fahrenheit
     * @return the absolute temperature in K
     */    
    public static double calc_absTemp(double temperature, String unit){
        double T0 = 273.15;
        double absTemp = 0;
        if(unit.equals("degC"))
            absTemp = temperature + T0;
        else if (unit.equals("F"))
            absTemp = (temperature - 32) * (5/9) + T0;
        
        return absTemp;
    }
    
    /**
     * calculates saturation vapour pressure at the given temperature in kPa
     * @param temperature the air temperature in °C
     * @return the saturation vapour pressure at temperature T [kPa]
     */    
    public static double calc_saturationVapourPressure(double temperature){
        double es_T = 0.6108 * Math.exp((17.27 * temperature)/(237.3 + temperature));
                    
        return es_T;
    }
    
    /**
     * calculates actual vapour pressure depending from relative humidity and saturation
     * vapour pressure
     * @param rhum relative humidtiy in %
     * @param es_T saturation vapour pressure in kPa
     * @return the actual vapour pressure in kPa
     */    
    public static double calc_vapourPressure(double rhum, double es_T){
        //vapour pressure e [kPa]
        double ea = es_T * (rhum / 100.0);
        return ea;
    }
    
    /**
     * calculates maximum possible humidity of the air at given temperature
     * @param temperature the current air temperature °C
     * @return tbe maximum possible humidity in g/cm°
     */    
    public static double calc_maxHum(double temperature){
        double esT = calc_saturationVapourPressure(temperature);
        //esT *10   kPa -> hPa
        esT = 10 * esT;
        double mH = esT * (216.7)/(temperature + 273.15);
        return mH;
    }
    
    /**
     * calculated latent heat of vaporization depending from temperature in MJ / kg
     * @param temperature the air temperature in °C
     * @return latent heat of vaporization in [MJ/kg]
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
      * atmospheric pressure in [kPa]
      * latent heat of vaporisation [MJ/kg]
      * specific heat at constant pressure = cp = 1.013E-3 MJ/kg°C
      * ratio molecular weight of water vapour / dry air = 0.622
      * @param pZ atmospheric pressure [kPa]
      * @param L latent heat of vaporisation [MJ/kg]
      * @return psychrometric constant [kPa / °C]
      */     
    public static double calc_psyConst(double pZ, double L){
        /**
         *specif. heat capacity of air [MJ / kg°C]
         */
        double CP = 1.013E-3;
        /**
         *Relation of mol weights wet air/dry air[-]
         */
        double VM = 0.622;
        //----------------------------------
        // Psychrometric constant psy [kPa/°C]
        //----------------------------------
        double psy = (CP * pZ)   / (VM * L);
        
        return psy;
    }
    
    /**
     * estimates atmospheric pressure for point of interest by using 
     * the gravity constant g = 9.811 [m/s] and the gas constant R = 8314.3 [J/kmol K]
     * @param elevation the elevation of the point of interest [m]
     * @param tabs the absolute air temperature [K]
     * @return atmospheric pressure [kPa]
     */    
    public static double calc_atmosphericPressure(double elevation, double tabs){
        double pZ = 1013 * Math.exp(-1*((9.811/(8314.3 * tabs))* elevation));
        return pZ / 10;
    }
    
    /**
     * calculates the slope of the saturation vapour pressure curve at given temperature
     * @param temperature the air temperature in °C
     * @return slope of saturation vapour pressure curve [kPa/°C]
     */    
    public static double calc_slopeOfSaturationPressureCurve(double temperature){
        double sospc = (4098*(0.6108*Math.exp((17.27 * temperature)/(temperature + 237.3))))/((temperature + 237.3)*(temperature + 237.3));
        //double sospc =(25040 / Math.pow((237.3 + temperature),2)) * Math.exp((17.27 * temperature)/(237.3 + temperature));
        return sospc;
    }
    
    public static double calc_VirtualTemperature(double tabs, double pz, double ea){
        double vt = tabs / (1-0.378*(ea/pz));
        return vt;
    }
    /**
     * calculates air density at constant pressure using the specific gas constant
     * R = 0.287 kJ/kg K
     * @param virtTemp the virtuel air temperature [K]
     * @param P the atmospheric pressure [kPa]
     * @return the air density at constant pressure in kg/m°
     */    
    public static double calc_AirDensityAtConstantPressure(double virtTemp, double P){
        //P from hPa to kPa
        double da = 3.486 * (P / virtTemp);
        return da;
    }
    
    
    
}
