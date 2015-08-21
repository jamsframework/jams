/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class ActionMenuBar extends JMenuBar {

    private JMenuItem play = new JMenuItem(new ImageIcon("src/lm/resources/images/play_button.gif"));

    public ActionMenuBar(){
        this.add(play);
    }

}
