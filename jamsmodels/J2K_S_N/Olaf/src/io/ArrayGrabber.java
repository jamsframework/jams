package io;

import java.util.Calendar;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Component summary info ...")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/ArrayGrabber.java $")
@VersionInfo
    ("$Id: ArrayGrabber.java 980 2010-02-12 15:56:11Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class ArrayGrabber  {
    
    @Description("temporal resolution")
    @Unit("d | h")
    @Role(PARAMETER)
    @In public String tempRes;

    @Description("Time")
    @In public java.util.Calendar time;

    @Description("extraTerrRadiationArray")
    @In public double[] extRadArray;

    @Description("LeafAreaIndexArray")
    @In public double[] LAIArray;

    @Description("EffectiveHeightArray")
    @In public double[] effHArray;

    @Description("rsc0Array")
    @In public double[] rsc0Array;

    @Description("slopeAscpectCorrectionFactorArray")
    @In public double[] slAsCfArray;

    @Description("actExtraTerrRadiation")
    @Out public double actExtRad;

    @Description("actLAI")
    @Out public double actLAI;

    @Description("actEffH")
    @Out public double actEffH;

    @Description("actRsc0")
    @Out public double actRsc0;

    @Description("actSlAsCf")
    @Out public double actSlAsCf;
  
    @Execute
    public void execute() {
        int month = time.get(Calendar.MONTH);
        int day = time.get(Calendar.DAY_OF_YEAR) - 1;
        
        actRsc0 = rsc0Array[month];
        if(tempRes.equals("d")){
            actLAI = LAIArray[day];
            actEffH = effHArray[day];
            actExtRad = extRadArray[day];
            actSlAsCf = slAsCfArray[day];
        } else if(tempRes.equals("h")){
            int hour = time.get(Calendar.HOUR) + (24 * day);
            actLAI = LAIArray[day];
            actEffH = effHArray[day];
            actExtRad = extRadArray[hour];
            actSlAsCf = slAsCfArray[day];
        }
    }  
}
