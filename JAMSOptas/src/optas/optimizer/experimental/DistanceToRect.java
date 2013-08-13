/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.optimizer.experimental;

import jams.JAMS;
import jams.data.Attribute;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;
import java.util.StringTokenizer;
import optas.optimizer.management.Tools;
import optas.optimizer.management.Tools.Rectangle;

/**
 *
 * @author chris
 */
public class DistanceToRect extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String regionsLowBounds;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String regionsUpBounds;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String boundaries;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double[] x;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double d;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double d_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double threshold;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double y;
    
    Tools.Rectangle rects[];
    int n = -1;

    double lowBound[]=null,upBound[]=null;

    public void init() {

        int counter = 0;
        StringTokenizer tokStartValueLB = new StringTokenizer(regionsLowBounds.getValue(), ";");
        StringTokenizer tokStartValueUB = new StringTokenizer(regionsUpBounds.getValue(), ";");
        ArrayList<double[]> lowBoundList = new ArrayList<double[]>();
        ArrayList<double[]> upBoundList = new ArrayList<double[]>();

        while (tokStartValueLB.hasMoreTokens()) {
            String paramLB = tokStartValueLB.nextToken();
            String paramUB = tokStartValueUB.nextToken();
            paramLB = paramLB.replace("[", "");
            paramLB = paramLB.replace("]", "");
            paramUB = paramUB.replace("[", "");
            paramUB = paramUB.replace("]", "");
            StringTokenizer subTokenizerLB = new StringTokenizer(paramLB, ",");
            StringTokenizer subTokenizerUB = new StringTokenizer(paramUB, ",");
            double LBi[] = new double[subTokenizerLB.countTokens()];
            double UBi[] = new double[subTokenizerUB.countTokens()];
            if (n != -1) {
                if (LBi.length != n ||UBi.length != n ) {
                    getModel().getRuntime().sendHalt("error different lengths .. ");
                    return;
                }
            } else {
                n = subTokenizerLB.countTokens();
            }

            int subCounter = 0;
            while (subTokenizerLB.hasMoreTokens()) {
                try {
                    LBi[subCounter] = Double.valueOf(subTokenizerLB.nextToken()).doubleValue();
                    UBi[subCounter] = Double.valueOf(subTokenizerUB.nextToken()).doubleValue();
                    subCounter++;
                } catch (NumberFormatException e) {
                    this.getModel().getRuntime().sendHalt(JAMS.i18n("Component") + " " + this.getInstanceName() + ": " + JAMS.i18n("unparseable_number"));
                }
            }
            lowBoundList.add(LBi);
            upBoundList.add(UBi);
            counter++;


            rects = new Tools.Rectangle[lowBoundList.size()];
            for (int j = 0; j < lowBoundList.size(); j++) {
                rects[j] = new Rectangle();
                rects[j].lb = new double[n];
                rects[j].ub = new double[n];

                for (int k = 0; k < n; k++) {
                    rects[j].lb[k] = lowBoundList.get(j)[k];
                    rects[j].ub[k] = upBoundList.get(j)[k];
                }
            }
        }
        lowBound = new double[n];
        upBound = new double[n];
        
        StringTokenizer tok = new StringTokenizer(this.boundaries.getValue(), ";");
        int i = 0;
        while (tok.hasMoreTokens()) {
            if (i >= n) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("too_many_boundaries"));
                return;
            }
            String key = tok.nextToken();
            key = key.substring(1, key.length() - 1);

            StringTokenizer boundTok = new StringTokenizer(key, ">");
            try {
                lowBound[i] = Double.parseDouble(boundTok.nextToken());
                upBound[i] = Double.parseDouble(boundTok.nextToken());
            } catch (NumberFormatException e) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("unsupported_number_format_found_for_lower_or_upper_bound"));
                return;
            }
            //check if upBound is higher than lowBound
            if (upBound[i] <= lowBound[i]) {
                this.getModel().getRuntime().sendHalt(JAMS.i18n("Component") + " " + this.getInstanceName() + ": " + JAMS.i18n("upBound_must_be_higher_than_lowBound"));
                return;
            }

            i++;
        }
    }

   
    @Override
    public void run() {
        double d = Double.POSITIVE_INFINITY;

        double point[] = new double[n];
        for (int i=0;i<n;i++){
            point[i] = this.x[i].getValue();
        }
        for (Rectangle r : rects){
            d = Math.min(d,Tools.dist(Tools.clamp(r, point),point,this.lowBound,this.upBound));
        }
        if (threshold.getValue() < y.getValue()){
            d = d*Math.exp(threshold.getValue()/y.getValue())/Math.exp(1);
        }
        this.d.setValue(d);
        this.d_normalized.setValue(-d);

    }
}
