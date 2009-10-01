/*
 * restartTupleSpace.java
 *
 * Created on 24. Mai 2007, 09:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.tspaces;

import org.unijena.jams.data.JAMSString;

/**
 *
 * @author ncb
 */
public class restartTupleSpace {
    
    /** Creates a new instance of restartTupleSpace */
    public restartTupleSpace() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JAMSString modelerKey = new JAMSString(args[0]);
        JAMSString tSpaceIP = new JAMSString(args[1]);
               
        SpaceTools spaceTools = new SpaceTools(modelerKey,tSpaceIP);
        spaceTools.deleteSpace();
        // set implizit spaceTools.monitor key
        spaceTools.setMonitor();
        for (int i=1;i<spaceTools.validMonitors.length;i++){
            spaceTools.setMonitor(spaceTools.validMonitors[i]);
           
        }
        
        
    }
    
}
