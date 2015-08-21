

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="J2KProcessReachRoutingWQ",
        author="Peter Krause & Manfred Fink & Marcel Wetzel",
        description="reach routing of water, energy and nitrogen to replace J2KProcessReachRouting",
        version="1.0_0",
        date="2010-09-07"
        )
        public class J2KProcessReachRoutingWQ_water extends JAMSComponent {
    
    /*
     *  Component variables
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection",
            unit = "-"
            )
            public JAMSEntityCollection entities;
      
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute length",
            unit = "m",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble length;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope",
            unit = "degree",
            lowerBound= 0,
            upperBound = 45
            )
            public JAMSDouble slope;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute width",
            unit = "m",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble width;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute roughness",
            unit = "-",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble roughness;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD1 inflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble inRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RD2 inflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble inRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG1 inflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble inRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach statevar RG2 inflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble inRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RD1 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RD2 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RG1 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar RG2 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Reach statevar simulated Runoff",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble simRunoff;
    
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
            description = "Channel storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble channelStorage;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "flow routing coefficient TA",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble flowRouteTA;

@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time",
            unit = "-",
            lowerBound= Double.NEGATIVE_INFINITY,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSCalendar time;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD1 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD2 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG1 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim runoff",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentSimRunoff;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "switch whether deep sink is allowed or not",
            defaultValue="0",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble deepsink;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "amount of water lost by deep sink",
            unit = "Liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble DeepsinkW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "K-Value for the riverbed",
            unit = "cm",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Ksink;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "mean water depth for specific reach",
            unit = "m",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
    public JAMSDouble waterdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "mean water velocity for specific reach",
            unit = "m/s",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
    public JAMSDouble velocity;

     
    private double depth; 
    private double rh;
    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity entity = entities.getCurrent();
        JAMSEntity DestReach = (JAMSEntity) entity.getObject("to_reach");
//        int datumjul = time.get(time.DAY_OF_YEAR);
        
        double deepsinkW = 0;
        double Larea = 0;
        
        double w = width.getValue();
        double s = slope.getValue();
        double rough = roughness.getValue();
        double l = length.getValue();

        double RD1act = this.actRD1.getValue() + this.inRD1.getValue();
        double RD2act = this.actRD2.getValue() + this.inRD2.getValue();
        double RG1act = this.actRG1.getValue() + this.inRG1.getValue();
        double RG2act = this.actRG2.getValue() + this.inRG2.getValue();
        
        depth = 0;
               
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        actRD1.setValue(0);
        actRD2.setValue(0);
        actRG1.setValue(0);
        actRG2.setValue(0);
            
        double RD1DestIn, RD2DestIn, RG1DestIn, RG2DestIn;
        if(DestReach.isEmpty()){
            RD1DestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());                     
        } else{
            RD1DestIn = DestReach.getDouble("inRD1");
            RD2DestIn = DestReach.getDouble("inRD2");
            RG1DestIn = DestReach.getDouble("inRG1");
            RG2DestIn = DestReach.getDouble("inRG2");
    
        }

        double q_act_tot = RD1act + RD2act + RG1act + RG2act;
        
        int ID = (int)entity.getDouble("ID");
       
        if(q_act_tot == 0){
            
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
       
            //nothing more to do here
            return;
        }
        
        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;

        //calculation of flow velocity
        double flow_veloc = this.calcFlowVelocity(q_act_tot, w, s, rough, 86400);
        velocity.setValue(flow_veloc);

        //recession coefficient
        double Rk = (flow_veloc / l) * this.flowRouteTA.getValue() * 3600;
        
        //the whole outflow
        double q_act_out;
        
        if(Rk > 0){
            q_act_out = q_act_tot * Math.exp(-1 / Rk);
        }
        else{
            q_act_out = 0;
        }

        // calculation of mean waterdepth H in m
        double H = 0;
        //q_m in m^3/s
        double q_m = q_act_tot / (1000 * 86400);
        H = (q_m / flow_veloc) / w;
        waterdepth.setValue(H);
    
        if (deepsink.getValue()==1.0){
            //calculation of deep sink
            //calculation of leckage area
            Larea = Math.pow(rh,2.0) * l;
            
            //calculation of deep sinks amount
            
            deepsinkW = Larea * Ksink.getValue() * 10;
            double deepsinkPart = deepsinkW / q_act_tot;
            deepsinkW = Math.min(deepsinkW,q_act_out);
            deepsinkW = Math.max(deepsinkW,0);
            
        }else{
          
            deepsinkW = 0;
        }
                      
        DeepsinkW.setValue(deepsinkW);
 
        //the actual outflow from the reach
          
        double RD1outdeep = deepsinkW * RD1_part;
        double RD2outdeep = deepsinkW * RD2_part;
        double RG1outdeep = deepsinkW * RG1_part;
        double RG2outdeep = deepsinkW * RG2_part;
 
        double RD1out = q_act_out * RD1_part - RD1outdeep;
        double RD2out = q_act_out * RD2_part - RD2outdeep;
        double RG1out = q_act_out * RG1_part - RG1outdeep;
        double RG2out = q_act_out * RG2_part - RG2outdeep;
 
        //transferring runoff from this reach to the next one
        RD1DestIn = RD1DestIn + RD1out;
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;

        //reducing the actual storages
        RD1act = RD1act - RD1out - RD1outdeep;
        if (RD1act < 0) RD1act = 0;
        RD2act = RD2act - RD2out - RD2outdeep;
        if (RD2act < 0) RD2act = 0;
        RG1act = RG1act - RG1out - RG1outdeep;
        if (RG1act < 0) RG1act = 0;
        RG2act = RG2act - RG2out - RG1outdeep;
        if (RG2act < 0) RG2act = 0;
 
        double channelStorage = RD1act + RD2act + RG1act + RG2act;
        double cumOutflow = RD1out + RD2out + RG1out + RG2out;
        
        simRunoff.setValue(cumOutflow);
        this.channelStorage.setValue(channelStorage);
      
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);

        actRD1.setValue(RD1act);
        actRD2.setValue(RD2act);
        actRG1.setValue(RG1act);
        actRG2.setValue(RG2act);
        
        outRD1.setValue(RD1out);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);

/*        if(entity.getObject("to_reach") == null){
 
        System.out.println(RD1out + " RD1out " + RD2out + " RD2out "+ RG1out +" RG1out " + RG2out +" RG2out ");
 
        }*/
        if(!DestReach.isEmpty()){
            DestReach.setDouble("inRD1",RD1DestIn);
            DestReach.setDouble("inRD2",RD2DestIn);
            DestReach.setDouble("inRG1",RG1DestIn);
            DestReach.setDouble("inRG2",RG2DestIn);
         
        }else{
            
            catchmentRD1.setValue(RD1out);
            catchmentRD2.setValue(RD2out);
            catchmentRG1.setValue(RG1out);
            catchmentRG2.setValue(RG2out);
            catchmentSimRunoff.setValue(cumOutflow);

        }
        
    }
    
    public void cleanup() {
        
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
    public double calcFlowVelocity(double q, double width, double slope, double rough, int secondsOfTimeStep){
        double afv = 1;
        double veloc = 0;
        
        /**
         *transfering liter/d to m³/s
         **/
        double q_m = q / (1000 * secondsOfTimeStep);
        this.rh = calcHydraulicRadius(afv, q_m, width);
        boolean cont = true;
        while(cont){
            veloc = (rough) * Math.pow(rh, (2.0/3.0)) * Math.sqrt(slope);
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
