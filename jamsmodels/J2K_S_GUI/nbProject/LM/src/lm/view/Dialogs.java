/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import javax.swing.*;
import lm.view.miniComponets.Tooltipp;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Dialogs {
    
    JDialog jd;

    public void showAbortDialog(String text){
       JFrame frame = new JFrame();
       JOptionPane.showMessageDialog(frame , text);
    }

    public void showNNDialog(){
        JFrame frame =new JFrame();
        JOptionPane.showMessageDialog(frame, "Ihr Eingabe ist keine Zahl");
    }

    public void showNotADateDialog(){
        JFrame frame =new JFrame();
        JOptionPane.showMessageDialog(frame, "Bitte geben sie eine Zahl zwischen 1 und 365 ein");
    }
    
    public void showWrongYear(){
        JFrame frame =new JFrame();
        JOptionPane.showMessageDialog(frame, "Das von Ihnen eingegebene Jahr ist niedriger als vorherige");
    }
    
    public void showCropRotationAdjusted(){
        JFrame frame =new JFrame();
        JOptionPane.showMessageDialog(frame, "Sie haben die Dauer einer ArableID verkleinert die CropRotation wurde angepasst");
    }
    public void showNotAllPathsKorrekt(){
        JFrame frame =new JFrame();
        JOptionPane.showMessageDialog(frame, "Nicht alle Pfade sind korrekt bitte überprüfen Sie Ihre eingaben");
    
    }
    public void showToolTipp(String s){
        jd = new JDialog();
        JTextArea jta = new JTextArea(s);
        jta.setSize(100, 200);
        PointerInfo info = MouseInfo.getPointerInfo();
        jd.setSize(100,200);
        jd.setUndecorated(true);              
        jd.add(jta);
        jd.setLocation(info.getLocation());
        jd.setVisible(true);
    }
    public void closeToolTipp(){
        jd.dispose();
    }

}
