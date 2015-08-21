/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import lm.Componet.Vector.LMCropRotationVector;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CropIDFrame extends JFrame {
    
    private Panel panel;


    public CropIDFrame(){
       // this.setResizable(false);
        this.setTitle("CropID -- Detail Frame");
        panel=new Panel();
        JPanel test=new JPanel();
        test.setLayout(new GridLayout(1,1));
        test.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        test.add(panel);
        this.setContentPane(test);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        //panel.createPanel();
        this.pack();
    }

    public void createAndShowGUI(LMCropRotationVector cropRotationVector){
        panel.update(cropRotationVector);
        this.setVisible(true);
    }

    public void addActionListenerToDelete(ActionListener l){
        ActionListener []a=panel.delete.getActionListeners();
        for(ActionListener c :a){
            panel.delete.removeActionListener(c);
        }
        panel.delete.addActionListener(l);
    }

    public void setActionCommandToDelete(String action){
        panel.delete.setActionCommand(action);
    }




    private class Panel extends JPanel{

        private LMCropRotationVector crv=null;
        private JButton delete=new JButton("delete");
        private Constraints c;


        public Panel(){
            this.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
           this.setPreferredSize(new Dimension(200,300));
        }

        public void createPanel(){
            this.setLayout(new GridBagLayout());
            c=new Constraints(0,0);
            c.anchor=GridBagConstraints.EAST;
            this.add(new JLabel("Dauer :"),c);
            c=new Constraints(1,0);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel(crv.getLastDay()+""),c);
            c=new Constraints(0,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(new JLabel("Genutzte Zeit :"),c);
            c=new Constraints(1,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel(crv.getUseDays()+""),c);
            c=new Constraints(0,2);
            c.anchor=GridBagConstraints.EAST;
            this.add(new JLabel("Ungenutzte Zeit :"),c);
            c=new Constraints(1,2);
            c.anchor=GridBagConstraints.WEST;
            this.add(new JLabel(crv.getFallowDays()+""),c);
            int i=3;
            if(!crv.getArableUses().isEmpty()){
                
                Integer key=crv.getArableUses().firstKey();
                while(key!=null){
                    c=new Constraints(0,i);
                    c.anchor=GridBagConstraints.EAST;
                    this.add(new JLabel("ArableID" + key),c);
                    c=new Constraints(1,i);
                    c.anchor=GridBagConstraints.WEST;
                    this.add(new JLabel(crv.getArableUses().get(key)+"D"),c);
                    i++;
                    key=crv.getArableUses().higherKey(key);
                }
            }
            c=new Constraints(0,i,2,1);
            c.fill=GridBagConstraints.HORIZONTAL;
            this.add(delete,c);

        }

        public void update(LMCropRotationVector crv){
            this.removeAll();
            this.crv=crv;
            createPanel();
        }


    }
}
