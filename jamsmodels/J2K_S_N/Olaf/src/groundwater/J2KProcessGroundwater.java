package groundwater;

import staging.j2k.types.HRU;
import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Component summary info ...")
@Author
    (name= "Peter Krause, Sven Kralisch")
@Keywords
    ("Groundwater")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/groundwater/J2KProcessGroundwater.java $")
@VersionInfo
    ("$Id: J2KProcessGroundwater.java 952 2010-02-11 19:55:02Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

public class J2KProcessGroundwater  {
    
    // @Description("area")
    double area;

    // @Description("slope")
    double slope;

    // @Description("recision coefficient k RG1")
    double kRG1;

    // @Description("recision coefficient k RG2")
    double kRG2;
    
    // @Description("actual RG1 storage")
    double actRG1;

    // @Description("actual RG2 storage")
    double actRG2;
    
    // @Description("RG1 inflow")
    double inRG1;

    // @Description("RG2 inflow")
    double inRG2;
    
    // @Description("RG1 outflow")
    double outRG1;
    
    // @Description("RG2 outflow")
    double outRG2;
    
    // @Description("percolation")
    double percolation;

    // @Description("gwExcess")
    public double gwExcess;

    // @Description("maximum soil storage")
    double maxMPS;
    
    // @Description("actual soil storage")
    double actMPS;
    
    @Description("RG1 correction factor")
    @Role(PARAMETER)
    @In public double gwRG1Fact;
    
    @Description("RG2 correction factor")
    @Role(PARAMETER)
    @In public double gwRG2Fact;
    
    @Description("RG1 RG2 distribution factor")
    @Role(PARAMETER)
    @In public double gwRG1RG2dist;
    
    @Description("capilary rise factor")
    @Role(PARAMETER)
    @In public double gwCapRise;

    @Description("relative initial RG1 storage")
    @Role(PARAMETER)
    @In public double initRG1;

    @Description("relative initial RG2 storage")
    @Role(PARAMETER)
    @In public double initRG2;

    @In @Out public HRU hru;
    
    double maxRG1;
    double maxRG2;
    double gradh;
    
    boolean init = true;
    
    private void init() {
        area = hru.area;
        slope = hru.slope;
        maxMPS = hru.maxMPS;
        
        kRG1 = hru.hgeoType.RG1_k;
        kRG2 = hru.hgeoType.RG2_k;
        
        maxRG1 = hru.hgeoType.RG1_max * area;
        maxRG2 = hru.hgeoType.RG2_max * area;
        actRG1 = maxRG1 * initRG1;
        actRG2 = maxRG2 * initRG2;

        double slope_weight = Math.tan(slope * (Math.PI / 180.0));
        gradh = ((1 - slope_weight) * gwRG1RG2dist);

        if(gradh < 0) {
            gradh = 0;
        } else if(gradh > 1) {
            gradh = 1;
        }
        
        init = false;
    }
    
    @Execute
    public void execute() {
        if (init) {
            init();
        }
        inRG1 = hru.inRG1;
        inRG2 = hru.inRG2;
        actRG1 = hru.actRG1;
        actRG2 = hru.actRG2;
        actMPS = hru.actMPS;
        gwExcess = hru.outRD2;
        percolation = hru.percolation;
        
        outRG1 = 0;
        outRG2 = 0;
        
        replenishSoilStor();
        redistRG1_RG2_in();
        distRG1_RG2();
        calcLinGWout();
        
        hru.inRG1 = inRG1;
        hru.inRG2 = inRG2;
        hru.actRG1 = actRG1;
        hru.actRG2 = actRG2;
        hru.outRG1 = outRG1;
        hru.outRG2 = outRG2;
        hru.actMPS = actMPS;
        hru.outRD2 = gwExcess;

    }
    
    private  void replenishSoilStor(){
        double deltaSoilStor = maxMPS - actMPS;
        double sat_SoilStor = 0;
        double inSoilStor = 0;
        
        if((actMPS > 0) && (maxMPS > 0)){
            sat_SoilStor = actMPS / maxMPS;
        } else {
            sat_SoilStor = 0.000001;
        }
        if(actRG2 > deltaSoilStor){
            double alpha = gwCapRise;
            inSoilStor = (deltaSoilStor) * (1. - Math.exp(-1*alpha / sat_SoilStor));
        }
        if(actRG2 >= inSoilStor){
            actMPS = actMPS + inSoilStor;
            actRG2 = actRG2 - inSoilStor;
        } else{
            actMPS = actMPS + actRG2;
            actRG2 = 0;
        }
    }
    
    private void redistRG1_RG2_in(){
        if(inRG1 > 0){
            double deltaRG1 = maxRG1 - actRG1;
            if(inRG1 <= deltaRG1){
                actRG1 = actRG1 + inRG1;
                inRG1 = 0;
            } else{
                actRG1 = maxRG1;
                outRG1 = outRG1 + inRG1 - deltaRG1;
                inRG1 = 0;
            }
        }
        if(inRG2 > 0){
            double deltaRG2 = maxRG2 - actRG2;
            if(inRG2 <= deltaRG2){
                actRG2 = actRG2 + inRG2;
                inRG2 = 0;
            } else{
                actRG2 = maxRG2;
                outRG2 = outRG2 + inRG2 - deltaRG2;
                inRG2 = 0;
            }
        }
    }
    
    private void distRG1_RG2(){
        double pot_RG1 = ((1 - gradh) * percolation);
        double pot_RG2 = (gradh * percolation);
        actRG1 = actRG1 + pot_RG1;
        actRG2 = actRG2 + pot_RG2;
        
        /** testing if inflows can be stored in groundwater storages */
        double delta_RG2 = actRG2 - maxRG2;
        if(delta_RG2 > 0){
            actRG1 = actRG1 + delta_RG2;
            actRG2 = maxRG2;
        }
        double delta_RG1 = actRG1 - maxRG1;
        if(delta_RG1 > 0){
            gwExcess = gwExcess + delta_RG1;
            actRG1 = maxRG1;
        }
//        if(delta_RG1 > 0){
//            //getModel().getRuntime().println("interflow surplus in gw: "+delta_RG1);
//        }
    }
    
    private void calcLinGWout(){
        //double k_rg1 = conc_index / RG1_k;
        double k_rg1 = 1 / (kRG1 * gwRG1Fact);
        if(k_rg1 > 1) {
            k_rg1 = 1;
        }
        double rg1_out = k_rg1 * actRG1;
        actRG1 = actRG1 - rg1_out;
        outRG1 = outRG1 + rg1_out;
        
        //double k_rg2 = conc_index / RG2_k;
        double k_rg2 = 1 / (kRG2 * gwRG2Fact);
        if(k_rg2 > 1) {
            k_rg2 = 1;
        }
        double rg2_out = k_rg2 * actRG2;
        actRG2 = actRG2 - rg2_out;
        outRG2 = outRG2 + rg2_out;
//        genRG1 = rg1_out;
//        genRG2 = rg2_out;
    }
}
