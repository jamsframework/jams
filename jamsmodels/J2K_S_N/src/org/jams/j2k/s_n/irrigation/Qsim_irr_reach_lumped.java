/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jams.j2k.s_n.irrigation;
import jams.data.*;
import jams.model.*;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(
        title="Qsim_irr_reach",
        author="c8fima",
        description="adding of irrigation bypass water to the river + remove of irrigation water from the flowing river water"
        )
        public class Qsim_irr_reach_lumped extends JAMSComponent {


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reststorage from irrigation in l"
            )
                public Attribute.Double Storage;


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchment SimRunoff in l"
            )
            public Attribute.Double catchmentSimRunoff;
 
@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchment RD1 in l"
            )
            public Attribute.Double catchmentRD1;

@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchment RD2 in l"
            )
            public Attribute.Double catchmentRD2;

@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchment RG1 in l"
            )
            public Attribute.Double catchmentRG1;

@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "catchment RG2 in l"
            )
            public Attribute.Double catchmentRG2;
 
 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Irrigationamount in l"
            )
            public Attribute.Double irrigationsum;
 
 
 


    public void run() {
        
        Double simrunoff = catchmentSimRunoff.getValue();
        
        
        double ratio = (simrunoff - irrigationsum.getValue()) /  simrunoff; 
        
        if (simrunoff > irrigationsum.getValue()){
        
        simrunoff = simrunoff - irrigationsum.getValue();
       
        
        }else{
        
            
            simrunoff = 0.0;
            ratio = 0.0;
        
        }
        
        catchmentSimRunoff.setValue((Storage.getValue()) +  simrunoff);
        Storage.setValue(0.0);
        irrigationsum.setValue(0.0);
        catchmentRD1.setValue(catchmentRD1.getValue()*ratio);
        catchmentRD2.setValue(catchmentRD2.getValue()*ratio);
        catchmentRG1.setValue(catchmentRG1.getValue()*ratio);
        catchmentRG2.setValue(catchmentRG2.getValue()*ratio);
       
        
        
    }
}
