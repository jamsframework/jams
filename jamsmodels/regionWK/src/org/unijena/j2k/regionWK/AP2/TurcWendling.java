/*
 * TurcWendling.java
 * Created on 24. November 2008, 13:57
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
package org.unijena.j2k.regionWK.AP2;

import java.io.*;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author Corina Manusch
 */
@JAMSComponentDescription(title = "CalcDailyETP_TurcWendling",
author = "Corina Manusch",
description = "Calculates potential ETP according Turc-Wendling")
public class TurcWendling extends JAMSComponent {

    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "state variable mean temperature")
    public Attribute.Double tmean;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "daily solar radiation [MJ/m²d]")
    public Attribute.Double solRad;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "coast factor")
    public Attribute.Double coastalFactor;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "potential ET [mm/ timeUnit]")
    public Attribute.Double potET;

     /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
       
        }
    

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {

        
            double gRad = 100 * this.solRad.getValue();
            double temperature = this.tmean.getValue();
            double fk = this.coastalFactor.getValue();
            
            
            
            double pET = ((gRad + 93 * fk)*(temperature + 22))/(150*(temperature + 123));
            

            this.potET.setValue(pET);
            
                     
        }
    
    public void cleanup() throws IOException {
        
    }
}

   