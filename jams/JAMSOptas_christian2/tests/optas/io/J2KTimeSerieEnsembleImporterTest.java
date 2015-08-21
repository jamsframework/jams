/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import java.io.File;
import java.io.IOException;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.time.api.TimeSerie;
import optas.data.ensemble.api.TimeSerieEnsemble;
import optas.data.view.ArrayView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class J2KTimeSerieEnsembleImporterTest {
    static J2KTimeSerieEnsembleImporter instance;
    
    public J2KTimeSerieEnsembleImporterTest() {
    }
    
    @Before
    public void setUp() throws IOException{
        instance = new J2KTimeSerieEnsembleImporter(new File(ClassLoader.getSystemClassLoader().getResource("resources/timeseries.dat").getFile()));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getDataSetNames method, of class J2KTimeSerieEnsembleImporter.
     */
    @Test
    public void testGetDataSetNames() {
        System.out.println("getDataSetNames");
        DataView<String> expResult = new ArrayView<>(new String[]{"catchmentSimRunoff_qm"});
        DataView<String> result = instance.getDataSetNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of importData method, of class J2KTimeSerieEnsembleImporter.
     */
    @Test
    public void testImportData() {
        System.out.println("importData");
        instance.setDataSetType("catchmentSimRunoff_qm", DataCollectionImporter.TimeSerieType.Simulation);
        DataCollection result = instance.importData();
        
        assert(result.getModelRunIds().length == 101);
        assert(result.getModelRun(0).getDataSet("catchmentSimRunoff_qm") instanceof TimeSerie);
        
        TimeSerieEnsemble ti = result.getTimeserieEnsemble("catchmentSimRunoff_qm");
        assert(Math.abs(ti.get(2, 2) - 0.051555085927248) < 0.0001);
    }
    
}
