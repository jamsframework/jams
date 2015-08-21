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
public class calc_irigation_lumped extends JAMSComponent {

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double storageInput;

 
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Fixed minimal bypass factor 0 - 1 [-], default 0"
            )
            public Attribute.Double Bypassfactor;
        
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Water amount in bypass [l]"
            )
            public Attribute.Double Bypasswater;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double storage;



    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU crop class"
            )
            public Attribute.Double irrigationsum;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "available water for irrigation"
            )
            public Attribute.Double irrigationpart;
    
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor which controls the amount of water can be used by irrigation (0 - 1) [-]"
            )
            public Attribute.Double availability;

    

//Berechnung
    public void init(){
    //    irrigationpart.setValue(0.0);
    //    storageInput.setValue(0);
    }
    public void run (){

        double irripart = 0;

        double run_storage = storage.getValue();

 //       double irrstorage = (storageInput.getValue()*1000); // from m³/day to l/day
        double irrstorage = storageInput.getValue() * availability.getValue(); 
 //       double irrifactor = irrigationsum.getValue()/irrstorage;
        
 //       irrifactor = Math.min(irrifactor, 1);
        
        double runBypassfactor = Bypassfactor.getValue(); //*(1-irrifactor);
        
        
        irrstorage = irrstorage * (1 - runBypassfactor);
        
        Bypasswater.setValue(irrstorage * runBypassfactor);
        if( irrstorage == 0) {
            irrstorage = 0.00000001;
        }
        irripart = irrigationsum.getValue()/irrstorage;

        

        if (irripart > 1) {
            irripart = 1;
        }

        irrigationpart.setValue(irripart);



        irrigationsum.setValue(0.0);
        
        storage.setValue(irrstorage + run_storage);


    }



}