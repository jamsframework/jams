package jams.logging;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import jams.JAMS;
import jams.JAMSException;
import jams.gui.input.NotificationDlg;
import jams.tools.StringTools;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author christian
 */
public class NotificationLogHandler extends Handler {
    public static NotificationDlg notificationDlg;
    static NotificationLogHandler instance = new NotificationLogHandler();
    private static Map<Level, String> msgHeaderTitle;
    
    private NotificationLogHandler(){
        notificationDlg = new NotificationDlg(null, JAMS.i18n("Info"));
        msgHeaderTitle = new HashMap();
        msgHeaderTitle.put(Level.OFF, "");
        msgHeaderTitle.put(Level.ALL, "General_information");
        msgHeaderTitle.put(Level.CONFIG, "Configuration");
        msgHeaderTitle.put(Level.FINE, "Information");
        msgHeaderTitle.put(Level.FINER, "Information");
        msgHeaderTitle.put(Level.FINEST, "Information");
        msgHeaderTitle.put(Level.INFO, "Information");
        msgHeaderTitle.put(Level.SEVERE, "Error");
        msgHeaderTitle.put(Level.WARNING, "Warning");        
    }
    
    public static NotificationLogHandler getInstance(){
        return instance;
    }
    
//    public void setParent(Frame parent) {
//        notificationDlg = new NotificationDlg(parent, JAMS.i18n("Info"));
//    }
    
    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() > Level.WARNING.intValue()) {
        }
        String[] line = record.getMessage().split("\n");
        String level = JAMS.i18n(msgHeaderTitle.get(record.getLevel()));
        String msg = level + ": " + line[0];
        for (int i = 1; i < line.length; i++) {
            msg += "\n" + String.format("%0" + level.length() + "d", 0).replace("0", " ") + line[i];
        }
        if (record.getLevel() == Level.SEVERE && record.getThrown() != null && !(record.getThrown() instanceof JAMSException)) {
            msg += "\n" + record.getThrown().toString();
            msg += "\n" + StringTools.getStackTraceString(record.getThrown().getStackTrace());
        }
        notificationDlg.addNotification(msg + "\n\n");
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
