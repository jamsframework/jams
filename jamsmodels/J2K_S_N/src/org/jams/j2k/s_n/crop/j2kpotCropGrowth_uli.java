    /*
     * j2kpotCropGrowth.java
     *
     * Created on 15. November 2005, 11:47
     * This file is part of JAMS
     *
     * Copyright (C) 2005 S. Kralisch and P. Krause
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
package org.jams.j2k.s_n.crop;

/**
 *
 * @author  c5ulbe
 */
import jams.model.*;
import jams.data.*;
import java.io.*;
import java.util.ArrayList;

@JAMSComponentDescription(
        title="j2kCropGrowth",
        author="Ulrike Bende-Michl",
        description="Module for calculation of crop growth according to the algorithms of SWAT"
        )
        
        public class j2kpotCropGrowth_uli extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current hru object"
            )
            public Attribute.Entity entity;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current hru object id"
            )
            public Attribute.Double idValue ;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area"
            )
            public Attribute.Double Area;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU daily mean temperature [°C]"
            )
            public Attribute.Double Tmean;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Daily solar radiation [MJ/m²]"
            )
            public Attribute.Double SolRad;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Biomass sum produced for a given day [kg/ha] drymass"
            )
            public Attribute.Double BioAct;
    
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Biomass sum produced for a day before [kg/ha] drymass"
            )
            public Attribute.Double BioOld; 
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual canopy Height [m]"
            )
            public Attribute.Double CanHeightAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual rooting depth [mm]"
            )
            public Attribute.Double ZRootD;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Fraction of nitrogen in the plant optimal biomass at the current growth's stage"
            )
            public Attribute.Double FNPlant;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual nitrogen stored in the plants biomass for a given day"
            )
            public Attribute.Double BioNact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Potential Crop Yield [kg/ha] for the actual day"
            )
            public Attribute.Double Yield;
    
    
  /*  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual Crop Yield [kg/ha]"
            )
            public Attribute.Double YieldAct; */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Biomass above ground on the day of harvest [kg/ha]"
            )
            public Attribute.Double BioagAct;
    
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "added residue pool after harvesting [kg N ha]"
            )
            public Attribute.Double Residue_pool;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water stress for a given day [mm H2O]"
            )
            public Attribute.Double Wstrs;*/
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU actual Transpiration [mm]"
            )
            public Attribute.Double aTP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU potential Transpiration [mm]"
            )
            public Attribute.Double pTP;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Light Extinct Coefficient [-0.65]"
            )
            public Attribute.Double LExCoef;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual N content in plants biomass [kg N/ha]"
            )
            public Attribute.Double BioNoptAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual plants N demand [kg N/ha]"
            )
            public Attribute.Double PlantNDemAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Daily fraction of max LAI [-]"
            )
            public Attribute.Double frLAImxAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Daily fraction of max LAI [-]"
            )
            public Attribute.Double frLAImx_xi;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Daily fraction of max root development [-]"
            )
            public Attribute.Double frRootAct;
       
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "root development of the day before [cm]"
            )
            public Attribute.Double ZRootOld;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual yield [kg/ha]"
            )
            public Attribute.Double BioYield;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual N content in yield [absolut]"
            )
            public Attribute.Double NYield;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual N content in yield [kg N/ha]"
            )
            public Attribute.Double NYield_ha;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual N in Biomass (after Stress)"
            )
            public Attribute.Double BioNAct;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;
    

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual harvest index [0-1]"
            )
            public Attribute.Double HarvIndex;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual potential heat units sum [-]"
            )
            public Attribute.Double FPHUact;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "delta potential heat units sum [-]"
            )
            public Attribute.Double PHUdelta;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "delta potential heat units sum [-]"
            )
            public Attribute.Double PHUdeltaold;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "delta LAI [-]"
            )
            public Attribute.Double LAIdelta;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual LAI"
            )
            public Attribute.Double LAI;
    
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "LAI of the day before "
            )
            public Attribute.Double LAIold;
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual nitrate uptake by plants in kgN/ha"
            )
            public Attribute.Double actN_up;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "flag plant existing yes or no " // attention its a boolean!
            )
            public Attribute.Boolean plantExisting;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual potential heat units sum [-]"
            )
            public Attribute.Double PHUact;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Plants base growth temperature [°C]"
            )
            public Attribute.Double tbase;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Plants optimum growth temperature [°C]"
            )
            public Attribute.Double topt;
    
       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Plants optimum growth temperature [°C]"
            )
            public Attribute.Double Test;
    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Plants daily biomass increase [kg/ha]"
            )
            public Attribute.Double BioOpt_delta;
    
          
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reset plant state variables?"
            )
            public Attribute.Boolean plantStateReset;
    
    
     /*
      
      *  Component run stages
      */
    
    
    private double area_ha;
    private double sc1_LAI;
    private double sc2_LAI;
    private double sc3_Nbio;
    private double sc4_Nbio;
    
    private double frLAImx_act;
    private double lai_act;
    private double lai_old;
    private double fnplant_act;
    
    private double residue_pool;
    private double hc_act;
    
    private double idc;
    private double phu_50;
    private double phu;
    private double phu_delta;
    private double fphu_act;
    private double phu_daily;
    private double phu_deltaold;
    private double phu_Xi;
    private double aTransP;
    private double pTransP;
    
    private double int_lai;
    private double mlai1;
    private double mlai;
    private double mlai2;
    
    private double frgrw1;
    private double frgrw2;
    private double frLAImx;
    private double frLAImx_actnew;
    private double frLAImx_Xi;
    private double LAI_actnew;
    private double solrad;
    private double rue;
    private double leco;
    
    private double chtmx;
    private double rdmx;
    private double frroot_act;
    private double zrootd_act;
    private double zrootd_old;
    private double zrootd;
    
    private double bn1;
    private double bn2;
    private double bn3;
    
    private double betaN;
    private double hvsti;
    private double cnyld;
    
    private double Ndemand_act;
    private double bioNopt_act;
    private double bioN_act;
    private double bio_act;
    private double bio_opt;
    private double bio_old;
    private double bioopt_ha;
    private double hi_act;
    private double bioag_act;
    private double yldN;
    private double yldN_ha;
    private double yield;
    
    private double tmean;
    private double Tbase;
    private double Topt;
    private double julday;
    
    public double famount;
    public boolean plant;
    public int harvest;
    public double fracharvest;
    
    
    private double LAI_delta;
    private double frLAImx_delta;
    
    private double enty_id;
    private double test;
    private double soil_no3;
    private double bioNopt_accumu ;
    private double Ndemand_accumu;
    private double actN_uprun;
    
    private double bio_opt_delta;
    
   /* double dec; // days to reach specific plant stage
    double hu_ec; // Potential heat units to reach specific plant stage
    int PS = 0;     //Plant status for optimal growth*/
    
    
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        /* this.bp1 = crop.bp1; // Phosphate uptake parameter not used at the moment
        this.bp2 = crop.bp2;
        this.bp3 = crop.bp3;*/
        // this.rsdco_pl = crop.rsdco_pl;
        /* this.hu_ec1 = crop.hu_ec1;
        this.enty_id = idValue.getValue();*/
        this.test = Test.getValue();
        this.tmean = Tmean.getValue();
        this.fphu_act = FPHUact.getValue();
        this.phu_daily = PHUact.getValue();
        this.phu_delta = PHUdelta.getValue();
        this.phu_deltaold = PHUdeltaold.getValue();
        this.area_ha = Area.getValue() /10000;
        this.solrad = SolRad.getValue();
        this.leco = LExCoef.getValue();
        this.frLAImx_act = frLAImxAct.getValue();
        this.frLAImx_Xi = frLAImx_xi.getValue();
        this.lai_act = LAI.getValue();
        this.LAI_delta = LAIdelta.getValue();
        this.lai_old = LAIold.getValue();
        this.hc_act = CanHeightAct.getValue(); /*actual Canopy height [m] on a given day */
        this.frroot_act = frRootAct.getValue();
        this.zrootd_act = ZRootD.getValue();
        this.zrootd_old = ZRootOld.getValue();
        this.fnplant_act = FNPlant.getValue();
        this.bioNopt_act = BioNoptAct.getValue(); /*actual biomass in kg/ha, optimal conditions */
        this.Ndemand_act = PlantNDemAct.getValue();
        this.bioN_act = BioNAct.getValue(); /*actual biomass in kg/ha adapted by stress*/
        this.hi_act = HarvIndex.getValue();;
        this.bioag_act = BioagAct.getValue();
        this.bio_opt = BioAct.getValue();
        this.actN_uprun = actN_up.getValue();
        this.bio_opt_delta= BioOpt_delta.getValue();
        this.yield =  BioYield.getValue();
        this.yldN =  NYield.getValue();
        this.yldN_ha = NYield_ha.getValue();
        this.residue_pool = Residue_pool.getValue();
               
        
        ArrayList<J2KSNCrop> rotation = (ArrayList<J2KSNCrop>) entity.getObject("landuseRotation");
        int rotPos = entity.getInt("rotPos");
        J2KSNCrop crop = rotation.get(rotPos);
        
        this.phu = crop.phu; /* total heat units required to reach maturity */
        this.idc = crop.idc;
        this.rue = crop.rue; // Radiation use efficiency
        this.hvsti = crop.hvsti;
        this.frgrw1 = crop.frgrw1; //Fraction of growing season corresponding to the first point of the optimal LAI development*/
        this.frgrw2 = crop.frgrw2; //Fraction of growing season corresponding to the second point of the optimal LAI development*/
        this.mlai = crop.mlai;
        this.mlai1 = crop.laimx1;
        this.mlai2 = crop.laimx2;
        /*this.dlai = crop.dlai;*/
        this.chtmx = crop.chtmx;
        this.rdmx = crop.rdmx;
        this.Topt = crop.topt; //Optimal growth temperature
        this.Tbase = crop.tbase;
        this.cnyld = crop.cnyld;
        this.bn1 = crop.bn1; //Normal fraction of N in the plant biomass at the emergence
        this.bn2 = crop.bn2; //Normal fraction of N in the plant biomass at 50% of plant growth
        this.bn3 = crop.bn3; //Normal fraction of N in the plant biomass near harvest
        
        
        /*ArrayList<J2KSNLMArable> managementList = currentCrop.managementList;
        int managementPos = entity.getInt("managementPos");
        J2KSNLMArable currentManagement = managementList.get(managementPos);*/
        
        if (plantExisting.getValue()) {
            calc_phu();
            calc_lai();
            bio_opt = calc_biomass();
            hc_act = calc_canopy();
            calc_root();
            calc_maturity();
            calc_nuptake();
            calc_cropyield();
            calc_cropyield_ha();
            calc_residues ();
            
            PHUdelta.setValue(phu_delta);
            //PHUdeltaold.setValue(phu_deltaold);
            PHUact.setValue(phu_daily);
            //tbase.setValue(Tbase);
            //topt.setValue(Topt);
            frLAImxAct.setValue(frLAImx_act); /*actual fraction of max LAI for a given day */
            LAIdelta.setValue(LAI_delta);
            LAI.setValue(lai_act);
            LAIold.setValue(lai_old);
            // BioOpt.setValue(bio_opt);
            // BioOpt.setValue(bio_opt * area_ha); /*Plants optimal biomass */
            CanHeightAct.setValue(hc_act); /*Actual canopy height */
            frRootAct.setValue(frroot_act);  /* daily fraction of root development [mm] */
            ZRootD.setValue(zrootd_act);  /* daily root development [mm] */
            ZRootOld.setValue(zrootd_old); 
            FNPlant.setValue(fnplant_act); /* daily fraction of N in plant biomass */
            BioNoptAct.setValue(bioNopt_act);
            BioAct.setValue(bio_opt); /*Plants optimal biomass */
            BioOld.setValue(bio_old);
            PlantNDemAct.setValue(Ndemand_act);
            BioOpt_delta.setValue(bio_opt_delta);
            HarvIndex.setValue(hi_act);
            BioagAct.setValue(bioag_act);
            BioYield.setValue(yield);
            NYield.setValue(yldN); /* N Content from the above biomass */
            NYield_ha.setValue(yldN_ha);
            FPHUact.setValue(fphu_act);
            BioNAct.setValue(bioN_act); /*actual biomass in kg/ha adapted by stress*/
            frLAImx_xi.setValue(frLAImx_Xi);
            Residue_pool.setValue(residue_pool);
            plantStateReset.setValue(true);
            Test.setValue(test);
            
        } else if (plantStateReset.getValue()) {
            
            //System.out.println("########################## resetting values ##########################");
            PHUdelta.setValue(0);
            PHUdeltaold.setValue(0);
            LAIdelta.setValue(0);
            frLAImxAct.setValue(0); /*actual fraction of max LAI for a given day */
            frLAImx_xi.setValue(0);
            LAI.setValue(0);
            LAIold.setValue(0);
            // BioOpt.setValue(bio_opt);
            // BioOpt.setValue(bio_opt * area_ha); /*Plants optimal biomass */
            CanHeightAct.setValue(0); /*Actual canopy height */
            frRootAct.setValue(0);  /* daily fraction of root development [mm] */
            ZRootD.setValue(0);  /* daily root development [mm] */
            ZRootOld.setValue(0);
            FNPlant.setValue(0); /* daily fraction of N in plant biomass */
            BioNoptAct.setValue(0);
            BioAct.setValue(0); /*Plants optimal biomass */
            PlantNDemAct.setValue(0);
            HarvIndex.setValue(0);
            BioagAct.setValue(0);
            BioOld.setValue(0);
            BioYield.setValue(0);
            BioOpt_delta.setValue(0);
            NYield.setValue(0); /* N Content from the above biomass */
            NYield_ha.setValue(0);
            FPHUact.setValue(0);
            BioNAct.setValue(0); /*actual biomass in kg/ha adapted by stress*/
            PHUact.setValue(0);
            PlantNDemAct.setValue(0);
            plantStateReset.setValue(false);
            PlantNDemAct.setValue(0);
            Residue_pool.setValue(0);
            Test.setValue(0);
            
        }
    }
//        idc = idc+1;
//        cropClass.setValue(idc);
    
//        cropClass.setValue(cropClass.getValue()+1);
    
    
// Optimal growth
    //
    // Biomass production
    //
    // First the daily development of the LAI is calculated as a fraction of maximimum LAI development (frLAImx)
    // Hereby the fraction of plants maximum leaf area index corresponding to a given fraction of PHU is calculated
    // and two shape-coefficient, sc1 and sc2 are needed
    // calculation the maximum leaf area corresponding to fraction of heat units,
    // expressed as LAI fraction of the known max LAI
    // @todo declare how is continuosly vegetated land use is determined
    
   private boolean calc_phu() {
        if (this.tmean > this.Tbase) {
            this.phu_daily = phu_daily + (this.tmean - this.Tbase); //phänologisch wirksame Temperatursumme
            this.fphu_act = this.phu_daily / this.phu;
            }
        //*double tDelta = this.Tbase - this.tmean;
        /*if (tDelta > 0) {
            this.phu_daily = this.phu_daily + tDelta;
             this.fphu_act = this.phu_daily / this.phu;
        }*/
        //}
        return true;
    }
   
    
   
    
    
    private boolean calc_lai() {
        
         /* Shape coefficients
            sc to determine LAI development */
        double sc1_lai1 = Math.log(this.frgrw1/this.mlai1- this.frgrw1);
        double sc2_lai2 = Math.log(this.frgrw2/this.mlai2- this.frgrw2);
        double sc_frpuh = this.frgrw2 - this.frgrw1;
        
        sc2_LAI = (sc1_lai1 - sc2_lai2)/sc_frpuh;
        sc1_LAI = sc1_lai1 + sc2_LAI * this.frgrw1;
        
        double sc_minus = this.fphu_act * sc2_LAI;
        //System.out.println("scaling factors LAI: " + sc1_LAI +" "+  sc2_LAI +" ");
        
        /* Fraction of plant's maximum LAI */
        frLAImx_Xi = frLAImx_act; // save frLAImx from the day before
        double x = (this.fphu_act + (Math.exp(sc1_LAI - sc_minus)));
        frLAImx_act = this.fphu_act / x;
        frLAImx_delta = frLAImx_act - frLAImx_Xi; //
       
        //System.out.println("factors LAI: " + frLAImx_act  +" "+  frLAImx_Xi +" "+  frLAImx_delta +" ");
        
        // Total leaf area index is calculated by frLAImx added on a day
        //lai_old = LAI_delta;
        double u1 = lai_old - this.mlai;
        double u2 = 5.0 * u1;
        double u3 = frLAImx_delta;
        this.LAI_delta = u3 * this.mlai *(1 - Math.exp(u2));
        this.lai_act = this.lai_act + this.LAI_delta;
        //this.lai_act = this.lai_old + this.LAI_delta;
        if 
                (this.lai_act == this.mlai){
                 this.lai_act = this.mlai;
        }
        
        //System.out.println("factors LAI: " + this.lai_act +" "+  this.LAI_delta +" "+  u1 +" ");
        
//        System.out.println(lai_act);
        
        // LAI will remain constant until leaf senescence begins to exceed leaf growth
        // this. phu_sense is the fphu when senescence becomes dominant
        // @todo declare what is fphu_sense; here assumed by phu 0.99 for forests determined by idc;
        // @todo declare when and what happens to the residues
        
        double fphu_sense = 0.99;
        
        if
                (this.idc == 7 || this.fphu_act > fphu_sense) {
            lai_act = 16 * this.mlai * Math.pow(1 - this.fphu_act,2);
            
        }
        return true;
    }
// Second the amount of daily solar radiation intercepted by the leaf area of the plant is calculated
// this.solrad = incoming total solar
// Hphosyn = the amount of intercepted photosynthetically active radiation on a day [MJ m-2]
    
// Third the amount of biomass (dry weight in kg/ha) produced per unit intercepted solar radiation is calulated using the plant-specific
// radiation-use efficiency declared in the crop growth database by parameter 'rue' in crop.par
// whereas the total biomass on a given day is summed up
    
    private double calc_biomass() {
        
        double Hphosyn = 0.5 * this.solrad * (1 - Math.exp(this.leco*this.lai_act)); // Intercepted photosynthetically active radiation [MJ/m²]
        
        this.bio_opt_delta = this.rue * Hphosyn;
        this.bio_opt = bio_opt_delta +  this.bio_opt;
        
        return bio_opt;
    }
    
//
// Canopy height and cover
//
// Canopy cover is expressed as leaf area index
// hc_daily = canopy height (m) for a given day
// mlai = Maximum LAI Parameter from crop.par
// chtmx = maximum canopy height (m), Parameter from crop.par
// frLAImx = fraction of plants maximum canopy height
    
    private double calc_canopy() {
        
        //double hc_old = hc_delta;
        double hc_delta = this.chtmx * Math.sqrt(frLAImx_act);
        hc_act = hc_delta + this.hc_act;
        
        return hc_act;
    }
//
// Root development
//
// Amount of total plants biomass partioned to the root system
// in general it varies in between 30-50% in seedlings and decreases to 5-20% in mature plants
// fraction of biomass in roots by SWAT varies between 0.40 at emergence and 0.20 at maturity
// daily fraction of root biomass is calculated by
    
// Fraction of root biomass
//
    private boolean calc_root() {
        
        frroot_act = 0.40 - 0.20 * this.fphu_act;
        //frroot_act = frroot + frroot_act;
        
     /* calculation of the root depth according to the plant types and conditions of IDC
          IDC Land cover/plant classification:
        1 warm season annual legume
        2 cold season annual legume
        3 perennial legume
        4 warm season annual
        5 cold season annual
        6 perennial
        7 trees
        Processes modeled differently for the 7 groups are:
        1 warm season annual legume
        • simulate nitrogen fixation
        • root depth varies during growing season due to root growth
        2 cold season annual legume
        • simulate nitrogen fixation
        • root depth varies during growing season due to root growth
        • fall-planted land covers will go dormant when daylength is less than the threshold daylength
        3 perennial legume
        • simulate nitrogen fixation
        • root depth always equal to the maximum allowed for the plant species and soil
        • plant goes dormant when daylength is less than the threshold daylength
        4 warm season annual
        • root depth varies during growing season due to root growth
        5 cold season annual
        • root depth varies during growing season due to root growth
        • fall-planted land covers will go dormant when daylength is less than the threshold daylength
        6 perennial
        • root depth always equal to the maximum allowed for the plant species and soil
        • plant goes dormant when daylength is less than the threshold daylength
        7 trees
        • root depth always equal to the maximum allowed for the plant species and soil
        • partitions new growth between leaves/needles (30%) and woody growth (70%). At the end of each growing season, biomass in the leaf fraction is converted to residue*/
        
        // Root development (mm in the soil) for plant types on a given day
        
        // Varying linearly from 0.0 at the beginning of the growing season to the maximum rooting depth at fphu = 0.4
        // Perennials and trees, as therefore rooting depth is not varying
        // idValue.getValue() == 6)
        if
                (this.idc == 3 || this.idc == 6 || this.idc == 7) {
            double zrootd_act = this.rdmx;
        }
        
        // annuals
        // if case: as long pfhu is within 0.4; as fphu 0.4 is the time of max root depth
        
        if
                (this.idc ==  1 || this.idc == 2 || this.idc == 4 || this.idc == 5 && this.fphu_act <= 0.40) {
            
            double zrootd = 2.5 * this.fphu_act * this.rdmx;
            zrootd_act = zrootd_act + zrootd;
        }
        if
                (this.fphu_act > 0.40) {
            zrootd_act = this.rdmx;
        }
       // System.out.println(" aktuelle idc_no: " + idc + " - ");
        return true;
    }
        
// Maturity
    
// is reached when fphu_act = 1
// as therefore no calculation is needed
// @todo nutrients & water uptake & transpiration will stopp depending on the condition fphu = 1
    
    private boolean calc_maturity(){
        
        if
                (this.fphu_act >= 1.00) {
            this.aTransP = 0;
            double Nup_act = 0;
            double Wup_act = 0;
        }
        return true;
    }
    
// Water uptake by plants
// Potential water uptake
    
    
// Nutrient uptake by plants
    
    private boolean calc_nuptake() {
        // is calculated by the fraction of the plant biomass as a function of growth stage given the optimal conditions
        // fnplant =fraction N in plant biomass
        // with bn1 as fraction of N in the plant biomass at the emergence
        // with bn2 as fraction of N in the plant biomass near the middle of the growing  season (bevor Blütenstand hevortritt)
        // with bn3 as fraction of N in the plant biomass at the maturity
        // with bn3_ca as fraction of N in the plant biomass near maturity
        // sc1_Nbio and sc2_Nbio are shape coefficients by solving the equation of two known points
        // (frn2 by 50% of PHU and frn3 by 100% of PHU
        
        // First calculation of shape coefficients n1 and n2 is needed
        
        phu_50 = this.phu / 2;
        //double frn_sub1 = this.bn1 - this.bn3;
        double b1 = this.bn1 - this.bn3;
        double t1 = Math.log(phu_50/( 1- (bn2 - bn3)/(bn1 - bn3) ) - phu_50);
        
        // WARNING : bug! Scaling factors
        // double sc2_Nbio = (t1 - Math.log(phu   /( 1- (0.00001)  /(bn1 - bn3) ) - phu)) / (phu - phu_50);
        // double sc1_Nbio = t1 + sc2_Nbio * phu_50;
        
        // scaling factors adopted manually by hand
        
         double sc2_Nbio = 30;
         double sc1_Nbio = 12;   
        /* Fraction of N in plant biomass as a function of growth stage given optimal conditions */
               
         double y = this.fphu_act / (this.fphu_act + Math.exp(sc1_Nbio - sc2_Nbio * this.fphu_act))    ;
         this.fnplant_act = b1 * (1- y) + this.bn3;
        
        
        // Determing the mass of N that should be stored in the plant biomass on a given day
        // whereas the fnplant is the optimal fraction of nitrogen in the plant biomass for the current growth stage
        // and bio_act is the total plant biomass on a given day [kg/ha]
        
        /* Mass N stored in the optimal plant biomass on a given day */
        
        bioNopt_act = this.fnplant_act * this.bio_opt;
               
        // Plant nitrogen demand
        // by taking the difference between the nitrogen content
        // of the optimal plants biomass and the actual N content of the plants biomass
        
       // double bioN_act;
        bioN_act = bioN_act + actN_uprun; //
        Ndemand_act = bioNopt_act - bioN_act; //@todo: declare the actual N content according to the
        
              
        // @todo should we take depth distribution into account? probably not as this point
        // N uptake within the soil profile
        
        if (this.betaN == 1) {
            
            double Nup_layer = zrootd_act;
            
        }else if (betaN > 1 ){
            
            double Nup_layer = this.betaN / zrootd_act * 100;
            
        }else if (betaN == 0 ){
            
            double Nup_layer = 0.1;
            
            double Nup_depth = Ndemand_act / (1 - Math.exp(-betaN)) * (1-Math.exp(-betaN * this.rdmx / zrootd_act));
        }
        return true;
        
    }
// Nitrogen fixation
// used when nitrate levels in the root zone are insufficient to meet the demand
    
// Phosphorus uptake
    
// Crop Yield
    private boolean calc_cropyield() {
       
        //for harvesting 4 codes are implemented:
        // (1) assumes harvesting with Haupt- & Nebenfrucht, plant growth stopped
        // (2) assumes harvesting with Hauptfrucht, Nebenfrucht remains on the field, plant growth stopped (former kill operation)
        // (3) assumes harvesting with Haupt- & Nebenfrucht, plant growth continues (may not be suitable for meadows)
        // (4) assumes harvesting with Hauptfrucht, plant growth continues//
        
        // when harvest&kill is determined in the crop management by code (1)
        // above-ground plant dry biomass removed as dry economic yield is called harvest index
        // for majority of crops the harvest index is between 0 and 1,
        // however for plant whose roots are harvested, such as potatos may have an harvest index greater than 1
        // harvest index is calculated for each day of the plant's growing season using the relationship
        // as hi is the potential harvest index for a given day and
        // hvsti is the potential harvest index for the plant
        // at maturity given ideal growing conditions (Parameter hvsti in crop.par)
        
        double u1 = 100 * this.fphu_act; 
        hi_act = this.hvsti * (u1)/(u1 + Math.exp(11.1 - 10.0 * this.fphu_act));
                
        // crop yield (kg/ha)is calculated as
        // above ground biomass
        
        double bio_ag = (1- frroot_act) * this.bio_opt; // actual aboveground biomass on the day of harvest
        bioag_act = bio_ag + bioag_act;
             
        
        if (this.fphu_act >= 1.00) {
            double bioag_harvest = bioag_act;
        }
        
        // total yield biomass on the day of harvest
        // @todo harvest options
        // first case: the total biomassis assumed to be yield
        if (this.hvsti <= 1) {
            this.yield = bioag_act * hi_act;
           // double yield_root = bio_root * hi_act;
        }
        // second case: a portion of the total biomass is assumed to be yield
        else if (this.hvsti > 1)											// bio is the total biomass on the day of the harvest (kg/ha)
        {
            this.yield = bio_opt * (1 - (1/(1+ hi_act)));
        }
       
        // Amounts of nitrogen [kg N/ha](and who wants P) to be removed from the field
        // whereas cnyld is the fraction of N being removed by the field crop
        
        this.yldN = this.cnyld * this.yield;
                
        //double yldP = this.cpyld * yield;
        return true;
    }
    private double calc_cropyield_ha() {
        
        this.yldN_ha = this.yldN * area_ha / 10000;
        
        return yldN_ha;
    }
    
    private boolean calc_residues () { 
        
        this.residue_pool = this.yield * (1 - this.fracharvest) ;
        return true;
    
    }
    public void cleanup()throws Attribute.Entity.NoSuchAttributeException{
        // store.close();
    }
    
    // public Attribute.Entity getEntityID() {
    //   return entityID;
    //}
    
}
