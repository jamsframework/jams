/*
 * InitJ2KProcessLayeredSoilWaterN.java
 * Created on 25. November 2005, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Peter Krause
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
package soilWater;

import ages.types.HRU;
import ages.types.SoilType;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 *
 * @author Peter Krause
 */
@Author 
    (name="Peter Krause")
@Description 
    ("Initialize soil water balance for each vertical layer")
@Keywords
    ("Soilwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilWater/InitJ2KProcessLayeredSoilWaterN2008.java $")
@VersionInfo
    ("$Id: InitJ2KProcessLayeredSoilWaterN2008.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class InitJ2KProcessLayeredSoilWaterN2008  {

    @Description ("field capacity adaptation factor")
    @Role(PARAMETER)
    @In public double FCAdaptation;

    @Description ("air capacity adaptation factor")
    @Role(PARAMETER)
    @In public double ACAdaptation;

    @Description ("The hru entities")
    @In public HRU hru;
    
    @Description ("attribute area")
    @In public double area;
   
    @Description ("number of horizons")
    @In public int horizons;
   
    @Description ("HRU attribute maximum MPS")
    @Out public double[] maxMPS_h;
   
    @Description ("HRU attribute maximum LPS")
    @Out public double[] maxLPS_h;
   
    @Description ("HRU attribute maximum FPS")
    @Out public double[] maxFPS_h;
   
    @Description ("HRU state var actual MPS")
    @Out public double[] actMPS_h;
   
    @Description ("HRU state var actual LPS")
    @Out public double[] actLPS_h;
   
    @Description ("HRU state var saturation of MPS")
    @Out public double[] satMPS_h;
   
    @Description ("HRU state var saturation of LPS")
    @Out public double[] satLPS_h;
   
    @Description ("RD2 inflow")
    @Out public double[] inRD2_h;
   
    @Execute
    public void execute()  {
        double[] mxMPS = new double[horizons];
        double[] mxLPS = new double[horizons];
        double[] mxFPS = new double[horizons];
        SoilType st = hru.soilType;
        for (int h = 0; h < horizons; h++) {
            mxMPS[h] = st.fieldcapacity[h] * area;
            mxFPS[h] = st.deadcapacity[h]  * area;
            mxLPS[h] = st.aircapacity[h]  * area;
        }
        maxFPS_h = mxFPS;
        maxMPS_h = mxMPS;
        maxLPS_h = mxLPS;
        actMPS_h = new double[horizons];
        actLPS_h = new double[horizons];
        satMPS_h = new double[horizons];
        satLPS_h = new double[horizons];
        inRD2_h = new double[horizons];
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new InitJ2KProcessLayeredSoilWaterN2008());
    }

}
