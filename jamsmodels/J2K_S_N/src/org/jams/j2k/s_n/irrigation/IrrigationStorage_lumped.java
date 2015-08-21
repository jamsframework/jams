/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.irrigation;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import java.util.Calendar;
import org.jams.j2k.s_n.crop.J2KSNCrop;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(title = "IrrigationStorage",
author = "c6gohe2",
description = "IrrigationStorage calculation of irrigation demand")
public class IrrigationStorage_lumped extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current organic fertilizer amount")
    public Attribute.Integer RotPos;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current hru object")
    public Attribute.EntityCollection entities;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "in [-] plant growth water stress factor")
    public Attribute.Double wstrs;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum MPS")
    public Attribute.Double maxMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum LPS")
    public Attribute.Double maxLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU state var saturation of MPS")
    public Attribute.Double satMPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU state var saturation of LPS")
    public Attribute.Double satLPS;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU crop class")
    public Attribute.Double irrigation;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU crop class")
    public Attribute.Double irrigationsum;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Waterstress thershold post season 0-1 (-)")
    public Attribute.Double wst_thr_out;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Waterstress thershold 0-1 (-)")
    public Attribute.Double wst_thr;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Irrigation multiplier (-)")
    public Attribute.Double Irr_mult;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Start of the Irrigation season in day of the year (-)")
    public Attribute.Double Irr_Start;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "End of the Irrigation season in day of the year (-)")
    public Attribute.Double Irr_End;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Exponent of the water use efficiency function (-)")
    public Attribute.Double Eff_exp;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "reduction factor for dripper irrigation (-)")
    public Attribute.Double dripperfactor;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double cropid;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Current time")
    public Attribute.Calendar time;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "Random intervall for the demandfactor (-)")
    public Attribute.Double rand_intervall;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Test value for water use efficiency function (-)")
    public Attribute.Double Eff_test;
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "LID which will be irrigated")
    public Attribute.Double IrrLid;

    //Berechnung
    public void run() {
        Attribute.Entity entity = entities.getCurrent();
        double wst = 1 - wstrs.getValue();


        double satMPSrun = satMPS.getValue();
        double maxMPSrun = maxMPS.getValue();
        double satLPSrun = satLPS.getValue();
        double maxLPSrun = maxLPS.getValue();
        double wstthr = 0;


        int act = time.get(Calendar.DAY_OF_YEAR);

        /*
         * if (act < change.getValue()){ wstthr = wst_thr.getValue(); }else{
         * wstthr = wst_thr_post.getValue(); }
         */
        double irr_intervall = Irr_End.getValue() - Irr_Start.getValue();
        double irr_center = irr_intervall / 2 + Irr_Start.getValue();
        double exp = Eff_exp.getValue();
        double act_irr = act - Irr_Start.getValue();

        double a_irr = 0;
        double b_irr = 0;
        double half_exp = Math.pow(irr_intervall / 2, exp);

        if (act < Irr_Start.getValue()) {
            wstthr = wst_thr_out.getValue();
        } else if (act < irr_center) {

            a_irr = (irr_intervall / 2) - act_irr;

            b_irr = (half_exp - Math.pow(a_irr, exp)) / half_exp;

            wstthr = ((wst_thr.getValue() - wst_thr_out.getValue()) * b_irr) + wst_thr_out.getValue();

        } else if (act <= Irr_End.getValue()) {

            a_irr = act_irr - (irr_intervall / 2);

            b_irr = (half_exp - Math.pow(a_irr, exp)) / half_exp;

            wstthr = ((wst_thr.getValue() - wst_thr_out.getValue()) * b_irr) + wst_thr_out.getValue();

        } else if (act > Irr_End.getValue()) {
            wstthr = wst_thr_out.getValue();
        }


        double Irr_mul = Irr_mult.getValue();
        double irrsum = irrigationsum.getValue();
        double irr = 0;
        double cropID = cropid.getValue();
       

        if (wstthr == 0) {
        } else {

            double random = (rand_intervall.getValue() * Math.random()) - (rand_intervall.getValue() / 2);



            wstthr = wstthr + random;
            if (wstthr < 0) {
                wstthr = 0;
            } else if (wstthr > wst_thr.getValue()) {
                wstthr = wst_thr.getValue();
            }





            if (cropID != IrrLid.getValue()) {
                irr = 0;
            } else {
                if (wst < wstthr) {
                    irr = ((maxMPSrun - (satMPSrun * maxMPSrun)) * Irr_mul);
                    irr += ((maxLPSrun - (satLPSrun * maxLPSrun)) * Irr_mul);

                } else {
                    irr = 0;
                }

            }
        }

        
    
    irrsum  = irrsum + irr;

    irrigation.setValue (irr);

    irrigationsum.setValue (irrsum);

    Eff_test.setValue (wstthr);
}
}
