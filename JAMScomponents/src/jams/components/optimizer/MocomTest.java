/*
 * VolumeError.java
 *
 * Created on 23. Mai 2006, 09:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jams.components.optimizer;


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
public class MocomTest extends JAMSComponent {
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSDouble x1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSDouble x2;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSDouble y1;
    
    @JAMSVarDescription(
    access = JAMSVarDescription.AccessType.WRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "List of parameter identifiers to be sampled"
            )
            public JAMSDouble y2;
               
    public void run(){
	y1.setValue(x1.getValue()*x1.getValue() + 
			    x2.getValue()*x2.getValue());
	
	y2.setValue(x2.getValue()*x2.getValue() + 
			    (x1.getValue()-1)*(x1.getValue()-1));
    }
    
}
