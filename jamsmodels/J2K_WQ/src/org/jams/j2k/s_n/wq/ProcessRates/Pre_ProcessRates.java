

package org.jams.j2k.s_n.wq.ProcessRates;

import java.io.*;
import jams.data.*;
import jams.model.*;
import java.util.HashMap;
import org.jams.j2k.s_n.wq.ProcessRates.Table2D.NoSuchEntryException;


@JAMSComponentDescription(
        title = "ProcessRates",
        author = "Marcel Wetzel",
        description = "Converts the specific load (substance (kg)) from routing in concentration (g/l) for process equation",
        version="1.0_0",
        date="2012-10-04"
        )

public class Pre_ProcessRates extends JAMSComponent {

    /*
     *  Component variables
     */

   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble actRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar RD1_N (for specific substance) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD1_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2_N (for specific substance) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD2_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1_N (for specific substance) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG1_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2_N (for specific substance) storage",
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
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_H;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of 1st stage nitrifiers",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "biomass of 2nd stage nitrifiers",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_N2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "algae and macrophytes",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_ALG;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "particulate organic material",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "inert particulate organic material",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble X_I;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved organic substances for rapid biodegradation",
            unit = "gSS/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_S;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_O2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "dissolved oxygen concentration in water body",
            unit = "g/l",
            lowerBound= 0,
            upperBound = 30
            )
            public JAMSDouble disOxy;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH4-N",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NH4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NH3-N",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_NH3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO2-N",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "NO3-N",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_NO3;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "HPO4-P part of inorganic dissolved phosphorus",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble S_HPO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "H2PO4-P part of inorganic dissolved phosphorus",
            unit = "g/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY,
            defaultValue = "0"
            )
            public JAMSDouble S_H2PO4;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "Hydrogen ions H^+",
            unit = "g/l",
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

    
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "load of specific substance in Reach",
            unit = "g/l",
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
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "reach total Q from storage and output to destination reach",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Sum_Q;

    
      
    /*
     *  Component run stages
     */


    public void init() throws JAMSEntity.NoSuchAttributeException {
    }

    public void run() throws JAMSEntity.NoSuchAttributeException, IOException, NoSuchEntryException {
        Attribute.Entity entity = entities.getCurrent();
        JAMSEntity DestReach = (JAMSEntity) entity.getObject("to_reach");
        
        /*  Einlesen der Frachten aus dem routing-Modul (Input) zum Kombinieren
         *  mit den Anteilen des Speicherinhalts aller vier Abflusskomponenten
         *  (z. B. SurfaceN_in + ActRD1_N = RD1_N)
         */
        
        double RD1DestIn, RD2DestIn, RG1DestIn, RG2DestIn;
                
        RD1DestIn = DestReach.getDouble("inRD1");
        RD2DestIn = DestReach.getDouble("inRD2");
        RG1DestIn = DestReach.getDouble("inRG1");
        RG2DestIn = DestReach.getDouble("inRG2");
        
        double RD1DestIn_N, RD2DestIn_N, RG1DestIn_N, RG2DestIn_N;
        
        RD1DestIn_N = DestReach.getDouble("SurfaceN_in");
        RD2DestIn_N = DestReach.getDouble("InterflowN_sum");
        RG1DestIn_N = DestReach.getDouble("N_RG1_in");
        RG2DestIn_N = DestReach.getDouble("N_RG2_in");
            
        double Sum_Sub, Conc_Sub;
        
        // Bestimmen der Gesamtstofffracht (Sum_Sub (kg)) und des Gesamtabflusses (Sum_Q (liter))
        // sowie der Gesamtkonzentration (Conc_Sub (g/l))
        Sum_Sub = RD1DestIn_N + RD2DestIn_N + RG1DestIn_N + RG2DestIn_N + ActRD1_N.getValue() + ActRD2_N.getValue() + ActRG1_N.getValue() + ActRG2_N.getValue();
        Sum_Q.setValue(RD1DestIn + RD2DestIn + RG1DestIn + RG2DestIn + actRD1.getValue() + actRD2.getValue() + actRG1.getValue() + actRG2.getValue());
        Conc_Sub = Sum_Sub * 1000 / Sum_Q.getValue();
        
        // Bestimmen der Anteile der einzelnen Abflusskomponenten f?r die erneute Aufteilung nach dem
        // Durchlaufen der Prozessgleichungen
        
        RD1DestIn_N_Part.setValue(RD1DestIn_N/Sum_Sub);
        RD2DestIn_N_Part.setValue(RD1DestIn_N/Sum_Sub);
        RG1DestIn_N_Part.setValue(RD1DestIn_N/Sum_Sub);
        RG2DestIn_N_Part.setValue(RD1DestIn_N/Sum_Sub);
        ActRD1_N_Part.setValue(ActRD1_N.getValue()/Sum_Sub);
        ActRD2_N_Part.setValue(ActRD2_N.getValue()/Sum_Sub);
        ActRG1_N_Part.setValue(ActRG1_N.getValue()/Sum_Sub);
        ActRG2_N_Part.setValue(ActRG2_N.getValue()/Sum_Sub);
        
        // Uebergabe der Konzentration (g/l) an die jeweilige Prozessgleichung
        Substance.setValue(Conc_Sub);
        
        
    }

     public void cleanup() {

    }

    


     

       
   

}


    
