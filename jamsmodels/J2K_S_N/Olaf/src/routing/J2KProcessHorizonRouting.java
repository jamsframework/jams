package routing;

import ages.types.HRU;
import ages.types.Reach;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description 
    ("Calculates flow routing between HRUs to stream reaches")
@Author 
    (name="Peter Krause")
@Keywords
    ("Routing")
@VersionInfo
    ("$Id: J2KProcessHorizonRouting.java 996 2010-02-19 21:17:43Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/routing/J2KProcessHorizonRouting.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KProcessHorizonRouting  {

    private static final Logger log =
            Logger.getLogger("oms3.model." + J2KProcessHorizonRouting.class.getSimpleName());
    
    @Description ("The current hru entity")
    @In @Out public HRU hru;

    @In public double[] outRD2_h;
//
    double[][] fracOut;
    double[] percOut;
    
    @Execute
    public void execute()  {

        HRU toPoly = hru.to_hru;
        Reach toReach = hru.to_reach;

        double RD1out = hru.outRD1;
//        double[] RD2out = hru.outRD2_h;
        double[] RD2out = outRD2_h;
        double RG1out = hru.outRG1;
        double RG2out = hru.outRG2;
        
        double RD2inReach = 0;

        if(toPoly != null){
            double[] srcDepth = hru.soilType.depth;
            double[] recDepth = toPoly.soilType.depth;

            int srcHors = srcDepth.length;
            int recHors = recDepth.length;

            double[] RD2in = new double[recHors];
            calcParts(srcDepth, recDepth);

            double[] rdAr = toPoly.inRD2_h;
            double RG1in = toPoly.inRG1;
            for(int j = 0; j < recHors; j++){
                RD2in[j] = rdAr[j];
                for(int i = 0; i < srcHors; i++){
                    RD2in[j] = RD2in[j] + RD2out[i] * fracOut[i][j];
                    RG1in = RG1in + RD2out[i] * percOut[i];
                }
            }
            for(int i = 0; i < srcHors; i++){
                RD2out[i] = 0;
            }
            if(recHors == 0){
                throw new RuntimeException("RecHors is null at entity " + hru.ID);
            }
            RD2in[recHors-1] += hru.gwExcess;

            double RD1in = toPoly.inRD1;
            double RG2in = toPoly.inRG2;

            RD1in = RD1in + RD1out;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;

            RD1out = 0;
            RG1out = 0;
            RG2out = 0;

            hru.outRD1 = 0;
            hru.outRD2_h = RD2out;
            hru.outRG1 = 0;
            hru.outRG2 = 0;
            hru.gwExcess = 0;

            double[] rdA = toPoly.inRD2_h;
            rdA = RD2in;

            toPoly.inRD1 = RD1in;
            toPoly.inRD2_h = rdA;
            toPoly.inRG1 = RG1in;
            toPoly.inRG2 = RG2in;
        } else if(toReach != null){
            double RD1in = toReach.inRD1;
            RD2inReach = toReach.inRD2;
            for(int h = 0; h < RD2out.length; h++){
                RD2inReach = RD2inReach + RD2out[h];
                RD2out[h] = 0;
            }

            RD2inReach += hru.gwExcess;
            double RG1in = toReach.inRG1;
            double RG2in = toReach.inRG2;

            RD1in = RD1in + RD1out;
            RG1in = RG1in + RG1out;
            RG2in = RG2in + RG2out;

            RD1out = 0;
            RG1out = 0;
            RG2out = 0;

            hru.outRD1 = RD1out;
            toReach.inRD1 = RD1in;
            hru.outRD2_h = RD2out;
            toReach.inRD2 = RD2inReach;
            hru.outRG1 = RG1out;
            toReach.inRG1 = RG1in;
            hru.outRG2 = RG2out;
            toReach.inRG2 = RG2in;
            hru.gwExcess = 0;
        } else{
            throw new RuntimeException("Current entity ID: " + hru.ID + " has no receiver.");
        }
        if (log.isLoggable(Level.INFO)) {
//            log.info();
        }
    }
    
    private void calcParts(double[] depthSrc, double[] depthRec){
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
            //System.out.getRuntime().println("Rec --> up: "+up+", low: "+low);
        }
        
        fracOut = new double[depthSrc.length][depthRec.length];
        percOut = new double[depthSrc.length];
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
            percOut[i] = 1.0 - sumFrac;
        }
    }
}
