/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class MainContentPanel extends JPanel {

    private ContentPanel panel = new ContentPanel();
   // private ActionMenuBar actionmenubar = new ActionMenuBar(); vorübergehened auf Eis gelegt

    public MainContentPanel(){
        this.setPreferredSize(new Dimension(500,500));
        this.setLayout(new GridLayout(1,1));
        this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
       // this.add(actionmenubar); vorübergehened auf Eis gelegt
        
        this.add(panel);
        }

    public ContentPanel getContentPane(){
        return panel;
    }


}
