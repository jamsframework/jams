/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.view.Conflict;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import lm.Componet.errors.Ierror_change_duration_LM_ID;
import lm.view.Constraints;

/**
 *
 * @author MultiMedia
 */
public class Conflictmanager_changeduration extends JFrame {
    
    private Panel panel;
    protected Ierror_change_duration_LM_ID error;
    
    public Conflictmanager_changeduration (Ierror_change_duration_LM_ID error){
        this.error=error;
        this.setResizable(false);    
        this.setTitle("Delete LM ID");
        panel=new Panel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        panel.createPanel();
        this.pack();
    }
    
    public void start(){
        this.setVisible(true);
    }
    
    public Ierror_change_duration_LM_ID stop(){
        this.setVisible(false);
        return error;
    }
    
    public void addDeleteListener(ActionListener l){
        panel.delete.addActionListener(l);
    }
    public void addCancelListener(ActionListener l){
        panel.cancel.addActionListener(l);
    }
    public void addRadioListener(ActionListener l){
        for(int i=0;i<panel.radio.size();i++){
            for(int p=0;p<panel.radio.get(i).size();p++){
                panel.radio.get(i).get(p).addActionListener(l);
            }
        }
    }
    
    
        public class Panel extends JPanel{
            
            private Constraints c;
            private JButton delete = new JButton("delete");
            private JButton cancel = new JButton("cancel");
            private ArrayList<ArrayList<JRadioButton>> radio =new ArrayList();
            private ArrayList<ButtonGroup> group= new ArrayList();
            
            public Panel(){
                
            }
            
            public void createPanel(){
                
                this.setLayout(new GridBagLayout());
                
                c=new Constraints(0,0,5,1);
                c.anchor=GridBagConstraints.CENTER;
                this.add(new JLabel("Bei LMArableID " + error.getID()+" soll die Länge geändert werden"),c);
                
                c=new Constraints(0,1);
                this.add(new JLabel("CR ID"),c);
            
                c=new Constraints(1,1);
                this.add(new JLabel("Position"),c);
                
                c=new Constraints(2,1);
                this.add(new JLabel("asFallow"),c);
            
                c=new Constraints(3,1);
                this.add(new JLabel("delete"),c);
            
                c=new Constraints(4,1);
                this.add(new JLabel("expand"),c);
                
                int p=2;
                for(int i=0;i<error.getArrayList().size();i++){
                    p=p+i;
                    if(Integer.valueOf(error.getDecision().get(i))!=-1){
                    c= new Constraints(0,p);
                    this.add(new JLabel(error.getArrayListElement(i).split("/")[0]+""),c);
                    c= new Constraints(1,p);
                    this.add(new JLabel(error.getArrayListElement(i).split("/")[1]+""),c);
                    
                    radio.add(new ArrayList<JRadioButton>());
                    radio.get(i).add(new JRadioButton());
                    radio.get(i).add(new JRadioButton());
                    radio.get(i).get(0).setActionCommand(i+"/"+0);
                    radio.get(i).get(1).setActionCommand(i+"/"+1);
                    group.add(new ButtonGroup());
                    group.get(i).add(radio.get(i).get(0));
                    group.get(i).add(radio.get(i).get(1));
                    
                    System.out.println((Boolean)error.getPossible().get(i));
                    
                    if((Boolean)error.getPossible().get(i)){
                        System.out.println("neuer Expand Button");
                        radio.get(i).add(new JRadioButton());
                        radio.get(i).get(2).setActionCommand(i+"/"+2);
                        group.get(i).add(radio.get(i).get(2));
                        radio.get(i).get(2).setSelected(true);
                        c=new Constraints(4,p);
                        c.anchor=GridBagConstraints.CENTER;
                        this.add(radio.get(i).get(2),c);
                    }else{
                        radio.get(i).get(1).setSelected(true);
                    }
                    
                    c=new Constraints(2,p);
                    c.anchor=GridBagConstraints.CENTER;
                    this.add(radio.get(i).get(0),c);
                
                    c=new Constraints(3,p);
                    c.anchor=GridBagConstraints.CENTER;
                    this.add(radio.get(i).get(1),c);
                    }
                }
                
                c=new Constraints(0,p+1,2,1);
                c.fill=GridBagConstraints.BOTH;
                this.add(delete,c);
            
                c=new Constraints(2,p+1,2,1);
                c.fill=GridBagConstraints.BOTH;
                this.add(cancel,c);
            }
        }
}
