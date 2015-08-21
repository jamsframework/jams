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
package j2k_Himalaya.interception;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause
 */
@JAMSComponentDescription(
        title="J2KProcessInterception",
        author="Peter Krause",
        description="Calculates daily interception based on DICKINSON 1984"
        )
        public class J2KProcessInterceptionSan extends JAMSComponent {
    
    /*
     *  Component variables
     */
   /* @JAMSVarDescription(
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
            description = "time"
            )
            public Attribute.Calendar time;
    */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable mean tempeature"
            )
            public Attribute.Double tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable rain"
            )
            public Attribute.Double rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable snow"
            )
            public Attribute.Double snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable potET"
            )
            public Attribute.Double potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET"
            )
            public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET"
            )
            public Attribute.Double ETInterception;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable LAI"
            )
            public Attribute.Double actLAI;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRS"
            )
            public Attribute.Double snow_trs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Snow parameter TRANS"
            )
            public Attribute.Double snow_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Interception parameter a_rain"
            )
            public Attribute.Double a_rain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Interception parameter a_snow"
            )
            public Attribute.Double a_snow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable net-rain"
            )
            public Attribute.Double netRain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable net-snow"
            )
            public Attribute.Double netSnow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable throughfall"
            )
            public Attribute.Double throughfall;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable dy-interception"
            )
            public Attribute.Double interception;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable interception storage"
            )
            public Attribute.Double intercStorage;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable throughfall"
            )
            public Attribute.Double throughfallRain;

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "state variable throughfall"
            )
            public Attribute.Double throughfallSnow;

    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        this.intercStorage.setValue(0);
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        double alpha = 0;
        double out_throughfall = 0;
        double out_interception = 0;
        
        double in_rain = rain.getValue();
        double in_snow = snow.getValue();
        double in_temp = tmean.getValue();
        
        double in_potETP = potET.getValue();
        double in_actETP = actET.getValue();
        
        double in_LAI = this.actLAI.getValue();
        double in_Area = area.getValue();
        
        double out_InterceptionStorage = intercStorage.getValue();
        double out_actETP = in_actETP;
        
        double sum_precip = in_rain + in_snow;
        
        double deltaETP = in_potETP - in_actETP;
                
        double relRain, relSnow;
        if(sum_precip > 0){
            relRain = in_rain / sum_precip;
            relSnow = in_snow / sum_precip;
        } else{
            relRain = 1.0; //throughfall without precip is in general considered to be liquid
            relSnow = 0;
        }
        
        //determining if precip falls as rain or snow
        if(in_temp < (snow_trs.getValue() - snow_trans.getValue())){
            //alpha = alpha_snow;
            alpha = a_snow.getValue();
        } else{
            //alpha = alpha_rain;
            alpha = a_rain.getValue();
        }
        
        //determinining maximal interception capacity of actual day
        double maxIntcCap = (in_LAI * alpha) * in_Area;
        
        //if interception storage has changed from snow to rain then throughfall
        //occur because interception storage of antecedend day might be larger
        //then the maximum storage capacity of the actual time step.
        if(out_InterceptionStorage > maxIntcCap){
            out_throughfall = out_InterceptionStorage - maxIntcCap;
            out_InterceptionStorage = maxIntcCap;
        }
        
        //determining the potential storage volume for daily Interception
        double deltaIntc = maxIntcCap - out_InterceptionStorage;
        
        //reducing rain and filling of Interception storage
        if(deltaIntc > 0){
            //double save_rain = sum_precip;
            if(sum_precip > deltaIntc){
                out_InterceptionStorage = maxIntcCap;
                sum_precip = sum_precip - deltaIntc;
                out_throughfall = out_throughfall + sum_precip;
                out_interception = deltaIntc;
                deltaIntc = 0;
            } else{
                out_InterceptionStorage = (out_InterceptionStorage + sum_precip);
                out_interception = sum_precip;
                sum_precip = 0;
            }
        } else{
            out_throughfall = out_throughfall + sum_precip;
        }
        
        //depletion of interception storage; beside the throughfall from above interc.
        //storage can only be depleted by evapotranspiration
        if(deltaETP > 0){
            if(out_InterceptionStorage > deltaETP){
                out_InterceptionStorage = out_InterceptionStorage - deltaETP;
                out_actETP = in_actETP + deltaETP;
                deltaETP = 0;
                
            } else{
                //deltaETP = deltaETP - out_InterceptionStorage;
                out_actETP = in_actETP + out_InterceptionStorage;
                out_InterceptionStorage = 0;

//                deltaETP = deltaETP - out_InterceptionStorage;
//                out_actETP = in_actETP + (in_potETP - deltaETP);
//                out_InterceptionStorage = 0;
            }
        } else{
            out_actETP = deltaETP;
        }
        //System.out.println("3. out_actETP: "+out_actETP);
        this.netRain.setValue(out_throughfall * relRain);
        this.netSnow.setValue(out_throughfall * relSnow);
        //throughfall was the mixture of both rain and snow earlier..with this new line, throughfall coming from snow and rain are segregated//
        this.throughfallRain.setValue(out_throughfall * relRain);
        this.throughfallSnow.setValue(out_throughfall * relSnow);
        
        this.actET.setValue(out_actETP);
        this.ETInterception.setValue(out_actETP);
        this.intercStorage.setValue(out_InterceptionStorage);
        this.interception.setValue(out_interception);
        this.throughfall.setValue(out_throughfall);
   
    }

   

    public void cleanup() {
        this.intercStorage.setValue(0);
    }
    
}
