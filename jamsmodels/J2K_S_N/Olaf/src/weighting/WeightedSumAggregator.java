package weighting;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("WeightedSumAggregator.")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Utilities")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/weighting/WeightedSumAggregator.java $")
@VersionInfo
    ("$Id: WeightedSumAggregator.java 957 2010-02-11 20:24:54Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class WeightedSumAggregator  {
    
    @Description("value")
    @In public double val;
    
    @Description("weight attribute.")
    @In public double weight;
    
    @Description("sum")
    @In public double sum_in;

    @Out public double sum_out;
    
    @Execute
    public void execute() {
        sum_out = sum_in + (val / weight);
    }

}
