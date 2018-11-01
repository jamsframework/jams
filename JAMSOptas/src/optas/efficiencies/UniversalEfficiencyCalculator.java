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
import jams.model.JAMSComponentDescription;
import jams.model.JAMSVarDescription;
import jams.model.VersionComments;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;

import java.util.StringTokenizer;
import optas.efficiencies.VolumeError.VolumeErrorType;

/**
 *
 * @author Chris, KGE added by IG.
 */
@JAMSComponentDescription(
        title = "UniversalEfficiencyCalculator",
        author = "Christian Fischer",
        description = "Calculates various efficiency measures",
        version = "1.0_1",
        date = "2018-11-01"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", date = "2018-11-01", comment = "Increased number of decimal digits in output.")
})
public class UniversalEfficiencyCalculator extends JAMSComponent{

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "names of measured attribute(s)",
    defaultValue="")
    public Attribute.String[] measurementAttributeName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "measured attribute(s)")
    public Attribute.Double[] measurement;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "names of simulated attribute(s)",
    defaultValue="")
    public Attribute.String[] simulationAttributeName;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "simulated attribute(s)")
    public Attribute.Double[] simulation;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Nash-Sutcliffe-Efficiency (power=1)",
    defaultValue = "0")
    public Attribute.Double[] e1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Nash-Sutcliffe-Efficiency (power=2)",
    defaultValue = "0")
    public Attribute.Double[] e2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: log. Nash-Sutcliffe-Efficiency (power=1)",
    defaultValue = "0")
    public Attribute.Double[] le1;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: log. Nash-Sutcliffe-Efficiency (power=2)",
    defaultValue = "0")
    public Attribute.Double[] le2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Absolute Volume Error",
    defaultValue = "0")
    public Attribute.Double[] ave;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: R²",
    defaultValue = "0")
    public Attribute.Double[] r2;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Relative Bias",
    defaultValue = "0")
    public Attribute.Double[] bias;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized e1",
    defaultValue = "0")
    public Attribute.Double[] e1_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized e2",
    defaultValue = "0")
    public Attribute.Double[] e2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized le1",
    defaultValue = "0")
    public Attribute.Double[] le1_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized le2",
    defaultValue = "0")
    public Attribute.Double[] le2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized ave",
    defaultValue = "0")
    public Attribute.Double[] ave_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized r2",
    defaultValue = "0")
    public Attribute.Double[] r2_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized bias",
    defaultValue = "0")
    public Attribute.Double[] bias_normalized;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: log. Maximumn Likelihood Efficiency",
    defaultValue = "0")
    public Attribute.Double[] log_likelihood;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized log_likelihood",
    defaultValue = "0")
    public Attribute.Double[] log_likelihood_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Kling-Gupta-Efficiency",
    defaultValue = "0")
    public Attribute.Double[] kge;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "efficiency value: Normalized kge",
    defaultValue = "0")
    public Attribute.Double[] kge_normalized;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "time interval(s) to take into account",
    defaultValue="")
    public Attribute.String timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "current model time")
    public Attribute.Calendar time;

    static final public int RMSE=0,NSE1=1,NSE2=2,LNSE1=3,LNSE2=4,AVE=5,R2=6,RBIAS=7,KGE=8;

    static public String[] availableEfficiencies = {
        "Root Mean Square Error", "Nash Sutcliffe (e1)", "Nash Sutcliffe (e2)",
        "log Nash Sutcliffe (le1)", "log Nash Sutcliffe (le2)", "Average Volume Error", "r2", "relative bias", "KGE", "normalized KGE"};

    ArrayList<Double> measurementList[], simulationList[];
    ArrayList<TimeInterval> timeIntervalList;
    EfficiencyCalculator calcE1 = new NashSutcliffe(1.0),
                         calcE2 = new NashSutcliffe(2.0),
                         calcLe1= new LogarithmicNashSutcliffe(1.0),
                         calcLe2= new LogarithmicNashSutcliffe(2.0),
                         calcAve= new VolumeError(VolumeErrorType.Absolute),
                         calcR2 = new CorrelationError(),
                         calcPBias = new VolumeError(VolumeErrorType.Relative),
                         calcLogLikelihood = new LogLikelihood(),
                         calcKGE = new KGE(); 


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

    DecimalFormat format = new DecimalFormat("######0.00000");
    private String round(double r){
        if (Double.MAX_VALUE == r){
            return "[-]";
        }
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
            if ((m.length == 0 || s.length == 0)){
                field[k].setValue(Double.NaN);
            }else{
                field[k].setValue(calc.calc(m, s));
            }
        }
        if (normalized_field != null && normalized_field.length > k && normalized_field[k] != null) {
            if ((m.length == 0 || s.length == 0)){
                normalized_field[k].setValue(Double.MAX_VALUE);
            } else {
                double value = calc.calcNormative(m, s);
                if (Double.isNaN(value)) {
                    normalized_field[k].setValue(Double.MAX_VALUE);
                } else {
                    normalized_field[k].setValue(value);
                }
            }
        }
    }
    
    @Override
    public void cleanup(){
        firstIteration = false;

        this.getModel().getRuntime().println("");
        this.getModel().getRuntime().println("************************************************************");
        this.getModel().getRuntime().println("*******UniversalEfficiencyCalculator: " + this.getInstanceName());        
        this.getModel().getRuntime().println("*******Time periods: " + this.timeIntervalList.size());
        for (int i = 0; i < Math.min(5, timeIntervalList.size()); i++) {
            this.getModel().getRuntime().println("*******(" + (i+1) + ")         : " + this.timeIntervalList.get(i));
        }
        if (timeIntervalList.size() > 5) {
            this.getModel().getRuntime().println("*******         : ...");
        }
            
            
        if (e1==null || e1.length < m) e1 = new Attribute.Double[m];
        if (e2==null || e2.length < m) e2 = new Attribute.Double[m];
        if (le1==null || le1.length < m) le1 = new Attribute.Double[m];
        if (le2==null || le2.length < m) le2 = new Attribute.Double[m];
        if (r2==null || r2.length < m) r2 = new Attribute.Double[m];
        if (ave==null || ave.length < m) ave = new Attribute.Double[m];
        if (bias==null || bias.length < m) bias = new Attribute.Double[m];
        if (e1_normalized==null || e1_normalized.length < m) e1_normalized = new Attribute.Double[m];
        if (e2_normalized==null || e2_normalized.length < m) e2_normalized = new Attribute.Double[m];
        if (le1_normalized==null || le1_normalized.length < m) le1_normalized = new Attribute.Double[m];
        if (le2_normalized==null || le2_normalized.length < m) le2_normalized = new Attribute.Double[m];
        if (r2_normalized==null || r2_normalized.length < m) r2_normalized = new Attribute.Double[m];
        if (ave_normalized==null || ave_normalized.length < m) ave_normalized = new Attribute.Double[m];
        if (bias_normalized==null || bias_normalized.length < m) bias_normalized = new Attribute.Double[m];
        if (kge==null || kge.length < m) kge = new Attribute.Double[m]; 
        if (kge_normalized==null || kge_normalized.length < m) kge_normalized = new Attribute.Double[m];

        
        for (int k=0;k<m;k++){
            double m[] = new double[measurementList[k].size()],
                    s[] = new double[simulationList[k].size()];
            for (int i = 0; i < measurementList[k].size(); i++) {
                m[i] = measurementList[k].get(i);
                s[i] = simulationList[k].get(i);
            }

            if (e1[k] == null) e1[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (e2[k] == null) e2[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (le1[k] == null) le1[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (le2[k] == null) le2[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (r2[k] == null) r2[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (ave[k] == null) ave[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (bias[k] == null) bias[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (kge[k] == null) kge[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (e1_normalized[k] == null) e1_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (e2_normalized[k] == null) e2_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (le1_normalized[k] == null) le1_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (le2_normalized[k] == null) le2_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (r2_normalized[k] == null) r2_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (ave_normalized[k] == null) ave_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (bias_normalized[k] == null) bias_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            if (kge_normalized[k] == null) kge_normalized[k] = DefaultDataFactory.getDataFactory().createDouble();
            
            setObjective(m,s,k,e1,e1_normalized,calcE1);
            setObjective(m,s,k,e2,e2_normalized,calcE2);
            setObjective(m,s,k,le1,le1_normalized,calcLe1);
            setObjective(m,s,k,le2,le2_normalized,calcLe2);
            setObjective(m,s,k,ave,ave_normalized,calcAve);
            setObjective(m,s,k,r2,r2_normalized,calcR2);
            setObjective(m,s,k,bias,bias_normalized,calcPBias);
            setObjective(m,s,k,log_likelihood, log_likelihood_normalized, calcLogLikelihood);
	    setObjective(m,s,k,kge, kge_normalized, calcKGE); 

                        
            this.getModel().getRuntime().println("************************************************************");
            this.getModel().getRuntime().println("*******Measurement: " + this.measurementAttributeName[k]);
            this.getModel().getRuntime().println("*******Simulation : " + this.simulationAttributeName[k]);            
            this.getModel().getRuntime().println("*******E1:    " + round(this.e1[k].getValue()) + "  (" + round(this.e1_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******E2:    " + round(this.e2[k].getValue()) + "  (" + round(this.e2_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******logE1: " + round(this.le1[k].getValue()) + "  (" + round(this.le1_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******logE2: " + round(this.le2[k].getValue()) + "  (" + round(this.le2_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******AVE:   " + round(this.ave[k].getValue()) + "  (" + round(this.ave_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******R2:    " + round(this.r2[k].getValue()) + "  (" + round(this.r2_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******Bias:  " + round(this.bias[k].getValue()) + "  (" + round(this.bias_normalized[k].getValue()) + ")");
            this.getModel().getRuntime().println("*******KGE:   " + round(this.kge[k].getValue()) + "  (" + round(this.kge_normalized[k].getValue()) + ")");
        }
            this.getModel().getRuntime().println("************************************************************");
    }
}
