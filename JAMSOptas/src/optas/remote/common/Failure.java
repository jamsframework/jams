/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package optas.remote.common;

import optas.remote.common.JAMSCommand;

/**
 *
 * @author chris
 */
public class Failure extends JAMSCommand{
    String errorMessage;
    public Failure(String errorMessage){
        this.setName("Failure Command");
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString(){
        return super.toString() + "message: " + errorMessage;
    }
}
