/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.hydro.calculations;

import java.util.ArrayList;
import java.util.TreeSet;
import optas.data.TimeSerie;

/**
 *
 * @author chris
 */
public class HydrographEvent implements Comparable {

    HydrographSection raisingEdge;
    Peak peak;
    HydrographSection fallingEdge;

    double width, height;
    double heightAboveGround, triangeShapeIndex;
    double quality;

    boolean isEvent;

    public HydrographEvent(HydrographSection raisingEdge, Peak peak, HydrographSection fallingEdge) {
        this.fallingEdge = fallingEdge;
        this.peak = peak;
        this.raisingEdge = raisingEdge;

        if (raisingEdge.value.isEmpty() || fallingEdge.value.isEmpty())
            isEvent = false;
        else
            isEvent = calcEventProperties();
    }

    public boolean isEvent(){
        return isEvent;
    }

    private boolean calcEventProperties(){
        int startIndex = this.raisingEdge.startIndex;
        int endIndex   = this.fallingEdge.endIndex;

        if (endIndex - startIndex <= 2)
            return false;

        double lbx = startIndex;
        double lby = this.raisingEdge.at(startIndex);
        double rbx = this.fallingEdge.endIndex-1;
        double rby = this.fallingEdge.at(endIndex-1);
        double tx  = this.peak.index;
        double ty  = this.peak.value;
        //construct triangle function
        double q1 = 0;
        for (int i = startIndex; i<endIndex;i++){
            //scheint nicht zu funktionieren (im stundenmodus .. )
            double y = triangleFunction(lbx, lby, rbx, rby, tx, ty, i);
            double ystar = 0;
            if (i<peak.index)
                ystar = raisingEdge.at(i);
            else if (i > peak.index)
                ystar = fallingEdge.at(i);

            if (Double.isNaN(y) || Double.isInfinite(y))
                continue;
            
            if (ystar != 0.0)
                q1 += Math.abs(y - ystar) / ystar;
        }
        q1 /= (rbx - lbx);

        //check some properties ..

        //difference between triangle and hydrograph larger then 100%
        /*if (q1 > 1.0)
            return false;*/


        double q2 = ty / (0.5*(lby+rby));

        //difference between peak and baseflow lower then 30%
        if (q2 < 1.3){
            return false;
        }

        this.width = endIndex - startIndex;
        this.height = peak.value;
        this.heightAboveGround = q2;
        this.triangeShapeIndex = (1.0 - q1);

        this.quality = height*(heightAboveGround)*Math.max(Math.min(Math.exp(triangeShapeIndex),0.33),3);

        return true;
    }

    public double getQuality(){
        return quality;
    }

    public HydrographSection getRaisingEdge(){
        return this.raisingEdge;
    }
    public HydrographSection getFallingEdge(){
        return this.fallingEdge;
    }
    public Peak getPeak(){
        return this.peak;
    }

    public int compareTo(Object obj) {
        if (obj instanceof HydrographEvent) {
            HydrographEvent p2 = (HydrographEvent) obj;

            double q1 = this.getQuality();
            double q2 = p2.getQuality();

            if (q1 < q2) {
                return 1;
            } else if (q1 > q2) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }
    //lb -- left base
    //rb -- right base
    //t -- top

    //linearer anstieg .. exp abfall
    private static double triangleFunction(double lbx, double lby, double rbx, double rby, double tx, double ty, double x){
        if (x < lbx)
            return 0.0;
        if (x > rbx)
            return 0.0;
        if (lbx > rbx || lbx > tx || tx > rbx)
            return 0.0;

        if (x < tx){            
            double w1 = x - lbx;
            double w2 = tx - x;
            double w = w1+w2;

            return (w2*lby + w1*ty) / w;
        }else{ //x >= tx            
            double w1 = rbx - tx;
            double w2 = Math.exp(x - tx);
            double w = w1+w2;

            return (w2*rby + w1*ty) / w;
        }
    }

    private static HydrographEvent isEvent(int evtStart, int evtEnd, TimeSerie hydrograph){
        if (evtEnd - evtStart < 2){
            return null;
        }
        int peak = 0;
        double peakValue = 0;
        for (int i = evtStart; i<evtEnd;i++){
            if (hydrograph.getValue(i)>peakValue){
                peakValue = hydrograph.getValue(i);
                peak = i;
            }
        }
        HydrographEvent event = new HydrographEvent(new HydrographSection(evtStart, peak, hydrograph ), new Peak(peak, peakValue), new HydrographSection(peak, evtEnd, hydrograph ));
        if (event.isEvent())
            return event;
        return null;
    }

    public static ArrayList<HydrographEvent> findEvents(TimeSerie hydrograph, int windowSize) {
        TreeSet<HydrographEvent> eventList = new TreeSet<HydrographEvent>();

        long n = hydrograph.getTimeDomain().getNumberOfTimesteps();

        double baseFlow[] = BaseFlow.groundwaterWindowMethod(hydrograph, windowSize);

        int eventStart = 0;        
        int eventEnd = 0;

        for (int i = 0; i < (int) n; i++) {
            double vF = hydrograph.getValue(i);
            double vB = baseFlow[i];

            if ( Math.abs( 1.0 - (vF / vB) ) < 0.05  ){
                eventEnd = i;
                HydrographEvent e = isEvent(eventStart, eventEnd, hydrograph);
                if (e!=null)
                    eventList.add(e);
                eventStart = i;
            }
        }

        return new ArrayList<HydrographEvent>(eventList);
    }
}
