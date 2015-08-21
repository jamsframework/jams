package soilWater;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Calculates soil water balance for each HRU without vertical layers")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Soilwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/soilWater/InitJ2KProcessLumpedSoilWaterStates.java $")
@VersionInfo
    ("$Id: InitJ2KProcessLumpedSoilWaterStates.java 893 2010-01-29 16:06:46Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")
    
 public class InitJ2KProcessLumpedSoilWaterStates  {
    
    @Description("field capacity adaptation factor")
    @Role(PARAMETER)
    @In public double FCAdaptation;
    
    @Description("air capacity adaptation factor")
    @Role(PARAMETER)
    @In public double ACAdaptation;

    @Description("start saturation of LPS")
    @Role(PARAMETER)
    @In public double satStartLPS;
    
    @Description("start saturation of MPS")
    @Role(PARAMETER)
    @In public double satStartMPS;

    @Description("field capacity")
    @In public double[] fc;
    
    @Description("area")
    @In public double area;

    @Description("HRU statevar rooting depth")
    @In public double rootDepth;

    @Description("aircap")
    @In public double aircap;

    @Description("HRU state var saturation of whole soil")
    @Out public double satSoil;

    @Description("HRU attribute maximum MPS")
    @Out public double maxMPS;

    @Description("HRU attribute maximum MPS")
    @Out public double maxLPS;

    @Description("HRU state var actual MPS")
    @Out public double actMPS;

    @Description("HRU state var actual LPS")
    @Out public double actLPS;

    @Description("HRU state var saturation of MPS")
    @Out public double satMPS;

    @Description("HRU state var saturation of LPS")
    @Out public double satLPS;
    
    @Execute
    public void execute() {

        double mxMPS = 0;
        for(int d = 0; d < rootDepth; d++){
            mxMPS += fc[d];
        }
        
        mxMPS = mxMPS * area;
        mxMPS = mxMPS * FCAdaptation;
        
        double mxLPS = aircap * area;
        mxLPS = mxLPS * ACAdaptation;

        actLPS = mxLPS * satStartLPS;
        actMPS = mxMPS * satStartMPS;
        maxMPS = mxMPS;
        maxLPS = mxLPS;
        satMPS = actMPS / mxMPS;
        satLPS = actLPS / mxLPS;

        satSoil = (actMPS + actLPS) / (mxMPS + mxLPS);
        System.out.print(".");
        System.out.flush();
    }
}
