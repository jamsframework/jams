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

/**
 *
 * @author c5ulbe
 */
public class ManageLanduse_gw extends ManageLanduse_szeno {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Type of harvest to distiguish between crops with undersown plants and normal harvesting")
    public Attribute.Double Irrigation_mm;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "attribute area",
    unit = "m^2")
    public Attribute.Double area;

    public void run() {
        
        double dayintervall = Dayintervall.getValue();
        Attribute.Entity entity = entities.getCurrent();
        this.fertNO3N.setValue(0);
        this.fertNH4N.setValue(0);
        this.fertorgNactive.setValue(0);
        this.fertorgNfresh.setValue(0);
        Irrigation_mm.setValue(0);
        boolean runplantex = false;
        this.runNredu = 0;
        runplantex = plantExisting.getValue();
        ArrayList<J2KSNCrop> rotation = (ArrayList<J2KSNCrop>) entity.getObject("landuseRotation");
        int rotPos = RotPos.getValue();
        J2KSNCrop currentCrop = rotation.get(rotPos);
        int idc = currentCrop.idc;
        this.endbioN = currentCrop.endbioN;
        this.bion02 = currentCrop.bion02;
        this.bion04 = currentCrop.bion04;
        this.bion06 = currentCrop.bion06;
        this.bion08 = currentCrop.bion08;
        if (idc != 1 && idc != 2 && idc != 4 && idc != 5) {
            runplantex = true;
        }

        ArrayList<J2KSNLMArable> managementList = currentCrop.managementList;
        int managementPos = ManagementPos.getValue();
        J2KSNLMArableGW currentManagement = (J2KSNLMArableGW) managementList.get(managementPos);

        int nextDay = currentManagement.jDay;
        doHarvest.setValue(false);

//            System.out.println("da" + nextDay + time.get(Attribute.Calendar.DAY_OF_YEAR));

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
            } else if (currentManagement.Irrigation != -1) {
                //do irrigation here!!
                Irrigation_mm.setValue(currentManagement.Irrigation * area.getValue());

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
