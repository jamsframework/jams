/*
 * SelectDataDlg.java
 *
 * Created on 1. Oktober 2007, 01:31
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package reg.spreadsheet;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
/**
 *
 * @author Developement
 */
public class SelectDataDlg extends JDialog{
    
    JDialog parent;
    JButton okButton = new JButton("ok");
    JButton cancelButton = new JButton("cancel");
    GraphProperties prop;

    /** Creates a new instance of SelectDataDlg */
    public SelectDataDlg() {
    
    }
    
    public SelectDataDlg(JDialog parent, String name, GraphProperties prop) {
        super(parent, "JAMS CTS Viewer");
        this.parent = parent;
        this.prop = prop;
        
        Point parentloc = parent.getLocation();
        setLocation(parentloc.x + 30, parentloc.y + 30);
        
        okButton.addActionListener(okClicked);
        //this.parent = parent;
        setLayout(new FlowLayout());
        add(okButton);
        add(cancelButton);
        pack();
        
    }
    
    
    ActionListener okClicked = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
                prop.setDataSelection();
                setVisible(false);
                parent.setVisible(true);
                
            } 
    };
    
    ActionListener cancelClicked = new ActionListener(){
         public void actionPerformed(ActionEvent e) {
                setVisible(false);
                parent.setVisible(true);
            } 
    };
    
}
