/*
 * InitJ2KNSoillayer.java
 * Created on 17. February 2006, 14:49
 *
 * This file is part of JAMS
 * Copyright (C) 2006 FSU Jena, Manfred Fink
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
package j2k.s_p;

import org.jams.j2k.s_n.init.*;
import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import org.jams.j2k.s_n.crop.J2KSNCrop;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title = "InitJ2KPSoillayer",
        author = "Manfred Fink",
        description = "intitiallizing Phosophorous Pools in Soil and additional variables. Method after SWAT2000",
        version = "1.0",
        date = "2015-05-12"
)
public class InitJ2KPSoillayer extends JAMSComponent {

    /*
     *  Component variables
     */
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil layer"
    )
    public Attribute.EntityCollection entities;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in cm depth of soil layer"
    )
    public Attribute.DoubleArray layerdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in kg/dm³ soil bulk density"
    )
    public Attribute.DoubleArray soil_bulk_density;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
    )
    public Attribute.DoubleArray C_org;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "P-Pool, ative mineral, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.DoubleArray Min_Act_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "P-Pool, stable mineral, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.DoubleArray Min_Sta_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "P-Organic Pool, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.DoubleArray P_org_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "PResiduePool Pool with fresh organic matter, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray PResiduePool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "P in the soil solution, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray Psolution;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "P concentration in soil for agricultural land",
            unit = "mg*kg^1",
            lowerBound = 0,
            upperBound = 1000000,
            defaultValue = "25 (the default value of SWAT)"
    )
    public Attribute.Double Pconc_arable;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "P concentration in soil for other land",
            unit = "mg*kg^1",
            lowerBound = 0,
            upperBound = 1000000,
            defaultValue = "5 (The default value of SWAT)"
    )
    public Attribute.Double Pconc_other;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Residue biomass in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray Residue_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "phosphorus availability index",
            unit = "-",
            lowerBound = 0.0,
            upperBound = 1,
            defaultValue = "0.4"
    )
    public Attribute.Double pai;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "organic Phosphorous input due to Fertilisation in P added to active org pool",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.Double fertP_activeorg;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current organic P fertilizer amount added to residue pool",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.Double fertorgPfresh;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Current organic P fertilizer amount added to solute pool",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )

    public Attribute.Double fertPmin;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "number of layers in soil profile in [-]"
    )
    public Attribute.Double Layer;
    /*
     *  Component run stages
     */

    public void init() throws Attribute.Entity.NoSuchAttributeException {

    }

    public void run() throws Attribute.Entity.NoSuchAttributeException {
        int i = 0;
        double orgNhum = 0; /*concentration of humic organic nitrogen in the layer (mg/kg)*/

        int layer = (int) Layer.getValue() + 1;
        double runlayerdepth;

        double runsoil_bulk_density;

        double runC_org;

        double runPsolution;
        double[] Psolutionvals = new double[layer];

        double runMin_Act_P;
        double[] Min_Act_Pvals = new double[layer];

        double runMin_Sta_P;
        double[] Min_Sta_Pvals = new double[layer];

        double runP_org_pool;
        double[] P_org_poolvals = new double[layer];

        double runPResiduePool;
        double[] PResiduePoolvals = new double[layer];

        double runResidue_pool;
      

        double Psolconc = 0;

        Attribute.Entity entity = entities.getCurrent();

        ArrayList<J2KSNCrop> rotation = (ArrayList<J2KSNCrop>) entity.getObject("landuseRotation");

        J2KSNCrop crop = rotation.get(0);
        int idc = crop.idc;

        if (idc == 7) {
            Psolconc = Pconc_other.getValue();
        } else {
            Psolconc = Pconc_arable.getValue();
        }

        double runMinActconc = (Psolconc * 1 - pai.getValue()) / pai.getValue();
        double runMinStaconc = runMinActconc * 4;


        while (i < layer) {
            
            if (i == 0) {
                runC_org = C_org.getValue()[i] / 1.72;
                runsoil_bulk_density = soil_bulk_density.getValue()[i];
                runlayerdepth = 10; //first  cm layer virtual according to the SWAT concept
            }else if (i == 1){
                runC_org = C_org.getValue()[i - 1] / 1.72;
                runsoil_bulk_density = soil_bulk_density.getValue()[i - 1];
                runlayerdepth = (layerdepth.getValue()[i - 1] * 10)  - 10; //from cm to mm
            }else{
                runC_org = C_org.getValue()[i - 1] / 1.72;
                runsoil_bulk_density = soil_bulk_density.getValue()[i - 1];
                runlayerdepth = (layerdepth.getValue()[i - 1] * 10); //from cm to mm
            }
            runResidue_pool = Residue_pool.getValue()[i];

            runPsolution = (Psolconc * runsoil_bulk_density * runlayerdepth) / 100; //kgP * ha^1
            runMin_Act_P = (runMinActconc * runsoil_bulk_density * runlayerdepth) / 100; //kgP * ha^1
            runMin_Sta_P = (runMinStaconc * runsoil_bulk_density * runlayerdepth) / 100; //kgP * ha^1
            orgNhum = 10000 * runC_org / 14;
            runP_org_pool = (orgNhum * 0.125 * runsoil_bulk_density * runlayerdepth) / 100; //kgP * ha^1
            runPResiduePool = 0.0003 * runResidue_pool; //kgP * ha^1
            
            Psolutionvals[i] = runPsolution; 
            Min_Act_Pvals[i] = runMin_Act_P;
            Min_Sta_Pvals[i] = runMin_Sta_P;
            P_org_poolvals[i] = runP_org_pool;
            PResiduePoolvals[i] = runPResiduePool;
            
            

            i++;
        }

        Min_Act_P.setValue(Min_Act_Pvals);
        Min_Sta_P.setValue(Min_Sta_Pvals);
        P_org_pool.setValue(P_org_poolvals);
        PResiduePool.setValue(PResiduePoolvals);
        Psolution.setValue(Psolutionvals);

        fertP_activeorg.setValue(0);
        fertorgPfresh.setValue(0);
        fertPmin.setValue(0);
        
    }

    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {

    }
}

