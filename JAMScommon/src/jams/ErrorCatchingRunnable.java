/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author christian
 */
public abstract class ErrorCatchingRunnable implements Runnable {
    static final Logger log = Logger.getLogger(ErrorCatchingRunnable.class.getName());
    
    public ErrorCatchingRunnable(){
        JAMSLogging.registerLogger(JAMSLogging.LogOption.Show, log);
    }
    abstract public void safeRun() throws Exception;
    
    @Override
    public void run() {
        try {
            safeRun();
        }catch (Throwable t) {
            //difficult to catch .. 
            if (t.toString().contains("Interrupted")){
                log.log(Level.SEVERE, "Action Canceled by User", t);
            }else{
                log.log(Level.SEVERE, t.getMessage(), t);
            }
        }
    }
}
