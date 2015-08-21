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
        public class Qsim_irr_reach extends JAMSComponent {


 @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
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
            description = "Irrigationamount in l"
            )
            public Attribute.Double irrigationsum;
 
 
 


    public void run() {
        
        Double simrunoff = catchmentSimRunoff.getValue();
        
        if (simrunoff > irrigationsum.getValue()){
        
        simrunoff = simrunoff - irrigationsum.getValue();
        
        }else{
        
            irrigationsum.setValue(simrunoff);
            simrunoff = 0.0;
        
        }
        
        catchmentSimRunoff.setValue((Storage.getValue()) +  simrunoff);
        
    }
}
