

package org.jams.j2k.s_n.wq.ProcessRates;

import java.io.*;
import jams.data.*;
import jams.model.*;
import java.util.HashMap;
import org.jams.j2k.s_n.wq.ProcessRates.Table2D.NoSuchEntryException;


@JAMSComponentDescription(
        title = "ProcessRates",
        author = "Marcel Wetzel",
        description = "Converts the specific concentration (g/l) from process equation to the load (substance (kg)) for routing",
        version="1.0_0",
        date="2011-03-03"
        )

public class Post_ProcessRates extends JAMSComponent {

    /*
     *  Component variables
     */

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1_N (SurfaceN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD1_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2_N ((fast) InterflowN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD2_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1_N ((slow) InterflowN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG1_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2_N (GoundwaterN) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG2_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection",
            unit = "-"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of heterotrophic organisms",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_H;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of 1st stage nitrifiers",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of 2nd stage nitrifiers",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "algae and macrophytes",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "particulate organic material",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "inert particulate organic material",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_I;
   
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved organic substances for rapid biodegradation",
            unit = "mgSS/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_O2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen concentration in water body",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble disOxy;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH4-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NH4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH3-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_NH3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO2-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO3-N",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "HPO4-P part of inorganic dissolved phosphorus",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_HPO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "H2PO4-P part of inorganic dissolved phosphorus",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H2PO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Hydrogen ions H^+",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "pH-Value",
            unit = "",
            lowerBound= 0,
            upperBound = 14
            )
            public JAMSDouble pH;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "reach total Q from storage and output to destination reach",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Sum_Q;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "specific substance in Reach",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Substance;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1_Part of specific substance of reach storage",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble ActRD1_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2_Part of specific substance in reach storage",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble ActRD2_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1_Part of specific substance in reach storage",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble ActRG1_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2_Part of specific substance in reach storage",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble ActRG2_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1_Part of specific substance in outflow to destination reach",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble RD1DestIn_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2_Part of specific substance in outflow to destination reach",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble RD2DestIn_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1_Part of specific substance in outflow to destination reach",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble RG1DestIn_N_Part;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2_Part of specific substance in outflow to destination reach",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble RG2DestIn_N_Part;


    
      
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException, IOException, NoSuchEntryException {
        Attribute.Entity entity = entities.getCurrent();
        JAMSEntity DestReach = (JAMSEntity) entity.getObject("to_reach");
        
        double RD1DestIn_N, RD2DestIn_N, RG1DestIn_N, RG2DestIn_N;
        double Sum_Sub;
                
       // Umwandeln der Konzentrationen in Frachten (kg) entsprechend der Anteile der Abflusskomponenten
        
        Sum_Sub = Substance.getValue() * Sum_Q.getValue() / 1000;
        RD1DestIn_N = RD1DestIn_N_Part.getValue() * Sum_Sub;
        RD2DestIn_N = RD2DestIn_N_Part.getValue() * Sum_Sub;
        RG1DestIn_N = RG1DestIn_N_Part.getValue() * Sum_Sub;
        RG2DestIn_N = RG2DestIn_N_Part.getValue() * Sum_Sub;
               
        /*  Ueberschreiben der aus dem routing-Modul geholten Frachten nach dem Durchlaufen der Prozessgleichungen
         *  Diese Frachten werden dann wieder vom routing-Module im naechsten Zeitschritt verwendet  
         */
        
        DestReach.setDouble("SurfaceN_in", RD1DestIn_N);
        DestReach.setDouble("InterflowN_sum", RD2DestIn_N);
        DestReach.setDouble("N_RG1_in", RG1DestIn_N);
        DestReach.setDouble("N_RG2_in", RG2DestIn_N);
        ActRD1_N.setValue(ActRD1_N_Part.getValue() * Sum_Sub);
        ActRD2_N.setValue(ActRD2_N_Part.getValue() * Sum_Sub);
        ActRG1_N.setValue(ActRG1_N_Part.getValue() * Sum_Sub);
        ActRG2_N.setValue(ActRG2_N_Part.getValue() * Sum_Sub);
        

    }

     public void cleanup() {

    }

    


     

       
   

}


    
