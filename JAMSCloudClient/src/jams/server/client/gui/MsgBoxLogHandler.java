/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.server.client.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author christian
 */
public class MsgBoxLogHandler extends Handler {

    private static final HashSet<Logger> loggers = new HashSet<>();
    private static final HashSet<Level> msgDialogHandling = new HashSet<>();
    private static final HashMap<Level, String> msgDialogTitle = new HashMap<>();
    private static final HashMap<Level, Integer> msgDialogIcon = new HashMap<>();

    static final MsgBoxLogHandler instance = new MsgBoxLogHandler();
    
    private MsgBoxLogHandler() {
        msgDialogHandling.add(Level.INFO);
        msgDialogHandling.add(Level.SEVERE);
        msgDialogHandling.add(Level.WARNING);
        msgDialogTitle.put(Level.OFF, "");
        msgDialogTitle.put(Level.ALL, "General information");
        msgDialogTitle.put(Level.CONFIG, "Configuration");
        msgDialogTitle.put(Level.FINE, "Information");
        msgDialogTitle.put(Level.FINER, "Information");
        msgDialogTitle.put(Level.FINEST, "Information");
        msgDialogTitle.put(Level.INFO, "Information");
        msgDialogTitle.put(Level.SEVERE, "Error");
        msgDialogTitle.put(Level.WARNING, "Warning");

        msgDialogIcon.put(Level.OFF, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.ALL, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.CONFIG, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.FINE, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.FINER, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.FINEST, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.INFO, JOptionPane.INFORMATION_MESSAGE);
        msgDialogIcon.put(Level.SEVERE, JOptionPane.ERROR_MESSAGE);
        msgDialogIcon.put(Level.WARNING, JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void publish(LogRecord record) {
        if (record.getThrown() != null) {
            return;
        }
        if (msgDialogHandling.contains(record.getLevel())) {
            JOptionPane.showMessageDialog(null, record.getMessage(),
                    msgDialogTitle.get(record.getLevel()),
                    msgDialogIcon.get(record.getLevel()));
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    public static void setLogState(Level lvl, boolean state) {
        if (state) {
            msgDialogHandling.add(lvl);
        } else {
            msgDialogHandling.remove(lvl);
        }
    }

    public static void clear(){
        for (Logger logger : loggers){
            unregisterLogger(logger);
        }
    }
    
    public static void unregisterLogger(Logger logger){
        logger.removeHandler(instance);
        loggers.remove(logger);
    }
    
    public static void registerLogger(Logger logger){
        loggers.add(logger);
        //make sure handler is only registered once
        logger.removeHandler(instance);
        logger.addHandler(instance);
    }
    
    
}
