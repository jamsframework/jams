/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.server.client;

import java.util.Observable;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author christian
 */
public class ObservableLogHandler extends Observable{    
    DefaultHandler handler = new DefaultHandler();
    Logger loggers[] = null;
    Filter filter = null;
    
    long threadID = -1;
    
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
        this(loggers, null);        
    }
    
    public ObservableLogHandler(Logger loggers[], Thread logThread) {     
        this.loggers = loggers;
        for (Logger logger : loggers){
            logger.addHandler(getHandler());
        }
        
        Filter f = new Filter() {

            @Override
            public boolean isLoggable(LogRecord record) {
                if (threadID!=-1 && record.getThreadID() != threadID)
                    return false;
                
                if (filter==null)
                    return true;
                
                return filter.isLoggable(record);
            }
        };
        getHandler().setFilter(f);
    }
    
    public void setLogLevel(Level level){
        for (Logger l : loggers){
            l.setLevel(level);
        }
    }
    
    public void setThreadID(long id){
        this.threadID = id;
    }
    
    public long getThreadID(){
        return threadID;
    }
    
    public void setFilter(Filter newFilter){
        this.filter = newFilter;
    }
    
    public Filter getFilter(){
        return filter;
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
