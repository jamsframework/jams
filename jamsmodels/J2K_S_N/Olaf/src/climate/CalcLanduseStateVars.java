package climate;

import oms3.annotations.*;
import static oms3.annotations.Role.*;

@Description
    ("Calculates land use state variables")
@Author
    (name= "Peter Krause, Sven Kralisch, Olaf David")
@Keywords
    ("I/O")
@SourceInfo
    ("$HeadURL: http://svn.javaforge.com/svn/oms/branches/oms3.prj.ceap/src/climate/CalcLanduseStateVars.java $")
@VersionInfo
    ("$Id: CalcLanduseStateVars.java 961 2010-02-11 20:35:32Z odavid $")
@License
    ("http://www.gnu.org/licenses/gpl-2.0.html")

 public class CalcLanduseStateVars  {
    
    @Description("HRU attribute name elevation")
    @In public double elevation;

    @Description("LAI")
    @In public double[] LAI;

    @Description("effHeight")
    @In public double[] effHeight;
    
    @Description("HRU statevar LAI values (366)")
    @Out public double[] LAIArray;
    
    @Description("HRU statevar eff. Height values (366)")
    @Out public double[] effHArray;
    
    @Execute
    public void execute() {
        LAIArray = new double[366];
        effHArray = new double[366];
        for(int i = 0; i < 366; i++){
            LAIArray[i] =  calcLAI(LAI, elevation, i+1);
            effHArray[i] =  calcEffHeight(effHeight, elevation, i+1);
        }
    }
    
    /**
     * Calculates LAI for the specific date
     * @param lais - the four LAI values at specific dates
     * @param targetElevation - the elevation of the modelling unit
     * @param julDay - the julian day
     * @return the LAI value
     */
    private double calcLAI(double[] lais, double targetElevation, int julDay){
        int dTime = 0;
        double Lait1 = 0;
        double dLai = 0;
        int d1_400 = 110;
        int d2_400 = 150;
        int d3_400 = 250;
        int d4_400 = 280;
        
        //---------------------------------------
        // Calculation of Julian date of the specific points of LAI and eff. Height change
        //---------------------------------------
        int d1 = (int)(d1_400 + 0.025 * (targetElevation - 400));
        int d2 = (int)(d2_400 + 0.025 * (targetElevation - 400));
        int d3 = (int)(d3_400 - 0.025 * (targetElevation - 400));
        int d4 = (int)(d4_400 - 0.025 * (targetElevation - 400));
        
        double LAI = 0;
        
        if(julDay <= d1){
            LAI = lais[0];
        } else if((julDay > d1) && (julDay <= d2)){
            double LAI_1 = lais[0];
            double LAI_2 = lais[1];
            dTime = d2 - d1;
            dLai  = LAI_2 - LAI_1;
            Lait1 = dLai / dTime;
            LAI  = (Lait1 * (julDay - d1) + LAI_1);
        } else if(julDay > d2 && julDay <= d3){
            double LAI_2 = lais[1];
            double LAI_3 = lais[2];
            dTime = d3 - d2;
            dLai  = LAI_3 - LAI_2;
            Lait1 = dLai / dTime;
            LAI  = (Lait1 * (julDay - d2) + LAI_2);
        } else if(julDay > d3 && julDay <= d4){
            double LAI_3 = lais[2];
            double LAI_4 = lais[3];
            dTime = d4 - d3;
            dLai  = LAI_4 - LAI_3;
            Lait1 = dLai / dTime;
            LAI  = (Lait1 * (julDay - d3) + LAI_3);
        } else if(julDay > d4){
            double LAI_4 = lais[3];
            LAI  = LAI_4;
        }
        return LAI;
    }
    
    /**
     * Calculates effective Height for the specific date
     * @param effHeight - the four effective height values at specific dates
     * @param targetElevation - the elevation of the modelling unit
     * @param julDay - the julian day
     * @return the effHeight value
     */
    private double calcEffHeight(double[] effHeight, double targetElevation, int julDay) {
        int dTime = 0;
        double effH_t1 = 0;
        double deffH = 0;
        int d1_400 = 110;
        int d2_400 = 150;
        int d3_400 = 250;
        int d4_400 = 280;
        
        //---------------------------------------
        // Calculation of Julian date of the specific points of LAI and eff. Height change
        //---------------------------------------
        int d1 = (int)(d1_400 + 0.025 * (targetElevation - 400));
        int d2 = (int)(d2_400 + 0.025 * (targetElevation - 400));
        int d3 = (int)(d3_400 - 0.025 * (targetElevation - 400));
        int d4 = (int)(d4_400 - 0.025 * (targetElevation - 400));
        
        double effH = 0;
        
        if(julDay <= d1){
            effH = effHeight[0];
        } else if((julDay > d1) && (julDay <= d2)){
            double effH_1 = effHeight[0];
            double effH_2 = effHeight[1];
            dTime = d2 - d1;
            deffH  = effH_2 - effH_1;
            effH_t1 = deffH / dTime;
            effH  = (effH_t1 * (julDay - d1) + effH_1);
        } else if(julDay > d2 && julDay <= d3){
            double effH_2 = effHeight[1];
            double effH_3 = effHeight[2];
            dTime = d3 - d2;
            deffH  = effH_3 - effH_2;
            effH_t1 = deffH / dTime;
            effH  = (effH_t1 * (julDay - d2) + effH_2);
        } else if(julDay > d3 && julDay <= d4){
            double effH_3 = effHeight[2];
            double effH_4 = effHeight[3];
            dTime = d4 - d3;
            deffH  = effH_4 - effH_3;
            effH_t1 = deffH / dTime;
            effH  = (effH_t1 * (julDay - d3) + effH_3);
        } else if(julDay > d4){
            double effH_4 = effHeight[3];
            effH  = effH_4;
        }
        return effH;
    }
}
