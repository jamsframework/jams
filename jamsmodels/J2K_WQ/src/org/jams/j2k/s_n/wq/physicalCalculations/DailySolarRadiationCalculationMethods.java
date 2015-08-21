/*
 * DailySolarRadiationCalculationMethods.java
 *
 * Created on 24. November 2005, 13:00
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
public class DailySolarRadiationCalculationMethods {
    
    /** Creates a new instance of DailySolarRadiationCalculationMethods */
    public DailySolarRadiationCalculationMethods() {
    }
    
    /**
     * calculates the sunset hour angle in rad.
     * @param latitude the latitude of the point of interest [rad.]
     * @param declination the sun's declination at the current date [rad.]
     * @return the sunset hour angle [rad.]
     */    
    public static double calc_SunsetHourAngle(double latitude, double declination){
        double sha = Math.acos(-1 * Math.tan(latitude) * Math.tan(declination));
        return sha;
    }
    
    /**
     * calculates the maximum possible sunshine hours on clear sky condititions
     * @param sunsetHourAngle the sunset hour angle [rad.]
     * @return maximum possible sunshine hours on clear sky conditions [hour]
     */    
    public static double calc_maximumSunshineHours(double sunsetHourAngle){
        double psh = 24 / Math.PI * sunsetHourAngle;
        return psh;
    }
    
    /**
     * calculates the daily extraterrestrial radiation
     * @param Gsc the solar constant [MJ / m°min.]
     * @param dr the inverse relative distance Earth-Sun [rad.]
     * @param ws the hour angle [rad.]
     * @param lat the latitude of the point of interest [rad.]
     * @param decl the sun's declination [rad.]
     * @return the extraterrestrial radiation [MJ / m° day]
     */    
    public static double calc_DailyExtraterrestrialRadiation(double Gsc, 
                                                             double dr, 
                                                             double ws, 
                                                             double lat, 
                                                             double decl){
        double Ra = ((24 * 60) / Math.PI) * Gsc * dr * (ws * Math.sin(lat) * Math.sin(decl) + Math.cos(lat) * Math.cos(decl) * Math.sin(ws)); 
        return Ra;
    }
    
    /**
     * calculates the net (outgoing) longwave radiation uses the 
     * Stefan Bolztmann constant in [MJ / K^4 m° day]
     * and 273.16 K to calculate absolute temperatures
     * @param tmean the air temperature [°C]
     * @param ea actual vapour pressure [kPa]
     * @param Rs actual solar radiation [MJ / m° day]
     * @param Rs0 the clear sky solar radiation [MJ / m° day]
     * @return the net (outgoing) longwave radiation [MJ / m° day]
     */    
    public static double calc_DailyNetLongwaveRadiation(double tmean, double ea, double Rs, double Rs0, boolean debug){
        double tabs = tmean + 273.16;
        /** the Stefan Bolztmann constant in [MJ / K^4 m° day] **/
        final double BOLTZMANN = 4.903E-9; 
        
        double Rnl = BOLTZMANN * Math.pow(tabs,4) * (0.34 - 0.14 * Math.sqrt(ea)) * (1.35 * (Rs/Rs0) - 0.35);
        
        if(debug)
            System.out.println("Tmean: " + tmean + "\n" +
                                         "ea: " + ea + "\n" +
                                         "Rs: " + Rs + "\n" +
                                         "Rs0: " + Rs0 + "\n" +
                                         "B: " + BOLTZMANN + "\n" +
                                         "Rnl: " + Rnl);
                    
        return Rnl;
    }
    
    
    
    /**
     * estimates the soil heat flux [MJ / m° day]
     * @param Rn the daily net radiation [MJ / m° day]
     * @param N the potential sunshine hours = daylength [hours]
     * @return the daily soil heat flux [MJ / m° day]
     */    
    public static double calc_SoilHeatFlux(double Rn, double N){
        double G;
        //day time
        double Gd = 0.1 * Rn * N / 24;
        //night time
        double Gn = 0.5 * Rn * ((24-N) / 24);
        return G = Gd + Gn;
    }
    
    /**
     * @param lat - the latitude in decimal degree
     * @param julDay - the julian day count
     * @return dayFraction - bright decimal part of the day
     */
    public static double calcDayFraction(double lat, int julDay){
    	double declination = org.unijena.j2k.physicalCalculations.SolarRadiationCalculationMethods.calc_SunDeclination(julDay);
        double latRad = org.unijena.j2k.mathematicalCalculations.MathematicalCalculations.deg2rad(lat);
        double sunsetHourAngle = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_SunsetHourAngle(latRad, declination);
        double maximumSunshine = org.unijena.j2k.physicalCalculations.DailySolarRadiationCalculationMethods.calc_maximumSunshineHours(sunsetHourAngle);
        double dayFraction = maximumSunshine / 24.0;
        if(dayFraction > 1){
        	System.out.println("Problem in DailySolarRadiationCalculationMethods: Day fraction is larger than 24");
        	dayFraction = 1;
        }
        if(dayFraction < 0){
        	System.out.println("Problem in DailySolarRadiationCalculationMethods: Day fraction is smaller than 24");
        	dayFraction = 0;
        }
        
        return dayFraction;
    }
    
}
