/*
 * SIDRA_Reader.java
 * Created on 15.03.2012, 16:37:02
 */

package sidra;

import jams.data.*;
import jams.model.*;
import jams.tools.JAMSTools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Sven Kralisch <sven.kralisch at uni-jena.de>
 */
@JAMSComponentDescription(
        title = "SIDRA data reader",
        author = "nsk",
        description = "This component reads timeseries of precipitation from an ASCII file",
        date = "2012-03-15",
        version = "0.1_0")
public class SIDRA_Reader extends JAMSComponent {

    @JAMSVarDescription(access = JAMSVarDescription.AccessType.READ,
    description = "File containg the data - relative from workspace input directory")
    public Attribute.String fileName;
    
    @JAMSVarDescription(access = JAMSVarDescription.AccessType.WRITE,
    description = "Precipitation read from the file")
    public Attribute.Double precip;
    
    
    private BufferedReader reader;

    public void init() {
        try {
            
            // get the file to read
            File file  = new File(getModel().getWorkspace().getInputDirectory().getPath(), fileName.getValue());
            
            // open the file
            reader = new BufferedReader(new FileReader(file));
            
            // skip header lines
            for (int i = 0; i < 16; i++) {
                reader.readLine();
            }
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
    }

    public void run() {
        
        try {
            // read next line
            String line = reader.readLine();
            
            // split line at ";"
            String[] tokens = line.split("\t");
        
            // convert second column to double
            double d = Double.parseDouble(tokens[2]);

            // set value of component attribute "precip"
            precip.setValue(d);
            
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }
        
    }

    public void cleanup() {
        try {
            // close the file
            reader.close();
        } catch (IOException ioe) {
            JAMSTools.handle(ioe);
        }        
    }
}