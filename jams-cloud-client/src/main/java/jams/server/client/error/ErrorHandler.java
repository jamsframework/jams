/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.error;

/**
 *
 * @author christian
 */
public interface ErrorHandler<T> {
    /**
     * handles an error during a processing operaton
     *
     * @param o : object affected by the error
     * @param ex : exception describing the error
     * @return true is processing should be continued when possible
     *         false aborts execution
     */
    boolean handleError(T o, Throwable ex);
}
