

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
        public class J2KProcessReachRoutingWQ extends JAMSComponent {
    
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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of Surface outflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RD1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of (fast) Interflow outflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RD2_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of (slow) Interflow outflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RG1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy of Groundwater outflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Energy_RG2_out;

     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of Reach statevar RD1 storage",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD1Energy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of Reach statevar RD2 storage",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRD2Energy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of Reach statevar RG1 storage",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG1Energy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of Reach statevar RG2 storage",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ActRG2Energy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reach Channel storage energy amount",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble ChannelStorageEnergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy Amount of simulated runoff",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble SimRunoffEnergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of Surface inflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRD1_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of (fast) Interflow inflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRD2_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of (slow) Interflow inflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRG1_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Energy of Goundwater inflow",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble EnergyRG2_in;

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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD1 energy",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentEnergyRD1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RD2 energy",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentEnergyRD2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG1 energy",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentEnergyRG1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Catchment outlet RG2 energy",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentEnergyRG2;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sim Catchment outlet temperature",
            unit = "°C",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentSimRunoffTemp;
    
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
            description = "amount of water lost by deep sink",
            unit = "Liter",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble DeepsinkW;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "energy amount of lost by deep sink",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble DeepsinkT;
    
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
            description = "K-Value for the riverbed",
            unit = "cm",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Ksink;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "average water temperature for specific reach",
            unit = "°C",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble watertempavg;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sim Catchment outlet energy",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble catchmentSimRunoffEnergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potET energy amount of specific reach",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble potETenergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "energy amount of net atmospheric longwave radiation",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble atloRadEnergy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "energy amount of net solar radiation",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble Rns;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "energy amount of back radiation from the water surface",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble waterbr;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "heat transfer due to conduction and convection",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble coco;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the sediment water heat flux",
            unit = "Kcal/(m^2*d)",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
            public JAMSDouble sedheat2;

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

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Energy amount for specific reach",
            unit = "Kcal",
            lowerBound= 0,
            upperBound = Double.POSITIVE_INFINITY
            )
    public JAMSDouble Reach_Energy;


    
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
        double deepsinkT = 0;
        double deepsinkN = 0;
        double Larea = 0;
        
        double w = width.getValue();
        double s = slope.getValue();
        double rough = roughness.getValue();
        double l = length.getValue();

        // surface heat flux variables (Kcal)
        double energypotET = potETenergy.getValue();
        // surface heat flux variables (Kcal / (m^2 * d))
        double energyatloRad = atloRadEnergy.getValue();
        double energyRns = Rns.getValue();
        double energywaterbr = waterbr.getValue();
        double energycoco = coco.getValue();
        double energysed = 0;
        potETenergy.setValue(0);
        atloRadEnergy.setValue(0);
        Rns.setValue(0);
        waterbr.setValue(0);
        coco.setValue(0);
        // sediment heat flux variables (Kcal / (m^2 * d))
        energysed = sedheat2.getValue();
        sedheat2.setValue(0);

        
        double RD1act = this.actRD1.getValue() + this.inRD1.getValue();
        double RD2act = this.actRD2.getValue() + this.inRD2.getValue();
        double RG1act = this.actRG1.getValue() + this.inRG1.getValue();
        double RG2act = this.actRG2.getValue() + this.inRG2.getValue();
        
        double RD1actEnergy = this.ActRD1Energy.getValue() + this.EnergyRD1_in.getValue();
        double RD2actEnergy = this.ActRD2Energy.getValue() + this.EnergyRD2_in.getValue();
        double RG1actEnergy = this.ActRG1Energy.getValue() + this.EnergyRG1_in.getValue();
        double RG2actEnergy = this.ActRG2Energy.getValue() + this.EnergyRG2_in.getValue();

        double RD1act_N = this.ActRD1_N.getValue() + this.SurfaceN_in.getValue();
        double RD2act_N = this.ActRD2_N.getValue() + this.InterflowN_sum.getValue();
        double RG1act_N = this.ActRG1_N.getValue() + this.N_RG1_in.getValue();
        double RG2act_N = this.ActRG2_N.getValue() + this.N_RG2_in.getValue();
        depth = 0;
        
        
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);
        
        actRD1.setValue(0);
        actRD2.setValue(0);
        actRG1.setValue(0);
        actRG2.setValue(0);

        EnergyRD1_in.setValue(0);
        EnergyRD2_in.setValue(0);
        EnergyRG1_in.setValue(0);
        EnergyRG2_in.setValue(0);

        ActRD1Energy.setValue(0);
        ActRD2Energy.setValue(0);
        ActRG1Energy.setValue(0);
        ActRG2Energy.setValue(0);

        SurfaceN_in.setValue(0);
        InterflowN_sum.setValue(0);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);

        ActRD1_N.setValue(0);
        ActRD2_N.setValue(0);
        ActRG1_N.setValue(0);
        ActRG2_N.setValue(0);
        
            
        double RD1DestIn, RD2DestIn, RG1DestIn, RG2DestIn, RD1DestInEnergy, RD2DestInEnergy, RG1DestInEnergy, RG2DestInEnergy,
               RD1DestIn_N, RD2DestIn_N, RG1DestIn_N, RG2DestIn_N;
        if(DestReach.isEmpty()){
            RD1DestIn = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());

            RD1DestInEnergy = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestInEnergy = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestInEnergy = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestInEnergy = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
            
            RD1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD1.getValue());
            RD2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRD2.getValue());
            RG1DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG1.getValue());
            RG2DestIn_N = 0;//entity.getDouble(aNameCatchmentOutRG2.getValue());
                        
        } else{
            RD1DestIn = DestReach.getDouble("inRD1");
            RD2DestIn = DestReach.getDouble("inRD2");
            RG1DestIn = DestReach.getDouble("inRG1");
            RG2DestIn = DestReach.getDouble("inRG2");
            
            RD1DestInEnergy = DestReach.getDouble("EnergyRD1_in");
            RD2DestInEnergy = DestReach.getDouble("EnergyRD2_in");
            RG1DestInEnergy = DestReach.getDouble("EnergyRG1_in");
            RG2DestInEnergy = DestReach.getDouble("EnergyRG2_in");

            RD1DestIn_N = DestReach.getDouble("SurfaceN_in");
            RD2DestIn_N = DestReach.getDouble("InterflowN_sum");
            RG1DestIn_N = DestReach.getDouble("N_RG1_in");
            RG2DestIn_N = DestReach.getDouble("N_RG2_in");
            
        }

        // area calculation of specific reach in m^2
        //double l = length.getValue();
        //double w = width.getValue();
        double reacharea = 0;
        reacharea = l * w; // m^2
        double surfheat,sedheat = 0;
        double q_act_tot_Energy = RD1actEnergy + RD2actEnergy + RG1actEnergy + RG2actEnergy;
        double Reach_Energy_run = (q_act_tot_Energy / reacharea);
        // calculation of surface heat flux in Kcal / d
        //surfheat = energyRns * reacharea - energypotET;
        surfheat = ((energyRns + energyatloRad - energywaterbr - energycoco) * reacharea) - energypotET;
        //surfheat = (energyRns + energyatloRad - energycoco - energywaterbr) * reacharea;
        // calculation of sediment heat flux in Kcal / d
        sedheat = energysed * reacharea;

        double heatflux = surfheat + sedheat;

        if (-heatflux > Reach_Energy_run){

            heatflux = - Reach_Energy_run;

        }
        
        double q_act_tot = RD1act + RD2act + RG1act + RG2act;
        q_act_tot_Energy = q_act_tot_Energy + heatflux;
        double q_act_tot_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        Reach_Energy.setValue(q_act_tot_Energy / reacharea);
        
        int ID = (int)entity.getDouble("ID");
        //System.out.println("surfheat: " + energyRns + energyatloRad + energywaterbr + energycoco + energypotET + " " + ID);
        //System.out.println("sedheat: " + sedheat + "q_act_tot_Energy: " + q_act_tot_Energy + " " + ID);

        if(q_act_tot == 0){
            
            outRD1.setValue(0);
            outRD2.setValue(0);
            outRG1.setValue(0);
            outRG2.setValue(0);
            
            Energy_RD1_out.setValue(0);
            Energy_RD2_out.setValue(0);
            Energy_RG1_out.setValue(0);
            Energy_RG2_out.setValue(0);

            SurfaceNabs.setValue(0);
            InterflowNabs.setValue(0);
            N_RG1_out.setValue(0);
            N_RG2_out.setValue(0);
            
            //nothing more to do here
            return;
        }
        
        //relative parts of the runoff components for later redistribution
        double RD1_part = RD1act / q_act_tot;
        double RD2_part = RD2act / q_act_tot;
        double RG1_part = RG1act / q_act_tot;
        double RG2_part = RG2act / q_act_tot;
        
        double RD1_part_Energy = 0;
        double RD2_part_Energy = 0;
        double RG1_part_Energy = 0;
        double RG2_part_Energy = 0;

        double RD1_part_N = 0;
        double RD2_part_N = 0;
        double RG1_part_N = 0;
        double RG2_part_N = 0;
        
        if(q_act_tot_Energy == 0){
            
            
            
        } else{
            
            RD1_part_Energy = RD1actEnergy / q_act_tot_Energy;
            RD2_part_Energy = RD2actEnergy / q_act_tot_Energy;
            RG1_part_Energy = RG1actEnergy / q_act_tot_Energy;
            RG2_part_Energy = RG2actEnergy / q_act_tot_Energy;
            
            
        }

        //calculation of Reach Total Temperature in °C with q_act_tot and q_act_tot_Temp
        double Temp_tot = q_act_tot_Energy / q_act_tot;
        

        if(q_act_tot_N == 0){

        


        } else{

            RD1_part_N = RD1act_N / q_act_tot_N;
            RD2_part_N = RD2act_N / q_act_tot_N;
            RG1_part_N = RG1act_N / q_act_tot_N;
            RG2_part_N = RG2act_N / q_act_tot_N;            


        }

        //calculation of N-Concentration with q_act_tot and q_act_tot_N
        double N_conc_tot = q_act_tot_N / q_act_tot;

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
        
        //calculation of energy amount in q_act_out
        
        double q_act_out_Energy = (q_act_out / q_act_tot) * q_act_tot_Energy;

        //calculation of N-content in q_act_out

        double q_act_out_N = q_act_out * N_conc_tot;
        
        
        
        if (deepsink.getValue()==1.0){
            //calculation of deep sink
            //calculation of leckage area
            Larea = Math.pow(rh,2.0) * l;
            
            //calculation of deep sinks amount
            
            deepsinkW = Larea * Ksink.getValue() * 10;

            double deepsinkPart = deepsinkW / q_act_tot;

            deepsinkT = deepsinkPart * q_act_tot_Energy;
            deepsinkN = deepsinkW * N_conc_tot;
            
            deepsinkW = Math.min(deepsinkW,q_act_out);
            deepsinkT = Math.min(deepsinkT,q_act_out_Energy);
            deepsinkN = Math.min(deepsinkN,q_act_out_N);
            deepsinkW = Math.max(deepsinkW,0);
            deepsinkT = Math.max(deepsinkT,0);
            deepsinkN = Math.max(deepsinkN,0);
 
            
        }else{
          
            deepsinkW = 0;
            deepsinkT = 0;
            deepsinkN = 0;
            
        }
        
              
        DeepsinkW.setValue(deepsinkW);
        DeepsinkT.setValue(deepsinkT);
        DeepsinkN.setValue(deepsinkN);
        
        //the actual outflow from the reach
       
        
        double RD1outdeep = deepsinkW * RD1_part;
        double RD2outdeep = deepsinkW * RD2_part;
        double RG1outdeep = deepsinkW * RG1_part;
        double RG2outdeep = deepsinkW * RG2_part;
        
        double RD1out_Tdeep = deepsinkT * RD1_part_Energy;
        double RD2out_Tdeep = deepsinkT * RD2_part_Energy;
        double RG1out_Tdeep = deepsinkT * RG1_part_Energy;
        double RG2out_Tdeep = deepsinkT * RG2_part_Energy;

        double RD1out_Ndeep = deepsinkN * RD1_part_N;
        double RD2out_Ndeep = deepsinkN * RD2_part_N;
        double RG1out_Ndeep = deepsinkN * RG1_part_N;
        double RG2out_Ndeep = deepsinkN * RG2_part_N;

        double RD1out = q_act_out * RD1_part - RD1outdeep;
        double RD2out = q_act_out * RD2_part - RD2outdeep;
        double RG1out = q_act_out * RG1_part - RG1outdeep;
        double RG2out = q_act_out * RG2_part - RG2outdeep;
        
        double RD1out_Energy = q_act_out_Energy * RD1_part_Energy - RD1out_Tdeep;
        double RD2out_Energy = q_act_out_Energy * RD2_part_Energy - RD2out_Tdeep;
        double RG1out_Energy = q_act_out_Energy * RG1_part_Energy - RG1out_Tdeep;
        double RG2out_Energy = q_act_out_Energy * RG2_part_Energy - RG2out_Tdeep;

        double RD1out_N = q_act_out_N * RD1_part_N - RD1out_Ndeep;
        double RD2out_N = q_act_out_N * RD2_part_N - RD2out_Ndeep;
        double RG1out_N = q_act_out_N * RG1_part_N - RG1out_Ndeep;
        double RG2out_N = q_act_out_N * RG2_part_N - RG2out_Ndeep;
        
        //transferring runoff from this reach to the next one
        RD1DestIn = RD1DestIn + RD1out;
        RD2DestIn = RD2DestIn + RD2out;
        RG1DestIn = RG1DestIn + RG1out;
        RG2DestIn = RG2DestIn + RG2out;
        
        RD1DestInEnergy = RD1DestInEnergy + RD1out_Energy;
        RD2DestInEnergy = RD2DestInEnergy + RD2out_Energy;
        RG1DestInEnergy = RG1DestInEnergy + RG1out_Energy;
        RG2DestInEnergy = RG2DestInEnergy + RG2out_Energy;

        RD1DestIn_N = RD1DestIn_N + RD1out_N;
        RD2DestIn_N = RD2DestIn_N + RD2out_N;
        RG1DestIn_N = RG1DestIn_N + RG1out_N;
        RG2DestIn_N = RG2DestIn_N + RG2out_N;
        
        //reducing the actual storages
        RD1act = RD1act - RD1out - RD1outdeep;
        if (RD1act < 0) RD1act = 0;
        RD2act = RD2act - RD2out - RD2outdeep;
        if (RD2act < 0) RD2act = 0;
        RG1act = RG1act - RG1out - RG1outdeep;
        if (RG1act < 0) RG1act = 0;
        RG2act = RG2act - RG2out - RG1outdeep;
        if (RG2act < 0) RG2act = 0;
        
        RD1actEnergy = RD1actEnergy - RD1out_Energy - RD1out_Tdeep;
        if (RD1actEnergy < 0) RD1actEnergy = 0;
        RD2actEnergy = RD2actEnergy - RD2out_Energy - RD2out_Tdeep;
        if (RD2actEnergy < 0) RD2actEnergy = 0;
        RG1actEnergy = RG1actEnergy - RG1out_Energy - RG1out_Tdeep;
        if (RG1actEnergy < 0) RG1actEnergy = 0;
        RG2actEnergy = RG2actEnergy - RG2out_Energy - RG2out_Tdeep;
        if (RG2actEnergy < 0) RG2actEnergy = 0;

        RD1act_N = RD1act_N - RD1out_N - RD1out_Ndeep;
        if (RD1act_N < 0) RD1act_N = 0;
        RD2act_N = RD2act_N - RD2out_N - RD2out_Ndeep;
        if (RD2act_N < 0) RD2act_N = 0;
        RG1act_N = RG1act_N - RG1out_N - RG1out_Ndeep;
        if (RG1act_N < 0) RG1act_N = 0;
        RG2act_N = RG2act_N - RG2out_N - RG2out_Ndeep;
        if (RG2act_N < 0) RG2act_N = 0;
        
        double channelStorage = RD1act + RD2act + RG1act + RG2act;
        double channelStorageEnergy = RD1actEnergy + RD2actEnergy + RG1actEnergy + RG2actEnergy;
        double channelStorage_N = RD1act_N + RD2act_N + RG1act_N + RG2act_N;
        
        double cumOutflow = RD1out + RD2out + RG1out + RG2out;
        double cumOutflowEnergy = RD1out_Energy + RD2out_Energy + RG1out_Energy + RG2out_Energy;
        double cumOutflow_N = RD1out_N + RD2out_N + RG1out_N + RG2out_N;
        
        
        
        simRunoff.setValue(cumOutflow);
        SimRunoffEnergy.setValue(cumOutflowEnergy);
        SimRunoff_N.setValue(cumOutflow_N);
        this.channelStorage.setValue(channelStorage);
        ChannelStorageEnergy.setValue(channelStorageEnergy);
        ChannelStorage_N.setValue(channelStorage_N);
        
        inRD1.setValue(0);
        inRD2.setValue(0);
        inRG1.setValue(0);
        inRG2.setValue(0);

        EnergyRD1_in.setValue(0);
        EnergyRD2_in.setValue(0);
        EnergyRG1_in.setValue(0);
        EnergyRG2_in.setValue(0);
                
        SurfaceN_in.setValue(0);
        InterflowN_sum.setValue(0);
        N_RG1_in.setValue(0);
        N_RG2_in.setValue(0);

        actRD1.setValue(RD1act);
        actRD2.setValue(RD2act);
        actRG1.setValue(RG1act);
        actRG2.setValue(RG2act);

        ActRD1Energy.setValue(RD1actEnergy);
        ActRD2Energy.setValue(RD2actEnergy);
        ActRG1Energy.setValue(RG1actEnergy);
        ActRG2Energy.setValue(RG2actEnergy);
                
        ActRD1_N.setValue(RD1act_N);
        ActRD2_N.setValue(RD2act_N);
        ActRG1_N.setValue(RG1act_N);
        ActRG2_N.setValue(RG2act_N);
                
        outRD1.setValue(RD1out);
        outRD2.setValue(RD2out);
        outRG1.setValue(RG1out);
        outRG2.setValue(RG2out);

        Energy_RD1_out.setValue(RD1out_Energy);
        Energy_RD2_out.setValue(RD2out_Energy);
        Energy_RG1_out.setValue(RG1out_Energy);
        Energy_RG2_out.setValue(RG2out_Energy);
        
        SurfaceNabs.setValue(RD1out_N);
        InterflowNabs.setValue(RD2out_N);
        N_RG1_out.setValue(RG1out_N);
        N_RG2_out.setValue(RG2out_N);

        watertempavg.setValue(Temp_tot);

        
/*        if(entity.getObject("to_reach") == null){
 
        System.out.println(RD1out + " RD1out " + RD2out + " RD2out "+ RG1out +" RG1out " + RG2out +" RG2out ");
 
        }*/
        if(!DestReach.isEmpty()){
            DestReach.setDouble("inRD1",RD1DestIn);
            DestReach.setDouble("inRD2",RD2DestIn);
            DestReach.setDouble("inRG1",RG1DestIn);
            DestReach.setDouble("inRG2",RG2DestIn);
            
            DestReach.setDouble("EnergyRD1_in", RD1DestInEnergy);
            DestReach.setDouble("EnergyRD2_in", RD2DestInEnergy);
            DestReach.setDouble("EnergyRG1_in", RG1DestInEnergy);
            DestReach.setDouble("EnergyRG2_in", RG2DestInEnergy);

            DestReach.setDouble("SurfaceN_in", RD1DestIn_N);
            DestReach.setDouble("InterflowN_sum", RD2DestIn_N);
            DestReach.setDouble("N_RG1_in", RG1DestIn_N);
            DestReach.setDouble("N_RG2_in", RG2DestIn_N);
            
        }else{
            
            catchmentRD1.setValue(RD1out);
            catchmentRD2.setValue(RD2out);
            catchmentRG1.setValue(RG1out);
            catchmentRG2.setValue(RG2out);
            catchmentSimRunoff.setValue(cumOutflow);
            
            catchmentEnergyRD1.setValue(RD1out_Energy);
            catchmentEnergyRD2.setValue(RD2out_Energy);
            catchmentEnergyRG1.setValue(RG1out_Energy);
            catchmentEnergyRG2.setValue(RG2out_Energy);
            catchmentSimRunoffEnergy.setValue(cumOutflowEnergy);
            catchmentSimRunoffTemp.setValue(cumOutflowEnergy / cumOutflow);

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
