/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time;

import jams.data.Attribute.Calendar;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.util.ArrayList;
import java.util.Date;
import optas.data.AbstractMapDataSetDecorator;
import optas.data.DefaultMapDataSet;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.api.MapDataSet;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TimeSerie;
import optas.data.view.AbstractListView;
import optas.data.view.ViewFactory;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public class DefaultTimeSerie<T> extends AbstractMapDataSetDecorator<Date, T, MapDataSet<Date, T>> implements TimeSerie<T> {

    final protected TimeInterval temporalDomain = DefaultDataFactory.getDataFactory().createTimeInterval();;
    protected boolean isRegularTimeSerie;

    public DefaultTimeSerie(String name, DataSetContainer parent, DataView<Date> dates, DataView<T> values) {
        super(new DefaultMapDataSet<>(name, parent, dates, values));
        
        isRegularTimeSerie = false;
        calculateTemporalDomain();
    }
        
    public DefaultTimeSerie(String name, DataSetContainer parent, TimeSerie<T> dataset) {
        this(name, parent, dataset.dates(), dataset.values());

        isRegularTimeSerie = dataset.isRegularTimeSerie();
    }
        
    public DefaultTimeSerie(String name, DataSetContainer parent, TimeInterval temporalDomain, DataView<T> value) {
        this(name, parent, new RegularDateSupplier(temporalDomain), value);
        
        isRegularTimeSerie = true;
    } 
    
    public DefaultTimeSerie(TimeSerie<T> dataset, DataView<Integer> filter) {
        this(dataset.getName(), dataset.getParent(), dataset);
        this.dataset = this.dataset.filter(filter);
        this.dataset.setParent(dataset.getParent());
        isRegularTimeSerie = false;
    }
            
    static private class RegularDateSupplier extends AbstractListView<Date, TimeInterval>{

        public RegularDateSupplier(TimeInterval ti){
            super(ti);
        }
        
        @Override
        public int getSize() {
            return (int)input.getNumberOfTimesteps();
        }

        @Override
        public Date getValue(int i) {
            Calendar c = input.getStart().clone(); //do a clone here
            c.add(input.getTimeUnit(), input.getTimeUnitCount()*i);
            return c.getTime();
        }

        @Override
        public Date setValue(int i, Date value) {
           throw new UnsupportedOperationException("RegularDataSupplier cannot be changed!");
        }
    }
            
    private DataView<Integer> mapFilteredTimeSteps(TimeFilter filter) {
        ArrayList<Integer> indexMap = new ArrayList<>();
        indexMap.clear();
        for (int i = 0; i < dataset.getSize(); i++) {
            Date d = dataset.getKey(i);
            if (!filter.isFiltered(d)) {
                indexMap.add(i);
            }
        }
        return ViewFactory.createView(indexMap);
    }
    
    @Override
    public int getNumberOfTimesteps() {
        return this.getSize();
    }
    
    @Override
    public boolean isRegularTimeSerie(){
        return isRegularTimeSerie;
    }
    
    private void calculateTemporalDomain(){     
        temporalDomain.getStart().setTimeInMillis(Long.MAX_VALUE);
        temporalDomain.getEnd().setTimeInMillis(Long.MIN_VALUE);
        for (Date date : dates()){
            long timeInMillis = date.getTime();
            if (timeInMillis <= temporalDomain.getStart().getTimeInMillis()){
                temporalDomain.getStart().setTimeInMillis(timeInMillis);
            }
            if (timeInMillis >= temporalDomain.getEnd().getTimeInMillis()){
                temporalDomain.getEnd().setTimeInMillis(timeInMillis);
            }else{
                throw new IllegalArgumentException("Cannot create irregular time serie! Dates must be sorted ascendingly!");
            }
        }
    }
    
    @Override
    public TimeInterval getTemporalDomain() {
        return temporalDomain;
    }

    @Override
    public Date getTime(int index) {
        return getKey(index);
    }
    
    @Override
    public T getValue(Date time) {
        int index = this.dataset.getIndex(time);
        if (index == -1)
            return null;
        return this.dataset.getValue(index);
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + super.hashCode();
        hash = 53 * hash + (this.getTemporalDomain() != null ? this.getTemporalDomain().hashCode() : 0);
        return hash;
    }

    @Override
    public DefaultTimeSerie<T> filter(TimeFilter filter) {
        return new DefaultTimeSerie<>(this, mapFilteredTimeSteps(filter));

    }
    
    @Override
    public DefaultTimeSerie<T> filter(DataView<Integer> filter) {
        return new DefaultTimeSerie<>(this, filter);

    }
        
    @Override
    public DataView<Date> dates() {
        return keys();
    }
}
