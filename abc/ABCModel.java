package abc;

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
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter a"
            )
            public JAMSDouble a;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter b"
            )
            public JAMSDouble b;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Parameter c"
            )
            public JAMSDouble c;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "The initial storage content [S_0]"
            )
            public JAMSDouble initStor;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The precip input"
            )
            public JAMSDouble precip;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "The current time"
            )
            public JAMSCalendar time;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Simulated runoff"
            )
            public JAMSDouble simRunoff;
    
    /*
     *  Component run stages
     */
    private double storage_old;
    
    
    public void init() {
        getModel().getRuntime().println("INIT ABCModel");
        storage_old = initStor.getValue();
    }
    
    public void run() {
        
        double precip = this.precip.getValue();
        double runoff;
        
        double a = this.a.getValue();
        double b = this.b.getValue();
        double c = this.c.getValue();
        
        if(a+b > 1.0){
            System.out.println("Constraint violated: a + b is larger than 1.0");
            return;
        }
        
        runoff = (1 - a - b) * precip + c * storage_old;
        storage_old = a * precip + (1-c) * storage_old;
        
        this.simRunoff.setValue(runoff);
        getModel().getRuntime().println("Time: " + time.get(JAMSCalendar.YEAR) + " Runoff: " + runoff);
    }
    
    public void cleanup() {
        getModel().getRuntime().println("CLEANUP ABCModel");
    }
    
}
