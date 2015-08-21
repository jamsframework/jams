package lm.view.changeCR;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lm.Componet.Vector.LMCropRotationElement;
import lm.view.Constraints;

/**
 *
 * @author Jens Wipprich ==> jens.wipprich (at) uni-jena.de
 */
public class CRDetailFrame extends JFrame {

    private Panel panel;
    
    public CRDetailFrame(){
        this.setResizable(false);
        this.setTitle("CR - Detail Frame");
        panel=new Panel();
        this.setContentPane(panel);
        this.setDefaultCloseOperation(HIDE_ON_CLOSE);
        panel.createPanel();
        this.pack();
    }
    
    public void createAndShowGUI(LMCropRotationElement cropRotationElement){
        panel.updatePanel(cropRotationElement);
        this.setVisible(true);
    }


    private class Panel extends JPanel{
        private JLabel arableIDLabel=new JLabel("ArableID :");
        private JLabel beginLabel=new JLabel("Begin :");
        private JLabel endLabel=new JLabel("End :");
        private JLabel absolutBeginLabel=new JLabel("Absoluter Begin :");
        private JLabel absolutEndLabel=new JLabel("Absolutes Ende :");
        private JLabel durationLabel=new JLabel("Dauer :");
        private JLabel arableIDField=new JLabel("-");
        private JLabel beginField=new JLabel("-");
        private JLabel endField=new JLabel("-");
        private JLabel absolutBeginField=new JLabel("-");
        private JLabel absolutEndField=new JLabel("-");
        private JLabel durationField=new JLabel("-");

        private Constraints c;


        public Panel(){
            this.setPreferredSize(new Dimension(200,150));
            this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        }

        public void createPanel(){
            this.setLayout(new GridBagLayout());
            c=new Constraints(0,0,2,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(arableIDLabel,c);
            c=new Constraints(0,1,2,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(beginLabel,c);
            c=new Constraints(0,2,2,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(endLabel,c);
            c=new Constraints(0,3,2,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(absolutBeginLabel,c);
            c=new Constraints(0,4,2,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(absolutEndLabel,c);
            c=new Constraints(0,5,2,1);
            c.anchor=GridBagConstraints.EAST;
            this.add(durationLabel,c);
            c=new Constraints(2,0,2,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(arableIDField,c);
            c=new Constraints(2,1,2,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(beginField,c);
            c=new Constraints(2,2,2,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(endField,c);
            c=new Constraints(2,3,2,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(absolutBeginField,c);
            c=new Constraints(2,4,2,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(absolutEndField,c);
            c=new Constraints(2,5,2,1);
            c.anchor=GridBagConstraints.WEST;
            this.add(durationField,c);
        }

        public void updatePanel(LMCropRotationElement cropRotationElement){
            arableIDField.setText(cropRotationElement.getArableID() +"");
            beginField.setText(cropRotationElement.getBegin()+"");
            endField.setText(cropRotationElement.getEnd()+"");
            if(cropRotationElement.getAbsolutBegin()%365==0){
                absolutBeginField.setText((cropRotationElement.getAbsolutBegin()/365)-1 +"Y " + "365 D");
            }else{
                absolutBeginField.setText(cropRotationElement.getAbsolutBegin()/365+"Y " + cropRotationElement.getAbsolutBegin()%365 + "D");
            }

            if(cropRotationElement.getAbsolutEnd()%365==0){
                absolutEndField.setText((cropRotationElement.getAbsolutEnd()/365)-1 +"Y " + "365D");
            }else{
                absolutEndField.setText(cropRotationElement.getAbsolutEnd()/365+"Y " + cropRotationElement.getAbsolutEnd()%365 + "D");
            }
            
            durationField.setText(cropRotationElement.getDuration()+"D");
            this.repaint();

        }

    }
}
