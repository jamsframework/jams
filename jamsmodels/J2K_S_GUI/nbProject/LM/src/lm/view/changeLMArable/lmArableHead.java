package lm.view.changeLMArable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import lm.Componet.Vector.LMArableID;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMCropVector;
import lm.view.Constraints;
import lm.view.miniComponets.ColorIcon;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class lmArableHead extends JPanel implements Observer {

    private Constraints c;
    private LMArableID lmArableID;
    private ArrayList<String> cidModel;

    private JLabel head=new JLabel("-");

    private JButton ChangeID=new JButton("Change Crop");
    
    private Color color;
    private JButton colorButton=new JButton();
    private JComboBox CIDBox=new JComboBox();



    public lmArableHead(){
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setLayout(new GridBagLayout());
        createGUI();
    }

//    public void set(String s,ArrayList<String> a){
//        this.setCIDBoxModel(a);
//        Head.setText(s + "  Prefercenses");
//
//    }

    public void createGUI(){
          c=new Constraints(0,0,3,1);
          //c.anchor=GridBagConstraints.NORTH;
          this.add(head,c);

          c= new Constraints(0,1,1,1);
          c.anchor=GridBagConstraints.EAST;
          this.add(CIDBox,c);

          c= new Constraints(1,1,1,1);
          c.anchor=GridBagConstraints.WEST;
          this.add(ChangeID,c);
          createAndReBuildButton();
          colorButton.setPreferredSize(new Dimension(100,25));
          c=new Constraints(2,1,1,1);
          this.add(colorButton);


    }

    private DefaultComboBoxModel getCIDBoxModel(ArrayList<String> a){
        DefaultComboBoxModel CIDModel =new DefaultComboBoxModel();
        for(String s :a){
            CIDModel.addElement(s);
        }
        return CIDModel;
    }

    public String getSelectedElement(){
        return (String)CIDBox.getSelectedItem();
    }

    private void setSelectedID(int i){
        for(int p=1;p<CIDBox.getModel().getSize();p++){
            String s=(String)CIDBox.getModel().getElementAt(p);
            s=s.split("\n")[0];
            if(i==Integer.parseInt(s)){
                CIDBox.setSelectedIndex(p);
                break;
            }
        }
    }

    public void createAndReBuildButton(){
        colorButton.setBackground(color);
        colorButton.revalidate();
        //colorButton.repaint();
    }

    public void addChangeIDActionListener(ActionListener l){
        ChangeID.addActionListener(l);
    }
    public void addColorButtonListener(ActionListener l){
        colorButton.addActionListener(l);
    }

    public void removeColorButtonListener(){
        ActionListener[] ters=colorButton.getActionListeners();
        for(ActionListener t : ters){
            colorButton.removeActionListener(t);
        }
    }
    public void ShowAndGetColor(){

        JColorChooser jColorChooser=new JColorChooser(color);
        Color newColor=jColorChooser.showDialog(jColorChooser, null, color);
                if(newColor==null){
                    newColor=color;
                }
        this.setColor(newColor);
        this.createAndReBuildButton();
    }

    public void change(){
        this.setColor(lmArableID.getColor());
        this.createAndReBuildButton();
        this.CIDBox.setModel(getCIDBoxModel(cidModel));
        setSelectedID(lmArableID.getCropID());
        head.setText((String)CIDBox.getSelectedItem());
        this.revalidate();
        this.repaint();
    }

    public Color getColor(){
        return this.color;
    }
    public void setColor(Color color){
        this.color=color;
    }

    public void setEmpty(){
        head.setText("-");
        this.revalidate();
        this.repaint();
    }

    public void setLMArableID(LMArableID id){
        this.lmArableID=id;
        change();
    }
    public void setCIDModel(ArrayList<String>s){
        this.cidModel=s;
    }

    public void update(Observable o, Object o1) {
        if(o1 instanceof Integer)
        if(((Integer)o1).equals(1)){
            change();
        }else{
            setEmpty();
        }
        
    }
}
