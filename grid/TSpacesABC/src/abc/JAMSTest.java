package abc;

import org.unijena.jams.data.*;
import org.unijena.jams.model.*;

/**
 *
 * @author S. Kralisch
 */
@JAMSComponentDescription(
        title="Test Component",
        author="S. Kralisch",
        description="This is only a test component"
        )
public class JAMSTest extends JAMSComponent {
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "A String to be printed"
            )
            public JAMSString ausgabe;
    
    /*
     *  Component run stages
     */
    
    public void init() {
        getModel().getRuntime().println("INIT");
    }
    
    public void run() {
        getModel().getRuntime().println(ausgabe.getValue());
    }
    
    public void cleanup() {
        getModel().getRuntime().println("CLEANUP");
    }
    
}
