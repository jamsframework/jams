/*
 * j2kSNStress.java
 *
 * Created on 14. Februar 2006, 16:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package crop;

/**
 *
 * @author c5ulbe
 */
public class j2kSNStress {
    
    /** Creates a new instance of j2kSNStress */
    public j2kSNStress() {
    }
 
       
    // Next please is determine the actual growth
    // as plant can be stressed by water, temperature and nutrients stess
    
    // Water stress is simulated by comparing actual and potential plant transpiration
    
    /*double watstrs = 1 - (this.actualEt/this.ET);*/
    
    // this actual.ET is the actual plant transpiration on a given day and ET is the maximal ET on that given day
    // is the same as total plant water uptake
    
    // Temperature stress is a function of the daily average air temperature and the optimal temperature for plant growth
    // is depending on different stages of the air temperature
    
        /*  if this.tmean < equalls this.tbase
          double tempstrs = 1;
          else if
          this.tbase < this.tmean < this.tppt
          this tempstrs = 1 - Math.exp ((-0.1054 * (this.topt - this.tmean)Â²/(this.tmean - this.tbase))
          else if
          this.topt < this.tmean < (2 * this.tOpt) - this.tBase
          this tempstrs = 1 - Math.exp ((-0.1054 * (this.topt - this.tmean)Â²/(2 * this.topt - this.tmean - this.tbase))
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
}
