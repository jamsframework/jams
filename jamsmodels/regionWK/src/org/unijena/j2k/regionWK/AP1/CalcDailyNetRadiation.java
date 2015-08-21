/*
 * CalcDailyNetRadiation.java
 * Created on 24. November 2005, 13:32
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
package org.unijena.j2k.regionWK.AP1;

import java.io.*;
//import jams.JAMS;
import jams.tools.JAMSTools;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(title = "Title",
author = "Author",
description = "Description")
public class CalcDailyNetRadiation extends JAMSComponent {

     @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "daily longwave radiation [MJ/m²]")
    public Attribute.Double lwRad;
     
     @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "daily shortwave radiation [MJ/m²]")
    public Attribute.Double swRad;
      
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "daily net radiation [MJ/m²]")
    public Attribute.Double netRad;

        
    /*
     *  Component run stages
     */
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
       
    }

    public void run() throws Attribute.Entity.NoSuchAttributeException, IOException {
                       
            //double netSWRadiation = org.unijena.j2k.regionWK.AP1.CalcShortWaveRadiation.shortwaveRad();
            //double netLWRadiation = org.unijena.j2k.regionWK.AP1.CalcLongWaveRadiation.longwaveRad();
            double netSWRadiation = this.swRad.getValue();
            double netLWRadiation = this.lwRad.getValue();
                    
            double nR_norm = netSWRadiation - netLWRadiation;
                
            if(nR_norm < 0){
                    nR_norm = 0;
            }
            
            netRad.setValue(nR_norm);
    }
            

    public void cleanup() throws IOException {
       
    }
}
