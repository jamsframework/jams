package climate;

import oms3.annotations.*;
import static oms3.annotations.Role.*;
import lib.*;

@Description
    ("Calculates relative humidity from temperature and absolute humidity")
@Author
    (name=" Peter Krause, Sven Kralisch")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/climate/CalcRelativeHumidity.java $")
@VersionInfo
    ("$Id: CalcRelativeHumidity.java 970 2010-02-11 20:53:38Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class CalcRelativeHumidity  {
    
    @Description("mean tempeature")
    @In public double tmean;

    @Description("absolute humidity")
    @In public double ahum;

    @Description("relative humidity")
    @Out public double rhum;
    
    @Execute
    public void execute() {
        rhum = (ahum / Climate.maxHum(tmean)) * 100;
        // rhum should not be larger than 100%
        if(rhum > 100) {
            rhum = 100;
        }
    }
}
