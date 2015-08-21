package weighting;

import staging.j2k.types.HRU;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Basin area aggregation without weighting")
@Author
    (name= "Olaf David")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/weighting/AreaAggregator.java $")
@VersionInfo
    ("$Id: AreaAggregator.java 957 2010-02-11 20:24:54Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
public class AreaAggregator  {

      private static final Logger log = Logger.getLogger("oms3.model." +
            AreaAggregator.class.getSimpleName());
    
    @Description("HRU list")
    @In public List<HRU> hrus;
    
    @Description("basin area")
    @Out public double basin_area;
    
    @Execute 
    public void execute() {
        basin_area = 0;
        for (HRU hru : hrus) {
            basin_area += hru.area;
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("Basin area :" + basin_area);
        }
    }
    
}
