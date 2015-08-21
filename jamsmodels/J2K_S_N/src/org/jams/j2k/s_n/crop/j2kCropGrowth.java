    /*
     * j2kCropGrowth.java
     *
     * Created on 15. November 2005, 11:47
     */

package org.jams.j2k.s_n.crop;

/**
 *
 * @author  c5ulbe
 */
import jams.model.*;
import jams.data.*;
import java.io.*;

@JAMSComponentDescription(
        title="j2kCropGrowth",
        author="Ulrike Bende-Michl",
        description="Module for calculation of crop growth according to the algorithms of SWAT"
        )
        
        public class j2kCropGrowth extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ
            )
            public Attribute.String fileName;
    
  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area"
            )
            public Attribute.Double Area;
    
 /*   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop id"
            )
            public Attribute.String cropID;*/
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.Double LClass;
    
  /*  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU land use typ (same as J2K land use id)"
            )
            public Attribute.String PTyp;*/
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU daily mean temperature [°C]"
            )
            public Attribute.Double Tmean;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU plant temperature [°C]"
            )
            public Attribute.Double Tbase;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU daily potential heat units"
            )
            public Attribute.Double PHUact;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU daily zero based heat units"
            )
            public Attribute.Double HU0act;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU fraction daily potential heat units"
            )
            public Attribute.Double FPHUact;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU fraction daily zero based potential heat units"
            )
            public Attribute.Double FHU0act;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU half of annual potential heat units"
            )
            public Attribute.Double PHU_50;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max LAI corresponding to the first point of the optimal LAI development"
            )
            public Attribute.Double MLAI1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max LAI corresponding to the second point of the optimal LAI development"
            
            )
            public Attribute.Double MLAI2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximimum LAI"
            )
            public Attribute.Double MLAI;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual LAI for a given day [-]"
            )
            public Attribute.Double LAI;
    
     
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of growing season corresponding to the first point of the optimal LAI development"
            )
            public Attribute.Double FrcGrow1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of growing season corresponding to the second point of the optimal LAI development"
            )
            public Attribute.Double FrcGrow2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily solar radiation [MJ/m²]"
            )
            public Attribute.Double SolRad;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Crop specific radiation use efficiency ([kg/ha] drymass per[MJm²])"
            )
            public Attribute.Double RadUse;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Intercepted photosynthetically active radiation [MJ/m²]"
            )
            public Attribute.Double Hphosyn;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Biomass sum produced for a given day [kg/ha] drymass"
            )
            public Attribute.Double BioAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Canopy Height [m]"
            )
            public Attribute.Double CanHeight;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum Canopy Height [m]"
            )
            public Attribute.Double MCanHeight;
    
      
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum rooting depth [mm]"
            )
            public Attribute.Double MRootD;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual rooting depth [mm]"
            )
            public Attribute.Double ZRootD;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal fraction of N in the plant biomass at the emergence"
            )
            public Attribute.Double Nuptake_1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal fraction of N in the plant biomass at 50% of maturity"
            )
            public Attribute.Double Nuptake_2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal fraction of N in the plant biomass at maturity"
            )
            public Attribute.Double Nuptake_3;
   
  
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optimal fraction of nitrogen in the plant biomass at the current growth's stage"
            )
            public Attribute.Double FNPlant; 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optimal plant nitrogen content in the plants biomass for a given day"
            )
            public Attribute.Double BioNopt;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Plants nitrogen demand for a given day"
            )
            public Attribute.Double BioNdem;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual nitrogen stored in the plants biomass for a given day"
            )
            public Attribute.Double BioNact;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Harvest Index [0-1]"
            )
            public Attribute.Double HarvIndex;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of harvest"
            )
            public Attribute.Double FracHarv;
    
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lower boundary of harvest index [0-1]"
            )
            public Attribute.Double LHarIndex;
    
    /* @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Potential Crop Yield [kg/ha]"
            )
            public Attribute.Double YieldPot;
    
  /*  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual Crop Yield [kg/ha]"
            )
            public Attribute.Double YieldAct; */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Biomass above ground on the day of harvest [kg/ha]"
            )
            public Attribute.Double BioagAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction N in Yield"
            )
            public Attribute.Double CNyld; 
    
 /*   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction P in Yield"
            )
            public Attribute.Double FrcPyld;*/
    
        
 /*   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Scaling factor for N stress"
            )
            public Attribute.Double ScalN; */
    
    
 /*   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optimal temperature for plant growth [°C]"
            )
            public Attribute.Double TOpt; */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Residuen pool (biomass)"
            )
            public Attribute.Double Residue_pool;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water stress for a given day [mm H2O]"
            )
            public Attribute.Double Wstrs;*/
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
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
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial Leaf Area Index [-]"
            )
            public Attribute.Double ILAI;
    
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Optimal daily biomass development [kg/ha]"
            )
            public Attribute.Double BioOpt;
    
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Actual daily Canopy Height [m]"
            )
            public Attribute.Double CanHeightAct; 
    
    
 
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
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily fraction of max LAI [-]"
            )
            public Attribute.Double frLAImxAct;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily fraction of max root development [-]"
            )
            public Attribute.Double frRootAct;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual yield [kg/ha]"
            )
            public Attribute.Double BioYield;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual N content in yield [absolut]"
            )
            public Attribute.Double NYield;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual N content in yield [kg N/ha]"
            )
            public Attribute.Double NYield_ha;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N distribution factor"
            )
            public Attribute.Double BetaN;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual N in Biomass (after Stress)"
            )
            public Attribute.Double BioNAct;
     
 /*     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of Heat Units required for EC stage "
            )
            public Attribute.DoubleArray HU_ECArray = new Attribute.DoubleArray();
           
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Array of days required for EC stage "
            )
            public Attribute.DoubleArray D_ECArray = new Attribute.DoubleArray(); 
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "time"
            )
            public Attribute.Calendar time;*/
    
      
        
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
    private double fnplant_act;
              
    private double residue_pool;
    private double hc_act;
    
    private double idc;
    private double phu_50;
    private double phu;  
    private double fphu_act;
    
    private double aTransP;
    private double pTransP;
    
    private double int_lai;
    private double mlai1;
    private double mlai;
    private double mlai2;
       
    private double frgrw1;
    private double frgrw2;
    private double frLAImx; 
    
    private double solrad;
    private double raduse;
    private double leco;
    
    private double chtmx;
    private double rdmx;
    private double frroot_act;
    private double zrootd_act;
    
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
    private double bioopt_ha;
    private double hi_act;
    private double bioag_act;
    private double yldN;
    private double yldN_ha;
    private double yield;
    
    private double tmean;
    private double tbase;
    
   /* double dec; // days to reach specific plant stage
    double huec; // Potential heat units to reach specific plant stage
    int PS = 0;     //Plant status for optimal growth*/
    
        
    public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        
            }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        
        this.phu = PHUact.getValue(); /* actual fraction of daily PHU */
        this.phu_50 = PHU_50.getValue(); /*50% of total PHU*/ 
        this.fphu_act = FPHUact.getValue (); /*Actual fraction of potential HU */
        this.aTransP = aTP.getValue(); /*actual transpiration in mm*/
        this.pTransP = pTP.getValue(); /*potential transpiration in mm*/
        this.idc = LClass.getValue(); /*idc plant typ re crop.par*/
        this.mlai1 = MLAI1.getValue(); /* */
        this.mlai2 = MLAI2.getValue(); /* */
        this.mlai = MLAI.getValue() ; /* */
        this.int_lai = ILAI.getValue() ; /* */
        this.frgrw1 = FrcGrow1.getValue();
        this.frgrw2 = FrcGrow2.getValue();
        this.solrad = SolRad.getValue();    
        this.raduse = RadUse.getValue();
        this.leco = LExCoef.getValue();
        this.chtmx = MCanHeight.getValue();
        this.rdmx = MRootD.getValue();
        this.bn1 = Nuptake_1.getValue();
        this.bn2 = Nuptake_2.getValue();
        this.bn3 = Nuptake_3.getValue();
        this.betaN = BetaN.getValue();
        this.hvsti = HarvIndex.getValue();     /*harvest index */  
        this.cnyld = CNyld.getValue();
        this.bioN_act = BioNAct.getValue();
        this.lai_act = LAI.getValue();
        this.frLAImx_act = frLAImxAct.getValue();
        this.hc_act = CanHeightAct.getValue(); /*actual Canopy height [m] on a given day */
        this.frroot_act = frRootAct.getValue();
        this.zrootd_act = ZRootD.getValue();
        this.fnplant_act = FNPlant.getValue();
        this.bioNopt_act = BioNoptAct.getValue(); /*actual biomass in kg/ha, optimal conditions */
        this.Ndemand_act = PlantNDemAct.getValue();
        this.bioN_act = BioNAct.getValue(); /*actual biomass in kg/ha adapted by stress*/
        this.hi_act = HarvIndex.getValue();;
        this.bioag_act = BioagAct.getValue();
        this.area_ha = Area.getValue() /10000;
        this.tbase = Tbase.getValue();
        this.tmean = Tmean.getValue();
    //   this.dec = D_ECArray.getValue();
    //    this.huec = HU_ECArray.getValue();
//        idc = idc+1;
//        cropClass.setValue(idc);
        
//        cropClass.setValue(cropClass.getValue()+1);
    
               
        calc_lai();
        bio_act = calc_biomass();
        hc_act = calc_canopy();
        calc_root();
        calc_maturity();
        calc_nuptake();
        calc_cropyield ();
        calc_cropyield_ha ();
      
    
     frLAImxAct.setValue(frLAImx_act); /*actual fraction of max LAI for a given day */
     LAI.setValue(lai_act);
     BioOpt.setValue(bio_opt); /*Plants optimal biomass */
     BioOpt.setValue(bio_opt * area_ha); /*Plants optimal biomass */
     CanHeightAct.setValue(hc_act); /*Actual canopy height */
     frRootAct.setValue(frroot_act);  /* daily fraction of root development [mm] */
     ZRootD.setValue(zrootd_act);  /* daily root development [mm] */
     FNPlant.setValue(fnplant_act); /* daily fraction of N in plant biomass */
     BioNoptAct.setValue(bioNopt_act);
     BioAct.setValue(bio_act);
     PlantNDemAct.setValue(Ndemand_act);
     HarvIndex.setValue(hi_act);
     BioagAct.setValue(bioag_act);
     BioYield.setValue(yield);
     NYield.setValue(yldN); /* N Content from the above biomass */
     NYield_ha.setValue(yldN_ha);
          
     }
    
// Optimal growth
//   is modeled according to the following 9 EC-growth stages
//    (0)   Keimung 
//    (1)   Keimtriebentwicklung (Blattentwicklung)
//    (2)   Bestockung
//    (3)   Schossen (Hauptrieb)
//    (4)   Ã„hren- und Rispenschwellen
//    (5)   Ã„hren- und Rispenschieben
//    (6)   Blüte
//    (7)   Fruchtbildung
//    (8)   Samenreife
//    (9)   Absterben
   
   // setze Bedingungen für einzelne Pflanzenentwicklungszustände
    
  /*  double CHU = CHU + (this.tmean - this.tbase); //phänologisch wirksame Temperatursumme
    int julday = time.get(time.JULDAY);
    double CJD = CJD++; */
    
  /*  private boolean calc_ps() { 
    if (Keimung)
        if CHU = PHU[PS] || CJD >= PJD[PS];
        Keimung;
        CJD = 0;
        CHU = 0;
        PS = PS++;
        
    if ()    
        return true;
    }
   **/
    
    //
    // Biomass production
    //
    // First the daily development of the LAI is calculated as a fraction of maximimum LAI development (frLAImx)
    // Hereby the fraction of plants maximum leaf area index corresponding to a given fraction of PHU is calculated
    // and two shape-coefficient, sc1 and sc2 are needed
    // calculation the maximum leaf area corresponding to fraction of heat units,
    // expressed as LAI fraction of the known max LAI
 
   
    
    private boolean calc_lai() {
        
         /* Shape coefficients
            sc to determine LAI development */
        
         /* todo start time */
        
       sc2_LAI = ((Math.log(this.frgrw1/this.mlai1)-this.frgrw1)-(Math.log(this.frgrw2/this.mlai2)-this.frgrw2))/ (this.frgrw2 - this.frgrw1);
       sc1_LAI = Math.log((this.frgrw1/this.mlai1)-this.frgrw1)+ this.sc2_LAI * this.frgrw1;
       
       /* Fraction of plant's maximum LAI */
       
        double frLAImx =  this.fphu_act / (this.fphu_act + Math.exp(sc1_LAI - sc2_LAI * this.fphu_act));
        double frLAImx_xi = frLAImx ;
        frLAImx_act = frLAImx + frLAImx_act; //
        
        // Total leaf area index is calculated by frLAImx added on a day
         
         double LAI_init = this.int_lai;        
         double LAI_delta1 = (frLAImx_act - frLAImx_xi) * this.mlai *(1 - Math.exp(5.0 * (LAI_init - this.mlai)));
         lai_act = LAI_delta1+lai_act;
//         System.out.println (lai_act);
         
        // LAI will remain constant until leaf senescence begins to exceed leaf growth
        // this. phu_sense is the fphu when senescence becomes dominant
        // @todo declare what is fphu_sense; here assumed by phu 0.75;
         
        double fphu_sense = 0.75;
        
        if (this.fphu_act > fphu_sense) {
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
        double bio_opt = 0;
        double Hphosyn = 0.5 * this.solrad * (1 - Math.exp(this.leco*lai_act));
        
        double bio_opt_delta = this.raduse * Hphosyn;
        bio_opt = bio_opt_delta +  bio_opt; 
        
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
        
        double hc_delta = 0;
        double frLAImx_act = 0;
        hc_delta = this.chtmx * Math.sqrt(frLAImx_act);
        double hc_act = hc_delta + this.hc_act;
        
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
        
        double frroot_act = 0;        
        double frroot = 0.40 - 0.20 * this.fphu_act;
        frroot_act = frroot+frroot_act;
             
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
        
        if
                (this.idc == 3 || this.idc == 6 || this.idc == 7) 
        {
            double zrootd_act = this.rdmx;
        }
        
        // annuals
        // if case: as long pfhu is within 0.4; as fphu 0.4 is the time of max root depth
        double zrootd_act = 0;
        if
                (this.idc ==  1 || this.idc == 2 || this.idc == 4 || this.idc == 5 && this.fphu_act <= 0.40) {
            
            double zrootd = 2.5 * this.fphu_act * this.rdmx;
            zrootd_act = zrootd + zrootd_act;
        }
        if
                (this.fphu_act > 0.40) {
                zrootd_act = this.rdmx;
        }
        return true;
       }
    
    // Maturity
    
    // is reached when fphu_act = 1
    // as therefore no calculation is needed
    // @todo nutrients & water uptake & transpiration will stopp depending on the condition fphu = 1
     
      private boolean calc_maturity (){
          
      if
         (this.fphu_act >= 1.00) 
      {
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
        // n1 and n2 are shape coefficients by solving the equation of two known points
        // (frn2 by 50% of PHU and frn3 by 100% of PHU
        
        double fnplant_act = 0;
        double bioNopt_act = 0;
        double Ndemand_act = 0;
        
        // First calculation of shape coefficients n1 and n2 is needed
        double frn_sub1 = this.bn1 - this.bn3;
        double frn_sub2 = this.bn2 - this.bn3;
        double bn3_ca = this.bn3 - 0.00001;
        double frn_sub3 = bn3_ca - this.bn3;  // the module assumes that the denominator term does not equals 1
        // therefore the construction of denominator term bn3_ca is needed
        
        double sc4_Nbio = (Math.log(this.phu_50/(1-(frn_sub2)/(frn_sub1))- this.phu_50)- (Math.log(this.phu/(1-(frn_sub3)/frn_sub1))))/ this.phu - this.phu_50;
        double sc3_Nbio = Math.log(this.phu_50/(1-((frn_sub2)/(frn_sub1))- this.phu_50) + sc4_Nbio * this.phu_50);
        
        /* Fraction of N in plant biomass as a function of growth stage given optimal conditions */
        
        double fnplant = (this.bn1 - this.bn3) * (1 -(this.fphu_act / this.fphu_act + Math.exp(sc3_Nbio - sc4_Nbio * this.fphu_act))) + this.bn3;
        fnplant_act = fnplant + fnplant_act;
        
        // Determing the mass of N that should be stored in the plant biomass on a given day
        // whereas the fnplant is the optimal fraction of nitrogen in the plant biomass for the current growth stage
        // and bio_act is the total plant biomass on a given day [kg/ha]
        
        /* Mass N stored in the optimal plant biomass on a given day */
        
        double bioNopt = fnplant_act * bio_opt;
        bioNopt_act = bioNopt + bioNopt_act;
        
        // Plant nitrogen demand
        
        // by taking the difference between the nitrogen content
        // of the optimal plants biomass and the actual N content of the plants biomass
        
        double bioN_act;
        double Ndemand = bioNopt_act - this.bioN_act; //@todo: declare the actual N content according to the
        Ndemand_act = Ndemand + Ndemand_act;
        
        
    // @todo should we take depth distribution into account? probably not as this point
    // N uptake within the soil profile
        
        zrootd_act = 0;
        double Nup_layer = 0;
                
            if (this.betaN == 1) {
        
        Nup_layer = zrootd_act;
                
            }else if (betaN > 1 ){
                
        Nup_layer = this.betaN / zrootd_act * 100;
                
            }else if (betaN == 0 ){
                
        Nup_layer = 0.1;
                  
           double Nup_depth = Ndemand_act / (1 - Math.exp(-betaN)) * (1-Math.exp(-betaN * this.rdmx / zrootd_act));         
            }
           return true;
            
    }
    // Nitrogen fixation
    // used when nitrate levels in the root zone are insufficient to meet the demand
    
    // Phosphorus uptake
    
    // Crop Yield
    private boolean calc_cropyield() {
        double yield;
        hi_act = 0;
        bioag_act = 0;
        frroot_act = 0 ;
        
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
        
        double hi = this.hvsti * (100 * this.fphu_act)/(100 * this.fphu_act + Math.exp(11.1 - 10.0 * this.fphu_act));
        hi_act = hi + hi_act;
        
        // crop yield (kg/ha)is calculated as
        // above ground biomass
        
        double bio_ag = (1- frroot_act) * bio_opt; // actual aboveground biomass on the day of harvest  
        bioag_act = bio_ag + bioag_act;
        
         if (this.fphu_act >= 1.00) {
             double bioag_harvest = bioag_act;
         }
        
        // total yield biomass on the day of harvest
        // @todo harvest options
                      
        if (hi_act <= 1.00 || this.fphu_act >= 1.00) {
            yield = bioag_act * hi_act;
        }						
        
        else if (hi_act > 1.00 || this.fphu_act >= 1.00)											// bio is the total biomass on the day of the harvest (kg/ha)
        {
            yield = bio_opt * (1 - (1/(1+ hi_act)));
        }
        
        // Amounts of nitrogen [kg N/ha](and who wants P) to be removed from the field
        // whereas cnyld is the fraction of N being removed by the field crop
        
        yield = 0;
        double yldN = this.cnyld * yield;
        
        //double yldP = this.cpyld * yield;
        return true;
    }
     private double calc_cropyield_ha() {
 
         yldN_ha = yldN * area_ha / 10000; 
         
         return yldN_ha;
     }
     
    public void cleanup()throws Attribute.Entity.NoSuchAttributeException{
       // store.close();
    }
    
}

