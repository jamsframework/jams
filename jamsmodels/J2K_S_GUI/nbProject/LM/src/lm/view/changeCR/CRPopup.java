/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRPopup extends JPopupMenu {

    private JMenuItem delete=new JMenuItem("Delete");
    private JMenuItem setFallow=new JMenuItem("Set As Fallow");
    private JMenuItem eigenschaften =new JMenuItem("Eigenschaften");
    private JMenuItem insertArableID = new JMenuItem("Insert ArableID");
    private JMenuItem insertArableIDRight=new JMenuItem("Insert Arable Right");
    private JMenuItem insertArableIDLeft=new JMenuItem("Insert Arable Left");
    private JMenuItem reduceFallow=new JMenuItem("Reduce Fallow");
    private JMenuItem maximizeFallow=new JMenuItem("Maximize Fallow");

    public CRPopup(){

    }

    public void createArableIDMenu(String s){
        this.removeAll();
        this.add(setFallow);
        this.addSeparator();
        this.add(delete);
        this.addSeparator();
        this.add(eigenschaften);
        this.addSeparator();
        this.add(insertArableIDLeft);
        this.addSeparator();
        this.add(insertArableIDRight);
        setActionCommand(s);
        show();
        
    }
    public void createFallowMenu(String s){
        this.removeAll();
        this.add(insertArableID);
        this.addSeparator();
        this.add(eigenschaften);
        this.addSeparator();
        this.add(reduceFallow);
        this.addSeparator();
        this.add(maximizeFallow);

        setActionCommand(s);
        show();
    }

    public void show(){
        PointerInfo info = MouseInfo.getPointerInfo();

        this.setLocation(info.getLocation());
        this.setVisible(true);

    }

    public void Hide(){
        this.setVisible(false);
    }

    public void setActionCommand(String s){
        setFallow.setActionCommand(s);
        delete.setActionCommand(s);
        eigenschaften.setActionCommand(s);
        insertArableID.setActionCommand(s);
        reduceFallow.setActionCommand(s);
        maximizeFallow.setActionCommand(s);
        insertArableIDLeft.setActionCommand(s);
        insertArableIDRight.setActionCommand(s);
    }


    public void addActionListenerToSetFallow(ActionListener l){
        setFallow.addActionListener(l);
    }
    public void addActionListenerToDelete(ActionListener l){
        delete.addActionListener(l);
    }

    public void addActionListenerToEigenschaften(ActionListener l){
        eigenschaften.addActionListener(l);
    }

    public void addActionListenerToInsertArableID(ActionListener l){
        insertArableID.addActionListener(l);
    }

    public void addActionListenerToReduceFallow(ActionListener l){
        reduceFallow.addActionListener(l);
    }

    public void addActionListenerToMaximizeFallow(ActionListener l){
        maximizeFallow.addActionListener(l);
    }

    public void addActionListenerToInsertArableRight(ActionListener l){
        insertArableIDRight.addActionListener(l);
    }

    public void addActionListenerToInsertArabeLeft(ActionListener l){
        insertArableIDLeft.addActionListener(l);
    }

}
