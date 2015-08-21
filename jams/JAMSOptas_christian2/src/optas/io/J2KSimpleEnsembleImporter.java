/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import static com.google.common.base.Preconditions.checkNotNull;
import jams.workspace.dsproc.DataMatrix;
import jams.workspace.dsproc.SimpleSerieProcessor;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import optas.data.NegativeEfficiency;
import optas.data.Parameter;
import optas.data.PositiveEfficiency;
import optas.data.SimpleDataSet;
import optas.data.StateVariable;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.api.ModelRun;
import optas.io.DataCollectionImporter.SimpleValueType;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 */
public class J2KSimpleEnsembleImporter extends J2KImporter<SimpleValueType> {

    SimpleSerieProcessor proc;
    File file;

    int badIDCounter = 1000000;

    public J2KSimpleEnsembleImporter(File file) throws IOException {
        checkNotNull(file, "File must not be null!");
        this.file = file;
        
        proc = new SimpleSerieProcessor(file);

        setDataSetType(SimpleValueType.Ignore);
    }

    @Override
    public DataView<String> getDataSetNames() {
        return super.getDataSetNames(proc.getDataStoreProcessor());
    }
   
    private int getIDFromString(String id) {
        int intID = 0;
        try {
            intID = Integer.parseInt(id);
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            //fallback (there should be a list of all used ids)
            intID = new Integer(badIDCounter++);
        }
        return intID;
    }

    @Override
    public DataCollection importData() {
        //force to get all attributes!
        String name = proc.getDataStoreProcessor().getFile().getName();
        DataCollection collection = DataCollection.getInstance(name);

        super.selectAllAttributes(proc.getDataStoreProcessor());

        int n = getDataSetNames().getSize();

        try {
            String ids[] = proc.getIDs(); //Modelrun
            DataMatrix result = proc.getData(ids);

            for (int i = 0; i < ids.length; i++) {
                String id = ids[i];
                ModelRun r = ModelRun.getInstance(id, getIDFromString(id));
                for (int j = 0; j < n; j++) {
                    String datasetName = getDataSetNames().getValue(j);
                    double value = result.get(i, j);
                    SimpleDataSet dataset = new SimpleDataSet(datasetName, value);

                    switch (this.getDataSetType(datasetName)) {
                        case Ignore:
                            continue;
                        case NegEfficiency:
                            r.addDataSet(new NegativeEfficiency(dataset));
                            break;
                        case PosEfficiency:
                            r.addDataSet(new PositiveEfficiency(dataset));
                            break;
                        case Parameter:
                            r.addDataSet(new Parameter(dataset));
                            break;
                        case StateVariable:
                            r.addDataSet(new StateVariable(dataset));
                            break;
                        case Unknown:
                            r.addDataSet(new StateVariable(dataset));
                            break;
                    }
                }
                collection.addModelRun(r);
            }

            proc.close();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

        return collection;
    }
}
