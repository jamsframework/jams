/*
 * ClimateIndicesForest.java
 * Created on 06.02.2023, 23:21:39
 *
 * This file is part of JAMS
 * Copyright (C) FSU Jena
 *
 * JAMS is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * JAMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JAMS. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jams.components.indices;

import jams.JAMS;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author Sven Kralisch <sven.kralisch@tlubn.thueringen.de>
 */
@JAMSComponentDescription(
        title = "ClimateIndicesForest",
        author = "Sven Kralisch",
        description = "Calculates the climatic water balance within the forest growing season")
public class ClimateIndicesForest extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "Current time")
    public Attribute.Calendar time;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily mean temperatur",
            unit = "°C")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily precipitation",
            unit = "L")
    public Attribute.Double precip;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "daily pot. evapotranspiration",
            unit = "L")
    public Attribute.Double potET;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "area of unit",
            unit = "m²")
    public Attribute.Double area;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
            description = "weight for calculating normalized climatic water balance, e.g. 30.5 for monthly values",
            defaultValue = "1")
    public Attribute.Double normalization_weight;    

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "current length of the forest vegetation period")
    public Attribute.Double forestVegetationPeriodLength;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "start day of the forest vegetation period")
    public Attribute.Double forestVegetationPeriodStart;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "end day of the forest vegetation period")
    public Attribute.Double forestVegetationPeriodEnd;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the climatic water balance (P-potET)",
            unit = "mm")
    public Attribute.Double KWB;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the climatic water balance (P-potET) during forest vegetation period",
            unit = "mm")
    public Attribute.Double KWBinForestVegetationPeriod;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during forest vegetation period",
            unit = "mm")
    public Attribute.Double KWBinForestVegetationPeriodNorm;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during Apr-Jun",
            unit = "mm")
    public Attribute.Double KWBinVP1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during Jul-Sep",
            unit = "mm")
    public Attribute.Double KWBinVP2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during Apr-Sep",
            unit = "mm")
    public Attribute.Double KWBinVP12;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 1 (3-5)",
            unit = "mm")
    public Attribute.Double KWBinS1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 2 (6-8)",
            unit = "mm")
    public Attribute.Double KWBinS2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 3 (9-11)",
            unit = "mm")
    public Attribute.Double KWBinS3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 4 (12-2)",
            unit = "mm")
    public Attribute.Double KWBinS4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during Apr-Jun",
            unit = "mm")
    public Attribute.Double potETinVP1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during Jul-Sep",
            unit = "mm")
    public Attribute.Double potETinVP2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during Apr-Sep",
            unit = "mm")
    public Attribute.Double potETinVP12;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 1 (3-5)",
            unit = "mm")
    public Attribute.Double potETinS1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 2 (6-8)",
            unit = "mm")
    public Attribute.Double potETinS2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 3 (9-11)",
            unit = "mm")
    public Attribute.Double potETinS3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
            description = "calculates the normalized climatic water balance (P-potET) during season 4 (12-2)",
            unit = "mm")
    public Attribute.Double potETinS4;

    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "tmp variables")
    public Attribute.DoubleArray tmp;

    private final int INDEX_successiveDaysWithTmeanAboveTenDegree = 0;
    private final int INDEX_KWB_in_forest_vegetation_period = 1;
    private final int INDEX_KWB_in_forest_vegetation_period_old = 2;
    private final int INDEX_FVB_end_day = 3;
    private final int INDEX_SIZE = INDEX_FVB_end_day + 1;

    @Override
    public void run() {

        int day = time.get(Attribute.Calendar.DAY_OF_YEAR);
        int maxDay = time.getActualMaximum(Attribute.Calendar.DAY_OF_YEAR);

        if (tmp.getValue() == null) {
            tmp.setValue(new double[INDEX_SIZE]);
        }
        double[] inTmp = tmp.getValue();

        if (day == 1) {
            forestVegetationPeriodStart.setValue(0);
            forestVegetationPeriodEnd.setValue(0);
            forestVegetationPeriodLength.setValue(0);
            KWBinForestVegetationPeriod.setValue(JAMS.getMissingDataValue());
            KWBinForestVegetationPeriodNorm.setValue(JAMS.getMissingDataValue());
            inTmp[INDEX_KWB_in_forest_vegetation_period] = 0;
        }

        //Vegetationsperioden
        if (tmean.getValue() >= 10.0) {
            inTmp[INDEX_successiveDaysWithTmeanAboveTenDegree] += 1;
        } else {
            //tmean<10° and 5+-day-period above 10° before
            if (inTmp[INDEX_successiveDaysWithTmeanAboveTenDegree] >= 5) {
                inTmp[INDEX_KWB_in_forest_vegetation_period_old] = inTmp[INDEX_KWB_in_forest_vegetation_period];
                inTmp[INDEX_FVB_end_day] = day - 1;
            }
            inTmp[INDEX_successiveDaysWithTmeanAboveTenDegree] = 0;

            //vor Start der FVP?
            if (forestVegetationPeriodStart.getValue() == 0) {
                inTmp[INDEX_KWB_in_forest_vegetation_period] = 0;
            }
        }

        if (inTmp[INDEX_successiveDaysWithTmeanAboveTenDegree] == 5
                && forestVegetationPeriodStart.getValue() == 0) {
            forestVegetationPeriodStart.setValue(day - 4);
        }

        //Klimatische Wasserbilanz
        double KWB_mm = (precip.getValue() - potET.getValue()) / area.getValue();
        KWB.setValue(KWB_mm);

        //nach Start der FVP?
        if (forestVegetationPeriodStart.getValue() == 0) {
            if (tmean.getValue() >= 10.0) {
                inTmp[INDEX_KWB_in_forest_vegetation_period] += KWB_mm;
            }
        } else {
            inTmp[INDEX_KWB_in_forest_vegetation_period] += KWB_mm;
        }

        //reset counters.. 
        if (day == maxDay) {
            if (inTmp[INDEX_successiveDaysWithTmeanAboveTenDegree] >= 5) {
                inTmp[INDEX_KWB_in_forest_vegetation_period_old] = inTmp[INDEX_KWB_in_forest_vegetation_period];
                inTmp[INDEX_FVB_end_day] = day - 1;
            }
            KWBinForestVegetationPeriod.setValue(inTmp[INDEX_KWB_in_forest_vegetation_period_old]);
            forestVegetationPeriodEnd.setValue(inTmp[INDEX_FVB_end_day]);
            forestVegetationPeriodLength.setValue(forestVegetationPeriodEnd.getValue() - forestVegetationPeriodStart.getValue() + 1);
            KWBinForestVegetationPeriodNorm.setValue(KWBinForestVegetationPeriod.getValue() * 
                    normalization_weight.getValue() / forestVegetationPeriodLength.getValue());

            inTmp[INDEX_KWB_in_forest_vegetation_period] = 0;
        }
        
        double potET_mm = potET.getValue() / area.getValue();
        
        int month = time.get(Attribute.Calendar.MONTH);
        if (month >= 3 && month <= 5) {
            KWBinVP1.setValue(KWB_mm);
            potETinVP1.setValue(potET_mm);
        } else {
            KWBinVP1.setValue(0);
            potETinVP1.setValue(0);
        }   
        if (month >= 6 && month <= 8) {
            KWBinVP2.setValue(KWB_mm);
            potETinVP2.setValue(potET_mm);
        } else {
            KWBinVP2.setValue(0);
            potETinVP2.setValue(0);
        }
        if (month >= 3 && month <= 8) {
            KWBinVP12.setValue(KWB_mm);
            potETinVP12.setValue(potET_mm);
        } else {
            KWBinVP12.setValue(0);
            potETinVP12.setValue(0);
        }                          
        if (month >= 2 && month <= 4) {
            KWBinS1.setValue(KWB_mm);
            potETinS1.setValue(potET_mm);
        } else {
            KWBinS1.setValue(0);   
            potETinS1.setValue(0);
        }                          
        if (month >= 5 && month <= 7) {
            KWBinS2.setValue(KWB_mm);
            potETinS2.setValue(potET_mm);
        } else {
            KWBinS2.setValue(0);    
            potETinS2.setValue(0);
        }                          
        if (month >= 8 && month <= 10) {
            KWBinS3.setValue(KWB_mm);
            potETinS3.setValue(potET_mm);
        } else {
            KWBinS3.setValue(0);    
            potETinS3.setValue(0);
        }                          
        if (month >= 11 || month <= 1) {
            KWBinS4.setValue(KWB_mm);
            potETinS4.setValue(potET_mm);
        } else {
            KWBinS4.setValue(0);    
            potETinS4.setValue(0);
        }                          

    }

}
