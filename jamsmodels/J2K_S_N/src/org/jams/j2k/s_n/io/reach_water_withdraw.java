/*
 * reach_water_withdraw.java
 * Created on 28. Juni 2007, 17:39
 *
 * This file is part of JAMS
 * Copyright (C) 2007 FSU Jena, c8fima
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
title="Reach water  withdrawal",
        author="Manfred Fink",
        description="Takes water and nitrogen out of a reach segment"
        )
        public class reach_water_withdraw extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 outflow in l"
            )
            public Attribute.Double outRD1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 outflow in l"
            )
            public Attribute.Double outRD2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 outflow in l"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 outflow in l"
            )
            public Attribute.Double outRG2;
    
    
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "Water that leaves the reach out of the system in m³/s determinated by the file"
            )
            public Attribute.Double measured_waterloss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "SurfaceN outflow in kgN"
            )
            public Attribute.Double SurfaceNabs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "(fast) InterflowN outflow in kgN"
            )
            public Attribute.Double InterflowNabs;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "(slow) InterflowN outflow in kgN"
            )
            public Attribute.Double N_RG1_out;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
            description = "GoundwaterN outflow in kgN"
            )
            public Attribute.Double N_RG2_out;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Water that leaves the reach out of the system in m³/s determinated by the model"
            )
            public Attribute.Double model_waterloss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "Nitrogen that leaves the reach out of the system in kgN determinated by the model"
            )
            public Attribute.Double Nitrogenloss;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "ID of the reach which losses water and nitrogen"
            )
            public Attribute.Integer reach_id;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        
    }
    
    public void run() {
        
        Attribute.Entity entity = entities.getCurrent();
        
        int runid =  (int)entity.getDouble("ID");
        
        if (runid == reach_id.getValue()){
        
        double sumwater = outRD1.getValue() + outRD2.getValue() + outRG1.getValue() + outRG2.getValue();
        
        double sumN = SurfaceNabs.getValue() + InterflowNabs.getValue() + N_RG1_out.getValue() + N_RG2_out.getValue();
        
        double waterloss_run = measured_waterloss.getValue() * 86400000; //transformation from m³/s in l/d
        
        double losspart = 0;
        
        
        if (waterloss_run < sumwater){
            losspart =  waterloss_run / sumwater;
        } else{
            losspart = 1;
        }
        double keep_part = 1 - losspart;
        
        outRD1.setValue(keep_part * outRD1.getValue());
        outRD2.setValue(keep_part * outRD2.getValue());
        outRG1.setValue(keep_part * outRG1.getValue());
        outRG2.setValue(keep_part * outRG2.getValue());
        
        SurfaceNabs.setValue(keep_part * SurfaceNabs.getValue());
        InterflowNabs.setValue(keep_part * InterflowNabs.getValue());
        N_RG1_out.setValue(keep_part * N_RG1_out.getValue());
        N_RG2_out.setValue(keep_part * N_RG2_out.getValue());
        
        double run_model_waterloss = sumwater * losspart / 86400000;
        double run_N_loss = sumN * losspart;
        
        model_waterloss.setValue(run_model_waterloss);
        
        Nitrogenloss.setValue(run_N_loss);
    }
    }
    
    public void cleanup() {
        
    }
}
