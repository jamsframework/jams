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
 * 
 */
@JAMSComponentDescription(title = "J2KContourBanks_layerinterface_out",
author = "Manfred Fink",
description = "Interface for the layered soilwater modul after the calculation of the contour banks")
public class J2KContourBNaCl_layerinterface_out extends JAMSComponent {


    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow NaCl")
    public Attribute.DoubleArray inRD2_Nacl;
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow")
    public Attribute.DoubleArray inRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU statevar RD2 inflow procuced by the contourbanks module")
    public Attribute.Double inRD2_CB;
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
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "RD2 outflow")
    public Attribute.DoubleArray outRD2;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "RD2 outflow from contour banks")
    public Attribute.Double outRD2cb;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "surface runoff leaving the HRU without Conturbanks",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double RD1_out_old;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "surface runoff leaving the HRU goes into Conturbanks",
    unit = "l",
    lowerBound = 0,
    upperBound = 100000000)
    public Attribute.Double RD1_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "conturbanks outflow",
    unit = "l")
    public Attribute.Double CB_outflow;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "conturbanks storage",
    unit = "l")
    public Attribute.Double CB_storage;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = " NaCl in surface runoff in kgNaCl/ha")
    public Attribute.Double SurfaceNaCl;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = " NaCl outflow in interflow in kgNaCl/ha")
    public Attribute.DoubleArray InterflowNaCl;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = " NaCl inflow in interflow in kgNaCl/ha")
    public Attribute.DoubleArray InterflowNaCl_in;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = " NaCl in Contour Banks storage in kgNaCl/ha")
    public Attribute.Double NaCl_CB;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "conturbanks NaCl outflow in kgNaCl",
    unit = "l")
    public Attribute.Double NaCl_CB_out;
    
    

    public void init() {
    }
    
    
    
    public void run() {
        Attribute.Entity actHRU;
        actHRU = hrus.getCurrent();
        int i = 0;
        int imax = (int) Layer.getValue();

        double[] runoutRD2 = new double[imax];
        double[] runInterflowNaCl = new double[imax];
        double[] runInterflowNaCl_in = new double[imax];
        double[] runinRD2 = new double[imax];
        double sumRD2 = 0;
        double sumRD2_NaCl = 0;



        while (i < imax) {

            runoutRD2[i] = outRD2.getValue()[i];
            runinRD2[i] = inRD2.getValue()[i];
            runInterflowNaCl[i] = InterflowNaCl.getValue()[i];
            runInterflowNaCl_in[i] = InterflowNaCl_in.getValue()[i];

            if (i < layermax.getValue()) {
                runoutRD2[i] = 0;
                runInterflowNaCl[i] = 0;
                sumRD2 =  sumRD2 + outRD2.getValue()[i];
                sumRD2_NaCl = sumRD2_NaCl + runInterflowNaCl[i];
            } else if (i == layermax.getValue()) {
                runoutRD2[i] = outRD2cb.getValue();
                sumRD2 =  sumRD2 + outRD2.getValue()[i];
                sumRD2_NaCl = sumRD2_NaCl + runInterflowNaCl[i];
                runinRD2[i] = runinRD2[i] + inRD2_CB.getValue();

            }
            i++;
        }


        // in means before the Contour banks module, out afterwards



        double watersum = sumRD2 + RD1_out_old.getValue() + CB_storage.getValue() + inRD2_CB.getValue() + 1.e-10;
        double NaClsum =  sumRD2_NaCl + SurfaceNaCl.getValue() + NaCl_CB.getValue();
        
        double CB_conc = NaClsum / watersum;

        double NaClrunoff_CB = CB_outflow.getValue() * CB_conc;
        double runoutRD2sum_NaCl = outRD2cb.getValue() * CB_conc;
        double runoutRD1_NaCl = RD1_out.getValue() * CB_conc;
        double runinRD2_NaCl = inRD2_CB.getValue() * CB_conc;

        NaCl_CB_out.setValue(NaClrunoff_CB);

        runInterflowNaCl[(int)layermax.getValue()] = runoutRD2sum_NaCl;
        runInterflowNaCl_in[(int)layermax.getValue()] = runinRD2_NaCl;

        InterflowNaCl.setValue(runInterflowNaCl);
        InterflowNaCl_in.setValue(runInterflowNaCl_in);
        outRD2.setValue(runoutRD2);
        SurfaceNaCl.setValue(runoutRD1_NaCl);
        actHRU.setDouble("cbrunofReachNaCl", NaClrunoff_CB);
        inRD2_CB.setValue(0);



    }
}
