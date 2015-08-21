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
import lm.Componet.errors.Ierror_delete_LM_ID;
import lm.view.Constraints;

/**
 *
 * @author MultiMedia
 */
public class Conflictmanager_deleteID extends JFrame{
    
    private Panel panel;
    protected Ierror_delete_LM_ID error;
    
    public Conflictmanager_deleteID(Ierror_delete_LM_ID error){
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
    public Ierror_delete_LM_ID stop(){
        this.setVisible(false);
        return error;
    }    
    
    public void addSaveListener(ActionListener l){
        panel.save.addActionListener(l);
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
        private JButton save = new JButton("Save");
        private JButton cancel = new JButton("Cancel");
        private ArrayList<ArrayList<JRadioButton>> radio =new ArrayList();
        private ArrayList<ButtonGroup> group= new ArrayList();
        
        public Panel(){
           
        }
        
        public void createPanel(){
            this.setLayout(new GridBagLayout());
            c=new Constraints(0,0,4,1);
            c.anchor=GridBagConstraints.CENTER;
            this.add(new JLabel(error.getID()+" soll gelöscht werden"),c);
            
            c=new Constraints(0,1);
            this.add(new JLabel("CR ID"),c);
            
            c=new Constraints(1,1);
            this.add(new JLabel("CR ID Position"),c);
            
            c=new Constraints(2,1);
            this.add(new JLabel("delete"),c);
            
            c=new Constraints(3,1);
            this.add(new JLabel("asFallow"),c);
            int p=2;
            for(int i = 0;i<error.getArrayList().size();i++){
                p=p+i;
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
                
                radio.get(i).get(1).setSelected(true);
                
                c=new Constraints(2,p);
                c.anchor=GridBagConstraints.CENTER;
                this.add(radio.get(i).get(0),c);
                
                c=new Constraints(3,p);
                c.anchor=GridBagConstraints.CENTER;
                this.add(radio.get(i).get(1),c);
                
            }
            
            c=new Constraints(0,p+1,2,1);
            c.fill=GridBagConstraints.BOTH;
            this.add(save,c);
            
            c=new Constraints(2,p+1,2,1);
            c.fill=GridBagConstraints.BOTH;
            this.add(cancel,c);
        }
        
    }
    
}
