/*
 * SolarRadiationCalculationMethods.java
 *
 * Created on 24. November 2005, 12:02
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
public class SolarRadiationCalculationMethods {
    
    /** Creates a new instance of SolarRadiationCalculationMethods */
    public SolarRadiationCalculationMethods() {
    }
    
    /**
     * calculates the declination of the sun at the given julian day in rad
     * @param julDay the julian day count 1 .. 365,366
     * @return the declination in rad
     */    
    public static double calc_SunDeclination(int julDay){
        double declination = 0.40954 * Math.sin(0.0172*(julDay - 79.35));
        //declination = j2k_org.tools.NumericTools.rad2deg(declination);
        //double declination = 0.409 * Math.sin(((2*Math.PI / 365) * julDay) - 1.39);
        return declination;
    }
    
    /**
     * Calculates the solar constant for the julian day
     * @param julDay the julian day count [1 ... 365,366]
     * @return the solar constant in MJ/m°min.
     */    
    public static double calc_SolarConstant(int julDay){
        //solar constant in J / m° min
        double S = 81930 + 2910 * Math.cos(Math.PI / 180 * (julDay - 15));
        // J --> MJ
        S = S / 1000000;
        
        return S;
    }
    
    /**
     * calculates the inverse relative distance Earth-Sun in rad.
     * @param julDay the julian day count 1 .. 365,366
     * @return the inverse relative distance Earth-sun [rad.]
     */    
    public static double calc_InverseRelativeDistanceEarthSun(int julDay){
        double irdes = 1 + 0.033 * Math.cos((2 * Math.PI / 365)*julDay);
        return irdes;
    }
    
    /**
     * calculates the daily solar or shortwave radiation
     * @param s the actual (measured) sunshine hours [h]
     * @param s0 the maximum possible sunshine hours [hour]
     * @param Ra the daily extraterrestrial radiation [MJ / m° day or hour]
     * @return the daily solar radiation [MJ / m° day or hour]
     */    
    public static double calc_SolarRadiation(double s, double s0, double Ra, double angstrom_a, double angstrom_b){
        double Rs;
        //avoid division by zero error during nighttimes and hourly time steps
        if(s0 > 0){
            //0.25 and 0.5 are recommended by Allen et al. 1998
            Rs = (angstrom_a + angstrom_b * (s / s0)) * Ra;
        }
        else
            Rs = 0;
        return Rs;
    }
    
    /**
     * calculates the daily net shortwave radiation resulting from the balance between incoming
     * and reflected solar radiation
     * @param albedo the albedo of the landcover [-]
     * @param Rs the daily solar radiation [MJ / m° day or hour]
     * @return net solar or shortwave radiation [MJ / m° day or hour]
     */    
    public static double calc_NetShortwaveRadiation(double albedo, double Rs){
        double Rns = (1 - albedo) * Rs;
        return Rns;
    }
    
    /**
     * calculates the net radiation
     * @param Rns the daily net solar or shortwave radiation [MJ / m° day or hour]
     * @param Rnl the daily net longwave radiation [MJ / m° day or hour]
     * @return the daily net radiation [MJ / m° day] or hour
     */    
    public static double calc_NetRadiation(double Rns, double Rnl){
        double Rn = Rns - Rnl;
        if(Rn < 0)
            Rn = 0;
        return Rn;
    }
    
    /**
     * calculates the daily clear sky solar radiation RS0
     * @param elevation the elevation of the point of interest [m a.s.l]
     * @param Ra the daily extraterrestrial radiation [MJ / m° day or hour]
     * @return the daily clear sky solar radiation [MJ / m° day or hour]
     */    
    public static double calc_ClearSkySolarRadiation(double elevation, double Ra){
        double cssr = (0.75 + 2E-5 * elevation) * Ra;
        return cssr;
    }
}
