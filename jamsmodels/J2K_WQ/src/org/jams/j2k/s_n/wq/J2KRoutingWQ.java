

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="J2KNRouting",
        author="Peter Krause & Manfred Fink & Marcel Wetzel",
        description="Passes the N output and energy demand of the entities as input to the respective reach or unit",
        version="1.0_0",
        date="2010-12-12"
        )
        public class J2KRoutingWQ extends JAMSComponent {
    
    /*
     *  Component variables
     */

        @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The current hru entity",
            unit = "-"
            )
            public JAMSEntityCollection entities;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Collection of reach objects",
            unit = "-"
            )
            public JAMSEntityCollection reaches;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 inflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRD1_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 inflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRD2_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 inflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRG1_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 inflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRG2_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 N inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SurfaceN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble InterflowN_in;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG1_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG2_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 outflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RD1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 outflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RD2_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 outflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RG1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 outflow energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RG2_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD1 N outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SurfaceNabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble InterflowNabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG1 N outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RG2 N outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG2_out;
    
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {

    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity entity = entities.getCurrent();
        //receiving polygon
        JAMSEntity toPoly = (JAMSEntity) entity.getObject("to_poly");
        //receiving reach
        JAMSEntity toReach = (JAMSEntity) entity.getObject("to_reach");

        double EnergyRD1out = Energy_RD1_out.getValue();
        double EnergyRD2out = Energy_RD2_out.getValue();
        double EnergyRG1out = Energy_RG1_out.getValue();
        double EnergyRG2out = Energy_RG2_out.getValue();

        double NRD1out = SurfaceNabs.getValue();
        double NRD2out = InterflowNabs.getValue();
        double NRG1out = N_RG1_out.getValue();
        double NRG2out = N_RG2_out.getValue();
//        System.out.println("NRD2out: " + NRD2out);
        
        if(toPoly != null){

            double EnergyRD1in = toReach.getDouble("EnergyRD1_in");
            double EnergyRD2in = toReach.getDouble("EnergyRD2_in");
            double EnergyRG1in = toReach.getDouble("EnergyRG1_in");
            double EnergyRG2in = toReach.getDouble("EnergyRG2_in");

            double NRD1in = toPoly.getDouble("SurfaceN_in");
            double NRD2in = toPoly.getDouble("InterflowN_in");
            double NRG1in = toPoly.getDouble("N_RG1_in");
            double NRG2in = toPoly.getDouble("N_RG2_in");
//            double NRG1in = 0;
//            double NRG2in = 0;

            EnergyRD1in = EnergyRD1in + EnergyRD1out;
            EnergyRD2in = EnergyRD2in + EnergyRD2out;
            EnergyRG1in = EnergyRG1in + EnergyRG1out;
            EnergyRG2in = EnergyRG2in + EnergyRG2out;

            EnergyRD1out = 0;
            EnergyRD2out = 0;
            EnergyRG1out = 0;
            EnergyRG2out = 0;

            NRD1in = NRD1in + NRD1out;
            NRD2in = NRD2in + NRD2out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
                  
            NRD1out = 0;
            NRD2out = 0;
            NRG1out = 0;
            NRG2out = 0;

            Energy_RD1_out.setValue(0);
            Energy_RD2_out.setValue(0);
            Energy_RG1_out.setValue(0);
            Energy_RG2_out.setValue(0);

            EnergyRD1_in.setValue(0);
            EnergyRD2_in.setValue(0);
            EnergyRG1_in.setValue(0);
            EnergyRG2_in.setValue(0);

            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
           
            SurfaceN_in.setValue(0);
            InterflowN_in.setValue(0);
            N_RG1_in.setValue(0);
            N_RG2_in.setValue(0);

            toPoly.setDouble("EnergyRD1_in",EnergyRD1in);
            toPoly.setDouble("EnergyRD2_in",EnergyRD2in);
            toPoly.setDouble("EnergyRG1_in",EnergyRG1in);
            toPoly.setDouble("EnergyRG2_in",EnergyRG2in);

            toPoly.setDouble("SurfaceN_in",NRD1in);
            toPoly.setDouble("InterflowN_in", NRD2in);
            toPoly.setDouble("N_RG1_in", NRG1in);
            toPoly.setDouble("N_RG2_in", NRG2in);
        } else if(toReach != null){

            double NRD1in = toReach.getDouble("SurfaceN_in");
            double NRD2in = toReach.getDouble("InterflowN_in");
            double NRG1in = toReach.getDouble("N_RG1_in");
            double NRG2in = toReach.getDouble("N_RG2_in");

            double EnergyRD1in = toReach.getDouble("EnergyRD1_in");
            double EnergyRD2in = toReach.getDouble("EnergyRD2_in");
            double EnergyRG1in = toReach.getDouble("EnergyRG1_in");
            double EnergyRG2in = toReach.getDouble("EnergyRG2_in");

            EnergyRD1in = EnergyRD1in + EnergyRD1out;
            EnergyRD2in = EnergyRD2in + EnergyRD2out;
            EnergyRG1in = EnergyRG1in + EnergyRG1out;
            EnergyRG2in = EnergyRG2in + EnergyRG2out;

            EnergyRD1out = 0;
            EnergyRD2out = 0;
            EnergyRG1out = 0;
            EnergyRG2out = 0;

            NRD1in = NRD1in + NRD1out;
            NRD2in = NRD2in + NRD2out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            NRD2out = 0;
            NRG1out = 0;
            NRG2out = 0;
            
            Energy_RD1_out.setValue(0);
            Energy_RD2_out.setValue(0);
            Energy_RG1_out.setValue(0);
            Energy_RG2_out.setValue(0);

            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);

            Energy_RD1_out.setValue(EnergyRD1out);
            toReach.setDouble("EnergyRD1_in", EnergyRD1in);
            Energy_RD2_out.setValue(EnergyRD2out);
            toReach.setDouble("EnergyRD2_in", EnergyRD2in);
            Energy_RG1_out.setValue(EnergyRG1out);
            toReach.setDouble("EnergyRG1_in", EnergyRG1in);
            Energy_RG2_out.setValue(EnergyRG2out);
            toReach.setDouble("EnergyRG2_in", EnergyRG2in);

            SurfaceNabs.setValue(NRD1out);
            toReach.setDouble("SurfaceN_in", NRD1in);
            InterflowNabs.setValue(NRD2out);
            toReach.setDouble("InterflowN_in", NRD2in);
            N_RG1_out.setValue(NRG1out);
            toReach.setDouble("N_RG1_in", NRG1in);
            N_RG2_out.setValue(NRG2out);
            toReach.setDouble("N_RG2_in", NRG2in);
            
        } else{
            System.out.println("Current entity ID: " + entity.getInt("ID") + " has no receiver.");
        }
        
    }
    
    public void cleanup() {
        
    }
}
