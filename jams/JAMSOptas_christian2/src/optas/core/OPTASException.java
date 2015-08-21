/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optas.core;

/**
 *
 * @author Christian Fischer - chr
 */
public class OPTASException extends RuntimeException {

    public OPTASException(String msg) {        
        super(msg);
    }
    
    public OPTASException(String msg, Exception underlyingException) {        
        super(msg, underlyingException);
    }

    @Override
    public String toString() {
        return getMessage();
    }

}
