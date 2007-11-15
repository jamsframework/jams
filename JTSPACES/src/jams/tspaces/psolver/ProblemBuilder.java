/*
 * ProblemBuilder.java
 *
 * Created on 2. Oktober 2007, 09:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspaces.psolver;
import jams.tspaces.*;
import org.unijena.jams.data.*;

/**
 *
 * @author ncb
 */
public class ProblemBuilder {
    InitSpace initSpace;
    SpaceTools spaceTools;
    /** Creates a new instance of ProblemBuilder ,
     *  reads context of the model and derives worker problems to the space
     */
    public ProblemBuilder() {
    }
    
    /**
     * @param args the command line arguments
     */
    public void init(JAMSString jmc){
        initSpace = new InitSpace(jmc.toString());
       // spaceTools = new SpaceTools(new JAMSString(initSpace.tupleSpaceName),new JAMSString(initSpace.host));
        
    }
    public static void main(String[] args) {
        // TODO code application logic here
        ProblemBuilder problemBuilder = new ProblemBuilder();
        String argument = "C:\\geoinformatik\\j2000\\modeldata\\TSpacesABC\\default.jmc";
        problemBuilder.init(new JAMSString(argument));
        problemBuilder.spaceTools.setString(new JAMSString("ProblemBuilder"),new JAMSString("start"));
        System.out.println(problemBuilder.spaceTools.readString(new JAMSString("ProblemBuilder")));
        System.out.println("Done.");
    }
    
}
