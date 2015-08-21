/*
 * VolumeError.java
 *
 * Created on 23. Mai 2006, 09:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.unijena.j2k.testFunctions;


import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.*;

import java.util.StringTokenizer;
import jams.data.*;
import jams.io.GenericDataWriter;
import jams.model.*;
import java.util.Arrays.*;

/**
 *
 * @author c0krpe
 */
public class Test extends JAMSContext {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.Double x1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.Double x2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.Double y1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            description = "List of parameter identifiers to be sampled"
            )
            public Attribute.Double y2;
               
    public void run(){
	y1.setValue(x1.getValue()*x1.getValue() + 
			    x2.getValue()*x2.getValue());
	
	y2.setValue(x1.getValue()*x1.getValue() + 
			    (x2.getValue()-1)*(x2.getValue()-1));
    }
    
}
