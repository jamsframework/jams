/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.data.time.api;

/**
 *
 * @author Christian Fischer - christian.fischer.2@uni-jena.de
 * @param <T>
 */
public interface ComparableTimeSerie<T extends Comparable> extends TimeSerie<T> {

    public int getArgMin();
    public int getArgMax();
    public T getMin();
    public T getMax();
}
