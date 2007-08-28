package jams.components.demomodel;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
title="ABCModel",
        author="Fiering, M.B.",
        description="The abc model is a simple linear model relating precipitation to streamflow on an annual basis. It " +
        "was developed by Fiering (1967), purely for educational purposes. The model is a simple water " +
        "balance calculation assuming that losses to evaporation and transpiration can simply be described " +
        "by a constant factor, while the watershed generally is assumed to behave like a linear reservoir. " +
        "The abc model has the following form: Qt = (1 – a – b)Pt + cSt-1 " +
        "where Q is the streamflow, P is the precipitation, a is a parameter describing the fraction of" +
        "precipitation that percolates through the soil to the groundwater, b is a parameter describing the" +
        "fraction of precipitation directly lost to the atmosphere through evapotranspiration, and c is a" +
        "parameter describing the amount of groundwater that leaves the aquifer storage S and drains into" +
        "the stream. The index t describes the year (t=1,2,…,N). Streamflow, precipitation and storage are" +
        "measured in volume units so that the additive relations derived are dimensionally homogeneous. " +
        "The groundwater storage at the end of the year t is: St = aPt + (1 – c)St-1" +
        "The following constraints are required:" +
        "0 < a,b,c < 1 ," +
        "0 < a + b < 1 ," +
        "Pt, St > 0"
        )
        public class ABCModel extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Parameter a"
            )
            public JAMSDouble a;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Parameter b"
            )
            public JAMSDouble b;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Parameter c"
            )
            public JAMSDouble c;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The initial storage content"
            )
            public JAMSDouble initStorage;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The precip input"
            )
            public JAMSDouble precip;
        
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current storage content"
            )
            public JAMSDouble storage;

    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Simulated runoff"
            )
            public JAMSDouble simRunoff;
    
    /*
     *  Component run stages
     */    
    
    public void init() {
        storage.setValue(initStorage.getValue());
    }
    
    public void run() {
    System.out.println(storage);
        double precip = this.precip.getValue();
        double runoff;
        
        double a = this.a.getValue();
        double b = this.b.getValue();
        double c = this.c.getValue();
        double storage = this.storage.getValue();
        
        if(a+b > 1.0){
            getModel().getRuntime().println("Constraint violated: a + b is larger than 1.0");
            return;
        }
                
        runoff = (1 - a - b) * precip + c * storage;
        this.storage.setValue(a * precip + (1-c) * storage);
        
        simRunoff.setValue(runoff);
    }
    
    public void cleanup() {
    }
    
}
