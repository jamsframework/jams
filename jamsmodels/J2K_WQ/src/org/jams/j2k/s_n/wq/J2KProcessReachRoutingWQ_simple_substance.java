

package org.jams.j2k.s_n.wq;

import jams.data.*;
import jams.model.*;


@JAMSComponentDescription(
        title="J2KProcessReachRoutingWQ_simple_substance",
        author="Peter Krause & Manfred Fink & Marcel Wetzel",
        description="simple substance reach routing",
        version="1.0_0",
        date="2014-02-04"
        )
        public class J2KProcessReachRoutingWQ_simple_substance extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Substance inflow",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Substance_in;
     
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Reach statevar Q_Sub (Substance) storage",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActQ_Sub;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach Channel storage Substance",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ChannelStorage_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Simulated Substance load",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SimRunoff_N;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet simulated Substance load",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentSimRunoffN;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Catchment outlet simulated Substance concentration",
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
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
            description = "specific substance in Reach",
            unit = "mg/l",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Substance;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach amount of specific substance",
            unit = "kg",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble QActOut_Sub;

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
        double SubIn = 0;
                
// Einlesen der wesentlichen Abflussvariablen zum Ableiten der einzelnen Abflusskomponenten
        
        q_act_tot = QActTot.getValue();
        q_act_out = QActOut.getValue();
        deepsinkW = DeepsinkW.getValue();
        cumOutflow = QCumOut.getValue();

// Bestimmen der Stofffracht bezogen auf den geladenenen Parameter
// (SUBSTANCE entsprechend der Kalibrierung im xml-File)
        
        SubIn = Substance.getValue() * QCumOut.getValue();
        
// Die Einheiten zwischen dem Input aus dem EZG und dem Reach m?ssen noch angeglichen werden (kg vs. mg ???)
        
        double QAct_Sub = this.ActQ_Sub.getValue() + SubIn;
        
        ActQ_Sub.setValue(0);
        
        double DestIn_Sub;
        if(DestReach.isEmpty()){
            DestIn_Sub = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
                                
        } else{
            DestIn_Sub = DestReach.getDouble("Substance_in");
                        
        }

        double q_act_tot_N = DestIn_Sub;
        
        int ID = (int)entity.getDouble("ID");
        //System.out.println("surfheat: " + energyRns + energyatloRad + energywaterbr + energycoco + energypotET + " " + ID);
        //System.out.println("sedheat: " + sedheat + "q_act_tot_Energy: " + q_act_tot_Energy + " " + ID);

        //calculation of N-Concentration with q_act_tot and q_act_tot_N
        double N_conc_tot = q_act_tot_N / q_act_tot;

        //calculation of N-content in q_act_out
        double q_act_out_N = q_act_out * N_conc_tot;
        QActOut_Sub.setValue(q_act_out_N);
        
        
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
        double Q_out_Ndeep = deepsinkN;
        double Q_out_N = q_act_out_N - Q_out_Ndeep;
                
        //transferring runoff from this reach to the next one
        DestIn_Sub = DestIn_Sub + Q_out_N;
                
        //reducing the actual storages
        QAct_Sub = QAct_Sub - Q_out_N - Q_out_Ndeep;
        if (QAct_Sub < 0) QAct_Sub = 0;
                
        double channelStorage_N = QAct_Sub;
        double cumOutflow_N = Q_out_N;
        
        SimRunoff_N.setValue(cumOutflow_N);
        ChannelStorage_N.setValue(channelStorage_N);
 
        ActQ_Sub.setValue(QAct_Sub);
        
/*        if(entity.getObject("to_reach") == null){
 
        System.out.println(RD1out + " RD1out " + RD2out + " RD2out "+ RG1out +" RG1out " + RG2out +" RG2out ");
 
        }*/
        if(!DestReach.isEmpty()){
            DestReach.setDouble("Substance_in", DestIn_Sub);
                        
        }else{
             
            catchmentSimRunoffN.setValue(cumOutflow_N);
            catchmentSimNconc.setValue((cumOutflow_N * 1000000)/cumOutflow);
            
        }
        
    }
    
    public void cleanup() {
        
    }
  
}
