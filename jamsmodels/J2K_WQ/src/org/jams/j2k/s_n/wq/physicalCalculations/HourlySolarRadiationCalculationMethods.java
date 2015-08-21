/*
 * HourlySolarRadiationCalculationMethods.java
 *
 * Created on 13. Januar 2006, 12:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.jams.j2k.s_n.wq.physicalCalculations;

import jams.data.Attribute;

/**
 *
 * @author c0krpe
 */
public class HourlySolarRadiationCalculationMethods {
    
    /** Creates a new instance of HourlySolarRadiationCalculationMethods */
    public HourlySolarRadiationCalculationMethods() {
    }
    
    /**
     * calculates the mid time hour angle between two time steps. The hour angle is defined as the angle of the sun before
     * noon. It is assumed that the sun moves 15° per hour. At times before noon the hour
     * angle is positive at noon it is zero and after noon the hour angle is negative.
     * @return the hour angle in dec. degree
     * @param localTimeDateObject the current J2KDate object transferred to real local time
     * @param julDay the julian day count [1 ... 365,366]
     * @param longSite the longitude of the point of interest in deg. west from Greenwich
     * @param longTZ the longitude of the time zone in deg. west from Greenwich
     */
    public static double calc_midTimeHourAngle(jams.data.JAMSCalendar time, int julDay, double longSite, double longTZ, boolean debug){
        long hour = time.get(Attribute.Calendar.HOUR_OF_DAY);//localTimeDateObject.decTime();
        long minute = time.get(Attribute.Calendar.MINUTE);
        double decMin = minute / 60;
        double decTime = hour + decMin;
        if(decTime >= 24.0)
            decTime = decTime - 24;
        double midTime = decTime + 0.5;
        double b = (2 * Math.PI * (julDay - 81)) / 364;
        double Sc = 0.1645 * Math.sin(2*b)-0.1255 * Math.cos(b) - 0.025 * Math.sin(b);
        double midTimeHourAngle = Math.PI / 12 * ((midTime + 0.06667 * (longTZ - longSite) + Sc) - 12);
        
        if(debug){
            System.out.println("midTime: " + midTime + "\n" +
                    "decTime: " + decTime + "\n" +
                    "b: " + b + "\n" +
                    "Sc: " + Sc + "\n" +
                    "midTimeHourAngle: " + midTimeHourAngle);
        }
        return midTimeHourAngle;
    }

    public static double calc_midTimeHourAngle(long hour, int julDay, double longSite, double longTZ, boolean debug){
        //long hour = time.get(time.HOUR_OF_DAY);//localTimeDateObject.decTime();
        long minute = 0;
        double decMin = minute / 60;
        double decTime = hour + decMin;
        if(decTime >= 24.0)
            decTime = decTime - 24;
        double midTime = decTime + 0.5;
        double b = (2 * Math.PI * (julDay - 81)) / 364;
        double Sc = 0.1645 * Math.sin(2*b)-0.1255 * Math.cos(b) - 0.025 * Math.sin(b);
        double midTimeHourAngle = Math.PI / 12 * ((midTime + 0.06667 * (longTZ - longSite) + Sc) - 12);
        
        if(debug){
            System.out.println("midTime: " + midTime + "\n" +
                    "decTime: " + decTime + "\n" +
                    "b: " + b + "\n" +
                    "Sc: " + Sc + "\n" +
                    "midTimeHourAngle: " + midTimeHourAngle);
        }
        return midTimeHourAngle;
    }
    
    /**
     * calculates the hour angle at the beginning of the current hourly time step
     * @param midTimeHourAngle the hour angle at the mid point of the current hourly time step [rad]
     * @return the hour angle at the beginning of the current hourly time step [rad]
     */
    public static double calc_startTimeHourAngle(double midTimeHourAngle){
        double startTimeHourAngle=(midTimeHourAngle - (Math.PI * 1/24));
        return startTimeHourAngle;
    }
    
    /**
     * calculates the hour angle at the end of the current hourly time step
     * @param midTimeHourAngle the hour angle at the mid point of the current hourly time step [rad]
     * @return the hour angle at the end of the current hourly time step [rad]
     */
    public static double calc_endTimeHourAngle(double midTimeHourAngle){
        double endTimeHourAngle=(midTimeHourAngle + (Math.PI * 1/24));
        return endTimeHourAngle;
    }
    
    /**
     * calculates the extraterrestrial radiation of the given time step
     * @param Gsc the solar constant of the current time step [MJ/m²min]
     * @param relDist the relative distance Earth -- Sun [rad]
     * @param w1 the hour angle at the beginning of the time step [rad]
     * @param w2 the hour angle at the end of the time step [rad]
     * @param radLat the latitude of the point of interest [rad]
     * @param decl the declination of the sun [rad]
     * @return the extraterrestial radiation [MJ / m² hour]
     */
    public static double calc_HourlyExtraterrestrialRadiation(double Gsc, double relDist, double w1, double w2, double radLat, double decl ){
        double Ra =((12*60)/Math.PI) * Gsc * relDist *((w2 - w1) * Math.sin(radLat) * Math.sin(decl) + Math.cos(radLat) * Math.cos(decl) *(Math.sin(w2)-Math.sin(w1)));
        //no negative radiation !!
        if(Ra < 0)
            Ra = 0;
        return Ra;
    }
    
    /**
     * calculates the maximum sunshine duration of the current time step by the simple
     * assumption that it is night when no radiation occurs and day when radiation occurs.
     * This is not really true for the first and last hour of the day, but works.
     * @param Ra the extraterrestrial radiation
     * @return the maximum sunshine duration of the time step [hours]
     */
    public static double calc_HourlyMaxSunshine(double Ra){
        double N;
        if(Ra > 0)
            N = 1;
        else
            N = 0;
        return N;
    }
    
    /**
     * calculates the net (outgoing) longwave radiation uses the
     * Stefan Bolztmann constant in [MJ / K^4 m² hour]
     * and 273.16 K to calculate absolute temperatures
     * @param tmean the air temperature [°C]
     * @param ea actual vapour pressure [kPa]
     * @param Rs actual solar radiation [MJ / m² hour]
     * @param Rs0 the clear sky solar radiation [MJ / m² hour]
     * @return the net (outgoing) longwave radiation [MJ / m² hour]
     */
    public static double calc_HourlyNetLongwaveRadiation(double tmean, double ea, double Rs, double Rs0, double Rs_Rs0_t0, boolean debug){
        double relGlobRad = 0;
        double tabs = tmean + 273.16;
        /** the Stefan Bolztmann constant in [MJ / K^4 m² hour] **/
        final double BOLTZMANN = 2.043E-10;
        double Rnl;
        if(Rs0 > 0){
            relGlobRad = Rs / Rs0;
        } else
            relGlobRad = Rs_Rs0_t0;
        
        Rnl = BOLTZMANN * Math.pow(tabs,4) * (0.34 - 0.14 * Math.sqrt(ea)) * (1.35 * (relGlobRad) - 0.35);
        
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
     * estimates the soil heat flux [MJ / m² hour]
     * @param Rn the daily net radiation [MJ / m² hour]
     * @param N the potential sunshine hours = daylength [hours]
     * @return the daily soil heat flux [MJ / m² hour]
     */
    public static double calc_SoilHeatFlux(double Rn, double N){
        double G;
        //day time
        if(N > 0)
            G = 0.1;
        //night time
        else
            G = 0.5 * Rn;
        return G;
    }
    
    /**
     * calculates the influence of the slope;aspect combination of point of interest
     * on the incoming radiation
     * @param sun_elevation_angle the sun elevation angle at the given date and time [dec. degree]
     * @param azimut the azimut of the sun at the given date and time [dec. degree]
     * @param slope the slope of the point of interest [dec. degree]
     * @param aspect the aspect of the point of interest [dec. degree] (0 = N, 90 = E, 180 = S, 270 = W)
     * @return the multiplicative slope aspect correction factor
     */
    public static double calc_SlopeAspectCorrectionFactor(double sun_elevation_angle, double azimut, double slope, double aspect){
        double SACF;
        if(aspect >= 0){
            double asp = 180 - aspect;
            double sintheta = Math.sin(sun_elevation_angle) * Math.cos(slope)
            + Math.cos(sun_elevation_angle) * Math.cos(asp - azimut) * Math.sin(slope);
            
            SACF = sintheta / Math.sin(sun_elevation_angle);
        } else
            SACF = 1;
        return SACF;
    }
    
}
