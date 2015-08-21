

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="J2KProcessReachRoutingWQ",
        author="Peter Krause & Manfred Fink & Marcel Wetzel",
        description="substance reach routing",
        version="1.0_0",
        date="2012-02-05"
        )
        public class J2KProcessReachRoutingWQ_substance extends JAMSComponent {
    
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
            description = "reach statevar total Q storage",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QActTot;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach statevar total Q_out",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QActOut;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach statevar total Q_out without deepsinkW",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QCumOut;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "amount of water lost by deep sink",
            unit = "Liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble DeepsinkW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "SurfaceN outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SurfaceNabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "(fast) InterflowN outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble InterflowNabs;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "(slow) InterflowN outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "GoundwaterN outflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG2_out;

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
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach Channel storage N",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ChannelStorage_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Simulated N Runoff",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SimRunoff_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "SurfaceN inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SurfaceN_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "(fast) InterflowN inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble InterflowN_sum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "(slow) InterflowN inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG1_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "GoundwaterN inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble N_RG2_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRD1 storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentNRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRD2 storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentNRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRG1 storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentNRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet NRG2 storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentNRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet sim Nitrogen runoff",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentSimRunoffN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet sim Nitrogen runoff",
            unit = "mg/L",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentSimNconc;
    
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
            description = "amount of nitrogen lost by deep sink",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble DeepsinkN;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar RD1 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar RD2 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar RG1 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Reach statevar RG2 outflow",
            unit = "liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble outRG2;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "specific substance in Reach",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Substance;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "relative part of RD1_N",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble PartRD1_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "relative part of RD2_N",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble PartRD2_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "relative part of RG1_N",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble PartRG1_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "relative part of RG2_N",
            unit = "-",
            lowerBound= 0,
            upperBound = 1
            )
            public JAMSDouble PartRG2_N;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach amount of specific substance",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QActOutN;

    /*
     *  Component run stages
     */
    
    public void init() throws JAMSEntity.NoSuchAttributeException {
        
    }
    
    public void run() throws JAMSEntity.NoSuchAttributeException {
        Attribute.Entity entity = entities.getCurrent();
        JAMSEntity DestReach = (JAMSEntity) entity.getObject("to_reach");
//        int datumjul = time.get(time.DAY_OF_YEAR);
        
   
        double deepsinkN = 0;
        double deepsinkW = 0;
        double q_act_tot = 0;
        double q_act_out = 0;
        double cumOutflow = 0;
        double rd1PartIn, rd2PartIn, rg1PartIn, rg2PartIn = 0;
        double rd1SubIn, rd2SubIn, rg1SubIn, rg2SubIn = 0;
        
// Einlesen der wesentlichen Abflussvariablen zum Ableiten der einzelnen Abflusskomponenten
        
        q_act_tot = QActTot.getValue();
        q_act_out = QActOut.getValue();
        deepsinkW = DeepsinkW.getValue();
        cumOutflow = QCumOut.getValue();

// Anteile der einzelnen Abflusskomponenten am Gesamtzufluss des Reaches im jeweiligen Zeitschritt
// (Summe 1)

        rd1PartIn = outRD1.getValue() / QCumOut.getValue();
        rd2PartIn = outRD2.getValue() / QCumOut.getValue();
        rg1PartIn = outRG1.getValue() / QCumOut.getValue();
        rg2PartIn = outRG2.getValue() / QCumOut.getValue();
        
// Bestimmen der Stofffrachten der einzelnen Abflusskomponenten jeweils bezogen auf den geladenenen Parameter
// (SUBSTANCE entsprechend der Kalibrierung im xml-File)
        
        rd1SubIn = rd1PartIn * Substance.getValue() * outRD1.getValue();
        rd2SubIn = rd2PartIn * Substance.getValue() * outRD2.getValue();
        rg1SubIn = rg1PartIn * Substance.getValue() * outRG1.getValue();
        rg2SubIn = rg2PartIn * Substance.getValue() * outRG2.getValue();
        
// Die Einheiten zwischen dem Input aus dem EZG und dem Reach m?ssen noch angeglichen werden (kg vs. mg ???)
        

        double RD1act_N = this.ActRD1_N.getValue() + this.SurfaceN_in.getValue() + rd1SubIn;
        double RD2act_N = this.ActRD2_N.getValue() + this.InterflowN_sum.getValue() + rd2SubIn;
        double RG1act_N = this.ActRG1_N.getValue() + this.N_RG1_in.getValue() + rg1SubIn;
        double RG2act_N = this.ActRG2_N.getValue() + this.N_RG2_in.getValue() + rg2SubIn;
    
        SurfaceN_in.setValue(0);
        InterflowN_sum.setValue(0);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);

        ActRD1_N.setValue(0);
        ActRD2_N.setValue(0);
        ActRG1_N.setValue(0);
        ActRG2_N.setValue(0);
        
            
        double RD1DestIn_N, RD2DestIn_N, RG1DestIn_N, RG2DestIn_N;
        if(DestReach.isEmpty()){
            RD1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
                        
        } else{
            RD1DestIn_N = DestReach.getDouble("SurfaceN_in");
            RD2DestIn_N = DestReach.getDouble("InterflowN_sum");
            RG1DestIn_N = DestReach.getDouble("N_RG1_in");
            RG2DestIn_N = DestReach.getDouble("N_RG2_in");
            
        }

        double q_act_tot_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        
        int ID = (int)entity.getDouble("ID");
        //System.out.println("surfheat: " + energyRns + energyatloRad + energywaterbr + energycoco + energypotET + " " + ID);
        //System.out.println("sedheat: " + sedheat + "q_act_tot_Energy: " + q_act_tot_Energy + " " + ID);

        if(q_act_tot == 0){
            
            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
            
            //nothing more to do here
            return;
        }
        
        //relative parts of the runoff components for later redistribution
  
        double RD1_part_N = 0;
        double RD2_part_N = 0;
        double RG1_part_N = 0;
        double RG2_part_N = 0;
        
        if(q_act_tot_N == 0){

        


        } else{

            RD1_part_N = RD1act_N / q_act_tot_N;
            RD2_part_N = RD2act_N / q_act_tot_N;
            RG1_part_N = RG1act_N / q_act_tot_N;
            RG2_part_N = RG2act_N / q_act_tot_N;            


        }

        
        PartRD1_N.setValue(RD1_part_N);
        PartRD2_N.setValue(RD2_part_N);
        PartRG1_N.setValue(RG1_part_N);
        PartRG2_N.setValue(RG2_part_N);
        
        //calculation of N-Concentration with q_act_tot and q_act_tot_N
        double N_conc_tot = q_act_tot_N / q_act_tot;

        //calculation of N-content in q_act_out
        double q_act_out_N = q_act_out * N_conc_tot;
        QActOutN.setValue(q_act_out_N);
        
        
        if (deepsink.getValue()==1.0){
            //calculation of deep sink
            //calculation of deep sinks amount
            
            deepsinkN = deepsinkW * N_conc_tot;
 
            deepsinkN = Math.min(deepsinkN,q_act_out_N);
            deepsinkN = Math.max(deepsinkN,0);
 
            
        }else{
          
             deepsinkN = 0;
            
        }

        DeepsinkN.setValue(deepsinkN);
        
        //the actual outflow from the reach
        double RD1out_Ndeep = deepsinkN * RD1_part_N;
        double RD2out_Ndeep = deepsinkN * RD2_part_N;
        double RG1out_Ndeep = deepsinkN * RG1_part_N;
        double RG2out_Ndeep = deepsinkN * RG2_part_N;

        double RD1out_N = q_act_out_N * RD1_part_N - RD1out_Ndeep;
        double RD2out_N = q_act_out_N * RD2_part_N - RD2out_Ndeep;
        double RG1out_N = q_act_out_N * RG1_part_N - RG1out_Ndeep;
        double RG2out_N = q_act_out_N * RG2_part_N - RG2out_Ndeep;
        
        //transferring runoff from this reach to the next one
        RD1DestIn_N = RD1DestIn_N + RD1out_N;
        RD2DestIn_N = RD2DestIn_N + RD2out_N;
        RG1DestIn_N = RG1DestIn_N + RG1out_N;
        RG2DestIn_N = RG2DestIn_N + RG2out_N;
        
        //reducing the actual storages
        RD1act_N = RD1act_N - RD1out_N - RD1out_Ndeep;
        if (RD1act_N < 0) RD1act_N = 0;
        RD2act_N = RD2act_N - RD2out_N - RD2out_Ndeep;
        if (RD2act_N < 0) RD2act_N = 0;
        RG1act_N = RG1act_N - RG1out_N - RG1out_Ndeep;
        if (RG1act_N < 0) RG1act_N = 0;
        RG2act_N = RG2act_N - RG2out_N - RG2out_Ndeep;
        if (RG2act_N < 0) RG2act_N = 0;
        
        double channelStorage_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        double cumOutflow_N = RD1out_N + RD2out_N + RG1out_N + RG2out_N;
        
        SimRunoff_N.setValue(cumOutflow_N);
        ChannelStorage_N.setValue(channelStorage_N);
 
        SurfaceN_in.setValue(0);
        InterflowN_sum.setValue(0);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);

        ActRD1_N.setValue(RD1act_N);
        ActRD2_N.setValue(RD2act_N);
        ActRG1_N.setValue(RG1act_N);
        ActRG2_N.setValue(RG2act_N);

        SurfaceNabs.setValue(RD1out_N);
        InterflowNabs.setValue(RD2out_N);
        N_RG1_out.setValue(RG1out_N);
        N_RG2_out.setValue(RG2out_N);

        
/*        if(entity.getObject("to_reach") == null){
 
        System.out.println(RD1out + " RD1out " + RD2out + " RD2out "+ RG1out +" RG1out " + RG2out +" RG2out ");
 
        }*/
        if(!DestReach.isEmpty()){
            DestReach.setDouble("SurfaceN_in", RD1DestIn_N);
            DestReach.setDouble("InterflowN_sum", RD2DestIn_N);
            DestReach.setDouble("N_RG1_in", RG1DestIn_N);
            DestReach.setDouble("N_RG2_in", RG2DestIn_N);
            
        }else{
 
            catchmentNRD1.setValue(RD1out_N);
            catchmentNRD2.setValue(RD2out_N);
            catchmentNRG1.setValue(RG1out_N);
            catchmentNRG2.setValue(RG2out_N);
            catchmentSimRunoffN.setValue(cumOutflow_N);
            catchmentSimNconc.setValue((cumOutflow_N * 1000000)/cumOutflow);
            
        }
        
    }
    
    public void cleanup() {
        
    }
  
}
