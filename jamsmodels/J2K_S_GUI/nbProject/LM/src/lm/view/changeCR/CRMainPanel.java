/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRMainPanel extends JPanel {

    private CRPanel panel;
    private CRHead head;
    private JButton save=new JButton("Save");
    private JButton cancel=new JButton("Cancel");

    private Constraints c;

    public CRMainPanel (){
        this.setPreferredSize(new Dimension(1030,600));
        panel=new CRPanel();
        head=new CRHead();
        head.createAndShowGUI();
    }
    public void createAndShowGUI(){
        panel.createAndShowGUI();
        this.setLayout(new GridBagLayout());
        c=new Constraints(0,0,3,1);
        c.fill=GridBagConstraints.BOTH;
        this.add(getHead(),c);
        JScrollPane JS=new JScrollPane(panel);
        JS.setMinimumSize(new Dimension(1000,500));
        c=new Constraints(0,1,3,1);
        c.fill=GridBagConstraints.BOTH;
        this.add(JS,c);
        save.setPreferredSize(cancel.getPreferredSize());
        c=new Constraints(1,2,1,1,0,0);
        c.anchor=GridBagConstraints.EAST;
        this.add(save,c);
        c=new Constraints(2,2,1,1,0,0);
        c.anchor=GridBagConstraints.EAST;
        this.add(cancel,c);
        this.repaint();
    }

    public void addActionListenerToSave(ActionListener l){
        save.addActionListener(l);
    }

    public void addActionListenerToCancel(ActionListener l){
        cancel.addActionListener(l);
    }

    public void clearRowList(){
        panel.removeAll();
        panel.clearRowList();
    }
    /**
     * @return the panel
     */
    public CRPanel getPanel() {
        return panel;
    }

    /**
     * @return the head
     */
    public CRHead getHead() {
        return head;
    }

}
