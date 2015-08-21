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
package crop;

import ages.types.HRU;
import oms3.annotations.*;
import static oms3.annotations.Role.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Author
    (name = "Ulrike Bende-Michl, Manfred Fink")
@Description
    ("Calculates crop growth using the SWAT crop growth model")
@Keywords
    ("Crop")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/crop/PotCropGrowth.java $")
@VersionInfo
    ("$Id: PotCropGrowth.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class PotCropGrowth  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + PotCropGrowth.class.getSimpleName());


// Parameter
    @Description("Light Extinct Coefficient [-0.65]")
    @Role(PARAMETER)
    @In public double LExCoef;

    @Description("Factor of rootdepth 0 - 10 default 1")
    @Role(PARAMETER)
    @In public double rootfactor;

// In
    @Description("Current organic fertilizer amount")
    @In public int rotPos;

    @Description("HRU attribute name area")
    @In public double area;

    @Description("HRU daily mean temperature")
    @Unit("C")
    @In public double tmean;
    
    @Description("Daily solar radiation")
    @Unit("MJ/mÂ²")
    @In public double solRad;

    @Description("indicates dormancy of plants")
    @In public boolean dormancy;

    @Description("max Root depth in soil")
    @Unit("m")
    @In public double soil_root;

    @Description("Type of harvest to distiguish between crops with undersown plants and normal harvesting")
    @In public int harvesttype;

    @Description("flag plant existing yes or no")
    @In public boolean plantExisting;

// Out
    
    @Description("Biomass added residue pool after harvesting")
    @Unit("kg/ha")
    @Out public double Addresidue_pool;
    
    @Description("Nitrogen added residue pool after harvesting")
    @Unit("kg N/ha")
    @Out public double Addresidue_pooln;
    
    @Description("Actual yield")
    @Unit("kg/ha")
    @Out public double BioYield;
    
    @Description("Actual N content in yield")
    @Unit("absolut")
    @Out public double NYield;
    
    @Description("Actual N content in yield")
    @Unit("kg N/ha")
    @Out public double NYield_ha;
   
    @Description("Plants base growth temperature")
    @Unit("C")
    @Out public double tbase;
    
    @Description("Plants optimum growth temperature")
    @Unit("C")
    @Out public double topt;

    @Description("Indicator for harvesting")
    @In public boolean doHarvest;

    @Description("id of the current crop")
    @Out public int cropid;

    @Description("Number of fertilisation action in crop")
    @In @Out public double gift;

// In Out

    @Description("Reset plant state variables?")
    @In @Out public boolean plantStateReset;

    @Description("actual LAI")
    @In @Out public double LAI;

    @Description("Biomass above ground on the day of harvest")
    @Unit("kg/ha")
    @In @Out public double BioagAct;
     
    @Description("actual potential heat units sum")
    @In @Out public double PHUact;

    @Description("Actual N content in plants biomass")
    @Unit("kg N/ha")
    @In @Out public double BioNoptAct;

    @Description("Daily fraction of max LAI")
    @In @Out public double frLAImxAct;

    @Description("Daily fraction of max LAI")
    @In @Out public double frLAImx_xi;

    @Description("Daily fraction of max root development")
    @In @Out public double frRootAct;

    @Description("Biomass sum produced for a given day drymass")
    @Unit("kg/ha")
    @In @Out public double BioAct;

    @Description("Actual canopy Height")
    @Unit("m")
    @In @Out public double CanHeightAct;

    @Description("Actual rooting depth")
    @Unit("m")
    @In @Out public double zrootd;

    @Description("Fraction of nitrogen in the plant optimal biomass at the current growth's stage")
    @In @Out public double FNPlant;

    @Description("Plants daily biomass increase")
    @Unit("kg/ha")
    @In @Out public double BioOpt_delta;

    @Description("Actual N in Biomass")
    @In @Out public double BioNAct;

    @Description("actual harvest index [0-1]")
    @In @Out public double HarvIndex;

    @Description("Fraction of actual potential heat units sum")
    @In @Out public double FPHUact;

    @Description("Current hru object")
    @In @Out public HRU hru;


//    @Description("delta LAI")
//    @In @Out public double LAIdelta;
    
    private double area_ha;
    private double sc1_LAI;
    private double sc2_LAI;
    private double frLAImx_act;
    private double lai_act;
    private double fnplant_act;
    private double addresidue_pool;
    private double addresidue_pooln;
    private double hc_act;
    private double idc;
    private double phu;
    private double fphu_act;
    private double phu_daily;
    private double mlai1;
    private double mlai;
    private double mlai2;
    private double lai_min;
    private double frgrw1;
    private double frgrw2;
    private double frLAImx_Xi;
    private double solrad;
    private double rue;
    private double leco;
    private double chtmx;
    private double rdmx;
    private double frroot_act;
    private double zrootd_act;
    private double bn1;
    private double bn2;
    private double bn3;
    private double hvsti;
    private double cnyld;
    private int cid;
    private double bioNopt_act;
    private double bioN_act;
    private double bio_opt;
    private double hi_act;
    private double bioag_act;
    private double yldN;
    private double yldN_ha;
    private double yield;
    private double Tmean;
    private double Tbase;
    private double Topt;
    public double famount;
    public boolean plant;
    public int harvest;
    public double fracharvest;
    public double fracharvestn;
    private double LAI_delta;
    private double frLAImx_delta;
    private double bio_opt_delta;

    @Execute
    public void execute() {
        yield = 0;
        yldN = 0; /* N Content from the above biomass */
        yldN_ha = 0;
        Tmean = tmean;
        fphu_act = FPHUact;
        phu_daily = PHUact;

        area_ha = area / 10000;
        solrad = solRad;
        leco = LExCoef;
        frLAImx_act = frLAImxAct;
        frLAImx_Xi = frLAImx_xi;
        lai_act = LAI;
//        LAI_delta = LAIdelta;
        hc_act = CanHeightAct; /*actual Canopy height [m] on a given day */
        frroot_act = frRootAct;
        zrootd_act = zrootd;

        fnplant_act = FNPlant;
        bioNopt_act = BioNoptAct; /*actual biomass in kg/ha, optimal conditions */

        bioN_act = BioNAct; /*actual biomass in kg/ha adapted by stress*/
        hi_act = HarvIndex;

        bioag_act = BioagAct;
        bio_opt = BioAct;
        bio_opt_delta = BioOpt_delta;

        ArrayList<J2KSNCrop> rotation = hru.landuseRotation;

        J2KSNCrop crop = rotation.get(rotPos);

        double oldcid = cropid;
        cid = crop.cid;

        if (oldcid == cid) {
        } else {
            gift = 0.0;
        }

        phu = crop.phu; /* total heat units required to reach maturity */
        idc = crop.idc;
        rue = crop.rue; // Radiation use efficiency
        hvsti = crop.hvsti;
        frgrw1 = crop.frgrw1; //Fraction of growing season corresponding to the first point of the optimal LAI development*/
        frgrw2 = crop.frgrw2; //Fraction of growing season corresponding to the second point of the optimal LAI development*/
        lai_min = crop.lai_min;
        mlai = crop.mlai;
        mlai1 = crop.laimx1;
        mlai2 = crop.laimx2;
        /*dlai = crop.dlai;*/
        chtmx = crop.chtmx;
        rdmx = crop.rdmx;
        Topt = crop.topt; //Optimal growth temperature
        Tbase = crop.tbase;
        cnyld = crop.cnyld;
        bn1 = crop.bn1; //Normal fraction of N in the plant biomass at the emergence
        bn2 = crop.bn2; //Normal fraction of N in the plant biomass at 50% of plant growth
        bn3 = crop.bn3; //Normal fraction of N in the plant biomass near harvest
        cropid = cid;

        /*ArrayList<J2KSNLMArable> managementList = currentCrop.managementList;
        int managementPos = entity.getInt("managementPos");
        J2KSNLMArable currentManagement = managementList.get(managementPos);*/
        if (crop.idc == 11) {
            phu_daily = 0;
            Tbase = 0;
            Topt = 0;
            frLAImx_act = 0; /*actual fraction of max LAI for a given day */
            LAI_delta = 0;
            lai_act = 1;
            // bio_opt = 0;
            // BioOpt = bio_opt * area_ha; /*Plants optimal biomass */
            hc_act = 0; /*Actual canopy height */
            frroot_act = 0;  /* daily fraction of root development [mm] */
            zrootd_act = 1000;  /* daily root development [mm] */

            fnplant_act = 0; /* daily fraction of N in plant biomass */
            bioNopt_act = 0;
            bio_opt = 0; /*Plants optimal biomass */


            bio_opt_delta = 0;
            hi_act = 0;
            bioag_act = 0;

            fphu_act = 0;
            bioN_act = 0; /*actual biomass in kg/ha adapted by stress*/
            frLAImx_Xi = 0;
            //residue_pool = 0;
            addresidue_pool = 0;
            addresidue_pooln = 0;
        } else if (plantExisting) {
            calc_phu();
            calc_lai();
            calc_biomass();
            hc_act = calc_canopy();
            calc_root();
            calc_nuptake();

            // time
            addresidue_pool = 0;
            addresidue_pooln = 0;
            if (doHarvest) {
                calc_cropyield();
                calc_cropyield_ha();
                calc_residues();
                doHarvest = false;
            }
        } else if (plantStateReset) {
            phu_daily = 0;
            Tbase = 0;
            Topt = 0;
            frLAImx_act = 0; /*actual fraction of max LAI for a given day */
            LAI_delta = 0;
            lai_act = 0;
            // bio_opt = 0;
            // BioOpt = bio_opt * area_ha; /*Plants optimal biomass */
            hc_act = 0; /*Actual canopy height */
            frroot_act = 0;  /* daily fraction of root development [mm] */
            zrootd_act = 0;  /* daily root development [mm] */

            fnplant_act = 0; /* daily fraction of N in plant biomass */
            bioNopt_act = 0;
            bio_opt = 0; /*Plants optimal biomass */

            bio_opt_delta = 0;
            hi_act = 0;
            bioag_act = 0;

            fphu_act = 0;
            bioN_act = 0; /*actual biomass in kg/ha adapted by stress*/
            frLAImx_Xi = 0;
            //residue_pool = 0;
            addresidue_pool = 0;
            addresidue_pooln = 0;
            plantStateReset = false;
             //System.out.println("########################## resetting values ##########################");
        }

        PHUact = phu_daily;
        tbase = Tbase;
        topt = Topt;
        frLAImxAct = frLAImx_act; /*actual fraction of max LAI for a given day */
//        LAIdelta = LAI_delta;
        LAI = lai_act;
        // BioOpt = bio_opt;
        // BioOpt = bio_opt * area_ha; /*Plants optimal biomass */
        CanHeightAct = hc_act; /*Actual canopy height */
        frRootAct = frroot_act;  /* daily fraction of root development [mm] */
        zrootd_act = Math.min(zrootd_act * rootfactor, soil_root);
        zrootd = zrootd_act;  /* daily root development [mm] */

        FNPlant = fnplant_act; /* daily fraction of N in plant biomass */
        BioNoptAct = bioNopt_act;
        BioAct = bio_opt; /*Plants optimal biomass */

        BioOpt_delta = bio_opt_delta;
        HarvIndex = hi_act;
        BioagAct = bioag_act;
        BioYield = yield;
        NYield = yldN; /* N Content from the above biomass */
        NYield_ha = yldN_ha;
        FPHUact = fphu_act;
        BioNAct = bioN_act; /*actual biomass in kg/ha adapted by stress*/
        frLAImx_xi = frLAImx_Xi;
        Addresidue_pool = addresidue_pool;
        Addresidue_pooln = addresidue_pooln;
        if (idc == 3 || idc == 6 || idc == 7) {
            plantStateReset = false;
        } else {
            plantStateReset = true;
        }

        if (log.isLoggable(Level.INFO)) {
//            log.info();
        }
    }

    // Biomass production.
    // First the daily development of the LAI is calculated as a fraction of maximimum LAI development (frLAImx)
    // Hereby the fraction of plants maximum leaf area index corresponding to a given fraction of PHU is calculated
    // and two shape-coefficient, sc1 and sc2 are needed
    // calculation the maximum leaf area corresponding to fraction of heat units,
    // expressed as LAI fraction of the known max LAI
    // @todo declare how is continuosly vegetated land use is determined
    private boolean calc_phu() {
        if (Tmean > Tbase) {
            phu_daily = phu_daily + (Tmean - Tbase); //phÃ¤nologisch wirksame Temperatursumme
            fphu_act = phu_daily / phu;
        }
        return true;
    }
 
    private boolean calc_lai() {
        /* Shape coefficients
        sc to determine LAI development */
        double sc1_lai1 = Math.log(frgrw1 / mlai1 - frgrw1);
        double sc2_lai2 = Math.log(frgrw2 / mlai2 - frgrw2);
        double sc_frpuh = frgrw2 - frgrw1;

        sc2_LAI = (sc1_lai1 - sc2_lai2) / sc_frpuh;
        sc1_LAI = sc1_lai1 + sc2_LAI * frgrw1;

        double sc_minus = fphu_act * sc2_LAI;
        //System.out.println("scaling factors LAI: " + sc1_LAI +" "+  sc2_LAI +" ");

        /* Fraction of plant's maximum LAI */
        frLAImx_Xi = frLAImx_act; // save frLAImx from the day before
        double x = (fphu_act + (Math.exp(sc1_LAI - sc_minus)));
        frLAImx_act = fphu_act / x;
        frLAImx_delta = frLAImx_act - frLAImx_Xi; //

        // Total leaf area index is calculated by frLAImx added on a day
        //lai_old = LAI_delta;
        double u1 = lai_act - mlai;
        double u2 = 5.0 * u1;
        double u3 = frLAImx_delta;
        LAI_delta = u3 * mlai * (1 - Math.exp(u2));
        /*       if (LAI_delta < 0){
        LAI_delta = 0;
        }*/
        lai_act = lai_act + LAI_delta;
        //lai_act = lai_old + LAI_delta;
        if (lai_act > mlai) {
            lai_act = mlai;
        }
        if (doHarvest && (idc == 3 || idc == 6 || idc == 7)) {
            frLAImx_act = 0;
            LAI_delta = 0;
            lai_act = lai_min;
            frLAImx_Xi = 0;
        }
        //System.out.println("factors LAI: " + lai_act +" "+  LAI_delta +" "+  u1 +" ");
        return true;
    }

    // Second the amount of daily solar radiation intercepted by the leaf area of the plant is calculated
    // solrad = incoming total solar
    // Hphosyn = the amount of intercepted photosynthetically active radiation on a day [MJ m-2]

    // Third the amount of biomass (dry weight in kg/ha) produced per unit intercepted solar radiation is calulated using the plant-specific
    // radiation-use efficiency declared in the crop growth database by parameter 'rue' in crop.par
    // whereas the total biomass on a given day is summed up
    private void calc_biomass() {
        double Hphosyn = 0.5 * solrad * (1 - Math.exp(leco * lai_act)); // Intercepted photosynthetically active radiation [MJ/mÂ²]
        bio_opt_delta = rue * Hphosyn;
        if (dormancy) {
            bio_opt_delta = 0;
        }
    }

    // Canopy height and cover
    //
    // Canopy cover is expressed as leaf area index
    // hc_daily = canopy height (m) for a given day
    // mlai = Maximum LAI Parameter from crop.par
    // chtmx = maximum canopy height (m), Parameter from crop.par
    // frLAImx = fraction of plants maximum canopy height
    private double calc_canopy() {
        //double hc_old = hc_delta;
        double hc_delta = chtmx * Math.sqrt(frLAImx_act);
        hc_act = hc_delta + hc_act;
        return hc_act;
    }

    // Root development
    //
    // Amount of total plants biomass partioned to the root system
    // in general it varies in between 30-50% in seedlings and decreases to 5-20% in mature plants
    // fraction of biomass in roots by SWAT varies between 0.40 at emergence and 0.20 at maturity
    // daily fraction of root biomass is calculated by
    // Fraction of root biomass
    private boolean calc_root() {
        double rootpartmodi = 0.20 * fphu_act;
        rootpartmodi = Math.min(rootpartmodi, 0.2);
        frroot_act = 0.40 - rootpartmodi;
        // Root development (mm in the soil) for plant types on a given day
        // Varying linearly from 0.0 at the beginning of the growing season to the maximum rooting depth at fphu = 0.4
        // Perennials and trees, as therefore rooting depth is not varying
        // idValue == 6)
        if (idc == 3 || idc == 6 || idc == 7) {
            zrootd_act = rdmx;
        }

        // annuals
        // if case: as long pfhu is within 0.4; as fphu 0.4 is the time of max root depth
        if ((idc == 1 || idc == 2 || idc == 4 || idc == 5 || idc == 8) && fphu_act <= 0.40) {
            zrootd_act = 2.5 * fphu_act * rdmx;
        }
        if (fphu_act > 0.40) {
            zrootd_act = rdmx;
        }
        return true;
    }

    // Maturity
    // is reached when fphu_act = 1
    // as therefore no calculation is needed
    // @todo nutrients & water uptake & transpiration will stopp depending on the condition fphu = 1
    // Water uptake by plants
    // Potential water uptake
    // Nutrient uptake by plants
    private boolean calc_nuptake() {
        // is calculated by the fraction of the plant biomass as a function of growth stage given the optimal conditions
        // fnplant =fraction N in plant biomass
        // with bn1 as fraction of N in the plant biomass at the emergence
        // with bn2 as fraction of N in the plant biomass near the middle of the growing  season (bevor BlÃ¼tenstand hevortritt)
        // with bn3 as fraction of N in the plant biomass at the maturity
        // with bn3_ca as fraction of N in the plant biomass near maturity
        // sc1_Nbio and sc2_Nbio are shape coefficients by solving the equation of two known points
        // (frn2 by 50% of PHU and frn3 by 100% of PHU

        /* Fraction of N in plant biomass as a function of growth stage given optimal conditions */
        //new Implementation by Manfred Fink
        if (bn1 > bn2 && bn2 > bn3 && bn3 > 0) {
            double s1 = Math.log((0.5 / (1 - ((bn2 - bn3) / (bn1 - bn3)))) - 0.5);
            double s2 = Math.log((1 / (1 - ((0.0001) / (bn1 - bn3)))) - 1);
            double n2 = (s1 - s2) / 0.5;
            double n1 = Math.log((0.5 / (1 - ((bn2 - bn3) / (bn1 - bn3)))) - 0.5) + (n2 * 0.5);
            fnplant_act = ((bn1 - bn3) * (1 - (fphu_act / (fphu_act + Math.exp(n1 - n2 * fphu_act))))) + bn3;
            if (harvesttype == 2) {
                fnplant_act = bn3;
            }
        } else {
            fnplant_act = 0.01;
        }
        // Determing the mass of N that should be stored in the plant biomass on a given day
        // whereas the fnplant is the optimal fraction of nitrogen in the plant biomass for the current growth stage
        // and bio_act is the total plant biomass on a given day [kg/ha]
        /* Mass N stored in the optimal plant biomass on a given day */
        bioNopt_act = fnplant_act * bio_opt;
        return true;
    }
    
    // Nitrogen fixation
    // used when nitrate levels in the root zone are insufficient to meet the demand

    // Phosphorus uptake
    // Crop Yield
    private boolean calc_cropyield() {
        if (idc == 3 || idc == 6 || idc == 7 || (idc == 8)) {

            double u1 = 100 * fphu_act;
            hi_act = hvsti * (u1) / (u1 + Math.exp(11.1 - 10.0 * fphu_act));
            hi_act = Math.max(hi_act, hvsti * 0.75);
            hi_act = Math.min(hi_act, hvsti);
            // crop yield (kg/ha)is calculated as
            // above ground biomass

            double bio_ag = (1 - frroot_act) * bio_opt; // actual aboveground biomass on the day of harvest
            bioag_act = bio_ag;

            // total yield biomass on the day of harvest
            // @todo harvest options
            // first case: the total biomassis assumed to be yield
            if (hvsti <= 1) {
                yield = bioag_act * hi_act;
                if (yield > bioag_act) {
                    yield = bioag_act;
                }
            // double yield_root = bio_root * hi_act;
            } // second case: a portion of the total biomass is assumed to be yield
            else if (hvsti > 1) // bio is the total biomass on the day of the harvest (kg/ha)
            {
                yield = bio_opt * (1 - (1 / (1 + hi_act)));
            }
            // Amounts of nitrogen [kg N/ha](and who wants P) to be removed from the field
            // whereas cnyld is the fraction of N being removed by the field crop
            yldN = bioN_act * (yield / bio_opt);
            //System.out.println (" Julianischer Tag "+ Attribute.Calendar.DAY_OF_YEAR + " hi_act: " + hi_act +  " hvsti: " + hvsti +  " fphu: " + fphu_act + " yldN " + yldN + " yield " + yield);
            //double yldP = cpyld * yield; time
            if (idc == 7) {
                fphu_act = 0;
            } else {
                fphu_act = Math.min(fphu_act, 1);
                fphu_act = (fphu_act * (1 - (yield / bio_opt)));
            }
            phu_daily = phu * fphu_act;
            bio_opt = bio_opt - yield;
            if (bio_opt < 0) {
                bio_opt = 0;
            }
            bioN_act = bioN_act - yldN;
            fracharvest = 1 - (yield / bio_opt);
            fracharvestn = 1 - (yldN / bioN_act);
        } else {
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
            double u1 = 100 * fphu_act;
            hi_act = hvsti * (u1) / (u1 + Math.exp(11.1 - 10.0 * fphu_act));
            hi_act = Math.max(hi_act, hvsti * 0.75);
            // crop yield (kg/ha)is calculated as
            // above ground biomass

            double bio_ag = (1 - frroot_act) * bio_opt; // actual aboveground biomass on the day of harvest
            bioag_act = bio_ag;

            // total yield biomass on the day of harvest
            // @todo harvest options
            // first case: the total biomassis assumed to be yield
            if (hvsti <= 1) {
                yield = bioag_act * hi_act;
            // double yield_root = bio_root * hi_act;
            } // second case: a portion of the total biomass is assumed to be yield
            else if (hvsti > 1) // bio is the total biomass on the day of the harvest (kg/ha)
            {
                yield = bio_opt * (1 - (1 / (1 + hi_act)));
            }
            /*if (idValue == 6) {
            System.out.println(" hi_act: " + hi_act +  " hvsti: " + hvsti +  " fphu: " + fphu_act + " - ");
            }*/
            // Amounts of nitrogen [kg N/ha](and who wants P) to be removed from the field
            // whereas cnyld is the fraction of N being removed by the field crop

            yldN = cnyld * yield;
            if (yldN > bioN_act * (yldN / (yldN + ((bio_opt - yield) * (bn3 / 2.0))))) {
                yldN = bioN_act * (yldN / (yldN + ((bio_opt - yield) * (bn3 / 2.0))));
            }
            //System.out.println (" Julianischer Tag "+ Attribute.Calendar.DAY_OF_YEAR + " hi_act: " + hi_act +  " hvsti: " + hvsti +  " fphu: " + fphu_act + " yldN " + yldN + " yield " + yield);
            //double yldP = cpyld * yield;
            fracharvest = 1 - (yield / bio_opt);
            fracharvestn = 1 - (yldN / bioN_act);
        }
        return true;
    }

    private double calc_cropyield_ha() {
        yldN_ha = yldN * area_ha / 10000;
        return yldN_ha;
    }

    private boolean calc_residues() {
        if (idc == 7) {
            addresidue_pool = yield;
            addresidue_pooln = yldN;
        } else if (idc == 1 || idc == 2 || idc == 4 || idc == 5) {
            addresidue_pool = bio_opt - yield;
            addresidue_pooln = bioN_act - yldN;
        } else if (idc == 6 || idc == 3) {
            addresidue_pool = yield * 0.1;
            addresidue_pooln = yldN * 0.1;
            addresidue_pool = Math.min(addresidue_pool, bio_opt);
            addresidue_pooln = Math.min(addresidue_pooln, bioN_act);
            bio_opt = bio_opt - addresidue_pool;
            bioN_act = bioN_act - addresidue_pooln;
        } else if (idc == 8) {
            addresidue_pool = yield * 0.1;
            addresidue_pooln = yldN * 0.1;
            addresidue_pool = Math.min(addresidue_pool, bio_opt);
            addresidue_pooln = Math.min(addresidue_pooln, bioN_act);
            bio_opt = bio_opt - addresidue_pool;
            bioN_act = bioN_act - addresidue_pooln;
        }
        return true;
    }
}
