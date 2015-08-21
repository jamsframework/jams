/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lm.view.changeCR;

import java.awt.GridBagLayout;
import javax.swing.*;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRMaximizeFallowFrame extends JFrame {

    private Panel panel;

    public CRMaximizeFallowFrame(){
        this.setResizable(false);
        this.setTitle("CR - MaximizeFallow");
        panel=new Panel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        panel.createPanel();
        this.pack();
    }




    private class Panel extends JPanel {

        private JComboBox moreYears;
        private JLabel durationAtTheMoment=new JLabel("Momentane Dauer");
        private JLabel newDuration =new JLabel("Neue Dauer");
        private JLabel durationAtTheMoment_value =new JLabel();
        private JLabel newDuration_value =new JLabel();
        private JButton save =new JButton("Save");
        private JButton cancel =new JButton("Cancel");
        private Constraints c;

        public Panel (){

        }

        public void createPanel(){

            this.setLayout(new GridBagLayout());
            c=new Constraints(0,0,2,1);
            this.add(moreYears,c);

        }

    }

}
