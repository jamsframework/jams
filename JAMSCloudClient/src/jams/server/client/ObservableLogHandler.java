/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.server.client;

import java.util.Observable;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class ObservableLogHandler extends Observable{    
    DefaultHandler handler = new DefaultHandler();
    Logger loggers[] = null;
    
    public class DefaultHandler extends Handler{
        
        public DefaultHandler(){            
        }
        
        @Override
        public void publish(LogRecord record) {
            setChanged();
            notifyObservers(record.getMessage());
        }

        @Override
        public void flush() {}

        @Override
        public void close() throws SecurityException {}
    };
    
    public ObservableLogHandler(Logger loggers[]) {     
        this.loggers = loggers;
        for (Logger logger : loggers){
            logger.addHandler(getHandler());
        }        
    }
                
    public Handler getHandler(){        
        return handler;
    }    
    
    public void cleanup(){
        for (Logger log : loggers){
            log.removeHandler(handler);
        }
    }
}
