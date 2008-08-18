/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unijena.jams.io;

import java.io.File;
import java.io.IOException;
import org.unijena.jams.JAMSTools;
import org.unijena.jams.data.JAMSDocument;
import org.unijena.jams.data.JAMSString;
import org.unijena.jams.io.XMLIO;
import org.unijena.jams.model.JAMSComponent;
import org.unijena.jams.model.JAMSVarDescription;
import org.unijena.jams.runtime.JAMSRuntime;
import org.unijena.jams.runtime.StandardRuntime;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Christian Fischer
 */
public class DocumentLoader extends JAMSComponent{
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Description"
            )
            public JAMSString modelFile;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Description"
            )
            public JAMSString workspaceDir;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE, 
            update = JAMSVarDescription.UpdateType.RUN, 
            description = "Collection of hru objects")
    public JAMSDocument modelDoc;
    
    public void init(){
        try{
            String info = "";
            String modelFilename = workspaceDir.toString() + modelFile.toString();
            //check if file exists
            File file = new File(modelFilename);
            if (!file.exists()) {
                System.out.println("Model file " + modelFilename + " could not be found - exiting!");
                return;
            }

            // do some search and replace on the input file and create new file if necessary
            String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
            if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
                info = "The model definition in \"" + modelFilename + "\" has been adapted in order to meet modifications of the JAMS model DTD.\nThe new definition has been stored in \"" + newModelFilename + "\" while your original file was left untouched.";
                modelFilename = newModelFilename;
            }

            String xmlString = JAMSTools.fileToString(modelFilename);
            String[] args = null;
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    xmlString = xmlString.replaceAll("%" + i, args[i]);
                }
            }

            try {

                modelDoc.setValue(XMLIO.getDocumentFromString(xmlString));
               
            } catch (IOException ioe) {
                System.out.println("The model definition file " + modelFilename + " could not be loaded, because: " + ioe.toString());                
            } catch (SAXException se) {
                System.out.println("The model definition file " + modelFilename + " contained errors!");
            }                        
        }catch(Exception e){
            this.getModel().getRuntime().sendHalt("Can´t load model file, because " + e.toString());
        }                
    }
    
}
