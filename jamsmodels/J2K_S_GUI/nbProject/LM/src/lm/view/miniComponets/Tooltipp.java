/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.view.miniComponets;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import javax.swing.*;

/**
 *
 * @author MultiMedia
 */
public class Tooltipp extends JDialog {
    
    
    
    public Tooltipp (String s){
        this.setSize(100,200);
        this.setUndecorated(true);
        this.removeAll();
        JTextArea jta = new JTextArea(s);
        PointerInfo info = MouseInfo.getPointerInfo();
        jta.setSize(100, 200);
        this.add(jta);
        this.setLocation(info.getLocation());
    }
    
    public void start(){
        this.setVisible(true);      
    }
    public void close(){
        this.close();
    }
}
