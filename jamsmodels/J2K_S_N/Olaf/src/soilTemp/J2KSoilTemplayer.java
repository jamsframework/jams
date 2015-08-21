package soilTemp;
/*
 * J2KSoilTemplayer.java
 * Created on 19. February 2006, 14:50
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, c8fima
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
import ages.types.HRU;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Author
    (name = "Manfred Fink")
@Description
    ("Calculates soil temperature at different depths")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilTemp/J2KSoilTemplayer.java $")
@VersionInfo
    ("$Id: J2KSoilTemplayer.java 994 2010-02-19 20:44:19Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KSoilTemplayer  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + J2KSoilTemplayer.class.getSimpleName());

    @Description("Temperature lag coefficient perhaps to calibrate, typcal value 0.8, range  0 - 1")
    @Role(PARAMETER)
    @In public double temp_lag;

    @Description("switch for mulch drilling scenario")
    @Role(PARAMETER)
    @In public double sceno;

    @Description("HRU attribute name area")
    @In public double area;

    @Description("daily max. temperature")
    @Unit("ï¿½C")
    @In public double tmax;

    @Description("daily min. temperature")
    @Unit("ï¿½C")
    @In public double tmin;

    @Description("anual mean temperature")
    @Unit("ï¿½C")
    @In public double tmeanavg;

    @Description("depth of soil layer")
    @Unit("mm")
    @In public double[] depth_h ;
    
    @Description("number of layers in soil profile in")
    @In public int horizons;

    @Description("Soil bulk density")
    @Unit("g/cmÂ³")
    @In public double[] bulk_density_h ;
    
    @Description("actual LPS in portion of sto_LPS soil water content")
    @In public double[] satLPS_h ;

    @Description("actual MPS in portion of sto_MPS soil water content")
    @In public double[] satMPS_h ;

    @Description("maximum MPS  in l soil water content")
    @In public double[] maxMPS_h ;
    
    @Description("maximum LPS  in l soil water content")
    @In public double[] maxLPS_h ;
    
    @Description("snowcover water aequivalent")
    @Unit("mm")
    @In public double snowTotSWE;
    
    @Description("global radiation")
    @Unit("MJ/(mÂ²*d)")
    @In public double solRad;
    
    @Description("soil temperature in different layerdepths")
    @Unit("ï¿½C")
    @In @Out public double[] soil_Temp_Layer ;

    @Description("aboveground biomass")
    @Unit("kg/ha")
    @In public double BioagAct;

    @Description(" Residue in Layer")
    @Unit("kgN/ha")
    @In public double[] residue_pool ;
    
    @Description("Output soil surface temperature")
    @Unit("C")
    @Out public double surfacetemp;
    
    @Description("Output soil average temperature")
    @Unit("C")
    @Out public double soil_Tempaverage;

    @Description("Current hru object")
    @In @Out public HRU hru;

    double[] Soiltemp_hor;
    double Soil_Temp;
    double Soil_Temp1;
    double surfacet;
    double radiat;
    double suml_depth;
    double total_depth;
   
    @Execute
    public void execute() {
        if (soil_Temp_Layer == null) {
            // should be initialized earlier (pre)
            soil_Temp_Layer = new double[horizons];
        }
        suml_depth = 0;
        total_depth = 0;
        Soiltemp_hor = soil_Temp_Layer;

        for (int i = 0; i < horizons; i++) {
            double l_depth = depth_h[i] * 10;
            total_depth = total_depth + l_depth;
        }
        radiat = solRad;
        
        double Soil_Temp_Sum = 0;
        for (int i = 0; i < horizons; i++) {
            double l_depth = depth_h[i] * 10;
            suml_depth = suml_depth + l_depth;
            Soil_Temp = Soiltemp_hor[i];
            if (i == 0) {
                Soil_Temp1 = Soil_Temp;
            }
            double runSoil_Temp_Layer = calc_Soil_Temp_Layer(i);
            Soiltemp_hor[i] = runSoil_Temp_Layer;
            Soil_Temp_Sum = Soil_Temp_Sum + runSoil_Temp_Layer;
        }
        soil_Tempaverage = Soil_Temp_Sum / horizons;
        soil_Temp_Layer = Soiltemp_hor;
        if (log.isLoggable(Level.INFO)) {
            log.info("soil_Tempaverage:" + soil_Tempaverage);
        }
    }

    private double calc_Soil_Temp_Layer(int i) {
        double temp_lag1 = temp_lag;
        double anavgtemp = tmeanavg;
        double depthfactor = calc_Soil_Temp_Depth_Factor(i);
        double surfacetemp = calc_Soil_Surface_Temp();
        Soil_Temp = temp_lag1 * Soil_Temp + (1 - temp_lag1) * (depthfactor * (anavgtemp - surfacetemp) + surfacetemp);
        return Soil_Temp;
    }

    private double calc_water_content(int i) {
        double sto_LPS = maxLPS_h[i] / area;
        double sto_MPS = maxMPS_h[i] / area;

        double sto_FPS = 0.3 * sto_MPS; //     Swat definition of FPS
        double act_LPS = sto_LPS * satLPS_h[i];
        /** actual LPS in mm soil water content */
        double act_MPS = sto_MPS * satMPS_h[i];
        /** actual MPS in mm soil water content */
        double soilwater = act_LPS + act_MPS + sto_FPS;
        return soilwater;
    }

    private double calc_Soil_Temp_Depth_Factor(int i) {
        double dampingdepth = calc_Soil_Temp_Dampingdepth(i);
        double depthfactor = dampingdepth / (dampingdepth + (Math.exp(-0.867 - (2.078 * dampingdepth))));
        return depthfactor;
    }

    private double calc_Soil_Temp_Dampingdepth(int i) {
        double soil_bulk_dens = bulk_density_h[i];
        double soilwater = calc_water_content(i);
        double dd_max = 1000 + ((2500 * soil_bulk_dens) / (soil_bulk_dens + 686 * Math.exp(-5.63 * soil_bulk_dens)));
        double lamda = soilwater / ((0.356 - 0.144 * soil_bulk_dens) * total_depth);
        double dd = dd_max * Math.exp(Math.log(500 / dd_max) * ((1 - lamda) / (1 + lamda)) * ((1 - lamda) / (1 + lamda)));
        double dampingdepth = suml_depth / dd;
        return dampingdepth;
    }

    private double calc_Soil_Surface_Temp() {   /* after SWAT */
        double coverweightsnow;
        double coverweightveg;
        double coverweight;
        double temp_bare_soil;
        double snowcov = snowTotSWE;
        double vegetationcover = BioagAct;

        if (sceno == 1) {
            vegetationcover = BioagAct + residue_pool[0];
        }

        coverweightveg = vegetationcover / (vegetationcover + Math.exp(7.563 - (0.0001297 * vegetationcover)));
        coverweightsnow = snowcov / (snowcov + Math.exp(6.055 - (0.3002 * snowcov)));
        coverweight = Math.max(coverweightveg, coverweightsnow);

        // temp_bare_soil = temp_mean + epsilon_solar * ((temp_max - temp_min)/2); /*SWAT Orginal*/
        /*       Combination of SWAT and Epic used to Calculate bare Soiltemp*/
        temp_bare_soil = calc_Soil_Surface_Temp2();
        surfacet = (coverweight * Soil_Temp1) + ((1 - coverweight) * temp_bare_soil);
        surfacetemp = surfacet;
        return surfacet;
    }

    private double calc_Soil_Surface_Temp2() {   /* after ArcEgmo  "Williams-algorithm"*/
        double albedofactor;
        double temp_min = tmin;
        double temp_max = tmax;
        double temp_bare_soil;

        albedofactor = 0.01; /*modified for bare Soil*/
        temp_bare_soil = (1 - albedofactor) * (temp_min + (temp_max - temp_min)
                * Math.pow(0.03 * radiat, 0.5)) + surfacet * albedofactor;
        return temp_bare_soil;
    }
}

