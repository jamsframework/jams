/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jams.logging;
import java.awt.Frame;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.JOptionPane;

/**
 *
 * @author christian
 */
public class MsgBoxLogHandler extends Handler {

    private static final HashSet<Level> msgDialogHandling = new HashSet<Level>();
    private static final HashMap<Level, String> msgDialogTitle = new HashMap<Level, String>();
    private static final HashMap<Level, Integer> msgDialogIcon = new HashMap<Level, Integer>();
    private Frame parent;

    static final MsgBoxLogHandler instance = new MsgBoxLogHandler();

    static public MsgBoxLogHandler getInstance(){
        return instance;
    }
    
//    public void setParent(Frame parent) {
//        this.parent = parent;
//    }        
    
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
        /*if (record.getThrown() != null) {
            return;
        }*/
        if (msgDialogHandling.contains(record.getLevel())) {                        
            if (record.getThrown() != null){               
                StringWriter w = new StringWriter();
                if (record.getThrown().getCause() != null) {
                    record.setThrown(record.getThrown().getCause());
                }
                record.getThrown().printStackTrace(new PrintWriter(w));
            
                MessageBoxWithDetailsDlg.showMessageBoxWithDetails(
                        parent, 
                        record.getMessage() == null ? record.getThrown().toString() : record.getMessage(),
                        msgDialogTitle.get(record.getLevel()),
                        w.toString(),
                        msgDialogIcon.get(record.getLevel()));
                try{
                    w.close();
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }else{
                MessageBoxWithDetailsDlg.showMessageBoxWithDetails(
                        parent, record.getMessage(),
                        msgDialogTitle.get(record.getLevel()),
                        null,
                        msgDialogIcon.get(record.getLevel()));
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }    
}
