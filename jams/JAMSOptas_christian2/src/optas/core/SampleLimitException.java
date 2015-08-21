/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.core;

/**
 *
 * @author chris
 */
public class SampleLimitException extends Exception {

    String msg;

    public SampleLimitException() {
        msg = "Optimization finished, because the maximum number of samples was reached";
    }

    public SampleLimitException(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }
}
