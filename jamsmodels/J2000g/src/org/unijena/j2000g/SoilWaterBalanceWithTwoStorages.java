/*
 * SoilWaterBalance.java
 * Created on 25. October 2006, 13:21
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, c0krpe
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 */
package org.unijena.j2000g;

import jams.data.*;
import jams.model.*;

/**
 *
 * @author Peter Krause, Christian Fischer
 */
@JAMSComponentDescription(
        title = "SoilWaterBalance",
        author = "Peter Krause, Christian Fischer",
        description = "Calculates a simplified soil water balance for each HRU including a maximum infiltration limit"
)
public class SoilWaterBalanceWithTwoStorages extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute slope",
            unit = "°",
            lowerBound = 0.0,
            upperBound = 90.0
    )
    public Attribute.Double slope;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "attribute area",
            unit = "m²",
            lowerBound = 0.0
    )
    public Attribute.Double area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum MPS",
            defaultValue = "Infinity",
            unit = "mm",
            lowerBound = 0.0
    )
    public Attribute.Double maxMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var actual MPS",
            unit = "mm",
            lowerBound = 0.0
    )
    public Attribute.Double actMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU state var relative saturation of MPS",
            unit = "-",
            lowerBound = 0.0,
            upperBound = 1.0
    )
    public Attribute.Double satMPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "HRU excess storage",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double excStor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "infiltration capacity",
            defaultValue = "Infinity",
            unit = "mm",
            lowerBound = 0.0
    )
    public Attribute.Double maxInf;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "direct runoff",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double dirQ;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "groundwater recharge",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double gwRecharge;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "total runoff",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double totQ;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "potential ET",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double potET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual ET",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double actET;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precipitation",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "snow melt",
            unit = "L",
            lowerBound = 0.0
    )
    public Attribute.Double snowMelt;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "lateral-vertical distribution coefficient",
            unit = "-",
            lowerBound = 0.0
    )
    public Attribute.Double latVertDist;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "lateral recession constant",
            unit = "1/t",
            defaultValue = "1.0",
            lowerBound = 0.0
    )
    public Attribute.Double recConst;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "ET reduction factor",
            unit = "-",
            lowerBound = 0.0,
            upperBound = 1.0
    )
    public Attribute.Double linETRed;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum excStor",
            unit = "mm",
            lowerBound = 0.0,
            defaultValue = "Infinity"
    )
    public Attribute.Double maxExcStor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "infiltration excess flow",
            unit = "mm",
            lowerBound = 0.0
    )
    public Attribute.Double infExcStor;


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "HRU attribute maximum percolation",
            unit = "mm",
            lowerBound = 0.0
    )
    public Attribute.Double maxPerc;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "PET adjustment factor",
            unit = "-",
            lowerBound = 0.0
    )
    public Attribute.Double petMult;


    protected double calculateMaxExcStorage() {
        return this.maxExcStor.getValue() * this.area.getValue();
    }
    
    protected double calculateMaxInfiltrationCapacity() {
        return this.maxInf.getValue() * this.area.getValue();
    }

    protected void calculatePotET() {
        this.potET.setValue(potET.getValue() * petMult.getValue());
    }

    protected void putInflowToSoil() {
        double actMPS = this.actMPS.getValue();
        double maxMPS = this.maxMPS.getValue();
        double infCap = calculateMaxInfiltrationCapacity();
        double infExcStor = this.infExcStor.getValue();
        double excStor = this.excStor.getValue();
        double inflow = this.precip.getValue() + this.snowMelt.getValue();
        
        //water deficit
        double deltaMPS = maxMPS - actMPS;

        //MPS is able to store all water
        if (inflow <= deltaMPS) {
            //all water can infiltrate
            if (inflow <= infCap) {
                actMPS = actMPS + inflow;
                infCap = 0;
                inflow = 0;
                //some infiltratio excess is generated
            } else {
                actMPS = actMPS + infCap;
                inflow -= infCap;
                infCap = 0;
                infExcStor = inflow;
                inflow = 0;
            }
        } //MPS is not able to store all water
        else {
            //no infiltratio excess is generated
            if (deltaMPS <= infCap) {
                inflow = inflow - deltaMPS;
                actMPS = maxMPS;
                infCap -= deltaMPS;
                excStor = inflow;
                inflow = 0;
                //infiltration excess is generated
            } else {
                inflow = inflow - infCap;
                infExcStor = inflow;
                inflow = 0;
                actMPS = actMPS + infCap;
                infCap = 0;
            }
        }

        this.actMPS.setValue(actMPS);
        this.satMPS.setValue(actMPS / maxMPS);
        this.infExcStor.setValue(infExcStor);
        this.excStor.setValue(excStor);
    }

    protected void takeETfromStorage(Attribute.Double storageParameter) {
        //et out of the soil
        double actET = this.actET.getValue();
        double potET = this.potET.getValue();
        double storage = storageParameter.getValue();
        double deltaET = potET - actET;

        //shortcut
        if (deltaET == 0)
            return;
        
        if (storage >= deltaET) {
            actET = potET;
            storage = storage - deltaET;
        } else {
            actET = actET + storage;
            storage = 0;
        }

        this.potET.setValue(potET);
        this.actET.setValue(actET);
        storageParameter.setValue(storage);
    }

    protected void takeETfromMPS() {
        double actET = this.actET.getValue();
        double potET = this.potET.getValue();
        double actMPS = this.actMPS.getValue();
        double maxMPS = this.maxMPS.getValue();

        double deltaET = potET - actET;
        //reduction function here
        double linRed = this.linETRed.getValue();
        double reduceET = 1.0;
        if (actMPS < (linRed * maxMPS)) {
            reduceET = actMPS / (linRed * maxMPS);
        }
        deltaET = deltaET * reduceET;

        if (actMPS >= deltaET) {
            actET = actET + deltaET;
            actMPS = actMPS - deltaET;
            deltaET = 0;
        } else {
            //should never happen
            actET = actET + actMPS;
            actMPS = 0;
        }

        this.actET.setValue(actET);
        this.actMPS.setValue(actMPS);
    }

    protected void calculateRunoffComponents(){
        double excStor = this.excStor.getValue();
        double maxExcStor = calculateMaxExcStorage();
        double k_factor = this.recConst.getValue();
        double overlandFlow = infExcStor.getValue();
        double interflow = 0;        
        double dirQ = 0;
        double gwRecharge = 0;        
        
        //excess water is distributed to Qdir and GWrecharge
        double slope_weight = (Math.tan(this.slope.getValue() * (Math.PI / 180.))) * this.latVertDist.getValue();
        if (slope_weight > 1) {
            slope_weight = 1;
        }

        interflow = slope_weight * excStor * (1.0 / k_factor);
        gwRecharge = (1 - slope_weight) * excStor * (1.0 / k_factor);
        
        excStor = excStor - interflow - gwRecharge;

        if (excStor > maxExcStor) {
            dirQ = (excStor - maxExcStor);
            excStor = maxExcStor;
        }

        //cross checking against maximum percolation
        double delta = 0;
        if (gwRecharge > maxPerc.getValue()) {
            delta = gwRecharge - maxPerc.getValue();
            interflow = interflow + delta;
            gwRecharge = maxPerc.getValue();
        }

        dirQ = dirQ + interflow + overlandFlow;
        
        //writing values back
        this.excStor.setValue(excStor);
        this.gwRecharge.setValue(gwRecharge);
        this.dirQ.setValue(dirQ);
        this.totQ.setValue(dirQ + gwRecharge);
        this.infExcStor.setValue(0);        
    }
    
    @Override
    public void run() {        
        
        calculatePotET();

        putInflowToSoil();

        //first took et from infiltration excess water
        takeETfromStorage(this.infExcStor);
        
        //second took et from infiltration excess water
        takeETfromStorage(this.excStor);
        
        //last took et from MPS
        takeETfromMPS();
        
        //distribute water to direct flow and gwRecharge
        calculateRunoffComponents();
    }
}
