/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unijena.j2k.soilWater;

import jams.data.*;
import jams.model.*;
import java.lang.Math.*;

/**
 *
 * @author manfred fink
 * 
 */
@JAMSComponentDescription(
title="J2KContourBanks_layerinterface_out",
        author="Manfred Fink",
        description="Interface for the layered soilwater modul after the calculation of the contour banks"
        )
public class J2KContourBanks_layerinterface_out extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Boolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "actual LPS water content",
    unit = "l")
    public Attribute.DoubleArray actLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "maximum LPS water content",
    unit = "l")
    public Attribute.DoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = " number of soil layers",
    unit = "-",
    lowerBound = 0,
    upperBound = 100)
    public Attribute.Double Layer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "depth of soil layer",
    unit = "cm",
    lowerBound = 0,
    upperBound = 10000)
    public Attribute.DoubleArray layerdepth;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "depth of soil layers cutted by the trench",
    unit = "cm",
    lowerBound = 0,
    upperBound = 10000)
    public Attribute.Double sumlayer;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "number of the deepest soil layers cutted by the trench",
    unit = "-",
    lowerBound = 0,
    upperBound = 100)
    public Attribute.Double layermax;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "average LPSsoil saturation in soil layers cutted by the trench",
    unit = "-",
    lowerBound = 0,
    upperBound = 1)
    public Attribute.Double avgsatsoil;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "RD2 outflow")
    public Attribute.DoubleArray outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "RD2 outflow from contour banks")
    public Attribute.Double outRD2cb;
    boolean modulCBaktiv;
    double kalibZufluss;

    public void init() {
      
    }

    public void run() {

        int i = 0;
        int imax = (int)Layer.getValue();
        
        double[]  runoutRD2 = new double[imax];


        

        while (i <= imax) {

            runoutRD2[i] = outRD2.getValue()[i];

            if (i < layermax.getValue()) {
                runoutRD2[i] = 0;
            }else if(i == layermax.getValue()){
                runoutRD2[i] = outRD2cb.getValue();
            }
            
        }
        
        outRD2.setValue(runoutRD2);


    }
}
