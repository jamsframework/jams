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
@JAMSComponentDescription(title = "Calculation of irrigation input ",
author = "c8fima",
description = "Calculation of irrigation input water ")
public class Irrigationinput_lumped extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "net amount of rain [l]")
    public Attribute.Double netRain;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU irrigationdemand [l]",
    defaultValue = "0")
    public Attribute.Double irrigation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "Irrigation Storage (available water) [l]")
    public Attribute.Double storage;

    /*     @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class"
    )
    public Attribute.Double area;*/
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU irrigation Waterinput  [l]")
    public Attribute.Double irrigationAct;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU Waterinput rain + irrigation [l]")
    public Attribute.Double Waterinput;

    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double re_use;*/

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "part of the irrigation demad satisfyed by the available water [-]")
    public Attribute.Double irrigationpart;
   

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sum of actual Irrigation [l]"
            )
            public Attribute.Double irrigationActsum;


    public void init() {
        // irrigation.setValue(0);
    }

//Berechnung
    public void run() {
        double rain = netRain.getValue();
        double Irrsoll = irrigation.getValue();
        double part = irrigationpart.getValue();
        double Storage = storage.getValue();
        double IrrigationActsum = irrigationActsum.getValue();

        double irract = 0;
        double x = 0;



        //Demand gap with rain

        //double Demandgap = Irrsoll * (1 - part);




        //Irrsoll kommt bereits in litern


        x = (Irrsoll * part);
        
        /* //consideration of the precip in the irrigation amount
        
        x = x - rain;*/

        if (x <= 0) {
            x = 0;
        }


        //Storage = (Storage);   // Umrechnng in liter

        if (Storage >= x) {
            irract = x;

        } else {
            irract = Storage;

        }



        //Storage = Storage - (irract*re_use.getValue());
        Storage = Storage - irract;
        irrigationAct.setValue(irract);
        IrrigationActsum = IrrigationActsum + irract;

//        Calculation of N-Amount

       

        //Storage = (Storage);   //zurückrechznung in m3/s
        //System.out.println("Storage " + Storage);


        storage.setValue(Storage);

        Waterinput.setValue(rain + irract);
        
        irrigationActsum.setValue(IrrigationActsum);
        
        

    }
}
