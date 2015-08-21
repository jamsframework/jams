/*
 * ClimatologicalVariables.java
 *
 * Created on 24. November 2005, 09:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package lib;

/**
 *
 * @author c0krpe
 */

 public class Climate {
    
    
    /**
     * calculates absolute temperature in K from temperatures in C or F
     * @param temperature the temperature in ï¿½C or F
     * @param unit degC - for Celsius; F - for Fahrenheit
     * @return the absolute temperature in K
     */    
    public static double absTemp(double temperature, String unit){
        double T0 = 273.15;
        double absTemp = 0;
        if(unit.equals("C")) {
            absTemp = temperature + T0;
        } else if (unit.equals("F")) {
            absTemp = (temperature - 32) * (5 / 9) + T0;
        } else {
            throw new IllegalArgumentException("unit");
        }
        return absTemp;
    }
    
    /**
     * calculates saturation vapour pressure at the given temperature in kPa
     * @param temperature the air temperature in ï¿½C
     * @return the saturation vapour pressure at temperature T [kPa]
     */    
    public static double saturationVapourPressure(double temperature){
        return 0.6108 * Math.exp((17.27 * temperature)/(237.3 + temperature));
    }
    
    /**
     * calculates actual vapour pressure depending from relative humidity and saturation
     * vapour pressure
     * @param rhum relative humidtiy in %
     * @param es_T saturation vapour pressure in kPa
     * @return the actual vapour pressure in kPa
     */    
    public static double vapourPressure(double rhum, double es_T){
        //vapour pressure e [kPa]
        return es_T * (rhum / 100.0);
    }
    
    /**
     * calculates maximum possible humidity of the air at given temperature
     * @param temperature the current air temperature ï¿½C
     * @return tbe maximum possible humidity in g/cmï¿½
     */    
    public static double maxHum(double temperature){
        double esT = saturationVapourPressure(temperature);
        //esT *10   kPa -> hPa
        esT = 10 * esT;
        return esT * (216.7)/(temperature + 273.15);
    }
    
    /**
     * calculated latent heat of vaporization depending from temperature in MJ / kg
     * @param temperature the air temperature in ï¿½C
     * @return latent heat of vaporization in [MJ/kg]
     */    
     public static double latentHeatOfVaporization(double temperature){
        //-------------------------------------
        // Latent heat of vaporization L MJ/kg=l=mm 
        //-------------------------------------
        return (2501 - (2.361 * temperature)) / 1000;
    }
     
     /**
      * calculates the psychrometric constant using:
      * atmospheric pressure in [kPa]
      * latent heat of vaporisation [MJ/kg]
      * specific heat at constant pressure = cp = 1.013E-3 MJ/kgï¿½C
      * ratio molecular weight of water vapour / dry air = 0.622
      * @param pZ atmospheric pressure [kPa]
      * @param L latent heat of vaporisation [MJ/kg]
      * @return psychrometric constant [kPa / ï¿½C]
      */     
    public static double psyConst(double pZ, double L){
        /**
         *specif. heat capacity of air [MJ / kgï¿½C]
         */
        double CP = 1.013E-3;
        /**
         *Relation of mol weights wet air/dry air[-]
         */
        double VM = 0.622;
        //----------------------------------
        // Psychrometric constant psy [kPa/ï¿½C]
        //----------------------------------
        return (CP * pZ)  / (VM * L);
    }
    
    /**
     * estimates atmospheric pressure for point of interest by using 
     * the gravity constant g = 9.811 [m/s] and the gas constant R = 8314.3 [J/kmol K]
     * @param elevation the elevation of the point of interest [m]
     * @param tabs the absolute air temperature [K]
     * @return atmospheric pressure [kPa]
     */    
    public static double atmosphericPressure(double elevation, double tabs){
        double pZ = 1013 * Math.exp(-1*((9.811/(8314.3 * tabs))* elevation));
        return pZ / 10;
    }
    
    /**
     * calculates the slope of the saturation vapour pressure curve at given temperature
     * @param temperature the air temperature in ï¿½C
     * @return slope of saturation vapour pressure curve [kPa/ï¿½C]
     */    
    public static double slopeOfSaturationPressureCurve(double temperature){
        double k_temp = temperature + 237.3;
        return (4098*(0.6108*Math.exp((17.27 * temperature)/k_temp)))/(k_temp*k_temp);

    //    double sospc = (4098*(0.6108*Math.exp((17.27 * temperature)/(temperature + 237.3))))/(Math.pow((temperature + 237.3),2));
        //double sospc =(25040 / Math.pow((237.3 + temperature),2)) * Math.exp((17.27 * temperature)/(237.3 + temperature));
    }
    
    public static double virtualTemperature(double tabs, double pz, double ea){
        //double vt = tabs * Math.pow((1-0.378*(ea/pz)),-1);
        return tabs / (1-0.378*(ea/pz));
    }
    
    /**
     * calculates air density at constant pressure using the specific gas constant
     * R = 0.287 kJ/kg K
     * @param virtTemp the virtuel air temperature [K]
     * @param P the atmospheric pressure [kPa]
     * @return the air density at constant pressure in kg/mï¿½
     */    
    public static double airDensityAtConstantPressure(double virtTemp, double P){
        //P from hPa to kPa
        return 3.486 * (P / virtTemp);
    }
}
