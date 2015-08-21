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
@JAMSComponentDescription(title = "Calculation of irrigation input",
author = "c6gohe2",
description = "Calculation of irrigation input")
public class IrrigationinputGW extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in [-] plant growth water stress factor")
    public Attribute.Double netRain;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU crop class",
    defaultValue = "0")
    public Attribute.Double irrigation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU crop class")
    public Attribute.Double Waterinput;


    

    public void init() {
        irrigation.setValue(0);
    }

//Berechnung
    public void run() {
        double rain = netRain.getValue();
        double runirrigation = irrigation.getValue();
        

        Waterinput.setValue(rain + runirrigation);

    }
}
