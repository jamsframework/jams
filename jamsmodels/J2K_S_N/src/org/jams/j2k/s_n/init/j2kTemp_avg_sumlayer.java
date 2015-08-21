/*
 * j2kTemp_avg_sumlayer.java
 * Created on 19. February 2006, 15:35
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c8fima
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

package org.jams.j2k.s_n.init;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="j2kTemp_avg_sumlayer",
        author="Manfred Fink",
        description="Module for the calculation of long yearly average temperature and temperature sum " +
        "and assingment of intitial temperatures of soil layers and soilsuface"
        
        )
        public class j2kTemp_avg_sumlayer extends JAMSComponent {
    
    
    
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily mean temperature in °C"
            )
            public Attribute.Double tmeanpre;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "mean temperature of the simulation period in °C"
            )
            public Attribute.Double tmeanavg;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "average yearly temperature sum of the simulation period in °C"
            )
            public Attribute.Double tmeansum;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "number of current days"
            )
            public Attribute.Double I;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "soil temperature in layerdepth in °C"
            )
            public Attribute.DoubleArray Soil_Temp_Layer;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "in °C *  Output soil surface temperature"
            )
            public Attribute.Double Surfacetemp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of layers in soil profile in [-]"
            )
            public Attribute.Double Layer;
    
 
    
    /*
     *  Component run stages
     */
    
    public void init() {
/*        tmeanavg.setValue(0);
        tmeansum.setValue(0);
        I.setValue(0); */
        
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        double tempmean = tmeanpre.getValue();
        double tempmeanavg = tmeanavg.getValue();
        double tempmeansum = tmeansum.getValue();
        double ii = I.getValue();
        int j = 0;
        int layer = (int)Layer.getValue();
        
        double[] Soil_Temp_Layervals  = new double[layer];
        ii++;
        tempmeanavg = ((tempmeanavg * (ii - 1)) + tempmean) / ii;
        
        tempmeansum = ((tempmeansum * ((ii - 1) / 365.25)) + tempmean )/ (ii / 365.25);
/*
        if (i == 1000) {
 
 
            System.out.println("tmeanavg = " + tmeanavg +" i =  "+ i);
            System.out.println("tmeansum = " + tmeansum +" i =  "+ i);
 
 
 
        }
 */
        tmeanavg.setValue(tempmeanavg);
        tmeansum.setValue(tempmeansum);
        while (j < layer){
            Soil_Temp_Layervals[j] = tempmeanavg;
            j++;
        }
        
        Soil_Temp_Layer.setValue(Soil_Temp_Layervals);
        Surfacetemp.setValue(tempmeanavg);
        I.setValue(ii);
    }
    
    public void cleanup() {
        
    }
}
