/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package staging;

import weighting.CalcAreaWeight;
import weighting.SumAggregator;
import weighting.WeightedSumAggregator;
import crop.J2KSNDormancy;
import erosion.Musle;
import geoprocessing.CalcLatLong;
import groundwater.InitJ2KProcessGroundWater;
import groundwater.J2KProcessGroundwater;
import climate.CalcAbsoluteHumidity;
import climate.CalcLanduseStateVars;
import climate.CalcRelativeHumidity;
import climate.RainCorrectionRichter;
import interception.J2KProcessInterception;
import io.ArrayGrabber;
import io.LayeredSoilParaReader;
import io.StandardEntityReader;
import io.StationReader;
import java.io.FileNotFoundException;
//import oms3.util.Generators;
import potet.PenmanMonteith;
import potet.RefET;
import radiation.CalcDailyNetRadiation;
import radiation.CalcDailyNetRadiationSolrad;
import radiation.CalcDailySolarRadiation;
import radiation.CalcExtraterrRadiation;
import regionalization.CalcIDWeights;
import regionalization.CalcNidwWeights;
import regionalization.Regionalization;
import regionalization.Regionalization1;
import routing.J2KProcessHorizonRouting;
import routing.J2KProcessReachRouting;
import routing.J2KProcessRouting;
import groundwater.J2KGroundwaterN;
import groundwater.J2KGroundwaterN2005;
import routing.J2KNRoutinglayer;
import soilWater.J2KNSoilLayer;
import crop.J2KPlantGrowthNitrogenStress;
import crop.J2KPlantGrowthStress;
import crop.J2KPlantGrowthTemperatureStress;
import crop.J2KPlantGrowthWaterStress;
import routing.J2KProcessReachRoutingN;
import soilTemp.J2KSoilTemplayer;
import crop.j2kSNStress;
import crop.PotCropGrowth;
import groundwater.InitJ2KGroundwaterN;
import soilWater.InitJ2KNSoillayer;
import soilWater.InitJ2KProcessLayeredSoilWaterN2008;
import soilTemp.TempAvgSumlayer;
import io.SoilLayerWriter4;
import io.ManagementParaReader;
import potet.ETPETP;
import management.ManageLanduseSzeno;
import soilWater.J2KProcessLayeredSoilWater;
import soilWater.J2KProcessLayeredSoilWater2008;
import snow.CalcRainSnowParts;
import snow.J2KProcessSnow;
import soilWater.InitJ2KProcessLumpedSoilWaterStates;
import soilWater.J2KProcessLumpedSoilWater;

/**
 *
 * @author od
 */
public class Generator {

    static Object[] c = {
        // aggregate
        new CalcAreaWeight(),
        new SumAggregator(),
        new WeightedSumAggregator(),
        // crop
        new J2KSNDormancy(),
        // erosion
        new Musle(),
        //                new Output()

        // geo
        new CalcLatLong(),
        // groundater
        new InitJ2KProcessGroundWater(),
        new J2KProcessGroundwater(),
        // inputdata
        new CalcAbsoluteHumidity(),
        new CalcLanduseStateVars(),
        new CalcRelativeHumidity(),
        new RainCorrectionRichter(),
        // interception
        new J2KProcessInterception(),
        // io
        new ArrayGrabber(),
        new LayeredSoilParaReader(),
        new StandardEntityReader(),
        new StationReader(),
        // potet
        new PenmanMonteith(),
        new RefET(),
        // radiation
        new CalcDailyNetRadiation(),
        new CalcDailyNetRadiationSolrad(),
        new CalcDailySolarRadiation(),
        new CalcExtraterrRadiation(),
        // regionalization
        new CalcIDWeights(),
        new CalcNidwWeights(),
        new Regionalization1(),
        new Regionalization(),
        // Routing
        new J2KProcessHorizonRouting(),
        new J2KProcessReachRouting(),
        new J2KProcessRouting(),
        // sn
        new J2KGroundwaterN(),
        new J2KGroundwaterN2005(),
        new J2KNRoutinglayer(),
        new J2KNSoilLayer(),
        new J2KPlantGrowthNitrogenStress(),
        new J2KPlantGrowthStress(),
        new J2KPlantGrowthTemperatureStress(),
        new J2KPlantGrowthWaterStress(),
        new J2KProcessGroundwater(),
        new J2KProcessReachRoutingN(),
        new J2KSoilTemplayer(),
        new ETPETP(),
        // sn.crop
        new PotCropGrowth(),
        new j2kSNStress(),
        // sn.init
        new InitJ2KGroundwaterN(),
        new InitJ2KNSoillayer(),
        new InitJ2KProcessLayeredSoilWaterN2008(),
        new TempAvgSumlayer(),
        // sn.io
        new SoilLayerWriter4(),
        new ManagementParaReader(),
        // sn.management
        new ManageLanduseSzeno(),
        // sn.soillayer
        new J2KProcessLayeredSoilWater(),
        new J2KProcessLayeredSoilWater2008(),
        // snow
        new CalcRainSnowParts(),
        new J2KProcessSnow(),
        // soilwater
        new InitJ2KProcessLumpedSoilWaterStates(),
        new J2KProcessLumpedSoilWater(),
    };

    public static void main(String[] args) throws FileNotFoundException {
//        Generators.generateDocBook(new File("c:/tmp/ceap3.xml"), c, "CEAP Model Components");
    }
}
