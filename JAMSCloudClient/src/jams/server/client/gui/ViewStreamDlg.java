/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jams.server.client.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author christian
 */
public class ViewStreamDlg extends JDialog{
    JTextArea text = new JTextArea(30, 80);
    JScrollPane pane = null;
    Thread t = null;
    
    final double LOOK_AHEAD = 1.2;
    BufferedReader reader = null;
    
    int currentView = 0;
    int currentRead = 0;
    
    /**
     *
     * @param owner
     * @param is
     * @param title
     */
    public ViewStreamDlg(Dialog owner, InputStream is, String title){
        super(owner);
        this.setTitle(title);
        this.reader = new BufferedReader(new InputStreamReader(is));
        
        JPanel mainPanel = new JPanel(new BorderLayout());        
        pane = new JScrollPane(text);
        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        pane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (e.getValueIsAdjusting()) 
                    return;
                
                int endIndex = 0;
                if (text.getText().length() > 0) {
                    JViewport viewport = pane.getViewport();//(ScrollBar) e.getSource();
                    Rectangle viewRect = viewport.getViewRect();

                    Point p = viewRect.getLocation();
                    int startIndex = text.viewToModel(p);

                    p.x += viewRect.width;
                    p.y += viewRect.height;
                    try{
                        endIndex = text.getLineOfOffset(text.viewToModel(p));
                    }catch(BadLocationException ble){
                        endIndex = 0;                               
                    }
                }
                
                if (endIndex + LOOK_AHEAD > currentView){
                    currentView = (int)(endIndex*LOOK_AHEAD) + 20;
                    fillBuffer();                    
                                        
                    pane.getVerticalScrollBar().getModel().setValue(e.getValue());
                }                
            };
        });
        DefaultCaret caret = (DefaultCaret)text.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        mainPanel.add(pane, BorderLayout.CENTER);
        mainPanel.add(new JButton(new AbstractAction ("OK"){
            @Override
            public void actionPerformed(ActionEvent e){
                ViewStreamDlg.this.setVisible(false);
            }
        }),BorderLayout.SOUTH);      
        add(mainPanel);
    }
    
    @Override
    public void setVisible(boolean isShown){                        
        if (isShown) {
            pack();
            currentView = (int)(text.getLineCount()*LOOK_AHEAD) + 20;
            fillBuffer();

            Runnable r = new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(1000);
                            fillBuffer();
                        } catch (Exception e) {

                        }
                    }
                }
            };
            t = new Thread(r);
            t.start();            
        }else{
            if (t != null){
                if (t.isAlive()){
                    t.stop();
                }
            }            
            close();
        }        
        super.setVisible(isShown);
    }
    
    /**
     *
     * @return
     */
    public String next(){
        try{
            String s = reader.readLine();
            if (s!=null)
                currentRead++;
            return s;
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        return null;        
    }    

    /**
     *
     */
    public void fillBuffer(){
        String s = null;
        while (currentRead < currentView && (s = next()) != null)
            text.append(s + "\n");        
    }
    
    private void close(){
        try{
            reader.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
