/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n;

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import org.jams.j2k.s_n.crop.J2KSNCrop;

/**
 *
 * @author c6gohe2
 */
@JAMSComponentDescription(
        title="J2KPlantGrowthNitrogenStress",
        author="Manfred Fink",
        description="Calculation of the plant groth nitrogen factor after SWAT"
        )
        public class IrrigationStorage extends JAMSComponent {

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
    public Attribute.DoubleArray maxMPS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU attribute maximum LPS")
    public Attribute.DoubleArray maxLPS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of MPS")
    public Attribute.DoubleArray satMPS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READWRITE,
    description = "HRU state var saturation of LPS")
    public Attribute.DoubleArray satLPS;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "HRU crop class")
    public Attribute.Double irrigation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double wst_thr;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double param2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "HRU crop class")
    public Attribute.Double cropid;

     //Berechnung
    public void run() throws Attribute.Entity.NoSuchAttributeException{
        Attribute.Entity entity = entities.getCurrent();
        double wst = wstrs.getValue();
        double satMPSArray[] = satMPS.getValue();
        double maxMPSArray[] = maxMPS.getValue();        
        double wstthr = wst_thr.getValue();
        double param = param2.getValue();
        double irr = 0;
        double cropID = cropid.getValue();
        double red = 0;

        if (cropID == 12) {
            irr = 0;
        } else {
            if (cropID == 98) {
                irr = 0;
            } else {
                if (wst <= wstthr) {
                    for (int i = 0; i < maxMPSArray.length; i++) {
                        irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * param);
                    }

                } else {
                    irr = 0;
                }

            }
        }

        if (cropID == 8){
               red = (1-wstthr);
               wstthr = wstthr+((red/2));

            if (wst <= wstthr) {
                    for (int i = 0; i < maxMPSArray.length; i++) {
                        irr += ((maxMPSArray[i] - (satMPSArray[i] * maxMPSArray[i])) * param);

                    }
                    irr = irr*0.25;
            }}

        irrigation.setValue(irr);

    }}
