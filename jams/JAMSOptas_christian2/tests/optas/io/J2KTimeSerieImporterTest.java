/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;


import java.io.File;
import java.io.IOException;
import optas.data.time.MeasuredTimeSerie;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.view.ArrayView;
import optas.io.DataCollectionImporter.TimeSerieType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class J2KTimeSerieImporterTest {
    
    J2KTimeSerieImporter instance = null;
    
    public J2KTimeSerieImporterTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws IOException{
         instance = new J2KTimeSerieImporter(new File(ClassLoader.getSystemClassLoader().getResource("resources/tmean.dat").getFile()));
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getDataSetNames method, of class J2KTimeSerieDCI.
     * @throws java.io.IOException
     */
    @Test
    public void testGetDataSetNames() throws IOException{
        System.out.println("getDataSetNames");

        DataView<String> expResult = new ArrayView<>(new String[]{"Artern","Erfurt","Schmücke"});
        DataView<String> result = instance.getDataSetNames();
        assertEquals(expResult, result);        
    }

    /**
     * Test of getDataSetType method, of class J2KTimeSerieDCI.
     */
    @Test
    public void testGetDataSetType() {
        System.out.println("getDataSetType");
        String name = "Artern";

        DataCollectionImporter.TimeSerieType expResult = TimeSerieType.Ignore;
        DataCollectionImporter.TimeSerieType result = instance.getDataSetType(name);
        assertEquals(expResult, result);
        
        name = "Schmücke";
        
        expResult = TimeSerieType.Ignore;
        result = instance.getDataSetType(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of setDataSetType method, of class J2KTimeSerieDCI.
     */
    @Test
    public void testSetDataSetType() {
        System.out.println("setDataSetType");
        String name = "Artern";
        DataCollectionImporter.TimeSerieType type = TimeSerieType.Measurement;
        instance.setDataSetType(name, type);
        
        DataCollectionImporter.TimeSerieType expResult = TimeSerieType.Measurement;
        DataCollectionImporter.TimeSerieType result = instance.getDataSetType(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of importData method, of class J2KTimeSerieDCI.
     */
    @Test
    public void testImportData() {
        System.out.println("importData");
        instance.setDataSetType("Artern", TimeSerieType.Measurement);
        
        DataCollection dc = instance.importData();
        assert dc.getGlobalDataset("Artern") instanceof MeasuredTimeSerie;
        
        instance.setDataSetType("Artern", TimeSerieType.Simulation);
        
        DataCollection dc2 = instance.importData();
        assert dc2.getModelRunIds().length==1;
        assert dc2.getModelRun(dc2.getModelRunIds()[0]).getDataSets().getSize()==1;
    }
    
}
