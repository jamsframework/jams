package staging.j2k;

import staging.j2k.types.HRU;
import weighting.CalcAreaWeight;
import geoprocessing.CalcLatLong;
import climate.CalcLanduseStateVars;
import java.util.List;
import java.util.logging.Logger;
import oms3.Compound;
import oms3.annotations.*;
import oms3.util.Threads;
import radiation.CalcExtraterrRadiation;
import regionalization.CalcNidwWeights;
import static oms3.util.Threads.*;
import static oms3.annotations.Role.*;

@Description
    ("InitHRU Context component.")
@Author
    (name = "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/staging/j2k/InitProcesses.java $")
@VersionInfo
    ("$Id: InitProcesses.java 961 2010-02-11 20:35:32Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
public class InitProcesses {

    private static final Logger log =
            Logger.getLogger("oms3.model." + InitProcesses.class.getSimpleName());

    Main model;

    @Description("HRU list")
    @In @Out public List<HRU> hrus;

    @In public double basin_area;

    @In public double[] xCoordTmean;
    @In public double[] yCoordTmean;
    @In public double[] xCoordTmin;
    @In public double[] yCoordTmin;
    @In public double[] xCoordTmax;
    @In public double[] yCoordTmax;
    @In public double[] xCoordAhum;
    @In public double[] yCoordAhum;
    @In public double[] xCoordPrecip;
    @In public double[] yCoordPrecip;
    @In public double[] xCoordSunh;
    @In public double[] yCoordSunh;
    @In public double[] xCoordWind;
    @In public double[] yCoordWind;

    public InitProcesses(Main model) {
        this.model = model;
    }
    
    @Execute
    public void execute() throws Exception {

        System.out.println("Init Processes ..");
        long now1 = System.currentTimeMillis();
        Threads.seq_ief(new CompList<HRU>(hrus) {

            @Override
            public Compound create(HRU hru) {
                return new Processes(hru, InitProcesses.this);
            }
        });

        long now2 = System.currentTimeMillis();
        System.out.println("   Init done.   " + (now2 - now1));
//        System.exit(1);

    }

    public class Processes extends Compound {

        HRU hru;
        InitProcesses init;
        
        CalcAreaWeight areaWeight = new CalcAreaWeight();
        CalcLatLong calcLatLong = new CalcLatLong();
        CalcLanduseStateVars luStateVars = new CalcLanduseStateVars();
        CalcExtraterrRadiation rad = new CalcExtraterrRadiation();
        
        CalcNidwWeights idwTmean = new CalcNidwWeights();
        CalcNidwWeights idwTmin = new CalcNidwWeights();
        CalcNidwWeights idwTmax = new CalcNidwWeights();
        CalcNidwWeights idwAhum = new CalcNidwWeights();
        CalcNidwWeights idwPrecip = new CalcNidwWeights();
        CalcNidwWeights idwSunh = new CalcNidwWeights();
        CalcNidwWeights idwWind = new CalcNidwWeights();

        Processes(HRU hru, InitProcesses init) {
            this.hru = hru;
            this.init = init;
        }

        private void idw(Object idw, String climate) {
            field2in(model, "nidw" + climate, idw, "nidw");
            field2in(model, "pidw" + climate, idw, "pidw");
            field2in(init, "xCoord" + climate, idw, "statX");
            field2in(init, "yCoord" + climate, idw, "statY");
            field2in(hru, "x", idw);
            field2in(hru, "y", idw);
            out2field(idw, "statWeights", hru, "statWeights" + climate);
        }

        @Initialize
        public void initialize() {
            // areaweighting
            field2in(init, "basin_area", areaWeight);
            field2in(hru, "area", areaWeight, "hru_area");
            out2field(areaWeight, "areaweight", hru, "area_weight");

            // latlong
            field2in(model, "projection", calcLatLong);
            field2in(hru, "x", calcLatLong);
            field2in(hru, "y", calcLatLong);
            field2in(hru, "slope", calcLatLong);
            field2in(hru, "aspect", calcLatLong);
            out2field(calcLatLong, "latitude", hru);
            out2field(calcLatLong, "longitude", hru);
            out2field(calcLatLong, "slAsCfArray", hru);

            // landuse
            field2in(hru, "elevation", luStateVars);
            field2in(hru.landuse, "LAI", luStateVars);
            field2in(hru.landuse, "effHeight", luStateVars);
            out2field(luStateVars, "LAIArray", hru);
            out2field(luStateVars, "effHArray", hru);

            // radiation
            field2in(model, "tempRes", rad);
            field2in(model, "locGrw", rad);
            field2in(model, "longTZ", rad);
            out2in(calcLatLong, "latitude", rad);
            out2in(calcLatLong, "longitude", rad);
            out2field(rad, "extRadArray", hru);

            // idw
            idw(idwTmean, "Tmean");
            idw(idwTmin,  "Tmin");
            idw(idwTmax,  "Tmax");
            idw(idwAhum,  "Ahum");
            idw(idwSunh,  "Sunh");
            idw(idwPrecip,  "Precip");
            idw(idwWind,  "Wind");
            
//            initializeComponents();
        }
    }

}
