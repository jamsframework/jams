/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.components.tools;

import jams.data.JAMSBoolean;
import jams.data.JAMSString;
import jams.model.JAMSComponent;
import jams.model.JAMSVarDescription;

/**
 *
 * @author Christian Fischer
 */
public class TimeStamp extends JAMSComponent{       
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description",
            defaultValue = "1"
            )
            public JAMSBoolean stopInit;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description",
            defaultValue = "1"
            )
            public JAMSBoolean stopRun;
    
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "Description",
            defaultValue = "1"
            )
            public JAMSBoolean stopCleanup;

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
