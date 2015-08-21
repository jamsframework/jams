/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

import java.io.File;
import java.io.IOException;
import optas.data.NegativeEfficiency;
import optas.data.api.DataCollection;
import optas.data.api.DataView;
import optas.data.view.ArrayView;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Christian Fischer <christian.fischer.2@uni-jena.de>
 */
public class J2KSimpleEnsembleImporterTest {
    static J2KSimpleEnsembleImporter instance;
    
    public J2KSimpleEnsembleImporterTest() {
    }
    
    @Before
    public void setUp() throws IOException{
         instance = new J2KSimpleEnsembleImporter(new File(ClassLoader.getSystemClassLoader().getResource("resources/parameter.dat").getFile()));
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

        DataView<String> expResult = new ArrayView<>(new String[]{"ACAdaptation", "bias_normalized", "ccf_factor", "e1_normalized"});
        DataView<String> result = instance.getDataSetNames();
        assertEquals(expResult, result);        
    }

    /**
     * Test of getDataSetType method, of class J2KTimeSerieDCI.
     */
    @Test
    public void testGetDataSetType() {
        System.out.println("getDataSetType");
        String name = "ACAdaptation";

        DataCollectionImporter.SimpleValueType expResult = DataCollectionImporter.SimpleValueType.Ignore;
        DataCollectionImporter.SimpleValueType result = instance.getDataSetType(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of setDataSetType method, of class J2KTimeSerieDCI.
     */
    @Test
    public void testSetDataSetType() {
        System.out.println("setDataSetType");
        for (String name : instance.getDataSetNames()){
            DataCollectionImporter.SimpleValueType type = null;
            
            switch (name){
                case "bias_normalized": type = DataCollectionImporter.SimpleValueType.NegEfficiency; break;                    
                case "e1_normalized": type = DataCollectionImporter.SimpleValueType.NegEfficiency; break;
                case "ACAdaptation": type = DataCollectionImporter.SimpleValueType.Parameter; break;                    
                case "ccf_factor": type = DataCollectionImporter.SimpleValueType.Parameter; break;                    
            }
            instance.setDataSetType(name, type);
        }
                
        assertEquals(instance.getDataSetType("bias_normalized"), DataCollectionImporter.SimpleValueType.NegEfficiency );
        assertEquals(instance.getDataSetType("e1_normalized"), DataCollectionImporter.SimpleValueType.NegEfficiency );
        assertEquals(instance.getDataSetType("ACAdaptation"), DataCollectionImporter.SimpleValueType.Parameter );
        assertEquals(instance.getDataSetType("ccf_factor"), DataCollectionImporter.SimpleValueType.Parameter );
    }

    /**
     * Test of importData method, of class J2KTimeSerieDCI.
     */
    @Test
    public void testImportData() {
        System.out.println("importData");
        
        for (String name : instance.getDataSetNames()){
            DataCollectionImporter.SimpleValueType type = null;
            
            switch (name){
                case "bias_normalized": type = DataCollectionImporter.SimpleValueType.NegEfficiency; break;                    
                case "e1_normalized": type = DataCollectionImporter.SimpleValueType.NegEfficiency; break;
                case "ACAdaptation": type = DataCollectionImporter.SimpleValueType.Parameter; break;                    
                case "ccf_factor": type = DataCollectionImporter.SimpleValueType.Parameter; break;                    
            }
            instance.setDataSetType(name, type);
        }
                        
        DataCollection dc = instance.importData();
        assert dc.getModelRunIds().length == 100;
        assert dc.getDatasetClass("bias_normalized") == NegativeEfficiency.class;
        assert dc.getDataSetTypes().size() == 2;
        assert dc.getDataSetNames(NegativeEfficiency.class).size() == 2;       
    }
    
}
