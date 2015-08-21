    /*
 * j2kCropGrowth.java
 *
 * Created on 15. November 2005, 11:47
 */

package org.unijena.crop;

/**
 *
 * @author  c5ulbe
 */
import org.unijena.jams.model.*;
import org.unijena.jams.data.*;
import org.unijena.jams.io.*;
import java.io.*;
import java.util.*;


    @JAMSComponentDescription(
        title="j2kCropGrowth",
        author="Ulrike Bende-Michl",
        description="Module for calculation of the crop growth according to the algorithms of SWAT"
        )
       
 public class j2kCropGrowth extends JAMSComponent {
        
     @JAMSVarDescription(
             access = JAMSVarDescription.AccessType.READ,
             )
             public Attribute.String fileName;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of hru objects"
            )
            public Attribute.EntityCollection hrus;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity"
            )
            public Attribute.Entity entity;
    
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute name area"
            )
            public Attribute.String aNameArea;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop id"
            )
            public Attribute.String aNameCropID;
    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU crop class"
            )
            public Attribute.String aNameCropClass; 
      
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU land use typ (same as J2K land use id)"
            )
            public Attribute.String aNamePTyp;             
     
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU daily potential heat units"
            )
            public Attribute.String aNamePHU_daily;
   
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU daily zero based heat units"
            )
            public Attribute.String aNameHU_daily;
          
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU fraction daily potential heat units"
            )
            public Attribute.String aNameFPHU_daily;      
   
   
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU fraction daily zero based potential heat units"
            )
            public Attribute.String aNameFHU0_daily;
 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU half of yearly potential heat units"
            )
            public Attribute.String aNamePHU_50;
    
/*     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "First shape coefficient to determine LAI development"
            )
            public Attribute.String aNameSC1_LAI;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Second shape coefficient to determine LAI development"
            )
            public Attribute.String aNameSC2_LAI;  */
        
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max LAI corresponding to the first point of the optimal LAI development"
            )
            public Attribute.String aNameMLAI_1;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Max LAI corresponding to the second point of the optimal LAI development"
             
            )
            public Attribute.String aNameMLAI_2;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximimum LAI"
            )
            public Attribute.String aNameMLAI;
    
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of growing season corresponding to the first point of the optimal LAI development"
            )
            public Attribute.String aNameFrcGrow1;
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of growing season corresponding to the second point of the optimal LAI development"
            )
            public Attribute.String aNameFrcGrow2;
      
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily solar radiation [MJ/m²]"
            )
            public Attribute.String aNameSolRad;
    
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Crop specific radiation use efficiency ([kg/ha] drymass per[MJm²])"
            )
            public Attribute.String aNameRadUse;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Intercepted photosynthetically active radiation [MJ/m²]"
            )
            public Attribute.String aNameHphosyn;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Biomass produced for a given day [kg/ha] drymass"
            )
            public Attribute.String aNameBiomass;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Canopy Height [m]"
            )
            public Attribute.String aNameCanHeight;
     
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum Canopy Height [m]"
            )
            public Attribute.String aNameMCanHeight;   
       
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily LAI [-]"
            )
            public Attribute.String aNameLAI_daily;
            
            
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Maximum rooting depth [mm]"
            )
            public Attribute.String aNameMRootD;
    
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Total rooting depth [mm]"
            )
            public Attribute.String aNameTrootD;
    
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal fraction of N in the plant biomass at the emergence"
            )
            public Attribute.String aNameNuptake_1;
         
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal fraction of N in the plant biomass at 50% of maturity"
            )
            public Attribute.String aNameNuptake_2;
          
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Normal fraction of N in the plant biomass at maturity"
            )
            public Attribute.String aNameNuptake_3;
      
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "First shape coefficient for nitrogen uptake in biomass"
            )
            public Attribute.String aNameSC3_NBio;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "second shape coefficient for nitrogen uptake in biomass"
            )
            public Attribute.String aNameSC4_NBio;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optimal fraction of nitrogen in the plant biomass at the current growth's stage"
            )
            public Attribute.String aNameFNPlant;
            
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optimal plant nitrogen content in the plants biomass for a given day"
            )
            public Attribute.String aNameBioNopt;
            
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Plants nitrogen demand for a given day"
            )
            public Attribute.String aNameBioNdem;       
          
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual nitrogen stored in the plants biomass for a given day"
            )
            public Attribute.String aNameBioNact;
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Harvest Index [0-1]"
            )
            public Attribute.String aNameHarvIndex;
        
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction of harvest"
            )
            public Attribute.String aNameFracHarv;
        
        
        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Lower boundary of harvest index [0-1]"
            )
            public Attribute.String aNameLHarIndex;
        
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Potential Crop Yield [kg/ha]"
            )
            public Attribute.String aNameYieldPot;
         
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual Crop Yield [kg/ha]"
            )
            public Attribute.String aNameYieldAct;
         
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Biomass above ground on the day of harvest [kg/ha]"
            )
            public Attribute.String aNameBioAg;
         
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction N in Yield"
            )
            public Attribute.String aNameFrcNYld;
         
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fraction P in Yield"
            )
            public Attribute.String aNameFrcPyld;
         
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual N mass stored in plant material [kg N/ha]"
            )
            public Attribute.String aNameBioAct;
         
  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Scaling factor for N stress"
            )
            public Attribute.String aNameScalN;
  
         
  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual LAI for a given day"
            )
            public Attribute.String aNameLAIact;
  
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Optimal temperature for plant growth [°C]"
            )
            public Attribute.String aNameTOpt; 
     
       @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Residuen pool (biomass)"
            )
            public Attribute.String aNameResidue_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Water stress for a given day [mm H2O]"
            )
            public Attribute.String aNameWstrs;
            
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU actual Transpiration in mm"
            )
            public Attribute.String aNameaTP;
 
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU potential Transpiration in mm"
            )
            public Attribute.String aNamepTP;
     
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Light Extinct Coefficient [-0.65]"
            )
            public Attribute.Double LExCoef;
    
   /*  @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Initial Leaf Area Index [-]"
            )
            public Attribute.String aNameLai_init;*/ //@todo muss noch im ParaReader für jedes Crop initialisiert werden (ALAI)
     
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual daily biomass [kg/ha]"
            )
            public Attribute.String aNamebio_daily; 
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual daily LAI [-]"
            )
            public Attribute.String aNamelai_daily; 
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual daily Canopy Height [m]"
            )
            public Attribute.String aNamehc_daily;       
          
      
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual daily fraction of root biomass [-]"
            )
            public Attribute.String aNamefrroot_daily;    
      
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual daily root development [mm]"
            )
            public Attribute.String aNameZRroot_daily;  
         
         @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual fraction N in plant [-]"
            )
            public Attribute.String aNamefNPlant_daily;   
         
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Actual N content in plants biomass [kg N/ha]"
            )
            public Attribute.String aNamebioNopt_daily;   
          
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily plants N demand [kg N/ha]"
            )
            public Attribute.String aNamebioNUp_daily;   
        
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily fraction of max LAI [-]"
            )
            public Attribute.String aNamefrLAImx_daily;   
     
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily fraction of max root development [-]"
            )
            public Attribute.String aNamefrRoot_daily;   
              
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Daily rooting depth [-]"
            )
            public Attribute.String aNameZRoot_daily;   
          
       
          @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N content in yield [kg N/ha]"
            )
            public Attribute.String aNameyldN;   
        
                           
 

    
      public void init() throws Attribute.Entity.NoSuchAttributeException, IOException {
        Attribute.EntityEnumerator eEnum = hrus.getEntityEnumerator(); /*Entity Array*/
        Attribute.Entity[] entities = hrus.getEntityArray();
          
     
     for (int i = 0; i < entities.length; i++) {
         entities[i].setDouble(aNamebio_daily.getValue(), 0);
         entities[i].setDouble(aNamelai_daily.getValue(), 0); /*Initialisierung des LAIs*/
         entities[i].setDouble(aNamefrLAImx_daily.getValue(), 0); /*Initialisierung der LAIFractions*/
         entities[i].setDouble(aNamehc_daily.getValue(), 0); /*Initialisierung der canopy height*/
         entities[i].setDouble(aNamefrRoot_daily.getValue(), 0); /*Initialisierung der root fraction*/
         entities[i].setDouble(aNameZRoot_daily.getValue(), 0); /*Initialsierung der rooting depth*/
         entities[i].setDouble(aNamefNPlant_daily.getValue(), 0); /*Initialsierung der fraction N in Biomass*/
         entities[i].setDouble(aNamebioNopt_daily.getValue(), 0); /*Initialsierung von N stored in Biomass*/
         entities[i].setDouble(aNamebioNUp_daily.getValue(), 0); /*Initialsierung von N stored in Biomass*/
         }          
     }
     
         public void run() throws Attribute.Entity.NoSuchAttributeException{
     
         double fphu = entity.getDouble(aNameFPHU_daily.getValue()); /* fraction of daily PHU */
         double aTP = entity.getDouble(aNameaTP.getValue()); /*actual transpiration in mm*/     
         double pTP = entity.getDouble(aNameaTP.getValue()); /*potential transpiration in mm*/  
         double id = entity.getDouble(aNameCropID.getValue()); /*id re crop.par*/  
         double idc = entity.getDouble(aNameCropClass.getValue()); /*idc plant typ re crop.par*/  
         double ptyp = entity.getDouble(aNamePTyp.getValue()); /*land use typ re land use par*/  
         double mlai = entity.getDouble(aNameMLAI.getValue()); /* maximum plant specific LAI */
         double mlai1 = entity.getDouble(aNameMLAI_1.getValue()); /*max lai first point */
         double mlai2 = entity.getDouble(aNameMLAI_2.getValue()); /*max lai second point*/
         double frgrw1 = entity.getDouble(aNameFrcGrow1.getValue()); /*fraction growing season first point */
         double frgrw2 = entity.getDouble(aNameFrcGrow2.getValue()); /*fraction growing season second point */
         double solrad = entity.getDouble(aNameSolRad.getValue()); /*incoming solar radition */
         double raduse = entity.getDouble(aNameRadUse.getValue()); /*radiation use efficiency*/
         double mcanhei = entity.getDouble(aNameMCanHeight.getValue()); /*maximum canopy height */
         double mrootd = entity.getDouble(aNameMRootD.getValue()); /*maximum rooting depth */
         double bn1 = entity.getDouble(aNameNuptake_1.getValue()); /*N uptake parameter emergence */
         double bn2 = entity.getDouble(aNameNuptake_2.getValue()); /*N uptake parameter 50% maturity */
         double bn3 = entity.getDouble(aNameNuptake_3.getValue()); /*N uptake parameter maturity */
         double hvsti = entity.getDouble(aNameHarvIndex.getValue()); /*harvest index */
         double cnyld = entity.getDouble(aNameFrcNYld.getValue()); /*fraction N in yield*/
         double topt = entity.getDouble(aNameTOpt.getValue()); /*Plants optimal growth temperature */
 /*        double tbase = entity.getDouble(aNameTBase.getValue()); /*Plants base temperature */
         double bio_daily = entity.getDouble(aNamebio_daily.getValue()); /*actual biomass in kg/ha -> können auch berechnete Werte sein, die wieder zuückgeladen werden*/
         double lai_daily = entity.getDouble(aNamelai_daily.getValue()); /*actual LAI*/
         double frLAImx_daily = entity.getDouble(aNamefrLAImx_daily.getValue()); /*actual fraction of max LAI for a given day */
         double hc_daily = entity.getDouble(aNamelai_daily.getValue()); /*actual Canopy height [m] on a given day */
         double frroot_daily = entity.setDouble(aNamefrRoot_daily.getValue(), frroot_daily);  /*fraction of root biomass */
         double zroot_daily = entity.setDouble(aNameZRoot_daily.getValue(), zroot_daily); /* daily root development [mm] */
         double fnplant_daily = entity.setDouble(aNamefNPlant_daily.getValue(), fnplant_daily); /* daily fraction of N in plant biomass */
         double bioNopt_daily = entity.setDouble(aNamebioNopt_daily.getValue(), bioNopt_daily); /*  daily N content on plants biomass */
         double bioNUp_daily = entity.setDouble(aNamebioNUp_daily.getValue(), bioNUp_daily); /*  daily N content by plant uptake */
         double yldN = entity.setDouble(aNameyldN.getValue(), yldN); /* N Content from the above biomass */
   
         calc_lai(); /*Rückgabewerte der Calc-Routinen*/
         bio_daily = calc_biomass();
         hc_daily = calc_canopy();
         calc_root ();
         calc_nuptake ();
        
         /* HRU bezogene Rückgabe von Werten 'nach aussen'*/
          entity.setDouble(aNamefrLAImx_daily.getValue(), frLAImx_daily);
          entity.setDouble(aNamelai_daily.getValue(), lai_daily);
          entity.setDouble(aNamebio_daily.getValue(), bio_daily); /*tägliche Wert von calc_biomass kann wieder eingelesen werden*/
          entity.setDouble(aNamehc_daily.getValue(), hc_daily);  
          entity.setDouble(aNamefrRoot_daily.getValue(), frroot_daily);   
          entity.setDouble(aNamefNPlant_daily.getValue(), fnplant_daily); 
          entity.setDouble(aNamebioNopt_daily.getValue(), bioNopt_daily);
          entity.setDouble(aNamebioNUp_daily.getValue(), bioNUp_daily);
               }              

//Optimal growth
         // Biomass production
         // First the daily development of the LAI is calculated as a fraction of maximimum LAI development (frLAImx)
         // Hereby the farction of plants maximum leaf area index corresponding to a given fraction of PHU is calculated
         // and two shape-coefficient, sc1 and sc2 are needed
         // calculation the maximum leaf area corresponding to fraction of heat units,
         // expressed as LAI fraction of the known max LAI
        
        private void calc_lai() throws Attribute.Entity.NoSuchAttributeException {
               
         /* Shape coefficients 
            sc to determine LAI development */
                   
        double sc2 = ((Math.ln(this.frgwr1/this.mlai1)-this.frgwr1)-(Math.ln(this.frgwr2/this.mlai2)-this.frgwr2))/ (this.frgwr2 - this.frgwr1);
        double sc1 = Math.ln((this.frgwr1/this.mlai1)-this.frgwr1)+ this.sc2 * this.frgrwr1;
                            
        double frLAImx_delta = this.fphu / (this.fphu + Math.exp(sc1 - sc2 * this.fphu));
        frLAImx_daily = this.frLAImx_delta + this.frLAImx_daily; // 
         
        // the total leaf area index is calculated by LAI added on a day
                                                    
        double lai_delta = (this.frLAImx_delta - this.frLAImx_daily) * mlai *(1 - Math.exp(5.0 * (this.lai_daily - mlai)));
        lai_daily = this.lai_daily + this.lai_delta;    
        
         // LAI will remain constant until leaf senescence begins to exceed leaf growth
                                                    // this. phu_sense is the fphu when senescence becomes dominant
         // @todo declare what is fphu_sense;
         /* double fphu_sense = */
         
         if (this.fphu > this.fphu_sense)
         {
         lai_daily = 16 * this.mlai * Math.pow(1 - this.fphu);
         }
        return;      
        } 
         // Second the amount of daily solar radiation intercepted by the leaf area of the plant is calculated
         // this.solrad = incoming total solar
         // Hphosyn = the amount of intercepted photosynthetically active radiation on a day [MJ m-2]
         // Third the amount of biomass (dry weight in kg/ha) produced per unit intercepted solar radiation is calulated using the plant-specific
         // radiation-use efficiency declared in the crop growth database by parameter 'rue' in crop.par
         // whereas the total biomass on a given day is summed up 
                      
        private double calc_biomass() throws Attribute.Entity.NoSuchAttributeException {
                   
         double Hphosyn = 0.5 * this.solrad * (1 - Math.exp(this.LExCoef * this.lai_daily));       
         double bio_delta = this.raduse * this.Hphosyn;
         double bio_daily1 = this.bio_delta +  this.bio_daily; // 
                  
         return bio_daily1; /*Möglichkeit zur Rückgabe von nur 1 Wert ausserhalb der Routine*/
        }
         // Canopy height and cover
         // Canopy cover is expressed as leaf area index
         // hc_daily = canopy height (m) for a given day
         // mlai = Maximum LAI Parameter from crop.par
         // chtmx = maximum canopy height (m), Parameter from crop.par
         // frLAImx = fraction of plants maximum canopy height
        
         private double calc_canopy() throws Attribute.Entity.NoSuchAttributeException {
         
         double hc_delta = this.chtmx * Math.sqrt(this.frLAImx_daily);
         double hc_daily = this.hc_delta + this.hc_daily;
         
         return hc_daily;
         }
         // Root development
         // Amount of total plants biomass partioned to the root system
         // in general it varies in between 30-50% in seedlings and decreases to 5-20% in mature plants
         // fraction of biomass in roots by SWAT varies between 0.40 at emergence and 0.20 at maturity
         // daily fraction of root biomass is calculated by
         
         // Fraction of root biomass
         private void calc_root() throws Attribute.Entity.NoSuchAttributeException {
        
         double frroot_delta = 0.40 - 0.20 * this.fphu;
         frroot_daily = this.frroot_delta + this.frroot_daily;
         
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
         simulate nitrogen fixation
         root depth varies during growing season due to root growth
        2 cold season annual legume
         simulate nitrogen fixation
         root depth varies during growing season due to root growth
         fall-planted land covers will go dormant when daylength is less than the threshold daylength
        3 perennial legume
         simulate nitrogen fixation
         root depth always equal to the maximum allowed for the plant species and soil
         plant goes dormant when daylength is less than the threshold daylength
        4 warm season annual
         root depth varies during growing season due to root growth
        5 cold season annual
         root depth varies during growing season due to root growth
         fall-planted land covers will go dormant when daylength is less than the threshold daylength
        6 perennial
         root depth always equal to the maximum allowed for the plant species and soil
         plant goes dormant when daylength is less than the threshold daylength
        7 trees
         root depth always equal to the maximum allowed for the plant species and soil
         partitions new growth between leaves/needles (30%) and woody growth (70%). At the end of each growing season, biomass in the leaf fraction is converted to residue*/
         
         // Root development (mm in the soil) for plant types on a given day
         // Varying linearly from 0.0 at the beginning of the growing season to the maximum rooting depth at fphu = 0.4
         // Perennials and trees, as therefore rooting depth is not varying
         if 
            (this.idc == 3 || this.idc == 6 || this.idc == 7)         
         {
             zroot_daily = this.rdmx;
         }
         // annuals
         // if case as long pfhu is within 0.4 as 0.4 is the time of max, root depth
         
         if              
             (this.idc ==  1 || this.idc == 2 || this.idc == 4 || this.idc == 5 && this.fphu <= 0.40)      
         {
            double zroot_delta = 2.5 * this.fphu * this.rdmx;
            zroot_daily = zroot_delta + zroot_daily;
         }
         if 
             (this.fphu > 0.40)
         {
             zroot_daily = this.rdmx;
         } 
         return;
         }
         // Maturity
         // is reached when fphu = 1
         // as therefore no calculation is needed
         // @todo nutrients & water uptake & transpiration will stopp depending on the condition fphu = 1
         
         // if 
         //  (this.fphu >= 1.00)
         // {
         //
         // Water uptake by plants
         // Potential water uptake
                        
                  
         // Nutrient uptake by plants
         
          private void calc_nuptake() throws Attribute.Entity.NoSuchAttributeException {        
         // is calculated by the fraction of the plant biomass as a function of growth stage given the optimal conditions
                            // fnplant =fraction N in plant biomass
                            // with bn1 as fraction of N in the plant biomass at the emergence
                            // with bn2 as fraction of N in the plant biomass at the 2. point of the known point
                            // with bn3 as fraction of N in the plant biomass at the maturity
                            // with bn3_ca as fraction of N in the plant biomass near maturity
                            // n1 and n2 are shape coefficients by solving the equation of two known points
                            // (frn2 by 50% of PHU and frn3 by 100% of PHU
         
        // First calculation of shape coefficients n1 and n2 is needed
          
         frn_sub1 = this.bn1 - this.bn3;
         frn_sub2 = this.bn2 - this.bn3;
         bn3_ca = this.bn3 - 0.00001;
         frn_sub3 = bn3_ca - this.bn3;  // the module assumes that the denominator term does not equals 1
                                        // therefore the construction of denominator term bn3_ca is needed
         
         double nc2 = (Math.ln(this.phu_50/(1-(frn_sub2)/(frn_sub1))- this.phu_50)- (Math.ln(this.phu/(1-(this.frn_sub3)/this.frn_sub1))))/ this.phu - this.phu_50;
         
         double nc1 = Math.ln(this.phu_50/(1-((frn_sub2)/(frn_sub1))- this.phu_50) + nc2 * this.phu_50);
         
         /* Fraction of N in plant biomass as a function of growth stage given optimal conditions */
         
         double fnplant_delta = (this.bn1 - this.bn3) * (1 -(this.fphu / this.fphu + Math.exp(this.nc1 - this.nc2 * this.pfhu))) + this.bn3;
         fnplant_daily = fnplant_delta + fnplant_daily;
         
         // Determing the mass of N that should be stored in the plant biomass on a given day
         // whereas the fnplant is the optimal fraction of nitrogen in the plant biomass for the current growth stage
         // and bio_daily is the total plant biomass on a given day [kg/ha]
         
         /* Mass N stored in the optimal plant biomass on a given day */
        
         double bioNopt = fnplant_daily * this.bio_daily;
         bioNopt_daily = bioNopt + bioNopt_daily;
         
         // Plant nitrogen demand
         // by taking the difference between the nitrogen content 
         // of the optimal plants biomass and the actual N content of the plants biomass
                  
         double nUp_delta = bioNopt_daily - bioN_daily; //@todo: declare the actual N content according to the 
         bioNUp_daily = Nup_delta + bioNUp_daily;
         
         return;
         }
         // @todo should we take depth distribution into account? probably no
         // N uptake within the soil profile
                 
          
         // Nitrogen fixation
         // used when nitrate levels in the root zone is insufficient to meet the demand
         
        // Phosphorus uptake
         
         // Crop Yield
           private void calc_cropyield() throws Attribute.Entity.NoSuchAttributeException {     
        
          //for harvesting 4 codes are implemented:
				// (1) assumes harvesting with Haupt- & Nebenfrucht, plant growth stopped
				// (2) assumes harvesting with Hauptfrucht, Nebenfrucht remains on the field, plant growth stopped (former kill operation)
				// (3) assumes harvesting with Haupt- & Nebenfrucht, plant growth continues (may not be suitable for meadows)
				// (4) assumes harvesting with Hauptfrucht, plant growth continues//
                // when harvest&kill as determined in the crop management by code (1)
                // above-ground plant dry biomass removed as dry economic yield is called harvest index      		
                // for majority of crops the harvest index is between 0 and 1, 
                // however for plant whose roots are harvested, such as potatos may have an harvest index greater than 1
                // harvest index is calculated for each day of the plant's growing season using the relationship
                        // as hi is the potential harvest index for a given day and
                        // hvsti is the potential harvest index for the plant at maturity given ideal growing conditions
           
           double hi_delta = this.hvsti * (100 * this.fphu)/(100 * this.fphu + Math.exp(11.1 - 10.0 * this.fphu));
           hi_daily = hi_delta + hi_daily;
           
           // crop yield (kg/ha)is calculated as
           // above ground biomass 
           
           double bio_ag_delta = (1- this.frroot_daily) * this.bio_daily; // actual aboveground biomass 
           bioag_daily = bio_ag_delta + bioag_daily;	
           
           // total yield biomass on the day of harvest
           // @todo harvest 
           if (hi_daily <= 1.00)
           { 
               double yield = bioag_daily * hi_daily;
           }									// froot is the fraction of the total biomass in the roots the day of harvest
           if (hi_daily > 1.00)											// bio is the total biomass on the day of the harvest (kg/ha)
           {
               double yield = bio_daily * (1 - 1/(1+ hi_daily));
           }    
                          
           // Amounts of nitrogen [kg N/ha](and who wants to P) be removed from the field
           // whereas cnyld is the fraction of N being removed by the field crop
           
           double yldN = this.cnyld * yield;
           
           //double yldP = this.cpyld * yield;
          return;
           }
           
           
          // Next please is determine the actual growth
          // as plant can be stressed by water, temperature and nutrients stess
           
          // Water stress is simulated by comparing actual and potential plant transpiration 		

          double watstrs = 1 - (this.actualEt/this.ET);
          
          // this actual.ET is the actual plant transpiration on a given day and ET is the maximal ET on that given day
          // is the same as total plant water uptake 
                    
          // Temperature stress is a function of the daily average air temperature and the optimal temperature for plant growth
          // is depending on different stages of the air temperature
          
        /*  if this.tmean < equalls this.tbase
          double tempstrs = 1;
          else if 
          this.tbase < this.tmean < this.tppt
          this tempstrs = 1 - Math.exp ((-0.1054 * (this.topt - this.tmean)²/(this.tmean - this.tbase))
          else if
          this.topt < this.tmean < (2 * this.tOpt) - this.tBase
          this tempstrs = 1 - Math.exp ((-0.1054 * (this.topt - this.tmean)²/(2 * this.topt - this.tmean - this.tbase))
          else if
          this.tmean > (2 * this.topt) - this.tbase
          this tempstrs = 1;
          
          
          // Nitrogen stress
          // is quantified by comparing actual and optimal plant nitrogen levels. 
          // N stress varies non-lineary between 0 at optimal content and 
          // 1 when N content of the plant is 50% or less of the optimal value
          // Scaling factor scal_n
          
          double scal_n = 200 * ((this.bio_n/this.bio_Nopt)- 0.5);
          
          double nstrs = 1 - (this.scal_n/this.scal_n + Math.exp(3.535 - 0.02597 * this.scal_n));
          
          // Actual growth
          // plant factor quantifies the fraction of potential growth achieved on a given day 
          // plgrwfac = plant growth factor (0 - 1), watstrs, tempstrs, nstrs (maybe needed in the future pstres)
          
          double plgrfac = 1 - Math.max (this.watstrs, this.tempstrs, this.nstrs);
          
          // potential biomass predicted is a adjusted daily if one of the three stress factors is greater than 1
          
          double bio_act = this.bio_daily * this.plgrfac;
          
          // Potential leaf area added on a given day, adjusted for plant stess
          
          double LAI_act = this.deltaLAI * Math.sqrt(this.pfgrfac);
          
          // biomass override, @todo
          // Modul assumes that the user will specify a total biomass what will be produce each year
          // When biomass override is specified in the operation mode the impact of variation in growing conditions
          // will be ignored
                    
          if this.Bio_over = 1;
          this bio_actch = this.bio_act * ((this.bio_tar - this.bio_act)/this.bio.tar);
          
          // Actual yield
          // predicted harvest index is affected by water deficit
          // with parameter harvest_min (WSYF from crop.par)
          
          double wat_deffac = 100 * (Math.sum this.actET(m-i/Math.sum this.potET (m-i))
          // whereas i is the day in the plant growing season, 
          // m is the day of harvest if the plant is harvested before it reaches maturity or the last day of the
          // growing season if the plant is harvested after it reaches maturity 
          
          double harvindex_actual = (this.HI - this.WSYF) * (this.watstrs/(this.wat_deffac + Math.exp(6.13 - 0.883 * this.wat_deffac))+ this.WSYF;
          
          // harvest index override by determine harvest operations
          // is used if harvest only operation 
          // it is allowed to specify a target harvest index
          // the target index is ususally set when the yield is removed using the harvest/kill operation but could also be
          // set by the harvest operation
          
          if this.HIact is > 0
          this hitar = this.HIact;
              
         // harvest efficiency, defines the fraction of yield biomass removed by harvest efficiency 
         // if harvest efficiency is not set or 0 the modul assumes that the user want to ignore harvest efficiency and
         // sets the fraction to 1.0, so that the entire yield is totally removed 
         
          double yld_act = this.yld - this.FracHarv;
          
          // the remainder is converted to residue as parameter residu_act
          
          double residu_act = this.yld * (1 - this.FracHarv);
          
          // and biomass is added to the residue pool on a given day;
          
          double resi_pool = this.resi_act + resi_surf; // whereas resi_surf is the actual material in the residue pool 
          
     }
*/
     public void cleanup(){
         store.close();
     }

}

         