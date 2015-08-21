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
@JAMSComponentDescription(title = "Calculation of irrigation water N-concentration",
author = "c8fima",
description = "Calculation of irrigation water N-concentration")
public class irigation_amount extends JAMSComponent {



    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Irrigation water which is not used for irrigation but left in the irrigation channel [l]"
            )
            public Attribute.Double Bypasswater;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Irrigation water which is damped in the irrigation channel [l]"
            )
            public Attribute.Double Bypassrest;

    
    

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationsum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationpart;
    
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Portion of the irrigation which bypasses the fields and goes directly into the dainage cannel [0 - 100] default = 0"
            )
            public Attribute.Double Bypassfactor;
   
     @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor for the delay of the irrigation which bypasses the fields and goes directly into the dainage cannel [1 - 100] default = 1"
            )
            public Attribute.Double Bypassdamping;
   
   
   @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Portion of the irrigation water depending on the demand [0 - 100] default = 0"
            )
            public Attribute.Double IrrWaterfactor;

 


//Berechnung
    public void init(){
        irrigationpart.setValue(0.0);
        irrigationsum.setValue(0.0);
        Bypassrest.setValue(0);
        
        
    }
    public void run (){

        double irripart = 0;
        double bypass = 0;
        double bypassact = 0;
        

         

        double irrigationwater = (irrigationsum.getValue() * IrrWaterfactor.getValue());
        
        bypass = irrigationwater * Bypassfactor.getValue() + Bypassrest.getValue();
        
        bypassact = bypass / Bypassdamping.getValue();
        
        Bypassrest.setValue(bypass - bypassact);
        
        
        
        
        Bypasswater.setValue(bypassact);

        if (irrigationwater > 0){
            irripart = irrigationsum.getValue()/ irrigationwater ;
        }else{
            irripart = 0;
        }
        
        irrigationpart.setValue(irripart);



        irrigationsum.setValue(0.0);


    }



}