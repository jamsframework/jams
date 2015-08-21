/*
 * InitJ2KNaClSoillayer.java
 * Created on 17. February 2006, 14:49
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

package org.jams.j2k.s_salt_init;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title="InitJ2KNaClSoillayer",
        author="Manfred Fink",
        description="intitiallizing NaCl Pools in Soil and additional variables"
        )
        public class InitJ2KNaClSoillayer extends JAMSComponent  {
    
    /*
     *  Component variables
     */
    
     @JAMSVarDescription(
             access = JAMSVarDescription.AccessType.READWRITE,
             description = "Collection of hru objects"
             )
    public Attribute.EntityCollection hrus;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil layer"
            )
            public Attribute.DoubleArray layerdepth;
    
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
            )
            public Attribute.DoubleArray soil_bulk_density;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
            )
            public Attribute.DoubleArray C_org;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " NO3-Pool in kgN/ha"
            )
            public Attribute.DoubleArray NaCl_Pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Residue in Layer in kgN/ha"
            )
            public Attribute.DoubleArray Residue_pool;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " N-Organic fresh Pool from Residue in kgN/ha"
            )
            public Attribute.DoubleArray NaCl_residue_pool_fresh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = " Nitrate in interflow in added to HRU layer in kgN"
            )
            public Attribute.DoubleArray InterflowNaCl_in ;
   
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " Input of plant residues kg/ha"
            )
            public Attribute.Double inp_biomass;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Nitrogen input of plant residues in kgN/ha"
            )
            public Attribute.Double inpNaCl_biomass;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of layers in soil profile in [-]"
            )
            public Attribute.Double Layer;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "flag plant existing yes or no " // attention its a boolean!
            )
            public Attribute.Boolean plantExisting;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = " state variable LAI in [-]"
            )
            public Attribute.Double LAI;
         
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Actual rooting depth [dm]"
            )
            public Attribute.Double ZRootD;
    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual salt uptake by plants in kgNaCl/ha"
            )
            public Attribute.Double actNaCl_up;
    
    /*
     *  Component run stages
     */
    
    
    
    
    
    public void init() throws Attribute.Entity.NoSuchAttributeException{
        
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        Attribute.Entity actHRU;
        actHRU = hrus.getCurrent();

        int i = 0;
        
        int layer = (int)Layer.getValue();
        double runlayerdepth;
        plantExisting.setValue(true);
        double runsoil_bulk_density;
        
        double runC_org;
        
        double hor_dept = 0;
        
        double runNaCl_Pool;
        double[] NaCl_Poolvals = new double[layer];
        
        double runNaCl_residue_pool_fresh;
        double[] NaCl_residue_pool_freshvals = new double[layer];
        
        double runResidue_pool;
        double[] Residue_poolvals = new double[layer];
        
        double[] InterflowNaCl_invals = new double[layer];
        
        
 
        
        
        
        
        while (i < layer){
            
            runC_org = C_org.getValue()[i];
            runsoil_bulk_density = soil_bulk_density.getValue()[i];
            runlayerdepth = layerdepth.getValue()[i] * 10; //from cm to mm
            hor_dept = hor_dept + runlayerdepth;
            runResidue_pool = 10;
            runNaCl_Pool = ((7 * Math.exp(-hor_dept/1000)) * runsoil_bulk_density * runlayerdepth)/1000;
            
            if (i==0){
            runNaCl_Pool =  actHRU.getDouble("HOR_1") * 1000;
            }
            if (i==1){
            runNaCl_Pool =  actHRU.getDouble("HOR_2") * 1000;
            }
            if (i==2){
            runNaCl_Pool =  actHRU.getDouble("HOR_3") * 1000;
            }
            if (i==3){
            runNaCl_Pool =  actHRU.getDouble("HOR_4") * 1000;
            }
            if (i==4){
            runNaCl_Pool =  actHRU.getDouble("HOR_5") * 1000;
            }
            
            runNaCl_residue_pool_fresh = 0.0015 * runResidue_pool;
            NaCl_Poolvals[i] = runNaCl_Pool;           
           
            Residue_poolvals[i] = runResidue_pool;
            NaCl_residue_pool_freshvals[i] = runNaCl_residue_pool_fresh;
            InterflowNaCl_invals[i] = 0;
            
            
            i++;
        }
        
        NaCl_Pool.setValue(NaCl_Poolvals);
        Residue_pool.setValue(Residue_poolvals);
        NaCl_residue_pool_fresh.setValue(NaCl_residue_pool_freshvals);
        InterflowNaCl_in.setValue(InterflowNaCl_invals);
        inp_biomass.setValue(0);
        inpNaCl_biomass.setValue(0);
        LAI.setValue(0);
        ZRootD.setValue(0);
        actNaCl_up.setValue(0);
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