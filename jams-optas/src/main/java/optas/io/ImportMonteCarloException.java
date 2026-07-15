/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.io;

/**
 *
 * @author christian
 */
public class ImportMonteCarloException extends Exception{
    String errorMsg;
    Throwable parent;
    public ImportMonteCarloException(String exception, Throwable parent){
        this.errorMsg = exception;
        this.parent = parent;
    }
    
    @Override
    public String toString(){
        return errorMsg + "\nUnderlying exception is:" + parent;
    }
}
