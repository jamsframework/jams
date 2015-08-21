/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lm.view.changeLMArable;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import lm.Componet.Vector.LMArableID;
import lm.view.Constraints;

/**
 *
 * @author MultiMedia
 */
public class lmArable_fact_head extends JPanel implements Observer {
    
    private Constraints c;
    
    private LMArableID aid;
    
    private JLabel fact_head =new JLabel("Boden Veränderungen");
    
    private JLabel nitrogenAmount = new JLabel("Nitrogen Amount :");
    private JLabel phosphorusAmount = new JLabel("Phosphorus Amount :");
    
    
    
    
    
    public lmArable_fact_head(){
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setLayout(new GridBagLayout());
    }
    
    public void createGUI(){
        
        this.removeAll();
        
        c=new Constraints(0,0,4,1);
        c.fill=GridBagConstraints.HORIZONTAL;
        this.add(fact_head,c);
        
        c=new Constraints(0,1,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(nitrogenAmount,c);
        
        c=new Constraints(1,1,1,1);
        c.anchor=GridBagConstraints.EAST;
        this.add(new JLabel(aid.get_Nitrogen()+""),c);
        
        
        c=new Constraints(2,1,1,1);
        c.anchor=GridBagConstraints.WEST;
        this.add(phosphorusAmount,c);
        
        c=new Constraints(3,1,1,1);
        c.anchor=GridBagConstraints.EAST;
        this.add(new JLabel(aid.get_Phosphorus()+""),c);
        
             
        
        
        this.revalidate();
        this.repaint();
    }
    
    public void addInput(){
        
        
        this.createGUI();
        
        
        
        
        
        
        
        
        this.revalidate();
        this.repaint();
    }
    
    public void setLMArableID(LMArableID aid){
        this.aid=aid;
    }

    public void update(Observable o, Object o1) {
        if(o1 instanceof Integer)
        if(((Integer)o1).equals(1)){
            createGUI();
        }else{
          //  setEmpty();
        }
    }
    
}
