/*
 * J2KProcessInterception.java
 * Created on 24. November 2005, 10:52
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
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
/*
<component class="org.unijena.j2k.interception.J2KProcessInterception" name="J2KProcessInterception">
    <jamsvar name="area" provider="HRUContext" providervar="currentEntity.area"/>
    <jamsvar name="tmean" provider="HRUContext" providervar="currentEntity.tmean"/>
    <jamsvar name="rain" provider="HRUContext" providervar="currentEntity.rain"/>
    <jamsvar name="snow" provider="HRUContext" providervar="currentEntity.snow"/>
    <jamsvar name="potETP" provider="HRUContext" providervar="currentEntity.potETP"/>
    <jamsvar name="actETP" provider="HRUContext" providervar="currentEntity.actETP"/>
    <jamsvar name="LAIArray" provider="HRUContext" providervar="currentEntity.actLAI"/>
    <jamsvar name="snow_trs" globvar="snow_trs"/>
    <jamsvar name="snow_trans" globvar="snow_trans"/>
    <jamsvar name="a_rain" value="0.2"/>
    <jamsvar name="a_snow" value="0.5"/>
    <jamsvar name="netRain" provider="HRUContext" providervar="currentEntity.netRain"/>
    <jamsvar name="netSnow" provider="HRUContext" providervar="currentEntity.netSnow"/>
    <jamsvar name="throughfall" provider="HRUContext" providervar="currentEntity.throughfall"/>
    <jamsvar name="interception" provider="HRUContext" providervar="currentEntity.interception"/>
    <jamsvar name="intercStorage" provider="HRUContext" providervar="currentEntity.intercStorage"/>
</component>
 */
package org.unijena.j2k.interception;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="LitterInterception",
        author="Peter Krause",
        description="Calculates daily interception based on DICKINSON 1984"
        )
        public class LitterInterception extends JAMSComponent {
    
    /*
     *  Component variables
     */
     
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "hru"
            )
            public Attribute.Entity hru;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "hruID"
            )
            public Attribute.Double hruID;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable net-rain"
            )
            public Attribute.Double netRain;
            
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "daily snow melt"
            )
            public Attribute.Double snowMelt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potE"
            )
            public Attribute.Double potE;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable actE"
            )
            public Attribute.Double actE;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LitterInterception parameter Cmax"
            )
            public Attribute.Double Cmax;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "LitterInterception parameter Cmin"
            )
            public Attribute.Double Cmin;
    
   /*@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable throughfall"
            )
            public Attribute.Double throughfall;*/
    
   /* @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable litter interception"
            )
            public Attribute.Double LitterInterception;*/
    
                
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "horizons"
            )
            public Attribute.Double horizons;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum MPS"
            )
            public Attribute.DoubleArray maxMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum LPS"
            )
            public Attribute.DoubleArray maxLPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU state var actual MPS"
            )
            public Attribute.DoubleArray actMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU state var actual LPS"
            )
            public Attribute.DoubleArray actLPS;
            
        @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU state var saturation of MPS"
            )
            public Attribute.DoubleArray satMPS;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU state var saturation of LPS"
            )
            public Attribute.DoubleArray satLPS;
            
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in summer [mm/d]"
            )
            public Attribute.Double soilMaxInfSummer;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate in winter [mm/d]"
            )
            public Attribute.Double soilMaxInfWinter;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "maximum infiltration rate on snow [mm/d]"
            )
            public Attribute.Double soilMaxInfSnow;
    
     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "Litter interception storage [mm]"
            )
            public Attribute.Double LitterInterceptionStorage;
     
     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "surface runoff generation"
            )
            public Attribute.Double litterRD1;
    
   @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "snow depth"
            )
            public Attribute.Double snowDepth;
   
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "litter infiltration"
            )
            public Attribute.Double litterInfiltration;
    /*
     *  Component run stages
     */
    double run_snowDepth, in_Area, run_satSoil,soilSatMps, soilSatLps, soilActMps, soilActLps, soilMaxMps, soilMaxLps,run_LitIntcStorage;
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_satMPS, run_satLPS, run_inRD2, run_satHor, run_outRD2, run_genRD2;
    int nhor;
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        this.LitterInterceptionStorage.setValue(0);
    }
    
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        double acte = 0;
        double sum_input = 0;

        double netrain = this.netRain.getValue();
        double snowmelt = this.snowMelt.getValue();
        
        double pot_E = this.potE.getValue();
        
        this.in_Area = area.getValue();
        
        double cmax = this.Cmax.getValue();
        double cmin = this.Cmin.getValue();
                
        run_LitIntcStorage = this.LitterInterceptionStorage.getValue();
        
        this.run_maxMPS = maxMPS.getValue();
        this.run_maxLPS = maxLPS.getValue();
        this.run_actMPS = actMPS.getValue();
        this.run_actLPS = actLPS.getValue();
        this.run_satMPS = satMPS.getValue();
        this.run_satLPS = satLPS.getValue();


        this.nhor = (int)horizons.getValue();
        this.run_satHor = new double[nhor];
        
        sum_input = netrain + snowmelt;

        this.run_snowDepth = snowDepth.getValue();

        
        //determining maximal litter interception capacity of actual day
        double maxLitIntcCap = cmax * in_Area;
        double minLitIntcCap = cmin * in_Area;
        
        //adding input from netRain and snowmelt to actual interception storage
        run_LitIntcStorage = run_LitIntcStorage + sum_input;
        
        //determining possible infiltration
        
            /** determining maximal infiltration rate */
       
        //System.out.println("Monat: " + time.get(time.MONTH)+1);
        double maxInf = calcMaxInfiltration(time.get(Attribute.Calendar.MONTH)+1);
        
        //Calculating available water for Infiltration
        double potInf = (run_LitIntcStorage - minLitIntcCap)/maxLitIntcCap;
        
        //preventing negative values
        if(potInf < 0){
              potInf = 0;
          }
        
        double inf;
        //calculating infiltration
        if(potInf > maxInf){
          inf = maxInf;
          }else{
              inf = potInf;
          }
        
        //Subtracting Infiltration from actual storage
        run_LitIntcStorage = run_LitIntcStorage - inf;

        //Calculating actual evaporation
        if(run_LitIntcStorage > pot_E){
          acte = pot_E;
        }else{
          acte = run_LitIntcStorage;     
        }
        
        //Subtracting Evaporation from actual storage
        run_LitIntcStorage = run_LitIntcStorage - acte;

        
        double rd1litter ;
        
        //calculating overland flow generation
        if(run_LitIntcStorage > maxLitIntcCap){
            rd1litter = run_LitIntcStorage - maxLitIntcCap;
        }else{
            rd1litter = 0;
        }          
       run_LitIntcStorage = run_LitIntcStorage - rd1litter;
       
       
        
        this.actE.setValue(acte);
        this.LitterInterceptionStorage.setValue(run_LitIntcStorage);
        this.litterRD1.setValue(rd1litter);
        this.litterInfiltration.setValue(inf);
        
    }
    
    public void cleanup() {
        this.LitterInterceptionStorage.setValue(0);
    }
    
     private double calcMaxInfiltration(int nowmonth){
        double maxInf = 0;
        this.calcSoilSaturations(false);
        if(this.run_snowDepth > 0)
            maxInf = this.soilMaxInfSnow.getValue() * this.in_Area;
        else if((nowmonth >= 5) & (nowmonth <=10))
            maxInf = (1 - this.run_satSoil) * soilMaxInfSummer.getValue() * this.in_Area;
        else
            maxInf = (1 - this.run_satSoil) * soilMaxInfWinter.getValue() * this.in_Area;

        return maxInf;

    }
 
 private boolean calcSoilSaturations(boolean debug){
        soilMaxMps = 0;
        soilActMps = 0;
        soilMaxLps = 0;
        soilActLps = 0;
        soilSatMps = 0;
        soilSatLps = 0;
        for(int h = 0; h < nhor; h++){
             if((this.run_actLPS[h] > 0) && (this.run_maxLPS[h] > 0)){
                this.run_satLPS[h] = this.run_actLPS[h] / this.run_maxLPS[h];
            } else
                this.run_satLPS[h] = 0;

            if((this.run_actMPS[h] > 0) && (this.run_maxMPS[h] > 0)){
                this.run_satMPS[h] = this.run_actMPS[h] / this.run_maxMPS[h];
            } else
                this.run_satMPS[h] = 0;

            if(((this.run_maxLPS[h] > 0) | (this.run_maxMPS[h] > 0)) & ((this.run_actLPS[h] > 0) | (this.run_actMPS[h] > 0))){
                this.run_satHor[h] = ((this.run_actLPS[h] + this.run_actMPS[h]) / (this.run_maxLPS[h] + this.run_maxMPS[h]));
            } else{
                this.run_satSoil = 0;
            }
            soilMaxMps += this.run_maxMPS[h];
            soilActMps += this.run_actMPS[h];
            soilMaxLps += this.run_maxLPS[h];
            soilActLps += this.run_actLPS[h];
        }
        if(((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))){
            this.run_satSoil = ((soilActLps + soilActMps) / (soilMaxLps + soilMaxMps));
            soilSatMps = (soilActMps / soilMaxMps);
            soilSatLps = (soilActLps / soilMaxLps);
        } else{
            this.run_satSoil = 0;
        }

        return true;
    }
}

  
    
    