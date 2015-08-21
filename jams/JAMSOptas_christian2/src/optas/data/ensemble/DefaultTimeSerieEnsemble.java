/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.ensemble;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jams.data.Attribute.TimeInterval;
import jams.data.DefaultDataFactory;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import optas.data.api.DataSetContainer;
import optas.data.api.DataView;
import optas.data.time.api.TimeFilter;
import optas.data.time.api.TimeSerie;
import optas.data.ensemble.api.TimeSerieEnsemble;
import optas.data.view.ViewFactory;
import optas.data.time.DefaultTimeSerie;
import optas.data.view.AbstractListView;

/**
 *
 * @author chris
 */
public class DefaultTimeSerieEnsemble extends DefaultEnsemble<TimeSerie<Double>> implements TimeSerieEnsemble {

    final TimeInterval timeInterval;
    final DataView<Date> dates;
    
    @Override
    public DefaultTimeSerieEnsemble clone() {
        DefaultTimeSerieEnsemble s = new DefaultTimeSerieEnsemble(getName(), getParent(), ids(), values());
        s.setParent(getParent());

        return s;
    }

    public DefaultTimeSerieEnsemble(String name, DataSetContainer parent, DataView<Integer> ids, DataView<TimeSerie<Double>> values) {
        super(name, parent, ids, values);

        Preconditions.checkArgument(values.getSize() > 0, "Can't create empty ensemble of timeseries!");
        dates = values.getValue(0).dates();

        for (TimeSerie s : values) {
            Preconditions.checkArgument(dates.equals(s.dates()),
                    "TimeSeries %s and %s are not equal",
                    values.getValue(0).getName(), s.getName());
        }
        
        //outsource .. 
        timeInterval = DefaultDataFactory.getDataFactory().createTimeInterval();
        timeInterval.getStart().setTimeInMillis(Long.MAX_VALUE);
        timeInterval.getEnd().setTimeInMillis(Long.MIN_VALUE);
        for (Date d : dates){
            if (d.before(timeInterval.getStart().getTime())){
                timeInterval.getStart().setTimeInMillis(d.getTime());
            }
            if (d.after(timeInterval.getEnd().getTime())){
                timeInterval.getEnd().setTimeInMillis(d.getTime());
            }
        }
    }

    public DefaultTimeSerieEnsemble filter(TimeFilter filter) {
        DataView<TimeSerie<Double>> filteredValues = new AbstractListView<TimeSerie<Double>, DataView<TimeSerie<Double>>>(values(), filter) {
            TimeFilter filter;
            Cache<Integer, TimeSerie> filterCache = CacheBuilder.newBuilder().maximumSize(10).softValues().build();

            {
                filter = (TimeFilter) args[0];
            }

            @Override
            public int getSize() {
                return input.getSize();
            }

            @Override
            public TimeSerie<Double> getValue(int i) {
                try {
                    return filterCache.get(i, () -> {
                        return input.getValue(i).filter(filter);
                    });
                } catch (ExecutionException exe) {
                    throw Throwables.propagate(exe);
                }
            }
        };
        return new DefaultTimeSerieEnsemble(getName(), getParent(), ids(), filteredValues);
    }

    /*private void buildTimeMapping() {
     timeMap.clear();
     Attribute.Calendar c1 = this.timeInterval.getStart().clone();
     for (int i = 0; i < value[0].length; i++) {
     c1.add(timeInterval.getTimeUnit(), timeInterval.getTimeUnitCount());
     Date d = (Date) c1.getTime().clone();
     boolean isFiltered = false;
     for (TimeFilter f : this.filter) {
     if (f.isFiltered(d)) {
     isFiltered = true;
     break;
     }
     }
     if (!isFiltered) {
     this.timeMap.add(i);
     }
     }
     }*/

    /*public DefaultSimpleEnsemble sumTS() {
     DefaultSimpleEnsemble e = new DefaultSimpleEnsemble("sum of" + name, size);
     for (int i = 0; i < this.size; i++) {
     double v = 0;
     Integer id = this.getId(i);

     for (int t = 0; t < this.getNumberOfTimesteps(); t++) {
     v += this.get(t, id);
     }

     e.set(i, id, v);
     }
     return e;
     }*/
    @Override
    public Date getDate(int time) {
        return dates.getValue(time);
    }

    @Override
    public TimeInterval getTemporalDomain() {
        return timeInterval;
    }
    
    @Override
    public int getNumberOfTimesteps() {
        return dates.getSize();
    }

    @Override
    public Double getValue(int timeIndex, Integer id) {
        return getValue(id).getValue(timeIndex);
    }

    @Override
    public DefaultEnsemble<Double> getEnsemble(int timeIndex) {
        DataView<Double> values = new AbstractListView<Double, DataView<TimeSerie<Double>>>(values(), timeIndex) {
            final int timeIndex;

            {
                timeIndex = (int) args[0];
            }

            @Override
            public int getSize() {
                return input.getSize();
            }

            @Override
            public Double getValue(int i) {
                return input.getValue(i).getValue(timeIndex);
            }
        };
        return new DefaultEnsemble<>(getName(), getParent(), ids(), values);
    }

    /*public double[] getValue(Integer id) {
     int index = getIndex(id);
     return this.value[index];
     }

     @Override
     public TimeSerie getTimeSerie(Integer id) {
     DataView<Double> d = ViewFactory.createView(getValue(id));
     TimeSerie t = new DefaultTimeSerie("row " + id + " of " + this.name, timeInterval, d);
     t.setParent(this.getParent());
     return t;
     }*/
    @Override
    public TimeSerie<Double> getMax() {
        double[] max = new double[getNumberOfTimesteps()];

        for (int t = 0; t < getNumberOfTimesteps(); t++) {
            max[t] = Double.NEGATIVE_INFINITY;
            for (int mc = 0; mc < getSize(); mc++) {
                max[t] = Math.max(this.getValue(t, this.getId(mc)), max[t]);
            }
        }
        TimeSerie t = new DefaultTimeSerie(
                String.format("maximum of %s", getName()),
                getParent(),
                dates,
                ViewFactory.createView(max)
        );
        return t;
    }

    @Override
    public TimeSerie<Double> getMin() {
        double[] max = new double[getNumberOfTimesteps()];

        for (int t = 0; t < getNumberOfTimesteps(); t++) {
            max[t] = Double.POSITIVE_INFINITY;
            for (int mc = 0; mc < getSize(); mc++) {
                max[t] = Math.min(this.getValue(t, this.getId(mc)), max[t]);
            }
        }
        TimeSerie t = new DefaultTimeSerie(
                String.format("minimum of %s", getName()),
                getParent(),
                dates,
                ViewFactory.createView(max)
        );
        return t;
    }

    @Override
    public TimeSerieEnsemble filterIds(DataView<Integer> filter) {
        return (TimeSerieEnsemble) super.filterIds(filter);
    }
    
    @Override
    public TimeSerieEnsemble removeIds(Integer ids[]) {
        return (TimeSerieEnsemble) super.removeIds(ids);
    }

    @Override
    public DefaultTimeSerieEnsemble getInstance(String name, DataSetContainer parent, DataView<Integer> ids, DataView<TimeSerie<Double>> values) {
        return new DefaultTimeSerieEnsemble(name, parent, ids, values);
    }
}
