package io;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Author
    (name="Manfred Fink")
@Description
    ("Writes array values of the soil storages for the first 4 " +
    "soil layers. If layers are not existing values are 0")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/io/SoilLayerWriter4.java $")
@VersionInfo
    ("$Id: SoilLayerWriter4.java 958 2010-02-11 20:29:31Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class SoilLayerWriter4  {
    
    @Description("depth of soil layer")
    @Unit("cm")
    @In public double[] layerdepth;
    
    @Description("HRU area")
    @Unit("m^2")
    @In public double area;
    
    @Description("HRU attribute maximum FPS")
    @Unit("mm")
    @In public double[] maxFPS;
    
    @Description("HRU attribute maximum MPS")
    @Unit("mm")
    @In public double[] maxMPS;
    
    @Description("HRU attribute maximum LPS")
    @Unit("mm")
    @In public double[] maxLPS;
    
    @Description("HRU state var actual MPS")
    @Unit("mm")
    @In public double[] actMPS;
    
    @Description("HRU state var actual LPS")
    @Unit("mm")
    @In public double[] actLPS;
    
    @Description("HRU state var actual LPS of the frist layer")
    @Unit("mm")
    @Out public double actLPS1;
    
    @Description("HRU state var actual LPS of the second layer")
    @Unit("mm")
    @Out public double actLPS2;
    
    @Description("HRU state var actual LPS of the third layer")
    @Unit("mm")
    @Out public double actLPS3;
    
    @Description("HRU state var actual LPS of the fourth layer")
    @Unit("mm")
    @Out public double actLPS4;
    
    @Description("HRU state var actual MPS of the frist layer")
    @Unit("mm")
    @Out public double actMPS1;
    
    @Description("HRU state var actual MPS of the second layer")
    @Unit("mm")
    @Out public double actMPS2;
    
    @Description("HRU state var actual MPS of the third layer")
    @Unit("mm")
    @Out public double actMPS3;
    
    @Description("HRU state var actual MPS of the fourth layer")
    @Unit("mm")
    @Out public double actMPS4;
    
    @Description("HRU soil moistrure of the frist layer")
    @Unit("%")
    @Out public double actMoist1;
    
    @Description("HRU soil moistrure of the second layer")
    @Unit("%")
    @Out public double actMoist2;
    
    @Description("HRU soil moistrure of the third layer")
    @Unit("%")
    @Out public double actMoist3;
    
    @Description("HRU soil moistrure of the fourth layer")
    @Unit("%")
    @Out public double actMoist4;
    
    @Description("HRU soil moistrure array of all layers")
    @Unit("%")
    @Out public double[] actMoist_h;
    
    double[] run_maxMPS, run_maxLPS, run_actMPS, run_actLPS,
            run_maxFPS, run_layerdepth;
    
    @Execute
    public void execute() {
        run_layerdepth =  layerdepth;
        run_maxFPS = maxFPS;
        run_maxMPS = maxMPS;
        run_maxLPS = maxLPS;
        run_actMPS = actMPS;
        run_actLPS = actLPS;
        
        int nhor = run_layerdepth.length;
        
        actMPS1 = (run_actMPS[0]/run_maxMPS[0])*100;
        actLPS1 = (run_actLPS[0]/run_maxLPS[0])*100;
        actMoist1 = (((run_actMPS[0] + run_actLPS[0] +
                run_maxFPS[0])/(run_layerdepth[0]*10*area))*100);
        
        if (nhor > 1){
            actMPS2 = (run_actMPS[1]/run_maxMPS[1])*100;
            actLPS2 = (run_actLPS[1]/run_maxLPS[1])*100;
            actMoist2 = (((run_actMPS[1] + run_actLPS[1] +
                    run_maxFPS[1])/(run_layerdepth[1]*10*area))*100);
        } else {
            actMPS2 = 0;
            actLPS2 = 0;
            actMoist2 = 0;
        }
        
        if (nhor > 2){
            actMPS3 = (run_actMPS[2]/run_maxMPS[2])*100;
            actLPS3 = (run_actLPS[2]/run_maxLPS[2])*100;
            actMoist3 = (((run_actMPS[2] + run_actLPS[2] +
                    run_maxFPS[2])/(run_layerdepth[2]*10*area))*100);
        } else {
            actMPS3 = 0;
            actLPS3 = 0;
            actMoist3 = 0;
        }
        
        if (nhor > 3){
            actMPS4 = (run_actMPS[3]/run_maxMPS[3])*100;
            actLPS4 = (run_actLPS[3]/run_maxLPS[3])*100;
            actMoist4 = (((run_actMPS[3] + run_actLPS[3] +
                    run_maxFPS[3])/(run_layerdepth[3]*10*area))*100);
        } else {
            actMPS4 = 0;
            actLPS4 = 0;
            actMoist4 = 0;
        }
        
        for (int i=0; i < nhor;i++){
            actMoist_h[i] = ((run_actMPS[i] + run_actLPS[i]
                    + run_maxFPS[i])/(run_layerdepth[i]*10*area))*100;
        }

    }
}
