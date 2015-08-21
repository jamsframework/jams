/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lm.Componet.Vector.LMCropRotationElement;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRRowPanel extends JPanel {

    private Integer iD;
    private ViewVector viewVector;
    private JButton jButton;

    private Constraints c;


    public CRRowPanel(Integer i){
        iD=i;
        viewVector=new ViewVector();
       // this.setPreferredSize(new Dimension(100,20));
    }

    public void addButton(LMCropRotationElement cropRotationElement,Color color,int zoom){
        viewVector.addButton(cropRotationElement,color,zoom);
    }
    public void addButton(Integer key){
        viewVector.addLastButton(key);

    }

    public void buttonsToView(){
        this.setLayout(new GridBagLayout());
        c=new Constraints(0,0);
        c.anchor=GridBagConstraints.WEST;
        //c.deleteInsets();
        jButton=new JButton(iD + "");
        jButton.setPreferredSize(new Dimension(70,20));
        jButton.setActionCommand(iD + "");
        this.add(jButton,c);
        for(int i=0;i<viewVector.size();i++){
            c=new Constraints(i+1,0);
            c.anchor=GridBagConstraints.WEST;
            c.deleteInsets();
            if(!viewVector.isLast(i)){
                c.weightx=(viewVector.getLastDay()/viewVector.get(i).getDuration());
            }
            c.fill=GridBagConstraints.VERTICAL;
            this.add(viewVector.get(i),c);
        }
    }
    public void addDetailListener(ActionListener l){
        for(int i=0;i<viewVector.size();i++){
            viewVector.get(i).addActionListener(l);
        }
    }
    public void addCropIDListener(ActionListener l){
        jButton.addActionListener(l);
    }
    
}
