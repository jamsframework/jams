package abc;

import java.io.*;
import java.util.StringTokenizer;
import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="ABCModel precip reader",
        author="Kralisch, S.",
        description="ABC model climate data reader"
        )
public class ClimateReader extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "Input data file name"
            )
            public JAMSString fileName;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Precip value read from file"
            )
            public JAMSDouble precip;
    
    private BufferedReader reader;
    
    public void init(){
        getModel().getRuntime().println("INIT Climate");
        try {
            reader = new BufferedReader(new FileReader(this.fileName.getValue()));
            reader.readLine();
            reader.readLine();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void run(){
        
        String line, token;
        try {
            
            line = reader.readLine();
            StringTokenizer st = new StringTokenizer(line);
            token = st.nextToken();
            
            token = st.nextToken();
            precip.setValue(Double.parseDouble(token));
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void cleanup(){
        getModel().getRuntime().println("CLEANUP Climate");
        try {
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
}
