/*
 * soillayer_writer4.java
 * Created on 12. März 2008, 11:43
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

package org.jams.j2k.s_n.io;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author c8fima
 */
@JAMSComponentDescription(
title="soillayer_writer4",
        author="Manfred Fink",
        description="Writes array values of the soil storages for the first 4 soil layers. If layers are not existing values are 0"
        )
        public class soillayer_writer4 extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil layer"
            )
            public Attribute.DoubleArray layerdepth;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU area in m^2"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum FPS in mm"
            )
            public Attribute.DoubleArray maxFPS;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum MPS in mm"
            )
            public Attribute.DoubleArray maxMPS;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum LPS in mm"
            )
            public Attribute.DoubleArray maxLPS;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU state var actual MPS in mm"
            )
            public Attribute.DoubleArray actMPS;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "HRU state var actual LPS in mm"
            )
            public Attribute.DoubleArray actLPS;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS of the frist layer in mm"
            )
            public Attribute.Double actLPS1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS of the second layer in mm"
            )
            public Attribute.Double actLPS2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS of the third layer in mm"
            )
            public Attribute.Double actLPS3;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual LPS of the fourth layer in mm"
            )
            public Attribute.Double actLPS4;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS of the frist layer in mm"
            )
            public Attribute.Double actMPS1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS of the second layer in mm"
            )
            public Attribute.Double actMPS2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS of the third layer in mm"
            )
            public Attribute.Double actMPS3;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU state var actual MPS of the fourth layer in mm"
            )
            public Attribute.Double actMPS4;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU soil moistrure of the frist layer in %"
            )
            public Attribute.Double actMoist1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU soil moistrure of the second layer in %"
            )
            public Attribute.Double actMoist2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU soil moistrure of the third layer in %"
            )
            public Attribute.Double actMoist3;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU soil moistrure of the fourth layer in %"
            )
            public Attribute.Double actMoist4;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU soil moistrure array of all layers in %"
            )
            public Attribute.DoubleArray actMoist_h;
    
    
    
    
    /*
     *  Component run stages
     */
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_maxFPS, run_layerdepth;
    public void init() {
        
    }
    
    public void run() {
        
        run_layerdepth = layerdepth.getValue();
        run_maxFPS =  maxFPS.getValue();
        run_maxMPS =  maxMPS.getValue();
        run_maxLPS =  maxLPS.getValue();
        run_actMPS =  actMPS.getValue();
        run_actLPS =  actLPS.getValue();
        
        
        int nhor = run_layerdepth.length;
        
        double[] run_actMoist_h = new double[nhor];
        
        actMPS1.setValue((run_actMPS[0]/run_maxMPS[0])*100);
        actLPS1.setValue((run_actLPS[0]/run_maxLPS[0])*100);
        actMoist1.setValue(((run_actMPS[0] + run_actLPS[0] + run_maxFPS[0])/(run_layerdepth[0]*10*area.getValue()))*100);
        
        if (nhor > 1){
            actMPS2.setValue((run_actMPS[1]/run_maxMPS[1])*100);
            actLPS2.setValue((run_actLPS[1]/run_maxLPS[1])*100);
            actMoist2.setValue(((run_actMPS[1] + run_actLPS[1] + run_maxFPS[1])/(run_layerdepth[1]*10*area.getValue()))*100);
        }else{
            actMPS2.setValue(0);
            actLPS2.setValue(0);
            actMoist2.setValue(0);}
        
        if (nhor > 2){
            actMPS3.setValue((run_actMPS[2]/run_maxMPS[2])*100);
            actLPS3.setValue((run_actLPS[2]/run_maxLPS[2])*100);
            actMoist3.setValue(((run_actMPS[2] + run_actLPS[2] + run_maxFPS[2])/(run_layerdepth[2]*10*area.getValue()))*100);
        }else{
            actMPS3.setValue(0);
            actLPS3.setValue(0);
            actMoist3.setValue(0);}
        
        if (nhor > 3){
            actMPS4.setValue((run_actMPS[3]/run_maxMPS[3])*100);
            actLPS4.setValue((run_actLPS[3]/run_maxLPS[3])*100);
            actMoist4.setValue(((run_actMPS[3] + run_actLPS[3] + run_maxFPS[3])/(run_layerdepth[3]*10*area.getValue()))*100);
        }else{
            actMPS4.setValue(0);
            actLPS4.setValue(0);
            actMoist4.setValue(0);}
        
        for (int i=0; i < nhor;i++){
        run_actMoist_h[i] =  ((run_actMPS[i] + run_actLPS[i] + run_maxFPS[i])/(run_layerdepth[i]*10*area.getValue()))*100;
        }
        actMoist_h.setValue(run_actMoist_h);
     int i = 0;
    }
    
    public void cleanup() {
        
    }
}
