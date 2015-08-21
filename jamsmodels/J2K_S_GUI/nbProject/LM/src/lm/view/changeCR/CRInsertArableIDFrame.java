/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRInsertArableIDFrame extends JFrame {

    private Panel panel;

    public CRInsertArableIDFrame(){
        this.setResizable(false);
        this.setTitle("CR - Insert ArableID");
        panel=new Panel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        panel.createPanel();
        this.pack();
    }

    public void createAndShowGUI(){
        panel.createPanel();
        this.setVisible(true);
    }

    public void addInsertActionListener(ActionListener l){
        panel.insert.addActionListener(l);
    }
    
    public void addCancelActionListener(ActionListener l){
        panel.cancel.addActionListener(l);
    }

    public String getArableID(){
        return (String)panel.arableIDList.getSelectedValue();
    }

    public void setListModel(DefaultListModel dlm){
        panel.arableIDList.setModel(dlm);
        panel.arableIDList.setSelectedIndex(0);
    }

    public void setActionCommandOK(String s){
        panel.insert.setActionCommand(s);
    }
    
    public void clear(){
        ActionListener[] al=panel.insert.getActionListeners();
        for(ActionListener l : al){
            panel.insert.removeActionListener(l);
        }
        ActionListener[] bl=panel.cancel.getActionListeners();
        for(ActionListener l : bl){
            panel.insert.removeActionListener(l);
        }
    }


    private class Panel extends JPanel{

        private JList arableIDList=new JList();
        private JButton insert=new JButton("Insert");
        private JButton cancel=new JButton("Cancel");

        private Constraints c;
        
        public Panel(){

        }

        public void createPanel(){
            this.removeAll();
            this.setLayout(new GridBagLayout());
            arableIDList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            c=new Constraints(0,0,2,1);
            c.fill=GridBagConstraints.BOTH;
            JScrollPane js=new JScrollPane(arableIDList);
            js.setPreferredSize(new Dimension(200,250));
            this.add(js,c);
            c=new Constraints(0,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(insert,c);
            c=new Constraints(1,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(cancel,c);
        }
    }
}
