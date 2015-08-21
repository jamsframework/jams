/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jams.data.Attribute;
import jams.data.Attribute.Entity;
import jams.data.DefaultDataFactory;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.DataStoreProcessor;
import jams.workspace.dsproc.EnsembleTimeSeriesProcessor;
import jams.workspace.dsproc.TimeSpaceProcessor;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import optas.data.DefaultEntityDataSet;
import optas.data.time.MeasuredTimeSerie;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.api.EntityDataSet;
import optas.data.api.ModelRun;
import optas.data.time.api.TemporalSpatialDataSet;
import optas.data.time.api.TimeSerie;
import optas.data.view.AbstractListView;
import optas.io.DataCollectionImporter.SpatioTemporalType;
import static optas.io.DataCollectionImporter.SpatioTemporalType.Ignore;
import static optas.io.DataCollectionImporter.SpatioTemporalType.Simulation;
import static optas.io.DataCollectionImporter.SpatioTemporalType.Unknown;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class J2KTemporalSpatialImporter extends J2KImporter<SpatioTemporalType> {

    TimeSpaceProcessor proc;
    File file;

    public J2KTemporalSpatialImporter(File file) throws IOException {
        checkNotNull(file, "File must not be null!");
        try { //this is quite ugly
            DataStoreProcessor dsp = new DataStoreProcessor(file);
            dsp.createDB();
            proc = new TimeSpaceProcessor(dsp);
        } catch (SQLException | ClassNotFoundException ex) {
            throw Throwables.propagate(ex);
        }
        this.file = file;

        setDataSetType(SpatioTemporalType.Ignore);
    }

    @Override
    public DataView<String> getDataSetNames() {
        return super.getDataSetNames(proc.getDataStoreProcessor());
    }

    private DataView<Date> createTemporalDataView() throws IOException, SQLException {
        DataView dates = new AbstractListView<Date, Attribute.Calendar[]>(proc.getTimeSteps()) {

            @Override
            public int getSize() {
                return input.length;
            }

            @Override
            public Date getValue(int i) {
                return input[i].getTime();
            }
        };
        return dates;
    }

    private DataView<Entity> createEntityDataView() throws IOException, SQLException {
        DataView<Entity> entities = new AbstractListView<Entity, Long[]>(proc.getEntityIDs()) {
            List<Entity> list = new ArrayList<Entity>();

            {
                for (Long id : input) {
                    Entity e = DefaultDataFactory.getDataFactory().createEntity();
                    e.setId(id);
                    list.add(e);
                }
            }

            @Override
            public int getSize() {
                return input.length;
            }

            @Override
            public Entity getValue(int i) {
                return list.get(i);
            }
        };
        return entities;
    }

    Cache<String, DataMatrix> tsCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .softValues() //allows to GC cached values
            .build();

    private EntityDataSet<Double> createEntityDataSet(String datasetName, Date date) throws IOException, SQLException {
        DataView<Entity> entities = createEntityDataView();

        DataView<Double> values = new AbstractListView<Double, TimeSpaceProcessor>(proc, datasetName, date) {
            final String[] date = new String[1];
            final int column;

            {
                column = getColumnOfAttribute(input.getDataStoreProcessor(), datasetName);
                date[0] = args[1].toString();
            }

            @Override
            public int getSize() {
                try {
                    return input.getEntityIDs().length;
                } catch (SQLException | IOException ex) {
                    throw Throwables.propagate(ex);
                }
            }

            @Override
            public Double getValue(int i) {
                try {
                    DataMatrix m = tsCache.get(date[0], () -> {
                        return input.getCrossProduct(
                                toPrimitive(input.getEntityIDs()), date);
                    });

                    return m.get(i, 0);
                } catch (ExecutionException ex) {
                    throw Throwables.propagate(ex);
                }
            }
        };

        return new DefaultEntityDataSet<>(date.toString(), entities, values);
    }

    Cache<Integer, EntityDataSet<Double>> entityCache = CacheBuilder.newBuilder()
            .maximumSize(20)
            .softValues() //allows to GC cached values
            .build();

    private DataView<EntityDataSet<Double>> createValueView(String datasetName) throws IOException, SQLException {
        DataView<Date> dates = createTemporalDataView();

        DataView<EntityDataSet<Double>> values = new AbstractListView<EntityDataSet<Double>, TimeSpaceProcessor>(proc, datasetName, dates) {
            final String datasetName;
            final DataView<Date> dates;

            {
                this.datasetName = (String) args[0];
                this.dates = (DataView<Date>) args[1];
            }

            @Override
            public int getSize() {
                return dates.getSize();
            }

            @Override
            public EntityDataSet<Double> getValue(int i) {
                try {
                    EntityDataSet<Double> ds = entityCache.get(i, () -> {
                        return createEntityDataSet(datasetName, dates.getValue(i));
                    });
                    return ds;
                } catch (ExecutionException ex) {
                    throw Throwables.propagate(ex);
                }
            }
        };

        return values;
    }

    private DataCollection importDataWithExceptions() throws IOException, SQLException{
        DataCollection result = DataCollection.getInstance(file.getName());

        super.selectAllAttributes(proc.getDataStoreProcessor());
        
        for (String datasetName : getDataSetNames()) {
            DataView<Date> dates = createTemporalDataView();
            DataView<EntityDataSet<Double>> values = createValueView(datasetName);

            TemporalSpatialDataSet<Double> t
                    = TemporalSpatialDataSet.getInstance(datasetName, dates, values);
            
            switch (getDataSetType(datasetName)) {
                case Ignore:
                    continue;
                case Measurement:
                    result.addGlobalDataSet(t);
                    break;
                case Simulation:
                    result.addModelRun(ModelRun.getInstance("#1", 1).addDataSet(t));
                    break;
                case Unknown:
                    result.addGlobalDataSet(t);
                    break;
            }
        }

        return result;
    }
    
    @Override
    public DataCollection importData() {
        try{
            return importDataWithExceptions();
        }catch(Exception e){
            throw Throwables.propagate(e);
        }
    }
}
