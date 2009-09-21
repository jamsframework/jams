/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.io;

import jams.io.*;
import java.io.File;
import java.io.IOException;
import jams.tools.JAMSTools;
import jams.data.JAMSDocument;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;
import org.xml.sax.SAXException;
import jams.JAMS;

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
    
    public String init_withResponse(){
        try{
            String info = "";
            String modelFilename = JAMSTools.CreateAbsoluteFileName(workspaceDir.toString(),modelFile.toString());
            //check if file exists
            File file = new File(modelFilename);
            if (!file.exists()) {                
                return JAMS.resources.getString("Model_file_") + modelFilename + JAMS.resources.getString("_could_not_be_found_-_exiting!");
            }

            // do some search and replace on the input file and create new file if necessary
            String newModelFilename = XMLProcessor.modelDocConverter(modelFilename);
            if (!newModelFilename.equalsIgnoreCase(modelFilename)) {
                info = JAMS.resources.getString("The_model_definition_in_") + modelFilename + JAMS.resources.getString("_has_been_adapted_in_order_to_meet_changes_in_the_JAMS_model_specification.The_new_definition_has_been_stored_in_") + newModelFilename + JAMS.resources.getString("_while_your_original_file_was_left_untouched.");
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
                return JAMS.resources.getString("The_model_definition_file_") + modelFilename + JAMS.resources.getString("_could_not_be_loaded,_because:_") + ioe.toString();
            } catch (SAXException se) {
                return JAMS.resources.getString("The_model_definition_file_") + modelFilename + JAMS.resources.getString("_contained_errors!");                
            }                        
        }catch(Exception e){
            return JAMS.resources.getString("Can^t_load_model_file,_because_") + e.toString();            
        } 
        return null;
    }
    public void init(){
        String error = init_withResponse();
        if (error!=null)
            this.getModel().getRuntime().sendHalt(error);
    }
    
}
