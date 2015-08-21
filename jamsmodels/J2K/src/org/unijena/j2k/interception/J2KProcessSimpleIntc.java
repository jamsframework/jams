/*
 * J2KProcessSimpleIntc.java
 * Created on 15. April 2009, 10:52
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

package org.unijena.j2k.interception;

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
        public class J2KProcessSimpleIntc extends JAMSComponent {
    
    /*
     *  Component variables
     */
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable precipitation"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable potET"
            )
            public Attribute.Double potET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable actET"
            )
            public Attribute.Double actET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "state variable LAI"
            )
            public Attribute.Double actLAI;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Interception parameter alpha"
            )
            public Attribute.Double alpha;
    
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
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        this.intercStorage.setValue(0);
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        double alpha = this.alpha.getValue();
        double out_throughfall = 0;
        double out_interception = 0;
        
        double in_precip = precip.getValue() * area.getValue();
        double in_potETP = potET.getValue() * area.getValue();
        double in_actETP = 0;
        
        double in_LAI = this.actLAI.getValue();
        double in_Area = area.getValue();
        
        double out_InterceptionStorage = intercStorage.getValue();
        double out_actETP = in_actETP;
        
        double deltaETP = in_potETP - in_actETP;
        
        //determinining maximal interception capacity of actual day
        double maxIntcCap = (in_LAI * alpha) * in_Area;
        
        //determining the potential storage volume for daily Interception
        double deltaIntc = maxIntcCap - out_InterceptionStorage;
        
        //reducing rain and filling of Interception storage
        if(deltaIntc > 0){
            //double save_rain = sum_precip;
            if(in_precip > deltaIntc){
                out_InterceptionStorage = maxIntcCap;
                in_precip = in_precip - deltaIntc;
                out_throughfall = out_throughfall + in_precip;
                out_interception = deltaIntc;
                deltaIntc = 0;
            } else{
                out_InterceptionStorage = (out_InterceptionStorage + in_precip);
                out_interception = in_precip;
                in_precip = 0;
            }
        } else{
            out_throughfall = out_throughfall + in_precip;
        }
        
        //depletion of interception storage; beside the throughfall from above interc.
        //storage can only be depleted by evapotranspiration
        if(deltaETP > 0){
            if(out_InterceptionStorage > deltaETP){
                out_InterceptionStorage = out_InterceptionStorage - deltaETP;
                out_actETP = in_actETP + deltaETP;
                deltaETP = 0;
                
            } else{
                deltaETP = deltaETP - out_InterceptionStorage;
                out_actETP = in_actETP + (in_potETP - deltaETP);
                out_InterceptionStorage = 0;
            }
        } else{
            out_actETP = deltaETP;
        }
        
        this.actET.setValue(out_actETP);
        this.potET.setValue(in_potETP);
        this.intercStorage.setValue(out_InterceptionStorage);
        this.interception.setValue(out_interception);
        this.throughfall.setValue(out_throughfall);
        
    }
    
    public void cleanup() {
        this.intercStorage.setValue(0);
    }
    
}
