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
package soilTemp;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Author
    (name = "Manfred Fink")
@Description
    ("Calculates yearly mean temperatures and assigns initial soil surface/layer temperatures")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilTemp/TempAvgSumlayer.java $")
@VersionInfo
    ("$Id: TempAvgSumlayer.java 959 2010-02-11 20:31:47Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class TempAvgSumlayer  {
    
    @Description("Daily mean temperature")
    @Unit("C")
    @In public double tmeanpre;

    @Description("number of layers in soil profile")
    @In public int horizons;
    
    @Description("output soil surface temperature")
    @Unit("C")
    @Out public double surfacetemp;
    
    @Description("soil temperature in layer depth")
    @Unit("C")
    @Out public double[] soil_Temp_Layer;

    @Description("mean temperature of the simulation period")
    @Unit("C")
    @In @Out public double tmeanavg;
    
    @Description("average yearly temperature sum of the simulation period")
    @Unit("C")
    @In @Out public double tmeansum;
    
    @Description("number of current days")
    @In @Out public int i;
   
    
    @Execute
    public void execute() {
        if (soil_Temp_Layer == null) {
            soil_Temp_Layer = new double[horizons];
        }
        i++;
        tmeanavg = ((tmeanavg * (i-1)) + tmeanpre) / i;
        tmeansum = ((tmeansum * ((i-1) / 365.25)) + tmeanpre) / (i / 365.25);
        for (int j = 0; j < soil_Temp_Layer.length; j++) {
            soil_Temp_Layer[j]  = tmeanavg;
        }
        surfacetemp = tmeanavg;
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new TempAvgSumlayer());
    }

}
