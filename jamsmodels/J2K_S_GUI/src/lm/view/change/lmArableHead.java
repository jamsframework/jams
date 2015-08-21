package lm.view.change;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import lm.Componet.Vector.LMArableVector;
import lm.Componet.Vector.LMCropVector;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class lmArableHead extends JPanel {

    private Constraints c;
    private LMArableVector V;

    private JLabel Head=new JLabel("-");

    private JButton ChangeID=new JButton("Change Crop");



    public lmArableHead(){
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.setLayout(new GridBagLayout());
        createGUI();
    }

    public void set(String s){
        Head.setText(s + "  Prefercenses");

    }

    public void createGUI(){
          c=new Constraints(0,0,5,1);
          c.anchor=GridBagConstraints.NORTH;
          this.add(Head,c);

          c= new Constraints(1,1,5,1);
          c.anchor=GridBagConstraints.CENTER;
          this.add(ChangeID,c);
    }

    public void addChangeIDActionListener(ActionListener l){
        ChangeID.addActionListener(l);
    }

}
