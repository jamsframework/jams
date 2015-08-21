package routing;

import staging.j2k.types.Reach;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Component summary info ...")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Routing")
@VersionInfo
    ("$Id: J2KProcessReachRouting.java 952 2010-02-11 19:55:02Z odavid $")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/routing/J2KProcessReachRouting.java $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class J2KProcessReachRouting  {
    
    @Description("temporal resolution")
    @Unit("d or h")
    @Role(PARAMETER)
    @In public String tempRes;

    @Description("flow routing coefficient TA")
    @Role(PARAMETER)
    @In public double flowRouteTA;

    @Description("Current REACH")
    @In public Reach reach;


    int sec_inTStep = 0;
    
    @Execute
    public void execute()  {
        if (sec_inTStep == 0) {
            if (tempRes.equals("d")) {
                sec_inTStep = 86400;
            } else if (tempRes.equals("h")) {
                sec_inTStep = 3600;
            }
        }

        Reach to_reach =  reach.to_reach;

        double RD1act = reach.actRD1 + reach.inRD1;
        double RD2act = reach.actRD2 + reach.inRD2;
        double RG1act = reach.actRG1 + reach.inRG1;
        double RG2act = reach.actRG2 + reach.inRG2;
        double addInAct = reach.actAddIn + reach.inAddIn;

        reach.inRD1 = 0;
        reach.inRD2 = 0;
        reach.inRG1 = 0;
        reach.inRG2 = 0;
        reach.inAddIn = 0;
        reach.actRD1 = 0;
        reach.actRD2 = 0;
        reach.actRG1 = 0;
        reach.actRG2 = 0;
        reach.actAddIn = 0;

        double RD1DestIn, RD2DestIn, RG1DestIn, RG2DestIn, addInDestIn;
        if (to_reach == null) {
            RD1DestIn = 0;
            RD2DestIn = 0;
            RG1DestIn = 0;
            RG2DestIn = 0;
            addInDestIn = 0;
        } else {
            RD1DestIn =  to_reach.inRD1;
            RD2DestIn =  to_reach.inRD2;
            RG1DestIn =  to_reach.inRG1;
            RG2DestIn =  to_reach.inRG2;
            addInDestIn = to_reach.inAddIn;
        }

        double q_act_tot = RD1act + RD2act + RG1act + RG2act + addInAct;
        if(q_act_tot == 0){
        // if(Math.abs(q_act_tot) < 0.00001){
            reach.outRD1 = 0;
            reach.outRD2 = 0;
            reach.outRG1 = 0;
            reach.outRG2 = 0;
            reach.outAddIn = 0;
            //nothing more to do here
            return;
        }

        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;
        double addInPart = addInAct / q_act_tot;

        //calculation of flow velocity
        double flow_veloc = calcFlowVelocity(q_act_tot, reach.width, reach.slope, reach.rough, sec_inTStep);

        //recession coefficient
        double Rk = (flow_veloc / reach.length) * flowRouteTA * 3600;

        //the whole outflow
        double q_act_out = 0;
        if(Rk > 0) {
            q_act_out = q_act_tot * Math.exp(-1 / Rk);
        } 

        //the actual outflow from the reach
        double RD1out = q_act_out * RD1_part;
        double RD2out = q_act_out * RD2_part;
        double RG1out = q_act_out * RG1_part;
        double RG2out = q_act_out * RG2_part;
        double addInOut = q_act_out * addInPart;

        //transferring runoff from this reach to the next one
        RD1DestIn = RD1DestIn + RD1out;
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;
        addInDestIn = addInDestIn + addInOut;

        //reducing the actual storages
        RD1act = RD1act - q_act_out * RD1_part;
        RD2act = RD2act - q_act_out * RD2_part;
        RG1act = RG1act - q_act_out * RG1_part;
        RG2act = RG2act - q_act_out * RG2_part;
        addInAct = addInAct - q_act_out * addInPart;

        reach.actRD1 = RD1act;
        reach.actRD2 = RD2act;
        reach.actRG1 = RG1act;
        reach.actRG2 = RG2act;
        reach.actAddIn = addInAct;
        reach.outRD1 = RD1out;
        reach.outRD2 = RD2out;
        reach.outRG1 = RG1out;
        reach.outRG2 = RG2out;
        reach.outAddIn = addInOut;
        reach.channelStorage = RD1act + RD2act + RG1act + RG2act + addInAct;
        reach.simRunoff = RD1out + RD2out + RG1out + RG2out + addInOut;

        if (to_reach != null) {
            to_reach.inRD1 = RD1DestIn;
            to_reach.inRD2 = RD2DestIn;
            to_reach.inRG1 = RG1DestIn;
            to_reach.inRG2 = RG2DestIn;
            to_reach.inAddIn = addInDestIn;
        } else {
//            // outlet reach.
//            catchmentRD1 = RD1out;
//            catchmentRD2 = RD2out;
//            catchmentRG1 = RG1out;
//            catchmentRG2 = RG2out;
//            catchmentAddIn = addInOut;
//            catchmentSimRunoff = simRunoff;
        }
    }
    
    /**
     * Calculates flow velocity in specific reach
     * @param q the runoff in the reach
     * @param width the width of reach
     * @param slope the slope of reach
     * @param rough the roughness of reach
     * @param secondsOfTimeStep the current time step in seconds
     * @return flow_velocity in m/s
     */
    public static double calcFlowVelocity(double q, double width, double slope,
            double rough, int secondsOfTimeStep){
        double afv = 1;
        
        /* transfering liter/d to m3/s
         **/
        double q_m = q / (1000 * secondsOfTimeStep);
        double rh = calcHydraulicRadius(afv, q_m, width);
        double sqrt_slope = Math.sqrt(slope);
        boolean cont = true;
        while(cont){
            double veloc = rough * Math.pow(rh, (2.0/3.0)) * sqrt_slope;
            if((Math.abs(veloc - afv)) > 0.001){
                afv = veloc;
                rh = calcHydraulicRadius(afv, q_m, width);
            } else{
                cont = false;
                afv = veloc;
            }
        }
        return afv;
    }
    
    /**
     * Calculates the hydraulic radius of a rectangular
     * stream bed depending on daily runoff and flow_velocity
     * @param v the flow velocity
     * @param q the daily runoff
     * @param width the width of reach
     * @return hydraulic radius in m
     */
    public static double calcHydraulicRadius(double v, double q, double width){
        double A = (q / v);
        double rh = A / (width + 2*(A / width));
        return rh;
    }
}
