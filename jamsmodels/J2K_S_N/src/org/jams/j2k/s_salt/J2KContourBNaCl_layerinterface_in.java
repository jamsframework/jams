/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_salt;

import jams.data.*;
import jams.model.*;
import java.lang.Math.*;

/**
 *
 * @author manfred fink
 */
@JAMSComponentDescription(title = "J2KContourBanks_layerinterface",
author = "Manfred Fink",
description = "Interface for the layered soilwater modul before the calculation of the contour banks")
public class J2KContourBNaCl_layerinterface_in extends JAMSComponent {


    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "An- bzw. Ausschalten des Moduls")
    public Attribute.Boolean cbModulAktiv;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "The current hru entity")
    public Attribute.EntityCollection hrus;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "actual LPS water content",
    unit = "l")
    public Attribute.DoubleArray actLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "maximum LPS water content",
    unit = "l")
    public Attribute.DoubleArray maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in cm/d soil hydraulic conductivity")
    public Attribute.DoubleArray kf_h;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "interflow reaching the HRU in every layer",
    unit = "L",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.DoubleArray RD2_out;
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
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "interflow reaching the HRU in the layers affected by the trench",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double RD2_outsum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "interflow salt reaching the HRU in the layers affected by the trench",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double RD2_outsumNaCl;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "average soil saturation of the layers affected by the trench",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double avgsatsoil;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD2 NaCl inflow in kgNaCl")
    public Attribute.DoubleArray InterflowNaCl_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "surface runoff leaving the HRU without Conturbanks",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double RD1_out_old;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "surface runoff leaving the HRU goes into Conturbanks",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double RD1_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "saturated conductivity of the Conturbanks",
    unit = "cm d^-1",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double Ks_bottom;
    boolean modulCBaktiv;

    public void init() {
        Attribute.Entity actHRU;
        double runsumlayer = 0;
        //which Layer are cutted by the trench 
        int i = 0;
        int runlayermax = 0;
        double run_kf_h = 0;

        actHRU = hrus.getCurrent();

        double tiefeGraben_vorn = actHRU.getDouble("cbGrabentiefe_vorn") * 100; //in cm

        while (i < Layer.getValue()) {
            if (runsumlayer < tiefeGraben_vorn) {
                runsumlayer = runsumlayer + layerdepth.getValue()[i];
                runlayermax = i;
                run_kf_h = kf_h.getValue()[i] / 100;
            }

            i++;
        }
        sumlayer.setValue(runsumlayer);
        layermax.setValue(runlayermax);
        actHRU.setDouble("kf_max", run_kf_h);
        Ks_bottom.setValue(run_kf_h);

    }

    public void run() {
        Attribute.Entity actHRU;
        int i = 0;
        double sumactLPS = 0;
        double summaxLPS = 0;
        double sumRD2_out = 0;
        double sumRD2_NaCl_out = 0;
        actHRU = hrus.getCurrent();
        double run_kf_h = 0;
        //calculate soil average saturation and interflow sum in layers affected by the trench

        while (i <= layermax.getValue()) {
            sumactLPS = sumactLPS + actLPS.getValue()[i];
            summaxLPS = summaxLPS + maxLPS.getValue()[i];
            sumRD2_out = sumRD2_out + RD2_out.getValue()[i];
            sumRD2_NaCl_out = sumRD2_NaCl_out + InterflowNaCl_out.getValue()[i];
            run_kf_h = kf_h.getValue()[i] / 100;

            i++;
        }

        avgsatsoil.setValue(sumactLPS / summaxLPS);
        RD2_outsum.setValue(sumRD2_out);
        RD2_outsumNaCl.setValue(sumRD2_NaCl_out);
        RD1_out_old.setValue(RD1_out.getValue());
        actHRU.setDouble("kf_max", run_kf_h);


    }
}
