/*
 * ManageLanduse.java
 *
 * Created on 16. März 2006, 13:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.management;

import java.util.ArrayList;
import org.jams.j2k.s_n.crop.*;
import jams.model.*;
import jams.data.*;
import java.util.HashMap;

/**
 *
 * @author c5ulbe
 */
public class ManageLanduse_gui extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current hru object")
    public JAMSEntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current time")
    public JAMSCalendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current NH4 fertilizer amount")
    public JAMSDouble fertNH4N;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current NO3 fertilizer amount")
    public JAMSDouble fertNO3N;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current organic fertilizer amount")
    public JAMSDouble fertorgNactive;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current organic fertilizer amount added to residue pool")
    public JAMSDouble fertorgNfresh;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Reduction Factor for Fertilisation 0 - 10 [-]")
    public JAMSDouble ReductionFactor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current organic fertilizer amount")
    public JAMSInteger RotPos;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Current organic fertilizer amount")
    public JAMSInteger ManagementPos;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Plant exisiting or not")
    public JAMSBoolean plantExisting;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Indicator for harvesting")
    public JAMSBoolean doHarvest;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Date to start reduction")
    public JAMSCalendar start;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Date to end reduction")
    public JAMSCalendar end;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "Encapsulating time interval")
    public JAMSTimeInterval timeInterval;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Indicates fertilazation optimization with plant demand")
    public JAMSDouble opti;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Mineral nitrogen content in the soil profile down to 60 cm depth")
    public JAMSDouble nmin;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "optimal nitrogen content in Biomass in (kgN/ha)")
    public JAMSDouble optibioN;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "actual nitrogen content in Biomass in (kgN/ha)")
    public JAMSDouble actbioN;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Fraction of actual potential heat units sum [-]")
    public JAMSDouble FPHUact;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Fertilisation reduction due to the plant demand routine [kgN/ha]")
    public JAMSDouble Nredu;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Number of fertilisation action in crop [-]")
    public JAMSDouble gift;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Maximum amount of N-fertilizer in [kg/ha*a]")
    public JAMSDouble maxfert;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Rest after former gifts amount of N-fertilizer in [kg/ha*a]")
    public JAMSDouble restfert;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "in [-] plant groth nitrogen stress factor")
    public JAMSDouble nstrs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Minimum counter between 2 fertilizer actions in days (only used when opti = 2)")
    public JAMSDouble Dayintervall;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "Type of harvest to distiguish between crops with undersown plants and normal harvesting")
    public JAMSInteger harvesttype;
    private JAMSTimeInterval ti;
    double endbioN;
    double bion02;
    double bion04;
    double bion06;
    double bion08;
    double runNredu;

    public void init() {
        ti = new JAMSTimeInterval(start, end, timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());

    }

    public void run() throws JAMSEntity.NoSuchAttributeException {
        double dayintervall = Dayintervall.getValue();
        Attribute.Entity entity = entities.getCurrent();
        this.fertNO3N.setValue(0);
        this.fertNH4N.setValue(0);
        this.fertorgNactive.setValue(0);
        this.fertorgNfresh.setValue(0);
        boolean runplantex = false;
        this.runNredu = 0;
        runplantex = plantExisting.getValue();

        ArrayList<ManagementOption> rotation = (ArrayList<ManagementOption>) entity.getObject("landuseRotation");
        //ArrayList<J2KSNCrop_gui> rotation = (ArrayList<J2KSNCrop_gui>) entity.getObject("landuseRotation");
        int rotPos = RotPos.getValue();
        ManagementOption managemento = rotation.get(rotPos);
        if (managemento instanceof ManagementGap) {
            //1. Fall: Lücke
            int d = ((ManagementGap) managemento).getDays();
            rotPos++;
        } else {
            J2KSNManagentType curentmana = (J2KSNManagentType) managemento;
            ArrayList<J2KSNLMArable_gui> managementList = curentmana.getmList();
            int managementPos = ManagementPos.getValue();
            J2KSNLMArable_gui curpos = managementList.get(managementPos);
            J2KSNCrop_gui currentCrop = curpos.crop;
            //J2KSNCrop_gui currentCrop = rotation.get(rotPos);

            int idc = currentCrop.idc;
            this.endbioN = currentCrop.endbioN;
            this.bion02 = currentCrop.bion02;
            this.bion04 = currentCrop.bion04;
            this.bion06 = currentCrop.bion06;
            this.bion08 = currentCrop.bion08;
            if (idc != 1 && idc != 2 && idc != 4 && idc != 5) {
                runplantex = true;
            }

            //ArrayList<J2KSNLMArable_gui> managementList = currentCrop.managementList;

            J2KSNLMArable_gui currentManagement = managementList.get(managementPos);

            int nextDay = currentManagement.jDay;
            doHarvest.setValue(false);

//            System.out.println("da" + nextDay + time.get(JAMSCalendar.DAY_OF_YEAR));

            if ((nextDay - 1) == time.get(Attribute.Calendar.DAY_OF_YEAR)) {
                if (currentManagement.harvest != -1) {
                    //do harvesting here!!
                    //System.out.println(" Julianischer Tag  "+ time.get(time.DAY_OF_YEAR));
                    doHarvest.setValue(true);
                }
            }

            if (nextDay == time.get(Attribute.Calendar.DAY_OF_YEAR)) {

                if ((managementPos + 1) == managementList.size()) {
                    ManagementPos.setValue(0);
                    int rotCount = rotation.size();
                    rotPos = (rotPos + 1) % rotCount;
                    RotPos.setValue(rotPos);
                } else {
                    ManagementPos.setValue(managementPos + 1);
                }

                if (currentManagement.till != null) {
                    //do tillage processing here!!
                } else if (currentManagement.fert != null) {
                    //do fetilization processing here!!
                    if ((opti.getValue() != 2) || ((gift.getValue() == 0) && (opti.getValue() == 2)) || (idc != 1 && idc != 2 && idc != 4 && idc != 5)) {
                        processFertilization(currentManagement);
                        restfert.setValue(currentCrop.maxfert);
                    }
                } else if (currentManagement.plant == true) {
                    //do planting here!!
                    //PHUact.setValue(0);
                    runplantex = true;
                } else if (currentManagement.harvest != -1 && (idc == 1 || idc == 2 || idc == 4 || idc == 5 || idc == 8)) {
                    //do harvesting here!!
                    runplantex = false;
                    harvesttype.setValue(1);
                    if (currentManagement.harvest == 2) {
                        harvesttype.setValue(2);
                        runplantex = true;
                    }
                }
            }

            double day = time.get(Attribute.Calendar.DAY_OF_YEAR);

            if ((opti.getValue() == 2) && (day > 90.0 && day < 300.0) && (gift.getValue() > 0) && (idc == 1 || idc == 2 || idc == 4 || idc == 5)) {
                if (nstrs.getValue() > 0.03 && gift.getValue() < 4) {
                    if (dayintervall < 1) {
                        processFertilizationopti(currentManagement);
                        dayintervall = 30;
                    }

                }
            }

            plantExisting.setValue(runplantex);
            this.Nredu.setValue(runNredu);
            if (dayintervall > 0) {
                dayintervall = dayintervall - 1;
            }
            Dayintervall.setValue(dayintervall);
        }
    }

    protected void processFertilization(J2KSNLMArable_gui currentManagement) {
        double run_gift = gift.getValue();
        double fertN_total = 0;


        J2KSNFertilizer fert = currentManagement.fert;

        double redu = ReductionFactor.getValue();

        if (time.after(ti.getStart()) && time.before(ti.getEnd())) {

            redu = Math.max(0, redu);
        } else {
            redu = 1;
        }

        double famount = currentManagement.famount * redu;



        fertN_total = famount * (fert.fminn + fert.forgn);

        //fertilasation in dependence of the demand and N_min in Soil (only for the first gift)

        if (opti.getValue() == 1 && run_gift == 0.0 || opti.getValue() == 2 && run_gift == 0.0) {





            double demand_factor = Math.min(Math.sqrt(FPHUact.getValue() + 0.15), 1);
            double future_demand = (demand_factor * endbioN) - optibioN.getValue();
            double actual_demand = optibioN.getValue() - actbioN.getValue();
            double total_demand = (future_demand + actual_demand) - nmin.getValue() + 30;


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
        gift.setValue(run_gift);
        double fertNH4N = famount * fert.fminn * fert.fnh4n;
        double fertNO3N = famount * fert.fminn * (1 - fert.fnh4n);
        double fertorgNfresh = 0.5 * fert.forgn * famount; // amount of nitrogen in the fresh organic pool added to the soil
        double fertorgNactive = 0.5 * famount * fert.forgn; //orgNact is the amount of nitrogen in the active organic pool added to the soil





        /*
         * if (fertorgN > 0 || fertNO3N > 0 || fertNH4N > 0) {
         * System.out.println("Gebe die Düngemengen aus :" + fertNO3N +
         * fertorgN + fertNH4N ); }
         */
        /*
         * double fertN03 = fertNO3_current + fertNO3_old; double fertNH4 =
         * fertNH4_current + fertNH4_old; double fertorg = fertorg_current + fertorg_old;
         */


        this.fertNO3N.setValue(fertNO3N);
        this.fertNH4N.setValue(fertNH4N);
        this.fertorgNfresh.setValue(fertorgNfresh);
        this.fertorgNactive.setValue(fertorgNactive);
    }

    protected void processFertilizationopti(J2KSNLMArable_gui currentManagement) {
        double run_gift = gift.getValue();
        double run_restfert = restfert.getValue();
        double targetN = 0;

        double fertN_total = 0;
        double fertNH4 = 0;
        double fertNO3 = 0;
        double fertNactive = 0;
        double fertNfresh = 0;
        double famount = 0;
        double Namount = 0;
        double actsollbio = 0;

        // Rindergülle   gift = 0        | fert.forgn = 0.03 fert.fminn = 0.01 (0.99 NH4; 0.01 NO3)
        // 15/15/15      gift = 1 & 2    | fert.forgn = 0.00 fert.fminn = 0.15 (0.00 NH4; 1.00 NO3)
        // Urea          gift = 3        | fert.forgn = 0.00 fert.fminn = 0.43 (1.00 NH4; 0.00 NO3)


        if (run_gift == 0) {

            fertNH4 = 0.01 * 0.99;
            fertNO3 = 0.01 * 0.01;
            fertNactive = 0.015;
            fertNfresh = 0.015;

        } else if (run_gift < 3) {

            fertNH4 = 0;
            fertNO3 = 0.15;
            fertNactive = 0;
            fertNfresh = 0;

        } else if (run_gift == 3) {

            fertNH4 = 0.43;
            fertNO3 = 0;
            fertNactive = 0;
            fertNfresh = 0;
        }

        fertN_total = fertNH4 + fertNO3 + fertNactive + fertNfresh;



        if (FPHUact.getValue() > 0 && FPHUact.getValue() <= 0.2) {
            targetN = (this.bion02 * FPHUact.getValue()) / 0.2;
        } else if (FPHUact.getValue() > 0.2 && FPHUact.getValue() <= 0.4) {
            targetN = (((this.bion04 - this.bion02) * FPHUact.getValue()) / 0.4) + this.bion02;
        } else if (FPHUact.getValue() > 0.4 && FPHUact.getValue() <= 0.6) {
            targetN = (((this.bion06 - this.bion04) * FPHUact.getValue()) / 0.6) + this.bion04;
        } else if (FPHUact.getValue() > 0.6 && FPHUact.getValue() <= 0.8) {
            targetN = (((this.bion08 - this.bion06) * FPHUact.getValue()) / 0.8) + this.bion06;
        } else if (FPHUact.getValue() > 0.8 && FPHUact.getValue() <= 1) {
            targetN = (((this.endbioN - this.bion08) * FPHUact.getValue()) / 1) + this.bion08;
        }

        if (optibioN.getValue() < targetN) {
            endbioN = endbioN - (targetN - optibioN.getValue());
        }


        double demand_factor = Math.min(Math.sqrt(FPHUact.getValue()) + 0.1, 1);
        double future_demand = (demand_factor * endbioN) - optibioN.getValue();

        double actual_demand = optibioN.getValue() - actbioN.getValue();
        double total_demand = (future_demand + actual_demand) - nmin.getValue();



        famount = total_demand / fertN_total;

        if (famount < 0) {
            famount = 0;
        }
        Namount = famount * fertN_total;

        if (run_gift > 0) {

            if (Namount < run_restfert) {
                Namount = Namount;
            } else {
                Namount = run_restfert;
            }


        } else {
        }

        run_restfert = run_restfert - Namount;
        famount = Namount / fertN_total;


        if (FPHUact.getValue() > 0.95) {
            famount = 0;
        }




        run_restfert = Math.max(run_restfert, 0);
        run_gift = run_gift + 1;
        gift.setValue(run_gift);
        double fertNH4N = famount * fertNH4;
        double fertNO3N = famount * fertNO3;
        double fertorgNfresh = famount * fertNfresh; // amount of nitrogen in the fresh organic pool added to the soil
        double fertorgNactive = famount * fertNactive; //orgNact is the amount of nitrogen in the active organic pool added to the soil





        /*
         * if (fertorgN > 0 || fertNO3N > 0 || fertNH4N > 0) {
         * System.out.println("Gebe die Düngemengen aus :" + fertNO3N +
         * fertorgN + fertNH4N ); }
         */
        /*
         * double fertN03 = fertNO3_current + fertNO3_old; double fertNH4 =
         * fertNH4_current + fertNH4_old; double fertorg = fertorg_current + fertorg_old;
         */

        this.restfert.setValue(run_restfert);
        this.fertNO3N.setValue(fertNO3N);
        this.fertNH4N.setValue(fertNH4N);
        this.fertorgNfresh.setValue(fertorgNfresh);
        this.fertorgNactive.setValue(fertorgNactive);
    }
}
