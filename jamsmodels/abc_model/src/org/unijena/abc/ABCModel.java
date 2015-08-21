/*
 * ABCModel.java
 *
 * Created on 18. Mai 2006, 15:20
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.abc;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;

/**
 *
 * @author c0krpe
 */
@JAMSComponentDescription(
        title="ABCModel",
        author="Peter Krause",
        description="The abc model is a simple linear model relating precipitation to streamflow on an annual basis. It " +
                    "was developed by Fiering (1967), purely for educational purposes. The model is a simple water " +
                    "balance calculation assuming that losses to evaporation and transpiration can simply be described " +
                    "by a constant factor, while the watershed generally is assumed to behave like a linear reservoir. " +
                    "The abc model has the following form: Qt = (1  a  b)Pt + cSt-1 " +
                    "where Q is the streamflow, P is the precipitation, a is a parameter describing the fraction of" +
                    "precipitation that percolates through the soil to the groundwater, b is a parameter describing the" +
                    "fraction of precipitation directly lost to the atmosphere through evapotranspiration, and c is a" +
                    "parameter describing the amount of groundwater that leaves the aquifer storage S and drains into" +
                    "the stream. The index t describes the year (t=1,2,,N). Streamflow, precipitation and storage are" +
                    "measured in volume units so that the additive relations derived are dimensionally homogeneous. " +
                    "The groundwater storage at the end of the year t is: St = aPt + (1  c)St-1" +
                    "The following constraints are required:" +
                    "0 < a,b,c < 1 ," +
                    "0 < a + b < 1 ," +
                    "Pt, St > 0"+
                    "Adaptation of original model:"+
                    "The actual adaptation allows to read in a potential ET rate. If this is the case this ET rate will be"+
                    "used to calculate the actual ET. In this case the parameter b will have no influence any longer."+
                    "If no ET is read in the original equation [et = b * precip] will be used."
        )
        public class ABCModel extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Parameter a"
            )
            public Attribute.Double a;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Parameter b"
            )
            public Attribute.Double b;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Parameter c"
            )
            public Attribute.Double c;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the initial storage content [S_0]"
            )
            public Attribute.Double initStor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "the storage at the time step before [S_t-1]"
            )
            public Attribute.Double storageTm1;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sonnenschein tage"
            )
            public Attribute.Double sunh;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the precip input"
            )
            public Attribute.Double precip;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "the potential ET"
            )
            public Attribute.Double pET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the base flow output"
            )
            public Attribute.Double simBaseFlow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the direct flow output"
            )
            public Attribute.Double simDirectFlow;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the et output"
            )
            public Attribute.Double simET;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "the baseflow + directflow output"
            )
            public Attribute.Double simRunoff;
    
    /*
     *  Component run stages
     */
    
    public void init() throws Attribute.Entity.NoSuchAttributeException {
        //System.out.println("INIT ABCModel");
        this.storageTm1.setValue(this.initStor.getValue());
        this.pET.setValue(0);
        if(a.getValue()+b.getValue() > 1.0){
            System.out.println("Constraint violated: a + b is larger than 1.0");
            return;
        }
    }
    
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        //System.out.println("RUN ABCModel");
        double storageTm1 = this.storageTm1.getValue();
        double precip = this.precip.getValue();
        double runoff = 0;
        
        
        double a = this.a.getValue();
        double b = this.b.getValue();
        double c = this.c.getValue();
        
        //new et
        double precipIn = precip;
        double aET = 0;
        
        double pET = this.pET.getValue();
        if(pET > 0){
            if(precip > pET){
                aET = pET;
                precip = precip - pET;
            } else{
                aET = precip;
                precip = 0;
            }
        }
        else{
            aET = b * precip;
        }
        double infiltration = a * precip;
        double q_dir = precipIn - aET - infiltration;
        
        //storageTm1 = infiltration + storageTm1;
        
        double q_bas = c * storageTm1;
        
        //storageTm1 = storageTm1 - q_bas;
        
        storageTm1 = infiltration + storageTm1 - q_bas;
        //runoff = ( 1 - a - b) * precip + c * storageTm1;
        //storageTm1 = a * precip + (1-c) * storageTm1;
        
        this.simET.setValue(aET);
        this.simBaseFlow.setValue(q_bas);
        this.simDirectFlow.setValue(q_dir);
        this.simRunoff.setValue(q_dir + q_bas);
        this.storageTm1.setValue(storageTm1);
    }
    
    public void cleanup() {
        
    }
    
}
