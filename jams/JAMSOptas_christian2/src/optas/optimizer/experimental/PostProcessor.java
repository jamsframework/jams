/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.experimental;

import jams.model.*;
import jams.data.*;
/**
 *
 * @author Christian Fischer
 */
@JAMSComponentDescription(
        title="PostProcessor",
        author="Christian Fischer",
        description="transforms efficiency "
        )
public class PostProcessor extends JAMSComponent {
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READWRITE,
            update = JAMSVarDescription.UpdateType.RUN,
            description = "function y"            
            )
            public JAMSDouble yVal;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "transform type"
            )
            public JAMSInteger type;
    @JAMSVarDescription(
            access = JAMSVarDescription.AccessType.READ,
            update = JAMSVarDescription.UpdateType.INIT,
            description = "transform type"
            )
            public JAMSDouble shift;   
    
    public void cleanup() {
        if (shift != null){
            yVal.setValue(yVal.getValue()+shift.getValue());
        }
        if (type.getValue() == 1){
            double y = yVal.getValue();
            boolean negative = false;
            if (y < 0.0){
                y = -y;
                negative = true;
            }
            y = Math.log(y);
            
            yVal.setValue(y);
            
        }
        if (type.getValue() == 2){
            boolean negative = false;
            double y = yVal.getValue();
            if (y < 0.0){
                y = -y;
                negative = true;
            }
            y = Math.sqrt(y);
            if (negative)
                y = -y;
            yVal.setValue(y);
        }
    }
}  
