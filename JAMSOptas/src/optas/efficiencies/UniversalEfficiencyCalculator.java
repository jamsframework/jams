/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.efficiencies;

import jams.data.Attribute;
import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import java.util.ArrayList;

import java.util.StringTokenizer;

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

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.INIT,
    description = "file name of optimization process description")
    public Attribute.String method;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double result;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Double normalizedResult;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description",
    defaultValue="")
    public Attribute.String timeInterval;

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    update = JAMSVarDescription.UpdateType.RUN,
    description = "file name of optimization process description")
    public Attribute.Calendar time;

    ArrayList<Double> measurementList, simulationList;
    ArrayList<TimeInterval> timeIntervalList;
    EfficiencyCalculator calculator;

    @Override
    public void init(){
        measurementList = new ArrayList<Double>();
        simulationList  = new ArrayList<Double>();

        timeIntervalList = new ArrayList<Attribute.TimeInterval>();
        StringTokenizer tok = new StringTokenizer(timeInterval.getValue(),";");
        while(tok.hasMoreTokens()){
            String interval = tok.nextToken();
            TimeInterval t = JAMSDataFactory.createTimeInterval();
            t.setValue(interval);
            timeIntervalList.add(t);
        }
        Class clazz = null;
        try{
            clazz = getModel().getRuntime().getClassLoader().loadClass(this.method.getValue());
        }catch(ClassNotFoundException cnfe){
            cnfe.printStackTrace();
            getModel().getRuntime().sendHalt("could not find class " + cnfe);            
        }
        Object o = null;
        try{
            o = clazz.newInstance();
        }catch(InstantiationException ie){
            ie.printStackTrace();
            getModel().getRuntime().sendHalt("could not instantiate class " + ie);
        }catch(IllegalAccessException iae){
            iae.printStackTrace();
            getModel().getRuntime().sendHalt("could not instantiate class " + iae);
        }
        if (o instanceof EfficiencyCalculator){
            calculator = (EfficiencyCalculator)o;
        }else{
            getModel().getRuntime().sendHalt("Efficiency-Class not of type EfficiencyCalculator");
        }
    }

    private void considerData(){
        measurementList.add(measurement.getValue());
        simulationList.add(simulation.getValue());
    }
    @Override
    public void run(){
        if (time==null || timeInterval.getValue().equals("")){
            considerData();
        }

        for (TimeInterval t : timeIntervalList){
            Calendar c = time.getValue();
            if (c.after(t.getStart()) && c.before(t.getEnd())){
                considerData();
            }else if(c.equals(t.getStart()) || c.equals(t.getEnd())){
                considerData();
            }
        }
    }

    @Override
    public void cleanup(){
        double m[] = new double[measurementList.size()],
               s[] = new double[simulationList.size()];
        for (int i=0;i<measurementList.size();i++){
            m[i] = measurementList.get(i);
            s[i] = simulationList.get(i);
        }

        this.result.setValue(calculator.calc(m,s));
        this.normalizedResult.setValue(calculator.calcNormative(m, s));

        this.getModel().getRuntime().println("------------------------------------");
        this.getModel().getRuntime().println("*******UniversalEfficiencyCalculator");
        this.getModel().getRuntime().println("*******Method     :"+this.method);
        this.getModel().getRuntime().println("*******Measurement:"+this.measurementAttributeName);
        this.getModel().getRuntime().println("*******Simulation :"+this.simulationAttributeName);
        this.getModel().getRuntime().println("*******Timesteps  :"+this.timeIntervalList.size());
        for (int i=0;i<Math.min(5, timeIntervalList.size());i++){
            this.getModel().getRuntime().println("*******("+i+")        :"+this.timeIntervalList.get(i));
        }
        if (timeIntervalList.size()>5){
            this.getModel().getRuntime().println("********          :...");
        }
        this.getModel().getRuntime().println("*******Result     :"+this.result.getValue());
    }
}
