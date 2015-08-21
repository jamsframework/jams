

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="J2KRoutinglayerWQ",
        author="Peter Krause & Manfred Fink & Marcel Wetzel",
        description="Passes the N output and energy amount of the entities as input to the respective reach or unit",
        version="1.0_0",
        date="2010-12-12"
        )
        public class J2KRoutinglayerWQ extends JAMSComponent {
    
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
            public JAMSDoubleArray InterflowN_in;
    
/*    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "RD2 N inflow lumped",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble InterflowN_sum;
 */
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
            public JAMSDoubleArray InterflowNabs;
    
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
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "gwExcess",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble NExcess;
    
    double[][] fracOut;
    double[] percNOut;
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
        double[] NRD2out_h = InterflowNabs.getValue();
        double NRG1out = N_RG1_out.getValue();
        double NRG2out = N_RG2_out.getValue();
        
        double reachNRD2in = 0;
//        System.out.println("NRD2out: " + NRD2out);
        
       if(toPoly.isEmpty()){
            double[] srcDepth = ((JAMSDoubleArray)entity.getObject("depth_h")).getValue();
            double[] recDepth = ((JAMSDoubleArray)toPoly.getObject("depth_h")).getValue();
            int srcHors = srcDepth.length;
            int recHors = recDepth.length;
            double[] NRD2in_h = new double[recHors];
            //this.calcParts(srcDepth, recDepth);
            
            double NRD1in = toPoly.getDouble("SurfaceN_in");
            double[] rdArN = ((JAMSDoubleArray)toPoly.getObject("InterflowN_in")).getValue();
/*
            Object o = toPoly.getObject("InterflowN_in");
 
            double[] rdArN = null;
            try {
                rdArN = ((JAMSDoubleArray)o).getValue();
 
            } catch (Exception e) {
                System.out.println("MIST");
            }*/
            double NRG1in = toPoly.getDouble("N_RG1_in");
            double NRG2in = toPoly.getDouble("N_RG2_in");
//            double NRG1in = 0;
//            double NRG2in = 0;
            for(int j = 0; j < recHors; j++){
                NRD2in_h[j] = rdArN[j];
                for(int i = 0; i < srcHors; i++){
                    //NRD2in_h[j] = NRD2in_h[j] + NRD2out_h[i] * fracOut[i][j];
                    NRD2in_h[j] = NRD2in_h[j] + (NRD2out_h[i] / recHors);
                    //NRG1in = NRG1in + NRD2out_h[i] * this.percNOut[i];
                    //RD2out[i] -= RD2out[i] * fracOut[i][j];
                }
            }
            
            
            for(int i = 0; i < srcHors; i++){
                NRD2out_h[i] = 0;
            }
            NRD2in_h[recHors-1] += NExcess.getValue();
            double RD1in = toPoly.getDouble("inRD1");
            
            
            double RG2in = toPoly.getDouble("inRG2");
            
            NRD1in = NRD1in + NRD1out;
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            NRG1out = 0;
            NRG2out = 0;
            
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(NRD2out_h);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
            NExcess.setValue(0);
            
            JAMSDoubleArray rdAN = (JAMSDoubleArray)toPoly.getObject("InterflowN_in");
            rdAN.setValue(NRD2in_h);
            toPoly.setDouble("SurfaceN_in",NRD1in);
            toPoly.setObject("InterflowN_in", rdAN);
            toPoly.setDouble("N_RG1_in", NRG1in);
            toPoly.setDouble("N_RG2_in", NRG2in);
        } else if(!toReach.isEmpty()){

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

            Energy_RD1_out.setValue(0);
            Energy_RD2_out.setValue(0);
            Energy_RG1_out.setValue(0);
            Energy_RG2_out.setValue(0);

            toReach.setDouble("EnergyRD1_in", EnergyRD1in);
            toReach.setDouble("EnergyRD2_in", EnergyRD2in);
            toReach.setDouble("EnergyRG1_in", EnergyRG1in);
            toReach.setDouble("EnergyRG2_in", EnergyRG2in);

            double NRD1in = toReach.getDouble("SurfaceN_in");
/*            
            try {
                reachNRD2in = toReach.getDouble("InterflowN_sum");
                
            } catch (Exception e) {
                System.out.println("MIST");
            }
*/            
            
            reachNRD2in = toReach.getDouble("InterflowN_sum");
            
            double NRG1in = toReach.getDouble("N_RG1_in");
            double NRG2in = toReach.getDouble("N_RG2_in");
            
            for(int h = 0; h < NRD2out_h.length; h++){
                reachNRD2in = reachNRD2in + NRD2out_h[h];
                NRD2out_h[h] = 0;
            }
            NRD1in = NRD1in + NRD1out;
            reachNRD2in += NExcess.getValue();
            NRG1in = NRG1in + NRG1out;
            NRG2in = NRG2in + NRG2out;
            
            NRD1out = 0;
            
            NRG1out = 0;
            NRG2out = 0;
            
            
            NExcess.setValue(0);
            SurfaceNabs.setValue(0);
            toReach.setDouble("SurfaceN_in", NRD1in);
            InterflowNabs.setValue(NRD2out_h);
            toReach.setDouble("InterflowN_sum", reachNRD2in);
            N_RG1_out.setValue(NRG1out);
            toReach.setDouble("N_RG1_in", NRG1in);
            N_RG2_out.setValue(NRG2out);
            toReach.setDouble("N_RG2_in", NRG2in);
            
        } else{
            System.out.println("Current entity ID: " + entity.getDouble("ID") + " has no receiver.");
        }
        
    }
    public void cleanup() {
        
    }
    
  /*  private void calcParts(double[] depthSrc, double[] depthRec){
        int srcHorizons = depthSrc.length;
        int recHorizons = depthRec.length;
        
        double[] upBoundSrc = new double[srcHorizons];
        double[] lowBoundSrc = new double[srcHorizons];
        double low = 0;
        double up = 0;
        for(int i = 0; i < srcHorizons; i++){
            low += depthSrc[i];
            up = low - depthSrc[i];
            upBoundSrc[i] = up;
            lowBoundSrc[i] = low;
            //System.out.println("Src --> up: "+up+", low: "+low);
            
        }
        double[] upBoundRec = new double[recHorizons];
        double[] lowBoundRec = new double[recHorizons];
        low = 0;
        up = 0;
        for(int i = 0; i < recHorizons; i++){
            low += depthRec[i];
            up = low - depthRec[i];
            upBoundRec[i] = up;
            lowBoundRec[i] = low;
            //System.out.println("Rec --> up: "+up+", low: "+low);
        }
        
      
        fracOut = new double[depthSrc.length][depthRec.length];
        percNOut = new double[depthSrc.length];
        for(int i = 0; i < depthSrc.length; i++){
            double sumFrac = 0;
            for(int j = 0; j < depthRec.length; j++){
                if((lowBoundSrc[i] > upBoundRec[j]) && (upBoundSrc[i] < lowBoundRec[j])){
                    double relDepth = Math.min(lowBoundSrc[i], lowBoundRec[j]) - Math.max(upBoundSrc[i], upBoundRec[j]);
                    double fracDepth = relDepth / depthSrc[i];
                    sumFrac += fracDepth;
                    fracOut[i][j] = fracDepth;
                }
            }
            percNOut[i] = 1.0 - sumFrac;
        }
    }*/
}
