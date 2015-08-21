/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jams.j2k.s_n.wq.ProcessRates;

/**
 *
 * @author c7wema
 */
public class ProcessCoefficients {

    private static Table2D stoiCoeff = null;
    private static Table2D massFrac = null;
    private static Table2D stoiPara = null;

    public static Table2D getStoichiometricCoefficients() {

        if (stoiCoeff == null) {
            // create Table2D
            stoiCoeff = new Table2D();

            // fill table with data
            // stoichiometric coefficients from Reichert et al. 2001 (RWQM1)

                //S_S dissolved organic substances in (gSS)
                //S_NH4 Ammonium (gN)
                //S_NH3 Ammonia (gN)
                //S_NO2 Nitrite (gN)
                //S_NO3 Nitrate (gN)
                //S_HPO4 part of inorganic dissolved phosphorus HPO4^2- (gP)
                //S_H2PO4 part of inorganic dissolved phosphorus H2PO4^- (gP)
                //S_O2 dissolved oxygen (gO)
                //S_CO2 sum of dissolved carbon dioxide (CO2) and H2CO3 (gC)
                //S_HCO3 bicarbonate HCO3^- (gC)
                //S_CO3 dissolved carbonate CO3^2- (gC)
                //S_H Hydrogen ions H^+ (gH)
                //S_OH Hydroxyl ions OH^- (gH)
                //S_Ca dissolved calcium ions Ca^2+ (gCa)
                //X_H heterotrophic organisms (gXH)
                //X_N1 organisms oxidising ammonia to nitrite (gN1)
                //X_N2 organisms oxidising nitrite to nitrate (gN2)
                //X_ALG algae and macrophytes (gALG)
                //X_CON consumers (gCON)
                //X_S particulate organic material (gXS)
                //X_I inert particulate organic material (gXI)
                //X_P phosphate adsorbed to particles (gXP)

            //values for process 1a: aerobic growth of heterotrophs with NH4
            stoiCoeff.addCell("growthXH_1a", "S_S", -1.666666667);
            stoiCoeff.addCell("growthXH_1a", "S_NH4", -0.02);
            stoiCoeff.addCell("growthXH_1a", "S_HPO4", -0.013333333);
            stoiCoeff.addCell("growthXH_1a", "S_O2", -1.37374808);
            stoiCoeff.addCell("growthXH_1a", "S_HCO3", 0.43);
            stoiCoeff.addCell("growthXH_1a", "S_H", 0.03640169);
            stoiCoeff.addCell("growthXH_1a", "X_H", 1);

            //values for process 1b: aerobic growth of heterotrophs with NO3
            stoiCoeff.addCell("growthXH_1b", "S_S", -1.666666667);
            stoiCoeff.addCell("growthXH_1b", "S_NO3", -0.02);
            stoiCoeff.addCell("growthXH_1b", "S_HPO4", -0.013333333);
            stoiCoeff.addCell("growthXH_1b", "S_O2", -1.282319508);
            stoiCoeff.addCell("growthXH_1b", "S_HCO3", 0.43);
            stoiCoeff.addCell("growthXH_1b", "S_H", 0.033544547);
            stoiCoeff.addCell("growthXH_1b", "X_H", 1);

            // values for process 2: aerobic endogenous respiration of heterotrophs
            stoiCoeff.addCell("respXH_2", "S_NH4", 0.114);
            stoiCoeff.addCell("respXH_2", "S_HPO4", 0.028);
            stoiCoeff.addCell("respXH_2", "S_O2", -1.236033794);
            stoiCoeff.addCell("respXH_2", "S_HCO3", 0.398);
            stoiCoeff.addCell("respXH_2", "S_H", 0.026830261);
            stoiCoeff.addCell("respXH_2", "X_H", -1);
            stoiCoeff.addCell("respXH_2", "X_I", 0.2);

            //values for process 3a: anoxic growth of heterotrophs with NO3
            stoiCoeff.addCell("growthXH_3a", "S_S", -2);
            stoiCoeff.addCell("growthXH_3a", "S_NO2", 1.724126344);
            stoiCoeff.addCell("growthXH_3a", "S_NO3", -1.724126344);
            stoiCoeff.addCell("growthXH_3a", "S_HPO4", -0.01);
            stoiCoeff.addCell("growthXH_3a", "S_HCO3", 0.62);
            stoiCoeff.addCell("growthXH_3a", "S_H", 0.051021505);
            stoiCoeff.addCell("growthXH_3a", "X_H", 1);

            //values for process 3b: anoxic growth of heterotrophs with NO2
            stoiCoeff.addCell("growthXH_3b", "S_S", -3.333333333);
            stoiCoeff.addCell("growthXH_3b", "S_NO2", -2.621675627);
            stoiCoeff.addCell("growthXH_3b", "S_HPO4", 0.003333333);
            stoiCoeff.addCell("growthXH_3b", "S_HCO3", 1.38);
            stoiCoeff.addCell("growthXH_3b", "S_H", -0.072047491);
            stoiCoeff.addCell("growthXH_3b", "X_H", 1);

            // values for process 4: anoxic endogenous respiration of heterotrophs
            stoiCoeff.addCell("respXH_4", "S_NH4", 0.114);
            stoiCoeff.addCell("respXH_4", "S_NO3", -0.432611828);
            stoiCoeff.addCell("respXH_4", "S_HPO4", 0.028);
            stoiCoeff.addCell("respXH_4", "S_HCO3", 0.398);
            stoiCoeff.addCell("respXH_4", "S_H", -0.004070584);
            stoiCoeff.addCell("respXH_4", "X_H", -1);
            stoiCoeff.addCell("respXH_4", "X_I", 0.2);

            // values for process 5: growth of 1st-stage nitrifiers
            stoiCoeff.addCell("growthXN1_5", "S_NH4", -7.692307692);
            stoiCoeff.addCell("growthXN1_5", "S_NO2", 7.572307692);
            stoiCoeff.addCell("growthXN1_5", "S_HPO4", -0.03);
            stoiCoeff.addCell("growthXN1_5", "S_O2", -24.35253574);
            stoiCoeff.addCell("growthXN1_5", "S_HCO3", -0.52);
            stoiCoeff.addCell("growthXN1_5", "S_H", 1.045060853);
            stoiCoeff.addCell("growthXN1_5", "X_N1", 1);

            // values for process 6: aerobic endogenous respiration of 1st-stage nitrifiers
            stoiCoeff.addCell("respXN1_6", "S_NH4", 0.114);
            stoiCoeff.addCell("respXN1_6", "S_HPO4", 0.028);
            stoiCoeff.addCell("respXN1_6", "S_O2", -1.236033794);
            stoiCoeff.addCell("respXN1_6", "S_HCO3", 0.398);
            stoiCoeff.addCell("respXN1_6", "S_H", 0.026830261);
            stoiCoeff.addCell("respXN1_6", "X_N1", -1);
            stoiCoeff.addCell("respXN1_6", "X_I", 0.2);

            // values for process 7: growth of 2nd-stage nitrifiers
            stoiCoeff.addCell("growthXN2_7", "S_NO2", -33.33333333);
            stoiCoeff.addCell("growthXN2_7", "S_NO3", 33.21333333);
            stoiCoeff.addCell("growthXN2_7", "S_HPO4", -0.03);
            stoiCoeff.addCell("growthXN2_7", "S_O2", -35.93700461);
            stoiCoeff.addCell("growthXN2_7", "S_HCO3", -0.52);
            stoiCoeff.addCell("growthXN2_7", "S_H", -0.053840246);
            stoiCoeff.addCell("growthXN2_7", "X_N2", 1);

            // values for process 8: aerobic endogenous respiration of 2nd-stage nitrifiers
            stoiCoeff.addCell("respXN2_8", "S_NH4", 0.114);
            stoiCoeff.addCell("respXN2_8", "S_HPO4", 0.028);
            stoiCoeff.addCell("respXN2_8", "S_O2", -1.236033794);
            stoiCoeff.addCell("respXN2_8", "S_HCO3", 0.398);
            stoiCoeff.addCell("respXN2_8", "S_H", 0.026830261);
            stoiCoeff.addCell("respXN2_8", "X_N2", -1);
            stoiCoeff.addCell("respXN2_8", "X_I", 0.2);

            // values for process 9a: growth of algae with NH4
            stoiCoeff.addCell("growthXALG_9a", "S_NH4", -0.06);
            stoiCoeff.addCell("growthXALG_9a", "S_HPO4", -0.01);
            stoiCoeff.addCell("growthXALG_9a", "S_O2", 0.930046083);
            stoiCoeff.addCell("growthXALG_9a", "S_HCO3", -0.36);
            stoiCoeff.addCell("growthXALG_9a", "S_H", -0.026359447);
            stoiCoeff.addCell("growthXALG_9a", "X_ALG", 1);

            // values for process 9b: growth of algae with NO3
            stoiCoeff.addCell("growthXALG_9b", "S_NO3", -0.06);
            stoiCoeff.addCell("growthXALG_9b", "S_HPO4", -0.01);
            stoiCoeff.addCell("growthXALG_9b", "S_O2", 1.204331797);
            stoiCoeff.addCell("growthXALG_9b", "S_HCO3", -0.36);
            stoiCoeff.addCell("growthXALG_9b", "S_H", -0.034930876);
            stoiCoeff.addCell("growthXALG_9b", "X_ALG", 1);

            // values for process 10: aerobic endogenous respiration of algae
            stoiCoeff.addCell("respXALG_10", "S_NH4", 0.054);
            stoiCoeff.addCell("respXALG_10", "S_HPO4", 0.008);
            stoiCoeff.addCell("respXALG_10", "S_O2", -0.556417819);
            stoiCoeff.addCell("respXALG_10", "S_HCO3", 0.238);
            stoiCoeff.addCell("respXALG_10", "S_H", 0.01649232);
            stoiCoeff.addCell("respXALG_10", "X_ALG", -1);
            stoiCoeff.addCell("respXALG_10", "X_I", 0.2);

            // values for process 11: death of algae
            stoiCoeff.addCell("deathXALG_11", "S_NH4", 0.02652);
            stoiCoeff.addCell("deathXALG_11", "S_HPO4", 0.0038);
            stoiCoeff.addCell("deathXALG_11", "S_O2", 0.189466298);
            stoiCoeff.addCell("deathXALG_11", "S_HCO3", 0.00164);
            stoiCoeff.addCell("deathXALG_11", "S_H", -0.001512458);
            stoiCoeff.addCell("deathXALG_11", "X_ALG", -1);
            stoiCoeff.addCell("deathXALG_11", "X_S", 0.496);
            stoiCoeff.addCell("deathXALG_11", "X_I", 0.124);

            // values for process 12a: growth of consumers on X_ALG
            stoiCoeff.addCell("growthXCON_12a", "S_NH4", 0.12);
            stoiCoeff.addCell("growthXCON_12a", "S_HPO4", 0.02);
            stoiCoeff.addCell("growthXCON_12a", "S_O2", -0.140092166);
            stoiCoeff.addCell("growthXCON_12a", "S_HCO3", 0.3);
            stoiCoeff.addCell("growthXCON_12a", "S_H", 0.017718894);
            stoiCoeff.addCell("growthXCON_12a", "X_ALG", -5);
            stoiCoeff.addCell("growthXCON_12a", "X_CON", 1);
            stoiCoeff.addCell("growthXCON_12a", "X_S", 2);

            // values for process 12b: growth of consumers on X_S
            stoiCoeff.addCell("growthXCON_12b", "S_NH4", 0.12);
            stoiCoeff.addCell("growthXCON_12b", "S_HPO4", 0.02);
            stoiCoeff.addCell("growthXCON_12b", "S_O2", -4.440092166);
            stoiCoeff.addCell("growthXCON_12b", "S_HCO3", 1.35);
            stoiCoeff.addCell("growthXCON_12b", "S_H", 0.105218894);
            stoiCoeff.addCell("growthXCON_12b", "X_CON", 1);
            stoiCoeff.addCell("growthXCON_12b", "X_S", -3);

            // values for process 12c: growth of consumers on X_H
            stoiCoeff.addCell("growthXCON_12c", "S_NH4", 0.42);
            stoiCoeff.addCell("growthXCON_12c", "S_HPO4", 0.12);
            stoiCoeff.addCell("growthXCON_12c", "S_O2", -3.538172043);
            stoiCoeff.addCell("growthXCON_12c", "S_HCO3", 1.1);
            stoiCoeff.addCell("growthXCON_12c", "S_H", 0.069408602);
            stoiCoeff.addCell("growthXCON_12c", "X_H", -5);
            stoiCoeff.addCell("growthXCON_12c", "X_CON", 1);
            stoiCoeff.addCell("growthXCON_12c", "X_S", 2);

            // values for process 12d: growth of consumers on X_N1
            stoiCoeff.addCell("growthXCON_12d", "S_NH4", 0.42);
            stoiCoeff.addCell("growthXCON_12d", "S_HPO4", 0.12);
            stoiCoeff.addCell("growthXCON_12d", "S_O2", -3.538172043);
            stoiCoeff.addCell("growthXCON_12d", "S_HCO3", 1.1);
            stoiCoeff.addCell("growthXCON_12d", "S_H", 0.069408602);
            stoiCoeff.addCell("growthXCON_12d", "X_N1", -5);
            stoiCoeff.addCell("growthXCON_12d", "X_CON", 1);
            stoiCoeff.addCell("growthXCON_12d", "X_S", 2);

            // values for process 12e: growth of consumers on X_N2
            stoiCoeff.addCell("growthXCON_12e", "S_NH4", 0.42);
            stoiCoeff.addCell("growthXCON_12e", "S_HPO4", 0.12);
            stoiCoeff.addCell("growthXCON_12e", "S_O2", -3.538172043);
            stoiCoeff.addCell("growthXCON_12e", "S_HCO3", 1.1);
            stoiCoeff.addCell("growthXCON_12e", "S_H", 0.069408602);
            stoiCoeff.addCell("growthXCON_12e", "X_N2", -5);
            stoiCoeff.addCell("growthXCON_12e", "X_CON", 1);
            stoiCoeff.addCell("growthXCON_12e", "X_S", 2);

            // values for process 13: aerobic endogenous respiration of consumers
            stoiCoeff.addCell("respXCON_13", "S_NH4", 0.054);
            stoiCoeff.addCell("respXCON_13", "S_HPO4", 0.008);
            stoiCoeff.addCell("respXCON_13", "S_O2", -0.556417819);
            stoiCoeff.addCell("respXCON_13", "S_HCO3", 0.238);
            stoiCoeff.addCell("respXCON_13", "S_H", 0.01649232);
            stoiCoeff.addCell("respXCON_13", "X_CON", -1);
            stoiCoeff.addCell("respXCON_13", "X_I", 0.2);

            // values for process 14: death of consumers
            stoiCoeff.addCell("deathXCON_14", "S_NH4", 0.02652);
            stoiCoeff.addCell("deathXCON_14", "S_HPO4", 0.0038);
            stoiCoeff.addCell("deathXCON_14", "S_O2", 0.189466298);
            stoiCoeff.addCell("deathXCON_14", "S_HCO3", 0.00164);
            stoiCoeff.addCell("deathXCON_14", "S_H", -0.001512458);
            stoiCoeff.addCell("deathXCON_14", "X_CON", -1);
            stoiCoeff.addCell("deathXCON_14", "X_S", 0.496);
            stoiCoeff.addCell("deathXCON_14", "X_I", 0.124);

            // values for process 15: hydrolysis
            stoiCoeff.addCell("hydrolysis_15", "S_S", 1);
            stoiCoeff.addCell("hydrolysis_15", "S_NH4", 0);
            stoiCoeff.addCell("hydrolysis_15", "S_HPO4", 0);
            stoiCoeff.addCell("hydrolysis_15", "S_O2", 0);
            stoiCoeff.addCell("hydrolysis_15", "S_HCO3", 0);
            stoiCoeff.addCell("hydrolysis_15", "S_H", 0);
            stoiCoeff.addCell("hydrolysis_15", "X_S", -1);

            // values for process 16: equilibrium CO2/HCO3^-
            stoiCoeff.addCell("equilibrium_16", "S_CO2", -1);
            stoiCoeff.addCell("equilibrium_16", "S_HCO3", 1);
            stoiCoeff.addCell("equilibrium_16", "S_H", 0.083333333);

            // values for process 17: equilibrium HCO3^-/CO3^2-
            stoiCoeff.addCell("equilibrium_17", "S_HCO3", -1);
            stoiCoeff.addCell("equilibrium_17", "S_CO3", 1);
            stoiCoeff.addCell("equilibrium_17", "S_H", 0.083333333);

            // values for process 18: equilibrium H^+/OH^-
            stoiCoeff.addCell("equilibrium_18", "S_H", 1);
            stoiCoeff.addCell("equilibrium_18", "S_OH", 1);

            // values for process 19: equilibrium NH4^+/NH3
            stoiCoeff.addCell("equilibrium_19", "S_NH4", -1);
            stoiCoeff.addCell("equilibrium_19", "S_NH3", 1);
            stoiCoeff.addCell("equilibrium_19", "S_H", 0.071428571);

            // values for process 20: equilibrium H2PO4^-/HPO4^2-
            stoiCoeff.addCell("equilibrium_20", "S_HPO4", 1);
            stoiCoeff.addCell("equilibrium_20", "S_H2PO4", -1);
            stoiCoeff.addCell("equilibrium_20", "S_H", 0.032258065);

            // values for process 21: equilibrium Ca^2+/CO3^2-
            stoiCoeff.addCell("equilibrium_21", "S_CO3", 0.3);
            stoiCoeff.addCell("equilibrium_21", "S_Ca", 1);

            // values for process 22: adsorption of phosphate
            stoiCoeff.addCell("adsorptionP_22", "S_HPO4", -1);
            stoiCoeff.addCell("adsorptionP_22", "X_P", 1);

            // values for process 23: desorption of phosphate
            stoiCoeff.addCell("desorptionP_23", "S_HPO4", 1);
            stoiCoeff.addCell("desorptionP_23", "X_P", -1);

        }
        
        return stoiCoeff;
    }

public static Table2D getMassFractions() {
        
        if (massFrac == null) {
            // create Table2D
            massFrac = new Table2D();

            // fill table with data
            // compositions of organic components from Reichert et al. 2001 (RWQM1)
          
                //S_S dissolved organic substances in (gSS)
                //S_I inert dissolved organic substances in (gSI)
                //X_H heterotrophic organisms (gXH)
                //X_N1 organisms oxidising ammonia to nitrite (gN1)
                //X_N2 organisms oxidising nitrite to nitrate (gN2)
                //X_ALG algae and macrophytes (gALG)
                //X_CON consumers (gCON)
                //X_S particulate organic material (gXS)
                //X_I inert particulate organic material (gXI)

                // alpha_C in gC/gOM
                // alpha_H in gH/gOM
                // alpha_O in gO/gOM
                // alpha_N in gN/gOM
                // alpha_P in gP/gOM
                // alpha_X in gX/gOM

            massFrac.addCell("alpha_C", "S_S", 0.57);
            massFrac.addCell("alpha_C", "S_I", 0.61);
            massFrac.addCell("alpha_C", "X_H", 0.52);
            massFrac.addCell("alpha_C", "X_N1", 0.52);
            massFrac.addCell("alpha_C", "X_N2", 0.52);
            massFrac.addCell("alpha_C", "X_ALG", 0.36);
            massFrac.addCell("alpha_C", "X_CON", 0.36);
            massFrac.addCell("alpha_C", "X_S", 0.57);
            massFrac.addCell("alpha_C", "X_I", 0.61);

            massFrac.addCell("alpha_H", "S_S", 0.08);
            massFrac.addCell("alpha_H", "S_I", 0.07);
            massFrac.addCell("alpha_H", "X_H", 0.08);
            massFrac.addCell("alpha_H", "X_N1", 0.08);
            massFrac.addCell("alpha_H", "X_N2", 0.08);
            massFrac.addCell("alpha_H", "X_ALG", 0.07);
            massFrac.addCell("alpha_H", "X_CON", 0.07);
            massFrac.addCell("alpha_H", "X_S", 0.08);
            massFrac.addCell("alpha_H", "X_I", 0.07);

            massFrac.addCell("alpha_O", "S_S", 0.28);
            massFrac.addCell("alpha_O", "S_I", 0.28);
            massFrac.addCell("alpha_O", "X_H", 0.25);
            massFrac.addCell("alpha_O", "X_N1", 0.25);
            massFrac.addCell("alpha_O", "X_N2", 0.25);
            massFrac.addCell("alpha_O", "X_ALG", 0.5);
            massFrac.addCell("alpha_O", "X_CON", 0.5);
            massFrac.addCell("alpha_O", "X_S", 0.28);
            massFrac.addCell("alpha_O", "X_I", 0.28);

            massFrac.addCell("alpha_N", "S_S", 0.06);
            massFrac.addCell("alpha_N", "S_I", 0.03);
            massFrac.addCell("alpha_N", "X_H", 0.12);
            massFrac.addCell("alpha_N", "X_N1", 0.12);
            massFrac.addCell("alpha_N", "X_N2", 0.12);
            massFrac.addCell("alpha_N", "X_ALG", 0.06);
            massFrac.addCell("alpha_N", "X_CON", 0.06);
            massFrac.addCell("alpha_N", "X_S", 0.06);
            massFrac.addCell("alpha_N", "X_I", 0.06);

            massFrac.addCell("alpha_P", "S_S", 0.01);
            massFrac.addCell("alpha_P", "S_I", 0.01);
            massFrac.addCell("alpha_P", "X_H", 0.03);
            massFrac.addCell("alpha_P", "X_N1", 0.03);
            massFrac.addCell("alpha_P", "X_N2", 0.03);
            massFrac.addCell("alpha_P", "X_ALG", 0.01);
            massFrac.addCell("alpha_P", "X_CON", 0.01);
            massFrac.addCell("alpha_P", "X_S", 0.01);
            massFrac.addCell("alpha_P", "X_I", 0.01);

            massFrac.addCell("alpha_X", "S_S", 0);
            massFrac.addCell("alpha_X", "S_I", 0);
            massFrac.addCell("alpha_X", "X_H", 0);
            massFrac.addCell("alpha_X", "X_N1", 0);
            massFrac.addCell("alpha_X", "X_N2", 0);
            massFrac.addCell("alpha_X", "X_ALG", 0);
            massFrac.addCell("alpha_X", "X_CON", 0);
            massFrac.addCell("alpha_X", "X_S", 0);
            massFrac.addCell("alpha_X", "X_I", 0);

        }

        return massFrac;

    }

public static Table2D getStoichiometricParameters() {

        if (stoiPara == null) {
            // create Table2D
            stoiPara = new Table2D();

            // fill table with data
            // stoichiometric parameters from Reichert et al. 2001 (RWQM1)

                //Y_Haer yield for aerobic heterotrophic growth (gXH/gSS)
                //Y_Hanox_NO3 yield for anoxic heterotrophic growth with NO3 (gXH/gSS)
                //Y_Hanox_NO2 yield for anoxic heterotrophic growth with NO2 (gXH/gSS)
                //fI_BAC fraction of respired heterotrophic and autotrophic biomass that becomes inert (gXI/gXH;N1;N2)
                //Y_N1 yield for growth of 1st-step nitrifiers (gXN1/gNH4-N)
                //Y_N2 yield for growth of 2nd-step nitrifiers (gXN2/gNO2-N)
                //fI_ALG fraction of particulate organic matter that becomes inert during death of algae (gXI/g(XS+XI))
                //Y_ALGdeath yield for death of algae (set to a value that avoids consumption of nutrients and oxygen) (g(XS+XI)/gXALG)
                //Y_CON yield for grazing (set to a value that avoids consumption of nutrients and oxygen) (gXCON/gXALG)
                //f_e fraction of incorporated biomass that is excreted as faecal pellets (gXS/gXCON)
                //fI_CON fraction of particulate organic matter that becomes inert during death of consumers (gXI/g(XS+XI))
                //Y_CONdeath yield for death of consumers (set to a value that avoids consumption of nutrients and oxygen) (g(XS+XI/gXCON)
                //Y_HYD yield for hydrolysis (set to a value that avoids consumption of nutrients and oxygen) (gSS/gXS)

            stoiPara.addCell("Y_Haer", "value", 0.6);
            stoiPara.addCell("Y_Hanox_NO3", "value", 0.5);
            stoiPara.addCell("Y_Hanox_NO2", "value", 0.3);
            stoiPara.addCell("fI_BAC", "value", 0.2);
            stoiPara.addCell("Y_N1", "value", 0.13);
            stoiPara.addCell("Y_N2", "value", 0.03);
            stoiPara.addCell("fI_ALG", "value", 0.2);
            stoiPara.addCell("Y_ALGdeath", "value", 0.62);
            stoiPara.addCell("Y_CON", "value", 0.2);
            stoiPara.addCell("f_e", "value", 0.4);
            stoiPara.addCell("fI_CON", "value", 0.2);
            stoiPara.addCell("Y_CONdeath", "value", 0.62);
            stoiPara.addCell("Y_HYD", "value", 1);

        }

        return stoiPara;
}
}