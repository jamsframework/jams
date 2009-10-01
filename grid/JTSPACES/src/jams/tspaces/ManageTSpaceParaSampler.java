/*
 * ManageTSpaceParaSampler.java
 *
 * Created on 13. Februar 2007, 12:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspace;
//import org.w3c.dom.*;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.*;

/**
 *
 * @author ncb
 */
public class ManageTSpaceParaSampler {
    
    /** Creates a new instance of ManageTSpaceParaSampler */
    public ManageTSpaceParaSampler() {
    }
   
    public void createTSpace(){
        
    }    
    public static void main(String[] arg) throws Exception {
  
   //      if (arg[0] !=null) {
//            SAXBuilder builder = new SAXBuilder();
//             FileInputStream infile = new FileInputStream("C:/geoinformatik/j2000/modeldata/TSpacesABC/abc_time.xml");
//
//       //      String modelFilename=new String("C:/geoinformatik/j2000/modeldata/TSpacesABC/abc_time.xml");
//             Document modelDoc = builder.build(infile);
//             Element docEle = modelDoc.getRootElement();
//              
//             List samplerElements = XPath.selectNodes(docEle, "//contextcomponent[@name='Sampler']");
//             System.out.println("samplerElements.size() = " + samplerElements.size()+samplerElements.toString());

 
           
       // }
      
                
//                File configFile = new File(configFilename);
//                String modelFilename = configFile.getParent() + File.separatorChar + configDoc.getDocumentElement().getAttribute("modeldefinition");
//                
//                
//                String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
//                if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
//                    rt.println("The model definition in \"" + modelFilename + "\" has been adapted in order to meet modifications of the JAMS model DTD.\nThe new definition has been stored in \"" + newModelFilename + "\" while your original file was left untouched.");
//                    modelFilename = newModelFilename;
//                }
//                rt.println("Loading model from \""+ modelFilename +"\"...", JAMS.STANDARD);
//                Document modelDoc = XMLIO.getDocument(modelFilename);
//                
//                ModelConfig config = new ModelConfig(configDoc);
//                modelDoc = (Document) modelDoc.cloneNode(true);
//                ModelPreprocessor preProc = new ModelPreprocessor(modelDoc, config, rt);
//                preProc.process();
//                
//                modelRun(properties, modelDoc, rt);
//                Runtime.getRuntime().gc();
//                    
      
    }
}
