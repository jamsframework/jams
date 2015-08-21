/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class Dialogs {

    public void showAbortDialog(String text){
       JFrame frame = new JFrame();
       JOptionPane.showMessageDialog(frame , text);
    }

}
