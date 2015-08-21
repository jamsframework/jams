/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.tools;

import static jams.tools.StringTools.format;
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
public class LogTools {

    /**
     *
     * @param c calling class
     * @param l level of log
     * @param msg message to be logged with optional placeholders indicated by
     * {i}
     * @param arguments for those placeholders
     */
    public static void log(Class c, Level l, String msg, Object... arguments) {
        Logger.getLogger(c.getName()).log(l, format(msg, arguments));

    }

    /**
     *
     * @param c calling class
     * @param l level of log
     * @param t exception to be logged
     * @param msg message to be logged with optional placeholders indicated by
     * {i}
     * @param arguments for those placeholders
     */
    public static void log(Class c, Level l, Throwable t, String msg, Object... arguments) {
        Logger.getLogger(c.getName()).log(l, format(msg, arguments), t);
    }

    /**
     * log handler that can be observed
     * @author Christian Fischer <christian.fischer.2@uni-jena.de>
     */
    static public class ObservableLogHandler extends Observable {

        DefaultHandler handler = new DefaultHandler();
        Logger loggers[] = null;
        Filter filter = null;

        long threadID = -1;

        /**
         * default handler for this class
         */
        protected class DefaultHandler extends Handler {

            @Override
            public void publish(LogRecord record) {
                setChanged();
                notifyObservers(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        /**
         * creates a ObservableLogHandler 
         * @param loggers that should be observed
         */
        public ObservableLogHandler(Logger... loggers) {
            this(loggers, null);
        }

        /**
         * creates a ObservableLogHandler 
         * @param loggers that should be observed
         * @param logThread only this thread is observed
         */
        public ObservableLogHandler(Logger loggers[], Thread logThread) {
            this.loggers = loggers;            
            for (Logger logger : loggers) {
                logger.addHandler(getHandler());
                logger.setLevel(Level.ALL);
            }
            //filter messages from the selected thread only
            Filter f = new Filter() {

                @Override
                public boolean isLoggable(LogRecord record) {
                    if (threadID != -1 && record.getThreadID() != threadID) {
                        return false;
                    }

                    if (filter == null) {
                        return true;
                    }

                    return filter.isLoggable(record);
                }
            };
            getHandler().setFilter(f);
        }

        /**
         * sets the log level
         * @param level to be set
         */
        public void setLogLevel(Level level) {
            for (Logger l : loggers) {
                l.setLevel(level);
            }
        }

        /**
         * sets the id of the thread from which messages should be logged
         * @param id of the thread
         */
        public void setThreadID(long id) {
            this.threadID = id;
        }

        /**
         *
         * @return the id of the thread
         */
        public long getThreadID() {
            return threadID;
        }

        /**
         * sets an additional logging filter
         * @param newFilter to be set
         */
        public void setFilter(Filter newFilter) {
            this.filter = newFilter;
        }

        /**
         *
         * @return the additional filter or null if there is no filter set
         */
        public Filter getFilter() {
            return filter;
        }

        /**
         *
         * @return
         */
        public Handler getHandler() {
            return handler;
        }

        /**
         * removes all handlers
         */
        public void cleanup() {
            for (Logger log : loggers) {
                log.removeHandler(handler);
            }
        }
    }

}
