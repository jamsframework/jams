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
public class ManageLanduse extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current hru object"
            )
            public Attribute.EntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
            )
            public Attribute.Calendar time;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current NH4 fertilizer amount"
            )
            public Attribute.Double fertNH4N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current NO3 fertilizer amount"
            )
            public Attribute.Double fertNO3N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current organic fertilizer amount"
            )
            public Attribute.Double fertorgNactive;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current organic fertilizer amount"
            )
            public Attribute.Double fertorgNfresh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reduction Factor for Fertilisation 0 - 10 [-]"
            )
            public Attribute.Double ReductionFactor;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current organic fertilizer amount"
            )
            public Attribute.Integer RotPos;
    
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Current organic fertilizer amount"
            )
            public Attribute.Integer ManagementPos;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Plant exisiting or not"
            )
            public Attribute.Boolean plantExisting;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Indicator for harvesting"
            )
            public Attribute.Boolean doHarvest;
    
    
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "actual potential heat units sum [-]"
            )
            public Attribute.Double PHUact; */
    
    
    public void run() {
        
        Attribute.Entity entity = entities.getCurrent();
        this.fertNO3N.setValue(0);
        this.fertNH4N.setValue(0);
        this.fertorgNactive.setValue(0);
        this.fertorgNfresh.setValue(0);
        
        
        ArrayList<J2KSNCrop> rotation = (ArrayList<J2KSNCrop>) entity.getObject("landuseRotation");
        int rotPos = RotPos.getValue();
        J2KSNCrop currentCrop = rotation.get(rotPos);
        int idc = currentCrop.idc;
        
        if (idc != 1 && idc != 2 && idc != 4 && idc != 5){
          this.plantExisting.setValue(true);  
        }
            
        ArrayList<J2KSNLMArable> managementList = currentCrop.managementList;
        int managementPos = ManagementPos.getValue();
        J2KSNLMArable currentManagement = managementList.get(managementPos);
        
        int nextDay = currentManagement.jDay;
        doHarvest.setValue(false);
        
//            System.out.println("da" + nextDay + time.get(Attribute.Calendar.DAY_OF_YEAR));
        
        if ((nextDay-1) == time.get(Attribute.Calendar.DAY_OF_YEAR)) {
            if (currentManagement.harvest != -1) {
                //do harvesting here!!
                //System.out.println(" Julianischer Tag  "+ time.get(time.DAY_OF_YEAR));
                doHarvest.setValue(true);
            }
        }
        
        if (nextDay == time.get(Attribute.Calendar.DAY_OF_YEAR)) {
            
            if ((managementPos+1) ==  managementList.size()) {
                ManagementPos.setValue(0);
                int rotCount = rotation.size();
                rotPos = (rotPos+1) % rotCount;
                RotPos.setValue(rotPos);
            } else {
                ManagementPos.setValue(managementPos+1);
            }
            
            if (currentManagement.till != null) {
                //do tillage processing here!!
            } else if (currentManagement.fert != null) {
                //do fetilization processing here!!
                processFertilization(currentManagement);
            } else if (currentManagement.plant == true) {
                //do planting here!!
                //PHUact.setValue(0);
                this.plantExisting.setValue(true);
            } else if (currentManagement.harvest != -1 && (idc == 1 || idc == 2 || idc == 4 || idc == 5 ) ) {
                //do harvesting here!!
                this.plantExisting.setValue(false);
                
            }
        }
       
    }
    
    private void processFertilization(J2KSNLMArable currentManagement) {
        J2KSNFertilizer fert = currentManagement.fert;
        
        double redu = ReductionFactor.getValue();
         redu = Math.max(0,redu);           
        
        
        double famount = currentManagement.famount * redu;
        
        /*double fertNO3_old = this.fertNO3.getValue();
        double fertNH4_old = this.fertNH4.getValue();
        double fertorg_old = this.fertorg.getValue();*/
        
        double fertN_total = famount * fert.fminn;
        double fertNH4N = fertN_total * fert.fnh4n;
        double fertNO3N = fertN_total - fertNH4N;
        double fertorgNfresh = 0.5 * fert.forgn * famount; // amount of nitrogen in the fresh organic pool added to the soil
        double fertorgNactive = 0.5 * famount * fert.forgn; //orgNact is the amount of nitrogen in the active organic pool added to the soil
        
        
       /* if (fertorgN > 0 || fertNO3N > 0 || fertNH4N > 0) {
        System.out.println("Gebe die Düngemengen aus :"  + fertNO3N + fertorgN + fertNH4N );
        } */
        /*double fertN03 = fertNO3_current + fertNO3_old;
        double fertNH4 = fertNH4_current + fertNH4_old;
        double fertorg = fertorg_current + fertorg_old;*/
        
        
        this.fertNO3N.setValue(fertNO3N);
        this.fertNH4N.setValue(fertNH4N);
        this.fertorgNfresh.setValue(fertorgNfresh);
        this.fertorgNactive.setValue(fertorgNactive);
    }
    
}
