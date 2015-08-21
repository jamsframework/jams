/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.api.EntityDataSet;
import optas.data.time.api.TemporalSpatialDataSet;
import optas.data.view.ArrayView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class J2KTemporalSpatialImporterTest {
    static J2KTemporalSpatialImporter instance;
    
    public J2KTemporalSpatialImporterTest() {
    }
    
    @Before
    public void setUp() throws IOException{
        instance = new J2KTemporalSpatialImporter(new File(ClassLoader.getSystemClassLoader().getResource("resources/temporalspatial.dat").getFile()));
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
        DataView<String> expResult = new ArrayView<>(new String[]{"value"});
        DataView<String> result = instance.getDataSetNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of importData method, of class J2KTimeSerieEnsembleImporter.
     */
    @Test
    public void testImportData() throws ParseException{
        System.out.println("importData");
        instance.setDataSetType("value", DataCollectionImporter.SpatioTemporalType.Measurement);
        DataCollection result = instance.importData();
        
        assert(result.getGlobalDataset("value") instanceof TemporalSpatialDataSet);
        
        TemporalSpatialDataSet<Double> ti = (TemporalSpatialDataSet)result.getGlobalDataset("value");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date d = sdf.parse("1963-01-01 00:00");
        EntityDataSet<Double> eds = ti.getValue(d);
        assert(eds != null);
        
        assert(Math.abs(eds.getValue(2) - 4.74400) < 0.001);
    }
    
}
