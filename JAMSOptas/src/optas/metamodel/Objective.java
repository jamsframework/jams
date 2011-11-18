/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.metamodel;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.JAMSDataFactory;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author chris
 */
public class Objective implements Serializable, Comparable {

    transient final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private AttributeWrapper measurement;
    private AttributeWrapper simulation;

    private String methodName;
    private ArrayList<TimeInterval> timeDomain;

    private int id;
    transient static int id_counter = 0;

    private String customName;

    @Override
    public Objective clone(){
        Objective o = new Objective();

        o.setMeasurement(this.getMeasurement());
        o.setSimulation(this.getSimulation());
        o.setMethodName(this.getMethodName().substring(0));
        for (TimeInterval t : getTimeDomain()){
            TimeInterval t2 = JAMSDataFactory.createTimeInterval();
            t2.setValue(t.getValue());
            o.getTimeDomain().add(t2);
        }
        return o;
    }

    public Objective(){
        timeDomain = new ArrayList<TimeInterval>();
        id=id_counter++;
    }
    
    public void setMethod(String method){
        this.setMethodName(method);
    }
    public String getMethod(){
        return this.getMethodName();
    }
    
    public ArrayList<TimeInterval> getTimeDomain(){
        return timeDomain;
    }

    public void addTimeDomain(TimeInterval timeDomain){
        this.getTimeDomain().add(timeDomain);
    }

    public void removeTimeDomain(TimeInterval timeDomain){
        this.getTimeDomain().remove(timeDomain);
    }
    
    /**
     * @return the measurement
     */
    public AttributeWrapper getMeasurement() {
        return measurement;
    }

    /**
     * @param measurement the measurement to set
     */
    public void setMeasurement(AttributeWrapper measurement) {
        this.measurement = measurement;
    }

    /**
     * @return the simulation
     */
    public AttributeWrapper getSimulation() {
        return simulation;
    }

    /**
     * @param simulation the simulation to set
     */
    public void setSimulation(AttributeWrapper simulation) {
        this.simulation = simulation;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        if (id>id_counter)
            id_counter = id+1;
        this.id = id;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    @Override
    public String toString(){
        if (getCustomName()!=null)
            return getCustomName();
        else
            return "("+getId()+")" + this.getMethodName();
    }

    public int compareTo(Object o){
        return this.toString().compareTo(o.toString());
    }

    /**
     * @param timeDomain the timeDomain to set
     */
    public void setTimeDomain(ArrayList<TimeInterval> timeDomain) {
        this.timeDomain = timeDomain;
    }

    /**
     * @return the customName
     */
    public String getCustomName() {
        return customName;
    }

    /**
     * @param customName the customName to set
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

}
