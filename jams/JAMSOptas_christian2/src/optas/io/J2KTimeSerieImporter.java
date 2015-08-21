/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.File;
import java.io.IOException;
import optas.data.time.MeasuredTimeSerie;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.api.ModelRun;
import optas.data.time.api.TimeSerie;
import optas.data.view.ViewFactory;
import optas.io.DataCollectionImporter.TimeSerieType;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class J2KTimeSerieImporter extends J2KImporter<TimeSerieType> {

    J2KTimeSerieReader reader;
    File file;

    public J2KTimeSerieImporter(File file) throws IOException {
        checkNotNull(file, "File must not be null!");
        reader = new J2KTimeSerieReader(file);
        this.file = file;
        
        setDataSetType(TimeSerieType.Ignore);
    }

    @Override
    public DataView<String> getDataSetNames() {
        return ViewFactory.createView(reader.getHeader().getAttributeNames());
    }

    @Override
    public DataCollection importData() {
        DataCollection result = DataCollection.getInstance(file.getName());
        
        J2KTimeSerie timeserie = reader.getData();
        checkNotNull(timeserie);
        
        
        for (int column = 0; column < timeserie.getTimeserieCount(); column++) {
            TimeSerie<Double> t = timeserie.getColumn(column);
            String name = timeserie.getAttributeNames()[column];

            switch (getDataSetType(name)) {
                case Ignore:
                    continue;
                case Measurement:
                    result.addGlobalDataSet(MeasuredTimeSerie.getInstance(t));
                    break;
                case Simulation:
                    result.addModelRun(ModelRun.getInstance("#1", 1).addDataSet(t));
                    break;
                case Unknown:
                    result.addGlobalDataSet(MeasuredTimeSerie.getInstance(t));
                    break;
            }
        }

        return result;
    }
}
