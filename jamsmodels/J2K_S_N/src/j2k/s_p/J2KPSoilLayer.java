/*
 * J2KNSoilLayer.java
 * Created on 27. November 2005, 15:47
 *
 * This file is part of JAMS
 * Copyright (C) 2005 FSU Jena, Manfred Fink
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

import jams.data.*;
import jams.model.*;
import java.util.ArrayList;
import org.jams.j2k.s_n.crop.J2KSNCrop;

/**
 *
 * @author Manfred Fink
 */
@JAMSComponentDescription(
        title = "J2KPSoilLayer",
        author = "Manfred Fink",
        description = "Calculates Phosphous transformation Processes in Soil. Method after SWAT2000 with adaptions Including Phosphorous transported due to Erosion",
        version = "1.0",
        date = "2015-05-05"
)
public class J2KPSoilLayer extends JAMSComponent {

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
            description = "attribute area",
            unit = "m^2",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double area;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " number of soil layers",
            unit = "-",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double Layer;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "depth of soil layer",
            unit = "cm",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.DoubleArray layerdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "depth of soil profile",
            unit = "cm",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.Double totaldepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "in m actual depth of roots",
            unit = "m",
            lowerBound = 0,
            upperBound = 100
    )
    public Attribute.Double rootdepth;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil bulk density",
            unit = "kg*dm^3^-1",
            lowerBound = 0,
            upperBound = 2.7
    )
    public Attribute.DoubleArray soil_bulk_density;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual LPS in portion of sto_LPS soil water content",
            unit = "-",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.DoubleArray sat_LPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "actual MPS in portion of sto_MPS soil water content",
            unit = "-",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.DoubleArray sat_MPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum MPS (Middle Pore Storage) soil water content",
            unit = "L",
            lowerBound = 0,
            upperBound = 2000
    )
    public Attribute.DoubleArray stohru_MPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum LPS (Large Pore Storage) soil water content",
            unit = "L",
            lowerBound = 0,
            upperBound = 2000
    )
    public Attribute.DoubleArray stohru_LPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "maximum FPS (Fine Pore Storage) soil water content",
            unit = "L",
            lowerBound = 0,
            upperBound = 2000
    )
    public Attribute.DoubleArray stohru_FPS;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "soil temperature in layerdepth",
            unit = "°C",
            lowerBound = -70,
            upperBound = 70
    )
    public Attribute.DoubleArray Soil_Temp_Layer;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "P-Pool, ative mineral, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.DoubleArray Min_Act_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "P-Pool, stable mineral, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.DoubleArray Min_Sta_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "P-Organic Pool, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.DoubleArray P_org_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum of P-Organic Pool, P content in the entire soil profile",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double sP_org_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N-Organic Pool with reactive organic matter, N content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.DoubleArray N_activ_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "N-Organic Pool with stable organic matter, N content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray N_stable_pool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum P-Pool, ative mineral, content in the entire soil profile",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.Double sMin_Act_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum P-Pool, stable mineral, content in the entire soil profile",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.Double sMin_Sta_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "PResiduePool Pool with fresh organic matter, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray PResiduePool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "sum of PResiduePool, P content in the entire soil profile",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.Double sPResiduePool;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "P in the soil solution, P content in layer",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray Psolution;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "sum of P in the soil solution, P content in layer in the entire soil profile",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.Double sPsolution;

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
            description = "surface runoff leaving the HRU",
            unit = "L",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double RD1_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "interflow leaving the HRU in every layer",
            unit = "L",
            lowerBound = 0,
            upperBound = 100000000
    )
    public Attribute.DoubleArray RD2_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "mineral P  fertilizer rate in P",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000
    )
    public Attribute.Double PH4inp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "nitrification rate from  NO3_Pool in N",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000
    )
    public Attribute.Double Nitri_trans;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Phosphorous in surface runoff added to HRU toplayer in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double SurfaceSolubleP_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Phosphorous in surface runoff leaving the HRU toplayer in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double SurfaceSolubleP_out;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "potential nitrogen content of plants in N",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.Double BioPoptAct;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual nitrate nitrogen content of plants in N",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.Double BioPAct;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "intfiltration poritions for the single horizonts",
            unit = "L",
            lowerBound = 0,
            upperBound = 10000000
    )
    public Attribute.DoubleArray infiltration_hor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "percolation out ouf the single horizonts",
            unit = "L",
            lowerBound = 0,
            upperBound = 10000000
    )
    public Attribute.DoubleArray perco_hor;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "percolation out ouf the single horizonts",
            unit = "L",
            lowerBound = 0,
            upperBound = 1000000
    )
    public Attribute.DoubleArray actETP_h;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "organic Phosphorous input due to Fertilisation in P added to active org pool",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000000
    )
    public Attribute.Double fertP_activeorg;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current organic P fertilizer amount added to residue pool",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000000
    )
    public Attribute.Double fertorgPfresh;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "Phosphorous added residue pool after harvesting [kg N/ha]",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000000
    )
    public Attribute.Double Addresidue_poolp;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current organic P fertilizer amount added to solute pool",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )

    public Attribute.Double fertPmin;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Sum of N input due fertilisation in P",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 100000
    )
    public Attribute.Double sum_Pinput;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Phosphorous input of plant residues in P",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 10000
    )
    public Attribute.Double inpP_biomass;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "precipitation",
            unit = "mm",
            lowerBound = 0,
            upperBound = 1000
    )
    public Attribute.Double precip;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Current time"
    )
    public Attribute.Calendar time;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "indicates dormancy of plants"
    )
    public Attribute.Boolean dormancy;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            description = "actual phosporous uptake of plants in P",
            unit = "kg*ha^-1",
            lowerBound = 0,
            upperBound = 1000
    )
    public Attribute.Double actPup;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Indicates whether roots can penetrate or not the soil layer",
            unit = "-",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.DoubleArray root_h;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Anmount of sediments entering the HRU",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double sedi_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Anmount of sediments leaving the HRU",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double sedi_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Organic-P in surface runoff added to HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double org_in_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Residue-P in surface runoff added to HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double residue_in_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Activ-P in surface runoff added to HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double activP_in;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Stable-P in surface runoff added to HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double stableP_in;
    


    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Organic-P in surface runoff leaving the HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double org_out_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Residue-P in surface runoff leaving the HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double residue_out_P;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Activ-P in surface runoff leaving the HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double activP_out;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            description = "Stable-P in surface runoff leaving the HRU in P",
            unit = "kg",
            lowerBound = 0,
            upperBound = 1000000000
    )
    public Attribute.Double stableP_out;

    // constants and calibration parameter
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "rate factor between N_activ_pool and NO3_Pool to be calibrated",
            unit = "-",
            lowerBound = 0.001,
            upperBound = 0.003,
            defaultValue = "0.002"
    )
    public Attribute.Double Beta_min;

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
            access = JAMSVarDescription.AccessType.READ,
            description = "nitrogen uptake distribution parameter to calibrate",
            unit = "-",
            lowerBound = 1,
            upperBound = 15,
            defaultValue = "1"
    )
    public Attribute.Double Beta_Pdist;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Enrichment factor for sediment bounded P-Pools",
            unit = "-",
            lowerBound = 1,
            upperBound = 10,
            defaultValue = "1"
    )
    public Attribute.Double enrichmentP;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " phosphorus percolation coefficient is the "
            + "ratio of the phosphorus concentration in the surface 10 mm of soil to the "
            + "concentration of phosphorus in percolate. ",
            unit = "10 m³/Mg",
            lowerBound = 10,
            upperBound = 17.5,
            defaultValue = "10"
    )
    public Attribute.Double P_prec_coef;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Indicates fertilazation optimization with plant demand"
    )
    public Attribute.Boolean opti;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "nutrient cycling residue composition factor []",
            unit = "-",
            lowerBound = 0,
            upperBound = 1
    )
    public Attribute.DoubleArray gamma_ntr;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "P concentration in soil for agricultural land",
            unit = "mg*kg^1",
            lowerBound = 0,
            upperBound = 1000000,
            defaultValue = "25"
    )
    public Attribute.Double Pconc_arable;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "P concentration in soil for other land",
            unit = "mg*kg^1",
            lowerBound = 0,
            upperBound = 1000000,
            defaultValue = "5"
    )
    public Attribute.Double Pconc_other;

    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = " in % organic Carbon in soil"
    )
    public Attribute.DoubleArray C_org;

    /*
     *  Component run stages
     */
    private double gamma_temp;
    private double gamma_water;
    private double runarea;
    private double runSoil_Temp_Layer;
    private double[] runlayerdepth;
    private double runsoil_bulk_density;
    private double sto_MPS;
    private double sto_LPS;

    private double sto_FPS;
    private double act_LPS;
    private double act_MPS;

    private double runPsolution;
    private double runP_org_act_pool;
    private double runP_org_sta_pool;
    private double runP_org_pool;
    private double runC_org;
    private double runP_Pool;
    private double runMin_Act_P;
    private double runMin_Sta_P;
    private double runPResiduePool;
    private double runsurfacesolubleP_in;
    private double runN_stable_pool;
    private double runN_activ_pool;
    private double runResidue_pool;
    private double RD1_out_mm;
    private double h_infilt_mm;
    private int layer;
    private int surlayer;
    private double runvolati_trans;

    private double sumlayer;

    private double runBeta_min;
    
    //balance Variables
    private double bP_Poolvals;
    private double sbP_Poolvals;
    private double bP_org_pool;
    private double sbP_org_pool;
    private double bMin_Act_P;
    private double sbMin_Act_P;
    private double bMin_Sta_P;
    private double sbMin_Sta_P;
    private double bPResiduePool;
    private double sbPResiduePool;
    private double bplant_up;
    private double runsum_Pinput;
    //
     
            

    private double theta_nit = 0.05; /*fraction of anion excluded soil water. depended from clay content min. 0.01  max. 1*/

    private double fr_actN = 0.02;
    /**
     * nitrogen active pool fraction. The fraction of organic nitrogen in the
     * active pool.
     */
    private double N_nit_vol = 0;
    /**
     * NH4 that is converted to NO3 Pool or volatilation .
     */
    private double frac_nitr = 0;
    /**
     * Fraction of N_nit_vol that is nitrification
     */
    private double frac_vol = 0;
    /**
     * Fraction of N_nit_vol that is volatilasation
     */
    private double Hum_trans; /*transformation rate from NOrg_acti_Pool to N_stable_pool and back in kgN/ha */

    private double Hum_act_min_P; /*mirelaization rate from NOrg_acti_Pool to NO3_Pool in kgN/ha */

    private double runnitri_trans = 0; /*nitrifikation rate from NH4_Pool to NO3_Pool in kgN/ha*/

    private double delta_ntr = 0; /*residue decomposition factor */

    private double concN_mobile = 0; /*NO3 concentration of the mobile soil water in kgN/mm H2O*/

    private int datumjul = 0;

    private double run_gamma_ntr;

    double[] hor_by_infilt;
    double[] P_Poolvals;
    double[] w_l_diff;
    double[] partnmin;
    double[] diffout;

    public void initAll() throws Attribute.Entity.NoSuchAttributeException {
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

        Double LandID = entity.getDouble("LID");

        if (LandID == 3 || LandID == 8) {
            Psolconc = Pconc_arable.getValue();
        } else {
            Psolconc = Pconc_other.getValue();
        }

        double runMinActconc = (Psolconc * 1 - pai.getValue()) / pai.getValue();
        double runMinStaconc = runMinActconc * 4;

        while (i < layer) {

            if (i == 0) {
                runC_org = C_org.getValue()[i] / 1.72;
                runsoil_bulk_density = soil_bulk_density.getValue()[i];
                runlayerdepth = 10; //first  cm layer virtual according to the SWAT concept
            } else if (i == 1) {
                runC_org = C_org.getValue()[i - 1] / 1.72;
                runsoil_bulk_density = soil_bulk_density.getValue()[i - 1];
                runlayerdepth = (layerdepth.getValue()[i - 1] * 10) - 10; //from cm to mm
            } else {
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

    public void run() throws Attribute.Entity.NoSuchAttributeException {
        /*         Attribute.Calendar testtime = new Attribute.Calendar();
         testtime.setValue("1993-10-12 07:30");
         if (time.equals(testtime)){
         System.out.println(time.getValue()) ;
         }*/

        int i = 0;

        this.gamma_temp = 0;
        this.gamma_water = 0;
        this.runarea = area.getValue();
        this.layer = (int) Layer.getValue();
        this.surlayer = layer + 1;
        sumlayer = 0;

        double sumh_infilt_mm = 0;
        
        double bfertP_activeorg = 0;
        double bfertorgPfresh = 0;
        double bAddresidue_poolp = 0;
        double ifertPmin = 0;
        double HorPBal = 0; 
        double PBal = 0;
        runPResiduePool = 0;
        double PrecoP = 0;
        double runnmin = 0;
        double delta_P = 0;
        
//        double[] NO3_Poolvals = new double[layer];
        runlayerdepth = new double[surlayer];

        double[] P_org_poolvals = new double[surlayer];
        double[] P_activ_poolvals = new double[surlayer];
        double[] P_stable_poolvals = new double[surlayer];
        double[] P_residue_pool_vals = new double[surlayer];

        sPsolution.setValue(0.0);
        sP_org_pool.setValue(0.0);
        sMin_Act_P.setValue(0.0);
        sMin_Sta_P.setValue(0.0);
        sPResiduePool.setValue(0.0);

        hor_by_infilt = new double[surlayer];
        diffout = new double[surlayer];
        partnmin = new double[surlayer];
        w_l_diff = new double[surlayer];
        i = 0;
        /*while (i < layer){
         NO3_Poolalt[i] = NO3_Pool.getValue()[i];
         i++;
         }*/
        P_Poolvals = calc_plantuptake();
//       NO3_Poolvals = NO3_Pool.getValue();

        while (i < layer) {

            this.h_infilt_mm = infiltration_hor.getValue()[i] / runarea;

            sumh_infilt_mm = sumh_infilt_mm + h_infilt_mm;
            i++;
        }

        i = 0;

        // horizont processies loop
        while (i < surlayer) {
            if (i == 0) {
                this.runSoil_Temp_Layer = Soil_Temp_Layer.getValue()[i];

                this.sto_MPS = stohru_MPS.getValue()[i] / runarea;
                this.sto_LPS = stohru_LPS.getValue()[i] / runarea;
                this.sto_FPS = stohru_FPS.getValue()[i] / runarea;

                this.act_LPS = sat_LPS.getValue()[i] * sto_LPS;
                this.act_MPS = sat_MPS.getValue()[i] * sto_MPS;
                this.runN_activ_pool = N_activ_pool.getValue()[i];
                
                this.runsum_Pinput = fertPmin.getValue() + fertP_activeorg.getValue() + fertP_activeorg.getValue();
                
                
                this.runN_stable_pool = N_stable_pool.getValue()[i];
                
                this.run_gamma_ntr = gamma_ntr.getValue()[i];
                this.runsurfacesolubleP_in = SurfaceSolubleP_in.getValue() * 10000 / runarea;
                SurfaceSolubleP_in.setValue(0);
                this.runP_Pool = P_Poolvals[i] + fertPmin.getValue() + runsurfacesolubleP_in;
                ifertPmin = fertPmin.getValue();                
                fertPmin.setValue(0);
                         
                this.runMin_Act_P = Min_Act_P.getValue()[i] + ((activP_in.getValue() * 10000) / runarea);
                this.bMin_Act_P = Min_Act_P.getValue()[i];
                activP_in.setValue(0);
                this.runMin_Sta_P = Min_Sta_P.getValue()[i] + ((stableP_in.getValue() * 10000) / runarea);
                this.bMin_Sta_P = Min_Sta_P.getValue()[i];
                stableP_in.setValue(0);
                this.runP_org_pool = P_org_pool.getValue()[i] + ((org_in_P.getValue() * 10000) / runarea) + fertP_activeorg.getValue();
                this.bP_org_pool = P_org_pool.getValue()[i];
                bfertP_activeorg = fertP_activeorg.getValue();
                fertP_activeorg.setValue(0);
                org_in_P.setValue(0);
                this.runPResiduePool = PResiduePool.getValue()[i] + ((residue_in_P.getValue() * 10000) / runarea) + fertorgPfresh.getValue() + Addresidue_poolp.getValue();
                this.bPResiduePool = PResiduePool.getValue()[i]; 
                bAddresidue_poolp =  Addresidue_poolp.getValue();
                bfertorgPfresh =  fertorgPfresh.getValue();
                
                fertorgPfresh.setValue(0);
                residue_in_P.setValue(0);
                Addresidue_poolp.setValue(0);
                
                
                this.runResidue_pool = Residue_pool.getValue()[i];

                this.RD1_out_mm = RD1_out.getValue() / runarea;
                this.runsoil_bulk_density = soil_bulk_density.getValue()[i];

            } else {

                
               
                this.runSoil_Temp_Layer = Soil_Temp_Layer.getValue()[i - 1];
                this.sto_MPS = stohru_MPS.getValue()[i - 1] / runarea;
                this.sto_LPS = stohru_LPS.getValue()[i - 1] / runarea;
                this.sto_FPS = stohru_FPS.getValue()[i - 1] / runarea;

                this.act_LPS = sat_LPS.getValue()[i - 1] * sto_LPS;
                this.act_MPS = sat_MPS.getValue()[i - 1] * sto_MPS;

                this.runP_Pool = P_Poolvals[i];
                this.runMin_Act_P = Min_Act_P.getValue()[i];
                this.bMin_Act_P = Min_Act_P.getValue()[i];                
                this.runMin_Sta_P = Min_Sta_P.getValue()[i];
                this.bMin_Sta_P = Min_Sta_P.getValue()[i];
                this.runP_org_pool = P_org_pool.getValue()[i];
                this.bP_org_pool = P_org_pool.getValue()[i];
                this.runPResiduePool = PResiduePool.getValue()[i];
                this.bPResiduePool = PResiduePool.getValue()[i];

                this.RD1_out_mm = RD1_out.getValue() / runarea;

            }

            this.runBeta_min = Beta_min.getValue();
           


            /*          calculation of amount of nitrogen uptake with epaporation from soil */
            gamma_temp = 0.9 * (runSoil_Temp_Layer / (runSoil_Temp_Layer * Math.exp(9.93 - 0.312 * runSoil_Temp_Layer))) + 0.1;

            if (sto_LPS + sto_MPS + sto_FPS > 0) {
                gamma_water = (act_LPS + act_MPS + sto_FPS) / (sto_LPS + sto_MPS + sto_FPS);
            } else {
                gamma_water = 0;
            }

            /*Calculations of PPools   Check Order of calculations !!!!!!!!!!!!!!*/
            runN_stable_pool = runN_stable_pool + Hum_trans;

            Hum_act_min_P = calc_Hum_act_min();

            runP_org_pool = runP_org_pool - Hum_act_min_P;

            runP_Pool = runP_Pool + Hum_act_min_P;

            if (runP_org_pool < 0) {
                runP_org_pool = 0;
            }

            if (runP_Pool < 0) {
                runP_Pool = 0;
            }

            calc_trans_P_act_sta();
            calc_trans_P_sol_P_act();

            delta_P = this.calc_Res_P_trans();

            delta_P = delta_P * runPResiduePool;

            runPResiduePool = runPResiduePool - delta_P + inpP_biomass.getValue();

            runP_Pool = runP_Pool + (0.8 * delta_P);

            runP_org_pool = runP_org_pool + (0.2 * delta_P);

            if (i < 1) {

                PrecoP = calc_P_leaching(i);

                runP_Pool = runP_Pool - PrecoP;

                P_Poolvals[i + 1] = P_Poolvals[i + 1] + PrecoP;

                /*Calculations of PFluxes (out)*/
                
                SurfaceSolubleP_out.setValue(calc_surfacePpool(runP_Pool));
                org_out_P.setValue(calc_surfacePpool(runP_org_pool)) ;
                residue_out_P.setValue(calc_surfacePpool(runPResiduePool));
                stableP_out.setValue(calc_surfacePpool(runMin_Sta_P));
                activP_out.setValue(calc_surfacePpool(runMin_Act_P));

                runP_Pool = runP_Pool - SurfaceSolubleP_out.getValue();
                runP_org_pool = runP_org_pool - org_out_P.getValue();
                runPResiduePool = runPResiduePool - residue_out_P.getValue();
                runMin_Sta_P = runMin_Sta_P - stableP_out.getValue();
                runMin_Act_P = runMin_Act_P - activP_out.getValue();
                
                SurfaceSolubleP_out.setValue(SurfaceSolubleP_out.getValue()*(area.getValue()/10000));
                org_out_P.setValue(org_out_P.getValue()*(area.getValue()/10000)) ;
                residue_out_P.setValue(residue_out_P.getValue()*(area.getValue()/10000));
                stableP_out.setValue(stableP_out.getValue()*(area.getValue()/10000));
                activP_out.setValue(activP_out.getValue()*(area.getValue()/10000));
                
                

            }

            this.P_Poolvals[i] = runP_Pool;

            P_org_poolvals[i] = runP_org_pool;

            P_activ_poolvals[i] = runMin_Act_P;

            P_stable_poolvals[i] = runMin_Sta_P;

            P_residue_pool_vals[i] = runPResiduePool;
            sPsolution.setValue(sPsolution.getValue() + runP_Pool);
            sP_org_pool.setValue(sP_org_pool.getValue() + runP_org_pool);
            sMin_Act_P.setValue(sMin_Act_P.getValue() + runMin_Act_P);
            sMin_Sta_P.setValue(sMin_Sta_P.getValue() + runMin_Sta_P);
            sPResiduePool.setValue(sPResiduePool.getValue() + runPResiduePool);
            
            HorPBal = HorPBal + bMin_Act_P + bMin_Sta_P + bP_org_pool + bPResiduePool;

            i++;
        }
        i = 0;
/*
       PBal =  (HorPBal + this.sbP_Poolvals) - (sPsolution.getValue() + sP_org_pool.getValue() + sMin_Act_P.getValue() + sMin_Sta_P.getValue() + sPResiduePool.getValue());
       PBal = PBal - (actPup.getValue() +  residue_out_P.getValue() + stableP_out.getValue() + activP_out.getValue() + org_out_P.getValue())+ bfertP_activeorg + bfertorgPfresh + bAddresidue_poolp + ifertPmin;
       
       if ((PBal > 0.0000001) || (PBal < -0.0000001)){                
                getModel().getRuntime().println("Balance calculation problem in P balance, derivation: " +  PBal);
            } 
 */    

       // writing of pools
        sum_Pinput.setValue(runsum_Pinput);
        Min_Act_P.setValue(P_activ_poolvals);
        Min_Sta_P.setValue(P_stable_poolvals);
        P_org_pool.setValue(P_org_poolvals);
        PResiduePool.setValue(P_residue_pool_vals);
        Psolution.setValue(this.P_Poolvals);

//        System.out.println("percoN = " + percoN +" percoNabs =  "+ percoNabs);
    }

    private double[] calc_plantuptake() {
        double upP_Pool = 0;
        double runrootdepth = (rootdepth.getValue() * 100);
        double[] partroot = new double[surlayer];
        this.sbP_Poolvals = 0;

        if (BioPoptAct.getValue() == 0) {
            BioPAct.setValue(0);
        }
        double runpotP_up = BioPoptAct.getValue() - BioPAct.getValue();

        if (dormancy.getValue()) {
            runpotP_up = 0;
        }

        if (runpotP_up < 0) {
            runpotP_up = 0;
        }

        double[] P_Poolvals1 = new double[surlayer];
        double[] potP_up_z = new double[surlayer];
        double[] demandP_up_z = new double[surlayer];
        double rootlayer = 0;
        double runBeta_Pdist = Beta_Pdist.getValue();

        double demand2 = 0;
        double demand1 = 0;
        double uptake1 = 0;
        int ii = 0;
        int jj = 0;
        int j = 0;
        int i = 0;

        P_Poolvals1 = Psolution.getValue();

        // plant uptake loop 1: calculating layer poritions within rootdepth
        while (i < surlayer) {
            
            this.sbP_Poolvals = sbP_Poolvals + P_Poolvals1[i];

            if (i == 0) {
                sumlayer = 1;
            } else {
                sumlayer = sumlayer + layerdepth.getValue()[i - 1] - 1;
            }

            this.runlayerdepth[i] = sumlayer;
            if (runrootdepth > runlayerdepth[0]) {
                if (runrootdepth > runlayerdepth[i]) {
                    partroot[i] = 1;
                    rootlayer = i;
                } else if (runrootdepth > runlayerdepth[i - 1]) {
                    partroot[i] = (runrootdepth - runlayerdepth[i - 1]) / (runlayerdepth[i] - runlayerdepth[i - 1]);
                    rootlayer = i;
                } else {
                    partroot[i] = 0;
                }
            } else if (i == 0) {
                partroot[i] = runrootdepth / runlayerdepth[0];
                rootlayer = i;
            }

            if (opti.getValue()) {
                double Nmin_depth = 60;
                if (Nmin_depth > runlayerdepth[0]) {
                    if (Nmin_depth > runlayerdepth[i]) {
                        partnmin[i] = 1;

                    } else if (Nmin_depth > runlayerdepth[i - 1]) {
                        partnmin[i] = (Nmin_depth - runlayerdepth[i - 1]) / (runlayerdepth[i] - runlayerdepth[i - 1]);

                    } else {
                        partnmin[i] = 0;
                    }
                } else if (i == 0) {
                    partnmin[i] = Nmin_depth / runlayerdepth[0];

                }

            }
            i++;

        }

        // plant uptake loop 2: calculating P demand by plants and rest soluble PO4_Pools
        while (j <= rootlayer) {
            upP_Pool = Psolution.getValue()[j];
            

            if (j == 0) {
                potP_up_z[j] = (runpotP_up / (1 - Math.exp(-runBeta_Pdist))) * (1 - Math.exp(-runBeta_Pdist * (runlayerdepth[j] / runrootdepth)));
                if (runlayerdepth[j] > runrootdepth) {
                    potP_up_z[j] = runpotP_up;
                }
                demand1 = upP_Pool - potP_up_z[j];

                uptake1 = potP_up_z[j];

            } else if (j > 0 && j < rootlayer) {

                potP_up_z[j] = ((runpotP_up / (1 - Math.exp(-runBeta_Pdist))) * (1 - Math.exp(-runBeta_Pdist * (runlayerdepth[j] / runrootdepth)))) - uptake1;

                demand1 = upP_Pool - potP_up_z[j];
                uptake1 = uptake1 + potP_up_z[j];

            } else if (j == rootlayer) {
                potP_up_z[j] = ((runpotP_up / (1 - Math.exp(-runBeta_Pdist))) * (1 - Math.exp(-runBeta_Pdist))) - uptake1;
                demand1 = (upP_Pool * partroot[j]) - potP_up_z[j];
                uptake1 = uptake1 + potP_up_z[j];
                /*
                 if (uptake1 == runpotN_up){
                 System.out.println("good");
                 }else{
                 System.out.println("bad");
                 }
                 */
            }

            if (demand1 >= 0) {

                demandP_up_z[j] = 0;

                upP_Pool = upP_Pool - potP_up_z[j];

            } else {

                /*demandN_up_z[j] = upNO3_Pool - potN_up_z[j];

                 upNO3_Pool = 0;*/
                demandP_up_z[j] = demand1;

                upP_Pool = upP_Pool - (upP_Pool * partroot[j]);

            }

            P_Poolvals1[j] = upP_Pool;
            
            // Balance calculation
            
            
            //
            j++;
        }

        // plant uptake loop 3: summarising rest N demand
        while (ii <= rootlayer) {

            demand2 = demandP_up_z[ii] + demand2;

            ii++;
        }
        /* switch off of loop 4
         if (demand2 < 0){

         // plant uptake loop 4: redistributing rest N demand on rest NO3_Pools within rootdepth
         while (jj < rootlayer) {
         demand3 = demand2;

         demand3 = demand3 + NO3_Poolvals1[jj];

         NO3_Poolvals1[jj]  = NO3_Poolvals1[jj] + demand2;

         if (NO3_Poolvals1[jj] < 0){
         NO3_Poolvals1[jj] = 0;
         }
         if (demand3 < 0){

         demand2 = demand3;

         } else{

         demand2 = 0;
         }

         jj++;
         }
         }
         */
        double runactP_up = runpotP_up + demand2;

        double bioPact = 0;
        //double nuptake = actnup.getValue();
        bioPact = BioPAct.getValue() + runactP_up;
        //nuptake = nuptake + runactN_up;
        actPup.setValue(runactP_up);
//        System.out.println("runactN_up = " + nuptake);
//        if (runpotN_up > runactN_up){
//        System.out.println("runpotN_up = " + runpotN_up + " runactN_up = " + runactN_up);
//        }
        BioPAct.setValue(bioPact);

        return P_Poolvals1;
    }

    private double calc_Hum_act_min() {
        double P_Hum_act_min = 0;

        if ((runN_activ_pool + runN_stable_pool) > 0) {
            runP_org_act_pool = runP_org_pool * (runN_activ_pool / (runN_activ_pool + runN_stable_pool));
            runP_org_sta_pool = runP_org_pool * (runN_stable_pool / (runN_activ_pool + runN_stable_pool));
        } else {
            runP_org_act_pool = 0.0;
            runP_org_sta_pool = 0.0;
        }

        P_Hum_act_min = 1.4 * runBeta_min * Math.sqrt(gamma_temp * gamma_water) * runP_org_act_pool;

        return P_Hum_act_min;
    }

    private double calc_Res_P_trans() { /*is only allowed in the first layer */


        double delta_P = 0;
        /*calculation of the c/n ratio */

        delta_P = run_gamma_ntr * Math.sqrt(gamma_temp * gamma_water);

        /*Res_N_trans = delta_ntr * N_residue_pool_fresh;
         /*splitting in decomposition 20% and Minteralisation 80%  in run method*/
        return delta_P;
    }

    private void calc_trans_P_sol_P_act() {
        // 
        double P_trans = 0; //amount of phosphorus transferred between the soluble and active mineral pool (kg P/ha)
        double temppai = this.runMin_Act_P * (pai.getValue() / (1 - pai.getValue()));

        if (this.runP_Pool > (temppai)) {
            P_trans = runP_Pool - temppai;
        } else {
            P_trans = 0.1 * (runP_Pool - temppai);
        }

        this.runP_Pool = Math.max(this.runP_Pool - P_trans, 0);

        this.runMin_Act_P = Math.max(this.runMin_Act_P + P_trans, 0);
    }

    private void calc_trans_P_act_sta() {

        double Trans_P_act_sta = 0;
        double Beta_Peq = 0.0006; //slow equilibration rate constant

        if (this.runMin_Act_P * 4 > this.runMin_Sta_P) {

            Trans_P_act_sta = Beta_Peq * (4 * this.runMin_Act_P - this.runMin_Sta_P);

        } else {

            Trans_P_act_sta = 0.1 * Beta_Peq * (4 * this.runMin_Act_P - this.runMin_Sta_P);

        }

        this.runMin_Act_P = Math.max(this.runMin_Act_P - Trans_P_act_sta, 0);
        this.runMin_Sta_P = Math.max(this.runMin_Sta_P + Trans_P_act_sta, 0);

    }

    private double calc_surfacePpool(double pool) {
        double surfacePpool = 0;

        //P concentration for diffrent P-pools in kg/kg,
        double concP_pool = pool / (runsoil_bulk_density * layerdepth.getValue()[0] * 100000);

        surfacePpool = sedi_out.getValue() * 1000  * concP_pool * enrichmentP.getValue();

        surfacePpool = Math.min(surfacePpool, pool);

        return surfacePpool;
    }

    private double calc_P_leaching(int i) {
        double percoP = 0;
        double percoW = 0;

        percoW = (hor_by_infilt[i] - (hor_by_infilt[i] / this.sumlayer)) / area.getValue();

        percoP = (this.runP_Pool * percoW) / (10 * runsoil_bulk_density * 10 * P_prec_coef.getValue());

        percoP = Math.min(percoP, this.runP_Pool);
        return percoP;
    }

    public void cleanup() throws Attribute.Entity.NoSuchAttributeException {

    }
}
