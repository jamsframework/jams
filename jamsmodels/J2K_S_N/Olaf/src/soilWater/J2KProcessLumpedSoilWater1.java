package soilWater;

import staging.j2k.Main;
import staging.j2k.types.HRU;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Calculates soil water balance for each HRU without vertical layers")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Soilwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilWater/J2KProcessLumpedSoilWater1.java $")
@VersionInfo
    ("$Id: J2KProcessLumpedSoilWater1.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class J2KProcessLumpedSoilWater1  {

//    public J2KProcessLumpedSoilWater1(Main model) {
//        soilMaxDPS = model.soilMaxDPS;
//        soilPolRed = model.soilPolRed;
//        soilLinRed = model.soilLinRed;
//        soilMaxInfSummer = model.soilMaxInfSummer;
//        soilMaxInfWinter = model.soilMaxInfWinter;
//        soilMaxInfSnow = model.soilMaxInfSnow;
//        soilImpGT80 = model.soilImpGT80 ;
//        soilImpLT80 = model.soilImpLT80 ;
//        soilDistMPSLPS = model.soilDistMPSLPS ;
//        soilDiffMPSLPS = model.soilDiffMPSLPS ;
//        soilOutLPS = model.soilOutLPS ;
//        soilLatVertLPS = model.soilLatVertLPS ;
//        soilMaxPerc = model.soilMaxPerc ;
//        soilConcRD1 = model.soilConcRD1 ;
//        soilConcRD2 = model.soilConcRD2 ;
//        FCAdaptation = model.FCAdaptation ;
//        ACAdaptation = model.ACAdaptation ;
//        satStartLPS = model.satStartLPS ;
//        satStartMPS = model.satStartMPS ;
//    }


    
    // @Description("area")
    double area;
    
    // @Description("slope")
    double slope;

    // @Description("sealed grade")
    double sealedGrade;
    
    // @Description("state variable net rain")
    double netRain;

    // @Description("state variable net snow")
    double netSnow;

    // @Description("state variable potET")
    double potET;
    
    // @Description("state variable actET")
    double actET;
    
    // @Description("snow depth")
    double snowDepth;
    
    // @Description("daily snow melt")
    double snowMelt;
    
    // @Description("HRU attribute maximum MPS")
    double maxMPS;
    
    // @Description("HRU attribute maximum MPS")
    double maxLPS;
    
    // @Description("HRU state var actual MPS")
    double actMPS;

    // @Description("HRU state var actual LPS")
    double actLPS;
    
    // @Description("HRU state var actual depression storage")
    double actDPS;
    
    // @Description("HRU state var saturation of MPS")
    double satMPS;

    // @Description("HRU state var saturation of LPS")
    double satLPS;
    
    // @Description("HRU state var saturation of whole soil")
    double satSoil;
    
    
    // @Description("HRU statevar interflow")
    double interflow;
    
    // @Description("HRU statevar percolation")
    double percolation;
    
    // @Description("HRU statevar RD1 inflow")
    double inRD1;
    
    // @Description("HRU statevar RD2 inflow")
    double inRD2;
    
    // @Description("HRU statevar RD1 outflow")
    double outRD1;
    
    // @Description("HRU statevar RD2 outflow")
    double outRD2;

    double  overlandflow;
    double infiltration;
    boolean init = true;
//
    @Description("maximum depression storage")
    @Unit("mm")
    @Role(PARAMETER)
    @In
    public double soilMaxDPS;
    
    @Description("poly reduction of ETP")
    @Role(PARAMETER)
    @In
    public double soilPolRed;

    @Description("linear reduction of ETP")
    @Role(PARAMETER)
    @In
    public double soilLinRed;
    
    @Description("maximum infiltration rate in summer")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In
    public double soilMaxInfSummer;
    
    @Description("maximum infiltration rate in winter")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In
    public double soilMaxInfWinter;

    @Description("maximum infiltration rate on snow")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In
    public double soilMaxInfSnow;

    @Description("maximum infiltration part on sealed areas (gt 80%)")
    @Role(PARAMETER)
    @In
    public double soilImpGT80;
    
    @Description("maximum infiltration part on sealed areas (lt 80%)")
    @Role(PARAMETER)
    @In
    public double soilImpLT80;
    
    @Description("MPS/LPS distribution coefficient for inflow")
    @Role(PARAMETER)
    @In
    public double soilDistMPSLPS;
    
    @Description("MPS/LPS diffusion coefficient")
    @Role(PARAMETER)
    @In
    public double soilDiffMPSLPS;
    
    @Description("LPS outflow coefficient")
    @Role(PARAMETER)
    @In
    public double soilOutLPS;
    
    @Description("LPS lateral-vertical distribution coefficient")
    @Role(PARAMETER)
    @In
    public double soilLatVertLPS;
    
    @Description("maximum percolation rate")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In
    public double soilMaxPerc;
    
    @Description("concentration coefficient for RD1")
    @Role(PARAMETER)
    @In
    public double soilConcRD1;
    
    @Description("concentration coefficient for RD2")
    @Role(PARAMETER)
    @In
    public double soilConcRD2;

    // from InitJ2J ....
    @Description("field capacity adaptation factor")
    @Role(PARAMETER)
    @In
    public double FCAdaptation;

    @Description("air capacity adaptation factor")
    @Role(PARAMETER)
    @In
    public double ACAdaptation;

    @Description("start saturation of LPS")
    @Role(PARAMETER)
    @In
    public double satStartLPS;

    @Description("start saturation of MPS")
    @Role(PARAMETER)
    @In
    public double satStartMPS;

    @Description("Time")
    @In public java.util.Calendar time;

    @In @Out public HRU hru;

    private void init() {
        area = hru.area;
        slope = hru.slope;
        sealedGrade = hru.landuse.sealedGrade;

        double[] fc = hru.soilType.fc;
        double mxMPS = 0;
        for(int i = 0; i < fc.length; i++){
            mxMPS += fc[i];
        }

        mxMPS = mxMPS * area;
        mxMPS = mxMPS * FCAdaptation;

        double aircap = hru.soilType.aircap;
        double mxLPS = aircap * area;
        mxLPS = mxLPS * ACAdaptation;

        actLPS = mxLPS * satStartLPS;
        actMPS = mxMPS * satStartMPS;
        maxMPS = mxMPS;
        maxLPS = mxLPS;
        satMPS = actMPS / mxMPS;
        satLPS = actLPS / mxLPS;
        satSoil = (actMPS + actLPS) / (mxMPS + mxLPS);
        init = false;
    }
    
    
    @Execute
    public void execute() {
        if (init) {
            init();
        }
//        run_actMPS = actMPS;
//        run_actLPS = actLPS;
        
        actET = hru.actET;
        potET = hru.potET;
        inRD1 = hru.inRD1;
        inRD2 = hru.inRD2;
        actDPS = hru.actDPS;
        netRain = hru.netRain;
        netSnow = hru.netSnow;
        snowMelt = hru.snowMelt;
        snowDepth = hru.snowDepth;

        // zero out output
        outRD1 = 0;
        outRD2 = 0;
        interflow = 0;
        percolation = 0;
        
        //calculation of saturations first
        calcSoilSaturations();
        
        /** redistributing RD1 and RD2 inflow of antecedent unit */
        redistRD1_RD2_in();
        
        /** calculation of ETP from dep. Storage and open water bodies */
        calcPreInfEvaporation();
        
        /** determining available water for infiltration */
        infiltration = netRain + netSnow + snowMelt + actDPS;
        
        actDPS = 0;
        
        /** infiltration on impervious areas and water bodies
         *  is directly routed as DirectRunoff to the next polygon
         *  a better implementation would be the next river reach */
        calcInfImperv(sealedGrade);
        
        /** determining maximal infiltration rate */
        int mo = time.get(java.util.Calendar.MONTH);
        
        double maxInf = calcMaxInfiltration(mo);
        if(maxInf < infiltration){
            double deltaInf = infiltration - maxInf;
            actDPS = actDPS + deltaInf;
            infiltration = maxInf;
        }
        
        /** determining inflow of infiltration to MPS */
        infiltration = calcMPSInflow(infiltration);
        
        /** determining transpiration from MPS */
        calcMPSTranspiration(false);
        
        /** inflow to LPS */
        infiltration = calcLPSInflow(infiltration);
        
        /** updating saturations */
        calcSoilSaturations();
        
        /** determining outflow from LPS */
        double mobileWater = 0;
        if(actLPS > 0){
            mobileWater = calcLPSoutflow();
        } else {
            mobileWater = 0;
        }
        
        /** Distribution of MobileWater to the lateral (interflow) and
         * vertical (percolation) flowpaths  */
        calcIntfPercRates(mobileWater);
        
        /** determining direct runoff from depression storage */
        overlandflow = overlandflow + calcDirectRunoff();
        
        /** determining internal area routing **/
        calcRD1_RD2_out();
        
        /** determining diffusion from LPS to MPS */
        calcDiffusion();
        
        /** updating saturations */
        calcSoilSaturations();
        
//        satSoil = run_satSoil;
//        satMPS = run_satMPS;
//        satLPS = run_satLPS;
//        actMPS = run_actMPS;
//        actLPS = run_actLPS;
//        actDPS = run_actDPS;

        hru.actET = actET;
        hru.actDPS = actDPS;
        hru.inRD1 = inRD1;
        hru.inRD2 = inRD2;
        hru.outRD1 = outRD1;
        hru.outRD2 = outRD2;
        hru.percolation = percolation;
        hru.interflow = interflow;
        hru.satSoil = satSoil;  // not needed ?
        hru.maxMPS = maxMPS;
        hru.maxLPS = maxLPS;
        hru.actMPS = actMPS;
        hru.actLPS = actLPS;
        hru.satMPS = satMPS;
        hru.satLPS = satLPS;

        
//        outRD1 = run_outRD1;
//        outRD2 = run_outRD2;
//        genRD1 = run_genRD1;
//        genRD2 = run_genRD2;
//        percolation = run_percolation;
//        interflow = run_interflow;
    }
    
    private void calcSoilSaturations(){
        if((actLPS > 0) && (maxLPS > 0)){
            satLPS = actLPS / maxLPS;
        } else {
            satLPS = 0;
        }
        
        if((actMPS > 0) && (maxMPS > 0)){
            satMPS = actMPS / maxMPS;
        } else {
            satMPS = 0;
        }
        
        if(((maxLPS > 0) | (maxMPS > 0)) & ((actLPS > 0) | (actMPS > 0))){
            satSoil = ((actLPS + actMPS) / (maxLPS + maxMPS));
        } else{
            satSoil = 0;
        }
    }
    
    private boolean redistRD1_RD2_in(){
        if(inRD1 > 0){
            actDPS = actDPS + inRD1;
            inRD1 = 0;
        }
        if(inRD2 > 0){
            inRD2 = calcMPSInflow(inRD2);
            inRD2 = calcLPSInflow(inRD2);
            if(inRD2 > 0){
                System.out.println("RD2 is not null");
            }
        }
        return true;
    }
    
    private boolean calcPreInfEvaporation(){
        double deltaETP = potET - actET;
        if(actDPS > 0){
            if(actDPS >= deltaETP){
                actDPS = actDPS - deltaETP;
                deltaETP = 0;
                actET = potET;
            } else{
                deltaETP = deltaETP - actDPS;
                actDPS = 0;
                actET = potET - deltaETP;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
        return true;
    }
    
    private boolean calcInfImperv(double sealedGrade){
        if(sealedGrade > 0.8){
            overlandflow = overlandflow + (1 - soilImpGT80) * infiltration;
            infiltration = infiltration * soilImpGT80;
        } else if(sealedGrade > 0 && sealedGrade <= 0.8){
            overlandflow = overlandflow +  (1 - soilImpLT80) * infiltration;
            infiltration = infiltration * soilImpLT80;
        }
        return true;
    }
    
    private double calcMaxInfiltration(int nowmonth){
        double maxInf = 0;
        calcSoilSaturations();
        if(snowDepth > 0) {
            maxInf = soilMaxInfSnow * area;
        } else if((nowmonth >= 5) & (nowmonth <=10)) {
            maxInf = (1 - satSoil) * soilMaxInfSummer * area;
        } else {
            maxInf = (1 - satSoil) * soilMaxInfWinter * area;
        }
        return maxInf;
    }

    private boolean calcMPSTranspiration(boolean debug){
        double maxTrans = 0;
        /** updating saturations */
        calcSoilSaturations();
        
        /** delta ETP */
        double deltaETP = potET - actET;
        
        /**linear reduction after MENZEL 1997 was chosen*/
        //if(etp_reduction == 0){
        if(soilLinRed > 0){
            /** reduction if actual saturation is smaller than linear factor */
            if(satMPS < soilLinRed){
                //if(sat_MPS < etp_linRed){
                double reductionFactor = satMPS / soilLinRed;
                //double reductionFactor = sat_MPS / etp_linRed;
                maxTrans = deltaETP * reductionFactor;
            } else{
                maxTrans = deltaETP;
            }
        }
        /** polynomial reduction after KRAUSE 2001 was chosen */
        else if(soilPolRed > 0){
            //else if(etp_reduction == 1){
            double sat_factor = -10. * Math.pow((1 - satMPS), soilPolRed);
            //double sat_factor = Math.pow((1 - sat_MPS), etp_polRed);
            double reductionFactor = Math.pow(10, sat_factor);
            maxTrans = deltaETP * reductionFactor;
            if(maxTrans > deltaETP) {
                maxTrans = deltaETP;
            }
        }
        
        /** Transpiration from MPS */
        if(deltaETP > 0){
            
            /** if enough water is available */
            if(actMPS > maxTrans){
                actMPS = actMPS - maxTrans;
                deltaETP = deltaETP - maxTrans;
            }
            /** storage is limiting ETP */
            else{
                deltaETP = deltaETP - actMPS;
                actMPS = 0;
            }
        }
        
        /** recalculation actual ETP */
        actET = potET - deltaETP;
        calcSoilSaturations();
        
        /* @todo: ETP from water bodies has to be implemented here */
        return true;
    }
    
    private double calcMPSInflow(double infiltration){
        double inflow = infiltration;
        
        /** updating saturations */
        calcSoilSaturations();
        
        /**checking if MPS can take all the water */
        if(inflow < (maxMPS - actMPS)){
            /** if MPS is empty it takes all the water */
            if(actMPS == 0){
                actMPS = actMPS + inflow;
                inflow = 0;
            }
            /** MPS is partly filled and gets part of the water */
            else{
                double alpha = soilDistMPSLPS;
                //if sat_MPS is 0 the next equation would produce an error,
                //therefore it is set to MPS_sat is set to 0.0000001 in that case
                if(satMPS == 0) {
                    satMPS = 0.0000001;
                }
                double inMPS = (inflow) * (1. - Math.exp(-1*alpha / satMPS));
                actMPS = actMPS + inMPS;
                inflow = inflow - inMPS;
            }
        }
        /** infiltration exceeds storage capacity of MPS */
        else{
            double deltaMPS = maxMPS - actMPS;
            actMPS = maxMPS;
            inflow = inflow - deltaMPS;
        }
        
        return inflow;
    }
    
    private double calcLPSInflow(double infiltration){
        actLPS = actLPS + infiltration;
        infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if(actLPS > maxLPS){
            actDPS = actDPS + (actLPS - maxLPS);
            actLPS = maxLPS;
        }
        return infiltration;
    }
    
    private double calcLPSoutflow(){
        double alpha = soilOutLPS;
        //if soilSat is 1 the outflow equation would produce an error,
        //for this (unlikely) case soilSat is set to 0.999999
        
        //testing if LPSsat might give a better behaviour
        if(satLPS == 1.0) {
            satLPS = 0.999999;
        }

        //original function
        //double potLPSoutflow = act_LPS * (1. - Math.exp(-1*alpha/(1-sat_LPS)));
        double potLPSoutflow = Math.pow(satSoil, alpha) * actLPS;
        
        //testing a simple function function out = 1/k * sto
        //double potLPSoutflow = 1 / alpha * act_LPS;//Math.pow(act_LPS, alpha);
        if(potLPSoutflow > actLPS) {
            potLPSoutflow = actLPS;
        }
        
        double LPSoutflow = potLPSoutflow;// * ( 1 / parameter.getDouble("lps_kfForm"));
        if(LPSoutflow > actLPS) {
            LPSoutflow = actLPS;
        }
        actLPS = actLPS - LPSoutflow;
        return LPSoutflow;
    }
    
    private boolean calcIntfPercRates(double mobileWater){
        if(mobileWater > 0){
            double slope_weight = (Math.tan(slope * (Math.PI / 180.0))) * soilLatVertLPS;
            
            /** potential part of percolation */
            double part_perc = (1 - slope_weight);
            if(part_perc > 1) {
                part_perc = 1;
            } else if(part_perc < 0) {
                part_perc = 0;
            }
            
            /** potential part of interflow */
            double part_intf = (1 - part_perc);
            
            interflow = mobileWater * part_intf;
            percolation = mobileWater * part_perc;
            
            /** checking if percolation rate is limited by parameter */
            double maxPerc = soilMaxPerc * area;
            if(percolation > maxPerc){
                double rest = percolation - maxPerc;
                percolation = maxPerc;
                interflow = interflow + rest;
            }
        }
        /** no MobileWater available */
        else {
            interflow = 0;
            percolation = 0;
        }
        return true;
    }
    
    private double calcDirectRunoff(){
        double directRunoff = 0;
        if(actDPS > 0){
            double maxDep = 0;
            /** depression storage on slopes is half the normal dep. storage */
            if(slope > 5.0){
                maxDep = (soilMaxDPS * area) / 2;
            } else {
                maxDep = soilMaxDPS * area;
            }
            if(actDPS > maxDep){
                directRunoff = actDPS - maxDep;
                actDPS = maxDep;
            }
        }
        return directRunoff;
    }
    
    private boolean calcRD1_RD2_out(){
        /** DIRECT OVERLANDFLOW */
        //switched off 15-03-2004
        //double RD1_output_factor = conc_index / parameter.getDouble("conc_recRD1");
        double RD1_output_factor = 1. / soilConcRD1;
        if(RD1_output_factor > 1) {
            RD1_output_factor = 1;
        } else if(RD1_output_factor < 0) {
            RD1_output_factor = 0;
        }
        
        /** real RD1 output */
        double RD1_output = overlandflow * RD1_output_factor;
        /** rest is put back to dep. Storage */
        actDPS = actDPS + (overlandflow - RD1_output);
        outRD1 = outRD1 + RD1_output;
//        genRD1 = outRD1;// - in_RD1;
        //in_RD1 = 0;
        /** lateral interflow */
        //switched of 15-03-2004
        //double RD2_output_factor = conc_index / parameter.getDouble("conc_recRD2");
        double RD2_output_factor = 1. / soilConcRD2;
        if(RD2_output_factor > 1) {
            RD2_output_factor = 1;
        } else if(RD2_output_factor < 0) {
            RD2_output_factor = 0;
        }
        
        /** real RD2 output */
        double RD2_output = interflow * RD2_output_factor;
        /** rest is put back to LPS Storage */
        actLPS = actLPS + (interflow - RD2_output);
        outRD2 = outRD2 + RD2_output;
//        genRD2 = outRD2;// - in_RD2;
//        if(genRD2 < 0) {
//            genRD2 = 0;
//        }

        //in_RD2 = 0;
        
        overlandflow = 0;
        interflow = 0;
        return true;
    }
    
    private boolean calcDiffusion(){
        double diffusion = 0;
        /** updating saturations */
        calcSoilSaturations();
        double deltaMPS = maxMPS - actMPS;
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if(satMPS == 0.0){
            diffusion = 0;
        } else{
            double diff = soilDiffMPSLPS;
            
            //new equation like all other exps 04.03.04
            diffusion = actLPS * (1. - Math.exp((-1. * diff) / satMPS));
        }
        
        if(diffusion > actLPS) {
            diffusion = actLPS;
        }
        
        /** MPS can take all the water from diffusion */
        if(diffusion < deltaMPS){
            actMPS = actMPS + diffusion;
            actLPS = actLPS - diffusion;
        }
        /** MPS can take only part of the water */
        else{
            double rest = maxMPS - actMPS;
            actMPS = maxMPS;
            actLPS = actLPS - rest;
        }
        return true;
    }
}
