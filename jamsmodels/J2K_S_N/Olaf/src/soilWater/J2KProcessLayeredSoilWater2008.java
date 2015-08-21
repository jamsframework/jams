/*
 * J2KProcessLumpedSoilWater.java
 * Created on 25. November 2005, 13:21
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
package soilWater;

import ages.types.HRU;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

/**
 *
 * @author Peter Krause modifications by Manfred Fink
 */
@Author
    (name="Peter Krause, modifications by Manfred Fink")
@Description
    ("Calculates soil water balance for each HRU with vertical layers")
@Keywords
    ("Soilwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilWater/J2KProcessLayeredSoilWater2008.java $")
@VersionInfo
    ("$Id: J2KProcessLayeredSoilWater2008.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class J2KProcessLayeredSoilWater2008  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + J2KProcessLayeredSoilWater2008.class.getSimpleName());


    @Description("maximum depression storage")
    @Unit("mm")
    @Role(PARAMETER)
    @In public double soilMaxDPS;

    @Description("poly reduction of ETP")
    @Role(PARAMETER)
    @In public double soilPolRed;

    @Description("linear reduction of ETP")
    @Role(PARAMETER)
    @In public double soilLinRed;

    @Description("maximum infiltration rate in summer")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxInfSummer;

    @Description("maximum infiltration rate in winter")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxInfWinter;

    @Description("maximum infiltration rate on snow")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxInfSnow;

    @Description("maximum infiltration part on sealed areas (gt 80%)")
    @Role(PARAMETER)
    @In public double soilImpGT80;

    @Description("maximum infiltration part on sealed areas (lt 80%)")
    @Role(PARAMETER)
    @In public double soilImpLT80;

    @Description("MPS/LPS distribution coefficient for inflow")
    @Role(PARAMETER)
    @In public double soilDistMPSLPS;

    @Description("MPS/LPS diffusion coefficient")
    @Role(PARAMETER)
    @In public double soilDiffMPSLPS;

    @Description("LPS outflow coefficient")
    @Role(PARAMETER)
    @In public double soilOutLPS;

    @Description("LPS lateral-vertical distribution coefficient")
    @Role(PARAMETER)
    @In public double soilLatVertLPS;

    @Description("maximum percolation rate in soil")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double soilMaxPerc;

    @Description("maximum percolation rate out of soil")
    @Unit("mm/d")
    @Role(PARAMETER)
    @In public double geoMaxPerc;

    @Description("concentration coefficient for RD1")
    @Role(PARAMETER)
    @In public double soilConcRD1;

    @Description("concentration coefficient for RD2")
    @Role(PARAMETER)
    @In public double soilConcRD2;

    @Description("water-use distribution parameter for Transpiration")
    @Role(PARAMETER)
    @In public double BetaW;

    @Description("Layer MPS diffusion factor > 0 [-]  resistance default = 10")
    @Role(PARAMETER)
    @In public double kdiff_layer;

    // In
    @Description("time")
    @In public java.util.Calendar time;
    
    @Description("attribute area")
    @In public double area;
    
    @Description("attribute slope")
    @In public double slope;
    
    @Description("sealed grade")
    @In public double sealedGrade;
    
    @Description("state variable net rain")
    @In public double netRain;
    
    @Description("state variable net snow")
    @In public double netSnow;
    
    @Description("state variable potET")
    @In public double potET;
    
    @Description("snow depth")
    @In public double snowDepth;
    
    @Description("daily snow melt")
    @In public double snowMelt;
    
    @Description("horizons")
    @In public int horizons;
    
    @Description("depth of soil layer")
    @Unit("cm")
    @In public double[] depth_h ;
    
    @Description("actual depth of roots")
    @Unit("m")
    @In public double zrootd;
    
    @Description("HRU attribute maximum MPS")
    @In public double[] maxMPS_h ;
    
    @Description("HRU attribute maximum LPS")
    @In public double[] maxLPS_h ;

    @Description("Array of state variables LAI ")
    @In public double LAI;

    @Description("estimated hydraulic conductivity")
    @Unit("cm/d")
    @In public double Kf_geo;

    @Description("in cm/d soil hydraulic conductivity")
    @In public double[] kf_h ;

    @Description("Indicates whether roots can penetrate or not the soil layer [-]")
    @In public double[] root_h ;
    
    @Description("HRU attribute maximum MPS of soil")
    @Out public double soilMaxMPS;
    
    @Description("HRU attribute maximum LPS of soil")
    @Out public double soilMaxLPS;
    
    @Description("HRU state var actual MPS of soil")
    @Out public double soilActMPS;
    
    @Description("HRU state var actual LPS of soil")
    @Out public double soilActLPS;
    
    @Description("HRU state var saturation of MPS of soil")
    @Out public double soilSatMPS;
    
    @Description("HRU state var saturation of LPS of soil")
    @Out public double soilSatLPS;
    
//    @Description("HRU state var saturation of whole soil")
//    public double satSoil;
    
    @Description("HRU statevar infiltration")
    @Out public double infiltration;
    
    @Description("HRU statevar interflow")
    @Out public double interflow;
    
    @Description("HRU statevar percolation")
    @Out public double percolation;
    
    @Description("HRU statevar RD1 outflow")
    @Out public double outRD1;
    
    @Description("HRU statevar RD1 generation")
    @Out public double genRD1;
    
    @Description("HRU statevar RD2 outflow")
    @Out public double[] outRD2_h ;
    
    @Description("HRU statevar RD2 generation")
    @Out public double[] genRD2_h ;
       
    @Description("intfiltration poritions for the single horizonts")
    @Out public double[] infiltration_hor ;
    
    @Description("percolation out of the single horizonts")
    @Out public double[] perco_hor ;

    @Description("evapotranspiration out of the single horizonts")
    @Out public double[] actETP_h ;

    @Description("mps diffusion between layers value")
    @Out public double[] w_layer_diff ;
    
    @Description("max Root depth in soil")
    @Unit("m")
    @Out public double soil_root;    //internal state variables

    // In Out
    @Description("state variable actET")
    @In @Out public double actET;

    @Description("HRU statevar RD2 inflow")
    @In @Out public double[] inRD2_h;

    @Description("HRU statevar RD1 inflow")
    @In @Out public double inRD1;

    @Description("HRU state var actual MPS")
    @In @Out public double[] actMPS_h;

    @Description("HRU state var actual LPS")
    @In @Out public double[] actLPS_h;

    @Description("HRU state var actual depression storage")
    @In @Out public double actDPS;

    @Description("HRU state var saturation of MPS")
    @In @Out public double[] satMPS_h;

    @Description("HRU state var saturation of LPS")
    @In @Out public double[] satLPS_h;

    @Description("Current hru object")
    @In @Out public HRU hru;

    @Role(Role.PARAMETER + Role.OUTPUT)
    @In public File balFile;
    
        
    double run_actDPS, run_satSoil1, run_inRain, run_inSnow,
            run_snowMelt, run_infiltration, run_latComp, run_vertComp, run_overlandflow, run_potETP, run_actETP, run_snowDepth, run_area, run_slope,
            run_inRD1, soilSatMps, soilSatLps, soilActMps, soilActLps, soilMaxMps, soilMaxLps, run_outRD1, run_genRD1, lowpart, top_satsoil;
    
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS, run_satMPS, run_satLPS, run_inRD2, run_satHor, run_outRD2, run_genRD2;
    double[] runlayerdepth, horETP, runkf_h, flux_h_h1;
    int nhor;
    boolean debug;

    double[] infilhor;
    double[] perchor;
    double[] actETP_hor;

    @Execute
    public void execute()  {
        if (actETP_hor == null) {
            infilhor = new double[horizons];
            perchor = new double[horizons];
            actETP_hor = new double[horizons];
        }

        double balMPSstart = 0;
        double balMPSend = 0;
        double balLPSstart = 0;
        double balLPSend = 0;
        double balDPSstart = 0;
        double balDPSend = 0;
        double balIn = 0;
        double balOut = 0;
        double balET = 0;
        double sumactETP = 0;

        debug = false;
        run_area = area;
        run_slope = slope;
        nhor = horizons;

        //FIXWA
        if (nhor == 0) {
            nhor = 1;
        }
        
        flux_h_h1 = new double[nhor - 1];
        run_satHor = new double[nhor];

        run_maxMPS = maxMPS_h;
        run_maxLPS = maxLPS_h;
        run_actMPS = actMPS_h;
        run_actLPS = actLPS_h;
        run_satMPS = satMPS_h;
        run_satLPS = satLPS_h;
        run_actDPS = actDPS;

        run_inRD1 = inRD1;
        run_inRD2 = inRD2_h;

        runkf_h = kf_h;

        balIn += run_inRD1;
        
        run_inRain = netRain;
        run_inSnow = netSnow;
        run_potETP = potET;
        run_actETP = actET;
        run_snowDepth = snowDepth;
        run_snowMelt = snowMelt;

        runlayerdepth = new double[nhor];
        run_genRD2 = new double[nhor];
        run_outRD2 = new double[nhor];
        
        run_latComp = 0;
        run_vertComp = 0;
        run_genRD1 = 0;
        run_outRD1 = 0;
        lowpart = 0;
        top_satsoil = 0;

        balET = run_actETP;
        balDPSstart = run_actDPS;
        for (int h = 0; h < nhor; h++) {
            /** determining inflow of infiltration to MPS */
            balIn += run_inRD2[h];
            balMPSstart += run_actMPS[h];
            balLPSstart += run_actLPS[h];
            run_genRD2[h] = 0;
            run_outRD2[h] = 0;
        }

        //calculation of saturations first
        calcSoilSaturations(false);

        layer_diffusion();

        //Attributes.setNewDoubleArray(flux_h_h1, w_layer_diff);
        w_layer_diff = flux_h_h1;

        /** redistributing RD1 and RD2 inflow of antecedent unit */
        redistRD1_RD2_in();

        /** calculation of ETP from dep. Storage and open water bodies */
        calcPreInfEvaporation();
        double preinfep = run_actETP;

        /** determining available water for infiltration */
        run_infiltration = run_inRain + run_inSnow + run_snowMelt + run_actDPS;

        if (run_infiltration < 0) {
            System.out.println("negative infiltration!");
            System.out.println("inRain: " + run_inRain);
            System.out.println("inSnow: " + run_inSnow);
            System.out.println("inSnowMelt: " + run_snowMelt);
            System.out.println("inDPS: " + run_actDPS);
        }
        //balance
        balIn += run_inRain;
        balIn += run_inSnow;
        balIn += run_snowMelt;

        run_actDPS = 0;
        run_inRain = 0;
        run_inSnow = 0;
        run_snowMelt = 0;

        /** infiltration on impervious areas and water bodies
         *  is directly routed as DirectRunoff to the next polygon
         *  a better implementation would be the next river reach */
        calcInfImperv(sealedGrade);
        calcSoilSaturations(false);
        /** determining maximal infiltration rate */

        double maxInf = calcMaxInfiltration(time.get(java.util.Calendar.MONTH) + 1);
        if (maxInf < run_infiltration) {
            //System.out.getRuntime().println("maxInf:");
            double deltaInf = run_infiltration - maxInf;
            run_actDPS = run_actDPS + deltaInf;
            run_infiltration = maxInf;
        }

        horETP = calcMPSEvapotranslayer(true, nhor);

        for (int h = 0; h < nhor; h++) {
            /** determining inflow of infiltration to MPS */
            //balMPSstart += run_actMPS[h];
            //balLPSstart += run_actLPS[h];
            double bak_infiltration = run_infiltration;
            run_infiltration = calcMPSInflow(run_infiltration, h);
            run_infiltration = calcLPSInflow(run_infiltration, h);
            infilhor[h] = (bak_infiltration - run_infiltration);
        }

        if (run_infiltration > 0) {
            //System.out.getRuntime().println("Infiltration after: " +  run_infiltration);
            run_actDPS += run_infiltration;
            run_infiltration = 0;
        }
        //balDPSstart = run_actDPS;
        for (int h = 0; h < nhor; h++) {
            /** determining inflow of infiltration to MPS */
            //run_infiltration = calcMPSInflow(run_infiltration, h);
            //distributing vertComp from antecedent horzion
            run_vertComp = calcMPSInflow(run_vertComp, h);
            //run_vertComp = calcLPSInflow(run_vertComp, h);
            /** determining transpiration from MPS */
            calcMPSTranspiration(false, h);
            actETP_hor[h] = run_actETP;
            /** inflow to LPS */
            //run_infiltration = calcLPSInflow(run_infiltration, h);
            run_vertComp = calcLPSInflow(run_vertComp, h);

            if (run_vertComp > 0) {
                //System.out.getRuntime().println("VertIn is not zero!");
                //we put it back where it came from, the horizon above!
                run_vertComp = calcMPSInflow(run_vertComp, h - 1);
                run_vertComp = calcLPSInflow(run_vertComp, h - 1);
                if (run_vertComp > 0) {
                    System.out.println("---------------------VertIn is still not zero!");
                }
            }
            /** updating saturations */
            calcSoilSaturations(false);

            /** determining outflow from LPS */
            double MobileWater = 0;
            if (run_actLPS[h] > 0) {
                MobileWater = calcLPSoutflow(h);
            } else {
                MobileWater = 0;
                /** Distribution of MobileWater to the lateral (interflow) and
                 * vertical (percolation) flowpaths  */       
            }
            calcIntfPercRates(MobileWater, h);

            /** updating saturations */
            calcSoilSaturations(false);
            perchor[h] = run_vertComp;

            /** determining internal area routing **/
            calcRD2_out(h);

            /** determining diffusion from LPS to MPS */
            calcDiffusion(h);

            /** updating saturations */
            calcSoilSaturations(false);
            //System.out.getRuntime().println("end of horizon loop");
        }
        
        if (run_overlandflow < 0) {
            System.out.println("overlandflow is negative! --> " + run_overlandflow);
            /** determining direct runoff from depression storage */
        }
        run_overlandflow = run_overlandflow + calcDirectRunoff();
        calcRD1_out();

        for (int h = 0; h < nhor; h++) {
            balMPSend += run_actMPS[h];
            balLPSend += run_actLPS[h];
            balOut += run_outRD2[h];
            sumactETP += actETP_hor[h];
        }
        balDPSend = run_actDPS;
        balET =  sumactETP + preinfep - balET;
        balOut += balET;   
        balOut += run_outRD1;
        balOut += run_vertComp;
        balET = sumactETP + preinfep;

        double balance = balIn + (balMPSstart - balMPSend) + (balLPSstart - balLPSend) + (balDPSstart - balDPSend) - balOut;
//        if (Math.abs(balance) > 0.00001) {
//            System.out.println("               balance error at : " + time.toString() + " --> " + balance);
//        }
        satMPS_h = run_satMPS;
        satLPS_h = run_satLPS;
        actMPS_h = run_actMPS;
        actLPS_h = run_actLPS;
        actDPS = run_actDPS;
//        netRain = run_inRain;
//        netSnow = run_inSnow;
        actET = balET;
        inRD1 = run_inRD1;
        inRD2_h = run_inRD2;
        outRD1 = run_outRD1;
        outRD2_h = run_outRD2;
        genRD1 = run_genRD1;
        genRD2_h = run_genRD2;

        percolation = run_vertComp;
        interflow = run_latComp;

        soilActMPS = soilActMps;
        soilMaxMPS = soilMaxMps;
        soilSatMPS = soilSatMps;
        soilActLPS = soilActLps;
        soilMaxLPS = soilMaxLps;
        soilSatLPS = soilSatLps;
        
        infiltration_hor = infilhor;
        perco_hor = perchor;
        actETP_h = actETP_hor;
        
        if (log.isLoggable(Level.INFO)) {
           log.info("RD2_out: " + run_outRD2[0] + "\t" + run_outRD2[1] + "\t" + run_outRD2[2]);
        }

        // for now, this will later go away.
//        if (hru.ID != 302) {
//            return;
//        }
//
//        if (balFile.getName().equals("-")) {
//            return;
//        }
//
//        // wb output
//        if (w == null) {
//            try {
//                w = new PrintWriter(balFile);
//            } catch (IOException E) {
//                throw new RuntimeException(E);
//            }
//            w.println("@T, balance");
//            w.println(" Created, " + new Date());
//            w.println(" HRU-id, " + hru.ID);
//            w.println("@H, date,balance,balIn,balOut,balMPSstart,balMPSend,balLPSstart,balLPSend,balDPSstart,balDPSend,test_outRD2,sumactETP,preinfep,run_inRD1,run_outRD1,run_vertComp");
//            w.println(" Type, Date,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double");
//            w.println(" Format, yyyy-MM-dd,,,,,,,,,,");
//        }
//
//        String s = String.format(",%1$tY-%1$tm-%1$td, %2$7.3f, %3$7.3f, %4$7.3f, %5$7.3f, %6$7.3f, %7$7.3f, %8$7.3f, %9$7.3f, %10$7.3f, %11$7.3f, %12$7.3f, %13$7.3f, %14$7.3f, %15$7.3f, %16$7.3f",
//                time, balance, balIn, balOut,
//                balMPSstart, balMPSend, balLPSstart, balLPSend, balDPSstart, balDPSend, test_outRD2, sumactETP, preinfep, run_inRD1, run_outRD1, run_vertComp);
//        w.println(s);
    }

    private PrintWriter w;

    @Finalize
    public void done() {
        if (w!=null) {
            w.close();
        }
    }
    
    private boolean calcSoilSaturations(boolean debug){
        
        soilMaxMps = 0;
        soilActMps = 0;
        soilMaxLps = 0;
        soilActLps = 0;
        soilSatMps = 0;
        soilSatLps = 0;
        
        double upperMaxMps = 0;
        double upperActMps = 0;
        double upperMaxLps = 0;
        double upperActLps = 0;
        double[] infil_depth = new double[nhor];
        double partdepth = 0;
        double soilinfil = 50;
        
        for(int h = 0; h < nhor; h++){
            if((run_actLPS[h] > 0) && (run_maxLPS[h] > 0)){
                run_satLPS[h] = run_actLPS[h] / run_maxLPS[h];
            } else {
                run_satLPS[h] = 0;
            }
            
            if((run_actMPS[h] > 0) && (run_maxMPS[h] > 0)){
                run_satMPS[h] = run_actMPS[h] / run_maxMPS[h];
            } else {
                run_satMPS[h] = 0;
            }
            
            if(((run_maxLPS[h] > 0) | (run_maxMPS[h] > 0)) & ((run_actLPS[h] > 0) | (run_actMPS[h] > 0))){
                run_satHor[h] = ((run_actLPS[h] + run_actMPS[h]) / (run_maxLPS[h] + run_maxMPS[h]));
            } else{
                run_satSoil1 = 0;
            }
            
            infil_depth[h]  += depth_h[h];
            if (infil_depth[h] <= soilinfil || h == 0) {
                upperMaxMps += run_maxMPS[h] * depth_h[h];
                upperActMps += run_actMPS[h] * depth_h[h];
                upperMaxLps += run_maxLPS[h] * depth_h[h];
                upperActLps += run_actLPS[h] * depth_h[h];
                partdepth += depth_h[h];
                
            } else if (infil_depth[h - 1] <= soilinfil) {
                lowpart = soilinfil - partdepth;
                upperMaxMps += run_maxMPS[h] * lowpart;
                upperActMps += run_actMPS[h] * lowpart;
                upperMaxLps += run_maxLPS[h] * lowpart;
                upperActMps += run_actLPS[h] * lowpart;
                
            }
            soilMaxMps += run_maxMPS[h];
            soilActMps += run_actMPS[h];
            soilMaxLps += run_maxLPS[h];
            soilActLps += run_actLPS[h];
        }
        
        if(((soilMaxLps > 0) | (soilMaxMps > 0)) & ((soilActLps > 0) | (soilActMps > 0))){
            run_satSoil1 = ((soilActLps + soilActMps) / (soilMaxLps + soilMaxMps));
            top_satsoil = ((upperActLps + upperActMps) / (upperMaxLps + upperMaxMps));
            soilSatMps = (soilActMps / soilMaxMps);
            soilSatLps = (soilActLps / soilMaxLps);
        } else{
            run_satSoil1 = 0;
        }
        return true;
    }
    
    private boolean redistRD1_RD2_in() {
        //RD1 is put to DPS first
        if(run_inRD1 > 0){
            run_actDPS = run_actDPS + run_inRD1;
            run_inRD1 = 0;
        }
        
        for(int h = 0; h < nhor; h++){
            if(run_inRD2[h] > 0){
                run_inRD2[h] = calcMPSInflow(run_inRD2[h], h);
                run_inRD2[h] = calcLPSInflow(run_inRD2[h], h);
                if(run_inRD2[h] > 0){
                    //System.out.getRuntime().println("RD2 of entity " + entity.getDouble("ID") + " and horizon " + h +  " is routed through RD2out: "+run_inRD2[h]);
                    run_outRD2[h] = run_outRD2[h] + run_inRD2[h];
                    run_inRD2[h] = 0;
                }
            }
        }
        return true;
    }
    
    private boolean layer_diffusion(){
        for(int h = 0; h < nhor - 1; h++){
            //calculate diffussion factor - order horizontal
            //diffusion only occur when gravitative flux is not dominating
            if ((run_satLPS[h] < 0.05 )&&(run_satMPS[h] < 0.8 || run_satMPS[h+1] < 0.8)&&(run_satMPS[h] > 0 || run_satMPS[h+1] > 0) ){
                
                //calculate gradient
                double gradient_h_h1 = (Math.log10(2 - run_satMPS[h]) - Math.log10(2 - run_satMPS[h+1]));
                
                //calculate resistance
                double satbalance = Math.pow((Math.pow(run_satMPS[h],2)+(Math.pow(run_satMPS[h+1],2)))/2.0,0.5);
                double resistance_h_h1 = Math.log10(satbalance) * - kdiff_layer;
                
                //calculate amount of water to equilize saturations in layers
                double avg_sat = ((run_maxMPS[h] * run_satMPS[h]) + (run_maxMPS[h+1] * run_satMPS[h+1]))/(run_maxMPS[h] + run_maxMPS[h+1]);
                double pot_flux = Math.abs((avg_sat - run_satMPS[h]) * run_maxMPS[h]);
                
                //calculate water fluxes
                double flux = (pot_flux * gradient_h_h1 / resistance_h_h1);
                if (flux >= 0) {
                    flux_h_h1[h] = Math.min(flux, pot_flux);
                    //flux_h1_h[h] = Math.min(Math.min(flux, pot_flux), maxflux);
                } else {
                    //flux_h1_h[h] = Math.max(Math.max(flux, -pot_flux), -maxflux);
                    flux_h_h1[h] = Math.max(flux, -pot_flux);
                }
            }else{
                flux_h_h1[h] = 0;
            }
            run_actMPS[h] = run_actMPS[h] + flux_h_h1[h];
            run_actMPS[h+1] = run_actMPS[h+1] - flux_h_h1[h];
        }
        return true;
    }
    
    
    private boolean calcPreInfEvaporation(){
        double deltaETP = run_potETP - run_actETP;
        if(run_actDPS > 0){
            if(run_actDPS >= deltaETP){
                run_actDPS = run_actDPS - deltaETP;
                deltaETP = 0;
                run_actETP = run_potETP;
            } else{
                deltaETP = deltaETP - run_actDPS;
                run_actDPS = 0;
                run_actETP = run_potETP - deltaETP;
            }
        }
        /** @todo implementation for open water bodies has to be implemented here */
        return true;
    }
    
    private boolean calcInfImperv(double sealedGrade){
        if(sealedGrade > 0.8){
            run_overlandflow = run_overlandflow + (1 - soilImpGT80) * run_infiltration;
            run_infiltration = run_infiltration * soilImpGT80;
        } else if(sealedGrade > 0 && sealedGrade <= 0.8){
            run_overlandflow = run_overlandflow +  (1 - soilImpLT80) * run_infiltration;
            run_infiltration = run_infiltration * soilImpLT80;
        }
        if(run_overlandflow < 0) {
            System.out.println("overlandflow gets negative because of sealing! " + soilImpGT80 + ", " + soilImpLT80 + ", " + run_infiltration);
        }
        return true;
    }
    
    private double calcMaxInfiltration(int nowmonth){
        double maxInf = 0;
        calcSoilSaturations(false);
        if(run_snowDepth > 0) {
            maxInf = soilMaxInfSnow * runkf_h[0] * run_area;
        } else if((nowmonth >= 5) & (nowmonth <=10)) {
            maxInf = (1 - run_satSoil1) * soilMaxInfSummer * runkf_h[0] * run_area;
        } else {
            maxInf = (1 - run_satSoil1) * soilMaxInfWinter * runkf_h[0] * run_area;
        }
        return maxInf;
    }
    
    private double[] calcMPSEvapotranslayer(boolean debug, int nhor){ //author: Manfred Fink; Method after SWAT
        double[] hETP = new double[nhor];
        double sumlayer = 0;
        int i = 0;
        double runrootdepth = (zrootd * 1000) + 10;
        double[] partroot = new double[nhor];
        double runLAI = LAI;
        double pTransp = 0;
        double pEvap = 0;
        double[] transp_hord = new double[nhor];
        double[] evapo_hord = new double[nhor];
        double[] transp_hor = new double[nhor];
        double[] evapo_hor = new double[nhor];
        double horbal = 0;
        double test = 0;
        
        // drifferentiation between evaporation and transpiration
        double deltaETP = run_potETP - run_actETP;
        if (runLAI <= 3){
            pTransp = (deltaETP * runLAI) / 3;
        } else if (runLAI > 3){
            pTransp = deltaETP;
        }
        pEvap = deltaETP - pTransp;
        
        double soilroot = 0;
        // EvapoTranspiration loop 1: calculating layer poritions within rootdepth
        while (i < nhor) {
            sumlayer = sumlayer  +  depth_h[i] * 10;
            if (root_h[i] == 1.0){
                soilroot = soilroot + depth_h[i] * 10;
            }
            runlayerdepth[i] = sumlayer;
            if (runrootdepth > runlayerdepth[0]){
                if (runrootdepth > runlayerdepth[i] && root_h[i] == 1.0){
                    partroot[i] = 1 ;
                }else if (runrootdepth > runlayerdepth[i - 1] && root_h[i] == 1.0){
                    partroot[i] = (runrootdepth - runlayerdepth[i - 1]) /  (runlayerdepth[i] - runlayerdepth[i - 1]);
                }else {
                    partroot[i] = 0;
                }
            }else if (i == 0){
                partroot[i] = runrootdepth /  runlayerdepth[0];
            }
            i++;
        }
        
        if (runrootdepth >= sumlayer) {
            runrootdepth = sumlayer;
        }
        
        i = 0;
        while (i < nhor) {
            runrootdepth = Math.min(runrootdepth,soilroot);
            // Transpiration loop 2: calculating transpiration distribution function with depth in layers
            transp_hord[i] = (pTransp  * (1 - Math.exp(-BetaW*(runlayerdepth[i]/runrootdepth)))) / (1 - Math.exp(-BetaW));
            if  (transp_hord[i] >  pTransp){
                transp_hord[i] = pTransp;
            }
            // Evaporation loop 2: calculating evaporation distribution function with depth in layers
            evapo_hord[i] = pEvap * (runlayerdepth[i] / (runlayerdepth[i] + (Math.exp(2.374 -(0.00713 * runlayerdepth[i])))));
            if  (evapo_hord[i] >  pEvap){
                evapo_hord[i] = pEvap;
            }
            //allocation of the rest Evap to the lowest horizon 
            if (i == nhor -1){
                evapo_hord[i] =  pEvap;
                transp_hord[i] =  pTransp;
            }
            i++;
        }
        i = 0;
        while (i < nhor) {
            if (i == 0){
                transp_hor[i] = transp_hord[i];
                evapo_hor[i] = evapo_hord[i];
            }else{
                transp_hor[i] = transp_hord[i] - transp_hord[i-1];
                evapo_hor[i] = evapo_hord[i] - evapo_hord[i-1];
            }
            hETP[i] =  transp_hor[i] + evapo_hor[i];
            if (debug) {
                horbal = horbal + hETP[i];
                test =  deltaETP - horbal;
            }
            i++;
        }
        if ((test > 0.0000001 || test < -0.0000001) && debug){
            System.out.println("evaporation balance error = " + test);
        }
        soil_root = soilroot / 1000;
        return hETP;
    }
    
    private boolean calcMPSTranspiration(boolean debug, int hor){
        double maxTrans = 0;
        /** updating saturations */
        calcSoilSaturations(debug);
        
        /** delta ETP */
        double deltaETP = horETP[hor];
        
        /**linear reduction after MENZEL 1997 was chosen*/
        //if(etp_reduction == 0){
        if(soilLinRed > 0){
            /** reduction if actual saturation is smaller than linear factor */
            if(run_satMPS[hor] < soilLinRed){
                //if(sat_MPS < etp_linRed){
                double reductionFactor = run_satMPS[hor] / soilLinRed;
                
                //double reductionFactor = sat_MPS / etp_linRed;
                maxTrans = deltaETP * reductionFactor;
                
            } else{
                maxTrans = deltaETP;
            }
        }
        /** polynomial reduction after KRAUSE 2001 was chosen */
        else if(soilPolRed > 0){
            //else if(etp_reduction == 1){
            double sat_factor = -10. * Math.pow((1 - run_satMPS[hor]), soilPolRed);
            //double sat_factor = Math.pow((1 - sat_MPS), etp_polRed);
            double reductionFactor = Math.pow(10, sat_factor);
            maxTrans = deltaETP * reductionFactor;
            if(maxTrans > deltaETP) {
                maxTrans = deltaETP;
            }
        }
        run_actMPS[hor] = run_actMPS[hor] - maxTrans;
        
        /** recalculation actual ETP */
        run_actETP =  maxTrans;
        calcSoilSaturations(debug);
        
        /* @todo: ETP from water bodies has to be implemented here */
        return true;
    }
    
    private double calcMPSInflow(double infiltration, int hor){
        double inflow = infiltration;
        
        /** updating saturations */
        calcSoilSaturations(false);
        
        /**checking if MPS can take all the water */
        if(inflow < (run_maxMPS[hor] - run_actMPS[hor])){
            /** if MPS is empty it takes all the water */
            if(run_actMPS[hor] == 0){
                run_actMPS[hor] = run_actMPS[hor] + inflow;
                inflow = 0;
            }
            /** MPS is partly filled and gets part of the water */
            else{
                double alpha = soilDistMPSLPS;
                //if sat_MPS is 0 the next equation would produce an error,
                //therefore it is set to MPS_sat is set to 0.0000001 in that case
                if(run_satMPS[hor] == 0) {
                    run_satMPS[hor] = 0.0000001;
                }
                double inMPS = (inflow) * (1. - Math.exp(-1*alpha / run_satMPS[hor]));
                run_actMPS[hor] = run_actMPS[hor] + inMPS;
                inflow = inflow - inMPS;
            }
        }
        /** infiltration exceeds storage capacity of MPS */
        else{
            double deltaMPS = run_maxMPS[hor] - run_actMPS[hor];
            run_actMPS[hor] = run_maxMPS[hor];
            inflow = inflow - deltaMPS;
        }
        /** updating saturations */
        calcSoilSaturations(false);
        return inflow;
    }
    /*
     *problem overflow is put to DPS, we have to deal with that problem
     */
    private double calcLPSInflow(double infiltration, int hor){
        /** updating saturations */
        calcSoilSaturations(false);
        run_actLPS[hor] = run_actLPS[hor] + infiltration;
        infiltration = 0;
        /** if LPS is saturated depression Storage occurs */
        if(run_actLPS[hor] > run_maxLPS[hor]){
            infiltration = (run_actLPS[hor] - run_maxLPS[hor]);
            //run_actDPS = run_actDPS + (run_actLPS[hor] - run_maxLPS[hor]);
            run_actLPS[hor] = run_maxLPS[hor];
        }
        /** updating saturations */
        calcSoilSaturations(false);
        return infiltration;
    }
    
    private double calcLPSoutflow(int hor){
        double alpha = soilOutLPS;
        //if soilSat is 1 the outflow equation would produce an error,
        //for this (unlikely) case soilSat is set to 0.999999
        
        //testing if LPSsat might give a better behaviour
        if(run_satLPS[hor] == 1.0) {
            run_satLPS[hor] = 0.999999;
        }
        //original function
        //double potLPSoutflow = act_LPS * (1. - Math.exp(-1*alpha/(1-sat_LPS)));
        //peters second
        //double potLPSoutflow = Math.pow(run_satHor[hor], alpha) * run_actLPS[hor];
        //Manfreds new
        double potLPSoutflow = (1 - (1/(Math.pow(run_satHor[hor], 2) + alpha))) * run_actLPS[hor];
        //testing a simple function function out = 1/k * sto
        //double potLPSoutflow = 1 / alpha * act_LPS;//Math.pow(act_LPS, alpha);
        if(potLPSoutflow > run_actLPS[hor]) {
            potLPSoutflow = run_actLPS[hor];
        }
        
        double LPSoutflow = potLPSoutflow;// * ( 1 / parameter.getDouble("lps_kfForm"));
        if(LPSoutflow > run_actLPS[hor]) {
            LPSoutflow = run_actLPS[hor];
        }
        
        run_actLPS[hor] = run_actLPS[hor] - LPSoutflow;
        return LPSoutflow;
    }
    
    private boolean calcIntfPercRates(double MobileWater, int hor){
        if(MobileWater > 0){
            double slope_weight = (Math.tan(run_slope * (Math.PI / 180.))) * soilLatVertLPS;
            
            /** potential part of percolation */
            double part_perc = (1 - slope_weight);
            if(part_perc > 1) {
                part_perc = 1;
            } else if(part_perc < 0) {
                part_perc = 0;
            }
            
            /** potential part of interflow */
            double part_intf = (1 - part_perc);
            run_latComp += MobileWater * part_intf;
            run_vertComp += MobileWater * part_perc;
            double maxPerc = 0;
            /** checking if percolation rate is limited by parameter */
            if (hor == nhor - 1){
                maxPerc = geoMaxPerc * run_area * Kf_geo / 86.4;
                /*if (Kf_geo < 10){
                 maxPerc = 0;
                }*/
                // 86.4 cm/d "middle" hydraulic conductivity in geology (1 E-5 m/s)
                if(run_vertComp > maxPerc){
                    double rest = run_vertComp - maxPerc;
                    run_vertComp = maxPerc;
                    run_latComp = run_latComp + rest;
                }
            } else {
                try{
                    maxPerc = soilMaxPerc * run_area * runkf_h[hor + 1] / 86.4;
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.out.println("SOILID = " + hru);
                }
                // 86.4 cm/d "middle" hydraulic conductivity in geology (1 E-5 m/s)
                if(run_vertComp > maxPerc){
                    double rest = run_vertComp - maxPerc;
                    run_vertComp = maxPerc;
                    run_latComp = run_latComp + rest;
                }
                
            }
        } else { /** no MobileWater available */
            run_latComp = 0;
            run_vertComp = 0;
        }
        return true;
    }
    
    private double calcDirectRunoff(){
        double directRunoff = 0;
        if(run_actDPS > 0){
            double maxDep = 0;
            /** depression storage on slopes is half the normal dep. storage */
            if(run_slope > 5.0){
                maxDep = (soilMaxDPS * run_area) / 2;
            } else {
                maxDep = soilMaxDPS * run_area;
            }
            if(run_actDPS > maxDep){
                directRunoff = run_actDPS - maxDep;
                run_actDPS = maxDep;
            }
        }
        if(directRunoff < 0) {
            System.out.println("directRunoff is negative! --> " + directRunoff);
        }
        return directRunoff;
    }
    
    private boolean calcRD2_out(int h){
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
        double RD2_output = run_latComp * RD2_output_factor;
        /** rest is put back to LPS Storage */
        run_actLPS[h] = run_actLPS[h] + (run_latComp - RD2_output);
        run_outRD2[h] = run_outRD2[h] + RD2_output;
        run_genRD2[h] = run_outRD2[h];// - in_RD2;
        if(run_genRD2[h] < 0) {
            run_genRD2[h] = 0;
        }
        //in_RD2 = 0;
        run_latComp = 0;
        return true;
    }
    
    private boolean calcRD1_out(){
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
        double RD1_output = run_overlandflow * RD1_output_factor;
        /** rest is put back to dep. Storage */
        run_actDPS = run_actDPS + (run_overlandflow - RD1_output);
        run_outRD1 = run_outRD1 + RD1_output;
        run_genRD1 = run_outRD1;// - in_RD1;
        //in_RD1 = 0;
        
        run_overlandflow = 0;
        return true;
    }
    
    private void calcDiffusion(int h){
        double diffusion = 0;
        /** updating saturations */
        calcSoilSaturations(false);
        double deltaMPS = run_maxMPS[h] - run_actMPS[h];
        //if sat_MPS is 0 the diffusion equation would produce an error,
        //for this (unlikely) case diffusion is set to zero
        if(run_satMPS[h] == 0.0){
            diffusion = 0;
        } else{
            double diff = soilDiffMPSLPS;
            
            //new equation like all other exps 04.03.04
            diffusion = run_actLPS[h] * (1. - Math.exp((-1. * diff) / run_satMPS[h]));
        }
        
        if(diffusion > run_actLPS[h]) {
            diffusion = run_actLPS[h];
        }
        
        /** MPS can take all the water from diffusion */
        if(diffusion < deltaMPS){
            run_actMPS[h] = run_actMPS[h] + diffusion;
            run_actLPS[h] = run_actLPS[h] - diffusion;
        }
        /** MPS can take only part of the water */
        else{
            double rest = run_maxMPS[h] - run_actMPS[h];
            run_actMPS[h] = run_maxMPS[h];
            run_actLPS[h] = run_actLPS[h] - rest;
        }
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new J2KProcessLayeredSoilWater2008());
    }

}
