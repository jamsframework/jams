/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.tools;

import jams.data.*;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author Christian Fischer
 */
public class TimeStamp extends JAMSComponent{       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description",
            defaultValue = "1"
            )
            public Attribute.Boolean stopInit;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description",
            defaultValue = "1"
            )
            public Attribute.Boolean stopRun;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            description = "Description",
            defaultValue = "1"
            )
            public Attribute.Boolean stopCleanup;

    @Override
    public void init(){        
        if (stopInit.getValue())
            this.getModel().getRuntime().println(this.getInstanceName() + "\tinitTime:" + System.currentTimeMillis());
    }
    @Override
    public void run(){    
        if (stopRun.getValue())
            this.getModel().getRuntime().println(this.getInstanceName() + "\trunTime:" + System.currentTimeMillis());
    }
    @Override
    public void cleanup(){  
        if (stopCleanup.getValue())
            this.getModel().getRuntime().println(this.getInstanceName() + "\tfinishTime:" + System.currentTimeMillis());
    }
}
