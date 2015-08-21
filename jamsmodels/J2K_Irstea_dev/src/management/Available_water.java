/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package management;



import jams.data.*;
import jams.model.*;
import java.util.ArrayList;

/**
 *
 * @author tilmant
 * 
 */

@JAMSComponentDescription(title = "Calculation of irrigation water N-concentration",
author = "c8fima",
description = "Calculation of irrigation water N-concentration")


public class Available_water extends JAMSComponent {
    

    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
  
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
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Available water"
            )
            public Attribute.DoubleArray storageArray;

@JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Reservoir where we take water"
            )
            public Attribute.Double storageInput;

      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Factor which controls the amount of water can be used by irrigation (0 - 1) [-]"
            )
            public Attribute.Double availability; 

      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "reach ID"
            )
            public Attribute.Double ID;
      @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "The reach collection"
            )
    public Attribute.EntityCollection entities;
    

//Berechnung
    public void init(){
    //    irrigationpart.setValue(0.0);
    //    storageInput.setValue(0);

    }
    public void run (){

//        entities.length;
        double irrstorage = storageInput.getValue() * availability.getValue(); 
        double[] irrStoTab = {};
        
        double runBypassfactor = Bypassfactor.getValue();
                
        irrstorage = irrstorage * (1 - runBypassfactor);
        
        if( irrstorage == 0) {
            irrstorage = 0.00000001;
        }
        int t=0;
        
     //   irrStoTab[1] = irrstorage;
           
        storageArray.setValue(irrStoTab);
        t++;
   

    }
}


