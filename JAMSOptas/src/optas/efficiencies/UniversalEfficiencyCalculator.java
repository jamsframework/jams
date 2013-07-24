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
    public Attribute.String measurementAttributeName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double measurement;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description",
    defaultValue="unknown")
    public Attribute.String simulationAttributeName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double simulation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double e1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double e2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double le1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double le2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double ave;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double r2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double bias;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double e1_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double e2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double le1_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double le2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double ave_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double r2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.Double bias_normalized;

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

    ArrayList<Double> measurementList, simulationList;
    ArrayList<TimeInterval> timeIntervalList;
    EfficiencyCalculator calcE1 = new NashSutcliffe(1.0),
                         calcE2 = new NashSutcliffe(2.0),
                         calcLe1= new LogarithmicNashSutcliffe(1.0),
                         calcLe2= new LogarithmicNashSutcliffe(2.0),
                         calcAve= new VolumeError(VolumeErrorType.Absolute),
                         calcR2 = new CorrelationError(),
                         calcPBias = new VolumeError(VolumeErrorType.Relative);

    boolean firstIteration = true;
    HashSet<Long> timeStepCache = new HashSet<Long>();

    @Override
    public void init(){
        measurementList = new ArrayList<Double>();
        simulationList  = new ArrayList<Double>();

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
        if (measurement.getValue() != JAMS.getMissingDataValue()){
            measurementList.add(measurement.getValue());
            simulationList.add(simulation.getValue());
        }
    }
    @Override
    public void run(){
        if (time==null || timeInterval.getValue().equals("")){
            considerData();
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

    @Override
    public void cleanup(){
        firstIteration = false;

        double m[] = new double[measurementList.size()],
               s[] = new double[simulationList.size()];
        for (int i=0;i<measurementList.size();i++){
            m[i] = measurementList.get(i);
            s[i] = simulationList.get(i);
        }
        double value=0;

        this.e1.setValue(calcE1.calc(m, s));
        value = calcE1.calcNormative(m, s);
        if (Double.isNaN(value))
            this.e1_normalized.setValue(Double.MAX_VALUE);
        else
            this.e1_normalized.setValue(value);

        this.e2.setValue(calcE2.calc(m, s));
        value = calcE2.calcNormative(m, s);
        if (Double.isNaN(value))
            this.e2_normalized.setValue(Double.MAX_VALUE);
        else
            this.e2_normalized.setValue(value);

        this.le1.setValue(calcLe1.calc(m, s));
        value = calcLe1.calcNormative(m, s);
        if (Double.isNaN(value) || Double.isInfinite(value))
            this.le1_normalized.setValue(Double.MAX_VALUE);
        else
            this.le1_normalized.setValue(value);

        this.le2.setValue(calcLe2.calc(m, s));
        value = calcLe2.calcNormative(m, s);
        if (Double.isNaN(value) || Double.isInfinite(value))
            this.le2_normalized.setValue(Double.MAX_VALUE);
        else
            this.le2_normalized.setValue(value);

        this.ave.setValue(calcAve.calc(m, s));
        value = calcAve.calcNormative(m, s);
        if (Double.isNaN(value) || Double.isInfinite(value))
            this.ave_normalized.setValue(Double.MAX_VALUE);
        else
            this.ave_normalized.setValue(value);

        this.r2.setValue(calcR2.calc(m, s));
        value = calcR2.calcNormative(m, s);
        if (Double.isNaN(value) || Double.isInfinite(value))
            this.r2_normalized.setValue(Double.MAX_VALUE);
        else
            this.r2_normalized.setValue(value);

        this.bias.setValue(calcPBias.calc(m, s));
        value = calcPBias.calcNormative(m, s);
        if (Double.isNaN(value) || Double.isInfinite(value))
            this.bias_normalized.setValue(Double.MAX_VALUE);
        else
            this.bias_normalized.setValue(value);
        
        this.getModel().getRuntime().println("------------------------------------");
        this.getModel().getRuntime().println("*******UniversalEfficiencyCalculator");        
        this.getModel().getRuntime().println("*******Measurement:"+this.measurementAttributeName);
        this.getModel().getRuntime().println("*******Simulation :"+this.simulationAttributeName);
        this.getModel().getRuntime().println("*******Timesteps  :"+this.timeIntervalList.size());
        for (int i=0;i<Math.min(5, timeIntervalList.size());i++){
            this.getModel().getRuntime().println("*******("+i+")        :"+this.timeIntervalList.get(i));
        }
        if (timeIntervalList.size()>5){
            this.getModel().getRuntime().println("********          :...");
        }
        this.getModel().getRuntime().println("*******Method     "+"E1    "+round(this.e1.getValue())  + "  ("+ round(this.e1_normalized.getValue()) + ")");
        this.getModel().getRuntime().println("*******Method     "+"E2    "+round(this.e2.getValue())  + "  ("+round(this.e2_normalized.getValue()) + ")");
        this.getModel().getRuntime().println("*******Method     "+"le1   "+round(this.le1.getValue()) + "  ("+round(this.le1_normalized.getValue()) + ")");
        this.getModel().getRuntime().println("*******Method     "+"le2   "+round(this.le2.getValue()) + "  ("+round(this.le2_normalized.getValue()) + ")");
        this.getModel().getRuntime().println("*******Method     "+"AVE   "+round(this.ave.getValue()) + "  ("+round(this.ave_normalized.getValue()) + ")");
        this.getModel().getRuntime().println("*******Method     "+"R2    "+round(this.r2.getValue())  + "  ("+round(this.r2_normalized.getValue()) + ")");
        this.getModel().getRuntime().println("*******Method     "+"Bias  "+round(this.bias.getValue())+ "  ("+round(this.bias_normalized.getValue()) + ")");
    }
}
