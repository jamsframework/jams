/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.efficiencies;

import jams.JAMS;
import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.StringTokenizer;
import optas.efficiencies.VolumeError.VolumeErrorType;

/**
 *
 * @author chris
 */
public class UniversalEfficiencyCalculator extends JAMSComponent{

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description",
    defaultValue="unknown")
    public Attribute.String[] measurementAttributeName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double[] measurement;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description",
    defaultValue="unknown")
    public Attribute.String[] simulationAttributeName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double[] simulation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] e1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] e2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] le1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] le2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] ave;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] r2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] bias;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] e1_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] e2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] le1_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] le2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] ave_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] r2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] bias_normalized;
    
    /*@JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double[] log_likelihood;*/

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description",
    defaultValue="")
    public Attribute.String timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Calendar time;

    static final public int RMSE = 0, NSE1=1,NSE2=2,LNSE1=3,LNSE2=4,AVE=5,R2=6,RBIAS=7;

    static public String[] availableEfficiencies = {
        "Root Mean Square Error", "Nash Sutcliffe (e1)", "Nash Sutcliffe (e2)",
        "log Nash Sutcliffe (le1)", "log Nash Sutcliffe (le2)", "Average Volume Error", "r2", "relative bias"};

    ArrayList<Double> measurementList[], simulationList[];
    ArrayList<TimeInterval> timeIntervalList;
    EfficiencyCalculator calcE1 = new NashSutcliffe(1.0),
                         calcE2 = new NashSutcliffe(2.0),
                         calcLe1= new LogarithmicNashSutcliffe(1.0),
                         calcLe2= new LogarithmicNashSutcliffe(2.0),
                         calcAve= new VolumeError(VolumeErrorType.Absolute),
                         calcR2 = new CorrelationError(),
                         calcPBias = new VolumeError(VolumeErrorType.Relative),
                         calcLogLikelihood = new LogLikelihood();

    int m = 0;
    boolean firstIteration = true;
    HashSet<Long> timeStepCache = new HashSet<Long>();

    @Override
    public void init(){
        if (measurement.length != simulation.length){
            getModel().getRuntime().sendHalt("Error: Number of measurement and simulation attributes in " + this.getInstanceName() + " does not fit!");
        }
        m = measurement.length;
        measurementList = new ArrayList[m];
        simulationList  = new ArrayList[m];
        for (int i=0;i<m;i++){
            measurementList[i] = new ArrayList<Double>();
            simulationList[i] = new ArrayList<Double>();
        }

        timeIntervalList = new ArrayList<Attribute.TimeInterval>();
        StringTokenizer tok = new StringTokenizer(timeInterval.getValue(),";");
        while(tok.hasMoreTokens()){
            String interval = tok.nextToken();
            TimeInterval t = DefaultDataFactory.getDataFactory().createTimeInterval();
            t.setValue(interval);
            timeIntervalList.add(t);
        }
        
    }

    private void considerData(){
        for (int i = 0; i < m; i++) {
            if (measurement[i].getValue() != JAMS.getMissingDataValue()) {
                measurementList[i].add(measurement[i].getValue());
                simulationList[i].add(simulation[i].getValue());
            }
        }
    }
    @Override
    public void run(){
        if (time==null || timeInterval.getValue().equals("")){
            considerData();
            return;
        }
        if (firstIteration) {
            for (TimeInterval t : timeIntervalList) {
                Calendar c = time.getValue();
                if (!c.before(t.getStart()) && !c.after(t.getEnd())) {
                    considerData();
                    timeStepCache.add(time.getTimeInMillis());
                }
            }
        }else{
            if (timeStepCache.contains(time.getTimeInMillis())){
                considerData();
            }
        }
    }

    DecimalFormat format = new DecimalFormat("######0.000");
    private String round(double r){
        if (Double.isInfinite(r) && r >0)
            return "Infinity";
        else if(Double.isNaN(r))
            return "NaN";
        else if (Double.isInfinite(r) && r <0)
            return "-Infinity";
        else
            return format.format(r);
    }

    private void setObjective(double m[], double s[], int k, Attribute.Double[] field, Attribute.Double[] normalized_field, EfficiencyCalculator calc) {
        if (field != null && field.length > k && field[k] != null) {
            field[k].setValue(calcE1.calc(m, s));
        }
        if (normalized_field != null && normalized_field.length > k && normalized_field[k] != null) {
            double value = calcE1.calcNormative(m, s);
            if (Double.isNaN(value)) {
                this.e1_normalized[k].setValue(Double.MAX_VALUE);
            } else {
                this.e1_normalized[k].setValue(value);
            }
            normalized_field[k].setValue(value);
        }
    }
    
    @Override
    public void cleanup(){
        firstIteration = false;

        this.getModel().getRuntime().println("--------------------------------------------------------");
        this.getModel().getRuntime().println("*******UniversalEfficiencyCalculator:" + this.getInstanceName());        
        this.getModel().getRuntime().println("*******Timesteps  :" + this.timeIntervalList.size());
        for (int i = 0; i < Math.min(5, timeIntervalList.size()); i++) {
                this.getModel().getRuntime().println("*******(" + i + ")        :" + this.timeIntervalList.get(i));
            }
            if (timeIntervalList.size() > 5) {
                this.getModel().getRuntime().println("********          :...");
            }
        for (int k=0;k<m;k++){
            double m[] = new double[measurementList[k].size()],
                    s[] = new double[simulationList[k].size()];
            for (int i = 0; i < measurementList[k].size(); i++) {
                m[i] = measurementList[k].get(i);
                s[i] = simulationList[k].get(i);
            }

            setObjective(m,s,k,e1,e1_normalized,calcE1);
            setObjective(m,s,k,e2,e2_normalized,calcE2);
            setObjective(m,s,k,le1,le1_normalized,calcLe1);
            setObjective(m,s,k,le2,le2_normalized,calcLe2);
            setObjective(m,s,k,ave,ave_normalized,calcAve);
            setObjective(m,s,k,r2,r2_normalized,calcR2);
            setObjective(m,s,k,bias,bias_normalized,calcPBias);
            
            //this.log_likelihood[k].setValue(calcLogLikelihood.calc(m, s));
            
            this.getModel().getRuntime().println("*******Measurement:" + this.measurementAttributeName[k]);
            this.getModel().getRuntime().println("*******Simulation :" + this.simulationAttributeName[k]);
            this.getModel().getRuntime().println("*******E1:    " + round(this.e1[k].getValue()) + "  (" + round(this.e1_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******E2:    " + round(this.e2[k].getValue()) + "  (" + round(this.e2_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******le1:   " + round(this.le1[k].getValue()) + "  (" + round(this.le1_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******le2:   " + round(this.le2[k].getValue()) + "  (" + round(this.le2_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******AVE:   " + round(this.ave[k].getValue()) + "  (" + round(this.ave_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******R2:    " + round(this.r2[k].getValue()) + "  (" + round(this.r2_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******Bias:  " + round(this.bias[k].getValue()) + "  (" + round(this.bias_normalized[k].getValue()) + ")");
        }
        this.getModel().getRuntime().println("--------------------------------------------------------");
    }
}
