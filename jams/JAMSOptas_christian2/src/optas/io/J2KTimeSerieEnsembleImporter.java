/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jams.data.Attribute.Calendar;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.DataStoreProcessor;
import jams.workspace.dsproc.EnsembleTimeSeriesProcessor;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import optas.data.api.DataCollection;
import optas.data.api.DataSet;
import optas.data.api.DataView;
import optas.data.api.ModelRun;
import optas.data.time.api.TimeSerie;
import optas.data.view.AbstractListView;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class J2KTimeSerieEnsembleImporter extends J2KImporter<DataCollectionImporter.TimeSerieType> {

    EnsembleTimeSeriesProcessor proc;
    File file;

    int badIDCounter = 1000000;

    Cache<Long, DataMatrix> tsCache = CacheBuilder.newBuilder()            
            .maximumSize(20)
            .softValues() //allows to GC cached values
            .build();
    
    public J2KTimeSerieEnsembleImporter(File file) throws IOException {
        checkNotNull(file, "File must not be null!");
        checkArgument(file.exists(), "%s is not existing!", file.getAbsoluteFile());
        this.file = file;
        try { //this is quite ugly
            DataStoreProcessor dsp = new DataStoreProcessor(file);
            dsp.createDB();
            proc = new EnsembleTimeSeriesProcessor(dsp);
        } catch (SQLException | ClassNotFoundException ex) {
            throw Throwables.propagate(ex);
        }

        setDataSetType(TimeSerieType.Ignore);
    }

    @Override
    public DataView<String> getDataSetNames() {
        return super.getDataSetNames(proc.getDataStoreProcessor());
    }
    
    private DataView<Double> createValueView(int id, String attrName) {
        DataView values = new AbstractListView<Double, EnsembleTimeSeriesProcessor>(proc, (Integer) id, attrName) {
            long id;
            int column;

            {
                this.id = ((Integer) args[0]);
                String attrName = (String) args[1];
                column = getColumnOfAttribute(input.getDataStoreProcessor(), attrName);
            }

            @Override
            public int getSize() {
                try {
                    return input.getTimeSteps().length;
                } catch (IOException | SQLException ex) {
                    throw Throwables.propagate(ex);
                }
            }

            @Override
            public Double getValue(int i) {
                try {
                    DataMatrix m = tsCache.get(id, () -> input.getTimeSeriesData(id));                    
                    return m.get(i, column);
                } catch (ExecutionException ex) {
                    throw Throwables.propagate(ex);
                }
            }
        };
        return values;
    }

    private DataView<Date> createTemporalDataView() throws IOException, SQLException {
        DataView<Date> dates = new AbstractListView<Date, Calendar[]>(proc.getTimeSteps()) {

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

    @Override
    public DataCollection importData() {
        String name = proc.getDataStoreProcessor().getFile().getName();
        checkNotNull(name);
        DataCollection result = DataCollection.getInstance(name);

        selectAllAttributes(proc.getDataStoreProcessor());

        int n = getDataSetNames().getSize();

        try {
            long ids[] = proc.getModelRuns();

            for (int i = 0; i < ids.length; i++) {
                int id = (int) ids[i];

                ModelRun r = ModelRun.getInstance(Integer.toString(id), id);
                for (int j = 0; j < n; j++) {
                    String datasetName = getDataSetNames().getValue(j);
                    checkNotNull(datasetName);

                    DataView<Date> dates = createTemporalDataView();
                    DataView<Double> values = createValueView(id, datasetName);

                    TimeSerie t = TimeSerie.getInstance(datasetName, dates, values);

                    switch (this.getDataSetType(datasetName)) {
                        case Ignore:
                            continue;
                        case Simulation:
                            r.addDataSet(t);
                            break;
                        case Measurement:
                            DataSet d = result.getGlobalDataset(datasetName);
                            if (d == null) {
                                result.addGlobalDataSet(t);
                            } else {
                                checkArgument(t.equals(d), "Try to add timeserie %s as measurement, "
                                        + "but id %s is different to existing timeseries.", datasetName, id);
                            }
                            break;
                        case Unknown:
                            r.addDataSet(t);
                            break;
                    }
                }
                result.addModelRun(r);
            }
        } catch (SQLException | IOException ex) {
            throw Throwables.propagate(ex);
        }                
        return result;
    }
}
