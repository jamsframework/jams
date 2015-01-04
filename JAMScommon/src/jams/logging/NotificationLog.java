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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author christian
 */
public class NotificationLog extends Handler {
    public static NotificationDlg notificationDlg;
    static NotificationLog instance = new NotificationLog();
    
    private NotificationLog(){
        notificationDlg = new NotificationDlg(null, JAMS.i18n("Info"));
    }
    
    public static NotificationLog getInstance(){
        return instance;
    }
    
    @Override
    public void publish(LogRecord record) {
        if (record.getLevel().intValue() > Level.WARNING.intValue()) {
        }
        String[] line = record.getMessage().split("\n");
        String level = JAMS.i18n(record.getLevel().toString());
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
