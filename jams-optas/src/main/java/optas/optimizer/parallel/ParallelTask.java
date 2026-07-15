/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.optimizer.parallel;

import java.util.ArrayList;

/**
 *
 * @author chris
 */
public abstract class ParallelTask<X,Y> {
    abstract public ArrayList<ParallelJob> split (X arg, int gridSize);
    abstract public Y reduce (ArrayList<Y> results);
}
