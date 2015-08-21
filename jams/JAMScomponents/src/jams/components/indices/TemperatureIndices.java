/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.components.indices;

import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import java.util.Calendar;

/**
 *
 * @author christian
 */
@JAMSComponentDescription(
        title = "KlimaKennwerte",
        author = "Christian Fischer",
        description = "Calculated standard Klimakennwerte for J2000Klima")
public class TemperatureIndices extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily min temperatur",
            unit = "°C")
    public Attribute.Double tmin;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily mean temperatur",
            unit = "°C")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily max temperatur",
            unit = "°C")
    public Attribute.Double tmax;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "relative humidity",
            unit = "percent")
    public Attribute.Double relHum;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "absolute humidity",
    unit = "g/m³")
    public Attribute.Double absHum;
        
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if hitzetag")
    public Attribute.Double isHotDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "number of hot days without interruption")
    public Attribute.Double successiveHotDays;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if hot period (more than 5 hot days) has just started")
    public Attribute.Double isBeginningOfHotPeriod;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if hot period (more than 5 hot days)")
    public Attribute.Double isHotPeriod;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "threshold for hot days",
            defaultValue = "30",
            unit = "°C")
    public Attribute.Double hotDayThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if tropennacht")
    public Attribute.Double isTropicalNight;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "threshold for tropennacht",
            defaultValue = "20",
            unit = "°C")
    public Attribute.Double tropicalNightThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if summerday")
    public Attribute.Double isSummerDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "threshold for summerday",
            defaultValue = "25",
            unit = "°C")
    public Attribute.Double summerDayThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if day is attractive for tourists (temp > 15 and < 30) and less than 0.5 mm rain TODO")
    public Attribute.Double isTouristDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if frostday")
    public Attribute.Double isFrostDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "threshold for frostday",
            defaultValue = "0",
            unit = "°C")
    public Attribute.Double frostDayThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "threshold for frostday",
            defaultValue = "-5",
            unit = "°C")
    public Attribute.Double permanentFrostDayThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "number of hitzetage without interruption")
    public Attribute.Double successivePermanentFrostDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if hitzeperiode has just started")
    public Attribute.Double isBeginningOfPermanentFrostPeriod;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if hitzeperiode")
    public Attribute.Double isPermanentFrostPeriod;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if eistag")
    public Attribute.Double isIceDay;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "threshold for iceday",
            defaultValue = "0",
            unit = "°C")
    public Attribute.Double iceDayThreshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "1 if tmin is/was below 0°C")
    public Attribute.Double isTempBelowZero;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "1 if frost/tau has been changed")
    public Attribute.Double isFrostDefrostChange;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates cooling degree days")
    public Attribute.Double coolingDegreeDays;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates heating days where tmean is below 15°C")
    public Attribute.Double isHeatDay;


    @Override
    public void run() {
        double tmean = this.tmean.getValue();

        isFrostDefrostChange.setValue(0.0);
        isHotDay.setValue(0.0);
        isHotPeriod.setValue(0.0);
        isBeginningOfHotPeriod.setValue(0.0);
        isSummerDay.setValue(0.0);
        isTropicalNight.setValue(0.0);
        isFrostDay.setValue(0.0);
        isPermanentFrostPeriod.setValue(0.0);
        isBeginningOfPermanentFrostPeriod.setValue(0.0);
        isIceDay.setValue(0.0);
        isHeatDay.setValue(0.0);
        isTouristDay.setValue(0.0);

        //Frosttauwechsel .. muss zuerst ausgeführt werden
        if (isTempBelowZero.getValue() == 1.0 && tmin.getValue() > 0.0) {
            isFrostDefrostChange.setValue(1.0);
        }

        isTempBelowZero.setValue(0.0);

        //Standardwerte .. Frosttage, Tropische Nächte .. 
        if (tmin.getValue() < 0.0) {
            isTempBelowZero.setValue(1.0);
        }

        if (tmax.getValue() >= summerDayThreshold.getValue()) {
            isSummerDay.setValue(1.0);
        }

        if (tmin.getValue() >= tropicalNightThreshold.getValue()) {
            isTropicalNight.setValue(1.0);
        }

        //Hitzeperioden und Hitzetage
        if (tmax.getValue() >= hotDayThreshold.getValue()) {
            isHotDay.setValue(1.0);
            successiveHotDays.setValue(successiveHotDays.getValue() + 1);
            if (successiveHotDays.getValue() == 5.0) {
                isHotPeriod.setValue(5.0);
                isBeginningOfHotPeriod.setValue(1.0);
            } else if (successiveHotDays.getValue() > 5.0) {
                isHotPeriod.setValue(1.0);

            }
        } else {
            successiveHotDays.setValue(0.0);
        }

        if (tmin.getValue() > tmean) {
            System.out.println("Ups .. tmin (" + tmin + ") ist größer als tmean (" + tmean + ")");
        }

        //Frosttage und Frostperioden
        if (tmin.getValue() < frostDayThreshold.getValue()) {
            isFrostDay.setValue(1.0);
        }

        if (tmin.getValue() < permanentFrostDayThreshold.getValue()) {
            successivePermanentFrostDay.setValue(successivePermanentFrostDay.getValue() + 1);
            if (successivePermanentFrostDay.getValue() == 3.0) {
                isPermanentFrostPeriod.setValue(3.0); //3 days in permanent frost period
                isBeginningOfPermanentFrostPeriod.setValue(1.0);
            } else if (successivePermanentFrostDay.getValue() >= 3.0) {
                isPermanentFrostPeriod.setValue(1.0);
            }
        } else {
            successivePermanentFrostDay.setValue(0.0);
        }

        if (tmax.getValue() < iceDayThreshold.getValue()) {
            isIceDay.setValue(1.0);
        }

        //Heiz- und Kühlgradtage
        coolingDegreeDays.setValue(Math.max(tmean - 18.3, 0.0));

        //make sure months are zero-based        
        if (time.get(Attribute.Calendar.MONTH) >= 8 || time.get(Attribute.Calendar.MONTH) < 5) {
            if (tmean < 15) {
                isHeatDay.setValue(1.0);
            }
        }

        //calculate saturation vapour pressure
        double est = 610.8 * Math.exp(17.1 * tmean / (234.2 + tmean));
        double maxHum = est * 0.018 / 8.314 / (273.15 + tmean) * 1000;
        double absHum = relHum.getValue() / 100. * maxHum;
        this.absHum.setValue(absHum);
    }

}
