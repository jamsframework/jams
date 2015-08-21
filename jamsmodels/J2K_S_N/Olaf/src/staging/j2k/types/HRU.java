package staging.j2k.types;

public class HRU {

    // from input parameter file;
    public int ID;
    public double x;
    public double y;
    public double elevation;
    public double area;
    public int type;
    public int to_hruID;
    public int to_reachID;
    public double slope;
    public double aspect;
    public double flowlength;
    public int soilID;
    public int landuseID;
    public int hgeoID;

    // references
    public HRU to_hru;
    public Reach to_reach;
    public Landuse landuse;
    public SoilType soilType;
    public HGeo hgeoType;
    
    public double area_weight;

    public double latitude;
    public double longitude;
    public double[] slAsCfArray;

    public double[] LAIArray;
    public double[] effHArray;
    public double[] extRadArray;

    public double maxRG1;
    public double maxRG2;
    public double actRG1;
    public double actRG2;

    public double[] statWeightsTmean;
    public double[] statWeightsTmin;
    public double[] statWeightsTmax;
    public double[] statWeightsAhum;
    public double[] statWeightsPrecip;
    public double[] statWeightsSunh;
    public double[] statWeightsWind;

    public double tmean;
    public double tmin;
    public double tmax;
    public double ahum;
    public double precip;
    public double sunh;
    public double wind;

    public double rhum;

    public double solRad;
    public double netRad;
    public double refET;

    public double ra;
    public double rs;
    public double potET;

    public double rain;
    public double snow;

    public double interception;
    public double throughfall;
    public double intercStorage;
    public double actET;
    public double netRain;
    public double netSnow;

    public double snowTotSWE;
    public double drySWE;
    public double totDens;
    public double dryDens;
    public double snowDepth;
    public double snowAge;
    public double snowColdContent;
    public double snowMelt;

    public double satSoil;
    public double maxMPS;
    public double maxLPS;
    public double actMPS;
    public double actLPS;
    public double satMPS;
    public double satLPS;
    public double inRD1;
    public double inRD2;
    public double outRD1;
    public double outRD2;
    public double interflow;

    public double inRG1;
    public double inRG2;
    public double outRG1;
    public double outRG2;

    public double percolation;
    public double thoughfall; // check
    public double actDPS; // check


    @Override
    public String toString() {
        return "HRU[id="+ID+"]";
    }
   
}

