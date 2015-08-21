/*
 * initJ2KNSoil.java
 * Created on 05. February 2006, 21:40
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, Manfred Fink
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
        title="initJ2KNSoil",
        author="Manfred Fink",
        description="intitiallizing Nitrogen Pools in Soil and additional variables. Method after SWAT2000"
        )
        public class InitJ2KNSoil extends JAMSComponent  {
    
    /*
     *  Component variables
     */

    

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil layer"
            )
            public Attribute.Double layerdepth;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in mm depth of soil profile"
            )
            public Attribute.Double totaldepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
            )
            public Attribute.Double soil_bulk_density;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
            )
            public Attribute.Double C_org;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NO3-Pool in kgN/ha"
            )
            public Attribute.Double NO3_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NH4-Pool in kgN/ha"
            )
            public Attribute.Double NH4_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic Pool with reactive organic matter in kgN/ha"
            )
            public Attribute.Double N_activ_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic Pool with stable organic matter in kgN/ha"
            )
            public Attribute.Double N_stabel_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Residue in Layer in kgN/ha"
            )
            public Attribute.Double Residue_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic fresh Pool from Residue in kgN/ha"
            )
            public Attribute.Double N_residue_pool_fresh;
    
/*
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layer"
            )
            public Attribute.Double Layer;
 */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " voltalisation rate from NH4_Pool in kgN/ha"
            )
            public Attribute.Double Volati_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NH4 fertilizer rate in kgN/ha"
            )
            public Attribute.Double NH4inp;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " plantuptake rate in kgN/ha"
            )
            public Attribute.Double PlantupN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " nitrification rate from  NO3_Pool in kgN/ha"
            )
            public Attribute.Double Nitri_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " denitrification rate from  NO3_Pool in kgN/ha"
            )
            public Attribute.Double Denit_trans;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff in  in kgN/ha"
            )
            public Attribute.Double SurfaceN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in  in kgN/ha"
            )
            public Attribute.Double InterflowN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in  in kgN/ha"
            )
            public Attribute.Double PercoN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in surface runoff in  in kgN"
            )
            public Attribute.Double SurfaceNabs;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in  in kgN"
            )
            public Attribute.Double InterflowNabs;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in percolation in  in kgN"
            )
            public Attribute.Double PercoNabs;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in surface runoff added to HRU layer in in kgN"
            )
            public Attribute.Double SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in interflow in added to HRU layer in kgN"
            )
            public Attribute.Double InterflowN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " Nitrate in percolation in added to HRU layer in kgN"
            )
            public Attribute.Double PercoN_in;
    
    
    
    // constants and calibration parameter

    
    
    /*
     *  Component run stages
     */
    
    
    
    private double runlayerdepth;
    private double runsoil_bulk_density;
 
    private double runC_org;
    private double runNO3_Pool;
    private double runNH4_Pool;
    private double runN_activ_pool;
    private double runN_stabel_pool;
    private double runN_residue_pool_fresh;
    private double runResidue_pool;
    private double runaEvap;
    private double runNH4inp;
    private double plantupN;
    private double surfaceN;
    private double interflowN;
    private double percoN;
    private double surfaceNabs;
    private double interflowNabs;
    private double percoNabs;
    private double surfaceN_in;
    private double interflowN_in;
    private double percoN_in;
    private double fr_actN = 0.02;      /** nitrogen active pool fraction. The fraction of organic nitrogen in the active pool. */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        
        
       
            double orgNhum = 0; /*concentration of humic organic nitrogen in the layer (mg/kg)*/
            this.runC_org = C_org.getValue();  
            this.runsoil_bulk_density = soil_bulk_density.getValue();
            this.runlayerdepth = layerdepth.getValue() * 100; //from dm to mm
            
            runResidue_pool = 10;
            runNO3_Pool = ((7 * Math.exp(-runlayerdepth/1000)) * runsoil_bulk_density * runlayerdepth)/100;
            runNH4_Pool = 0.1 * runNO3_Pool;
            orgNhum = 10000 * runC_org / 14;
            runN_activ_pool = ((orgNhum * fr_actN) * runsoil_bulk_density * runlayerdepth)/100;
            runN_stabel_pool = ((orgNhum * (1 - fr_actN)) * runsoil_bulk_density * runlayerdepth)/100;
            runN_residue_pool_fresh = 0.0015 * runResidue_pool;
            
            
            NO3_Pool.setValue(this.runNO3_Pool);
            NH4_Pool.setValue(this.runNH4_Pool);
            N_activ_pool.setValue(this.runN_activ_pool);
            N_stabel_pool.setValue(this.runN_stabel_pool);
            Residue_pool.setValue(this.runResidue_pool);
            N_residue_pool_fresh.setValue(this.runN_residue_pool_fresh);
            NH4inp.setValue(0);
            PlantupN.setValue(0);
            SurfaceN_in.setValue(0);
            InterflowN_in.setValue(0);
            PercoN_in.setValue(0);
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            PercoNabs.setValue(0);
    }
    
 
    public void cleanup() throws Attribute.Entity.NoSuchAttributeException{
        
    }
}
/*
		<component class="org.jams.j2k.s_n.init.initJ2KNSoil" name="initJ2KNSoil">
			<jamsvar name="NO3_Pool" provider="InitHRUContext" providervar="currentEntity.NO3_Pool"/>
			<jamsvar name="NH4_Pool" provider="InitHRUContext" providervar="currentEntity.NH4_Pool"/>
			<jamsvar name="N_activ_pool" provider="InitHRUContext" providervar="currentEntity.N_activ_pool"/>
			<jamsvar name="N_stabel_pool" provider="InitHRUContext" providervar="currentEntity.N_stabel_pool"/>
			<jamsvar name="Residue_pool" provider="InitHRUContext" providervar="currentEntity.residue_pool"/>
			<jamsvar name="N_residue_pool_fresh" provider="InitHRUContext" providervar="currentEntity.N_residue_pool_fresh"/>
			<jamsvar name="Volati_trans" provider="InitHRUContext" providervar="currentEntity.Volati_rate"/>
			<jamsvar name="NH4inp" provider="InitHRUContext" providervar="currentEntity.NH4inp"/>
			<jamsvar name="PlantupN" provider="InitHRUContext" providervar="currentEntity.PlantupN"/>
			<jamsvar name="Nitri_trans" provider="InitHRUContext" providervar="currentEntity.Nitri_rate"/>
			<jamsvar name="Denit_trans" provider="InitHRUContext" providervar="currentEntity.Denit_rate"/>
			<jamsvar name="SurfaceN" provider="InitHRUContext" providervar="currentEntity.SurfaceN"/>
			<jamsvar name="InterflowN" provider="InitHRUContext" providervar="currentEntity.InterflowN"/>
			<jamsvar name="PercoN" provider="InitHRUContext" providervar="currentEntity.PercoN"/>
			<jamsvar name="SurfaceNabs" provider="InitHRUContext" providervar="currentEntity.SurfaceNabs"/>
			<jamsvar name="InterflowNabs" provider="InitHRUContext" providervar="currentEntity.InterflowNabs"/>
			<jamsvar name="PercoNabs" provider="InitHRUContext" providervar="currentEntity.PercoNabs"/>
			<jamsvar name="SurfaceN_in" provider="InitHRUContext" providervar="currentEntity.SurfaceN_in"/>
			<jamsvar name="InterflowN_in" provider="InitHRUContext" providervar="currentEntity.InterflowN_in"/>
			<jamsvar name="PercoN_in" provider="InitHRUContext" providervar="currentEntity.PercoN_in"/>

			<jamsvar name="layerdepth" provider="InitHRUContext" providervar="currentEntity.rootDepth"/>
			<jamsvar name="totaldepth" provider="InitHRUContext" providervar="currentEntity.rootDepth"/>
			<jamsvar name="soil_bulk_density" value="1.3"/>
			<jamsvar name="C_org" value="1.5"/>
		</component>      
 */