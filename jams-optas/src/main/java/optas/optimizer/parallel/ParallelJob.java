/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.parallel;

import java.io.File;
import java.io.Serializable;

/**
 *
 * @author chris
 */
public abstract class ParallelJob<X,Y> implements Serializable{
    public X arg;
    public ParallelJob(X arg){
        this.arg = arg;
    }
    public abstract void moveWorkspace(File newWorkspace);
    public abstract Y execute();
}
