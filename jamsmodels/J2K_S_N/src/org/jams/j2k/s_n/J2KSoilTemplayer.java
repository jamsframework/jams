package org.jams.j2k.s_n;
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

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="J2KSoilTemplayer",
        author="Manfred Fink",
        description="Calculates soil temperature in diffrent depths"
        )
        public class J2KSoilTemplayer extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area in m²"
            )
            public Attribute.Double area;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C daily max. temperature"
            )
            public Attribute.Double atemp_max;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C dayly min. temperature"
            )
            public Attribute.Double atemp_min;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C dayly mean. temperature"
            )
            public Attribute.Double atemp_mean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in °C anual mean temperature"
            )
            public Attribute.Double anatemp_mean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil layer"
            )
            public Attribute.DoubleArray layerdepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil profile"
            )
            public Attribute.Double totaldepth;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of layers in soil profile in [-]"
            )
            public Attribute.Double Layer;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Soil bulk density in g/cm³"
            )
            public Attribute.DoubleArray bulk_density_h;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Temperature lag coefficient perhaps to calibrate, typcal value 0.8, range  0 - 1"
            )
            public Attribute.Double temp_lag;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual LPS in portion of sto_LPS soil water content"
            )
            public Attribute.DoubleArray Sat_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual MPS in portion of sto_MPS soil water content"
            )
            public Attribute.DoubleArray Sat_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS  in l soil water content"
            )
            public Attribute.DoubleArray stohru_MPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS  in l soil water content"
            )
            public Attribute.DoubleArray stohru_LPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum FPS  in l soil water content"
            )
             public Attribute.DoubleArray stohru_FPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snowcover in mm water aequivalent"
            )
            public Attribute.Double snowcover;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soilalbedo"
            )
            public Attribute.Double soilalbedo;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "global radiation in MJ/(m²*d)"
            )
            public Attribute.Double radiation;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "soil temperature in different layerdepths in °C"
            )
            public Attribute.DoubleArray Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "in °C *  Output soil surface temperature"
            )
            public Attribute.Double Surfacetemp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "in °C *  Output soil average temperature"
            )
            public Attribute.Double Soil_Tempaverage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "aboveground biomass in kg/ha"
            )
            public Attribute.Double biomass;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = " Residue in Layer in kgN/ha"
            )
            public Attribute.DoubleArray Residue_pool;

    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "switch for mulch drilling scenario"
            )
            public Attribute.Double sceno;
    
 
    double[] Soiltemp_hor;
    double Soil_Temp;
    double Soil_Temp1;
    double surfacet;
    double radiat;
    double suml_depth;
    double total_depth;
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
    }
    
    public void run() {
        int i = 0;
        int layer = (int)Layer.getValue();
        double Soil_Temp_Sum = 0;
        double Soil_Temp_avg = 0;
        suml_depth = 0;
        total_depth = 0;
        Soiltemp_hor = Soil_Temp_Layer.getValue();
        while (i < layer){
        double l_depth = layerdepth.getValue()[i] * 10;
        total_depth = total_depth + l_depth;
        i++;
        }
        i = 0;
       
        this.radiat = radiation.getValue();
        
        
        while (i < layer){
            double l_depth = layerdepth.getValue()[i] * 10;
            suml_depth = suml_depth + l_depth;
            this.Soil_Temp = Soiltemp_hor[i];
            if (i == 0){
                Soil_Temp1 = Soil_Temp;
            }
            double runSoil_Temp_Layer = calc_Soil_Temp_Layer(i);
            Soiltemp_hor[i] = runSoil_Temp_Layer;
            Soil_Temp_Sum = Soil_Temp_Sum + runSoil_Temp_Layer;
            i++;
        }
        
        Soil_Temp_avg = Soil_Temp_Sum / layer;
        Soil_Tempaverage.setValue(Soil_Temp_avg);
        Soil_Temp_Layer.setValue(Soiltemp_hor);
    }
    
    private double calc_Soil_Temp_Layer(int i) {
        
        
        
        double temp_lag1 = temp_lag.getValue();
        double anavgtemp = anatemp_mean.getValue();
        
        
        double depthfactor = calc_Soil_Temp_Depth_Factor(i);
        double surfacetemp = calc_Soil_Surface_Temp();
        this.Soil_Temp = temp_lag1 * this.Soil_Temp + (1 - temp_lag1) * (depthfactor *(anavgtemp - surfacetemp)+surfacetemp);
        
        /**
         * Frostroitine
         * Wärmekapazität von Wasser 4.18 kJ/(kg K)
         * Wärmekapazität von Eis 2.1 kJ/(kg K)
         * Schmelzwärme von Wasser 332 kJ/(kg K)
         * Annahme ca. 33% des Bodens ist Wasser
         *
         *//*
          if ((Soil_Temp_Layer < 0 && Soil_Temp_Layer_1 > 0)||(Soil_Temp_Layer > 0 && Soil_Temp_Layer_1 < 0)) {
          Soil_Temp_Layer = 0.9924 * Soil_Temp_Layer_1 + (1 - 0.9924) * (depthfactor *(anatemp_mean-surfacetemp)+surfacetemp);
          }
            
          if (Soil_Temp_Layer < -1) {
          Soil_Temp_Layer = 0.7 * Soil_Temp_Layer_1 + (1 - 0.7) * (depthfactor *(anatemp_mean-surfacetemp)+surfacetemp) +3 ;
          }
        /**/
        return Soil_Temp;
    }
    
    private double calc_water_content(int i) {
        double soilwater = 0;
        double area_ = area.getValue();
        double sto_LPS = stohru_LPS.getValue()[i] / area_;
        double sto_MPS = stohru_MPS.getValue()[i] / area_;
//     double sto_FPS = stohru_FPS.getValue() / area_;
        
        double sto_FPS = 0.3 * sto_MPS; //     Swat definition of FPS
        double act_LPS = sto_LPS * Sat_LPS.getValue()[i];         /** actual LPS in mm soil water content */
        double act_MPS = sto_MPS * Sat_MPS.getValue()[i];         /** actual MPS in mm soil water content */
        
        soilwater = act_LPS + act_MPS + sto_FPS;
//     soilwater = act_LPS + act_MPS + (sto_MPS + sto_LPS) *  sto_FPS;
        
        return soilwater;
    }
    private double calc_Soil_Temp_Depth_Factor(int i) {
        double depthfactor;
        
        double dampingdepth = calc_Soil_Temp_Dampingdepth(i);
        
        depthfactor = dampingdepth / (dampingdepth + (Math.exp(-0.867-(2.078*dampingdepth)))) ;
        
        return depthfactor;
    }
    
    private double calc_Soil_Temp_Dampingdepth(int i) {
        double dampingdepth;
        double dd;
        double dd_max;
        double lamda;
        
        double soil_bulk_dens = bulk_density_h.getValue()[i];

        double soilwater = calc_water_content(i);
        
        dd_max = 1000 + ((2500*soil_bulk_dens)/(soil_bulk_dens+ 686*Math.exp(-5.63*soil_bulk_dens)) );
        
        lamda = soilwater/((0.356-0.144*soil_bulk_dens)*total_depth);
        
        dd = dd_max * Math.exp(Math.log(500/dd_max)*((1-lamda)/(1+lamda))*((1-lamda)/(1+lamda)));
        
        dampingdepth = suml_depth/dd ;
        
        return dampingdepth;
    }
    private double calc_Soil_Surface_Temp() {   /* after SWAT */
        double coverweightsnow;
        double coverweightveg;
        double coverweight;
        double epsilon_solar;
        double temp_bare_soil;
        
        
        double snowcov = snowcover.getValue();
        
// dummy calculation for vegetationcover /**vegetationcover estimated from measurements of Paul et. al. 2002*/
//          double vegetationcover =  728.3 * LAI_temp + 326;
           double vegetationcover = biomass.getValue();
          
           if (sceno.getValue() == 1){
               vegetationcover = biomass.getValue() + this.Residue_pool.getValue()[0];
           }
           
           vegetationcover =Math.min(vegetationcover,15);
           
           double radiation = radiat / 1000;
//        double temp_min = atemp_min.getValue();
//        double temp_max = atemp_max.getValue();
//        double temp_mean = atemp_mean.getValue();
/*        epsilon_solar = (entity.getDouble(aNameradiation.getValue())*(1-entity.getDouble(aNamesoilalbedo.getValue()))-14)/20;
 */
        //epsilon_solar = (radiation * (1 - 0.2) - 14) / 20;
        coverweightveg = vegetationcover/(vegetationcover + Math.exp(7.563-(0.0001297*vegetationcover)));
        
        coverweightsnow = snowcov/(snowcov + Math.exp(6.055-(0.3002*snowcov)));
        
        coverweight = Math.max(coverweightveg,coverweightsnow);
        
        
        // temp_bare_soil = temp_mean + epsilon_solar * ((temp_max - temp_min)/2); /*SWAT Orginal*/
        /*       Combination of SWAT and Epic used to Calculate bare Soiltemp*/
        temp_bare_soil = calc_Soil_Surface_Temp2();
        
        surfacet = (coverweight * Soil_Temp1) + ((1- coverweight) * temp_bare_soil);
        
        Surfacetemp.setValue(surfacet);
        
        
        return surfacet;
    }
    
    
    private double calc_Soil_Surface_Temp2() {   /* after ArcEgmo  "Williams-algorithm"*/
        double  albedofactor;
        double temp_min = atemp_min.getValue();
        double temp_max = atemp_max.getValue();
        double  temp_bare_soil;
        
//     albedofactor = soilalbedo * Math.exp(-0.5 * LAI) +  0.25 * (1 - Math.exp(-0.5 * LAI));/*orignal*/
        albedofactor = 0.01 ;/*modified for bare Soil*/
        temp_bare_soil = (1 - albedofactor) * (temp_min + (temp_max - temp_min) * Math.pow(0.03 * radiat, 0.5)) +  Surfacetemp.getValue() * albedofactor;
        
/*        if (unitnr < 2 && datumjul == 20 ) {
        j2k_org.core.message.deb_msg("calc_Soil_Temp_Layer \n"+
                                     "ID = " + unitnr + "\n" +
                                     "datumjul = " + datumjul + "\n" +
                                     "LAI = " + LAI + "\n" +
                                     "albedofactor = " + albedofactor + "\n" +
                                     "Soil_Temp_Layer = " + Soil_Temp_Layer + "\n" +
                                     "layerdepth = " + layerdepth + "\n" );
 
        }
 */
        
        return temp_bare_soil;
    }
    
    
    public void cleanup() {
        
    }
}

/*
   <component class="org.jams.j2k.s_n.J2KSoilTemplayer" name="J2KSoilTemplayer">
                <jamsvar name="time" provider="TemporalContext" providervar="current"/>
                <jamsvar name="area"  provider="HRUContext" providervar="currentEntity.area"/>
                <jamsvar name="atemp_mean" provider="HRUContext" providervar="currentEntity.tmean"/>
                <jamsvar name="atemp_max" provider="HRUContext" providervar="currentEntity.tmax"/>
                <jamsvar name="atemp_min" provider="HRUContext" providervar="currentEntity.tmin"/>
                <jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.LAIArray"/>
                <jamsvar name="snowcover" provider="HRUContext" providervar="currentEntity.snowTotSWE"/>
                <jamsvar name="stohru_MPS" provider="HRUContext" providervar="currentEntity.maxMPS"/>
                <jamsvar name="stohru_LPS" provider="HRUContext" providervar="currentEntity.maxLPS"/>
                <jamsvar name="Sat_MPS" provider="HRUContext" providervar="currentEntity.satMPS"/>
                <jamsvar name="Sat_LPS" provider="HRUContext" providervar="currentEntity.satLPS"/>
                <jamsvar name="radiation" provider="HRUContext" providervar="currentEntity.solRad"/>
                <jamsvar name="soilalbedo" provider="HRUContext" providervar="currentEntity.albedo"/>
                <jamsvar name="Surfacetemp" provider="HRUContext" providervar="currentEntity.surfacetemp"/>
                <jamsvar name="Soil_Temp_Layer" provider="HRUContext" providervar="currentEntity.Soil_Temp_Layer"/>
                <!--<!Prelinimary variable settings">-->
                <jamsvar name="layerdepth" provider="HRUContext" providervar="currentEntity.rootDepth"/>
                <jamsvar name="totaldepth" provider="HRUContext" providervar="currentEntity.rootDepth"/>
                <!--<!Prelinimary variable settings end">-->
                <jamsvar name="temp_lag" value="0.8"/>
                <jamsvar name="anatemp_mean" provider="HRUContext" providervar="currentEntity.tmeanavg"/>
        </component>
 */