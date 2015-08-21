package org.unijena.j2k.gw;

/*
 * J2KProcessGroundwater.java
 * Created on 25. November 2005, 16:54
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



import jams.data.*;
import jams.model.*;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="J2KGroundwater",
        author="Peter Krause",
        description="Description"
        )
        public class J2KLakeModule extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area"
            )
            public Attribute.Double area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope"
            )
            public Attribute.Double slope;
           
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net rain"
            )
            public Attribute.Double netRain;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "state variable net snow"
            )
            public Attribute.Double netSnow;
    
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
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG1 storage"
            )
            public Attribute.Double maxRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum RG2 storage"
            )
            public Attribute.Double maxRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG1"
            )
            public Attribute.Double kRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "recision coefficient k RG2"
            )
            public Attribute.Double kRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG1 storage"
            )
            public Attribute.Double actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG2 storage"
            )
            public Attribute.Double actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG1 storage"
            )
            public Attribute.Double actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual RG2 storage"
            )
            public Attribute.Double actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow"
            )
            public Attribute.Double inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow"
            )
            public Attribute.Double inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow"
            )
            public Attribute.Double inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow"
            )
            public Attribute.Double inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 outflow"
            )
            public Attribute.Double outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 outflow"
            )
            public Attribute.Double outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG1 generation"
            )
            public Attribute.Double genRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "RG2 generation"
            )
            public Attribute.Double genRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 correction factor"
            )
            public Attribute.Double gwRG1Fact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG2 correction factor"
            )
            public Attribute.Double gwRG2Fact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "RG1 RG2 distribution factor"
            )
            public Attribute.Double gwRG1RG2dist;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual Volume of Lake"
            )
            public Attribute.Double actLakeStor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual Depth of Lake"
            )
            public Attribute.Double actLakeDepth;        
    
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual Depth of Lake"
            )
            public Attribute.Double lakeDepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual Depth of Lake"
            )
            public Attribute.Double KfGeo;     
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "clogging Factor of Lake Bank"
            )
            public Attribute.Double cloggingFactor;  
    
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var saturation of MPS"
            )
            public Attribute.Double satMPS;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var saturation of LPS"
            )
            public Attribute.Double satLPS;

    
    
    double run_lakeArea, run_KfGeo, run_cloggingFactor, run_maxRG1, run_maxRG2, run_actRD1, run_actRD2, run_actRG1, run_actRG2, run_actLakeStor, run_actLakeDepth, lakeInflow, lakeOutflow, run_inRain, run_inSnow, run_potET, run_actET, run_inRD1, run_inRD2, run_inRG1, run_inRG2, run_outRG1, run_outRG2, run_genRG1, run_genRG2,
           run_k_RG1, run_k_RG2, run_RG1_rec, run_RG2_rec, run_maxSoilStor, run_actSoilStor, run_slope,
           run_percolation, run_gwExcess, run_satMPS, run_satLPS;
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException {
        
        Attribute.Entity entity = entities.getCurrent();
        // getModel().getRuntime().println("Das ist ein See!");

        //Reading Variables and Parameters

        
        //Reading state Variables
        this.run_actLakeStor = actLakeStor.getValue();
        this.run_actLakeDepth = actLakeDepth.getValue();
        this.run_actRD1 = actRD1.getValue();
        this.run_actRD2 = actRD2.getValue();        
        this.run_actRG1 = actRG1.getValue();
        this.run_actRG2 = actRG2.getValue();
        
        //brauch ich das?
        this.run_maxRG1 = maxRG1.getValue();
        this.run_maxRG2 = maxRG2.getValue();
        this.run_satMPS = 1;
        this.run_satLPS = 1;        

        // Reading inputs
        this.run_inRD1 = inRD1.getValue();
        this.run_inRD2 = inRD2.getValue();
        this.run_inRG1 = inRG1.getValue();
        this.run_inRG2 = inRG2.getValue();
    
        this.run_inRain = netRain.getValue();
        this.run_inSnow = netSnow.getValue();
        
        this.run_potET = potET.getValue();

        //Lake Evaporation = potential Evaporation
        this.run_actET = this.run_potET;
        
        this.run_outRG1 = 0;
        this.run_outRG2 = 0;
        this.run_genRG1 = 0;
        this.run_genRG2 = 0;
        
        this.run_lakeArea = area.getValue();
        this.run_slope = slope.getValue();
        
        //Reading parameters
        this.run_KfGeo = entity.getDouble("Kf_geo");
                
        this.run_cloggingFactor = cloggingFactor.getValue();
        
        this.run_k_RG1 = kRG1.getValue();
        this.run_k_RG2 = kRG2.getValue();
        
        this.run_RG1_rec = this.run_k_RG1 * this.gwRG1Fact.getValue();
        this.run_RG2_rec = this.run_k_RG2 * this.gwRG2Fact.getValue();     
        
        double Receiver = entity.getDouble("to_poly");
        
        this.calcVirtStorages();
        this.calcLakeStorage();
        
        //this.distRG1_RG2();
        //this.calcDeepSink();
        //this.calcExpGWout();
        this.calcLinGWout();
        
        this.calcLakeStorage();

        actLakeStor.setValue(this.run_actLakeStor);
        actLakeDepth.setValue(this.run_actLakeDepth);
        lakeDepth.setValue(this.run_actLakeDepth);       
        
        actRD1.setValue(this.run_actRD1);
        actRD2.setValue(this.run_actRD2);
        actRG1.setValue(this.run_actRG1);
        actRG2.setValue(this.run_actRG2);
        
        outRG1.setValue(this.run_outRG1);
        outRG2.setValue(this.run_outRG2);
        genRG1.setValue(this.run_genRG1);
        genRG2.setValue(this.run_genRG2);
        
        inRD1.setValue(this.run_inRD1);
        inRD2.setValue(this.run_inRD2);
        inRG1.setValue(this.run_inRG1);
        inRG2.setValue(this.run_inRG2);
        
        satMPS.setValue(this.run_satMPS);
        satLPS.setValue(this.run_satLPS);      
    }
    
    public void cleanup() {
        
    }
    
    private boolean calcVirtStorages(){
        this.run_actRG1 = this.run_actRG1 + this.run_inRG1;
        this.run_actRG2 = this.run_actRG2 + this.run_inRG2;
        this.run_actRD1 = this.run_actRD1 + this.run_inRD1;
        this.run_actRD2 = this.run_actRD2 + this.run_inRD2;
        return true;
    }
    
    private boolean calcLakeStorage(){
        this.lakeInflow = this.run_inRD1 + this.run_inRD2 + this.run_inRG1 + this.run_inRG2;
        this.lakeOutflow = this.run_outRG1 + this.run_outRG2;
        double oldLakeStor = this.run_actLakeStor;
        this.run_actLakeStor = this.run_actLakeStor + this.lakeInflow + this.run_inRain + this.run_inSnow - this.lakeOutflow;
        
        if(this.run_actLakeStor > this.run_actET){
            this.run_actLakeStor = this.run_actLakeStor - this.run_actET;
        }
        else{ //Evaporation exceeds Storage
            this.run_actET = this.run_actLakeStor;
            this.run_actLakeStor = 0;
        }
        
        double diffLakeStor = oldLakeStor - this.run_actLakeStor;
        double waterTableChange = diffLakeStor /1000 / this.run_lakeArea;
                
        this.run_actLakeDepth = this.run_actLakeDepth - waterTableChange;
        
        if(this.run_actLakeDepth < 0){
            this.run_actLakeDepth = 0;
            this.run_actLakeStor = 0;
        }
        
        this.run_inRD1 = 0;
        this.run_inRD2 = 0;
        this.run_inRG1 = 0;
        this.run_inRG2 = 0;
        
        this.run_inRain = 0;
        this.run_inSnow = 0;
        
        return true;
    }
    
    
    private boolean distRG1_RG2(){
        double slope_weight = Math.tan(this.run_slope * (Math.PI / 180.));
        double gradh = ((1 - slope_weight) * this.gwRG1RG2dist.getValue());
        
        if(gradh < 0)
            gradh = 0;
        else if(gradh > 1)
            gradh = 1;
        
        double pot_RG1 = ((1 - gradh) * this.run_percolation);
        double pot_RG2 = (gradh * this.run_percolation);
        
        this.run_actRG1 = this.run_actRG1 + pot_RG1;
        this.run_actRG2 = this.run_actRG2 + pot_RG2;
        
        /** testing if inflows can be stored in groundwater storages */
        double delta_RG2 = this.run_actRG2 - this.run_maxRG2;
        if(delta_RG2 > 0){
            this.run_actRG1 = this.run_actRG1 + delta_RG2;
            this.run_actRG2 = this.run_maxRG2;
        }
        double delta_RG1 = this.run_actRG1 - this.run_maxRG1;
        if(delta_RG1 > 0){
            this.run_gwExcess = this.run_gwExcess + delta_RG1;
            this.run_actRG1 = this.run_maxRG1;
        }
        if(delta_RG1 > 0){
            //// getModel().getRuntime().println("interflow surplus in gw: "+delta_RG1);
        }
        return true;
    }
    
    private boolean calcLinGWout(){
        
        double lakeBankCut = this.run_actLakeDepth * 300; //Annahme: Seebreite = 300 m
        double potOutflow = this.run_cloggingFactor * lakeBankCut * 100 * this.run_KfGeo; // * I ( (Ha - Hb) / l ) //* 100 um auf Liter zu kommen
        
        this.run_actRG1 = 0.3 * potOutflow;
        this.run_actRG2 = 0.7 * potOutflow;
        
        
        //double k_rg1 = this.conc_index / this.RG1_k;
        double k_rg1 = 1 / this.run_RG1_rec;
        if(k_rg1 > 1)
            k_rg1 = 1;
        double rg1_out = k_rg1 * this.run_actRG1;
        this.run_actRG1 = this.run_actRG1 - rg1_out;
        this.run_outRG1 = this.run_outRG1 + rg1_out;
        
        //double k_rg2 = this.conc_index / this.RG2_k;
        double k_rg2 = 1 / this.run_RG2_rec;
        if(k_rg2 > 1)
            k_rg2 = 1;
        double rg2_out = k_rg2 * this.run_actRG2;
        this.run_actRG2 = this.run_actRG2 - rg2_out;
        this.run_outRG2 = this.run_outRG2 + rg2_out;
        
        this.run_genRG1 = rg1_out;
        this.run_genRG2 = rg2_out;
        
        return true;
    }
}
