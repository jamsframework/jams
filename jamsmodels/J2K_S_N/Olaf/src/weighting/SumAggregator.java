package weighting;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Variable aggregation without weighting")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/weighting/SumAggregator.java $")
@VersionInfo
    ("$Id: SumAggregator.java 957 2010-02-11 20:24:54Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class SumAggregator  {
    
    @Description("value")
    @In public double val;
    
    @Description("sum")
    @Out public double sum;
    
    @Execute public void execute() {
        sum = sum + val;
    }
    
}
