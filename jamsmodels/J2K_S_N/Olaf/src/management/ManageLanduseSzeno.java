/*
 * ManageLanduse.java
 *
 * Created on 16. MÃ¤rz 2006, 13:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package management;

import crop.J2KSNLMArable;
import crop.J2KSNCrop;
import crop.J2KSNFertilizer;
import ages.types.HRU;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Controls HRU management (crop, rotation, tillage, fertilization) operations")
@Author
    (name="c5ulbe")
@Keywords
    ("Management")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/management/ManageLanduseSzeno.java $")
@VersionInfo
    ("$Id: ManageLanduseSzeno.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class ManageLanduseSzeno  {

     private static final Logger log =
            Logger.getLogger("oms3.model." + ManageLanduseSzeno.class.getSimpleName());

    // Parameter
    @Description("Date to start reduction")
    @Role(PARAMETER)
    @In public java.util.Calendar startReduction;

    @Description("Date to end reduction")
    @Role(PARAMETER)
    @In public java.util.Calendar endReduction;

    @Description("Indicates fertilazation optimization with plant demand")
    @Role(PARAMETER)
    @In public double opti;

    // In
    @Description("Current time")
    @In public java.util.Calendar time;

    @Description("plant groth nitrogen stress factor")
    @In public double nstrs;

    @Description("Reduction Factor for Fertilisation 0 - 10")
    @In public double reductionFactor;
    
    @Description("Mineral nitrogen content in the soil profile down to 60 cm depth")
    @In public double nmin;

    @Description("optimal nitrogen content in Biomass")
    @Unit("kgN/ha")
    @In public double BioNoptAct;

    @Description("actual nitrogen content in Biomass")
    @Unit("kgN/ha")
    @In public double BioNAct;
    
    @Description("Fraction of actual potential heat units sum")
    @In public double FPHUact;


    // Out
    @Description("Rest after former gifts amount of N-fertilizer")
    @Unit("kg/ha*a")
    @Out public double restfert;

    @Description("Type of harvest to distiguish between crops with undersown " +
    "plants and normal harvesting")
    @Out public int harvesttype;

    @Description("Current NH4 fertilizer amount")
    @Out public double fertNH4;

    @Description("Current NO3 fertilizer amount")
    @Out public double fertNO3;

    @Description("Current organic fertilizer amount")
    @Out public double fertorgNactive;

    @Description("Current organic fertilizer amount added to residue pool")
    @Out public double fertorgNfresh;

    @Description("Indicator for harvesting")
    @Out public boolean doHarvest;

    @Description("Fertilisation reduction due to the plant demand routine")
    @Unit("kgN/ha")
    @Out public double Nredu;

    // In Out
    @Description("Current organic fertilizer amount")
    @In @Out  public int rotPos;

    @Description("Current organic fertilizer amount")
    @In @Out  public int managementPos;

    @Description("Plant exisiting or not")
    @In @Out public boolean plantExisting;

    @Description("Minimum counter between 2 fertilizer actions in days " +
    "(only used when opti = 2)")
    @In @Out public double dayintervall;

    @Description("Number of fertilisation action in crop")
    @In @Out public double gift;

    @Description("Current hru object")
    @In @Out public HRU hru;

    double endbioN;
    double bion02;
    double bion04;
    double bion06;
    double bion08;
    double runNredu;

    @Execute
    public void execute() {
        fertNO3 = 0;
        fertNH4 = 0;
        fertorgNactive = 0;
        fertorgNfresh = 0;
        runNredu = 0;
        boolean runplantex = plantExisting;

        ArrayList<J2KSNCrop> rotation = hru.landuseRotation;

        int rotPos = this.rotPos;
        J2KSNCrop currentCrop = rotation.get(rotPos);
        int idc = currentCrop.idc;
        endbioN = currentCrop.endbioN;
        bion02 = currentCrop.bion02;
        bion04 = currentCrop.bion04;
        bion06 = currentCrop.bion06;
        bion08 = currentCrop.bion08;

        if (idc != 1 && idc != 2 && idc != 4 && idc != 5) {
            runplantex = true;
        }

        ArrayList<J2KSNLMArable> managementList = currentCrop.managementList;
        int mgmtPos = this.managementPos;
        J2KSNLMArable currentManagement = managementList.get(mgmtPos);
        int nextDay = currentManagement.jDay;
        doHarvest = false;

//        if (time.get(Calendar.DAY_OF_YEAR) == 140 && hru.ID == 302) {
//            System.out.println("");
//        }

        if ((nextDay - 1) == time.get(Calendar.DAY_OF_YEAR)) {
            if (currentManagement.harvest != -1) {
                //do harvesting here!!
                //System.out.println(" Julianischer Tag  "+ time.get(time.DAY_OF_YEAR));
                doHarvest = true;
            }
        }

        if (nextDay == time.get(Calendar.DAY_OF_YEAR)) {
            if ((mgmtPos + 1) == managementList.size()) {
                managementPos = 0;
                int rotCount = rotation.size();
                rotPos = (rotPos + 1) % rotCount;
                this.rotPos = rotPos;
            } else {
                managementPos = mgmtPos + 1;
            }

            if (currentManagement.till != null) {
                //do tillage processing here!!
            } else if (currentManagement.fert != null) {
                //do fetilization processing here!!
                if ((opti != 2) || ((gift == 0) && (opti == 2)) || (idc != 1 && idc != 2 && idc != 4 && idc != 5)) {
                    processFertilization(currentManagement);
                    restfert = currentCrop.maxfert;
                }
            } else if (currentManagement.plant == true) {
                //do planting here!!
                //PHUact = 0;
                runplantex = true;
            } else if (currentManagement.harvest != -1 &&
                    (idc == 1 || idc == 2 || idc == 4 || idc == 5 || idc == 8)) {
                //do harvesting here!!
                runplantex = false;
                harvesttype = 1;
                if (currentManagement.harvest == 2) {
                    harvesttype = 2;
                    runplantex = true;
                }
            }
        }

        double day = time.get(Calendar.DAY_OF_YEAR);
        if ((opti == 2) && (day > 90.0 && day < 300.0) && (gift > 0) &&
                (idc == 1 || idc == 2 || idc == 4 || idc == 5)) {
            if (nstrs > 0.03 && gift < 4) {
                if (dayintervall < 1) {
                    processFertilizationopti(currentManagement);
                    dayintervall = 30;
                }

            }
        }
        plantExisting = runplantex;
        Nredu = runNredu;
        if (dayintervall > 0) {
            dayintervall = dayintervall - 1;
        }

        if (log.isLoggable(Level.INFO)) {
        }
    }

    private void processFertilization(J2KSNLMArable currentManagement) {
        double run_gift = gift;
        double fertN_total = 0;

        J2KSNFertilizer fert = currentManagement.fert;
        double redu = reductionFactor;

        if (time.after(startReduction) && time.before(endReduction)) {
            redu = Math.max(0, redu);
        } else {
            redu = 1;
        }

        double famount = currentManagement.famount * redu;
        fertN_total = famount * (fert.fminn + fert.forgn);

        //fertilize depending on the demand and N_min in Soil (only for the first gift)
        if (opti == 1 && run_gift == 0.0 || opti == 2 && run_gift == 0.0) {
            double demand_factor = Math.min(Math.sqrt(FPHUact + 0.15), 1);
            double future_demand = (demand_factor * endbioN) - BioNoptAct;
            double actual_demand = BioNoptAct - BioNAct;
            double total_demand = (future_demand + actual_demand) - nmin + 30;

            redu = total_demand / fertN_total;
            if (redu < 0) {
                redu = 0;
            }
            redu = Math.min(redu, 1.0);
            runNredu = (1 - redu) * (fert.forgn + fert.fminn) * famount;
            famount = redu * famount;
            fertN_total = famount * (fert.fminn + fert.forgn);
        }

        run_gift = run_gift + 1;
        gift = run_gift;
        
        double fertNH4N = famount * fert.fminn * fert.fnh4n;
        double fertNO3N = famount * fert.fminn * (1 - fert.fnh4n);
        fertorgNfresh = 0.5 * fert.forgn * famount; // amount of nitrogen in the fresh organic pool added to the soil
        fertorgNactive = 0.5 * famount * fert.forgn; //orgNact is the amount of nitrogen in the active organic pool added to the soil

        fertNO3 = fertNO3N;
        fertNH4 = fertNH4N;
    }

    private void processFertilizationopti(J2KSNLMArable currentManagement) {
        double run_gift = gift;
        double run_restfert = restfert;
        double targetN = 0;
        double fertNH4_ = 0;
        double fertNO3_ = 0;
        double fertNactive = 0;
        double fertNfresh = 0;
        double famount = 0;
        double Namount = 0;

        // Manure        gift = 0       | fert.forgn = 0.03 fert.fminn = 0.01 (0.99 NH4; 0.01 NO3)
        // 15/15/15      gift = 1 & 2    | fert.forgn = 0.00 fert.fminn = 0.15 (0.00 NH4; 1.00 NO3)
        // Urea          gift = 3        | fert.forgn = 0.00 fert.fminn = 0.43 (1.00 NH4; 0.00 NO3)
        if (run_gift == 0) {
            fertNH4_ = 0.01 * 0.99;
            fertNO3_ = 0.01 * 0.01;
            fertNactive = 0.015;
            fertNfresh = 0.015;
        } else if (run_gift < 3) {
            fertNH4_ = 0;
            fertNO3_ = 0.15;
            fertNactive = 0;
            fertNfresh = 0;
        } else if (run_gift == 3) {
            fertNH4_ = 0.43;
            fertNO3_ = 0;
            fertNactive = 0;
            fertNfresh = 0;
        }
        
        double fertN_total = fertNH4_ + fertNO3_ + fertNactive + fertNfresh;

        if (FPHUact > 0 && FPHUact <= 0.2) {
            targetN = (bion02 * FPHUact) / 0.2;
        } else if (FPHUact > 0.2 && FPHUact <= 0.4) {
            targetN = (((bion04 - bion02) * FPHUact) / 0.4) + bion02;
        } else if (FPHUact > 0.4 && FPHUact <= 0.6) {
            targetN = (((bion06 - bion04) * FPHUact) / 0.6) + bion04;
        } else if (FPHUact > 0.6 && FPHUact <= 0.8) {
            targetN = (((bion08 - bion06) * FPHUact) / 0.8) + bion06;
        } else if (FPHUact > 0.8 && FPHUact <= 1) {
            targetN = (((endbioN - bion08) * FPHUact) / 1) + bion08;
        }

        if (BioNoptAct < targetN) {
            endbioN = endbioN - (targetN - BioNoptAct);
        }

        double demand_factor = Math.min(Math.sqrt(FPHUact) + 0.1, 1);
        double future_demand = (demand_factor * endbioN) - BioNoptAct;
        double actual_demand = BioNoptAct - BioNAct;
        double total_demand = (future_demand + actual_demand) - nmin;

        famount = total_demand / fertN_total;

        if (famount < 0) {
            famount = 0;
        }
        Namount = famount * fertN_total;

        if (run_gift > 0) {
            if (Namount < run_restfert) {
            } else {
                Namount = run_restfert;
            }
        }
        run_restfert = run_restfert - Namount;
        famount = Namount / fertN_total;
        if (FPHUact > 0.95) {
            famount = 0;
        }
        run_restfert = Math.max(run_restfert, 0);
        run_gift++;

        gift = run_gift;
        fertNH4 = famount * fertNH4_;
        fertNO3 = famount * fertNO3_;
        fertorgNfresh = famount * fertNfresh;    // amount of nitrogen in the fresh organic pool added to the soil
        fertorgNactive = famount * fertNactive;  //orgNact is the amount of nitrogen in the active organic pool added to the soil
        restfert = run_restfert;
    }

    public static void main(String[] args) {
        oms3.util.Components.explore(new ManageLanduseSzeno());
    }
}
