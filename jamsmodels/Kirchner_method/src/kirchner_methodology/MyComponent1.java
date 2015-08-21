package kirchner_methodology;

/**
 * JAMS example component - can be used as template for new components
 */

import jams.data.*;
import jams.model.*;

/**
 *
 * @author John Doe
 */
 @JAMSComponentDescription(
    title="Title",
    author="Author",
    description="Description",
    date = "YYYY-MM-DD",
    version = "1.0_0"
)
@VersionComments(entries = {
    @VersionComments.Entry(version = "1.0_0", comment = "Initial version"),
    @VersionComments.Entry(version = "1.0_1", comment = "Some improvements")
})        
public class MyComponent1 extends JAMSComponent {

    /*
     *  Component attributes
     */
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,       // type of access, i.e. READ, WRITE, READWRITE
            description = "Description",                       // description of purpose
            defaultValue = "42",                                // default value, defaults to "%NULL%"
            unit = "l/m²",                                     // unit of this var if numeric, defaults to ""
            lowerBound = 0,                                    // lowest allowed value of var if numeric, defaults to "0"
            upperBound = 1000,                                 // highest allowed value of var if numeric, defaults to "0"
            length = 0                                         // length of variable if string, defaults to "0"            
            )
            public Attribute.Double[] attribName;                // for a list of attribute types, see jams.data.Attribute  

    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ         
            )
            public Attribute.Calendar time;   
    /*
     *  Component run stages
     */
    
    @Override
    public void init() {
        
    }

    @Override
    public void run() {
        getModel().getRuntime().println("Hello World: " + attribName[0].getValue() + " " + time.getValue());
    }

    @Override
    public void cleanup() {
    }
}