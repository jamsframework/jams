package geoprocessing;

import lib.UTMConversion;
import lib.GKConversion;
import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates latitude and longitude")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/geoprocessing/CalcLatLong.java $")
@VersionInfo
    ("$Id: CalcLatLong.java 996 2010-02-19 21:17:43Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class CalcLatLong  {
    
    @Description("Projection [GK, UTMZZL]")
    @Role(PARAMETER)
    @In public String projection;

    @Description("entity x-coordinate")
    @In public double x;

    @Description("entity y-coordinate")
    @In public double y;

    @Description("entity slope")
    @In public double slope;

    @Description("entity aspect")
    @In public double aspect;
    
    @Description("entity latitude")
    @Out public double latitude;

    @Description("entity longitude")
    @Out public double longitude;

    @Description("entity entity slopeAspectCorrectionFactor")
    @Out public double[] slAsCfArray;
    
    @Execute
    public void execute() {
        
        String proj = projection;
//        if(this.projection == null)
//            proj = "GK";
//        else
//            proj = this.projection;
//        double[] latLong = new double[2];
        double[] latlong = null;
        if(proj.equals("GK")){
            latlong = GKConversion.GK2LatLon(x, y);
        } else if(proj.substring(0,3).equals("UTM")){
            int len = proj.length();
            String zoneStr = proj.substring(3, len);
            latlong = UTMConversion.utm2LatLong(x, y, zoneStr);
        }
        latitude = latlong[0];
        longitude = latlong[1];
        slAsCfArray = new double[366];
        for(int i = 0; i < 366; i++){
            slAsCfArray[i] = Geo.slopeAspectCorrFactor(i+1, latitude, slope, aspect);
        }
    }
}
